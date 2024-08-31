package com.bp.middleware.bankaccountandhsncode;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/bankorHsn")
public class BankAccAndHsnCodeController {

	@Autowired
	private BankAccAndHsnCodeService service;
	
	@PostMapping("/addbankDetails")
	public ResponseStructure addBankDetails(@RequestBody RequestModel model) {
		return service.addBankDetails(model);
	}
	
	
	@PostMapping("/addHsn")
	public ResponseStructure addHsn(@RequestBody RequestModel model) {
		return service.addHsn(model);
	}
	
	
	@GetMapping("/viewAll-bankAcc")
	public ResponseStructure viewAllBankAcc() {
		return service.viewAllBankAcc();
	}
	
	
	@GetMapping("/viewAll-hsn")
	public ResponseStructure viewAllHsn() {
		return service.viewAllHsn();
	}
	
	
	@PutMapping("/update-hsn")
	public ResponseStructure updateHsn(@RequestBody RequestModel model) {
		return service.updateHsn(model);
	}
	
	
	@PutMapping("/update-bankacc")
	public ResponseStructure updateBankAcc(@RequestBody RequestModel model) {
		return service.updateBankAcc(model);
	}
	
	
	@GetMapping("/viewbyActive-bankAcc")
	public ResponseStructure viewByActiveBankAcc() {
		return service.viewByActiveBankAcc();
	}
	
	
	@GetMapping("/viewbyActive-hsn")
	public ResponseStructure viewByActiveHsn() {
		return service.viewByActiveHsn();
	}
	
	
	@PutMapping("/updateStatus-hsn")
	public ResponseStructure updateStatusHsn(@RequestBody RequestModel model) {
		return service.updateStatusHsn(model);
	}
	
	
	@PutMapping("/updateStatus-bankacc")
	public ResponseStructure updateStatusBankAcc(@RequestBody RequestModel model) {
		return service.updateStatusBankAcc(model);
	}
	
}
