package com.bp.middleware.vendors;


import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="vendor_model")
public class VendorModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="vendor_id")
	private int vendorId;
	
	@Column(name="vendor_name")
	private String vendorName;
	@Column(name="status")
	private boolean status;
	@Column(name="created_by")
	private String createdBy;
	@Column(name="created_at")
	private LocalDate createdAt;
	@Column(name="modified_by")
	private String modifiedBy;
	@Column(name="modified_at")
	private LocalDate modifiedAt;
	@Column(name="vendor_request")
	private int vendorRequest;
	@Column(name="vendor_response")
	private int vendorResponse;
	@Column(name="monthly_count")
	private int monthlyCount;
	
	public int getVendorRequest() {
		return vendorRequest;
	}
	public void setVendorRequest(int vendorRequest) {
		this.vendorRequest = vendorRequest;
	}
	public int getVendorResponse() {
		return vendorResponse;
	}
	public void setVendorResponse(int vendorResponse) {
		this.vendorResponse = vendorResponse;
	}
	public int getMonthlyCount() {
		return monthlyCount;
	}
	public void setMonthlyCount(int monthlyCount) {
		this.monthlyCount = monthlyCount;
	}
	public int getVendorId() {
		return vendorId;
	}
	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}
	public String getVendorName() {
		return vendorName;
	}
	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
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
	
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
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
