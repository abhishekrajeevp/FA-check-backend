package com.bp.middleware.ticketcategory;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ticket_category")
public class TicketCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="ticket_category_id")
	private int ticketCategoryId;
	
	@Column(name="ticket_type")
	private String ticketType;
	
	@Column(name="created_at")
	private Date createdDateAndTime;
	
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="modified_at")
	private Date modifiedDateAndTime;
	
	@Column(name="modified_by")
	private String modifiedBy;
	
	@Column(name="status")
	private boolean status;

	public int getTicketCategoryId() {
		return ticketCategoryId;
	}

	public void setTicketCategoryId(int ticketCategoryId) {
		this.ticketCategoryId = ticketCategoryId;
	}

	public String getTicketType() {
		return ticketType;
	}

	public void setTicketType(String ticketType) {
		this.ticketType = ticketType;
	}

	public Date getCreatedDateAndTime() {
		return createdDateAndTime;
	}

	public void setCreatedDateAndTime(Date createdDateAndTime) {
		this.createdDateAndTime = createdDateAndTime;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getModifiedDateAndTime() {
		return modifiedDateAndTime;
	}

	public void setModifiedDateAndTime(Date modifiedDateAndTime) {
		this.modifiedDateAndTime = modifiedDateAndTime;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
	
	
}
