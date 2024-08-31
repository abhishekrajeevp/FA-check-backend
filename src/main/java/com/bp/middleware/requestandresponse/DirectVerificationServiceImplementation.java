package com.bp.middleware.requestandresponse;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.payment.PaymentRepository;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.PasswordUtils;
import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

@Service
public class DirectVerificationServiceImplementation implements DirectVerificationService{

	
	@Autowired
	private ResponseRepository repository;

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
	private PrepaidRepository prepaidRepository;

	@Autowired
	private PostpaidRepository postpaidRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private RequestRepository requestRepository;
	
	@Autowired
	FileUtils fu;

	@Override
	public ResponseStructure verification(RequestModel dto) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);
			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(userDecryption);

			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 1) {
						return panVerification(dto, userModel, reference_id, serviceModel, source, source_type,
								userSecretKey, vendorModel, vendorPrice);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}
				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 1) {
						return panVerification(dto, userModel, reference_id, serviceModel, source, source_type,
								userSecretKey, vendorModel, vendorPrice);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {

					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	
	private ResponseStructure panVerification(RequestModel dto, EntityModel user, String referenceNumber,
			VendorVerificationModel serviceModel, String source, String source_type, String userSecretKey,
			VendorModel vendorModel, VendorPriceModel vendorPrice) {

		ResponseStructure structure = new ResponseStructure();
		try {
			
			    // Request Count
				int count = user.getRequestCount() + 1;
			    user.setRequestCount(count);

			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, user);

			Request request = new Request();
			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			request.setReferenceId(referenceNumber);
			request.setSource(source);
			request.setSourceType(source_type);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(new Date());
			request.setPrice(merchantPriceModel.getIdPrice());

			inputParams.put("reference_id", referenceNumber);
			inputParams.put("source_type", source_type);
			inputParams.put("source", source);

			// String key = AppConstants.ENCRYPTION_KEY;
			// String jsonString = inputParams.toString();
			String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);
			// System.out.println("Encryption Json =" + encryptedJson);

			encryptDatas.put("api_data", encryptedJson);
			encryptDatas.put("enc_mode", "symmetric");
			encryptDatas.put("is_encrypted", true);

			// create an instance of RestTemplate
			RestTemplate restTemplate = new RestTemplate();
			
			//VendorReq
			int reqCount=vendorModel.getVendorRequest()+1;			
			vendorModel.setVendorRequest(reqCount);

			// create headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.add("x-parse-rest-api-key", vendorPrice.getApiKey());
			headers.add("x-parse-application-id", vendorPrice.getApplicationId());
			headers.add("Content-Type", AppConstants.CONTENT_TYPE);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			 //VendorResponse
			 int respCount=vendorModel.getVendorResponse()+1;
			 vendorModel.setVendorResponse(respCount);
			
			System.out.println("DATA : " + data);

			// Convert the string to a JSONObject
			JSONObject jsonObject = new JSONObject(data);

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			String status = jsonObject.getString("status");
			//
			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			System.out.println("status: " + status);
			//

			String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);
			// String decryptData=PasswordUtils.jsonDecryption(encryptedResponse);

			Response response = new Response();

			JSONObject decryptJson = new JSONObject(decryptData);
			String statuss = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			response.setReferenceId(referenceId);
			response.setStatus(statuss);
			response.setSourceType(source_type);
			response.setSource(source);
			response.setRequestDateAndTime(new Date());
			response.setRequestBy(dto.getRequestBy());
			response.setUser(user);
			response.setVendorModel(vendorModel);
			response.setRequest(request);
			

			JSONObject object = new JSONObject();
			object.put("status", statuss);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
			System.out.println("RETURN RESPONSE : " + returnResponse);

			// Prepaid Amount Reduction
			if (user.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = user.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = user.getConsumedAmount() + merchantPriceModel.getIdPrice();

				user.setRemainingAmount(remainingAmount);
				user.setConsumedAmount(consumedAmount);
				user.setPaymentStatus("No Dues");
			}

			if (statuss.equals("success")) {
				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validJson = resultJson.getJSONObject("validated_data");
				String name = validJson.getString("full_name");
				String panNumber = validJson.getString("pan_number");
				String transactionId = decryptJson.getString("transaction_id");

				String message = decryptJson.getString("message");

				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setResponseDateAndTime(responseTimeStamp);
				request.setStatus(status);
				request.setUser(user);
				request.setVerificationModel(serviceModel);
				reqRepository.save(request);

				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setFullName(name);
				response.setPanNumber(panNumber);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setResponseDateAndTime(responseTimeStamp);
				response.setRequest(request);

				repository.save(response);

			} else {
				String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(statuss);
				request.setResponseDateAndTime(responseTimeStamp);
				request.setUser(user);
				request.setVerificationModel(serviceModel);
				request.setError(error);
				request.setErrorCode(errorCode);
				reqRepository.save(request);

				response.setResponse(decryptData);
				response.setError(error);
				response.setErrorCode(errorCode);
				response.setResponseDateAndTime(responseTimeStamp);
				response.setRequest(request);

				repository.save(response);
			}

			// Response Count
			int responseCount = user.getResponseCount();  
			user.setResponseCount(++responseCount);
			userRepository.save(user);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", verifyRequest);
//			mapNew.put("Request", request);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	
	
	
	@Override
	public ResponseStructure gstVerification(RequestModel dto) {
		ResponseStructure structure = new ResponseStructure();
		try {

			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);
			System.out.println("UserDecryption  : " + userDecryption);

			JSONObject userJosn = new JSONObject(userDecryption);
			boolean filingStatus = userJosn.getBoolean("filing_status_get");
			String reference_id = userJosn.getString("reference_id");
			System.out.println("Reference Id : " + reference_id);
			String source = userJosn.getString("source");
			System.out.println("Source : " + source);
			String source_type = userJosn.getString("source_type");

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 2) {
						return gstNumberVerification(dto, userModel, serviceModel, reference_id, source, source_type,
								filingStatus, userSecretKey, vendorModel, merchantPriceModel, serviceModel);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}
				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 2) {
						return gstNumberVerification(dto, userModel, serviceModel, reference_id, source, source_type,
								filingStatus, userSecretKey, vendorModel, merchantPriceModel, serviceModel);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}
				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	
	private ResponseStructure gstNumberVerification(RequestModel dto, EntityModel user, VendorVerificationModel service,
			String referenceNumber, String source, String source_type, boolean filingStatus, String userSecretKey,
			VendorModel vendorModel, MerchantPriceModel merchantPriceModel, VendorVerificationModel serviceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {
			
			// Request Count
			int count = user.getRequestCount() + 1;
			user.setRequestCount(count);

			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			// TAKE VENDOR & VENDORVERIFICATION FROM MERCHANT PRICE MODEL
			Request request = new Request();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			request.setReferenceId(referenceNumber);
			request.setSource(source);
			request.setSourceType(source_type);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(new Date());
			request.setPrice(merchantPriceModel.getIdPrice());

			inputParams.put("reference_id", referenceNumber);
			inputParams.put("source_type", "id");
			inputParams.put("source", source);
			inputParams.put("filing_status_get", true);

			System.out.println("Reference id in = " + inputParams.toString());
			String key = AppConstants.ENCRYPTION_KEY;
			// String jsonString = inputParams.toString();
			String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);
			System.out.println("Encryption Json =" + encryptedJson);

			encryptDatas.put("api_data", encryptedJson);
			encryptDatas.put("enc_mode", "symmetric");
			encryptDatas.put("is_encrypted", true);

			// create an instance of RestTemplate
			RestTemplate restTemplate = new RestTemplate();

			// create headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.add("x-parse-rest-api-key", vendorPrice.getApiKey());
			headers.add("x-parse-application-id", vendorPrice.getApplicationId());
			headers.add("Content-Type", AppConstants.CONTENT_TYPE);

			//VendorReq
			int reqCount=vendorModel.getVendorRequest()+1;			
			vendorModel.setVendorRequest(reqCount);
			
			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();
			
			 //VendorResponse
			 int respCount=vendorModel.getVendorResponse()+1;
			 vendorModel.setVendorResponse(respCount);

			// Convert the string to a JSONObject
			JSONObject jsonObject = new JSONObject(data);

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			String status = jsonObject.getString("status");

			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			System.out.println("status: " + status);
			//
			String decryptData = PasswordUtils.decryptString(encryptedResponse, key);
			// String decryptData=PasswordUtils.jsonDecryption(encryptedResponse);

			Response response = new Response();

			JSONObject decryptJson = new JSONObject(decryptData);

			String statuss = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			System.out.println("Decrypted Response = " + decryptData);

			response.setReferenceId(referenceId);
			response.setStatus(statuss);
			response.setSourceType(source_type);
			response.setSource(source);
			response.setRequestDateAndTime(new Date());
			response.setRequestBy(dto.getRequestBy());
			response.setUser(user);
			response.setVendorModel(vendorModel);
			response.setRequest(request);

			JSONObject object = new JSONObject();
			object.put("status", statuss);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
			System.out.println("RETURN RESPONSE : " + returnResponse);

			// Prepaid Amount Reduction
			if (user.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = user.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = user.getConsumedAmount() + merchantPriceModel.getIdPrice();

				user.setRemainingAmount(remainingAmount);
				user.setConsumedAmount(consumedAmount);
				user.setPaymentStatus("No Dues");
			}

			if (statuss.equals("success")) {

				String message = decryptJson.getString("message");
				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");
				String gstIn = validatedJson.getString("gstin");
				String businessName = validatedJson.getString("business_name");
				String transactionId = decryptJson.getString("transaction_id");
				String dateOfReg = validatedJson.getString("date_of_registration");
				String dateOfCancel = validatedJson.getString("date_of_cancellation");

				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setUser(user);
				request.setResponseDateAndTime(responseTimeStamp);
				request.setFilingStatus(filingStatus);
				request.setVerificationModel(service);
				reqRepository.save(request);

				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setResponseDateAndTime(responseTimeStamp);
				response.setGstIn(gstIn);
				response.setBusinessName(businessName);
				response.setFilingStatus(filingStatus);
				response.setDateOfRegistration(dateOfReg);
				response.setDateOfCancellation(dateOfCancel);
				response.setRequest(request);

				repository.save(response);
			} else {
				String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(statuss);
				request.setResponseDateAndTime(responseTimeStamp);
				request.setUser(user);
				request.setError(error);
				request.setErrorCode(errorCode);
				request.setVerificationModel(service);
				reqRepository.save(request);

				response.setResponse(decryptData);
				response.setError(error);
				response.setErrorCode(errorCode);
				response.setResponseDateAndTime(responseTimeStamp);
				response.setRequest(request);

				repository.save(response);
			}

			// Response Count
			int responseCount = user.getResponseCount();
			user.setResponseCount(++responseCount);
			userRepository.save(user);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", verifyRequest);
//			mapNew.put("Request", request);

			structure.setData(mapNew);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}
	
	
	
	
	@Override
	public ResponseStructure aadharXmlVerification(RequestModel dto) {

		ResponseStructure structure = new ResponseStructure();
		try {

			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String UserDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);
			System.out.println("UserDecryption  : " + UserDecryption);
			JSONObject userJosn = new JSONObject(UserDecryption);
			String reference_id = userJosn.getString("reference_id");
			System.out.println("Reference Id : " + reference_id);
			String source = userJosn.getString("source");
			System.out.println("Source : " + source);
			String source_type = userJosn.getString("source_type");

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 6) {
						return aadharXmlVerificationProcess(dto, userModel, serviceModel, reference_id, source, source_type,
								userSecretKey);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}
				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 6) {
						return aadharXmlVerificationProcess(dto, userModel, serviceModel, reference_id, source, source_type,
								userSecretKey);
					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}
				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}


	private ResponseStructure aadharXmlVerificationProcess(RequestModel dto, EntityModel userModel,
			VendorVerificationModel serviceModel, String reference_id, String source, String source_type,
			String userSecretKey) {
		ResponseStructure structure = new ResponseStructure();
		try {

			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);
			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			            // Request Count
						int count = userModel.getRequestCount() + 1;
						userModel.setRequestCount(count);
			
			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// ? String referenceNumber=FileUtils.getRandomOTPnumber(10);

			inputParams.put("reference_id", reference_id);
			inputParams.put("source_type", "id");
			inputParams.put("source", source);

			System.out.println("Reference id in = " + inputParams.toString());
			String key = AppConstants.ENCRYPTION_KEY;
			// String jsonString = inputParams.toString();
			String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);
			System.out.println("Encryption Json =" + encryptedJson);

			encryptDatas.put("api_data", encryptedJson);
			encryptDatas.put("enc_mode", "symmetric");
			encryptDatas.put("is_encrypted", true);

			// create an instance of RestTemplate
			RestTemplate restTemplate = new RestTemplate();

			// create headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
			headers.add("x-parse-rest-api-key", vendorPrice.getApiKey());
			headers.add("x-parse-application-id", vendorPrice.getApplicationId());
			headers.add("Content-Type", AppConstants.CONTENT_TYPE);

			//VendorReq
			int reqCount=vendorModel.getVendorRequest()+1;			
			vendorModel.setVendorRequest(reqCount);
			
			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();
			
			 //VendorResponse
			 int respCount=vendorModel.getVendorResponse()+1;
			 vendorModel.setVendorResponse(respCount);

			// Convert the string to a JSONObject
			JSONObject jsonObject = new JSONObject(data);

			Request request = new Request();
			Date reqDate = new Date();

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			String status = jsonObject.getString("status");
			//
			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			System.out.println("status: " + status);
			//
			String decryptData = PasswordUtils.decryptString(encryptedResponse, key);
			// String decryptData=PasswordUtils.jsonDecryption(encryptedResponse);
			JSONObject decryptJson = new JSONObject(decryptData);
			//
			System.out.println("Decrypted Response = " + decryptData);

			String responseStatus = decryptJson.getString("status");

			String referenceId = decryptJson.getString("reference_id");
			String responseTime = decryptJson.getString("response_time_stamp");

			request.setReferenceId(referenceId);

			request.setSource(source);
			request.setSourceType(source_type);
			request.setRequestBy(userModel.getName());
			request.setRequestDateAndTime(reqDate);
			request.setStatus(responseStatus);
			request.setResponseDateAndTime(responseTime);
			request.setUser(userModel);
			request.setVerificationModel(serviceModel);
			request.setPrice(merchantPriceModel.getIdPrice());

			Response response = new Response();

			response.setReferenceId(referenceId);
			response.setSource(source);
			response.setSourceType(source_type);
			response.setRequestBy(userModel.getName());
			response.setRequestDateAndTime(reqDate);
			response.setStatus(responseStatus);
			response.setResponseDateAndTime(responseTime);
			response.setRequest(request);
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			//response.setResponse(decryptData);

			if (responseStatus.equals("success")) {
				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");
				request.setMessage(message);
				request.setTransactionId(transactionId);
				response.setTransactionId(transactionId);

				response.setMessage(message);
				reqRepository.save(request);
				repository.save(response);

			} else {
				String errorCode = decryptJson.getString("error_code");
				request.setErrorCode(errorCode);
				response.setErrorCode(errorCode);
				repository.save(response);
				reqRepository.save(request);

			}
			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);
			object.put("request_id", request.getRequestId());
			object.put("response_id", response.getResponseId());

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
			System.out.println("RETURN RESPONSE : " + returnResponse);

			//
			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setData(mapNew);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure aadhaarOtpVerification(RequestModel dto) {
		ResponseStructure structure = new ResponseStructure();
		try {

			if (dto.getVendorVerificationId() == 5) {
				EntityModel userModel = userRepository.findByUserId(dto.getUserId());
				String userSecretKey = userModel.getSecretKey();

				VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
				VendorVerificationModel serviceModel = verificationRepository
						.findByVendorVerificationId(dto.getVendorVerificationId());
				MerchantPriceModel merchantPriceModel = merchantPriceRepository
						.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel,
								userModel);
				VendorPriceModel vendorPrice = vendorPriceRepository
						.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

				Response response = repository.findByResponseId(dto.getResponseId());
				Request request = reqRepository.findByRequestId(dto.getRequestId());

				String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);
				JSONObject json = new JSONObject(userDecryption);

				String referenceId = json.getString("reference_id");
				String transactionId = json.getString("transaction_id");
				String otp = json.getString("otp");

				JSONObject inputParams = new JSONObject();
				JSONObject encryptDatas = new JSONObject();

				// String referenceNumber=FileUtils.getRandomOTPnumber(10);

				inputParams.put("reference_id", referenceId);
				inputParams.put("transaction_id", transactionId);
				inputParams.put("otp", otp);

				System.out.println("Reference id in = " + inputParams.toString());
				String key = AppConstants.ENCRYPTION_KEY;
				// String jsonString = inputParams.toString();
				String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);
				System.out.println("Encryption Json =" + encryptedJson);

				encryptDatas.put("api_data", encryptedJson);
				encryptDatas.put("enc_mode", "symmetric");
				encryptDatas.put("is_encrypted", true);

				// create an instance of RestTemplate
				RestTemplate restTemplate = new RestTemplate();

				// create headers
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				headers.add("x-parse-rest-api-key", vendorPrice.getApiKey());
				headers.add("x-parse-application-id", vendorPrice.getApplicationId());
				headers.add("Content-Type", AppConstants.CONTENT_TYPE);
				
				//VendorReq
				int reqCount=vendorModel.getVendorRequest()+1;			
				vendorModel.setVendorRequest(reqCount);

				HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

				ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
						String.class);
				String data = clientResponse.getBody();

				 //VendorResponse
				 int respCount=vendorModel.getVendorResponse()+1;
				 vendorModel.setVendorResponse(respCount);
				
				// Convert the string to a JSONObject
				JSONObject jsonObject = new JSONObject(data);

				// Extract specific data
				String encryptedResponse = jsonObject.getString("encrypted_response");
				String status = jsonObject.getString("status");
				//
				// // Print the extracted data
				System.out.println("encrypted_response: " + encryptedResponse);
				System.out.println("status: " + status);
				//
				String decryptData = PasswordUtils.decryptString(encryptedResponse, key);
				// String decryptData=PasswordUtils.jsonDecryption(encryptedResponse);
				JSONObject decryptJson = new JSONObject(decryptData);
				//
				System.out.println("Decrypted Response = " + decryptData);

				String responseTime = decryptJson.getString("response_time_stamp");

				String statusResponse = decryptJson.getString("status");

				request.setStatus(statusResponse);
				request.setOtp(otp);
				
				response.setStatus(statusResponse);

				JSONObject object = new JSONObject();
				object.put("status", status);
				object.put("encrypted_response", decryptData);

				String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
				System.out.println("RETURN RESPONSE : " + returnResponse);

				System.out.println("Status : " + status);
				System.out.println("Status Response :" + statusResponse);

				if (statusResponse.equals("success")) {

					JSONObject resultJson = decryptJson.getJSONObject("result");
					JSONObject validatedData = resultJson.getJSONObject("validated_data");

					//String fullName = validatedData.getString("full_name");
					//String aadharNumber = validatedData.getString("aadhaar_number");
					//String dob = validatedData.getString("dob");

					String message = decryptJson.getString("message");
					String transaction = decryptJson.getString("transaction_id");

					request.setTransactionId(transaction);
					request.setMessage(message);
					request.setResponseDateAndTime(responseTime);
					//request.setFullName(fullName);
					//request.setAadharNumber(aadharNumber);
					//request.setDob(dob);
					reqRepository.save(request);

					response.setTransactionId(transaction);
					response.setMessage(message);
					response.setResponseDateAndTime(responseTime);
					//response.setResponse(decryptData);
					response.setRequest(request);
					//response.setFullName(fullName);
					//response.setAadhaarNumber(aadharNumber);
					//response.setDob(dob);
					repository.save(response);
				} else {
					// String error = decryptJson.getString("error");
					String errorCode = decryptJson.getString("error_code");

					request.setResponseDateAndTime(responseTime);
					// request.setError(error);
					request.setErrorCode(errorCode);
					reqRepository.save(request);

					response.setResponse(decryptData);
					// response.setError(error);
					response.setErrorCode(errorCode);
					response.setResponseDateAndTime(responseTime);
					response.setRequest(request);

					repository.save(response);

				}
				
				vendorRepository.save(vendorModel);

				Map<String, Object> mapNew = new HashMap<>();
				mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

				structure.setData(mapNew);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setMessage(AppConstants.SUCCESS);
			} else {
				structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}
	
	
	
	
	@Override
	public ResponseStructure cinVerification(RequestModel dto) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(userDecryption);
			
			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 9) {

						return cinVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
								userSecretKey, vendorModel, merchantPriceModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 9) {

						return cinVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
								userSecretKey, vendorModel, merchantPriceModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return structure;
		}

		return structure;
	}

	private ResponseStructure cinVerificationProcess(RequestModel dto, EntityModel user, String reference_Id,
			VendorVerificationModel serviceModel, String source, String sourceType, String userSecretKey,
			VendorModel vendorModel, MerchantPriceModel merchantPriceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {

			 // Request Count
			int count = user.getRequestCount() + 1;
			user.setRequestCount(count);
			
			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			Request request = new Request();
			Date reqDate = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// set request details

			inputParams.put("reference_id", reference_Id);
			inputParams.put("source_type", sourceType);
			inputParams.put("source", source);

			String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);

			encryptDatas.put("api_data", encryptedJson);
			encryptDatas.put("enc_mode", "symmetric");
			encryptDatas.put("is_encrypted", true);

			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			headers.add("x-parse-rest-api-key", vendorPrice.getApiKey());
			headers.add("x-parse-application-id", vendorPrice.getApplicationId());
			headers.add("Content-Type", AppConstants.CONTENT_TYPE);

			//VendorReq
			int reqCount=vendorModel.getVendorRequest()+1;			
			vendorModel.setVendorRequest(reqCount);
			
			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();

			 //VendorResponse
			 int respCount=vendorModel.getVendorResponse()+1;
			 vendorModel.setVendorResponse(respCount);
			
			JSONObject jsonObject = new JSONObject(data);

			Response response = new Response();

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			//
			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			//

			String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);
			// System.out.println("RESPONSE : " + decryptData);

			JSONObject decryptJson = new JSONObject(decryptData);

			String status = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			// Prepaid Amount Reduction
			if (user.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = user.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = user.getConsumedAmount() + merchantPriceModel.getIdPrice();

				user.setRemainingAmount(remainingAmount);
				user.setConsumedAmount(consumedAmount);
				user.setPaymentStatus("No Dues");
			}

			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSource(source);
			request.setSourceType(sourceType);
			request.setPrice(merchantPriceModel.getIdPrice());
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);

			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(sourceType);
			response.setSource(source);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(user);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);
			System.out.println("RETURN RESPONSE : " + returnResponse);

			if (status.equals("success")) {

				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");

				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");

				String companyName = validatedJson.getString("company_name");
				String companyId = validatedJson.getString("company_id");
				String companyType = validatedJson.getString("company_type");

				JSONObject details = validatedJson.getJSONObject("details");
				JSONObject companyInfo = details.getJSONObject("company_info");
				String email = companyInfo.getString("email_id");

				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setCompanyName(companyName);
				request.setCompanyId(companyId);
				request.setCompanyType(companyType);
				request.setEmail(email);
				request.setUser(user);
				request.setVerificationModel(serviceModel);
				reqRepository.save(request);

				response.setBusinessName(companyName);
				response.setCompanyId(companyId);
				response.setCompanyType(companyType);
				response.setEmail(email);
				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setRequest(request);
				repository.save(response);

			} else {

				String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				request.setUser(user);
				request.setError(error);
				request.setErrorCode(errorCode);
				request.setVerificationModel(serviceModel);
				reqRepository.save(request);

				response.setResponse(decryptData);
				response.setError(error);
				response.setErrorCode(errorCode);
				response.setRequest(request);

				repository.save(response);

			}

			// Response Count
			int responseCount = user.getResponseCount();
			user.setResponseCount(++responseCount);
			userRepository.save(user);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setData(mapNew);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	
	@Override
	public ResponseStructure dinVerification(RequestModel dto) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(userDecryption);
			
			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 10) {

						return dinVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
								userSecretKey, vendorModel, merchantPriceModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 10) {

						return dinVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
								userSecretKey, vendorModel, merchantPriceModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	
	private ResponseStructure dinVerificationProcess(RequestModel dto, EntityModel userModel, String reference_Id,
			VendorVerificationModel serviceModel, String source, String sourceType, String userSecretKey,
			VendorModel vendorModel, MerchantPriceModel merchantPriceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {

			 // Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);
			
			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			Request request = new Request();
			Date reqDate = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// set request details

			inputParams.put("reference_id", reference_Id);
			inputParams.put("source_type", sourceType);
			inputParams.put("source", source);

			String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);

			encryptDatas.put("api_data", encryptedJson);
			encryptDatas.put("enc_mode", "symmetric");
			encryptDatas.put("is_encrypted", true);

			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			headers.add("x-parse-rest-api-key", vendorPrice.getApiKey());
			headers.add("x-parse-application-id", vendorPrice.getApplicationId());
			headers.add("Content-Type", AppConstants.CONTENT_TYPE);

			//VendorReq
			int reqCount=vendorModel.getVendorRequest()+1;			
			vendorModel.setVendorRequest(reqCount);
			
			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();
			
			 //VendorResponse
			 int respCount=vendorModel.getVendorResponse()+1;
			 vendorModel.setVendorResponse(respCount);

			JSONObject jsonObject = new JSONObject(data);

			Response response = new Response();

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			//
			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			//

			String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

			JSONObject decryptJson = new JSONObject(decryptData);

			String status = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			System.out.println("RESPONSE :  " + decryptData);

			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}

			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSource(source);
			request.setSourceType(sourceType);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);
			request.setPrice(merchantPriceModel.getIdPrice());

			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(sourceType);
			response.setSource(source);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);

			if (status.equals("success")) {

				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");

				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");

				String email = validatedJson.getString("email");
				// String pan = validatedJson.getString("pan_number");
				String address = validatedJson.getString("permanent_address");

				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setEmail(email);
				request.setUser(userModel);
				request.setVerificationModel(serviceModel);
				reqRepository.save(request);

				response.setEmail(email);
				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setRequest(request);
				response.setAddress(address);
				// response.setPanNumber(pan);
				repository.save(response);

			} else {

				// {"status":"failed","reference_id":"9511134592","response_time_stamp":"2023-08-30T09:50:37","message":"Backend
				// Timed Out. Try Again.","error_code":"kyc_028"}

				// String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				request.setUser(userModel);
				// request.setError(error);
				request.setErrorCode(errorCode);
				request.setVerificationModel(serviceModel);
				reqRepository.save(request);

				response.setResponse(decryptData);
				// response.setError(error);
				response.setErrorCode(errorCode);
				response.setRequest(request);

				repository.save(response);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	
	
	@Override
	public ResponseStructure msmeVerification(RequestModel dto) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(userDecryption);
			
			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 7) {

						return msmeVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
								userSecretKey, vendorModel, merchantPriceModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 7) {

						return msmeVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
								userSecretKey, vendorModel, merchantPriceModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	
	private ResponseStructure msmeVerificationProcess(RequestModel dto, EntityModel userModel, String reference_Id,
			VendorVerificationModel serviceModel, String source, String sourceType, String userSecretKey,
			VendorModel vendorModel, MerchantPriceModel merchantPriceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {

			 // Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);
			
			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			Request request = new Request();
			Date reqDate = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// set request details

			inputParams.put("reference_id", reference_Id);
			inputParams.put("source_type", sourceType);
			inputParams.put("source", source);

			String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);

			encryptDatas.put("api_data", encryptedJson);
			encryptDatas.put("enc_mode", "symmetric");
			encryptDatas.put("is_encrypted", true);

			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			headers.add("x-parse-rest-api-key", vendorPrice.getApiKey());
			headers.add("x-parse-application-id", vendorPrice.getApplicationId());
			headers.add("Content-Type", AppConstants.CONTENT_TYPE);
			
			//VendorReq
			int reqCount=vendorModel.getVendorRequest()+1;			
			vendorModel.setVendorRequest(reqCount);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();
			
			 //VendorResponse
			 int respCount=vendorModel.getVendorResponse()+1;
			 vendorModel.setVendorResponse(respCount);

			JSONObject jsonObject = new JSONObject(data);

			Response response = new Response();

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			//
			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			//

			String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

			System.out.println("RESPONSE :  " + decryptData);

			JSONObject decryptJson = new JSONObject(decryptData);

			String status = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}

			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSourceType(sourceType);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);
			request.setPrice(merchantPriceModel.getIdPrice());

			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(sourceType);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);

			if (status.equals("success")) {

				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");

				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");

				String sourceId = validatedJson.getString("uan");
				JSONObject mainDetails = validatedJson.getJSONObject("main_details");

				String nameOfEnterprise = mainDetails.getString("name_of_enterprise");
				String email = mainDetails.getString("email");

				request.setSource(sourceId);
				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setCompanyName(nameOfEnterprise);
				request.setEmail(email);
				request.setUser(userModel);
				request.setVerificationModel(serviceModel);
				reqRepository.save(request);

				response.setSource(sourceId);
				response.setBusinessName(nameOfEnterprise);
				response.setEmail(email);
				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setRequest(request);
				repository.save(response);

			} else {

				String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				request.setUser(userModel);
				request.setError(error);
				request.setErrorCode(errorCode);
				request.setVerificationModel(serviceModel);

				reqRepository.save(request);

				response.setResponse(decryptData);
				response.setError(error);
				response.setErrorCode(errorCode);
				response.setRequest(request);

				repository.save(response);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	
	
	@Override
	public ResponseStructure rcVerification(RequestModel dto) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(userDecryption);
			
			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 8) {

						return rcVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
								userSecretKey, vendorModel, merchantPriceModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 8) {

						return rcVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
								userSecretKey, vendorModel, merchantPriceModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}



	private ResponseStructure rcVerificationProcess(RequestModel dto, EntityModel userModel, String reference_Id,
			VendorVerificationModel serviceModel, String source, String sourceType, String userSecretKey,
			VendorModel vendorModel, MerchantPriceModel merchantPriceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {

			 // Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);
			
			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			Request request = new Request();
			Date reqDate = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// set request details

			inputParams.put("reference_id", reference_Id);
			inputParams.put("source_type", sourceType);
			inputParams.put("source", source);

			String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);

			encryptDatas.put("api_data", encryptedJson);
			encryptDatas.put("enc_mode", "symmetric");
			encryptDatas.put("is_encrypted", true);

			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			headers.add("x-parse-rest-api-key", vendorPrice.getApiKey());
			headers.add("x-parse-application-id", vendorPrice.getApplicationId());
			headers.add("Content-Type", AppConstants.CONTENT_TYPE);

			//VendorReq
			int reqCount=vendorModel.getVendorRequest()+1;			
			vendorModel.setVendorRequest(reqCount);
			
			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();
			
			 //VendorResponse
			 int respCount=vendorModel.getVendorResponse()+1;
			 vendorModel.setVendorResponse(respCount);

			JSONObject jsonObject = new JSONObject(data);
			
			System.out.println("JSON OBJ : "+jsonObject);

			Response response = new Response();

			System.out.println("JSON OBJ : " + jsonObject);

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");

			System.out.println("encrypted_response: " + encryptedResponse);

			String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

			JSONObject decryptJson = new JSONObject(decryptData);

			String status = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}

			request.setSource(source);
			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSourceType(sourceType);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);
			request.setPrice(merchantPriceModel.getIdPrice());

			response.setSource(source);
			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(sourceType);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);

			if (status.equals("success")) {

				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");

				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");

				String address = validatedJson.getString("permanent_address");
				String fullName = validatedJson.getString("owner_name");

				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setFullName(fullName);
				request.setUser(userModel);
				request.setVerificationModel(serviceModel);
				reqRepository.save(request);

				response.setFullName(fullName);
				response.setAddress(address);
				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setRequest(request);
				repository.save(response);

			} else {

				String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				request.setUser(userModel);
				request.setError(error);
				request.setErrorCode(errorCode);
				request.setVerificationModel(serviceModel);
				reqRepository.save(request);

				response.setResponse(decryptData);
				response.setError(error);
				response.setErrorCode(errorCode);
				response.setRequest(request);

				repository.save(response);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	
	
	@Override
	public ResponseStructure drivingLicenceId(RequestModel dto) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(userDecryption);
			
			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");
			String dob = userJosn.getString("dob");

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 4) {

						return drivingLicenceIdVerificationProcess(dto, userModel, reference_id, serviceModel, source,
								source_type, userSecretKey, dob, vendorModel, merchantPriceModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 4) {

						return drivingLicenceIdVerificationProcess(dto, userModel, reference_id, serviceModel, source,
								source_type, userSecretKey, dob, vendorModel, merchantPriceModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}



	private ResponseStructure drivingLicenceIdVerificationProcess(RequestModel dto, EntityModel userModel,
			String reference_Id, VendorVerificationModel serviceModel, String source, String sourceType,
			String userSecretKey, String dob, VendorModel vendorModel, MerchantPriceModel merchantPriceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {

			 // Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);
			
			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			Request request = new Request();
			Date reqDate = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// set request details

			inputParams.put("reference_id", reference_Id);
			inputParams.put("source_type", sourceType);
			inputParams.put("source", source);
			inputParams.put("dob", dob);

			String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);

			encryptDatas.put("api_data", encryptedJson);
			encryptDatas.put("enc_mode", "symmetric");
			encryptDatas.put("is_encrypted", true);

			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			//VendorReq
			int reqCount=vendorModel.getVendorRequest()+1;			
			vendorModel.setVendorRequest(reqCount);
			
			headers.add("x-parse-rest-api-key", vendorPrice.getApiKey());
			headers.add("x-parse-application-id", vendorPrice.getApplicationId());
			headers.add("Content-Type", AppConstants.CONTENT_TYPE);

			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();
			
			 //VendorResponse
			 int respCount=vendorModel.getVendorResponse()+1;
			 vendorModel.setVendorResponse(respCount);

			JSONObject jsonObject = new JSONObject(data);

			Response response = new Response();

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");
			//
			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);
			//

			String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

			System.out.println("RESPONSE :  " + decryptData);

			JSONObject decryptJson = new JSONObject(decryptData);

			String status = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}
			request.setPrice(merchantPriceModel.getIdPrice());
			request.setSource(source);
			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSourceType(sourceType);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);

			response.setSource(source);
			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(sourceType);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);

			if (status.equals("success")) {

				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");

				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");

				String fullName = validatedJson.getString("name");
				String dateOfBirth = validatedJson.getString("dob");
				String address = validatedJson.getString("permanent_address");
				String state = validatedJson.getString("state");

				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setFullName(fullName);
				request.setDob(dateOfBirth);
				request.setState(state);
				request.setUser(userModel);
				request.setVerificationModel(serviceModel);
				reqRepository.save(request);

				response.setFullName(fullName);
				response.setDob(dateOfBirth);
				response.setState(state);
				response.setAddress(address);
				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setRequest(request);
				repository.save(response);

			} else {

				// {"status":"failed","reference_id":"2984265558","response_time_stamp":"2023-08-30T10:42:39","message":"Internal
				// Server Error Occurred","error_code":"kyc_028"}

				// String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				request.setUser(userModel);
				// request.setError(error);
				request.setErrorCode(errorCode);
				request.setVerificationModel(serviceModel);
				reqRepository.save(request);

				response.setResponse(decryptData);
				// response.setError(error);
				response.setErrorCode(errorCode);
				response.setRequest(request);

				repository.save(response);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

	
	
	@Override
	public ResponseStructure passportId(RequestModel dto) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(dto.getUserId());
			VendorVerificationModel serviceModel = verificationRepository
					.findByVendorVerificationId(dto.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(dto.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, serviceModel, userModel);

			String userSecretKey = userModel.getSecretKey();
			String userDecryption = PasswordUtils.demoDecrypt(dto.getEncrypted_data(), userSecretKey);

			JSONObject userJosn = new JSONObject(userDecryption);
			
			String reference_id = userJosn.getString("reference_id");
			String source = userJosn.getString("source");
			String source_type = userJosn.getString("source_type");
			String dob = userJosn.getString("dob");

			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

					if (dto.getVendorVerificationId() == 3) {

						return passportIdVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
								userSecretKey, dob, vendorModel, merchantPriceModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					userModel.setPaymentStatus("Dues");
					userRepository.save(userModel);

					structure.setMessage("Please Recharge Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					if (dto.getVendorVerificationId() == 3) {

						return passportIdVerificationProcess(dto, userModel, reference_id, serviceModel, source, source_type,
								userSecretKey, dob, vendorModel, merchantPriceModel);

					} else {
						structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
					}

				} else {
					structure.setMessage("Please pay the Amount");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}



	private ResponseStructure passportIdVerificationProcess(RequestModel dto, EntityModel userModel,
			String reference_Id, VendorVerificationModel serviceModel, String source, String sourceType,
			String userSecretKey, String dob, VendorModel vendorModel, MerchantPriceModel merchantPriceModel) {

		ResponseStructure structure = new ResponseStructure();
		try {

			 // Request Count
			int count = userModel.getRequestCount() + 1;
			userModel.setRequestCount(count);
			
			VendorPriceModel vendorPrice = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, serviceModel);

			Request request = new Request();
			Date reqDate = new Date();

			JSONObject inputParams = new JSONObject();
			JSONObject encryptDatas = new JSONObject();

			// set request details

			inputParams.put("reference_id", reference_Id);
			inputParams.put("source_type", sourceType);
			inputParams.put("source", source);
			inputParams.put("dob", dob);

			String encryptedJson = PasswordUtils.demoEncryptionECB(inputParams);

			encryptDatas.put("api_data", encryptedJson);
			encryptDatas.put("enc_mode", "symmetric");
			encryptDatas.put("is_encrypted", true);

			RestTemplate restTemplate = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			headers.add("x-parse-rest-api-key", vendorPrice.getApiKey());
			headers.add("x-parse-application-id", vendorPrice.getApplicationId());
			headers.add("Content-Type", AppConstants.CONTENT_TYPE);

			//VendorReq
			int reqCount=vendorModel.getVendorRequest()+1;			
			vendorModel.setVendorRequest(reqCount);
			
			HttpEntity<String> entity = new HttpEntity<>(encryptDatas.toString(), headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(vendorPrice.getApiLink(), entity,
					String.class);
			String data = clientResponse.getBody();
			
			 //VendorResponse
			 int respCount=vendorModel.getVendorResponse()+1;
			 vendorModel.setVendorResponse(respCount);

			JSONObject jsonObject = new JSONObject(data);

			Response response = new Response();

			// Extract specific data
			String encryptedResponse = jsonObject.getString("encrypted_response");

			// // Print the extracted data
			System.out.println("encrypted_response: " + encryptedResponse);

			String decryptData = PasswordUtils.decryptString(encryptedResponse, AppConstants.ENCRYPTION_KEY);

			JSONObject decryptJson = new JSONObject(decryptData);

			System.out.println("RESPONSE : " + decryptData);

			String status = decryptJson.getString("status");
			String responseTimeStamp = decryptJson.getString("response_time_stamp");
			String referenceId = decryptJson.getString("reference_id");

			// Prepaid Amount Reduction
			if (userModel.getPaymentMethod().getPaymentId() == 2) {
				double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getIdPrice();
				double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getIdPrice();

				userModel.setRemainingAmount(remainingAmount);
				userModel.setConsumedAmount(consumedAmount);
				userModel.setPaymentStatus("No Dues");
			}

			request.setSource(source);
			request.setStatus(status);
			request.setResponseDateAndTime(responseTimeStamp);
			request.setReferenceId(referenceId);
			request.setSourceType(sourceType);
			request.setRequestBy(dto.getRequestBy());
			request.setRequestDateAndTime(reqDate);
			request.setPrice(merchantPriceModel.getIdPrice());

			response.setSource(source);
			response.setReferenceId(referenceId);
			response.setStatus(status);
			response.setSourceType(sourceType);
			response.setRequestDateAndTime(reqDate);
			response.setRequestBy(dto.getRequestBy());
			response.setUser(userModel);
			response.setVendorModel(vendorModel);
			response.setResponseDateAndTime(responseTimeStamp);

			JSONObject object = new JSONObject();
			object.put("status", status);
			object.put("encrypted_response", decryptData);

			String returnResponse = PasswordUtils.demoEncryption(object, userSecretKey);

			if (status.equals("success")) {

				String message = decryptJson.getString("message");
				String transactionId = decryptJson.getString("transaction_id");

				JSONObject resultJson = decryptJson.getJSONObject("result");
				JSONObject validatedJson = resultJson.getJSONObject("validated_data");
				JSONObject extractedData = resultJson.getJSONObject("extracted_data");

				String dateOfBirth = extractedData.getString("dob");
				String address = extractedData.getString("address");
				String fullName = validatedJson.getString("full_name");

				request.setMessage(message);
				request.setTransactionId(transactionId);
				request.setStatus(status);
				request.setFullName(fullName);
				request.setDob(dateOfBirth);
				request.setUser(userModel);
				request.setVerificationModel(serviceModel);
				reqRepository.save(request);

				response.setFullName(fullName);
				response.setDob(dateOfBirth);
				response.setAddress(address);
				response.setTransactionId(transactionId);
				response.setMessage(message);
				response.setResponse(decryptData);
				response.setEncryptedJson(encryptedJson);
				response.setRequest(request);
				repository.save(response);

			} else {

				String error = decryptJson.getString("error");
				String errorCode = decryptJson.getString("error_code");

				request.setStatus(status);
				request.setUser(userModel);
				request.setError(error);
				request.setErrorCode(errorCode);
				request.setVerificationModel(serviceModel);
				reqRepository.save(request);

				response.setResponse(decryptData);
				response.setError(error);
				response.setErrorCode(errorCode);
				response.setRequest(request);

				repository.save(response);

			}

			// Response Count
			int responseCount = userModel.getResponseCount();
			userModel.setResponseCount(++responseCount);
			userRepository.save(userModel);
			vendorRepository.save(vendorModel);

			Map<String, Object> mapNew = new HashMap<>();
			mapNew.put("return_response", returnResponse);
//			mapNew.put("Response", response);
//			mapNew.put("Request", request);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(mapNew);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(2);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

		}
		return structure;
	}

}
