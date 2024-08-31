package com.bp.middleware.bond;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BondDetailRepository extends JpaRepository<BondDetails, Integer> {

	List<BondDetails> findByBond(MerchantBond bond);

	List<BondDetails> findBySealedDate(LocalDate sealDate);

	BondDetails findByBondNumber(String bondNumber);

	BondDetails findByBondDetailId(int i);

	List<BondDetails> findByBondStatus(int i);
}
