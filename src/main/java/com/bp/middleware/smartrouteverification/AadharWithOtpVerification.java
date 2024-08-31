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
import com.bp.middleware.duplicateverificationresponse.AadhaarReplica;
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
public class AadharWithOtpVerification {

	private final String VERIFICATION_TYPE = AppConstants.AADHAR_XML_VERIFY;
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

	public ResponseStructure aadharWithOtpVerification(RequestModel model, HttpServletRequest servletRequest) {

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
						.findByVerificationDocument(AppConstants.AADHAR_XML_VERIFY);

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

						return aadhaarReplica.generateOtpDuplicate(model, userModel, vendorVerifyModel);

					} else if (userModel.getNoRestriction() > 0) {

						userModel.setNoRestriction(userModel.getNoRestriction() - 1);

						model.setFreeHit(true);
					}

					String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
							userModel.getSecretKey());
					JSONObject userJson = new JSONObject(userDecryption);

					String referenceNumber = FileUtils.getRandomOTPnumber(10);
					userJson.put("reference_id", referenceNumber);
					userJson.put("source_type", "id");

					System.err.println("NO SOURCE CHECKIN FOR AADHAAR");
					return aadharWithOtpSmartRoute(userJson, model, userModel, vendorVerifyModel);

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

	private ResponseStructure aadharWithOtpSmartRoute(JSONObject userJson, RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel) throws Exception {

		List<Request> reqList = reqRepository.findByUserAndVerificationModel(userModel, vendorVerifyModel);

		Request lastRequest = new Request();

		InetAddress ipAddressLocalHost = InetAddress.getLocalHost();
		String ipAddress = ipAddressLocalHost.getHostAddress();

		long timeDifference = 0;
		if (!reqList.isEmpty()) {
			lastRequest = reqList.get(reqList.size() - 1);

			Date currentDatetime = new Date();
			Date requestDatetime = lastRequest.getRequestDateAndTime();

			timeDifference = DateUtil.secondsDifferenceCalculator(requestDatetime, currentDatetime);
		}

		int attempt = lastRequest.getAttempt();
//		int priority = attempt+1;
		model.setAttempt(lastRequest.getAttempt());

		if (reqList.isEmpty() || attempt == 0 || timeDifference > 60) // first Priority -
		{
			System.err.println("ATTEMPT 0 & Time Difference : " + timeDifference);
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 1);
			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);

		} else if (attempt == 1 && timeDifference < 120) // Second Priority -
		{
			System.err.println("ATTEMPT 1 & Time Difference : " + timeDifference);
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 2);
			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);

		} else if (attempt == 2 && timeDifference < 120) // Third Priority -
		{
			System.err.println("ATTEMPT 1 & Time Difference : " + timeDifference);
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 3);
			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);

		} else // Common Priority
		{
			System.err.println("ATTEMPT 3 COMMON");

			VendorModel highSuccessVendor = smartRouteUtils.vendorSuccessRate(vendorVerifyModel);

			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorModelAndVendorVerificationModel(userModel, highSuccessVendor,
							vendorVerifyModel);

			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);
		}
	}

	private ResponseStructure requestVendorRouting(JSONObject userJson, RequestModel model,
			MerchantPriceModel merchantPriceModel, EntityModel userModel, VendorVerificationModel vendorVerifyModel)
			throws Exception {

		VendorModel vendorModel = merchantPriceModel.getVendorModel();
		VendorPriceModel vendorPrice = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(vendorModel,
				vendorVerifyModel);

		ResponseStructure balanceCheck = smartRouteUtils.balanceCheck(userModel, merchantPriceModel,vendorVerifyModel);

		if (balanceCheck.getFlag() == 1) {

			if (vendorModel.getVendorId() == 1) {

				System.err.println("SIGN DESK");
				return signDeskAadharWithOtpVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);

			} else if (vendorModel.getVendorId() == 2) {

				System.err.println("SPRINT V");

				return sprintVerifyAadharWithOtpVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);
			} else if (vendorModel.getVendorId() == 4) {

				System.err.println("SUREPASS");

				return surepassAadharWithOtpVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);
			}
		}
		return balanceCheck;
	}

	private ResponseStructure signDeskAadharWithOtpVerification(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel,
			MerchantPriceModel merchantPriceModel, VendorPriceModel vendorPrice) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		JSONObject inputParams = new JSONObject();
		JSONObject encryptDatas = new JSONObject();

		String userReferenceId = userJson.getString("reference_id");
		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");

		Request request = new Request();
		Date reqDate = new Date();

		inputParams.put("reference_id", userReferenceId);
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

		String responseStatus = decryptJson.getString("status");

		String responseTime = decryptJson.getString("response_time_stamp");

		Response response = new Response();

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);
		
		String maskedAadhaar = FileUtils.getFirstFourChar(source)+"XXXX"+FileUtils.stringSplitter(source,8);

		request.setSource(maskedAadhaar);
		request.setSourceType(sourceType);
		request.setRequestBy(userModel.getName());
		request.setRequestDateAndTime(reqDate);
		request.setStatus(responseStatus);
		request.setResponseDateAndTime(responseTime);
		request.setUser(userModel);
		request.setVerificationModel(vendorVerifyModel);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());
		request.setConsider(true);

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
		response.setVerificationModel(vendorVerifyModel);

		RequestModel temporary = new RequestModel();

		temporary.setResponseDateAndTime(responseTime);
		temporary.setVendorModel(vendorModel);

		String errorCode ="";
		
		if (responseStatus.equals("success")) {

			String referenceId = decryptJson.optString("reference_id", "");
			String message = decryptJson.optString("message", "");
			String transactionId = decryptJson.optString("transaction_id", "");

			request.setMessage(message);
			request.setTransactionId(transactionId);
			request.setAttempt(0);
			request.setReferenceId(referenceId);

			response.setTransactionId(transactionId);
			response.setMessage(message);
			response.setReferenceId(referenceId);

			temporary.setStatus("success");
			temporary.setReferenceId(referenceId);
		} else {

			 errorCode = decryptJson.optString("error_code", "");
			request.setErrorCode(errorCode);
			request.setAttempt(model.getAttempt() + 1);
			request.setReferenceId(userReferenceId);

			response.setErrorCode(errorCode);
			response.setReferenceId(userReferenceId);

			temporary.setStatus("failed");
			temporary.setReferenceId(userReferenceId);
		}

		smartRouteUtils.signDeskErrorCodes(errorCode,vendorModel,userModel,vendorVerifyModel);
		
		JSONObject commonResponse = CommonResponseStructure.commonResponseAadharwithOtp(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !model.isFreeHit()  && smartRouteUtils.signDeskError(errorCode)) {

			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
			
		}else if(!model.isFreeHit() && smartRouteUtils.signDeskError(errorCode)) {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
			
		}else {
			
			request.setConsider(false);
			reqRepository.save(request);
		}

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

	private ResponseStructure sprintVerifyAadharWithOtpVerification(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel,
			MerchantPriceModel merchantPriceModel, VendorPriceModel vendorPrice) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");
		String referenceNumber = userJson.getString("reference_id");

		JSONObject obj = new JSONObject();
		obj.put("id_number", source);

		Request request = new Request();
		Date reqDate = new Date();

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
		
		Response response = new Response();
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setReferenceId(referenceNumber);
		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(vendorVerifyModel);
		request.setResponseDateAndTime(responseTime);
		request.setMessage(message);
		request.setFreeHit(model.isFreeHit());
		request.setConsider(true);

		response.setReferenceId(referenceNumber);
		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(responseTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(vendorVerifyModel);

		RequestModel temporary = new RequestModel();
		temporary.setReferenceId(referenceNumber);
		temporary.setResponseDateAndTime(responseTime);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setVendorModel(vendorModel);

		if (status) {

			JSONObject internalData = wholeData.getJSONObject("data");
			String clientId = internalData.optString("client_id", "");
			boolean otpSent = internalData.getBoolean("otp_sent");
			boolean ifNumber = internalData.getBoolean("if_number");
			boolean validAadhar = internalData.getBoolean("valid_aadhaar");
			String otpGenerationStatus = internalData.optString("status", "");

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

		JSONObject commonResponse = CommonResponseStructure.commonResponseAadharwithOtp(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

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

	private ResponseStructure surepassAadharWithOtpVerification(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel,
			MerchantPriceModel merchantPriceModel, VendorPriceModel vendorPrice) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");
		String referenceNumber = userJson.getString("reference_id");

		JSONObject obj = new JSONObject();
		obj.put("id_number", source);

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

		smartRouteUtils.errorCodes(statusCodeNumber, vendorModel,userModel,vendorVerifyModel);
		
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

		request.setReferenceId(referenceNumber);
		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(vendorVerifyModel);
		request.setResponseDateAndTime(responseTime);
		request.setMessage(message);
		request.setFreeHit(model.isFreeHit());
		request.setConsider(true);

		response.setReferenceId(referenceNumber);
		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(responseTime);
		response.setResponse(surepassResponse);
		response.setVerificationModel(vendorVerifyModel);

		RequestModel temporary = new RequestModel();
		temporary.setReferenceId(referenceNumber);
		temporary.setResponseDateAndTime(responseTime);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setVendorModel(vendorModel);

		if (status) {

			JSONObject internalData = wholeData.getJSONObject("data");
			String clientId = internalData.optString("client_id", "");
			boolean otpSent = internalData.getBoolean("otp_sent");
			boolean ifNumber = internalData.getBoolean("if_number");
			boolean validAadhar = internalData.getBoolean("valid_aadhaar");
			String otpGenerationStatus = internalData.optString("status", "");

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

		JSONObject commonResponse = CommonResponseStructure.commonResponseAadharwithOtp(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

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
