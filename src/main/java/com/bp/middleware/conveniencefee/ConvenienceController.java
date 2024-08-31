package com.bp.middleware.conveniencefee;

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
@RequestMapping("/conveniencefee")
@CrossOrigin
public class ConvenienceController {

	
	@Autowired
	private ConvenienceService service;
	
	
	
	@PostMapping("/create")
	public ResponseStructure addConveniencePercentage(@RequestBody RequestModel model) {
		
		return service.addConveniencePercentage(model);
	}
	
	@GetMapping("/viewall")
	public ResponseStructure viewAll() {
		
		return service.viewAll();
	}
	
	@GetMapping("/viewbyid/{convenienceId}")
	public ResponseStructure viewById(@PathVariable("convenienceId")int convenienceId) {
		
		return service.viewById(convenienceId);
	}
	
	@GetMapping("/viewbystatus/{status}")
	public ResponseStructure viewByStatus(@PathVariable("status")int status) {
		
		return service.viewByStatus(status);
	}
	
	@GetMapping("/viewby-modeofpay/{modeId}")
	public ResponseStructure viewByModeOfPay(@PathVariable("modeId")int modeId) {
		return service.viewByModeOfPay(modeId);
	}
	
	@GetMapping("/viewby-modeofpayandcurrentlyActive/{modeId}")
	public ResponseStructure viewByModeOfPayAndCurrentlyActive(@PathVariable("modeId")int modeId) {
		return service.viewByModeOfPayAndCurrentlyActive(modeId);
	}
	
	@GetMapping("/viewby-modeofpayandamount/{modeId}/{amount}")
	public ResponseStructure viewByModeOfPayAndAmount(@PathVariable("modeId")int modeId,@PathVariable("amount")int amount) {
		return service.viewByModeOfPayAndAmount(modeId,amount);
	}
	
	@PutMapping("/update")
	public ResponseStructure update(@RequestBody RequestModel model) {
		
		return service.update(model);
	}
	
	@PutMapping("/update-status")
	public ResponseStructure updateStatus(@RequestBody RequestModel model) {
		
		return service.updateStatus(model);
	}
	
}
