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
public class FssaiReplica {
	
	
	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure fssaiDuplicateResponse(RequestModel model, EntityModel userModel, VendorVerificationModel vendorVerifyModel) throws Exception{
		
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
		
		Pattern pattern = Pattern.compile(AppConstants.FSSAI_PATTERN);//FSSAI PATTERN SHOULD BE SET.
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

			validatedData.put("fssai_number", "11109015001234");
			validatedData.put("application_number", "11101207002500111");
			
			JSONObject data = new JSONObject();
			
			data.put("state_premises", "KL");
			data.put("district_premises", 000);
			data.put("taluk_premises",  12345);
			data.put("app_type", "N");
			data.put("pincode", 12345);
			data.put("address_premesis", "Kerala, 721602");
			data.put("status_desc", "Registration Certificate issued");
			data.put("district_name", "canannore");
			data.put("display_ref_id", "00001207002500000");
			data.put("taluk_name", "MUNICIPALITY");
			data.put("company_name", "AK ENTERPRISE");
			data.put("app_type_desc", "New License Or New Registration");
			data.put("state", "Kerala");
			data.put("license_category_name", "Registration");
			data.put("app_submition_date", "2000-00-07");
			data.put("last_updated_on", "2000-00-07");
			data.put("fbo_id",000111);
			data.put("ref_id", 98765432);

			validatedData.put("details", data);
			
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
