package com.bp.middleware.rolesandpermission;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface PermissionService {

	ResponseStructure addPermission(RequestModel model);

	ResponseStructure viewByPermissionId(int permissionId);

	ResponseStructure viewall();

	ResponseStructure changeStatus(int permissionId, RequestModel model);

	ResponseStructure viewByAdminId(int adminId);

	ResponseStructure update(int permissionId, RequestModel model);

	ResponseStructure viewByStatus(boolean status);

	ResponseStructure addPermissionForUser(RequestModel model);

	ResponseStructure viewByUserId(int userId);

}
