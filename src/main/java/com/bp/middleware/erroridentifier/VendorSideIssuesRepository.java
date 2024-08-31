package com.bp.middleware.erroridentifier;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VendorSideIssuesRepository extends JpaRepository<VendorSideIssues, Integer>{

	@Query(value = "SELECT * FROM vendor_side_issues order by vendor_side_issue_id desc",nativeQuery = true)
	List<VendorSideIssues> getVendorSidedIssue();

}
