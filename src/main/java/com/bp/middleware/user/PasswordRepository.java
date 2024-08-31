package com.bp.middleware.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRepository extends JpaRepository<PasswordModelHistory, Integer>{

	List<PasswordModelHistory> findByUser(EntityModel user);


}
