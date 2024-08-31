package com.bp.middleware.role;

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
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/role")
@CrossOrigin(origins = {AppConstants.CROSS_ORIGIN})
public class RoleController {
	
	@Autowired
	private RoleService service;
	

	
	@PostMapping("/addroles")
	public ResponseStructure createRoles(@RequestBody RequestModel role) {
		return service.createRoles(role);
		
	}
	@GetMapping("/listall")
	public ResponseStructure getAllRoles() {
		return service.getAllRoles();
	}
	@GetMapping("/fetchbyid/{roleId}")
	public ResponseStructure getById(@PathVariable("roleId") int roleId) {
		return service.getById(roleId);
	}
	@PutMapping("/updateStatus")
	public ResponseStructure updateRoleStatus(@RequestBody RequestModel dto ) {
		return service.updateRoleStatus(dto);
		
	}
	@GetMapping("/activeaccount/{roleStatus}")
	public ResponseStructure activeAccounts(@PathVariable("roleStatus") boolean roleStatus) {
		return service.activeAccounts(roleStatus);	
	}
	@PutMapping("/update/{roleId}")
	public ResponseStructure updareRoleDetails(@PathVariable("roleId") int roleId,@RequestBody RequestModel entity) {
		return service.updateRoleDetails(entity,roleId);
	}
	@PostMapping("/uploadpicture")
	public ResponseStructure uploadLogo(@RequestParam("roleId") int roleId,@RequestParam("profilePicture") MultipartFile profilePicture) {
		return service.uploadAdminProfilePicture(roleId, profilePicture);
	}
	@GetMapping("/view/{roleId}")
	public ResponseEntity<Resource> getKycImage(@PathVariable("roleId") int roleId, HttpServletRequest request){
				return service.viewImage(roleId, request);
	}
	
}
