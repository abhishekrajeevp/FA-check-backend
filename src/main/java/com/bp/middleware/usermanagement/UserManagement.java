package com.bp.middleware.usermanagement;

import java.time.LocalDate;
import java.util.Date;

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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name="user_management")
public class UserManagement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="usermanagement_id")
	private int userManagementId;
	@Column(name="name")
	private String name;
	@Column(name="email")
	@Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}")
	private String email;
	@Column(name="address")
	private String address;
	@Transient
	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
	private String password;
	@Column(name="mobile_number")
	@Pattern(regexp="(^$|[0-9]{10})")
	private String mobileNumber;
	@Column(name="last_login")
	private Date lastLogin;
	@Column(name="ip_address")
	private String ipAddress;
	@Column(name="created_by")
	private String createdBy;
	@Column(name="created_date") 
	private Date createdDate;
	@Column(name="account_status")
	private boolean accountStatus; //Active/Inactive
	@Column(name="modified_by")
	private String modifyBy;
	@Column(name="modified_date")
	private Date modifyDate;
	@Column(name="login_failed_count")
	private int loginFailedCount;
	@Column(name="profile_picture")
	private String profilePhoto;
	@Column(name="otp_status")
	private boolean otpVerificationStatus; //success/Failure
	@Column(name="otp_code")
	private String otpCode;
	@Column(name="otp_exp_on")
	private Date otpExpiryOn;
	@Transient
	private int flag;
	@Column(name="usermanagement_flag")
	private int userManagementFlag;
	//LOG
	@Column(name="log_period")
	private int logPeriod;
	@Column(name="log_updatedate")
	private LocalDate logUpdatedAt;
	@Column(name="log_updatedby")
	private String logUpdatedBy;
	@Column(name="pan")
	private String pan;
	@Column(name="aadhaar_number")
	private String aadhaarNumber;
	@Column(name="role_name")
	private String roleName;
	
	
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel user;


	@Transient
	private Object jwtResponse;
	
	
	
	public Object getJwtResponse() {
		return jwtResponse;
	}


	public EntityModel getUser() {
		return user;
	}


	public void setUser(EntityModel user) {
		this.user = user;
	}


	public void setJwtResponse(Object jwtResponse) {
		this.jwtResponse = jwtResponse;
	}


	public int getUserManagementId() {
		return userManagementId;
	}


	public void setUserManagementId(int userManagementId) {
		this.userManagementId = userManagementId;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getMobileNumber() {
		return mobileNumber;
	}


	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}


	public Date getLastLogin() {
		return lastLogin;
	}


	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}


	public String getIpAddress() {
		return ipAddress;
	}


	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
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


	public boolean isAccountStatus() {
		return accountStatus;
	}


	public void setAccountStatus(boolean accountStatus) {
		this.accountStatus = accountStatus;
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


	public int getLoginFailedCount() {
		return loginFailedCount;
	}


	public void setLoginFailedCount(int loginFailedCount) {
		this.loginFailedCount = loginFailedCount;
	}


	public String getProfilePhoto() {
		return profilePhoto;
	}


	public void setProfilePhoto(String profilePhoto) {
		this.profilePhoto = profilePhoto;
	}


	public boolean isOtpVerificationStatus() {
		return otpVerificationStatus;
	}


	public void setOtpVerificationStatus(boolean otpVerificationStatus) {
		this.otpVerificationStatus = otpVerificationStatus;
	}


	public String getOtpCode() {
		return otpCode;
	}


	public void setOtpCode(String otpCode) {
		this.otpCode = otpCode;
	}


	public Date getOtpExpiryOn() {
		return otpExpiryOn;
	}


	public void setOtpExpiryOn(Date otpExpiryOn) {
		this.otpExpiryOn = otpExpiryOn;
	}


	public int getFlag() {
		return flag;
	}


	public void setFlag(int flag) {
		this.flag = flag;
	}


	public int getUserManagementFlag() {
		return userManagementFlag;
	}


	public void setUserManagementFlag(int userManagementFlag) {
		this.userManagementFlag = userManagementFlag;
	}


	public int getLogPeriod() {
		return logPeriod;
	}


	public void setLogPeriod(int logPeriod) {
		this.logPeriod = logPeriod;
	}


	public LocalDate getLogUpdatedAt() {
		return logUpdatedAt;
	}


	public void setLogUpdatedAt(LocalDate logUpdatedAt) {
		this.logUpdatedAt = logUpdatedAt;
	}


	public String getLogUpdatedBy() {
		return logUpdatedBy;
	}


	public void setLogUpdatedBy(String logUpdatedBy) {
		this.logUpdatedBy = logUpdatedBy;
	}


	public String getPan() {
		return pan;
	}


	public void setPan(String pan) {
		this.pan = pan;
	}


	public String getAadhaarNumber() {
		return aadhaarNumber;
	}


	public void setAadhaarNumber(String aadhaarNumber) {
		this.aadhaarNumber = aadhaarNumber;
	}



	public String getRoleName() {
		return roleName;
	}


	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	
	
}
