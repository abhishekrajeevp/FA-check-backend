package com.bp.middleware.transaction;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.bp.middleware.admin.RequestModel;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.DateUtil;

@Service
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	private TransactionRepository repository;

//	@Autowired
//	private LoanRepository loanRepository;

	@Override
	public TransactionDto saveTransactionHistory(TransactionDto transactionHistory) {
		return repository.save(transactionHistory);
	}

	@Override
	public ResponseStructure findTransactionHistoryModelById(int transactionHistoryId) {

		ResponseStructure structure = new ResponseStructure();
		Optional<TransactionDto> entity = repository.findByTrancsactionId(transactionHistoryId);
		if (entity.isPresent()) {

			TransactionDto model = entity.get();

			structure.setData(model);
			structure.setMessage("Transaction Detailes are....");
			structure.setFlag(1);
		} else {

			structure.setMessage("Transaction Detailes are....");
			structure.setData(null);
			structure.setFlag(2);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		return structure;
	}

	@Override
	public ResponseStructure updatTransactionHistory(TransactionDto transactionHistory, int transactionHistoryId) {

		ResponseStructure structure = new ResponseStructure();

		Optional<TransactionDto> dto = repository.findById(transactionHistoryId);
		if (dto.isPresent()) {
			TransactionDto prof = dto.get();
			if (prof.getTrancsactionId() != 0) {
				prof.setPaidAmount(transactionHistory.getPaidAmount());

				repository.save(prof);
				structure.setData(prof);
				structure.setMessage("Update Successfully...!!!");
				structure.setFlag(1);

			} else {
				structure.setData(null);
				structure.setMessage("Updated not Successfully...!!!");
				structure.setFlag(2);

			}

		} else {
			structure.setData(null);
			structure.setMessage(AppConstants.ID_NOT_FOUND);
			structure.setFlag(2);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		return structure;
	}

	@Override
	public ResponseStructure viewByTrackId(String trackId) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<TransactionDto> opt = repository.findByTrackId(trackId);

			if (opt.isPresent()) {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setData(opt.get());
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);

			}
		} catch (Exception e) {

			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure deleteTransactionHistoryModel(int transactionHistoryId) {

		ResponseStructure structure = new ResponseStructure();
		repository.deleteById(transactionHistoryId);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setMessage("Deleted Successfully...!!!");
		structure.setFlag(1);

		return structure;
	}

	@Override
	public ResponseStructure getAllTransactions() {

		ResponseStructure structure = new ResponseStructure();
		try {
			List<TransactionDto> list = repository.findAll();
			if (!list.isEmpty()) {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Transaction details are");
				structure.setData(list);
				structure.setFlag(1);
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	@Override
	public ResponseStructure getTransactionsForNumberOfDays(int numberOfDays) {

		ResponseStructure structure = new ResponseStructure();

		try {

			String toDate = LocalDate.now().toString() + " 23:59:59.999999";
			String fromDate = LocalDate.now().minusDays(numberOfDays).toString() + " 00:00:00.000000";

			List<TransactionDto> transactionBetweenDates = repository.findByPaymentDatetime(fromDate, toDate);
			int count = transactionBetweenDates.size();

			if (!transactionBetweenDates.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Transaction details Between " + LocalDate.now().minusDays(numberOfDays) + " and "
						+ LocalDate.now());
				structure.setData(transactionBetweenDates);
				structure.setFlag(1);
				structure.setCount(count);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Transaction done Between " + LocalDate.now().minusDays(numberOfDays) + " and "
						+ LocalDate.now());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(count);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

//	@Override
//	public ResponseStructure transactionDetailByLoan(int loanId) {
//		ResponseStructure structure=new ResponseStructure();
//		
//		Optional<LoanDetails> optional = loanRepository.findById(loanId);
//		if (optional.isPresent()) {
//			LoanDetails details = optional.get();
//			List<TransactionDto> transactions= repository.findByLoan(details);
//			
//			structure.setMessage(AppConstants.SUCCESS);
//			structure.setData(transactions);
//			structure.setFlag(1);
//		} else {
//			structure.setMessage(AppConstants.NO_DATA_FOUND);
//			structure.setData(null);
//			structure.setFlag(2);
//
//		}
//		structure.setStatusCode(HttpStatus.OK.value());
//		return structure;
//	}

	public ResponseStructure viewByInvoiceNumber(String invoiceNumber) {

		ResponseStructure structure = new ResponseStructure();
		try {

			TransactionDto transaction = repository.findByInvoiceNumber(invoiceNumber);

			if (transaction != null) {
				structure.setMessage(AppConstants.SUCCESS);
				structure.setData(transaction);
				structure.setFlag(1);
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);

			}
		} catch (Exception e) {

			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

}