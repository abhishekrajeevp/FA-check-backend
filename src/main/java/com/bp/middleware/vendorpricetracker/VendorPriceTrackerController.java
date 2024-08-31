package com.bp.middleware.vendorpricetracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.annotation.GetExchange;

import com.bp.middleware.responsestructure.ResponseStructure;

@RestController
@RequestMapping("/vendorpricetracker")
@CrossOrigin
public class VendorPriceTrackerController {

	
	@Autowired
	private VendorPriceTrackerService service;
	
	
	@GetMapping("/viewall")
	public ResponseStructure viewAll() {
		return service.viewAll();
	}
	
	
	@GetMapping("/viewbyid/{id}")
	public ResponseStructure viewById(@PathVariable("id")int id) {
		return service.viewById(id);
	}
	
	@GetMapping("/view-by/currentlyActive")
	public ResponseStructure viewByCurrentlyActive() {
		return service.viewByCurrentlyActive();
	}
	
	
	@GetMapping("/view-by/lastlyUsed")
	public ResponseStructure viewByLastlyUsed() {
		return service.viewByLastlyUsed();
	}
	
	
	@GetMapping("/view-by/activatedToday")
	public ResponseStructure viewByActivatedToday() {
		return service.viewByActivatedToday();
	}
	
}
