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
import com.bp.middleware.duplicateverificationresponse.RcReplica;
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
public class RcVerification {

	
	private final String VERIFICATION_TYPE = AppConstants.RC_VERIFY;
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
	private RcReplica rcReplica;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;

	public ResponseStructure rcVerification(RequestModel model, HttpServletRequest servletRequest) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);
			
			if (userModel == null) {
				userModel = userRepository.findByApiSandboxKeyAndApplicationId(apiKey, applicationId);
			}

			if (userModel!=null && userModel.isAccountStatus()) {

				ENTITY=userModel;
				
				VendorVerificationModel vendorVerifyModel = vendorVerificationRepository
						.findByVerificationDocument(AppConstants.RC_VERIFY);

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
					
					if (userModel.getApiSandboxKey().equals(apiKey) && userModel.getNoRestriction()==0) {

						return rcReplica.rcDuplicateResponse(model, userModel,vendorVerifyModel);
						
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
						System.err.println("Source Response : " + (sourceResponse.getResponseId() < 1));

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
							return rcVerificationSmartRoute(userJson, model, userModel, vendorVerifyModel);
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

	private ResponseStructure rcVerificationSmartRoute(JSONObject userJson, RequestModel model, EntityModel userModel,
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

		model.setAttempt(lastRequest.getAttempt());

		if (reqList.isEmpty() || lastRequest.getAttempt() == 0 || timeDifference > 60) // first Priority
		{
			System.err.println("ATTEMPT 1");
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 1);
			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);

		} else if (lastRequest.getAttempt() == 1 && timeDifference < 120) // Second Priority
		{
			System.err.println("ATTEMPT 2");
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 2);
			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);

		} 
		else if (lastRequest.getAttempt() == 2 && timeDifference < 140) // Third Priority
		{
			System.err.println("ATTEMPT 3");
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 3);
			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);

		}
		else // Common Priority
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

//			if (merchantPriceModel.getVendorModel().getVendorId() == 1) {
//
//				System.err.println("SIGN DESK");
//				return signDeskRcVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
//						merchantPriceModel, vendorPrice);
//
//			} 
			
			if (merchantPriceModel.getVendorModel().getVendorId() == 2) {

				System.err.println("SPRINT V");

				return sprintVerifyRcVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);
			} 
			else if (merchantPriceModel.getVendorModel().getVendorId() == 4) {

				System.err.println("SUREPASS");

				return surepassRcVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);
			}
		}
		return balanceCheck;
	}


	private ResponseStructure signDeskRcVerification(JSONObject userJson, RequestModel model, EntityModel userModel,
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

		// set request details

		inputParams.put("reference_id", userReferenceId);
		inputParams.put("source_type", sourceType);
		inputParams.put("source", source);

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

		Response response = new Response();

		System.out.println("JSON OBJ : " + jsonObject);

		// Extract specific data
		String encryptedResponse = jsonObject.getString("encrypted_response");
		
		String statusOfResponse = jsonObject.optString("status", "");

		if (statusOfResponse.equalsIgnoreCase("failed")) {

			String errorCode = jsonObject.optString("error_code", "");

			if (errorCode.equalsIgnoreCase("vcip_017")) {

				String error = jsonObject.optString("error", "");

				throw new InvalidApiKeyOrApplicationIdException("SIGN DESK : " + error, 522);
			}
		}

		System.out.println("encrypted_response: " + encryptedResponse);

		String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

		JSONObject decryptJson = new JSONObject(decryptData);

		String status = decryptJson.getString("status");
		String responseTimeStamp = decryptJson.getString("response_time_stamp");
		String referenceId = decryptJson.getString("reference_id");

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setStatus(status);
		request.setResponseDateAndTime(responseTimeStamp);
		request.setReferenceId(referenceId);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
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
		response.setResponseDateAndTime(responseTimeStamp);
		response.setVerificationModel(vendorVerifyModel);

		JSONObject object = new JSONObject();
		object.put("status", status);
		object.put("encrypted_response", decryptData);

		String returnResponse = PasswordUtils.demoEncryption(object, userModel.getSecretKey());
 
		RequestModel temporary = new RequestModel();
		
		String errorCode = "";
		
		if (status.equals("success")) {

			String message = decryptJson.getString("message");
			String transactionId = decryptJson.getString("transaction_id");

			JSONObject resultJson = decryptJson.getJSONObject("result");
			JSONObject validatedJson = resultJson.getJSONObject("validated_data");

			String address = validatedJson.getString("permanent_address");
			String fullName = validatedJson.getString("owner_name");

			request.setMessage(message);
			request.setTransactionId(transactionId);
			request.setStatus(status);
			request.setFullName(fullName);
			request.setAttempt(0);
			reqRepository.save(request);

			response.setFullName(fullName);
			response.setAddress(address);
			response.setTransactionId(transactionId);
			response.setMessage(message);
			response.setResponse(decryptData);
			response.setEncryptedJson(encryptedJson);
			response.setRequest(request);
			respRepository.save(response);

		} else {

			String error = decryptJson.getString("error");
			errorCode = decryptJson.getString("error_code");

			request.setStatus(status);
			request.setError(error);
			request.setErrorCode(errorCode);
			request.setAttempt(model.getAttempt() + 1);
			reqRepository.save(request);

			response.setResponse(decryptData);
			response.setError(error);
			response.setErrorCode(errorCode);
			response.setRequest(request);

			respRepository.save(response);

		}
		
		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !model.isFreeHit()   && smartRouteUtils.signDeskError(errorCode)) {

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

		smartRouteUtils.signDeskErrorCodes(errorCode,vendorModel,userModel,vendorVerifyModel);
		
		JSONObject commonResponse = CommonResponseStructure.commonResponseRc(temporary);

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", commonResponse);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		userRepository.save(userModel);

		return structure;
	}

	private ResponseStructure sprintVerifyRcVerification(JSONObject userJson, RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPrice) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");

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
			String clientId = internalData.optString("client_id","");

			String rcNumber = internalData.optString("rc_number","");
			String registeredDate = internalData.optString("registration_date","");
			String ownerName = internalData.optString("owner_name","");
			String fatherName = internalData.optString("father_name","");
			String presentAddress = internalData.optString("present_address","");
			String permanentAddress = internalData.optString("permanent_address","");
			String mobileNumber = internalData.optString("mobile_number","");
			String vehicleCategory = internalData.optString("vehicle_category","");
			String vehicleChasisNumber = internalData.optString("vehicle_chasi_number","");
			String vehicleEngineNumber = internalData.optString("vehicle_engine_number","");
			String makerDescription = internalData.optString("maker_description","");
			String makerModel = internalData.optString("maker_model","");
			String bodyType = internalData.optString("body_type","");
			String fuelType = internalData.optString("fuel_type","");
			String color = internalData.optString("color","");
			String normsType = internalData.optString("norms_type","");
			String fitUpTo = internalData.optString("fit_up_to","");
			String financer = internalData.optString("financer","");
			String insuranceCompany = internalData.optString("insurance_company","");
			String insurancePolicyNumber = internalData.optString("insurance_policy_number","");
			String insuranceUpto = internalData.optString("insurance_upto","");
			String manufacturingDate = internalData.optString("manufacturing_date","");
			String manufacturingDateFormat = internalData.optString("manufacturing_date_formatted","");
			String registeredAt = internalData.optString("registered_at","");
			String latestBy = internalData.optString("latest_by","");
			String taxUpto = internalData.optString("tax_upto","");
			String taxPaidUpto = internalData.optString("tax_paid_upto","");
			String cubicCapacity = internalData.optString("cubic_capacity","");
			String vehicleGrossWeight = internalData.optString("vehicle_gross_weight","");
			String noOfCylinders = internalData.optString("no_cylinders","");
			String seatCapacity = internalData.optString("seat_capacity","");
			String sleeperCapacity = internalData.optString("sleeper_capacity","");
			String standingCapacity = internalData.optString("standing_capacity","");
			String wheelBase = internalData.optString("wheelbase","");
			String unladenWeight = internalData.optString("unladen_weight","");
			String vehicleCategoryDescription = internalData.optString("vehicle_category_description","");
			String puccNumber = internalData.optString("pucc_number","");
			String puccUpto = internalData.optString("pucc_upto","");
			String permitNumber = internalData.optString("permit_number","");
			String permitIssueDate = internalData.optString("permit_issue_date","");
			String permitValidFrom = internalData.optString("permit_valid_from","");
			String permitValidUpto = internalData.optString("permit_valid_upto","");
			String permitType = internalData.optString("permit_type","");
			String nationalPermitNumber = internalData.optString("national_permit_number","");
			String nationalPermitUpto = internalData.optString("national_permit_upto","");
			String nationalPermitIssuedBy = internalData.optString("national_permit_issued_by","");
			String nonUseStatus = internalData.optString("non_use_status","");
			String nonUseFrom = internalData.optString("non_use_from","");
			String nonUseTo = internalData.optString("non_use_to","");
			String blackListStatus = internalData.optString("blacklist_status","");
			String nocDetails = internalData.optString("noc_details","");
			String ownerNumber = internalData.optString("owner_number","");
			String rcStatus = internalData.optString("rc_status","");
			String variant = internalData.optString("variant","");
			String challanDetails = internalData.optString("challan_details","");
			
			boolean financed =internalData.getBoolean("financed");
			boolean lessInfo =internalData.getBoolean("less_info");
			boolean maskedName =internalData.getBoolean("masked_name");
			
			request.setReferenceId(referenceId);
			request.setFullName(ownerName);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setFullName(ownerName);
			response.setAddress(permanentAddress);
			response.setDateOfRegistration(registeredDate);
			response.setStatus("success");
			response.setReferenceId(referenceId);
			
			temporary.setRcNumber(rcNumber);
			temporary.setDateOfRegistration(registeredDate);
			temporary.setOwnerName(ownerName);
			temporary.setFatherName(fatherName);
			temporary.setPresentAddress(presentAddress);
			temporary.setPermanentAddress(permanentAddress);
			temporary.setMobileNumber(mobileNumber);
			temporary.setVehicleCategory(vehicleCategory);
			temporary.setVehicleChasisNumber(vehicleChasisNumber);
			temporary.setVehicleEngineNumber(vehicleEngineNumber);
			temporary.setMakerDescription(makerDescription);
			temporary.setMakerModel(makerModel);
			temporary.setBodyType(bodyType);
			temporary.setFuelType(fuelType);
			temporary.setColor(color);
			temporary.setNormsType(normsType);
			temporary.setFitUpTo(fitUpTo);
			temporary.setFinancer(financer);
			temporary.setInsuranceCompany(insuranceCompany);
			temporary.setInsurancePolicyNumber(insurancePolicyNumber);
			temporary.setInsuranceUpto(insuranceUpto);
			temporary.setManufacturingDate(manufacturingDate);
			temporary.setManufacturingDateForma(manufacturingDateFormat);
			temporary.setRegisteredAt(registeredAt);
			temporary.setLatestBy(latestBy);
			temporary.setTaxUpto(taxUpto);
			temporary.setTaxPaidUpto(taxPaidUpto);
			temporary.setCubicCapacity(cubicCapacity);
			temporary.setVehicleGrossWeight(vehicleGrossWeight);
			temporary.setNoOfCylinders(noOfCylinders);
			temporary.setSeatCapacity(seatCapacity);
			temporary.setSleeperCapacity(sleeperCapacity);
			temporary.setStandingCapacity(standingCapacity);
			temporary.setWheelBase(wheelBase);
			temporary.setUnladenWeight(unladenWeight);
			temporary.setVehicleCategoryDescription(vehicleCategoryDescription);
			temporary.setPuccNumber(puccNumber);
			temporary.setPuccUpto(puccUpto);
			temporary.setPermitNumber(permitNumber);
			temporary.setPermitIssueDate(permitIssueDate);
			temporary.setPermitValidFrom(permitValidFrom);
			temporary.setPermitValidUpto(permitValidUpto);
			temporary.setPermitType(permitType);
			temporary.setNationalPermitNumber(nationalPermitNumber);
			temporary.setNationalPermitUpto(nationalPermitUpto);
			temporary.setNationalPermitIssuedBy(nationalPermitIssuedBy);
			temporary.setNonUseStatus(nonUseStatus);
			temporary.setNonUseFrom(nonUseFrom);
			temporary.setNonUseTo(nonUseTo);
			temporary.setBlackListStatus(blackListStatus);
			temporary.setNocDetails(nocDetails);
			temporary.setOwnerNumber(ownerNumber);
			temporary.setRcStatus(rcStatus);
			temporary.setVariant(variant);
			temporary.setChallanDetails(challanDetails);
			temporary.setFinanced(financed);
			temporary.setLessInfo(lessInfo);
			temporary.setMaskedNamePresent(maskedName);

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
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && statusCodeNumber != 401 && statusCodeNumber != 403 && !model.isFreeHit()) {

			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else if(statusCodeNumber!=401 && statusCodeNumber!=403 && !model.isFreeHit()) {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		JSONObject commonResponse = CommonResponseStructure.commonResponseRc(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);
		
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
	
	
	
	private ResponseStructure surepassRcVerification(JSONObject userJson, RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPrice) throws Exception{
		
		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");

		Request request = new Request();
		Date reqDate = new Date();

		JSONObject obj = new JSONObject();
		obj.put("id_number", source);

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
			String clientId = internalData.optString("client_id","");

			String rcNumber = internalData.optString("rc_number","");
			String registeredDate = internalData.optString("registration_date","");
			String ownerName = internalData.optString("owner_name","");
			String fatherName = internalData.optString("father_name","");
			String presentAddress = internalData.optString("present_address","");
			String permanentAddress = internalData.optString("permanent_address","");
			String mobileNumber = internalData.optString("mobile_number","");
			String vehicleCategory = internalData.optString("vehicle_category","");
			String vehicleChasisNumber = internalData.optString("vehicle_chasi_number","");
			String vehicleEngineNumber = internalData.optString("vehicle_engine_number","");
			String makerDescription = internalData.optString("maker_description","");
			String makerModel = internalData.optString("maker_model","");
			String bodyType = internalData.optString("body_type","");
			String fuelType = internalData.optString("fuel_type","");
			String color = internalData.optString("color","");
			String normsType = internalData.optString("norms_type","");
			String fitUpTo = internalData.optString("fit_up_to","");
			String financer = internalData.optString("financer","");
			String insuranceCompany = internalData.optString("insurance_company","");
			String insurancePolicyNumber = internalData.optString("insurance_policy_number","");
			String insuranceUpto = internalData.optString("insurance_upto","");
			String manufacturingDate = internalData.optString("manufacturing_date","");
			String manufacturingDateFormat = internalData.optString("manufacturing_date_formatted","");
			String registeredAt = internalData.optString("registered_at","");
			String latestBy = internalData.optString("latest_by","");
			String taxUpto = internalData.optString("tax_upto","");
			String taxPaidUpto = internalData.optString("tax_paid_upto","");
			String cubicCapacity = internalData.optString("cubic_capacity","");
			String vehicleGrossWeight = internalData.optString("vehicle_gross_weight","");
			String noOfCylinders = internalData.optString("no_cylinders","");
			String seatCapacity = internalData.optString("seat_capacity","");
			String sleeperCapacity = internalData.optString("sleeper_capacity","");
			String standingCapacity = internalData.optString("standing_capacity","");
			String wheelBase = internalData.optString("wheelbase","");
			String unladenWeight = internalData.optString("unladen_weight","");
			String vehicleCategoryDescription = internalData.optString("vehicle_category_description","");
			String puccNumber = internalData.optString("pucc_number","");
			String puccUpto = internalData.optString("pucc_upto","");
			String permitNumber = internalData.optString("permit_number","");
			String permitIssueDate = internalData.optString("permit_issue_date","");
			String permitValidFrom = internalData.optString("permit_valid_from","");
			String permitValidUpto = internalData.optString("permit_valid_upto","");
			String permitType = internalData.optString("permit_type","");
			String nationalPermitNumber = internalData.optString("national_permit_number","");
			String nationalPermitUpto = internalData.optString("national_permit_upto","");
			String nationalPermitIssuedBy = internalData.optString("national_permit_issued_by","");
			String nonUseStatus = internalData.optString("non_use_status","");
			String nonUseFrom = internalData.optString("non_use_from","");
			String nonUseTo = internalData.optString("non_use_to","");
			String blackListStatus = internalData.optString("blacklist_status","");
			String nocDetails = internalData.optString("noc_details","");
			String ownerNumber = internalData.optString("owner_number","");
			String rcStatus = internalData.optString("rc_status","");
			String variant = internalData.optString("variant","");
			String challanDetails = internalData.optString("challan_details","");

			boolean financed =internalData.getBoolean("financed");
			boolean lessInfo =internalData.getBoolean("less_info");
			boolean maskedName =internalData.getBoolean("masked_name");
			
			request.setReferenceId(referenceId);
			request.setFullName(ownerName);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setFullName(ownerName);
			response.setAddress(permanentAddress);
			response.setDateOfRegistration(registeredDate);
			response.setStatus("success");
			response.setReferenceId(referenceId);

			temporary.setRcNumber(rcNumber);
			temporary.setDateOfRegistration(registeredDate);
			temporary.setOwnerName(ownerName);
			temporary.setFatherName(fatherName);
			temporary.setPresentAddress(presentAddress);
			temporary.setPermanentAddress(permanentAddress);
			temporary.setMobileNumber(mobileNumber);
			temporary.setVehicleCategory(vehicleCategory);
			temporary.setVehicleChasisNumber(vehicleChasisNumber);
			temporary.setVehicleEngineNumber(vehicleEngineNumber);
			temporary.setMakerDescription(makerDescription);
			temporary.setMakerModel(makerModel);
			temporary.setBodyType(bodyType);
			temporary.setFuelType(fuelType);
			temporary.setColor(color);
			temporary.setNormsType(normsType);
			temporary.setFitUpTo(fitUpTo);
			temporary.setFinancer(financer);
			temporary.setInsuranceCompany(insuranceCompany);
			temporary.setInsurancePolicyNumber(insurancePolicyNumber);
			temporary.setInsuranceUpto(insuranceUpto);
			temporary.setManufacturingDate(manufacturingDate);
			temporary.setManufacturingDateForma(manufacturingDateFormat);
			temporary.setRegisteredAt(registeredAt);
			temporary.setLatestBy(latestBy);
			temporary.setTaxUpto(taxUpto);
			temporary.setTaxPaidUpto(taxPaidUpto);
			temporary.setCubicCapacity(cubicCapacity);
			temporary.setVehicleGrossWeight(vehicleGrossWeight);
			temporary.setNoOfCylinders(noOfCylinders);
			temporary.setSeatCapacity(seatCapacity);
			temporary.setSleeperCapacity(sleeperCapacity);
			temporary.setStandingCapacity(standingCapacity);
			temporary.setWheelBase(wheelBase);
			temporary.setUnladenWeight(unladenWeight);
			temporary.setVehicleCategoryDescription(vehicleCategoryDescription);
			temporary.setPuccNumber(puccNumber);
			temporary.setPuccUpto(puccUpto);
			temporary.setPermitNumber(permitNumber);
			temporary.setPermitIssueDate(permitIssueDate);
			temporary.setPermitValidFrom(permitValidFrom);
			temporary.setPermitValidUpto(permitValidUpto);
			temporary.setPermitType(permitType);
			temporary.setNationalPermitNumber(nationalPermitNumber);
			temporary.setNationalPermitUpto(nationalPermitUpto);
			temporary.setNationalPermitIssuedBy(nationalPermitIssuedBy);
			temporary.setNonUseStatus(nonUseStatus);
			temporary.setNonUseFrom(nonUseFrom);
			temporary.setNonUseTo(nonUseTo);
			temporary.setBlackListStatus(blackListStatus);
			temporary.setNocDetails(nocDetails);
			temporary.setOwnerNumber(ownerNumber);
			temporary.setRcStatus(rcStatus);
			temporary.setVariant(variant);
			temporary.setChallanDetails(challanDetails);
			temporary.setFinanced(financed);
			temporary.setLessInfo(lessInfo);
			temporary.setMaskedNamePresent(maskedName);
			
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
		
		JSONObject commonResponse = CommonResponseStructure.commonResponseRc(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
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
