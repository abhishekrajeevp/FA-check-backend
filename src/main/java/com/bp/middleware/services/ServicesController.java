package com.bp.middleware.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.admin.RequestModel;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.util.AppConstants;

@RestController
@RequestMapping("/services")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class ServicesController {

	@Autowired
	private ServicesService service;
	
	@PostMapping("/add")
	public ResponseStructure addServices(@RequestBody RequestModel model) {
		return service.addServices(model);
	}
	
	@PutMapping("/update")
	public ResponseStructure updateService(@RequestBody RequestModel model) {
		return service.updateServices(model);
	}
	
	@GetMapping("/searchbyid/{id}")
	public ResponseStructure getById(@PathVariable("id")int id) {
		return service.getById(id);
	}
	
	@GetMapping("/viewall")
	public ResponseStructure viewAll() {
		return service.viewAll();
	}
	
	@PutMapping("/updateaccountstatus")
	public ResponseStructure accountStatusChange(@RequestBody RequestModel model ) {
		return service.accountStatusChange(model);

	}
}
