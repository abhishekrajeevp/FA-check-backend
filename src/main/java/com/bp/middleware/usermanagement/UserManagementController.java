package com.bp.middleware.usermanagement;

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

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/usermanagement")
@CrossOrigin
public class UserManagementController {

	@Autowired
	private UserManagementService service;
	
	@PostMapping("/create")
	public ResponseStructure createUserManagement(@RequestBody RequestModel model,HttpServletRequest servletRequest){
		return service.createUserManagement(model,servletRequest);
	}
	
	@PostMapping("/login")
	public ResponseStructure login(@RequestBody RequestModel model) {
		return service.login(model);
	}
	
	@PutMapping("/block-unblock/{userManagementId}/{blockUnblockStatus}")
	public ResponseStructure blockUnblock(@PathVariable("userManagementId") int userManagementId,@PathVariable("blockUnblockStatus") boolean blockUnblockStatus) {
		return service.blockUnblock(userManagementId,blockUnblockStatus);
	}
	
	@GetMapping("/viewbyid/{userManagementId}")
	public ResponseStructure viewById(@PathVariable("userManagementId") int userManagementId) {
		return service.viewById(userManagementId);
	}
	
	@GetMapping("/viewall")
	public ResponseStructure viewAll() {
		return service.viewAll();
	}
	
	
	//FORGOT PASSWOORD
	
	@GetMapping("/verifyEmail/{email}")
	public ResponseStructure emailVerify(@PathVariable("email") String email ) {
		return service.emailVerify(email);
	}
	
	@PutMapping("/otpverification/{userManagementId}")
	public ResponseStructure otpVerification(@PathVariable("userManagementId") int userManagementId,@RequestBody RequestModel model) {
		return service.verifyOtp(userManagementId,model);
	}
	
	@GetMapping("/resendotp/{userManagementId}")
	public ResponseStructure resendOtp(@PathVariable("userManagementId") int userManagementId) {
		return service.resendOtp(userManagementId);
	}
	
	@PostMapping("/reset")
	public ResponseStructure reset(@RequestBody RequestModel model) {
		return service.reset(model);
	}
	
	@PutMapping("/changepassword")
	public ResponseStructure changePassword(@RequestBody RequestModel model ) {
		return service.changePassword(model);
	}
	
	@PutMapping("/updateaccountstatus")
	public ResponseStructure changeAccountStatus(@RequestBody RequestModel model ) {
		return service.changeAccountStatus(model);
	}
	
	@GetMapping("/activeaccount/{accountStatus}")
	public ResponseStructure activeAccounts(@PathVariable("accountStatus") boolean accountStatus) {
		return service.activeAccounts(accountStatus);
	}
	
	@PutMapping("/update/{userManagementId}")
	public ResponseStructure updateDetails(@PathVariable("userManagementId") int userManagementId,@RequestBody RequestModel model) {
		return service.updateDetails(userManagementId,model);
	}
	
	@PostMapping("/uploadpicture")
	public ResponseStructure uploadPhoto(@RequestParam("userManagementId") int userManagementId,@RequestParam("profilePhoto") MultipartFile profilePhoto) {
		return service.uploadAdminProfilePicture(userManagementId, profilePhoto);
	}
	
	@GetMapping("/viewpicture/{userManagementId}")
	public ResponseEntity<Resource> viewImage(@PathVariable("userManagementId") int userManagementId, HttpServletRequest request){
		return service.viewImage(userManagementId, request);
	}
	
	@GetMapping("/viewbyuser/{userId}")
	public ResponseStructure viewByUser(@PathVariable("userId") int userId) {
		return service.viewByUser(userId);
	}
}
