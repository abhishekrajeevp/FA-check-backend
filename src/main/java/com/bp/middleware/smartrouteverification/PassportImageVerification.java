package com.bp.middleware.smartrouteverification;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
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
public class PassportImageVerification {

	
	private final String VERIFICATION_TYPE = AppConstants.PASSPORT_IMAGE;
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
	
	
	public ResponseStructure passportImageVerification(RequestModel model, HttpServletRequest servletRequest) {
		
		ResponseStructure structure = new ResponseStructure();
		try {
			
			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			if (userModel != null && userModel.isAccountStatus()) {
				
				ENTITY=userModel;
				
			VendorVerificationModel vendorVerifyModel = vendorVerificationRepository.findByVerificationDocument(AppConstants.PASSPORT_IMAGE);

			List<MerchantPriceModel> merchantPriceList = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndStatus(userModel, vendorVerifyModel, true);
			
			boolean accepted = true;
			
			for (MerchantPriceModel merchantPriceModel : merchantPriceList) {
				
				if(accepted == true) {
					
					accepted = merchantPriceModel.isAccepted();
				}else {
					 break ;
				}
			}

			List<VendorPriceModel> vendorPriceList = vendorPriceRepository
					.findByVendorVerificationModelAndStatus(vendorVerifyModel, true);

			if (!merchantPriceList.isEmpty() && !vendorPriceList.isEmpty()  &&  accepted) {
			
			String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
			JSONObject userJson = new JSONObject(userDecryption);
			
			String referenceNumber = FileUtils.getRandomOTPnumber(10);
			userJson.put("reference_id", referenceNumber);
			userJson.put("source_type", "base64");
			
			return passportImageSmartRoute(userJson, model, userModel, vendorVerifyModel);
			
			}else {
				if(vendorPriceList.isEmpty()) {
					return smartRouteUtils.verificationCurrentlyNotAvailable(userModel, vendorVerifyModel,model);
				}else if(merchantPriceList.isEmpty()){
					return smartRouteUtils.noAccessForThisVerification(userModel, vendorVerifyModel,model);
				}else {
					return smartRouteUtils.notAccepted(userModel, vendorVerifyModel,model);
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


	private ResponseStructure passportImageSmartRoute(JSONObject userJson, RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel) throws Exception{

		List<Request> reqList = reqRepository.findByUserAndVerificationModel(userModel, vendorVerifyModel);

		Request lastRequest = new Request();
		
		InetAddress ipAddressLocalHost = InetAddress.getLocalHost();
		String ipAddress = ipAddressLocalHost.getHostAddress();
		
		long timeDifference=0;
		if (!reqList.isEmpty()) {
			lastRequest = reqList.get(reqList.size() - 1);
			
			Date currentDatetime = new Date();
			Date requestDatetime = lastRequest.getRequestDateAndTime();

			 timeDifference = DateUtil.secondsDifferenceCalculator(requestDatetime, currentDatetime);
		}

		model.setAttempt(lastRequest.getAttempt());

			System.err.println("ATTEMPT 1");
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 1);

			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);
	}


	private ResponseStructure requestVendorRouting(JSONObject userJson, RequestModel model,
			MerchantPriceModel merchantPriceModel, EntityModel userModel, VendorVerificationModel vendorVerifyModel) throws Exception{

		VendorModel vendorModel = merchantPriceModel.getVendorModel();
		VendorPriceModel vendorPrice = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(vendorModel,
				vendorVerifyModel);

        ResponseStructure balanceCheck = smartRouteUtils.balanceCheckForOcr(userModel, merchantPriceModel,vendorVerifyModel);
		
		if(balanceCheck.getFlag()==1) {
			
			if (merchantPriceModel.getVendorModel().getVendorId() == 1) {

				System.err.println("SIGN DESK");
				return signDeskPassportImageVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);

			}//No Other Vendor available for DL IMAGE
		}
		return balanceCheck;
	}


	private ResponseStructure signDeskPassportImageVerification(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel,
			MerchantPriceModel merchantPriceModel, VendorPriceModel vendorPrice) throws Exception{

            ResponseStructure structure = new ResponseStructure();
         
            String reference_id = userJson.getString("reference_id");
			JSONArray source = userJson.getJSONArray("source");
			String source_type = userJson.getString("source_type");

			Request request = new Request();
			Date reqDate = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// String referenceNumber=FileUtils.getRandomOTPnumber(10);

			// String encryptImage=PasswordUtils.demoImageEncryption(image);
			inputParams.put("reference_id", reference_id);
			inputParams.put("source_type", source_type);
			inputParams.put("source", source);
			System.out.println("Reference id in = " + inputParams.toString());
			String key = AppConstants.ENCRYPTION_KEY;
			// String jsonString = inputParams.toString();
			String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);
			System.out.println("Encryption Json =" + encryptedJson);

			encryptDatas.put("api_data", encryptedJson);
			encryptDatas.put("enc_mode", "symmetric");
			encryptDatas.put("is_encrypted", true);

			// create an instance of RestTemplate
			RestTemplate restTemplate = new RestTemplate();

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

			// VendorReq
			vendorModel.setVendorRequest(vendorModel.getVendorRequest()+1);
			// VendorResponse
			vendorModel.setVendorResponse(vendorModel.getVendorResponse()+1);
			// MonthlyCount
			vendorModel.setMonthlyCount(vendorModel.getMonthlyCount()+1);

			vendorRepository.save(vendorModel);

			// Convert the string to a JSONObject
			JSONObject jsonObject = new JSONObject(data);
			
			String statusOfResponse = jsonObject.optString("status", "");

			if (statusOfResponse.equalsIgnoreCase("failed")) {

				String errorCode = jsonObject.optString("error_code", "");

				if (errorCode.equalsIgnoreCase("vcip_017")) {

					String error = jsonObject.optString("error", "");

					throw new InvalidApiKeyOrApplicationIdException("SIGN DESK : " + error, 522);
				}
			}

			Response response = new Response();

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			// String status = jsonObject.getString("status");
			//
			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			//
			String decryptData = PasswordUtils.decryptString(encryptedResponse, key);

			JSONObject decryptJson = new JSONObject(decryptData);

			String status = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSourceType(source_type);
			request.setRequestBy(model.getRequestBy());
			request.setRequestDateAndTime(reqDate);
			request.setPrice(merchantPriceModel.getImagePrice());
			request.setUser(userModel);
			request.setVerificationModel(vendorVerifyModel);
			request.setFreeHit(model.isFreeHit());
			request.setConsider(true);

			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(source_type);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(model.getRequestBy());
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);
			response.setVerificationModel(vendorVerifyModel);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userModel.getSecretKey());

			System.out.println("RESONSE : " + decryptData);
			String errorCode = "";

			if (status.equals("success")) {

				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");

				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");
				JSONObject extractedData = resultJson.getJSONObject("extracted_data");

				String fullName = extractedData.getString("name");
				String dateOfBirth = extractedData.getString("dob");
				String address = extractedData.getString("address");
				String state = validatedJson.getString("state");

				String sourceId = extractedData.getString("dl_number");

				request.setSource(sourceId);
				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setFullName(fullName);
				request.setDob(dateOfBirth);
				request.setState(state);

				response.setSource(sourceId);
				response.setFullName(fullName);
				response.setDob(dateOfBirth);
				response.setState(state);
				response.setAddress(address);
				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setRequest(request);

			} else {

				// String error = decryptJson.getString("error");
				errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				request.setError("error");
				request.setErrorCode(errorCode);

				response.setResponse(decryptData);
				response.setError("error");
				response.setErrorCode(errorCode);
				response.setRequest(request);
			}

			reqRepository.save(request);
			respRepository.save(response);
			
			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);

			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !model.isFreeHit()   && smartRouteUtils.signDeskError(errorCode)) {

				smartRouteUtils.deductAmountForOcr(userModel,merchantPriceModel);
				
			}else if(!model.isFreeHit() && smartRouteUtils.signDeskError(errorCode)) {
				
				smartRouteUtils.postpaidConsumedAmountForOcr(userModel, merchantPriceModel);
				
			}else {
				
				request.setConsider(false);
				reqRepository.save(request);
			}
			
			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);
			
			userRepository.save(userModel);
			
			return structure;
	}
	
	
}
