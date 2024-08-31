package com.bp.middleware.businesstype;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="business_type_table")
public class BusinessType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="business_id")
	private int businessTypeId;
	
	@Column(name="business_type")
	private String businessType;
	
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="created_at")
	private Date createdDateAndTime;
	
	@Column(name="status")
	private boolean status;
	

	@Column(name="modified_by")
	private String modifiedBy;
	
	@Column(name="modified_at")
	private Date modifiedDateAndTime;
	
	public int getBusinessTypeId() {
		return businessTypeId;
	}
	public void setBusinessTypeId(int businessTypeId) {
		this.businessTypeId = businessTypeId;
	}
	public String getBusinessType() {
		return businessType;
	}
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreatedDateAndTime() {
		return createdDateAndTime;
	}
	public void setCreatedDateAndTime(Date createdDateAndTime) {
		this.createdDateAndTime = createdDateAndTime;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public Date getModifiedDateAndTime() {
		return modifiedDateAndTime;
	}
	public void setModifiedDateAndTime(Date modifiedDateAndTime) {
		this.modifiedDateAndTime = modifiedDateAndTime;
	}
	
	
	
}
