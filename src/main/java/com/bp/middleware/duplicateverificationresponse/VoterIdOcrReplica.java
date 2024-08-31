package com.bp.middleware.duplicateverificationresponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.PasswordUtils;
import com.bp.middleware.vendors.VendorVerificationModel;

@Component
public class VoterIdOcrReplica {

	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure VoterIdOcrResponse(MultipartFile model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		RequestModel reqModel = new RequestModel();
		
		reqModel.setSource("Voter ID OCR Image");
		reqModel.setSourceType("Image");
		reqModel.setRequestDateAndTime(new Date());
		reqModel.setFilingStatus(false);
		reqModel.setRequestBy(userModel.getName());
		
		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String referenceId = FileUtils.getRandomOTPnumber(10);

		JSONObject response = new JSONObject();

		response.put("response_time", responseTime);
		response.put("reference_id", referenceId);

		if (true) {

			response.put("status", "success");
			response.put("messsage", AppConstants.DUMMY_SUCCESS_MESSAGE);

			JSONObject validatedData = new JSONObject();

			JSONArray ocrFields = new JSONArray();

			JSONObject arrayObject = new JSONObject();

			arrayObject.put("document_type", "voterid_front");

			JSONObject fullName = new JSONObject();

			fullName.put("value", "Munna ");
			fullName.put("confidence", 9);

			arrayObject.put("full_name", fullName);

			JSONObject age = new JSONObject();

			age.put("value", "30 ");
			age.put("confidence", 96);

			arrayObject.put("age", age);

			JSONObject careOf = new JSONObject();

			careOf.put("value", "Kaleen");
			careOf.put("confidence", "98");

			arrayObject.put("care_of", careOf);

			JSONObject dob = new JSONObject();

			dob.put("value", "1990-01-01");
			dob.put("confidence", "98");

			arrayObject.put("dob", dob);

			JSONObject gender = new JSONObject();

			gender.put("value", "m");
			gender.put("confidence", "84");

			arrayObject.put("gender", gender);

			JSONObject epicNumber = new JSONObject();

			epicNumber.put("value", "");
			epicNumber.put("confidence", "0");

			arrayObject.put("epic_number", epicNumber);

			ocrFields.put(arrayObject);

			validatedData.put("ocr_fields", ocrFields);

			response.put("validated_data", validatedData);
			
			reqModel.setStatus("Success");
			reqModel.setResponseDateAndTime(responseTime);
			reqModel.setMessage(AppConstants.DUMMY_SUCCESS_MESSAGE);

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
