package com.bp.middleware.invoiceupdate;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

import jakarta.servlet.ServletContext;

@Service
public class InvoiceUpdateService {

	@Autowired
	ServletContext context;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private PrepaidRepository prepaidRepository;
	@Autowired
	private PrepaidInvoiceEdit prepaidInvoiceEdit;
	@Autowired
	private PostpaidRepository postpaidRepository;
	@Autowired
	private PostpaidInvoiceEdit postpaidInvoiceEdit;

	public ResponseStructure editPrepaidInvoice(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<PrepaidPayment> prepaidOpt = prepaidRepository.findById(model.getPrepaidId());
			Optional<TransactionDto> transactionOpt = transactionRepository.findByInvoice(model.getInvoice());
		

			if (transactionOpt.isPresent() && prepaidOpt.isPresent()) {

				TransactionDto transactionDto = transactionOpt.get();
				PrepaidPayment prepaidPayment = prepaidOpt.get();

				Path con = Paths.get(context.getRealPath("/WEB-INF/"));

				EntityModel entity = prepaidPayment.getEntityModel();

				String generatedIn = "";

				boolean nativeLocal = entity.getStateName().equalsIgnoreCase("Tamilnadu")
						|| entity.getStateName().equalsIgnoreCase("Tamil nadu");

				if (nativeLocal) {

					generatedIn = prepaidInvoiceEdit.updatePrepaidCgstInvoice(transactionDto, prepaidPayment, model,
							con);

				} else {
					generatedIn = prepaidInvoiceEdit.updatePrepaidIgstInvoice(transactionDto, prepaidPayment, model,
							con);
				}

				if (generatedIn.equalsIgnoreCase("A")) {

					structure.setMessage("Invoice edited and saved in Invoice-A");
					structure.setFlag(1);
				} else if (generatedIn.equalsIgnoreCase("B")) {
					structure.setMessage("Invoice edited and saved in Invoice-B");
					structure.setFlag(1);
				} else {
					structure.setMessage("Invoice Editing failed since no slot left to save edited invoice.");
					structure.setFlag(2);
				}

				structure.setData(generatedIn);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {

				if (transactionOpt.isEmpty()) {
					structure.setMessage("BASE INVOICE NOT FOUND IN TRANSACTION");
				} else {
					structure.setMessage("BASE INVOICE NOT FOUND IN PREPAID");
				}
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(4);
				structure.setData(null);
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

	public ResponseStructure deletePrepaidEditedInvoice(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<PrepaidPayment> opt = prepaidRepository.findById(model.getPrepaidId());

			if (opt.isPresent()) {

				PrepaidPayment prepaidPayment = opt.get();

				if (model.isDeleteInvoiceTwo()) {

					prepaidPayment.setInvoiceTwo("Deleted");
				}
				if (model.isDeleteInvoiceThree()) {

					prepaidPayment.setInvoiceThree("Deleted");
				}

				prepaidRepository.save(prepaidPayment);
				
				if(model.isDeleteInvoiceTwo() && model.isDeleteInvoiceThree()) {
					structure.setMessage("Both the edited invoice deleted");
				}else if(model.isDeleteInvoiceTwo()) {
					structure.setMessage("Edited invoice - A deleted");
				}else if(model.isDeleteInvoiceThree()) {
					structure.setMessage("Edited invoice - B deleted");
				}else {
					structure.setMessage("None of the edited invoice deleted");
				}
				
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(prepaidPayment);
				
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

	public ResponseStructure editPostpaidInvoice(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<PostpaidPayment> postOpt = postpaidRepository.findById(model.getPostpaidId());
			Optional<TransactionDto> transactionOpt = transactionRepository.findByInvoice(model.getInvoice());
		

			if (transactionOpt.isPresent() && postOpt.isPresent()) {

				TransactionDto transactionDto = transactionOpt.get();
				PostpaidPayment postpaidPayment = postOpt.get();

				Path con = Paths.get(context.getRealPath("/WEB-INF/"));

				EntityModel entity = postpaidPayment.getEntityModel();

				String generatedIn = "";

				boolean nativeLocal = entity.getStateName().equalsIgnoreCase("Tamilnadu")
						|| entity.getStateName().equalsIgnoreCase("Tamil nadu");

				if (nativeLocal) {

					generatedIn = postpaidInvoiceEdit.updatePostpaidCgstInvoice(transactionDto, postpaidPayment, model,
							con);

				} else {
					generatedIn = postpaidInvoiceEdit.updatePostpaidIgstInvoice(transactionDto, postpaidPayment, model,
							con);
				}

				if (generatedIn.equalsIgnoreCase("A")) {

					structure.setMessage("Invoice edited and saved in Invoice-A");
					structure.setFlag(1);
				} else if (generatedIn.equalsIgnoreCase("B")) {
					structure.setMessage("Invoice edited and saved in Invoice-B");
					structure.setFlag(1);
				} else {
					structure.setMessage("Invoice Editing failed since no slot left to save edited invoice.");
					structure.setFlag(2);
				}

				structure.setData(generatedIn);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {

				if (transactionOpt.isEmpty()) {
					structure.setMessage("BASE INVOICE NOT FOUND IN TRANSACTION");
				} else {
					structure.setMessage("BASE INVOICE NOT FOUND IN PREPAID");
				}
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(4);
				structure.setData(null);
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

	
	
	public ResponseStructure deletePostpaidEditedInvoice(RequestModel model) {
	
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<PostpaidPayment> opt = postpaidRepository.findById(model.getPostpaidId());

			if (opt.isPresent()) {

				PostpaidPayment postpaid = opt.get();

				if (model.isDeleteInvoiceTwo()) {

					postpaid.setInvoiceTwo("Deleted");
				}
				if (model.isDeleteInvoiceThree()) {

					postpaid.setInvoiceThree("Deleted");
				}

				postpaidRepository.save(postpaid);
				
				if(model.isDeleteInvoiceTwo() && model.isDeleteInvoiceThree()) {
					structure.setMessage("Both the edited invoice deleted");
				}else if(model.isDeleteInvoiceTwo()) {
					structure.setMessage("Edited invoice - A deleted");
				}else if(model.isDeleteInvoiceThree()) {
					structure.setMessage("Edited invoice - B deleted");
				}else {
					structure.setMessage("None of the edited invoice deleted");
				}
				
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(postpaid);
				
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

}
