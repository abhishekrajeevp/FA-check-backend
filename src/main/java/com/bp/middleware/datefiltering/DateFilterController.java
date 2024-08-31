package com.bp.middleware.datefiltering;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

@RestController
@RequestMapping("/datefilter")
@CrossOrigin
public class DateFilterController {

	
	@Autowired
	private DateFilterService service;
	
	
	@PostMapping("/transaction")
	public ResponseStructure transactionDateFiltering(@RequestBody RequestModel model) {
		return service.transactionDateFiltering(model);
	}
	
	@PostMapping("/transaction-byEntity")
	public ResponseStructure transactionDateFilteringForEntity(@RequestBody RequestModel model) {
		return service.transactionDateFilteringForEntity(model);
	}
	
	@PostMapping("/manualpayment")
	public ResponseStructure manualPaymentDateFiltering(@RequestBody RequestModel model) {
		return service.manualPaymentDateFiltering(model);
	}
	
	@PostMapping("/manualpayment--byEntity")
	public ResponseStructure manualPaymentDateFilteringForEntity(@RequestBody RequestModel model) {
		return service.manualPaymentDateFilteringForEntity(model);
	}
	
	@PostMapping("/prepaid")
	public ResponseStructure prepaidDateFiltering(@RequestBody RequestModel model) {
		return service.prepaidDateFiltering(model);
	}
	
	@PostMapping("/prepaid-byEntity")
	public ResponseStructure prepaidDateFilteringForEntity(@RequestBody RequestModel model) {
		return service.prepaidDateFilteringForEntity(model);
	}
	
	
	@PostMapping("/postaid")
	public ResponseStructure postaidDateFiltering(@RequestBody RequestModel model) {
		return service.postaidDateFiltering(model);
	}
	
	@PostMapping("/postaid-byEntity")
	public ResponseStructure postaidDateFilteringForEntity(@RequestBody RequestModel model) {
		return service.postaidDateFilteringForEntity(model);
	}
	
	
	
	@PostMapping("/vendor-hits")
	public ResponseStructure vendorHitsDateFiltering(@RequestBody RequestModel model) {
		return service.vendorHitsDateFiltering(model);
	}
	
	
	@PostMapping("/vendordatefilter/byspecific-vendor")
	public ResponseStructure vendorHitsDateFilteringBySpecificVendor(@RequestBody RequestModel model) {
		return service.vendorHitsDateFilteringBySpecificVendor(model);
	}
	
	
	@PostMapping("/vendordatefilter/byspecific-vendorandVerification")
	public ResponseStructure vendorHitsDateFilteringBySpecificVendorAndVerification(@RequestBody RequestModel model) {
		return service.vendorHitsDateFilteringBySpecificVendorAndVerification(model);
	}
	
	@PostMapping("/merchant-hits")
	public ResponseStructure merchantHitsDateFiltering(@RequestBody RequestModel model) {
		return service.merchantHitsDateFiltering(model);
	}
	
	@PostMapping("/merchant-hits/forEntity")
	public ResponseStructure merchantHitsByParticularEntityDateFiltering(@RequestBody RequestModel model) {
		return service.merchantHitsByParticularEntityDateFiltering(model);
	}
	
	@PostMapping("/merchant-UAThits")
	public ResponseStructure merchantUatHits(@RequestBody RequestModel model) {
		return service.merchantUatHits(model);
	}
	
	
	@PostMapping("/merchant-UAThits/byParticularEntity")
	public ResponseStructure merchantUatHitsByParticularEntity(@RequestBody RequestModel model) {
		return service.merchantUatHitsByParticularEntity(model);
	}
	
}
