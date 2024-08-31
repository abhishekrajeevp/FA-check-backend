package com.bp.middleware.payment;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentMethod, Integer>{


	PaymentMethod getByPaymentId(int paymentId);

	PaymentMethod findByPaymentType(String paymentType);

	PaymentMethod findByPaymentId(int paymentId);


}
