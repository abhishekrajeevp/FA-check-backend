package com.bp.middleware.emailserviceadmin;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailAdminRepository extends JpaRepository<EmailAdmin, Integer>{

	EmailAdmin findByCurrentlyActive(boolean b);

}
