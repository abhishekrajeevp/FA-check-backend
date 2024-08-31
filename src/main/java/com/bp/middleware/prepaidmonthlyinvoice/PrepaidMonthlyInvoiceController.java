package com.bp.middleware.prepaidmonthlyinvoice;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/prepaidMonthlyInvoice")
@CrossOrigin
public class PrepaidMonthlyInvoiceController {

	
	@Autowired
	private PrepaidMonthlyInvoiceService service;
	
	//MONTHLY
	
	@PostMapping("/upload-prepaidMonthlyInvoice")
	public ResponseStructure uploadMonthlyInvoice(@RequestParam("entityId") int entityId,
			@RequestParam("fromDate") LocalDate fromDate,@RequestParam("toDate") LocalDate toDate) {
		
		return service.uploadMonthlyInvoice(entityId,fromDate,toDate);
	}
	
	@GetMapping("/view-prepaidMonthlyInvoice/{monthlyInvoiceId}")
	public ResponseEntity<Resource> viewMonthlyInvoice(@PathVariable("monthlyInvoiceId") int monthlyInvoiceId, HttpServletRequest request){
		return service.viewMonthlyInvoice(monthlyInvoiceId, request);
	}
	
	@PostMapping("/getMonthlyInvo-byEntity")
	public ResponseStructure getMonthlyInvoByEntity(@RequestBody RequestModel model) {
		return service.getMonthlyInvoByEntity(model);
	}
	
	
	
	@PutMapping("/updateMonthlyInvoice")
	public ResponseStructure updatePrepaidMonthlyInvo(@RequestParam("monthlyInvoId") int monthlyInvoId,@RequestParam("invoice") MultipartFile invoice) {
		
		return service.updatePrepaidMonthlyInvo(monthlyInvoId, invoice);
	}
	
	
	@GetMapping("/viewByUniqueId/{uniqueId}")
	public ResponseStructure viewByUniqueId(@PathVariable("uniqueId") String uniqueId) {
		
		return service.viewByUniqueId(uniqueId);
	}
	
	
	//CONVENIENCE
	
	@PostMapping("/upload-conveniencePrepaidInvoice")
	public ResponseStructure uploadPrepaidConvenienceInvoice(@RequestParam("prepaidId") int prepaidId,@RequestParam("invoice") MultipartFile invoice) {
		return service.uploadPrepaidConvenienceInvoice(prepaidId, invoice);
	}
	
	
	@GetMapping("/view-prepaidConvenienceInvoice/{prepaidId}")
	public ResponseEntity<Resource> viewPrepaidConvenienceInvoice(@PathVariable("prepaidId") int prepaidId, HttpServletRequest request){
		return service.viewPrepaidConvenienceInvoice(prepaidId, request);
	}
	
}
