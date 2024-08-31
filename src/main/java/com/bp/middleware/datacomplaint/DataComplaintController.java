package com.bp.middleware.datacomplaint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;


@RestController
@RequestMapping("/datacomplaint")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class DataComplaintController {

	
	@Autowired
	private DataCompaintService service;
	
	
	@PostMapping("/adddatacomplaint")
	public ResponseStructure addDataComplaint(@RequestBody RequestModel model) {
		return service.addDataComplaint(model);
	}
	
	@PostMapping("/datacomplaintresolve/{dataComplaintId}")
	public ResponseStructure resolveDataComplaint(@PathVariable("dataComplaintId")int dataComplaintId,@RequestBody RequestModel model) {
		return service.resolveDataComplaint(dataComplaintId,model);
	}
	
	@GetMapping("/viewall")
	public ResponseStructure viewAll() {
		return service.viewAll();
	}
	
	@GetMapping("/viewbyid/{dataComplaintId}")
	public ResponseStructure viewById(@PathVariable("dataComplaintId")int dataComplaintId) {
		return service.viewById(dataComplaintId);
	}
	
	@GetMapping("/viewbycomplaintstatus/{complaintActive}")
	public ResponseStructure viewByComplaintStatus(@PathVariable("complaintActive")boolean complaintActive) {
		return service.viewByComplaintStatus(complaintActive);
	}
	
	@GetMapping("/viewbyuser/{userId}")
	public ResponseStructure viewByUserId(@PathVariable("userId")int userId) {
		return service.viewByUserId(userId);
	}
	
}
