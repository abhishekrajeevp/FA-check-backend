package com.bp.middleware.prepaidpostpaid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
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

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/prepaid")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class PrepaidController {

	@Autowired
	private PrepaidService service;
	
	@PostMapping("/addprepaiddetails")
	public ResponseStructure addPrepaidDetails(@RequestBody RequestModel model) {
		return service.addPrepaidDetails(model);
	}
	
	@GetMapping("/getprepaiddetailsbyid/{prepaidId}")
	public ResponseStructure getPrepaidDetailsById(@PathVariable("prepaidId")int prepaidId) {
		return service.getPrepaidDetailsById(prepaidId);
	}
	
	@GetMapping("/getallprepaiddetails")
	public ResponseStructure getAllPrepaidDetails() {
		return service.getAllPrepaidDetails();
	}
	
	@GetMapping("/getprepaiddetailsforuserId/{userId}")
	public ResponseStructure getPrepaidDetailsForUserId(@PathVariable("userId")int userId) {
		return service.getPrepaidDetailsForUserId(userId);
	}
	
	@GetMapping("/lastrechargeddetailsforprepaiduser/{userId}")
	public ResponseStructure lastRechargedDetailsForPrepaidUser(@PathVariable("userId")int userId){
		return service.lastRechargedDetailsForPrepaidUser(userId);
	}
	
	@GetMapping("/viewprepaidreciept/{prepaidId}")
	public ResponseEntity<Resource> viewPrepaidReciept(@PathVariable("prepaidId") int prepaidId, HttpServletRequest request){
		return service.viewPrepaidReciept(prepaidId, request);
	}
	
	@GetMapping("/viewprepaidinvoice/{prepaidId}")
	public ResponseEntity<Resource> viewPrepaidInvoice(@PathVariable("prepaidId") int prepaidId, HttpServletRequest request){
		return service.viewPrepaidInvoice(prepaidId, request);
	}
	
	@GetMapping("/viewprepaidinvoice-A/{prepaidId}")
	public ResponseEntity<Resource> viewPrepaidInvoiceTwo(@PathVariable("prepaidId") int prepaidId, HttpServletRequest request){
		return service.viewPrepaidInvoiceTwo(prepaidId, request);
	}
	
	
	@GetMapping("/viewprepaidinvoice-B/{prepaidId}")
	public ResponseEntity<Resource> viewPrepaidInvoiceThree(@PathVariable("prepaidId") int prepaidId, HttpServletRequest request){
		return service.viewPrepaidInvoiceThree(prepaidId, request);
	}
	
	
	@GetMapping("/viewbynumberofdays/{noOfDays}")
	public ResponseStructure viewByNumberOfDays(@PathVariable("noOfDays") int noOfDays){
		return service.viewByNumberOfDays(noOfDays);
	}
	
	@PostMapping("/viewbymonth")
	public ResponseStructure viewByMonth(@RequestBody RequestModel model){
		return service.viewByMonth(model);
	}
	
	
	
	@GetMapping("/viewByUniqueId/{uniqueId}")
	public ResponseStructure viewByUniqueId(@PathVariable("uniqueId") String uniqueId) {
		
		return service.viewByUniqueId(uniqueId);
	}
	
	
	
	
}
