package com.bp.middleware.subpermission;

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


@RestController
@RequestMapping("/subpermission")
@CrossOrigin
public class SubPermissionController {

	
	@Autowired
	private SubPermissionService service;
	
	//ADMIN
	@PostMapping("/createsubpermission")
	public ResponseStructure addSubPermission(@RequestBody RequestModel model) {
	    return service.addSubPermission(model);
	}
	
	@GetMapping("/viewbyadmin/{adminId}")
	public ResponseStructure viewByAdminId(@PathVariable("adminId")int adminId) {
		return service.viewByAdminId(adminId);
	}
	
	@GetMapping("/viewbyadminandpermission/{adminId}/{permissionId}")
	public ResponseStructure viewByAdminIdAndPermissionId(@PathVariable("adminId")int adminId,@PathVariable("permissionId")int permissionId) {
		return service.viewByAdminIdAndPermissionId(adminId,permissionId);
	}
	
	
	//USER
	@PostMapping("/createsubpermissionforuser")
	public ResponseStructure addSubPermissionForUser(@RequestBody RequestModel model) {
	    return service.addSubPermissionForUser(model);
	}
	
	@GetMapping("/viewbyuserid/{userid}")
	public ResponseStructure viewByUserId(@PathVariable("userid")int userId) {
		return service.viewByUserId(userId);
	}
	
	@GetMapping("/viewbyuserandpermission/{userId}/{permissionId}")
	public ResponseStructure viewByUserIdIdAndPermissionId(@PathVariable("userId")int userId,@PathVariable("permissionId")int permissionId) {
		return service.viewByUserIdIdAndPermissionId(userId,permissionId);
	}
	
	
	//COMMON
	@GetMapping("/viewall")
	public ResponseStructure viewall() {
		return service.viewall();
	}
	
	@GetMapping("/viewbyid/{subPermissionId}")
	public ResponseStructure viewBySubPermissionId(@PathVariable("subPermissionId")int subPermissionId) {
		return service.viewBySubPermissionId(subPermissionId);
	}
	
	@GetMapping("/viewbystatus/{status}")
	public ResponseStructure viewByStatus(@PathVariable("status")boolean status) {
		return service.viewByStatus(status);
	}
	
	@GetMapping("/viewbypermission/{permissionId}")
	public ResponseStructure viewByPermissionId(@PathVariable("permissionId")int permissionId) {
		return service.viewByPermissionId(permissionId);
	}
	
	@PutMapping("/updatestatus/{subPermissionId}")
	public ResponseStructure changeStatus(@PathVariable("subPermissionId")int subPermissionId,@RequestBody RequestModel model ) {
	        return service.changeStatus(subPermissionId,model);
	}
	
	@PutMapping("/update/{subPermissionId}")
	public ResponseStructure update(@PathVariable("subPermissionId")int subPermissionId,@RequestBody RequestModel model ) {
	        return service.update(subPermissionId,model);
	}
}
