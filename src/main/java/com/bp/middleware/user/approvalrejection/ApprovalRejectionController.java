package com.bp.middleware.user.approvalrejection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.util.AppConstants;

@RestController
@RequestMapping("/approverejection")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class ApprovalRejectionController {

	@Autowired
	private ApprovalRejectionService service;
	
	
	@GetMapping("/viewall")
	public ResponseStructure viewall() { 
		return service.viewall();
	}
	
	@GetMapping("/viewbyid/{approvalRejectionId}")
	public ResponseStructure viewById(@PathVariable("approvalRejectionId")int approvalRejectionId) {
		return service.viewById(approvalRejectionId);
	}
	
	@GetMapping("/admintotalapprovedorrejectedcount/{adminId}/{approvedStatus}")
	public ResponseStructure adminTotalApprovedOrRejectedCount(@PathVariable("adminId")int adminId,@PathVariable("approvedStatus")boolean approvedStatus) {
		return service.adminTotalApprovedOrRejectedCount(adminId,approvedStatus);
	}
	
	@GetMapping("/adminrejectedcountforanentity/{adminId}/{userId}")
	public ResponseStructure adminRejectedCountForAnEntity(@PathVariable("adminId")int adminId,@PathVariable("userId")int userId) {
		return service.adminRejectedCountForAnEntity(adminId,userId);
	}
}
