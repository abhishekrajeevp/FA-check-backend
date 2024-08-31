package com.bp.middleware.smartrouteverification;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.duplicateverificationresponse.BankVerificationReplica;
import com.bp.middleware.duplicateverificationresponse.PanReplica;
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
public class BankVerification1 {

	private final String VERIFICATION_TYPE = AppConstants.BANK1_VERIFY;
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
	private CommonResponseStructure CommonResponseStructure;
	@Autowired
	private BankVerificationReplica bankVerificationReplica;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;

	public ResponseStructure bankVerificationOne(RequestModel model, HttpServletRequest servletRequest) {

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
				
				System.err.println("UserName : " + userModel.getName());

				VendorVerificationModel vendorVerifyModel = vendorVerificationRepository
						.findByVerificationDocument(AppConstants.BANK1_VERIFY);

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

				if (!merchantPriceList.isEmpty() && !vendorPriceList.isEmpty() &&  accepted) {
					
					if(userModel.getApiSandboxKey().equals(apiKey) && userModel.getNoRestriction()==0) {
						
						return bankVerificationReplica.bav1DuplicateResponse(model,userModel,vendorVerifyModel);
						
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
						System.err.println("Source Response : " + sourceResponse);

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
							return bankVerificationOneSmartRoute(userJson, model, userModel, vendorVerifyModel);
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

	private ResponseStructure bankVerificationOneSmartRoute(JSONObject userJson, RequestModel model,
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
			System.err.println("ATTEMPT 0 & TimeDifference : " + timeDifference);
			MerchantPriceModel merchantPriority = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 1);
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
				return surepassBankVerification(userJson, model, userModel, vendorVerifyModel, vendorModel,
						merchantPriceModel, vendorPrice);
			}
		}
		return balanceCheck;
	}

	private ResponseStructure surepassBankVerification(JSONObject userJson, RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");
		String ifscCode = userJson.getString("ifsc_code");

		boolean ifscDetails = false;

		if (userJson.getInt("extended_data") == 1) {

			ifscDetails = true;
		}

		Request request = new Request();
		Date reqDate = new Date();

		JSONObject obj = new JSONObject();
		obj.put("id_number", source);
		obj.put("ifsc", ifscCode);
		obj.put("ifsc_details", ifscDetails);

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

		smartRouteUtils.errorCodes(statusCodeNumber, vendorModel,userModel,verificationModel);
		
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
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
		request.setResponseDateAndTime(responseTime);
		request.setMessage(message);
		request.setPrice(merchantPriceModel.getIdPrice());
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
		response.setVerificationModel(verificationModel);

		RequestModel temporary = new RequestModel();

		temporary.setResponseDateAndTime(responseTime);
		temporary.setMessage(message);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setVendorModel(vendorModel);

		if (status) {

			String referenceId = FileUtils.getRandomOTPnumber(10);

			JSONObject internalData = wholeData.getJSONObject("data");
			String clientId = internalData.getString("client_id");

			boolean accountExists = internalData.getBoolean("account_exists");

			String fullName = "";
			String upiId = "";
			String remarks = "";

			if (accountExists) {
				fullName = internalData.optString("full_name", "");
				upiId = internalData.optString("upi_id", "");
				remarks = internalData.optString("remarks", "");
			}

			temporary.setFullName(fullName);
			temporary.setUpiId(upiId);
			temporary.setRemarks(remarks);

			JSONObject ifscMetaData = internalData.getJSONObject("ifsc_details");

			if (ifscDetails && !(remarks.equalsIgnoreCase("invalid_ifsc"))) {

				int id = ifscMetaData.getInt("id");
				String ifsc = ifscMetaData.optString("ifsc", "");
				String micr = ifscMetaData.optString("micr", "");
				String iso3166 = ifscMetaData.optString("iso3166", "");
				String swift = ifscMetaData.optString("swift", "");
				String bank = ifscMetaData.optString("bank", "");
				String bankCode = ifscMetaData.optString("bank_code", "");
				String bankName = ifscMetaData.optString("bank_name", "");
				String branch = ifscMetaData.optString("branch", "");
				String centre = ifscMetaData.optString("centre", "");
				String district = ifscMetaData.optString("district", "");
				String state = ifscMetaData.optString("state", "");
				String city = ifscMetaData.optString("city", "");
				String address = ifscMetaData.optString("address", "");
				String contact = ifscMetaData.optString("contact", "");

				boolean imps = ifscMetaData.getBoolean("imps");
				boolean rtgs = ifscMetaData.getBoolean("rtgs");
				boolean neft = ifscMetaData.getBoolean("neft");
				boolean upi = ifscMetaData.getBoolean("upi");
				boolean micrCheck = ifscMetaData.getBoolean("micr_check");

				request.setState(state);

				response.setAddress(address);
				response.setState(state);

				temporary.setId(id);
				temporary.setIfsc(ifsc);
				temporary.setMicr(micr);
				temporary.setIso3166(iso3166);
				temporary.setSwift(swift);
				temporary.setBank(bank);
				temporary.setBankCode(bankCode);
				temporary.setBankName(bankName);
				temporary.setBranch(branch);
				temporary.setCentre(centre);
				temporary.setDistrict(district);
				temporary.setState(state);
				temporary.setCity(city);
				temporary.setAddress(address);
				temporary.setContact(contact);
				temporary.setImps(imps);
				temporary.setRtgs(rtgs);
				temporary.setUpiPresent(upi);
				temporary.setNeft(neft);
				temporary.setMicrCheck(micrCheck);
			}

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setFullName(fullName);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setFullName(fullName);
			response.setStatus("success");
			response.setReferenceId(referenceId);

			temporary.setStatus("success");
			temporary.setReferenceId(referenceId);
			temporary.setAccountExists(accountExists);
			temporary.setFullName(fullName);
			temporary.setUpiId(upiId);
			temporary.setRemarks(remarks);

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

		JSONObject commonResponse = CommonResponseStructure.commonResponseBank(temporary);
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

}
