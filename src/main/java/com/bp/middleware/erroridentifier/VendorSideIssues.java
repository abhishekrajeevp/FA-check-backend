package com.bp.middleware.erroridentifier;

import java.util.Date;

import com.bp.middleware.vendors.VendorModel;
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
@Table(name = "vendor_side_issues")
public class VendorSideIssues {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="vendor_side_issue_id")
	private int vendorSideIssueId;
	
	@Column(name="date")
	private Date date;
	
	@Column(name="message")
	private String message;
	
	@Column(name="errorCode")
	private String errorCode;
	
	@Column(name="status_code")
	private int statusCode;
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "vendor_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private VendorModel vendorModel;

	public int getVendorSideIssueId() {
		return vendorSideIssueId;
	}

	public void setVendorSideIssueId(int vendorSideIssueId) {
		this.vendorSideIssueId = vendorSideIssueId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public VendorModel getVendorModel() {
		return vendorModel;
	}

	public void setVendorModel(VendorModel vendorModel) {
		this.vendorModel = vendorModel;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	
}
