package com.bp.middleware.rolesandpermission;

import java.time.LocalDate;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.subpermission.SubPermission;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.usermanagement.UserManagement;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

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
@Table(name="action_model")
public class ActionModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="action_id")
	private long actionId;
	
	@Column(name="action")
	private String action;
	@Column(name="created_by")
	private String createdBy;
	@Column(name="created_at")
	private LocalDate createdAt;
	@Column(name="modified_by")
	private String modifiedBy;
	@Column(name="modified_date_time")
    private LocalDate modifiedAt;
	@Column(name="status")
	private boolean accountStatus;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name="makerchecker_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private MakerAndChecker checker;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name="permission_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private PermissionModel permissionModel;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name="subpermission_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private SubPermission subPermission;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "superadmin_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private AdminDto superAdmin;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private AdminDto adminModel;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usermanagement_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private UserManagement userManagement;

	public long getActionId() {
		return actionId;
	}

	public void setActionId(long actionId) {
		this.actionId = actionId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public boolean isAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(boolean accountStatus) {
		this.accountStatus = accountStatus;
	}

	public PermissionModel getPermissionModel() {
		return permissionModel;
	}

	public void setPermissionModel(PermissionModel permissionModel) {
		this.permissionModel = permissionModel;
	}

	
	public MakerAndChecker getChecker() {
		return checker;
	}

	public void setChecker(MakerAndChecker checker) {
		this.checker = checker;
	}

	public AdminDto getAdminModel() {
		return adminModel;
	}

	public void setAdminModel(AdminDto adminModel) {
		this.adminModel = adminModel;
	}

	public SubPermission getSubPermission() {
		return subPermission;
	}

	public void setSubPermission(SubPermission subPermission) {
		this.subPermission = subPermission;
	}

	public AdminDto getSuperAdmin() {
		return superAdmin;
	}

	public void setSuperAdmin(AdminDto superAdmin) {
		this.superAdmin = superAdmin;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDate getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(LocalDate modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public EntityModel getUser() {
		return user;
	}

	public void setUser(EntityModel user) {
		this.user = user;
	}

	public UserManagement getUserManagement() {
		return userManagement;
	}

	public void setUserManagement(UserManagement userManagement) {
		this.userManagement = userManagement;
	}
	
	
	
}
