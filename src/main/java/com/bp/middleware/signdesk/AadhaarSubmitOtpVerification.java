package com.bp.middleware.signdesk;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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

import com.bp.middleware.erroridentifier.ErrorIdentifierRepository;
import com.bp.middleware.erroridentifier.ErrorIdentifierService;
import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.payment.PaymentRepository;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.smartrouteverification.CommonResponseStructure;
import com.bp.middleware.smartrouteverification.SmartRouteUtils;
import com.bp.middleware.surepass.SurepassCommons;
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
public class AadhaarSubmitOtpVerification {

	
	private final String VERIFICATION_TYPE = AppConstants.AADHAR_OTP_VERIFY;
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
	private PaymentRepository paymentRepository;
	@Autowired
	private PostpaidRepository postpaidRepository;
	@Autowired
	private SurepassCommons surepassCommons;
	@Autowired
	private SmartRouteUtils smartRouteUtils;
	@Autowired
	private ErrorIdentifierService errorIdentifierService;
	@Autowired
	private  CommonResponseStructure CommonResponseStructure;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;
	
	public ResponseStructure aadhaarSubmitOtpService(RequestModel model ,HttpServletRequest servletRequest) {
		
		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			if (userModel != null && userModel.isAccountStatus()) {

				ENTITY=userModel;
				
				VendorVerificationModel verificationModel = verificationRepository
						.findByVerificationDocument(AppConstants.AADHAR_OTP_VERIFY);
				VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SIGN_DESK_VENDOR);
				
				MerchantPriceModel merchantPriceModel = merchantPriceRepository
						.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
								userModel,true);
				
				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.getByVendorModelAndVendorVerificationModelAndStatus(vendorModel, verificationModel,true);

				if (merchantPriceModel != null && vendorPriceModel!=null && merchantPriceModel.isAccepted()) {

					ResponseStructure balance = smartRouteUtils.balanceCheck(userModel, merchantPriceModel,verificationModel);

					if (balance.getFlag() == 1) {
						String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
								userModel.getSecretKey());
						JSONObject userJson = new JSONObject(userDecryption);

						String referenceNumber = FileUtils.getRandomOTPnumber(10);
						userJson.put("reference_id", referenceNumber);
						userJson.put("source_type", "id");

						String source = userJson.getString("source");
						String referenceId = userJson.getString("reference_id");

						Response responseModel = respRepository.findByReferenceId(referenceId);
						Request requestModel = reqRepository.findByReferenceId(referenceId);

						System.err.println("SOURCE : " + source);

							return signDeskSubmitaadhaarOtpVerification(userJson, model, userModel, verificationModel, vendorModel,
									merchantPriceModel, vendorPriceModel,requestModel,responseModel);
						
					} else {
						return balance;
					}
				}else {
					
					if(vendorPriceModel == null) {
						return smartRouteUtils.verificationCurrentlyNotAvailable(userModel, verificationModel,model);
					}else if(merchantPriceModel==null){
						return smartRouteUtils.noAccessForThisVerification(userModel, verificationModel,model);
					}else {
						return smartRouteUtils.notAccepted(userModel, verificationModel,model);
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

	public ResponseStructure signDeskSubmitaadhaarOtpVerification(JSONObject userJson, RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPrice, Request requestModel, Response responseModel) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String referenceId = userJson.getString("reference_id");
		String transactionId = responseModel.getTransactionId();// userJson.getString("transaction_id");
		String otp = userJson.getString("otp");

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

		Request request = requestModel;
		Response response = responseModel;

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setStatus(statusResponse);
		request.setOtp(otp);
		request.setPrice(request.getPrice()+merchantPriceModel.getIdPrice());

		response.setStatus(statusResponse);

		JSONObject object = new JSONObject();
		object.put("status", status);
		object.put("encrypted_response", decryptData);

		String returnResponse = PasswordUtils.demoEncryption(object, userModel.getSecretKey());
		System.out.println("RETURN RESPONSE : " + returnResponse);

		System.out.println("Status : " + status);
		System.out.println("Status Response :" + statusResponse);

		RequestModel temporary = new RequestModel();

		temporary.setStatus(statusResponse);
		temporary.setResponseDateAndTime(responseTime);
		temporary.setReferenceId(referenceId);
		temporary.setVendorModel(vendorModel);

		if (statusResponse.equalsIgnoreCase("success")) {

			JSONObject resultJson = decryptJson.getJSONObject("result");
			JSONObject validatedData = resultJson.getJSONObject("validated_data");

			String message = decryptJson.getString("message");
			String transaction = decryptJson.getString("transaction_id");

			String fullName = validatedData.getString("full_name");
			String aadharNumber = validatedData.getString("aadhaar_number");
			String dob = validatedData.getString("dob");
			String gender = validatedData.getString("gender");
			String address = validatedData.getJSONObject("address").toString();
			boolean mobileVerified = validatedData.getBoolean("mobile_verified");

			String zip = validatedData.getString("zip");
			String mobileHash = validatedData.getString("mobile_hash");
			String emailHash = validatedData.getString("email_hash");
			String rawXml = validatedData.getString("raw_xml");
			String zipData = validatedData.getString("zip_data");
			String careOf = validatedData.getString("care_of");
			String shareCode = validatedData.getString("share_code");
			String aadharReferenceId = validatedData.getString("reference_id");
			String aadharStatus = validatedData.getString("status");
			String uniquenessId = validatedData.getString("uniqueness_id");
			boolean faceStatus = validatedData.getBoolean("face_status");
			boolean hasImage = validatedData.getBoolean("has_image");
			int faceScore = validatedData.getInt("face_score");

			temporary.setFullName(fullName);
			temporary.setAadhaarNumber(aadharNumber);
			temporary.setDob(dob);
			temporary.setMessage(message);
			temporary.setTransactionId(transactionId);
			temporary.setAddress(address);
			temporary.setGender(gender);
			temporary.setMobileVerified(mobileVerified);
			temporary.setZip(zip);
			temporary.setMobileHash(mobileHash);
			temporary.setEmailHash(emailHash);
			temporary.setRawXml(rawXml);
			temporary.setZipData(zipData);
			temporary.setCareOf(careOf);
			temporary.setShareCode(shareCode);
			temporary.setAadharReferenceId(aadharReferenceId);
			temporary.setAadharStatus(aadharStatus);
			temporary.setUniquenessId(uniquenessId);
			temporary.setFaceStatus(faceStatus);
			temporary.setHasImage(hasImage);
			temporary.setFaceScore(faceScore);

			request.setTransactionId(transaction);
			request.setMessage(message);
			request.setResponseDateAndTime(responseTime);

			response.setTransactionId(transaction);
			response.setMessage(message);
			response.setResponseDateAndTime(responseTime);
			response.setRequest(request);

		} else {

			// String error = decryptJson.getString("error");
			String errorCode = decryptJson.getString("error_code");

			temporary.setErrorCode(errorCode);

			request.setResponseDateAndTime(responseTime);
			// request.setError(error);
			request.setErrorCode(errorCode);

			response.setResponse(decryptData);
			// response.setError(error);
			response.setErrorCode(errorCode);
			response.setResponseDateAndTime(responseTime);
			response.setRequest(request);

		}

		JSONObject commonResponse = CommonResponseStructure.commonResponseAadharOtpSubmit(temporary);
		
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !model.isFreeHit()) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else if (!model.isFreeHit()) {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
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
		
		vendorRepository.save(vendorModel);
		userRepository.save(userModel);
		

		return structure;

	}
}
