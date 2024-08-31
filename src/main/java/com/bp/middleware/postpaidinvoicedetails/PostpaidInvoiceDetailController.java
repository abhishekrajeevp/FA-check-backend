package com.bp.middleware.postpaidinvoicedetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/postpaidInvoice")
@CrossOrigin
public class PostpaidInvoiceDetailController {

	
	@Autowired
	private PostpaidInvoiceDetailService service;
	
	//REMINDER INVOICE
	@GetMapping("/postpaidReminder-invoice")
	public ResponseStructure postpaidReminderInvoice() {
		
		return service.postpaidReminderInvoice();
	}
	
	//FORCE OR GRACE INVO
	@PostMapping("/postpaidForceGrace-invoice")
	public ResponseStructure postpaidGraceOrForceInvoice(@RequestBody RequestModel model) {
		
		return service.postpaidGraceOrForceInvoice(model);
	}
	
	//CONVENIENCE INVOICE
	@PostMapping("/postpaidConvenience-invoice")
	public ResponseStructure postpaidConvenienceInvoice(@RequestBody RequestModel model) {
		
		return service.postpaidConvenienceInvoice(model);
	}
	
	
	
	//INVOICE ADDING
	@PostMapping("/uploadPostpaidInvoice-reminder")
	public ResponseStructure uploadPostpaidReminderInvoice(@RequestParam("postpaidId") int postpaidId,@RequestParam("invoice") MultipartFile invoice) {
		return service.uploadPostpaidReminderInvoice(postpaidId, invoice);
	}
	
	@PostMapping("/uploadPostpaidInvoice-conveInvo")
	public ResponseStructure uploadPostpaidConvenienceInvoice(@RequestParam("postpaidId") int postpaidId,@RequestParam("invoice") MultipartFile invoice) {
		return service.uploadPostpaidConvenienceInvoice(postpaidId, invoice);
	}
	
	@PostMapping("/uploadPostpaidInvoice-graceInvo")
	public ResponseStructure uploadPostpaidGraceInvoice(@RequestParam("postpaidId") int postpaidId,@RequestParam("invoice") MultipartFile invoice) {
		return service.uploadPostpaidGraceInvoice(postpaidId, invoice);
	}
	
	
	
	//INVOICE VIEW
	@GetMapping("/viewpostpaid-reminderInvoice/{postpaidId}")
	public ResponseEntity<Resource> viewPostpaidReminderinvoice(@PathVariable("postpaidId") int postpaidId, HttpServletRequest request){
		return service.viewPostpaidReminderinvoice(postpaidId, request);
	}
	
	
	@GetMapping("/viewpostpaid-conveInvoice/{postpaidId}")
	public ResponseEntity<Resource> viewPostpaidConvenienceInvoice(@PathVariable("postpaidId") int postpaidId, HttpServletRequest request){
		return service.viewPostpaidConvenienceInvoice(postpaidId, request);
	}
	
	
	@GetMapping("/viewpostpaid-graceInvoice/{postpaidId}")
	public ResponseEntity<Resource> viewPostpaidGraceInvoice(@PathVariable("postpaidId") int postpaidId, HttpServletRequest request){
		return service.viewPostpaidGraceInvoice(postpaidId, request);
	}
}
