package com.bp.middleware.duplicateverificationresponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
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
public class DrivingLicenseReplica {

	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure DrivingLicenceDuplicateResponse(RequestModel model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(), userModel.getSecretKey());
		JSONObject userJson = new JSONObject(userDecryption);

		String source = userJson.getString("source");
		String dob = userJson.getString("dob");

		RequestModel reqModel = new RequestModel();

		reqModel.setSource(source);
		reqModel.setSourceType("ID");
		reqModel.setDob(dob);
		reqModel.setRequestDateAndTime(new Date());
		reqModel.setFilingStatus(false);
		reqModel.setRequestBy(userModel.getName());

		Pattern pattern = Pattern.compile(AppConstants.DL_PATTERN);
		Matcher matcher = pattern.matcher(source);

		Pattern pattern2 = Pattern.compile(AppConstants.DL_PATTERN_2);
		Matcher matcher2 = pattern2.matcher(source);

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String referenceId = FileUtils.getRandomOTPnumber(10);

		JSONObject response = new JSONObject();

		response.put("response_time", responseTime);
		response.put("reference_id", referenceId);

		if (matcher.matches() || matcher2.matches()) {
			
			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("Success");
			reqModel.setMessage("Success");

			response.put("status", "success");
			response.put("messsage", AppConstants.DUMMY_SUCCESS_MESSAGE);

			JSONObject validatedData = new JSONObject();

			validatedData.put("license_number", source);
			validatedData.put("name", "Dummy name");
			validatedData.put("permanent_address",
					"3/2, Indira Nagar 4th Main Rd, Indira Nagar, Adyar, Chennai, Tamil Nadu 600020, India");
			validatedData.put("dob", dob);
			validatedData.put("state", "Tamil nadu");
			validatedData.put("gender", "Male");
			validatedData.put("permanent_zip", "600065");
			validatedData.put("temporary_address",
					"3/2, Indira Nagar 4th Main Rd, Indira Nagar, Adyar, Chennai, Tamil Nadu 600020, India");
			validatedData.put("temporary_zip", "600065");
			validatedData.put("citizenship", "IND");
			validatedData.put("ola_name", "RTO, POONAMALEE");
			validatedData.put("ola_code", "TN18");
			validatedData.put("father_or_husband_name", "Alex");
			validatedData.put("doe", "2041-03-27");
			validatedData.put("transport_doe", "1800-01-01");
			validatedData.put("doi", "2019-10-25");
			validatedData.put("transport_doi", "1800-01-01");
			validatedData.put("has_image", true);
			validatedData.put("blood_group", "A+");
			validatedData.put("less_info", false);
			validatedData.put("initial_doi", "2019-10-25");
			validatedData.put("current_status", "Active");
			validatedData.put("vehicle_classes", new JSONArray());
			validatedData.put("additional_check", new JSONArray());
			validatedData.put("profile_image", "");

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
