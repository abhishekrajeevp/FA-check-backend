package com.bp.middleware.admin;

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
import com.bp.middleware.util.AppConstants;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class AdminController{

	@Autowired
	private AdminService adminService;
	
	@PostMapping("/createsuperadmin") // CREATE SUPER ADMIN /OK
	public ResponseEntity<ResponseStructure> createSuperAdmin(@RequestBody RequestModel entity,
			HttpServletRequest servletRequest){
		return adminService.createSuperAdmin(entity, servletRequest);
	}
	
	@PostMapping("/addadmin") // CREATE ADMIN /OK
	public ResponseEntity<ResponseStructure> createUser(@RequestBody RequestModel entity,
			HttpServletRequest servletRequest){
		return adminService.createUser(entity, servletRequest);
	}
	
	
	@PostMapping("/adminlogin") // ADMIN LOGIN
	public ResponseStructure adminLogin(@RequestBody RequestModel entity) {
		return adminService.login(entity);
	}
	
	@GetMapping("/block-unblock/{adminId}/{blockUnblockStatus}")
	public ResponseStructure blockUnblock(@PathVariable("adminId") int adminId,@PathVariable("blockUnblockStatus") boolean blockUnblockStatus) {
		return adminService.blockUnblock(adminId,blockUnblockStatus);
	}
	
	@GetMapping("/emailverify/{email}")
	public ResponseStructure verifyEmail(@PathVariable("email") String email) {
		return adminService.verifyEmail(email);
	}
	
	@PutMapping("/otpverify/{adminId}")
	public ResponseStructure verifyotp(@PathVariable("adminId") int adminId,@RequestBody RequestModel user) {
		return adminService.verifyotp(adminId,user);
	}

	@GetMapping("/resendotp/{adminId}")
	public ResponseStructure resendOtp(@PathVariable("adminId") int adminId) {
		return adminService.resndOtp(adminId);
	}
	
	@PostMapping("/reset")
	public ResponseStructure reset(@RequestBody RequestModel model) {
		return adminService.reset(model);
	}

	@PutMapping("/changepassword") 
	public ResponseStructure changeExsistingPassword(@RequestBody RequestModel model ) {
		return adminService.changePassword(model);
	}
	
	@PutMapping("/updateaccountstatus")
	public ResponseStructure accountStatusChange(@RequestBody RequestModel model ) {
		return adminService.accountStatusChange(model);
	}
	
	@GetMapping("/activeaccount/{accountStatus}")
	public ResponseStructure activeAccounts(@PathVariable("accountStatus") boolean accountStatus) {
		return adminService.activeAccounts(accountStatus);
	}
	
	@GetMapping("/adminprofile/{adminId}")
	public ResponseStructure getById(@PathVariable("adminId") int adminId) {
		return adminService.fetchById(adminId);
	}
	
	
	@PutMapping("/update/{adminId}")
	public ResponseStructure updateDetails(@PathVariable("adminId") int adminId,@RequestBody RequestModel model) {
		return adminService.updateDetails(adminId,model);
	}
	
	@PostMapping("/uploadpicture")
	public ResponseStructure uploadLogo(@RequestParam("adminId") int adminId,@RequestParam("profilePhoto") MultipartFile profilePhoto) {
		return adminService.uploadAdminProfilePicture(adminId, profilePhoto);
	}
	
	@GetMapping("/viewpicture/{adminId}")
	public ResponseEntity<Resource> getKycImage(@PathVariable("adminId") int adminId, HttpServletRequest request){
		return adminService.viewImage(adminId, request);
	}
	
	
	@GetMapping("/viewall")
	public ResponseStructure getAllAdmins() {
		return adminService.viewAllAdmin();
	}
	
	
	@GetMapping("/decode/{passwordId}")
	public ResponseStructure getDecodeValue(@PathVariable("passwordId") int passwordId) {
		return adminService.decodeValue(passwordId);
	}

}
