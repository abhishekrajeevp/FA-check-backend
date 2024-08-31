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
public class GstImageVerification {

	private final String VERIFICATION_TYPE = AppConstants.GST_IMAGE;
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

	public ResponseStructure gstImageVerification(RequestModel model, HttpServletRequest servletRequest) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			if (userModel != null && userModel.isAccountStatus()) {

				ENTITY=userModel;
				
				VendorVerificationModel vendorVerifyModel = vendorVerificationRepository
						.findByVerificationDocument(AppConstants.GST_IMAGE);

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

					String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
							userModel.getSecretKey());
					JSONObject userJson = new JSONObject(userDecryption);

					String referenceNumber = FileUtils.getRandomOTPnumber(10);
					userJson.put("reference_id", referenceNumber);
					userJson.put("source_type", "base64");

					return gstImageSmartRoute(userJson, model, userModel, vendorVerifyModel);

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

	private ResponseStructure gstImageSmartRoute(JSONObject userJson, RequestModel model, EntityModel userModel,
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

//		if (reqList.isEmpty() || lastRequest.getAttempt() == 0 || timeDifference > 60) // first Priority
//		{
		System.err.println("ATTEMPT 1");
		MerchantPriceModel merchantPriority = merchantPriceRepository
				.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 1);
		return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);

//		} else if (lastRequest.getAttempt() == 1 && timeDifference < 120) // Second Priority
//		{
//			System.err.println("ATTEMPT 2");
//			MerchantPriceModel merchantPriority = merchantPriceRepository
//					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 2);
//			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);
//
//		} else // Common Priority
//		{
//			System.err.println("ATTEMPT 3 COMMON");
//
//			VendorModel highSuccessVendor = smartRouteUtils.vendorSuccessRate(vendorVerifyModel);
//
//			MerchantPriceModel merchantPriority = merchantPriceRepository
//					.findByEntityModelAndVendorModelAndVendorVerificationModel(userModel, highSuccessVendor,
//							vendorVerifyModel);
//
//			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);
//		}
	}

	private ResponseStructure requestVendorRouting(JSONObject userJson, RequestModel model,
			MerchantPriceModel merchantPriceModel, EntityModel userModel, VendorVerificationModel vendorVerifyModel)
			throws Exception {
		ResponseStructure balanceCheck = smartRouteUtils.balanceCheckForOcr(userModel, merchantPriceModel,vendorVerifyModel);

		VendorModel vendorModel = merchantPriceModel.getVendorModel();
		VendorPriceModel vendorPrice = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(vendorModel,
				vendorVerifyModel);
		if (balanceCheck.getFlag() == 1) {

			if (merchantPriceModel.getVendorModel().getVendorId() == 1) {

				System.err.println("SIGN DESK");
				return signDeskGstImageVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);

			} // No Other Vendor available for PAN IMAGE
		}
		return balanceCheck;
	}

	private ResponseStructure signDeskGstImageVerification(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel,
			MerchantPriceModel merchantPriceModel, VendorPriceModel vendorPrice) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String referenceId = userJson.getString("reference_id");
		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");
		boolean filingStatus = userJson.getBoolean("filing_status_get");

		Date requesTime = new Date();

		JSONObject inputParams = new JSONObject();
		JSONObject encryptDatas = new JSONObject();

		inputParams.put("reference_id", referenceId);
		inputParams.put("source_type", sourceType);
		inputParams.put("source", source);
		inputParams.put("filing_status_get", filingStatus);

		String key = AppConstants.ENCRYPTION_KEY;
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

		// Extract specific data
		String encryptedResponse = jsonObject.getString("encrypted_response");

		// // Print the extracted data
		System.out.println("encrypted_response: " + encryptedResponse);
		// System.out.println("status: " + status);
		//
		String decryptData = PasswordUtils.decryptString(encryptedResponse, key);
		// String decryptData=PasswordUtils.jsonDecryption(encryptedResponse);
		JSONObject decryptJson = new JSONObject(decryptData);

		System.err.println("Decrypt : " + decryptJson);

		String responseDateTime = decryptJson.getString("response_time_stamp");
		String status = decryptJson.getString("status");

		Request request = new Request();
		// Request Count
		int count = userModel.getRequestCount() + 1;
		userModel.setRequestCount(count);

		request.setStatus(status);
		request.setReferenceId(referenceId);
		// request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestDateAndTime(requesTime);
		request.setResponseDateAndTime(responseDateTime);
		request.setRequestBy(userModel.getName());
		request.setUser(userModel);
		request.setVerificationModel(vendorVerifyModel);
		request.setPrice(merchantPriceModel.getImagePrice());
		request.setFreeHit(model.isFreeHit());
		request.setConsider(true);

		Response responseData = new Response();
		responseData.setReferenceId(referenceId);
		// responseData.setSource();
		responseData.setSourceType(sourceType);
		responseData.setRequestDateAndTime(requesTime);
		responseData.setResponseDateAndTime(responseDateTime);
		responseData.setRequestBy(userModel.getName());
		responseData.setStatus(status);
		// responseData.setEncryptedJson(encryptedJson);
		responseData.setUser(userModel);
		responseData.setVendorModel(vendorModel);
		responseData.setVerificationModel(vendorVerifyModel);

		RequestModel temporary = new RequestModel();

		temporary.setStatus(status);
		temporary.setReferenceId(referenceId);
		temporary.setResponseDateAndTime(responseDateTime);
		temporary.setVendorModel(vendorModel);

		String errorCode = "";
		
		if (status.equals("success")) {

			JSONObject resultJson = decryptJson.getJSONObject("result");

			JSONObject extractedData = resultJson.getJSONObject("extracted_data");
			JSONObject validatedData = resultJson.getJSONObject("validated_data");
			JSONObject dataMatch = resultJson.getJSONObject("data_match");
			boolean validGst = resultJson.getBoolean("valid_gstn");
			int dataMatchAggregate = resultJson.getInt("data_match_aggregate");

			String transactionId = decryptJson.getString("transaction_id");
			String message = decryptJson.getString("message");

			if (validGst) {

				// ED
				String tradeName = extractedData.getString("trade_name");
				String doi = extractedData.getString("doi");

				// VD
				String gstIn = validatedData.getString("gstin");
				String businessName = validatedData.getString("business_name");
				String clientId = validatedData.getString("client_id");
				String dateOfRegistration = validatedData.getString("date_of_registration");
				String dateOfCancellation = validatedData.getString("date_of_cancellation");
				String taxpayerType = validatedData.getString("taxpayer_type");
				String address = validatedData.getString("address");
				String coreBusinessActivityCode = validatedData.getString("nature_of_core_business_activity_code");
				String coreBusinessActivityDescription = validatedData
						.getString("nature_of_core_business_activity_description");
				String centerJurisdiction = validatedData.getString("center_jurisdiction");
				String constitutionOfBusiness = validatedData.getString("constitution_of_business");
				String gstInStatus = validatedData.getString("gstin_status");
				String panNumber = validatedData.getString("pan_number");
				String stateJurisdiction = validatedData.getString("state_jurisdiction");
				String aadhaarValidationDate = validatedData.getString("aadhaar_validation_date");
				String fieldVisitConducted = validatedData.getString("field_visit_conducted");
				String legalName = validatedData.getString("legal_name");
				String aadharValidation = validatedData.getString("aadhaar_validation");
				JSONObject hsnInfo = validatedData.getJSONObject("hsn_info");
				JSONArray filingStatusList = validatedData.getJSONArray("filing_status");
				JSONArray natureBusActivity = validatedData.getJSONArray("nature_bus_activities");

				int dataMatchTaxpayerType = dataMatch.getInt("taxpayer_type");
				int dataMatchBusinessNamer = dataMatch.getInt("business_name");
				int dataMatchAddress = dataMatch.getInt("address");
				int dataMatchCostitutionOfBusiness = dataMatch.getInt("constitution_of_business");
				int dataMatchDateOfRegistration = dataMatch.getInt("date_of_registration");
				int dataMatchGstIn = dataMatch.getInt("gstin");
				int dataMatchTradeName = dataMatch.getInt("trade_name");

				request.setClientId(clientId);
				request.setExtractedData(gstIn);
				responseData.setSource(gstIn);

				responseData.setGstIn(gstIn);
				responseData.setClientId(clientId);
				responseData.setExtractedData(gstIn);
				responseData.setBusinessName(businessName);
				responseData.setDateOfRegistration(dateOfRegistration);
				responseData.setDateOfCancellation(dateOfCancellation);
				responseData.setSource(gstIn);

				temporary.setGstIn(gstIn);
				temporary.setBusinessName(businessName);
				temporary.setClientId(clientId);
				temporary.setDateOfRegistration(dateOfRegistration);
				temporary.setDateOfCancellation(dateOfCancellation);
				temporary.setTaxpayerType(taxpayerType);
				temporary.setAddress(address);
				temporary.setCoreBusinessActivityCode(coreBusinessActivityCode);
				temporary.setCoreBusinessActivityDescription(coreBusinessActivityDescription);
				temporary.setCenterJurisdiction(centerJurisdiction);
				temporary.setConstitutionOfBusiness(constitutionOfBusiness);
				temporary.setGstInStatus(gstInStatus);
				temporary.setPanNumber(panNumber);
				temporary.setStateJurisdiction(stateJurisdiction);
				temporary.setAadharValidatedDate(aadhaarValidationDate);
				temporary.setFieldVisitConducted(fieldVisitConducted);
				temporary.setLegalName(legalName);
				temporary.setAadharValidation(aadharValidation);
				temporary.setDoi(doi);
				temporary.setTradeName(tradeName);
				temporary.setHsnInfo(hsnInfo.toString());
				temporary.setFilingStatusList(filingStatusList.toString());
				temporary.setNatureBusActivity(natureBusActivity.toString());
				temporary.setDataMatchTaxpayerType(dataMatchTaxpayerType);
				temporary.setDataMatchBusinessNamer(dataMatchBusinessNamer);
				temporary.setDataMatchAddress(dataMatchAddress);
				temporary.setDataMatchCostitutionOfBusiness(dataMatchCostitutionOfBusiness);
				temporary.setDataMatchDateOfRegistration(dataMatchDateOfRegistration);
				temporary.setDataMatchGstIn(dataMatchGstIn);
				temporary.setDataMatchTradeName(dataMatchTradeName);
				temporary.setValidGst(validGst);
				temporary.setDataMatchAggregate(dataMatchAggregate);

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

		JSONObject commonResponse = CommonResponseStructure.commonResponseGstImage(temporary);
		responseData.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(responseData);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !model.isFreeHit()  && smartRouteUtils.signDeskError(errorCode)) {

			smartRouteUtils.deductAmountForOcr(userModel,merchantPriceModel);
			
		}else if(!model.isFreeHit() && smartRouteUtils.signDeskError(errorCode)) {
			
			smartRouteUtils.postpaidConsumedAmountForOcr(userModel, merchantPriceModel);
			
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

		structure.setData(mapNew);
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setMessage(AppConstants.SUCCESS);

		userRepository.save(userModel);

		return structure;
	}

}
