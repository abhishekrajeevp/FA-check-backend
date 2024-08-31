package com.bp.middleware.vendors;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorRepository extends JpaRepository<VendorModel, Integer>{

	VendorModel findByVendorId(int vendorId);

	VendorModel findByVendorName(String string);

	List<VendorModel> findBystatus(boolean status);



}
