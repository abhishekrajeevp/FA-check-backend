package com.bp.middleware.smartrouteverification;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import com.bp.middleware.duplicateverificationresponse.DrivingLicenseReplica;
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
public class DrivingLicenseVerification {

	
	private final String VERIFICATION_TYPE = AppConstants.DL_VERIFY;
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
	private  CommonResponseStructure CommonResponseStructure;
	@Autowired
	private DrivingLicenseReplica licenseReplica;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;

	public ResponseStructure drivingLicenseVerification(RequestModel model, HttpServletRequest servletRequest) {
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
						.findByVerificationDocument(AppConstants.DL_VERIFY);
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
					
					if(userModel.getApiSandboxKey().equals(apiKey) && userModel.getNoRestriction()==0) {
						
						return licenseReplica.DrivingLicenceDuplicateResponse(model,userModel,vendorVerifyModel);
						
					}else if(userModel.getNoRestriction()>0) {
						
						userModel.setNoRestriction(userModel.getNoRestriction()-1);
						
						model.setFreeHit(true);
					}

					MerchantPriceModel merchantPriceModel = merchantPriceList.get(0);

					ResponseStructure balanceCheck = smartRouteUtils.balanceCheck(userModel, merchantPriceModel,vendorVerifyModel);

					if (balanceCheck.getFlag() == 1) {

						String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
								userModel.getSecretKey());
						JSONObject userJson = new JSONObject(userDecryption);

						String referenceNumber = FileUtils.getRandomOTPnumber(10);
						userJson.put("reference_id", referenceNumber);
						userJson.put("source_type", "id");

						String source = userJson.getString("source");

						System.err.println("SOURCE : " + source);

						Response sourceResponse = smartRouteUtils.sourceCheck(source, userModel, merchantPriceModel);
						System.err.println("Source Response : " + sourceResponse);

						if (sourceResponse.getResponseId() > 0) {
							System.err.println("Source IF");

							Response response = smartRouteUtils.setRequest(sourceResponse, model, merchantPriceModel,
									userModel, vendorVerifyModel, userJson);

							JSONObject jsonSource = new JSONObject(response.getCommonResponse());

							LocalDateTime dateTime = LocalDateTime.now();
							DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
							String responseTime = dateTime.format(format);

							jsonSource.put("response_time", responseTime);
							jsonSource.put("reference_id", referenceNumber);

							String commonResponse = PasswordUtils.demoEncryption(jsonSource, userModel.getSecretKey());

							Map<String, Object> mapNew = new HashMap<>();
							mapNew.put("return_response", commonResponse);

							structure.setData(mapNew);
							structure.setStatusCode(HttpStatus.OK.value());
							structure.setFlag(1);
							structure.setMessage(AppConstants.SUCCESS);

							return structure;

						} else {
							System.err.println("Source ELSE");
							return drivingLicenseVerificationSmartRoute(userJson, model, userModel, vendorVerifyModel);
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

	private ResponseStructure drivingLicenseVerificationSmartRoute(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel) throws Exception {

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
	//	int priority = attempt + 1;

	//	System.out.println("ATTEMPT : " + attempt + "           PRIORITY : " + priority);

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
			System.err.println("ATTEMPT 3 & Time Difference : " + timeDifference);
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 3);
			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);

		} else // Common Priority
		{
			System.err.println("ATTEMPT 4 COMMON");

			VendorModel highSuccessVendor = smartRouteUtils.vendorSuccessRate(vendorVerifyModel);

			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorModelAndVendorVerificationModel(userModel, highSuccessVendor,
							vendorVerifyModel);

			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);
		}
	}

	public ResponseStructure requestVendorRouting(JSONObject userJson, RequestModel model,
			MerchantPriceModel merchantPriceModel, EntityModel userModel, VendorVerificationModel vendorVerifyModel)
			throws Exception {

		VendorModel vendorModel = merchantPriceModel.getVendorModel();
		VendorPriceModel vendorPrice = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(vendorModel,
				vendorVerifyModel);

		ResponseStructure balanceCheck = smartRouteUtils.balanceCheck(userModel, merchantPriceModel,vendorVerifyModel);

		if (balanceCheck.getFlag() == 1) {

			if (vendorModel.getVendorId() == 1) {

				System.err.println("SIGN DESK");
				return signDeskDrivingLicenseVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);

			} else if (vendorModel.getVendorId() == 2) {

				System.err.println("SPRINT V");

				return sprintVerifyDrivingLicenseVerification(userJson, model, userModel, vendorVerifyModel,
						vendorModel, merchantPriceModel, vendorPrice);
			} else if (vendorModel.getVendorId() == 4) {

				System.err.println("SUREPASS");

				return surepassDrivingLicenseVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);
			}
		}
		return balanceCheck;
	}

	private ResponseStructure signDeskDrivingLicenseVerification(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel,
			MerchantPriceModel merchantPriceModel, VendorPriceModel vendorPrice) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String userReferenceId = userJson.getString("reference_id");
		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");
		String dob = userJson.getString("dob");

		Request request = new Request();
		Date reqDate = new Date();

		JSONObject inputParams = new JSONObject();
		JSONObject encryptDatas = new JSONObject();

		inputParams.put("reference_id", userReferenceId);
		inputParams.put("source_type", sourceType);
		inputParams.put("source", source);
		inputParams.put("dob", dob);

		String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);

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

		Response response = new Response();

		// Extract specific data
		String encryptedResponse = jsonObject.getString("encrypted_response");
		//
		// // Print the extracted data
		System.out.println("encrypted_response: " + encryptedResponse);
		//

		String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

		System.err.println("RESPONSE :  " + decryptData);

		JSONObject decryptJson = new JSONObject(decryptData);

		String status = decryptJson.getString("status");
		String responseTimeStamp = decryptJson.getString("response_time_stamp");
		String referenceId = decryptJson.getString("reference_id");
		String message = decryptJson.getString("message");

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setPrice(merchantPriceModel.getIdPrice());
		request.setSource(source);
		request.setStatus(status);
		request.setResponseDateAndTime(responseTimeStamp);
		request.setReferenceId(referenceId);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setUser(userModel);
		request.setMessage(message);
		request.setVerificationModel(vendorVerifyModel);
		request.setFreeHit(model.isFreeHit());
		request.setConsider(true);

		response.setSource(source);
		response.setReferenceId(referenceId);
		response.setStatus(status);
		response.setSourceType(sourceType);
		response.setRequestDateAndTime(reqDate);
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setVendorModel(vendorModel);
		response.setMessage(message);
		response.setResponseDateAndTime(responseTimeStamp);
		response.setVerificationModel(vendorVerifyModel);

		JSONObject object = new JSONObject();
		object.put("status", status);
		object.put("encrypted_response", decryptData);

		RequestModel temporary = new RequestModel();

		temporary.setStatus(status);
		temporary.setReferenceId(referenceId);
		temporary.setResponseDateAndTime(responseTimeStamp);
		temporary.setMessage(message);
		temporary.setVendorModel(vendorModel);

		String errorCode ="";
		
		if (status.equals("success")) {

			String transactionId = decryptJson.getString("transaction_id");

			JSONObject resultJson = decryptJson.getJSONObject("result");

			boolean validDl = resultJson.getBoolean("valid_dl");

			if (validDl) {

				JSONObject validatedJson = resultJson.getJSONObject("validated_data");

				String fullName = validatedJson.getString("name");
				String dateOfBirth = validatedJson.getString("dob");
				String address = validatedJson.getString("permanent_address");
				String state = validatedJson.getString("state");

				String licenceNumber = validatedJson.getString("license_number");
				String permanentZip = validatedJson.getString("permanent_zip");
				String temporaryAddress = validatedJson.getString("temporary_address");
				String temporaryZip = validatedJson.getString("temporary_zip");
				String citizenShip = validatedJson.getString("citizenship");
				String olaName = validatedJson.getString("ola_name");
				String olaCode = validatedJson.getString("ola_code");
				String gender = validatedJson.getString("gender");
				String fatherOrHusbandName = validatedJson.getString("father_or_husband_name");
				String doe = validatedJson.getString("doe");
				String transportDoe = validatedJson.getString("transport_doe");
				String doi = validatedJson.getString("doi");
				String transportDoi = validatedJson.getString("transport_doi");
				String profileImage = validatedJson.getString("profile_image");
				String bloodGroup = validatedJson.getString("blood_group");
				String initialDoi = validatedJson.getString("initial_doi");
				// String currentStatus = validatedJson.getString("current_status");
				boolean hasImage = validatedJson.getBoolean("has_image");
				boolean lessInfo = validatedJson.getBoolean("less_info");
				JSONArray vehicleClasses = validatedJson.getJSONArray("vehicle_classes");
				JSONArray additionalCheck = validatedJson.getJSONArray("additional_check");

				request.setFullName(fullName);
				request.setDob(dateOfBirth);
				request.setState(state);

				response.setFullName(fullName);
				response.setDob(dateOfBirth);
				response.setState(state);
				response.setAddress(address);

				temporary.setFullName(fullName);
				temporary.setDob(dateOfBirth);
				temporary.setAddress(address);
				temporary.setStateName(state);
				temporary.setLicenceNumber(licenceNumber);
				temporary.setPermanentZip(permanentZip);
				temporary.setTemporaryAddress(temporaryAddress);
				temporary.setTemporaryZip(temporaryZip);
				temporary.setCitizenShip(citizenShip);
				temporary.setOlaName(olaName);
				temporary.setOlaCode(olaCode);
				temporary.setGender(gender);
				temporary.setFatherOrHusbandName(fatherOrHusbandName);
				temporary.setDoe(doe);
				temporary.setTransportDoe(transportDoe);
				temporary.setDoi(doi);
				temporary.setTransportDoi(transportDoi);
				temporary.setProfileImage(profileImage);
				temporary.setBloodGroup(bloodGroup);
				temporary.setInitialDoi(initialDoi);
//				temporary.setCurrentStatus(currentStatus);
				temporary.setHasImage(hasImage);
				temporary.setLessInfo(lessInfo);
				temporary.setVehicleClasses(vehicleClasses.toString());
				temporary.setAdditionalCheck(additionalCheck.toString());
			}

			request.setTransactionId(transactionId);
			request.setStatus(status);
			request.setAttempt(0);

			response.setTransactionId(transactionId);
			response.setResponse(decryptData);
			response.setEncryptedJson(encryptedJson);
			response.setRequest(request);

		} else {

			// {"status":"failed","reference_id":"2984265558","response_time_stamp":"2023-08-30T10:42:39","message":"Internal
			// Server Error Occurred","error_code":"kyc_028"}

			// String error = decryptJson.getString("error");
			 errorCode = decryptJson.getString("error_code");

			request.setStatus(status);
			request.setError("Error");
			request.setErrorCode(errorCode);
			request.setAttempt(model.getAttempt() + 1);

			response.setResponse(decryptData);
			response.setError("Error");
			response.setErrorCode(errorCode);
			response.setResponseDateAndTime(responseTimeStamp);
			response.setRequest(request);

			temporary.setError("Error");
		}
		
		smartRouteUtils.signDeskErrorCodes(errorCode,vendorModel,userModel,vendorVerifyModel);
		
		JSONObject commonResponse = CommonResponseStructure.commonResponseDrivingLicense(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

		// Response Count
		int responseCount = userModel.getResponseCount();
		userModel.setResponseCount(++responseCount);
		

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")  && !model.isFreeHit()  && smartRouteUtils.signDeskError(errorCode)) {

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

	private ResponseStructure sprintVerifyDrivingLicenseVerification(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel,
			MerchantPriceModel merchantPriceModel, VendorPriceModel vendorPrice) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String dob = userJson.getString("dob");
		String sourceType = userJson.getString("source_type");

		JSONObject obj = new JSONObject();
		obj.put("id_number", source);
		obj.put("dob", dob);

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

		temporary.setResponseDateAndTime(responseTime);
		temporary.setMessage(message);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setVendorModel(vendorModel);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");

			String clientId = internalData.getString("client_id");
			String state = internalData.getString("state");
			String fullName = internalData.getString("name");
			String address = internalData.getString("permanent_address");
			String dateOfBirth = internalData.getString("dob");

			String licenceNumber = internalData.getString("license_number");
			String permanentZip = internalData.getString("permanent_zip");
			String temporaryAddress = internalData.getString("temporary_address");
			String temporaryZip = internalData.getString("temporary_zip");
			String citizenShip = internalData.getString("citizenship");
			String olaName = internalData.getString("ola_name");
			String olaCode = internalData.getString("ola_code");
			String gender = internalData.getString("gender");
			String fatherOrHusbandName = internalData.getString("father_or_husband_name");
			String doe = internalData.getString("doe");
			String transportDoe = internalData.getString("transport_doe");
			String doi = internalData.getString("doi");
			String transportDoi = internalData.getString("transport_doi");
			String profileImage = internalData.getString("profile_image");
			String bloodGroup = internalData.getString("blood_group");
			String initialDoi = internalData.getString("initial_doi");
//			String currentStatus = internalData.getString("current_status");
			boolean hasImage = internalData.getBoolean("has_image");
			boolean lessInfo = internalData.getBoolean("less_info");
			JSONArray vehicleClasses = internalData.getJSONArray("vehicle_classes");
			JSONArray additionalCheck = internalData.getJSONArray("additional_check");

			request.setReferenceId(referenceId);
			request.setClientId(clientId);
			request.setAttempt(0);
			request.setStatus("success");

			response.setClientId(clientId);
			response.setState(state);
			response.setStatus("success");
			response.setFullName(fullName);
			response.setDob(dateOfBirth);
			response.setAddress(address);

			temporary.setStatus("success");
			temporary.setReferenceId(referenceId);

			temporary.setFullName(fullName);
			temporary.setDob(dateOfBirth);
			temporary.setAddress(address);
			temporary.setStateName(state);
			temporary.setLicenceNumber(licenceNumber);
			temporary.setPermanentZip(permanentZip);
			temporary.setTemporaryAddress(temporaryAddress);
			temporary.setTemporaryZip(temporaryZip);
			temporary.setCitizenShip(citizenShip);
			temporary.setOlaName(olaName);
			temporary.setOlaCode(olaCode);
			temporary.setGender(gender);
			temporary.setFatherOrHusbandName(fatherOrHusbandName);
			temporary.setDoe(doe);
			temporary.setTransportDoe(transportDoe);
			temporary.setDoi(doi);
			temporary.setTransportDoi(transportDoi);
			temporary.setProfileImage(profileImage);
			temporary.setBloodGroup(bloodGroup);
			temporary.setInitialDoi(initialDoi);
//			temporary.setCurrentStatus(currentStatus);
			temporary.setHasImage(hasImage);
			temporary.setLessInfo(lessInfo);
			temporary.setVehicleClasses(vehicleClasses.toString());
			temporary.setAdditionalCheck(additionalCheck.toString());

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(model.getAttempt() + 1);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");

			temporary.setStatus("failed");
			temporary.setReferenceId(FileUtils.getRandomOTPnumber(10));
			temporary.setError("Error");
		}

		JSONObject commonResponse = CommonResponseStructure.commonResponseDrivingLicense(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && statusCodeNumber != 401 && statusCodeNumber != 403 && !model.isFreeHit()) {

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

	private ResponseStructure surepassDrivingLicenseVerification(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel,
			MerchantPriceModel merchantPriceModel, VendorPriceModel vendorPrice) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String dob = userJson.getString("dob");
		String sourceType = userJson.getString("source_type");

		JSONObject obj = new JSONObject();
		obj.put("id_number", source);
		obj.put("dob", dob);

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
			message = wholeData.getString("message_code");
		} else {
			message = wholeData.getString("message");
		}

		Response response = new Response();

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

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

		temporary.setResponseDateAndTime(responseTime);
		temporary.setMessage(message);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setVendorModel(vendorModel);

		if (status) {

			String referenceId = FileUtils.getRandomOTPnumber(10);

			JSONObject internalData = wholeData.getJSONObject("data");

			String clientId = internalData.getString("client_id");
			String state = internalData.getString("state");
			String fullName = internalData.getString("name");
			String address = internalData.getString("permanent_address");
			String dateOfBirth = internalData.getString("dob");

			String licenceNumber = internalData.getString("license_number");
			String permanentZip = internalData.getString("permanent_zip");
			String temporaryAddress = internalData.getString("temporary_address");
			String temporaryZip = internalData.getString("temporary_zip");
			String citizenShip = internalData.getString("citizenship");
			String olaName = internalData.getString("ola_name");
			String olaCode = internalData.getString("ola_code");
			String gender = internalData.getString("gender");
			String fatherOrHusbandName = internalData.getString("father_or_husband_name");
			String doe = internalData.getString("doe");
			String transportDoe = internalData.getString("transport_doe");
			String doi = internalData.getString("doi");
			String transportDoi = internalData.getString("transport_doi");
			String profileImage = internalData.getString("profile_image");
			String bloodGroup = internalData.getString("blood_group");
			String initialDoi = internalData.getString("initial_doi");
//			String currentStatus = internalData.getString("current_status");
			boolean hasImage = internalData.getBoolean("has_image");
			boolean lessInfo = internalData.getBoolean("less_info");
			JSONArray vehicleClasses = internalData.getJSONArray("vehicle_classes");
			JSONArray additionalCheck = internalData.getJSONArray("additional_check");

			request.setReferenceId(referenceId);
			request.setClientId(clientId);
			request.setAttempt(0);
			request.setStatus("success");

			response.setClientId(clientId);
			response.setState(state);
			response.setStatus("success");
			response.setFullName(fullName);
			response.setDob(dateOfBirth);
			response.setAddress(address);

			temporary.setStatus("success");
			temporary.setReferenceId(referenceId);

			temporary.setFullName(fullName);
			temporary.setDob(dateOfBirth);
			temporary.setAddress(address);
			temporary.setStateName(state);
			temporary.setLicenceNumber(licenceNumber);
			temporary.setPermanentZip(permanentZip);
			temporary.setTemporaryAddress(temporaryAddress);
			temporary.setTemporaryZip(temporaryZip);
			temporary.setCitizenShip(citizenShip);
			temporary.setOlaName(olaName);
			temporary.setOlaCode(olaCode);
			temporary.setGender(gender);
			temporary.setFatherOrHusbandName(fatherOrHusbandName);
			temporary.setDoe(doe);
			temporary.setTransportDoe(transportDoe);
			temporary.setDoi(doi);
			temporary.setTransportDoi(transportDoi);
			temporary.setProfileImage(profileImage);
			temporary.setBloodGroup(bloodGroup);
			temporary.setInitialDoi(initialDoi);
//			temporary.setCurrentStatus(currentStatus);
			temporary.setHasImage(hasImage);
			temporary.setLessInfo(lessInfo);
			temporary.setVehicleClasses(vehicleClasses.toString());
			temporary.setAdditionalCheck(additionalCheck.toString());

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(model.getAttempt() + 1);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");

			temporary.setStatus("failed");
			temporary.setReferenceId(FileUtils.getRandomOTPnumber(10));
			temporary.setError("Error");
		}

		JSONObject commonResponse = CommonResponseStructure.commonResponseDrivingLicense(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && statusCodeNumber != 401 && statusCodeNumber != 403 && !model.isFreeHit()) {

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
