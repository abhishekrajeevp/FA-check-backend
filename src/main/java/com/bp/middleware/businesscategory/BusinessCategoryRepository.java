package com.bp.middleware.businesscategory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.businesstype.BusinessType;

public interface BusinessCategoryRepository extends JpaRepository<BusinessCategory, Integer>{

	BusinessCategory getByBusinessCategoryId(int businessCategoryId);

	BusinessCategory findByBusinessCategoryId(int businessCategoryId);

	Optional<BusinessCategory> findByBusinessCategoryName(String businessCategoryName);

	BusinessCategory getByBusinessCategoryName(String businessCategory);

	List<BusinessCategory> findByStatus(boolean b);

}
