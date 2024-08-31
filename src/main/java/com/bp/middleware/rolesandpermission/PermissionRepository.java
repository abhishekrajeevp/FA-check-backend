package com.bp.middleware.rolesandpermission;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.user.EntityModel;

public interface PermissionRepository extends JpaRepository<PermissionModel, Integer>{

	List<PermissionModel> findByAdminModel(AdminDto admin);

	PermissionModel findByPermissionId(int permissionId);

	List<PermissionModel> findByStatus(boolean status);

	PermissionModel findByPermissionAndAdminModel(String permission, AdminDto admin);

	PermissionModel findByPermissionAndUser(String permission, EntityModel entity);

	List<PermissionModel> findByUser(EntityModel entity);


}
