package com.bp.middleware.payment;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.bp.middleware.admin.AdminServiceImplementation;
import com.bp.middleware.conveniencefee.ConveniencePercentageEntity;
import com.bp.middleware.conveniencefee.ConveniencePercentageRepository;
import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.modeofpgpayment.ModeOfPaymentPg;
import com.bp.middleware.modeofpgpayment.ModeOfPaymentPgRepository;
import com.bp.middleware.pgmode.PGModeModel;
import com.bp.middleware.pgmode.PGModeRepository;
import com.bp.middleware.postpaidstatement.PostpaidUserStatementService;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.prepaidstatement.PrepaidUserStatementService;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.transaction.TransactionRepository;
import com.bp.middleware.transactionamountmodel.TransactionAmountModel;
import com.bp.middleware.transactionamountmodel.TransactionAmountRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.InvoiceGenerate;
import com.bp.middleware.util.ReceiptGenerator;

import jakarta.servlet.ServletContext;

@Service
public class PaymentServiceImplementation implements PaymentService {

	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PostpaidRepository postpaidRepository;
	@Autowired
	private PrepaidRepository prepaidRepository;
	@Autowired
	private PaymentModelRepository paymentModelRepository;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private PGModeRepository pgModeRepository;
	@Autowired
	private ConveniencePercentageRepository conveniencePercentageRepository;
	@Autowired
	private ModeOfPaymentPgRepository modeOfPaymentPgRepository;
	@Autowired
	private TransactionAmountRepository transactionAmountRepository;
	@Autowired
	EmailService emailService;
	@Autowired
	InvoiceGenerate invoiceGenerate;
	@Autowired
	FileUtils fu;
	@Autowired
	ReceiptGenerator receiptGenerator;
	@Autowired
	private PrepaidUserStatementService prepaidUserStatementService;
	@Autowired
	private PostpaidUserStatementService postpaidUserStatementService;
	@Autowired
	ServletContext context;

	private String contentType = "Content-Type";

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImplementation.class);

	@Override
	public ResponseStructure makePayment(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			LOGGER.info(
					"MAKE PAYMENT START ---------------------------------------------------------------------------------");

			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
			EntityModel user = userRepository.findByUserId(model.getUserId());

			if (user != null) {

				TransactionDto transactionObj = new TransactionDto();

				PaymentModel pay = new PaymentModel();
				pay.setPaymentStatus("Initiated");
				pay.setPaymentMode("Online");

				boolean nativeLocal = user.getStateName().equalsIgnoreCase("Tamil nadu")
						|| user.getStateName().equalsIgnoreCase("Tamilnadu");

				double exclusiveAmount = 0.0;
				double totalAmount = 0.0;
				double overAllAmount = 0.0;
				double sgst = 0.0;
				double cgst = 0.0;

				if (user.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {

					System.err.println("Postpaid");

					PostpaidPayment postpaidHistory = postpaidRepository.findByEntityModelAndPaymentFlag(user, false);

					exclusiveAmount = postpaidHistory.getDueAmount();

					if (exclusiveAmount <= 0) {

						structure.setStatusCode(HttpStatus.OK.value());
						structure.setMessage("CAN'T MAKE PAYMENT SINCE THE AMOUNT IS 0");
						structure.setFlag(3);
						structure.setData(null);

						return structure;
					}

					double gstAsNineCent = calculateGstNinePercent(exclusiveAmount);

					sgst = gstAsNineCent;
					cgst = gstAsNineCent;
					totalAmount = exclusiveAmount + sgst + cgst;

					model.setSgst(sgst);
					model.setCgst(cgst);
					model.setIgst(cgst + sgst);
					model.setExclusiveAmount(exclusiveAmount);
					model.setNativeLocal(nativeLocal);

					overAllAmount = getConvenienceFee(exclusiveAmount, model, transactionObj, user ,null,postpaidHistory) + totalAmount;

					postpaidHistory.setRemark("initiated");
					postpaidHistory.setPaymentMode(transactionObj.getModeOfPaymentPg().getModeOfPayment());
					postpaidHistory.setEntityModel(user);
					postpaidHistory.setPaidDate(LocalDate.now());
					postpaidHistory.setTotalAmount(overAllAmount);
					postpaidHistory.setUniqueId(FileUtils.generateApiKeys(8));

					pay.setDueDate(user.getGraceDate());
					pay.setCreatedBy(model.getCreatedBy());
					pay.setCreatedDateTime(sdf.format(new Date()));
					pay.setPaymentDateTime(sdf.format(new Date()));

					String trackId = FileUtils.getRandomString();
					pay.setTrackId(trackId);
					pay.setEntity(user);
					pay.setDescription(AppConstants.PAYMENT_INITIATE_STATUS);

					paymentModelRepository.save(pay);

					postpaidHistory.setPayId(pay.getPayid());
					postpaidRepository.save(postpaidHistory);
					
					transactionObj.setPostpaidId(postpaidHistory.getPostpaidId());

				} else {

					System.err.println("Prepaid");

					PrepaidPayment prepaid = new PrepaidPayment();

					exclusiveAmount = model.getRechargedAmount();

					if (exclusiveAmount <= 0) {

						structure.setStatusCode(HttpStatus.OK.value());
						structure.setMessage("CAN'T MAKE PAYMENT SINCE THE AMOUNT IS 0");
						structure.setFlag(3);
						structure.setData(null);

						return structure;
					}

//					double gstAsNineCent = calculateGstNinePercent(exclusiveAmount);
//
//					sgst = gstAsNineCent;
//					System.err.println("SGST 1 : " + sgst);
//					cgst = gstAsNineCent;
//					System.err.println("CGST 1 : " + cgst);
					
					totalAmount = exclusiveAmount;// + sgst + cgst;

					model.setSgst(sgst);
					model.setCgst(cgst);
					model.setIgst(cgst + sgst);
					model.setExclusiveAmount(exclusiveAmount);
					model.setNativeLocal(nativeLocal);

					overAllAmount = getConvenienceFee(exclusiveAmount, model, transactionObj, user,prepaid,null)+totalAmount;

					prepaid.setRemark("initiated");
					prepaid.setPaymentMode(transactionObj.getModeOfPaymentPg().getModeOfPayment());
					prepaid.setRechargedAmount(exclusiveAmount);
					prepaid.setEntityModel(user);
					prepaid.setPaymentProcess("On process");
					prepaid.setUniqueId(FileUtils.generateApiKeys(8));

					pay.setCreatedBy(model.getCreatedBy());
					pay.setCreatedDateTime(sdf.format(new Date()));
					pay.setPaymentDateTime(sdf.format(new Date()));

					String trackId = FileUtils.getRandomString();
					pay.setTrackId(trackId);
					pay.setEntity(user);
					pay.setDescription(AppConstants.PAYMENT_INITIATE_STATUS);

					paymentModelRepository.save(pay);

					prepaid.setPayId(pay.getPayid());

					prepaidRepository.save(prepaid);
					
					transactionObj.setPrepaidId(prepaid.getPrepaidId());
				}

				pay.setCreatedBy(model.getCreatedBy());
				pay.setCreatedDateTime(sdf.format(new Date()));
				pay.setPaymentDateTime(sdf.format(new Date()));
				pay.setPaidAmount(overAllAmount);
				pay.setExclusiveAmount(exclusiveAmount);

				if (nativeLocal) {

					System.err.println("SGST 2 : " + sgst);
					System.err.println("CGST 2 : " + cgst);

					pay.setCgstAmount(cgst);
					pay.setSgstAmount(sgst);

				} else {
					pay.setCgstAmount(0.0);
					pay.setSgstAmount(0.0);
				}

				String trackId = FileUtils.getRandomString();
				pay.setTrackId(trackId);
				pay.setEntity(user);
				pay.setDescription(AppConstants.PAYMENT_INITIATE_STATUS);

				paymentModelRepository.save(pay);

				transactionObj.setPayid(pay);
				transactionObj.setPaymentStatus(AppConstants.PAYMENT_INITIATE_STATUS);
				transactionObj.setCreatedBy(model.getCreatedBy());
				transactionObj.setPaymentId(pay.getPaymentId());
				transactionObj.setReceiptNumber(pay.getReceiptNumber());
				transactionObj.setPaidAmount(overAllAmount);
				transactionObj.setExclusiveAmount(exclusiveAmount);
				transactionObj.setPaymentDatetime(sdf.format(new Date()));

				if (nativeLocal) {

					transactionObj.setCgstAmount(cgst);
					transactionObj.setSgstAmount(sgst);
					transactionObj.setIgstAmount(0.0);

				} else {
					transactionObj.setCgstAmount(0.0);
					transactionObj.setSgstAmount(0.0);
					transactionObj.setIgstAmount(cgst + sgst);
				}

				transactionObj.setCreatedDatetime(new Date());
				transactionObj.setDescription(AppConstants.PAYMENT_INITIATE_STATUS);
				transactionObj.setPaymentMode(transactionObj.getModeOfPaymentPg().getModeOfPayment());
				transactionObj.setEntity(user);
				transactionObj.setTrackId(trackId);

				transactionRepository.save(transactionObj);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Payment has been posted successfully!");
				structure.setData(pay.getTrackId());
				structure.setFlag(1);

				TransactionDto transObj = transactionRepository.findFirstByPayidOrderByCreatedDatetimeDesc(pay);

				if (transObj != null) {
					structure.setData(transObj.getTrancsactionId());
				}

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setData(null);
			}

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		LOGGER.info(
				"MAKE PAYMENT END ---------------------------------------------------------------------------------");

		return structure;
	}

	private double getConvenienceFee(double exclusiveamount, RequestModel model, TransactionDto transactionObj,
			EntityModel user ,PrepaidPayment prepaid , PostpaidPayment postpaid) throws Exception {

		Optional<ModeOfPaymentPg> opt = modeOfPaymentPgRepository.findById(model.getModeId());

		double conveFeeWithGst = 0;

		if (opt.isPresent() && opt.get().isConvenienceFetch()) {

			ModeOfPaymentPg modeOfPay = opt.get();

			List<ConveniencePercentageEntity> convFee = conveniencePercentageRepository
					.getAllActiveLessThresholdAmountIDesc(exclusiveamount, modeOfPay.getModeId());

			if (!convFee.isEmpty()) {

				ConveniencePercentageEntity thresholdMatch = convFee.get(0);

				double convenienceAmount = 0;
				double fixedFee = 0;
				double convAndFixedFee = 0;
				double gstOfConvFee = 0;

				convenienceAmount = exclusiveamount * thresholdMatch.getConveniencePercentage() / 100;
				fixedFee = thresholdMatch.getFixedAmount();

				convAndFixedFee = convenienceAmount + fixedFee;

				gstOfConvFee = calculateGstEighteenPercent(convAndFixedFee);

				conveFeeWithGst = convAndFixedFee + gstOfConvFee;

				transactionObj.setConveniencePercentageEntity(thresholdMatch);

				TransactionAmountModel amountModel = new TransactionAmountModel();

				amountModel.setExclusiveAmount(exclusiveamount);
				amountModel.setExclusiveWithGst(exclusiveamount + model.getIgst());
				amountModel.setConvenianceFee(convenienceAmount);
				amountModel.setConvenienceFetched(true);
				amountModel.setFixedFee(fixedFee);
				amountModel.setOtherGst(gstOfConvFee);
				amountModel.setOverAllTotalAmount(exclusiveamount + model.getIgst() + conveFeeWithGst);
				amountModel.setRemarks("Convenience Fee calculated successfully");

				System.err.println("exclusive amount         : "+exclusiveamount); //500
				System.err.println("exclusive amount (GST)   : "+model.getIgst()); //90
				System.err.println("conv fee                 : "+convenienceAmount); //10
				System.err.println("fixed fee                : "+fixedFee); //20
				System.err.println("gst of Conv              : "+gstOfConvFee); //5.4
				
				System.err.print("\nTOTAL                    : ");
				System.err.println(exclusiveamount+model.getIgst()+convenienceAmount+fixedFee+gstOfConvFee);
				
				
				if (model.isNativeLocal()) {

					amountModel.setCgst(model.getCgst());
					amountModel.setSgst(model.getSgst());
					amountModel.setIgst(0);

				} else {

					amountModel.setCgst(0);
					amountModel.setSgst(0);
					amountModel.setIgst(model.getIgst());
				}

				transactionAmountRepository.save(amountModel);

				if(prepaid!=null) {
					prepaid.setTransactionAmountModel(amountModel);
				}else {
					postpaid.setTransactionAmountModel(amountModel);
				}
				
				transactionObj.setTransactionAmountModel(amountModel);
				transactionObj.setModeOfPaymentPg(modeOfPay);
				transactionObj.setConvenienceAmount(convenienceAmount);
			
			}else {
				
				TransactionAmountModel amountModel = new TransactionAmountModel();

				amountModel.setExclusiveAmount(exclusiveamount);
				amountModel.setExclusiveWithGst(exclusiveamount + model.getIgst());
				amountModel.setConvenianceFee(0);
				amountModel.setConvenienceFetched(false);
				amountModel.setFixedFee(0);
				amountModel.setOtherGst(0);
				amountModel.setOverAllTotalAmount(exclusiveamount + model.getIgst() + conveFeeWithGst);
				amountModel.setRemarks("No Convenience fee created for "+exclusiveamount+" payed via "+modeOfPay.getModeOfPayment());

				if (model.isNativeLocal()) {

					amountModel.setCgst(model.getCgst());
					amountModel.setSgst(model.getSgst());
					amountModel.setIgst(0);

				} else {

					amountModel.setCgst(0);
					amountModel.setSgst(0);
					amountModel.setIgst(model.getIgst());
				}

				transactionAmountRepository.save(amountModel);

				if(prepaid!=null) {
					prepaid.setTransactionAmountModel(amountModel);
				}else {
					postpaid.setTransactionAmountModel(amountModel);
				}
				
				transactionObj.setTransactionAmountModel(amountModel);
				transactionObj.setModeOfPaymentPg(modeOfPay);
			}

			return conveFeeWithGst;

		} else {

			if (opt.isPresent() && !opt.get().isConvenienceFetch()) {

				ModeOfPaymentPg modeOfPay = opt.get();
				
				TransactionAmountModel amountModel = new TransactionAmountModel();

				amountModel.setExclusiveAmount(exclusiveamount);
				amountModel.setExclusiveWithGst(exclusiveamount + model.getIgst());
				amountModel.setConvenianceFee(0);
				amountModel.setConvenienceFetched(false);
				amountModel.setFixedFee(0);
				amountModel.setOtherGst(0);
				amountModel.setOverAllTotalAmount(exclusiveamount + model.getIgst() + conveFeeWithGst);
				amountModel.setRemarks("No Convenience fee needed for "+modeOfPay.getModeOfPayment());

				if (model.isNativeLocal()) {

					amountModel.setCgst(model.getCgst());
					amountModel.setSgst(model.getSgst());
					amountModel.setIgst(0);

				} else {

					amountModel.setCgst(0);
					amountModel.setSgst(0);
					amountModel.setIgst(model.getIgst());
				}

				transactionAmountRepository.save(amountModel);

				if(prepaid!=null) {
					prepaid.setTransactionAmountModel(amountModel);
				}else {
					postpaid.setTransactionAmountModel(amountModel);
				}
				
				transactionObj.setTransactionAmountModel(amountModel);
				transactionObj.setModeOfPaymentPg(modeOfPay);
			}

			return conveFeeWithGst;
		}

	}
	

	private double calculateGstNinePercent(double exclusiveAmount) throws Exception {

		return exclusiveAmount * AppConstants.SGST / 100;

	}

	private double calculateGstEighteenPercent(double exclusiveAmount) throws Exception {

		return exclusiveAmount * 18 / 100;

	}

	@Override
	public ResponseStructure trackTransactionFromPg(int payId) {

		ResponseStructure response = new ResponseStructure();
		try {

			Optional<PaymentModel> paymentOptional = paymentModelRepository.findById(payId);

			Optional<PGModeModel> pgOptional = pgModeRepository.findByPgOnoffStatus(1);

			if (paymentOptional.isPresent()) {

				PaymentModel pay = paymentOptional.get();

				if (pgOptional.isPresent()) {

					PGModeModel pg = pgOptional.get();

					JSONObject inputParams = new JSONObject();

					inputParams.put(AppConstants.MERCHANT_ORDER_NO, pay.getTrackId());// 1."YOGUF0LZY506132024030428"
					inputParams.put(AppConstants.AMOUNT, pay.getPaidAmount().toString());// 2.""23.6""

					String[] hashColumns;
					hashColumns = new String[] { AppConstants.MERCHANT_ORDER_NO, AppConstants.AMOUNT };

					System.out.println("hashColumns :" + hashColumns);

					String hashData = pg.getApikey();// 3."c057e2ca-6175-4acb-9238-d88a3cb8af2b"

					for (int i = 0; i < hashColumns.length; i++) {
						hashData += '|' + inputParams.get(hashColumns[i]).toString().trim();

					}

					hashData += '|' + pg.getSecretKey();// 4."YE25SSNMUqw5yDBUX2BLu/mh18mDfzZnYXF8YIkDkIc="

					System.out.println("hashData :" + hashData);

					String secureHash = getHashCodeFromString(hashData);

					System.out.println("secureHash :" + secureHash);

					RestTemplate restTemplate = new RestTemplate();

					// create headers

					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
					headers.add(AppConstants.API_HASH, secureHash);

					headers.add(AppConstants.PG_API_KEY, pg.getApikey());// 5."c057e2ca-6175-4acb-9238-d88a3cb8af2b"
					headers.add(contentType, AppConstants.APPLICATION_JSON);

//				// build the request
					HttpEntity<String> entity = new HttpEntity<>(inputParams.toString(), headers);

					System.out.println("entity :" + entity);

					String pgPostUrl = null;

					if (pg.getPgMode().equalsIgnoreCase("TEST")) {

						System.err.println("TEST");
						pgPostUrl = AppConstants.PG_TRACK_URL;

					} else {

						System.err.println("PRODUCTION");
						pgPostUrl = AppConstants.PG_PROD_TRACK_URL;
					}

					// send POST request
					ResponseEntity<String> clientResponse = restTemplate.postForEntity(pgPostUrl, entity, String.class);

					System.out.println("clientResponse :" + clientResponse);

					String keys = clientResponse.getBody();

					System.out.println("keys :" + keys);

					JSONObject json = new JSONObject(keys);

					String status = json.optString("status");
					String paymentId = json.optString("paymentId");
					String paidAt = json.optString("paidAt");

					if (!pay.getPaymentStatus().equalsIgnoreCase(status)) {

						TransactionDto transaction = transactionRepository.findByPayid(pay);

						pay.setPaymentStatus(status);

						transaction.setMemberName(json.get("customerName").toString());
						transaction.setEmail(json.get("customerEmail").toString());
						transaction.setMobileNumber(json.get("customerMobile").toString());
						transaction.setAddress(json.get("address").toString());
						transaction.setPaymentStatus(json.get(AppConstants.STATUS).toString());
						transaction.setPaymentMethod(json.get("paymentMethod").toString());
						transaction.setPostalCode(json.get("postalCode").toString());
						transaction.setBankReference(json.get("bankReference").toString());
						transaction.setCardExpiry(json.get("cardExpiry").toString());
						transaction.setCardNoMasked(json.get("cardNoMasked").toString());

						if (status.equalsIgnoreCase("success")) {

							String invoiceNumber = FileUtils.getRandomOTPnumber(7);

							LocalDateTime currentLocalDateTime = LocalDateTime.now();
							DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
							String formattedDateTime = currentLocalDateTime.format(dateTimeFormatter);

							double paidAmount = json.getDouble(AppConstants.ORDERAMOUNT);

							pay.setPaymentId(paymentId);
							pay.setPaymentDateTime(paidAt);
							pay.setPaidAmount(paidAmount);

							transaction.setPaymentDatetime(paidAt);
							transaction.setPaidAmount(paidAmount);
							transaction.setInvoiceNumber(invoiceNumber);

							EntityModel user = transaction.getEntity();

							if (user.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

								PrepaidPayment prepaid = prepaidRepository.findByEntityModelAndPayId(user,
										pay.getPayid());


								pay.setPaymentId(json.get("paymentId").toString());
								pay.setPaymentStatus("Success");
								pay.setPaymentDateTime(formattedDateTime);

								prepaid.setRemark(json.get(AppConstants.STATUS).toString());
								prepaid.setRechargedAmount(pay.getExclusiveAmount());
								prepaid.setTransactionId(json.get("paymentId").toString());
								prepaid.setPaymentProcess("Completed Successfully");
								prepaid.setPaidDate(LocalDate.now());
								prepaid.setMonth(LocalDate.now().getMonth().toString());

								user.setRemainingAmount(fu.twoDecimelDouble(user.getRemainingAmount() + pay.getExclusiveAmount())); // calculation
								user.setPaymentStatus(json.get(AppConstants.STATUS).toString());
								
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
								transaction.setPaymentId(json.get("paymentId").toString());
								transaction.setInvoiceNumber(invoiceNumber);
								
								prepaid.setAddress(json.get("address").toString());
								prepaid.setPaymentDateTime(formattedDateTime);
								prepaid.setCardExpiry(json.get("cardExpiry").toString());
								prepaid.setCardNoMasked(json.get("cardNoMasked").toString());
								prepaid.setPostalCode(json.get("postalCode").toString());
								prepaid.setBankReference(json.get("bankReference").toString());
								
								
								userRepository.save(user);
								prepaidRepository.save(prepaid);

								LOGGER.info("212");
								transactionRepository.save(transaction);
								LOGGER.info("221");

								prepaidRepository.save(prepaid);

								RequestModel dto = new RequestModel();
								
								dto.setDebit(0);
								dto.setCredit(pay.getExclusiveAmount());
								dto.setRemark("Credit");
								dto.setDebitGst(0);
								dto.setClosingBalance(user.getRemainingAmount());
								dto.setConsumedBalance(user.getConsumedAmount());
								
								prepaidUserStatementService.statementEntry(user, dto);
								
								emailService.prepaidPaymentSuccessMail(user.getEmail(), transaction, prepaid);
								
							} else if (user.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {

								pay.setPaymentId(json.get("paymentId").toString());
								pay.setPaymentStatus(json.get(AppConstants.STATUS).toString());
								pay.setPaymentDateTime(formattedDateTime);

								PostpaidPayment postpaidPayment = postpaidRepository
										.findByEntityModelAndPaymentFlagAndPayId(user, false, payId);

								postpaidPayment.setPaymentFlag(true);
								postpaidPayment.setRemark(json.get(AppConstants.STATUS).toString());
								postpaidPayment.setPaidAmount(pay.getPaidAmount());
								
								if(pay.getPaidAmount() - postpaidPayment.getTotalAmount()<=0) {
									postpaidPayment.setDueAmount(0);
								}else {
									postpaidPayment.setDueAmount(pay.getPaidAmount() - postpaidPayment.getTotalAmount());
								}
							
								postpaidPayment.setTransactionId(json.get("paymentId").toString());
								postpaidPayment.setAddress(json.get("address").toString());
								postpaidPayment.setPaymentDateTime(formattedDateTime);
								postpaidPayment.setCardExpiry(json.get("cardExpiry").toString());
								postpaidPayment.setCardNoMasked(json.get("cardNoMasked").toString());
								postpaidPayment.setPostalCode(json.get("postalCode").toString());
								postpaidPayment.setBankReference(json.get("bankReference").toString());

								postpaidRepository.save(postpaidPayment);

								PostpaidPayment newPostpaidPayment = new PostpaidPayment();

								newPostpaidPayment.setStartDate(user.getEndDate().plusDays(1));
								newPostpaidPayment.setEndDate(endDateByPaymentCycle(newPostpaidPayment.getStartDate(),
										user.getPostpaidPaymentCycle()));
								newPostpaidPayment.setEntityModel(user);
								newPostpaidPayment.setPaymentFlag(false);
								newPostpaidPayment.setTotalAmount(0);

								LocalDate graceDate = newPostpaidPayment.getEndDate().plusDays(user.getGracePeriod());

								user.setStartDate(user.getEndDate().plusDays(1));
								user.setEndDate(
										endDateByPaymentCycle(user.getStartDate(), user.getPostpaidPaymentCycle()));
								user.setPostpaidFlag(false);
								user.setConsumedAmount(postpaidPayment.getDueAmount());
								user.setPaymentStatus("No dues");
								user.setGraceDate(graceDate);
								user.setPaymentStatus(json.get(AppConstants.STATUS).toString());
								
								LOGGER.info("267");
								
								postpaidRepository.save(newPostpaidPayment);
								userRepository.save(user);
								LOGGER.info("270");

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
								transaction.setPaymentId(json.get("paymentId").toString());
								transaction.setInvoiceNumber(postpaidPayment.getInvoiceNumber());
								transaction.setInvoice(postpaidPayment.getInvoice());

//								String receiptNo = FileUtils.getRandomOTPnumber(8);
//								Path con = Paths.get(context.getRealPath("/WEB-INF/"));
//								
//								if(transaction.getTransactionAmountModel()!=null && transaction.getTransactionAmountModel().getConvenianceFee()>0) {
//									
//									String conveInvo = invoiceGenerate.convenienceInvoice(con, transaction, user, postpaidPayment, null);
//									
//									postpaidPayment.setConvInvoice(conveInvo);
//									transaction.setConvInvoice(conveInvo);
//								}
//
//								String receipt = "";


//								transaction.setReceiptNumber(receiptNo);

								transactionRepository.save(transaction);
								LOGGER.info("291");

								postpaidRepository.save(postpaidPayment);
								
								RequestModel dto = new RequestModel();
								
								dto.setDebit(0);
								dto.setDebitGst(0);
								dto.setCredit(pay.getExclusiveAmount());
								dto.setCreditGst(pay.getExclusiveAmount()*18/100);
								dto.setRemark("Credit");
								dto.setConsumedBalance(user.getConsumedAmount());
								
								postpaidUserStatementService.statementEntry(user, dto);
							}
						}

						paymentModelRepository.save(pay);
						transactionRepository.save(transaction);
					}

					Map<String, Object> map = new HashMap<>();
					map.put("orderReference", json.optString("orderReference"));
					map.put("paymentId", json.optString("paymentId"));
					map.put("status", json.optString("status"));
					map.put("error", json.optString("error"));
					map.put("paymentMethod", json.optString("paymentMethod"));
					map.put("paymentMode", json.optString("paymentMode"));
					map.put("bankReference", json.optString("bankReference"));
					map.put("paidAt", json.optString("paidAt"));
					map.put("currency", json.optString("currency"));

					map.put("amount", json.optString("amount"));
					map.put("orderAmount", json.optString("orderAmount"));

					map.put("merchantOrderNo", json.optString("merchantOrderNo"));

					map.put("cardNoMasked", json.optString("cardNoMasked"));
					map.put("customerName", json.optString("customerName"));
					map.put("customerEmail", json.optString("customerEmail"));
					map.put("customerMobile", json.optString("customerMobile"));

					map.put("address", json.optString("address"));
					map.put("postalCode", json.optString("postalCode"));
					map.put("city", json.optString("city"));
					map.put("region", json.optString("region"));

					map.put("country", json.optString("country"));

					map.put("deliveryName", json.optString("deliveryName"));

					map.put("deliveryMobile", json.optString("deliveryMobile"));

					map.put("deliveryAddress", json.optString("deliveryAddress"));
					map.put("deliveryPostalCode", json.optString("deliveryPostalCode"));
					map.put("deliveryCity", json.optString("deliveryCity"));
					map.put("deliveryRegion", json.optString("deliveryRegion"));
					map.put("deliveryCountry", json.optString("deliveryCountry"));
					map.put("udf1", json.optString("udf1"));
					map.put("udf2", json.optString("udf2"));
					map.put("udf3", json.optString("udf3"));
					map.put("udf4", json.optString("udf4"));
					map.put("udf5", json.optString("udf5"));

					System.out.println("Status Message :" + status);
					response.setData(map);
					response.setFlag(1);
					response.setMessage(AppConstants.SUCCESS);

				} else {

					response.setFlag(2);
					response.setMessage(AppConstants.NO_DATA_FOUND);

				}
			} else {
				response.setFlag(3);
				response.setMessage(AppConstants.NO_DATA_FOUND);

			}
			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			response.setFlag(4);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(AppConstants.TECHNICAL_ERROR);
			response.setErrorDiscription(e.getLocalizedMessage());
		}

		return response;
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

	/*
	 * -----------------------------------------------------------------------------
	 * --------------------------------------------
	 */

	@Override
	public ResponseStructure addPaymentMethod(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			PaymentMethod paymentMethod = new PaymentMethod();

			paymentMethod.setPaymentType(model.getPaymentType());
			paymentMethod.setCreatedBy(model.getCreatedBy());
			paymentMethod.setCreatedAt(LocalDate.now());
			paymentMethod.setStatus(true);

			paymentRepository.save(paymentMethod);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(paymentMethod);
			structure.setMessage(AppConstants.SUCCESS);

		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure viewPaymentById(int paymentId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<PaymentMethod> optional = paymentRepository.findById(paymentId);
			if (optional.isPresent()) {

				PaymentMethod paymentMethod = optional.get();

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(paymentMethod);
				structure.setMessage(AppConstants.SUCCESS);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewAllPaymentMethod() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<PaymentMethod> list = paymentRepository.findAll();

			if (!list.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(list);
				structure.setMessage(AppConstants.SUCCESS);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure changeStatus(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<PaymentMethod> optional = paymentRepository.findById(model.getPaymentId());
			if (optional.isPresent()) {
				PaymentMethod statusChange = optional.get();
				statusChange.setStatus(model.isAccountStatus());
				paymentRepository.save(statusChange);

				structure.setMessage("Account status updated");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(statusChange);
				structure.setFlag(1);
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);

			}
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	
	
	private LocalDate endDateByPaymentCycle(LocalDate startDate, String paymentCycle) throws Exception {

		LocalDate now = startDate;

		int dayOfMonth = now.getDayOfMonth();

		LocalDate endDate = LocalDate.now();

		if (dayOfMonth == 1) {

			if (paymentCycle.equalsIgnoreCase("Monthly")) {

				endDate = now.plusDays(now.lengthOfMonth());

			} else if (paymentCycle.equalsIgnoreCase("Quarterly")) {

				endDate = now.plusMonths(2);

			} else if (paymentCycle.equalsIgnoreCase("Half Yearly")) {

				endDate = now.plusMonths(5);

			} else {

				endDate = now.plusYears(1);
			}

		} else {

			if (paymentCycle.equalsIgnoreCase("Monthly")) {

				return now.plusDays(now.lengthOfMonth() - dayOfMonth);

			} else if (paymentCycle.equalsIgnoreCase("Quarterly")) {

				LocalDate day1 = now.minusDays(now.getDayOfMonth()-1);
				
				return day1.plusMonths(2);

			} else if (paymentCycle.equalsIgnoreCase("Half Yearly")) {
				
				LocalDate day1 = now.minusDays(now.getDayOfMonth()-1);
				return day1.plusMonths(5);
				
			} else {
				
				LocalDate day1 = now.minusDays(now.getDayOfMonth()-1);
				return day1.plusYears(1);
			}
		}
		return endDate;
	}

	
	
	
	
}
