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
public class InternationalPassportOcrReplica {

	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure InternationalPassportOcrResponse(MultipartFile model, EntityModel userModel,
			VendorVerificationModel vendorVerifyModel) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		RequestModel reqModel = new RequestModel();
		
		reqModel.setSource("International Passport OCR Image");
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

			arrayObject.put("type", "international_passport");

			JSONObject details = new JSONObject();

			details.put("birth_date", "111111");
			details.put("birth_date_hash", "2");
			details.put("country_code", "SVK");
			details.put("country", "Slovakia");
			details.put("document_number", "PD000000");
			details.put("document_number_hash", "5");
			details.put("document_type", "p");
			details.put("expiry_date", "150104");
			details.put("expiry_date_hash", "111111");
			details.put("final_hash", "3");
			details.put("name", "3");
			details.put("nationality_code", "5VK");
			details.put("nationality", "");
			details.put("optional_data", "");
			details.put("optional_data_hash", "0");
			details.put("sex", "m");
			details.put("surname", "SPECIMEN");
			details.put("mrz_line_1", "P<SVKSPECIMEN<<VZOR<<<<<<<<<<<<<<<<<<<<<<<<<");
			details.put("mrz_line_2", "PD000000<55VK1111112M1501043<<<<<<<<<<<<<<00");

			ocrFields.put(details);

			validatedData.put("ocr_fields", ocrFields);

			response.put("validated_data", validatedData);

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
