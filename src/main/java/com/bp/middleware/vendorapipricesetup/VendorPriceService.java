package com.bp.middleware.vendorapipricesetup;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface VendorPriceService {

	ResponseStructure addVendorPriceAndUrl(RequestModel model);

	ResponseStructure viewByVendorPriceId(int vendorPriceId);

	ResponseStructure viewAllVendorPrice();

	ResponseStructure viewByVendorId(int vendorId);

	ResponseStructure updateVendorPriceOrUrl(RequestModel model);

	ResponseStructure updateVendorPriceStatus(RequestModel model);

	ResponseStructure viewByVendorIdAndVerificationId(int vendorId, int verificationId);

	ResponseStructure viewByVendorVerificationId(int vendorVerificationId);

	ResponseStructure viewByVendorPriceStatus(boolean status);

	ResponseStructure updateAmount(int vendorPriceId, RequestModel model);

}
