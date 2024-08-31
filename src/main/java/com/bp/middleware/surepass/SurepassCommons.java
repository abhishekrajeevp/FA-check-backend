package com.bp.middleware.surepass;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.erroridentifier.ErrorIdentifierRepository;
import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.smartrouteverification.SmartRouteUtils;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.PasswordUtils;
import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;
//import com.mashape.unirest.http.HttpResponse;
//import com.mashape.unirest.http.Unirest;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

@Component
public class SurepassCommons {

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
	private ErrorIdentifierRepository errorIdentifierRepository;
	@Autowired
	FileUtils fu;
	@Autowired
	SmartRouteUtils smartRouteUtils;

//	public String ocrMechanisms(EntityModel entityModel, MerchantPriceModel merchantPriceModel, MultipartFile file)
//			throws Exception {
//
//		VendorPriceModel vendorPriceModel = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(
//				merchantPriceModel.getVendorModel(), merchantPriceModel.getVendorVerificationModel());
//
//		String token = AppConstants.SUREPASS_TOKEN;
//		String url = vendorPriceModel.getApiLink();
//
//		HttpResponse<String> response = Unirest.post(url).header("Authorization", token).body("file").asString();
//		
//		return response.getBody();
//
//	}

	public Response sourceCheck(String source, EntityModel user, MerchantPriceModel merchantPriceModel)
			throws Exception {

		System.err.println("NO SOURCE : " + (merchantPriceModel.isNoSourceCheck()));

		if (merchantPriceModel.isNoSourceCheck()) {

			System.err.println("NO SOURCE IN");
			return new Response();
		}

		System.err.println("NO SOURCE OUT");
		
		VendorVerificationModel vendorVerify = merchantPriceModel.getVendorVerificationModel();

		List<Response> sourceList = respRepository.findBySourceAndVerificationModel(source,vendorVerify);
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
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !dto.isFreeHit()) {

			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else if(!dto.isFreeHit()) {
			
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

//	public  ResponseStructure commonErrorResponse(EntityModel entityModel) {
//		try {
//
//			ResponseStructure structure = new ResponseStructure();
//
//			JSONObject response = new JSONObject();
//
//			LocalDateTime dateTime = LocalDateTime.now();
//			DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
//			String responseTime = dateTime.format(format);
//
//			response.put("status", "failed");
//			response.put("response_time", responseTime);
//			response.put("error", "api key or application id not found");
//			response.put("error_code", "fc_301");
//			response.put("message", "Verification failed");
//
//			String encryptedCommonResponse = PasswordUtils.demoEncryption(response, entityModel.getSecretKey());
//
//			structure.setData(encryptedCommonResponse);
//			structure.setFlag(5);
//			structure.setMessage("Key mis-match");
//
//			return structure;
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}

	public String surepassVerification(JSONObject obj, VendorPriceModel vendorPrice) {

		try {
	
			System.out.println("OBJ : "+obj);
			
			String token = AppConstants.SUREPASS_TOKEN;

			String apiEndpoint = vendorPrice.getApiLink();
			String requestBody = obj.toString();

			HttpClient httpClient = HttpClient.newBuilder().build();

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiEndpoint))
					.header("Accept", "application/json").header("Content-Type", "application/json")
					.header("Authorization", token).method("POST", HttpRequest.BodyPublishers.ofString(requestBody)).build();

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

}
