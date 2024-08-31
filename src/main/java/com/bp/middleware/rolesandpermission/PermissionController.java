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
@RequestMapping("/permission")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class PermissionController {

	@Autowired
	private PermissionService service;
	
	//ADMIN
	@PostMapping("/addpermission")
	public ResponseStructure addPermission(@RequestBody RequestModel model) {
		return service.addPermission(model);
	}
	
	@GetMapping("/viewbyadminid/{adminId}")
	public ResponseStructure viewByAdminId(@PathVariable("adminId")int adminId) {
		return service.viewByAdminId(adminId);
	}
	
	//USER
	@PostMapping("/addpermissionforuser")
	public ResponseStructure addPermissionForUser(@RequestBody RequestModel model) {
		return service.addPermissionForUser(model);
	}
	
	@GetMapping("/viewbyuserid/{userId}")
	public ResponseStructure viewByUserId(@PathVariable("userId")int userId) {
		return service.viewByUserId(userId);
	}
	
	
	//COMMON
	@GetMapping("/viewbyid/{permissionId}")
	public ResponseStructure viewByPermissionId(@PathVariable("permissionId")int permissionId) {
		return service.viewByPermissionId(permissionId);
	}
	
	
	@GetMapping("/viewall")
	public ResponseStructure viewall() {
		return service.viewall();
	}
	
	@GetMapping("/viewbystatus/{status}")
	public ResponseStructure viewByStatus(@PathVariable("status")boolean status) {
		return service.viewByStatus(status);
	}
	
	@PutMapping("/updatestatus/{permissionId}")
	public ResponseStructure changeStatus(@PathVariable("permissionId")int permissionId,@RequestBody RequestModel model ) {
	        return service.changeStatus(permissionId,model);
	}
	
	
	@PutMapping("/update/{permissionId}")
	public ResponseStructure update(@PathVariable("permissionId")int permissionId,@RequestBody RequestModel model ) {
	        return service.update(permissionId,model);
	}
}
