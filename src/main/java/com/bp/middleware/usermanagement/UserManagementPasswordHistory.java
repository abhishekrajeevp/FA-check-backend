package com.bp.middleware.usermanagement;

import java.util.Date;

import com.bp.middleware.user.EntityModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="usermanagement_passwordhistory")
public class UserManagementPasswordHistory {

	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int passwordId;	
	private String userPassword;
	private String userSaltKey; 
	private int currentPasswordStatus;
	private Date modifiedDate;
	private String  reasonForChange;
	private String modifiedBy;
	private String ipaddress;
	private String reqDeviceType;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usermanagement_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private UserManagement userManagement;

	
	public int getPasswordId() {
		return passwordId;
	}

	public void setPasswordId(int passwordId) {
		this.passwordId = passwordId;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getUserSaltKey() {
		return userSaltKey;
	}

	public void setUserSaltKey(String userSaltKey) {
		this.userSaltKey = userSaltKey;
	}

	public int getCurrentPasswordStatus() {
		return currentPasswordStatus;
	}

	public void setCurrentPasswordStatus(int currentPasswordStatus) {
		this.currentPasswordStatus = currentPasswordStatus;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getReasonForChange() {
		return reasonForChange;
	}

	public void setReasonForChange(String reasonForChange) {
		this.reasonForChange = reasonForChange;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public String getReqDeviceType() {
		return reqDeviceType;
	}

	public void setReqDeviceType(String reqDeviceType) {
		this.reqDeviceType = reqDeviceType;
	}

	public UserManagement getUserManagement() {
		return userManagement;
	}

	public void setUserManagement(UserManagement userManagement) {
		this.userManagement = userManagement;
	}
	
	
}
