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
public class IecAdvancedReplica {

	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure IecAdvancedResponse(RequestModel model, EntityModel userModel,
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
		
		Pattern pattern = Pattern.compile(AppConstants.IEC_PATTERN);
		Matcher matcher = pattern.matcher(source);

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String referenceId = FileUtils.getRandomOTPnumber(10);

		JSONObject response = new JSONObject();

		response.put("response_time", responseTime);
		response.put("reference_id", referenceId);

		if (source.length() == 10) {// matcher.matches()
			
			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setStatus("Success");
			reqModel.setMessage("Success");

			response.put("status", "success");
			response.put("messsage", AppConstants.DUMMY_SUCCESS_MESSAGE);

			JSONObject validatedData = new JSONObject();

			validatedData.put("iec_number", source);
			validatedData.put("firm_name", "DEEPAK ");
			validatedData.put("pan_number", "ABCD1234E");
			validatedData.put("dob", "1986-03-07");
			validatedData.put("iec_issuance_date", "2017-05-29");
			validatedData.put("iec_status", "Valid");
			validatedData.put("del_status", "n");
			validatedData.put("iec_cancelled_date", "");
			validatedData.put("iec_suspended_date", "");
			validatedData.put("file_number", "123456789XXXXXX");
			validatedData.put("file_date", "2023-08-09");
			validatedData.put("dgft_ra_office", "RA RAJKOT");
			validatedData.put("nature_of_concern", "Proprietorship");
			validatedData.put("category_of_exporters", "Exporter");
			validatedData.put("address", "Gruh,Rajkot, Gujarat, 360002 ,Rajkot , RAJKOT ,GUJARAT");
			validatedData.put("firm_mobileno", "1234567890");
			validatedData.put("firm_email_id", "deepak@gmail.com");

			JSONArray branchDetails = new JSONArray();

			validatedData.put("branch_details", branchDetails);

			JSONArray rcmcDetails = new JSONArray();

			JSONObject arrayObject = new JSONObject();

			arrayObject.put("rcmc_number", "AHD/350/2018-2019");
			arrayObject.put("issue_date", "2023-06-07");
			arrayObject.put("issue_authority", "Organisations");
			arrayObject.put("products_registered", "agriculture, building materials");
			arrayObject.put("expiry_date", "2024-03-31");
			arrayObject.put("status_source", "Active");
			arrayObject.put("exporter_type", "Merchant Cum Manufacturer Exporter");
			arrayObject.put("validity_period", "298");
			arrayObject.put("validated_by_epc_cb", "y");

			rcmcDetails.put(arrayObject);

			validatedData.put("rcmc_details", rcmcDetails);

			JSONArray directorDetails = new JSONArray();

			JSONObject arrayObject1 = new JSONObject();

			arrayObject1.put("father_name", "KAMARIYA");
			arrayObject1.put("pan_number", "ABCD1234E");
			arrayObject1.put("name", "KAMARIYA");
			arrayObject1.put("address", "TO. KHAMBHALA,TA. PADDHARI Contact No: 1234567890,RAJKOT");

			validatedData.put("director_details", directorDetails);

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
