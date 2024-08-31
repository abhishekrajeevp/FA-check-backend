package com.bp.middleware.conveniencefee;


import java.math.BigDecimal;
import java.math.RoundingMode;

import com.bp.middleware.modeofpgpayment.ModeOfPaymentPg;
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
@Table(name = "convenience_percentage")

public class ConveniencePercentageEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "convenience_id")
	private int convenienceId;
	
	@Column(name="threshold_amount")
	private Double thresholdAmount;

	@Column(name = "fixed_amount")
	private Double fixedAmount;
	
	@Column(name = "convenience_percentage")
	private Double conveniencePercentage;

	@Column(name = "active_status")
	private int activeStatus;
	
	@Column(name = "created_by")
	private String createdBy;
	
	@Column(name="created_date_time")
	private String createdDateTime;

	@Column(name = "modified_by")
	private String modifiedBy;
	
	@Column(name="modified_date_time")
	private String modifeidDateTime;
	
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "mode_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private ModeOfPaymentPg modeOfPaymentPg;
	
	
	public int getConvenienceId() {
		return convenienceId;
	}

	public void setConvenienceId(int convenienceId) {
		this.convenienceId = convenienceId;
	}



	public Double getThresholdAmount() {
		return thresholdAmount;
	}

	public void setThresholdAmount(Double thresholdAmount) {
		this.thresholdAmount = twoDecimelDouble(thresholdAmount);
	}

	public Double getFixedAmount() {
		return fixedAmount;
	}

	public void setFixedAmount(Double fixedAmount) {
		this.fixedAmount = twoDecimelDouble(fixedAmount);
	}

	public Double getConveniencePercentage() {
		return conveniencePercentage;
	}

	public void setConveniencePercentage(Double conveniencePercentage) {
		this.conveniencePercentage = conveniencePercentage;
	}

	public int getActiveStatus() {
		return activeStatus;
	}

	public void setActiveStatus(int activeStatus) {
		this.activeStatus = activeStatus;
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

	public ModeOfPaymentPg getModeOfPaymentPg() {
		return modeOfPaymentPg;
	}

	public void setModeOfPaymentPg(ModeOfPaymentPg modeOfPaymentPg) {
		this.modeOfPaymentPg = modeOfPaymentPg;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getModifeidDateTime() {
		return modifeidDateTime;
	}

	public void setModifeidDateTime(String modifeidDateTime) {
		this.modifeidDateTime = modifeidDateTime;
	}
	
	
	
	
	
	
	private double twoDecimelDouble(double value) {
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places

		double roundedValue = bd.doubleValue();
		
		return roundedValue;
	}
}
