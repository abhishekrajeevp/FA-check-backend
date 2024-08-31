package com.bp.middleware.role;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<RoleDto, Integer>{
	
	@Query(value="select * from role_table where role_status=:status",nativeQuery = true)
	List<RoleDto> getByAccountStatus(@Param("status")boolean roleStatus);

}
