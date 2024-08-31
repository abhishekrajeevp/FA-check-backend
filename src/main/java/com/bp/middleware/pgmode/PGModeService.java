package com.bp.middleware.pgmode;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface PGModeService {

	ResponseStructure getActivePaymentMode();

	ResponseStructure createPgOrder(int transactionId, int pgModeId);

	ResponseStructure checkRequest(int transactionId, int pgModeId);

	ResponseStructure paymentResponse(String referNo, boolean status, int pgModeId);

	ResponseStructure generatePaymentLink(RequestModel model);

}
