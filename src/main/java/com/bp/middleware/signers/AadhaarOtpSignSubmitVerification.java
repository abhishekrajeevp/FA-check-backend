package com.bp.middleware.signers;

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
import org.springframework.stereotype.Component;
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
import com.bp.middleware.smartrouteverification.CommonResponseStructure;
import com.bp.middleware.smartrouteverification.SmartRouteUtils;
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

@Component
public class AadhaarOtpSignSubmitVerification {
	
	
	
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
	

	public ResponseStructure aadhaarOtpVerification(SignerModel signer, RequestModel model) throws Exception{
		
		EntityModel userModel = signer.getEntityModel();
		
		if (userModel != null && userModel.isAccountStatus()) {

			VendorVerificationModel verificationModel = vendorVerificationRepository.findByVerificationDocument(AppConstants.AADHAR_OTP_VERIFY);

			VendorModel vendorModel = new VendorModel();
			
			if (AppConstants.SUREPASS_ROUTE) {
				 vendorModel =vendorRepository.findByVendorName(AppConstants.SUREPASS_VENDOR);
			} else {
				 vendorModel =vendorRepository.findByVendorName(AppConstants.SIGN_DESK_VENDOR);
			}
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository.getByVendorModelAndVendorVerificationModelAndStatus(vendorModel, verificationModel,true);

			System.err.println(vendorModel.getVendorName()+" "+verificationModel.getVerificationDocument()+" "+userModel.getUserId());
			
			if (merchantPriceModel!=null && vendorPriceModel!=null) {

				if(AppConstants.SUREPASS_ROUTE) {
					return surepassAadhaarOtpValidation(model, userModel, verificationModel,vendorModel,merchantPriceModel,vendorPriceModel,signer);
				}else {
					return signDeskAadhaarOtpValidation(model, userModel, verificationModel,vendorModel,merchantPriceModel,vendorPriceModel,signer);
				}

			} else {
				if(vendorPriceModel==null) {
					return smartRouteUtils.verificationCurrentlyNotAvailable(userModel, verificationModel,model);
				}else {
					return smartRouteUtils.noAccessForThisVerification(userModel, verificationModel,model);
				}
			}
		} else {

			if (userModel == null) {
				return smartRouteUtils.commonErrorResponse();
			} else {
				return smartRouteUtils.accountInactive(userModel);
			}
		}
	}

	
	
	

	private ResponseStructure surepassAadhaarOtpValidation(RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel,
			MerchantPriceModel merchantPriceModel, VendorPriceModel vendorPrice,SignerModel signer) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		JSONObject obj = new JSONObject();
		obj.put("otp", model.getOtpCode());
		obj.put("client_id", signer.getAadhaarRequestId());

		Request request = reqRepository.findByReferenceId(signer.getAadhaarRequestId());

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

		String message = null;
		if (status) {
			message = wholeData.optString("message_code", "");
		} else {
			message = wholeData.optString("message", "");
		}

		Response response = respRepository.findByReferenceId(signer.getAadhaarRequestId());
		
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

        String ipAddress = ipAndLocation.publicIpAddress();

		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setPrice(request.getPrice());
		request.setUser(userModel);
		request.setResponseDateAndTime(responseTime);
		request.setMessage(message);

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

		String referenceId = signer.getAadhaarRequestId();
		
		boolean responseStatus = status;
		String fullName ="";
		String aadharNumber = "";
		String dob = "";
		String gender = "";
		String address = "";
		boolean mobileVerified = false;
		
		if (status) {

			temporary.setStatus("success");

			JSONObject internalData = wholeData.getJSONObject("data");

			 String clientId = internalData.optString("client_id", "");
			 fullName = internalData.optString("full_name", "");
			 aadharNumber = internalData.optString("aadhaar_number", "");
			 dob = internalData.optString("dob", "");
			 gender = internalData.optString("gender", "");
			 address = internalData.getJSONObject("address").toString();
			 mobileVerified = internalData.getBoolean("mobile_verified");

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
			
			structure.setFlag(1);
			structure.setMessage(AppConstants.SUCCESS);

		} else {

			request.setError("error");
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setReferenceId(referenceId);
			response.setError("error");
			response.setStatus("failed");

			temporary.setStatus("failed");
			temporary.setReferenceId(referenceId);
			
			structure.setFlag(2);
			structure.setMessage(message);
		}

		JSONObject commonResponse = CommonResponseStructure.commonResponseAadharOtpSubmit(temporary);

		reqRepository.save(request);
		respRepository.save(response);

		String statusResponse = (status)? "Success":"failed";
		smartRouteUtils.setAadhaarOtpReqResponse(request,response,merchantPriceModel,statusResponse,message);
		
		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && statusCodeNumber != 401
				&& statusCodeNumber != 403) {

			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else if (statusCodeNumber != 401 && statusCodeNumber != 403) {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		System.err.println("---> "+wholeData);

		model.setFullName(fullName);
		model.setAadhaarNumber(aadharNumber);
		model.setDob(dob);
		model.setGender(gender);
		model.setMobileVerified(mobileVerified);
		model.setStatusFlag(responseStatus);
		
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setData(null);
		structure.setModelData(model);

		userRepository.save(userModel);

		return structure;
	}
	
	
	
	
	
	
	
	private ResponseStructure signDeskAadhaarOtpValidation(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, SignerModel signer) throws Exception{

		ResponseStructure structure = new ResponseStructure();

		Request request = reqRepository.findByReferenceId(signer.getAadhaarRequestId());
		Response response = respRepository.findByReferenceId(signer.getAadhaarRequestId());
		
		String referenceId = request.getReferenceId();
		String transactionId = request.getTransactionId();// userJson.getString("transaction_id");
		String otp = model.getOtpCode();

		JSONObject inputParams = new JSONObject();
		JSONObject encryptDatas = new JSONObject();

		// String referenceNumber=FileUtils.getRandomOTPnumber(10);

		System.err.println(referenceId +" refernce");
		System.err.println(transactionId +" transactionId");
		System.err.println(otp +" otp");
		
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
		headers.add("x-parse-rest-api-key", vendorPriceModel.getApiKey());
		headers.add("x-parse-application-id", vendorPriceModel.getApplicationId());
		headers.add("Content-Type", AppConstants.CONTENT_TYPE);

		HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

		ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPriceModel.getApiLink(), entity,
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

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setOtp(otp);
		request.setPrice(request.getPrice());

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
		
		boolean responseStatus = false;
		String respFullName ="";
		String respAadharNumber = "";
		String respDob = "";
		String respGender = "";
		String respAddress = "";
		boolean respMobileVerified = false;

		String message ="";
		
		if (statusResponse.equalsIgnoreCase("success")) {

			responseStatus=true;
			
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
			
			
			model.setFullName(fullName);
			model.setAadhaarNumber(aadharNumber);
			model.setDob(dob);
			model.setGender(gender);
			model.setMobileVerified(mobileVerified);
			model.setStatusFlag(responseStatus);

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
			
			structure.setFlag(1);
			structure.setMessage(AppConstants.SUCCESS);

		} else {

			String error = decryptJson.optString("error","");
			message = decryptJson.optString("message","");
			String errorCode = decryptJson.optString("error_code", "");

			temporary.setErrorCode(errorCode);

			request.setResponseDateAndTime(responseTime);
			// request.setError(error);
			request.setErrorCode(errorCode);

			response.setResponse(decryptData);
			// response.setError(error);
			response.setErrorCode(errorCode);
			response.setResponseDateAndTime(responseTime);
			response.setRequest(request);

			structure.setFlag(2);
			
			if(!message.equalsIgnoreCase("")) {
				structure.setMessage(message);
			}else {
				structure.setMessage(error);
			}
		}

		JSONObject commonResponse = CommonResponseStructure.commonResponseAadharOtpSubmit(temporary);

		reqRepository.save(request);
		respRepository.save(response);

		smartRouteUtils.setAadhaarOtpReqResponse(request,response,merchantPriceModel,statusResponse,message);
		
		// Response Count
		int responseCount = userModel.getResponseCount();
		userModel.setResponseCount(++responseCount);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !model.isFreeHit()) {

			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else if (!model.isFreeHit()) {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		String encryptedCommonResponse = PasswordUtils.demoEncryption(commonResponse, userModel.getSecretKey());
		
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setData(null);
		structure.setModelData(model);

		userRepository.save(userModel);

		return structure;

	}
	
	
	
	
	
}
