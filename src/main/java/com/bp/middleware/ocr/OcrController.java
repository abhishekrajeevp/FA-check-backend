package com.bp.middleware.ocr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.util.AppConstants;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/facheck-ocr")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class OcrController {

	@Autowired
	private AadhaarOcrService aadhaarOcrService;
	@Autowired
	private PanOcrService panOcrService;
	@Autowired
	private VoterIdOcrService voterIdOcrService;
	@Autowired
	private ChequeOcrService chequeOcrService;
	@Autowired
	private LicenseOcrService licenseOcrService;
	@Autowired
	private PassportOcrService passportOcrService;
	@Autowired
	private GstOcrService gstOcrService;
	@Autowired
	private CinOcrService cinOcrService;
	@Autowired
	private ItrOcrService itrOcrService;
	@Autowired
	private DocumentDetectOcrService documentDetectOcrService;
	@Autowired
	private InternationalPassportOcrService internationalPassportOcrService;
	
	@PostMapping("/aadhaar")
	public ResponseStructure aadhaarOcr(@RequestParam("file")MultipartFile file,HttpServletRequest servletRequest) {

		return aadhaarOcrService.aadhaarOcr(file,servletRequest);
	}
	
	@PostMapping("/pan")
	public ResponseStructure panOcr(@RequestParam("file")MultipartFile file,HttpServletRequest servletRequest) {
		return panOcrService.panOcr(file,servletRequest);
	}
	
	@PostMapping("/voterid")
	public ResponseStructure voterIdOcr(@RequestParam("file")MultipartFile file,HttpServletRequest servletRequest) {
		return voterIdOcrService.voterIdOcr(file,servletRequest);
	}
	
	@PostMapping("/cheque")
	public ResponseStructure chequeOcr(@RequestParam("file")MultipartFile file,HttpServletRequest servletRequest) {
		return chequeOcrService.chequeOcr(file,servletRequest);
	}
	
	@PostMapping("/license")
	public ResponseStructure licenseOcr(@RequestParam("front")MultipartFile front,@RequestParam("back")MultipartFile back,HttpServletRequest servletRequest) {
		return licenseOcrService.licenseOcr(front,back,servletRequest);
	}
	
	@PostMapping("/passport")
	public ResponseStructure passportOcr(@RequestParam("file")MultipartFile file,HttpServletRequest servletRequest) {
		return passportOcrService.passportOcr(file,servletRequest);
	}
	
	@PostMapping("/gst")
	public ResponseStructure gstOcr(@RequestParam("file")MultipartFile file,HttpServletRequest servletRequest) {
		return gstOcrService.gstOcr(file,servletRequest);
	}
	
	@PostMapping("/cin")
	public ResponseStructure cinOcr(@RequestParam("file")MultipartFile file,HttpServletRequest servletRequest) {
		return cinOcrService.cinOcr(file,servletRequest);
	}
	
	@PostMapping("/documentdetect")
	public ResponseStructure documentDetectOcr(@RequestParam("file")MultipartFile file,HttpServletRequest servletRequest) {
		return documentDetectOcrService.documentDetectOcr(file,servletRequest);
	}
	
	@PostMapping("/internationalpassport")
	public ResponseStructure internationalPassportOcr(@RequestParam("file")MultipartFile file,HttpServletRequest servletRequest) {
		return internationalPassportOcrService.internationalPassportOcr(file,servletRequest);
	}
	
	@PostMapping("/itr")
	public ResponseStructure itrOcr(@RequestParam("pdf")String pdf,@RequestParam("file")MultipartFile file,HttpServletRequest servletRequest) {
		return itrOcrService.itrOcr(file,pdf,servletRequest);
	}
}
