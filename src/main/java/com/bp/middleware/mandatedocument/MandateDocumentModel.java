package com.bp.middleware.mandatedocument;

import com.bp.middleware.businesscategory.BusinessCategory;
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
@Table(name="mandate_document")
public class MandateDocumentModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name ="mandatedoc_id")
	private long mandateId;
	
	@Column(name="kyc_doc_name")
	private String kycDocName;
	@Column(name="created_by")
	private String createdBy;
	@Column(name="created_date_time")
	private String createdDateTime;
	@Column(name="modified_date_time")
	private String modifiedDateAndTime;
	@Column(name="modified_by")
	private String modifiedBy;
	@Column(name="doc_type")
	private String docType;
	@Column(name="status",columnDefinition = "BOOLEAN DEFAULT TRUE")
	private boolean status;
	
	@ManyToOne(fetch = FetchType.LAZY,optional=false)
	@JoinColumn(name = "business_id",columnDefinition = "INT DEFAULT 1")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private BusinessCategory businessCategory;
	
	public long getMandateId() {
		return mandateId;
	}
	public void setMandateId(long mandateId) {
		this.mandateId = mandateId;
	}
	public String getKycDocName() {
		return kycDocName;
	}
	public void setKycDocName(String kycDocName) {
		this.kycDocName = kycDocName;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getCreatedDateTime() {
		return createdDateTime;
	}
	public void setCreatedDateTime(String createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	public String getModifiedDateAndTime() {
		return modifiedDateAndTime;
	}
	public void setModifiedDateAndTime(String modifiedDateAndTime) {
		this.modifiedDateAndTime = modifiedDateAndTime;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}
	public BusinessCategory getBusinessCategory() {
		return businessCategory;
	}
	public void setBusinessCategory(BusinessCategory businessCategory) {
		this.businessCategory = businessCategory;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	
}
