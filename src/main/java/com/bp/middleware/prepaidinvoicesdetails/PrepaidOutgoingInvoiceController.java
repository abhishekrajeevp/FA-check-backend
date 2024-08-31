package com.bp.middleware.prepaidinvoicesdetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

@RestController
@RequestMapping("/outgoingInvoice")
public class PrepaidOutgoingInvoiceController {

	
	@Autowired
	private PrepaidOutgoingInvoiceService service;
	
	//MONTHLY INVOICE
	@GetMapping("/prepaidMonthend-invoice")
	public ResponseStructure prepaidMonthEndInvoice() {
		return service.prepaidMonthEndInvoice();
	}
	
	
	//MONTHLY INVOICE
	@GetMapping("/prepaidMonthEndInvoiceMailTrigger")
	public ResponseStructure prepaidMonthEndInvoiceMailTrigger() {
		return service.prepaidMonthEndInvoiceMailTrigger();
	}
	
	
	//CONVENIENCE INVOICE
	@PostMapping("/prepaidConvenience-invoice")
	public ResponseStructure prepaidConvenienceInvoice(@RequestBody RequestModel model) {
		
		return service.prepaidConvenienceInvoice(model);
	}
	
	
	
	//FORCED INVOICE
	@PostMapping("/prepaidForced-invoice")
	public ResponseStructure prepaidForcedInvoice(@RequestBody RequestModel model) {
		
		return service.prepaidForcedInvoice(model);
	}
	
}
