package com.bp.middleware.usermanagement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserManagementPasswordRepository extends JpaRepository<UserManagementPasswordHistory, Integer>{

	List<UserManagementPasswordHistory> findByUserManagement(UserManagement user);

}
