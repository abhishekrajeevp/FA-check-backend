package com.bp.middleware.prepaidpostpaid;

import java.text.ParseException;

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
@RequestMapping("/postpaid")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class PostpaidController {

	
	@Autowired
	private PostpaidService service;
	
	@PostMapping("/addpostpaiddetails")//NOT NECESSARY
	public ResponseStructure addPostpaidDetails(@RequestBody RequestModel model) {
		return service.addPostpaidDetails(model);
	}
	
	@GetMapping("/getpostpaiddetailsbyid/{postpaidId}")
	public ResponseStructure getPostpaidDetailsById(@PathVariable("postpaidId")int postpaidId) {
		return service.getPostpaidDetailsById(postpaidId);
	}
	
	@GetMapping("/getallpostpaiddetails")
	public ResponseStructure getAllPostpaidDetails() {
		return service.getAllPostpaidDetails();
	}
	
	@PutMapping("/updatepostpaid")
	public ResponseStructure updatePostpaidDetails(@RequestBody RequestModel model) {
		return service.updatePostpaidDetails(model);
	}
	
	@GetMapping("/totalhitandcount/{userId}")
	public ResponseStructure totalHitAndCount(@PathVariable("userId")int userId) throws ParseException {
		return service.totalHitAndCount(userId);
	}
	
	@GetMapping("/getpostpaiddetailsforuserId/{userId}")
	public ResponseStructure getPostpaidDetailsForUserId(@PathVariable("userId")int userId) {
		return service.getPostpaidDetailsForUserId(userId);
	}

	@GetMapping("/lastpaydetailsforpostpaiduser/{userId}")
	public ResponseStructure lastPayDetailsForPostpaidUser(@PathVariable("userId")int userId){
		return service.lastPayDetailsForPostpaidUser(userId);
	}
	
	@GetMapping("/getpostpaiddetailsofuseraftergeneratinginvoice/{userId}")
	public ResponseStructure getPostpaidDetailsOfUserAfterGeneratingInvoice(@PathVariable("userId")int userId) {
		return service.getPostpaidDetailsOfUserAfterGeneratingInvoice(userId);
	}
	
	@GetMapping("/viewpostpaidreciept/{postpaidId}")
	public ResponseEntity<Resource> viewPostpaidReciept(@PathVariable("postpaidId") int postpaidId, HttpServletRequest request){
		return service.viewPostpaidReciept(postpaidId, request);
	}
	
	@GetMapping("/viewpostpaidinvoice/{postpaidId}")
	public ResponseEntity<Resource> viewPostpaidInvoice(@PathVariable("postpaidId") int postpaidId, HttpServletRequest request){
		return service.viewPostpaidInvoice(postpaidId, request);
	}
	
	
	@GetMapping("/viewpostpaidinvoice-A/{postpaidId}")
	public ResponseEntity<Resource> viewPostpaidInvoiceOne(@PathVariable("postpaidId") int postpaidId, HttpServletRequest request){
		return service.viewPostpaidInvoiceOne(postpaidId, request);
	}
	
	
	@GetMapping("/viewpostpaidinvoice-B/{postpaidId}")
	public ResponseEntity<Resource> viewPostpaidInvoiceTwo(@PathVariable("postpaidId") int postpaidId, HttpServletRequest request){
		return service.viewPostpaidInvoiceTwo(postpaidId, request);
	}
	
	
	@GetMapping("/viewbynumberofdays/{noOfDays}")
	public ResponseStructure viewByNumberOfDays(@PathVariable("noOfDays") int noOfDays){
		return service.viewByNumberOfDays(noOfDays);
	}
	
	
	@GetMapping("/view-conveInvoice/{postpaidId}")
	public ResponseEntity<Resource> viewConveInvoice(@PathVariable("postpaidId") int postpaidId, HttpServletRequest request){
		return service.viewConveInvoice(postpaidId, request);
	}
	
	@GetMapping("/view-graceInvoice/{postpaidId}")
	public ResponseEntity<Resource> viewGraceInvoice(@PathVariable("postpaidId") int postpaidId, HttpServletRequest request){
		return service.viewGraceInvoice(postpaidId, request);
	}
	
	
	@PostMapping("/generate-graceInvoice")
	public ResponseStructure generateGraceInvoice(@RequestBody RequestModel model){
		return service.generateGraceInvoice(model);
	}
	
	
	@PostMapping("/force-invoiceGeneration")
	public ResponseStructure forceInvoiceGeneration(@RequestBody RequestModel model){
		return service.forceInvoiceGeneration(model);
	}
	
	
	
	
	@GetMapping("/viewByUniqueId/{uniqueId}")
	public ResponseStructure viewByUniqueId(@PathVariable("uniqueId") String uniqueId) {
		
		return service.viewByUniqueId(uniqueId);
	}
	
	
}
