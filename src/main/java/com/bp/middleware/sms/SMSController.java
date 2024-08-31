package com.bp.middleware.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.responsestructure.ResponseStructure;

@RestController
@RequestMapping("/sms")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class SMSController {

	@Autowired
	private SMSService service;
	
	
	@PostMapping("/addByAdmin")
	public ResponseStructure createSMSByAdmin(@RequestBody RequestModel model) {
		return service.createSMSByAdmin(model);
	}
	
	@PostMapping("/add")
	public ResponseStructure createSMS(@RequestBody RequestModel model) {
		return service.addSMS(model);
	}
	
	@GetMapping("/getbytempid/{smsId}")
	public ResponseStructure getByTemplateId(@PathVariable("smsId") int smsId) {
		return service.addGetByTemplateId(smsId);
	}
	
	@GetMapping("/viewall")
	public ResponseStructure listAll() {
		return service.listAll();
	}
	
	@PutMapping("/update/{smsId}")
	public ResponseStructure updateTemplate(@PathVariable("smsId") int smsId,@RequestBody RequestModel model) {
		return service.updateDetails(smsId,model);
	}
	
	@PutMapping("/changetempstatus/{smsId}")
	public ResponseStructure changeTempStatus(@PathVariable("smsId") int smsId,@RequestBody RequestModel model) {
		return service.changeTemplateStatus(smsId,model);
	}
	
	
	@GetMapping("/getbyAdmin/{adminId}")
	public ResponseStructure viewByAdmin(@PathVariable("adminId") int adminId) {
		return service.viewByAdmin(adminId);
	}
	
	
	@GetMapping("/getAllAdminSms")
	public ResponseStructure allAdminSms() {
		return service.allAdminSms();
	}
	
	
	@GetMapping("/getbyuser/{userId}")
	public ResponseStructure getTemplateByUser(@PathVariable("userId") int userId) {
		return service.getTemplateByUser(userId);
	}
	
	@DeleteMapping("/delete/{smsId}")
	public ResponseStructure deleteById(@PathVariable("smsId") int smsId) {
		return service.deleteById(smsId);
	}
	
	
	@DeleteMapping("/truncate")
	public ResponseStructure truncate() { // try truncate query
		return service.truncate();
	}
}
