package com.bp.middleware.bond;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantBondRepository extends JpaRepository<MerchantBond, Integer>{

	MerchantBond findByBondId(int bondId);
	
	MerchantBond findByBondAmount(double bondAmount);

}
