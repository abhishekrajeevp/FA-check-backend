package com.bp.middleware.ticketraising;


import java.time.LocalDate;
import java.util.Date;

import com.bp.middleware.ticketcategory.TicketCategory;
import com.bp.middleware.user.EntityModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="ticket_raising")
public class TicketRaising {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int ticketId;
	
	@Column(name="customer_name")
	private String customerName;
	
	@Column(name="mobile_number")
	private String mobileNumber;
	
	@Column(name="email")
	private String email;
	
	@Column(name = "user_type")
	private String userType;
	
	@Column(name = "remarks")
	private String remarks;
	
	@Column(name = "status")
	private boolean status;
	
	@Column(name = "ticket_status")
	private String ticketStatus;
	
	@Column(name="reason")
	private String reason;
	
 	@Column(name="description")
	@Size(max = 10000)
	private String description;
	
	@Column(name="created_at")
	private LocalDate createdAt;
	
	@Column(name="reference_number")
	private String referenceNumber;
	
	@Column(name="attachment_proof")
	private String attachment;
	
	@Column(name="modified_by")
	private String modifiedBy;
	
	@Column(name="modified_at")
	private LocalDate modifiedAt;
	
	@ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
	@JoinColumn(name="ticket_category_id",nullable = true)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private TicketCategory category;
	
	@ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
	@JoinColumn(name="user_id")
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private EntityModel entityModel;

	public int getTicketId() {
		return ticketId;
	}

	public void setTicketId(int ticketId) {
		this.ticketId = ticketId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}


	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public String getAttachment() {
		return attachment;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}

	public TicketCategory getCategory() {
		return category;
	}

	public void setCategory(TicketCategory category) {
		this.category = category;
	}

	public EntityModel getEntityModel() {
		return entityModel;
	}

	public void setEntityModel(EntityModel entityModel) {
		this.entityModel = entityModel;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean isStatus() {
		return status;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public LocalDate getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(LocalDate modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public String getTicketStatus() {
		return ticketStatus;
	}

	public void setTicketStatus(String ticketStatus) {
		this.ticketStatus = ticketStatus;
	}
	
	
	
	
	
}
