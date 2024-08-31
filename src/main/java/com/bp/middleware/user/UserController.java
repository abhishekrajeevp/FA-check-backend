package com.bp.middleware.user;

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
@RequestMapping("/user")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class UserController {

	@Autowired
	private UserService userService;
	
	@PostMapping("/create")
	public ResponseStructure createUser(@RequestBody RequestModel model,
			HttpServletRequest servletRequest){
		return userService.createUser(model,servletRequest);
	}
	
	@PostMapping("/login")
	public ResponseStructure login(@RequestBody RequestModel model) {
		return userService.login(model);
	}
	
	@GetMapping("/block-unblock/{userId}/{blockUnblockStatus}")
	public ResponseStructure blockUnblock(@PathVariable("userId") int userId,@PathVariable("blockUnblockStatus") boolean blockUnblockStatus) {
		return userService.blockUnblock(userId,blockUnblockStatus);
	}
	
	@GetMapping("/verifyEmail/{email}")
	public ResponseStructure emailVerify(@PathVariable("email") String email ) {
		return userService.emailVerify(email);
	}
	
	@PutMapping("/otpverification/{userId}")
	public ResponseStructure otpVerification(@PathVariable("userId") int userId,@RequestBody RequestModel model) {
		return userService.verifyotp(userId,model);
	}
	
	@GetMapping("/resendotp/{userId}")
	public ResponseStructure resendOtp(@PathVariable("userId") int userId) {
		return userService.resendOtp(userId);
	}
	
	@PostMapping("/reset")
	public ResponseStructure reset(@RequestBody RequestModel model) {
		return userService.reset(model);
	}
	
	@PutMapping("/changepassword")
	public ResponseStructure changePassword(@RequestBody RequestModel model ) {
		return userService.changePassword(model);

	}
	
	@PutMapping("/updateaccountstatus")
	public ResponseStructure changeAccountStatus(@RequestBody RequestModel model ) {
		return userService.changeAccountStatus(model);
	}
	
	
	@PutMapping("/update-bankaccountstatus")
	public ResponseStructure updateBankAccountStatus(@RequestBody RequestModel model ) {
		return userService.updateBankAccountStatus(model);
	}
	
	@PutMapping("/approvalStatus/{userId}")
	public ResponseStructure approvalStatus(@PathVariable("userId") int userId,@RequestBody RequestModel model ) {
		return userService.approvalStatus(userId,model);
	}
	
	@GetMapping("/activeaccount/{accountStatus}")
	public ResponseStructure activeAccounts(@PathVariable("accountStatus") boolean accountStatus) {
		return userService.activeAccounts(accountStatus);
	}
	
	
	@GetMapping("/findBySalt/{saltKey}")
	public ResponseStructure findBySalt(@PathVariable("saltKey") String saltKey) {
		return userService.findBySalt(saltKey);
	}
	
	@GetMapping("/userprofile/{userId}")
	public ResponseStructure adminProfile(@PathVariable("userId") int userId) {
		return userService.fetchById(userId);

	}
	
	@PutMapping("/update/{userId}")
	public ResponseStructure updateDetails(@PathVariable("userId") int userId,@RequestBody RequestModel model) {
		return userService.updateDetails(userId,model);
	}
	
	@PutMapping("/updateaddbankdetails/{userId}")
	public ResponseStructure updateBankDetails(@PathVariable("userId") int userId,@RequestBody RequestModel model) {
		return userService.updateBankDetails(userId,model);
	}
	
	@PostMapping("/uploadpicture")
	public ResponseStructure uploadPhoto(@RequestParam("userId") int userId,@RequestParam("profilePhoto") MultipartFile profilePhoto) {
		return userService.uploadAdminProfilePicture(userId, profilePhoto);
	}
	
	@GetMapping("/viewpicture/{userId}")
	public ResponseEntity<Resource> viewImage(@PathVariable("userId") int userId, HttpServletRequest request){
		return userService.viewImage(userId, request);
	}
	
	@GetMapping("/viewall")
	public ResponseStructure viewAll() {
		return userService.viewAllAdmin();
	}
	
	@GetMapping("/viewbysigningactive")
	public ResponseStructure viewBySigningActive() {
		return userService.viewBySigningActive();
	}
	
	@GetMapping("/viewbyverificationactive")
	public ResponseStructure viewByVerificationActive() {
		return userService.viewByVerificationActive();
	}
	
	@PutMapping("/livekeys-showorhide")
	public ResponseStructure liveKeysShowOrHide(@RequestBody RequestModel model ) {
		return userService.liveKeysShowOrHide(model);
	}
	
	@PutMapping("/verification-withoutrestriction")
	public ResponseStructure forVerificationWithoutRestriction(@RequestBody RequestModel model) {
		return userService.forVerificationWithoutRestriction(model);
	}
	
	
	
	///////////////////////////////////////////////
	@PostMapping("/encrypt")
	public ResponseStructure encrypt(@RequestParam("pic")MultipartFile pic,@RequestParam("password")String password) {
		return userService.encrypt(pic,password);
	}
	
	@PostMapping("/ecbencrypt")
	public ResponseStructure encryptECB(@RequestParam("photo")MultipartFile pic) {
		return userService.encryptECB(pic);
	}
	
	@PostMapping("/encryptbase64")
	public ResponseStructure encryptBase64(@RequestParam("photo")MultipartFile pic) {
		return userService.encryptBase64(pic);
	}
	 
	@PostMapping("/addamount")
	public ResponseStructure addAmount(@RequestBody RequestModel model) {
		return userService.addAmount(model);
	}
	
	
	
	
	
	
//	@PostMapping("/salt")
//	public String salt(@RequestParam("salt")String salt) {
//		return userService.salt(salt);
//	}
	
	@PutMapping("/logupdate/{userId}")
	public ResponseStructure updateLogDetails(@PathVariable("userId") int userId,@RequestBody RequestModel model) {
		return userService.updateLogDetails(userId,model);

	}
	
	
	@GetMapping("/historyaccordingtologperiod/{userId}")
	public ResponseStructure requestHistoryAccordingToLogPeriod(@PathVariable("userId") int userId) {
		return userService.requestHistoryAccordingToLogPeriod(userId);

	}
	
	//postpaidPrepaid
	
	@GetMapping("/getpostpaidprepaid/{paymentId}")
	public ResponseStructure getPrepaidPostpaidUsers(@PathVariable("paymentId") int paymentId) {
		return userService.getPrepaidPostpaidUsers(paymentId);

	}
	
	@GetMapping("/getpostpaiduserbyflag/{flagStatus}")
	public ResponseStructure getPostpaidUsersByFlag(@PathVariable("flagStatus") boolean flagStatus) {
		return userService.getPostpaidUsersByFlag(flagStatus);

	}
	
	@GetMapping("/getprepaidpostpaidcount/{paymentId}")
	public ResponseStructure getPrepaidPostpaidCount(@PathVariable("paymentId") int paymentId) {
		return userService.getPrepaidPostpaidCount(paymentId);

	}
	
	
	
	
}
