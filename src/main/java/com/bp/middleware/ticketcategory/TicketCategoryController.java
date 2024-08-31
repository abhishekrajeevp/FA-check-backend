package com.bp.middleware.ticketcategory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;


@RestController
@RequestMapping("/ticketcategory")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class TicketCategoryController {

	@Autowired
	private TicketCategoryService service;
	
	@PostMapping("/add")
	public ResponseStructure saveCategoryType(@RequestBody RequestModel model) {
		return service.saveCategoryType(model);
		
	}
	@PutMapping("/update/{ticketCategoryId}")
	public ResponseStructure updateTicket(@RequestBody RequestModel model,@PathVariable("ticketCategoryId") int ticketCategoryId) {
		return service.updateTicket(model,ticketCategoryId);
		
	}
	
	@GetMapping("/viewall")
	public ResponseStructure viewAllTicketCategories() {
		return service.viewAllTicketCategories();
	}
	
}
