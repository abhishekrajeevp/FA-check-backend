package com.bp.middleware.manualpayment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.postpaidstatement.PostpaidUserStatementService;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.prepaidstatement.PrepaidUserStatementService;
import com.bp.middleware.responsestructure.ResponseStructure;
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
import jakarta.servlet.http.HttpServletRequest;

@Service
public class ManualPaymentService {

	@Autowired
	private ManualPaymentRepository manualPaymentRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PrepaidRepository prepaidRepository;
	@Autowired
	private PostpaidRepository postpaidRepository;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	EmailService emailService;
	@Autowired
	ServletContext context;
	@Autowired
	InvoiceGenerate invoiceGenerate;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	FileUtils fu;
	@Autowired
	ReceiptGenerator receiptGenerator;
	@Autowired
	private PrepaidUserStatementService prepaidUserStatementService;
	@Autowired
	private PostpaidUserStatementService postpaidUserStatementService;

	public ResponseStructure makeManualPayment(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<EntityModel> opt = userRepository.findById(model.getUserId());

			if (opt.isPresent() && model.getTotalAmount() > 0) {

				EntityModel entity = opt.get();

				if (entity.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

					return prepaidManualPayment(entity, model);

				} else if (entity.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {

					return postpaidManualPayment(entity, model);
				}

			} else {

				if (model.getTotalAmount() <= 0) {
					structure.setMessage("Please choose payment amount");

				} else {
					structure.setMessage(AppConstants.NO_DATA_FOUND);
				}
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	private ResponseStructure prepaidManualPayment(EntityModel entity, RequestModel model) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		LocalDateTime currentLocalDateTime = LocalDateTime.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		String formattedDateTime = currentLocalDateTime.format(dateTimeFormatter);

		boolean entityState = entity.getStateName().equalsIgnoreCase("Tamilnadu")
				|| entity.getStateName().equalsIgnoreCase("Tamil nadu");

		double totalAmount = fu.twoDecimelDouble(model.getTotalAmount());
//		double igstAmount = Math.ceil(gstDeduction(entity, totalAmount));
//		double exclusiveAmount = Math.ceil(totalAmount - igstAmount);
//		double cgstOrSgst = Math.ceil(igstAmount / 2);

		String receiptNo = FileUtils.getRandomOTPnumber(8);
		String trackId = FileUtils.getRandomString();
		String paymentTransactionId = FileUtils.getRandomOTPnumber(9);
		String invoiceNumber = FileUtils.getRandomOTPnumber(7);

		PrepaidPayment prepaid = new PrepaidPayment();

		prepaid.setRemark("Success");
		prepaid.setPaymentMode(model.getPaymentMode());
		prepaid.setPaidDate(LocalDate.now());
		prepaid.setMonth(LocalDate.now().getMonth().toString());
		prepaid.setRechargedAmount(totalAmount);// exclusive amount replced by total amount because gst is calculated
												// per hit
		prepaid.setEntityModel(entity);
		prepaid.setTransactionId(paymentTransactionId);
		prepaid.setPaymentProcess("Completed Successfully");
		prepaid.setReceipt(receiptNo);
		prepaid.setPaymentDateTime(model.getPaymentDateTime());
		prepaid.setUniqueId(FileUtils.generateApiKeys(8));

		TransactionDto transactionObj = new TransactionDto();

		transactionObj.setPayid(null);
		transactionObj.setPaymentStatus("Success");
		transactionObj.setCreatedBy(model.getCreatedBy());
		transactionObj.setInvoiceNumber(invoiceNumber);
		transactionObj.setPaymentId(paymentTransactionId);
		transactionObj.setReceiptNumber(receiptNo);
		transactionObj.setPaidAmount(totalAmount);
		transactionObj.setExclusiveAmount(totalAmount);
		transactionObj.setCreatedDatetime(new Date());
		transactionObj.setDescription(AppConstants.SUCCESS);
		transactionObj.setPaymentMode(model.getPaymentMode());
		transactionObj.setAddress(entity.getAddress());
		transactionObj.setEmail(entity.getEmail());
		transactionObj.setMemberName(entity.getAccountHolderName());
		transactionObj.setMobileNumber(entity.getMobileNumber());
		transactionObj.setPaymentDatetime(model.getPaymentDateTime());
		transactionObj.setPaymentMethod(AppConstants.PAYMENT_TYPE);
		transactionObj.setEntity(entity);
		transactionObj.setTrackId(trackId);

//		if (entityState) {
//			transactionObj.setCgstAmount(cgstOrSgst);
//			transactionObj.setSgstAmount(cgstOrSgst);
//
//			transactionObj.setIgstAmount(0.0);
//		} else {
//			transactionObj.setIgstAmount(igstAmount);
//
//			transactionObj.setCgstAmount(0.0);
//			transactionObj.setSgstAmount(0.0);
//		}

		entity.setRemainingAmount(fu.twoDecimelDouble(entity.getRemainingAmount() + totalAmount));

		userRepository.save(entity);
		prepaidRepository.save(prepaid);

		transactionObj.setPrepaidId(prepaid.getPrepaidId());
		transactionRepository.save(transactionObj);

		ManualPayment manualPay = new ManualPayment();

		manualPay.setCustomerName(entity.getName());
		manualPay.setCustomerAddress(entity.getAddress());
		manualPay.setContactNumber(entity.getMobileNumber());
		manualPay.setPaidAmount(totalAmount);
		manualPay.setExclusiveAmount(totalAmount);
		manualPay.setReceiptNumber(receiptNo);
		manualPay.setPaymentDateTime(formattedDateTime);
		manualPay.setDescription(model.getDescription());
		manualPay.setCreatedBy(model.getCreatedBy());
		manualPay.setCreatedAt(new Date());
		manualPay.setTrackId(trackId);
		manualPay.setModeOfPay(model.getModeOfPay());
		manualPay.setPaymentMode(model.getPaymentMode());
		manualPay.setReferenceId(FileUtils.generateApiKeys(8));
		manualPay.setPaymentId(paymentTransactionId);

		manualPay.setEntity(entity);
		manualPay.setPrepaid(prepaid);
		manualPay.setTransaction(transactionObj);

		manualPaymentRepository.save(manualPay);

		Path con = Paths.get(context.getRealPath("/WEB-INF/"));

//		String receipt = "";

//		if (entityState) {
//
//			receipt = ReceiptGenerator.prepaidRecieptGeneratorCgstSgst(con, transactionObj, prepaid, entity, receiptNo,
//					formattedDateTime);
//		} else {
//			receipt = ReceiptGenerator.prepaidRecieptGeneratorIgst(con, transactionObj, prepaid, entity, receiptNo,
//					formattedDateTime);
//		}

//		prepaid.setReceipt(receipt);

//		transactionObj.setReceipt(receipt);
//		manualPay.setReceipt(receipt);

		prepaidRepository.save(prepaid);
		transactionRepository.save(transactionObj);
		manualPaymentRepository.save(manualPay);

		model.setDebit(0);
		model.setCredit(totalAmount);
		model.setRemark("Credit");
		model.setDebitGst(0);
		model.setClosingBalance(entity.getRemainingAmount());
		model.setConsumedBalance(entity.getConsumedAmount());
		
		prepaidUserStatementService.statementEntry(entity, model);
		
		boolean sent = emailService.prepaidPaymentSuccessMail(entity.getEmail(), transactionObj, prepaid);
		
		structure.setData(manualPay);
		structure.setMessage(AppConstants.SUCCESS);
		structure.setFlag(1);
		structure.setStatusCode(HttpStatus.OK.value());

		return structure;
	}

	private double gstDeduction(EntityModel entity, double totalAmount) throws Exception {

		double igstAmount = totalAmount * AppConstants.IGST;

		return igstAmount;

	}

	
	private ResponseStructure postpaidManualPayment(EntityModel entity, RequestModel model) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		LocalDateTime currentLocalDateTime = LocalDateTime.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		String formattedDateTime = currentLocalDateTime.format(dateTimeFormatter);

		boolean entityState = entity.getStateName().equalsIgnoreCase("Tamilnadu")
				|| entity.getStateName().equalsIgnoreCase("Tamil nadu");

		double totalAmount = fu.twoDecimelDouble(model.getTotalAmount());
		double igstAmount = fu.twoDecimelDouble(gstDeduction(entity, totalAmount));
		double exclusiveAmount = fu.twoDecimelDouble(totalAmount - igstAmount);
		double cgstOrSgst = fu.twoDecimelDouble(igstAmount / 2);

		PostpaidPayment postpaidHistory = postpaidRepository.findByEntityModelAndPaymentFlag(entity, false);

//		String receiptNo = FileUtils.getRandomOTPnumber(8);
		String trackId = FileUtils.getRandomString();
		String paymentTransactionId = FileUtils.getRandomOTPnumber(9);
//		String invoiceNumber = FileUtils.getRandomOTPnumber(7);

		double dueAmount = postpaidHistory.getTotalAmount() - totalAmount;

		postpaidHistory.setRemark("Success");
		postpaidHistory.setPaymentMode(model.getPaymentMode());
		postpaidHistory.setEntityModel(entity);
		postpaidHistory.setPaidDate(LocalDate.now());
		postpaidHistory.setExclusiveAmount(exclusiveAmount);
		postpaidHistory.setPaidAmount(totalAmount);
		postpaidHistory.setPayId(0);
		postpaidHistory.setTransactionId(paymentTransactionId);
		postpaidHistory.setPaymentDateTime(model.getPaymentDateTime());
		postpaidHistory.setUniqueId(FileUtils.generateApiKeys(8));

		if (dueAmount <= 0) {
			
			postpaidHistory.setDueAmount(0);
			postpaidHistory.setPaymentFlag(true);
			postpaidHistory.setRemark("Success");

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
			entity.setConsumedAmount(0);
			entity.setPaymentStatus("No dues");
			entity.setGraceDate(graceDate);

			postpaidRepository.save(newPostpaidPayment);

		} else {
			
			postpaidHistory.setDueAmount(dueAmount);
			postpaidHistory.setRemark("Success");

			entity.setPaymentStatus("Dues pending");
		}

		TransactionDto transactionObj = new TransactionDto();

		transactionObj.setPayid(null);
		transactionObj.setPaymentStatus("Success");
		transactionObj.setCreatedBy(model.getCreatedBy());
		transactionObj.setPaymentId(paymentTransactionId);
		transactionObj.setReceiptNumber("");
		transactionObj.setPaidAmount(totalAmount);
		transactionObj.setExclusiveAmount(exclusiveAmount);
		transactionObj.setCreatedDatetime(new Date());
		transactionObj.setDescription(AppConstants.SUCCESS);
		transactionObj.setPaymentMode(model.getPaymentMode());
		transactionObj.setAddress(entity.getAddress());
		transactionObj.setEmail(entity.getEmail());
		transactionObj.setMemberName(entity.getAccountHolderName());
		transactionObj.setMobileNumber(entity.getMobileNumber());
		transactionObj.setPaymentDatetime(model.getPaymentDateTime());
		transactionObj.setPaymentMethod(AppConstants.PAYMENT_TYPE);
		transactionObj.setEntity(entity);
		transactionObj.setTrackId(trackId);
		transactionObj.setInvoiceNumber(postpaidHistory.getInvoiceNumber());
		transactionObj.setInvoice(postpaidHistory.getInvoice());

		if (entityState) {
			transactionObj.setCgstAmount(cgstOrSgst);
			transactionObj.setSgstAmount(cgstOrSgst);

			transactionObj.setIgstAmount(0.0);
		} else {
			transactionObj.setIgstAmount(igstAmount);

			transactionObj.setCgstAmount(0.0);
			transactionObj.setSgstAmount(0.0);
		}

		entity.setPaymentStatus("Success");
		
		userRepository.save(entity);
		postpaidRepository.save(postpaidHistory);

		transactionObj.setPostpaidId(postpaidHistory.getPostpaidId());
		transactionRepository.save(transactionObj);

		ManualPayment manualPay = new ManualPayment();

		manualPay.setCustomerName(entity.getName());
		manualPay.setCustomerAddress(entity.getAddress());
		manualPay.setContactNumber(entity.getMobileNumber());
		manualPay.setPaidAmount(totalAmount);
		manualPay.setExclusiveAmount(exclusiveAmount);
		manualPay.setReceiptNumber("");
		manualPay.setPaymentDateTime(formattedDateTime);
		manualPay.setDescription(model.getDescription());
		manualPay.setCreatedBy(model.getCreatedBy());
		manualPay.setCreatedAt(new Date());
		manualPay.setModeOfPay("Manual");
		manualPay.setTrackId(trackId);
		manualPay.setPaymentMode(model.getPaymentMode());
		manualPay.setReferenceId(FileUtils.generateApiKeys(8));
		manualPay.setPaymentId(paymentTransactionId);

		if (entityState) {
			manualPay.setCgst(cgstOrSgst);
			manualPay.setSgst(cgstOrSgst);

			manualPay.setIgst(0.0);
		} else {
			manualPay.setIgst(igstAmount);

			manualPay.setCgst(0.0);
			manualPay.setSgst(0.0);
		}

		manualPay.setEntity(entity);
		manualPay.setPostpaid(postpaidHistory);
		manualPay.setTransaction(transactionObj);

		manualPaymentRepository.save(manualPay);

		Path con = Paths.get(context.getRealPath("/WEB-INF/"));

//		String invoice = invoiceGenerate.invoiceGenerateRouter(con, transactionObj, entity, postpaidHistory, null);

//		String receipt = "";
//		
//		if(entityState) {
//			
//			receipt = ReceiptGenerator.postpaidRecieptGeneratorCgstSgst(con, transactionObj, postpaidHistory, entity, receiptNo, formattedDateTime);
//		}else {
//			receipt = ReceiptGenerator.postpaidRecieptGeneratorIgst(con, transactionObj, postpaidHistory, entity, receiptNo, formattedDateTime);
//		}

//		System.err.println("Receipt : " + receipt);
//		System.err.println("Invoice : "+ invoice);

//		postpaidHistory.setReceipt(receipt);
//		postpaidHistory.setInvoice(invoice);

//		transactionObj.setInvoice(invoice);
//		transactionObj.setReceipt(receipt);

//		manualPay.setReceipt(receipt);

		// ALSO SEND INVOICE

		postpaidRepository.save(postpaidHistory);
		transactionRepository.save(transactionObj);
		manualPaymentRepository.save(manualPay);

		
		model.setDebit(0);
		model.setDebitGst(0);
		model.setCredit(postpaidHistory.getExclusiveAmount());
		model.setCreditGst(postpaidHistory.getExclusiveAmount()*18/100);
		model.setRemark("Credit");
		model.setConsumedBalance(entity.getConsumedAmount());
		
		postpaidUserStatementService.statementEntry(entity, model);
		
//		emailService.postpaidPaymentSuccessMail(entity.getEmail(), transactionObj, postpaidHistory);

		structure.setData(manualPay);
		structure.setMessage(AppConstants.SUCCESS);
		structure.setFlag(1);
		structure.setStatusCode(HttpStatus.OK.value());

		return structure;
	}

	public ResponseStructure viewAllManualPayment() {
		ResponseStructure structure = new ResponseStructure();

		try {

			List<ManualPayment> all = manualPaymentRepository.findAll();

			if (!all.isEmpty()) {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(all);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
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
	
	
	
	
	

	public ResponseStructure manualPaymentViewById(int manualPayId) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<ManualPayment> opt = manualPaymentRepository.findById(manualPayId);

			if (opt.isPresent()) {

				ManualPayment manualPayment = opt.get();

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(manualPayment);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure manualPaymentViewByUserId(int userId) {

		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel entity = userRepository.findByUserId(userId);

			if (entity != null) {
				List<ManualPayment> all = manualPaymentRepository.findByEntity(entity);

				if (!all.isEmpty()) {

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(all);
					structure.setFlag(1);

				} else {

					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure manualPaymentViewByTransactionId(int transactionId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<TransactionDto> trans = transactionRepository.findByTrancsactionId(transactionId);

			if (trans.isPresent()) {

				TransactionDto transactionDto = trans.get();

				Optional<ManualPayment> opt = manualPaymentRepository.findByTransaction(transactionDto);

				if (opt.isPresent()) {

					ManualPayment manualPayment = opt.get();

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(manualPayment);
					structure.setFlag(1);

				} else {

					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure postpaidActivation(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<EntityModel> opt = userRepository.findById(model.getUserId());

			if (opt.isPresent()) {

				EntityModel entity = opt.get();

				if (entity.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {

					if (model.isPostpaidDueAllow()) {

						PostpaidPayment postpaidHistory = postpaidRepository.findByEntityModelAndPaymentFlag(entity,
								false);

						double dueAmount = postpaidHistory.getDueAmount();

						postpaidHistory.setDueAmount(0);
						postpaidHistory.setPaymentFlag(true);
						postpaidHistory.setRemark("Success");

						PostpaidPayment newPostpaidPayment = new PostpaidPayment();

						newPostpaidPayment.setStartDate(entity.getEndDate().plusDays(1));
						newPostpaidPayment.setEndDate(endDateByPaymentCycle(newPostpaidPayment.getStartDate(),
								entity.getPostpaidPaymentCycle()));
						newPostpaidPayment.setEntityModel(entity);
						newPostpaidPayment.setPaymentFlag(false);
						newPostpaidPayment.setTotalAmount(dueAmount);

						LocalDate graceDate = newPostpaidPayment.getEndDate().plusDays(entity.getGracePeriod());

						entity.setStartDate(entity.getEndDate().plusDays(1));
						entity.setEndDate(
								endDateByPaymentCycle(entity.getStartDate(), entity.getPostpaidPaymentCycle()));
						entity.setPostpaidFlag(false);
						entity.setConsumedAmount(fu.twoDecimelDouble(dueAmount));
						entity.setPaymentStatus("No dues");
						entity.setGraceDate(graceDate);

						userRepository.save(entity);
						
						postpaidRepository.save(postpaidHistory);
						postpaidRepository.save(newPostpaidPayment);

						structure.setMessage("The due amount has been added to the next payment.");
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(newPostpaidPayment);
						structure.setFlag(1);

					} else {

						structure.setMessage("This entity can only use services after paying the due amount.");
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(null);
						structure.setFlag(2);
					}

				} else {

					structure.setMessage("This entity is not a postpaid user.");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(3);
				}

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(4);
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

	public ResponseStructure addProofImage(int manualPayId, MultipartFile proof) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<ManualPayment> opt = manualPaymentRepository.findById(manualPayId);

			if (opt.isPresent()) {

				ManualPayment mp = opt.get();
				mp.setImageProof(saveProof(proof));

				manualPaymentRepository.save(mp);

				structure.setMessage("Image proof updated successfully");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(mp);
				structure.setFlag(1);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}
		} catch (Exception e) {

			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setData(structure);
		}
		return structure;
	}

	private String saveProof(MultipartFile profilePhoto) throws Exception {

		String extensionType = null;

		StringTokenizer st = new StringTokenizer(profilePhoto.getOriginalFilename(), ".");

		while (st.hasMoreElements()) {
			extensionType = st.nextElement().toString();
		}

		String fileName = profilePhoto.getOriginalFilename();
		Path currentWorkingDir = Paths.get(context.getRealPath(AppConstants.WEB_INF));
		File saveFile = new File(currentWorkingDir + "/manualpayment/");
		saveFile.mkdir();
		byte[] bytes = profilePhoto.getBytes();
		Path path = Paths.get(saveFile + "/" + fileName);
		Files.write(path, bytes);

		return fileName;

	}

	public ResponseEntity<Resource> viewProof(int manualpayId, HttpServletRequest request) {

		Optional<ManualPayment> opt = manualPaymentRepository.findById(manualpayId);

		if (opt.isPresent()) {

			if (opt.get().getImageProof() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/manualpayment/" + opt.get().getImageProof());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
					ex.printStackTrace();
				}

				// Fallback to the default content type if type could not be determined
				if (contentType == null) {
					contentType = "application/octet-stream";
				}
				return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
						.header(HttpHeaders.CONTENT_DISPOSITION,
								"attachment; filename=\"" + resource.getFilename() + "\"")
						.body(resource);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public ResponseStructure manualPaymentUpdate(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<ManualPayment> opt = manualPaymentRepository.findById(model.getManualPaymentId());

			if (opt.isPresent()) {

				ManualPayment manualPay = opt.get();

				EntityModel entity = manualPay.getEntity();

				if (entity.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

					return prepaidManualPayUpdate(model, manualPay, entity);

				} else {

					return postpaidManualPayUpdate(model, manualPay, entity);
				}

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(3);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}
		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setData(structure);
		}

		return structure;
	}

	private ResponseStructure prepaidManualPayUpdate(RequestModel model, ManualPayment manualPay, EntityModel entity)
			throws Exception {

		ResponseStructure structure = new ResponseStructure();

		double paidAmount = manualPay.getPaidAmount();

		manualPay.setPaidAmount(model.getTotalAmount());
		manualPay.setExclusiveAmount(model.getTotalAmount());
		manualPay.setPaymentDateTime(model.getPaymentDateTime());
		manualPay.setDescription(model.getDescription());
		manualPay.setModifiedBy(model.getModifiedBy());
		manualPay.setModifiedAt(new Date());
		manualPay.setModeOfPay("Manual");
		manualPay.setPaymentMode(model.getPaymentMode());

		PrepaidPayment prepaid = manualPay.getPrepaid();

		prepaid.setPaymentMode(model.getPaymentMode());
		prepaid.setRechargedAmount(model.getTotalAmount());// exclusive amount replced by total amount because gst is
															// calculated per hit
		prepaid.setPaymentProcess("Completed Successfully");

		TransactionDto transactionObj = manualPay.getTransaction();

		transactionObj.setPaidAmount(model.getTotalAmount());
		transactionObj.setExclusiveAmount(model.getTotalAmount());
		transactionObj.setDescription(AppConstants.SUCCESS);
		transactionObj.setPaymentMode(model.getPaymentMode());
		transactionObj.setPaymentDatetime(model.getPaymentDateTime());
		transactionObj.setPaymentMethod(AppConstants.PAYMENT_TYPE);

		entity.setRemainingAmount( fu.twoDecimelDouble(entity.getRemainingAmount() + (model.getTotalAmount() - paidAmount)) );

		userRepository.save(entity);
		prepaidRepository.save(prepaid);

		transactionRepository.save(transactionObj);

		manualPay.setEntity(entity);
		manualPay.setPrepaid(prepaid);
		manualPay.setTransaction(transactionObj);

		manualPaymentRepository.save(manualPay);

		structure.setData(manualPay);
		structure.setFlag(1);
		structure.setMessage("Updation Success");
		structure.setStatusCode(HttpStatus.OK.value());

		return structure;
	}

	private ResponseStructure postpaidManualPayUpdate(RequestModel model, ManualPayment manualPay, EntityModel entity)
			throws Exception {

		ResponseStructure structure = new ResponseStructure();

		double paidAmount = manualPay.getPaidAmount();

		if (paidAmount == model.getTotalAmount()) {

			manualPay.setReceiptNumber("");
			manualPay.setDescription(model.getDescription());
			manualPay.setModifiedBy(model.getModifiedBy());
			manualPay.setModifiedAt(new Date());
			manualPay.setModeOfPay("Manual");
			manualPay.setPaymentMode(model.getPaymentMode());

			TransactionDto transactionObj = new TransactionDto();

			transactionObj.setReceiptNumber("");
			transactionObj.setCreatedDatetime(new Date());
			transactionObj.setDescription(AppConstants.SUCCESS);
			transactionObj.setPaymentMode(model.getPaymentMode());
			transactionObj.setPaymentMethod(AppConstants.PAYMENT_TYPE);
			transactionObj.setEntity(entity);

			transactionRepository.save(transactionObj);

			manualPaymentRepository.save(manualPay);

			structure.setData(manualPay);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} else {

			structure.setData(manualPay);
			structure.setMessage("Can't update amount for Postpaid entity");
			structure.setFlag(2);
			structure.setStatusCode(HttpStatus.OK.value());
		}

		return structure;
	}

	public ResponseStructure triggerMail(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<ManualPayment> opt = manualPaymentRepository.findById(model.getManualPaymentId());

			if (opt.isPresent()) {

				ManualPayment manualPay = opt.get();

				EntityModel entity = manualPay.getEntity();
				TransactionDto trans = manualPay.getTransaction();
				PrepaidPayment prep = manualPay.getPrepaid();

//				boolean entityState = entity.getStateName().equalsIgnoreCase("Tamilnadu")
//						|| entity.getStateName().equalsIgnoreCase("Tamil nadu");

				Path con = Paths.get(context.getRealPath("/WEB-INF/"));

				if (entity.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

					String receipt = "";

					receipt = receiptGenerator.prepaidRecieptGeneratorCgstSgst(con, manualPay.getTransaction(),
							manualPay.getPrepaid(), entity, manualPay.getReceiptNumber(),
							manualPay.getPaymentDateTime());

					prep.setReceipt(receipt);
					trans.setReceipt(receipt);
					manualPay.setReceipt(receipt);

					prepaidRepository.save(prep);
					transactionRepository.save(trans);
					manualPaymentRepository.save(manualPay);

//					boolean sent = emailService.prepaidPaymentSuccessMail(entity.getEmail(), trans, prep);

					boolean sent = emailService.prepaidPaymentSuccessMail(entity.getEmail(), trans, prep);
					
					if (sent) {

						structure.setData(manualPay);
						structure.setMessage("Receipt Generated and has been mailed");
						structure.setFlag(1);
						structure.setStatusCode(HttpStatus.OK.value());

					} else {

						structure.setData(manualPay);
						structure.setMessage("Mail not send");
						structure.setFlag(2);
						structure.setStatusCode(HttpStatus.OK.value());
					}

				} else {

					structure.setData(manualPay);
					structure.setMessage("Can't generate receipt for postpaid user");
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(3);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}
		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setData(structure);
		}

		return structure;
	}

}
