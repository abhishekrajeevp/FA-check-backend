package com.bp.middleware.smartrouteverification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/facheck-authentication")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class SmartRouteController {

	@Autowired
	private PanVerification panService;
	@Autowired
	private GstVerification gstService;
	@Autowired
	private DrivingLicenseVerification drivingLicenseService;
	@Autowired
	private PassportVerification passportService;
	@Autowired
	private AadharWithOtpVerification aadharWithOtpService;
	@Autowired
	private AadharOtpSubmitVerification aadharOtpSubmitService;
	@Autowired
	private CinVerification cinVerificationService;
	@Autowired
	private DinVerification dinVerificationService;
	@Autowired
	private RcVerification rcVerificationService;
	@Autowired
	private UdyamAadharMsmeVerification udyamAadharMsmeVerificationService;
	@Autowired
	private PanImageVerification panImageVerificationService;
	@Autowired
	private GstImageVerification gstImageVerificationService;
	@Autowired
	private DrivingLicenseImageVerification drivingLicenseImageVerification;
	@Autowired
	private PassportImageVerification passportImageVerification;
	@Autowired
	private VoterIdVerification voterIdVerificationService;
	@Autowired
	private BankVerification1 bankVerification1;
	@Autowired
	private BankVerificationPennyless bankVerificationPennyless;
	@Autowired
	private AadhaarDirect aadhaarDirect;
	
	
	@PostMapping("/pan")//
	public ResponseStructure panVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return panService.panVerification(model,servletRequest);
	}
	
	@PostMapping("/gst")//
	public ResponseStructure gstVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return gstService.gstVerification(model,servletRequest);
	}
	
	@PostMapping("/driving-license")//
	public ResponseStructure drivingLicenseVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return drivingLicenseService.drivingLicenseVerification(model,servletRequest);
	}
	
	@PostMapping("/passport")//
	public ResponseStructure passportVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return passportService.passportVerification(model,servletRequest);
	}
	
	@PostMapping("/aadhaar/generate-otp")//
	public ResponseStructure aadharWithOtpVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return aadharWithOtpService.aadharWithOtpVerification(model,servletRequest);
	}
	
	@PostMapping("/aadhaar/submit-otp")//
	public ResponseStructure aadharOtpSubmitVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return aadharOtpSubmitService.aadharOtpSubmitVerification(model,servletRequest);
	}
	
	@PostMapping("/aadhaar")//
	public ResponseStructure aadhaarDirect(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return aadhaarDirect.aadhaarDirectVerification(model,servletRequest);
	}
	
	@PostMapping("/cin")//
	public ResponseStructure cinVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return cinVerificationService.cinVerification(model,servletRequest);
	}
	
	@PostMapping("/din")//
	public ResponseStructure dinVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return dinVerificationService.dinVerification(model,servletRequest);
	}
	
	@PostMapping("/rc")//
	public ResponseStructure rcVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return rcVerificationService.rcVerification(model,servletRequest);
	}
	
	@PostMapping("/udyamaadhaar-msme")//
	public ResponseStructure udyamAadharMsmeVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return udyamAadharMsmeVerificationService.udyamAadharMsmeVerification(model,servletRequest);
	}
	
	@PostMapping("/voterid")//
	public ResponseStructure voterIdVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return voterIdVerificationService.voterIdVerification(model,servletRequest);
	}
	
	@PostMapping("/bankverify1")//
	public ResponseStructure bankVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return bankVerification1.bankVerificationOne(model,servletRequest);
	}
	
	@PostMapping("/bank-pennyless")//
	public ResponseStructure bankVerificationPennyless(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return bankVerificationPennyless.bankPennylessVerification(model,servletRequest);
	}
	
	@PostMapping("/panOcr-advanced") //Pend -//
	public ResponseStructure panImageVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return panImageVerificationService.panImageVerification(model,servletRequest);
	}
	
	@PostMapping("/gst-image")//Pend -//
	public ResponseStructure gstImageVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return gstImageVerificationService.gstImageVerification(model,servletRequest);
	}
	
	@PostMapping("/dl-image")//Pend -//
	public ResponseStructure dlImageVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return drivingLicenseImageVerification.dlImageVerification(model,servletRequest);
	}
	
	@PostMapping("/passport-image")//Pend -//
	public ResponseStructure passportImageVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return passportImageVerification.passportImageVerification(model,servletRequest);
	}
}
