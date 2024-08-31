package com.bp.middleware.bond;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface BondDetailsService {

	ResponseStructure uploadBondDetails(RequestModel model);

	ResponseStructure listAllUploadedBond(int bondId);

	ResponseStructure getBondBySealedDate(RequestModel model);

	ResponseStructure listByBondNumber(String bondNumber);

}
