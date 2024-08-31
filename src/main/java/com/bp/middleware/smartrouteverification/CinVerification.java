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
import com.bp.middleware.duplicateverificationresponse.CinReplica;
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
public class CinVerification {

	private final String VERIFICATION_TYPE = AppConstants.CIN_VERIFY;
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
	private CinReplica cinReplica;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;

	public ResponseStructure cinVerification(RequestModel model, HttpServletRequest servletRequest) {
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
						.findByVerificationDocument(AppConstants.CIN_VERIFY);

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

						return cinReplica.cinDuplicateResponse(model, userModel, vendorVerifyModel);

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
							return cinVerificationSmartRoute(userJson, model, userModel, vendorVerifyModel);
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

	private ResponseStructure cinVerificationSmartRoute(JSONObject userJson, RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel) throws Exception {

		List<Request> reqList = reqRepository.findByUserAndVerificationModel(userModel, vendorVerifyModel);

		Request lastRequest = new Request();

		long timeDifference = 0;
		if (!reqList.isEmpty()) {
			lastRequest = reqList.get(reqList.size() - 1);

			Date currentDatetime = new Date();
			Date requestDatetime = lastRequest.getRequestDateAndTime();

			timeDifference = DateUtil.secondsDifferenceCalculator(requestDatetime, currentDatetime);
		}

		int attempt = lastRequest.getAttempt();
		// int priority = attempt + 1;

		model.setAttempt(attempt);

		if (reqList.isEmpty() || attempt == 0 || timeDifference > 60) // first Priority -
		{
			System.err.println("ATTEMPT 0 & Time Diff : " + timeDifference);
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 1);

			// System.err.println("MPM : "+merchantPriority.getMerchantPriceId());

			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);

		} else if (attempt == 2 && timeDifference < 150) { // third Priority -
			System.err.println("ATTEMPT 2 & Time Diff : " + timeDifference);
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 3);
			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);

		} else if (attempt == 1 && timeDifference < 120) // Second Priority -
		{
			System.err.println("ATTEMPT 1 & Time Diff : " + timeDifference);
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 2);
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

		ResponseStructure structure = new ResponseStructure();

		VendorModel vendorModel = merchantPriceModel.getVendorModel();
		VendorPriceModel vendorPrice = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(vendorModel,
				vendorVerifyModel);

		ResponseStructure balanceCheck = smartRouteUtils.balanceCheck(userModel, merchantPriceModel,vendorVerifyModel);

		if (balanceCheck.getFlag() == 1) {

			if (vendorModel.getVendorId() == 1) {

				System.err.println("SIGN DESK");
				return signDeskCinVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);

			} else if (vendorModel.getVendorId() == 2) {

				System.err.println("SPRINT V");

				return sprintVerifyCinVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);

			} else if (vendorModel.getVendorId() == 4) {

				System.err.println("SUREPASS");

				return surepassCinVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);
			}
		}
		return balanceCheck;
	}

	private ResponseStructure signDeskCinVerification(JSONObject userJson, RequestModel model, EntityModel userModel,
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
		headers.add("x-parse-application-id",vendorPrice.getApplicationId()); 
		headers.add("Content-Type", AppConstants.CONTENT_TYPE);


		HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

		ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
				String.class);
		String data = clientResponse.getBody();
		
		System.err.println("dt : "+data);

		JSONObject jsonObject = new JSONObject(data);
		
		// VendorReq
		vendorModel.setVendorRequest(vendorModel.getVendorRequest()+1);
		// VendorResponse
		vendorModel.setVendorResponse(vendorModel.getVendorResponse()+1);
		// MonthlyCount
		vendorModel.setMonthlyCount(vendorModel.getMonthlyCount()+1);

		vendorRepository.save(vendorModel);

		String statusOfResponse = jsonObject.optString("status", "");

		if (statusOfResponse.equalsIgnoreCase("failed")) {

			String errorCode = jsonObject.optString("error_code", "");

			if (errorCode.equalsIgnoreCase("vcip_017")) {

				String error = jsonObject.optString("error", "");

				throw new InvalidApiKeyOrApplicationIdException("SIGN DESK : " + error, 522);
			}
		}

		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		Response response = new Response();

		// Extract specific data
		String encryptedResponse = jsonObject.getString("encrypted_response");
		//
		// // Print the extracted data
		System.out.println("encrypted_response: " + encryptedResponse);
		//

		String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);
		// System.out.println("RESPONSE : " + decryptData);

		JSONObject decryptJson = new JSONObject(decryptData);

		String status = decryptJson.getString("status");
		String responseTimeStamp = decryptJson.getString("response_time_stamp");
		String referenceId = decryptJson.getString("reference_id");

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setStatus(status);
		request.setResponseDateAndTime(responseTimeStamp);
		request.setReferenceId(referenceId);
		request.setSource(source);
		request.setSourceType(sourceType);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setUser(userModel);
		request.setVerificationModel(vendorVerifyModel);
		request.setFreeHit(model.isFreeHit());
		request.setConsider(true);

		response.setReferenceId(referenceId);
		response.setStatus(status);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(reqDate);
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(responseTimeStamp);
		response.setVerificationModel(vendorVerifyModel);

		JSONObject object = new JSONObject();
		object.put("status", status);
		object.put("encrypted_response", decryptData);

		RequestModel temporary = new RequestModel();

		temporary.setStatus(status);
		temporary.setReferenceId(referenceId);
		temporary.setResponseDateAndTime(responseTimeStamp);
		temporary.setVendorModel(vendorModel);

		String errorCode ="";
		
		if (status.equals("success")) {

			String message = decryptJson.getString("message");
			String transactionId = decryptJson.getString("transaction_id");

			JSONObject resultJson = decryptJson.getJSONObject("result");

			boolean validCin = resultJson.getBoolean("valid_cin");

			if (validCin) {

				JSONObject validatedJson = resultJson.getJSONObject("validated_data");

				String companyName = validatedJson.optString("company_name", "");
				String companyId = validatedJson.optString("company_id", "");
				String companyType = validatedJson.optString("company_type", "");

				JSONObject details = validatedJson.getJSONObject("details");
				JSONObject companyInfo = details.getJSONObject("company_info");

				String cin = companyInfo.optString("cin", "");
				String rocCode = companyInfo.optString("roc_code", "");
				String registrationNumber = companyInfo.optString("registration_number", "");
				String companyCategory = companyInfo.optString("company_category", "");
				String classOfCompany = companyInfo.optString("class_of_company", "");
				String companySubCategory = companyInfo.optString("company_sub_category", "");
				String authorizedCapital = companyInfo.optString("authorized_capital", "");
				String paidUpCapital = companyInfo.optString("paid_up_capital", "");
				String numberOfMembers = companyInfo.optString("number_of_members", "");
				String dateOfIncorporation = companyInfo.optString("date_of_incorporation", "");
				String registeredAddress = companyInfo.optString("registered_address", "");
				String addressOtherThanRo = companyInfo.optString("address_other_than_ro", "");
				String email = companyInfo.optString("email_id", "");
				String listedStatus = companyInfo.optString("listed_status", "");
				String activeCompilance = companyInfo.optString("active_compliance", "");
				String suspendedAtStockExchange = companyInfo.optString("suspended_at_stock_exchange", "");
				String lastAgmDate = companyInfo.optString("last_agm_date", "");
				String lastBsDate = companyInfo.optString("last_bs_date", "");
				String companyStatus = companyInfo.optString("company_status", "");
				String statusUnderCirp = companyInfo.optString("status_under_cirp", "");

				JSONArray directors = details.getJSONArray("directors");
				JSONArray charges = details.getJSONArray("charges");

				request.setCompanyName(companyName);
				request.setCompanyId(companyId);
				request.setCompanyType(companyType);
				request.setEmail(email);

				response.setBusinessName(companyName);
				response.setCompanyId(companyId);
				response.setCompanyType(companyType);
				response.setEmail(email);

				temporary.setCompanyId(companyId);
				temporary.setCompanyType(companyType);
				temporary.setCompanyName(companyName);

				temporary.setCin(cin);
				temporary.setRocCode(rocCode);
				temporary.setRegistrationNumber(registrationNumber);
				temporary.setCompanyCategory(companyCategory);
				temporary.setClassOfCompany(classOfCompany);
				temporary.setCompanySubCategory(companySubCategory);
				temporary.setAuthorizedCapital(authorizedCapital);
				temporary.setPaidUpCapital(paidUpCapital);
				temporary.setNumberOfMembers(numberOfMembers);
				temporary.setDateOfIncorporation(dateOfIncorporation);
				temporary.setRegisteredAddress(registeredAddress);
				temporary.setAddressOtherThanRo(addressOtherThanRo);
				temporary.setEmail(email);
				temporary.setListedStatus(listedStatus);
				temporary.setActiveCompilance(activeCompilance);
				temporary.setSuspendedAtStockExchange(suspendedAtStockExchange);
				temporary.setLastAgmDate(lastAgmDate);
				temporary.setLastBsDate(lastBsDate);
				temporary.setCompanyStatus(companyStatus);
				temporary.setStatusUnderCirp(statusUnderCirp);
				temporary.setDirectors(directors);
				temporary.setCharges(charges);
			}

			request.setMessage(message);
			request.setTransactionId(transactionId);
			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setAttempt(0);
			reqRepository.save(request);

			response.setTransactionId(transactionId);
			response.setMessage(message);
			response.setResponse(decryptData);
			response.setEncryptedJson(encryptedJson);
			response.setRequest(request);
			respRepository.save(response);

			temporary.setMessage(message);
			temporary.setStatus("success");
			temporary.setReferenceId(referenceId);

		} else {

			String error = decryptJson.getString("error");
			errorCode = decryptJson.getString("error_code");

			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setAttempt(model.getAttempt() + 1);
			request.setError(error);
			request.setErrorCode(errorCode);
			reqRepository.save(request);

			response.setResponse(decryptData);
			response.setError(error);
			response.setErrorCode(errorCode);
			response.setResponseDateAndTime(responseTimeStamp);
			response.setRequest(request);
			respRepository.save(response);

			temporary.setError(error);
		}

		smartRouteUtils.signDeskErrorCodes(errorCode,vendorModel,userModel,vendorVerifyModel);
		
		JSONObject commonResponse = CommonResponseStructure.commonResponseCin(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

		
		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !model.isFreeHit() && smartRouteUtils.signDeskError(errorCode)) {

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

	private ResponseStructure sprintVerifyCinVerification(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel,
			MerchantPriceModel merchantPriceModel, VendorPriceModel vendorPrice) throws Exception {

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

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");

			String clientId = internalData.optString("client_id", "");
			String companyId = internalData.optString("company_id", "");
			String companyType = internalData.optString("company_type", "");
			String companyName = internalData.optString("company_name", "");

			JSONObject details = internalData.getJSONObject("details");
			JSONObject companyInfo = details.getJSONObject("company_info");

			String cin = companyInfo.optString("cin", "");
			String rocCode = companyInfo.optString("roc_code", "");
			String registrationNumber = companyInfo.optString("registration_number", "");
			String companyCategory = companyInfo.optString("company_category", "");
			String classOfCompany = companyInfo.optString("class_of_company", "");
			String companySubCategory = companyInfo.optString("company_sub_category", "");
			String authorizedCapital = companyInfo.optString("authorized_capital", "");
			String paidUpCapital = companyInfo.optString("paid_up_capital", "");
			String numberOfMembers = companyInfo.optString("number_of_members", "");
			String dateOfIncorporation = companyInfo.optString("date_of_incorporation", "");
			String registeredAddress = companyInfo.optString("registered_address", "");
			String addressOtherThanRo = companyInfo.optString("address_other_than_ro", "");
			String email = companyInfo.optString("email_id", "");
			String listedStatus = companyInfo.optString("listed_status", "");
			String activeCompilance = companyInfo.optString("active_compliance", "");
			String suspendedAtStockExchange = companyInfo.optString("suspended_at_stock_exchange", "");
			String lastAgmDate = companyInfo.optString("last_agm_date", "");
			String lastBsDate = companyInfo.optString("last_bs_date", "");
			String companyStatus = companyInfo.optString("company_status", "");
			String statusUnderCirp = companyInfo.optString("status_under_cirp", "");

			JSONArray directors = details.getJSONArray("directors");
			JSONArray charges = details.getJSONArray("charges");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setCompanyId(companyId);
			request.setCompanyType(companyType);
			request.setCompanyName(companyName);
			request.setEmail(email);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setCompanyId(companyId);
			response.setCompanyType(companyType);
			response.setBusinessName(companyName);// CompanyName->BusinessName
			response.setEmail(email);
			response.setStatus("success");
			response.setReferenceId(referenceId);

			temporary.setStatus("success");
			temporary.setReferenceId(referenceId);
			temporary.setCompanyId(companyId);
			temporary.setCompanyType(companyType);
			temporary.setCompanyName(companyName);

			temporary.setCin(cin);
			temporary.setRocCode(rocCode);
			temporary.setRegistrationNumber(registrationNumber);
			temporary.setCompanyCategory(companyCategory);
			temporary.setClassOfCompany(classOfCompany);
			temporary.setCompanySubCategory(companySubCategory);
			temporary.setAuthorizedCapital(authorizedCapital);
			temporary.setPaidUpCapital(paidUpCapital);
			temporary.setNumberOfMembers(numberOfMembers);
			temporary.setDateOfIncorporation(dateOfIncorporation);
			temporary.setRegisteredAddress(registeredAddress);
			temporary.setAddressOtherThanRo(addressOtherThanRo);
			temporary.setEmail(email);
			temporary.setListedStatus(listedStatus);
			temporary.setActiveCompilance(activeCompilance);
			temporary.setSuspendedAtStockExchange(suspendedAtStockExchange);
			temporary.setLastAgmDate(lastAgmDate);
			temporary.setLastBsDate(lastBsDate);
			temporary.setCompanyStatus(companyStatus);
			temporary.setStatusUnderCirp(statusUnderCirp);
			temporary.setDirectors(directors);
			temporary.setCharges(charges);

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

		JSONObject commonResponse = CommonResponseStructure.commonResponseCin(temporary);
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

	private ResponseStructure surepassCinVerification(JSONObject userJson, RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPrice) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");

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
		response.setVendorModel(vendorModel);
		response.setRequest(request);
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

			String clientId = internalData.optString("client_id", "");
			String companyId = internalData.optString("company_id", "");
			String companyType = internalData.optString("company_type", "");
			String companyName = internalData.optString("company_name", "");

			JSONObject details = internalData.getJSONObject("details");
			JSONObject companyInfo = details.getJSONObject("company_info");

			String cin = companyInfo.optString("cin", "");
			String rocCode = companyInfo.optString("roc_code", "");
			String registrationNumber = companyInfo.optString("registration_number", "");
			String companyCategory = companyInfo.optString("company_category", "");
			String classOfCompany = companyInfo.optString("class_of_company", "");
			String companySubCategory = companyInfo.optString("company_sub_category", "");
			String authorizedCapital = companyInfo.optString("authorized_capital", "");
			String paidUpCapital = companyInfo.optString("paid_up_capital", "");
			String numberOfMembers = companyInfo.optString("number_of_members", "");
			String dateOfIncorporation = companyInfo.optString("date_of_incorporation", "");
			String registeredAddress = companyInfo.optString("registered_address", "");
			String addressOtherThanRo = companyInfo.optString("address_other_than_ro", "");
			String email = companyInfo.optString("email_id", "");
			String listedStatus = companyInfo.optString("listed_status", "");
			String activeCompilance = companyInfo.optString("active_compliance", "");
			String suspendedAtStockExchange = companyInfo.optString("suspended_at_stock_exchange", "");
			String lastAgmDate = companyInfo.optString("last_agm_date", "");
			String lastBsDate = companyInfo.optString("last_bs_date", "");
			String companyStatus = companyInfo.optString("company_status", "");
			String statusUnderCirp = companyInfo.optString("status_under_cirp", "");

			JSONArray directors = details.getJSONArray("directors");
			JSONArray charges = details.getJSONArray("charges");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setCompanyId(companyId);
			request.setCompanyType(companyType);
			request.setCompanyName(companyName);
			request.setEmail(email);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setCompanyId(companyId);
			response.setCompanyType(companyType);
			response.setBusinessName(companyName);
			response.setEmail(email);
			response.setStatus("success");
			response.setReferenceId(referenceId);

			temporary.setStatus("success");
			temporary.setReferenceId(referenceId);
			temporary.setCompanyId(companyId);
			temporary.setCompanyType(companyType);
			temporary.setCompanyName(companyName);

			temporary.setCin(cin);
			temporary.setRocCode(rocCode);
			temporary.setRegistrationNumber(registrationNumber);
			temporary.setCompanyCategory(companyCategory);
			temporary.setClassOfCompany(classOfCompany);
			temporary.setCompanySubCategory(companySubCategory);
			temporary.setAuthorizedCapital(authorizedCapital);
			temporary.setPaidUpCapital(paidUpCapital);
			temporary.setNumberOfMembers(numberOfMembers);
			temporary.setDateOfIncorporation(dateOfIncorporation);
			temporary.setRegisteredAddress(registeredAddress);
			temporary.setAddressOtherThanRo(addressOtherThanRo);
			temporary.setEmail(email);
			temporary.setListedStatus(listedStatus);
			temporary.setActiveCompilance(activeCompilance);
			temporary.setSuspendedAtStockExchange(suspendedAtStockExchange);
			temporary.setLastAgmDate(lastAgmDate);
			temporary.setLastBsDate(lastBsDate);
			temporary.setCompanyStatus(companyStatus);
			temporary.setStatusUnderCirp(statusUnderCirp);
			temporary.setDirectors(directors);
			temporary.setCharges(charges);

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

		JSONObject commonResponse = CommonResponseStructure.commonResponseCin(temporary);
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
