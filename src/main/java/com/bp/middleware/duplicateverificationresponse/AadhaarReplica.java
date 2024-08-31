package com.bp.middleware.duplicateverificationresponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.PasswordUtils;
import com.bp.middleware.vendors.VendorVerificationModel;

@Component
public class AadhaarReplica {

	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure generateOtpDuplicate(RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
		JSONObject userJson = new JSONObject(userDecryption);

		String source = userJson.getString("source");

		RequestModel reqModel = new RequestModel();

		reqModel.setSource(source);
		reqModel.setSourceType("ID");
		reqModel.setRequestDateAndTime(new Date());
		reqModel.setFilingStatus(false);
		reqModel.setRequestBy(userModel.getName());

		Pattern pattern = Pattern.compile(AppConstants.AADHAAR_PATTERN);
		Matcher matcher = pattern.matcher(source);

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String referenceId = FileUtils.getRandomOTPnumber(10);

		JSONObject response = new JSONObject();

		response.put("response_time", responseTime);
		response.put("reference_id", referenceId);

		if (matcher.matches()) {

			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("Success");
			reqModel.setMessage("Success");

			response.put("status", "success");
			response.put("messsage",
					"Please provide a random 6-digit number as OTP for the next API.However please note that no OTP will be received as this is a test case.");
			response.put("otp_generated", true);

		} else {

			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("failed");
			reqModel.setMessage(AppConstants.DUMMY_FAILED_MESSAGE);

			response.put("otp_generated", false);
			response.put("status", "failed");
			response.put("messsage", AppConstants.DUMMY_FAILED_MESSAGE);
			response.put("error", AppConstants.DUMMY_ERROR_MESSAGE);
		}

		reqModel.setCommonResponse(response.toString());

		duplicateUtils.setReqRespReplica(userModel, vendorVerifyModel, reqModel);

		String encryptedCommonResponse = PasswordUtils.demoEncryption(response, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);

		return structure;
	}

	public ResponseStructure aadhaarSubmitOtpDuplicate(RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel) throws Exception {
		ResponseStructure structure = new ResponseStructure();

		String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
		JSONObject userJson = new JSONObject(userDecryption);

		String source = userJson.getString("otp");
		
		RequestModel reqModel = new RequestModel();

		reqModel.setSource(source);
		reqModel.setSourceType("ID");
		reqModel.setRequestDateAndTime(new Date());
		reqModel.setFilingStatus(false);
		reqModel.setRequestBy(userModel.getName());

		Pattern pattern = Pattern.compile(AppConstants.AADHAAR_OTP_PATTERN);
		Matcher matcher = pattern.matcher(source);

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String referenceId = FileUtils.getRandomOTPnumber(10);

		JSONObject response = new JSONObject();

		response.put("response_time", responseTime);
		response.put("reference_id", referenceId);

		if (matcher.matches()) {
			
			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("Success");
			reqModel.setMessage("Success");

			response.put("status", "success");
			response.put("messsage", AppConstants.DUMMY_SUCCESS_MESSAGE);

			JSONObject validatedData = new JSONObject();
			
			JSONObject address = new JSONObject();
					
			address.put("country", "India");
			address.put("loc", "");
			address.put("subdist", "");
			address.put("vtc", "vtc");
			address.put("street", "street");
			address.put("dist", "Kancheepuram");
			address.put("state", "Tamil Nadu");
			address.put("landmark", "");
			address.put("house", "11/143");
			address.put("po", "");

			validatedData.put("full_name", "Dummy name");
			validatedData.put("aadhaar_number", "634567789447");
			validatedData.put("dob", "1945-12-12");
			validatedData.put("address", address.toString());
			validatedData.put("gender", "Male");
			validatedData.put("mobile_verified", true);

			validatedData.put("face_status", false);
			validatedData.put("face_score", -1);
			validatedData.put("zip", "");
			validatedData.put("has_image", true);
			validatedData.put("mobile_hash", "");
			validatedData.put("email_hash", "");
			validatedData.put("raw_xml", "");
			validatedData.put("zip_data", "");
			validatedData.put("care_of", "S/O: Rahul Das");
			validatedData.put("share_code", "4444");
			validatedData.put("reference_id", "028120231122140038948");
			validatedData.put("status", "success_aadhaar");
			validatedData.put("uniqueness_id", "d58ed84adfb1e287974b38f6b1e01a421c87447708dc4bd550425b493118becc");

			response.put("validated_data", validatedData);

		} else {
			
			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("failed");
			reqModel.setMessage("Invalid OTP");

			response.put("status", "failed");
			response.put("messsage", "Invalid OTP");
			response.put("error", AppConstants.DUMMY_ERROR_MESSAGE);
		}

		reqModel.setCommonResponse(response.toString());
		
		duplicateUtils.setReqRespReplica(userModel, vendorVerifyModel, reqModel);

		String encryptedCommonResponse = PasswordUtils.demoEncryption(response, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);

		return structure;
	}

	public ResponseStructure aadhaarDirectDuplicate(RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
		JSONObject userJson = new JSONObject(userDecryption);

		String source = userJson.getString("source");

		RequestModel reqModel = new RequestModel();

		reqModel.setSource(source);
		reqModel.setSourceType("ID");
		reqModel.setRequestDateAndTime(new Date());
		reqModel.setFilingStatus(false);
		reqModel.setRequestBy(userModel.getName());

		Pattern pattern = Pattern.compile(AppConstants.AADHAAR_PATTERN);
		Matcher matcher = pattern.matcher(source);

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String referenceId = FileUtils.getRandomOTPnumber(10);

		JSONObject response = new JSONObject();

		response.put("response_time", responseTime);
		response.put("reference_id", referenceId);

		if (matcher.matches()) {

			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("Success");
			reqModel.setMessage("Success");

			response.put("status", "success");
			response.put("messsage", AppConstants.DUMMY_SUCCESS_MESSAGE);

			JSONObject validatedData = new JSONObject();

			validatedData.put("aadhaar_number", source);
			validatedData.put("age_range", "20-30");
			validatedData.put("state", "UTTAR PRADESH");
			validatedData.put("gender", "Male");
			validatedData.put("last_digits", "700");
			validatedData.put("remarks", "");
			validatedData.put("is_mobile", false);
			validatedData.put("less_info", false);

			response.put("validated_data", validatedData);

		} else {

			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("failed");
			reqModel.setMessage(AppConstants.DUMMY_FAILED_MESSAGE);

			response.put("status", "failed");
			response.put("messsage", AppConstants.DUMMY_FAILED_MESSAGE);
			response.put("error", AppConstants.DUMMY_ERROR_MESSAGE);
		}

		reqModel.setCommonResponse(response.toString());

		duplicateUtils.setReqRespReplica(userModel, vendorVerifyModel, reqModel);

		String encryptedCommonResponse = PasswordUtils.demoEncryption(response, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);

		return structure;
	}

}
