package com.bp.middleware.surepass;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.erroridentifier.ErrorIdentifierRepository;
import com.bp.middleware.erroridentifier.ErrorIdentifierService;
import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.smartrouteverification.CommonResponseStructure;
import com.bp.middleware.smartrouteverification.SmartRouteUtils;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
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
public class CinService {

	
	private final String VERIFICATION_TYPE = AppConstants.CIN_VERIFY;
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
	private VendorVerificationRepository verificationRepository;
	@Autowired
	private VendorPriceRepository vendorPriceRepository;
	@Autowired
	private SurepassCommons surepassCommons;
	@Autowired
	private SmartRouteUtils smartRouteUtils;
	@Autowired
	private ErrorIdentifierService errorIdentifierService;
	@Autowired
	private  CommonResponseStructure CommonResponseStructure;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;

	public ResponseStructure companyIdentifyNumber(RequestModel model, HttpServletRequest servletRequest) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);

			if (userModel != null && userModel.isAccountStatus()) {

				ENTITY=userModel;
				
				VendorVerificationModel verificationModel = verificationRepository
						.findByVerificationDocument(AppConstants.CIN_VERIFY);
				VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SUREPASS_VENDOR);
				
				MerchantPriceModel merchantPriceModel = merchantPriceRepository
						.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
								userModel,true);
				
				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.getByVendorModelAndVendorVerificationModelAndStatus(vendorModel, verificationModel,true);

				if (merchantPriceModel != null && vendorPriceModel!=null && merchantPriceModel.isAccepted()) {

					ResponseStructure balance = smartRouteUtils.balanceCheck(userModel, merchantPriceModel,verificationModel);

					if (balance.getFlag() == 1) {
						String userDecryption = PasswordUtils.demoDecrypt(model.getEncrypted_data(),
								userModel.getSecretKey());
						JSONObject userJson = new JSONObject(userDecryption);

						userJson.put("source_type", "id");

						String source = userJson.getString("source");

						System.err.println("SOURCE : " + source);

						Response sourceResponse = surepassCommons.sourceCheck(source, userModel, merchantPriceModel);
						System.err.println("Source Response : " + sourceResponse);

						if (sourceResponse.getResponseId() > 0) {
							System.err.println("SOURCE PRESENT");

							return surepassCommons.setRequest(sourceResponse, model, merchantPriceModel, userModel,
									verificationModel);

						} else {

							return surepassCin(model, userModel, verificationModel, vendorModel, merchantPriceModel,
									vendorPriceModel, userJson);
						}
					} else {
						return balance;
					}
				} else {

					if(vendorPriceModel == null) {
						return smartRouteUtils.verificationCurrentlyNotAvailable(userModel, verificationModel,model);
					}else if(merchantPriceModel==null){
						return smartRouteUtils.noAccessForThisVerification(userModel, verificationModel,model);
					}else {
						return smartRouteUtils.notAccepted(userModel, verificationModel,model);
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

	private ResponseStructure surepassCin(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

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
		request.setPrice(merchantPriceModel.getIdPrice());
		request.setUser(userModel);
		request.setVerificationModel(verificationModel);
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
		response.setVerificationModel(verificationModel);

		RequestModel temporary = new RequestModel();

		temporary.setResponseDateAndTime(responseTime);
		temporary.setMessage(message);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setVendorModel(vendorModel);

		if (status) {

			String referenceId = FileUtils.getRandomOTPnumber(10);

			JSONObject internalData = wholeData.getJSONObject("data");
			
			String clientId = internalData.optString("client_id","");
			String companyId = internalData.optString("company_id","");
			String companyType = internalData.optString("company_type","");
			String companyName = internalData.optString("company_name","");
			
			JSONObject details = internalData.getJSONObject("details");
			JSONObject companyInfo = details.getJSONObject("company_info");
			
			String cin = companyInfo.optString("cin", "");
			String rocCode = companyInfo.optString("roc_code", "");
			String registrationNumber = companyInfo.optString("registration_number", "");
			String companyCategory = companyInfo.optString("company_category", "");
			String classOfCompany = companyInfo.optString("class_of_company", "");
			String companySubCategory = companyInfo.optString("company_sub_category", "");
			String authorizedCapital = companyInfo.optString("authorized_capital", "");
			String paidUpCapital = companyInfo.optString("paid_up_capital", "");
			String numberOfMembers = companyInfo.optString("number_of_members", "");
			String dateOfIncorporation = companyInfo.optString("date_of_incorporation", "");
			String registeredAddress = companyInfo.optString("registered_address", "");
			String addressOtherThanRo = companyInfo.optString("address_other_than_ro", "");
			String email = companyInfo.optString("email_id", "");
			String listedStatus = companyInfo.optString("listed_status", "");
			String activeCompilance = companyInfo.optString("active_compliance", "");
			String suspendedAtStockExchange = companyInfo.optString("suspended_at_stock_exchange", "");
			String lastAgmDate = companyInfo.optString("last_agm_date", "");
			String lastBsDate = companyInfo.optString("last_bs_date", "");
			String companyStatus = companyInfo.optString("company_status", "");
			String statusUnderCirp = companyInfo.optString("status_under_cirp", "");
			
			JSONArray directors=details.getJSONArray("directors");
			JSONArray charges=details.getJSONArray("charges");

			request.setReferenceId(referenceId);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setCompanyId(companyId);
			request.setCompanyType(companyType);
			request.setCompanyName(companyName);
			request.setEmail(email);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setCompanyId(companyId);
			response.setCompanyType(companyType);
			response.setBusinessName(companyName);
			response.setEmail(email);
			response.setStatus("success");
			response.setReferenceId(referenceId);
			
			temporary.setStatus("success");
			temporary.setReferenceId(referenceId);
            temporary.setCompanyId(companyId);
            temporary.setCompanyType(companyType);
            temporary.setCompanyName(companyName);
            
            temporary.setCin(cin);
            temporary.setRocCode(rocCode);
            temporary.setRegistrationNumber(registrationNumber);
            temporary.setCompanyCategory(companyCategory);
            temporary.setClassOfCompany(classOfCompany);
            temporary.setCompanySubCategory(companySubCategory);
            temporary.setAuthorizedCapital(authorizedCapital);
            temporary.setPaidUpCapital(paidUpCapital);
            temporary.setNumberOfMembers(numberOfMembers);
            temporary.setDateOfIncorporation(dateOfIncorporation);
            temporary.setRegisteredAddress(registeredAddress);
            temporary.setAddressOtherThanRo(addressOtherThanRo);
            temporary.setEmail(email);
            temporary.setListedStatus(listedStatus);
            temporary.setActiveCompilance(activeCompilance);
            temporary.setSuspendedAtStockExchange(suspendedAtStockExchange);
            temporary.setLastAgmDate(lastAgmDate);
            temporary.setLastBsDate(lastBsDate);
            temporary.setCompanyStatus(companyStatus);
            temporary.setStatusUnderCirp(statusUnderCirp);
            temporary.setDirectors(directors);
            temporary.setCharges(charges);
			
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

		JSONObject commonResponse = CommonResponseStructure.commonResponseCin(temporary);
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
