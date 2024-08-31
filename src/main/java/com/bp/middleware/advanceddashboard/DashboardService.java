package com.bp.middleware.advanceddashboard;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface DashboardService {

	ResponseStructure allMerchantSuccessFailureCount();

	ResponseStructure allVendorSuccessFailureCount();

	ResponseStructure specificMerchantSuccessFailureCount(int userId);

	ResponseStructure specificVendorSuccessFailureCount(int vendorId);

	ResponseStructure filterByEntityAndVerificationDocument(int userId, int verificationId);

	ResponseStructure merchantDetailsByDays(int numberOfDays);

	ResponseStructure merchantDetailsForLastSevenDays();

	ResponseStructure merchantDetailsForLastFifteenDays();

	ResponseStructure merchantDetailsForLastThirtyDays();

	ResponseStructure merchantDetailsBetweenTwoDates(String startDate, String endDate);

	ResponseStructure vendorDetailsByDays(int numberOfDays);
	

	ResponseStructure vendorDetailsForLastSevenDays();

	ResponseStructure vendorDetailsForLastFifteenDays();

	ResponseStructure vendorDetailsForLastThirtyDays();

	ResponseStructure vendorDetailsBetweenTwoDates(String startDate, String endDate);

	ResponseStructure dashboardForEntityAndVerificationDocument(int userId, int verificationId);

	ResponseStructure overAllSigners();

	ResponseStructure signersByEntity(int userId);

	ResponseStructure merchantDetailsBetweenTwoDatesAsSingleDate(RequestModel model);

	ResponseStructure vendorDetailsBetweenTwoDatesAsSingleDate(RequestModel model);

	ResponseStructure overAllMerchantAgreement();

	ResponseStructure merchantAgreementByEntity(int userId);

	ResponseStructure overAllMerchantAgreementFilterBetweenDates(RequestModel model);

	ResponseStructure merchantAgreementByEntityFilterBetweenDates(RequestModel model);

	

}
