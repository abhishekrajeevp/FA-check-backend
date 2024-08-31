package com.bp.middleware.merchantapipricesetup;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface MerchantPriceService {

	ResponseStructure addMerchantPriceAndUrl(RequestModel model);

	ResponseStructure viewByMerchantPriceId(int vendorPriceId);

	ResponseStructure viewAllMerchantPrice();

	ResponseStructure viewByVendorId(int vendorId);

	ResponseStructure updateMerchantPriceOrUrl(RequestModel model);

	ResponseStructure updateMerchantPriceStatus(RequestModel model);

	ResponseStructure viewByVendorIdAndVerificationId(int vendorId, int verificationId);

	ResponseStructure viewByVendorVerificationId(int vendorVerificationId);

	ResponseStructure viewByMerchantPriceStatus(boolean status);

	ResponseStructure viewByUserId(int userId);

	ResponseStructure viewByVendorIdAndVerificationIdAndUser(int vendorId, int verificationId, int userId);

	ResponseStructure addMerchantPriceInList(RequestModel model);

	ResponseStructure updateMerchantPriceStatusByUserAndVerification(RequestModel model, int userId,
			int verificationId);

	ResponseStructure copyMerchantPriceFromOneUserToAnother(RequestModel model);

	ResponseStructure updateMerchantSourceCheck(int userId, int vendorVerificationId, RequestModel model);

	ResponseStructure viewByUserAndVerification(int userId, int vendorVerificationId);

	ResponseStructure viewAllForUser(int userId);

	ResponseStructure addMerchantPriceForAadhaarBasedSigning(RequestModel model);

	ResponseStructure acceptNewPrice(int userId);

	ResponseStructure updatePriority(RequestModel model);

	ResponseStructure entityVerificationDropDown(int userId);

}
