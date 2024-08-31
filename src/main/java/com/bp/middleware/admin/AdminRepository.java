package com.bp.middleware.admin;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminRepository extends JpaRepository<AdminDto, Integer> {

	AdminDto findByEmail(String email);

	Object findByMobileNumber(String string);

	@Query(value="select * from admin_table where account_status=:status",nativeQuery = true)
	List<AdminDto> getByAccountStatus(@Param("status") boolean accountStatus);

	AdminDto findByAdminId(int adminId);
	

}
