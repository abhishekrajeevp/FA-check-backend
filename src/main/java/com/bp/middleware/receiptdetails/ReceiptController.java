package com.bp.middleware.receiptdetails;

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
@RequestMapping("/receipt")
@CrossOrigin
public class ReceiptController {

	@Autowired
	private ReceiptService service;
	
	
	@PostMapping("/generate")
	public ResponseStructure receipt(@RequestBody RequestModel model) {
		
		return service.receipt(model);
	}
	
	
	//ADD RECEIPT
	
	@PostMapping("/upload-prepaidReceipt")
	public ResponseStructure uploadPrepaidReceipt(@RequestParam("prepaidId") int prepaidId,@RequestParam("receipt") MultipartFile receipt) {
		return service.uploadPrepaidReceipt(prepaidId, receipt);
	}

	@PostMapping("/upload-postpaidReceipt")
	public ResponseStructure uploadPostpaidReceipt(@RequestParam("postpaidId") int postpaidId,@RequestParam("receipt") MultipartFile receipt) {
		return service.uploadPostpaidReceipt(postpaidId, receipt);
	}
	
	
	
	//VIEW RECEIPT
	
	@GetMapping("/view-prepaidReceipt/{prepaidId}")
	public ResponseEntity<Resource> viewPrepaidReceipt(@PathVariable("prepaidId") int prepaidId, HttpServletRequest request){
		return service.viewPrepaidReceipt(prepaidId, request);
	}
	
	@GetMapping("/view-postpaidReceipt/{postpaidId}")
	public ResponseEntity<Resource> viewPostpaidReceipt(@PathVariable("postpaidId") int postpaidId, HttpServletRequest request){
		return service.viewPostpaidReceipt(postpaidId, request);
	}
	
	
}
