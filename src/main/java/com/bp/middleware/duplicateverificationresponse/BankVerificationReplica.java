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
public class BankVerificationReplica {
	
	
	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure bav1DuplicateResponse(RequestModel model, EntityModel userModel, VendorVerificationModel vendorVerifyModel) throws Exception{
		
		ResponseStructure structure = new ResponseStructure();
		
		String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),userModel.getSecretKey());
		JSONObject userJson = new JSONObject(userDecryption);
		
		String source = userJson.getString("source");
		String ifscCode = userJson.getString("ifsc_code");
		int ifscRequired = userJson.getInt("extended_data");
		
		RequestModel reqModel = new RequestModel();
		
		reqModel.setSource(source);
		reqModel.setIfscCode(ifscCode);
		reqModel.setSourceType("ID");
		reqModel.setRequestDateAndTime(new Date());
		reqModel.setFilingStatus(false);
		reqModel.setRequestBy(userModel.getName());

		Pattern pattern = Pattern.compile(AppConstants.BANKACCOUNT_PATTERN);
		Pattern ifscPattern = Pattern.compile(AppConstants.IFSC_PATTERN);
		
		Matcher matcher = pattern.matcher(source);
		Matcher ifscMatcher = ifscPattern.matcher(ifscCode);
		
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);
		
		String referenceId = FileUtils.getRandomOTPnumber(10);
		
		JSONObject response = new JSONObject();
		
		response.put("response_time", responseTime);
		response.put("reference_id", referenceId);
		
		boolean sourceLength = source.length()>=6 && source.length()<=22;
		
		if(sourceLength && ifscMatcher.matches()) {
			
			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("Success");
			reqModel.setMessage("Success");
			
			response.put("status", "success");
			response.put("messsage", AppConstants.DUMMY_SUCCESS_MESSAGE);

			JSONObject validatedData = new JSONObject();

			validatedData.put("account_exists", true);
			validatedData.put("full_name", "Dummy name");
			validatedData.put("upi_id", "");
			validatedData.put("remarks", "success");
			
			if(ifscRequired==1) {
				
			JSONObject ifscDetails = new JSONObject();
			
			ifscDetails.put("id", 1);
			ifscDetails.put("ifsc", ifscCode);
			ifscDetails.put("micr", "627002032");
			ifscDetails.put("iso3166", "IN-TN");
			ifscDetails.put("bank",  "State Bank of India");
			ifscDetails.put("bank_code", "SBIN");
			ifscDetails.put("bank_name", "State Bank of India");
			ifscDetails.put("branch", "VILATHIKULAM");
			ifscDetails.put("centre", "TOOTHUKUDI");
			ifscDetails.put("district", "TOOTHUKUDI");
			ifscDetails.put("state", "TAMIL NADU");
			ifscDetails.put("city", "TOOTHUKUDI");
			ifscDetails.put("address", "REDDY JANA SANGAR COMPLEX  1ST FLR SOUTH CAR ST  DISTT  THUTHUKUDI  TAMIL NADU 627907");
			ifscDetails.put("imps", true);
			ifscDetails.put("rtgs", true);
			ifscDetails.put("upi", true);
			ifscDetails.put("neft",true);
			ifscDetails.put("micr_check", true);
			ifscDetails.put("swift", "");
			ifscDetails.put("contact", "");
			
			validatedData.put("ifsc_details", ifscDetails);
			}
			
			response.put("validated_data", validatedData);
			
		}else {
			
			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("failed");
			reqModel.setMessage(AppConstants.DUMMY_FAILED_MESSAGE);
			
			response.put("status", "failed");
			response.put("messsage", AppConstants.DUMMY_FAILED_MESSAGE);
			
			if(!ifscMatcher.matches()) {
				response.put("messsage", "Invalid IFSC Number");
				reqModel.setMessage("Invalid IFSC Number");
			}
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

	
	
	public ResponseStructure bavPennylessDuplicateResponse(RequestModel model, EntityModel userModel, VendorVerificationModel vendorVerifyModel) throws Exception{

		ResponseStructure structure = new ResponseStructure();
		
		String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),userModel.getSecretKey());
		JSONObject userJson = new JSONObject(userDecryption);
		
		String source = userJson.getString("source");
		String ifscCode = userJson.getString("ifsc_code");
		int ifscRequired = userJson.getInt("extended_data");

		RequestModel reqModel = new RequestModel();
		
		reqModel.setSource(source);
		reqModel.setIfscCode(ifscCode);
		reqModel.setSourceType("ID");
		reqModel.setRequestDateAndTime(new Date());
		reqModel.setFilingStatus(false);
		reqModel.setRequestBy(userModel.getName());
		
		Pattern pattern = Pattern.compile(AppConstants.BANKACCOUNT_PATTERN);
		Pattern ifscPattern = Pattern.compile(AppConstants.IFSC_PATTERN);
		
		Matcher matcher = pattern.matcher(source);
		Matcher ifscMatcher = ifscPattern.matcher(ifscCode);
		
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);
		
		String referenceId = FileUtils.getRandomOTPnumber(10);
		
		JSONObject response = new JSONObject();
		
		response.put("response_time", responseTime);
		response.put("reference_id", referenceId);
		
		boolean sourceLength = source.length()>=6 && source.length()<=22;
		
		if(sourceLength && ifscMatcher.matches()) {
			
			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("Success");
			reqModel.setMessage("Success");
			
			response.put("status", "success");
			response.put("messsage", AppConstants.DUMMY_SUCCESS_MESSAGE);

			JSONObject validatedData = new JSONObject();

			validatedData.put("account_exists", true);
			validatedData.put("full_name", "Dummy name");
			validatedData.put("upi_id", "");
			validatedData.put("remarks", "success");
			
			if(ifscRequired==1) {
				
			JSONObject ifscDetails = new JSONObject();
			
			ifscDetails.put("id", 1);
			ifscDetails.put("ifsc", ifscCode);
			ifscDetails.put("micr", "627002032");
			ifscDetails.put("iso3166", "IN-TN");
			ifscDetails.put("bank",  "State Bank of India");
			ifscDetails.put("bank_code", "SBIN");
			ifscDetails.put("bank_name", "State Bank of India");
			ifscDetails.put("branch", "VILATHIKULAM");
			ifscDetails.put("centre", "TOOTHUKUDI");
			ifscDetails.put("district", "TOOTHUKUDI");
			ifscDetails.put("state", "TAMIL NADU");
			ifscDetails.put("city", "TOOTHUKUDI");
			ifscDetails.put("address", "REDDY JANA SANGAR COMPLEX  1ST FLR SOUTH CAR ST  DISTT  THUTHUKUDI  TAMIL NADU 627907");
			ifscDetails.put("imps", true);
			ifscDetails.put("rtgs", true);
			ifscDetails.put("upi", true);
			ifscDetails.put("neft",true);
			ifscDetails.put("micr_check", true);
			ifscDetails.put("swift", "");
			ifscDetails.put("contact", "");
			
			validatedData.put("ifsc_details", ifscDetails);
			}
			
			response.put("validated_data", validatedData);
			
		}else {

			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("failed");
			reqModel.setMessage(AppConstants.DUMMY_FAILED_MESSAGE);
			
			response.put("status", "failed");
			response.put("messsage", AppConstants.DUMMY_FAILED_MESSAGE);
			
			if(!ifscMatcher.matches()) {
				response.put("messsage", "Invalid IFSC Number");
				reqModel.setMessage("Invalid IFSC Number");
			}
			response.put("error", AppConstants.DUMMY_ERROR_MESSAGE);
		}
		
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
