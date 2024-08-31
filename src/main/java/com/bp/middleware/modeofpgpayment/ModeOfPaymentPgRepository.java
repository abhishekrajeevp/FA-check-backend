package com.bp.middleware.modeofpgpayment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ModeOfPaymentPgRepository extends JpaRepository<ModeOfPaymentPg, Integer>{

	Optional<ModeOfPaymentPg> findByModeOfPayment(String paymentMode);

	Optional<ModeOfPaymentPg> findByPaymentCode(String paymentCode);

	List<ModeOfPaymentPg> findByStatus(boolean status);

}
