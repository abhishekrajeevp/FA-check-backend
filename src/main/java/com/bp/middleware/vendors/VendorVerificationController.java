package com.bp.middleware.vendors;

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
@RequestMapping("/vendorverification")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class VendorVerificationController {

	@Autowired
	private VendorVerificationService service;
	
	@PostMapping("/addvendorverification")
	public ResponseStructure addVendorVerification(@RequestBody RequestModel model) {
		return service.addVendorVerification(model);
	}
	
	@GetMapping("/getvendorverify/{vendorVerifyId}")
	public ResponseStructure viewVendorVerification(@PathVariable("vendorVerifyId")int vendorVerifyId) {
		return service.viewVendorVerification(vendorVerifyId);
	}
	
	@GetMapping("/viewallvendorverify")
	public ResponseStructure viewAllVendorVerify() {
		return service.viewAllVendorVerify();
	}
	
	@GetMapping("/viewallvendorverify-bystatus/{status}")
	public ResponseStructure viewAllVendorVerificationsByStatus(@PathVariable("status")boolean status) {
		return service.viewAllVendorVerificationsByStatus(status);
	}
	
	@GetMapping("/activeverifications")
	public ResponseStructure activeVerifications() {
		return service.activeVerifications();
	}
	
	@PutMapping("/updatevendorverification")
	public ResponseStructure updateVendorVerification(@RequestBody RequestModel model) {
		return service.updateVendorVerification(model);
	}
	
	@PutMapping("/updatevendorverificationstatus")
	public ResponseStructure updateVendorVerificationStatus(@RequestBody RequestModel model) {
		return service.updateVendorVerificationStatus(model);
	}
}
