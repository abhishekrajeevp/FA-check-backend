package com.bp.middleware.requestandresponse;

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
import com.bp.middleware.util.CommonRequestDto;

@RestController
@RequestMapping("/verification")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class DirectVerificationController {

	
	@Autowired
	private DirectVerificationService service;
	
	@PostMapping("/panverification")
	public ResponseStructure panVerification(@RequestBody RequestModel dto ) {
		return service.verification(dto);
		
	}
	@PostMapping("/gstverification")
	public ResponseStructure gstVerification(@RequestBody RequestModel dto) {
		return service.gstVerification(dto);
		
	}

	@PostMapping("/aadharxmlverification")
	public ResponseStructure aadharXmlVerification(@RequestBody RequestModel dto) {
		return service.aadharXmlVerification(dto);
		
	}
	@PostMapping("/aadharotpsubmit")
	public ResponseStructure aadharotpVerification(@RequestBody RequestModel dto) {
		return service.aadhaarOtpVerification(dto);
		
	}

	@PostMapping("/cinverification")
	public ResponseStructure cinVerification(@RequestBody RequestModel dto ) {
		return service.cinVerification(dto);
		
	}
	
	@PostMapping("/dinverification")
	public ResponseStructure dinVerification(@RequestBody RequestModel dto ) {
		return service.dinVerification(dto);
		
	}
	
	@PostMapping("/msmeverification")
	public ResponseStructure msmeVerification(@RequestBody RequestModel dto ) {
		return service.msmeVerification(dto);
		
	}
	
	@PostMapping("/rcverification")
	public ResponseStructure rcVerification(@RequestBody RequestModel dto ) {
		return service.rcVerification(dto);
		
	}
	
	@PostMapping("/drivingliceneceid")
	public ResponseStructure drivingLicenceId(@RequestBody RequestModel dto ) {
		return service.drivingLicenceId(dto);
		
	}
	
	@PostMapping("/passportid")
	public ResponseStructure passportId(@RequestBody RequestModel dto ) {
		return service.passportId(dto);
		
	}
	
	
}
