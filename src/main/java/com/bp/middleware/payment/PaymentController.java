package com.bp.middleware.payment;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import com.bp.middleware.pgmode.PGModeModel;
import com.bp.middleware.pgmode.PGModeRepository;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.transaction.TransactionRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.DateUtil;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.PostpaidReceiptPdf;
import com.bp.middleware.util.ReceiptGenerator;
import com.bp.middleware.util.RecieptCheck;
import com.itextpdf.io.IOException;

import jakarta.servlet.ServletContext;

@RestController
@RequestMapping("/payment")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class PaymentController {

	@Autowired
	private PaymentService service;
	@Autowired
	private PGModeRepository pgModeRepository;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired 
	private UserRepository userRepository;
	@Autowired 
	private PaymentModelRepository paymentModelRepository;
	@Autowired 
	private PostpaidRepository postpaidRepository;
	@Autowired 
	private PrepaidRepository prepaidRepository;
	@Autowired
	ServletContext context;
	@Autowired
	FileUtils fu;
	
	
	@PostMapping("/makepayment")
	public ResponseStructure makepayment(@RequestBody RequestModel user) {
		return service.makePayment(user);
	}
	
	
	@GetMapping("/trackTransaction/{payId}")
	public ResponseStructure trackTransactionFromPg(@PathVariable("payId")int payId) {
		return service.trackTransactionFromPg(payId);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	@RequestMapping(value = "/returnpageres", method = RequestMethod.POST)
	@PostMapping("/returnpageres")
	public RedirectView redirectToUrl(@ModelAttribute RequestModel dto)
			throws NoSuchAlgorithmException, IOException {
		RedirectView redirectView = new RedirectView();
		try {
			Optional<PGModeModel> pgoptional = pgModeRepository.findByPgOnoffStatus(1);
			int payId = 0;
			if (pgoptional!=null) {
				
				PGModeModel pgModel = pgoptional.get();
				
				JSONObject inputParams = new JSONObject();
				inputParams.put("reference", dto.getReference());
				inputParams.put("success", dto.isSuccess());


				String[] hashColumns;
				hashColumns = new String[] { "reference", "success" };

				String hashData = pgModel.getApikey();
				for (int i = 0; i < hashColumns.length; i++) {
					hashData += '|' + inputParams.get(hashColumns[i]).toString().trim();

				}

				hashData += '|' + pgModel.getSecretKey();


				String secureHash = getHashCodeFromString(hashData);

				RestTemplate restTemplate = new RestTemplate();

				// create headers
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
				headers.add(AppConstants.API_HASH, secureHash);
				headers.add(AppConstants.PG_API_KEY, pgModel.getApikey());
				headers.add("Content-Type", "application/json");

				// build the request
				HttpEntity<String> entity = new HttpEntity<>(inputParams.toString(), headers);

				String pgPostUrl = null;

				if (pgModel.getPgMode().equalsIgnoreCase("TEST")) {
					pgPostUrl = AppConstants.PG_RES_URL;
				} else {
					pgPostUrl = AppConstants.PG_PRODUCTION_URL;
				}

				// send POST request
				ResponseEntity<String> clientResponse = restTemplate.postForEntity(pgPostUrl, entity, String.class);
				
				String keys = clientResponse.getBody();
				
				return receiveJson(keys,payId,redirectView);
			} 
		} catch (Exception e) {
			return null;
		}
		return redirectView;
		
	}
	
	private RedirectView receiveJson(String keys, int payId, RedirectView redirectView) {
		try {
			// save response and update transaction status
			JSONObject json = new JSONObject(keys);

//			logger.info("Json Object",json);

			String merchantOrder = null;
			merchantOrder = json.get("merchantOrderNo").toString();
			Optional<TransactionDto> transactionHistory=transactionRepository.findByTrackId(merchantOrder);
			
			// Get current LocalDateTime
	        LocalDateTime currentLocalDateTime = LocalDateTime.now();
	 
	        // Create DateTimeFormatter instance with specified format
	        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	 
	        // Format LocalDateTime to String
	        String formattedDateTime = currentLocalDateTime.format(dateTimeFormatter);
	        int  flag=0;
			if (transactionHistory.isPresent()) {
				TransactionDto transaction=transactionHistory.get();
//				MemberModel memberModel = memberRepository.findByMemberId(transaction.getPayid().getMemberModel().getMemberId());
				EntityModel entity = transaction.getPayid().getEntity();
				
				if (json.get(AppConstants.STATUS).toString().equalsIgnoreCase("Success")) {
					payId=transaction.getPayid().getPayid();
					
					//RenewableModel renewablesModel=new RenewableModel();
					
					if(entity.getPaymentMethod().getPaymentId()==1) {
						
						return postpaidRedirectViewSuccess(json,transaction,payId,entity,formattedDateTime,redirectView);
						
					}else if (entity.getPaymentMethod().getPaymentId()==2) {
						return prepaidRedirectViewSuccess(json,transaction,payId,entity,formattedDateTime,redirectView);
					}
					
					
					
					
				}else if (json.get(AppConstants.STATUS).toString().equalsIgnoreCase("Failure")) {
					transaction.setPaymentStatus(json.get(AppConstants.STATUS).toString());
					Optional<PaymentModel> optional = paymentModelRepository.findById(payId);
					
					if (optional.isPresent()) {
						PaymentModel payModel = optional.get();

						payModel.setPaymentStatus(transaction.getPaymentStatus());
						paymentModelRepository.save(payModel);
					}
					
					if(entity.getPaymentMethod().getPaymentId()==1) {
						
						PostpaidPayment postpaidPayment = postpaidRepository.findByEntityModelAndPaymentFlag(entity, false);
						
						postpaidPayment.setRemark(json.get(AppConstants.STATUS).toString());
						postpaidRepository.save(postpaidPayment);
						
					}else if(entity.getPaymentMethod().getPaymentId()==2) {
						
						PrepaidPayment prepaid = prepaidRepository.findByEntityModelAndPaymentProcess(entity,"On process");
						
						prepaid.setRemark(json.get(AppConstants.STATUS).toString());
						prepaid.setPaymentProcess("Failed");
						prepaidRepository.save(prepaid);
					}
				}
				
				transactionRepository.save(transaction);
				
				redirectView.setUrl("http://64.227.149.125:9087/paymentfailure?transactionId=" + transaction.getTrancsactionId());
			}
	
		} catch (Exception e) {
			return null;
		}
		return redirectView;
      }
	

	private RedirectView prepaidRedirectViewSuccess(JSONObject json, TransactionDto transaction, int payId,
			EntityModel entity, String formattedDateTime, RedirectView redirectView) {

		Optional<PaymentModel> optional = paymentModelRepository.findById(payId);
		if (optional.isPresent()) {
			
			PaymentModel payModel = optional.get();
			payModel.setPaymentId(json.get("paymentId").toString());
			payModel.setPaymentStatus("Success");
			payModel.setPaymentDateTime(formattedDateTime);
			
			PrepaidPayment prepaid = prepaidRepository.findByEntityModelAndPaymentProcess(entity,"On process");
      
		 prepaid.setRemark(json.get(AppConstants.STATUS).toString());
		 prepaid.setRechargedAmount(payModel.getPaidAmount());
	     prepaid.setTransactionId(json.get("paymentId").toString());
	     prepaid.setPaymentProcess("Completed Successfully");
	     
	     entity.setRemainingAmount(fu.twoDecimelDouble(entity.getRemainingAmount()+payModel.getPaidAmount()));
	     
	     userRepository.save(entity);
	     prepaidRepository.save(prepaid);

		transaction.setMemberName(json.get("customerName").toString());
		transaction.setEmail(json.get("customerEmail").toString());
		transaction.setMobileNumber(json.get("customerMobile").toString());
		transaction.setAddress(json.get("address").toString());
		transaction.setPaymentStatus(json.get(AppConstants.STATUS).toString());
		transaction.setPaymentMethod(json.get("paymentMethod").toString());
		transaction.setPostalCode(json.get("postalCode").toString());
		transaction.setPaidAmount(json.getDouble(AppConstants.ORDERAMOUNT));
		transaction.setBankReference(json.get("bankReference").toString());
		transaction.setCardExpiry(json.get("cardExpiry").toString());
		transaction.setCardNoMasked(json.get("cardNoMasked").toString());
		
		transaction.setPaymentDatetime(formattedDateTime);
		Path con=	Paths.get(context.getRealPath("/WEB-INF/"));
		String receiptnot=	RecieptCheck.receiptGenerator(con,transaction,entity,null,prepaid);
		
		String receipt = "";//"HERE"ReceiptGenerator.prepaidRecieptGenerator(con,transaction,prepaid,entity,null,null);
		//transaction.setReceipt(receipt);
		
		transactionRepository.save(transaction);
		
		prepaid.setReceipt(receipt);
		prepaidRepository.save(prepaid);

		
		}
               
		redirectView.setUrl("http://64.227.149.125:9087/paymentsuccess?transactionId=" + transaction.getTrancsactionId());
		return redirectView;
	}

	private RedirectView postpaidRedirectViewSuccess(JSONObject json, TransactionDto transaction, int payId, EntityModel entity, String formattedDateTime, RedirectView redirectView)throws Exception {


		Optional<PaymentModel> optional = paymentModelRepository.findById(payId);
		if (optional.isPresent()) {
			
			PaymentModel payModel = optional.get();
			payModel.setPaymentId(json.get("paymentId").toString());
			payModel.setPaymentStatus("Success");
			payModel.setPaymentDateTime(formattedDateTime);
			double amount=0.0;
			
			PostpaidPayment postpaidPayment = postpaidRepository.findByEntityModelAndPaymentFlag(entity, false);


			postpaidPayment.setPaymentFlag(true);
//			postpaidPayment.setUpdatedDate(updatedDate);
			postpaidPayment.setRemark(json.get(AppConstants.STATUS).toString());
			postpaidPayment.setPaidAmount(payModel.getPaidAmount());
			postpaidPayment.setTransactionId(json.get("paymentId").toString()); 

			postpaidRepository.save(postpaidPayment);
			
			PostpaidPayment newPostpaidPayment = new PostpaidPayment();

			LocalDate endDate=LocalDate.now().plusDays(entity.getDuration());
			LocalDate graceDate=endDate.plusDays(entity.getGracePeriod());

			newPostpaidPayment.setStartDate(LocalDate.now());
			newPostpaidPayment.setEndDate(endDate);
			newPostpaidPayment.setEntityModel(entity);
			newPostpaidPayment.setPaymentFlag(false);
			newPostpaidPayment.setTotalAmount(0);

			entity.setPostpaidFlag(true);
			entity.setPaymentStatus("No dues");
			entity.setGraceDate(graceDate);

			postpaidRepository.save(newPostpaidPayment);
			userRepository.save(entity);
				

		transaction.setMemberName(json.get("customerName").toString());
		transaction.setEmail(json.get("customerEmail").toString());
		transaction.setMobileNumber(json.get("customerMobile").toString());
		transaction.setAddress(json.get("address").toString());
		transaction.setPaymentStatus(json.get(AppConstants.STATUS).toString());
		transaction.setPaymentMethod(json.get("paymentMethod").toString());
		transaction.setPostalCode(json.get("postalCode").toString());
		transaction.setPaidAmount(json.getDouble(AppConstants.ORDERAMOUNT));
		transaction.setBankReference(json.get("bankReference").toString());
		transaction.setCardExpiry(json.get("cardExpiry").toString());
		transaction.setCardNoMasked(json.get("cardNoMasked").toString());
		
		transaction.setPaymentDatetime(formattedDateTime);
		Path con=	Paths.get(context.getRealPath("/WEB-INF/"));
		//String receipt=	PostpaidReceiptPdf.writeDataToPDF(con,transaction,entity,postpaidPayment,0,0);
		String receipt = "";//"HERE"ReceiptGenerator.postpaidRecieptGenerator(con,transaction,postpaidPayment,entity,null,null);
		transaction.setReceipt(receipt);
		
		transactionRepository.save(transaction);
		
		postpaidPayment.setReceipt(receipt);
		postpaidRepository.save(postpaidPayment);
		}	
		redirectView.setUrl("http://64.227.149.125:9087/paymentsuccess?transactionId=" + transaction.getTrancsactionId());
		return redirectView;
	}
	
	
	public  PrepaidPayment lastPrepaidModel(EntityModel entity) {
		
		List<PrepaidPayment> list = prepaidRepository.findByEntityModel(entity);
		PrepaidPayment prepaid = new PrepaidPayment();
		
		if(!list.isEmpty()) {
			
			   int finalCount = 0;
			   for (PrepaidPayment renew : list) {
				
				finalCount++;
				if(list.size()==finalCount) {
					prepaid = renew;
				  }
			   }
		}
		return prepaid;
	}

	private static String getHashCodeFromString(String str)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {

		MessageDigest md = MessageDigest.getInstance("SHA-512");
		md.update(str.getBytes("UTF-8"));
		byte byteData[] = md.digest();

		// convert the byte to hex format method 1
		StringBuilder hashCodeBuffer = new StringBuilder();
		for (int i = 0; i < byteData.length; i++) {
			hashCodeBuffer.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		return hashCodeBuffer.toString().toUpperCase();
	}
	
	
	
	@PostMapping("/addpaymentmethod")
	public ResponseStructure addPaymentMethod(@RequestBody RequestModel model) {
		return service.addPaymentMethod(model);
	}
	
	@GetMapping("/viewpaymentmethodbyid/{paymentId}")
	public ResponseStructure viewPaymentById(@PathVariable("paymentId")int paymentId) {
		return service.viewPaymentById(paymentId);
	}
	
	@GetMapping("/viewallpaymentmethod")
	public ResponseStructure viewAllPaymentMethod() {
		return service.viewAllPaymentMethod();
	}
	
	@PutMapping("/updatestatus")
	public ResponseStructure changeStatus(@RequestBody RequestModel model ) {
	        return service.changeStatus(model);

	}
	
	
	
}




