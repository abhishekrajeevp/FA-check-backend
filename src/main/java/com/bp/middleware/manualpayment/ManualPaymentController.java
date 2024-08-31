package com.bp.middleware.manualpayment;

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

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/manualpayment")
@CrossOrigin
public class ManualPaymentController {

	
	
	@Autowired
	private ManualPaymentService service;
	
	
	@PostMapping("/makepayment")
	public ResponseStructure makeManualPayment(@RequestBody RequestModel model) {
		return service.makeManualPayment(model);
	}
	
	@PostMapping("/postpaid-activation")
	public ResponseStructure postpaidActivation(@RequestBody RequestModel model) {
		return service.postpaidActivation(model);
	}
	
	
	@PostMapping("/trigger-mail")
	public ResponseStructure triggerMail(@RequestBody RequestModel model) {
		return service.triggerMail(model);
	}
	
	
	@PostMapping("/addimage")
	public ResponseStructure addProofImage(@RequestParam("manualPayId") int manualPayId,@RequestParam("proof") MultipartFile proof) {
		return service.addProofImage(manualPayId,proof);
	}
	
	
	@PutMapping("/update")
	public ResponseStructure manualPaymentUpdate(@RequestBody RequestModel model) {
		return service.manualPaymentUpdate(model);
	}
	
	
	
	@GetMapping("/viewproof/{manualpayId}")
	public ResponseEntity<Resource> viewProof(@PathVariable("manualpayId") int manualpayId, HttpServletRequest request){
		return service.viewProof(manualpayId, request);
	}
	
	
	@GetMapping("/viewall")
	public ResponseStructure viewAllManualPayment() {
		return service.viewAllManualPayment();
	}
	
	@GetMapping("/viewbyid/{manualPayId}")
	public ResponseStructure manualPaymentViewById(@PathVariable("manualPayId")int manualPayId) {
		return service.manualPaymentViewById(manualPayId);
	}
	
	@GetMapping("/viewby-entity/{userId}")
	public ResponseStructure manualPaymentViewByUserId(@PathVariable("userId")int userId) {
		return service.manualPaymentViewByUserId(userId);
	}
	
	@GetMapping("/viewby-transaction/{transactionId}")
	public ResponseStructure manualPaymentViewByTransactionId(@PathVariable("transactionId")int transactionId) {
		return service.manualPaymentViewByTransactionId(transactionId);
	}
	
}
