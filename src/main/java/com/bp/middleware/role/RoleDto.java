package com.bp.middleware.role;

import java.util.Date;

import com.bp.middleware.department.DepartmentModel;
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
@Table(name="role_table")
public class RoleDto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="role_id")
	private int roleId;
	@Column(name="role_code")
	private int roleCode;
	@Column(name="role_name")
	private String roleName;
	@Column(name="role_status")
	private boolean roleStatus;
	@Column(name="create_by")
	private String createdBy;
	@Column(name="create_date")
	private Date createdDate;
	@Column(name="profile_picture")
	private String profilePicture;
	@Column(name="modify_by")
	private String modifyBy;
	@Column(name="modify_date")
	private Date modifyDate;
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "department_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private DepartmentModel department;
	
	public int getRoleId() {
		return roleId;
	}
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
	public int getRoleCode() {
		return roleCode;
	}
	public void setRoleCode(int roleCode) {
		this.roleCode = roleCode;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public boolean isRoleStatus() {
		return roleStatus;
	}
	public void setRoleStatus(boolean roleStatus) {
		this.roleStatus = roleStatus;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getProfilePicture() {
		return profilePicture;
	}
	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}
	public String getModifyBy() {
		return modifyBy;
	}
	public void setModifyBy(String modifyBy) {
		this.modifyBy = modifyBy;
	}
	public Date getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}
	public DepartmentModel getDepartment() {
		return department;
	}
	public void setDepartment(DepartmentModel department) {
		this.department = department;
	}
	
}
