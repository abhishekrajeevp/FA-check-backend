package com.bp.middleware.requestandresponse;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.payment.PaymentMethod;
import com.bp.middleware.payment.PaymentRepository;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.smartrouteverification.CommonResponseStructure;
import com.bp.middleware.smartrouteverification.SmartRouteUtils;
import com.bp.middleware.surepass.SurepassCommons;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.CommonRequestDto;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.GetPublicIpAndLocation;
import com.bp.middleware.util.PasswordUtils;
import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class VerificationServiceImpl implements VerificationService {

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
	private VendorVerificationRepository verificationRepository;
	@Autowired
	private VendorPriceRepository vendorPriceRepository;
	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private PostpaidRepository postpaidRepository;
	@Autowired
	private SurepassCommons surepassCommons;
	@Autowired
	private SmartRouteUtils smartRouteUtils;
	@Autowired
	private CommonResponseStructure CommonResponseStructure;
	@Autowired
	private RequestResponseReplicaRepository replicaRepository;
	@Autowired
	FileUtils fu;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;

	private Response sourceCheck(List<Response> sourceList) {
		Response model = new Response();
		for (Response response : sourceList) {
			if (response.getStatus().equals("success")) {
				model = response;
			}
		}
		return model;
	}

	public ResponseStructure setRequest(Response model, RequestModel dto, MerchantPriceModel merchantPriceModel,
			EntityModel userModel, VendorVerificationModel serviceModel) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		Request request = new Request();

		Date date = new Date();
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String stringDate = simple.format(date);

		request.setReferenceId(model.getReferenceId());
		request.setMessage(model.getMessage());
		request.setTransactionId(model.getTransactionId());
		request.setSource(model.getSource());
		request.setSourceType(model.getSourceType());
		request.setRequestBy(dto.getRequestBy());
		request.setRequestDateAndTime(new Date());
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setResponseDateAndTime(stringDate);
		request.setStatus(model.getStatus());
		request.setUser(userModel);
		request.setVerificationModel(serviceModel);

		request.setFilingStatus(model.isFilingStatus());
		request.setCompanyName(model.getBusinessName());
		request.setCompanyId(model.getCompanyId());
		request.setCompanyType(model.getCompanyType());
		request.setEmail(model.getEmail());
		request.setFullName(model.getFullName());
		request.setDob(model.getDob());
		request.setState(model.getState());

		reqRepository.save(request);

		JSONObject object = new JSONObject();
		object.put("status", model.getStatus());
		object.put("encrypted_response", model.getResponse());

		if (serviceModel.getVendorVerificationId() == 6) {
			object.put("request_id", request.getRequestId());
			object.put("response_id", model.getResponseId());
		}

		String returnResponse = PasswordUtils.demoEncryption(object, userModel.getSecretKey());
		System.out.println("RETURN RESPONSE : " + returnResponse);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2) {
			double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
			double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

			userModel.setRemainingAmount(remainingAmount);
			userModel.setConsumedAmount(consumedAmount);
			userModel.setPaymentStatus("No Dues");
		}

		// Response Count
		int responseCount = userModel.getResponseCount();
		userModel.setResponseCount(responseCount + 1);
		userRepository.save(userModel);

		System.err.println("Source PRESENT");

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", returnResponse);

		structure.setData(mapNew);
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setMessage(AppConstants.SUCCESS);

		return structure;
	}

	@Override
	public ResponseStructure verification(RequestModel model, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			String userIdString = servletRequest.getHeader("userId");
			int userId = Integer.parseInt(userIdString);

			EntityModel entityModel = userRepository.findByUserId(userId);
			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			System.out.println("USER : " + userModel.getUserId());

			if (userModel.getUserId() > 0) {

				VendorVerificationModel verificationModel = verificationRepository
						.findByVerificationDocument(AppConstants.PAN_VERIFY);
				VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SIGN_DESK_VENDOR);
				MerchantPriceModel merchantPriceModel = merchantPriceRepository
						.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, verificationModel,
								userModel);
				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

				if (merchantPriceModel != null) {

					ResponseStructure balance = smartRouteUtils.balanceCheck(userModel, merchantPriceModel,verificationModel);

					if (balance.getFlag() == 1) {
						String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
								userModel.getSecretKey());
						JSONObject userJson = new JSONObject(userDecryption);

						userJson.put("source_type", "id");

						String source = userJson.getString("source");

						System.err.println("SOURCE : " + source);

						Response sourceResponse = surepassCommons.sourceCheck(source, userModel, merchantPriceModel);
						System.err.println("Source Response : " + sourceResponse);

						if (sourceResponse.getResponseId() > 0) {
							System.err.println("SOURCE PRESENT");

							return surepassCommons.setRequest(sourceResponse, model, merchantPriceModel, userModel,
									verificationModel);

						} else {

							return signDeskPanVerification(userJson, model, userModel, verificationModel, vendorModel,
									merchantPriceModel, vendorPriceModel);
						}
					} else {
						return balance;
					}
				} else {

					return smartRouteUtils.noAccessForThisVerification(entityModel, verificationModel,model);
				}

			} else {
				return smartRouteUtils.commonErrorResponse();
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	public ResponseStructure signDeskPanVerification(JSONObject userJson, RequestModel model, EntityModel userModel,
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

		inputParams.put("reference_id", userReferenceId);
		inputParams.put("source_type", sourceType);
		inputParams.put("source", source);

		// String key = AppConstants.ENCRYPTION_KEY;
		// String jsonString = inputParams.toString();
		String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);
		// System.out.println("Encryption Json =" + encryptedJson);

		encryptDatas.put("api_data", encryptedJson);
		encryptDatas.put("enc_mode", "symmetric");
		encryptDatas.put("is_encrypted", true);

		// create an instance of RestTemplate
		RestTemplate restTemplate = new RestTemplate();

		// VendorReq
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);

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

		System.err.println("DATA = " + data);
		System.err.println("MERCH = " + merchantPriceModel.getMerchantPriceId());

		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		// Convert the string to a JSONObject
		JSONObject jsonObject = new JSONObject(data);

		// Extract specific data
		String encryptedResponse = jsonObject.getString("encrypted_response");
		String status = jsonObject.getString("status");

		// // Print the extracted data
		System.out.println("encrypted_response: " + encryptedResponse);
		System.out.println("status: " + status);

		String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

		Response response = new Response();

		JSONObject decryptJson = new JSONObject(decryptData);
		String signDeskStatus = decryptJson.getString("status");
		String responseTimeStamp = decryptJson.getString("response_time_stamp");
		String referenceId = decryptJson.getString("reference_id");

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setReferenceId(referenceId);
		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(vendorVerifyModel);
		request.setResponseDateAndTime(responseTimeStamp);
		request.setStatus(signDeskStatus);

		response.setReferenceId(referenceId);
		response.setStatus(signDeskStatus);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(responseTimeStamp);
		response.setResponse(decryptData);
		response.setVerificationModel(vendorVerifyModel);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2) {
			double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
			double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

			userModel.setRemainingAmount(remainingAmount);
			userModel.setConsumedAmount(consumedAmount);
			userModel.setPaymentStatus("No Dues");
		}

		RequestModel temporary = new RequestModel();

		temporary.setStatus(signDeskStatus);
		temporary.setReferenceId(referenceId);
		temporary.setResponseDateAndTime(responseTimeStamp);

		if (signDeskStatus.equals("success")) {
			JSONObject resultJson = decryptJson.getJSONObject("result");

			boolean validPan = resultJson.getBoolean("valid_pan");

			if (validPan) {

				JSONObject validJson = resultJson.getJSONObject("validated_data");
				String name = validJson.getString("full_name");
				String panNumber = validJson.getString("pan_number");
				String category = validJson.getString("category");

				response.setFullName(name);
				response.setPanNumber(panNumber);

				temporary.setFullName(name);
				temporary.setSource(panNumber);
				temporary.setCategory(category);
			}

			String transactionId = decryptJson.getString("transaction_id");
			String message = decryptJson.getString("message");

			request.setMessage(message);
			request.setTransactionId(transactionId);
			request.setAttempt(0);

			response.setTransactionId(transactionId);
			response.setMessage(message);
			response.setEncryptedJson(encryptedJson);

			temporary.setMessage(message);

		} else {

			String error = decryptJson.getString("error");
			String errorCode = decryptJson.getString("error_code");

			request.setError(error);
			request.setErrorCode(errorCode);
			request.setAttempt(model.getAttempt() + 1);

			response.setError(error);
			response.setErrorCode(errorCode);

			temporary.setError(error);
		}

		JSONObject commonResponse = CommonResponseStructure.commonResponsePan(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

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

	@Override
	public ResponseStructure gstVerification(RequestModel dto, HttpServletRequest servletRequest) {
		ResponseStructure structure = new ResponseStructure();
		try {

			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);
			System.out.println("UserDecryption  : " + userDecryption);

			JSONObject userJosn = new JSONObject(userDecryption);
			boolean filingStatus = userJosn.getBoolean("filing_status_get");
			String reference_id = userJosn.getString("reference_id");
			System.out.println("Reference Id : " + reference_id);
			String source = userJosn.getString("source");
			System.out.println("Source : " + source);
			String source_type = userJosn.getString("source_type");

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 2) {
						return gstNumberCheck(source, userModel, merchantPriceModel, dto, reference_id, serviceModel,
								source_type, userSecretKey, vendorModel, filingStatus);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}
				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 2) {
						return gstNumberCheck(source, userModel, merchantPriceModel, dto, reference_id, serviceModel,
								source_type, userSecretKey, vendorModel, filingStatus);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}
				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure gstNumberCheck(String source, EntityModel userModel,
			MerchantPriceModel merchantPriceModel, RequestModel dto, String reference_id,
			VendorVerificationModel serviceModel, String source_type, String userSecretKey, VendorModel vendorModel,
			boolean filingStatus) {
		ResponseStructure structure = new ResponseStructure();
		try {

			// Request request = new Request();
			// Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);

			List<Response> sourceList = respRepository.findBySource(source);
			// Response model = new Response();
			if (!sourceList.isEmpty()) {

				Response model = sourceCheck(sourceList);
				boolean isMatch = false;
				if (model != null) {
					isMatch = true;
				}

//				boolean isMatch = false;
//				for (Response response : sourceList) {
//					if (response.getStatus().equals("success")) {
//						model = response;
//						isMatch = true;
//						break;
//					}

				if (isMatch) {

					return setRequest(model, dto, merchantPriceModel, userModel, serviceModel);

//					request.setReferenceId(model.getReferenceId());
//					request.setMessage(model.getMessage());
//					request.setTransactionId(model.getTransactionId());
//					request.setSource(model.getSource());
//					request.setSourceType(model.getSourceType());
//					request.setRequestBy(dto.getRequestBy());
//					request.setFilingStatus(filingStatus);
//					request.setRequestDateAndTime(new Date());
//					request.setPrice(merchantPriceModel.getIdPrice());
//
//					Date date = new Date();
//					SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					String stringDate = simple.format(date);
//					request.setResponseDateAndTime(stringDate);
//					request.setStatus(model.getStatus());
//					request.setUser(userModel);
//					request.setVerificationModel(serviceModel);
//
//					reqRepository.save(request);

//					JSONObject object = new JSONObject();
//					object.put("status", model.getStatus());
//					object.put("encrypted_response", model.getResponse());
//
//					String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
//					System.out.println("RETURN RESPONSE : " + returnResponse);

//					// Prepaid Amount Reduction
//					if (userModel.getPaymentMethod().getPaymentId() == 2) {
//						double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
//						double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();
//
//						userModel.setRemainingAmount(remainingAmount);
//						userModel.setConsumedAmount(consumedAmount);
//						userModel.setPaymentStatus("No Dues");
//					}

//					// Response Count
//					int responseCount = userModel.getResponseCount();
//					userModel.setResponseCount(++responseCount);
//					userRepository.save(userModel);

//					Map<String, Object> mapNew = new HashMap<>();
//					mapNew.put("Return Response", returnResponse);
//					mapNew.put("Response", model);
//					mapNew.put("Request", request);

//					structure.setData(returnResponse);
//					structure.setStatusCode(HttpStatus.OK.value());
//					structure.setFlag(1);
//					structure.setMessage(AppConstants.SUCCESS);

				} else {
					return gstNumberVerification(dto, userModel, serviceModel, reference_id, source, source_type,
							filingStatus, userSecretKey, vendorModel, merchantPriceModel, serviceModel);
				}

			} else {
				return gstNumberVerification(dto, userModel, serviceModel, reference_id, source, source_type,
						filingStatus, userSecretKey, vendorModel, merchantPriceModel, serviceModel);
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure gstNumberVerification(RequestModel dto, EntityModel user, VendorVerificationModel service,
			String referenceNumber, String source, String source_type, boolean filingStatus, String userSecretKey,
			VendorModel vendorModel, MerchantPriceModel merchantPriceModel, VendorVerificationModel serviceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {

			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			// TAKE VENDOR & VENDORVERIFICATION FROM MERCHANT PRICE MODEL
			Request request = new Request();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			request.setReferenceId(referenceNumber);
			request.setSource(source);
			request.setSourceType(source_type);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(new Date());
			request.setPrice(merchantPriceModel.getIdPrice());

			inputParams.put("reference_id", referenceNumber);
			inputParams.put("source_type", "id");
			inputParams.put("source", source);
			inputParams.put("filing_status_get", true);

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

			// VendorReq
			int reqCount = vendorModel.getVendorRequest() + 1;
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			// VendorResponse
			int respCount = vendorModel.getVendorResponse() + 1;
			vendorModel.setVendorResponse(respCount);

			// Convert the string to a JSONObject
			JSONObject jsonObject = new JSONObject(data);

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			String status = jsonObject.getString("status");

			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			System.out.println("status: " + status);
			//
			String decryptData = PasswordUtils.decryptString(encryptedResponse, key);
			// String decryptData=PasswordUtils.jsonDecryption(encryptedResponse);

			Response response = new Response();

			JSONObject decryptJson = new JSONObject(decryptData);

			String statuss = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			System.out.println("Decrypted Response = " + decryptData);

			response.setReferenceId(referenceId);
			response.setStatus(statuss);
			response.setSourceType(source_type);
			response.setSource(source);
			response.setRequestDateAndTime(new Date());
			response.setRequestBy(dto.getRequestBy());
			response.setUser(user);
			response.setVendorModel(vendorModel);
			response.setRequest(request);

			JSONObject object = new JSONObject();
			object.put("status", statuss);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
			System.out.println("RETURN RESPONSE : " + returnResponse);

			// Prepaid Amount Reduction
			if (user.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = user.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = user.getConsumedAmount() + merchantPriceModel.getIdPrice();

				user.setRemainingAmount(remainingAmount);
				user.setConsumedAmount(consumedAmount);
				user.setPaymentStatus("No Dues");
			}

			if (statuss.equals("success")) {

				String message = decryptJson.getString("message");
				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");
				String gstIn = validatedJson.getString("gstin");
				String businessName = validatedJson.getString("business_name");
				String transactionId = decryptJson.getString("transaction_id");
				String dateOfReg = validatedJson.getString("date_of_registration");
				String dateOfCancel = validatedJson.getString("date_of_cancellation");
				String panNumber = validatedJson.getString("pan_number");
				String address = validatedJson.getString("address");

				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setUser(user);
				request.setResponseDateAndTime(responseTimeStamp);
				request.setFilingStatus(filingStatus);
				request.setVerificationModel(service);
				reqRepository.save(request);

				response.setAddress(address);
				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setResponseDateAndTime(responseTimeStamp);
				response.setGstIn(gstIn);
				response.setPanNumber(panNumber);
				response.setBusinessName(businessName);
				response.setFilingStatus(filingStatus);
				response.setDateOfRegistration(dateOfReg);
				response.setDateOfCancellation(dateOfCancel);
				response.setRequest(request);

				respRepository.save(response);
			} else {
				String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(statuss);
				request.setResponseDateAndTime(responseTimeStamp);
				request.setUser(user);
				request.setError(error);
				request.setErrorCode(errorCode);
				request.setVerificationModel(service);
				reqRepository.save(request);

				response.setResponse(decryptData);
				response.setError(error);
				response.setErrorCode(errorCode);
				response.setResponseDateAndTime(responseTimeStamp);
				response.setRequest(request);

				respRepository.save(response);
			}

			// Response Count
			int responseCount = user.getResponseCount();
			user.setResponseCount(++responseCount);
			userRepository.save(user);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", verifyRequest);
//			mapNew.put("Request", request);

			structure.setData(mapNew);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure panImageVerification(RequestModel dto, HttpServletRequest servletRequest) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String UserDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);
			JSONObject userJosn = new JSONObject(UserDecryption);

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getImagePrice()) {

					if (dto.getVendorVerificationId() == 1) {
						return panImageVerificationProcess(userModel, merchantPriceModel, serviceModel, userJosn,
								userSecretKey, vendorModel);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}
				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("PLEASE RECHARGE THE AMOUNT");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 1) {
						return panImageVerificationProcess(userModel, merchantPriceModel, serviceModel, userJosn,
								userSecretKey, vendorModel);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {

					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure panImageVerificationProcess(EntityModel userModel, MerchantPriceModel merchantPriceModel,
			VendorVerificationModel serviceModel, JSONObject userJosn, String userSecretKey, VendorModel vendorModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");

			Date requesTime = new Date();

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

			// VendorReq
			int reqCount = vendorModel.getVendorRequest() + 1;
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			// VendorResponse
			int respCount = vendorModel.getVendorResponse() + 1;
			vendorModel.setVendorResponse(respCount);

			// Convert the string to a JSONObject
			JSONObject jsonObject = new JSONObject(data);

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			// String status = jsonObject.getString("status");
			//
			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			//
			String decryptData = PasswordUtils.decryptString(encryptedResponse, key);
			// String decryptData=PasswordUtils.jsonDecryption(encryptedResponse);
			JSONObject decryptJson = new JSONObject(decryptData);

			String response = decryptJson.getString("response_time_stamp");

			String status = decryptJson.getString("status");

			Request request = new Request();
			// Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);

			request.setStatus(status);
			request.setReferenceId(reference_id);
			// request.setSource(source);
			request.setSourceType(source_type);
			request.setRequestDateAndTime(requesTime);
			request.setRequestBy(userModel.getName());
			request.setUser(userModel);
			request.setVerificationModel(serviceModel);
			request.setPrice(merchantPriceModel.getImagePrice());

			Response responseData = new Response();
			responseData.setReferenceId(reference_id);
			// responseData.setSource(source);
			responseData.setSourceType(source_type);
			responseData.setRequestDateAndTime(requesTime);
			responseData.setRequestBy(userModel.getName());
			responseData.setStatus(status);
			// responseData.setEncryptedJson(encryptedJson);
			responseData.setUser(userModel);
			responseData.setVendorModel(vendorModel);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
			System.out.println("RETURN RESPONSE : " + returnResponse);

			// Prepaid Amount Reduction
			double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getImagePrice();
			double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getImagePrice();

			userModel.setRemainingAmount(remainingAmount);
			userModel.setConsumedAmount(consumedAmount);
			userModel.setPaymentStatus("No Dues");

			if (status.equals("success")) {
				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject extractedData = resultJson.getJSONObject("extracted_data");
				JSONObject validatedData = resultJson.getJSONObject("validated_data");
				String extractedPan = extractedData.getString("pan_number");
				String client_id = validatedData.getString("client_id");

				String transactionId = decryptJson.getString("transaction_id");
				String message = decryptJson.getString("message");

				request.setTransactionId(transactionId);
				request.setMessage(message);
				request.setResponseDateAndTime(response);
				request.setClientId(client_id);
				request.setExtractedData(extractedPan);
				request.setSource(extractedPan);
				reqRepository.save(request);

				responseData.setTransactionId(transactionId);
				responseData.setMessage(message);
				responseData.setResponseDateAndTime(response);
				responseData.setClientId(client_id);
				responseData.setExtractedData(extractedPan);
				responseData.setResponse(decryptData);
				responseData.setRequest(request);
				responseData.setSource(extractedPan);
				respRepository.save(responseData);
			} else {
				String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setResponseDateAndTime(response);
				request.setError(error);
				request.setErrorCode(errorCode);
				reqRepository.save(request);

				responseData.setResponse(decryptData);
				responseData.setError(error);
				responseData.setErrorCode(errorCode);
				responseData.setRequest(request);
				responseData.setResponseDateAndTime(response);

				respRepository.save(responseData);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);
//
			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", responseData);
//			mapNew.put("Request", request);

			structure.setData(mapNew);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setFlag(1);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
		}
		return structure;
	}

	@Override
	public ResponseStructure gstImageVerification(RequestModel dto, HttpServletRequest servletRequest) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String UserDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);
			JSONObject userJosn = new JSONObject(UserDecryption);

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getImagePrice()) {

					if (dto.getVendorVerificationId() == 2) {
						return gstImageVerificationProcess(userModel, serviceModel, vendorModel, merchantPriceModel,
								userSecretKey, userJosn);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}
				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setMessage("PLEASE RECHARGE");
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 2) {
						return gstImageVerificationProcess(userModel, serviceModel, vendorModel, merchantPriceModel,
								userSecretKey, userJosn);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}
				} else {

					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure gstImageVerificationProcess(EntityModel userModel, VendorVerificationModel serviceModel,
			VendorModel vendorModel, MerchantPriceModel merchantPriceModel, String userSecretKey, JSONObject userJosn) {
		ResponseStructure structure = new ResponseStructure();
		try {

			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");
			boolean filingStatus = userJosn.getBoolean("filing_status_get");

			Date requesTime = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// String referenceNumber=FileUtils.getRandomOTPnumber(10);

			// String encryptImage=PasswordUtils.demoImageEncryption(image);

			inputParams.put("reference_id", reference_id);
			inputParams.put("source_type", source_type);
			inputParams.put("source", source);
			inputParams.put("filing_status_get", filingStatus);

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

			// VendorReq
			int reqCount = vendorModel.getVendorRequest() + 1;
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			// VendorResponse
			int respCount = vendorModel.getVendorResponse() + 1;
			vendorModel.setVendorResponse(respCount);

			// Convert the string to a JSONObject
			JSONObject jsonObject = new JSONObject(data);

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			// String status = jsonObject.getString("status");

			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			// System.out.println("status: " + status);
			//
			String decryptData = PasswordUtils.decryptString(encryptedResponse, key);
			// String decryptData=PasswordUtils.jsonDecryption(encryptedResponse);
			JSONObject decryptJson = new JSONObject(decryptData);
			//

			String response = decryptJson.getString("response_time_stamp");

			String status = decryptJson.getString("status");

			Request request = new Request();
			// Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);

			request.setStatus(status);
			request.setReferenceId(reference_id);
			// request.setSource(source);
			request.setSourceType(source_type);
			request.setRequestDateAndTime(requesTime);
			request.setRequestBy(userModel.getName());
			request.setUser(userModel);
			request.setVerificationModel(serviceModel);
			request.setPrice(merchantPriceModel.getImagePrice());

			Response responseData = new Response();
			responseData.setReferenceId(reference_id);
			// responseData.setSource();
			responseData.setSourceType(source_type);
			responseData.setRequestDateAndTime(requesTime);
			responseData.setRequestBy(userModel.getName());
			responseData.setStatus(status);
			// responseData.setEncryptedJson(encryptedJson);
			responseData.setUser(userModel);
			responseData.setVendorModel(vendorModel);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
			System.out.println("RETURN RESPONSE : " + returnResponse);

			// Prepaid Amount Reduction
			double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getImagePrice();
			double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getImagePrice();

			userModel.setRemainingAmount(remainingAmount);
			userModel.setConsumedAmount(consumedAmount);
			userModel.setPaymentStatus("No Dues");

			if (status.equals("success")) {
				JSONObject resultJson = decryptJson.getJSONObject("result");
				// JSONObject extractedData = resultJson.getJSONObject("extracted_data");
				JSONObject validatedData = resultJson.getJSONObject("validated_data");

				String gstIn = validatedData.getString("gstin");
				String businessName = validatedData.getString("business_name");
				String client_id = validatedData.getString("client_id");
				String dateOfRegistration = validatedData.getString("date_of_registration");
				String dateOfCancellation = validatedData.getString("date_of_cancellation");

				String transactionId = decryptJson.getString("transaction_id");
				String message = decryptJson.getString("message");

				request.setTransactionId(transactionId);
				request.setMessage(message);
				request.setResponseDateAndTime(response);
				request.setClientId(client_id);
				request.setExtractedData(gstIn);
				responseData.setSource(gstIn);

				reqRepository.save(request);

				responseData.setGstIn(gstIn);
				responseData.setTransactionId(transactionId);
				responseData.setMessage(message);
				responseData.setResponseDateAndTime(response);
				responseData.setClientId(client_id);
				responseData.setExtractedData(gstIn);
				responseData.setResponse(decryptData);
				responseData.setBusinessName(businessName);
				responseData.setRequest(request);
				responseData.setDateOfRegistration(dateOfRegistration);
				responseData.setDateOfCancellation(dateOfCancellation);
				responseData.setSource(gstIn);
				respRepository.save(responseData);
			} else {
				String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setResponseDateAndTime(response);
				request.setError(error);
				request.setErrorCode(errorCode);
				reqRepository.save(request);

				responseData.setResponse(decryptData);
				responseData.setError(error);
				responseData.setErrorCode(errorCode);
				responseData.setResponseDateAndTime(response);
				responseData.setRequest(request);

				respRepository.save(responseData);

			}
			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", responseData);
//			mapNew.put("Request", request);

			structure.setData(mapNew);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setFlag(1);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
		}
		return structure;
	}

	@Override
	public ResponseStructure aadharXmlVerification(RequestModel dto, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {

			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String UserDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);
			System.out.println("UserDecryption  : " + UserDecryption);
			JSONObject userJosn = new JSONObject(UserDecryption);
			String reference_id = userJosn.getString("reference_id");
			System.out.println("Reference Id : " + reference_id);
			String source = userJosn.getString("source");
			System.out.println("Source : " + source);
			String source_type = userJosn.getString("source_type");

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 6) {
						return aadharNumberCheck(source, userModel, merchantPriceModel, dto, reference_id, serviceModel,
								source_type, userSecretKey, vendorModel);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}
				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 6) {
						return aadharNumberCheck(source, userModel, merchantPriceModel, dto, reference_id, serviceModel,
								source_type, userSecretKey, vendorModel);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}
				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure aadharNumberCheck(String source, EntityModel userModel,
			MerchantPriceModel merchantPriceModel, RequestModel dto, String reference_id,
			VendorVerificationModel serviceModel, String source_type, String userSecretKey, VendorModel vendorModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Request request = new Request();
			// Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);

			List<Response> sourceList = respRepository.findBySource(source);
			// Response model = new Response();
			if (!sourceList.isEmpty()) {

				Response model = sourceCheck(sourceList);
				boolean isMatch = false;
				if (model != null) {
					isMatch = true;
				}

//				boolean isMatch = false;
//				for (Response response : sourceList) {
//					if (response.getStatus().equals("success")) {
//						model = response;
//						isMatch = true;
//						break;
//					}
//				}
				if (isMatch) {

					// return setRequest(model, dto, merchantPriceModel, userModel, serviceModel);

//					request.setReferenceId(model.getReferenceId());
//					request.setMessage(model.getMessage());
//					request.setTransactionId(model.getTransactionId());
//					request.setSource(model.getSource());
//					request.setSourceType(model.getSourceType());
//					request.setRequestBy(dto.getRequestBy());
//					request.setRequestDateAndTime(new Date());
//					request.setPrice(merchantPriceModel.getIdPrice());
//
//					Date date = new Date();
//					SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					String stringDate = simple.format(date);
//					request.setResponseDateAndTime(stringDate);
//					request.setStatus(model.getStatus());
//					request.setUser(userModel);
//					request.setVerificationModel(serviceModel);
//					reqRepository.save(request);

//					JSONObject object = new JSONObject();
//					object.put("status", model.getStatus());
//					object.put("encrypted_response", model.getResponse());
//					object.put("request_id", request.getRequestId());
//					object.put("response_id", model.getResponseId());
//
//					String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
//					System.out.println("RETURN RESPONSE : " + returnResponse);
//
//					// Prepaid Amount Reduction
//					if (userModel.getPaymentMethod().getPaymentId() == 2) {
//						double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
//						double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();
//
//						userModel.setRemainingAmount(remainingAmount);
//						userModel.setConsumedAmount(consumedAmount);
//						userModel.setPaymentStatus("No Dues");
//					}
//
//					// Response Count
//					int responseCount = userModel.getResponseCount();
//					userModel.setResponseCount(++responseCount);
//					userRepository.save(userModel);
//
////					Map<String, Object> mapNew = new HashMap<>();
////					mapNew.put("Return Response", returnResponse);
////					mapNew.put("Response", model);
////					mapNew.put("Request", request);
//
//					structure.setData(returnResponse);
//					structure.setStatusCode(HttpStatus.OK.value());
//					structure.setFlag(1);
//					structure.setMessage(AppConstants.SUCCESS);
				} else {
					return aadharVerification(dto, userModel, serviceModel, reference_id, source, source_type,
							userSecretKey);
				}

			} else {
				return aadharVerification(dto, userModel, serviceModel, reference_id, source, source_type,
						userSecretKey);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setFlag(3);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
		}
		return structure;
	}

	private ResponseStructure aadharVerification(RequestModel dto, EntityModel userModel,
			VendorVerificationModel serviceModel, String reference_id, String source, String source_type,
			String userSecretKey) {
		ResponseStructure structure = new ResponseStructure();
		try {

			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);
			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// ? String referenceNumber=FileUtils.getRandomOTPnumber(10);

			inputParams.put("reference_id", reference_id);
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

			// VendorReq
			int reqCount = vendorModel.getVendorRequest() + 1;
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			// VendorResponse
			int respCount = vendorModel.getVendorResponse() + 1;
			vendorModel.setVendorResponse(respCount);

			// Convert the string to a JSONObject
			JSONObject jsonObject = new JSONObject(data);

			Request request = new Request();
			Date reqDate = new Date();

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			String status = jsonObject.getString("status");
			//
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

			String referenceId = decryptJson.getString("reference_id");
			String responseTime = decryptJson.getString("response_time_stamp");

			request.setReferenceId(referenceId);

			request.setSource(source);
			request.setSourceType(source_type);
			request.setRequestBy(userModel.getName());
			request.setRequestDateAndTime(reqDate);
			request.setStatus(responseStatus);
			request.setResponseDateAndTime(responseTime);
			request.setUser(userModel);
			request.setVerificationModel(serviceModel);
			request.setPrice(merchantPriceModel.getIdPrice());

			Response response = new Response();

			response.setReferenceId(referenceId);
			response.setSource(source);
			response.setSourceType(source_type);
			response.setRequestBy(userModel.getName());
			response.setRequestDateAndTime(reqDate);
			response.setStatus(responseStatus);
			response.setResponseDateAndTime(responseTime);
			response.setRequest(request);
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponse(decryptData);

			if (responseStatus.equals("success")) {
				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");
				request.setMessage(message);
				request.setTransactionId(transactionId);
				response.setTransactionId(transactionId);

				response.setMessage(message);
				reqRepository.save(request);
				respRepository.save(response);

			} else {
				String errorCode = decryptJson.getString("error_code");
				request.setErrorCode(errorCode);
				response.setErrorCode(errorCode);
				respRepository.save(response);
				reqRepository.save(request);

			}
			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);
			object.put("request_id", request.getRequestId());
			object.put("response_id", response.getResponseId());

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
			System.out.println("RETURN RESPONSE : " + returnResponse);

			//
			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
			mapNew.put("Response", response);
			mapNew.put("Request", request);

			structure.setData(mapNew);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure aadhaarOtpVerification(RequestModel dto, HttpServletRequest servletRequest) {
		ResponseStructure structure = new ResponseStructure();
		try {

			if (dto.getVendorVerificationId() == 5) {
				EntityModel userModel = userRepository.findByUserId(dto.getUserId());
				String userSecretKey = userModel.getSecretKey();

				VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
				VendorVerificationModel serviceModel = verificationRepository
						.findByVendorVerificationId(dto.getVendorVerificationId());
				MerchantPriceModel merchantPriceModel = merchantPriceRepository
						.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel,
								userModel);
				VendorPriceModel vendorPrice = vendorPriceRepository
						.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

				Response response = respRepository.findByResponseId(dto.getResponseId());
				Request request = reqRepository.findByRequestId(dto.getRequestId());

				String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);
				JSONObject json = new JSONObject(userDecryption);

				String referenceId = json.getString("reference_id");
				String transactionId = json.getString("transaction_id");
				String otp = json.getString("otp");

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

				// VendorReq
				int reqCount = vendorModel.getVendorRequest() + 1;
				vendorModel.setVendorRequest(reqCount);

				HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

				ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
						String.class);
				String data = clientResponse.getBody();

				// VendorResponse
				int respCount = vendorModel.getVendorResponse() + 1;
				vendorModel.setVendorResponse(respCount);

				// Convert the string to a JSONObject
				JSONObject jsonObject = new JSONObject(data);

				// Extract specific data
				String encryptedResponse = jsonObject.getString("encrypted_response");
				String status = jsonObject.getString("status");
				//
				// // Print the extracted data
				System.out.println("encrypted_response: " + encryptedResponse);
				System.out.println("status: " + status);
				//
				String decryptData = PasswordUtils.decryptString(encryptedResponse, key);
				// String decryptData=PasswordUtils.jsonDecryption(encryptedResponse);
				JSONObject decryptJson = new JSONObject(decryptData);
				//
				System.out.println("Decrypted Response = " + decryptData);

				String responseTime = decryptJson.getString("response_time_stamp");

				String statusResponse = decryptJson.getString("status");

				request.setStatus(statusResponse);
				request.setOtp(otp);

				response.setStatus(statusResponse);

				JSONObject object = new JSONObject();
				object.put("status", status);
				object.put("encrypted_response", decryptData);

				String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
				System.out.println("RETURN RESPONSE : " + returnResponse);

				System.out.println("Status : " + status);
				System.out.println("Status Response :" + statusResponse);

				if (statusResponse.equals("success")) {

					JSONObject resultJson = decryptJson.getJSONObject("result");
					JSONObject validatedData = resultJson.getJSONObject("validated_data");

//					String fullName = validatedData.getString("full_name");
//					String aadharNumber = validatedData.getString("aadhaar_number");
//					String dob = validatedData.getString("dob");

					String message = decryptJson.getString("message");
					String transaction = decryptJson.getString("transaction_id");

					request.setTransactionId(transaction);
					request.setMessage(message);
					request.setResponseDateAndTime(responseTime);
//					request.setFullName(fullName);
//					request.setAadharNumber(aadharNumber);
//					request.setDob(dob);
					reqRepository.save(request);

					response.setTransactionId(transaction);
					response.setMessage(message);
					response.setResponseDateAndTime(responseTime);
					response.setRequest(request);
//					response.setResponse(decryptData);
//					response.setFullName(fullName);
//					response.setAadhaarNumber(aadharNumber);
//					response.setDob(dob);
					respRepository.save(response);
				} else {
					// String error = decryptJson.getString("error");
					String errorCode = decryptJson.getString("error_code");

					request.setResponseDateAndTime(responseTime);
					// request.setError(error);
					request.setErrorCode(errorCode);
					reqRepository.save(request);

					response.setResponse(decryptData);
					// response.setError(error);
					response.setErrorCode(errorCode);
					response.setResponseDateAndTime(responseTime);
					response.setRequest(request);

					respRepository.save(response);

				}
				vendorRepository.save(vendorModel);
				Map<String, Object> mapNew = new HashMap<>();
				mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

				structure.setData(mapNew);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setMessage(AppConstants.SUCCESS);
			} else {
				structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	// E SIGN
	@Override
	public ResponseStructure eSignDemo(CommonRequestDto dto) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String referenceId = FileUtils.getRandomOTPnumber(10);
			String content = PasswordUtils.demoImageEncryption(dto.getContent());
			System.out.println("Pdf Content : " + content);

			List<JSONObject> document = new ArrayList<>();
			JSONObject object1 = new JSONObject();
			object1.put("reference_doc_id", dto.getReference_doc_id());
			object1.put("content_type", dto.getContent_type());
			object1.put("content", content);
			object1.put("signature_sequence", dto.getSignature_sequence());

			document.add(object1);

			List<JSONObject> singerInfo = new ArrayList<>();

			JSONObject object2 = new JSONObject();
			object2.put("appearance", dto.getAppearance());

			JSONObject object4 = new JSONObject();
			object4.put("name_as_per_aadhaar", dto.getName_as_per_aadhaar());

			JSONObject object3 = new JSONObject();
			object3.put("document_to_be_signed", dto.getDocument_to_be_signed());
			object3.put("trigger_esign_request", true);
			object3.put("signer_position", object2);
			object3.put("signer_ref_id", dto.getSigner_ref_id());
			object3.put("signer_email", dto.getSigner_email());
			object3.put("signer_name", dto.getSigner_name());
			object3.put("sequence", dto.getSequence());
			object3.put("page_number", dto.getPage_number());
			object3.put("esign_type", dto.getEsign_type());
			object3.put("signer_mobile", dto.getSigner_mobile());
			object3.put("signature_type", dto.getSignature_type());
			object3.put("signer_validation_inputs", object4);

			singerInfo.add(object3);

			JSONObject object = new JSONObject();
			object.put("reference_id", referenceId);
			object.put("docket_title", dto.getDocket_title());
			object.put("documents", document);
			object.put("signers_info", singerInfo);

			EntityModel model = userRepository.findByUserId(dto.getUserId());
			String key = model.getSecretKey();
			String encryptJson = PasswordUtils.demoEncryption(object, key);

			structure.setData(encryptJson);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {

			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
		}
		return structure;
	}

	@Override
	public ResponseStructure esignValidate(RequestModel dto) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Request request = new Request();

			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);
			System.out.println("UserDecryption  : " + userDecryption);
			JSONObject userJosn = new JSONObject(userDecryption);

			// Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 11) {

						return esignValidationProcess(merchantPriceModel, request, userModel, userJosn, serviceModel,
								vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 11) {

						return esignValidationProcess(merchantPriceModel, request, userModel, userJosn, serviceModel,
								vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {

					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
		}
		return structure;
	}

	public ResponseStructure esignValidationProcess(MerchantPriceModel merchantPriceModel, Request request,
			EntityModel userModel, JSONObject userJosn, VendorVerificationModel serviceModel, VendorModel vendorModel) {

		ResponseStructure structure = new ResponseStructure();
		try {
			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			Date requestDate = new Date();
			// create an instance of RestTemplate

			RestTemplate restTemplate = new RestTemplate();

			// create headers

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.add("x-parse-rest-api-key", vendorPrice.getApiKey());
			headers.add("x-parse-application-id", vendorPrice.getApplicationId());
			headers.add("Content-Type", AppConstants.CONTENT_TYPE);

			// VendorReq
			int reqCount = vendorModel.getVendorRequest() + 1;
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(userJosn.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			System.out.println(clientResponse.getStatusCode());

			String data = clientResponse.getBody();

			// VendorResponse
			int respCount = vendorModel.getVendorResponse() + 1;
			vendorModel.setVendorResponse(respCount);

			System.out.println("DATA : " + data);

			JSONObject jsonObject = new JSONObject(data);
			System.out.println("JsonObject : " + jsonObject);

			String status = jsonObject.getString("status");
			String apiResponseId = jsonObject.getString("api_response_id");
			String responseTimeStamp = jsonObject.getString("response_time_stamp");

			request.setStatus(status);
			request.setApiResponseId(apiResponseId);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setRequestDateAndTime(requestDate);
			request.setUser(userModel);
			request.setVerificationModel(serviceModel);
			request.setPrice(merchantPriceModel.getIdPrice());

			Response response = new Response();

			response.setStatus(status);
			response.setApiResponseId(apiResponseId);
			response.setResponseDateAndTime(responseTimeStamp);
			response.setRequestDateAndTime(requestDate);
			response.setUser(userModel);
			response.setVendorModel(vendorModel);

			// Prepaid Amount Reduction
			double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
			double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

			userModel.setRemainingAmount(remainingAmount);
			userModel.setConsumedAmount(consumedAmount);
			userModel.setPaymentStatus("No Dues");

			if (status.equals("success")) {

				String message = jsonObject.getString("message");
				String docketId = jsonObject.getString("docket_id");

				JSONArray signerDetails = jsonObject.getJSONArray("signer_info");

				JSONObject internalSignerDetails = signerDetails.getJSONObject(0);

				String signerRefId = internalSignerDetails.getString("signer_ref_id");
				String signerId = internalSignerDetails.getString("signer_id");
				String documentId = internalSignerDetails.getString("document_id");
				String referenceDocId = internalSignerDetails.getString("reference_doc_id");

				request.setDocketId(docketId);
				request.setMessage(message);
				request.setSignerRefId(signerRefId);
				request.setSignerId(signerId);
				request.setDocumentId(documentId);
				request.setReferenceDocId(referenceDocId);
				reqRepository.save(request);

				response.setResponse(data);
				response.setMessage(message);
				response.setDocketId(docketId);
				response.setSignerRefId(signerRefId);
				response.setSignerId(signerId);
				response.setDocumentId(documentId);
				response.setReferenceDocId(referenceDocId);
				response.setRequest(request);

				respRepository.save(response);

			} else {

				String errorCode = jsonObject.getString("error_code");
				String error = jsonObject.getString("error");

				request.setErrorCode(errorCode);
				request.setError(error);
				reqRepository.save(request);

				response.setResponse(data);
				response.setErrorCode(errorCode);
				response.setError(error);
				response.setRequest(request);
				respRepository.save(response);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(responseCount + 1);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", data);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			// // Extract specific data
			// String encryptedResponse = jsonObject.getString("encrypted_response");
			// String status = jsonObject.getString("status");
			// // Print the extracted data
			// System.out.println("encrypted_response: " + encryptedResponse);
			// System.out.println("status: " + status);
			// String decryptData=PasswordUtils.decryptString(encryptedJson,key);
			// String decryptData=PasswordUtils.jsonDecryption(encryptedResponse);
			// JSONObject decryptJson = new JSONObject(decryptData);
			// System.out.println("Decrypted Response = "+ decryptData);

			structure.setData(mapNew);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
		}

		return structure;
	}

	@Override
	public ResponseStructure findById(int requestId) {
		ResponseStructure structure = new ResponseStructure();
		Optional<Response> optional = respRepository.findById(requestId);
		if (optional.isPresent()) {
			Response request = optional.get();

			structure.setData(request);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);

		} else {
			structure.setData(null);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseStructure demoController(RequestModel dto) {
		ResponseStructure structure = new ResponseStructure();
		try {
			// String encryptImage=PasswordUtils.demoImageEncryption(model);

			JSONObject inputParams = new JSONObject();
			String referenceNumber = FileUtils.getRandomOTPnumber(10);

			inputParams.put("source", dto.getSource());
			// inputParams.put("reference_id", referenceNumber);
			// inputParams.put("source_type", "id");
			inputParams.put("ifsc_code", dto.getIfscCode());
			inputParams.put("extended_data", dto.getExtendedData());
			inputParams.put("dob", dto.getDob()); // FOR DL & PASSSPORT
			inputParams.put("filing_status_get", dto.isFilingStatus()); // FOR GST

			EntityModel user = userRepository.findByUserId(dto.getUserId());
			String key = user.getSecretKey();
			System.out.println("Secret key : " + key);
			String json = PasswordUtils.demoEncryption(inputParams, key);
			System.out.println("json : " + json);
			String decryption1 = PasswordUtils.demoDecrypt(json, key);

			System.out.println("json Decrypt : " + decryption1);

			structure.setData(json);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			e.printStackTrace();
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure demoOtpController(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			// String encryptImage=PasswordUtils.demoImageEncryption(model);
			// Response response = respRepository.findByResponseId(model.getResponseId());
			EntityModel user = userRepository.findByUserId(model.getUserId());
			JSONObject inputParams = new JSONObject();

			inputParams.put("reference_id", model.getReferenceId());// response.
			inputParams.put("transaction_id", model.getTransactionId());
			inputParams.put("otp", model.getOtp());

			String key = user.getSecretKey();
			System.out.println("Secret key : " + key);
			String json = PasswordUtils.demoEncryption(inputParams, key);
			System.out.println("json : " + json);
			String decryption1 = PasswordUtils.demoDecrypt(json, key);

			System.out.println("json Decrypt : " + decryption1);
			// JSONObject jsonParams=new JSONObject();
			// jsonParams.put("encrypted_data", json);
			// jsonParams.put("user_id", model.getUserId());
			// String jsonEncrypt=PasswordUtils.demoEncryption(jsonParams,key);
			// System.out.println("jsonEncrypt : "+jsonEncrypt);
			//
			//
			// String decryption=PasswordUtils.demoDecrypt(jsonEncrypt, key);
			// System.out.println(" Decryption Final : "+decryption);

			structure.setData(json);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			e.printStackTrace();
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure demoImageController(MultipartFile image, int userId) {
		ResponseStructure structure = new ResponseStructure();

		try {
			String encryptImage = PasswordUtils.demoImageEncryption(image);

			JSONObject inputParams = new JSONObject();
			String referenceNumber = FileUtils.getRandomOTPnumber(10);

			inputParams.put("reference_id", referenceNumber);
			inputParams.put("source_type", "base64");
			inputParams.put("source", encryptImage);
			inputParams.put("filing_status_get", true);
			EntityModel user = userRepository.findByUserId(userId);
			String key = user.getSecretKey();
			System.out.println("Secret key : " + key);
			String json = PasswordUtils.demoEncryption(inputParams, key);
			System.out.println("json : " + json);
			String decryption1 = PasswordUtils.demoDecrypt(json, key);

			System.out.println("json Decrypt : " + decryption1);
			// JSONObject jsonParams=new JSONObject();
			// jsonParams.put("encrypted_data", json);
			// jsonParams.put("user_id", model.getUserId());
			// String jsonEncrypt=PasswordUtils.demoEncryption(jsonParams,key);
			// System.out.println("jsonEncrypt : "+jsonEncrypt);
			//
			//
			// String decryption=PasswordUtils.demoDecrypt(jsonEncrypt, key);
			// System.out.println(" Decryption Final : "+decryption);

			structure.setData(json);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			e.printStackTrace();
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure demoImageControllerForTwoImages(MultipartFile front, MultipartFile rear, int userId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			String frontImage = PasswordUtils.demoImageEncryption(front);
			String rearImage = PasswordUtils.demoImageEncryption(rear);
			List<String> encryptedImage = new ArrayList<>();
			encryptedImage.add(frontImage);
			encryptedImage.add(rearImage);

			JSONObject inputParams = new JSONObject();
			String referenceNumber = FileUtils.getRandomOTPnumber(10);

			inputParams.put("reference_id", referenceNumber);
			inputParams.put("source_type", "base64");
			inputParams.put("source", encryptedImage);
			inputParams.put("filing_status_get", true);
			EntityModel user = userRepository.findByUserId(userId);
			String key = user.getSecretKey();
			System.out.println("Secret key : " + key);
			String json = PasswordUtils.demoEncryption(inputParams, key);
			System.out.println("json : " + json);
			String decryption1 = PasswordUtils.demoDecrypt(json, key);

			System.out.println("json Decrypt : " + decryption1);
			// JSONObject jsonParams=new JSONObject();
			// jsonParams.put("encrypted_data", json);
			// jsonParams.put("user_id", model.getUserId());
			// String jsonEncrypt=PasswordUtils.demoEncryption(jsonParams,key);
			// System.out.println("jsonEncrypt : "+jsonEncrypt);
			//
			//
			// String decryption=PasswordUtils.demoDecrypt(jsonEncrypt, key);
			// System.out.println(" Decryption Final : "+decryption);

			structure.setData(json);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			e.printStackTrace();
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;

	}

	@Override
	public ResponseStructure totalHitCount() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<Request> list = reqRepository.findAll();

			structure.setCount(list.size());
			structure.setData(list);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			structure.setCount(0);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure hitCountByEntity(int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> model = userRepository.findById(userId);

			if (model.isPresent()) {

				EntityModel entityModel = model.get();

				List<Request> list = reqRepository.findByUserAndFreeHit(entityModel, false);

				structure.setCount(list.size());
				structure.setData(list);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {
				structure.setCount(0);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setCount(0);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure hitForToday() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<Request> list = reqRepository.findAll();
			List<Request> arrayList = new ArrayList<>();
			int count = 0;
			for (Request request : list) {

				LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
						ZoneId.systemDefault());
				if (date.equals(LocalDate.now())) {
					arrayList.add(request);
					count++;
					System.out.println(date);
				}

			}
			System.out.println(count);

			structure.setCount(count);
			structure.setData(arrayList);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			structure.setCount(0);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure hitForThisMonth(int month) {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<Request> list = reqRepository.findAll();
			List<Request> arrayList = new ArrayList<>();
			int count = 0;
			for (Request request : list) {

				LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
						ZoneId.systemDefault());
				System.out.println("Month Value : " + date.getMonthValue());
				System.out.println("month input : " + month);

				if (date.getMonthValue() == month) {
					arrayList.add(request);
					count++;
					System.out.println(date);
				}

			}
			structure.setCount(count);
			structure.setData(arrayList);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			structure.setCount(0);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure hitForThisWeek(String date1, String date2) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Date dateTime1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date1);
			Date dateTime2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date2);

			List<Request> list = reqRepository.findByRequestDateAndTime(dateTime1, dateTime2);

			int count = 0;
			for (Request request : list) {
				count++;
			}

			structure.setCount(count);
			structure.setData(list);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			e.printStackTrace();
			structure.setCount(0);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure totalResponseHitCount() {

		ResponseStructure structure = new ResponseStructure();
		try {
			List<Response> list = respRepository.findAll();

			structure.setCount(list.size());
			structure.setData(list);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			structure.setCount(0);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure responsehitCountByEntity(int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> model = userRepository.findById(userId);
			if (model.isPresent()) {

				List<Response> list = respRepository.findByUser(model);
				int count = 0;
				for (Response response : list) {
					count++;
				}

				System.out.println("Count : " + count);

				structure.setCount(count);
				structure.setData(list);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {
				structure.setCount(0);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setCount(0);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;

	}

	@Override
	public ResponseStructure responseHitCountForToday() {

		ResponseStructure structure = new ResponseStructure();
		try {

			List<Response> list = respRepository.findAll();
			List<Response> arrayList = new ArrayList<>();
			int count = 0;
			for (Response response : list) {

				LocalDate date = LocalDate.ofInstant(response.getRequestDateAndTime().toInstant(),
						ZoneId.systemDefault());
				if (date.equals(LocalDate.now())) {
					arrayList.add(response);
					count++;
					// System.out.println(date);
				}

			}
			System.out.println(count);

			structure.setCount(count);
			structure.setData(arrayList);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			structure.setCount(0);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure hitResponseForThisMonth(int month) {

		ResponseStructure structure = new ResponseStructure();
		try {

			List<Response> list = respRepository.findAll();
			List<Response> arrayList = new ArrayList<>();
			int count = 0;
			for (Response response : list) {

				LocalDate date = LocalDate.ofInstant(response.getRequestDateAndTime().toInstant(),
						ZoneId.systemDefault());

				if (date.getMonthValue() == month) {
					arrayList.add(response);
					count++;
				}

			}
			structure.setCount(count);
			structure.setData(arrayList);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			structure.setCount(0);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure responseHitForThisWeek(String date1, String date2) {
		ResponseStructure structure = new ResponseStructure();
		try {
			String suffix = "T00:00:00";
			String suffix2 = "T23:59:59";

			LocalDateTime dateTime1 = LocalDateTime.parse(date1 + suffix);
			LocalDateTime dateTime2 = LocalDateTime.parse(date2 + suffix2);

			List<Response> list = respRepository.findAll();
			List<LocalDateTime> listDate = new ArrayList<>();

			int count = 0;
			for (Response response : list) {

				LocalDateTime responseDateTime = LocalDateTime.parse(response.getResponseDateAndTime());
				listDate.add(responseDateTime);
			}

			int size = listDate.size();
			List<LocalDateTime> responseData = new ArrayList<>();

			for (int i = 0; i < size; i++) {

				if (listDate.get(i).equals(dateTime1) || listDate.get(i).equals(dateTime2)) {
					count++;
					responseData.add(listDate.get(i));

				}
				if (listDate.get(i).isAfter(dateTime1) && listDate.get(i).isBefore(dateTime2)) {
					count++;
					responseData.add(listDate.get(i));
				}

			}
			structure.setCount(count);
			// structure.setData(responseData);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			e.printStackTrace();
			structure.setCount(0);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;

	}

	@Override
	public ResponseStructure hitResponseCountByRequest(int requestId) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<Request> req = reqRepository.findById(requestId);
			if (req.isPresent()) {

				List<Response> list = respRepository.findByRequest(req);
				int count = 0;
				for (Response response : list) {
					count++;
				}

				System.out.println("Count : " + count);

				structure.setCount(count);
				structure.setData(list);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {
				structure.setCount(0);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setCount(0);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;

	}

	@Override
	public ResponseStructure successResponseCount() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<Request> requestList = reqRepository.findAll();

			int successCount = 0;
			int failureCount = 0;

			if (!requestList.isEmpty()) {

				for (Request request : requestList) {

					if (request.getStatus().equals("success")) {
						successCount++;
					}
					if (request.getStatus().equals("failed")) {
						failureCount++;

					}

				}

				Map<String, Integer> map = new HashMap<>();
				map.put("successCount", successCount);
				map.put("failurCount", failureCount);

				structure.setData(map);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {
				structure.setCount(0);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setCount(0);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure databaseSuccessResponse() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<Response> responseList = respRepository.findAll();
			int responseCount = 0;

			if (!responseList.isEmpty()) {
				for (Response response : responseList) {
					if (response.getStatus().equals("success")) {
						responseCount++;
					}
				}

				System.out.println("Success Response : " + responseCount);

				// structure.setData();
				structure.setCount(responseCount);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
			} else {
				structure.setCount(0);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setCount(0);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure adminAllInOneDashboard() {
		ResponseStructure structure = new ResponseStructure();
		try {

			// 1.MERCHANT COUNT
			List<EntityModel> entityList = userRepository.findAll();
			int userCount = 0;

			if (!entityList.isEmpty()) {
				for (EntityModel model : entityList) {
					userCount++;
				}
			}

			// 2.SUCCESS&RSPONSE COUNT
			List<Request> requestList = reqRepository.findAll();

			int successCount = 0;
			int failureCount = 0;

			if (!requestList.isEmpty()) {

				for (Request request : requestList) {

					if (request.getStatus().equals("success")) {
						successCount++;
					}
					if (request.getStatus().equals("failed")) {
						failureCount++;

					}

				}
			}

			// 3.DB SUCCESSRESPONSE
			List<Response> responseList = respRepository.findAll();
			int responseCount = 0;

			if (!responseList.isEmpty()) {
				for (Response response : responseList) {
					if (response.getStatus().equals("success")) {
						responseCount++;
					}
				}

			}

			// 4.POSTPAID MERCHANT COUNT
			// 5.PREPAID MERCHANT COUNT

			PaymentMethod postpaidPayment = paymentRepository.findByPaymentId(1);// POSTPAID
			PaymentMethod prepaidPayment = paymentRepository.findByPaymentId(2);// PREPAID

			int postpaidCount = 0;
			int prepaidCount = 0;

			List<EntityModel> postpaidPaymentList = userRepository.findByPaymentMethod(postpaidPayment);
			List<EntityModel> prepaidPaymentList = userRepository.findByPaymentMethod(prepaidPayment);

			if (!postpaidPaymentList.isEmpty()) {
				for (EntityModel entityModel : postpaidPaymentList) {
					postpaidCount++;
				}
			}
			if (!prepaidPaymentList.isEmpty()) {
				for (EntityModel entityModel : prepaidPaymentList) {
					prepaidCount++;
				}

				// 6.DATABASE SUCCESS RESPONSE
				List<Response> dbSuccessResponseList = respRepository.findAll();
				int dbSuccessResponseCount = 0;

				if (!responseList.isEmpty()) {
					for (Response response : dbSuccessResponseList) {
						if (response.getStatus().equals("success")) {
							dbSuccessResponseCount++;
						}
					}
				}

				Map<String, Integer> map = new LinkedHashMap<>();
				map.put("onBoardMerchant", userCount);
				map.put("successCount", successCount);
				map.put("failurCount", failureCount);
				map.put("postPaidMerchant", postpaidCount);
				map.put("prePaidMerchant", prepaidCount);
				map.put("dataBaseSuccessResponse", dbSuccessResponseCount);

				structure.setData(map);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			}
		} catch (Exception e) {

			e.printStackTrace();
			structure.setData(null);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
		}
		return structure;
	}

	@Override
	public ResponseStructure merchantAllInOneDashboard(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			// 1.TOTAL LOG HIT
			Optional<EntityModel> optional = userRepository.findById(model.getUserId());
			if (optional.isPresent()) {
				EntityModel entity = optional.get();

				List<Request> list = reqRepository.findByUser(entity);

				int logCount = 0;
				int successResponseCount = 0;
				int failureResponseCount = 0;
				int monthlyCount = 0;
				int monthlySuccessCount = 0;
				int monthlyFailureCount = 0;

				double prepaidRemainingAmount = entity.getRemainingAmount();
				double postpaidAmount = 0;
				LocalDate postPaidDate = LocalDate.now();

				List<PostpaidPayment> paymentList = postpaidRepository.findByEntityModel(entity);

				int size = paymentList.size();
				if (size >= 2) {

					PostpaidPayment lastPaymentDetails = paymentList.get(size - 2);

					postpaidAmount = lastPaymentDetails.getTotalAmount();

					postPaidDate = lastPaymentDetails.getPaidDate();

					System.out.println("Amount : " + postpaidAmount);
					System.out.println("Date : " + postPaidDate);
				}

				for (Request userRequest : list) {
					logCount++;

					if (userRequest.getStatus().equals("success")) {
						successResponseCount++;
					} else if (userRequest.getStatus().equals("failed")) {
						failureResponseCount++;
					}

					// MONTHLY COUNTS

					LocalDate date = LocalDate.ofInstant(userRequest.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());

					if (date.getMonthValue() == model.getMonth()) {

						monthlyCount++;

						if (userRequest.getStatus().equals("success")) {
							monthlySuccessCount++;
						} else if (userRequest.getStatus().equals("failed")) {
							monthlyFailureCount++;
						}
					}
				}

				String merchantLogCount = String.valueOf(logCount);
				String merchantSuccessResponse = String.valueOf(successResponseCount);
				String merchantFailureResponse = String.valueOf(failureResponseCount);
				String merchantMonthlyCount = String.valueOf(monthlyCount);
				String merchantMonthlySuccessCount = String.valueOf(monthlySuccessCount);
				String merchantMonthlyFailureCount = String.valueOf(monthlyFailureCount);

				String remainingPrepaidAmount = String.valueOf(prepaidRemainingAmount);
				String LastPayPostpaidAmount = String.valueOf(postpaidAmount);
				String postpaidDate = postPaidDate.toString();

				Map<String, String> map = new LinkedHashMap<>();

				map.put("merchantLogCount", merchantLogCount);
				map.put("merchantSuccessResponse", merchantSuccessResponse);
				map.put("merchantFailureResponse", merchantFailureResponse);
				map.put("merchantMonthlyCount", merchantMonthlyCount);
				map.put("merchantMonthlySuccessCount", merchantMonthlySuccessCount);
				map.put("merchantMonthlyFailureCount", merchantMonthlyFailureCount);
				map.put("remainingPrepaidAmount", remainingPrepaidAmount);
				map.put("lastPayPostpaidAmount", LastPayPostpaidAmount);
				map.put("lastPayPostpaidDate", postpaidDate);

				structure.setData(map);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
		}
		return structure;
	}

	@Override
	public ResponseStructure cinVerification(RequestModel dto, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(userDecryption);

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 9) {

						return cinNumberCheck(userJosn, userModel, merchantPriceModel, dto, serviceModel, userSecretKey,
								vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 9) {

						return cinNumberCheck(userJosn, userModel, merchantPriceModel, dto, serviceModel, userSecretKey,
								vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return structure;
		}

		return structure;
	}

	private ResponseStructure cinNumberCheck(JSONObject userJosn, EntityModel userModel,
			MerchantPriceModel merchantPriceModel, RequestModel dto, VendorVerificationModel serviceModel,
			String userSecretKey, VendorModel vendorModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");
			// Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);

			// Request request = new Request();
			List<Response> sourceList = respRepository.findBySource(source);
			// Response model = new Response();
			if (!sourceList.isEmpty()) {

				Response model = sourceCheck(sourceList);
				boolean isMatch = false;
				if (model != null) {
					isMatch = true;
				}

//				boolean isMatch = false;
//				for (Response response : sourceList) {
//					if (response.getStatus().equals("success")) {
//						model = response;
//						isMatch = true;
//						break;
//					}
//				}
				if (isMatch) {

					return setRequest(model, dto, merchantPriceModel, userModel, serviceModel);

//					request.setReferenceId(model.getReferenceId());
//					request.setMessage(model.getMessage());
//					request.setTransactionId(model.getTransactionId());
//					request.setSource(model.getSource());
//					request.setSourceType(model.getSourceType());
//					request.setRequestBy(dto.getRequestBy());
//					request.setRequestDateAndTime(new Date());
//					request.setPrice(merchantPriceModel.getIdPrice());
//
//					request.setCompanyName(model.getBusinessName());
//					request.setCompanyId(model.getCompanyId());
//					request.setCompanyType(model.getCompanyType());
//					request.setEmail(model.getEmail());
//
//					Date date = new Date();
//					SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					String stringDate = simple.format(date);
//					request.setResponseDateAndTime(stringDate);
//					request.setStatus(model.getStatus());
//					request.setUser(userModel);
//					request.setVerificationModel(serviceModel);
//
//					reqRepository.save(request);

//					JSONObject object = new JSONObject();
//					object.put("status", model.getStatus());
//					object.put("encrypted_response", model.getResponse());
//
//					String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
//					System.out.println("RETURN RESPONSE : " + returnResponse);
//
//					// Prepaid Amount Reduction
//					if (userModel.getPaymentMethod().getPaymentId() == 2) {
//						double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
//						double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();
//
//						userModel.setRemainingAmount(remainingAmount);
//						userModel.setConsumedAmount(consumedAmount);
//						userModel.setPaymentStatus("No Dues");
//					}
//
//					// Response Count
//					int responseCount = userModel.getResponseCount();
//					userModel.setResponseCount(responseCount + 1);
//					userRepository.save(userModel);
//
////					Map<String, Object> mapNew = new HashMap<>();
////					mapNew.put("Return Response", returnResponse);
////					mapNew.put("Response", model);
////					mapNew.put("Request", request);
//
//					structure.setData(returnResponse);
//					structure.setStatusCode(HttpStatus.OK.value());
//					structure.setFlag(1);
//					structure.setMessage(AppConstants.SUCCESS);

				} else {
					return cinVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
							userSecretKey, vendorModel, merchantPriceModel);

				}

			} else {
				return cinVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
						userSecretKey, vendorModel, merchantPriceModel);
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure cinVerificationProcess(RequestModel dto, EntityModel user, String reference_Id,
			VendorVerificationModel serviceModel, String source, String sourceType, String userSecretKey,
			VendorModel vendorModel, MerchantPriceModel merchantPriceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {

			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			Request request = new Request();
			Date reqDate = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// set request details

			inputParams.put("reference_id", reference_Id);
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

			// VendorReq
			int reqCount = vendorModel.getVendorRequest() + 1;
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			JSONObject jsonObject = new JSONObject(data);

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

			// Prepaid Amount Reduction
			if (user.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = user.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = user.getConsumedAmount() + merchantPriceModel.getIdPrice();

				user.setRemainingAmount(remainingAmount);
				user.setConsumedAmount(consumedAmount);
				user.setPaymentStatus("No Dues");
			}

			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSource(source);
			request.setSourceType(sourceType);
			request.setPrice(merchantPriceModel.getIdPrice());
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);
			request.setUser(user);
			request.setVerificationModel(serviceModel);

			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(sourceType);
			response.setSource(source);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(user);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
			System.out.println("RETURN RESPONSE : " + returnResponse);

			if (status.equals("success")) {

				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");

				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");

				String companyName = validatedJson.getString("company_name");
				String companyId = validatedJson.getString("company_id");
				String companyType = validatedJson.getString("company_type");

				JSONObject details = validatedJson.getJSONObject("details");
				JSONObject companyInfo = details.getJSONObject("company_info");
				String email = companyInfo.getString("email_id");

				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setCompanyName(companyName);
				request.setCompanyId(companyId);
				request.setCompanyType(companyType);
				request.setEmail(email);
				request.setResponseDateAndTime(responseTimeStamp);
				reqRepository.save(request);

				response.setBusinessName(companyName);
				response.setCompanyId(companyId);
				response.setCompanyType(companyType);
				response.setEmail(email);
				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setRequest(request);
				respRepository.save(response);

			} else {

				String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				request.setResponseDateAndTime(responseTimeStamp);

				request.setError(error);
				request.setErrorCode(errorCode);
				reqRepository.save(request);

				response.setResponse(decryptData);
				response.setError(error);
				response.setErrorCode(errorCode);
				response.setResponseDateAndTime(responseTimeStamp);
				response.setRequest(request);

				respRepository.save(response);

			}

			// Response Count
			int responseCount = user.getResponseCount();
			user.setResponseCount(++responseCount);
			userRepository.save(user);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("Return Response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setData(mapNew);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	@Override
	public ResponseStructure dinVerification(RequestModel dto, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(userDecryption);

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 10) {

						return dinNumberCheck(userJosn, userModel, merchantPriceModel, dto, serviceModel, userSecretKey,
								vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 10) {

						return dinNumberCheck(userJosn, userModel, merchantPriceModel, dto, serviceModel, userSecretKey,
								vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	private ResponseStructure dinNumberCheck(JSONObject userJosn, EntityModel userModel,
			MerchantPriceModel merchantPriceModel, RequestModel dto, VendorVerificationModel serviceModel,
			String userSecretKey, VendorModel vendorModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");

			// Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);

			// Request request = new Request();
			List<Response> sourceList = respRepository.findBySource(source);
			// Response model = new Response();
			if (!sourceList.isEmpty()) {

				Response model = sourceCheck(sourceList);
				boolean isMatch = false;
				if (model != null) {
					isMatch = true;
				}

//				boolean isMatch = false;
//				for (Response response : sourceList) {
//					if (response.getStatus().equals("success")) {
//						model = response;
//						isMatch = true;
//						break;
//					}
//				}
				if (isMatch) {

					return setRequest(model, dto, merchantPriceModel, userModel, serviceModel);

//					request.setReferenceId(model.getReferenceId());
//					request.setMessage(model.getMessage());
//					request.setTransactionId(model.getTransactionId());
//					request.setSource(model.getSource());
//					request.setSourceType(model.getSourceType());
//					request.setRequestBy(dto.getRequestBy());
//					request.setRequestDateAndTime(new Date());
//					request.setPrice(merchantPriceModel.getIdPrice());
//
//					request.setEmail(model.getEmail());
//
//					Date date = new Date();
//					SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					String stringDate = simple.format(date);
//					request.setResponseDateAndTime(stringDate);
//					request.setStatus(model.getStatus());
//					request.setUser(userModel);
//					request.setVerificationModel(serviceModel);
//
//					reqRepository.save(request);

//					JSONObject object = new JSONObject();
//					object.put("status", model.getStatus());
//					object.put("encrypted_response", model.getResponse());
//
//					String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
//					System.out.println("RETURN RESPONSE : " + returnResponse);
//
//					// Prepaid Amount Reduction
//					if (userModel.getPaymentMethod().getPaymentId() == 2) {
//						double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
//						double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();
//
//						userModel.setRemainingAmount(remainingAmount);
//						userModel.setConsumedAmount(consumedAmount);
//						userModel.setPaymentStatus("No Dues");
//					}
//
//					// Response Count
//					int responseCount = userModel.getResponseCount();
//					userModel.setResponseCount(responseCount + 1);
//					userRepository.save(userModel);
//
////					Map<String, Object> mapNew = new HashMap<>();
////					mapNew.put("Return Response", returnResponse);
////					mapNew.put("Response", model);
////					mapNew.put("Request", request);
//
//					structure.setData(returnResponse);
//					structure.setStatusCode(HttpStatus.OK.value());
//					structure.setFlag(1);
//					structure.setMessage(AppConstants.SUCCESS);
//
//					return structure;
				} else {
					return dinVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
							userSecretKey, vendorModel, merchantPriceModel);

				}

			} else {
				return dinVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
						userSecretKey, vendorModel, merchantPriceModel);
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;

	}

	private ResponseStructure dinVerificationProcess(RequestModel dto, EntityModel userModel, String reference_Id,
			VendorVerificationModel serviceModel, String source, String sourceType, String userSecretKey,
			VendorModel vendorModel, MerchantPriceModel merchantPriceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {

			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			Request request = new Request();
			Date reqDate = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// set request details

			inputParams.put("reference_id", reference_Id);
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

			// VendorReq
			int reqCount = vendorModel.getVendorRequest() + 1;
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			// VendorResponse
			int respCount = vendorModel.getVendorResponse() + 1;
			vendorModel.setVendorResponse(respCount);

			JSONObject jsonObject = new JSONObject(data);

			Response response = new Response();

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			//
			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			//

			String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

			JSONObject decryptJson = new JSONObject(decryptData);

			String status = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			System.out.println("RESPONSE :  " + decryptData);

			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}

			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSource(source);
			request.setSourceType(sourceType);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);
			request.setPrice(merchantPriceModel.getIdPrice());
			request.setUser(userModel);
			request.setVerificationModel(serviceModel);

			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(sourceType);
			response.setSource(source);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);

			if (status.equals("success")) {

				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");

				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");

				String email = validatedJson.getString("email");
				// String pan = validatedJson.getString("pan_number");
				String address = validatedJson.getString("permanent_address");

				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setEmail(email);
				request.setResponseDateAndTime(responseTimeStamp);
				reqRepository.save(request);

				response.setEmail(email);
				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setRequest(request);
				response.setAddress(address);
				// response.setPanNumber(pan);
				respRepository.save(response);

			} else {

				// {"status":"failed","reference_id":"9511134592","response_time_stamp":"2023-08-30T09:50:37","message":"Backend
				// Timed Out. Try Again.","error_code":"kyc_028"}

				// String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				request.setResponseDateAndTime(responseTimeStamp);

				// request.setError(error);
				request.setErrorCode(errorCode);
				reqRepository.save(request);

				response.setResponse(decryptData);
				// response.setError(error);
				response.setErrorCode(errorCode);
				response.setResponseDateAndTime(responseTimeStamp);
				response.setRequest(request);

				respRepository.save(response);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	@Override
	public ResponseStructure msmeVerification(RequestModel dto, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(userDecryption);

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 7) {

						return msmeNumberCheck(userJosn, userModel, merchantPriceModel, dto, serviceModel,
								userSecretKey, vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 7) {

						return msmeNumberCheck(userJosn, userModel, merchantPriceModel, dto, serviceModel,
								userSecretKey, vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure msmeNumberCheck(JSONObject userJosn, EntityModel userModel,
			MerchantPriceModel merchantPriceModel, RequestModel dto, VendorVerificationModel serviceModel,
			String userSecretKey, VendorModel vendorModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");

			// Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);

			// Request request = new Request();
			List<Response> sourceList = respRepository.findBySource(source);
			// Response model = new Response();
			if (!sourceList.isEmpty()) {

				Response model = sourceCheck(sourceList);
				boolean isMatch = false;
				if (model != null) {
					isMatch = true;
				}

//				boolean isMatch = false;
//				for (Response response : sourceList) {
//					if (response.getStatus().equals("success")) {
//						model = response;
//						isMatch = true;
//						break;
//					}
//				}
				if (isMatch) {

					return setRequest(model, dto, merchantPriceModel, userModel, serviceModel);

//					request.setReferenceId(model.getReferenceId());
//					request.setMessage(model.getMessage());
//					request.setTransactionId(model.getTransactionId());
//					request.setSource(model.getSource());
//					request.setSourceType(model.getSourceType());
//					request.setRequestBy(dto.getRequestBy());
//					request.setRequestDateAndTime(new Date());
//					request.setPrice(merchantPriceModel.getIdPrice());
//					
//					request.setEmail(model.getEmail());
//					request.setCompanyName(model.getBusinessName());
//
//					Date date = new Date();
//					SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					String stringDate = simple.format(date);
//					request.setResponseDateAndTime(stringDate);
//					request.setStatus(model.getStatus());
//					request.setUser(userModel);
//					request.setVerificationModel(serviceModel);
//
//					reqRepository.save(request);

//					JSONObject object = new JSONObject();
//					object.put("status", model.getStatus());
//					object.put("encrypted_response", model.getResponse());
//
//					String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
//					System.out.println("RETURN RESPONSE : " + returnResponse);
//
//					// Prepaid Amount Reduction
//					if (userModel.getPaymentMethod().getPaymentId() == 2) {
//						double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
//						double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();
//
//						userModel.setRemainingAmount(remainingAmount);
//						userModel.setConsumedAmount(consumedAmount);
//						userModel.setPaymentStatus("No Dues");
//					}
//
//					// Response Count
//					int responseCount = userModel.getResponseCount();
//					userModel.setResponseCount(responseCount + 1);
//					userRepository.save(userModel);
//
////					Map<String, Object> mapNew = new HashMap<>();
////					mapNew.put("Return Response", returnResponse);
////					mapNew.put("Response", model);
////					mapNew.put("Request", request);
//
//					structure.setData(returnResponse);
//					structure.setStatusCode(HttpStatus.OK.value());
//					structure.setFlag(1);
//					structure.setMessage(AppConstants.SUCCESS);
//
//					return structure;
				} else {
					return msmeVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
							userSecretKey, vendorModel, merchantPriceModel);

				}

			} else {
				return msmeVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
						userSecretKey, vendorModel, merchantPriceModel);
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure msmeVerificationProcess(RequestModel dto, EntityModel userModel, String reference_Id,
			VendorVerificationModel serviceModel, String source, String sourceType, String userSecretKey,
			VendorModel vendorModel, MerchantPriceModel merchantPriceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {

			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			Request request = new Request();
			Date reqDate = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// set request details

			inputParams.put("reference_id", reference_Id);
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

			// VendorReq
			int reqCount = vendorModel.getVendorRequest() + 1;
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			// VendorResponse
			int respCount = vendorModel.getVendorResponse() + 1;
			vendorModel.setVendorResponse(respCount);

			JSONObject jsonObject = new JSONObject(data);

			Response response = new Response();

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			//
			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			//

			String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

			System.out.println("RESPONSE :  " + decryptData);

			JSONObject decryptJson = new JSONObject(decryptData);

			String status = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}

			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSourceType(sourceType);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);
			request.setPrice(merchantPriceModel.getIdPrice());
			request.setUser(userModel);
			request.setVerificationModel(serviceModel);

			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(sourceType);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);

			if (status.equals("success")) {

				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");

				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");

				String sourceId = validatedJson.getString("uan");
				JSONObject mainDetails = validatedJson.getJSONObject("main_details");

				String nameOfEnterprise = mainDetails.getString("name_of_enterprise");
				String email = mainDetails.getString("email");

				request.setSource(sourceId);
				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setCompanyName(nameOfEnterprise);
				request.setEmail(email);

				reqRepository.save(request);

				response.setSource(sourceId);
				response.setBusinessName(nameOfEnterprise);
				response.setEmail(email);
				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setRequest(request);
				respRepository.save(response);

			} else {

				String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				request.setError(error);
				request.setErrorCode(errorCode);
				reqRepository.save(request);

				response.setResponse(decryptData);
				response.setError(error);
				response.setErrorCode(errorCode);
				response.setRequest(request);

				respRepository.save(response);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	@Override
	public ResponseStructure rcVerification(RequestModel dto, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(userDecryption);

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 8) {

						return rcNumberCheck(userJosn, userModel, merchantPriceModel, dto, serviceModel, userSecretKey,
								vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 8) {

						return rcNumberCheck(userJosn, userModel, merchantPriceModel, dto, serviceModel, userSecretKey,
								vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	private ResponseStructure rcNumberCheck(JSONObject userJosn, EntityModel userModel,
			MerchantPriceModel merchantPriceModel, RequestModel dto, VendorVerificationModel serviceModel,
			String userSecretKey, VendorModel vendorModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");

			// Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);

			// Request request = new Request();
			List<Response> sourceList = respRepository.findBySource(source);
			// Response model = new Response();
			if (!sourceList.isEmpty()) {

				Response model = sourceCheck(sourceList);
				boolean isMatch = false;
				if (model != null) {
					isMatch = true;
				}

//				boolean isMatch = false;
//				for (Response response : sourceList) {
//					if (response.getStatus().equals("success")) {
//						model = response;
//						isMatch = true;
//						break;
//					}
//				}
				if (isMatch) {

					return setRequest(model, dto, merchantPriceModel, userModel, serviceModel);

//					request.setReferenceId(model.getReferenceId());
//					request.setMessage(model.getMessage());
//					request.setTransactionId(model.getTransactionId());
//					request.setSource(model.getSource());
//					request.setSourceType(model.getSourceType());
//					request.setRequestBy(dto.getRequestBy());
//					request.setRequestDateAndTime(new Date());
//					
//					request.setFullName(model.getFullName());
//
//					Date date = new Date();
//					SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					String stringDate = simple.format(date);
//					request.setResponseDateAndTime(stringDate);
//					request.setStatus(model.getStatus());
//					request.setUser(userModel);
//					request.setVerificationModel(serviceModel);
//					request.setPrice(merchantPriceModel.getIdPrice());
//
//					reqRepository.save(request);

//					JSONObject object = new JSONObject();
//					object.put("status", model.getStatus());
//					object.put("encrypted_response", model.getResponse());
//
//					String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
//					System.out.println("RETURN RESPONSE : " + returnResponse);
//
//					// Prepaid Amount Reduction
//					if (userModel.getPaymentMethod().getPaymentId() == 2) {
//						double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
//						double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();
//
//						userModel.setRemainingAmount(remainingAmount);
//						userModel.setConsumedAmount(consumedAmount);
//						userModel.setPaymentStatus("No Dues");
//					}
//
//					// Response Count
//					int responseCount = userModel.getResponseCount();
//					userModel.setResponseCount(responseCount + 1);
//					userRepository.save(userModel);
//
////					Map<String, Object> mapNew = new HashMap<>();
////					mapNew.put("Return Response", returnResponse);
////					mapNew.put("Response", model);
////					mapNew.put("Request", request);
//
//					structure.setData(returnResponse);
//					structure.setStatusCode(HttpStatus.OK.value());
//					structure.setFlag(1);
//					structure.setMessage(AppConstants.SUCCESS);
				} else {
					return rcVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
							userSecretKey, vendorModel, merchantPriceModel);

				}

			} else {
				return rcVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
						userSecretKey, vendorModel, merchantPriceModel);
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure rcVerificationProcess(RequestModel dto, EntityModel userModel, String reference_Id,
			VendorVerificationModel serviceModel, String source, String sourceType, String userSecretKey,
			VendorModel vendorModel, MerchantPriceModel merchantPriceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {

			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			Request request = new Request();
			Date reqDate = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// set request details

			inputParams.put("reference_id", reference_Id);
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

			// VendorReq
			int reqCount = vendorModel.getVendorRequest() + 1;
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			// VendorResponse
			int respCount = vendorModel.getVendorResponse() + 1;
			vendorModel.setVendorResponse(respCount);

			JSONObject jsonObject = new JSONObject(data);

			Response response = new Response();

			System.out.println("JSON OBJ : " + jsonObject);

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");

			System.out.println("encrypted_response: " + encryptedResponse);

			String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

			JSONObject decryptJson = new JSONObject(decryptData);

			String status = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}

			request.setSource(source);
			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSourceType(sourceType);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);
			request.setPrice(merchantPriceModel.getIdPrice());
			request.setUser(userModel);
			request.setVerificationModel(serviceModel);

			response.setSource(source);
			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(sourceType);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);

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
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				request.setError(error);
				request.setErrorCode(errorCode);
				reqRepository.save(request);

				response.setResponse(decryptData);
				response.setError(error);
				response.setErrorCode(errorCode);
				response.setRequest(request);

				respRepository.save(response);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	@Override
	public ResponseStructure drivingLicenceId(RequestModel dto, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(userDecryption);

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 4) {

						return drivingLicenceNumberCheck(userJosn, userModel, merchantPriceModel, dto, serviceModel,
								userSecretKey, vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 4) {

						return drivingLicenceNumberCheck(userJosn, userModel, merchantPriceModel, dto, serviceModel,
								userSecretKey, vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	private ResponseStructure drivingLicenceNumberCheck(JSONObject userJosn, EntityModel userModel,
			MerchantPriceModel merchantPriceModel, RequestModel dto, VendorVerificationModel serviceModel,
			String userSecretKey, VendorModel vendorModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");
			String dob = userJosn.getString("dob");

			// Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);

			// Request request = new Request();
			List<Response> sourceList = respRepository.findBySource(source);
			// Response model = new Response();
			if (!sourceList.isEmpty()) {

				Response model = sourceCheck(sourceList);
				boolean isMatch = false;
				if (model != null) {
					isMatch = true;
				}

//				boolean isMatch = false;
//				for (Response response : sourceList) {
//					if (response.getStatus().equals("success")) {
//						model = response;
//						isMatch = true;
//						break;
//					}
//				}
				if (isMatch) {

					return setRequest(model, dto, merchantPriceModel, userModel, serviceModel);

//					request.setReferenceId(model.getReferenceId());
//					request.setMessage(model.getMessage());
//					request.setTransactionId(model.getTransactionId());
//					request.setSource(model.getSource());
//					request.setSourceType(model.getSourceType());
//					request.setRequestBy(dto.getRequestBy());
//					request.setRequestDateAndTime(new Date());
//					
//					request.setFullName(model.getFullName());
//					request.setDob(model.getDob());
//					request.setState(model.getState());
//					request.setPrice(merchantPriceModel.getIdPrice());
//
//					Date date = new Date();
//					SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					String stringDate = simple.format(date);
//					request.setResponseDateAndTime(stringDate);
//					request.setStatus(model.getStatus());
//					request.setUser(userModel);
//					request.setVerificationModel(serviceModel);
//
//					reqRepository.save(request);

//					JSONObject object = new JSONObject();
//					object.put("status", model.getStatus());
//					object.put("encrypted_response", model.getResponse());
//
//					String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
//					System.out.println("RETURN RESPONSE : " + returnResponse);
//
//					// Prepaid Amount Reduction
//					if (userModel.getPaymentMethod().getPaymentId() == 2) {
//						double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
//						double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();
//
//						userModel.setRemainingAmount(remainingAmount);
//						userModel.setConsumedAmount(consumedAmount);
//						userModel.setPaymentStatus("No Dues");
//					}
//
//					// Response Count
//					int responseCount = userModel.getResponseCount();
//					userModel.setResponseCount(responseCount + 1);
//					userRepository.save(userModel);
//
////					Map<String, Object> mapNew = new HashMap<>();
////					mapNew.put("Return Response", returnResponse);
////					mapNew.put("Response", model);
////					mapNew.put("Request", request);
//
//					structure.setData(returnResponse);
//					structure.setStatusCode(HttpStatus.OK.value());
//					structure.setFlag(1);
//					structure.setMessage(AppConstants.SUCCESS);

				} else {
					return drivingLicenceIdVerificationProcess(dto, userModel, reference_id, serviceModel, source,
							source_type, userSecretKey, dob, vendorModel, merchantPriceModel);

				}

			} else {
				return drivingLicenceIdVerificationProcess(dto, userModel, reference_id, serviceModel, source,
						source_type, userSecretKey, dob, vendorModel, merchantPriceModel);
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure drivingLicenceIdVerificationProcess(RequestModel dto, EntityModel userModel,
			String reference_Id, VendorVerificationModel serviceModel, String source, String sourceType,
			String userSecretKey, String dob, VendorModel vendorModel, MerchantPriceModel merchantPriceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {

			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			Request request = new Request();
			Date reqDate = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// set request details

			inputParams.put("reference_id", reference_Id);
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

			// VendorReq
			int reqCount = vendorModel.getVendorRequest() + 1;
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			// VendorResponse
			int respCount = vendorModel.getVendorResponse() + 1;
			vendorModel.setVendorResponse(respCount);

			JSONObject jsonObject = new JSONObject(data);

			Response response = new Response();

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			//
			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			//

			String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

			System.out.println("RESPONSE :  " + decryptData);

			JSONObject decryptJson = new JSONObject(decryptData);

			String status = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}
			request.setPrice(merchantPriceModel.getIdPrice());
			request.setSource(source);
			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSourceType(sourceType);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);
			request.setUser(userModel);
			request.setVerificationModel(serviceModel);

			response.setSource(source);
			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(sourceType);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);

			if (status.equals("success")) {

				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");

				JSONObject resultJson = decryptJson.getJSONObject("result");

				boolean validDl = resultJson.getBoolean("valid_dl");

				if (validDl) {
					JSONObject validatedJson = resultJson.getJSONObject("validated_data");

					String fullName = validatedJson.getString("name");
					String dateOfBirth = validatedJson.getString("dob");
					String address = validatedJson.getString("permanent_address");
					String state = validatedJson.getString("state");

					request.setFullName(fullName);
					request.setDob(dateOfBirth);
					request.setState(state);

					response.setFullName(fullName);
					response.setDob(dateOfBirth);
					response.setState(state);
					response.setAddress(address);
				}

				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);

				reqRepository.save(request);

				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setRequest(request);
				respRepository.save(response);

			} else {

				// {"status":"failed","reference_id":"2984265558","response_time_stamp":"2023-08-30T10:42:39","message":"Internal
				// Server Error Occurred","error_code":"kyc_028"}

				// String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				// request.setError(error);
				request.setErrorCode(errorCode);
				reqRepository.save(request);

				response.setResponse(decryptData);
				// response.setError(error);
				response.setErrorCode(errorCode);
				response.setResponseDateAndTime(responseTimeStamp);
				response.setRequest(request);

				respRepository.save(response);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	@Override
	public ResponseStructure passportId(RequestModel dto, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(userDecryption);

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 3) {

						return passportNumberCheck(userJosn, userModel, merchantPriceModel, dto, serviceModel,
								userSecretKey, vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 3) {

						return passportNumberCheck(userJosn, userModel, merchantPriceModel, dto, serviceModel,
								userSecretKey, vendorModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure passportNumberCheck(JSONObject userJosn, EntityModel userModel,
			MerchantPriceModel merchantPriceModel, RequestModel dto, VendorVerificationModel serviceModel,
			String userSecretKey, VendorModel vendorModel) {
		ResponseStructure structure = new ResponseStructure();
		try {
			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");
			String dob = userJosn.getString("dob");

			// Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);

			// Request request = new Request();
			List<Response> sourceList = respRepository.findBySource(source);
			// Response model = new Response();
			if (!sourceList.isEmpty()) {

				Response model = sourceCheck(sourceList);
				boolean isMatch = false;
				if (model != null) {
					isMatch = true;
				}

//				boolean isMatch = false;
//				for (Response response : sourceList) {
//					if (response.getStatus().equals("success")) {
//						model = response;
//						isMatch = true;
//						break;
//					}
//				}
				if (isMatch) {

					return setRequest(model, dto, merchantPriceModel, userModel, serviceModel);

//					request.setReferenceId(model.getReferenceId());
//					request.setMessage(model.getMessage());
//					request.setTransactionId(model.getTransactionId());
//					request.setSource(model.getSource());
//					request.setSourceType(model.getSourceType());
//					request.setRequestBy(dto.getRequestBy());
//					request.setRequestDateAndTime(new Date());
//					
//					request.setFullName(model.getFullName());
//					request.setDob(model.getDob());
//
//					Date date = new Date();
//					SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					String stringDate = simple.format(date);
//					request.setResponseDateAndTime(stringDate);
//					request.setStatus(model.getStatus());
//					request.setUser(userModel);
//					request.setVerificationModel(serviceModel);
//					request.setPrice(merchantPriceModel.getIdPrice());
//
//					reqRepository.save(request);

//					JSONObject object = new JSONObject();
//					object.put("status", model.getStatus());
//					object.put("encrypted_response", model.getResponse());
//
//					String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
//					System.out.println("RETURN RESPONSE : " + returnResponse);
//
//					// Prepaid Amount Reduction
//					if (userModel.getPaymentMethod().getPaymentId() == 2) {
//						double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
//						double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();
//
//						userModel.setRemainingAmount(remainingAmount);
//						userModel.setConsumedAmount(consumedAmount);
//						userModel.setPaymentStatus("No Dues");
//					}
//
//					// Response Count
//					int responseCount = userModel.getResponseCount();
//					userModel.setResponseCount(responseCount + 1);
//					userRepository.save(userModel);
//
////					Map<String, Object> mapNew = new HashMap<>();
////					mapNew.put("Return Response", returnResponse);
////					mapNew.put("Response", model);
////					mapNew.put("Request", request);
//
//					structure.setData(returnResponse);
//					structure.setStatusCode(HttpStatus.OK.value());
//					structure.setFlag(1);
//					structure.setMessage(AppConstants.SUCCESS);
//
//					return structure;
				} else {
					return passportIdVerificationProcess(dto, userModel, reference_id, serviceModel, source,
							source_type, userSecretKey, dob, vendorModel, merchantPriceModel);

				}

			} else {
				return passportIdVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
						userSecretKey, dob, vendorModel, merchantPriceModel);
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return structure;
		}
	}

	private ResponseStructure passportIdVerificationProcess(RequestModel dto, EntityModel userModel,
			String reference_Id, VendorVerificationModel serviceModel, String source, String sourceType,
			String userSecretKey, String dob, VendorModel vendorModel, MerchantPriceModel merchantPriceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {

			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			Request request = new Request();
			Date reqDate = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// set request details

			inputParams.put("reference_id", reference_Id);
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

			// VendorReq
			int reqCount = vendorModel.getVendorRequest() + 1;
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			// VendorResponse
			int respCount = vendorModel.getVendorResponse() + 1;
			vendorModel.setVendorResponse(respCount);

			JSONObject jsonObject = new JSONObject(data);

			Response response = new Response();

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");

			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);

			String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

			JSONObject decryptJson = new JSONObject(decryptData);

			System.out.println("RESPONSE : " + decryptData);

			String status = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}

			request.setSource(source);
			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSourceType(sourceType);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);
			request.setPrice(merchantPriceModel.getIdPrice());
			request.setUser(userModel);
			request.setVerificationModel(serviceModel);

			response.setSource(source);
			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(sourceType);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);

			if (status.equals("success")) {

				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");

				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");
				JSONObject extractedData = resultJson.getJSONObject("extracted_data");

				String dateOfBirth = extractedData.getString("dob");
				String address = extractedData.getString("address");
				String fullName = validatedJson.getString("full_name");

				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setFullName(fullName);
				request.setDob(dateOfBirth);
				reqRepository.save(request);

				response.setFullName(fullName);
				response.setDob(dateOfBirth);
				response.setAddress(address);
				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setRequest(request);
				respRepository.save(response);

			} else {

				String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				request.setError(error);
				request.setErrorCode(errorCode);
				reqRepository.save(request);

				response.setResponse(decryptData);
				response.setError(error);
				response.setErrorCode(errorCode);
				response.setResponseDateAndTime(responseTimeStamp);
				response.setRequest(request);

				respRepository.save(response);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	@Override
	public ResponseStructure passportImage(RequestModel dto, HttpServletRequest servletRequest) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String UserDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(UserDecryption);

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getImagePrice()) {

					if (dto.getVendorVerificationId() == 3) {

						return passportImageVerificationProcess(userModel, merchantPriceModel, serviceModel,
								vendorModel, userSecretKey, userJosn, dto);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setMessage("PLEASE RECHARGE");
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 3) {

						return passportImageVerificationProcess(userModel, merchantPriceModel, serviceModel,
								vendorModel, userSecretKey, userJosn, dto);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {

					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;

	}

	private ResponseStructure passportImageVerificationProcess(EntityModel userModel,
			MerchantPriceModel merchantPriceModel, VendorVerificationModel serviceModel, VendorModel vendorModel,
			String userSecretKey, JSONObject userJosn, RequestModel dto) {
		ResponseStructure structure = new ResponseStructure();
		try {

			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			String reference_id = userJosn.getString("reference_id");
			JSONArray source = userJosn.getJSONArray("source");
			String source_type = userJosn.getString("source_type");
			// boolean filingStatus=userJosn.getBoolean("filing_status_get");

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

			// VendorReq
			int reqCount = vendorModel.getVendorRequest() + 1;
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			// VendorResponse
			int respCount = vendorModel.getVendorResponse() + 1;
			vendorModel.setVendorResponse(respCount);

			// Convert the string to a JSONObject
			JSONObject jsonObject = new JSONObject(data);

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

			System.out.println("RESPONSE : " + decryptData);

			String status = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}

			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSourceType(source_type);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);
			request.setPrice(merchantPriceModel.getImagePrice());
			request.setUser(userModel);
			request.setVerificationModel(serviceModel);

			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(source_type);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);

			if (status.equals("success")) {

				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");

				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");
				JSONObject extractedData = resultJson.getJSONObject("extracted_data");

				String sourceId = validatedJson.getString("passport_number");
				String dateOfBirth = extractedData.getString("dob");
				String address = extractedData.getString("address");
				String fullName = validatedJson.getString("full_name");

				request.setSource(sourceId);
				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setFullName(fullName);
				request.setDob(dateOfBirth);
				reqRepository.save(request);

				response.setSource(sourceId);
				response.setFullName(fullName);
				response.setDob(dateOfBirth);
				response.setAddress(address);
				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setRequest(request);
				respRepository.save(response);

			} else {

				// String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				// request.setError(error);
				request.setErrorCode(errorCode);
				reqRepository.save(request);

				response.setResponse(decryptData);
				// response.setError(error);
				response.setErrorCode(errorCode);
				response.setRequest(request);

				respRepository.save(response);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure drivingLicenceImage(RequestModel dto, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String UserDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(UserDecryption);

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getImagePrice()) {

					if (dto.getVendorVerificationId() == 4) {

						return drivingLicenceImageVerificationProcess(userModel, serviceModel, vendorModel,
								merchantPriceModel, userSecretKey, userJosn, dto);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setMessage("PLEASE RECHARGE");
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 4) {

						return drivingLicenceImageVerificationProcess(userModel, serviceModel, vendorModel,
								merchantPriceModel, userSecretKey, userJosn, dto);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {

					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure drivingLicenceImageVerificationProcess(EntityModel userModel,
			VendorVerificationModel serviceModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			String userSecretKey, JSONObject userJosn, RequestModel dto) {
		ResponseStructure structure = new ResponseStructure();
		try {

			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			String reference_id = userJosn.getString("reference_id");
			JSONArray source = userJosn.getJSONArray("source");
			String source_type = userJosn.getString("source_type");
			// boolean filingStatus=userJosn.getBoolean("filing_status_get");

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

			// VendorReq
			int reqCount = vendorModel.getVendorRequest() + 1;
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			// VendorResponse
			int respCount = vendorModel.getVendorResponse() + 1;
			vendorModel.setVendorResponse(respCount);

			// Convert the string to a JSONObject
			JSONObject jsonObject = new JSONObject(data);

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

			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}

			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSourceType(source_type);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);
			request.setPrice(merchantPriceModel.getImagePrice());
			request.setUser(userModel);
			request.setVerificationModel(serviceModel);

			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(source_type);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);

			System.out.println("RESONSE : " + decryptData);

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
				reqRepository.save(request);

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
				respRepository.save(response);

			} else {

				// String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				// request.setError(error);
				request.setErrorCode(errorCode);
				reqRepository.save(request);

				response.setResponse(decryptData);
				// response.setError(error);
				response.setErrorCode(errorCode);
				response.setRequest(request);

				respRepository.save(response);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	// @Scheduled(fixedRate = 3000000) // 10 minutes in milliseconds
//	public void printMessage() {
//
//		try {
//
//
//			List<PostpaidPayment> all = postpaidRepository.findAll();
//			List<PostpaidPayment> flagFalseUser = new ArrayList<>();
//			for (PostpaidPayment postpaidPayment : all) {
//				if (!postpaidPayment.isPaymentFlag()) {
//					flagFalseUser.add(postpaidPayment);
//				}
//			}
//
//			for (PostpaidPayment postpaidPayment : flagFalseUser) {
//
//				if (LocalDate.now().isAfter(postpaidPayment.getEndDate())) {
//
//					EntityModel entity = userRepository.findByUserId(postpaidPayment.getEntityModel().getUserId());
//
//					try {
//
//						List<Request> reqList = requestRepository.findByUser(entity);
//						List<Request> time = new ArrayList<>();
//
//						for (Request request : reqList) {
//							DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//							String startDate = new StringBuilder()
//									.append(postpaidPayment.getStartDate().format(formatter)).append(" 00:00:00")
//									.toString();
//							String endDate = new StringBuilder().append(postpaidPayment.getEndDate().format(formatter))
//									.append(" 00:00:00").toString();
//
//							DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
//							DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//							LocalDateTime dateTime1 = LocalDateTime.parse(startDate, inputFormatter);
//							LocalDateTime dateTime2 = LocalDateTime.parse(endDate, inputFormatter);
//							String outputDate1 = dateTime1.format(outputFormatter);
//							String outputDate2 = dateTime2.format(outputFormatter);
//
//							Date date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(outputDate1);
//							Date date2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(outputDate2);
//
//							time = requestRepository.findByRequestDateAndTime(date1, date2);
//						}
//						System.out.println(" Time : " + time.size());
//						// int n=0;
//						double price = 0;
//						int totalHit = 0;
//						System.out.println("Entity Id : " + entity.getUserId());
//						for (Request req : time) {
//							System.out.println("Request User :" + req.getUser().getUserId());
//
//							System.out.println("Looop In");
//							if (req.getUser().getUserId() == entity.getUserId()) {
//								System.out.println("Req Price : " + req.getPrice());
//								price = price + req.getPrice();
//								totalHit = totalHit + 1;
//							}
//						}
//
//						LocalDate extendDate = entity.getEndDate().plusDays(entity.getDuration());
//						// LocalDate extendedGraceDate=extendDate.plusDays(entity.getGracePeriod());
//						System.out.println("Price : " + price);
//						postpaidPayment.setTotalAmount(price);
//						
//						Path con=	Paths.get(context.getRealPath("/WEB-INF/"));
//						String receipt = new InvoiceGeneratePdf().writeDataToPDF(con,entity,postpaidPayment,price,totalHit);
//						
//						postpaidPayment.setInvoice(receipt);
//						
//						postpaidRepository.save(postpaidPayment);
//
//						
//						
//						if (entity.isPostpaidFlag()) {
//							LocalDate startDate = entity.getEndDate().plusDays(1);
//							entity.setStartDate(startDate);
//							entity.setEndDate(extendDate);
//							// entity.setGraceDate(extendedGraceDate);
//							entity.setPostpaidFlag(false);
//						}
//
//						userRepository.save(entity);
//
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//
//				}
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

//	@Override
//	public ResponseStructure jweDemoController(RequestModel model) {
//		ResponseStructure structure = new ResponseStructure();
//		try {
//			// String encryptImage=PasswordUtils.demoImageEncryption(model);
//
//			JSONObject inputParams = new JSONObject();
//			String referenceNumber = FileUtils.getRandomOTPnumber(10);
//
//			inputParams.put("reference_id", referenceNumber);
//			// inputParams.put("source_type", "id");
//			inputParams.put("source", model.getSource());
//
//			// EntityModel user = userRepository.findByUserId(model.getUserId());
//			// String key = user.getSecretKey();
//			// System.out.println("Secret key : " + key);
//
//			String jsonString = inputParams.toString();
//
//			String json = FileEncryptDecryptUtil.jweEncryption(model);
//
//			System.out.println("json : " + json);
//
//			// String decryption1 = PasswordUtils.demoDecrypt(json, key);
//
//			// System.out.println("json Decrypt : " + decryption1);
//			// JSONObject jsonParams=new JSONObject();
//			// jsonParams.put("encrypted_data", json);
//			// jsonParams.put("user_id", model.getUserId());
//			// String jsonEncrypt=PasswordUtils.demoEncryption(jsonParams,key);
//			// System.out.println("jsonEncrypt : "+jsonEncrypt);
//			//
//			//
//			// String decryption=PasswordUtils.demoDecrypt(jsonEncrypt, key);
//			// System.out.println(" Decryption Final : "+decryption);
//
//			structure.setData(json);
//			structure.setMessage(AppConstants.SUCCESS);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			structure.setData(null);
//			structure.setMessage(AppConstants.TECHNICAL_ERROR);
//			structure.setFlag(4);
//			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
//		}
//		return structure;
//	}

	@Override
	public ResponseStructure toManuallyChangeStatus(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<Response> sourceList = respRepository.findBySource(model.getSource());

			List<Response> failedSourceList = new ArrayList<>();

			if (!sourceList.isEmpty()) {

				for (Response response : sourceList) {
					response.setStatus("failed");
					respRepository.save(response);

					failedSourceList.add(response);
				}

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(failedSourceList);
				structure.setMessage(AppConstants.SUCCESS);

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND + " FOR GIVEN SOURCE");

			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setFlag(4);
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure demoGstController(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			// String encryptImage=PasswordUtils.demoImageEncryption(model);

			JSONObject inputParams = new JSONObject();
			String referenceNumber = FileUtils.getRandomOTPnumber(10);

			inputParams.put("source", model.getSource());
			inputParams.put("filing_status_get", model.isFilingStatus()); // FOR GST

			EntityModel user = userRepository.findByUserId(model.getUserId());
			String key = user.getSecretKey();
			System.out.println("Secret key : " + key);
			String json = PasswordUtils.demoEncryption(inputParams, key);
			System.out.println("json : " + json);
			String decryption1 = PasswordUtils.demoDecrypt(json, key);

			System.out.println("json Decrypt : " + decryption1);

			structure.setData(json);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			e.printStackTrace();
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewMerchantRequestByNumberOfDays(int noOfDays) {

		ResponseStructure structure = new ResponseStructure();

		try {

			String toDate = LocalDate.now().toString() + " 23:59:59.999999";
			String fromDate = LocalDate.now().minusDays(noOfDays).toString() + " 00:00:00.000000";

			List<Request> reqList = reqRepository.getByRequestDateAndTime(fromDate, toDate);
			int count = reqList.size();

			if (!reqList.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Merchant Request details Between " + LocalDate.now().minusDays(noOfDays) + " and "
						+ LocalDate.now());
				structure.setData(reqList);
				structure.setFlag(1);
				structure.setCount(count);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Merchant Request found Between " + LocalDate.now().minusDays(noOfDays)
						+ " and " + LocalDate.now());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(count);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure viewVendorRequestByNumberOfDays(int noOfDays) {
		ResponseStructure structure = new ResponseStructure();

		try {

			String toDate = LocalDate.now().toString() + " 23:59:59.999999";
			String fromDate = LocalDate.now().minusDays(noOfDays).toString() + " 00:00:00.000000";

			List<Response> reqList = respRepository.getByRequestDateAndTime(fromDate, toDate);
			int count = reqList.size();

			if (!reqList.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Vendor Request details Between " + LocalDate.now().minusDays(noOfDays) + " and "
						+ LocalDate.now());
				structure.setData(reqList);
				structure.setFlag(1);
				structure.setCount(count);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Vendor Request found Between " + LocalDate.now().minusDays(noOfDays) + " and "
						+ LocalDate.now());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(count);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure viewAllRequestResponseReplica() {

		ResponseStructure structure = new ResponseStructure();

		try {

			List<RequestResponseReplica> replicaList = replicaRepository.findAll();

			if (!replicaList.isEmpty()) {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(replicaList);
				structure.setFlag(1);

			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure viewByIdRequestResponseReplica(int replicaId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<RequestResponseReplica> optional = replicaRepository.findById(replicaId);

			if (optional.isPresent()) {

				RequestResponseReplica replica = optional.get();

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(replica);
				structure.setFlag(1);

			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure viewByEntityRequestResponseReplicaCombined(int userId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel entity = userRepository.findByUserId(userId);

			if (entity != null) {

//				List<Request> liveRequestList = reqRepository.findByUserAndFreeHit(entity, true);
				List<RequestResponseReplica> dummyRequestList = replicaRepository.findByUser(entity);

//				System.err.println("LIVE SIZE : " + liveRequestList.size());
				System.err.println("DUMMY SIZE : " + dummyRequestList.size());

				if (!dummyRequestList.isEmpty()) {//!liveRequestList.isEmpty() || 

					List<Object> allTogether = new ArrayList<>();

//					allTogether.addAll(liveRequestList);
					allTogether.addAll(dummyRequestList);

					System.err.println("ALL TOG SIZE : " + allTogether.size());

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(allTogether);
					structure.setFlag(1);

				} else {

					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	@Override
	public ResponseStructure reqByOneEntity() {
		ResponseStructure structure = new ResponseStructure();

		try {

			Set<EntityModel> entitySet = new LinkedHashSet<>();
			
			List<Request> allList = reqRepository.findAll();
			
			List<Request> oneEntity = new ArrayList<>();
			
			if(!allList.isEmpty()) {
				
				for (Request request : allList) {
					
					if(!entitySet.contains(request.getUser())) {
						
						oneEntity.add(request);
						entitySet.add(request.getUser());
					}
				}
				
				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(oneEntity);
				structure.setCount(oneEntity.size());
				structure.setFlag(1);
				
			}else {
				
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	@Override
	public ResponseStructure byEntityAndVerificationType(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {
			
			Optional<EntityModel> optEntity = userRepository.findById(model.getUserId());
			
			if(optEntity.isPresent()) {
				
				if(model.getVendorVerificationId()==0) {
					
					List<Request> allList = reqRepository.findByUserAndFreeHit(optEntity.get(),false);
					
					if(!allList.isEmpty()) {
						
						structure.setMessage(AppConstants.SUCCESS);
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(allList);
						structure.setCount(allList.size());
						structure.setFlag(1);
						
					}else {
						
						structure.setMessage(AppConstants.NO_DATA_FOUND);
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(null);
						structure.setFlag(5);
					}
				}else {
					
					Optional<VendorVerificationModel> optVer = verificationRepository.findById(model.getVendorVerificationId());
					
					if(optVer.isPresent()) {
						
						List<Request> allList = reqRepository.findByUserAndVerificationModelAndFreeHit(optEntity.get(),optVer.get(),false);
						
						structure.setMessage(AppConstants.SUCCESS);
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(allList);
						structure.setCount(allList.size());
						structure.setFlag(1);
						
					}else {
						
						structure.setMessage(AppConstants.NO_DATA_FOUND);
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(null);
						structure.setFlag(4);
					}
				}
			}else {
				
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(6);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}

		return structure;
	}

	@Override
	public ResponseStructure reqByOneVendor() {
		ResponseStructure structure = new ResponseStructure();

		try {

			Set<VendorModel> vendorSet = new LinkedHashSet<>();
			
			List<Response> allList = respRepository.findAll();
			
			List<Response> oneEntity = new ArrayList<>();
			
			if(!allList.isEmpty()) {
				
				for (Response resp : allList) {
					
					if(!vendorSet.contains(resp.getVendorModel())) {
						
						oneEntity.add(resp);
						vendorSet.add(resp.getVendorModel());
					}
				}
				
				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(oneEntity);
				structure.setCount(oneEntity.size());
				structure.setFlag(1);
				
			}else {
				
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	@Override
	public ResponseStructure byVendorAndVerificationType(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {
			
			Optional<VendorModel> optEntity = vendorRepository.findById(model.getVendorId());
			
			if(optEntity.isPresent()) {
				
				if(model.getVendorVerificationId()==0) {
					
					List<Response> allList = respRepository.findByVendorModel(optEntity.get());
					
					if(!allList.isEmpty()) {
						
						structure.setMessage(AppConstants.SUCCESS);
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(allList);
						structure.setCount(allList.size());
						structure.setFlag(1);
						
					}else {
						
						structure.setMessage(AppConstants.NO_DATA_FOUND);
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(null);
						structure.setFlag(5);
					}
				}else {
					
					Optional<VendorVerificationModel> optVer = verificationRepository.findById(model.getVendorVerificationId());
					
					if(optVer.isPresent()) {
						
						List<Response> allList = respRepository.findByVendorModelAndVerificationModel(optEntity.get(),optVer.get());
						
						structure.setMessage(AppConstants.SUCCESS);
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(allList);
						structure.setCount(allList.size());
						structure.setFlag(1);
						
					}else {
						
						structure.setMessage(AppConstants.NO_DATA_FOUND);
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(null);
						structure.setFlag(4);
					}
				}
			}else {
				
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(6);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}

		return structure;
	}

}
