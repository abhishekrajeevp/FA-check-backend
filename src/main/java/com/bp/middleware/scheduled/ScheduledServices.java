package com.bp.middleware.scheduled;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.signers.SignerModel;
import com.bp.middleware.signers.SignerRepository;
import com.bp.middleware.signmerchant.MerchantModel;
import com.bp.middleware.signmerchant.MerchantRepository;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.transaction.TransactionRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.DateUtil;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.InvoiceGenerate;
import com.bp.middleware.vendors.VendorVerificationModel;

import jakarta.servlet.ServletContext;

@Component
public class ScheduledServices {

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

	@Scheduled(fixedRate = 86400000) // ONEDAY
	public void deleteFieldsSixMonthsFromResponse() {

		LocalDate currentDate = LocalDate.now();
		List<Response> responseList = responseRepository.getNotDeletedStatus();

		for (Response response : responseList) {
			LocalDate localDateConverter = DateUtil.convertDateToLocalDateViaSql(response.getRequestDateAndTime());

			long monthsBetween = ChronoUnit.MONTHS.between(localDateConverter, currentDate);

			if (monthsBetween > 3) {
				response.setStatus("expired");
				responseRepository.save(response);
			}
		}
	}

	
//	@Scheduled(fixedRate = 3000000)
	public void sendPostPaidInvoice() {

		try {

			System.err.println("A");
			
			List<PostpaidPayment> flagFalseUser = postpaidRepository.findByPaymentFlag(false);

			Path con = Paths.get(context.getRealPath("/WEB-INF/"));

			for (PostpaidPayment postpaidPayment : flagFalseUser) {

				System.err.println("B");
				
				if (LocalDate.now().isAfter(postpaidPayment.getEndDate())
						&& postpaidPayment.getEntityModel().isAccountStatus()) {

					System.err.println("C");
					
					EntityModel entity = postpaidPayment.getEntityModel();

					String startDate = postpaidPayment.getStartDate().toString() + " 00:00:00.0000000";
					String endDate = postpaidPayment.getEndDate().toString() + " 23:59:59.9999999";

					List<Request> time = requestRepository.getBetween(entity.getUserId(), startDate, endDate);

					String stringPrice = requestRepository.getSummedAmount(entity.getUserId(), startDate, endDate);

					if (stringPrice == null) {
						stringPrice = "0";
					}

					double price = Double.parseDouble(stringPrice);

					int totalHit = time.size();

					Set<VendorVerificationModel> set = new HashSet<>();

					for (Request path : time) {

						set.add(path.getVerificationModel());
					}

					String usedServices = "";

					for (VendorVerificationModel vendorVerificationModel : set) {

						if (usedServices.equalsIgnoreCase("")) {

							usedServices += vendorVerificationModel.getVerificationDocument();
						} else {

							usedServices += "," + vendorVerificationModel.getVerificationDocument();
						}
					}

//					postpaidPayment.setUsedServices(usedServices);

					System.err.println("PRICE & ID "+price+"   "+entity.getUserId());
					
					if (price > 0 && !entity.isInvoGenerated()) {

						System.err.println("D");
						
						System.err.println("ENTITY ID : " + entity.getUserId() + " with price " + price + " and hit "
								+ totalHit + " also the postpaid id is " + postpaidPayment.getPostpaidId());

						postpaidPayment.setExclusiveAmount(postpaidPayment.getExclusiveAmount() + price);
						postpaidPayment.setDueAmount(postpaidPayment.getDueAmount() + price);
						postpaidPayment.setTotalHits(totalHit);

						String invoiceNumber = invoiceGenerate.invoiceGenerateRouter(con, null, entity, postpaidPayment,
								null);

						postpaidPayment.setInvoiceNumber(invoiceNumber);
						
						String month = LocalDate.now().minusMonths(1).getMonth().toString();
						String graceDate = postpaidPayment.getEndDate()
								.plusDays(postpaidPayment.getEntityModel().getGracePeriod()).toString();

						boolean mailSent = emailService.sendMonthlyReminderMail(entity, postpaidPayment, month,
								graceDate);

						emailService.sendEmailAdminOTPVerification("abhishek.p@basispay.in", "Postpaid invoce generated at month end",
								"Time : "+new Date(), "to -> "+entity.getEmail(), "www.google.com");
						
						System.err.println("MAIL SENT : " + mailSent);

						if (!entity.isPostpaidFlag() && mailSent) {

							System.err.println("E");
							
							entity.setInvoGenerated(true);
							entity.setPostpaidFlag(true);
						}

						userRepository.save(entity);

					} else {

						System.err.println("F");
						
						if (LocalDate.now().isAfter(postpaidPayment.getEntityModel().getGraceDate())) {

							System.err.println("G");
							
							postpaidPayment.setPaymentFlag(true);
							postpaidPayment.setRemark("No verification done");
							postpaidPayment.setPaidAmount(0);
							postpaidPayment.setDueAmount(0);
							postpaidPayment.setTransactionId("");

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
							entity.setConsumedAmount(0);
							entity.setPaymentStatus("No dues");
							entity.setGraceDate(graceDate);
							entity.setPaymentStatus("");
							
							postpaidRepository.save(newPostpaidPayment);
							userRepository.save(entity);
						}
					}
				}
			}

			System.err.println("H");
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	
	

//	@Scheduled(cron = "0 55 23 L * ?")
	public void prepaidInvoiceAtMonthEnd() throws Exception {

		try {

			String month = LocalDate.now().getMonth().toString();

			List<PrepaidPayment> currentMonthList = prepaidRepository.findByRemarkAndMonth("Success", month);

			Path con = Paths.get(context.getRealPath("/WEB-INF/"));

			if (!currentMonthList.isEmpty()) {

				LocalDate now = LocalDate.now();
				Set<EntityModel> mailed = new HashSet<>();

				for (PrepaidPayment prepaidPayment : currentMonthList) {

					if (prepaidPayment.getEntityModel().isAccountStatus()
							&& now.getMonth() == prepaidPayment.getPaidDate().getMonth()) {

						if (!mailed.contains(prepaidPayment.getEntityModel())) {

							RequestModel model = getHitsForThisMonth(prepaidPayment, now);

							PrepaidPayment prepaid = model.getPrepaid();
							Set<VendorVerificationModel> set = model.getVerificationModelSet();

							String usedServices = "";

							for (VendorVerificationModel vendorVerificationModel : set) {

								if (usedServices.equalsIgnoreCase("")) {

									usedServices += vendorVerificationModel.getVerificationDocument();
								} else {

									usedServices += "," + vendorVerificationModel.getVerificationDocument();
								}
							}

							prepaid.setUsedServices(usedServices);

							if (prepaid.getUsedAmount() > 0) {//

								TransactionDto trans = transactionRepository.findByPrepaidId(prepaid.getPrepaidId());

								invoiceGenerate.invoiceGenerateRouter(con, trans, prepaid.getEntityModel(), null,
										prepaid);

//								boolean sent = emailService.prepaidInvoiceGenerateMail(entity.getEmail(), monthlyInvo);
//
//								boolean sent = emailService.prepaidInvoiceGenerateMail("a", monthlyInvo);
								
//								System.err.println("--------------------Mail sent to "
//										+ prepaid.getEntityModel().getEmail() + " " + sent);

//								if (sent) {
//									mailed.add(prepaidPayment.getEntityModel());
//								}

								setInvoiceToOtherEntries(prepaid, month);
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	
	private void setInvoiceToOtherEntries(PrepaidPayment prepaid, String month) throws Exception {

		int userId = prepaid.getEntityModel().getUserId();
		int prepaidId = prepaid.getPrepaidId();

		List<PrepaidPayment> otherEntriesThisMonth = prepaidRepository.getByPrepaidIdAndMonth("Success", month, userId,
				prepaidId);

		if (!otherEntriesThisMonth.isEmpty()) {

			for (PrepaidPayment prepaidPayment : otherEntriesThisMonth) {

				prepaidPayment.setTotalHits(prepaid.getTotalHits());
				prepaidPayment.setUsedAmount(prepaid.getUsedAmount());
				prepaidPayment.setWalletBalance(prepaid.getWalletBalance());
				prepaidPayment.setInvoice(prepaid.getInvoice());
				prepaidPayment.setInvoiceGeneratedDate(prepaid.getInvoiceGeneratedDate());

				prepaidRepository.save(prepaidPayment);
			}
		}
	}

	
	
	private RequestModel getHitsForThisMonth(PrepaidPayment prepaidPayment, LocalDate now) throws Exception {

		EntityModel entity = prepaidPayment.getEntityModel();

		String startDate = now.minusDays(now.getDayOfMonth() - 1).toString() + " 00:00:00.0000000";
		String endDate = now.toString() + " 23:59:59.9999999";

		List<Request> hitLogs = requestRepository.getBetween(entity.getUserId(), startDate, endDate);

		System.err.println("START DATE : " + startDate);
		System.err.println("END DATE : " + endDate);
		System.err.println("entity ID : " + entity.getUserId());

		String stringPrice = requestRepository.getSummedAmount(entity.getUserId(), startDate, endDate);

		if (stringPrice == null) {
			stringPrice = "0";
		}

		double price = Math.ceil(Double.parseDouble(stringPrice));

		System.err.println("Price : " + price);

		int hits = hitLogs.size();

		prepaidPayment.setTotalHits(hits);
		prepaidPayment.setUsedAmount(price);
		prepaidPayment.setWalletBalance(entity.getRemainingAmount());

		Set<VendorVerificationModel> set = new HashSet<>();

		for (Request request : hitLogs) {

			set.add(request.getVerificationModel());
		}

		RequestModel model = new RequestModel();

		model.setPrepaid(prepaidPayment);
		model.setVerificationModelSet(set);

		return model;
	}

	
	
	@Scheduled(cron = "0 01 00 ? * *") // RUNS AT 12:01 AM EVERY DAY --->0=SECONDS 01 = MINUTE 00 = HOUR OF THE DAY & *
										// * * = DAY,DAY OF MONTH,DAY OF WEEK
	public void agreementExpiring() {
		
		
		try {

			List<MerchantModel> merchantList = merchantRepository.findByExpired(false);

			for (MerchantModel merchantModel : merchantList) {

				boolean expireDateCheck = merchantModel.getDocumentExpiryAt().isBefore(LocalDate.now());

				if (expireDateCheck) {

					merchantModel.setExpired(true);

					List<SignerModel> signerList = signerRepository.findByMerchantModel(merchantModel);

					for (SignerModel signerModel : signerList) {
						signerModel.setExpired(true);
						signerRepository.save(signerModel);
					}

					merchantRepository.save(merchantModel);
				}
			}
		
			
		} catch (Exception e) {
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// @Scheduled(fixedRate = 3000000) // 10 minutes in milliseconds
		public void generateInvoiceAtMonthEnd() {

			try {

				List<PostpaidPayment> all = postpaidRepository.findAll();
				List<PostpaidPayment> flagFalseUser = new ArrayList<>();

				for (PostpaidPayment postpaidPayment : all) {
					if (!postpaidPayment.isPaymentFlag()) {
						flagFalseUser.add(postpaidPayment);
					}
				}

				for (PostpaidPayment postpaidPayment : flagFalseUser) {

					if (LocalDate.now().isAfter(postpaidPayment.getEndDate())) {

						EntityModel entity = userRepository.findByUserId(postpaidPayment.getEntityModel().getUserId());

						try {

							List<Request> reqList = requestRepository.findByUserAndFreeHit(entity, false);
							List<Request> time = new ArrayList<>();

							for (Request request : reqList) {
								DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
								String startDate = new StringBuilder()
										.append(postpaidPayment.getStartDate().format(formatter)).append(" 00:00:00")
										.toString();
								String endDate = new StringBuilder().append(postpaidPayment.getEndDate().format(formatter))
										.append(" 00:00:00").toString();

								DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
								DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

								LocalDateTime dateTime1 = LocalDateTime.parse(startDate, inputFormatter);
								LocalDateTime dateTime2 = LocalDateTime.parse(endDate, inputFormatter);
								String outputDate1 = dateTime1.format(outputFormatter);
								String outputDate2 = dateTime2.format(outputFormatter);

								Date date1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(outputDate1);
								Date date2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(outputDate2);

								time = requestRepository.findByRequestDateAndTime(date1, date2);
							}
							System.out.println(" Time : " + time.size());
							// int n=0;
							double price = 0;
							int totalHit = 0;
							System.out.println("Entity Id : " + entity.getUserId());
							for (Request req : time) {
								System.out.println("Request User :" + req.getUser().getUserId());

								System.out.println("Looop In");
								if (req.getUser().getUserId() == entity.getUserId()) {
									System.out.println("Req Price : " + req.getPrice());
									price = price + req.getPrice();
									totalHit = totalHit + 1;
								}
							}

							LocalDate extendDate = entity.getEndDate().plusDays(entity.getDuration());
							// LocalDate extendedGraceDate=extendDate.plusDays(entity.getGracePeriod());
							System.out.println("Price : " + price);
							postpaidPayment.setExclusiveAmount(postpaidPayment.getExclusiveAmount() + price);
							postpaidPayment.setDueAmount(postpaidPayment.getDueAmount() + price);

							Path con = Paths.get(context.getRealPath("/WEB-INF/"));
							// String invoice = InvoiceGenerate.postpaidInvoiceCgst(con, entity,
							// postpaidPayment, price,totalHit);

							postpaidPayment.setInvoice("");

							postpaidRepository.save(postpaidPayment);

							// ,String path,String name,String month
							String email = postpaidPayment.getEntityModel().getEmail();
							String fileName = con + "/invoice/" + "";
							String name = postpaidPayment.getEntityModel().getName();
							String month = LocalDate.now().minusMonths(1).getMonth().toString();
							String graceDate = postpaidPayment.getEndDate()
									.plusDays(postpaidPayment.getEntityModel().getGracePeriod()).toString();

							System.err.println("USER ID : " + postpaidPayment.getEntityModel().getUserId());

							// emailService.sendMonthlyReminderMail(email,fileName,name,month,graceDate);

							if (entity.isPostpaidFlag()) {
								LocalDate startDate = entity.getEndDate().plusDays(1);
								entity.setStartDate(startDate);
								entity.setEndDate(extendDate);
								// entity.setGraceDate(extendedGraceDate);
								entity.setPostpaidFlag(false);
							}

							userRepository.save(entity);

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}


}
