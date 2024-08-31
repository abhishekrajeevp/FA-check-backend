package com.bp.middleware.businesstype;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessTypeRepository extends JpaRepository<BusinessType, Integer>{

	BusinessType getByBusinessTypeId(int businessTypeId);

	BusinessType findByBusinessTypeId(int businessTypeId);

	Optional<BusinessType> findByBusinessType(String businessType);

	BusinessType getByBusinessType(String businessType);

	List<BusinessType> findByStatus(boolean b);

}
