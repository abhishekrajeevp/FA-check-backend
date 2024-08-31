package com.bp.middleware.rolesandpermission;

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
@Table(name="permission_model")
public class PermissionModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="permission_id")
	private long permissionId;
	
	@Column(name="permission")
	private String permission;
	@Column(name="created_by")
	private String createdBy;
	@Column(name="created_date_time")
    private String createdDateTime;
	@Column(name="status")
	private boolean status;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "superadmin_id", nullable=true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private AdminDto adminModel;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable=true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel user;
	
	public long getPermissionId() {
		return permissionId;
	}
	public void setPermissionId(long permissionId) {
		this.permissionId = permissionId;
	}
	public String getPermission() {
		return permission;
	}
	public void setPermission(String permission) {
		this.permission = permission;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getCreatedDateTime() {
		return createdDateTime;
	}
	public void setCreatedDateTime(String createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	public AdminDto getAdminModel() {
		return adminModel;
	}
	public void setAdminModel(AdminDto adminModel) {
		this.adminModel = adminModel;
	}
	public EntityModel getUser() {
		return user;
	}
	public void setUser(EntityModel user) {
		this.user = user;
	}
	
	
}
