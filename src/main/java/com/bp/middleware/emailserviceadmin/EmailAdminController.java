package com.bp.middleware.emailserviceadmin;

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

import kotlin.internal.RequireKotlin.Container;

@RestController
@RequestMapping("/adminemail")
@CrossOrigin
public class EmailAdminController {

	
	@Autowired
	private EmailAdminService service;
	
	
	@PostMapping("/add")
	public ResponseStructure addMail(@RequestBody RequestModel model) {
		return service.addMail(model);
	}
	
	
	@PutMapping("/update")
	public ResponseStructure update(@RequestBody RequestModel model) {
		return service.update(model);
	}
	
	
	@PutMapping("/activation")
	public ResponseStructure activateOtherMail(@RequestBody RequestModel model) {
		return service.activateOtherMail(model);
	}
	
	
	@GetMapping("/viewall")
	public ResponseStructure viewAll() {
		return service.viewAll();
	}
	
	@GetMapping("/view-byId/{id}")
	public ResponseStructure viewById(@PathVariable("id")int id) {
		return service.viewById(id);
	}
	
	
	@GetMapping("/view-currentlyActive")
	public ResponseStructure viewCurrentlyActive() {
		return service.viewCurrentlyActive();
	}
}
