package com.bp.middleware.bond;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@RestController
@RequestMapping("/bond")
@CrossOrigin(origins = {AppConstants.CROSS_ORIGIN})
public class BondDetailsController {
	
	@Autowired
	private BondDetailsService service;
	
	@PostMapping("/upload")
	public ResponseStructure addBondDetails(@RequestParam("sealedDate") String sealedDate,@RequestParam("bondId") int bondId,
			@RequestParam("document") MultipartFile document,@RequestParam("bondStatus") int bondStatus,@RequestParam("createdBy") String createdBy,
			@RequestParam("status") boolean status,@RequestParam("bondNumber") String bondNumber) {
		RequestModel model=new RequestModel();
		model.setSealedDate(sealedDate);
		model.setBondId(bondId);
		model.setDocument(document);
		model.setBondStatus(bondStatus);
		model.setCreatedBy(createdBy);
		model.setStatusFlag(status);
		model.setBondNumber(bondNumber);
		
		return service.uploadBondDetails(model);
	}
	
	@GetMapping("/getall/{bondId}")
	public ResponseStructure listAllUploadedBond(@PathVariable("bondId") int bondId) {
		return service.listAllUploadedBond(bondId);
	}
	@GetMapping("/getbysealdate")
	public ResponseStructure listBySealedDate(@RequestBody RequestModel model) {
		return service.getBondBySealedDate(model);
	}
	@GetMapping("getbybondnumber/{bondNumber}")
	public ResponseStructure listByBondNumber(@PathVariable("bondNumber") String bondNumber) {
		return service.listByBondNumber(bondNumber);
	}

}
