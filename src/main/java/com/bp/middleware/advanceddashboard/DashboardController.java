package com.bp.middleware.advanceddashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@RestController
@RequestMapping("/advdashboard")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class DashboardController {

	
	@Autowired
	private DashboardService service;
	
	@GetMapping("/allmerchantsuccessfailurecount")
	public ResponseStructure allMerchantSuccessFailureCount() {
		return service.allMerchantSuccessFailureCount();
	}
	
	@GetMapping("/allvendorsuccessfailurecount")
	public ResponseStructure allVendorSuccessFailureCount() {
		return service.allVendorSuccessFailureCount();
	}
	
	@GetMapping("/specificmerchantsuccessfailurecount/{userId}")
	public ResponseStructure specificMerchantSuccessFailureCount(@PathVariable("userId")int userId) {
		return service.specificMerchantSuccessFailureCount(userId);
	}
	
	@GetMapping("/specificvendorsuccessfailurecount/{vendorId}")
	public ResponseStructure specificVendorSuccessFailureCount(@PathVariable("vendorId")int vendorId) {
		return service.specificVendorSuccessFailureCount(vendorId);
	}
	
	//TO FILTER  BY ENTITY AND VERIFICATION CARD (WHOLE DATA)
	@GetMapping("/filterbyentityandverificationdocument/{userId}/{verificationId}")
	public ResponseStructure filterByEntityAndVerificationDocument(@PathVariable("userId")int userId,@PathVariable("verificationId")int verificationId) {
		return service.filterByEntityAndVerificationDocument(userId,verificationId);
	}
	
	
	
	//TO FILTER  BY ENTITY AND VERIFICATION CARD (COUNT)
	@GetMapping("/dashboardforentityandverificationdocument/{userId}/{verificationId}")
	public ResponseStructure dashboardForEntityAndVerificationDocument(@PathVariable("userId")int userId,@PathVariable("verificationId")int verificationId) {
		return service.dashboardForEntityAndVerificationDocument(userId,verificationId);
	}
	
	
	
	//MERCHANT DAYS FILTERING API's
	@GetMapping("/merchantdetailsbydays/{numberOfDays}")
	public ResponseStructure merchantDetailsByDays(@PathVariable("numberOfDays")int numberOfDays) { 
		return service.merchantDetailsByDays(numberOfDays);
	}
	
	
	@GetMapping("/merchantdetailsforlast-sevendays")
	public ResponseStructure merchantDetailsForLastSevenDays() { 
		return service.merchantDetailsForLastSevenDays();
	}
	
	@GetMapping("/merchantdetailsforlast-fifteendays")
	public ResponseStructure merchantDetailsForLastFifteenDays() { 
		return service.merchantDetailsForLastFifteenDays();
	}
	
	@GetMapping("/merchantdetailsforlast-thirtydays")
	public ResponseStructure merchantDetailsForLastThirtyDays() { 
		return service.merchantDetailsForLastThirtyDays();
	}
	
	@GetMapping("/merchantdetailsbetweentwodates/{startDate}/{endDate}")
	public ResponseStructure merchantDetailsBetweenTwoDates(@PathVariable("startDate")String startDate,@PathVariable("endDate")String endDate) { 
		return service.merchantDetailsBetweenTwoDates(startDate,endDate);
	}
	
	
	@PostMapping("/merchantdetailsbetweentwodates-assingledate")// 01/11/2025-02/12/2025
	public ResponseStructure merchantDetailsBetweenTwoDatesAsSingleDate(@RequestBody RequestModel model) { 
		return service.merchantDetailsBetweenTwoDatesAsSingleDate(model);
	}
	
	
	
	//VENDOR DAYS FILTERING API's
	@GetMapping("/vendordetailsbydays/{numberOfDays}")
	public ResponseStructure vendorDetailsByDays(@PathVariable("numberOfDays")int numberOfDays) { 
		return service.vendorDetailsByDays(numberOfDays);
	}
	
	@GetMapping("/vendordetailsforlast-sevendays")
	public ResponseStructure vendorDetailsForLastSevenDays() { 
		return service.vendorDetailsForLastSevenDays();
	}
	
	@GetMapping("/vendordetailsforlast-fifteendays")
	public ResponseStructure vendorDetailsForLastFifteenDays() { 
		return service.vendorDetailsForLastFifteenDays();
	}
	
	@GetMapping("/vendordetailsforlast-thirtydays")
	public ResponseStructure vendorDetailsForLastThirtyDays() { 
		return service.vendorDetailsForLastThirtyDays();
	}
	
	@GetMapping("/vendordetailsbetweentwodates/{startDate}/{endDate}")
	public ResponseStructure vendorDetailsBetweenTwoDates(@PathVariable("startDate")String startDate,@PathVariable("endDate")String endDate) { 
		return service.vendorDetailsBetweenTwoDates(startDate,endDate);
	}
	
	@PostMapping("/vendordetailsbetweentwodates-assingledate")// 01/11/2025-02/12/2025
	public ResponseStructure vendorDetailsBetweenTwoDatesAsSingleDate(@RequestBody RequestModel model) { 
		return service.vendorDetailsBetweenTwoDatesAsSingleDate(model);
	}
	
	////////// SIGNERS DASHBOARD
	
	@GetMapping("/overallsigners")
	public ResponseStructure overAllSigners() { 
		return service.overAllSigners();
	}
	
	
	@GetMapping("/signersbyentity/{userId}")
	public ResponseStructure signersByEntity(@PathVariable("userId")int userId) { 
		return service.signersByEntity(userId);
	}
	
	
	//MERCHANT AGREEMENT
	
	@GetMapping("/merchantagreement")
	public ResponseStructure overAllMerchantAgreement() { 
		return service.overAllMerchantAgreement();
	}
	
	@PostMapping("/merchantagreement-filterbetweendates")
	public ResponseStructure overAllMerchantAgreementFilterBetweenDates(@RequestBody RequestModel model) { 
		return service.overAllMerchantAgreementFilterBetweenDates(model);
	}
	
	@GetMapping("/merchantagreementforentity/{userId}")
	public ResponseStructure merchantAgreementByEntity(@PathVariable("userId")int userId) { 
		return service.merchantAgreementByEntity(userId);
	}
	
	@PostMapping("/merchantagreementforentity-filterbetweendates")
	public ResponseStructure merchantAgreementByEntityFilterBetweenDates(@RequestBody RequestModel model) { 
		return service.merchantAgreementByEntityFilterBetweenDates(model);
	}
	
}
