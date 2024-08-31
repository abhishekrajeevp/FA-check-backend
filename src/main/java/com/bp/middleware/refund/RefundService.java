package com.bp.middleware.refund;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bp.middleware.pgmode.PGModeModel;
import com.bp.middleware.pgmode.PGModeRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.transaction.TransactionRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;

@Service
public class RefundService {

	
	@Autowired
	private RefundRepository refundRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private PGModeRepository pgRepository;

	
	public ResponseStructure requestRefund(RequestModel model) {
		
		ResponseStructure structure = new ResponseStructure();
		
		try {
			
			Optional<EntityModel> userOpt = userRepository.findById(model.getUserId());
			Optional<TransactionDto> transOpt = transactionRepository.findById(model.getTransactId());
			Optional<PGModeModel> pgOpt = pgRepository.findByPgOnoffStatus(1);
			
			if(userOpt.isPresent() && transOpt.isPresent() && pgOpt.isPresent()) {
				
				EntityModel user = userOpt.get();
				TransactionDto transaction = transOpt.get();
				PGModeModel pg = pgOpt.get();
				
				String paymentId = transaction.getPayid().getPaymentId();
				String requestId = FileUtils.generateApiKeys(8);
				double amount = Math.floor(model.getAmount());
				
				LocalDate paymentDate = localDateTimeStringToLocalDate(transaction.getPaymentDatetime());
			
				long paymentDays = ChronoUnit.DAYS.between(paymentDate, LocalDate.now());
				
				System.err.println("Payment days : "+paymentDays);
				
				if(amount<=transaction.getExclusiveAmount() && paymentDays<=7) {
					
					String refundAmount = Double.toString(amount);
					
					JSONObject inputParams = new JSONObject();
					
					inputParams.put(AppConstants.PaymentID, paymentId);
					inputParams.put(AppConstants.RequestID, requestId); //Random String
					inputParams.put(AppConstants.Amount, refundAmount);
					
					String hashData = pg.getApikey() + "|" + paymentId + "|" + requestId + "|" + refundAmount + "|"
							+ pg.getSecretKey();
					
					String secureHash = getHashCodeFromString(hashData);
					
					RestTemplate restTemplate = new RestTemplate();
					
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
					headers.add(AppConstants.PG_API_KEY, pg.getApikey());
					headers.add(AppConstants.API_HASH, secureHash);
					
					HttpEntity<String> obj = new HttpEntity<>(inputParams.toString(), headers);

					ResponseEntity<String> clientResponse = restTemplate.postForEntity(AppConstants.Refund_Request_Url, obj,
							String.class);

					String keys = clientResponse.getBody();
					JSONObject json = new JSONObject(keys);
					
					System.err.println(keys);
					
					
					String activityId = json.optString("activityId");
					
					//NOT NEEDED AT THE MOMENT.
					
					
				}else {
					
					if(amount>transaction.getExclusiveAmount()) {
					    structure.setMessage("The given amount is higher than the paid amount.");
					}else {
						structure.setMessage("Refund can only be done within 7 days of payment");
					}
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(4);
				}
			}else {
				
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(5);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}
	
	
	
	private LocalDate localDateTimeStringToLocalDate(String paymentDatetime) throws Exception{
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime dateTime = LocalDateTime.parse(paymentDatetime,formatter);
		
		return dateTime.toLocalDate();
	}



	private static String getHashCodeFromString(String str)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {

		MessageDigest md = MessageDigest.getInstance("SHA-512");
		md.update(str.getBytes("UTF-8"));
		byte byteData[] = md.digest();

		StringBuilder hashCodeBuffer = new StringBuilder();
		for (int i = 0; i < byteData.length; i++) {
			hashCodeBuffer.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}

		return hashCodeBuffer.toString().toUpperCase();
	}
	
	
	
}
