package com.bp.middleware.surepass;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.erroridentifier.ErrorIdentifierRepository;
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
public class AadhaarSubmitOtpService {

	
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
	private SurepassCommons surepassCommons;
	@Autowired
	private SmartRouteUtils smartRouteUtils;
	@Autowired
	private ErrorIdentifierService errorIdentifierService;
	@Autowired
	private  CommonResponseStructure CommonResponseStructure;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;

	public ResponseStructure aadhaarOtpSubmit(RequestModel model, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			if (userModel != null && userModel.isAccountStatus()) {
				
				ENTITY=userModel;

				VendorVerificationModel verificationModel = verificationRepository
						.findByVerificationDocument(AppConstants.AADHAR_OTP_VERIFY);
				VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SUREPASS_VENDOR);
				
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

						userJson.put("source_type", "id");

						return surepassAadharOtpSubmit(model, userModel, verificationModel, vendorModel,
								merchantPriceModel, vendorPriceModel, userJson);

					} else{
						return balance;
					}
				} else {

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

	private ResponseStructure surepassAadharOtpSubmit(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {
		ResponseStructure structure = new ResponseStructure();

		String referenceNumber = userJson.getString("reference_id");// Contains client_id for the time being
		String otp = userJson.getString("otp");

		Response responseModel = respRepository.findByReferenceId(referenceNumber);
		Request requestModel = reqRepository.findByReferenceId(referenceNumber);

		JSONObject obj = new JSONObject();
		obj.put("otp", otp);
		obj.put("client_id", responseModel.getClientId());

		Request request = requestModel;

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
		
		smartRouteUtils.errorCodes(statusCodeNumber, vendorModel,userModel,verificationModel);
		
		String message =null;
		if(status) {
			message = wholeData.getString("message_code");
		}else {
			message = wholeData.getString("message");
		}
		
		
		Response response = responseModel;
	    LocalDateTime dateTime=LocalDateTime.now();
	    DateTimeFormatter format=DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	    String responseTime=dateTime.format(format);

	    String ipAddress = ipAndLocation.publicIpAddress();
		
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setPrice(request.getPrice()+merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(responseTime);
		request.setMessage(message);
		request.setFreeHit(model.isFreeHit());
		request.setConsider(true);

		response.setMessage(message);
		response.setRequestDateAndTime(new Date());
		response.setUser(userModel);
		response.setRequest(request);
		response.setVendorModel(vendorModel);
		response.setResponseDateAndTime(responseTime);
		response.setResponse(surepassResponse);

        RequestModel temporary = new RequestModel();
        
		temporary.setResponseDateAndTime(responseTime);
		temporary.setMessage(message);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setVendorModel(vendorModel);

		if (status) {

			temporary.setStatus("success");

			String referenceId = FileUtils.getRandomOTPnumber(10);

			JSONObject internalData = wholeData.getJSONObject("data");

			String clientId = internalData.getString("client_id");
			String fullName = internalData.getString("full_name");
			String aadharNumber = internalData.getString("aadhaar_number");
			String dob = internalData.getString("dob");
			String gender = internalData.getString("gender");
			String address = internalData.getJSONObject("address").toString();
			boolean mobileVerified = internalData.getBoolean("mobile_verified");
			
			String zip = internalData.getString("zip");
			String mobileHash = internalData.getString("mobile_hash");
			String emailHash = internalData.getString("email_hash");
			String rawXml = internalData.getString("raw_xml");
			String zipData = internalData.getString("zip_data");
			String careOf = internalData.getString("care_of");
			String shareCode = internalData.getString("share_code");
			String aadharReferenceId = internalData.getString("reference_id");
			String aadharStatus = internalData.getString("status");
			String uniquenessId = internalData.getString("uniqueness_id");
			boolean faceStatus = internalData.getBoolean("face_status");
			boolean hasImage = internalData.getBoolean("has_image");
			int faceScore = internalData.getInt("face_score");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setStatus("success");
			response.setReferenceId(referenceId);
			
			temporary.setFullName(fullName);
			temporary.setAadhaarNumber(aadharNumber);
			temporary.setDob(dob);
			temporary.setMessage(message);
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
			temporary.setReferenceId(referenceId);

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setReferenceId(referenceNumber);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setReferenceId(referenceNumber);
			response.setError("error");
			response.setStatus("failed");
			
			temporary.setStatus("failed");
			temporary.setReferenceId(referenceNumber);
		}
		
		JSONObject commonResponse = CommonResponseStructure.commonResponseAadharOtpSubmit(temporary);
		
		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && statusCodeNumber != 401 && statusCodeNumber != 403 && !model.isFreeHit()) {

			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
			
		}else if(statusCodeNumber!=401 && statusCodeNumber!=403 && !model.isFreeHit()) {
			
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

}
