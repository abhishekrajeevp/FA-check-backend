package com.bp.middleware.signdesk;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bp.middleware.erroridentifier.ErrorIdentifierService;
import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.payment.PaymentRepository;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.smartrouteverification.CommonResponseStructure;
import com.bp.middleware.smartrouteverification.SmartRouteUtils;
import com.bp.middleware.surepass.SurepassCommons;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.GetPublicIpAndLocation;
import com.bp.middleware.util.PasswordUtils;
import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PanVerificationSignDesk {

	
	private final String VERIFICATION_TYPE = AppConstants.PAN_VERIFY;
	private EntityModel ENTITY = null;
	
	@Autowired
	private ResponseRepository respRepository;
	@Autowired
	private RequestRepository reqRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MerchantPriceRepository merchantPriceRepository;
	@Autowired
	private VendorRepository vendorRepository;
	@Autowired
	private VendorVerificationRepository verificationRepository;
	@Autowired
	private VendorPriceRepository vendorPriceRepository;
	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private PostpaidRepository postpaidRepository;
	@Autowired
	private SurepassCommons surepassCommons;
	@Autowired
	private SmartRouteUtils smartRouteUtils;
	@Autowired
	private ErrorIdentifierService errorIdentifierService;
	@Autowired
	private  CommonResponseStructure CommonResponseStructure;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;
	
	public ResponseStructure panVerificationService(RequestModel model ,HttpServletRequest servletRequest) {
		
		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			if (userModel != null && userModel.isAccountStatus()) {

				ENTITY=userModel;
				
				VendorVerificationModel verificationModel = verificationRepository
						.findByVerificationDocument(AppConstants.PAN_VERIFY);
				VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SIGN_DESK_VENDOR);
				
				MerchantPriceModel merchantPriceModel = merchantPriceRepository
						.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
								userModel,true);
				
				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.getByVendorModelAndVendorVerificationModelAndStatus(vendorModel, verificationModel,true);

				if (merchantPriceModel != null && vendorPriceModel!=null && merchantPriceModel.isAccepted()) {

					ResponseStructure balance = smartRouteUtils.balanceCheck(userModel, merchantPriceModel,verificationModel);

					if (balance.getFlag() == 1) {
						String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
								userModel.getSecretKey());
						JSONObject userJson = new JSONObject(userDecryption);

						String referenceNumber = FileUtils.getRandomOTPnumber(10);
						userJson.put("reference_id", referenceNumber);
						userJson.put("source_type", "id");

						String source = userJson.getString("source");

						System.err.println("SOURCE : " + source);

						Response sourceResponse = surepassCommons.sourceCheck(source, userModel, merchantPriceModel);
						System.err.println("Source Response : " + sourceResponse);

						if (sourceResponse.getResponseId() > 0) {
							System.err.println("SOURCE PRESENT");

							return surepassCommons.setRequest(sourceResponse, model, merchantPriceModel, userModel,
									verificationModel);

						} else {

							return signDeskPanVerification(userJson, model, userModel, verificationModel, vendorModel,
									merchantPriceModel, vendorPriceModel);
						}
					} else {
						return balance;
					}
				}else {
					
					if(vendorPriceModel == null) {
						return smartRouteUtils.verificationCurrentlyNotAvailable(userModel, verificationModel,model);
					}else if(merchantPriceModel==null){
						return smartRouteUtils.noAccessForThisVerification(userModel, verificationModel,model);
					}else {
						return smartRouteUtils.notAccepted(userModel, verificationModel,model);
					}
				}
				
			} else {
				if(userModel == null) {
					return smartRouteUtils.commonErrorResponse();
				}else {
					return smartRouteUtils.accountInactive(userModel);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			
			structure.setErrorReferenceId(errorIdentifierService.errorSaver(e, VERIFICATION_TYPE,ENTITY));
			structure.setFileName(VERIFICATION_TYPE);
			structure.setData(null);
			structure.setMessage(AppConstants.ERROR_MESSAGE_RESPONSE);
			structure.setErrorDiscription(AppConstants.ERROR_DESCRIPTION_RESPONSE);
			structure.setFlag(7);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	public ResponseStructure signDeskPanVerification(JSONObject userJson, RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPrice) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String userReferenceId = userJson.getString("reference_id");
		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");

		Request request = new Request();
		Date reqDate = new Date();

		JSONObject inputParams = new JSONObject();
		JSONObject encryptDatas = new JSONObject();

		inputParams.put("reference_id", userReferenceId);
		inputParams.put("source_type", sourceType);
		inputParams.put("source", source);

		// String key = AppConstants.ENCRYPTION_KEY;
		// String jsonString = inputParams.toString();
		String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);
		// System.out.println("Encryption Json =" + encryptedJson);

		encryptDatas.put("api_data", encryptedJson);
		encryptDatas.put("enc_mode", "symmetric");
		encryptDatas.put("is_encrypted", true);

		// create an instance of RestTemplate
		RestTemplate restTemplate = new RestTemplate();

		// VendorReq
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);

		// create headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.add("x-parse-rest-api-key", vendorPrice.getApiKey());
		headers.add("x-parse-application-id", vendorPrice.getApplicationId());
		headers.add("Content-Type", AppConstants.CONTENT_TYPE);

		HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

		ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
				String.class);
		String data = clientResponse.getBody();

		System.err.println("DATA = " + data);
		System.err.println("MERCH = " + merchantPriceModel.getMerchantPriceId());

		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		// Convert the string to a JSONObject
		JSONObject jsonObject = new JSONObject(data);

		// Extract specific data
		String encryptedResponse = jsonObject.getString("encrypted_response");
		String status = jsonObject.getString("status");

		// // Print the extracted data
		System.out.println("encrypted_response: " + encryptedResponse);
		System.out.println("status: " + status);

		String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

		Response response = new Response();

		JSONObject decryptJson = new JSONObject(decryptData);
		
		System.err.println("DC JSON : "+decryptJson);
		
		String signDeskStatus = decryptJson.getString("status");
		String responseTimeStamp = decryptJson.getString("response_time_stamp");
		String referenceId = decryptJson.getString("reference_id");

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setReferenceId(referenceId);
		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(vendorVerifyModel);
		request.setResponseDateAndTime(responseTimeStamp);
		request.setStatus(signDeskStatus);
		request.setFreeHit(model.isFreeHit());

		response.setReferenceId(referenceId);
		response.setStatus(signDeskStatus);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(responseTimeStamp);
		response.setResponse(decryptData);
		response.setVerificationModel(vendorVerifyModel);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !model.isFreeHit()) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else if (!model.isFreeHit()) {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		RequestModel temporary = new RequestModel();

		temporary.setStatus(signDeskStatus);
		temporary.setReferenceId(referenceId);
		temporary.setResponseDateAndTime(responseTimeStamp);
		temporary.setVendorModel(vendorModel);

		if (signDeskStatus.equals("success")) {
			JSONObject resultJson = decryptJson.getJSONObject("result");

			boolean validPan = resultJson.getBoolean("valid_pan");

			if (validPan) {

				JSONObject validJson = resultJson.getJSONObject("validated_data");
				String name = validJson.getString("full_name");
				String panNumber = validJson.getString("pan_number");
				String category = validJson.getString("category");

				response.setFullName(name);
				response.setPanNumber(panNumber);

				temporary.setFullName(name);
				temporary.setSource(panNumber);
				temporary.setCategory(category);
			}

			String transactionId = decryptJson.getString("transaction_id");
			String message = decryptJson.getString("message");

			request.setMessage(message);
			request.setTransactionId(transactionId);
			request.setAttempt(0);

			response.setTransactionId(transactionId);
			response.setMessage(message);
			response.setEncryptedJson(encryptedJson);

			temporary.setMessage(message);

		} else {

			String error = decryptJson.getString("error");
			String errorCode = decryptJson.getString("error_code");

			request.setError(error);
			request.setErrorCode(errorCode);
			request.setAttempt(model.getAttempt() + 1);

			response.setError(error);
			response.setErrorCode(errorCode);

			temporary.setError(error);
		}

		JSONObject commonResponse = CommonResponseStructure.commonResponsePan(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

		// Response Count
		int responseCount = userModel.getResponseCount();
		userModel.setResponseCount(++responseCount);
		vendorRepository.save(vendorModel);

		String encryptedCommonResponse = PasswordUtils.demoEncryption(commonResponse, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		userRepository.save(userModel);

		return structure;
	}
}
