package com.bp.middleware.vendorapipricesetup;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.user.EntityModel;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorVerificationModel;

public interface VendorPriceRepository extends JpaRepository<VendorPriceModel, Integer>{

	List<VendorPriceModel> findByVendorModel(VendorModel vendorModel);

	VendorPriceModel findByVendorModelAndVendorVerificationModel(VendorModel vendorModel,
			VendorVerificationModel vendorVerificationModel);

	VendorPriceModel findByVendorVerificationModel(VendorVerificationModel vendorVerificationModel);

	List<VendorPriceModel> findByStatus(boolean status);

	List<VendorPriceModel> findByVendorVerificationModelAndStatus(VendorVerificationModel vendorVerifyModel, boolean status);

	VendorPriceModel getByVendorModelAndVendorVerificationModelAndStatus(VendorModel vendorModel,
			VendorVerificationModel verificationModel, boolean status);


}
