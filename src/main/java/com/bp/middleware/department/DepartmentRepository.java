package com.bp.middleware.department;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<DepartmentModel, Integer>{

	DepartmentModel findByDepartmentId(int departmentId);

}
