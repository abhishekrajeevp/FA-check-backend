package com.bp.middleware.receiptdetails;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.StringTokenizer;

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
import com.bp.middleware.payment.PaymentRepository;
import com.bp.middleware.prepaidinvoicesdetails.PrepaidUtils;
import com.bp.middleware.prepaidmonthlyinvoice.PrepaidMonthlyInvoiceService;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.signers.SignerRepository;
import com.bp.middleware.signmerchant.MerchantRepository;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.transaction.TransactionRepository;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AmountToWords;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.InvoiceGenerate;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class ReceiptService {

	
	private static final Logger LOGGER=LoggerFactory.getLogger(ReceiptService.class);
	
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
	private PrepaidUtils prepaidUtils;
	@Autowired
	private ResourceLoader resourceLoader;

	
	
	
	public ResponseStructure receipt(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {
			
			
			if(model.getPrepaidId()>0) {
				
				PrepaidPayment prepaid = prepaidRepository.findByPrepaidId(model.getPrepaidId());
				
				if(prepaid != null && prepaid.getRemark().equalsIgnoreCase("Success")) {
					
					TransactionDto transaction = transactionRepository.findByPrepaidId(model.getPrepaidId());
					
					return prepaidReceipt(transaction,prepaid);
					
				}else {
					
					if(prepaid==null) {
						structure.setMessage("Prepaid details not found.");
					}else {
						structure.setMessage("Prepaid payment status should be Success");
					}
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
				}
				
			}else {
				
				PostpaidPayment postpaid = postpaidRepository.findByPostpaidId(model.getPostpaidId());
				
				if(postpaid != null && postpaid.isPaymentFlag()) {
					
					TransactionDto transaction = transactionRepository.findByPostpaidId(model.getPostpaidId());
					
					return postpaidReceipt(transaction,postpaid);
					
				}else {
					
					if(postpaid==null) {
						structure.setMessage("Postpaid details not found.");
					}else {
						structure.setMessage("Postpaid payment status should be Success");
					}
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
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

	
	
	
	
	public ResponseStructure postpaidReceipt(TransactionDto trans, PostpaidPayment postpaid) throws Exception{

		ResponseStructure structure = new ResponseStructure();
		
        String bankReference = trans.getBankReference();
		
		if(bankReference==null){
			
			bankReference="-";
		}
		
		String recNo = FileUtils.getRandomOTPnumber(8);
		
		ReceiptDetails rec = new ReceiptDetails();
		
		rec.setTransactionId(trans.getTrancsactionId());
		rec.setPrepaidId(0);
		rec.setPostpaidId(trans.getPostpaidId());
		rec.setReceiptNo(recNo);
		rec.setReceiptName("Rec_"+recNo);
		rec.setReceiptIssuanceDate(new Date());
		rec.setPlanType("Postpaid");
		rec.setBankReferenceNo(bankReference);
		rec.setPaymentType(trans.getPaymentMethod());
		rec.setPaymentDate(trans.getPaymentDatetime());
		rec.setPaidBy(trans.getEntity().getName());
		rec.setPaidAmount(trans.getExclusiveAmount());
		rec.setGst(trans.getExclusiveAmount()*18/100);
		rec.setTotalPaidAmount(trans.getPaidAmount());
		
		rec.setAmountInWords(AmountToWords.convertAmountToWords(trans.getPaidAmount()));
		
		structure.setMessage(AppConstants.SUCCESS);
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setData(rec);
		structure.setFlag(1);
		
		return structure;
	}





	public ResponseStructure prepaidReceipt(TransactionDto trans, PrepaidPayment prepaid) throws Exception{
		
		ResponseStructure structure = new ResponseStructure();
		
		String bankReference = trans.getBankReference();
		
		if(bankReference==null){
			
			bankReference="-";
		}
		
		String recNo = FileUtils.getRandomOTPnumber(8);
		
		ReceiptDetails rec = new ReceiptDetails();
		
		rec.setTransactionId(trans.getTrancsactionId());
		rec.setPrepaidId(trans.getPrepaidId());
		rec.setPostpaidId(0);
		rec.setReceiptNo(recNo);
		rec.setReceiptName("Rec_"+recNo);
		rec.setReceiptIssuanceDate(new Date());
		rec.setPlanType("Prepaid");
		rec.setBankReferenceNo(bankReference);
		rec.setPaymentType(trans.getPaymentMethod());
		rec.setPaymentDate(trans.getPaymentDatetime());
		rec.setPaidBy(trans.getEntity().getName());
		rec.setPaidAmount(trans.getExclusiveAmount());
		rec.setGst(0);
		rec.setTotalPaidAmount(trans.getPaidAmount());
		
		rec.setAmountInWords(AmountToWords.convertAmountToWords(trans.getPaidAmount()));
		
		structure.setMessage(AppConstants.SUCCESS);
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setData(rec);
		structure.setFlag(1);
		
		return structure;
	}





	public ResponseStructure uploadPrepaidReceipt(int prepaidId, MultipartFile receipt) {

		ResponseStructure structure = new ResponseStructure();

		try {

			PrepaidPayment prepaid = prepaidRepository.findByPrepaidId(prepaidId);

			if (prepaid != null) {
				
				prepaid.setReceipt(saveReceipt(receipt));
				prepaid.setReceiptGeneratedDate(LocalDate.now());
				prepaidRepository.save(prepaid);
				
				TransactionDto transaction = transactionRepository.findByPrepaidId(prepaidId);
				emailService.prepaidPaymentSuccessMail(prepaid.getEntityModel().getEmail(), transaction, prepaid);
				
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


	

	public ResponseStructure uploadPostpaidReceipt(int postpaidId, MultipartFile receipt) {

		ResponseStructure structure = new ResponseStructure();

		try {

			PostpaidPayment post = postpaidRepository.findByPostpaidId(postpaidId);

			if (post != null) {
				
				post.setReceipt(saveReceipt(receipt));
				post.setReceiptGeneratedDate(LocalDate.now());
				postpaidRepository.save(post);
				
				TransactionDto transaction = transactionRepository.findByPostpaidId(postpaidId);
				emailService.postpaidPaymentSuccessMail(post.getEntityModel().getEmail(), transaction,
						post);
				
				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(post);
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
	
	
	
	
	
	public String saveReceipt(MultipartFile profilePhoto) {

		try {
			String extensionType = null;
			StringTokenizer st = new StringTokenizer(profilePhoto.getOriginalFilename(), ".");
			while (st.hasMoreElements()) {
				extensionType = st.nextElement().toString();
			}
			String fileName = profilePhoto.getOriginalFilename();
			Path currentWorkingDir = Paths.get(context.getRealPath("/WEB-INF/"));
			File saveFile = new File(currentWorkingDir + "/receipt/");
			saveFile.mkdir();

			byte[] bytes = profilePhoto.getBytes();
			Path path = Paths.get(saveFile + "/" + fileName);
			Files.write(path, bytes);
			return fileName;

		} catch (Exception e) {
			return null;
		}
	}





	public ResponseEntity<Resource> viewPrepaidReceipt(int prepaidId, HttpServletRequest request) {
		
		Optional<PrepaidPayment> invo = prepaidRepository.findById(prepaidId);

		if (invo.isPresent()) {
			
			if (invo.get().getReceipt() != null) {

				final Resource resource = resourceLoader.getResource("/WEB-INF/receipt/" + invo.get().getReceipt());
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





	public ResponseEntity<Resource> viewPostpaidReceipt(int postpaidId, HttpServletRequest request) {
		
		Optional<PostpaidPayment> invo = postpaidRepository.findById(postpaidId);

		if (invo.isPresent()) {
			
			if (invo.get().getReceipt() != null) {

				final Resource resource = resourceLoader.getResource("/WEB-INF/receipt/" + invo.get().getReceipt());
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
