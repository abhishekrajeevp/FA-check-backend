package com.bp.middleware.rolesandpermission;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.user.EntityModel;


public interface MakerAndCheckerRepository extends JpaRepository<MakerAndChecker, Integer>{

	List<MakerAndChecker> findByAdminModel(AdminDto admin);

	MakerAndChecker findByMakerCheckerId(int makerCheckerId);

	List<MakerAndChecker> findByStatus(boolean status);

	MakerAndChecker findByMakerCheckerRoleNameAndAdminModel(String makerCheckerRoleName, AdminDto admin);

	MakerAndChecker findByMakerCheckerRoleNameAndUser(String makerCheckerRoleName, EntityModel entity);

	List<MakerAndChecker> findByUser(EntityModel entity);


	

}
