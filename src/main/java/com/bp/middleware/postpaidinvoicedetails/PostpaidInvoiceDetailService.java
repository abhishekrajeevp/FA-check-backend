package com.bp.middleware.postpaidinvoicedetails;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.conveniencefee.ConveniencePercentageEntity;
import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.modeofpgpayment.ModeOfPaymentPg;
import com.bp.middleware.payment.PaymentMethod;
import com.bp.middleware.payment.PaymentRepository;
import com.bp.middleware.prepaidinvoicesdetails.PrepaidConvenienceInvoice;
import com.bp.middleware.prepaidinvoicesdetails.PrepaidMonthlyInvoice;
import com.bp.middleware.prepaidinvoicesdetails.PrepaidUtils;
import com.bp.middleware.prepaidmonthlyinvoice.PrepaidMonthlyInvoiceService;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.signers.SignerRepository;
import com.bp.middleware.signmerchant.MerchantRepository;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.transaction.TransactionRepository;
import com.bp.middleware.transactionamountmodel.TransactionAmountModel;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AmountToWords;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.InvoiceGenerate;
import com.bp.middleware.vendors.VendorVerificationModel;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class PostpaidInvoiceDetailService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PostpaidInvoiceDetailService.class);

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ResponseRepository responseRepository;
	@Autowired
	private RequestRepository requestRepository;
	@Autowired
	private PostpaidRepository postpaidRepository;
	@Autowired
	private PrepaidRepository prepaidRepository;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private MerchantRepository merchantRepository;
	@Autowired
	private SignerRepository signerRepository;
	@Autowired
	private ServletContext context;
	@Autowired
	EmailService emailService;
	@Autowired
	InvoiceGenerate invoiceGenerate;
	@Autowired
	FileUtils fu;
	@Autowired
	private PaymentRepository paymentMethodRepository;
	@Autowired
	private PostpaidUtils postpaidUtils;
	@Autowired
	private ResourceLoader resourceLoader;

	public ResponseStructure postpaidReminderInvoice() {

		ResponseStructure structure = new ResponseStructure();

		try {

			List<PostpaidPayment> flagFalseUser = postpaidRepository.findByPaymentFlag(false);
			List<PostpaidReminderInvoice> monthlyInvoList = new ArrayList<>();

			String accNo = postpaidUtils.getAccountNumber();
			String hsnNo = postpaidUtils.getHsnNumber();

			for (PostpaidPayment postpaidPayment : flagFalseUser) {

				if (LocalDate.now().isAfter(postpaidPayment.getEndDate())
						&& postpaidPayment.getEntityModel().isAccountStatus()) {

					EntityModel entity = postpaidPayment.getEntityModel();

					String startDate = postpaidPayment.getStartDate().toString() + " 00:00:00.0000000";
					String endDate = postpaidPayment.getEndDate().toString() + " 23:59:59.9999999";

					List<Request> hitLogs = requestRepository.getBetween(entity.getUserId(), startDate, endDate);

					if (!hitLogs.isEmpty()) {

						String invoiceNo = fu.getRandomOTPnumber(8);

						double logPrices = fu
								.twoDecimelDouble(postpaidUtils.getMonthHitPrice(entity, startDate, endDate));
						double logPriceGst = fu.twoDecimelDouble(logPrices * 18 / 100);

						double totalPrice = fu.twoDecimelDouble(logPrices + logPriceGst);
						int totalHits = hitLogs.size();

						List<Map<String, Object>> usedServices = postpaidUtils.getUsedServices(hitLogs, entity,
								startDate, endDate);

						postpaidPayment.setExclusiveAmount(postpaidPayment.getExclusiveAmount() + logPrices);
						postpaidPayment.setDueAmount(postpaidPayment.getDueAmount() + totalPrice);
						postpaidPayment.setTotalHits(totalHits);
						postpaidPayment.setInvoiceNumber(invoiceNo);
//						postpaidPayment.setUsedServices(usedServices);

						postpaidRepository.save(postpaidPayment);

						LocalDate graceDate = postpaidPayment.getEndDate()
								.plusDays(postpaidPayment.getEntityModel().getGracePeriod());

						PostpaidReminderInvoice invo = new PostpaidReminderInvoice();

						invo.setPostpaidId(postpaidPayment.getPostpaidId());

						invo.setStartDate(postpaidPayment.getStartDate());
						invo.setEndDate(postpaidPayment.getEndDate());
						invo.setDueDate(graceDate);

						invo.setUserId(entity.getUserId());
						invo.setEntityName(entity.getName());
						invo.setEntityAddress(entity.getAddress());
						invo.setEntityCountry(entity.getCountryName());
						invo.setEntityState(entity.getStateName());
						invo.setEntityCity(entity.getCityName());
						invo.setEntityPincode(entity.getPincode());
						invo.setEntityGst(entity.getGst());
						invo.setEntityContactNumber(entity.getMobileNumber());
						invo.setEntityMail(entity.getEmail());

						invo.setIfscCode(postpaidUtils.getIfscCode());
						invo.setBankName(postpaidUtils.getBankName());
						invo.setAdminBankAccountNo(accNo);
						invo.setAdminHsnCode(hsnNo);
						invo.setInvoiceNumber(invoiceNo);

						invo.setTotalHits(totalHits);
						invo.setUsedAmount(logPrices);
						invo.setUsedAmountGst(logPriceGst);
						invo.setTotalAmount(totalPrice);

						invo.setUsedAmountInWords(AmountToWords.convertAmountToWords(logPrices));
						invo.setUsedAmountGstInWords(AmountToWords.convertAmountToWords(logPriceGst));
						invo.setTotalAmountInWords(AmountToWords.convertAmountToWords(totalPrice));

						invo.setUsedServices(usedServices);

						monthlyInvoList.add(invo);

					} else {

						postpaidPayment.setPaymentFlag(false);
						postpaidPayment.setStartDate(entity.getEndDate().plusDays(1));
						postpaidPayment.setEndDate(postpaidUtils.endDateByPaymentCycle(postpaidPayment.getStartDate(),
								entity.getPostpaidPaymentCycle()));

						postpaidRepository.save(postpaidPayment);

						entity.setStartDate(entity.getEndDate().plusDays(1));
						entity.setEndDate(postpaidUtils.endDateByPaymentCycle(entity.getStartDate(),
								entity.getPostpaidPaymentCycle()));
						entity.setPostpaidFlag(false);
						entity.setConsumedAmount(0);
						entity.setPaymentStatus("No dues");
						entity.setGraceDate(entity.getEndDate().plusDays(entity.getGracePeriod()));

						userRepository.save(entity);
					}
				}
			}
			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(monthlyInvoList);
			structure.setFlag(1);

		} catch (Exception e) {

			e.printStackTrace();
		}
		return structure;
	}

	public ResponseStructure postpaidConvenienceInvoice(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			TransactionDto trans = transactionRepository.findByPostpaidId(model.getPostpaidId());

			if (trans != null) {

				EntityModel entityModel = trans.getEntity();
				ModeOfPaymentPg modeOfPaymentPg = trans.getModeOfPaymentPg();
				ConveniencePercentageEntity convenience = trans.getConveniencePercentageEntity();
				TransactionAmountModel transactionAmountModel = trans.getTransactionAmountModel();

				String accNo = postpaidUtils.getAccountNumber();
				String hsnNo = postpaidUtils.getHsnNumber();

				if (transactionAmountModel != null && transactionAmountModel.isConvenienceFetched()) {

					double conveniencePercentage = fu.twoDecimelDouble(convenience.getConveniencePercentage());

					double paidAmount = fu.twoDecimelDouble(trans.getExclusiveAmount());
					double fixedAmount = fu.twoDecimelDouble(transactionAmountModel.getFixedFee());
					double convenienceFee = fu.twoDecimelDouble(transactionAmountModel.getConvenianceFee());
					double gst = fu.twoDecimelDouble(transactionAmountModel.getOtherGst());

					double totalAmount = fu.twoDecimelDouble(fixedAmount + convenienceFee + gst);

					PostpaidConvenienceInvoice invo = new PostpaidConvenienceInvoice();

					invo.setUserId(entityModel.getUserId());
					invo.setPostpaidId(trans.getPostpaidId());
					invo.setTransactionId(trans.getTrancsactionId());

					invo.setEntityName(entityModel.getName());
					invo.setEntityAddress(entityModel.getAddress());
					invo.setEntityCountry(entityModel.getCountryName());
					invo.setEntityState(entityModel.getStateName());
					invo.setEntityCity(entityModel.getCityName());
					invo.setEntityPincode(entityModel.getPincode());
					invo.setEntityGst(entityModel.getGst());
					invo.setEntityContactNumber(entityModel.getMobileNumber());
					invo.setEntityMail(entityModel.getEmail());

					invo.setIfscCode(postpaidUtils.getIfscCode());
					invo.setBankName(postpaidUtils.getBankName());
					invo.setAdminBankAccountNo(accNo);
					invo.setAdminHsnCode(hsnNo);
					invo.setConveInvoiceNo("C-inv_" + fu.getRandomAlphaNumericString());
					invo.setModeOfPayment(modeOfPaymentPg.getModeOfPayment());

					invo.setPaidAmount(paidAmount);
					invo.setFixedAmount(fixedAmount);
					invo.setConvenienceAmount(convenienceFee);
					invo.setConveniencePercentage(conveniencePercentage);
					invo.setGst(gst);
					invo.setTotalAmount(totalAmount);
					invo.setTotalAmountInWords(AmountToWords.convertAmountToWords(totalAmount));

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(invo);
					structure.setFlag(1);

				} else {

					structure.setMessage("NO CONVENIENCE FEE IS FETCHED");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(4);
				}
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

	public ResponseStructure postpaidGraceOrForceInvoice(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel entity = userRepository.findByUserId(model.getUserId());

			if (entity != null) {

				String accNo = postpaidUtils.getAccountNumber();
				String hsnNo = postpaidUtils.getHsnNumber();

				PostpaidPayment postpaidPayment = postpaidRepository.getByPaymentFlagAndEntityModel(false, entity);

				String startDate = model.getStartDate().toString() + " 00:00:00.0000000";
				String endDate = model.getEndDate().toString() + " 23:59:59.9999999";

				List<Request> hitLogs = requestRepository.getBetween(entity.getUserId(), startDate, endDate);

				if (!hitLogs.isEmpty()) {

					String invoiceNo = fu.getRandomOTPnumber(8);

					double logPrices = fu.twoDecimelDouble(postpaidUtils.getMonthHitPrice(entity, startDate, endDate));
					double logPriceGst = fu.twoDecimelDouble(logPrices * 18 / 100);

					double totalPrice = fu.twoDecimelDouble(logPrices + logPriceGst);
					int totalHits = hitLogs.size();

					List<Map<String, Object>> usedServices = postpaidUtils.getUsedServices(hitLogs, entity, startDate,
							endDate);

					postpaidPayment.setStartDate(model.getStartDate());
					postpaidPayment.setEndDate(model.getEndDate());
					postpaidPayment.setExclusiveAmount(postpaidPayment.getExclusiveAmount() + logPrices);
					postpaidPayment.setDueAmount(postpaidPayment.getDueAmount() + totalPrice);
					postpaidPayment.setTotalHits(totalHits);
					postpaidPayment.setInvoiceNumber(invoiceNo);
//				    postpaidPayment.setUsedServices(usedServices);

					postpaidRepository.save(postpaidPayment);

					LocalDate graceDate = postpaidPayment.getEndDate()
							.plusDays(postpaidPayment.getEntityModel().getGracePeriod());

					PostpaidReminderInvoice invo = new PostpaidReminderInvoice();

					invo.setPostpaidId(postpaidPayment.getPostpaidId());

					invo.setStartDate(postpaidPayment.getStartDate());
					invo.setEndDate(postpaidPayment.getEndDate());
					invo.setDueDate(graceDate);

					invo.setUserId(entity.getUserId());
					invo.setEntityName(entity.getName());
					invo.setEntityAddress(entity.getAddress());
					invo.setEntityCountry(entity.getCountryName());
					invo.setEntityState(entity.getStateName());
					invo.setEntityCity(entity.getCityName());
					invo.setEntityPincode(entity.getPincode());
					invo.setEntityGst(entity.getGst());
					invo.setEntityContactNumber(entity.getMobileNumber());
					invo.setEntityMail(entity.getEmail());

					invo.setIfscCode(postpaidUtils.getIfscCode());
					invo.setBankName(postpaidUtils.getBankName());
					invo.setAdminBankAccountNo(accNo);
					invo.setAdminHsnCode(hsnNo);
					invo.setInvoiceNumber(invoiceNo);

					invo.setTotalHits(totalHits);
					invo.setUsedAmount(logPrices);
					invo.setUsedAmountGst(logPriceGst);
					invo.setTotalAmount(totalPrice);

					invo.setUsedAmountInWords(AmountToWords.convertAmountToWords(logPrices));
					invo.setUsedAmountGstInWords(AmountToWords.convertAmountToWords(logPriceGst));
					invo.setTotalAmountInWords(AmountToWords.convertAmountToWords(totalPrice));

					invo.setUsedServices(usedServices);

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(invo);
					structure.setFlag(1);

				} else {

					postpaidPayment.setPaymentFlag(false);
					postpaidPayment.setStartDate(entity.getEndDate().plusDays(1));
					postpaidPayment.setEndDate(postpaidUtils.endDateByPaymentCycle(postpaidPayment.getStartDate(),
							entity.getPostpaidPaymentCycle()));

					postpaidRepository.save(postpaidPayment);

					entity.setStartDate(entity.getEndDate().plusDays(1));
					entity.setEndDate(postpaidUtils.endDateByPaymentCycle(entity.getStartDate(),
							entity.getPostpaidPaymentCycle()));
					entity.setPostpaidFlag(false);
					entity.setConsumedAmount(0);
					entity.setPaymentStatus("No dues");
					entity.setGraceDate(entity.getEndDate().plusDays(entity.getGracePeriod()));

					userRepository.save(entity);

					structure.setMessage(
							"NO HIT LOGS FOUND BETWEEN " + model.getStartDate() + " AND " + model.getEndDate());
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

			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	public ResponseStructure uploadPostpaidReminderInvoice(int postpaidId, MultipartFile invoice) {

		ResponseStructure structure = new ResponseStructure();

		try {

			PostpaidPayment postpaid = postpaidRepository.findByPostpaidId(postpaidId);

			if (postpaid != null) {

				postpaid.setInvoice(postpaidUtils.saveReminderInvo(invoice));
				postpaid.setInvoiceGeneratedDate(LocalDate.now());

				String month = LocalDate.now().getMonth().toString();

				EntityModel entity = postpaid.getEntityModel();

				boolean mailSent = emailService.sendMonthlyReminderMail(entity, postpaid, month,
						entity.getGraceDate().toString());

				if (!entity.isPostpaidFlag() && mailSent) {

					entity.setInvoGenerated(true);
					entity.setPostpaidFlag(true);
				}

				userRepository.save(entity);
				postpaidRepository.save(postpaid);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(postpaid);
				structure.setFlag(1);

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

	public ResponseStructure uploadPostpaidConvenienceInvoice(int postpaidId, MultipartFile invoice) {

		ResponseStructure structure = new ResponseStructure();

		try {

			PostpaidPayment postpaid = postpaidRepository.findByPostpaidId(postpaidId);

			if (postpaid != null) {

				postpaid.setConvInvoice(postpaidUtils.saveConveInvo(invoice));
				postpaidRepository.save(postpaid);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(postpaid);
				structure.setFlag(1);

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

	public ResponseStructure uploadPostpaidGraceInvoice(int postpaidId, MultipartFile invoice) {

		ResponseStructure structure = new ResponseStructure();

		try {

			PostpaidPayment postpaid = postpaidRepository.findByPostpaidId(postpaidId);

			if (postpaid != null) {

				postpaid.setGraceInvoice(postpaidUtils.saveReminderInvo(invoice));
				postpaid.setGraceInvoiceGeneratedDate(LocalDate.now());
				postpaidRepository.save(postpaid);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(postpaid);
				structure.setFlag(1);

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

	public ResponseEntity<Resource> viewPostpaidReminderinvoice(int postpaidId, HttpServletRequest request) {

		Optional<PostpaidPayment> invo = postpaidRepository.findById(postpaidId);

		if (invo.isPresent()) {

			if (invo.get().getInvoice() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/PostpaidReminderInvoice/" + invo.get().getInvoice());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
					LOGGER.info("Could not determine file type.");

				}

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

	public ResponseEntity<Resource> viewPostpaidConvenienceInvoice(int postpaidId, HttpServletRequest request) {

		Optional<PostpaidPayment> invo = postpaidRepository.findById(postpaidId);

		if (invo.isPresent()) {

			if (invo.get().getConvInvoice() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/PostpaidConveInvoice/" + invo.get().getConvInvoice());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
					LOGGER.info("Could not determine file type.");

				}

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

	public ResponseEntity<Resource> viewPostpaidGraceInvoice(int postpaidId, HttpServletRequest request) {

		Optional<PostpaidPayment> invo = postpaidRepository.findById(postpaidId);

		if (invo.isPresent()) {

			if (invo.get().getGraceInvoice() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/PostpaidReminderInvoice/" + invo.get().getGraceInvoice());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
					LOGGER.info("Could not determine file type.");

				}

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

}
