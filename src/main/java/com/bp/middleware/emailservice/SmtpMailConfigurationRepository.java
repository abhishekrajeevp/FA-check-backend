package com.bp.middleware.emailservice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SmtpMailConfigurationRepository extends JpaRepository<SmtpMailConfiguration, Integer>{

}
