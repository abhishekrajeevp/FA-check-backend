package com.bp.middleware.signers;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.signmerchant.MerchantModel;
import com.bp.middleware.user.EntityModel;


public interface SignerRepository extends JpaRepository<SignerModel, Integer>{

	SignerModel findBySignerId(int signerId);

	List<SignerModel> findByMerchantModel(MerchantModel merchantModel);

	List<SignerModel> findByEntityModel(EntityModel model);

	List<SignerModel> findByMerchantModelAndEntityModel(MerchantModel merchant, EntityModel entity);

	List<SignerModel> findByMerchantModelAndOtpVerificationStatus(MerchantModel merchantModel, boolean b);

	SignerModel findByReferenceNumber(String referenceId);

	SignerModel findBymerchantModelAndSignerAadhaar(MerchantModel merchantModel, String maskedAadhar);
}
