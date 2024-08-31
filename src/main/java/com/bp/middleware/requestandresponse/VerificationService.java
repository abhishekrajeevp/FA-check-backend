package com.bp.middleware.requestandresponse;


import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.CommonRequestDto;

import jakarta.servlet.http.HttpServletRequest;

public interface VerificationService {
	
	ResponseStructure verification(RequestModel dto ,HttpServletRequest servletRequest);

	ResponseStructure gstVerification(RequestModel dto ,HttpServletRequest servletRequest);

	ResponseStructure panImageVerification(RequestModel model ,HttpServletRequest servletRequest);

	ResponseStructure gstImageVerification(RequestModel model ,HttpServletRequest servletRequest);

	ResponseStructure aadharXmlVerification(RequestModel dto ,HttpServletRequest servletRequest);

	ResponseStructure aadhaarOtpVerification(RequestModel dto ,HttpServletRequest servletRequest);

	ResponseStructure findById(int requestId);

	ResponseStructure demoController(RequestModel model);

	ResponseStructure demoOtpController(RequestModel model);

	ResponseStructure demoImageController(MultipartFile image, int userId);

	ResponseStructure totalHitCount();

	ResponseStructure hitCountByEntity(int userId);

	ResponseStructure hitForToday();

	ResponseStructure hitForThisMonth(int month);

	ResponseStructure hitForThisWeek(String date, String date2);

	ResponseStructure totalResponseHitCount();

	ResponseStructure responsehitCountByEntity(int userId);

	ResponseStructure responseHitCountForToday();

	ResponseStructure hitResponseForThisMonth(int month);

	ResponseStructure responseHitForThisWeek(String date1, String date2);

	ResponseStructure hitResponseCountByRequest(int requestId);

	ResponseStructure successResponseCount();

	ResponseStructure databaseSuccessResponse();

	ResponseStructure eSignDemo(CommonRequestDto dto);

	ResponseStructure esignValidate(RequestModel model);

	ResponseStructure adminAllInOneDashboard();

	ResponseStructure merchantAllInOneDashboard(RequestModel model);

	ResponseStructure cinVerification(RequestModel dto ,HttpServletRequest servletRequest);

	ResponseStructure dinVerification(RequestModel dto ,HttpServletRequest servletRequest);

	ResponseStructure msmeVerification(RequestModel dto ,HttpServletRequest servletRequest);

	ResponseStructure rcVerification(RequestModel dto ,HttpServletRequest servletRequest);

	ResponseStructure drivingLicenceId(RequestModel dto ,HttpServletRequest servletRequest);

	ResponseStructure passportId(RequestModel dto ,HttpServletRequest servletRequest);

	ResponseStructure demoImageControllerForTwoImages(MultipartFile front, MultipartFile rear, int userId);

	ResponseStructure passportImage(RequestModel dto ,HttpServletRequest servletRequest);

	ResponseStructure drivingLicenceImage(RequestModel dto ,HttpServletRequest servletRequest);

//	ResponseStructure jweDemoController(RequestModel model);

	ResponseStructure toManuallyChangeStatus(RequestModel model);

	ResponseStructure demoGstController(RequestModel model);

	ResponseStructure viewMerchantRequestByNumberOfDays(int noOfDays);

	ResponseStructure viewVendorRequestByNumberOfDays(int noOfDays);

	ResponseStructure viewAllRequestResponseReplica();

	ResponseStructure viewByIdRequestResponseReplica(int replicaId);

	ResponseStructure viewByEntityRequestResponseReplicaCombined(int userId);

	ResponseStructure reqByOneEntity();

	ResponseStructure byEntityAndVerificationType(RequestModel model);

	ResponseStructure reqByOneVendor();

	ResponseStructure byVendorAndVerificationType(RequestModel model);

}
