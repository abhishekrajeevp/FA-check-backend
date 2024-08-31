package com.bp.middleware.signdesk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.requestandresponse.VerificationService;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/facheck-signauthentication")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class SignDeskController {
	
	@Autowired
	private PanVerificationSignDesk panVerification;
	@Autowired
	private GstVerificationSignDesk gstVerification;
	@Autowired
	private CinVerificationSignDesk cinVerificationSignDesk;
	@Autowired
	private DinVerificationSignDesk dinVerificationSignDesk;
	@Autowired
	private DrivingLicenseVerificationSignDesk drivingLicenseVerification;
	@Autowired
	private MsmeVerification msmeVerification;
	@Autowired
	private PassportVerificationSignDesk passportVerification;
	@Autowired
	private RcVerificationSignDesk rcVerification;
	@Autowired
	private AadhaarGenerateOtpVerification aadhaarGenerateOtpVerification;
	@Autowired
	private AadhaarSubmitOtpVerification aadhaarSubmitOtpVerification;
	

	@PostMapping("/panverification")//Done
	public ResponseStructure panVerification(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return panVerification.panVerificationService(dto,servletRequest);	
	}
	
	@PostMapping("/gstverification")//Done
	public ResponseStructure gstVerification(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return gstVerification.gstVerificationService(dto,servletRequest);	
	}
	
	@PostMapping("/aadhaargenerateotp")//Done
	public ResponseStructure aadhaarGenerateOtpVerification(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return aadhaarGenerateOtpVerification.aadhaarGenerateOtpService(dto,servletRequest);	
	}
	
	@PostMapping("/aadhaarsubmitotp")//Done
	public ResponseStructure aadhaarSubmitOtpVerification(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return aadhaarSubmitOtpVerification.aadhaarSubmitOtpService(dto,servletRequest);	
	}
	
	@PostMapping("/cinverification")//Done
	public ResponseStructure cinVerificationMethod(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return cinVerificationSignDesk.cinVerificationService(dto,servletRequest);	
	}
	
	@PostMapping("/dinverification")//Done
	public ResponseStructure dinVerification(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return dinVerificationSignDesk.dinVerificationService(dto,servletRequest);	
	}
	
	@PostMapping("/msmeverification")//Done
	public ResponseStructure msmeVerification(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return msmeVerification.msmeVerificationService(dto,servletRequest);	
	}
	
	@PostMapping("/dlverification")//Done
	public ResponseStructure dlVerification(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return drivingLicenseVerification.dlVerificationService(dto,servletRequest);	
	}
	
	@PostMapping("/rcverification")//Done
	public ResponseStructure rcVerification(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return rcVerification.rcVerificationService(dto,servletRequest);	
	}
	
	@PostMapping("/passportverification")//Done
	public ResponseStructure passportVerification(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return passportVerification.passportVerificationService(dto,servletRequest);	
	}
}
