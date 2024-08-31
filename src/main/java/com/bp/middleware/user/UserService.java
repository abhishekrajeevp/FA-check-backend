package com.bp.middleware.user;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;

import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

	ResponseStructure createUser(RequestModel model, HttpServletRequest servletRequest);

	ResponseStructure login(RequestModel model);

	ResponseStructure emailVerify(String email);

	ResponseStructure verifyotp(int userId, RequestModel model);

	ResponseStructure resendOtp(int userId);

	ResponseStructure reset(RequestModel model);

	ResponseStructure changePassword(RequestModel model);

	ResponseStructure changeAccountStatus(RequestModel model);

	ResponseStructure activeAccounts(boolean accountStatus);

	ResponseStructure fetchById(int userId);

	ResponseStructure updateDetails(int userId, RequestModel model);

	ResponseStructure uploadAdminProfilePicture(int userId, MultipartFile profilePhoto);

	ResponseEntity<Resource> viewImage(int userId, HttpServletRequest request);

	ResponseStructure viewAllAdmin();

	ResponseStructure encrypt(MultipartFile pic, String password);

	ResponseStructure encryptECB(MultipartFile pic);

	ResponseStructure encryptBase64(MultipartFile pic);

	ResponseStructure addAmount(RequestModel model);

	ResponseStructure updateLogDetails(int userId, RequestModel model);

	ResponseStructure getPrepaidPostpaidUsers(int paymentId);

	ResponseStructure getPostpaidUsersByFlag(boolean flagStatus);

	ResponseStructure getPrepaidPostpaidCount(int paymentId);

	ResponseStructure updateBankDetails(int userId, RequestModel model);

	ResponseStructure approvalStatus(int userId, RequestModel model);

	ResponseStructure requestHistoryAccordingToLogPeriod(int userId);

	ResponseStructure updateBankAccountStatus(RequestModel model);

	ResponseStructure viewBySigningActive();

	ResponseStructure viewByVerificationActive();

	ResponseStructure blockUnblock(int userId, boolean blockUnblockStatus);

	ResponseStructure liveKeysShowOrHide(RequestModel model);

	ResponseStructure forVerificationWithoutRestriction(RequestModel model);

	ResponseStructure findBySalt(String saltKey);

//	String salt(String salt);

}
