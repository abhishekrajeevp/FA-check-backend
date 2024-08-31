package com.bp.middleware.erroridentifier;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

@RestController
@RequestMapping("/erroridentifier")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class ErrorIdentifierController {


	@Autowired
	private ErrorIdentifierService service; 

	@GetMapping("/viewall")
	public ResponseStructure viewAll() {
		return service.viewAll();
	}
	
	@GetMapping("/viewby/error-referenceid")
	public ResponseStructure viewByErrorReferenceId(@RequestBody RequestModel model) {
		return service.viewByErrorReferenceId(model);
	}
	
	
	
	@GetMapping("/viewall-vendorsideissues")
	public ResponseStructure viewAllVendorSideIssues() {
		return service.viewAllVendorSideIssues();
	}
	
	
	@GetMapping("/lastVendorIssue")
	public ResponseStructure lastVendorIssue() {
		return service.lastVendorIssue();
	}
}
