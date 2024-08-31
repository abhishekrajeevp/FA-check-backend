package com.bp.middleware.ocr;

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
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.duplicateverificationresponse.ChequeOcrReplica;
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
public class ChequeOcrService {

	
	private final String VERIFICATION_TYPE = AppConstants.CHEQUE_OCR;
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
	private SurepassOcrMechanisms ocrMechanisms;
	@Autowired
	private ErrorIdentifierService errorIdentifierService;
	@Autowired
	private  CommonResponseStructure commonResponseStructure;
	@Autowired
	private ChequeOcrReplica chequeOcrReplica;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;

	public ResponseStructure chequeOcr( MultipartFile file, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();
		try {
			
			RequestModel model = new RequestModel();

			String apiKey = servletRequest.getHeader("x-parse-rest-api-key");
			String applicationId = servletRequest.getHeader("x-parse-application-id");

			EntityModel userModel = userRepository.findByApiKeyAndApplicationId(apiKey, applicationId);
			

			if (userModel == null) {
				userModel = userRepository.findByApiSandboxKeyAndApplicationId(apiKey, applicationId);
			}

			if (userModel != null && userModel.isAccountStatus()) {
				
				ENTITY=userModel;
				
				int fileFormatCheckFlag = smartRouteUtils.FileFormatCheck(file,userModel);
				
				VendorVerificationModel vendorVerifyModel = vendorVerificationRepository
						.findByVerificationDocument(AppConstants.CHEQUE_OCR);
				
				if(fileFormatCheckFlag == 2 || fileFormatCheckFlag == 3 || fileFormatCheckFlag == 4) {
					
					return smartRouteUtils.fileFormatFailed(fileFormatCheckFlag,userModel,vendorVerifyModel);
				}

				VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.SUREPASS_VENDOR);

				MerchantPriceModel merchantPriceModel = merchantPriceRepository
						.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel, vendorVerifyModel,
								userModel,true);

				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.getByVendorModelAndVendorVerificationModelAndStatus(vendorModel, vendorVerifyModel,true);

				if (merchantPriceModel != null && vendorPriceModel!=null && merchantPriceModel.isAccepted()) {
					

					if (userModel.getApiSandboxKey().equals(apiKey)  && userModel.getNoRestriction()==0) {

						return chequeOcrReplica.ChequeOcrResponse(file, userModel,vendorVerifyModel);
					
					}else if(userModel.getNoRestriction()>0) {
						
						userModel.setNoRestriction(userModel.getNoRestriction()-1);
						model.setFreeHit(true);
					}

					ResponseStructure balanceCheck = smartRouteUtils.balanceCheckForOcr(userModel, merchantPriceModel,vendorVerifyModel);

					if (balanceCheck.getFlag() == 1) {

						return chequeOcrVerification(userModel, vendorModel, vendorVerifyModel, merchantPriceModel,
								file,model);

					}

					return balanceCheck;

				} else {

					if(vendorPriceModel == null) {
						return smartRouteUtils.verificationCurrentlyNotAvailable(userModel, vendorVerifyModel,model);
					}else if(merchantPriceModel==null){
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

	private ResponseStructure chequeOcrVerification(EntityModel entityModel, VendorModel vendorModel,
			VendorVerificationModel vendorVerifyModel, MerchantPriceModel merchantPriceModel, MultipartFile file,RequestModel model)
			throws Exception {

		ResponseStructure structure = new ResponseStructure();

		Request request = new Request();
		Date reqDate = new Date();

		String surepassResponse = ocrMechanisms.ocrMechanisms(entityModel,merchantPriceModel,file);

//		String surepassResponse = AppConstants.OCR_AADHAAR;

		JSONObject wholeData = new JSONObject(surepassResponse);

		System.err.println("resp : " + wholeData);

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

		smartRouteUtils.errorCodes(statusCodeNumber, vendorModel,entityModel,vendorVerifyModel);
		
		String message = null;
		if (status) {
			message = wholeData.getString("message_code");
		} else {
			message = wholeData.getString("message");
		}

		String referenceId = FileUtils.getRandomOTPnumber(10);

		Response response = new Response();

		LocalDateTime dateTime = LocalDateTime.now();
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		String responseTime = dateTime.format(format);

		String ipAddress = ipAndLocation.publicIpAddress();
		request.setIpAddress(ipAddress);
		response.setIpAddress(ipAddress);

		request.setSourceType("Image");
		// request.setRequestBy(model.getRequestBy());
		request.setRequestDateAndTime(reqDate);
		request.setPrice(merchantPriceModel.getImagePrice());
		request.setUser(entityModel);
		request.setVerificationModel(vendorVerifyModel);
		request.setResponseDateAndTime(responseTime);
		request.setMessage(message);
		request.setReferenceId(referenceId);
		request.setFreeHit(model.isFreeHit());
		request.setConsider(true);

		response.setMessage(message);
		response.setSourceType("Image");
		response.setRequestDateAndTime(new Date());
		// response.setRequestBy(model.getRequestBy());
		response.setUser(entityModel);
		response.setVendorModel(vendorModel);
		response.setRequest(request);
		response.setResponseDateAndTime(responseTime);
		response.setResponse(surepassResponse);
		response.setReferenceId(referenceId);

		RequestModel temporary = new RequestModel();

		temporary.setResponseDateAndTime(responseTime);
		temporary.setMessage(message);
		temporary.setStatusCodeNumber(statusCodeNumber);
		temporary.setReferenceId(referenceId);
		temporary.setVendorModel(vendorModel);

		if (status) {

			JSONObject data = wholeData.getJSONObject("data");

			System.err.println("DATA : -- " + data);
			String clientId = data.getString("client_id");
//			JSONArray ocrFields = data.getJSONArray("ocr_fields");

//			JSONObject indexZeroFromList = ocrFields.getJSONObject(0);

//			String documentType = indexZeroFromList.getString("document_type");

			JSONObject micrDetails = data.getJSONObject("micr");
			JSONObject accountNumberDetails = data.getJSONObject("account_number");
			JSONObject ifscCodeDetails = data.getJSONObject("ifsc_code");

			String micr = micrDetails.getString("value");
			String accountNumber = accountNumberDetails.getString("value");
			String ifscCode = ifscCodeDetails.getString("value");

			request.setSource(micr + " OCR");
			request.setClientId(clientId);
			request.setStatus("success");

			response.setSource(micr + " OCR");
			response.setClientId(clientId);
			response.setStatus("success");

			temporary.setOcrData(data);
			temporary.setStatus("success");

		} else {

			request.setError(message);
			request.setMessage(message);
			request.setStatus("failed");

			response.setError(message);
			response.setMessage(message);
			response.setStatus("failed");

			temporary.setError(message);
			temporary.setMessage(message);
			temporary.setStatus("failed");

		}

		JSONObject commonResponse = commonResponseStructure.commonResponseAadhaarOcr(temporary);
		response.setCommonResponse(commonResponse.toString());

		reqRepository.save(request);
		respRepository.save(response);

		// Prepaid Amount Reduction
		if (entityModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid") && statusCodeNumber != 401 && statusCodeNumber != 403 && !model.isFreeHit()) {

			smartRouteUtils.deductAmountForOcr(entityModel,merchantPriceModel);
			
		}else if(statusCodeNumber != 401 && statusCodeNumber != 403 && !model.isFreeHit()) {
			
			smartRouteUtils.postpaidConsumedAmountForOcr(entityModel, merchantPriceModel);
			
		}else {
			
			request.setConsider(false);
			reqRepository.save(request);
		}

		String encryptedCommonResponse = PasswordUtils.demoEncryption(commonResponse, entityModel.getSecretKey());

		Map<String, Object> mapNew = new HashMap<>();
		mapNew.put("return_response", encryptedCommonResponse);
		
		userRepository.save(entityModel);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(mapNew);
		structure.setMessage(AppConstants.SUCCESS);

		return structure;
	}
}
