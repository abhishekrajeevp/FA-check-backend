package com.bp.middleware.businesstype;

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
@RequestMapping("/businesstype")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class BusinessTypeController {

	@Autowired
	private BusinessTypeService service;
	
	@PostMapping("/addbusiness")
	public ResponseStructure saveBusinessType(@RequestBody RequestModel model) {
		return service.addBusinessType(model);
		
	}
	@GetMapping("/getbyid/{businessTypeId}")
	public ResponseStructure getBusinessTypeId(@PathVariable("businessTypeId") int businessTypeId) {
		return service.getBusinessTypeId(businessTypeId);
		
	}
	@GetMapping("viewall")
	public ResponseStructure vieAllBusinessType() {
		return service.vieAllBusinessType();
	}
	
	@PutMapping("update/{businessTypeId}")
	public ResponseStructure updateBusinessCategory(@PathVariable("businessTypeId") int businessTypeId,@RequestBody RequestModel model) {
		return service.update(businessTypeId,model);
	}
	
	
	@PutMapping("updatestatus/{businessTypeId}")
	public ResponseStructure updateBusinessCategoryStatus(@PathVariable("businessTypeId") int businessTypeId,@RequestBody RequestModel model) {
		return service.updateBusinessCategoryStatus(businessTypeId,model);
	}
	
	
	@GetMapping("viewall-active")
	public ResponseStructure viewAllBusinessTypeActive() {
		return service.viewAllBusinessTypeActive();
	}
	
}
