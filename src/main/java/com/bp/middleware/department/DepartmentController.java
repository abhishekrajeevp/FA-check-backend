package com.bp.middleware.department;

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
@RequestMapping("/department")
@CrossOrigin(origins = {AppConstants.CROSS_ORIGIN})
public class DepartmentController {
	
	@Autowired
	private DepartmentService service;
	
	@PostMapping("/add")
	public ResponseStructure addDepartment(@RequestBody RequestModel model) {
		return service.addDepartment(model);
	}
	@GetMapping("/getall")
	public ResponseStructure listAll() {
		return service.listAll();
	}
	@GetMapping("/getbyid/{departmentId}")
	public ResponseStructure getByDepartmentId(@PathVariable("departmentId") int departmentId) {
		return service.getByDepartmentId(departmentId);
	}
	@PutMapping("/update/{departmentId}")
	public ResponseStructure updateDepartment(@PathVariable("departmentId") int departmentId,@RequestBody RequestModel model) {
		return service.updateDepartment(departmentId,model);
	}
	@PutMapping("/updatestatus/{departmentId}")
	public ResponseStructure updateStatus(@PathVariable("departmentId") int departmentId,@RequestBody RequestModel model) {
	return service.updateStatus(departmentId,model);	
	}

}
