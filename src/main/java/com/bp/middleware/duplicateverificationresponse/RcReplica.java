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
public class RcReplica {
	
	
	
	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure rcDuplicateResponse(RequestModel model, EntityModel userModel, VendorVerificationModel vendorVerifyModel) throws Exception{
		
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
		
		Pattern pattern = Pattern.compile(AppConstants.RC_PATTERN);
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

			validatedData.put("rc_number", source);
			validatedData.put("date_of_registration", "2020-01-06");
			validatedData.put("owner_name","Abimanyu");
			validatedData.put("father_name","Thirumalisamy");
			validatedData.put("present_address","NO 47 ,vadakku theru,Thumbichipalayam,Kallimandayam,Oddanchatram 624616");
			validatedData.put("permanent_address","NO 47 ,vadakku theru,Thumbichipalayam,Kallimandayam,Oddanchatram 624616" );
			validatedData.put("mobile_number","6385798207" );
			validatedData.put("vehicle_category","2WN" );
			validatedData.put("vehicle_chasis_number","AS2A36FI3KCL55735" );
			validatedData.put("engine_number","DECCKH28889" );
			validatedData.put("maker_description","TVS AUTO LTD");
			validatedData.put("maker_model","APACHE RTR");
			validatedData.put("body_type","SOLO WITH PILLION");
			validatedData.put("fuel_type","PETROL");
			validatedData.put("color","Violet" );
			validatedData.put("norms_type","BHARAT STAGE VI");
			validatedData.put("fit_upto","2055-12-13" );
			validatedData.put("financer","WHEELS EMI PVT LTD" );
			validatedData.put("insurance_company","Acko General Insurance Limited" );
			validatedData.put("insurance_policy_number","BBBA00224723010/00");
			validatedData.put("insurance_upto","" );
			validatedData.put("manufacturing_date","11/2019" );
			validatedData.put("manufacturing_date_format","2019-11" );
			validatedData.put("registered_at","ODDANCHATRAM RTO, Tamil Nadu" );
			validatedData.put("latest_by","2024-03-05" );
			validatedData.put("tax_upto","" );
			validatedData.put("tax_paid_upto","2035-01-05" );
			validatedData.put("cubic_capacity","199.50" );
			validatedData.put("vehicle_gross_weight","150");
			validatedData.put("no_of_cylinders","2" );
			validatedData.put("seat_capacity","2" );
			validatedData.put("sleeper_capacity","0" );
			validatedData.put("standing_capacity","0" );
			validatedData.put("wheel_base","1363" );
			validatedData.put("unladen_weight","124" );
			validatedData.put("vehicle_category_description","M-Cycle/Scooter(2WN)" );
			validatedData.put("pucc_number","" );
			validatedData.put("pucc_upto","" );
			validatedData.put("permit_number","" );
			validatedData.put("permit_issue_date","" );
			validatedData.put("permit_valid_from","" );
			validatedData.put("permit_valid_upto","");
			validatedData.put("permit_type","" );
			validatedData.put("national_permit_number","" );
			validatedData.put("national_permit_upto", "");
			validatedData.put("national_permit_issuedby","" );
			validatedData.put("non_use_status","");
			validatedData.put("non_use_form","" );
			validatedData.put("non_use_to","" );
			validatedData.put("black_list_status","");
			validatedData.put("noc_details","" );
			validatedData.put("owner_name","Abimanyu");
			validatedData.put("rc_status","ACTIVE" );
			validatedData.put("variant","" );
			validatedData.put("challan_details","" );
			validatedData.put("financed",true);
			validatedData.put("less_info",true );
			validatedData.put("masked_name",false );

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
