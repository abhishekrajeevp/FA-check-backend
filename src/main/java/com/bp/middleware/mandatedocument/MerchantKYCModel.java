package com.bp.middleware.mandatedocument;

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
@Table(name="merchant_kyc_model")
public class MerchantKYCModel {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "doc_Id")
	private Long merchantId;

	@Column(name = "card_number")
	private String cardNumber;
	@Column(name = "admin_approval")
	private String adminApproval;
	@Column(name = "super_admin_approval")
	private String superAdminApproval;
	@Column(name = "approval_date_time")
	private String approvalDateTime;
	@Column(name = "file_type")
	private String fileType;
	@Column(name = "doc_front_path")
	private String docFrontPath;
	@Column(name = "doc_back_path")
	private String docBackPath;
	@Column(name = "doc_type")
	private String docType;
	@Column(name = "created_by")
	private String createdBy;
	@Column(name = "created_date_time")
	private String createdDateTime;
	private String flag;
	@Column(name = "modified_By")
	private String modifiedBy;
	@Column(name = "modified_date_time")
	private String modifiedDateTime;
	@Column(name = "deleted_flag")
	private int deletedFlag;
	@Column(name = "approvalByL1")
	private String approvalByL1;
	@Column(name = "approvalByL2")
	private String approvalByL2;
	@Column(name = "remarks")
	private String Remarks;

	@ManyToOne(fetch = FetchType.LAZY,optional=false)
	@JoinColumn(name = "user_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel entityModel;

	@ManyToOne(fetch = FetchType.LAZY,optional=false)
	@JoinColumn(name = "mandatedoc_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private MandateDocumentModel mandateDocumentModel;

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getAdminApproval() {
		return adminApproval;
	}

	public void setAdminApproval(String adminApproval) {
		this.adminApproval = adminApproval;
	}

	public String getSuperAdminApproval() {
		return superAdminApproval;
	}

	public void setSuperAdminApproval(String superAdminApproval) {
		this.superAdminApproval = superAdminApproval;
	}

	public String getApprovalDateTime() {
		return approvalDateTime;
	}

	public void setApprovalDateTime(String approvalDateTime) {
		this.approvalDateTime = approvalDateTime;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getDocFrontPath() {
		return docFrontPath;
	}

	public void setDocFrontPath(String docFrontPath) {
		this.docFrontPath = docFrontPath;
	}

	public String getDocBackPath() {
		return docBackPath;
	}

	public void setDocBackPath(String docBackPath) {
		this.docBackPath = docBackPath;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
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

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getModifiedDateTime() {
		return modifiedDateTime;
	}

	public void setModifiedDateTime(String modifiedDateTime) {
		this.modifiedDateTime = modifiedDateTime;
	}

	public int getDeletedFlag() {
		return deletedFlag;
	}

	public void setDeletedFlag(int deletedFlag) {
		this.deletedFlag = deletedFlag;
	}

	public String getApprovalByL1() {
		return approvalByL1;
	}

	public void setApprovalByL1(String approvalByL1) {
		this.approvalByL1 = approvalByL1;
	}

	public String getApprovalByL2() {
		return approvalByL2;
	}

	public void setApprovalByL2(String approvalByL2) {
		this.approvalByL2 = approvalByL2;
	}

	public String getRemarks() {
		return Remarks;
	}

	public void setRemarks(String remarks) {
		Remarks = remarks;
	}

	public EntityModel getEntityModel() {
		return entityModel;
	}

	public void setEntityModel(EntityModel entityModel) {
		this.entityModel = entityModel;
	}

	public MandateDocumentModel getMandateDocumentModel() {
		return mandateDocumentModel;
	}

	public void setMandateDocumentModel(MandateDocumentModel mandateDocumentModel) {
		this.mandateDocumentModel = mandateDocumentModel;
	}
	
	
	
}
