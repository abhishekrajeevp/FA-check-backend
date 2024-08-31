package com.bp.middleware.sprintverify;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

import jakarta.servlet.http.HttpServletRequest;

@Service
public class SprintVerifyServiceImplementation implements SprintVerifyService {

	
	private final String VERIFICATION_TYPE = AppConstants.AADHAR_VERIFY;
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
	private VendorVerificationRepository verificationRepository;
	@Autowired
	private VendorPriceRepository vendorPriceRepository;
	@Autowired
	private SmartRouteUtils smartRouteUtils;
	@Autowired
	private ErrorIdentifierService errorIdentifierService;
	@Autowired
	private  CommonResponseStructure CommonResponseStructure;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;

	public ResponseStructure balanceCheck(EntityModel userModel, MerchantPriceModel merchantPriceModel,
			VendorVerificationModel verificationModel) {

		ResponseStructure structure = new ResponseStructure();

		if (userModel.getPaymentMethod().getPaymentId() == 2) {
			if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

				structure.setCount(1);

			} else {
				userModel.setPaymentStatus("Dues");
				userRepository.save(userModel);

				structure.setCount(0);
				structure.setMessage("Please Recharge Amount");
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
			if (LocalDate.now().isEqual(userModel.getGraceDate())
					|| LocalDate.now().isBefore(userModel.getGraceDate())) {

				structure.setCount(1);

			} else {
				structure.setCount(0);
				structure.setMessage("Please pay the Amount");
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}
		} else {
			structure.setCount(0);
			structure.setMessage("PAYMENT METHOD NOT AVAILABLE");
		}
		return structure;
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
//		object.put("status", model.getStatus());
//		object.put("encrypted_response", model.getResponse());

		if (serviceModel.getVendorVerificationId() == 6) {
			object.put("request_id", request.getRequestId());
			object.put("response_id", model.getResponseId());
		}

		JSONObject wholeData = new JSONObject(model.getResponse());
		String returnResponse = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		// String returnResponse = PasswordUtils.demoEncryption(object,
		// userModel.getSecretKey());
		System.out.println("RETURN RESPONSE : " + returnResponse);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2) {
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
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

	public Response sourceCheck(String source, EntityModel user, MerchantPriceModel merchantPriceModel)
			throws Exception {

		System.err.println("NO SOURCE : " + (merchantPriceModel.isNoSourceCheck()));

		if (merchantPriceModel.isNoSourceCheck()) {

			System.err.println("NO SOURCE IN");
			return new Response();
		}

		System.err.println("NO SOURCE OUT");
		
		VendorVerificationModel verificationModel = merchantPriceModel.getVendorVerificationModel();

		List<Response> sourceList = respRepository.findBySourceAndVerificationModel(source,verificationModel);
		Response model = new Response();

		if (!sourceList.isEmpty()) {
			for (Response response : sourceList) {
				if (response.getStatus().equalsIgnoreCase("success")) {
					model = response;
				}
			}
		}
		return model;
	}

	@Override
	public ResponseStructure aadharDirect(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());

			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.AADHAR_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			System.err.println("userModel :" + userModel.getUserId());
			System.err.println("AAAA :" + AppConstants.AADHAR_VERIFY);
			System.err.println("verificationModel :" + verificationModel.getVendorVerificationId());
			System.err.println("vendorModel :" + vendorModel.getVendorId());

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {

				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				return aadharDirectVerification(model, userModel, verificationModel, vendorModel, merchantPriceModel,
						vendorPriceModel, userJson);

			} else if (balance.getCount() == 0) {
				return balance;
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

	private ResponseStructure aadharDirectVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

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
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);
		

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");
			String clientId = internalData.getString("client_id");

			String aadharNumber = internalData.getString("aadhaar_number");
			String state = internalData.getString("state");
			String gender = internalData.getString("gender");
			String remarks = internalData.getString("remarks");
			boolean isMobile = internalData.getBoolean("is_mobile");

			request.setReferenceId(referenceId);
			// request.setFullName(ownerName);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setAttempt(0);

			response.setClientId(clientId);
//				response.setFullName(ownerName);
//				response.setAddress(permanentAddress);
//				response.setDateOfRegistration(regDate);
			response.setStatus("success");
			response.setReferenceId(referenceId);

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// String commonResponse = CommonResponseStructure.commonResponseRc(response,
		// userModel);

		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);

		return structure;

	}

	@Override
	public ResponseStructure individualCrimeCheck(RequestModel model, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			if (userModel != null) {

				VendorVerificationModel verificationModel = verificationRepository
						.findByVerificationDocument("INDIVIDUAL CRIME CHECK");
				VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

				List<MerchantPriceModel> merchantPriceList = merchantPriceRepository
						.findByEntityModelAndVendorVerificationModelAndStatus(userModel, verificationModel, true);

				if (!merchantPriceList.isEmpty()) {

					MerchantPriceModel merchantPriceModel = merchantPriceList.get(1);

					ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

					if (balance.getCount() == 1) {

						String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
								userModel.getSecretKey());
						JSONObject userJson = new JSONObject(userDecryption);

						userJson.put("source_type", "id");

						return sprintVIndividualCrimeCheck(model, userModel, verificationModel, vendorModel,
								merchantPriceModel, vendorPriceModel, userJson);

					} else if (balance.getCount() == 0) {
						return balance;
					}
				} else {

					return smartRouteUtils.noAccessForThisVerification(userModel, verificationModel,model);
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

	private ResponseStructure sprintVIndividualCrimeCheck(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String fatherName = userJson.getString("father_name");
		String address = userJson.getString("address");
		String dob = userJson.getString("dob");
		String panNumber = userJson.getString("pan_number");
		String requestTag = userJson.getString("req_tag");
		String ticketSize = userJson.getString("ticket_size");
		String sourceType = userJson.getString("source_type");
		boolean crimeWatch = userJson.getBoolean("crimewatch");
		boolean reportMode = userJson.getBoolean("report_mode");

		String realTimeHighAccuracy = null;

		if (reportMode) {
			realTimeHighAccuracy = "realTimeHighAccuracy";
		}

		JSONObject obj = new JSONObject();
		obj.put("name", source);
		obj.put("father_name", fatherName);
		obj.put("address", address);
		obj.put("dob", dob);
		obj.put("pan_number", panNumber);
		obj.put("report_mode", realTimeHighAccuracy);
		obj.put("req_tag", requestTag);
		obj.put("ticket_size", ticketSize);
		obj.put("crimewatch", crimeWatch);

		Request request = new Request();
		Date reqDate = new Date();

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);
		

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");

			String requestId = internalData.getString("requestId");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setClientId(requestId);
			request.setAttempt(0);

			response.setClientId(requestId);
			response.setStatus("success");
			response.setReferenceId(referenceId);

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}

		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// String commonResponse = CommonResponseStructure.commonResponseRc(response,
		// userModel);

		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure crimeCheckPdfDownload(RequestModel model, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			if (userModel != null) {

				VendorVerificationModel verificationModel = verificationRepository
						.findByVerificationDocument("CRIMECHECK PDF REPORT");

				VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

				List<MerchantPriceModel> merchantPriceList = merchantPriceRepository
						.findByEntityModelAndVendorVerificationModelAndStatus(userModel, verificationModel, true);

				if (!merchantPriceList.isEmpty()) {

					MerchantPriceModel merchantPriceModel = merchantPriceList.get(1);

					ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

					if (balance.getCount() == 1) {

						String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
								userModel.getSecretKey());
						JSONObject userJson = new JSONObject(userDecryption);

						userJson.put("source_type", "id");

						return sprintVCrimeCheckPdfDownload(model, userModel, verificationModel, vendorModel,
								merchantPriceModel, vendorPriceModel, userJson);

					} else if (balance.getCount() == 0) {
						return balance;
					}

				} else {

					return smartRouteUtils.noAccessForThisVerification(userModel, verificationModel,model);
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

	private ResponseStructure sprintVCrimeCheckPdfDownload(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");

		JSONObject obj = new JSONObject();
		obj.put("request_id", source);

		Request request = reqRepository.findByClientId(source);

		System.out.println("REQ ID : " + request.getRequestId());
		Date reqDate = new Date();

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = respRepository.findByClientId(source);
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		// request.setIpAddress(ipAddress);
		// response.setIpAddress(ipAddress);

		// request.setSource(source);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice() + request.getPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		// response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			String internalData = wholeData.getString("data");

			request.setAttempt(0);

			response.setStatus("success");
			response.setCommonResponse(internalData);

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}

		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// String commonResponse = CommonResponseStructure.commonResponseRc(response,
		// userModel);

		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure crimeCheckJsonReportDownload(RequestModel model, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			if (userModel != null) {

				VendorVerificationModel verificationModel = verificationRepository
						.findByVerificationDocument("CRIMECHECK JSON REPORT");

				VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

				List<MerchantPriceModel> merchantPriceList = merchantPriceRepository
						.findByEntityModelAndVendorVerificationModelAndStatus(userModel, verificationModel, true);

				if (!merchantPriceList.isEmpty()) {

					MerchantPriceModel merchantPriceModel = merchantPriceList.get(1);

					ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

					if (balance.getCount() == 1) {

						String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
								userModel.getSecretKey());
						JSONObject userJson = new JSONObject(userDecryption);

						userJson.put("source_type", "id");

						return sprintVCrimeCheckJsonReportDownload(model, userModel, verificationModel, vendorModel,
								merchantPriceModel, vendorPriceModel, userJson);

					} else if (balance.getCount() == 0) {
						return balance;
					}

				} else {

					return smartRouteUtils.noAccessForThisVerification(userModel, verificationModel,model);
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

	private ResponseStructure sprintVCrimeCheckJsonReportDownload(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");

		JSONObject obj = new JSONObject();
		obj.put("request_id", source);

		Request request = reqRepository.findByClientId(source);

		System.out.println("REQ ID : " + request.getRequestId());
		Date reqDate = new Date();

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = respRepository.findByClientId(source);
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		// request.setIpAddress(ipAddress);
		// response.setIpAddress(ipAddress);

		// request.setSource(source);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice() + request.getPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		// response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			JSONObject internalData = wholeData.getJSONObject("data");

			request.setStatus("success");
			request.setAttempt(0);

			response.setStatus("success");
			response.setCommonResponse(internalData.toString());

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}

		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2) {
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// String commonResponse = CommonResponseStructure.commonResponseRc(response,
		// userModel);

		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);

		return structure;
	}

	@Override
	public ResponseStructure aadharWithOtp(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			EntityModel userModel = userRepository.findByUserId(model.getUserId());

			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.AADHAR_XML_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
							userModel.getSecretKey());
					JSONObject userJson = new JSONObject(userDecryption);

					return aadharwithOtpVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel, userJson);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
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

	private ResponseStructure aadharwithOtpVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

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
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setReferenceId(referenceNumber);
		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setReferenceId(referenceNumber);
		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			JSONObject internalData = wholeData.getJSONObject("data");
			String clientId = internalData.getString("client_id");
			boolean otpSent = internalData.getBoolean("otp_sent");
			boolean ifNumber = internalData.getBoolean("if_number");
			boolean validAadhar = internalData.getBoolean("valid_aadhaar");
			String otpGenerationStatus = internalData.getString("status");

			request.setStatus("success");
			request.setClientId(clientId);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setStatus("success");

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// String commonResponse =
		// CommonResponseStructure.commonResponseAadharwithOtp(request,response,
		// userModel);

		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);

		vendorRepository.save(vendorModel);
		userRepository.save(userModel);
		
		return structure;
	}

	@Override
	public ResponseStructure aadharOtpValidate(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());

			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.AADHAR_OTP_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
							userModel.getSecretKey());
					JSONObject userJson = new JSONObject(userDecryption);

					return aadharOtpValidateVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel, userJson);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
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

	private ResponseStructure aadharOtpValidateVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String referenceNumber = userJson.getString("reference_id");

		Response responseModel = respRepository.findByReferenceId(referenceNumber);
		Request requestModel = reqRepository.findByReferenceId(referenceNumber);

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
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = responseModel;
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setRequestDateAndTime(new Date());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);

		RequestModel temporary = new RequestModel();

		temporary.setResponseDateAndTime(respTime);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setVendorModel(vendorModel);

		if (status) {

			temporary.setStatus("success");

			long referenceIdNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceIdNumber);

			JSONObject internalData = wholeData.getJSONObject("data");

			String clientId = internalData.getString("client_id");
			String fullName = internalData.getString("full_name");
			String aadharNumber = internalData.getString("aadhaar_number");
			String dob = internalData.getString("dob");
			String gender = internalData.getString("gender");
			String address = internalData.getJSONObject("address").toString();
			boolean mobileVerified = internalData.getBoolean("mobile_verified");

			temporary.setReferenceId(referenceId);
			temporary.setFullName(fullName);
			temporary.setAadhaarNumber(aadharNumber);
			temporary.setDob(dob);
			temporary.setMessage(message);
			temporary.setTransactionId(clientId);
			temporary.setAddress(address);
			temporary.setGender(gender);
			temporary.setMobileVerified(mobileVerified);

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setStatus("success");
			response.setReferenceId(referenceId);

		} else {

			temporary.setStatus("failed");
			temporary.setReferenceId(referenceNumber);

			request.setError("error");
			request.setStatus("failed");
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// String commonResponse =
		// CommonResponseStructure.commonResponseAadharOtpSubmit(temporary, userModel);

		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure voterId(RequestModel model, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			if (userModel != null) {

				VendorVerificationModel verificationModel = verificationRepository
						.findByVerificationDocument(AppConstants.VOTERID_VERIFY);
				VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

				List<MerchantPriceModel> merchantPriceList = merchantPriceRepository
						.findByEntityModelAndVendorVerificationModelAndStatus(userModel, verificationModel, true);

				if (!merchantPriceList.isEmpty()) {

					MerchantPriceModel merchantPriceModel = merchantPriceList.get(1);

					ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

					if (balance.getCount() == 1) {
						String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
								userModel.getSecretKey());
						JSONObject userJson = new JSONObject(userDecryption);

						userJson.put("source_type", "id");

						String source = userJson.getString("source");

						System.err.println("SOURCE : " + source);

						Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
						System.err.println("Source Response : " + sourceResponse);

						if (sourceResponse.getResponseId() > 0) {
							System.err.println("SOURCE PRESENT");

							return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

						} else {

							return voterIdVerification(model, userModel, verificationModel, vendorModel,
									merchantPriceModel, vendorPriceModel, userJson);
						}
					} else if (balance.getCount() == 0) {
						return balance;
					}
				} else {

					return smartRouteUtils.noAccessForThisVerification(userModel, verificationModel,model);
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

	private ResponseStructure voterIdVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

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
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");

			String name = internalData.getString("name");
			// String dob=internalData.getString("dob");
			String state = internalData.getString("state");
			String clientId = internalData.getString("client_id");
			String area = internalData.getString("area");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setFullName(name);
			// request.setDob(dob);
			request.setClientId(clientId);
			request.setAttempt(0);

			response.setStatus("success");
			response.setFullName(name);
			// response.setDob(dob);
			response.setState(state);
			response.setClientId(clientId);
			response.setAddress(area);
			response.setExtractedData(internalData.toString());
			response.setReferenceId(referenceId);

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure drivingLicence(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.DL_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				userJson.put("source_type", "id");

				String source = userJson.getString("source");

				System.err.println("SOURCE : " + source);

				Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
				System.err.println("Source Response : " + sourceResponse);

				if (sourceResponse.getResponseId() > 0) {
					System.err.println("SOURCE PRESENT");

					return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

				} else {

					return drivingLicenceVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel, userJson);
				}
			} else if (balance.getCount() == 0) {
				return balance;
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

	private ResponseStructure drivingLicenceVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

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
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");
			String clientId = internalData.getString("client_id");
			String state = internalData.getString("state");
			String fullName = internalData.getString("name");
			String address = internalData.getString("permanent_address");
			String dateOfBirth = internalData.getString("dob");

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
			response.setExtractedData(internalData.toString());

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// String commonResponse =
		// CommonResponseStructure.commonResponseDrivingLicense(response, userModel);
		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);

		return structure;
	}

	@Override
	public ResponseStructure gst(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.GST_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				userJson.put("source_type", "id");

				String source = userJson.getString("source");

				System.err.println("SOURCE : " + source);

				Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
				System.err.println("Source Response : " + sourceResponse);

				if (sourceResponse.getResponseId() > 0) {
					System.err.println("SOURCE PRESENT");

					return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

				} else {

					return gstVerification(model, userModel, verificationModel, vendorModel, merchantPriceModel,
							vendorPriceModel, userJson);
				}
			} else if (balance.getCount() == 0) {
				return balance;
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

	private ResponseStructure gstVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");
		boolean fileStatus = userJson.getBoolean("filing_status_get");

		String filingStatus = Boolean.toString(fileStatus);

		JSONObject obj = new JSONObject();
		obj.put("id_number", source);
		obj.put("filing_status", filingStatus);

		Request request = new Request();
		Date reqDate = new Date();

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setFilingStatus(fileStatus);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setFilingStatus(fileStatus);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");
			String clientId = internalData.getString("client_id");
			String pan = internalData.getString("pan_number");
			String gstIn = internalData.getString("gstin");

			String businessName = internalData.getString("business_name");
			String dateOfReg = internalData.getString("date_of_registration");
			String dateOfCancellation = internalData.getString("date_of_cancellation");
			String address = internalData.getString("address");

			if (fileStatus) {

				JSONArray filingStatusJsonList = internalData.getJSONArray("filing_status");
				//temporary.setFilingStatusJsonList(filingStatusJsonList);

			} else {
				JSONArray filingStatusJsonList = new JSONArray();
				//temporary.setFilingStatusJsonList(filingStatusJsonList);

			}

			
			request.setReferenceId(referenceId);
			request.setClientId(clientId);
			request.setCompanyName(businessName);
			request.setAttempt(0);
			request.setStatus("success");

			response.setGstIn(gstIn);
			response.setClientId(clientId);
			response.setPanNumber(pan);
			response.setStatus("success");
			response.setReferenceId(referenceId);
			response.setBusinessName(businessName);
			response.setDateOfRegistration(dateOfReg);
			response.setDateOfCancellation(dateOfCancellation);
			response.setAddress(address);
			response.setExtractedData(internalData.toString());

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// String commonResponse = CommonResponseStructure.commonResponseGst(response,
		// userModel);
		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure passport(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.PASSPORT_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				// String referenceNumber = FileUtils.getRandomOTPnumber(10);
				userJson.put("source_type", "id");

				String source = userJson.getString("source");

				System.err.println("SOURCE : " + source);

				Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
				System.err.println("Source Response : " + sourceResponse);

				if (sourceResponse.getResponseId() > 0) {
					System.err.println("SOURCE PRESENT");

					return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

				} else {

					return passportVerification(model, userModel, verificationModel, vendorModel, merchantPriceModel,
							vendorPriceModel, userJson);
				}
			} else if (balance.getCount() == 0) {
				return balance;
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

	private ResponseStructure passportVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

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
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {// Changes are to be made.

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");
			String clientId = internalData.getString("client_id");
			String passportNumber = internalData.getString("passport_number");
			String fullName = internalData.getString("full_name");
			// String address = internalData.getString("permanent_address");
			String dateOfBirth = internalData.getString("dob");

			request.setReferenceId(referenceId);
			request.setClientId(clientId);
			request.setAttempt(0);
			request.setStatus("success");

			response.setClientId(clientId);
			response.setStatus("success");
			response.setFullName(fullName);
			response.setDob(dateOfBirth);
			response.setExtractedData(internalData.toString());

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// String commonResponse =
		// CommonResponseStructure.commonResponsePassport(response, userModel);
		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure udyamAadhar(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.MSME_UDYAMAADHAR_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {

				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				String referenceNumber = FileUtils.getRandomOTPnumber(10);
				userJson.put("source_type", "id");

				String source = userJson.getString("source");

				System.err.println("SOURCE : " + source);

				Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
				System.err.println("Source Response : " + sourceResponse);

				if (sourceResponse.getResponseId() > 0) {
					System.err.println("SOURCE PRESENT");

					return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

				} else {

					return udyamAadharVerification(model, userModel, verificationModel, vendorModel, merchantPriceModel,
							vendorPriceModel, userJson);
				}
			} else if (balance.getCount() == 0) {
				return balance;
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

	private ResponseStructure udyamAadharVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

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
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");
			String clientId = internalData.getString("client_id");
			String uan = internalData.getString("uan");

			JSONObject mainDetails = internalData.getJSONObject("main_details");

			String enterpriseName = mainDetails.getString("name_of_enterprise ");
			String state = mainDetails.getString("state");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setState(state);
			request.setCompanyName(enterpriseName);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setState(state);
			response.setBusinessName(enterpriseName);
			response.setStatus("success");
			response.setReferenceId(referenceId);
			response.setExtractedData(internalData.toString());

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// String commonResponse =
		// CommonResponseStructure.commonResponseUdyamMsme(response, userModel);
		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure itrComplianceCheck(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return itrComplianceCheckVerificatoin(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure itrComplianceCheckVerificatoin(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String panNumber = model.getPanNumber();

			JSONObject obj = new JSONObject();
			obj.put("pan_number", panNumber);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure itrAcknowledgement(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return itrAcknowledgementVerificatoin(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure itrAcknowledgementVerificatoin(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String acknowledgementNumber = model.getAcknowledgementNumber();

			JSONObject obj = new JSONObject();
			obj.put("ack_number", acknowledgementNumber);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure mcaCompanyDetails(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return mcaCompanyDetailsVerificatoin(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure mcaCompanyDetailsVerificatoin(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String mcaId = model.getMcaId();

			JSONObject obj = new JSONObject();
			obj.put("id_number", mcaId);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure mcaDin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.DIN_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				userJson.put("source_type", "id");

				String source = userJson.getString("source");

				System.err.println("SOURCE : " + source);

				Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
				System.err.println("Source Response : " + sourceResponse);

				if (sourceResponse.getResponseId() > 0) {
					System.err.println("SOURCE PRESENT");

					return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

				} else {

					return dinVerification(model, userModel, verificationModel, vendorModel, merchantPriceModel,
							vendorPriceModel, userJson);
				}
			} else if (balance.getCount() == 0) {
				return balance;
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

	private ResponseStructure dinVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

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
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		JSONObject returnJson = new JSONObject();

		returnJson.put("status", status);
		returnJson.put("statuscode", statusCodeNumber);

		if (status) {

			JSONObject data = new JSONObject();

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");
			String clientId = internalData.getString("client_id");

			String companyId = internalData.getString("company_id");
			String companyType = internalData.getString("company_type");
			String companyName = internalData.getString("company_name");

			JSONObject details = internalData.getJSONObject("details");
			JSONObject companyInfo = details.getJSONObject("company_info");

			JSONArray directors = details.getJSONArray("directors");

			String email = companyInfo.getString("email_id");

			returnJson.put("reference_id", referenceId);
			returnJson.put("client_id", clientId);
			data.put("company_type", companyType);
			data.put("company_id", companyId);
			data.put("company_name", companyName);
			data.put("directors", directors);
			returnJson.put("data", data);

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
			response.setExtractedData(internalData.toString());

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// String commonResponse = CommonResponseStructure.commonResponseDin(response,
		// userModel);
		String encryptedWholeData = PasswordUtils.demoEncryption(returnJson, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure mcaCin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.CIN_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				// String referenceNumber = FileUtils.getRandomOTPnumber(10);
				userJson.put("source_type", "id");

				String source = userJson.getString("source");

				System.err.println("SOURCE : " + source);

				Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
				System.err.println("Source Response : " + sourceResponse);

				if (sourceResponse.getResponseId() > 0) {
					System.err.println("SOURCE PRESENT");

					return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

				} else {

					return cinVerification(model, userModel, verificationModel, vendorModel, merchantPriceModel,
							vendorPriceModel, userJson);
				}
			} else if (balance.getCount() == 0) {
				return balance;
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

	private ResponseStructure cinVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

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
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();

	    LocalDateTime dateTime=LocalDateTime.now();
	    DateTimeFormatter format=DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	    String responseTime=dateTime.format(format);
		
		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(responseTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());
		

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
		response.setVerificationModel(verificationModel);

        RequestModel temporary = new RequestModel();
        
		temporary.setResponseDateAndTime(responseTime);
		temporary.setMessage(message);
		temporary.setStatusCodeNumber(statusCodeNumber);
		
		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");
			
			String clientId = internalData.optString("client_id","");
			String companyId = internalData.optString("company_id","");
			String companyType = internalData.optString("company_type","");
			String companyName = internalData.optString("company_name","");
			
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
			
			JSONArray directors=details.getJSONArray("directors");
			JSONArray charges=details.getJSONArray("charges");

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
			response.setBusinessName(companyName);//CompanyName->BusinessName
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
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		String encryptedCommonResponse = PasswordUtils.demoEncryption(commonResponse, userModel.getSecretKey());
		
		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure taxDeductionAccountNumber(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return taxDeductionAccountNumberVerificatoin(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure taxDeductionAccountNumberVerificatoin(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String taxDeductionIdNumber = model.getTaxDeductionIdNumber();

			JSONObject obj = new JSONObject();
			obj.put("id_number", taxDeductionIdNumber);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure stateListGet(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return stateListGetVerificatoin(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure stateListGetVerificatoin(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			JSONObject obj = new JSONObject();

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure shopEstablishmentDetails(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return shopEstablishmentDetailsVerificatoin(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure shopEstablishmentDetailsVerificatoin(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String stateCode = model.getStateCode();
			String shopNumber = model.getShopNumber();

			JSONObject obj = new JSONObject();
			obj.put("state_code", stateCode);
			obj.put("shop_number", shopNumber);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure registerCertificate(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.RC_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				userJson.put("source_type", "id");

				String source = userJson.getString("source");

				System.err.println("SOURCE : " + source);

				Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
				System.err.println("Source Response : " + sourceResponse);

				if (sourceResponse.getResponseId() > 0) {
					System.err.println("SOURCE PRESENT");

					return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

				} else {

					return registerCertificateVerificatoin(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel, userJson);
				}
			} else if (balance.getCount() == 0) {
				return balance;
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

	private ResponseStructure registerCertificateVerificatoin(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

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
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(responseTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

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
		response.setVerificationModel(verificationModel);

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
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		JSONObject commonResponse = CommonResponseStructure.commonResponseRc(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}
		
		String encryptedCommonResponse = PasswordUtils.demoEncryption(commonResponse, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure iecCheck(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return iecCheckVerificatoin(model, userModel, verificationModel, vendorModel, merchantPriceModel,
							vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure iecCheckVerificatoin(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String iecNumber = model.getIecNumber();

			JSONObject obj = new JSONObject();
			obj.put("iec_number", iecNumber);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure fssaiCheck(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return fssaiCheckVerificationn(model, userModel, verificationModel, vendorModel, merchantPriceModel,
							vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure fssaiCheckVerificationn(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String fssaiNumber = model.getFssaiNumber();

			JSONObject obj = new JSONObject();
			obj.put("fssai_number", fssaiNumber);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure emailCheck(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.EMAIL_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				userJson.put("source_type", "id");

				String source = userJson.getString("source");

				System.err.println("SOURCE : " + source);

				Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
				System.err.println("Source Response : " + sourceResponse);

				if (sourceResponse.getResponseId() > 0) {
					System.err.println("SOURCE PRESENT");

					return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

				} else {

					return emailCheckVerification(model, userModel, verificationModel, vendorModel, merchantPriceModel,
							vendorPriceModel, userJson);
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure emailCheckVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");

		JSONObject obj = new JSONObject();
		obj.put("email", source);

		Request request = new Request();
		Date reqDate = new Date();

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");
			String clientId = internalData.getString("client_id");
			String email = internalData.getString("email");
			String fullName = internalData.getString("username");

			request.setReferenceId(referenceId);
			request.setClientId(clientId);
			request.setEmail(email);
			request.setFullName(fullName);
			request.setAttempt(0);
			request.setStatus("success");

			response.setClientId(clientId);
			response.setEmail(email);
			response.setStatus("success");
			response.setFullName(fullName);
			response.setExtractedData(internalData.toString());

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// String commonResponse =
		// CommonResponseStructure.commonResponseDrivingLicense(response, userModel);
		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure legalEntityIdentifier(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return legalEntityIdentifierVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure legalEntityIdentifierVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String leiNumber = model.getLeiNumber();

			JSONObject obj = new JSONObject();
			obj.put("id_number", leiNumber);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure aadharQrSearch(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return aadharQrSearchVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure aadharQrSearchVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String qrText = model.getQrText();

			JSONObject obj = new JSONObject();
			obj.put("qr_text", qrText);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure epfoOtpSend(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return epfoOtpSendVerification(model, userModel, verificationModel, vendorModel, merchantPriceModel,
							vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure epfoOtpSendVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String epfoNumber = model.getEpfoNumber();

			JSONObject obj = new JSONObject();
			obj.put("epfo_number", epfoNumber);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure epfoOtpVerify(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return epfoOtpVerifyVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure epfoOtpVerifyVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String clientId = model.getClientId();
			String otp = model.getOtp();

			JSONObject obj = new JSONObject();
			obj.put("client_id", clientId);
			obj.put("otp", otp);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure passbookDownload(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return passbookDownloadVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure passbookDownloadVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String clientId = model.getClientId();

			JSONObject obj = new JSONObject();
			obj.put("client_id", clientId);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure epfoKycDetailsGet(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return epfoKycDetailsVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure epfoKycDetailsVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String clientId = model.getClientId();

			JSONObject obj = new JSONObject();
			obj.put("client_id", clientId);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;

	}

	@Override
	public ResponseStructure epfoWithoutOtp(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
		
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return epfoWithoutOtpVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure epfoWithoutOtpVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String uanNumber = model.getUanNumber();

			JSONObject obj = new JSONObject();
			obj.put("uan_number", uanNumber);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure ckycSearch(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return ckycSearchVerification(model, userModel, verificationModel, vendorModel, merchantPriceModel,
							vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure ckycSearchVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String documentType = model.getDocumentType();
			String idNumber = model.getIdNumber();

			JSONObject obj = new JSONObject();
			obj.put("document_type", documentType);
			obj.put("id_number", idNumber);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure ckycDownload(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return ckycDownloadVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure ckycDownloadVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String authFactorType = model.getAuthFactorType();
			String authFactor = model.getAuthFactor();
			String clientId = model.getClientId();

			JSONObject obj = new JSONObject();
			obj.put("auth_factor_type", authFactorType);
			obj.put("auth_factor", authFactor);
			obj.put("client_id", clientId);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure faceMatch(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return faceMatchVerification(model, userModel, verificationModel, vendorModel, merchantPriceModel,
							vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure faceMatchVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String threshold = model.getAuthFactorType();
			String image1 = model.getImage1();
			String image2Url = model.getImage2Url();

			JSONObject obj = new JSONObject();
			obj.put("threshold", threshold);
			obj.put("image1", image1);
			obj.put("image2_url", image2Url);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure livenessCheck(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return livenessCheckVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure livenessCheckVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String videoFile = model.getVideoFile();
			String videoUrl = model.getVideoUrl();

			JSONObject obj = new JSONObject();
			obj.put("video_file", videoFile);
			obj.put("video_url", videoUrl);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure upiIndex(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return upiIndexVerification(model, userModel, verificationModel, vendorModel, merchantPriceModel,
							vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;

	}

	private ResponseStructure upiIndexVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String upiId = model.getUpiId();

			JSONObject obj = new JSONObject();
			obj.put("id_number", upiId);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure vehicleChallan(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return vehicleChallanVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure vehicleChallanVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String vehicleId = model.getVehicleId();
			String chassis = model.getChassis();

			JSONObject obj = new JSONObject();
			obj.put("vehicle_id", vehicleId);
			obj.put("chassis", chassis);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure opticalCharacterRecognition(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
		
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return opticalCharacterRecognitionVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure opticalCharacterRecognitionVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String documentType = model.getDocumentType();
			String file = model.getFile();
			String link = model.getLink();
			String backImage = model.getBackImage();

			JSONObject obj = new JSONObject();
			obj.put("type", documentType);
			obj.put("file", file);
			obj.put("link", link);
			obj.put("back", backImage);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure bankAccountVerificationOne(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.BANK1_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
		
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				userJson.put("source_type", "id");

				String source = userJson.getString("source");

				System.err.println("SOURCE : " + source);

				Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
				System.err.println("Source Response : " + sourceResponse);

				if (sourceResponse.getResponseId() > 0) {
					System.err.println("SOURCE PRESENT");

					return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

				} else {

					return bankAccountVerificationOneVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel, userJson);
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure bankAccountVerificationOneVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String ifscCode = userJson.getString("ifsc_code");
		String sourceType = userJson.getString("source_type");

		JSONObject obj = new JSONObject();
		obj.put("account_number", source);
		obj.put("ifsc_code", ifscCode);

		Request request = new Request();
		Date reqDate = new Date();

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");
			int responseCode = internalData.getInt("response_code");
			boolean accountExist = internalData.getBoolean("account_exists");
			String name = internalData.getString("account_name");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setFullName(name);
			request.setFilingStatus(accountExist);
			request.setAttempt(0);

			response.setStatus("success");
			response.setFullName(name);
			response.setFilingStatus(accountExist);
			response.setExtractedData(internalData.toString());
			response.setReferenceId(referenceId);

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure bankAccountVerificationTwo(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.BANK2_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				userJson.put("source_type", "id");

				String source = userJson.getString("source");

				System.err.println("SOURCE : " + source);

				Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
				System.err.println("Source Response : " + sourceResponse);

				if (sourceResponse.getResponseId() > 0) {
					System.err.println("SOURCE PRESENT");

					return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

				} else {

					return bankAccountVerificationTwoVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel, userJson);
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure bankAccountVerificationTwoVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String ifscCode = userJson.getString("ifsc_code");
		String sourceType = userJson.getString("source_type");

		JSONObject obj = new JSONObject();
		obj.put("account_number", source);
		obj.put("ifsc_code", ifscCode);

		Request request = new Request();
		Date reqDate = new Date();

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");

			String newTransRefId = internalData.getString("nwtxnrefid");
			String newResponseMessage = internalData.getString("nwrespmessg");
			String newResponseCode = internalData.getString("nwrespcode");
			String name = internalData.getString("c_name");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setFullName(name);
			request.setAttempt(0);

			response.setStatus("success");
			response.setFullName(name);
			response.setExtractedData(internalData.toString());
			response.setReferenceId(referenceId);

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure bavPennyLess(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.BAVPENNYLESS_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				userJson.put("source_type", "id");

				String source = userJson.getString("source");

				System.err.println("SOURCE : " + source);

				Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
				System.err.println("Source Response : " + sourceResponse);

				if (sourceResponse.getResponseId() > 0) {
					System.err.println("SOURCE PRESENT");

					return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

				} else {

					return bavPennyLessVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel, userJson);
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure bavPennyLessVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String ifscCode = userJson.getString("ifsc_code");
		String sourceType = userJson.getString("source_type");

		JSONObject obj = new JSONObject();
		obj.put("account_number", source);
		obj.put("ifsc_code", ifscCode);

		Request request = new Request();
		Date reqDate = new Date();

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");

			String newTransRefId = internalData.getString("nwtxnrefid");
			String newResponseMessage = internalData.getString("nwrespmessg");
			String newResponseCode = internalData.getString("nwrespcode");
			String name = internalData.getString("c_name");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setFullName(name);
			request.setAttempt(0);

			response.setStatus("success");
			response.setFullName(name);
			response.setExtractedData(internalData.toString());
			response.setReferenceId(referenceId);

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}
		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure bavPennydropVOne(RequestModel model, HttpServletRequest servletRequest) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			if (userModel != null) {

				VendorVerificationModel verificationModel = verificationRepository
						.findByVerificationDocument(AppConstants.BAVPENNYDROP_V1_VERIFY);
				VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

				List<MerchantPriceModel> merchantPriceList = merchantPriceRepository
						.findByEntityModelAndVendorVerificationModelAndStatus(userModel, verificationModel, true);

				if (!merchantPriceList.isEmpty()) {

					MerchantPriceModel merchantPriceModel = merchantPriceList.get(1);

					ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

					if (balance.getCount() == 1) {
						String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
								userModel.getSecretKey());
						JSONObject userJson = new JSONObject(userDecryption);

						userJson.put("source_type", "id");

						String source = userJson.getString("source");

						System.err.println("SOURCE : " + source);

						Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
						System.err.println("Source Response : " + sourceResponse);

						if (sourceResponse.getResponseId() > 0) {
							System.err.println("SOURCE PRESENT");

							return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

						} else {

							return bavPennydropVOneVerification(model, userModel, verificationModel, vendorModel,
									merchantPriceModel, vendorPriceModel, userJson);
						}
					} else if (balance.getCount() == 0) {
						return balance;
					}
				} else {

					return smartRouteUtils.noAccessForThisVerification(userModel, verificationModel,model);
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

	private ResponseStructure bavPennydropVOneVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String ifscCode = userJson.getString("ifsc_code");
		int extendedData = userJson.getInt("extended_data");
		String sourceType = userJson.getString("source_type");

		JSONObject obj = new JSONObject();
		obj.put("account_number", source);
		obj.put("ifsc_code", ifscCode);
		obj.put("extended_data", extendedData);

		Request request = new Request();
		Date reqDate = new Date();

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		wholeData.put("response_time", respTime);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");

			String accountNumber = internalData.getString("account_number");
			String accountName = internalData.getString("account_name");
			String ifsc = internalData.getString("ifsc");
			// String upiId = internalData.getString("upi_id"); //NOT A STRING OR JSOONOBJ
			String npciResponseCode = internalData.getString("npci_bo_response_code");
			String npciStatusCode = internalData.getString("npci_status_code");
			String npciStatusDescription = internalData.getString("npci_status_description");
			boolean accountExist = internalData.getBoolean("account_exists");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setFullName(accountName);
			request.setFilingStatus(accountExist);
			request.setAttempt(0);

			response.setStatus("success");
			response.setFullName(accountName);
			response.setFilingStatus(accountExist);
			// response.setExtractedData(internalData.toString());
			response.setReferenceId(referenceId);

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);

		vendorRepository.save(vendorModel);
		userRepository.save(userModel);
		
		return structure;
	}

	@Override
	public ResponseStructure bavPennydropVTwo(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.BAVPENNYDROP_V2_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				userJson.put("source_type", "id");

				String source = userJson.getString("source");

				System.err.println("SOURCE : " + source);

				Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
				System.err.println("Source Response : " + sourceResponse);

				if (sourceResponse.getResponseId() > 0) {
					System.err.println("SOURCE PRESENT");

					return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

				} else {

					return bavPennydropVTwoVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel, userJson);
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure bavPennydropVTwoVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String ifscCode = userJson.getString("ifsc_code");
		String sourceType = userJson.getString("source_type");

		JSONObject obj = new JSONObject();
		obj.put("account_number", source);
		obj.put("ifsc_code", ifscCode);

		Request request = new Request();
		Date reqDate = new Date();

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		
		String ipAddress =  ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");

			String newTransRefId = internalData.getString("nwtxnrefid");
			String newResponseMessage = internalData.getString("nwrespmessg");
			String newResponseCode = internalData.getString("nwrespcode");
			String transactionRefNo = internalData.getString("txnrefno");
			String name = internalData.getString("c_name");
			JSONObject requestDetails = internalData.getJSONObject("reqdtls");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setFullName(name);
			request.setAttempt(0);

			response.setStatus("success");
			response.setFullName(name);
			response.setExtractedData(internalData.toString());
			response.setReferenceId(referenceId);

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;
	}

	@Override
	public ResponseStructure revereseGeoLocation(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return revereseGeoLocationVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure revereseGeoLocationVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String latitude = model.getLatitude();
			String longitude = model.getLongitude();

			JSONObject obj = new JSONObject();
			obj.put("latitude", latitude);
			obj.put("longitude", longitude);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure ipAddressLookUp(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return ipAddressLookUpVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure ipAddressLookUpVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String ipAddress = model.getIpAddress();

			JSONObject obj = new JSONObject();
			obj.put("ip_address", ipAddress);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure mobileOperatorCheck(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return mobileOperatorCheckVerification(model, userModel, verificationModel, vendorModel,
							merchantPriceModel, vendorPriceModel);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure mobileOperatorCheckVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) {
		ResponseStructure structure = new ResponseStructure();
		try {

			long mobile = model.getMobile();

			JSONObject obj = new JSONObject();
			obj.put("mobile", mobile);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			System.err.println("sprintVerifyResponse : " + sprintVerifyResponse);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure itrCreateClient(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return itrCreateClientVerificatoin(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure itrCreateClientVerificatoin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String panNumber = model.getPanNumber();
			String password = model.getPassword();

			JSONObject obj = new JSONObject();
			obj.put("username", panNumber);
			obj.put("password", password);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure itrForgetPassword(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return itrForgetPasswordVerificatoin(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure itrForgetPasswordVerificatoin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String clientId = model.getClientId();
			String newPassword = model.getNewPassword();

			JSONObject obj = new JSONObject();
			obj.put("client_id", clientId);
			obj.put("password", newPassword);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure itrOtpSubmit(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return itrOtpSubmitVerificatoin(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure itrOtpSubmitVerificatoin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String clientId = model.getClientId();
			String otp = model.getOtp();

			JSONObject obj = new JSONObject();
			obj.put("client_id", clientId);
			obj.put("otp", otp);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure itrProfileGet(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return itrProfileGetVerificatoin(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure itrProfileGetVerificatoin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String clientId = model.getClientId();

			JSONObject obj = new JSONObject();
			obj.put("client_id", clientId);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure getItrList(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return getItrListVerificatoin(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure getItrListVerificatoin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String clientId = model.getClientId();

			JSONObject obj = new JSONObject();
			obj.put("client_id", clientId);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure getSingleItrDetails(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return getSingleItrDetailsVerificatoin(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure getSingleItrDetailsVerificatoin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String clientId = model.getClientId();
			String itrId = model.getItrId();

			JSONObject obj = new JSONObject();
			obj.put("client_id", clientId);
			obj.put("itr_id", itrId);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure get26AsList(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return get26AsListVerificatoin(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure get26AsListVerificatoin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String clientId = model.getClientId();

			JSONObject obj = new JSONObject();
			obj.put("client_id", clientId);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure getSingle26AsList(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return getSingle26AsListVerificatoin(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure getSingle26AsListVerificatoin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String clientId = model.getClientId();
			String tdsId = model.getTdsId();

			JSONObject obj = new JSONObject();
			obj.put("client_id", clientId);
			obj.put("tds_id", tdsId);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure courtCaseStatus(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return courtCaseStatusVerificatoin(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure courtCaseStatusVerificatoin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String name = model.getName();

			JSONObject obj = new JSONObject();
			obj.put("name", name);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure companyTanLookup(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return companyTanLookupVerificatoin(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure companyTanLookupVerificatoin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String companyName = model.getCompanyName();

			JSONObject obj = new JSONObject();
			obj.put("company_name", companyName);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure fuelPriceFetch(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return fuelPriceFetchVerificatoin(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure fuelPriceFetchVerificatoin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String cityState = model.getCityState();

			JSONObject obj = new JSONObject();
			obj.put("citystate", cityState);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure panToGst(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return panToGstVerificatoin(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure panToGstVerificatoin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String panNumber = model.getPanNumber();

			JSONObject obj = new JSONObject();
			obj.put("pan_number", panNumber);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure stockPriceVerify(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return stockPriceVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure stockPriceVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String companyName = model.getCompanyName();

			JSONObject obj = new JSONObject();
			obj.put("company", companyName);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure regionalTransport(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return regionalTransportVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure regionalTransportVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String rtoCode = model.getRtoCode();

			JSONObject obj = new JSONObject();
			obj.put("rto_code", rtoCode);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure mobileNumberCase(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return mobileNumberCaseVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure mobileNumberCaseVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			long mobile = model.getMobile();

			JSONObject obj = new JSONObject();
			obj.put("mobile", mobile);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure sprintVPan(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVerificationDocument(AppConstants.PAN_VERIFY);
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
				JSONObject userJson = new JSONObject(userDecryption);

				// String referenceNumber = FileUtils.getRandomOTPnumber(10);
				userJson.put("source_type", "id");

				String source = userJson.getString("source");

				System.err.println("SOURCE : " + source);

				Response sourceResponse = sourceCheck(source, userModel, merchantPriceModel);
				System.err.println("Source Response : " + sourceResponse);

				if (sourceResponse.getResponseId() > 0) {
					System.err.println("SOURCE PRESENT");

					return setRequest(sourceResponse, model, merchantPriceModel, userModel, verificationModel);

				} else {

					return sprintVPanVerification(model, userModel, verificationModel, vendorModel, merchantPriceModel,
							vendorPriceModel, userJson);
				}
			} else if (balance.getCount() == 0) {
				return balance;
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

	private ResponseStructure sprintVPanVerification(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");

		JSONObject obj = new JSONObject();
		obj.put("pannumber", source);

		Request request = new Request();
		Date reqDate = new Date();

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		int reqCount = vendorModel.getVendorRequest() + 1;
		vendorModel.setVendorRequest(reqCount);
		
		// VendorResponse
		int respCount = vendorModel.getVendorResponse() + 1;
		vendorModel.setVendorResponse(respCount);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		Response response = new Response();
		String respTime = LocalDateTime.now().toString();

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(respTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setFreeHit(model.isFreeHit());

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(respTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(verificationModel);

		if (status) {

			long referenceNumber = wholeData.getLong("reference_id");
			String referenceId = Long.toString(referenceNumber);

			JSONObject internalData = wholeData.getJSONObject("data");
			String clientId = internalData.getString("client_id");
			String pan = internalData.getString("pan_number");
			String fullName = internalData.getString("full_name");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setFullName(fullName);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setFullName(fullName);
			response.setPanNumber(pan);
			response.setStatus("success");
			response.setReferenceId(referenceId);
			response.setExtractedData(internalData.toString());

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(0);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");
		}
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentId() == 2 && statusCodeNumber!=401 && statusCodeNumber!=403) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// String commonResponse = CommonResponseStructure.commonResponsePan(response,
		// userModel);

		String encryptedWholeData = PasswordUtils.demoEncryption(wholeData, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedWholeData);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);

		return structure;

	}

	@Override
	public ResponseStructure panDetailedInfo(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return panDetailedInfoVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure panDetailedInfoVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String panNumber = model.getPanNumber();

			JSONObject obj = new JSONObject();
			obj.put("id_number", panNumber);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure panComprehensive(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return panComprehensiveVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure panComprehensiveVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String panNumber = model.getPanNumber();

			JSONObject obj = new JSONObject();
			obj.put("pan_number", panNumber);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure companyNameToCin(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return companyNameToCinVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure companyNameToCinVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String companyName = model.getCompanyName();

			JSONObject obj = new JSONObject();
			obj.put("company_name", companyName);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure telecomSendOtp(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return telecomSendOtpVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure telecomSendOtpVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String mobile = model.getMobileNumber();

			JSONObject obj = new JSONObject();
			obj.put("id_number", mobile);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure telecomGetDetails(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return telecomGetDetailsVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure telecomGetDetailsVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String clientId = model.getClientId();
			String otp = model.getOtp();

			JSONObject obj = new JSONObject();
			obj.put("client_id", clientId);
			obj.put("otp", otp);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure digiLockerInitiateSession(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return digiLockerInitiateSessionVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure digiLockerInitiateSessionVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String referenceId = model.getReferenceId();
			String redirectUrl = model.getRedirectUrl();

			JSONObject obj = new JSONObject();
			obj.put("refid", referenceId);
			obj.put("redirect_url", redirectUrl);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure digiLockerAccessTokenGeneration(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return digiLockerAccessTokenGenerationVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure digiLockerAccessTokenGenerationVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String referenceId = model.getReferenceId();

			JSONObject obj = new JSONObject();
			obj.put("refid", referenceId);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure digiLockerGetIssuedFiles(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return digiLockerGetIssuedFilesVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure digiLockerGetIssuedFilesVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String referenceId = model.getReferenceId();

			JSONObject obj = new JSONObject();
			obj.put("refid", referenceId);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure digiLockerDownloadDocInPdf(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return digiLockerDownloadDocInPdfVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure digiLockerDownloadDocInPdfVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String referenceId = model.getReferenceId();
			String uri = model.getUri();

			JSONObject obj = new JSONObject();
			obj.put("refid", referenceId);
			obj.put("uri", uri);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure digiLockerDownloadDocInXml(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return digiLockerDownloadDocInXmlVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure digiLockerDownloadDocInXmlVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String referenceId = model.getReferenceId();
			String uri = model.getUri();

			JSONObject obj = new JSONObject();
			obj.put("refid", referenceId);
			obj.put("uri", uri);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure digiLockerEaadhaarDocInXml(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {
				if (verificationModel.getVendorVerificationId() == 1) {

					return digiLockerEaadhaarDocInXmlVerification(model);

				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			} else if (balance.getCount() == 0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure digiLockerEaadhaarDocInXmlVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String referenceId = model.getReferenceId();

			JSONObject obj = new JSONObject();
			obj.put("refid", referenceId);

			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);

			structure.setData(sprintVerifyResponse);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure sprintVBulkPan(List<RequestModel> model, HttpServletRequest servletRequest) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String userIdString = servletRequest.getHeader("userId");
			int userId = Integer.parseInt(userIdString);

			EntityModel userModel = userRepository.findByUserId(userId);

			VendorVerificationModel verificationModel = verificationRepository.findByVerificationDocument("BULK PAN");
			VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SPRINT_VERIFY_VENDOR);
			
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
							userModel,true);
			
			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance = balanceCheck(userModel, merchantPriceModel, verificationModel);

			if (balance.getCount() == 1) {

				return sprintVBulkPanVerification(model, userModel, verificationModel, vendorModel, merchantPriceModel,
						vendorPriceModel);

			} else if (balance.getCount() == 0) {
				return balance;
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure sprintVBulkPanVerification(List<RequestModel> panList, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		List<JSONObject> jsonPanList = new ArrayList<>();

		JSONArray jsonArray = new JSONArray();

		for (RequestModel requestModel : panList) {

			String referenceId = FileUtils.getRandomOrderNumer();
			JSONObject pan = new JSONObject();

			pan.put("pan_number", requestModel.getPanNumber());
			pan.put("refid", referenceId);

			System.err.println("ref id : " + referenceId);

			jsonArray.put(pan);
		}

		System.err.println("JSON ARRAY : " + jsonArray);

		JSONObject obj = new JSONObject();
		obj.put("data", jsonArray.toString());

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		structure.setData(wholeData);
		return structure;
	}

	@Override
	public ResponseStructure demoSprintVController(RequestModel dto) {
		ResponseStructure structure = new ResponseStructure();
		try {

			JSONObject inputParams = new JSONObject();

			inputParams.put("source", dto.getSource());
			inputParams.put("ifsc_code", dto.getIfscCode());
			inputParams.put("extended_data", dto.getExtendedData());
			inputParams.put("dob", dto.getDob());
			inputParams.put("father_name", dto.getFatherName());
			inputParams.put("address", dto.getAddress());
			inputParams.put("pan_number", dto.getPanNumber());
			inputParams.put("req_tag", dto.getRequestTag());
			inputParams.put("ticket_size", dto.getTicketSize());
			inputParams.put("crimewatch", dto.isCrimeWatch());
			inputParams.put("report_mode", dto.isReportMode());

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

}
