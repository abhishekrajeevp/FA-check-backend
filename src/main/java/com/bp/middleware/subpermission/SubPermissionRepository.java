package com.bp.middleware.subpermission;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.rolesandpermission.PermissionModel;
import com.bp.middleware.user.EntityModel;

public interface SubPermissionRepository extends JpaRepository<SubPermission, Integer>{

	List<SubPermission> findByStatus(boolean status);

	List<SubPermission> findByAdmin(AdminDto admin);

	List<SubPermission> findByPermission(PermissionModel permission);

	SubPermission findBySubPermissionId(int subPermissionId);

	SubPermission findBySubPermissionNameAndPermissionAndAdmin(String subPermissionName, PermissionModel permission,
			AdminDto admin);

	SubPermission findBySubPermissionNameAndPermissionAndUser(String subPermissionName, PermissionModel permission,
			EntityModel entity);

	List<SubPermission> findByUser(EntityModel entity);

	List<SubPermission> findByAdminAndPermission(AdminDto admin, PermissionModel permission);

	List<SubPermission> findByUserAndPermission(EntityModel entity, PermissionModel permission);


}
