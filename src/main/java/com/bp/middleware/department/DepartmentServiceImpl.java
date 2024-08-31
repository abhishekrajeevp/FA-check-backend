package com.bp.middleware.department;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@Service
public class DepartmentServiceImpl implements DepartmentService{
	
	@Autowired
	private DepartmentRepository departmentRepository;

	@Override
	public ResponseStructure addDepartment(RequestModel model) {
		ResponseStructure structure=new ResponseStructure();
		try {
			DepartmentModel department=new DepartmentModel();
			
			department.setDepartmentName(model.getDepartmentName());
			department.setDepartmentHeadName(model.getDepartmentHeadName());
			department.setStatus(true);
			department.setCreatedBy(model.getCreatedBy());
			department.setCreatedAt(LocalDate.now());
			departmentRepository.save(department);
			
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Department Details added");
			structure.setData(department);
			structure.setFlag(1);
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseStructure listAll() {
		ResponseStructure structure=new ResponseStructure();
		try {
			List<DepartmentModel> list = departmentRepository.findAll();
			if (!list.isEmpty()) {
				List<DepartmentModel> bond =new ArrayList<>();
				for (DepartmentModel department : list) {
					if (department.isStatus()) {
						bond.add(department);
					}
				}
				structure.setMessage("Department List Details");
				structure.setData(bond);
				structure.setFlag(1);
			} else {
				structure.setMessage("Department Details Not Found");
				structure.setData(null);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure getByDepartmentId(int departmentId) {

		ResponseStructure structure=new ResponseStructure();
		
		DepartmentModel department = departmentRepository.findByDepartmentId(departmentId);
		if (department!=null) {
			structure.setMessage("Department List Details");
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(department);
			structure.setFlag(1);
		} else {
			structure.setMessage("Department Details Not Found");
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	
	}

	@Override
	public ResponseStructure updateDepartment(int departmentId, RequestModel model) {

		ResponseStructure structure=new ResponseStructure();
		try {
			DepartmentModel departmentModel = departmentRepository.findByDepartmentId(departmentId);
			if (departmentModel!=null) {
				departmentModel.setDepartmentName(model.getDepartmentName());
				departmentModel.setDepartmentHeadName(model.getDepartmentHeadName());
				departmentModel.setModifiedBy(model.getModifiedBy());
				departmentModel.setModifiedAt(LocalDate.now());
				
				departmentRepository.save(departmentModel);
				
				structure.setMessage("Department Details Updated Successfully...");
				structure.setData(departmentModel);
				structure.setFlag(1);
			} else {
				structure.setMessage("Department Details Not Found");
				structure.setData(null);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure updateStatus(int departmentId, RequestModel model) {
		ResponseStructure structure=new ResponseStructure();
		try {
			DepartmentModel departmentModel = departmentRepository.findByDepartmentId(departmentId);
			if (departmentModel!=null) {
				departmentModel.setStatus(model.isStatusFlag());
				
				departmentRepository.save(departmentModel);
				
				structure.setMessage("Department Details Updated Successfully...");
				structure.setData(departmentModel);
				structure.setFlag(1);
			} else {
				structure.setMessage("Department Details Not Found");
				structure.setData(null);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

}
