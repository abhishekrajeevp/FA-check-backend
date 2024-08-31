package com.bp.middleware.ticketraising;

import java.io.IOException;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/ticketraiser")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class TicketRaisingController {

	@Autowired
	private TicketRaisingService ticketRaisingService;
	

	
	@PostMapping("/save")//userId & ticketCategoryId
	public ResponseStructure saveTicket(@RequestParam("customerName") String customerName,@RequestParam("mobileNumber") String mobileNumber,
			@RequestParam("email") String email,@RequestParam("userType") String userType,@RequestParam("reason") String reason,
			@RequestParam("description") String description,@RequestParam("attachment") MultipartFile attachment,@RequestParam("userId") String userId
			,@RequestParam("ticketCategoryId") String ticketCategoryId,@RequestParam("remarks") String remarks) {
		
		RequestModel model=new RequestModel();
		
		int userIdConverted = Integer.parseInt(userId);
		int categoryIdConverted = Integer.parseInt(ticketCategoryId);
		
		model.setUserId(userIdConverted); //userId
		model.setTicketCategoryId(categoryIdConverted); //ticketCategoryId
		model.setCustomerName(customerName);
		model.setMobileNumber(mobileNumber);
		model.setEmail(email);
		model.setUserType(userType);
		model.setReason(reason);
		model.setDescription(description);
		model.setFileAttachment(attachment);
		model.setRemarks(remarks);
		
		return ticketRaisingService.saveTicket(model);
	}
	
	@GetMapping("/viewattachment/{ticketId}")//
	public ResponseEntity<Resource> getKycImage(@PathVariable("ticketId") int ticketId, HttpServletRequest request){
		return ticketRaisingService.viewAttachment(ticketId, request);
	}
	
	@GetMapping("/byreferencenumber")
	public ResponseStructure getByReferenceNumber(@RequestParam("referenceNumber") String referenceNumber) {
		return ticketRaisingService.getByReferenceNumber(referenceNumber);
	}
	
	@GetMapping("/byticketid/{ticketId}")
	public ResponseStructure getByTicketId(@PathVariable("ticketId") int ticketId) {
		return ticketRaisingService.getByTicketId(ticketId);
	}
	
	@PutMapping("/ticketverification/{ticketId}")
	public ResponseStructure ticketVerification(@PathVariable("ticketId") int ticketId,@RequestParam("status") boolean status,@RequestParam("reason") String reason 
			,@RequestParam("modifiedBy") String modifiedBy) {
		return ticketRaisingService.ticketVerificationChange(ticketId,status,reason,modifiedBy);
	}
	
	@PutMapping("/updateremarks/{ticketId}")
	public ResponseStructure updateRemarks(@PathVariable("ticketId") int ticketId,@RequestBody RequestModel model) {
		return ticketRaisingService.updateRemarks(ticketId,model);
	}
	
	
	@PutMapping("/updateattachment")
	public ResponseStructure updateAttachment(@RequestParam("ticketId") int ticketId,
			@RequestParam("attachment") MultipartFile attachment) throws IOException {
		return ticketRaisingService.updateAttachment(ticketId, attachment);
	}
    
	
	@GetMapping("/byuserid/{userId}")
	public ResponseStructure getByUserId(@PathVariable("userId") int userId) {
		return ticketRaisingService.getByUserId(userId);
	}
	
	@GetMapping("/getbystatus/{status}")
	public ResponseStructure getByTicketStatus(@PathVariable("status") boolean status) {
		return ticketRaisingService.getByTicketStatus(status);	
	}

	@PutMapping("/updateticketstatus")
	public ResponseStructure updateTicketStatus(@RequestBody RequestModel model ) {
		return ticketRaisingService.updateTicketStatus(model);
	}

    @GetMapping("/viewall")
    public ResponseStructure viewAllTicketRaiser() {
    	return ticketRaisingService.viewAllTicketRaiser();
    }



}
