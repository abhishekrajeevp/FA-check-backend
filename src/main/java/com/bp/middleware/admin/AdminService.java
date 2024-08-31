package com.bp.middleware.admin;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;

import jakarta.servlet.http.HttpServletRequest;

public interface AdminService {

	ResponseEntity<ResponseStructure> createUser(RequestModel entity, HttpServletRequest servletRequest);

	ResponseStructure login(RequestModel entity);

	ResponseStructure verifyEmail(String email);

	ResponseStructure verifyotp(int adminId, RequestModel user);

	ResponseStructure resndOtp(int adminId);

	ResponseStructure reset(RequestModel model);

	ResponseStructure changePassword(RequestModel model);

	ResponseStructure accountStatusChange(RequestModel model);

	ResponseStructure activeAccounts(boolean accountStatus);

	ResponseStructure fetchById(int adminId);
	

	ResponseStructure updateDetails(int adminId, RequestModel model);

	ResponseStructure uploadAdminProfilePicture(int adminId, MultipartFile profilePhoto);

	ResponseEntity<Resource> viewImage(int adminId, HttpServletRequest request);

	ResponseStructure viewAllAdmin();

	ResponseStructure decodeValue(int passwordId);

	ResponseEntity<ResponseStructure> createSuperAdmin(RequestModel entity, HttpServletRequest servletRequest);

	ResponseStructure blockUnblock(int adminId, boolean blockUnblockStatus);


}
