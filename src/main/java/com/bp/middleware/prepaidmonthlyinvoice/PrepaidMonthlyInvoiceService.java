package com.bp.middleware.prepaidmonthlyinvoice;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
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
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.prepaidinvoicesdetails.PrepaidUtils;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.transaction.TransactionRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.DateUtil;
import com.bp.middleware.util.FileUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class PrepaidMonthlyInvoiceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrepaidMonthlyInvoiceService.class);

	@Autowired
	private PrepaidMonthlyInvoiceRepository prepaidMonthlyInvoiceRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ServletContext context;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private PrepaidUtils prepaidUtils;
	@Autowired
	private PrepaidRepository prepaidRepository;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	EmailService emailService;

	public ResponseStructure uploadMonthlyInvoice(int entityId, LocalDate fromDate,
			LocalDate toDate) {

		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel entity = userRepository.findByUserId(entityId);

			if (entity != null) {

				List<PrepaidMonthlyInvoice> list = prepaidMonthlyInvoiceRepository.findByEntity(entity);

				boolean dateOverlapped = false;

				LocalDate existingFromDate = LocalDate.now();
				LocalDate existingToDate = LocalDate.now();

				for (PrepaidMonthlyInvoice prepaidMonthlyInvoice : list) {

					if (!dateOverlapped) {

						existingFromDate = prepaidMonthlyInvoice.getFromDate();
						existingToDate = prepaidMonthlyInvoice.getToDate();

						dateOverlapped = DateUtil.localDateOverLapDetector(fromDate, toDate, existingFromDate,
								existingToDate);

					} else {
						break;
					}
				}

				if (list.isEmpty() || !dateOverlapped) {

					PrepaidMonthlyInvoice monthlyInvo = new PrepaidMonthlyInvoice();

//					monthlyInvo.setInvoice(prepaidUtils.saveMonthlyInvo(invoice));
					monthlyInvo.setFromDate(fromDate);
					monthlyInvo.setToDate(toDate);
					monthlyInvo.setDateTime(new Date());
					monthlyInvo.setMonth(toDate.getMonth().toString());
					monthlyInvo.setYear(toDate.getYear());
					monthlyInvo.setUniqueId(FileUtils.generateApiKeys(8));
					monthlyInvo.setEntity(entity);

//					boolean sent = emailService.prepaidInvoiceGenerateMail(entity.getEmail(), monthlyInvo);

					monthlyInvo.setMailSent(true);

					prepaidMonthlyInvoiceRepository.save(monthlyInvo);

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(monthlyInvo);
					structure.setFlag(1);

				} else {

					structure.setMessage("Date Overlapped! An invoice already exist between " + existingFromDate
							+ " and " + existingToDate);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
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

	public ResponseEntity<Resource> viewMonthlyInvoice(int monthlyInvoiceId, HttpServletRequest request) {

		Optional<PrepaidMonthlyInvoice> invo = prepaidMonthlyInvoiceRepository.findById(monthlyInvoiceId);

		if (invo.isPresent()) {

			if (invo.get().getInvoice() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/PrepaidMonthlyInvoice/" + invo.get().getInvoice());
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

	public ResponseStructure getMonthlyInvoByEntity(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel entity = userRepository.findByUserId(model.getUserId());

			if (entity != null) {

				List<PrepaidMonthlyInvoice> monthlyInvo = prepaidMonthlyInvoiceRepository.findByEntity(entity);

				if (!monthlyInvo.isEmpty()) {

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(monthlyInvo);
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

	public ResponseStructure uploadPrepaidConvenienceInvoice(int prepaidId, MultipartFile invoice) {

		ResponseStructure structure = new ResponseStructure();

		try {

			PrepaidPayment prepaid = prepaidRepository.findByPrepaidId(prepaidId);

			if (prepaid != null) {

				prepaid.setInvoice(prepaidUtils.saveConveInvo(invoice));
				prepaid.setConveInvoGeneratedDate(LocalDate.now());
				prepaidRepository.save(prepaid);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(prepaid);
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

	public ResponseEntity<Resource> viewPrepaidConvenienceInvoice(int prepaidId, HttpServletRequest request) {

		Optional<PrepaidPayment> invo = prepaidRepository.findById(prepaidId);

		if (invo.isPresent()) {

			if (invo.get().getInvoice() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/PrepaidConveInvoice/" + invo.get().getInvoice());
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

	public ResponseStructure updatePrepaidMonthlyInvo(int monthlyInvoId, MultipartFile invoice) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<PrepaidMonthlyInvoice> opt = prepaidMonthlyInvoiceRepository.findById(monthlyInvoId);

			if (opt.isPresent()) {

				PrepaidMonthlyInvoice invo = opt.get();

				invo.setInvoice(prepaidUtils.saveMonthlyInvo(invoice));

				prepaidMonthlyInvoiceRepository.save(invo);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(invo);
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

	
	
	
	public ResponseStructure viewByUniqueId(String uniqueId) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<PrepaidMonthlyInvoice> opt = prepaidMonthlyInvoiceRepository.findByUniqueId(uniqueId);

			if (opt.isPresent()) {

				PrepaidMonthlyInvoice invo = opt.get();

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(invo);
				structure.setCount(invo.getMonthlyInvoiceId());
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
