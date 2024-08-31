package com.bp.middleware.uploadhistory;

import java.util.Date;

import com.bp.middleware.user.EntityModel;
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
@Table(name="upload_document")
public class UploadModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="upload_id")
	private int uploadId;
	@Column(name="upload_count")
	private int uploadCount;
	@Column(name="success_count")
	private int successCount;
	@Column(name="failed_count")
	private int failedCount;
	@Column(name="upload_document")
	private String uploadDocument;
	@Column(name="finished_document")
	private String finishedDocument;
	@Column(name="created_by")
	private String createdBy;
	@Column(name="created_at")
	private Date createdAt;
	@Column(name="ip_address")
	private String ipAddress;
	@Column(name="category")
	private String category;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel entity;


	public int getUploadId() {
		return uploadId;
	}

	public void setUploadId(int uploadId) {
		this.uploadId = uploadId;
	}

	public int getUploadCount() {
		return uploadCount;
	}

	public void setUploadCount(int uploadCount) {
		this.uploadCount = uploadCount;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}

	public int getFailedCount() {
		return failedCount;
	}

	public void setFailedCount(int failedCount) {
		this.failedCount = failedCount;
	}

	public String getUploadDocument() {
		return uploadDocument;
	}

	public void setUploadDocument(String uploadDocument) {
		this.uploadDocument = uploadDocument;
	}

	public String getFinishedDocument() {
		return finishedDocument;
	}

	public void setFinishedDocument(String finishedDocument) {
		this.finishedDocument = finishedDocument;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public EntityModel getEntity() {
		return entity;
	}

	public void setEntity(EntityModel entity) {
		this.entity = entity;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	
	

}
