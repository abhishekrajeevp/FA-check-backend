package com.bp.middleware.invoiceupdate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

@RestController
@RequestMapping("/invoiceupdate")
@CrossOrigin
public class InvoiceupdateController {

	
	@Autowired
	private InvoiceUpdateService service;
	
	
	@PostMapping("/prepaid")
	public ResponseStructure editPrepaidInvoice(@RequestBody RequestModel model) {
		return service.editPrepaidInvoice(model);
	}
	
	
	@PostMapping("/delete-prepaidEditedInvoice")
	public ResponseStructure deletePrepaidEditedInvoice(@RequestBody RequestModel model) {
		return service.deletePrepaidEditedInvoice(model);
	}
	
	
	
	
	@PostMapping("/postpaid")
	public ResponseStructure editPostpaidInvoice(@RequestBody RequestModel model) {
		return service.editPostpaidInvoice(model);
	}
	
	
	@PostMapping("/delete-postpaidEditedInvoice")
	public ResponseStructure deletePostpaidEditedInvoice(@RequestBody RequestModel model) {
		return service.deletePostpaidEditedInvoice(model);
	}
	
}
