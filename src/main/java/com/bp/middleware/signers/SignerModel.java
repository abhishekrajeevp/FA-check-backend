package com.bp.middleware.signers;

import java.time.LocalDate;
import java.util.Date;

import com.bp.middleware.signmerchant.MerchantModel;
import com.bp.middleware.user.EntityModel;
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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name="signer_model")
public class SignerModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="signer_id")
	private int signerId;
	
	@Column(name="signer_name")
	private String signerName;
	
	@Column(name="Signer_mobile")
	@Pattern(regexp="(^$|[0-9]{10})")
	private String SignerMobile;
	
	@Column(name="signer_email")
	@Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}")
	private String signerEmail;
	
	@Column(name="signed_at")
	private String signedAt;
	
	@Column(name="created_at")
	private LocalDate createdAt;
	
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="modified_at")
	private LocalDate modifiedAt;
	
	@Column(name="modified_by")
	private String modifiedBy;
	
	@Column(name="reference_number")
	private String referenceNumber;
	
	@Column(name="status")
	private boolean status;
	
	@JsonIgnore
	@Column(name="otpcode")
	private String otpcode;
	
	@Column(name="otp_expiry")
	private Date otpExpiry;
	
	@Column(name="otpStatus")
	private boolean otpVerificationStatus;
	
	@Column(name="consent", columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean consent;
	@Column(name="consent_accepted_at")
	private Date consentAcceptedAt;
	
	@Column(name="ip_address")
	private String ipAddress;
	
	@Column(name="aadhaar_req_id")
	private String aadhaarRequestId;
	
	@Column(name="aadhaar_req_time")
	private LocalDate aadhaarRequestTime;
	
	@Column(name="signer_aadhaar")
	private String signerAadhaar;
	
	@Column(name="bond_payer")
	private boolean bondPayer;
	
	@Column(name="get_location",columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean getLocation;
	
	@Column(name="expired",columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean expired;
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false,cascade = CascadeType.ALL)
	@JoinColumn(name = "merchant_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private MerchantModel merchantModel;
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false,cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel entityModel;

	public int getSignerId() {
		return signerId;
	}

	public void setSignerId(int signerId) {
		this.signerId = signerId;
	}

	public String getSignerName() {
		return signerName;
	}

	public void setSignerName(String signerName) {
		this.signerName = signerName;
	}

	public String getSignerMobile() {
		return SignerMobile;
	}

	public void setSignerMobile(String signerMobile) {
		SignerMobile = signerMobile;
	}

	public String getSignerEmail() {
		return signerEmail;
	}

	public void setSignerEmail(String signerEmail) {
		this.signerEmail = signerEmail;
	}

	public String getSignedAt() {
		return signedAt;
	}

	public void setSignedAt(String signedAt) {
		this.signedAt = signedAt;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDate getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(LocalDate modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
	public MerchantModel getMerchantModel() {
		return merchantModel;
	}

	public void setMerchantModel(MerchantModel merchantModel) {
		this.merchantModel = merchantModel;
	}

	public String getOtpcode() {
		return otpcode;
	}

	public void setOtpcode(String otpcode) {
		this.otpcode = otpcode;
	}

	public Date getOtpExpiry() {
		return otpExpiry;
	}

	public void setOtpExpiry(Date otpExpiry) {
		this.otpExpiry = otpExpiry;
	}

	public boolean isOtpVerificationStatus() {
		return otpVerificationStatus;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	public void setOtpVerificationStatus(boolean otpVerificationStatus) {
		this.otpVerificationStatus = otpVerificationStatus;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public EntityModel getEntityModel() {
		return entityModel;
	}

	public void setEntityModel(EntityModel entityModel) {
		this.entityModel = entityModel;
	}

	public boolean isBondPayer() {
		return bondPayer;
	}

	public void setBondPayer(boolean bondPayer) {
		this.bondPayer = bondPayer;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public String getAadhaarRequestId() {
		return aadhaarRequestId;
	}

	public void setAadhaarRequestId(String aadhaarRequestId) {
		this.aadhaarRequestId = aadhaarRequestId;
	}

	public LocalDate getAadhaarRequestTime() {
		return aadhaarRequestTime;
	}

	public void setAadhaarRequestTime(LocalDate aadhaarRequestTime) {
		this.aadhaarRequestTime = aadhaarRequestTime;
	}

	public String getSignerAadhaar() {
		return signerAadhaar;
	}

	public void setSignerAadhaar(String signerAadhaar) {
		this.signerAadhaar = signerAadhaar;
	}

	public boolean isConsent() {
		return consent;
	}

	public void setConsent(boolean consent) {
		this.consent = consent;
	}

	public Date getConsentAcceptedAt() {
		return consentAcceptedAt;
	}

	public void setConsentAcceptedAt(Date consentAcceptedAt) {
		this.consentAcceptedAt = consentAcceptedAt;
	}

	public boolean isGetLocation() {
		return getLocation;
	}

	public void setGetLocation(boolean getLocation) {
		this.getLocation = getLocation;
	}
	
	
	
}
