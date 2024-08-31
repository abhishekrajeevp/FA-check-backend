package com.bp.middleware.refund;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

@RestController
@RequestMapping("/refund")
@CrossOrigin
public class RefundController {

	@Autowired 
	private RefundService service;
	
	
	@PostMapping("/refund-request")
	public ResponseStructure requestRefund(@RequestBody RequestModel model) {
		return service.requestRefund(model);
	}
}
