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
public class VoterIdReplica {
	
	
	
	@Autowired
	private DuplicateUtils duplicateUtils;

	public ResponseStructure voterIdDuplicateResponse(RequestModel model, EntityModel userModel, VendorVerificationModel vendorVerifyModel) throws Exception{
		
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
		
		Pattern pattern = Pattern.compile(AppConstants.VOTERID_PATTERN);
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
			
			validatedData.put("name_v1", "எஸ். ராஜு");
			validatedData.put("rln_name_v1", "சரோஜா");
			validatedData.put("relation_type", "Mother");
			validatedData.put("epic_no", source);
			validatedData.put("gender", "Male");
			validatedData.put("assembly_constituency_number", "170");
			validatedData.put("client_id", "");
			validatedData.put("state", "Tamil nadu");
			validatedData.put("ps_lat_long", "");
			validatedData.put("id", "41941114_TAL197892_T22");
			
			validatedData.put("assembly_constituency", "Aranthangi");
			validatedData.put("area", "(West Faced Middle Terraced Building) North Portion ");
			validatedData.put("multiple", false);
			validatedData.put("parliamentary_constituency", "Ramanathapuram");
			validatedData.put("part_number", "223");
			validatedData.put("name", "S.Raju");
			validatedData.put("polling_station", "Panchayat Union Middle School - (West Faced Middle Terraced Building) North Portion");
			validatedData.put("section_no", "1");
			validatedData.put("slno_inpart","755");
			validatedData.put("relation_name", "Saroja");
			validatedData.put("age", 26);
			validatedData.put("part_name", "Panchayat Union Middle School - (West Faced Middle Terraced Building) North Portion");
			
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
