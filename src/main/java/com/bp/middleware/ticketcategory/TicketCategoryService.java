package com.bp.middleware.ticketcategory;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface TicketCategoryService {

	ResponseStructure saveCategoryType(RequestModel model);

	ResponseStructure updateTicket(RequestModel model, int ticketCategoryId);

	ResponseStructure viewAllTicketCategories();

}
