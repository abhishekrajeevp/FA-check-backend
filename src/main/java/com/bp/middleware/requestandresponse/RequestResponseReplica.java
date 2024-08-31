package com.bp.middleware.requestandresponse;

import java.util.Date;

import com.bp.middleware.user.EntityModel;
import com.bp.middleware.vendors.VendorVerificationModel;
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
@Table(name = "req_resp_replica")
public class RequestResponseReplica {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="replica_id")
	private int replicaId;
	@Column(name="source_type")
	private String sourceType;
	@Column(name="ifsc_code")
	private String ifscCode;
	@Column(name="source")
	private String source;
	@Column(name="filing_status")
	private boolean filingStatus;
	@Column(name="request_at")
	private Date requestDateAndTime;
	@Column(name="request_by")
	private String requestBy;
	@Column(name="otpcode")
	private String otp;	
	@Column(name="response_at")
	private String responseDateAndTime;
	@Column(name="message")
	private String message;
	@Column(name="status")
	private String status;
	@Column(name="dob")
	private String dob;
	@Column(name="common_response",columnDefinition = "LONGTEXT")
	private String commonResponse;
		
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "user_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel user;
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "vendor_verification_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private VendorVerificationModel verificationModel;

	public int getReplicaId() {
		return replicaId;
	}

	public void setReplicaId(int replicaId) {
		this.replicaId = replicaId;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public boolean isFilingStatus() {
		return filingStatus;
	}

	public void setFilingStatus(boolean filingStatus) {
		this.filingStatus = filingStatus;
	}

	public Date getRequestDateAndTime() {
		return requestDateAndTime;
	}

	public void setRequestDateAndTime(Date requestDateAndTime) {
		this.requestDateAndTime = requestDateAndTime;
	}

	public String getRequestBy() {
		return requestBy;
	}

	public void setRequestBy(String requestBy) {
		this.requestBy = requestBy;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public String getResponseDateAndTime() {
		return responseDateAndTime;
	}

	public void setResponseDateAndTime(String responseDateAndTime) {
		this.responseDateAndTime = responseDateAndTime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public EntityModel getUser() {
		return user;
	}

	public void setUser(EntityModel user) {
		this.user = user;
	}

	public VendorVerificationModel getVerificationModel() {
		return verificationModel;
	}

	public void setVerificationModel(VendorVerificationModel verificationModel) {
		this.verificationModel = verificationModel;
	}

	public String getCommonResponse() {
		return commonResponse;
	}

	public void setCommonResponse(String commonResponse) {
		this.commonResponse = commonResponse;
	}

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	
}
