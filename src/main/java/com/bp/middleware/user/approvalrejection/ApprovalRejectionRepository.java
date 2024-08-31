package com.bp.middleware.user.approvalrejection;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.user.EntityModel;

public interface ApprovalRejectionRepository extends JpaRepository<ApprovalRejectionHistory, Integer>{

	List<ApprovalRejectionHistory> findByAdminAndApprovedStatus(AdminDto admin, boolean approvedStatus);

	List<ApprovalRejectionHistory> findByAdminAndUserAndApprovedStatus(AdminDto admin, EntityModel user, boolean approvedStatus);

}
