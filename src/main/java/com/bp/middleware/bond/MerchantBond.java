package com.bp.middleware.bond;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="merchant_bond")
public class MerchantBond {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="bond_id")
	private int bondId;
	@Column(name="bond_amount")
	private double bondAmount;
	@Column(name="upload_count")
	private int uploadCount;
	@Column(name="used_count")
	private int usedCount;
	@Column(name="remaining_count")
	private int remainingCount;
	@Column(name="on_process")
	private int onProcess;
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
	
	public int getBondId() {
		return bondId;
	}
	public void setBondId(int bondId) {
		this.bondId = bondId;
	}
	public double getBondAmount() {
		return bondAmount;
	}
	public void setBondAmount(double bondAmount) {
		this.bondAmount = twoDecimelDouble(bondAmount);
	}
	public int getUploadCount() {
		return uploadCount;
	}
	public void setUploadCount(int uploadCount) {
		this.uploadCount = uploadCount;
	}
	public int getUsedCount() {
		return usedCount;
	}
	public void setUsedCount(int usedCount) {
		this.usedCount = usedCount;
	}
	public int getRemainingCount() {
		return remainingCount;
	}
	public void setRemainingCount(int remainingCount) {
		this.remainingCount = remainingCount;
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
	public int getOnProcess() {
		return onProcess;
	}
	public void setOnProcess(int onProcess) {
		this.onProcess = onProcess;
	}
	
	
	
	
	
	
	private double twoDecimelDouble(double value) {
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places

		double roundedValue = bd.doubleValue();
		
		return roundedValue;
	}
	
}
