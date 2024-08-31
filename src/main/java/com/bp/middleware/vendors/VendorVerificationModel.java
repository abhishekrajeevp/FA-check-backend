package com.bp.middleware.vendors;

import java.time.LocalDate;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vendor_verification")
public class VendorVerificationModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "vendor_verification_id")
	private int vendorVerificationId;

	@Column(name="verification_document")
	private String verificationDocument;
//	@Column(name="type")
//	private String type;
	@Column(name="status")
	private boolean status;
	@Column(name="created_by")
	private String createdBy;
	@Column(name="created_at")
	private LocalDate createdAt;
	@Column(name="modified_by")
	private String modifiedby;
	@Column(name="modified_at")
	private LocalDate modifiedAt;

//	@ManyToOne(fetch = FetchType.LAZY, optional = false)
//	@JoinColumn(name = "vendor_id")
//	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
//	private VendorModel vendorModel;

	public int getVendorVerificationId() {
		return vendorVerificationId;
	}

	public void setVendorVerificationId(int vendorVerificationId) {
		this.vendorVerificationId = vendorVerificationId;
	}

	public String getVerificationDocument() {
		return verificationDocument;
	}

	public void setVerificationDocument(String verificationDocument) {
		this.verificationDocument = verificationDocument;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedby() {
		return modifiedby;
	}

	public void setModifiedby(String modifiedby) {
		this.modifiedby = modifiedby;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDate getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(LocalDate modifiedAt) {
		this.modifiedAt = modifiedAt;
	}
	
	
	
	
}
