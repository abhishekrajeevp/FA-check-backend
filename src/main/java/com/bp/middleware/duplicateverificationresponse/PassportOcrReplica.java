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
public class PassportOcrReplica {

	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure PassportOcrResponse(MultipartFile model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		RequestModel reqModel = new RequestModel();
		
		reqModel.setSource("Passport OCR Image");
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

			arrayObject.put("document_type", "passport_back");

			JSONObject address = new JSONObject();

			address.put("value", "TRIPATHI HAVELI, MIRZAPUR, UTTAR PRADESH");
			address.put("confidence", 99);

			arrayObject.put("address", address);

			JSONObject father = new JSONObject();

			father.put("value", "Kaleen ");
			father.put("confidence", 96);

			arrayObject.put("father", father);

			JSONObject mother = new JSONObject();

			mother.put("value", "devi");
			mother.put("confidence", "98");

			arrayObject.put("mother", mother);

			JSONObject fileNum = new JSONObject();

			fileNum.put("value", "DL1061961923456");
			fileNum.put("confidence", "98");

			arrayObject.put("file_num", fileNum);

			JSONObject oldDoi = new JSONObject();

			oldDoi.put("value", "");
			oldDoi.put("confidence", "0");

			arrayObject.put("old_doi", oldDoi);

			JSONObject oldPassportNum = new JSONObject();

			oldPassportNum.put("value", "");
			oldPassportNum.put("confidence", "0");

			arrayObject.put("old_passport_num", oldPassportNum);

			JSONObject oldPlaceOfIssue = new JSONObject();

			oldPlaceOfIssue.put("value", "");
			oldPlaceOfIssue.put("confidence", "0");

			arrayObject.put("old_place_of_issue", oldPlaceOfIssue);

			JSONObject pin = new JSONObject();

			pin.put("value", "231001");
			pin.put("confidence", "0");

			arrayObject.put("pin", pin);

			JSONObject spouse = new JSONObject();

			spouse.put("value", "231001");
			spouse.put("confidence", "0");

			arrayObject.put("spouse", spouse);

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
