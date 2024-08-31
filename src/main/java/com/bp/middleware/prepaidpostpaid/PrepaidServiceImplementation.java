package com.bp.middleware.prepaidpostpaid;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.payment.PaymentMethod;
import com.bp.middleware.prepaidmonthlyinvoice.PrepaidMonthlyInvoice;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.DateUtil;
import com.bp.middleware.util.FileUtils;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PrepaidServiceImplementation implements PrepaidService {

	@Autowired
	private PrepaidRepository prepaidRepository;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	FileUtils fu;

	private static final Logger LOGGER = LoggerFactory.getLogger(PrepaidServiceImplementation.class);

	@Override
	public ResponseStructure addPrepaidDetails(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> optional = userRepository.findById(model.getUserId());
			if (optional.isPresent()) {

				EntityModel entityModel = optional.get();
				PaymentMethod method = entityModel.getPaymentMethod();

				if (method.getPaymentType().equalsIgnoreCase("Prepaid")) {

					PrepaidPayment prepaid = new PrepaidPayment();

					double totalAmount = entityModel.getRemainingAmount() + model.getAmount();

					entityModel.setRemainingAmount(fu.twoDecimelDouble(totalAmount));
					entityModel.setConsumedAmount(0);
					entityModel.setRequestCount(0);
					// entityModel.setResponseCount(0);
					userRepository.save(entityModel);

					prepaid.setEntityModel(entityModel);
					prepaid.setRechargedAmount(model.getAmount());

					LocalDate paidDate = DateUtil.stringToLocalDate(model.getPaymentDate());
					LocalDate updatedDate = DateUtil.stringToLocalDate(model.getUpdatedDate());

					prepaid.setPaidDate(paidDate);
					prepaid.setMonth(LocalDate.now().getMonth().toString());
					prepaid.setUpdatedDate(updatedDate);
					prepaid.setRemark(model.getRemark());
					prepaid.setPaymentMode(model.getPaymentMode());

					if (model.getPaymentMode().equalsIgnoreCase("Cash")) {

						prepaid.setTransactionId("N/A");

					} else {
						String transactionId = FileUtils.getRandomOrderNumer();
						prepaid.setTransactionId(transactionId);
					}

					prepaidRepository.save(prepaid);

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
	public ResponseStructure getPrepaidDetailsById(int prepaidId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<PrepaidPayment> optional = prepaidRepository.findById(prepaidId);
			if (optional.isPresent()) {

				PrepaidPayment prepaidPayment = optional.get();

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(prepaidPayment);
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
	public ResponseStructure getAllPrepaidDetails() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<PrepaidPayment> list = prepaidRepository.findAll();

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
	public ResponseStructure getPrepaidDetailsForUserId(int userId) {
		ResponseStructure structure = new ResponseStructure();

		Optional<EntityModel> optional = userRepository.findById(userId);

		if (optional.isPresent()) {
			EntityModel entityModel = optional.get();

			List<PrepaidPayment> prepaidHistory = prepaidRepository.findByEntityModel(entityModel);

			if (!prepaidHistory.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(prepaidHistory);
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
	public ResponseStructure lastRechargedDetailsForPrepaidUser(int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> optional = userRepository.findById(userId);

			if (optional.isPresent()) {
				EntityModel entity = optional.get();

				List<PrepaidPayment> paymentList = prepaidRepository.findByEntityModel(entity);

				int size = paymentList.size();
				if (size >= 1) {

					PrepaidPayment lastPaymentDetails = paymentList.get(size - 1);

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
	public ResponseEntity<Resource> viewPrepaidReciept(int prepaidId, HttpServletRequest request) {

		Optional<PrepaidPayment> prepaid = prepaidRepository.findById(prepaidId);

		if (prepaid.isPresent()) {
			if (prepaid.get().getReceipt() != null) {

				final Resource resource = resourceLoader.getResource("/WEB-INF/receipt/" + prepaid.get().getReceipt());
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

			List<PrepaidPayment> list = prepaidRepository.findByPaidDate(fromDate, toDate);

			System.err.println("SZ  : " + list.size());

			int count = list.size();

			if (!list.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(
						"Prepaid Details between " + LocalDate.now().minusDays(noOfDays) + " and " + LocalDate.now());
				structure.setData(list);
				structure.setFlag(1);
				structure.setCount(count);

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Prepaid details found between " + LocalDate.now().minusDays(noOfDays) + " and "
						+ LocalDate.now());
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
	public ResponseEntity<Resource> viewPrepaidInvoice(int prepaidId, HttpServletRequest request) {
		
		Optional<PrepaidPayment> prepaid = prepaidRepository.findById(prepaidId);

		if (prepaid.isPresent()) {
			if (prepaid.get().getInvoice() != null) {

				final Resource resource = resourceLoader.getResource("/WEB-INF/invoice/" + prepaid.get().getInvoice());
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
	public ResponseEntity<Resource> viewPrepaidInvoiceTwo(int prepaidId, HttpServletRequest request) {
		Optional<PrepaidPayment> prepaid = prepaidRepository.findById(prepaidId);

		if (prepaid.isPresent()) {
			if (prepaid.get().getInvoice() != null) {

				final Resource resource = resourceLoader.getResource("/WEB-INF/invoice/" + prepaid.get().getInvoiceTwo());
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
	public ResponseEntity<Resource> viewPrepaidInvoiceThree(int prepaidId, HttpServletRequest request) {
		Optional<PrepaidPayment> prepaid = prepaidRepository.findById(prepaidId);

		if (prepaid.isPresent()) {
			if (prepaid.get().getInvoice() != null) {

				final Resource resource = resourceLoader.getResource("/WEB-INF/invoice/" + prepaid.get().getInvoiceThree());
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
	public ResponseStructure viewByMonth(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		Optional<EntityModel> optional = userRepository.findById(model.getUserId());

		if (optional.isPresent()) {
			
			EntityModel entityModel = optional.get();

			List<PrepaidPayment> byMonth = prepaidRepository.findByEntityModelAndMonth(entityModel,model.getMonthName());

			if (!byMonth.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(byMonth);
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
	public ResponseStructure viewByUniqueId(String uniqueId) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<PrepaidPayment> opt = prepaidRepository.findByUniqueId(uniqueId);

			if (opt.isPresent()) {

				PrepaidPayment prep = opt.get();

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(prep);
				structure.setCount(prep.getPrepaidId());
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
