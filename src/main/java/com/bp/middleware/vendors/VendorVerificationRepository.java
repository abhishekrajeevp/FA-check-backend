package com.bp.middleware.vendors;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorVerificationRepository extends JpaRepository<VendorVerificationModel, Integer>{

	VendorVerificationModel findByVendorVerificationId(int vendorVerificationId);

	VendorVerificationModel findByVerificationDocument(String verificationDocument);

	List<VendorVerificationModel> findByStatus(boolean status);

	VendorVerificationModel findByVerificationDocumentAndStatus(String verificationDocument,boolean status);



}
