package com.bp.middleware.usermanagement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.user.EntityModel;

public interface UserManagementRepository extends JpaRepository<UserManagement, Integer>{

	UserManagement findByEmail(String email);

	UserManagement findByMobileNumber(String mobileNumber);

	List<UserManagement> getByAccountStatus(boolean accountStatus);

	List<UserManagement> findByUser(EntityModel user);

	UserManagement findByUserManagementId(int userManagementId);

}
