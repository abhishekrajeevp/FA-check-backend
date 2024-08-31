package com.bp.middleware.ticketraising;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.user.EntityModel;


public interface TicketRaisingRepository extends JpaRepository<TicketRaising, Integer>{

	TicketRaising findByReferenceNumber(String referenceNumber);

	List<TicketRaising> findByEntityModel(EntityModel model);

	List<TicketRaising> getByStatus(boolean status);
}
