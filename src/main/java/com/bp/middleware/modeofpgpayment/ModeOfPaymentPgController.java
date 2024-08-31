package com.bp.middleware.modeofpgpayment;

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

@RestController
@RequestMapping("/modeofpayment")
@CrossOrigin
public class ModeOfPaymentPgController {

	
	@Autowired
	private ModeOfPaymentPgService service;
	
	@PostMapping("/create-modeofpayment")
	public ResponseStructure addModeOfPayment(@RequestBody RequestModel model) {
		
		return service.addModeOfPayment(model);
	}
	
	@GetMapping("/viewall")
	public ResponseStructure viewAll() {
		
		return service.viewAll();
	}
	
	@GetMapping("/viewbyid/{modeId}")
	public ResponseStructure viewById(@PathVariable("modeId")int modeId) {
		
		return service.viewById(modeId);
	}
	
	@GetMapping("/viewbystatus/{status}")
	public ResponseStructure viewByStatus(@PathVariable("status")boolean status) {
		
		return service.viewByStatus(status);
	}
	
	@PutMapping("/update/{modeId}")
	public ResponseStructure update(@PathVariable("modeId")int modeId,@RequestBody RequestModel model) {
		
		return service.update(modeId,model);
	}
	
	@PutMapping("/updatestatus/{modeId}")
	public ResponseStructure updateStatus(@PathVariable("modeId")int modeId,@RequestBody RequestModel model) {
		
		return service.updateStatus(modeId,model);
	}
	
}
