package com.bp.middleware.merchantpricetracker;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorVerificationModel;

public interface MerchantPriceTrackerRepository extends JpaRepository<MerchantPriceTracker, Integer>{

	Optional<MerchantPriceTracker> findByVendorModelAndVendorVerificationModelAndEntityModelAndRecentIdentifier(
			VendorModel vendorModel, VendorVerificationModel vendorVerificationModel, EntityModel user, int i);

	List<MerchantPriceTracker> findByCurrentlyInUse(boolean b);

	List<MerchantPriceTracker> findByRecentIdentifier(int i);

	List<MerchantPriceTracker> findByApplicableFromDateAndCurrentlyInUse(LocalDate now, boolean b);

	List<MerchantPriceTracker> findByCurrentlyInUseAndEntityModel(boolean b, EntityModel entityModel);

	List<MerchantPriceTracker> findByRecentIdentifierAndEntityModel(int i, EntityModel entityModel);

	List<MerchantPriceTracker> findByApplicableFromDateAndCurrentlyInUseAndEntityModel(LocalDate now, boolean b,
			EntityModel entityModel);

	@Query(value= "select * from merchant_price_tracker where recent_identifier=:recentIdentifier and user_id=:userId",nativeQuery = true)
	List<MerchantPriceTracker> getByRecentIdentifierAndEntity(@Param("recentIdentifier") int recentIdentifier,@Param("userId") int userId);

	@Query(value= "select * from merchant_price_tracker where recent_identifier=:recentIdentifier and user_id=:userId and priority=:priority",nativeQuery = true)
	List<MerchantPriceTracker> getByRecentIdentifierAndEntityAndPriority(@Param("recentIdentifier")int recentIdentifier,@Param("userId")int userId,@Param("priority") int priority);

	List<MerchantPriceTracker> findByCurrentlyInUseAndMailSend(boolean currentlyInUse, boolean mailSend);

}
