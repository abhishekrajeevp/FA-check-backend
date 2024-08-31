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
public class GstReplica {
	
	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure gstDuplicateResponse(RequestModel model, EntityModel userModel, VendorVerificationModel vendorVerifyModel) throws Exception{
		
		ResponseStructure structure = new ResponseStructure();
		
		String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),userModel.getSecretKey());
		JSONObject userJson = new JSONObject(userDecryption);
		
		String source = userJson.optString("source");
		boolean filingStatus = userJson.getBoolean("filing_status_get");
		
		RequestModel reqModel = new RequestModel();

		reqModel.setSource(source);
		reqModel.setSourceType("ID");
		reqModel.setRequestDateAndTime(new Date());
		reqModel.setFilingStatus(filingStatus);
		reqModel.setRequestBy(userModel.getName());
		
		Pattern pattern = Pattern.compile(AppConstants.GST_PATTERN,Pattern.CASE_INSENSITIVE);
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

			validatedData.put("gstin", source);
			validatedData.put("pan_number", "ABCTY1234D");
			validatedData.put("business_name", "Globex Corporation");
			validatedData.put("legal_name", "Globex Corporation");
			validatedData.put("center_jurisdiction", "Commissionerate - MADURAI,Division - SIVAKASI,Range - SIVAKASI - I RANGE");
			validatedData.put("state_jurisdiction", "State - Tamil Nadu,Division - VIRUDHUNAGAR,Zone - Sivakasi,Circle - SIVAKASI - II (Jurisdictional Office)");
			validatedData.put("date_of_registration", "2017-07-01");
			validatedData.put("date_of_cancellation", "1800-01-01");
			validatedData.put("constitution_of_business",  "Partnership");
			validatedData.put("taxpayer_type", "Regular");
			validatedData.put("gstin_status", "Active");
			validatedData.put("field_visit_conducted", "No");
			validatedData.put("nature_of_core_business_activity_code", "TRD:TRR");
			validatedData.put("nature_of_core_business_activity_description","Trader, Retailer");
			validatedData.put("aadhaar_validation", "No");
			validatedData.put("aadhaar_validation_date", "1800-01-01");
			validatedData.put("address", "6, NA, PAVALIAN STREET, SIVAKAASI, vlc-nagar, Telungana, 626123");
			validatedData.put("filing_status", new JSONArray());

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
