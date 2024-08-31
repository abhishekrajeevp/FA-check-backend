package com.bp.middleware.emailservice;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.user.EntityModel;


public interface SmtpMailRepository extends JpaRepository<SmtpMailConfiguration, Integer>{

	Optional<SmtpMailConfiguration> findByEntity(EntityModel user);



}
