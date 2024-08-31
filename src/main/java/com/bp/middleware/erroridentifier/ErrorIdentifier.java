package com.bp.middleware.erroridentifier;

import java.util.Date;

import com.bp.middleware.mcccode.MCCCodesModel;
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
import jakarta.validation.constraints.Email;

@Entity
@Table(name = "error_identifier")
public class ErrorIdentifier {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="erroridentifier_id")
	private int errorIdentifierId;
	@Column(name="error_reference_number")
	private String errorReferenceNumber;
	@Column(name="occured_date")
	private Date occuredDate;
	@Column(name="reason",columnDefinition = "LONGTEXT")
	private String reason;
	@Column(name="api_name")
	private String apiName;
	@Column(name="line_number")
	private long lineNumber;
	@Column(name="class_name")
	private String className;
	@Column(name="method_name")
	private String methodName;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendorissue_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private VendorSideIssues vendorSideIssues;
	
	public int getErrorIdentifierId() {
		return errorIdentifierId;
	}
	public void setErrorIdentifierId(int errorIdentifierId) {
		this.errorIdentifierId = errorIdentifierId;
	}
	public String getErrorReferenceNumber() {
		return errorReferenceNumber;
	}
	public void setErrorReferenceNumber(String errorReferenceNumber) {
		this.errorReferenceNumber = errorReferenceNumber;
	}
	public Date getOccuredDate() {
		return occuredDate;
	}
	public void setOccuredDate(Date occuredDate) {
		this.occuredDate = occuredDate;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getApiName() {
		return apiName;
	}
	public void setApiName(String apiName) {
		this.apiName = apiName;
	}
	public long getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(long lineNumber) {
		this.lineNumber = lineNumber;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public VendorSideIssues getVendorSideIssues() {
		return vendorSideIssues;
	}
	public void setVendorSideIssues(VendorSideIssues vendorSideIssues) {
		this.vendorSideIssues = vendorSideIssues;
	}
	
}
