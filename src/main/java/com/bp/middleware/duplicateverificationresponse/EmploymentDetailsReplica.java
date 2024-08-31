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
public class EmploymentDetailsReplica {

	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure EmploymentDetailsResponse(RequestModel model, EntityModel userModel,
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

		Pattern pattern = Pattern.compile(AppConstants.UAN_PATTERN);
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

			JSONObject employmentHistory = new JSONObject();

			employmentHistory.put("name", "VISHALini");
			employmentHistory.put("guardian_name", "RAJENDRA");
			employmentHistory.put("establishment_name", "PROCESS SERVICES PVT. LTD.");
			employmentHistory.put("member_id", "KDOPL00478080000317859");
			employmentHistory.put("date_of_joining", "2023-03-17");
			employmentHistory.put("date_of_exit", "2023-03-18");

			validatedData.put("employment_history", employmentHistory);

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
