package com.bp.middleware.admin;

import java.util.Date;
import java.util.List;

import com.bp.middleware.role.RoleDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name="admin_table")
public class AdminDto {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="admin_id")
	private int adminId;
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
	@JsonIgnore
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

	@OneToMany(mappedBy = "admin", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private List<AdminPasswordHistory> passwordHistory;

	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "role_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private RoleDto role;
//	
	public int getAdminId() {
		return adminId;
	}

	public void setAdminId(int adminId) {
		this.adminId = adminId;
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

	@JsonProperty(access=Access.WRITE_ONLY)
	public List<AdminPasswordHistory> getPasswordHistory() {
		return passwordHistory;
	}

	public void setPasswordHistory(List<AdminPasswordHistory> passwordHistory) {
		this.passwordHistory = passwordHistory;
	}	
	
	public RoleDto getRole() {
		return role;
	}
	public void setRole(RoleDto role) {
		this.role = role;
	}



	/* JWT Settings Starts Here */
	@Transient
	private Object jwtResponse; 

	public Object getJwtResponse() {
		return jwtResponse;
	}

	public void setJwtResponse(Object jwtResponse) {
		this.jwtResponse = jwtResponse;
	}
	/* JWT Settings Ends Here */

	




}
