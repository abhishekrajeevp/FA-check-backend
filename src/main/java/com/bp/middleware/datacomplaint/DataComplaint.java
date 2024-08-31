package com.bp.middleware.datacomplaint;

import java.time.LocalDate;

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
@Table(name="data_complaint")
public class DataComplaint {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="datacomplaint_id")
	private int dataComplaintId;
	
	@Column(name="merchant_name")
	private String merchantName;
	@Column(name="merchant_email")
	private String merchantEmail;
	@Column(name="verification_type")
	private String verificationType;
	@Column(name="file_number")
	private String fileNumber;
	@Column(name="customer_name")
	private String customerName;
	@Column(name="fetched_date")
	private LocalDate fetchedDate;
	@Column(name="lastfetched_date")
	private LocalDate lastFetchedDate;
	@Column(name="comment")
	private String comment;
	@Column(name="remarks")
	private String remarks;
	@Column(name="submitted_date")
	private LocalDate submittedDate;
	@Column(name="modified_by")
	private String modifiedBy;
	@Column(name="resolved_date")
	private LocalDate resolvedDate;
	@Column(name="complaint_active")
	private boolean complaintActive;
	@Column(name="response",columnDefinition = "LONGTEXT")
	private String response;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel user;

	public int getDataComplaintId() {
		return dataComplaintId;
	}

	public void setDataComplaintId(int dataComplaintId) {
		this.dataComplaintId = dataComplaintId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getMerchantEmail() {
		return merchantEmail;
	}

	public void setMerchantEmail(String merchantEmail) {
		this.merchantEmail = merchantEmail;
	}

	public String getVerificationType() {
		return verificationType;
	}

	public void setVerificationType(String verificationType) {
		this.verificationType = verificationType;
	}

	public String getFileNumber() {
		return fileNumber;
	}

	public void setFileNumber(String fileNumber) {
		this.fileNumber = fileNumber;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public LocalDate getFetchedDate() {
		return fetchedDate;
	}

	public void setFetchedDate(LocalDate fetchedDate) {
		this.fetchedDate = fetchedDate;
	}

	public LocalDate getLastFetchedDate() {
		return lastFetchedDate;
	}

	public void setLastFetchedDate(LocalDate lastFetchedDate) {
		this.lastFetchedDate = lastFetchedDate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public LocalDate getSubmittedDate() {
		return submittedDate;
	}

	public void setSubmittedDate(LocalDate submittedDate) {
		this.submittedDate = submittedDate;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public EntityModel getUser() {
		return user;
	}

	public void setUser(EntityModel user) {
		this.user = user;
	}

	public LocalDate getResolvedDate() {
		return resolvedDate;
	}

	public void setResolvedDate(LocalDate resolvedDate) {
		this.resolvedDate = resolvedDate;
	}

	public boolean isComplaintActive() {
		return complaintActive;
	}

	public void setComplaintActive(boolean complaintActive) {
		this.complaintActive = complaintActive;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	
}
