package com.bp.middleware.signers;

import java.net.InetAddress;
import java.time.LocalDate;
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
public class AadhaarSigningVerification {

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
	private SignerRepository signerRepository;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;

	public ResponseStructure aadhaarNumberVerification(SignerModel signer, RequestModel model) throws Exception {

		EntityModel userModel = signer.getEntityModel();

		if (userModel != null && userModel.isAccountStatus()) {

			VendorVerificationModel verificationModel = vendorVerificationRepository
					.findByVerificationDocument(AppConstants.AADHAR_XML_VERIFY);

			VendorModel vendorModel = new VendorModel();
			
			if (AppConstants.SUREPASS_ROUTE) {
				 vendorModel =vendorRepository.findByVendorName(AppConstants.SUREPASS_VENDOR);
			} else {
				 vendorModel =vendorRepository.findByVendorName(AppConstants.SIGN_DESK_VENDOR);
			}
			
			System.err.println(vendorModel.getVendorName()+" "+verificationModel.getVerificationDocument()+" "+userModel.getUserId());

			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel, true);

			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.getByVendorModelAndVendorVerificationModelAndStatus(vendorModel, verificationModel, true);

			if (merchantPriceModel != null && vendorPriceModel != null) {

				if (AppConstants.SUREPASS_ROUTE) {
					return surepassAadhaarNumberValidation(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel, signer);
				} else {
					return signDeskAadhaarNumberValidation(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel, signer);
				}

			} else {
				if (vendorPriceModel == null) {
					return smartRouteUtils.verificationCurrentlyNotAvailable(userModel, verificationModel,model);
				} else {
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

	private ResponseStructure surepassAadhaarNumberValidation(RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPrice, SignerModel signer) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		JSONObject obj = new JSONObject();
		obj.put("id_number", model.getAadhaarNumber());

		Request request = new Request();
		Date reqDate = new Date();

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

		Response response = new Response();
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(model.getAadhaarNumber());
		request.setSourceType("Id");
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(vendorVerifyModel);
		request.setResponseDateAndTime(responseTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());

		response.setMessage(message);
		response.setSourceType("Id");
		response.setSource(model.getAadhaarNumber());
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(responseTime);
		response.setResponse(surepassResponse);
		response.setVerificationModel(vendorVerifyModel);

		RequestModel temporary = new RequestModel();

		temporary.setResponseDateAndTime(responseTime);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setVendorModel(vendorModel);

		boolean otpSent = false;
		boolean validAadhar = false;
		String otpGenerationStatus = "";
		String clientId = "";

		if (status) {

			JSONObject internalData = wholeData.getJSONObject("data");

			clientId = internalData.optString("client_id", "");
			otpSent = internalData.optBoolean("otp_sent");
			boolean ifNumber = internalData.optBoolean("if_number");
			validAadhar = internalData.optBoolean("valid_aadhaar");
			otpGenerationStatus = internalData.optString("status", "");

			request.setStatus("success");
			request.setClientId(clientId);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setStatus("success");

			temporary.setStatus("success");

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(model.getAttempt() + 1);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");

			temporary.setStatus("failed");
		}

		request.setReferenceId(clientId);
		temporary.setReferenceId(clientId);
		response.setReferenceId(clientId);

		signer.setAadhaarRequestId(clientId);
		signer.setAadhaarRequestTime(LocalDate.now());

		JSONObject commonResponse = CommonResponseStructure.commonResponseAadharwithOtp(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && statusCodeNumber != 401
				&& statusCodeNumber != 403) {

			smartRouteUtils.deductAmountForId(userModel, merchantPriceModel);
		} else if (statusCodeNumber != 401 && statusCodeNumber != 403) {

			smartRouteUtils.postpaidConsumedAmount(userModel, merchantPriceModel);
		}

//		String maskedAadhaar = FileUtils.getFirstFourChar(model.getAadhaarNumber())+"XXXX"+FileUtils.stringSplitter(model.getAadhaarNumber(),8);
//		signer.setSignerAadhaar(maskedAadhaar);/

		signerRepository.save(signer);

		Map<String, Object> map = new HashMap<>();

		if (otpSent) {
			structure.setMessage("Otp send successfully");
		} else {
			structure.setMessage("Otp sending failed");
		}

		map.put("otp_sent", otpSent);
		map.put("otp_generation_status", otpGenerationStatus);
		map.put("valid_aadhaar", validAadhar);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(map);

		userRepository.save(userModel);

		return structure;
	}

	private ResponseStructure signDeskAadhaarNumberValidation(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, SignerModel signer) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		JSONObject inputParams = new JSONObject();
		JSONObject encryptDatas = new JSONObject();

		String refNo = FileUtils.getRandomOTPnumber(10);
		String source = model.getAadhaarNumber();
		String sourceType = "id";

		Request request = new Request();
		Date reqDate = new Date();

		inputParams.put("reference_id", refNo);
		inputParams.put("source_type", "id");
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

		String responseStatus = decryptJson.getString("status");

		String responseTime = decryptJson.getString("response_time_stamp");

		Response response = new Response();

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		String maskedAadhaar = FileUtils.getFirstFourChar(source) + "XXXX" + FileUtils.stringSplitter(source, 8);

		request.setSource(maskedAadhaar);
		request.setSourceType(sourceType);
		request.setRequestBy(userModel.getName());
		request.setRequestDateAndTime(reqDate);
		request.setStatus(responseStatus);
		request.setResponseDateAndTime(responseTime);
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setSource(maskedAadhaar);
		response.setSourceType(sourceType);
		response.setRequestBy(userModel.getName());
		response.setRequestDateAndTime(reqDate);
		response.setStatus(responseStatus);
		response.setResponseDateAndTime(responseTime);
		response.setRequest(request);
		response.setUser(userModel);
		response.setVendorModel(vendorModel);
		response.setResponse(decryptData);
		response.setVerificationModel(verificationModel);

		RequestModel temporary = new RequestModel();

		temporary.setResponseDateAndTime(responseTime);
		temporary.setVendorModel(vendorModel);

		signer.setAadhaarRequestId(refNo);
		signer.setAadhaarRequestTime(LocalDate.now());

		signerRepository.save(signer);

		if (responseStatus.equals("success")) {

			String referenceId = decryptJson.optString("reference_id", "");
			String message = decryptJson.optString("message", "");
			String transactionId = decryptJson.optString("transaction_id", "");

			request.setMessage(message);
			request.setTransactionId(transactionId);
			request.setAttempt(0);
			request.setReferenceId(refNo);

			response.setTransactionId(transactionId);
			response.setMessage(message);
			response.setReferenceId(referenceId);

			temporary.setStatus("success");
			temporary.setReferenceId(refNo);
		} else {

			String errorCode = decryptJson.optString("error_code", "");
			request.setErrorCode(errorCode);
			request.setAttempt(model.getAttempt() + 1);
			request.setReferenceId(refNo);

			response.setErrorCode(errorCode);
			response.setReferenceId(refNo);

			temporary.setStatus("failed");
			temporary.setReferenceId(refNo);
		}

		JSONObject commonResponse = CommonResponseStructure.commonResponseAadharwithOtp(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !model.isFreeHit()) {

			smartRouteUtils.deductAmountForId(userModel, merchantPriceModel);
		} else if (!model.isFreeHit()) {

			smartRouteUtils.postpaidConsumedAmount(userModel, merchantPriceModel);
		}

		// Response Count
		int responseCount = userModel.getResponseCount();
		userModel.setResponseCount(++responseCount);

		userRepository.save(userModel);

		Map<String, Object> map = new HashMap<>();

		if (responseStatus.equals("success")) {
			structure.setMessage("Otp send successfully");
			map.put("otp_sent", true);
			map.put("otp_generation_status", "success");
			map.put("valid_aadhaar", true);
			
		} else {
			structure.setMessage("Otp sending failed");
			map.put("otp_sent", false);
			map.put("otp_generation_status", "failed");
			map.put("valid_aadhaar", false);
		}

		
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(map);

		return structure;
	}

}
