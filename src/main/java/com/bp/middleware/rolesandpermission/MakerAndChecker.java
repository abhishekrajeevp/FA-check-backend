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
@Table(name="maker_and_checker")
public class MakerAndChecker {


	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="maker_checker_id")
	private int makerCheckerId;
	
	@Column(name="role_name")
	private String makerCheckerRoleName;
	@Column(name="maker_checker_role_code")
	private String makerCheckeRoleCode;
	@Column(name="remarks")
	private String remarks;
	@Column(name="maker_checker_role_type")
	private String makerCheckerRoleType;
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


	public int getMakerCheckerId() {
		return makerCheckerId;
	}


	public void setMakerCheckerId(int makerCheckerId) {
		this.makerCheckerId = makerCheckerId;
	}


	public String getMakerCheckerRoleName() {
		return makerCheckerRoleName;
	}


	public void setMakerCheckerRoleName(String makerCheckerRoleName) {
		this.makerCheckerRoleName = makerCheckerRoleName;
	}


	public String getMakerCheckeRoleCode() {
		return makerCheckeRoleCode;
	}


	public void setMakerCheckeRoleCode(String makerCheckeRoleCode) {
		this.makerCheckeRoleCode = makerCheckeRoleCode;
	}


	public String getRemarks() {
		return remarks;
	}


	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}


	public String getMakerCheckerRoleType() {
		return makerCheckerRoleType;
	}


	public void setMakerCheckerRoleType(String makerCheckerRoleType) {
		this.makerCheckerRoleType = makerCheckerRoleType;
	}

	public AdminDto getAdminModel() {
		return adminModel;
	}


	public void setAdminModel(AdminDto adminModel) {
		this.adminModel = adminModel;
	}


	public boolean isStatus() {
		return status;
	}


	public void setStatus(boolean status) {
		this.status = status;
	}


	public EntityModel getUser() {
		return user;
	}


	public void setUser(EntityModel user) {
		this.user = user;
	}




	
}
