package com.bp.middleware.merchantapipricesetup;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.user.EntityModel;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorVerificationModel;

public interface MerchantPriceRepository extends JpaRepository<MerchantPriceModel, Integer>{

	List<MerchantPriceModel> findByVendorModel(VendorModel vendorModel);

	MerchantPriceModel findByVendorModelAndVendorVerificationModel(VendorModel vendorModel,
			VendorVerificationModel vendorVerificationModel);

	MerchantPriceModel findByVendorVerificationModel(VendorVerificationModel vendorVerificationModel);

	List<MerchantPriceModel> findByStatus(boolean status);

	List<MerchantPriceModel> findByEntityModel(EntityModel vendorModel);

	MerchantPriceModel findByVendorModelAndVendorVerificationModelAndEntityModel(VendorModel vendorModel,
			VendorVerificationModel vendorVerificationModel, EntityModel entityModel);

	MerchantPriceModel findByEntityModelAndVendorModelAndVendorVerificationModel(EntityModel entityModel,
			VendorModel vendorModel, VendorVerificationModel vendorVerificationModel);

	MerchantPriceModel findByEntityModelAndVendorVerificationModelAndPriority(EntityModel user,
			VendorVerificationModel vendorVerifyModel, int i);

	List<MerchantPriceModel> findByEntityModelAndVendorVerificationModel(EntityModel entityModel,
			VendorVerificationModel vendorVerificationModel);

	List<MerchantPriceModel>findByEntityModelAndVendorVerificationModelAndStatus(EntityModel userModel,
			VendorVerificationModel vendorVerifyModel, boolean status);

	List<MerchantPriceModel> findByEntityModelAndStatus(EntityModel userModel, boolean status);

	MerchantPriceModel getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(VendorModel vendorModel,
			VendorVerificationModel verificationModel, EntityModel userModel, boolean status);

	MerchantPriceModel getByEntityModelAndVendorVerificationModelAndStatus(EntityModel user,
			VendorVerificationModel vendorVerifyModel, boolean b);

	List<MerchantPriceModel> findByEntityModelAndAccepted(EntityModel userModel, boolean b);

}
