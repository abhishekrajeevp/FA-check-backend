package com.bp.middleware.smartrouteverification;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.admin.AdminServiceImplementation;
import com.bp.middleware.customexception.FacheckSideException;
import com.bp.middleware.duplicateverificationresponse.DuplicateUtils;
import com.bp.middleware.erroridentifier.VendorSideIssues;
import com.bp.middleware.erroridentifier.VendorSideIssuesRepository;
import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.postpaidstatement.PostpaidUserStatementService;
import com.bp.middleware.prepaidstatement.PrepaidUserStatementService;
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
import com.google.api.Http;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Component
public class SmartRouteUtils {

	private static final Logger LOGGER=LoggerFactory.getLogger(SmartRouteUtils.class);
	
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;
	@Autowired
	private VendorRepository vendorRepository;
	@Autowired
	private ResponseRepository respRepository;
	@Autowired
	private RequestRepository reqRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private VendorPriceRepository vendorPriceRepository;
	@Autowired
	private DuplicateUtils duplicateUtils;
	@Autowired
	FileUtils fu;
	@Autowired
	private PrepaidUserStatementService prepaidUserStatementService;
	@Autowired
	private PostpaidUserStatementService postpaidUserStatementService;
	@Autowired
	private VendorSideIssuesRepository vendorSideIssuesRepository;
	
	public final String[] signDeskErrorCodes = {"kyc_001","kyc_002","kyc_003","kyc_004","kyc_005","kyc_006","kyc_007","kyc_008","kyc_009","kyc_011","kyc_013","kyc_014","kyc_015","kyc_016","kyc_017","kyc_023","kyc_024"};
	
	public final String[] messageFilteration = {"kyc_003","kyc_005","kyc_015","kyc_016","kyc_017","kyc_023","kyc_024"};
	
	
	public ResponseStructure balanceCheck(EntityModel userModel, MerchantPriceModel merchantPriceModel,VendorVerificationModel vendorVerificationModel)
			throws Exception {

		ResponseStructure structure = new ResponseStructure();

		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

			double price = merchantPriceModel.getIdPrice();
			double gst = price * 18 / 100;

			double total = price + gst;

			if (userModel.getRemainingAmount() > total) {

				structure.setFlag(1);

			} else {

				return balanceEncryptResponse(userModel,vendorVerificationModel);
			}

		} else if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {

			boolean dateCheck = LocalDate.now().isEqual(userModel.getGraceDate())
					|| LocalDate.now().isBefore(userModel.getGraceDate());

			if (dateCheck) {

				structure.setFlag(1);

			} else {

				return balanceEncryptResponse(userModel,vendorVerificationModel);
			}
		}
		return structure;
	}

	
	
	public ResponseStructure balanceCheckForOcr(EntityModel userModel, MerchantPriceModel merchantPriceModel,VendorVerificationModel vendorVerificationModel)
			throws Exception {

		ResponseStructure structure = new ResponseStructure();

		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

			double price = merchantPriceModel.getImagePrice();
			double gst = price * 18 / 100;

			double total = price + gst;

			if (userModel.getRemainingAmount() > total) {

				structure.setFlag(1);

			} else {

				return balanceEncryptResponse(userModel,vendorVerificationModel);
			}

		} else if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {

			boolean dateCheck = LocalDate.now().isEqual(userModel.getGraceDate())
					|| LocalDate.now().isBefore(userModel.getGraceDate());

			if (dateCheck) {

				structure.setFlag(1);

			} else {

				return balanceEncryptResponse(userModel,vendorVerificationModel);
			}
		}
		return structure;
	}

	
	
	public ResponseStructure balanceCheckForSignature(EntityModel userModel, MerchantPriceModel merchantPriceModel,VendorVerificationModel vendorVerificationModel)
			throws Exception {

		ResponseStructure structure = new ResponseStructure();

		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

			double price = merchantPriceModel.getSignaturePrice();
			double gst = price * 18 / 100;

			double total = price + gst;

			if (userModel.getRemainingAmount() > total) {

				structure.setFlag(1);

			} else {

				return balanceEncryptResponse(userModel,vendorVerificationModel);
			}

		} else if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {

			boolean dateCheck = LocalDate.now().isEqual(userModel.getGraceDate())
					|| LocalDate.now().isBefore(userModel.getGraceDate());

			if (dateCheck) {

				structure.setFlag(1);

			} else {

				return balanceEncryptResponse(userModel,vendorVerificationModel);
			}
		}
		return structure;
	}

	
	
	public ResponseStructure balanceEncryptResponse(EntityModel entityModel,VendorVerificationModel vendorVerificationModel) throws Exception { //HERE

		ResponseStructure structure = new ResponseStructure();

		JSONObject response = new JSONObject();

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		response.put("status", "failed");
		response.put("response_time", responseTime);
		response.put("error_code", "fc_302");

		if (entityModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {

			response.put("error", "Payment Pending.!");
			response.put("message", "Your payment is pending, Please pay the amount to continue.");

		} else {

			response.put("error", "Balance Exhausted");
			response.put("message", "Your Wallet is Empty please recharge to continue.");
		}

		String encryptedCommonResponse = PasswordUtils.demoEncryption(response, entityModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);

		structure.setData(mapNew);
		structure.setFlag(2);

		if (entityModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {
			structure.setMessage("Payment Pending.!");
		} else {
			structure.setMessage("Balance Exhausted");
		}

		RequestModel model = new RequestModel();
		
		String error = (entityModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid"))?"Balance Exhausted":"Payment Pending !";
		
		model.setMessage(structure.getMessage());
		model.setError(error);
		model.setErrorCode("fc_302");
		
		trackEveryRequest(model,entityModel,vendorVerificationModel);
		
		return structure;
	}

	public Response sourceCheck(String source, EntityModel user, MerchantPriceModel merchantPriceModel)
			throws Exception {

		System.err.println("NO SOURCE CHECK : " + (merchantPriceModel.isNoSourceCheck()));

		if (merchantPriceModel.isNoSourceCheck()) {

			System.err.println("SOURCE CHECK IS NOT REQUIRED");
			return new Response();
		}

		System.err.println("SOURCE CHECK IS REQUIRED");

		VendorVerificationModel vendorVerify = merchantPriceModel.getVendorVerificationModel();

		List<Response> sourceList = respRepository.findBySourceAndVerificationModel(source, vendorVerify);
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

	
	
	public Response setRequest(Response sourceResponse, RequestModel model, MerchantPriceModel merchantPriceModel,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, JSONObject userJson) throws Exception {

		Request request = new Request();

		Date date = new Date();
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String stringDate = simple.format(date);

		String referenceId = userJson.getString("reference_id");

		request.setReferenceId(referenceId);
		request.setMessage(sourceResponse.getMessage());
		request.setTransactionId(sourceResponse.getTransactionId());
		request.setSource(sourceResponse.getSource());
		request.setSourceType(sourceResponse.getSourceType());
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(new Date());
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setResponseDateAndTime(stringDate);
		request.setStatus(sourceResponse.getStatus());
		request.setUser(userModel);
		request.setVerificationModel(vendorVerifyModel);

		request.setFilingStatus(sourceResponse.isFilingStatus());
		request.setCompanyName(sourceResponse.getBusinessName());
		request.setCompanyId(sourceResponse.getCompanyId());
		request.setCompanyType(sourceResponse.getCompanyType());
		request.setEmail(sourceResponse.getEmail());
		request.setFullName(sourceResponse.getFullName());
		request.setDob(sourceResponse.getDob());
		request.setState(sourceResponse.getState());
		request.setAttempt(0);

		reqRepository.save(request);

		JSONObject object = new JSONObject();
		object.put("status", sourceResponse.getStatus());
		object.put("encrypted_response", sourceResponse.getResponse());

		if (vendorVerifyModel.getVendorVerificationId() == 6) {
			object.put("request_id", request.getRequestId());
			object.put("response_id", sourceResponse.getResponseId());
		}

		String returnResponse = PasswordUtils.demoEncryption(object, userModel.getSecretKey());
		System.out.println("RETURN RESPONSE : " + returnResponse);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !model.isFreeHit()) {

			deductAmountForId(userModel,merchantPriceModel);
		}else if(!model.isFreeHit()) {
			
			postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		// Response Count
		int responseCount = userModel.getResponseCount();
		userModel.setResponseCount(responseCount + 1);
		userRepository.save(userModel);

		System.err.println("Source PRESENT");

		return sourceResponse;
	}

	public String sprintVerifyDocument(JSONObject obj, MerchantPriceModel merchantPriceModel) {

		try {

			VendorPriceModel vendorPrice = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(
					merchantPriceModel.getVendorModel(), merchantPriceModel.getVendorVerificationModel());

			String jwtToken = SmartRouteUtils.sprintVJwtToken();

			String apiEndpoint = vendorPrice.getApiLink();
			String requestBody = obj.toString();

			HttpClient httpClient = HttpClient.newBuilder().build();

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiEndpoint))
					.header("accept", "application/json").header("Content-Type", "application/json")
					.header("Token", jwtToken).header("authorisedkey", "TVRJek5EVTJOelUwTnpKRFQxSlFNREF3TURFPQ==")
					.method("POST", HttpRequest.BodyPublishers.ofString(requestBody)).build();

			System.err.println("BEFORE COMPLETABLE FUTURE");

			CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(request,
					HttpResponse.BodyHandlers.ofString());

			System.err.println("DURING COMPLETABLE FUTURE");

			try {

				HttpResponse<String> response = responseFuture.join();// Error here

				System.err.println("AFTER COMPLETABLE FUTURE");

				int statusCode = response.statusCode();
				String responseBody = response.body();

				System.out.println("Response Code: " + statusCode);

				return responseBody;

			} catch (Exception e) {
				Throwable cause = e.getCause();
				cause.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static String sprintVJwtToken() {
		try {
			String secretKey = "UTA5U1VEQXdNREF4VFZSSmVrNUVWVEpPZWxVd1RuYzlQUT09";

			JWSSigner signer = new MACSigner(secretKey);

			Map<String, Object> map = new HashMap<>();
			map.put("timestamp", new Date());
			map.put("partnerId", "CORP00001");
			map.put("reqid", 12345);

			JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().claim("timestamp", map.get("timestamp"))
					.claim("partnerId", map.get("partnerId")).claim("reqid", map.get("reqid")).build();

			SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaims);
			signedJwt.sign(signer);

			String jwtToken = signedJwt.serialize();
			System.err.println("JWT : " + jwtToken);

			return jwtToken;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public VendorModel vendorSuccessRate(VendorVerificationModel vendorVerifyModel) {

		Date currentDate = new Date();
		Calendar calender = Calendar.getInstance();

		calender.setTime(currentDate);
		calender.add(Calendar.MONTH, -1);

		Date oneMonthBefore = calender.getTime();

		List<Response> responseList = respRepository.findAll();
		VendorVerificationModel vendorVerify = vendorVerifyModel;

		long vendor1 = 0;
		long vendor2 = 0;
		long vendor3 = 0;
		long vendor4 = 0;

		for (Response response : responseList) {

			if (currentDate.after(response.getRequestDateAndTime())
					&& oneMonthBefore.before(response.getRequestDateAndTime())) {

				if (response.getRequest().getVerificationModel().getVendorVerificationId() == vendorVerify
						.getVendorVerificationId()) {

					if (response.getStatus().equalsIgnoreCase("success")) {

						if (response.getVendorModel().getVendorId() == 1) {
							vendor1++;
						} else if (response.getVendorModel().getVendorId() == 2) {
							vendor2++;
						} else if (response.getVendorModel().getVendorId() == 3) {
							vendor3++;
						} else if (response.getVendorModel().getVendorId() == 4) {
							vendor4++;
						}
					}
				}
			}
		}

		VendorModel vendorModel = new VendorModel();

		if (vendor1 >= vendor2 && vendor1 >= vendor3 && vendor1 > vendor4) {
			vendorModel = vendorRepository.findByVendorId(1);
		} else if (vendor2 >= vendor1 && vendor2 >= vendor3 && vendor2 > vendor4) {
			vendorModel = vendorRepository.findByVendorId(2);
		} else if (vendor3 >= vendor1 && vendor3 >= vendor2 && vendor3 > vendor4) {
			vendorModel = vendorRepository.findByVendorId(3);
		} else if (vendor4 >= vendor1 && vendor4 >= vendor2 && vendor4 > vendor3) {
			vendorModel = vendorRepository.findByVendorId(4);
		}

		System.err.println("VENDOR Name : " + vendorModel.getVendorName());

		return vendorModel;

	}

	public ResponseStructure commonErrorResponse() throws Exception {

		ResponseStructure structure = new ResponseStructure();

		structure.setErrorReferenceId("");
		structure.setData(null);
		structure.setMessage(AppConstants.HEADER_ISSUE_MESSAGE);
		structure.setErrorDiscription(AppConstants.HEADER_ISSUE_ERRORDESCRIPTION);
		structure.setFlag(7);
		structure.setStatusCode(HttpStatus.PRECONDITION_FAILED.value());
		structure.setFileName("");

		return structure;
	}

	public String surepassMechanism(JSONObject obj, MerchantPriceModel merchantPriceModel) {
		try {

			VendorPriceModel vendorPrice = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(
					merchantPriceModel.getVendorModel(), merchantPriceModel.getVendorVerificationModel());

			System.out.println("OBJ : " + obj);

			String token = AppConstants.SUREPASS_TOKEN;

			System.out.println("API KEY : " + vendorPrice.getApiLink());

			String apiEndpoint = vendorPrice.getApiLink();
			String requestBody = obj.toString();

			HttpClient httpClient = HttpClient.newBuilder().build();

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiEndpoint))
					.header("Accept", "application/json").header("Content-Type", "application/json")
					.header("Authorization", token).method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
					.build();

			System.err.println("BEFORE COMPLETABLE FUTURE");

			CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(request,
					HttpResponse.BodyHandlers.ofString());

			System.err.println("DURING COMPLETABLE FUTURE");

			try {

				HttpResponse<String> response = responseFuture.join();// Error here

				System.err.println("AFTER COMPLETABLE FUTURE");

				int statusCode = response.statusCode();
				String responseBody = response.body();

				System.out.println("Response Code: " + statusCode);

				return responseBody;

			} catch (Exception e) {
				Throwable cause = e.getCause();
				cause.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public ResponseStructure noAccessForThisVerification(EntityModel entityModel,
			VendorVerificationModel vendorVerifyModel,RequestModel model) throws Exception { //HERE

		ResponseStructure structure = new ResponseStructure();

		String verification = "this";

		if (vendorVerifyModel != null) {
			verification = vendorVerifyModel.getVerificationDocument();
		}

		JSONObject response = new JSONObject();

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		response.put("status", "failed");
		response.put("response_time", responseTime);
		response.put("error", "You don't have access for " + verification + " Verification");
		response.put("error_code", "fc_403");
		response.put("message", "No access has been granted for this verification");

		String encryptedCommonResponse = PasswordUtils.demoEncryption(response, entityModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);
		
		structure.setData(mapNew);
		structure.setFlag(5);
		structure.setMessage(AppConstants.NO_ACCESS_MESSAGE);
		structure.setStatusCode(HttpStatus.FORBIDDEN.value());
		structure.setErrorDiscription(AppConstants.NO_ACCESS_DESCRIPTION);

		model.setMessage("You don't have access for " + verification + " Verification");
		model.setError("No access has been granted for this verification");
		model.setErrorCode("fc_403");
		
		trackEveryRequest(model,entityModel,vendorVerifyModel);
		
		
		return structure;
	}

	public ResponseStructure verificationCurrentlyNotAvailable(EntityModel entityModel,
			VendorVerificationModel vendorVerifyModel,RequestModel model) throws Exception { //HERE

		ResponseStructure structure = new ResponseStructure();

		String verification = "this";

		if (vendorVerifyModel != null) {
			verification = vendorVerifyModel.getVerificationDocument();
		}

		JSONObject response = new JSONObject();

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		response.put("status", "failed");
		response.put("response_time", responseTime);
		response.put("error", verification + " verification is currently not available");
		response.put("error_code", "fc_503");
		response.put("message", "The verification you are trying to access is temporarly not available.");
		
		String encryptedCommonResponse = PasswordUtils.demoEncryption(response, entityModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);
		
		structure.setData(mapNew);
		structure.setFlag(4);
		structure.setMessage(AppConstants.NOT_AVAILABLE);
		structure.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
		structure.setErrorDiscription(AppConstants.NOT_AVAILABLE_DESCRIPTION);
		
		model.setMessage(verification + " verification is currently not available");
		model.setError("The verification you are trying to access is temporarly not available.");
		model.setErrorCode("fc_503");
		
		trackEveryRequest(model,entityModel,vendorVerifyModel);
		
		return structure;
	}

	public int FileFormatCheck(MultipartFile file, EntityModel userModel) throws Exception {

		int formatCorrect = 1;
		long twoMegaByte = 2 * 1024 * 1024;

		if (file == null || file.isEmpty()) {

			formatCorrect = 2;
			return formatCorrect;
		}

		if (file.getSize() > twoMegaByte) {

			formatCorrect = 4;
			return formatCorrect;
		}

		if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
			formatCorrect = 3;
			return formatCorrect;
		}

		return formatCorrect;
	}

	
	
	
	private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png", "image/bmp");

	public ResponseStructure fileFormatFailed(int fileFormatCheckFlag, EntityModel entityModel,
			VendorVerificationModel vendorVerify) throws Exception { //HERE

		ResponseStructure structure = new ResponseStructure();

		RequestModel reqModel = new RequestModel();
		reqModel.setSource(vendorVerify.getVerificationDocument() + " Image");
		reqModel.setSourceType("Image");
		reqModel.setRequestDateAndTime(new Date());

		JSONObject response = new JSONObject();

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		response.put("status", "failed");
		response.put("response_time", responseTime);

		if (fileFormatCheckFlag == 2) {

			response.put("error", "File is empty or missing");
			response.put("message", "File is missing");
			response.put("error_code", "fc_404");

			reqModel.setMessage("File is empty or missing");

		} else if (fileFormatCheckFlag == 3) {

			response.put("error", "File is not a supported image type");
			response.put("message", "Unsupported media-type");
			response.put("error_code", "fc_415");

			reqModel.setMessage("Unsupported media-type");

		} else if (fileFormatCheckFlag == 4) {

			response.put("error", "Image size too large");
			response.put("message", "Image size exceeds the limit, Please use image below 2MB.");
			response.put("error_code", "fc_413");

			reqModel.setMessage("Image size exceeds the limit, Please use image below 2MB.");
		}

		reqModel.setFilingStatus(false);
		reqModel.setRequestBy(entityModel.getName());
		reqModel.setStatus("failed");
		reqModel.setResponseDateAndTime(responseTime);
		reqModel.setCommonResponse(response.toString());

		duplicateUtils.setReqRespReplica(entityModel, vendorVerify, reqModel);

		String encryptedCommonResponse = PasswordUtils.demoEncryption(response, entityModel.getSecretKey());

		if (fileFormatCheckFlag == 2) {

			structure.setMessage(AppConstants.FILE_MISSING_OR_EMPTY);
			structure.setErrorDiscription(AppConstants.FILE_MISSING_OR_EMPTY);
			structure.setStatusCode(HttpStatus.NOT_FOUND.value());

		} else if (fileFormatCheckFlag == 3) {

			structure.setMessage(AppConstants.NOT_SUPPORTED);
			structure.setErrorDiscription(AppConstants.NOT_SUPPORTED);
			structure.setStatusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());

		} else if (fileFormatCheckFlag == 4) {

			structure.setMessage(AppConstants.TOO_LARGE);
			structure.setErrorDiscription(AppConstants.TOO_LARGE);
			structure.setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE.value());
		}

		structure.setData(encryptedCommonResponse);
		structure.setFlag(6);

		RequestModel model = new RequestModel();
		
		model.setMessage(structure.getMessage());
		model.setError(structure.getErrorDiscription());
		model.setErrorCode("fc_"+structure.getStatusCode());
		
		trackEveryRequest(model,entityModel,vendorVerify);
		
		return structure;
	}

	
	public ResponseStructure accountInactive(EntityModel entityModel) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		JSONObject response = new JSONObject();

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		response.put("status", "failed");
		response.put("response_time", responseTime);
		response.put("error", "Account Inactive");
		response.put("error_code", "fc_503");
		response.put("message", "Your account is currently Inactive.Please contact "+AppConstants.SUPPORT_MAIL+" to know more.");
		
		String encryptedCommonResponse = PasswordUtils.demoEncryption(response, entityModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);
		
		structure.setErrorReferenceId("");
		structure.setData(mapNew);
		structure.setMessage(AppConstants.ACCOUNT_INACTIVE_MESSAGE);
		structure.setErrorDiscription(AppConstants.ACCOUNT_INACTIVE_ERRORDESC);
		structure.setFlag(3);
		structure.setStatusCode(HttpStatus.FORBIDDEN.value());
		structure.setFileName("");

		return structure;
	}

	
	
	
	public void deductAmountForId(EntityModel userModel, MerchantPriceModel merchantPriceModel) throws Exception {

		double price = merchantPriceModel.getIdPrice();
		double gst = price * 18 / 100;

		double totalDeduction = price + gst;

		LOGGER.info("Price : "+price+"  &&  GST : "+gst);
		System.err.println("Price : "+price+"  &&  GST : "+gst);
		
		double remainingAmount = userModel.getRemainingAmount() - totalDeduction;
		double consumedAmount = userModel.getConsumedAmount() + totalDeduction;

		LOGGER.info("Remaining amount 1 : "+userModel.getRemainingAmount());
		LOGGER.info("Remaining amount 2 : "+remainingAmount);
		LOGGER.info("Consumed           : "+consumedAmount);
		
		System.err.println("Remaining amount 1 : "+userModel.getRemainingAmount());
		System.err.println("Remaining amount 2 : "+remainingAmount);
		System.err.println("Consumed           : "+consumedAmount);
		
		userModel.setRemainingAmount(fu.twoDecimelDouble(remainingAmount));
		userModel.setConsumedAmount(fu.twoDecimelDouble(consumedAmount));
		userModel.setMonthlyGst(fu.twoDecimelDouble(userModel.getMonthlyGst() + gst));
		userModel.setPaymentStatus("No Dues");
		
		RequestModel rm = new RequestModel();
		
		rm.setDebit(price);
		rm.setCredit(0);
		rm.setRemark("Debit");
		rm.setDebitGst(gst);
		rm.setClosingBalance(remainingAmount);
		rm.setConsumedBalance(consumedAmount);
		rm.setService(merchantPriceModel.getVendorVerificationModel().getVerificationDocument());
		
		prepaidUserStatementService.statementEntry(userModel, rm);
	}
	

	public void deductAmountForOcr(EntityModel userModel, MerchantPriceModel merchantPriceModel) throws Exception {

		double price = merchantPriceModel.getImagePrice();
		double gst = price * 18 / 100;

		double totalDeduction = price + gst;

		double remainingAmount = userModel.getRemainingAmount() - totalDeduction;
		double consumedAmount = userModel.getConsumedAmount() + totalDeduction;

		userModel.setRemainingAmount(fu.twoDecimelDouble(remainingAmount));
		userModel.setConsumedAmount(fu.twoDecimelDouble(consumedAmount));
		userModel.setMonthlyGst(fu.twoDecimelDouble(userModel.getMonthlyGst() + gst));
		userModel.setPaymentStatus("No Dues");
		
		RequestModel rm = new RequestModel();
		
		rm.setDebit(price);
		rm.setCredit(0);
		rm.setRemark("Debit");
		rm.setDebitGst(gst);
		rm.setClosingBalance(remainingAmount);
		rm.setConsumedBalance(consumedAmount);
		rm.setService(merchantPriceModel.getVendorVerificationModel().getVerificationDocument());
		
		prepaidUserStatementService.statementEntry(userModel, rm);
	}

	public void deductAmountForSignature(EntityModel userModel, MerchantPriceModel merchantPriceModel,double bondPrice)
			throws Exception {

		double price = merchantPriceModel.getSignaturePrice()+bondPrice;
		double gst = price * 18 / 100;

		double totalDeduction = price + gst;

		double remainingAmount = userModel.getRemainingAmount() - totalDeduction;
		double consumedAmount = userModel.getConsumedAmount() + totalDeduction;

		userModel.setRemainingAmount(fu.twoDecimelDouble(remainingAmount));
		userModel.setConsumedAmount(fu.twoDecimelDouble(consumedAmount));
		userModel.setMonthlyGst(fu.twoDecimelDouble(userModel.getMonthlyGst() + gst));
		userModel.setPaymentStatus("No Dues");
		
		RequestModel rm = new RequestModel();
		
		rm.setDebit(price);
		rm.setCredit(0);
		rm.setRemark("Debit");
		rm.setDebitGst(gst);
		rm.setClosingBalance(remainingAmount);
		rm.setConsumedBalance(consumedAmount);
		rm.setService(merchantPriceModel.getVendorVerificationModel().getVerificationDocument());
		
		prepaidUserStatementService.statementEntry(userModel, rm);
	}

	
	
	
	public ResponseStructure notAccepted(EntityModel entityModel, VendorVerificationModel vendorVerifyModel,RequestModel model)
			throws Exception { //HERE

		ResponseStructure structure = new ResponseStructure();

		String verification = "this";

		if (vendorVerifyModel != null) {
			verification = vendorVerifyModel.getVerificationDocument();
		}

		JSONObject response = new JSONObject();

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		response.put("status", "failed");
		response.put("response_time", responseTime);
		response.put("error", "You have not accepted the latest price updation for " + verification);
		response.put("error_code", "fc_407");
		response.put("message", "Need to accept the latest price updation");

		String encryptedCommonResponse = PasswordUtils.demoEncryption(response, entityModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);
		
		structure.setData(mapNew);
		structure.setFlag(6);
		structure.setMessage("Need to accept the latest price updation");
		structure.setStatusCode(HttpStatus.FORBIDDEN.value());
		structure.setErrorDiscription("You have not accepted the latest price updation for " + verification);

		model.setMessage("You have not accepted the latest price updation for " + verification);
		model.setError("Need to accept the latest price updation");
		model.setErrorCode("fc_407");
		
		trackEveryRequest(model,entityModel,vendorVerifyModel);
		
		
		return structure;
	}

	public void postpaidConsumedAmount(EntityModel userModel, MerchantPriceModel merchantPriceModel) throws Exception {

		double price = merchantPriceModel.getIdPrice();
		double gst = price * 18 / 100;

		double totalDeduction = price + gst;

		double consumedAmount = userModel.getConsumedAmount() + totalDeduction;

		userModel.setConsumedAmount(fu.twoDecimelDouble(consumedAmount));
		userModel.setMonthlyGst(fu.twoDecimelDouble(userModel.getMonthlyGst() + gst));
		
		RequestModel rm = new RequestModel();
		
		rm.setDebit(price);
		rm.setCredit(0);
		rm.setRemark("Debit");
		rm.setDebitGst(gst);
		rm.setCreditGst(0);
		rm.setConsumedBalance(consumedAmount);
		rm.setService(merchantPriceModel.getVendorVerificationModel().getVerificationDocument());
		
		postpaidUserStatementService.statementEntry(userModel, rm);
	}

	public void postpaidConsumedAmountForOcr(EntityModel userModel, MerchantPriceModel merchantPriceModel)
			throws Exception {

		double price = merchantPriceModel.getImagePrice();
		double gst = price * 18 / 100;

		double totalDeduction = price + gst;

		double consumedAmount = userModel.getConsumedAmount() + totalDeduction;

		userModel.setConsumedAmount(fu.twoDecimelDouble(consumedAmount));
		userModel.setMonthlyGst(fu.twoDecimelDouble(userModel.getMonthlyGst() + gst));
		
		RequestModel rm = new RequestModel();
		
		rm.setDebit(price);
		rm.setCredit(0);
		rm.setRemark("Debit");
		rm.setDebitGst(gst);
		rm.setCreditGst(0);
		rm.setConsumedBalance(consumedAmount);
		rm.setService(merchantPriceModel.getVendorVerificationModel().getVerificationDocument());
		
		postpaidUserStatementService.statementEntry(userModel, rm);
	}

	
	public void postpaidConsumedAmountForSignature(EntityModel userModel, MerchantPriceModel merchantPriceModel,double bondAmount)
			throws Exception {

		double price = merchantPriceModel.getSignaturePrice()+bondAmount;
		double gst = price * 18 / 100;

		double totalDeduction = price + gst;

		double consumedAmount = userModel.getConsumedAmount() + totalDeduction;

		userModel.setConsumedAmount(fu.twoDecimelDouble(consumedAmount));
		userModel.setMonthlyGst(fu.twoDecimelDouble(userModel.getMonthlyGst() + gst));
		
		RequestModel rm = new RequestModel();
		
		rm.setDebit(price);
		rm.setCredit(0);
		rm.setRemark("Debit");
		rm.setDebitGst(gst);
		rm.setCreditGst(0);
		rm.setConsumedBalance(consumedAmount);
		rm.setService(merchantPriceModel.getVendorVerificationModel().getVerificationDocument());
		
		postpaidUserStatementService.statementEntry(userModel, rm);
	}

	public boolean signDeskError(String errorCode) throws Exception{

		if(Arrays.asList(signDeskErrorCodes).contains(errorCode)) {
			
			return false;
		}else {
			
			return true;
		}
	}

	
	public void errorCodes(int errorCode , VendorModel vendor ,EntityModel entity,VendorVerificationModel verification) throws Exception{

		ArrayList<Integer> errorCodes = new ArrayList<>();
		errorCodes.add(401);
		errorCodes.add(403);
		errorCodes.add(500);
		
		if(errorCodes.contains(errorCode)) {
			
			String errorMessage = getErrorMessage(errorCode,vendor);
			
			VendorSideIssues issue = new VendorSideIssues();
			issue.setStatusCode(errorCode);
			issue.setMessage(errorMessage);
			issue.setDate(new Date());
			issue.setVendorModel(vendor);
	          
	        vendorSideIssuesRepository.save(issue);
			
			throw new FacheckSideException("An issue occured at server-side", errorCode, issue,entity,verification);
		}
		
	}

	private String getErrorMessage(int errorCode , VendorModel vendor) throws Exception{
		
		String message ="";
		
		if(errorCode==401 || errorCode==403) {
			
			message = "The request to the "+vendor.getVendorName()+" server failed.";
			
		}else if(errorCode==500) {
			
			message = "Internal server error from "+vendor.getVendorName();
		}
		
		return message;
	}

	
	
	
	
	public void signDeskErrorCodes(String errorCode, VendorModel vendor,EntityModel entity,VendorVerificationModel verification) throws Exception{
		
		if(Arrays.asList(signDeskErrorCodes).contains(errorCode)) {
			
			String errorMessage = getSignDeskErrorMessage(errorCode,vendor);
			
			VendorSideIssues issue = new VendorSideIssues();
			issue.setErrorCode(errorCode);
			issue.setMessage(errorMessage);
			issue.setDate(new Date());
			issue.setVendorModel(vendor);
	          
	        vendorSideIssuesRepository.save(issue);
			
	        if(!Arrays.asList(messageFilteration).contains(errorCode)) {
	        	errorMessage = "An issue occured at server-side";
	        }
	        
	        
			throw new FacheckSideException(errorMessage,521,errorCode,issue,entity,verification);
		}
	}

	
	private String getSignDeskErrorMessage(String errorCode, VendorModel vendorModel) throws Exception{
		
		String message ="";
		
		if(errorCode.equalsIgnoreCase("kyc_001")) {
			
			message="x-parse-application-id and x-parse-rest-api-key do not match";
			
		}else if(errorCode.equalsIgnoreCase("kyc_002")) {
			
			message="The Public key was not found in the request";
			
		}else if(errorCode.equalsIgnoreCase("kyc_003")) {
			
			message="The enc_mode used in the request is invalid";
			
		}else if(errorCode.equalsIgnoreCase("kyc_004")) {
			
			message="The x-parse-application-id && x-parse-rest-api-key was not found in the request";
			
		}else if(errorCode.equalsIgnoreCase("kyc_005")) {
			
			message="The enc_mode was not found in the request";
			
		}else if(errorCode.equalsIgnoreCase("kyc_006")) {
			
			message="The x-parse-application-id was not found in the request";
			
		}else if(errorCode.equalsIgnoreCase("kyc_007")) {
			
			message="The x-parse-rest-api-key was not found in the request";
			
		}else if(errorCode.equalsIgnoreCase("kyc_008")) {
			
			message="The reference_id used in the request is invalid";
			
		}else if(errorCode.equalsIgnoreCase("kyc_009")) {
			
			message="The source_type used in the request is invalid";
			
		}else if(errorCode.equalsIgnoreCase("kyc_011")) {
			
			message="The source_type was not found in the request";
			
		}else if(errorCode.equalsIgnoreCase("kyc_013")) {
			
			message="The document verification services are not configured for this organization.";
			
		}else if(errorCode.equalsIgnoreCase("kyc_014")) {
			
			message="The document verification APIs are not configured for this organization";
			
		}else if(errorCode.equalsIgnoreCase("kyc_015")) {
			
			message="The reference_id was not found in the request";
			
		}else if(errorCode.equalsIgnoreCase("kyc_016")) {
			
			message="The documents could not be processed";
			
		}else if(errorCode.equalsIgnoreCase("kyc_017")) {
			
			message="Unknown error";
			
		}else if(errorCode.equalsIgnoreCase("kyc_023")) {
			
			message="The Customer data was not found";
			
		}else if(errorCode.equalsIgnoreCase("kyc_024")) {
			
			message="The reference_id used in the request should be unique";
		}
		
		return message;
	}

	
	
	public void setAadhaarOtpReqResponse(Request request, Response response, MerchantPriceModel merchantPriceModel, String statusResponse, String message) throws Exception{
		
		Request  newReq  = new Request();
		Response newResp = new Response();

		newReq.setIpAddress(ipAndLocation.publicIpAddress());
		newReq.setSource(request.getSource());
		newReq.setSourceType(request.getSourceType());
		newReq.setRequestBy(request.getRequestBy());
		newReq.setRequestDateAndTime(new Date());
		newReq.setStatus(statusResponse);
		newReq.setResponseDateAndTime(new Date().toString());
		newReq.setUser(request.getUser());
		newReq.setVerificationModel(merchantPriceModel.getVendorVerificationModel());
		newReq.setPrice(merchantPriceModel.getIdPrice());
		newReq.setFreeHit(request.isFreeHit());
		newReq.setConsider(true);
		newReq.setMessage(message);
		newReq.setTransactionId(request.getTransactionId());
		newReq.setAttempt(0);
		newReq.setReferenceId(FileUtils.getRandomOTPnumber(8));
		newReq.setErrorCode(request.getErrorCode());
		newReq.setAttempt(0);

		reqRepository.save(newReq);
		
		newResp.setSource(response.getSource());
		newResp.setSourceType(response.getSourceType());
		newResp.setRequestBy(response.getRequestBy());
		newResp.setRequestDateAndTime(new Date());
		newResp.setStatus(statusResponse);
		newResp.setResponseDateAndTime(new Date().toString());
		newResp.setRequest(request);
		newResp.setUser(merchantPriceModel.getEntityModel());
		newResp.setVendorModel(merchantPriceModel.getVendorModel());
		newResp.setResponse(response.getResponse());
		newResp.setVerificationModel(merchantPriceModel.getVendorVerificationModel());
		newResp.setTransactionId(newReq.getTransactionId());
		newResp.setMessage(message);
		newResp.setReferenceId(newReq.getReferenceId());
		newResp.setRequest(newReq);
	
		respRepository.save(newResp);
	}
	
	
	
	
	public void trackEveryRequest(RequestModel model,EntityModel entityModel,VendorVerificationModel vendorVerificationModel) throws Exception{
		
		Request req = new Request();
		
		req.setSource(model.getSource());
		req.setSourceType(model.getSourceType());
		req.setMessage(model.getMessage());
		req.setErrorCode(model.getErrorCode());
		req.setError(model.getError());
		req.setMerchant(model.getMerchantDoc());
		req.setReferenceId(model.getReferenceId());;
		req.setVerificationModel(vendorVerificationModel);
		
		req.setRequestBy(entityModel.getName());
		req.setUser(entityModel);
		
		req.setIpAddress(ipAndLocation.publicIpAddress());
		req.setRequestDateAndTime(new Date());
		req.setStatus("aborted");
		req.setResponseDateAndTime(new Date().toString());
		req.setPrice(0);
		req.setFreeHit(false);
		req.setConsider(true);
		
		reqRepository.save(req);
	}
	
	
}
