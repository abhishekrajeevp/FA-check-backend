package com.bp.middleware.requestandresponse;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.CommonRequestDto;

public interface DirectVerificationService {

	ResponseStructure verification(RequestModel dto);

	ResponseStructure gstVerification(RequestModel dto);

	ResponseStructure aadharXmlVerification(RequestModel dto);

	ResponseStructure aadhaarOtpVerification(RequestModel dto);

	ResponseStructure cinVerification(RequestModel dto);

	ResponseStructure dinVerification(RequestModel dto);

	ResponseStructure msmeVerification(RequestModel dto);

	ResponseStructure rcVerification(RequestModel dto);

	ResponseStructure passportId(RequestModel dto);

	ResponseStructure drivingLicenceId(RequestModel dto);

}
