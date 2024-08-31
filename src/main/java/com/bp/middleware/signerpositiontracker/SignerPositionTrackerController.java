package com.bp.middleware.signerpositiontracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

@RestController
@RequestMapping("/positiontracker")
@CrossOrigin
public class SignerPositionTrackerController {

	
	@Autowired
	public SignerPositionTrackerService service;
	
	@GetMapping("/viewall")
	public ResponseStructure viewAll() {
		return service.viewAll();
	}
	
	
	@GetMapping("/viewbyid")
	public ResponseStructure viewById(@RequestBody RequestModel model) {
		return service.viewById(model);
	}
	
	
	@GetMapping("/viewbysigner")
	public ResponseStructure viewBySigner(@RequestBody RequestModel model) {
		return service.viewBySigner(model);
	}
	
	
}
