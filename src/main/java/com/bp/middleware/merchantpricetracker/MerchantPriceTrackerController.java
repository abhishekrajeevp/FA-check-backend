package com.bp.middleware.merchantpricetracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

@RestController
@RequestMapping("/merchantpricetracker")
@CrossOrigin
public class MerchantPriceTrackerController {

	
	@Autowired
	private MerchantPriceTrackerService service;
	
	
	@GetMapping("/mailtrigger")
	public ResponseStructure mailTriggerAllTogether() {
		return service.mailTriggerAllTogether();
	}
	
	
	@GetMapping("/mailtrigger-forEntity/{userId}")
	public ResponseStructure mailTriggerIndividually(@PathVariable("userId")int userId) {
		return service.mailTriggerIndividually(userId);
	}
	
	
	@PostMapping("/mailtrigger-ByMultiselect")
	public ResponseStructure mailTriggerByMultiSelect(@RequestBody RequestModel model) {
		return service.mailTriggerByMultiSelect(model);
	}
	
	
	
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
	
	
	//BY ENTITIES 
	
	@GetMapping("/view-by/currentlyActiveforEntity/{userId}")
	public ResponseStructure viewByCurrentlyActiveForUser(@PathVariable("userId")int userId) {
		return service.viewByCurrentlyActiveForUser(userId);
	}
	
	
	@GetMapping("/view-by/lastlyUsedforEntity/{userId}")
	public ResponseStructure viewByLastlyUsedForUser(@PathVariable("userId")int userId) {
		return service.viewByLastlyUsedForUser(userId);
	}
	
	
	@GetMapping("/view-by/activatedTodayforEntity/{userId}")
	public ResponseStructure viewByActivatedTodayForUser(@PathVariable("userId")int userId) {
		return service.viewByActivatedTodayForUser(userId);
	}
	
	@PostMapping("/viewbyRecentIdentityFierAndEntity")
	public ResponseStructure findByRecentIdentifierAndEntity(@RequestBody RequestModel model) {
		return service.findByRecentIdentifierAndEntity(model);
	}
	
	
	
	@PostMapping("/viewbyRecentIdentifierrAndEntityAndPriority")
	public ResponseStructure findByRecentIdentifierAndEntityAndPriority(@RequestBody RequestModel model) {
		return service.findByRecentIdentifierAndEntityAndPriority(model);
	}
	
}
