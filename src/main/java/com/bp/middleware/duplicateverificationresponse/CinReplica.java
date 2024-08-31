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
public class CinReplica {
	
	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure cinDuplicateResponse(RequestModel model, EntityModel userModel, VendorVerificationModel vendorVerifyModel) throws Exception{
		
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
		
		Pattern pattern = Pattern.compile(AppConstants.CIN_PATTERN);
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

			JSONObject companyInfo = new JSONObject();
			
			companyInfo.put("cin", source);
			companyInfo.put("roc_code", "RoC-Bangalore");
			companyInfo.put("registration_number", "032926");
			companyInfo.put("company_category", "Company limited by Shares");
			companyInfo.put("class_of_company", "Private");
			companyInfo.put("company_sub_category", "Non-govt company");
			companyInfo.put("authorized_capital", "25000000");
			companyInfo.put("paid_up_capital", "2500000");
			companyInfo.put("number_of_members", "2");
			companyInfo.put("date_of_incorporation", "2003-11-27");
			companyInfo.put("registered_address", "DIVYASREE GREENS,GR FLOOR,SY NOS.12/1 12/2A AND 13/1A, GHALLAGHATTA VILLAGE VARTHUR HOBLI, BANGALORE KA 560071 IN");
			companyInfo.put("address_other_than_ro","");
			companyInfo.put("email", "abc@gmail.com");
			companyInfo.put("listed_status", "Unlisted");
			companyInfo.put("active_compilance", "");
			companyInfo.put("suspended_at_stock","");
			companyInfo.put("last_agm_date", "2013-09-30");
			companyInfo.put("last_bs_date","2013-09-30");
			companyInfo.put("company_status", "Amalgamated");
			companyInfo.put("status_under_cirp", "");
			
			JSONObject validatedData = new JSONObject();

			validatedData.put("company_id", source);
			validatedData.put("company_type", "Company");
			validatedData.put("company_name", "ABC INDIA PRIVATE LIMITED");
			validatedData.put("company_info", companyInfo);
			validatedData.put("directors", new JSONArray());
			validatedData.put("charges", new JSONArray());

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
