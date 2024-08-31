package com.bp.middleware.user;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.bp.middleware.businesscategory.BusinessCategory;
import com.bp.middleware.businesstype.BusinessType;
import com.bp.middleware.locations.CityModel;
import com.bp.middleware.locations.CountryModel;
import com.bp.middleware.locations.PincodeModel;
import com.bp.middleware.locations.StateModel;
import com.bp.middleware.mcccode.MCCCodesModel;
import com.bp.middleware.payment.PaymentMethod;
import com.bp.middleware.role.RoleDto;
import com.bp.middleware.util.FileUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
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
@Table(name="user_model")
public class EntityModel {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="user_id")
	private int userId;
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
	
	@JsonIgnore
	private String tempPass;
	
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
	@Column(name="consent_id")
	private String consentId;
	
	@Column(name="verification_required")
	private boolean verificationRequired;
	@Column(name="signing_required")
	private boolean signingRequired;
	@Column(name="aadhaar_based_signing",columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean aadhaarBasedSigning;
	@Column(name="rejected_count")
	private Long rejectedCount;
	@Column(name="approval_status")
	private String approvalStatus;
	
	@Column(name="general_Signing",columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean generalSigning;
	@Column(name="aadhaar_XML_price",columnDefinition = "DOUBLE DEFAULT 0")
	private double aadhaarXmlPrice;
	@Column(name="aadhaar_OTP_price",columnDefinition = "DOUBLE DEFAULT 0")
	private double aadhaarOtpPrice;
	
	//LOG
	@Column(name="log_period")
	private int logPeriod;
	@Column(name="log_updatedat")
	private LocalDate logUpdatedAt;
	@Column(name="log_updatedby")
	private String logUpdatedBy;
	
	//ADDITIONALLY ADDED
	@Column(name="pan")
	private String pan;
	@Column(name="gst")
	private String gst;
	@Column(name="contact_person_name")
	private String contactPersonName;
	@Column(name="contact_person_mobile")
	@Pattern(regexp="(^$|[0-9]{10})")
	private String contactPersonMobile;
	@Column(name="contact_person_email")
	@Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}")
	private String contactPersonEmail;
	
	@Column(name="public_url")
	private String publicUrl;
	
	//BANK
	@Column(name="account_number")
	private String accountNumber;
	@Column(name="ifsc_code")
	private String ifscCode;
	@Column(name="account_holder_name")
	private String accountHolderName;
	@Column(name="branch_name")
	private String branchName;
	@Column(name="micr_code")
	private String micrCode;
	@Column(name="bank_name")
	private String bankName;
	@Column(name = "bank_account_status")
	private boolean bankAccountStatus;
	@Column(name=" bank_account_type")
	private String accountType;
	
	
	@Column(name="website_Integration ")
	private String websiteIntegration;
	@Column(name="salt_key")
	private String saltKey;
	@Column(name="api_sandbox_key")
	private String apiSandboxKey;
	@Column(name="api_key")
	private String apiKey;
	@Column(name="application_id")
	private String applicationId;
	@Column(name="plugin")
	private String plugin;
	
	@Column(name="country_name")
	private String countryName;
	@Column(name="state_name")
	private String stateName;
	@Column(name="city_name")
	private String cityName;
	@Column(name="pincode")
	private String pincode;
	
	@Column(name="secret_key")
	private String secretKey;
	
	@Column(name="licence_number")
	private String licenceNumber;
	
	@Column(name="licence_type")
	private String licenceType;
	
	@Column(name="licence_Issue_Date")
	private LocalDate licenceIssueDate;
	
	@Column(name="licence_expiration_date")
	private LocalDate licenceExpirationDate;
	
	//payment
	@Column(name="remaining_amount")
	private double remainingAmount;
	@Column(name="consumed_amount")
	private double consumedAmount;
	@Column(name="monthly_gst",columnDefinition = "DOUBLE DEFAULT 0")
	private double monthlyGst;

	@Column(name="request_count")
    private int requestCount;
	@Column(name="response_count")
	private int responseCount;

	@Column(name="payment_status")
	private String paymentStatus;
	
	//postpaid
	@Column(name="start_date")
	private LocalDate startDate;
	@Column(name="end_date")
	private LocalDate endDate;
	@Column(name="grace_date")
	private LocalDate graceDate;
	@Column(name="grace_period")
	private int gracePeriod;
	@Column(name="duration")
	private int duration;
	@Column(name="postpaid_flag")//true means payment pending
	private boolean postpaidFlag;
	@Column(name="postpaid_payment_cycle")
	private String postpaidPaymentCycle;
	
	
	@Column(name="no_restriction")
	private int noRestriction;
	@Column(name="mail_present",columnDefinition = "BOOLEAN DEFAULT false")
	private boolean mailPresent;
	@Column(name="sms_present",columnDefinition = "BOOLEAN DEFAULT false")
	private boolean smsPresent;
	@Column(name="show_livekeys",columnDefinition = "BOOLEAN DEFAULT false")
	private boolean showLiveKeys;
	@Column(name="mail_triggered",columnDefinition = "BOOLEAN DEFAULT false")
	private boolean mailTriggered;
	@Column(name="invo_generated",columnDefinition = "BOOLEAN DEFAULT false")
	private boolean invoGenerated;
	
	@Column(name="bulk_upload_access",columnDefinition = "BOOLEAN DEFAULT false")
	private boolean bulkUploadAccess;
	@Column(name="create_pdf_access",columnDefinition = "BOOLEAN DEFAULT false")
	private boolean createPdfAccess;
	@Column(name="document_price",columnDefinition = "DOUBLE DEFAULT 0")
	private double documentPrice;

	@Column(name="admin_id",columnDefinition = "INTEGER DEFAULT 1")
	private int adminId;
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "role_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private RoleDto role;

	
	@ManyToOne(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	@JoinColumn(name="payment_id")
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private PaymentMethod paymentMethod;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "business_category_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private BusinessCategory category;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "business_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private BusinessType type;
	

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "mccCode_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private MCCCodesModel mccCodesModel;
	
	
	public int getGracePeriod() {
		return gracePeriod;
	}

	public void setGracePeriod(int gracePeriod) {
		this.gracePeriod = gracePeriod;
	}
	
	
	public int getRequestCount() {
		return requestCount;
	}

	public void setRequestCount(int requestCount) {
		this.requestCount = requestCount;
	}

	public int getResponseCount() {
		return responseCount;
	}
	public void setResponseCount(int responseCount) {
		this.responseCount = responseCount;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}


	public String getWebsiteIntegration() {
		return websiteIntegration;
	}

	public void setWebsiteIntegration(String websiteIntegration) {
		this.websiteIntegration = websiteIntegration;
	}

	public String getApiKey() {
		return apiKey;
	}

	public boolean isPostpaidFlag() {
		return postpaidFlag;
	}

	public void setPostpaidFlag(boolean postpaidFlag) {
		this.postpaidFlag = postpaidFlag;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getPlugin() {
		return plugin;
	}

	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getPublicUrl() {
		return publicUrl;
	}

	public void setPublicUrl(String publicUrl) {
		this.publicUrl = publicUrl;
	}

	public String getLicenceNumber() {
		return licenceNumber;
	}

	public void setLicenceNumber(String licenceNumber) {
		this.licenceNumber = licenceNumber;
	}

	public String getLicenceType() {
		return licenceType;
	}

	public void setLicenceType(String licenceType) {
		this.licenceType = licenceType;
	}

	public LocalDate getLicenceIssueDate() {
		return licenceIssueDate;
	}

	public void setLicenceIssueDate(LocalDate licenceIssueDate) {
		this.licenceIssueDate = licenceIssueDate;
	}

	public LocalDate getLicenceExpirationDate() {
		return licenceExpirationDate;
	}

	public void setLicenceExpirationDate(LocalDate licenceExpirationDate) {
		this.licenceExpirationDate = licenceExpirationDate;
	}

	public int getUserId() {
		return userId;
	}

	public boolean isSmsPresent() {
		return smsPresent;
	}

	public void setSmsPresent(boolean smsPresent) {
		this.smsPresent = smsPresent;
	}

	public boolean isVerificationRequired() {
		return verificationRequired;
	}

	public void setVerificationRequired(boolean verificationRequired) {
		this.verificationRequired = verificationRequired;
	}

	public boolean isSigningRequired() {
		return signingRequired;
	}

	public void setSigningRequired(boolean signingRequired) {
		this.signingRequired = signingRequired;
	}

	public double getRemainingAmount() {
		return remainingAmount;
	}

	public void setRemainingAmount(double remainingAmount) {
		this.remainingAmount = twoDecimelDouble(remainingAmount);
	}

	public double getConsumedAmount() {
		return consumedAmount;
	}

	public void setConsumedAmount(double consumedAmount) {
		this.consumedAmount = twoDecimelDouble(consumedAmount);
	}

	public void setUserId(int userId) {
		this.userId = userId;
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

	public String getSaltKey() {
		return saltKey;
	}

	public void setSaltKey(String saltKey) {
		this.saltKey = saltKey;
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
	
	

	
	
	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	public String getAccountHolderName() {
		return accountHolderName;
	}

	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getMicrCode() {
		return micrCode;
	}

	public void setMicrCode(String micrCode) {
		this.micrCode = micrCode;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public boolean isBankAccountStatus() {
		return bankAccountStatus;
	}

	public void setBankAccountStatus(boolean bankAccountStatus) {
		this.bankAccountStatus = bankAccountStatus;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}


	@Transient
	private Object jwtResponse; 

	public Object getJwtResponse() {
		return jwtResponse;
	}

	public void setJwtResponse(Object jwtResponse) {
		this.jwtResponse = jwtResponse;
	}

	public RoleDto getRole() {
		return role;
	}

	public void setRole(RoleDto role) {
		this.role = role;
	}

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public String getGst() {
		return gst;
	}

	public void setGst(String gst) {
		this.gst = gst;
	}

	public String getContactPersonName() {
		return contactPersonName;
	}

	public void setContactPersonName(String contactPersonName) {
		this.contactPersonName = contactPersonName;
	}

	public String getContactPersonMobile() {
		return contactPersonMobile;
	}

	public void setContactPersonMobile(String contactPersonMobile) {
		this.contactPersonMobile = contactPersonMobile;
	}

	public String getContactPersonEmail() {
		return contactPersonEmail;
	}

	public void setContactPersonEmail(String contactPersonEmail) {
		this.contactPersonEmail = contactPersonEmail;
	}

	public BusinessType getType() {
		return type;
	}

	public void setType(BusinessType type) {
		this.type = type;
	}

	public BusinessCategory getCategory() {
		return category;
	}

	public void setCategory(BusinessCategory category) {
		this.category = category;
	}

	public MCCCodesModel getMccCodesModel() {
		return mccCodesModel;
	}

	public void setMccCodesModel(MCCCodesModel mccCodesModel) {
		this.mccCodesModel = mccCodesModel;
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

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public LocalDate getGraceDate() {
		return graceDate;
	}

	public void setGraceDate(LocalDate graceDate) {
		this.graceDate = graceDate;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public Long getRejectedCount() {
		return rejectedCount;
	}

	public void setRejectedCount(Long rejectedCount) {
		this.rejectedCount = rejectedCount;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public String getConsentId() {
		return consentId;
	}

	public void setConsentId(String consentId) {
		this.consentId = consentId;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	
	public String getApiSandboxKey() {
		return apiSandboxKey;
	}

	public void setApiSandboxKey(String apiSandboxKey) {
		this.apiSandboxKey = apiSandboxKey;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public int getNoRestriction() {
		return noRestriction;
	}

	public void setNoRestriction(int noRestriction) {
		this.noRestriction = noRestriction;
	}

	public boolean isMailPresent() {
		return mailPresent;
	}

	public void setMailPresent(boolean mailPresent) {
		this.mailPresent = mailPresent;
	}

	public boolean isBulkUploadAccess() {
		return bulkUploadAccess;
	}

	public void setBulkUploadAccess(boolean bulkUploadAccess) {
		this.bulkUploadAccess = bulkUploadAccess;
	}

	public boolean isCreatePdfAccess() {
		return createPdfAccess;
	}

	public void setCreatePdfAccess(boolean createPdfAccess) {
		this.createPdfAccess = createPdfAccess;
	}

	public double getDocumentPrice() {
		return documentPrice;
	}

	public void setDocumentPrice(double documentPrice) {
		this.documentPrice = twoDecimelDouble(documentPrice);
	}

	public boolean isShowLiveKeys() {
		return showLiveKeys;
	}

	public void setShowLiveKeys(boolean showLiveKeys) {
		this.showLiveKeys = showLiveKeys;
	}

	public double getMonthlyGst() {
		return monthlyGst;
	}

	public void setMonthlyGst(double monthlyGst) {
		this.monthlyGst = twoDecimelDouble(monthlyGst);
	}

	public boolean isAadhaarBasedSigning() {
		return aadhaarBasedSigning;
	}

	public void setAadhaarBasedSigning(boolean aadhaarBasedSigning) {
		this.aadhaarBasedSigning = aadhaarBasedSigning;
	}

	public boolean isMailTriggered() {
		return mailTriggered;
	}

	public void setMailTriggered(boolean mailTriggered) {
		this.mailTriggered = mailTriggered;
	}

	public String getPostpaidPaymentCycle() {
		return postpaidPaymentCycle;
	}

	public void setPostpaidPaymentCycle(String postpaidPaymentCycle) {
		this.postpaidPaymentCycle = postpaidPaymentCycle;
	}

	public String getTempPass() {
		return tempPass;
	}

	public void setTempPass(String tempPass) {
		this.tempPass = tempPass;
	}

	public boolean isInvoGenerated() {
		return invoGenerated;
	}

	public void setInvoGenerated(boolean invoGenerated) {
		this.invoGenerated = invoGenerated;
	}
	
	public boolean isGeneralSigning() {
		return generalSigning;
	}

	public void setGeneralSigning(boolean generalSigning) {
		this.generalSigning = generalSigning;
	}

	public double getAadhaarXmlPrice() {
		return aadhaarXmlPrice;
	}

	public void setAadhaarXmlPrice(double aadhaarXmlPrice) {
		this.aadhaarXmlPrice = twoDecimelDouble(aadhaarXmlPrice);
	}

	public double getAadhaarOtpPrice() {
		return aadhaarOtpPrice;
	}

	public void setAadhaarOtpPrice(double aadhaarOtpPrice) {
		this.aadhaarOtpPrice = twoDecimelDouble(aadhaarOtpPrice);
	}
	

	
	
	
	
	
	public int getAdminId() {
		return adminId;
	}

	public void setAdminId(int adminId) {
		this.adminId = adminId;
	}

	private double twoDecimelDouble(double value) {
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places

		double roundedValue = bd.doubleValue();
		
		return roundedValue;
	}
}
