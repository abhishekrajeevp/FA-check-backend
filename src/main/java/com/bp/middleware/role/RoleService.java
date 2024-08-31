package com.bp.middleware.role;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

import jakarta.servlet.http.HttpServletRequest;

public interface RoleService {

	ResponseStructure createRoles(RequestModel role);

	ResponseStructure getAllRoles();

	ResponseStructure getById(int roleId);

	ResponseStructure updateRoleStatus(RequestModel dto);

	ResponseStructure activeAccounts(boolean roleStatus);

	ResponseStructure updateRoleDetails(RequestModel entity, int roleId);
	
	ResponseStructure uploadAdminProfilePicture(int roleId, MultipartFile profilePhoto);

	ResponseEntity<Resource> viewImage(int roleId, HttpServletRequest request);

}
