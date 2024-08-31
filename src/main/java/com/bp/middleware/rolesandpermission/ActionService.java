package com.bp.middleware.rolesandpermission;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface ActionService {


	ResponseStructure addActionForAdmin(RequestModel model);
	
	ResponseStructure viewActionById(int actionId);

	ResponseStructure viewall();

	ResponseStructure viewByStatus(boolean status);

	ResponseStructure viewByPermissionId(int permissionId);

	ResponseStructure viewByMakerId(int makerId);

	ResponseStructure viewByAdminId(int adminId);

	ResponseStructure viewByAdminAndPermission(int adminId, int permissionId);

	ResponseStructure viewByAdminAndMakerChecker(int adminId, int makerId);

	ResponseStructure viewByAdminAndMakerCheckerAndPermission(int adminId, int makerId, int permissionId);

	ResponseStructure copyRolesAndPermissionWithEmail(int adminId,RequestModel model);

	ResponseStructure viewBySubPermissionId(int subPermissionId);

	ResponseStructure viewBySuperAdminId(int superAdminId);

	ResponseStructure changeStatus(int actionId, RequestModel model);

	ResponseStructure addActionForUser(RequestModel model);

	ResponseStructure viewByUserId(int userId);

	ResponseStructure viewByUserManagement(int userManagementId);

	ResponseStructure copyRolesAndPermissionWithEmailForUserManagement(int userManagementId, RequestModel model);

}
