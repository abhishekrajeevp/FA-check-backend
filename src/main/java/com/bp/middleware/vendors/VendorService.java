package com.bp.middleware.vendors;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface VendorService {

	ResponseStructure addVendors(RequestModel model);

	ResponseStructure getVendorById(int vendorId);

	ResponseStructure getAllVendors();

	ResponseStructure updateVendors(RequestModel model);

	ResponseStructure updateVendorStatus(RequestModel model);

	void monthlyVendorHitCount();

	ResponseStructure getAllActiveVendors();

}
