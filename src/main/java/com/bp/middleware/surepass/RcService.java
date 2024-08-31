package com.bp.middleware.surepass;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.duplicateverificationresponse.RcReplica;
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
public class RcService {

	
	private final String VERIFICATION_TYPE = AppConstants.RC_VERIFY;
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
	private RcReplica rcreplica;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;

	public ResponseStructure registrationCertificate(RequestModel model, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);
			
			if (userModel == null) {
				userModel = userRepository.findByApiSandboxKeyAndApplicationId(apiKey, applicationId);
			}

			if (userModel != null  && userModel.isAccountStatus()) {

				ENTITY=userModel;
				
				VendorVerificationModel verificationModel = verificationRepository
						.findByVerificationDocument(AppConstants.RC_VERIFY);
				VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SUREPASS_VENDOR);
				
				MerchantPriceModel merchantPriceModel = merchantPriceRepository
						.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, verificationModel,
								userModel,true);
				
				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.getByVendorModelAndVendorVerificationModelAndStatus(vendorModel, verificationModel,true);

				if (merchantPriceModel != null && vendorPriceModel!=null && merchantPriceModel.isAccepted()) {
					
					if (userModel.getApiSandboxKey().equals(apiKey)  && userModel.getNoRestriction()==0) {

						return rcreplica.rcDuplicateResponse(model, userModel,verificationModel);
					
					}else if(userModel.getNoRestriction()>0) {
						
						userModel.setNoRestriction(userModel.getNoRestriction()-1);
						model.setFreeHit(true);
					}


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

							return surepassRc(model, userModel, verificationModel, vendorModel, merchantPriceModel,
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

	private ResponseStructure surepassRc(RequestModel model, EntityModel userModel,
			VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			VendorPriceModel vendorPriceModel, JSONObject userJson) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		String source = userJson.getString("source");
		String sourceType = userJson.getString("source_type");

		Request request = new Request();
		Date reqDate = new Date();

		JSONObject obj = new JSONObject();
		obj.put("id_number", source);

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
		response.setRequest(request);
		response.setVendorModel(vendorModel);
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

			String rcNumber = internalData.optString("rc_number","");
			String registeredDate = internalData.optString("registration_date","");
			String ownerName = internalData.optString("owner_name","");
			String fatherName = internalData.optString("father_name","");
			String presentAddress = internalData.optString("present_address","");
			String permanentAddress = internalData.optString("permanent_address","");
			String mobileNumber = internalData.optString("mobile_number","");
			String vehicleCategory = internalData.optString("vehicle_category","");
			String vehicleChasisNumber = internalData.optString("vehicle_chasi_number","");
			String vehicleEngineNumber = internalData.optString("vehicle_engine_number","");
			String makerDescription = internalData.optString("maker_description","");
			String makerModel = internalData.optString("maker_model","");
			String bodyType = internalData.optString("body_type","");
			String fuelType = internalData.optString("fuel_type","");
			String color = internalData.optString("color","");
			String normsType = internalData.optString("norms_type","");
			String fitUpTo = internalData.optString("fit_up_to","");
			String financer = internalData.optString("financer","");
			String insuranceCompany = internalData.optString("insurance_company","");
			String insurancePolicyNumber = internalData.optString("insurance_policy_number","");
			String insuranceUpto = internalData.optString("insurance_upto","");
			String manufacturingDate = internalData.optString("manufacturing_date","");
			String manufacturingDateFormat = internalData.optString("manufacturing_date_formatted","");
			String registeredAt = internalData.optString("registered_at","");
			String latestBy = internalData.optString("latest_by","");
			String taxUpto = internalData.optString("tax_upto","");
			String taxPaidUpto = internalData.optString("tax_paid_upto","");
			String cubicCapacity = internalData.optString("cubic_capacity","");
			String vehicleGrossWeight = internalData.optString("vehicle_gross_weight","");
			String noOfCylinders = internalData.optString("no_cylinders","");
			String seatCapacity = internalData.optString("seat_capacity","");
			String sleeperCapacity = internalData.optString("sleeper_capacity","");
			String standingCapacity = internalData.optString("standing_capacity","");
			String wheelBase = internalData.optString("wheelbase","");
			String unladenWeight = internalData.optString("unladen_weight","");
			String vehicleCategoryDescription = internalData.optString("vehicle_category_description","");
			String puccNumber = internalData.optString("pucc_number","");
			String puccUpto = internalData.optString("pucc_upto","");
			String permitNumber = internalData.optString("permit_number","");
			String permitIssueDate = internalData.optString("permit_issue_date","");
			String permitValidFrom = internalData.optString("permit_valid_from","");
			String permitValidUpto = internalData.optString("permit_valid_upto","");
			String permitType = internalData.optString("permit_type","");
			String nationalPermitNumber = internalData.optString("national_permit_number","");
			String nationalPermitUpto = internalData.optString("national_permit_upto","");
			String nationalPermitIssuedBy = internalData.optString("national_permit_issued_by","");
			String nonUseStatus = internalData.optString("non_use_status","");
			String nonUseFrom = internalData.optString("non_use_from","");
			String nonUseTo = internalData.optString("non_use_to","");
			String blackListStatus = internalData.optString("blacklist_status","");
			String nocDetails = internalData.optString("noc_details","");
			String ownerNumber = internalData.optString("owner_number","");
			String rcStatus = internalData.optString("rc_status","");
			String variant = internalData.optString("variant","");
			String challanDetails = internalData.optString("challan_details","");

			boolean financed =internalData.getBoolean("financed");
			boolean lessInfo =internalData.getBoolean("less_info");
			boolean maskedName =internalData.getBoolean("masked_name");
			
			request.setReferenceId(referenceId);
			request.setFullName(ownerName);
			request.setStatus("success");
			request.setClientId(clientId);
			request.setAttempt(0);

			response.setClientId(clientId);
			response.setFullName(ownerName);
			response.setAddress(permanentAddress);
			response.setDateOfRegistration(registeredDate);
			response.setStatus("success");
			response.setReferenceId(referenceId);

			temporary.setRcNumber(rcNumber);
			temporary.setDateOfRegistration(registeredDate);
			temporary.setOwnerName(ownerName);
			temporary.setFatherName(fatherName);
			temporary.setPresentAddress(presentAddress);
			temporary.setPermanentAddress(permanentAddress);
			temporary.setMobileNumber(mobileNumber);
			temporary.setVehicleCategory(vehicleCategory);
			temporary.setVehicleChasisNumber(vehicleChasisNumber);
			temporary.setVehicleEngineNumber(vehicleEngineNumber);
			temporary.setMakerDescription(makerDescription);
			temporary.setMakerModel(makerModel);
			temporary.setBodyType(bodyType);
			temporary.setFuelType(fuelType);
			temporary.setColor(color);
			temporary.setNormsType(normsType);
			temporary.setFitUpTo(fitUpTo);
			temporary.setFinancer(financer);
			temporary.setInsuranceCompany(insuranceCompany);
			temporary.setInsurancePolicyNumber(insurancePolicyNumber);
			temporary.setInsuranceUpto(insuranceUpto);
			temporary.setManufacturingDate(manufacturingDate);
			temporary.setManufacturingDateForma(manufacturingDateFormat);
			temporary.setRegisteredAt(registeredAt);
			temporary.setLatestBy(latestBy);
			temporary.setTaxUpto(taxUpto);
			temporary.setTaxPaidUpto(taxPaidUpto);
			temporary.setCubicCapacity(cubicCapacity);
			temporary.setVehicleGrossWeight(vehicleGrossWeight);
			temporary.setNoOfCylinders(noOfCylinders);
			temporary.setSeatCapacity(seatCapacity);
			temporary.setSleeperCapacity(sleeperCapacity);
			temporary.setStandingCapacity(standingCapacity);
			temporary.setWheelBase(wheelBase);
			temporary.setUnladenWeight(unladenWeight);
			temporary.setVehicleCategoryDescription(vehicleCategoryDescription);
			temporary.setPuccNumber(puccNumber);
			temporary.setPuccUpto(puccUpto);
			temporary.setPermitNumber(permitNumber);
			temporary.setPermitIssueDate(permitIssueDate);
			temporary.setPermitValidFrom(permitValidFrom);
			temporary.setPermitValidUpto(permitValidUpto);
			temporary.setPermitType(permitType);
			temporary.setNationalPermitNumber(nationalPermitNumber);
			temporary.setNationalPermitUpto(nationalPermitUpto);
			temporary.setNationalPermitIssuedBy(nationalPermitIssuedBy);
			temporary.setNonUseStatus(nonUseStatus);
			temporary.setNonUseFrom(nonUseFrom);
			temporary.setNonUseTo(nonUseTo);
			temporary.setBlackListStatus(blackListStatus);
			temporary.setNocDetails(nocDetails);
			temporary.setOwnerNumber(ownerNumber);
			temporary.setRcStatus(rcStatus);
			temporary.setVariant(variant);
			temporary.setChallanDetails(challanDetails);
			temporary.setFinanced(financed);
			temporary.setLessInfo(lessInfo);
			temporary.setMaskedNamePresent(maskedName);
			
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
		
		JSONObject commonResponse = CommonResponseStructure.commonResponseRc(temporary);
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
