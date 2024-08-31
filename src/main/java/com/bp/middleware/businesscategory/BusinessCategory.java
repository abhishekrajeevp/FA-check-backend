package com.bp.middleware.businesscategory;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="business_category")
public class BusinessCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="business_category_id")
	private int businessCategoryId;
	
	@Column(name="business_category_name")
	private String businessCategoryName;
	
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

	public int getBusinessCategoryId() {
		return businessCategoryId;
	}

	public void setBusinessCategoryId(int businessCategoryId) {
		this.businessCategoryId = businessCategoryId;
	}

	public String getBusinessCategoryName() {
		return businessCategoryName;
	}

	public void setBusinessCategoryName(String businessCategoryName) {
		this.businessCategoryName = businessCategoryName;
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
