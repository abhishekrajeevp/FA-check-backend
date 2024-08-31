package com.bp.middleware.mcccode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping(path ="/mccCodes")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class MCCController {
	
	@Autowired
	private MCCService mccService;
	
	@PostMapping(path="/addMCCCode")
	public ResponseStructure addMCCCode(@RequestBody RequestModel dto){
		return mccService.addMCCCode(dto);
	}
	
	@GetMapping(path ="/viewMCCCodeById/{mccId}")
	public ResponseStructure viewMCCCodeById(@PathVariable("mccId") int mccId){
		return mccService.viewMCCCodeById(mccId);
	}
	
	@GetMapping(path ="/viewAllMCCCode")
	public ResponseStructure viewAllMCCCode(){
		return mccService.viewAllMCCCode();
	}
	
	@PutMapping(path = "/updateMCCCode/{mccId}")
	public ResponseStructure updateMCCCodeById(@PathVariable("mccId") int mccId,@RequestBody RequestModel dto){
		return mccService.updateMCCCodeById(mccId,dto);
	}
	
	@PutMapping("/updateaccountstatus")
	public ResponseStructure changeAccountStatus(@RequestBody RequestModel model ) {
		return mccService.changeAccountStatus(model);
	}
	
	@GetMapping(path ="/viewAllMCCCode-active")
	public ResponseStructure viewAllMCCCodeActive(){
		return mccService.viewAllMCCCodeActive();
	}
	
	@DeleteMapping(path="/deleteMCCCode/{mccId}")
	public ResponseStructure deleteById(@PathVariable int mccId){
		return mccService.deletMCCCodeById(mccId);
		
	}

}
