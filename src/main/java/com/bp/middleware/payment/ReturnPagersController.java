package com.bp.middleware.payment;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.pgmode.PGModeModel;
import com.bp.middleware.pgmode.PGModeRepository;
import com.bp.middleware.postpaidstatement.PostpaidUserStatementService;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.prepaidstatement.PrepaidUserStatementService;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.transaction.TransactionRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.InvoiceGenerate;
import com.bp.middleware.util.ReceiptGenerator;

import jakarta.servlet.ServletContext;

@Controller

@RequestMapping("/returns")
public class ReturnPagersController {

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

	private static final Logger LOGGER = LoggerFactory.getLogger(ReturnPagersController.class);

	@RequestMapping(path = "/viewpage", method = RequestMethod.GET)
	public String getViewPage() {
		System.out.println("Return");
		LOGGER.info("This is view page API.");
		return "Welcome";
	}

	@RequestMapping(value = "/returnpager", method = RequestMethod.POST)
	public RedirectView returnPagers(@ModelAttribute RequestModel dto) {

		RedirectView redirectView = new RedirectView();
		try {

			LOGGER.info(
					"RETURN PAGER START ---------------------------------------------------------------------------------");
			Optional<PGModeModel> pgOptional = pgModeRepository.findByPgOnoffStatus(1);
			int payId = 0;
			if (pgOptional.isPresent()) {

				PGModeModel pgModel = pgOptional.get();

				JSONObject inputParams = new JSONObject();
				inputParams.put("reference", dto.getReference());
				inputParams.put("success", dto.isSuccess());

				String[] hashColumns;
				hashColumns = new String[] { "reference", "success" };

				String hashData = pgModel.getApikey();
				for (int i = 0; i < hashColumns.length; i++) {
					hashData += '|' + inputParams.get(hashColumns[i]).toString().trim();

				}
				LOGGER.info("110");
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
				HttpEntity<String> httpEntity = new HttpEntity<>(inputParams.toString(), headers);

				String pgPostUrl = null;

				if (pgModel.getPgMode().equalsIgnoreCase("TEST")) {
					pgPostUrl = AppConstants.PG_RES_URL;
				} else {
					pgPostUrl = AppConstants.PG_PRODUCTION_URL;
				}

				ResponseEntity<String> clientResponse = restTemplate.postForEntity(pgPostUrl, httpEntity, String.class);

				String keys = clientResponse.getBody();
				JSONObject json = new JSONObject(keys);

				String merchantOrder = null;
				merchantOrder = json.get("merchantOrderNo").toString();

				LOGGER.info("154");
				Optional<TransactionDto> transactionHistory = transactionRepository.findByTrackId(merchantOrder);
				LOGGER.info("156");

				LocalDateTime currentLocalDateTime = LocalDateTime.now();
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				String formattedDateTime = currentLocalDateTime.format(dateTimeFormatter);

				int flag = 0;

				if (transactionHistory.isPresent()) {
					TransactionDto transaction = transactionHistory.get();

					int transactionId = transaction.getTrancsactionId();

					EntityModel entity = transaction.getPayid().getEntity();
					payId = transaction.getPayid().getPayid();

					String invoiceNumber = FileUtils.getRandomOTPnumber(7);

					if (json.get(AppConstants.STATUS).toString().equalsIgnoreCase("Success")) {

						// emailService.sendEmailAdminOTPVerification("abhishek.p@basispay.in",
						// keys,"Payment Id"+json.get("paymentId").toString(), "transaction Id
						// "+transaction.getTrancsactionId(), "www.google.com");

						boolean entityState = entity.getStateName().equalsIgnoreCase("Tamilnadu")
								|| entity.getStateName().equalsIgnoreCase("Tamil nadu");

						if (entity.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

							LOGGER.info("SUCCESS PAYEMENT ID : 1");

							LOGGER.info("178");
							Optional<PaymentModel> optional = paymentModelRepository.findById(payId);

							LOGGER.info("181");
							if (optional.isPresent()) {

								PaymentModel payModel = optional.get();
								payModel.setPaymentId(json.get("paymentId").toString());
								payModel.setPaymentStatus("Success");
								payModel.setPaymentDateTime(formattedDateTime);

								PrepaidPayment prepaid = prepaidRepository.findByEntityModelAndPayId(entity,
										payModel.getPayid());

								prepaid.setRemark(json.get(AppConstants.STATUS).toString());
								prepaid.setRechargedAmount(payModel.getExclusiveAmount());
								prepaid.setTransactionId(json.get("paymentId").toString());
								prepaid.setPaymentProcess("Completed Successfully");
								prepaid.setPaidDate(LocalDate.now());
								prepaid.setMonth(LocalDate.now().getMonth().toString());

								entity.setRemainingAmount(fu
										.twoDecimelDouble(entity.getRemainingAmount() + payModel.getExclusiveAmount())); // calculation
								entity.setPaymentStatus(json.get(AppConstants.STATUS).toString());

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

								userRepository.save(entity);
								prepaidRepository.save(prepaid);

								Path con = Paths.get(context.getRealPath("/WEB-INF/"));
								// String invoice = InvoiceGenerate.invoiceGenerateRouter(con, transaction,
								// entity, null, prepaid);

								String receiptNo = FileUtils.getRandomOTPnumber(8);
//								String receipt = "";

//								if(entityState) {

//								receipt = receiptGenerator.prepaidRecieptGeneratorCgstSgst(con, transaction, prepaid,
//										entity, receiptNo, formattedDateTime);
//								}else {
//									receipt = ReceiptGenerator.prepaidRecieptGeneratorIgst(con, transaction, prepaid, entity, receiptNo, formattedDateTime);
//								} 

//								transaction.setReceipt(receipt);
								transaction.setReceiptNumber(receiptNo);
								// transaction.setInvoice(invoice);

								LOGGER.info("212");
								transactionRepository.save(transaction);
								LOGGER.info("221");

//								prepaid.setReceipt(receipt);
								// prepaid.setInvoice(invoice);
								prepaidRepository.save(prepaid);

								dto.setDebit(0);
								dto.setCredit(payModel.getExclusiveAmount());
								dto.setRemark("Credit");
								dto.setDebitGst(0);
								dto.setClosingBalance(entity.getRemainingAmount());
								dto.setConsumedBalance(entity.getConsumedAmount());

								prepaidUserStatementService.statementEntry(entity, dto);

								emailService.prepaidPaymentSuccessMail(entity.getEmail(), transaction, prepaid);

							}
//							return postpaidRedirectViewSuccess(json,transaction,payId,entity,formattedDateTime,redirectView);

						} else if (entity.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {

							LOGGER.info("SUCCESS PAYEMENT ID : 2");
							LOGGER.info("231");
							Optional<PaymentModel> optional = paymentModelRepository.findById(payId);
							LOGGER.info("233");

							if (optional.isPresent()) {

								PaymentModel payModel = optional.get();
								payModel.setPaymentId(json.get("paymentId").toString());
								payModel.setPaymentStatus(json.get(AppConstants.STATUS).toString());
								payModel.setPaymentDateTime(formattedDateTime);

								PostpaidPayment postpaidPayment = postpaidRepository
										.findByEntityModelAndPaymentFlagAndPayId(entity, false, payId);

								postpaidPayment.setPaymentFlag(true);
								postpaidPayment.setRemark(json.get(AppConstants.STATUS).toString());
								postpaidPayment.setPaidAmount(payModel.getPaidAmount());

								if (payModel.getPaidAmount() - postpaidPayment.getTotalAmount() <= 0) {
									postpaidPayment.setDueAmount(0);
								} else {
									postpaidPayment
											.setDueAmount(payModel.getPaidAmount() - postpaidPayment.getTotalAmount());
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

								newPostpaidPayment.setStartDate(entity.getEndDate().plusDays(1));
								newPostpaidPayment.setEndDate(endDateByPaymentCycle(newPostpaidPayment.getStartDate(),
										entity.getPostpaidPaymentCycle()));
								newPostpaidPayment.setEntityModel(entity);
								newPostpaidPayment.setPaymentFlag(false);
								newPostpaidPayment.setTotalAmount(0);

								LocalDate graceDate = newPostpaidPayment.getEndDate().plusDays(entity.getGracePeriod());

								entity.setStartDate(entity.getEndDate().plusDays(1));
								entity.setEndDate(
										endDateByPaymentCycle(entity.getStartDate(), entity.getPostpaidPaymentCycle()));
								entity.setPostpaidFlag(false);
								entity.setConsumedAmount(postpaidPayment.getDueAmount());
								entity.setPaymentStatus("No dues");
								entity.setGraceDate(graceDate);
								entity.setPaymentStatus(json.get(AppConstants.STATUS).toString());

								LOGGER.info("267");

								postpaidRepository.save(newPostpaidPayment);
								userRepository.save(entity);
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

								String receiptNo = FileUtils.getRandomOTPnumber(8);
								Path con = Paths.get(context.getRealPath("/WEB-INF/"));

//								if (transaction.getTransactionAmountModel() != null
//										&& transaction.getTransactionAmountModel().getConvenianceFee() > 0) {
//
//									String conveInvo = invoiceGenerate.convenienceInvoice(con, transaction, entity,
//											postpaidPayment, null);
//
//									postpaidPayment.setConvInvoice(conveInvo);
//									transaction.setConvInvoice(conveInvo);
//								}

								String receipt = "";

//								if(entityState) {
//									
//									receipt = ReceiptGenerator.postpaidRecieptGeneratorCgstSgst(con, transaction, postpaidPayment, entity, receiptNo, formattedDateTime);
//								}else {
//									receipt = ReceiptGenerator.postpaidRecieptGeneratorIgst(con, transaction, postpaidPayment, entity, receiptNo, formattedDateTime);
//								}

//								transaction.setReceipt(receipt);
//								transaction.setReceiptNumber(receiptNo);
//								transaction.setInvoice(invoice);

								transactionRepository.save(transaction);
								LOGGER.info("291");
//								postpaidPayment.setReceipt(receipt);
//								postpaidPayment.setInvoice(invoice);

								postpaidRepository.save(postpaidPayment);

								dto.setDebit(0);
								dto.setDebitGst(0);
								dto.setCredit(payModel.getExclusiveAmount());
								dto.setCreditGst(payModel.getExclusiveAmount() * 18 / 100);
								dto.setRemark("Credit");
								dto.setConsumedBalance(entity.getConsumedAmount());

								postpaidUserStatementService.statementEntry(entity, dto);

//								emailService.postpaidPaymentSuccessMail(entity.getEmail(), transaction,
//										postpaidPayment);
							}
							LOGGER.info("296");
						}

						redirectView
								.setUrl("http://157.245.105.135:5031/paymentsuccess?transactionId=" + transaction.getTrackId());

					} else if (json.get(AppConstants.STATUS).toString().equalsIgnoreCase("Failure")) {

						transaction.setPaymentId(json.get("paymentId").toString());
						transaction.setPaymentStatus(json.get(AppConstants.STATUS).toString());
						Optional<PaymentModel> optional = paymentModelRepository.findById(payId);

						if (optional.isPresent()) {

							PaymentModel payModel = optional.get();

							payModel.setPaymentStatus(transaction.getPaymentStatus());
							paymentModelRepository.save(payModel);
						}

						if (entity.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {

							LOGGER.info("FAILURE PAYMENT ID : 1");

							PostpaidPayment postpaidPayment = postpaidRepository.findByEntityModelAndPayId(entity,
									payId);

							entity.setPaymentStatus(json.get(AppConstants.STATUS).toString());
							userRepository.save(entity);

							postpaidPayment.setRemark(json.get(AppConstants.STATUS).toString());
							postpaidRepository.save(postpaidPayment);

						} else if (entity.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

							LOGGER.info("FAILURE PAYEMENT ID : 2");
							LOGGER.info("327");
							PrepaidPayment prepaid = prepaidRepository.findByEntityModelAndPayId(entity, payId);
							LOGGER.info("329");

							entity.setPaymentStatus(json.get(AppConstants.STATUS).toString());

							userRepository.save(entity);

							prepaid.setRemark(json.get(AppConstants.STATUS).toString());
							prepaid.setPaymentProcess("Failed");
							prepaidRepository.save(prepaid);
						}
						LOGGER.info("334");
						redirectView
								.setUrl("http://157.245.105.135:5031/paymentfailure?transactionId=" + transaction.getTrackId());
					}
					transactionRepository.save(transaction);
				}
			}
		} catch (Exception e) {
			try {

				int line = 0;
				String classname = "";

				StackTraceElement[] stackTraceElements = e.getStackTrace();
				if (stackTraceElements.length > 0) {

					line = stackTraceElements[0].getLineNumber();
					classname = (stackTraceElements[0].getClassName());
				}
				emailService.sendEmailAdminOTPVerification("abhishek.p@basispay.in", e.getLocalizedMessage(),
						"LINE NUMBER : " + line, "Class : " + classname, "www.google.com");
			} catch (Exception f) {
				LOGGER.info("SUCCESS PAYEMENT ID : 1");
			}

			LOGGER.info("An Error occured in return pagers", e);
			return null;
		}
		return redirectView;
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

				LocalDate day1 = now.minusDays(now.getDayOfMonth() - 1);

				return day1.plusMonths(2);

			} else if (paymentCycle.equalsIgnoreCase("Half Yearly")) {

				LocalDate day1 = now.minusDays(now.getDayOfMonth() - 1);
				return day1.plusMonths(5);

			} else {

				LocalDate day1 = now.minusDays(now.getDayOfMonth() - 1);
				return day1.plusYears(1);
			}
		}
		return endDate;
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

}
