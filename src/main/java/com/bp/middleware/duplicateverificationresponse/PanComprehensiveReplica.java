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
public class PanComprehensiveReplica {

	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure ComprehensiveReplicaanResponse(RequestModel model, EntityModel userModel,
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

		Pattern pattern = Pattern.compile(AppConstants.PAN_PATTERN);
		Matcher matcher = pattern.matcher(source);

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String referenceId = FileUtils.getRandomOTPnumber(10);

		JSONObject response = new JSONObject();

		response.put("response_time", responseTime);
		response.put("reference_id", referenceId);

		if (matcher.matches()) {// matcher.matches()
			
			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("Success");
			reqModel.setMessage("Success");

			response.put("status", "success");
			response.put("messsage", AppConstants.DUMMY_SUCCESS_MESSAGE);

			JSONObject validatedData = new JSONObject();

			validatedData.put("pan_number", source);
			validatedData.put("full_name", "MR. RATHORE");
			validatedData.put("masked_aadhaar", "XXXXXXXX1234");
			validatedData.put("email", "RATHORE@GMAIL.COM");
			validatedData.put("phone_number", "1234567810");
			validatedData.put("gender", "n");
			validatedData.put("dob", "0001-07-00");
			validatedData.put("input_dob", "");
			validatedData.put("aadhaar_linked", true);
			validatedData.put("dob_verified", false);
			validatedData.put("dob_check", false);
			validatedData.put("category", "person");
			validatedData.put("less_info", false);

			JSONArray fullNameSplit = new JSONArray();

			fullNameSplit.put("MR");
			fullNameSplit.put("");
			fullNameSplit.put("RATHORE");

			validatedData.put("full_name_split", fullNameSplit);

			JSONObject address = new JSONObject();

			address.put("line_1", "A 000 ");
			address.put("line_2", "Jagatpura");
			address.put("street_name", "S.O");
			address.put("zip", "123456");
			address.put("city", "Jaipur");
			address.put("state", "RAJASTHAN");
			address.put("country", "INDIA");
			address.put("full", "A 000 RAJASTHAN INDIA");

			validatedData.put("address", address);

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
		duplicateUtils.setReqRespReplica(userModel,vendorVerifyModel,reqModel);

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
