package com.bp.middleware.user.approvalrejection;


import java.time.LocalDate;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.user.EntityModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="approval_rejection_history")
public class ApprovalRejectionHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="approval_rejection_id")
	private int approvalRejectionId;
	
	@Column(name="approveReject")
	private String approveReject;
	@Column(name="rejection_count")
	private long rejectionCount;
	@Column(name="approved_status")
	private boolean approvedStatus;
	@Column(name="comments")
	private String comments;
	@Column(name="date")
	private LocalDate date;
	
	
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "user_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel user;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name="admin_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private AdminDto admin;

	public int getApprovalRejectionId() {
		return approvalRejectionId;
	}

	public void setApprovalRejectionId(int approvalRejectionId) {
		this.approvalRejectionId = approvalRejectionId;
	}

	public String getApproveReject() {
		return approveReject;
	}

	public void setApproveReject(String approveReject) {
		this.approveReject = approveReject;
	}

	public long getRejectionCount() {
		return rejectionCount;
	}

	public void setRejectionCount(long rejectionCount) {
		this.rejectionCount = rejectionCount;
	}

	public boolean isApprovedStatus() {
		return approvedStatus;
	}

	public void setApprovedStatus(boolean approvedStatus) {
		this.approvedStatus = approvedStatus;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public EntityModel getUser() {
		return user;
	}

	public void setUser(EntityModel user) {
		this.user = user;
	}

	public AdminDto getAdmin() {
		return admin;
	}

	public void setAdmin(AdminDto admin) {
		this.admin = admin;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	
	
}
