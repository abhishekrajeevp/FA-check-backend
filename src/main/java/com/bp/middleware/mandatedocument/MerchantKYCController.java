package com.bp.middleware.mandatedocument;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

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
import com.bp.middleware.util.CommonRequestDto;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping(path = "/merchantkycDocument")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class MerchantKYCController {

	@Autowired
	private MerchantKYCService service;
	
	
	@PostMapping(path = "/uploadMerchantKyc")
	public ResponseStructure uploadMerchantKyc(@RequestParam("userId") int userId,
			@RequestParam("fileType") String fileType, @RequestParam("cardNumber") String cardNumber,
			@RequestParam("frontPath") MultipartFile docFrontPath, @RequestParam("backPath") MultipartFile docBackPath,
			@RequestParam("docType") String docType, @RequestParam("createdBy") String createdBy,
			@RequestParam("id") long id) {
		
		CommonRequestDto model = new CommonRequestDto();
		model.setUserId(userId);
		model.setFileType(fileType);
		model.setCardNumber(cardNumber);
		model.setDocFrontPath(docFrontPath);
		model.setDocBackPath(docBackPath);
		model.setDocType(docType);
		model.setCreatedBy(createdBy);
		model.setId(id);
		
		return service.uploadMerchantKyc(model);
	}
	
	@GetMapping("/getViewDocument/{kycId}/{flag}")
	public ResponseEntity<Resource> getImageViewByMerchantId(@PathVariable("kycId") long id,
			@PathVariable("flag") int flag, HttpServletRequest request) throws IOException {
		return service.getParticularMerchantDocs(id, flag, request);
	}
	
	
	@GetMapping("/viewDocbyUserAndMandate/{userId}/{mandateDocId}/{flag}")
	public ResponseEntity<Resource> viewImageByUserAndMandateDoc(@PathVariable("userId") int userId,@PathVariable("mandateDocId") int mandateDocId,
			@PathVariable("flag") int flag, HttpServletRequest request) throws IOException {
		
		return service.viewImageByUserAndMandateDoc(userId,mandateDocId,flag, request);
	}
	
	@GetMapping("/getViewMerchantDocuments/{userId}")
	public ResponseStructure getViewMerchantDocument(@PathVariable int userId) {
		return service.getViewMerchantDocuments(userId);
	}
	
	@PutMapping("/updateapprovalstatus/{kycId}")
	public ResponseStructure adminApprovalStatus(@PathVariable("kycId") long id,
			@RequestBody CommonRequestDto dto) throws NoSuchAlgorithmException {
		return service.getAdminApproval(id, dto);
	}
	
	@PutMapping("/updateSuperAdminStatus/{kycId}")
	public ResponseStructure superAdminApprovalStatus(@PathVariable("kycId") long id,
			@RequestBody CommonRequestDto dto) throws NoSuchAlgorithmException {
		return service.getSuperAdminApproval(id, dto);
	}
	
	@PutMapping("/updateKycDoc/{kycId}")
	public ResponseStructure updateKycDoc(@PathVariable("kycId") long id,
			@RequestParam("userId") int userId, @RequestParam("fileType") String fileType,
			@RequestParam("cardNumber") String cardNumber, @RequestParam("frontPath") MultipartFile docFrontPath,
			@RequestParam("backPath") MultipartFile docBackPath, @RequestParam("docType") String docType,
			@RequestParam("modifiedBy") String modifiedBy) {
		CommonRequestDto dto = new CommonRequestDto();
		dto.setFileType(fileType);
		dto.setCardNumber(cardNumber);
		dto.setDocFrontPath(docFrontPath);
		dto.setDocBackPath(docBackPath);
		dto.setDocType(docType);
		dto.setModifiedBy(modifiedBy);
		dto.setUserId(userId);
		return service.updateKycDoc(id, dto);

	}
	
	@GetMapping("/getMerchantDocuments/{mandateDocId}/{userId}")
	public ResponseStructure view(@PathVariable("mandateDocId") long id,
			@PathVariable("userId") int userId) {
		return service.viewkyc(id, userId);
	}


}
