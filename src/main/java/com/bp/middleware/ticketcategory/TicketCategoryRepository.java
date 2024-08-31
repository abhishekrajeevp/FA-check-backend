package com.bp.middleware.ticketcategory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCategoryRepository extends JpaRepository<TicketCategory, Integer> {

	TicketCategory findByTicketCategoryId(int ticketCategoryId);

}
