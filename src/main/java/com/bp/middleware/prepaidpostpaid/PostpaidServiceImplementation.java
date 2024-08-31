package com.bp.middleware.prepaidpostpaid;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.admin.AdminServiceImplementation;
import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.payment.PaymentMethod;
import com.bp.middleware.payment.PaymentModel;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.DateUtil;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.GracePeriodInvoiceGenerate;
import com.bp.middleware.util.InvoiceGenerate;
import com.bp.middleware.vendors.VendorVerificationModel;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class PostpaidServiceImplementation implements PostpaidService {

	@Autowired
	private PostpaidRepository postpaidRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RequestRepository requestRepository;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private ServletContext context;
	@Autowired
	EmailService emailService;
	@Autowired
	GracePeriodInvoiceGenerate invoiceGenerate;

	private static final Logger LOGGER = LoggerFactory.getLogger(PostpaidServiceImplementation.class);

	@Override
	public ResponseStructure addPostpaidDetails(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> optional = userRepository.findById(model.getUserId());
			if (optional.isPresent()) {

				EntityModel entityModel = optional.get();
				PaymentMethod method = entityModel.getPaymentMethod();

				if (method.getPaymentType().equalsIgnoreCase("Postpaid")) {

					PostpaidPayment prepaid = new PostpaidPayment();

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setData(prepaid);
					structure.setMessage(AppConstants.SUCCESS);

				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(2);
					structure.setData(null);
					structure.setMessage("YOU ARE NOT A PREPAID CUSTOMER");
				}

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
	public ResponseStructure getPostpaidDetailsById(int postpaidId) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<PostpaidPayment> optional = postpaidRepository.findById(postpaidId);
			if (optional.isPresent()) {

				PostpaidPayment postpaidPayment = optional.get();

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(postpaidPayment);
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
	public ResponseStructure getAllPostpaidDetails() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<PostpaidPayment> list = postpaidRepository.findByPaymentFlag(true);

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
	public ResponseStructure updatePostpaidDetails(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<PostpaidPayment> optional = postpaidRepository.findById(model.getPostpaidId());

			if (optional.isPresent()) {

				PostpaidPayment postpaidPayment = optional.get();

				postpaidPayment.setPaymentMode(model.getPaymentMode());
				postpaidPayment.setPaymentFlag(true);

				LocalDate paidDate = DateUtil.stringToLocalDate(model.getPaymentDate());
				LocalDate updatedDate = DateUtil.stringToLocalDate(model.getUpdatedDate());
				postpaidPayment.setPaidDate(paidDate);
				postpaidPayment.setUpdatedDate(updatedDate);
				postpaidPayment.setRemark(model.getRemark());
				postpaidPayment.setPaidAmount(model.getPaidAmount());

				if (model.getPaymentMode().equalsIgnoreCase("Cash")) {

					postpaidPayment.setTransactionId("N/A");

				} else {
					String transactionId = FileUtils.getRandomOrderNumer();
					postpaidPayment.setTransactionId(transactionId);
				}

				postpaidRepository.save(postpaidPayment);

				EntityModel entity = userRepository.findByUserId(postpaidPayment.getEntityModel().getUserId());

				PostpaidPayment newPostpaidPayment = new PostpaidPayment();

				LocalDate graceDate = entity.getEndDate().plusDays(entity.getGracePeriod());

				newPostpaidPayment.setStartDate(entity.getStartDate());
				newPostpaidPayment.setEndDate(entity.getEndDate());
				newPostpaidPayment.setEntityModel(entity);
				newPostpaidPayment.setPaymentFlag(false);
				newPostpaidPayment.setTotalAmount(0);

				entity.setPostpaidFlag(true);
				entity.setGraceDate(graceDate);

				postpaidRepository.save(newPostpaidPayment);
				userRepository.save(entity);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(postpaidPayment);
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
	public ResponseStructure totalHitAndCount(int userId) throws ParseException {
		ResponseStructure structure = new ResponseStructure();

		Optional<EntityModel> optional = userRepository.findById(userId);
		if (optional.isPresent()) {
			EntityModel entity = optional.get();

			List<PostpaidPayment> list = postpaidRepository.findByEntityModel(entity);
			PostpaidPayment falseFlag = new PostpaidPayment();

			for (PostpaidPayment postpaidPayment : list) {
				if (!postpaidPayment.isPaymentFlag()) {
					falseFlag = postpaidPayment;
				}
			}
			double price = 0;
			int count = 0;

			List<Request> reqList = requestRepository.findByUser(entity);
			List<Request> time = new ArrayList<>();

			for (Request request : reqList) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				String startDate = new StringBuilder().append(falseFlag.getStartDate().format(formatter))
						.append(" 00:00:00").toString();
				String endDate = new StringBuilder().append(falseFlag.getEndDate().format(formatter))
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
			int n = 0;
			for (Request req : time) {
				if (req.getUser().equals(entity)) {
					price = price + req.getPrice();
					count = count + 1;
				}
			}
			Map<String, Object> map = new LinkedHashMap<>();
			map.put("Count", count);
			map.put("Price", price);

			structure.setData(map);
			structure.setFlag(1);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure getPostpaidDetailsForUserId(int userId) {

		ResponseStructure structure = new ResponseStructure();

		Optional<EntityModel> optional = userRepository.findById(userId);

		if (optional.isPresent()) {
			EntityModel entityModel = optional.get();

			List<PostpaidPayment> postpaidHistory = postpaidRepository.findByPaymentFlagAndEntityModel(true,
					entityModel);

			if (!postpaidHistory.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(postpaidHistory);
				structure.setMessage(AppConstants.SUCCESS);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(4);
			structure.setData(null);
			structure.setMessage(AppConstants.NO_DATA_FOUND);
		}

		return structure;

	}

	@Override
	public ResponseStructure lastPayDetailsForPostpaidUser(int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> optional = userRepository.findById(userId);

			if (optional.isPresent()) {
				EntityModel entity = optional.get();

				List<PostpaidPayment> paymentList = postpaidRepository.findByEntityModel(entity);

				int size = paymentList.size();
				if (size >= 2) {

					PostpaidPayment lastPaymentDetails = paymentList.get(size - 2);

					structure.setData(lastPaymentDetails);
					structure.setFlag(1);
					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
				}
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
	public ResponseStructure getPostpaidDetailsOfUserAfterGeneratingInvoice(int userId) {

		ResponseStructure structure = new ResponseStructure();

		Optional<EntityModel> optional = userRepository.findById(userId);

		if (optional.isPresent()) {
			EntityModel entityModel = optional.get();

			List<PostpaidPayment> postpaidHistory = postpaidRepository.findByEntityModel(entityModel);
			PostpaidPayment invoicePostpaid = new PostpaidPayment();

			if (!postpaidHistory.isEmpty()) {

				for (PostpaidPayment postpaidPayment : postpaidHistory) {
					if (!postpaidPayment.isPaymentFlag()) {
						invoicePostpaid = postpaidPayment;
					}
				}

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(invoicePostpaid);
				structure.setMessage(AppConstants.SUCCESS);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(4);
			structure.setData(null);
			structure.setMessage(AppConstants.NO_DATA_FOUND);
		}

		return structure;

	}

	@Override
	public ResponseEntity<Resource> viewPostpaidReciept(int postpaidId, HttpServletRequest request) {

		Optional<PostpaidPayment> optional = postpaidRepository.findById(postpaidId);

		if (optional.isPresent()) {
			if (optional.get().getReceipt() != null) {

				final Resource resource = resourceLoader.getResource("/WEB-INF/receipt/" + optional.get().getReceipt());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
					LOGGER.info("Could not determine file type.");

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

	@Override
	public ResponseEntity<Resource> viewPostpaidInvoice(int postpaidId, HttpServletRequest request) {
		Optional<PostpaidPayment> optional = postpaidRepository.findById(postpaidId);

		if (optional.isPresent()) {
			if (optional.get().getInvoice() != null) {

				final Resource resource = resourceLoader.getResource("/WEB-INF/invoice/" + optional.get().getInvoice());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
					LOGGER.info("Could not determine file type.");

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

	@Override
	public ResponseStructure viewByNumberOfDays(int noOfDays) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String toDate = LocalDate.now().toString();
			String fromDate = LocalDate.now().minusDays(noOfDays).toString();

			List<PostpaidPayment> list = postpaidRepository.findByPaidDate(fromDate, toDate);

			System.err.println("SZ  : " + list.size());

			int count = list.size();

			if (!list.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(
						"Postpaid Details between " + LocalDate.now().minusDays(noOfDays) + " and " + LocalDate.now());
				structure.setData(list);
				structure.setFlag(1);
				structure.setCount(count);

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Postpaid details found between " + LocalDate.now().minusDays(noOfDays)
						+ " and " + LocalDate.now());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(count);

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
	public ResponseEntity<Resource> viewPostpaidInvoiceOne(int postpaidId, HttpServletRequest request) {
		Optional<PostpaidPayment> optional = postpaidRepository.findById(postpaidId);

		if (optional.isPresent()) {
			if (optional.get().getInvoice() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/invoice/" + optional.get().getInvoiceTwo());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
					LOGGER.info("Could not determine file type.");

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

	@Override
	public ResponseEntity<Resource> viewPostpaidInvoiceTwo(int postpaidId, HttpServletRequest request) {
		Optional<PostpaidPayment> optional = postpaidRepository.findById(postpaidId);

		if (optional.isPresent()) {
			if (optional.get().getInvoice() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/invoice/" + optional.get().getInvoiceThree());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
					LOGGER.info("Could not determine file type.");

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

	@Override
	public ResponseStructure generateGraceInvoice(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<PostpaidPayment> opt = postpaidRepository.findById(model.getPostpaidId());

			if (opt.isPresent()) {

				PostpaidPayment postpaidPayment = opt.get();

				generateGraceInvoice(postpaidPayment, model);

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(4);
			}

		} catch (Exception e) {
			
			LOGGER.info(e.getMessage());
			
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	private ResponseStructure generateGraceInvoice(PostpaidPayment postpaidPayment, RequestModel model) throws Exception {

		ResponseStructure structure = new ResponseStructure();
		
		Path con = Paths.get(context.getRealPath("/WEB-INF/"));

		EntityModel entity = postpaidPayment.getEntityModel();

		String startDate = postpaidPayment.getEndDate().plusDays(1).toString() + " 00:00:00.0000000";
		String endDate = postpaidPayment.getEndDate().plusDays(entity.getGracePeriod()) + " 23:59:59.9999999";

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

//		postpaidPayment.setUsedServices(usedServices);

		if (price > 0 && postpaidPayment.isPaymentFlag() && !entity.isPostpaidFlag()) {

			System.err.println("ENTITY ID : " + entity.getUserId() + " with price " + price + " and hit " + totalHit
					+ " also the postpaid id is " + postpaidPayment.getPostpaidId());

			model.setFromDate(startDate);
			model.setToDate(endDate);
			model.setTotalHit(totalHit);
			model.setIdPrice(price);

			String invoiceNumber = invoiceGenerate.graceInvoiceGenerateRouter(con, null, entity, postpaidPayment,
					model);

			String month = LocalDate.now().minusMonths(1).getMonth().toString();

			boolean mailSent = emailService.sendGraceInvoice(entity, postpaidPayment, month, "",model);

			System.err.println("MAIL SENT : " + mailSent);

			if (!entity.isPostpaidFlag() && mailSent && entity.isInvoGenerated()) {

				entity.setPostpaidFlag(true);
				userRepository.save(entity);
				
				
				
				postpaidRepository.save(postpaidPayment);
				
				PostpaidPayment postpaidHistory = postpaidRepository.findByEntityModelAndPaymentFlag(entity, false);
				
				postpaidHistory.setExclusiveAmount(price);
				postpaidHistory.setDueAmount(postpaidHistory.getDueAmount() + price);
				postpaidHistory.setStartDate(postpaidPayment.getEndDate().plusDays(1));
				postpaidHistory.setEndDate(postpaidPayment.getEndDate().plusDays(entity.getGracePeriod()));
				postpaidHistory.setInvoice(postpaidPayment.getGraceInvoice());
				postpaidHistory.setInvoiceGeneratedDate(LocalDate.now());
//				postpaidHistory.setUsedServices(usedServices);
				postpaidHistory.setEntityModel(entity);
				postpaidHistory.setGraceInvoPayment(true);
				
				postpaidRepository.save(postpaidHistory);
				
				Map<String, PostpaidPayment> map = new LinkedHashMap<>();
				map.put("old_postpaid", postpaidPayment);
				map.put("new_postpaid", postpaidHistory);
				
				structure.setFlag(1);
				structure.setData(map);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				
			}else {
				
				structure.setFlag(3);
				structure.setData(null);
				
				if(!mailSent) {
					structure.setMessage("Invoice generated but mail , didn't send");
				}else {
					structure.setMessage("Unable to proceed , since postpaid payment is pending");
				}
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} else {

			structure.setFlag(2);
			structure.setData(null);
			
			if(!postpaidPayment.isPaymentFlag() || !entity.isPostpaidFlag()) {
				structure.setMessage("Unable to proceed , since postpaid payment is pending");
			}else {
				structure.setMessage("This entity doesn't need to pay any amount.");
			}
			structure.setStatusCode(HttpStatus.OK.value());
		}
		
		return structure;
	}

	private double calculateGstEighteenPercent(double exclusiveAmount) throws Exception {

		return exclusiveAmount * 18 / 100;

	}

	@Override
	public ResponseEntity<Resource> viewConveInvoice(int postpaidId, HttpServletRequest request) {
		Optional<PostpaidPayment> optional = postpaidRepository.findById(postpaidId);

		if (optional.isPresent()) {
			if (optional.get().getInvoice() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/conveInvoice/" + optional.get().getConvInvoice());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
					LOGGER.info("Could not determine file type.");

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

	@Override
	public ResponseEntity<Resource> viewGraceInvoice(int postpaidId, HttpServletRequest request) {
		Optional<PostpaidPayment> optional = postpaidRepository.findById(postpaidId);

		if (optional.isPresent()) {
			if (optional.get().getInvoice() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/graceInvoice/" + optional.get().getGraceInvoice());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
					LOGGER.info("Could not determine file type.");

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

	@Override
	public ResponseStructure forceInvoiceGeneration(RequestModel model) {

		return new ResponseStructure();
//		try {
//
//			System.err.println("A");
//			
//			List<PostpaidPayment> flagFalseUser = postpaidRepository.findByPaymentFlag(false);
//
//			Path con = Paths.get(context.getRealPath("/WEB-INF/"));
//
//			for (PostpaidPayment postpaidPayment : flagFalseUser) {
//
//				System.err.println("B");
//				
//				if (LocalDate.now().isAfter(postpaidPayment.getEndDate())
//						&& postpaidPayment.getEntityModel().isAccountStatus()) {
//
//					System.err.println("C");
//					
//					EntityModel entity = postpaidPayment.getEntityModel();
//
//					String startDate = postpaidPayment.getStartDate().toString() + " 00:00:00.0000000";
//					String endDate = postpaidPayment.getEndDate().toString() + " 23:59:59.9999999";
//
//					List<Request> time = requestRepository.getBetween(entity.getUserId(), startDate, endDate);
//
//					String stringPrice = requestRepository.getSummedAmount(entity.getUserId(), startDate, endDate);
//
//					if (stringPrice == null) {
//						stringPrice = "0";
//					}
//
//					double price = Double.parseDouble(stringPrice);
//
//					int totalHit = time.size();
//
//					Set<VendorVerificationModel> set = new HashSet<>();
//
//					for (Request path : time) {
//
//						set.add(path.getVerificationModel());
//					}
//
//					String usedServices = "";
//
//					for (VendorVerificationModel vendorVerificationModel : set) {
//
//						if (usedServices.equalsIgnoreCase("")) {
//
//							usedServices += vendorVerificationModel.getVerificationDocument();
//						} else {
//
//							usedServices += "," + vendorVerificationModel.getVerificationDocument();
//						}
//					}
//
//					postpaidPayment.setUsedServices(usedServices);
//
//					System.err.println("PRICE & ID "+price+"   "+entity.getUserId());
//					
//					if (price > 0 && !entity.isInvoGenerated()) {
//
//						System.err.println("D");
//						
//						System.err.println("ENTITY ID : " + entity.getUserId() + " with price " + price + " and hit "
//								+ totalHit + " also the postpaid id is " + postpaidPayment.getPostpaidId());
//
//						postpaidPayment.setExclusiveAmount(postpaidPayment.getExclusiveAmount() + price);
//						postpaidPayment.setDueAmount(postpaidPayment.getDueAmount() + price);
//						postpaidPayment.setTotalHits(totalHit);
//
//						String invoiceNumber = invoiceGenerate.invoiceGenerateRouter(con, null, entity, postpaidPayment,
//								null);
//
//						postpaidPayment.setInvoiceNumber(invoiceNumber);
//						
//						String month = LocalDate.now().minusMonths(1).getMonth().toString();
//						String graceDate = postpaidPayment.getEndDate()
//								.plusDays(postpaidPayment.getEntityModel().getGracePeriod()).toString();
//
//						boolean mailSent = emailService.sendMonthlyReminderMail(entity, postpaidPayment, month,
//								graceDate);
//
//						emailService.sendEmailAdminOTPVerification("abhishek.p@basispay.in", "Postpaid invoce generated at month end",
//								"Time : "+new Date(), "to -> "+entity.getEmail(), "www.google.com");
//						
//						System.err.println("MAIL SENT : " + mailSent);
//
//						if (!entity.isPostpaidFlag() && mailSent) {
//
//							System.err.println("E");
//							
//							entity.setInvoGenerated(true);
//							entity.setPostpaidFlag(true);
//						}
//
//						userRepository.save(entity);
//
//					} else {
//
//						System.err.println("F");
//						
//						if (LocalDate.now().isAfter(postpaidPayment.getEntityModel().getGraceDate())) {
//
//							System.err.println("G");
//							
//							postpaidPayment.setPaymentFlag(true);
//							postpaidPayment.setRemark("No verification done");
//							postpaidPayment.setPaidAmount(0);
//							postpaidPayment.setDueAmount(0);
//							postpaidPayment.setTransactionId("");
//
//							postpaidRepository.save(postpaidPayment);
//
//							PostpaidPayment newPostpaidPayment = new PostpaidPayment();
//
//							newPostpaidPayment.setStartDate(entity.getEndDate().plusDays(1));
//							newPostpaidPayment.setEndDate(endDateByPaymentCycle(newPostpaidPayment.getStartDate(),
//									entity.getPostpaidPaymentCycle()));
//							newPostpaidPayment.setEntityModel(entity);
//							newPostpaidPayment.setPaymentFlag(false);
//							newPostpaidPayment.setTotalAmount(0);
//
//							LocalDate graceDate = newPostpaidPayment.getEndDate().plusDays(entity.getGracePeriod());
//
//							entity.setStartDate(entity.getEndDate().plusDays(1));
//							entity.setEndDate(
//									endDateByPaymentCycle(entity.getStartDate(), entity.getPostpaidPaymentCycle()));
//							entity.setPostpaidFlag(false);
//							entity.setConsumedAmount(0);
//							entity.setPaymentStatus("No dues");
//							entity.setGraceDate(graceDate);
//							entity.setPaymentStatus("");
//							
//							postpaidRepository.save(newPostpaidPayment);
//							userRepository.save(entity);
//						}
//					}
//				}
//			}
//
//			System.err.println("H");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	
	
	
	@Override
	public ResponseStructure viewByUniqueId(String uniqueId) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<PostpaidPayment> opt = postpaidRepository.findByUniqueId(uniqueId);

			if (opt.isPresent()) {

				PostpaidPayment post = opt.get();

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(post);
				structure.setCount(post.getPostpaidId());
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

}
