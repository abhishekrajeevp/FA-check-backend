package com.bp.middleware.vendors;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface VendorVerificationService {

	ResponseStructure addVendorVerification(RequestModel model);

	ResponseStructure viewVendorVerification(int vendorVerifyId);

	ResponseStructure viewAllVendorVerify();

	//ResponseStructure getVendorVerificationByVendor(int vendorId);

	ResponseStructure updateVendorVerification(RequestModel model);

	ResponseStructure updateVendorVerificationStatus(RequestModel model);

	ResponseStructure viewAllVendorVerificationsByStatus(boolean status);

	ResponseStructure activeVerifications();

}
