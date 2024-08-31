package com.bp.middleware.vendorpricetracker;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorVerificationModel;

public interface VendorPriceTrackerRepository extends JpaRepository<VendorPriceTracker, Integer>{

	Optional<VendorPriceTracker> findByVendorModelAndVendorVerificationModelAndRecentIdentifier(
			VendorModel vendorModel, VendorVerificationModel vendorVerificationModel, int recentIdentifier);

	List<VendorPriceTracker> findByCurrentlyInUse(boolean b);

	List<VendorPriceTracker> findByRecentIdentifier(int i);

	List<VendorPriceTracker> findByApplicableFromDateAndCurrentlyInUse(LocalDate now, boolean b);

}
