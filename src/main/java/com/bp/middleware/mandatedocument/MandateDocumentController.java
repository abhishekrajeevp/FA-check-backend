package com.bp.middleware.mandatedocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.bp.middleware.util.AppConstants;



@RestController
@RequestMapping(path = "/mandateDocuments")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class MandateDocumentController {
	
	@Autowired
	private MandateDocumentServices services;
	
	@PostMapping(path = "/addMandateDocDetails")
	public ResponseStructure addMandateDocDetails(@RequestBody RequestModel model){
		return services.addMandateDocDetails(model);
	}
	
	@GetMapping(path ="/viewMandateDocDetailsById/{mandatedocId}")
	public ResponseStructure viewMandateDocDetailsById(@PathVariable("mandatedocId") int id){
		return services.viewMandateDocDetailsById(id);
	}
	
	@GetMapping(path = "/viewAllMandateDoc")
	public ResponseStructure viewAllMandateDoc(){
		return services.viewAllMandateDoc();
	}
	
	@GetMapping(path = "/viewAllMandateDoc-bybusinesscategory/{businessCategoryId}")
	public ResponseStructure viewAllMandateDocByBusinessCategory(@PathVariable("businessCategoryId")int businessCategoryId){
		return services.viewAllMandateDocByBusinessCategory(businessCategoryId);
	}
	
	@PutMapping(path = "/updateMandateDocById/{mandatedocId}")
	public ResponseStructure updateMandateDoc(@PathVariable("mandatedocId") int id,@RequestBody RequestModel model){
		return services.updateMandateDoc(id,model);
	}
	
	
	@PutMapping(path = "/updateMandateDocStatus/{mandatedocId}")
	public ResponseStructure updateMandateDocStatus(@PathVariable("mandatedocId") int id,@RequestBody RequestModel model){
		return services.updateMandateDocStatus(id,model);
	}
	
	@DeleteMapping(path = "/delete/{mandatedocId}")
	public ResponseStructure deleteMandateDoc(@PathVariable("mandatedocId")int id){
		return services.deleteMandateDoc(id);
	}
}
