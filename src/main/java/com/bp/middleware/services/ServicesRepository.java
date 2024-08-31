package com.bp.middleware.services;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicesRepository extends JpaRepository<ServicesEntity, Integer>{

	ServicesEntity findByServiceId(int serviceId);

}
