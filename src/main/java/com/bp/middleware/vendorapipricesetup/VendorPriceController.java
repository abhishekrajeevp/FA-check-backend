package com.bp.middleware.vendorapipricesetup;

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
@RequestMapping("/vendorprice")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class VendorPriceController {

	
	@Autowired
	private VendorPriceService service;
	
	@PostMapping("/addvendorprice")
	public ResponseStructure addVendorPriceAndUrl(@RequestBody RequestModel model) {
		return service.addVendorPriceAndUrl(model);
	}
	
	@GetMapping("/viewbyid/{vendorPriceId}")
	public ResponseStructure viewByVendorPriceId(@PathVariable("vendorPriceId")int vendorPriceId) {
		return service.viewByVendorPriceId(vendorPriceId);
	}
	
	@GetMapping("/viewall")
	public ResponseStructure viewAllVendorPrice() {
		return service.viewAllVendorPrice();
	}
	
	@GetMapping("/viewbyvendor/{vendorId}")
	public ResponseStructure viewByVendorId(@PathVariable("vendorId")int vendorId) {
		return service.viewByVendorId(vendorId);
	}
	
	@GetMapping("/viewbyvendorverifictaion/{vendorVerificationId}")
	public ResponseStructure viewByVendorVerificationId(@PathVariable("vendorVerificationId")int vendorVerificationId) {
		return service.viewByVendorVerificationId(vendorVerificationId);
	}
	
	@GetMapping("/viewbystatus/{status}")
	public ResponseStructure viewByVendorPriceStatus(@PathVariable("status")boolean status) {
		return service.viewByVendorPriceStatus(status);
	}
	
	@GetMapping("/viewbyvendorandverification/{vendorId}/{verificationId}")
	public ResponseStructure viewByVendorIdAndVerificationId(@PathVariable("vendorId")int vendorId,@PathVariable("verificationId")int verificationId) {
		return service.viewByVendorIdAndVerificationId(vendorId,verificationId);
	}
	
	@PutMapping("/updatevendorpricedetails")
	public ResponseStructure updateVendorPriceOrUrl(@RequestBody RequestModel model) {
		return service.updateVendorPriceOrUrl(model);
	}
	
	@PutMapping("/updatevendorpricestatus")
	public ResponseStructure updateVendorPriceStatus(@RequestBody RequestModel model) {
		return service.updateVendorPriceStatus(model);
	}
	
	@PutMapping("/updateamount/{vendorPriceId}")
	public ResponseStructure updateAmount(@PathVariable("vendorPriceId")int vendorPriceId,@RequestBody RequestModel model) {
		return service.updateAmount(vendorPriceId,model);
	}
	
}
