package com.bp.middleware.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.admin.RequestModel;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.util.AppConstants;



@RestController
@RequestMapping("/transaction")
@CrossOrigin(origins = {AppConstants.CROSS_ORIGIN})
public class TransactionController {

	@Autowired
	private TransactionService service;

	@GetMapping("/view/{transactionHistoryId}")
	public  ResponseStructure getById(@PathVariable("transactionHistoryId") int transactionHistoryId) {

		return service.findTransactionHistoryModelById(transactionHistoryId);
	}
	
	
	@GetMapping("/viewByTrackId/{trackId}")
	public  ResponseStructure viewByTrackId(@PathVariable("trackId") String trackId) {

		return service.viewByTrackId(trackId);
	}
	
	
	
	@PutMapping("{transactionHistoryId}")
	public ResponseStructure updateEntity(@RequestBody TransactionDto transactionHistory,
			@PathVariable("transactionHistoryId") int transactionHistoryId) {
		
		return service.updatTransactionHistory(transactionHistory, transactionHistoryId);
	}
	
	@DeleteMapping("{transactionHistoryId}")
	public ResponseStructure deleteCityEntity(@PathVariable("transactionHistoryId") int transactionHistoryId) {
		
		return service.deleteTransactionHistoryModel(transactionHistoryId);
	}
	
	@GetMapping("/transaction")
	public ResponseStructure getAllTransactions() { 
		
		return service.getAllTransactions();
	}
	
	@GetMapping("/transactiondetailsbydays/{numberOfDays}")
	public ResponseStructure getTransactionsForNumberOfDays(@PathVariable("numberOfDays")int numberOfDays) { 
		return service.getTransactionsForNumberOfDays(numberOfDays);
	}
	

	@GetMapping("/viewByInvoiceNumber/{invoiceNumber}")
	public ResponseStructure viewByInvoiceNumber(@PathVariable("invoiceNumber")String invoiceNumber) { 
		return service.viewByInvoiceNumber(invoiceNumber);
	}
	
	
//	@GetMapping("/details/{loanId}")
//	public ResponseStructure getTransactionDetailByLoanId(@PathVariable("loanId") int loanId) {
//		return service.transactionDetailByLoan(loanId);
//		
//	}

	
}
