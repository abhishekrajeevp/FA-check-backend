package com.bp.middleware.smartrouteverification;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.duplicateverificationresponse.PanReplica;
import com.bp.middleware.duplicateverificationresponse.VoterIdReplica;
import com.bp.middleware.erroridentifier.ErrorIdentifierRepository;
import com.bp.middleware.erroridentifier.ErrorIdentifierService;
import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.DateUtil;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.GetPublicIpAndLocation;
import com.bp.middleware.util.PasswordUtils;
import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class VoterIdVerification {

	
	private final String VERIFICATION_TYPE = AppConstants.VOTERID_VERIFY;
	private EntityModel ENTITY = null;
	
	@Autowired
	private ResponseRepository respRepository;
	@Autowired
	private RequestRepository reqRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MerchantPriceRepository merchantPriceRepository;
	@Autowired
	private VendorRepository vendorRepository;
	@Autowired
	private VendorVerificationRepository vendorVerificationRepository;
	@Autowired
	private VendorPriceRepository vendorPriceRepository;
	@Autowired
	private SmartRouteUtils smartRouteUtils;
	@Autowired
	private ErrorIdentifierService errorIdentifierService;
	@Autowired
	private  CommonResponseStructure CommonResponseStructure;
	@Autowired
	private VoterIdReplica voterIdReplica;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;

	public ResponseStructure voterIdVerification(RequestModel model, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);
			
			if (userModel == null) {
				userModel = userRepository.findByApiSandboxKeyAndApplicationId(apiKey, applicationId);
			}

			if (userModel != null && userModel.isAccountStatus()) {

				ENTITY=userModel;
				
				VendorVerificationModel vendorVerifyModel = vendorVerificationRepository
						.findByVerificationDocument(AppConstants.VOTERID_VERIFY);

				List<MerchantPriceModel> merchantPriceList = merchantPriceRepository
						.findByEntityModelAndVendorVerificationModelAndStatus(userModel, vendorVerifyModel, true);
				
				 boolean accepted = true;
					
					for (MerchantPriceModel merchantPriceModel : merchantPriceList) {
						
						if(accepted == true) {
							
							accepted = merchantPriceModel.isAccepted();
						}else {
							 break ;
						}
					}
				

				List<VendorPriceModel> vendorPriceList = vendorPriceRepository
						.findByVendorVerificationModelAndStatus(vendorVerifyModel, true);

				if (!merchantPriceList.isEmpty() && !vendorPriceList.isEmpty()  &&  accepted) {
					
					if(userModel.getApiSandboxKey().equals(apiKey) && userModel.getNoRestriction()==0) {
						
						return voterIdReplica.voterIdDuplicateResponse(model,userModel,vendorVerifyModel);
						
					}else if(userModel.getNoRestriction()>0) {
						
						userModel.setNoRestriction(userModel.getNoRestriction()-1);
						
						model.setFreeHit(true);
					}

					MerchantPriceModel merchantPriceModel = merchantPriceList.get(0);

					ResponseStructure balanceCheck = smartRouteUtils.balanceCheck(userModel, merchantPriceModel,vendorVerifyModel);

					if (balanceCheck.getFlag() == 1) {

						String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
								userModel.getSecretKey());
						JSONObject userJson = new JSONObject(userDecryption);

						String referenceNumber = FileUtils.getRandomOTPnumber(10);
						userJson.put("reference_id", referenceNumber);
						userJson.put("source_type", "id");

						String source = userJson.getString("source");

						System.err.println("SOURCE : " + source);

						Response sourceResponse = smartRouteUtils.sourceCheck(source, userModel, merchantPriceModel);
						System.err.println("Source Response : " + (sourceResponse.getResponseId() < 1));

						if (sourceResponse.getResponseId() > 0) {
							System.err.println("Source IF");

							Response response = smartRouteUtils.setRequest(sourceResponse, model, merchantPriceModel,
									userModel, vendorVerifyModel, userJson);

							JSONObject jsonSource = new JSONObject(response.getCommonResponse());

							LocalDateTime dateTime = LocalDateTime.now();
							DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
							String responseTime = dateTime.format(format);

							jsonSource.put("response_time", responseTime);
							jsonSource.put("reference_id", referenceNumber);

							String commonResponse = PasswordUtils.demoEncryption(jsonSource, userModel.getSecretKey());

							Map<String, Object> mapNew = new HashMap<>();
							mapNew.put("return_response", commonResponse);

							structure.setData(mapNew);
							structure.setStatusCode(HttpStatus.OK.value());
							structure.setFlag(1);
							structure.setMessage(AppConstants.SUCCESS);

							return structure;

						} else {
							System.err.println("Source ELSE");
							return voterIdVerificationSmartRoute(userJson, model, userModel, vendorVerifyModel);
						}
					}

					return balanceCheck;

				} else {

					if(vendorPriceList.isEmpty()) {
						return smartRouteUtils.verificationCurrentlyNotAvailable(userModel, vendorVerifyModel,model);
					}else if(merchantPriceList.isEmpty()){
						return smartRouteUtils.noAccessForThisVerification(userModel, vendorVerifyModel,model);
					}else {
						return smartRouteUtils.notAccepted(userModel, vendorVerifyModel,model);
					}
				}

			} else {

				if(userModel == null) {
					return smartRouteUtils.commonErrorResponse();
				}else {
					return smartRouteUtils.accountInactive(userModel);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			
			structure.setErrorReferenceId(errorIdentifierService.errorSaver(e, VERIFICATION_TYPE,ENTITY));
			structure.setFileName(VERIFICATION_TYPE);
			structure.setData(null);
			structure.setMessage(AppConstants.ERROR_MESSAGE_RESPONSE);
			structure.setErrorDiscription(AppConstants.ERROR_DESCRIPTION_RESPONSE);
			structure.setFlag(7);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure voterIdVerificationSmartRoute(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel) throws Exception {

		List<Request> reqList = reqRepository.findByUserAndVerificationModel(userModel, vendorVerifyModel);

		Request lastRequest = new Request();

		InetAddress ipAddressLocalHost = InetAddress.getLocalHost();
		String ipAddress = ipAddressLocalHost.getHostAddress();

		long timeDifference = 0;
		if (!reqList.isEmpty()) {
			lastRequest = reqList.get(reqList.size() - 1);

			Date currentDatetime = new Date();
			Date requestDatetime = lastRequest.getRequestDateAndTime();

			timeDifference = DateUtil.secondsDifferenceCalculator(requestDatetime, currentDatetime);
		}

		int attempt = lastRequest.getAttempt();
//		int priority = attempt + 1;

		model.setAttempt(attempt);

		if (reqList.isEmpty() || attempt == 0 || timeDifference > 60) // first Priority -
		{
			System.err.println("ATTEMPT 0 & Time Diff : " + timeDifference);
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 1);
			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);

		} else if (attempt == 1 && timeDifference < 120) // Second Priority -
		{
			System.err.println("ATTEMPT 1 & Time Diff : " + timeDifference);
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 2);
			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);

		} else // Common Priority
		{
			System.err.println("ATTEMPT 3 COMMON");

			VendorModel highSuccessVendor = smartRouteUtils.vendorSuccessRate(vendorVerifyModel);

			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorModelAndVendorVerificationModel(userModel, highSuccessVendor,
							vendorVerifyModel);

			return requestVendorRouting(userJson, model, merchantPriority, userModel, vendorVerifyModel);
		}
	}

	private ResponseStructure requestVendorRouting(JSONObject userJson, RequestModel model,
			MerchantPriceModel merchantPriceModel, EntityModel userModel, VendorVerificationModel vendorVerifyModel)
			throws Exception {

		VendorModel vendorModel = merchantPriceModel.getVendorModel();
		VendorPriceModel vendorPrice = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(vendorModel,
				vendorVerifyModel);

		ResponseStructure balanceCheck = smartRouteUtils.balanceCheck(userModel, merchantPriceModel,vendorVerifyModel);

		if (balanceCheck.getFlag() == 1) {

			if (vendorModel.getVendorId() == 4) {

				System.err.println("SUREPASS");
				return surepassVoterIdVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);

			} else if (vendorModel.getVendorId() == 2) {

				System.err.println("SPRINT V");

				return sprintVerifyVoterIdVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);
			}
		}

		return balanceCheck;
	}

	private ResponseStructure surepassVoterIdVerification(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel,
			MerchantPriceModel merchantPriceModel, VendorPriceModel vendorPrice) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");

		JSONObject obj = new JSONObject();
		obj.put("id_number", source);

		Request request = new Request();
		Date reqDate = new Date();

		String surepassResponse = smartRouteUtils.surepassMechanism(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(surepassResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		vendorModel.setVendorRequest(vendorModel.getVendorRequest()+1);
		// VendorResponse
		vendorModel.setVendorResponse(vendorModel.getVendorResponse()+1);
		// MonthlyCount
		vendorModel.setMonthlyCount(vendorModel.getMonthlyCount()+1);

		vendorRepository.save(vendorModel);

		int statusCodeNumber = wholeData.getInt("status_code");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("success");

		smartRouteUtils.errorCodes(statusCodeNumber, vendorModel,userModel,vendorVerifyModel);
		
		String message = null;
		if (status) {
			message = wholeData.getString("message_code");
		} else {
			message = wholeData.getString("message");
		}

		Response response = new Response();

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(vendorVerifyModel);
		request.setResponseDateAndTime(responseTime);
		request.setMessage(message);
		request.setFreeHit(model.isFreeHit());
		request.setConsider(true);

		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setVendorModel(vendorModel);
		response.setRequest(request);
		response.setResponseDateAndTime(responseTime);
		response.setResponse(surepassResponse);
		response.setVerificationModel(vendorVerifyModel);

		RequestModel temporary = new RequestModel();

		temporary.setResponseDateAndTime(responseTime);
		temporary.setMessage(message);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setVendorModel(vendorModel);

		if (status) {

			String referenceId = FileUtils.getRandomOTPnumber(10);

			JSONObject internalData = wholeData.getJSONObject("data");

			String inputVoterId = internalData.optString("input_voter_id","");
			String stCode = internalData.optString("st_code","");
			String gender = internalData.optString("gender","");
			String assemblyContituencyNumber = internalData.optString("assembly_constituency_number","");
			String relationNameV2 = internalData.optString("rln_name_v2","");
			String relationNameV1 = internalData.optString("rln_name_v1","");
			String relationNameV3 = internalData.optString("rln_name_v3","");
			String relationType = internalData.optString("relation_type","");
			String namev1 = internalData.optString("name_v1","");
			String namev2 = internalData.optString("name_v2","");
			String namev3 = internalData.optString("name_v3","");
			String clientId = internalData.optString("client_id","");
			String epicNo = internalData.optString("epic_no","");
			String psLatLong = internalData.optString("ps_lat_long","");
			String state = internalData.optString("state","");
			String idNumber = internalData.optString("id","");
			String assemblyConstituency = internalData.optString("assembly_constituency","");
			String area = internalData.optString("area","");
			String parliamentaryName = internalData.optString("parliamentary_name","");
			boolean multiple = internalData.getBoolean("multiple");
			String parliamentaryConstituency = internalData.optString("parliamentary_constituency","");
			JSONArray additionalCheck = internalData.getJSONArray("additional_check");
			String parliamentaryNumber = internalData.optString("parliamentary_number","");
			String dob = internalData.optString("dob","");
			String houseNo = internalData.optString("house_no","");
			String partNumber = internalData.optString("part_number","");
			String name = internalData.optString("name","");
			String polling_station = internalData.optString("polling_station","");
			String sectionNo = internalData.optString("section_no","");
			String slnoInpart = internalData.optString("slno_inpart","");
			String relationName = internalData.optString("relation_name","");
			String age = internalData.optString("age","");
			String partName = internalData.optString("part_name","");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setFullName(name);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setFullName(name);
			response.setStatus("success");
			response.setReferenceId(referenceId);

			temporary.setStatus("success");
			temporary.setReferenceId(referenceId);
			temporary.setFullName(name);
			temporary.setSource(epicNo);
			temporary.setGender(gender);
			temporary.setAssemblyContituencyNumber(assemblyContituencyNumber);
			temporary.setRelationNameV1(relationNameV1);
			temporary.setRelationType(relationType);
			temporary.setNamev1(namev1);
			temporary.setClientId(clientId);
			temporary.setPsLatLong(psLatLong);
			temporary.setState(state);
			temporary.setIdNumber(idNumber);
			temporary.setAssemblyConstituency(assemblyConstituency);
			temporary.setArea(area);
			temporary.setMultiple(multiple);
			temporary.setParliamentaryConstituency(parliamentaryConstituency);
			temporary.setPartNumber(partNumber);
			temporary.setPollingStation(polling_station);
			temporary.setSectionNo(sectionNo);
			temporary.setSlnoInpart(slnoInpart);
			temporary.setRelationName(relationName);
			temporary.setPartName(partName);
			temporary.setParliamentaryName(parliamentaryName);
			temporary.setParliamentaryNumber(parliamentaryNumber);

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(model.getAttempt() + 1);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");

			temporary.setStatus("failed");
			temporary.setReferenceId(FileUtils.getRandomOTPnumber(10));
			temporary.setError("Error");
		}

		
		JSONObject commonResponse = CommonResponseStructure.commonResponseVoterId(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && statusCodeNumber != 401 && statusCodeNumber != 403 && !model.isFreeHit()) {

			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
			
		}else if(statusCodeNumber!=401 && statusCodeNumber!=403 && !model.isFreeHit()) {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
			
		}else {
			
			request.setConsider(false);
			reqRepository.save(request);
		}

		String encryptedCommonResponse = PasswordUtils.demoEncryption(commonResponse, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		userRepository.save(userModel);

		return structure;
	}

	private ResponseStructure sprintVerifyVoterIdVerification(JSONObject userJson, RequestModel model,
			EntityModel userModel, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel,
			MerchantPriceModel merchantPriceModel, VendorPriceModel vendorPrice) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");

		JSONObject obj = new JSONObject();
		obj.put("id_number", source);

		Request request = new Request();
		Date reqDate = new Date();

		String sprintVerifyResponse = smartRouteUtils.sprintVerifyDocument(obj, merchantPriceModel);

		JSONObject wholeData = new JSONObject(sprintVerifyResponse);
		System.err.println("whole Data " + wholeData);

		// VendorReq
		vendorModel.setVendorRequest(vendorModel.getVendorRequest()+1);
		// VendorResponse
		vendorModel.setVendorResponse(vendorModel.getVendorResponse()+1);
		// MonthlyCount
		vendorModel.setMonthlyCount(vendorModel.getMonthlyCount()+1);

		vendorRepository.save(vendorModel);

		int statusCodeNumber = wholeData.getInt("statuscode");
		String statusCode = Integer.toString(statusCodeNumber);
		boolean status = wholeData.getBoolean("status");
		String message = wholeData.getString("message");

		smartRouteUtils.errorCodes(statusCodeNumber, vendorModel,userModel,vendorVerifyModel);
		
		Response response = new Response();

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSource(source);
		request.setSourceType(sourceType);
		request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(vendorVerifyModel);
		request.setResponseDateAndTime(responseTime);
		request.setMessage(message);
		request.setFreeHit(model.isFreeHit());
		request.setConsider(true);
		
		response.setMessage(message);
		response.setSourceType(sourceType);
		response.setSource(source);
		response.setRequestDateAndTime(new Date());
		response.setRequestBy(model.getRequestBy());
		response.setUser(userModel);
		response.setVendorModel(vendorModel);
		response.setRequest(request);
		response.setResponseDateAndTime(responseTime);
		response.setResponse(sprintVerifyResponse);
		response.setVerificationModel(vendorVerifyModel);

		RequestModel temporary = new RequestModel();

		temporary.setResponseDateAndTime(responseTime);
		temporary.setMessage(message);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setVendorModel(vendorModel);

		if (status) {

			String referenceId = FileUtils.getRandomOTPnumber(10);

			JSONObject internalData = wholeData.getJSONObject("data");

			String stCode = internalData.getString("st_code");
			String gender = internalData.getString("gender");
			String assemblyContituencyNumber = internalData.getString("assembly_constituency_number");
			String relationNameV2 = internalData.getString("rln_name_v2");
			String relationNameV1 = internalData.getString("rln_name_v1");
			String relationNameV3 = internalData.getString("rln_name_v3");
			String relationType = internalData.getString("relation_type");

			String namev1 = internalData.getString("name_v1");
			String namev2 = internalData.getString("name_v2");
			String namev3 = internalData.getString("name_v3");
			String clientId = internalData.getString("client_id");
			String epicNo = internalData.getString("epic_no");
			String positionLatLong = internalData.getString("ps_lat_long");
			String state = internalData.getString("state");
			String idNumber = internalData.getString("id");
			String assemblyConstituency = internalData.getString("assembly_constituency");
			String area = internalData.getString("area");
			boolean multiple = internalData.getBoolean("multiple");
			String parliamentaryConstituency = internalData.getString("parliamentary_constituency");
			JSONArray additionalCheck = internalData.getJSONArray("additional_check");
//			String parliamentaryNumber = internalData.getString("parliamentary_number");
//			String parliamentaryName = internalData.getString("parliamentary_name");
//   		String dob = internalData.getString("dob");
//   		String houseNo = internalData.getString("house_no");
//          String inputVoterId = internalData.getString("input_voter_id");
			String partNumber = internalData.getString("part_number");
			String name = internalData.getString("name");
			String pollingStation = internalData.getString("polling_station");
			String sectionNo = internalData.getString("section_no");
			String slnoInpart = internalData.getString("slno_inpart");
			String relationName = internalData.getString("relation_name");
			String age = internalData.getString("age");
			String partName = internalData.getString("part_name");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setFullName(name);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setFullName(name);
			response.setStatus("success");
			response.setReferenceId(referenceId);

			temporary.setStatus("success");
			temporary.setReferenceId(referenceId);
			temporary.setFullName(name);
			temporary.setSource(epicNo);
			temporary.setGender(gender);
			temporary.setAssemblyContituencyNumber(assemblyContituencyNumber);
			temporary.setRelationNameV1(relationNameV1);
			temporary.setRelationType(relationType);
			temporary.setNamev1(namev1);
			temporary.setClientId(clientId);
			temporary.setPsLatLong(positionLatLong);
			temporary.setState(state);
			temporary.setIdNumber(idNumber);
			temporary.setAssemblyConstituency(assemblyConstituency);
			temporary.setArea(area);
			temporary.setMultiple(multiple);
			temporary.setParliamentaryConstituency(parliamentaryConstituency);
			temporary.setPartNumber(partNumber);
			temporary.setPollingStation(pollingStation);
			temporary.setSectionNo(sectionNo);
			temporary.setSlnoInpart(slnoInpart);
			temporary.setRelationName(relationName);
			temporary.setPartName(partName);

		} else {

			request.setError("error");
			request.setStatus("failed");
			request.setAttempt(model.getAttempt() + 1);
			request.setErrorCode(statusCode);

			response.setErrorCode(statusCode);
			response.setError("error");
			response.setStatus("failed");

			temporary.setStatus("failed");
			temporary.setReferenceId(FileUtils.getRandomOTPnumber(10));
			temporary.setError("Error");
		}

		JSONObject commonResponse = CommonResponseStructure.commonResponseVoterId(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && statusCodeNumber != 401 && statusCodeNumber != 403 && !model.isFreeHit()) {

			smartRouteUtils.deductAmountForId(userModel,merchantPriceModel);
		}else if(statusCodeNumber!=401 && statusCodeNumber!=403 && !model.isFreeHit()) {
			
			smartRouteUtils.postpaidConsumedAmount(userModel,merchantPriceModel);
		}

		String encryptedCommonResponse = PasswordUtils.demoEncryption(commonResponse, userModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);
		
		userRepository.save(userModel);

		return structure;

	}
}
