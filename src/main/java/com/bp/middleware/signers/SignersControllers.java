package com.bp.middleware.signers;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/signer")
//@CrossOrigin(origins = {AppConstants.CROSS_ORIGIN})
public class SignersControllers {

	@Autowired
	private SignerService service;
	
	@PostMapping("/add")
	public ResponseStructure addSigners(@RequestBody RequestModel model) {
		return service.addSigners(model);
	}
	
	@GetMapping("/viewall")
	public ResponseStructure viewAll() {
		return service.viewAllDetails();
	}
	
	@GetMapping("/viewbyid/{signerId}")
	public ResponseStructure viewSignerDetailById(@PathVariable("signerId") int signerId) {
		return service.viewById(signerId);
	}
	
	@GetMapping("/viewby-signerreference/{referenceId}")
	public ResponseStructure viewBySignerReference(@PathVariable("referenceId") String referenceId) {
		return service.viewBySignerReference(referenceId);
	}
	
	@PutMapping("/update/{signerId}")
	public ResponseStructure updateSigner(@RequestBody RequestModel model,@PathVariable("signerId") int signerId) {
		return service.updateDetails(model,signerId);
	}
	
	
	@PutMapping("/signerExpire/{signerId}")
	public ResponseStructure signerExpire(@RequestBody RequestModel model,@PathVariable("signerId") int signerId) {
		return service.signerExpire(model,signerId);
	}
	
	@GetMapping("/otpsent/{signerId}")
	public ResponseStructure sentOtp(@PathVariable("signerId") int signerId) {
		return service.sentOtp(signerId);
	}
	
	
	@PutMapping("/otpverify/{signerId}")
	public ResponseStructure verifyotp(@PathVariable("signerId") int signerId, @RequestBody RequestModel users,HttpServletRequest request) throws IOException, MessagingException, ParseException {
		return service.verifyotp(signerId, users,request);
	}
	
	
	@GetMapping("/emailtrigger/{merchantId}")
	public ResponseStructure emailTrigger(@PathVariable("merchantId") int merchantId) {
		return service.emailTrigger(merchantId);
	}
	
	
	@GetMapping("/signerbymerchant/{merchantId}")
	public ResponseStructure viewById(@PathVariable("merchantId") int merchantId) {
		return service.viewByMerechantId(merchantId);
	}
	
	
	@GetMapping("/signerByentity/{userId}")
	public ResponseStructure viewByUserId(@PathVariable("userId") int userId) {
		return service.viewByUserId(userId);
	}
	
	
	@GetMapping("/signerBymerchantandentity/{merchantId}/{userId}")
	public ResponseStructure viewByMerchantAndUser(@PathVariable("merchantId") int merchantId,@PathVariable("userId") int userId) {
		return service.viewByMerchantAndUser(merchantId,userId);
	}
	
	
	@GetMapping("/invite/{signerId}")
	public ResponseStructure inviteSigner(@PathVariable("signerId") int signerId) {
		return service.inviteSigner(signerId);
	}
	
	
	@DeleteMapping("/delete/{signerId}")
	public ResponseStructure deleteSigner(@PathVariable("signerId") int signerId) {
		return service.deleteSigner(signerId);
	}
	
	
	@GetMapping("/ipaddress")
	public String ipAddress(HttpServletRequest request) {
		String ip=request.getRemoteAddr();
		return ip;
	}
	
	
	@GetMapping("/imprtexcel-forsigners")
	public ResponseStructure importExcelForSigner() {
		return service.importExcelForSigner();
	}
	
	
	
	@PostMapping("/aadhaarSigning-aadhaarsubmit")
	public ResponseStructure aadhaarSigning(@RequestBody RequestModel model) {
		return service.aadhaarSigning(model);
	}
	
	@PostMapping("/aadhaarSigning-otpsubmit")
	public ResponseStructure aadhaarSigningOtpSubmit(@RequestBody RequestModel model,HttpServletRequest request) {
		return service.aadhaarSigningOtpSubmit(model,request);
	}
	
	
	@PostMapping("/signer-consent")
	public ResponseStructure signerConsent(@RequestBody RequestModel model) {
		return service.signerConsent(model);
	}
	
	
	@PostMapping("/signer-locationtracker")
	public ResponseStructure signerLocationTracker(@RequestBody RequestModel model) {
		return service.signerLocationTracker(model);
	}
	
}
