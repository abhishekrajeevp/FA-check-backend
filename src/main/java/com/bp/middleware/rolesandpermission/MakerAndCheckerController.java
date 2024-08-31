package com.bp.middleware.rolesandpermission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@RequestMapping("/makerchecker")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class MakerAndCheckerController {

	@Autowired
	private MakerAndCheckerService service;
	
	//ADMIN
	@PostMapping("/addmakerandchecker")
	public ResponseStructure addMakerAndChecker(@RequestBody RequestModel model) {
		return service.addMakerAndChecker(model);
	}
	
	@GetMapping("/viewbyadminid/{adminId}")
	public ResponseStructure viewByAdminId(@PathVariable("adminId")int adminId) {
		return service.viewByAdminId(adminId);
	}
	
	
	//USER
	@PostMapping("/addmakerandcheckerforuser")
	public ResponseStructure addMakerAndCheckerForUser(@RequestBody RequestModel model) {
		return service.addMakerAndCheckerForUser(model);
	}
	
	@GetMapping("/viewbyuserid/{userId}")
	public ResponseStructure viewByUserId(@PathVariable("userId")int userId) {
		return service.viewByUserId(userId);
	}
	
	
	//COMMON
	@GetMapping("/viewall")
	public ResponseStructure viewallMakeAndChecker() {
		return service.viewallMakeAndChecker();
	}
	
	@GetMapping("/viewbyid/{makerCheckerId}")
	public ResponseStructure viewByMakerCheckerId(@PathVariable("makerCheckerId")int makerCheckerId) {
		return service.viewByMakerCheckerId(makerCheckerId);
	}
	
	@GetMapping("/viewbystatus/{status}")
	public ResponseStructure viewByStatus(@PathVariable("status")boolean status) {
		return service.viewByStatus(status);
	}
	
	@PutMapping("/updatestatus/{makerCheckerId}")
	public ResponseStructure changeStatus(@PathVariable("makerCheckerId")int makerCheckerId,@RequestBody RequestModel model ) {
	        return service.changeStatus(makerCheckerId,model);
	}
	
	@PutMapping("/update/{makerCheckerId}")
	public ResponseStructure update(@PathVariable("makerCheckerId")int makerCheckerId,@RequestBody RequestModel model ) {
	        return service.update(makerCheckerId,model);
	}
	
}
