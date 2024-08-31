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
@RequestMapping("/action")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class ActionController {

	@Autowired
	private ActionService service;
	
	
	//ADMIN
	@PostMapping("/addactionforadmin")
	public ResponseStructure addActionForAdmin(@RequestBody RequestModel model) {
		return service.addActionForAdmin(model);
	}
	
	@GetMapping("/viewbysuperadminid/{superAdminId}")
	public ResponseStructure viewBySuperAdminId(@PathVariable("superAdminId")int superAdminId) {
		return service.viewBySuperAdminId(superAdminId);
	}
	
	@GetMapping("/viewbyadminid/{adminId}")
	public ResponseStructure viewByAdminId(@PathVariable("adminId")int adminId) {
		return service.viewByAdminId(adminId);
	}
	
	@PostMapping("/copyrolesandpermissionwithemail/{adminId}")
	public ResponseStructure copyRolesAndPermissionWithEmailForAdmin(@PathVariable("adminId")int adminId,@RequestBody RequestModel model) {
		return service.copyRolesAndPermissionWithEmail(adminId,model);
	}
	
	@GetMapping("/viewbyadminandpermission/{adminId}/{permissionId}")
	public ResponseStructure viewByAdminAndPermission(@PathVariable("adminId")int adminId,@PathVariable("permissionId")int permissionId) {
		return service.viewByAdminAndPermission(adminId,permissionId);
	}
	
	@GetMapping("/viewbyadminandmakerchecker/{adminId}/{makerId}")
	public ResponseStructure viewByAdminAndMakerChecker(@PathVariable("adminId")int adminId,@PathVariable("makerId")int makerId) {
		return service.viewByAdminAndMakerChecker(adminId,makerId);
	}

	@GetMapping("/viewbyadminandmakercheckerandpermission/{adminId}/{makerId}/{permissionId}")
	public ResponseStructure viewByAdminAndMakerCheckerAndPermission(@PathVariable("adminId")int adminId,@PathVariable("makerId")int makerId,@PathVariable("permissionId")int permissionId) {
		return service.viewByAdminAndMakerCheckerAndPermission(adminId,makerId,permissionId);
	}
	
	
	
	//USER
	@PostMapping("/addactionforuser")
	public ResponseStructure addActionForUser(@RequestBody RequestModel model) {
		return service.addActionForUser(model);
	}
	
	@GetMapping("/viewbyuserid/{userId}")
	public ResponseStructure viewByUserId(@PathVariable("userId")int userId) {
		return service.viewByUserId(userId);
	}
	
	@GetMapping("/viewbyusermanagement/{userManagementId}")
	public ResponseStructure viewByUserManagement(@PathVariable("userManagementId")int userManagementId) {
		return service.viewByUserManagement(userManagementId);
	}
	
	@PostMapping("/copyrolesandpermissionwithemailforusermanagement/{userManagementId}")
	public ResponseStructure copyRolesAndPermissionWithEmailForUserManagement(@PathVariable("userManagementId")int userManagementId,@RequestBody RequestModel model) {
		return service.copyRolesAndPermissionWithEmailForUserManagement(userManagementId,model);
	}
	

	
	//COMMON
	@GetMapping("/viewbyid/{actionId}")
	public ResponseStructure viewActionById(@PathVariable("actionId")int actionId) {
		return service.viewActionById(actionId);
	}
	
	@GetMapping("/viewall")
	public ResponseStructure viewall() {
		return service.viewall();
	}
	
	@GetMapping("/viewbystatus/{status}")
	public ResponseStructure viewByStatus(@PathVariable("status")boolean status) {
		return service.viewByStatus(status);
	}
	
	@GetMapping("/viewbypermissionid/{permissionId}")
	public ResponseStructure viewByPermissionId(@PathVariable("permissionId")int permissionId) {
		return service.viewByPermissionId(permissionId);
	}
	
	@GetMapping("/viewbysubpermissionid/{subPermissionId}")
	public ResponseStructure viewBySubPermissionId(@PathVariable("subPermissionId")int subPermissionId) {
		return service.viewBySubPermissionId(subPermissionId);
	}
	
	@GetMapping("/viewbymakerid/{makerId}")
	public ResponseStructure viewByMakerId(@PathVariable("makerId")int makerId) {
		return service.viewByMakerId(makerId);
	}
	
	@PutMapping("/updatestatus/{actionId}")
	public ResponseStructure changeStatus(@PathVariable("actionId")int actionId,@RequestBody RequestModel model ) {
	        return service.changeStatus(actionId,model);
	}
	
}
