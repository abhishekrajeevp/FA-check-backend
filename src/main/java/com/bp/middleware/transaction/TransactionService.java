package com.bp.middleware.transaction;

import com.bp.middleware.admin.RequestModel;
import com.bp.middleware.responsestructure.ResponseStructure;

public interface TransactionService {

	TransactionDto saveTransactionHistory(TransactionDto transactionHistory);

	ResponseStructure findTransactionHistoryModelById(int transactionHistoryId);

	ResponseStructure updatTransactionHistory(TransactionDto transactionHistory, int transactionHistoryId) ;

	ResponseStructure deleteTransactionHistoryModel(int transactionHistoryId);

	ResponseStructure getAllTransactions();

	ResponseStructure getTransactionsForNumberOfDays(int numberOfDays);

	ResponseStructure viewByInvoiceNumber( String invoiceNumber);

	ResponseStructure viewByTrackId(String trackId);

//	ResponseStructure transactionDetailByLoan(int loanId);

}
