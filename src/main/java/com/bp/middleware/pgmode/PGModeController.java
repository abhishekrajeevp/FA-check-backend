package com.bp.middleware.pgmode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;


@RestController
@RequestMapping("/pgmode")
@CrossOrigin(origins = {AppConstants.CROSS_ORIGIN})
public class PGModeController {
	
	@Autowired
	private PGModeService service;
	

	@GetMapping("/createorder/{transactionId}/{pgModeId}")
	public ResponseStructure getActivePaymentMode(@PathVariable("transactionId") int transactionId,
			@PathVariable("pgModeId") int pgModeId) {
		return service.createPgOrder(transactionId, pgModeId);
	}
	
	@GetMapping("/initiate/{pgModeId}/{transactionId}")
	public ResponseStructure initiatePayment(@PathVariable("pgModeId") int pgModeId,@PathVariable("transactionId") int transactionId) {
		return service.checkRequest(transactionId, pgModeId);
	}
	
	
	@PostMapping("/generate-paymentlink")
	public ResponseStructure generatePaymentLink(@RequestBody RequestModel model) {
		
		return service.generatePaymentLink(model);
	}
	
	
	
	@GetMapping("/view")
	public ResponseStructure getActivePaymentMode() {
		return service.getActivePaymentMode();
	}
	
	@GetMapping("/paymentresponse/{referNo}/{status}/{pgModeId}")
	public ResponseStructure paymentResponse (@PathVariable String referNo,
			@PathVariable boolean status, @PathVariable int pgModeId) {
		return service.paymentResponse(referNo, status, pgModeId);
	}
	@GetMapping(value = "/merchant")
	public ResponseStructure getMerchant(@RequestParam String reference, @RequestParam boolean success,
			@RequestParam int pgModeId) {
		return service.paymentResponse(reference, success, pgModeId);
	}
	

}
