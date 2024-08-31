package com.bp.middleware.bond;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface MerchantBondService {

	ResponseStructure addBondAmount(RequestModel model);

	ResponseStructure listAllBondPrice();

	ResponseStructure getBondById(int bondId);

	ResponseStructure updateStatus(int bondId, RequestModel model);
}
