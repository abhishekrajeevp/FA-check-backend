package com.bp.middleware.smartrouteverification;

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
import com.bp.middleware.duplicateverificationresponse.AadhaarReplica;
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
public class AadharOtpSubmitVerification {

	private final String VERIFICATION_TYPE = AppConstants.AADHAR_OTP_VERIFY;
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
	private AadhaarReplica aadhaarReplica;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;

	public ResponseStructure aadharOtpSubmitVerification(RequestModel model, HttpServletRequest servletRequest) {

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
						.findByVerificationDocument(AppConstants.AADHAR_OTP_VERIFY);

				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				String referenceId = userJson.getString("reference_id");

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

				if (!merchantPriceList.isEmpty() && !vendorPriceList.isEmpty() &&  accepted) {

					if (userModel.getApiSandboxKey().equals(apiKey) && userModel.getNoRestriction() == 0) {

						return aadhaarReplica.aadhaarSubmitOtpDuplicate(model, userModel, vendorVerifyModel);

					} else if (userModel.getNoRestriction() > 0) {

						userModel.setNoRestriction(userModel.getNoRestriction() - 1);

						model.setFreeHit(true);
					}

					Response responseModel = respRepository.findByReferenceId(referenceId);
					Request requestModel = reqRepository.findByReferenceId(referenceId);

					VendorModel vendorModel = responseModel.getVendorModel();

					MerchantPriceModel merchantPriceModel = merchantPriceRepository
							.findByEntityModelAndVendorModelAndVendorVerificationModel(userModel, vendorModel,
									vendorVerifyModel);

					System.err.println("merchantPriceModel : " + merchantPriceModel.getMerchantPriceId());

					ResponseStructure balanceCheck = smartRouteUtils.balanceCheck(userModel, merchantPriceModel,vendorVerifyModel);

					if (balanceCheck.getFlag() == 1) {

						// Balance Checked
						if (vendorModel.getVendorName().equalsIgnoreCase("SIGN DESK")) {

							System.err.println("SIGN DESK------");
							return signDeskAadharOtpSubmit(userModel, userJson, vendorModel, vendorVerifyModel,
									merchantPriceModel, responseModel, requestModel, model);

						} else if (vendorModel.getVendorName().equalsIgnoreCase("SPRINT VERIFY")) {

							System.err.println("SPRINT V------");
							return sprintVerifyAadharOtpSubmit(userModel, userJson, vendorModel, vendorVerifyModel,
									merchantPriceModel, responseModel, requestModel, model);
						} else if (vendorModel.getVendorName().equalsIgnoreCase("SUREPASS")) {

							System.err.println("SUREPASS------");
							return surepassAadharOtpSubmit(userModel, userJson, vendorModel, vendorVerifyModel,
									merchantPriceModel, responseModel, requestModel, model);
						}
					}

					return balanceCheck;
				} else {

					if(vendorPriceList.isEmpty()) {
						return smartRouteUtils.verificationCurrentlyNotAvailable(userModel, vendorVerifyModel,model);
					}else if(merchantPriceList.isEmpty()){
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

	private ResponseStructure signDeskAadharOtpSubmit(EntityModel userModel, JSONObject userJson,
			VendorModel vendorModel, VendorVerificationModel vendorVerifyModel, MerchantPriceModel merchantPriceModel,
			Response responseModel, Request requestModel, RequestModel model) throws Exception {

		System.err.println("SIGN DESK OTP METHOD");

		ResponseStructure structure = new ResponseStructure();

		VendorPriceModel vendorPrice = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(vendorModel,
				vendorVerifyModel);

		String referenceId = userJson.getString("reference_id");
		String transactionId = responseModel.getTransactionId();// userJson.getString("transaction_id");
		String otp = userJson.getString("otp");

		JSONObject inputParams = new JSONObject();
		JSONObject encryptDatas = new JSONObject();

		// String referenceNumber=FileUtils.getRandomOTPnumber(10);

		inputParams.put("reference_id", referenceId);
		inputParams.put("transaction_id", transactionId);
		inputParams.put("otp", otp);

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

		String status = jsonObject.optString("status", "");

		if (status.equalsIgnoreCase("failed")) {

			String errorCode = jsonObject.optString("error_code", "");

			if (errorCode.equalsIgnoreCase("vcip_017")) {

				String error = jsonObject.optString("error", "");

				throw new InvalidApiKeyOrApplicationIdException("SIGN DESK : " + error, 522);
			}
		}

		// Extract specific data
		String encryptedResponse = jsonObject.getString("encrypted_response");

		// // Print the extracted data
		System.out.println("encrypted_response: " + encryptedResponse);
		System.out.println("status: " + status);
		//
		String decryptData = PasswordUtils.decryptString(encryptedResponse, key);
		// String decryptData=PasswordUtils.jsonDecryption(encryptedResponse);
		JSONObject decryptJson = new JSONObject(decryptData);
		//
		System.out.println("Decrypted Response = " + decryptData);

		String responseTime = decryptJson.optString("response_time_stamp", "");

		String statusResponse = decryptJson.optString("status", "");

		Request request = requestModel;
		Response response = responseModel;

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setOtp(otp);
		request.setPrice(request.getPrice());
		request.setFreeHit(model.isFreeHit());
		request.setConsider(true);
		
		JSONObject object = new JSONObject();
		object.put("status", status);
		object.put("encrypted_response", decryptData);

		String returnResponse = PasswordUtils.demoEncryption(object, userModel.getSecretKey());
		System.out.println("RETURN RESPONSE : " + returnResponse);

		System.out.println("Status : " + status);
		System.out.println("Status Response :" + statusResponse);

		RequestModel temporary = new RequestModel();

		temporary.setStatus(statusResponse);
		temporary.setResponseDateAndTime(responseTime);
		temporary.setReferenceId(referenceId);
		temporary.setVendorModel(vendorModel);

		String errorCode ="";
		String message ="";
		
		if (statusResponse.equalsIgnoreCase("success")) {

			JSONObject resultJson = decryptJson.getJSONObject("result");
			JSONObject validatedData = resultJson.getJSONObject("validated_data");

			 message = decryptJson.optString("message", "");
			String transaction = decryptJson.optString("transaction_id", "");

			String fullName = validatedData.optString("full_name", "");
			String aadharNumber = validatedData.optString("aadhaar_number", "");
			String dob = validatedData.optString("dob", "");
			String gender = validatedData.optString("gender", "");
			String address = validatedData.getJSONObject("address").toString();
			boolean mobileVerified = validatedData.getBoolean("mobile_verified");

			String zip = validatedData.optString("zip", "");
			String mobileHash = validatedData.optString("mobile_hash", "");
			String emailHash = validatedData.optString("email_hash", "");
			String rawXml = validatedData.optString("raw_xml", "");
			String zipData = validatedData.optString("zip_data", "");
			String careOf = validatedData.optString("care_of", "");
			String shareCode = validatedData.optString("share_code", "");
			String aadharReferenceId = validatedData.optString("reference_id", "");
			String aadharStatus = validatedData.optString("status", "");
			String uniquenessId = validatedData.optString("uniqueness_id", "");
			boolean faceStatus = validatedData.getBoolean("face_status");
			boolean hasImage = validatedData.getBoolean("has_image");
			int faceScore = validatedData.getInt("face_score");

			temporary.setFullName(fullName);
			temporary.setAadhaarNumber(aadharNumber);
			temporary.setDob(dob);
			temporary.setMessage(message);
			temporary.setTransactionId(transactionId);
			temporary.setAddress(address);
			temporary.setGender(gender);
			temporary.setMobileVerified(mobileVerified);
			temporary.setZip(zip);
			temporary.setMobileHash(mobileHash);
			temporary.setEmailHash(emailHash);
			temporary.setRawXml(rawXml);
			temporary.setZipData(zipData);
			temporary.setCareOf(careOf);
			temporary.setShareCode(shareCode);
			temporary.setAadharReferenceId(aadharReferenceId);
			temporary.setAadharStatus(aadharStatus);
			temporary.setUniquenessId(uniquenessId);
			temporary.setFaceStatus(faceStatus);
			temporary.setHasImage(hasImage);
			temporary.setFaceScore(faceScore);

			request.setTransactionId(transaction);
			request.setMessage(message);
			request.setResponseDateAndTime(responseTime);

			response.setTransactionId(transaction);
			response.setMessage(message);
			response.setResponseDateAndTime(responseTime);
			response.setRequest(request);

		} else {

			// String error = decryptJson.optString("error","");
			 errorCode = decryptJson.optString("error_code", "");

			temporary.setErrorCode(errorCode);

			request.setResponseDateAndTime(responseTime);
			// request.setError(error);
			request.setErrorCode(errorCode);

			response.setResponse(decryptData);
			// response.setError(error);
			response.setErrorCode(errorCode);
			response.setResponseDateAndTime(responseTime);
			response.setRequest(request);

		}

		smartRouteUtils.signDeskErrorCodes(errorCode,vendorModel,userModel,vendorVerifyModel);
		
		JSONObject commonResponse = CommonResponseStructure.commonResponseAadharOtpSubmit(temporary);

		reqRepository.save(request);
		respRepository.save(response);
		
		smartRouteUtils.setAadhaarOtpReqResponse(request,response,merchantPriceModel,statusResponse,message);
		
		// Response Count
		int responseCount = userModel.getResponseCount();
		userModel.setResponseCount(++responseCount);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !model.isFreeHit() && smartRouteUtils.signDeskError(errorCode)) {

			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
			
		}else if(!model.isFreeHit() && smartRouteUtils.signDeskError(errorCode)) {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
			
		}else {
			
			request.setConsider(false);
			reqRepository.save(request);
		}

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

	private ResponseStructure sprintVerifyAadharOtpSubmit(EntityModel userModel, JSONObject userJson,
			VendorModel vendorModel, VendorVerificationModel vendorVerifyModel, MerchantPriceModel merchantPriceModel,
			Response responseModel, Request requestModel, RequestModel model) throws Exception {

		System.err.println("SPRINT V OTP METHOD");

		ResponseStructure structure = new ResponseStructure();

		String referenceNumber = userJson.getString("reference_id");

		String otp = userJson.getString("otp");
		String clientIdNumber = responseModel.getClientId();

		JSONObject obj = new JSONObject();
		obj.put("otp", otp);
		obj.put("client_id", clientIdNumber);

		Request request = requestModel;

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		vendorModel.setVendorRequest(vendorModel.getVendorRequest()+1);
		// VendorResponse
		vendorModel.setVendorResponse(vendorModel.getVendorResponse()+1);
		// MonthlyCount
		vendorModel.setMonthlyCount(vendorModel.getMonthlyCount()+1);

		vendorRepository.save(vendorModel);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		smartRouteUtils.errorCodes(statusCodeNumber, vendorModel,userModel,vendorVerifyModel);
		
		Response response = responseModel;
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setPrice(request.getPrice());
		request.setFreeHit(model.isFreeHit());
		request.setConsider(true);
		request.setUser(userModel);
		request.setResponseDateAndTime(responseTime);
		request.setMessage(message);

		response.setMessage(message);
		response.setRequestDateAndTime(new Date());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(responseTime);
		response.setResponse(sprintVerifyResponse);

		RequestModel temporary = new RequestModel();

		temporary.setResponseDateAndTime(responseTime);
		temporary.setMessage(message);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setVendorModel(vendorModel);

		if (status) {

			temporary.setStatus("success");

			long referenceIdNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceIdNumber);

			JSONObject internalData = wholeData.getJSONObject("data");

			String clientId = internalData.optString("client_id", "");
			String fullName = internalData.optString("full_name", "");
			String aadharNumber = internalData.optString("aadhaar_number", "");
			String dob = internalData.optString("dob", "");
			String gender = internalData.optString("gender", "");
			String address = internalData.getJSONObject("address").toString();
			boolean mobileVerified = internalData.getBoolean("mobile_verified");

			String zip = internalData.optString("zip", "");
			String mobileHash = internalData.optString("mobile_hash", "");
			String emailHash = internalData.optString("email_hash", "");
			String rawXml = internalData.optString("raw_xml", "");
			String zipData = internalData.optString("zip_data", "");
			String careOf = internalData.optString("care_of", "");
			String shareCode = internalData.optString("share_code", "");
			String aadharReferenceId = internalData.optString("reference_id", "");
			String aadharStatus = internalData.optString("status", "");
			String uniquenessId = internalData.optString("uniqueness_id", "");
			boolean faceStatus = internalData.getBoolean("face_status");
			boolean hasImage = internalData.getBoolean("has_image");
			int faceScore = internalData.getInt("face_score");

			request.setReferenceId(referenceId);
			request.setClientId(clientId);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setStatus("success");
			response.setReferenceId(referenceId);

			temporary.setFullName(fullName);
			temporary.setAadhaarNumber(aadharNumber);
			temporary.setDob(dob);
			temporary.setMessage(message);
			temporary.setAddress(address);
			temporary.setGender(gender);
			temporary.setMobileVerified(mobileVerified);
			temporary.setZip(zip);
			temporary.setMobileHash(mobileHash);
			temporary.setEmailHash(emailHash);
			temporary.setRawXml(rawXml);
			temporary.setZipData(zipData);
			temporary.setCareOf(careOf);
			temporary.setShareCode(shareCode);
			temporary.setAadharReferenceId(aadharReferenceId);
			temporary.setAadharStatus(aadharStatus);
			temporary.setUniquenessId(uniquenessId);
			temporary.setFaceStatus(faceStatus);
			temporary.setHasImage(hasImage);
			temporary.setFaceScore(faceScore);
			temporary.setReferenceId(referenceId);

		} else {

			request.setError("error");
			request.setReferenceId(referenceNumber);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setReferenceId(referenceNumber);
			response.setError("error");

			temporary.setStatus("failed");
			temporary.setReferenceId(referenceNumber);
		}

		JSONObject commonResponse = CommonResponseStructure.commonResponseAadharOtpSubmit(temporary);

		reqRepository.save(request);
		respRepository.save(response);
		
		String statusResponse = (status)? "Success":"failed";
		smartRouteUtils.setAadhaarOtpReqResponse(request,response,merchantPriceModel,statusResponse,message);
		
		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && statusCodeNumber != 401
				&& statusCodeNumber != 403 && !model.isFreeHit()) {

			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else if(statusCodeNumber!=401 && statusCodeNumber!=403 && !model.isFreeHit()) {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

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

	private ResponseStructure surepassAadharOtpSubmit(EntityModel userModel, JSONObject userJson,
			VendorModel vendorModel, VendorVerificationModel vendorVerifyModel, MerchantPriceModel merchantPriceModel,
			Response responseModel, Request requestModel, RequestModel model) throws Exception {

		System.err.println("SUREPASS METHOD");

		ResponseStructure structure = new ResponseStructure();

		String referenceNumber = userJson.getString("reference_id");

		String otp = userJson.getString("otp");
		String clientIdNumber = responseModel.getClientId();

		JSONObject obj = new JSONObject();
		obj.put("otp", otp);
		obj.put("client_id", clientIdNumber);

		Request request = requestModel;

		String surepassResponse = smartRouteUtils.surepassMechanism(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(surepassResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		vendorModel.setVendorRequest(vendorModel.getVendorRequest()+1);
		// VendorResponse
		vendorModel.setVendorResponse(vendorModel.getVendorResponse()+1);
		// MonthlyCount
		vendorModel.setMonthlyCount(vendorModel.getMonthlyCount()+1);

		vendorRepository.save(vendorModel);

		int statusCodeNumber = wholeData.getInt("status_code");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("success");

		smartRouteUtils.errorCodes(statusCodeNumber, vendorModel,userModel,vendorVerifyModel);
		
		String message = null;
		if (status) {
			message = wholeData.optString("message_code", "");
		} else {
			message = wholeData.optString("message", "");
		}

		Response response = responseModel;
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String ipAddress = ipAndLocation.publicIpAddress();

		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setPrice(request.getPrice());
		request.setFreeHit(model.isFreeHit());
		request.setUser(userModel);
		request.setResponseDateAndTime(responseTime);
		request.setMessage(message);
		request.setConsider(true);

		response.setMessage(message);
		response.setRequestDateAndTime(new Date());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(responseTime);
		response.setResponse(surepassResponse);

		RequestModel temporary = new RequestModel();

		temporary.setResponseDateAndTime(responseTime);
		temporary.setMessage(message);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setVendorModel(vendorModel);

		if (status) {

			temporary.setStatus("success");

			String referenceId = FileUtils.getRandomOTPnumber(10);

			JSONObject internalData = wholeData.getJSONObject("data");

			String clientId = internalData.optString("client_id", "");
			String fullName = internalData.optString("full_name", "");
			String aadharNumber = internalData.optString("aadhaar_number", "");
			String dob = internalData.optString("dob", "");
			String gender = internalData.optString("gender", "");
			String address = internalData.getJSONObject("address").toString();
			boolean mobileVerified = internalData.getBoolean("mobile_verified");

			String zip = internalData.optString("zip", "");
			String mobileHash = internalData.optString("mobile_hash", "");
			String emailHash = internalData.optString("email_hash", "");
			String rawXml = internalData.optString("raw_xml", "");
			String zipData = internalData.optString("zip_data", "");
			String careOf = internalData.optString("care_of", "");
			String shareCode = internalData.optString("share_code", "");
			String aadharReferenceId = internalData.optString("reference_id", "");
			String aadharStatus = internalData.optString("status", "");
			String uniquenessId = internalData.optString("uniqueness_id", "");
			boolean faceStatus = internalData.getBoolean("face_status");
			boolean hasImage = internalData.getBoolean("has_image");
			int faceScore = internalData.getInt("face_score");

			request.setReferenceId(referenceId);
			request.setClientId(clientId);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setReferenceId(referenceId);

			temporary.setFullName(fullName);
			temporary.setAadhaarNumber(aadharNumber);
			temporary.setDob(dob);
			temporary.setMessage(message);
			temporary.setAddress(address);
			temporary.setGender(gender);
			temporary.setMobileVerified(mobileVerified);
			temporary.setZip(zip);
			temporary.setMobileHash(mobileHash);
			temporary.setEmailHash(emailHash);
			temporary.setRawXml(rawXml);
			temporary.setZipData(zipData);
			temporary.setCareOf(careOf);
			temporary.setShareCode(shareCode);
			temporary.setAadharReferenceId(aadharReferenceId);
			temporary.setAadharStatus(aadharStatus);
			temporary.setUniquenessId(uniquenessId);
			temporary.setFaceStatus(faceStatus);
			temporary.setHasImage(hasImage);
			temporary.setFaceScore(faceScore);
			temporary.setReferenceId(referenceId);

		} else {

			request.setError("error");
			request.setReferenceId(referenceNumber);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setReferenceId(referenceNumber);
			response.setError("error");

			temporary.setStatus("failed");
			temporary.setReferenceId(referenceNumber);
		}

		JSONObject commonResponse = CommonResponseStructure.commonResponseAadharOtpSubmit(temporary);

		reqRepository.save(request);
		respRepository.save(response);

		String statusResponse = (status)? "Success":"failed";
		smartRouteUtils.setAadhaarOtpReqResponse(request,response,merchantPriceModel,statusResponse,message);
		
		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && statusCodeNumber != 401
				&& statusCodeNumber != 403 && !model.isFreeHit()) {

			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else if(statusCodeNumber!=401 && statusCodeNumber!=403 && !model.isFreeHit()) {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

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
