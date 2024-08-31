package com.bp.middleware.bond;

import java.time.LocalDate;

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
@Table(name="bond_details")
public class BondDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="bond_detail_id")
	private int bondDetailId;
	@Column(name="sealed_date")
	private LocalDate sealedDate;
	@Column(name="bond_number")
	private String bondNumber;
	@Column(name="document")
	private String document;
	@Column(name="bond_status")
	private int bondStatus;
	@Column(name="created_by")
	private String createdBy;
	@Column(name="created_at")
	private LocalDate createdAt;
	@Column(name="modified_by")
	private String modifiedBy;
	@Column(name="modified_at")
	private LocalDate modifiedAt;
	@Column(name="status")
	private boolean status;
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "bond_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private MerchantBond bond;
	
	public int getBondDetailId() {
		return bondDetailId;
	}
	public void setBondDetailId(int bondDetailId) {
		this.bondDetailId = bondDetailId;
	}

	public LocalDate getSealedDate() {
		return sealedDate;
	}
	public void setSealedDate(LocalDate sealedDate) {
		this.sealedDate = sealedDate;
	}
	public String getBondNumber() {
		return bondNumber;
	}
	public void setBondNumber(String bondNumber) {
		this.bondNumber = bondNumber;
	}
	public String getDocument() {
		return document;
	}
	public void setDocument(String document) {
		this.document = document;
	}
	public int getBondStatus() {
		return bondStatus;
	}
	public void setBondStatus(int bondStatus) {
		this.bondStatus = bondStatus;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
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
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public MerchantBond getBond() {
		return bond;
	}
	public void setBond(MerchantBond bond) {
		this.bond = bond;
	}
	
	
	
	
}
