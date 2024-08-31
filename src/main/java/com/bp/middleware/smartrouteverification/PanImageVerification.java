package com.bp.middleware.smartrouteverification;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

import com.bp.middleware.customexception.InvalidApiKeyOrApplicationIdException;
import com.bp.middleware.duplicateverificationresponse.PanReplica;
import com.bp.middleware.erroridentifier.ErrorIdentifierRepository;
import com.bp.middleware.erroridentifier.ErrorIdentifierService;
import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.DateUtil;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.PasswordUtils;
import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PanImageVerification {

	private final String VERIFICATION_TYPE = AppConstants.PAN_OCR_ADVANCED;
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
	private VendorVerificationRepository vendorVerificationRepository;
	@Autowired
	private VendorPriceRepository vendorPriceRepository;
	@Autowired
	private SmartRouteUtils smartRouteUtils;
	@Autowired
	private ErrorIdentifierService errorIdentifierService;
	@Autowired
	private CommonResponseStructure CommonResponseStructure;
	@Autowired
	FileUtils fu;
	@Autowired
	private PanReplica panReplica;

	public ResponseStructure panImageVerification(RequestModel model, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			if (userModel == null) {
				userModel = userRepository.findByApiSandboxKeyAndApplicationId(apiKey, applicationId);
			}

			if (userModel != null && userModel.isAccountStatus()) {

				ENTITY=userModel;
				
				VendorVerificationModel vendorVerifyModel = vendorVerificationRepository
						.findByVerificationDocument(AppConstants.PAN_OCR_ADVANCED);
				
				VendorModel vendor = vendorRepository.findByVendorName(AppConstants.SIGN_DESK_VENDOR);

				MerchantPriceModel merchantPriceModel = merchantPriceRepository
						.findByEntityModelAndVendorModelAndVendorVerificationModel(userModel, vendor, vendorVerifyModel);
				
                boolean accepted = (merchantPriceModel!=null)?merchantPriceModel.isAccepted():false;

				VendorPriceModel vendorPrice = vendorPriceRepository
						.findByVendorModelAndVendorVerificationModel(vendor, vendorVerifyModel);

				if ( vendorPrice!=null  &&  merchantPriceModel!=null && accepted) {

					if (userModel.getApiSandboxKey().equals(apiKey) && userModel.getNoRestriction() == 0) {

						return panReplica.panImageDuplicateResponse(model, userModel, vendorVerifyModel);

					} else if (userModel.getNoRestriction() > 0) {

						userModel.setNoRestriction(userModel.getNoRestriction() - 1);

						model.setFreeHit(true);
					}

					ResponseStructure balanceCheck = smartRouteUtils.balanceCheck(userModel, merchantPriceModel,vendorVerifyModel);

					if (balanceCheck.getFlag() == 1) {

						String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
								userModel.getSecretKey());
						
						JSONObject userJson = new JSONObject(userDecryption);

						return signDeskPanImageVerification(userJson, model, userModel, vendorVerifyModel,vendor,merchantPriceModel,vendorPrice);
					}
					return balanceCheck;
				} else {

					if(vendorPrice==null) {
						return smartRouteUtils.verificationCurrentlyNotAvailable(userModel, vendorVerifyModel,model);
					}else if(merchantPriceModel==null){
						return smartRouteUtils.noAccessForThisVerification(userModel, vendorVerifyModel,model);
					}else {
						return smartRouteUtils.notAccepted(userModel, vendorVerifyModel,model);
					}
				}

			} else {

				if (userModel == null) {
					return smartRouteUtils.commonErrorResponse();
				} else {
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

	

	private ResponseStructure signDeskPanImageVerification(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel,
			MerchantPriceModel merchantPriceModel, VendorPriceModel vendorPrice) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String referenceId = FileUtils.getRandomOTPnumber(10);
		String sourceType = "base64";

		Date requesTime = new Date();

		JSONObject inputParams = new JSONObject();
		JSONObject encryptDatas = new JSONObject();

		inputParams.put("reference_id", referenceId);
		inputParams.put("source_type", sourceType);
		inputParams.put("source", source);
		
		System.out.println("Reference id in = " + inputParams.toString());
		String key = AppConstants.ENCRYPTION_KEY;
		String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);
		System.out.println("Encryption Json =" + encryptedJson);

		encryptDatas.put("api_data", encryptedJson);
		encryptDatas.put("enc_mode", "symmetric");
		encryptDatas.put("is_encrypted", true);

		RestTemplate restTemplate = new RestTemplate();

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

		// VendorReq
		vendorModel.setVendorRequest(vendorModel.getVendorRequest()+1);
		// VendorResponse
		vendorModel.setVendorResponse(vendorModel.getVendorResponse()+1);
		// MonthlyCount
		vendorModel.setMonthlyCount(vendorModel.getMonthlyCount()+1);

		vendorRepository.save(vendorModel);

		JSONObject jsonObject = new JSONObject(data);

		String statusOfResponse = jsonObject.optString("status", "");

		if (statusOfResponse.equalsIgnoreCase("failed")) {

			String errorCode = jsonObject.optString("error_code", "");

			if (errorCode.equalsIgnoreCase("vcip_017")) {

				String error = jsonObject.optString("error", "");

				throw new InvalidApiKeyOrApplicationIdException("SIGN DESK : " + error, 522);
			}
		}

		String encryptedResponse = jsonObject.getString("encrypted_response");
		System.out.println("encrypted_response: " + encryptedResponse);

		String decryptData = PasswordUtils.decryptString(encryptedResponse, key);
		JSONObject decryptJson = new JSONObject(decryptData);

		System.err.println("DECRYPTED DATA : " + decryptData);

		String responseTime = decryptJson.getString("response_time_stamp");
		String status = decryptJson.getString("status");

		Request request = new Request();
		int count = userModel.getRequestCount() + 1;
		userModel.setRequestCount(count);

		request.setStatus(status);
		request.setReferenceId(referenceId);
		request.setSourceType(sourceType);
		request.setResponseDateAndTime(responseTime);
		request.setRequestDateAndTime(requesTime);
		request.setRequestBy(userModel.getName());
		request.setUser(userModel);
		request.setVerificationModel(vendorVerifyModel);
		request.setPrice(merchantPriceModel.getImagePrice());
		request.setFreeHit(model.isFreeHit());
		request.setConsider(true);

		Response responseData = new Response();
		responseData.setReferenceId(referenceId);
		responseData.setSourceType(sourceType);
		responseData.setRequestDateAndTime(requesTime);
		responseData.setRequestBy(userModel.getName());
		responseData.setStatus(status);
		responseData.setResponseDateAndTime(responseTime);
		// responseData.setEncryptedJson(encryptedJson);
		responseData.setUser(userModel);
		responseData.setVendorModel(vendorModel);
		responseData.setVerificationModel(vendorVerifyModel);


		RequestModel temporary = new RequestModel();

		temporary.setStatus(status);
		temporary.setReferenceId(referenceId);
		temporary.setResponseDateAndTime(responseTime);
		temporary.setVendorModel(vendorModel);
		
		String errorCode = "";

		if (status.equals("success")) {

			JSONObject resultJson = decryptJson.getJSONObject("result");
			JSONObject extractedData = resultJson.getJSONObject("extracted_data");
			JSONObject validatedData = resultJson.getJSONObject("validated_data");
			JSONObject dataMatch = resultJson.getJSONObject("data_match");
			boolean validPan = resultJson.getBoolean("valid_pan");
			int dataMatchAggregate = resultJson.getInt("data_match_aggregate");

			String transactionId = decryptJson.getString("transaction_id");
			String message = decryptJson.getString("message");

			if (validPan) {

				String extractedPan = extractedData.getString("pan_number");
				String dob = extractedData.getString("dob");
				String fathersName = extractedData.getString("father_name");
				String name = extractedData.getString("name");

				String clientId = validatedData.getString("client_id");
				String fullName = validatedData.getString("full_name");
				String category = validatedData.getString("category");

				int dataMatchFullName = dataMatch.getInt("full_name");
				int dataMatchPanNumber = dataMatch.getInt("pan_number");

				request.setClientId(clientId);
				request.setSource(extractedPan);
				request.setExtractedData(extractedPan);

				responseData.setClientId(clientId);
				responseData.setExtractedData(extractedPan);
				responseData.setSource(extractedPan);

				temporary.setPan(extractedPan);
				temporary.setName(name);
				temporary.setFullName(fullName);
				temporary.setDob(dob);
				temporary.setFathersName(fathersName);
				temporary.setCategory(category);
				temporary.setDataMatchFullName(dataMatchFullName);
				temporary.setDataMatchPanNumber(dataMatchPanNumber);
				temporary.setDataMatchAggregate(dataMatchAggregate);
				temporary.setClientId(clientId);

			}
			request.setTransactionId(transactionId);
			request.setMessage(message);

			responseData.setTransactionId(transactionId);
			responseData.setMessage(message);
			responseData.setResponse(decryptData);
			responseData.setRequest(request);

			temporary.setMessage(message);

		} else {
			// String error = decryptJson.getString("error");
			 errorCode = decryptJson.getString("error_code");

			request.setError("error");
			request.setErrorCode(errorCode);

			responseData.setResponse(decryptData);
			responseData.setError("error");
			responseData.setErrorCode(errorCode);
			responseData.setRequest(request);

			temporary.setError("error");
		}

		JSONObject commonResponse = CommonResponseStructure.commonResponsePanImage(temporary);
		responseData.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(responseData);

		int responseCount = userModel.getResponseCount();
		userModel.setResponseCount(++responseCount);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !model.isFreeHit()  && smartRouteUtils.signDeskError(errorCode)) {

			smartRouteUtils.deductAmountForOcr(userModel,merchantPriceModel);
			
		}else if(!model.isFreeHit() && smartRouteUtils.signDeskError(errorCode)) {
			
			smartRouteUtils.postpaidConsumedAmountForOcr(userModel, merchantPriceModel);
			
		}else {
			
			request.setConsider(false);
			reqRepository.save(request);
		}

		String encryptedCommonResponse = PasswordUtils.demoEncryption(commonResponse, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);

		structure.setData(mapNew);
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setMessage(AppConstants.SUCCESS);

		userRepository.save(userModel);

		return structure;

	}

}
