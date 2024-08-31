package com.bp.middleware.businesscategory;

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
@RequestMapping("/businesscategory")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class BusinessCategoryController {
	
	@Autowired
	private BusinessCategoryService service;
	
	@PostMapping("/addcategory")
	public ResponseStructure addBusinessCategory(@RequestBody RequestModel model) {
		return service.addcategory(model);
		
	}
	@GetMapping("/getbyid/{businessCategoryId}")
	public ResponseStructure getByBusinessCategoryId(@PathVariable("businessCategoryId") int businessCategoryId) {
		return service.getByBusinessId(businessCategoryId);
		
	}
	
	@GetMapping("/viewall")
	public ResponseStructure viewAll() {
		return service.viewAll();
	}
	
	@PutMapping("update/{businessCategoryId}")
	public ResponseStructure updateBusinessCategory(@PathVariable("businessCategoryId") int businessCategoryId,@RequestBody RequestModel model) {
		return service.update(businessCategoryId,model);
	}
	
	@PutMapping("updatestatus/{businessCategoryId}")
	public ResponseStructure updateBusinessCategoryStatus(@PathVariable("businessCategoryId") int businessCategoryId,@RequestBody RequestModel model) {
		return service.updateBusinessCategoryStatus(businessCategoryId,model);
	}
	
	@GetMapping("/viewall-active")
	public ResponseStructure viewAllActive() {
		return service.viewAllActive();
	}

}
