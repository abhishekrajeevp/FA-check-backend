package com.bp.middleware.payment;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface PaymentService {

	ResponseStructure addPaymentMethod(RequestModel model);

	ResponseStructure viewPaymentById(int paymentId);

	ResponseStructure viewAllPaymentMethod();

	ResponseStructure changeStatus(RequestModel model);

	ResponseStructure makePayment(RequestModel user);

	ResponseStructure trackTransactionFromPg(int payId);

}
