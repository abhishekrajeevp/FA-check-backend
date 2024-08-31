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
public class DrivingLicenseVerificationSignDesk {

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
	
	public ResponseStructure dlVerificationService(RequestModel model ,HttpServletRequest servletRequest) {
		
		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			if (userModel != null && userModel.isAccountStatus()) {

				ENTITY=userModel;
				
				VendorVerificationModel verificationModel = verificationRepository
						.findByVerificationDocument(AppConstants.DL_VERIFY);
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

						System.err.println("SOURCE : " + source);

						Response sourceResponse = surepassCommons.sourceCheck(source, userModel, merchantPriceModel);
						System.err.println("Source Response : " + sourceResponse);

						if (sourceResponse.getResponseId() > 0) {
							System.err.println("SOURCE PRESENT");

							return surepassCommons.setRequest(sourceResponse, model, merchantPriceModel, userModel,
									verificationModel);

						} else {

							return signDeskDlVerification(userJson, model, userModel, verificationModel, vendorModel,
									merchantPriceModel, vendorPriceModel);
						}
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

	public ResponseStructure signDeskDlVerification(JSONObject userJson, RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPrice) throws Exception {

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

		System.err.println("RESPONSE :  " + decryptData);

		JSONObject decryptJson = new JSONObject(decryptData);

		String status = decryptJson.getString("status");
		String responseTimeStamp = decryptJson.getString("response_time_stamp");
		String referenceId = decryptJson.getString("reference_id");
		String message = decryptJson.getString("message");

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && !model.isFreeHit()) {
			
			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else if (!model.isFreeHit()) {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

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
			String errorCode = decryptJson.getString("error_code");

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

		JSONObject commonResponse = CommonResponseStructure.commonResponseDrivingLicense(temporary);
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
}
