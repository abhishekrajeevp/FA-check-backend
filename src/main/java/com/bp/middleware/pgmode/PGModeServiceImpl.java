package com.bp.middleware.pgmode;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.transaction.TransactionRepository;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@Service
public class PGModeServiceImpl implements PGModeService {

	private static final Logger logger = LoggerFactory.getLogger(PGModeServiceImpl.class);

	@Autowired
	private PGModeRepository repository;

	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	EmailService emailService;

	@Override
	public ResponseStructure getActivePaymentMode() {

		ResponseStructure structure = new ResponseStructure();
		try {
			List<PGModeModel> entities = repository.getByPgOnoffStatus(1);

			if (!entities.isEmpty()) {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entities);
				structure.setMessage("PG ActiveList are....!!! ");
				structure.setFlag(1);
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setMessage("PG ActiveList are Not Found....!!! ");
				structure.setFlag(2);
			}
		} catch (Exception e) {
			logger.info("PGserviceImpl getActivePaymentMode method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
		}
		return structure;
	}

	@Override
	public ResponseStructure createPgOrder(int transactionId, int pgModeId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			logger.info(
					"CREATE ORDER START ---------------------------------------------------------------------------------");

			Optional<TransactionDto> opt = transactionRepository.findByTrancsactionId(transactionId);
			Optional<PGModeModel> model = repository.findById(pgModeId);
			if (opt.isPresent()) {
				TransactionDto orderObj = opt.get();

				JSONObject inputParams = new JSONObject();
				JSONObject customerFields = new JSONObject();

				inputParams.put("merchantOrderNo", orderObj.getPayid().getTrackId());
				inputParams.put("currency", "INR");
				inputParams.put("amount", "" + orderObj.getPayid().getPaidAmount());
				inputParams.put("returnUrl", AppConstants.RETURN_URL);
				inputParams.put("description", "Online Payment");

				customerFields.put("udf1", "");
				customerFields.put("udf2", "");
				customerFields.put("udf3", "");
				customerFields.put("udf4", "");
				customerFields.put("udf5", "");

				inputParams.put("customFields", customerFields);

				String[] hashColumns;
				hashColumns = new String[] { "merchantOrderNo", "currency", "amount", "returnUrl" };

				PGModeModel mode = new PGModeModel();
				if (model.isPresent()) {
					mode = model.get();
				}

				String hashData = mode.getApikey();
				for (int i = 0; i < hashColumns.length; i++) {
					hashData += '|' + inputParams.getString(hashColumns[i]).trim();

				}

				hashData += '|' + mode.getSecretKey();

				String secureHash = getHashCodeFromString(hashData);

				// create an instance of RestTemplate
				RestTemplate restTemplate = new RestTemplate();

				// create headers

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				headers.add("Api-Hash", secureHash);

				headers.add("Api-Key", mode.getApikey());
				headers.add("Content-Type", "application/json");

				logger.info("SECURE HASH : " + secureHash);
				logger.info("API KEY : " + mode.getApikey());

				// build the request
				HttpEntity<String> entity = new HttpEntity<>(inputParams.toString(), headers);

				// send POST request
				ResponseEntity<String> clientResponse = restTemplate.postForEntity(AppConstants.PG_URL, entity,
						String.class);

				String keys = clientResponse.getBody();
				JSONObject json = new JSONObject(keys);
				orderObj.setOrderReference(json.get(AppConstants.ORDER_REFERENCE).toString());
				transactionRepository.save(orderObj);

				Map<String, Object> map = new HashMap<>();
				map.put("transactionId", transactionId);
				map.put(AppConstants.ORDER_REFERENCE, json.get(AppConstants.ORDER_REFERENCE));

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Payment mode has loaded");
				structure.setFlag(1);
				structure.setData(map);

				logger.info(
						"CREATE ORDER END F1---------------------------------------------------------------------------------");

				return structure;

			} else {
				Map<String, Integer> map = new HashMap<>();
				map.put("flag", 2);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Payment mode not found");
				structure.setData(map);
				structure.setFlag(2);

				logger.info(
						"CREATE ORDER END F2---------------------------------------------------------------------------------");

				return structure;

			}

		} catch (Exception e) {
			logger.info(AppConstants.TECHNICAL_ERROR, e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage("Something went wrong while accessing request please try again!");
			structure.setFlag(3);
			structure.setErrorDiscription(e.getLocalizedMessage());
			return structure;
		}
	}

	private static String getHashCodeFromString(String str)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {

		MessageDigest md = MessageDigest.getInstance("SHA-512");
		md.update(str.getBytes("UTF-8"));
		byte byteData[] = md.digest();

		// convert the byte to hex format method 1
		StringBuffer hashCodeBuffer = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			hashCodeBuffer.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		return hashCodeBuffer.toString().toUpperCase();
	}

	@Override
	public ResponseStructure checkRequest(int transactionId, int pgModeId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			logger.info(
					"INITIATE ORDER START---------------------------------------------------------------------------------");

			System.err.println("PG ID    : " + pgModeId);
			System.err.println("Trans ID : " + transactionId);

			Optional<TransactionDto> opt = transactionRepository.findByTrancsactionId(transactionId);
			PGModeModel entity = repository.getById(pgModeId);

			if (opt.isPresent()) {

				TransactionDto orderObj = opt.get();
				JSONObject inputParams = new JSONObject();
				
				inputParams.put("address", orderObj.getPayid().getEntity().getAddress());
				inputParams.put("apiKey", entity.getApikey());
				inputParams.put("city", orderObj.getPayid().getEntity().getCityName());
				inputParams.put("country", "IND");
				inputParams.put("customerEmail", orderObj.getPayid().getEntity().getEmail());
				inputParams.put("customerMobile", orderObj.getPayid().getEntity().getMobileNumber());
				inputParams.put("customerName", orderObj.getPayid().getEntity().getName());
				inputParams.put("deliveryAddress", orderObj.getPayid().getEntity().getAddress());
				inputParams.put("deliveryCity", orderObj.getPayid().getEntity().getCityName());
				inputParams.put("deliveryCountry", "IND");
				inputParams.put("deliveryMobile", orderObj.getPayid().getEntity().getMobileNumber());
				inputParams.put("deliveryName", orderObj.getPayid().getEntity().getName());
				inputParams.put("deliveryPostalCode", orderObj.getPayid().getEntity().getPincode());
				inputParams.put("deliveryRegion", orderObj.getPayid().getEntity().getStateName());
				inputParams.put("orderReference", orderObj.getOrderReference());
				inputParams.put("paymentMethod", "");
				inputParams.put("paymentMode", orderObj.getModeOfPaymentPg().getPaymentCode());
				inputParams.put("postalCode", orderObj.getPayid().getEntity().getPincode());
				inputParams.put("region", orderObj.getPayid().getEntity().getStateName());

				
				logger.info("paymentMode", orderObj.getModeOfPaymentPg().getModeOfPayment());
				logger.info("paymentMethod", orderObj.getModeOfPaymentPg().getPaymentCode());
				logger.info("orderReference", orderObj.getOrderReference());
				logger.info("customerName", orderObj.getPayid().getEntity().getName());
				logger.info("customerEmail", orderObj.getPayid().getEntity().getEmail());
				logger.info("customerMobile", orderObj.getPayid().getEntity().getMobileNumber());
				logger.info("address", orderObj.getPayid().getEntity().getAddress());
				logger.info("postalCode", orderObj.getPayid().getEntity().getPincode());
				logger.info("city", orderObj.getPayid().getEntity().getCityName());
				logger.info("region", orderObj.getPayid().getEntity().getStateName());
				logger.info("country", "IND");
				logger.info("deliveryName", orderObj.getPayid().getEntity().getName());
				logger.info("deliveryMobile", orderObj.getPayid().getEntity().getMobileNumber());
				logger.info("deliveryAddress", orderObj.getPayid().getEntity().getAddress());
				logger.info("deliveryPostalCode", orderObj.getPayid().getEntity().getPincode());
				logger.info("deliveryCity", orderObj.getPayid().getEntity().getCityName());
				logger.info("deliveryRegion", orderObj.getPayid().getEntity().getStateName());
				logger.info("deliveryCountry", "IND");
				logger.info("apiKey", entity.getApikey());

				System.err.println("paymentMode : " + orderObj.getModeOfPaymentPg().getPaymentCode());
				System.err.println("paymentMethod : " + orderObj.getModeOfPaymentPg().getPaymentCode());
				System.err.println("orderReference : " + orderObj.getOrderReference());
				System.err.println("customerName : " + orderObj.getPayid().getEntity().getName());
				System.err.println("customerEmail : " + orderObj.getPayid().getEntity().getEmail());
				System.err.println("customerMobile : " + orderObj.getPayid().getEntity().getMobileNumber());
				System.err.println("address : " + orderObj.getPayid().getEntity().getAddress());
				System.err.println("postalCode : " + orderObj.getPayid().getEntity().getPincode());
				System.err.println("city : " + orderObj.getPayid().getEntity().getCityName());
				System.err.println("region : " + orderObj.getPayid().getEntity().getStateName());
				System.err.println("country : " + "IND");
				System.err.println("deliveryName : " + orderObj.getPayid().getEntity().getName());
				System.err.println("deliveryMobile : " + orderObj.getPayid().getEntity().getMobileNumber());
				System.err.println("deliveryAddress : " + orderObj.getPayid().getEntity().getAddress());
				System.err.println("deliveryPostalCode : " + orderObj.getPayid().getEntity().getPincode());
				System.err.println("deliveryCity : " + orderObj.getPayid().getEntity().getCityName());
				System.err.println("deliveryRegion : " + orderObj.getPayid().getEntity().getStateName());
				System.err.println("deliveryCountry : " + "IND");
				System.err.println("apiKey : " + entity.getApikey());

				String[] hashColumns;
				hashColumns = new String[] {"paymentMode","paymentMethod", "orderReference", "customerName", "customerEmail", "customerMobile",
						"address", "postalCode", "city", "region", "country", "deliveryName", "deliveryMobile",
						"deliveryAddress", "deliveryPostalCode", "deliveryCity", "deliveryRegion", "deliveryCountry",
						"apiKey" };

				Arrays.sort(hashColumns);
				
				System.out.println("start "+hashColumns.length);
				for (int i = 0; i < hashColumns.length; i++) {
					
					System.err.println(hashColumns[i]);
				}
				System.out.println("end");

				String hashData = entity.getApikey();
				for (int i = 0; i < hashColumns.length; i++) {
					hashData += '|' + inputParams.getString(hashColumns[i]).trim();

				}

				hashData += '|' + entity.getSecretKey();
				
				String secureHash = getHashCodeFromString(hashData);

				inputParams.put("secureHash", secureHash);

				System.err.println("HashString : "+hashData);
				System.err.println("HASH : " + secureHash);
				
//				 emailService.sendEmailAdminOTPVerification("abhishek.p@basispay.in","HASHSTRING : "+hashData,
//				"HASHDATA : "+secureHash,"", "www.google.com");

				logger.info("SECURE HASH INITIATE ORDER : " + secureHash);
				logger.info("SECRET KEY INITIATE ORDER : " + entity.getSecretKey());
				logger.info("API KEY INITIATE ORDER : " + entity.getApikey());

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.PAYMENT_MODE_LOADED);
				structure.setData(secureHash);

			} else {
				Map<String, Integer> map = new HashMap<>();
				map.put("flag", 1);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Request has been sent successfully!");
				structure.setData(map);
			}
		} catch (Exception e) {
			logger.info("PGserviceImpl checkRequest method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.ERROR_MESSAGE);
			structure.setErrorDiscription(e.getLocalizedMessage());
		}

		logger.info(
				"INITIATE ORDER END---------------------------------------------------------------------------------");

		return structure;
	}

	@Override
	public ResponseStructure paymentResponse(String referenceNo, boolean status, int pgModeId) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<PGModeModel> pgMode = repository.findById(pgModeId);
			if (pgMode.isPresent()) {
				PGModeModel model = pgMode.get();
				JSONObject inputParams = new JSONObject();
				try {
					inputParams.put("reference", referenceNo);
					inputParams.put("success", status);

				} catch (JSONException e) {
					logger.info("PGserviceImpl paymentResponse method", e);
				}

				String[] hashColumns;
				hashColumns = new String[] { "reference", "success" };

				String hashData = model.getApikey();
				for (int i = 0; i < hashColumns.length; i++) {
					hashData += '|' + inputParams.get(hashColumns[i]).toString().trim();

				}

				hashData += '|' + model.getSecretKey();

				String secureHash = getHashCodeFromString(hashData);

				// create an instance of RestTemplate
				RestTemplate restTemplate = new RestTemplate();

				// create headers
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				headers.add(AppConstants.API_HASH, secureHash);
				headers.add(AppConstants.PG_API_KEY, model.getApikey());
				headers.add("Content-Type", "application/json");

				// build the request
				HttpEntity<String> entity = new HttpEntity<>(inputParams.toString(), headers);

				// send POST request
				ResponseEntity<String> clientResponse = restTemplate.postForEntity(AppConstants.PG_RES_URL, entity,
						String.class);

				String keys = clientResponse.getBody();

				structure.setData(keys);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Success");

			} else {
				Map<String, Integer> map = new HashMap<>();
				map.put("flag", 1);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Request has been sent successfully!");
				structure.setData(map);
			}
		} catch (Exception e) {
			logger.info("PGserviceImpl paymentResponse method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage("Something went wrong while accessing request please try again!");
			structure.setErrorDiscription(e.getLocalizedMessage());

		}
		return structure;
	}

	@Override
	public ResponseStructure generatePaymentLink(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<PGModeModel> opt = repository.findByPgOnoffStatus(1);
			
			if(opt.isPresent()) {
				
				double amount = model.getTotalAmount();
				
				String amountInString = Double.toString(amount);
				
				PGModeModel pgMode = opt.get();
				
				JSONObject inputParams = new JSONObject();
				inputParams.put(AppConstants.MERCHANT_ORDER_NO, model.getReferenceNo()); //check
				inputParams.put(AppConstants.CURRENCY, "INR");
				inputParams.put(AppConstants.AMOUNT, amountInString);
				inputParams.put(AppConstants.EXPIRY, model.getLinkExpiry()); //check yyyy-MM-dd
				inputParams.put(AppConstants.REDIRECT,"true");
				inputParams.put(AppConstants.RETURNURL, model.getReturnUrl()); //check
				inputParams.put(AppConstants.CUSTOMERNAME, model.getName());
				inputParams.put(AppConstants.CUSTOMERMOBILE, model.getMobileNumber());
				inputParams.put(AppConstants.DESCRIPTION, "Payment link generation");
				
				System.out.println("Input Params " + inputParams);

				String hashData = pgMode.getApikey() + "|" + model.getReferenceNo() + "|" + "INR" + "|" + amount + "|"
						+  model.getLinkExpiry() + "|" + "true" + "|" + model.getReturnUrl() + "|" + pgMode.getSecretKey();
				
				System.err.println("Api key " + pgMode.getApikey());
				System.err.println("Secret Key " + pgMode.getSecretKey());
				System.out.println("Hash Data " + hashData);
				
				String secureHash = getHashCodeFromString(hashData);
				
				System.err.println("Secure Hash: " + secureHash);

				RestTemplate restTemplate = new RestTemplate();

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				headers.add(AppConstants.PG_API_KEY, pgMode.getApikey());
				headers.add(AppConstants.API_HASH, secureHash);
				
				HttpEntity<String> obj = new HttpEntity<>(inputParams.toString(), headers);

				ResponseEntity<String> clientResponse = restTemplate.postForEntity(AppConstants.Payment_Link_Gen_Url,
						obj, String.class);
				
				String keys = clientResponse.getBody();
				System.err.println("Keys : "+keys);
				
				JSONObject json = new JSONObject(keys);
				
				String message = json.optString("message");
				String link = json.optString("payLink");
				boolean status = json.optBoolean("success");
				
				structure.setData(link);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setMessage("Payment link generated successfully");
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("PGMode not found");
				structure.setData(null);
				structure.setFlag(4);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(5);
		}
		return structure;
	}

}
