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
public class DinReplica {
	
	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure dinDuplicateResponse(RequestModel model, EntityModel userModel, VendorVerificationModel vendorVerifyModel) throws Exception{
		
		ResponseStructure structure = new ResponseStructure();
		
		String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),userModel.getSecretKey());
		JSONObject userJson = new JSONObject(userDecryption);
		
		String source = userJson.getString("source");
		
		RequestModel reqModel = new RequestModel();
		
		reqModel.setSource(source);
		reqModel.setSourceType("ID");
		reqModel.setRequestDateAndTime(new Date());
		reqModel.setFilingStatus(false);
		reqModel.setRequestBy(userModel.getName());
		
		Pattern pattern = Pattern.compile(AppConstants.DIN_PATTERN);
		Matcher matcher = pattern.matcher(source);
		
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);
		
		String referenceId = FileUtils.getRandomOTPnumber(10);
		
		
		JSONObject response = new JSONObject();
		
		response.put("response_time", responseTime);
		response.put("reference_id", referenceId);
		
		if(matcher.matches()) {
			
			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("Success");
			reqModel.setMessage("Success");
			
			response.put("status", "success");
			response.put("messsage", AppConstants.DUMMY_SUCCESS_MESSAGE);

			JSONObject validatedData = new JSONObject();

			validatedData.put("din_number", source);
			validatedData.put("father_name", "Simon");
			validatedData.put("full_name", "Dummy name");
			validatedData.put("dob", "1998-06-30");
			validatedData.put("nationality","Indian");
			validatedData.put("present_address", "Pl. No. 63, EVP Padma Avenue$Vallancheri$Chengalpattu$TN$603202$India$");
			validatedData.put("permanent_address", "Pl. No. 63, EVP Padma Avenue$Vallancheri$Chengalpattu$TN$603202$India$");
			validatedData.put("email", "abc@gmail.com");
			validatedData.put("pan_number", "ABCTY1234D");
			validatedData.put("din_status", "success");
			validatedData.put("companies_associated", new JSONArray());

			response.put("validated_data", validatedData);
			
		}else {
			
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
