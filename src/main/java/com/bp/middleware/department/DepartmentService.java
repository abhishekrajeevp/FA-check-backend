package com.bp.middleware.department;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface DepartmentService {

	ResponseStructure addDepartment(RequestModel model);

	ResponseStructure listAll();

	ResponseStructure getByDepartmentId(int departmentId);

	ResponseStructure updateDepartment(int departmentId, RequestModel model);

	ResponseStructure updateStatus(int departmentId, RequestModel model);

}
