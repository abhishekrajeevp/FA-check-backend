package com.bp.middleware.vendors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
@RequestMapping("/vendors")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class VendorController {

	@Autowired
	private VendorService service;

	@PostMapping("/addvendor")
	public ResponseStructure addVendors(@RequestBody RequestModel model) {
		return service.addVendors(model);
	}

	@GetMapping("/getvendor/{vendorId}")
	public ResponseStructure getVendorById(@PathVariable("vendorId") int vendorId) {
		return service.getVendorById(vendorId);
	}

	@GetMapping("/getallvendors")
	public ResponseStructure getAllVendors() {
		return service.getAllVendors();
	}
	
	@GetMapping("/getvendors-active")	
	public ResponseStructure getAllActiveVendors() {
		return service.getAllActiveVendors();
	}
	
	
	@PutMapping("/updatevendor")
	public ResponseStructure updateVendors(@RequestBody RequestModel model) {
		return service.updateVendors(model);
	}
	
	@PutMapping("/updatestatus")
	public ResponseStructure updateVendorStatus(@RequestBody RequestModel model) {
		return service.updateVendorStatus(model);
	}
	
	//@Scheduled(fixedRate = 86400000)//One day
	public void monthlyVendorHitCount() {
		 service.monthlyVendorHitCount();
	}
}



