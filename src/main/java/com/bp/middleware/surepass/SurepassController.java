package com.bp.middleware.surepass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/facheck-sureverification")
@CrossOrigin(origins = { AppConstants.CROSS_ORIGIN })
public class SurepassController {

	@Autowired
	private AadhaarGenerateOtpService aadhaarGenerateOtpService;
	@Autowired
	private AadhaarSubmitOtpService aadhaarSubmitOtpService;
	@Autowired
	private PanService panService;
	@Autowired
	private PanComprehensiveService panComprehensiveService;
	@Autowired
	private GstService gstService;
	@Autowired
	private DrivingLicenseService drivingLicenseService;
	@Autowired
	private RcService rcService;
	@Autowired
	private VoterIdService voterIdService;
	@Autowired
	private CinService cinService;
	@Autowired
	private DinService dinService;
	@Autowired
	private UdyamAadhaarService udyamAadhaarService;
	@Autowired
	private EmailCheckService emailCheckService;
	@Autowired
	private ItrCompilanceService itrCompilanceService;
	@Autowired
	private PassportService passportService;
	@Autowired
	private FssaiService fssaiService;
	@Autowired
	private UpiService upiService;
	@Autowired
	private BankVerificationService bankVerificationService;
	@Autowired
	private BankVerificationPennylessService bankVerificationPennylessService;
	@Autowired
	private AadhaarToUan aadhaarToUanService;
	@Autowired
	private MobileToUan mobileToUanService;
	@Autowired
	private PanToUan panToUanService;
	@Autowired
	private EmploymentDetailsService employmentDetailsService;
	@Autowired
	private DirectorMobile directorMobileService;
	@Autowired
	private CharteredAccountantIndia charteredAccountantIndia;
	@Autowired
	private InternationalElectrotechnicalCommission internationalElectrotechnicalCommission;
	@Autowired
	private IecAdvanced iecAdvanced;
	@Autowired
	private EmployeeStateInsurance employeeStateInsurance;
	@Autowired
	private  EsicAdvanced esicAdvanced;
	@Autowired
	private  DirectAadhaarService directAadhaarService;
	
	
	@PostMapping("/directaadhaar")
	public ResponseStructure directAadhaar(@RequestBody RequestModel model, HttpServletRequest servletRequest) {
		return directAadhaarService.directAadhaar(model, servletRequest);
	}
	
	@PostMapping("/aadhaargenerateotp")
	public ResponseStructure aadhaarGenerateOtp(@RequestBody RequestModel model, HttpServletRequest servletRequest) {
		return aadhaarGenerateOtpService.aadhaarGenerateOtp(model, servletRequest);
	}

	@PostMapping("/aadhaarotpsubmit")
	public ResponseStructure aadhaarOtpSubmit(@RequestBody RequestModel model, HttpServletRequest servletRequest) {
		return aadhaarSubmitOtpService.aadhaarOtpSubmit(model, servletRequest);
	}

	@PostMapping("/pan")
	public ResponseStructure pan(@RequestBody RequestModel model, HttpServletRequest servletRequest) {
		return panService.pan(model, servletRequest);
	}
	
	@PostMapping("/pan-comprehensive")
	public ResponseStructure panComprehensive(@RequestBody RequestModel model, HttpServletRequest servletRequest) {
		return panComprehensiveService.panComprehensive(model, servletRequest);
	}

	@PostMapping("/gst")
	public ResponseStructure gst(@RequestBody RequestModel model, HttpServletRequest servletRequest) {
		return gstService.gst(model, servletRequest);
	}

	@PostMapping("/dl")
	public ResponseStructure drivingLicense(@RequestBody RequestModel model, HttpServletRequest servletRequest) {
		return drivingLicenseService.drivingLicense(model, servletRequest);
	}
	
	@PostMapping("/rc")
	public ResponseStructure registrationCertificate(@RequestBody RequestModel model, HttpServletRequest servletRequest) {
		return rcService.registrationCertificate(model, servletRequest);
	}
	
	@PostMapping("/voterId")
	public ResponseStructure voterId(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return voterIdService.voterId(model,servletRequest);
	}
	
	@PostMapping("/cin")
	public ResponseStructure companyIdentifyNumber(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return cinService.companyIdentifyNumber(model,servletRequest);
	}
	
	@PostMapping("/din")
	public ResponseStructure directorIdentifyNumber(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return dinService.directorIdentifyNumber(model,servletRequest);
	}
	
	@PostMapping("/udyamaadhaar")
	public ResponseStructure udyamAadhaar(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return udyamAadhaarService.udyamAadhaar(model,servletRequest);
	}
	
	@PostMapping("/emailcheck")
	public ResponseStructure emailCheck(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return emailCheckService.emailCheck(model,servletRequest);
	}
	
	@PostMapping("/itrcompilance-check")
	public ResponseStructure itrCompilanceCheck(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return itrCompilanceService.itrCompilanceCheck(model,servletRequest);
	}
	
	@PostMapping("/passport")
	public ResponseStructure passport(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return passportService.passport(model,servletRequest);
	}
	
	@PostMapping("/fssai")
	public ResponseStructure foodSafetyAndStandards(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return fssaiService.foodSafetyAndStandards(model,servletRequest);
	}
	
	@PostMapping("/upi")
	public ResponseStructure unifiedPaymentsInterface(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return upiService.unifiedPaymentsInterface(model,servletRequest);
	}
	
	@PostMapping("/bankVerify")
	public ResponseStructure bankVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return bankVerificationService.bankVerification(model,servletRequest);
	}
	
	@PostMapping("/bankVerify-pennyless")
	public ResponseStructure bankVerificationPennyless(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return bankVerificationPennylessService.bankVerificationPennyless(model,servletRequest);
	}
	
	@PostMapping("/aadhaar-to-uan")
	public ResponseStructure aadhaarToUan(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return aadhaarToUanService.aadhaarToUan(model,servletRequest);
	}
	
	@PostMapping("/mobile-to-uan")
	public ResponseStructure mobileToUan(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return mobileToUanService.mobileToUan(model,servletRequest);
	}
	
	@PostMapping("/pan-to-uan")
	public ResponseStructure panToUan(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return panToUanService.panToUan(model,servletRequest);
	}
	
	@PostMapping("/employmentdetails")
	public ResponseStructure employmentDetails(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return employmentDetailsService.employmentDetails(model,servletRequest);
	}
	
	@PostMapping("/directormobile")
	public ResponseStructure directorMobile(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return directorMobileService.directorMobile(model,servletRequest);
	}
	
	@PostMapping("/icai")
	public ResponseStructure charteredAccountant(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return charteredAccountantIndia.charteredAccountant(model,servletRequest);
	}
	
	@PostMapping("/iec")
	public ResponseStructure internationalElectroTechCommission(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return internationalElectrotechnicalCommission.internationalElectroTechCommission(model,servletRequest);
	}
	
	@PostMapping("/iec-advanced")
	public ResponseStructure iecAdvanced(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return iecAdvanced.iecAdvanced(model,servletRequest);
	}
	
	@PostMapping("/esic")
	public ResponseStructure employeeStateInsuranceVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return employeeStateInsurance.employeeStateInsuranceVerification(model,servletRequest);
	}
	
	@PostMapping("/esic-advanced")
	public ResponseStructure esicAdvancedVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return esicAdvanced.esicAdvancedVerification(model,servletRequest);
	}
}
