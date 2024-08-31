package com.bp.middleware.merchantapipricesetup;

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
@RequestMapping("/merchantprice")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class MerchantPriceController {

	
	@Autowired
	private MerchantPriceService service;
	
	@PostMapping("/addmerchantprice")
	public ResponseStructure addMerchantPriceAndUrl(@RequestBody RequestModel model) {
		return service.addMerchantPriceAndUrl(model);
	}
	
	
	@PostMapping("/aadhaarAccessfor-signers")
	public ResponseStructure addMerchantPriceForAadhaarBasedSigning(@RequestBody RequestModel model) {
		return service.addMerchantPriceForAadhaarBasedSigning(model);
	}
	
	
	@PostMapping("/addmerchantpriceaslist")
	public ResponseStructure addMerchantPriceInList(@RequestBody RequestModel model) {
		return service.addMerchantPriceInList(model);
	}
	
	@GetMapping("/viewbyid/{merchantPriceId}")
	public ResponseStructure viewByMerchantPriceId(@PathVariable("merchantPriceId")int merchantPriceId) {
		return service.viewByMerchantPriceId(merchantPriceId);
	}
	
	@GetMapping("/viewall")
	public ResponseStructure viewAllMerchantPrice() {
		return service.viewAllMerchantPrice();
	}
	
	@GetMapping("/viewbyvendor/{vendorId}")
	public ResponseStructure viewByVendorId(@PathVariable("vendorId")int vendorId) {
		return service.viewByVendorId(vendorId);
	}
	
	@GetMapping("/viewbyuser/{userId}")
	public ResponseStructure viewByUserId(@PathVariable("userId")int userId) {
		return service.viewByUserId(userId);
	}
	
	@GetMapping("/viewAllforUser/{userId}")
	public ResponseStructure viewAllForUser(@PathVariable("userId")int userId) {
		return service.viewAllForUser(userId);
	}
	
	@GetMapping("/viewbyvendorverifictaion/{vendorVerificationId}")
	public ResponseStructure viewByVendorVerificationId(@PathVariable("vendorVerificationId")int vendorVerificationId) {
		return service.viewByVendorVerificationId(vendorVerificationId);
	}
	
	@GetMapping("/viewbystatus/{status}")
	public ResponseStructure viewByMerchantPriceStatus(@PathVariable("status")boolean status) {
		return service.viewByMerchantPriceStatus(status);
	}
	
	@GetMapping("/viewbyvendorandverificaton/{vendorId}/{verificationId}")
	public ResponseStructure viewByVendorIdAndVerificationId(@PathVariable("vendorId")int vendorId,@PathVariable("verificationId")int verificationId) {
		return service.viewByVendorIdAndVerificationId(vendorId,verificationId);
	}
	
	@PutMapping("/updatemerchantpricedetails")
	public ResponseStructure updateMerchantPriceOrUrl(@RequestBody RequestModel model) {
		return service.updateMerchantPriceOrUrl(model);
	}
	 
	
	@PutMapping("/updatemerchant-priority")
	public ResponseStructure updatePriority(@RequestBody RequestModel model) {
		return service.updatePriority(model);
	}
	
	
	@PutMapping("/updatemerchantpricestatus")
	public ResponseStructure updateMerchantPriceStatus(@RequestBody RequestModel model) {
		return service.updateMerchantPriceStatus(model);
	}
	
	@GetMapping("/viewbyvendorverifyuser/{vendorId}/{verificationId}/{userId}")
	public ResponseStructure viewByVendorIdAndVerificationIdAndUser(@PathVariable("vendorId")int vendorId,@PathVariable("verificationId")int verificationId,@PathVariable("userId")int userId) {
		return service.viewByVendorIdAndVerificationIdAndUser(vendorId,verificationId,userId);
	}
	
	@PutMapping("/updatemerchantpricestatusbyuserandverification/{userId}/{verificationId}")
	public ResponseStructure updateMerchantPriceStatusByUserAndVerification(@RequestBody RequestModel model,@PathVariable("userId")int userId,@PathVariable("verificationId")int verificationId) {
		return service.updateMerchantPriceStatusByUserAndVerification(model,userId,verificationId);
	}
	
	@PostMapping("/copymerchantprice")
	public ResponseStructure copyMerchantPriceFromOneUserToAnother(@RequestBody RequestModel model) {
		return service.copyMerchantPriceFromOneUserToAnother(model);
	}
	
	@GetMapping("/acceptNewPrice/{userId}")
	public ResponseStructure acceptNewPrice(@PathVariable("userId")int userId) {
		return service.acceptNewPrice(userId);
	}
	
	//------------
	
	@PutMapping("/updatesourcecheck/{userId}/{vendorVerificationId}")
	public ResponseStructure updateMerchantSourceCheck(@PathVariable("userId")int userId,@PathVariable("vendorVerificationId")int vendorVerificationId,@RequestBody RequestModel model) {
		return service.updateMerchantSourceCheck(userId,vendorVerificationId,model);
	}
	
	@GetMapping("/viewbyuserandvendorverification/{userId}/{vendorVerificationId}")
	public ResponseStructure viewByUserAndVerification(@PathVariable("userId")int userId,@PathVariable("vendorVerificationId")int vendorVerificationId) {
		return service.viewByUserAndVerification(userId,vendorVerificationId);
	}
	
	
	
	
	@GetMapping("/entityVerificationDropDown/{userId}")
	public ResponseStructure entityVerificationDropDown(@PathVariable("userId")int userId) {
		return service.entityVerificationDropDown(userId);
	}
	
	
}
