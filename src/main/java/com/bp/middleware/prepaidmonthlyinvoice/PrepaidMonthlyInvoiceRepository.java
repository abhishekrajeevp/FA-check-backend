package com.bp.middleware.prepaidmonthlyinvoice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.user.EntityModel;

public interface PrepaidMonthlyInvoiceRepository extends JpaRepository<PrepaidMonthlyInvoice, Integer>{


	List<PrepaidMonthlyInvoice> findByEntity(EntityModel entity);

	Optional<PrepaidMonthlyInvoice> findByUniqueId(String uniqueId);

}
