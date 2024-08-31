package com.bp.middleware.rolesandpermission;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.subpermission.SubPermission;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.usermanagement.UserManagement;

public interface ActionRepository extends JpaRepository<ActionModel, Integer>{

	List<ActionModel> findByAccountStatus(boolean status);

	List<ActionModel> findByPermissionModel(PermissionModel entityModel);

	List<ActionModel> findBySubPermission(SubPermission sub);

	List<ActionModel> findByChecker(MakerAndChecker maker);

	List<ActionModel> findByAdminModel(AdminDto adminDto);

	List<ActionModel> findByAdminModelAndPermissionModel(AdminDto entityModel, PermissionModel model);

	List<ActionModel> findByAdminModelAndChecker(AdminDto entityModel, MakerAndChecker model);

	List<ActionModel> findByAdminModelAndCheckerAndPermissionModel(AdminDto entityModel,
			MakerAndChecker makerAndChecker, PermissionModel permissionModel);

	List<ActionModel> findBySuperAdmin(AdminDto adminDto);

	ActionModel findBySubPermissionAndPermissionModelAndCheckerAndAdminModel(SubPermission subPermission,
			PermissionModel permission, MakerAndChecker makerChecker, AdminDto admin);

	ActionModel findBySubPermissionAndPermissionModelAndCheckerAndUserAndUserManagement(SubPermission subPermission,
			PermissionModel permission, MakerAndChecker makerChecker, EntityModel entity, UserManagement userManage);

	List<ActionModel> findByUser(EntityModel entity);

	List<ActionModel> findByUserManagement(UserManagement userManagement);

	List<ActionModel> findByAdminModelAndAccountStatus(AdminDto admin, boolean b);

	List<ActionModel> findByUserManagementAndAccountStatus(UserManagement user, boolean b);

	

}
