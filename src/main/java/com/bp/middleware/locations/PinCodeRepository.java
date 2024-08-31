package com.bp.middleware.locations;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PinCodeRepository extends JpaRepository<PincodeModel, Integer> {

	List<PincodeModel> findByCityModel(CityModel model);
	
	PincodeModel findByPincodeId(int pincodeId);

//	Optional<PincodeModel> findByPincodeOpt(String pincode);
	
	PincodeModel findByPincode(String pincode);
}
