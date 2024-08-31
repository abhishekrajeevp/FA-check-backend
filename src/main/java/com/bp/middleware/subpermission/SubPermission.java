package com.bp.middleware.subpermission;

import java.time.LocalDate;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.rolesandpermission.PermissionModel;
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
@Table(name="sub_permission")
public class SubPermission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="subpermission_id")
	private int subPermissionId;
	
	@Column(name="subpermission_name")
	private String subPermissionName;
	@Column(name="status")
	private boolean status;
	@Column(name="created_by")
	private String createdBy;
	@Column(name="created_date")
	private LocalDate createdDate;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name="permission_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private PermissionModel permission;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="superadmin_id", nullable=true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private AdminDto admin;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable=true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel user;

	public int getSubPermissionId() {
		return subPermissionId;
	}

	public void setSubPermissionId(int subPermissionId) {
		this.subPermissionId = subPermissionId;
	}

	public String getSubPermissionName() {
		return subPermissionName;
	}

	public void setSubPermissionName(String subPermissionName) {
		this.subPermissionName = subPermissionName;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDate getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDate createdDate) {
		this.createdDate = createdDate;
	}

	public PermissionModel getPermission() {
		return permission;
	}

	public void setPermission(PermissionModel permission) {
		this.permission = permission;
	}

	public AdminDto getAdmin() {
		return admin;
	}

	public void setAdmin(AdminDto admin) {
		this.admin = admin;
	}

	public EntityModel getUser() {
		return user;
	}

	public void setUser(EntityModel user) {
		this.user = user;
	}
	
	
}
