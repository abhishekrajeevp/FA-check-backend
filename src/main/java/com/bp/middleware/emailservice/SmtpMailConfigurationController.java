package com.bp.middleware.emailservice;

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
@RequestMapping("/smtpmail")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class SmtpMailConfigurationController {
	
	@Autowired
	private SmtpMailService service;
	
	@PostMapping("/addsmtpmail")
	public ResponseStructure addSmtpMail(@RequestBody RequestModel model) {
		return service.addSmptMailConfiguration(model);
	}
	
	@GetMapping("/getByid/{mailId}")
	public ResponseStructure getById(@PathVariable("mailId") int mailId) {
		return service.getByMailId(mailId);
	}
	
	@GetMapping("/viewall")
	public ResponseStructure listAll() {
		return service.viewAll();
	}
	
	@PutMapping("/updateemail/{mailId}")
	public ResponseStructure updateEmail(@RequestBody RequestModel model,
			@PathVariable ("mailId") int mailId ) {
		return service.updateEamil(model,mailId);
	}
	
	@GetMapping("/viewbyuserId/{userId}")
	public ResponseStructure viewByUser(@PathVariable("userId") int userid) {
		return service.viewByUser(userid);
	}

}
