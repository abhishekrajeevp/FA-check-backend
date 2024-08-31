package com.bp.middleware.bond;

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
import com.bp.middleware.util.AppConstants;

@RestController
@RequestMapping("/merchantbond")
@CrossOrigin(origins = {AppConstants.CROSS_ORIGIN})
public class MerchantBondController {
	
	@Autowired
	private MerchantBondService service;
	
	@PostMapping("/add")
	public ResponseStructure addBondAmount(@RequestBody RequestModel model) {
		return service.addBondAmount(model);
	}
	@GetMapping("/getall")
	public ResponseStructure listAll() {
		return service.listAllBondPrice();
	}
	@GetMapping("/getbyid/{bondId}")
	public ResponseStructure getById(@PathVariable("bondId") int bondId) {
		return service.getBondById(bondId);
	}
	@PutMapping("/updatestatus/{bondId}")
	public ResponseStructure updateStatus(@PathVariable("bondId") int bondId,@RequestBody RequestModel model) {
		return service.updateStatus(bondId,model);
	}

}
