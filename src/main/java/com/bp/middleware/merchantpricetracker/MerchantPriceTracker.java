package com.bp.middleware.merchantpricetracker;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import com.bp.middleware.user.EntityModel;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorVerificationModel;
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
@Table(name="merchant_price_tracker")
public class MerchantPriceTracker {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="merchantprice_tracker_id")
	private int merchantPriceTrackerId;
	
	@Column(name="id_amount")
	private double idAmount;
	
	@Column(name="ocr_amount")
	private double ocrAmount;
	
	@Column(name="signature_amount")
	private double signatureAmount;
	
	@Column(name="applicable_from_date")
	private LocalDate applicableFromDate;
	
	@Column(name="end_date")
	private LocalDate endDate;
	
	@Column(name="currently_in_use")
	private boolean currentlyInUse;
	
	@Column(name="mailSend",columnDefinition = "BOOLEAN DEFAULT TRUE")
	private boolean mailSend;
	
	@Column(name="remark")
	private String remark;
	
	@Column(name="priority")
	private int priority;
	
	@Column(name="recent_identifier") // 1 = latest ,2= before that ,3 = way earlier or above
	private int recentIdentifier;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vendor_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler"})
	private VendorModel vendorModel;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vendor_verification_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler"})
	private VendorVerificationModel vendorVerificationModel;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler"})
	private EntityModel  entityModel;
	

	public double getIdAmount() {
		return idAmount;
	}

	public void setIdAmount(double idAmount) {
		this.idAmount = twoDecimelDouble(idAmount);
	}

	public double getOcrAmount() {
		return ocrAmount;
	}

	public void setOcrAmount(double ocrAmount) {
		this.ocrAmount = twoDecimelDouble(ocrAmount);
	}

	public double getSignatureAmount() {
		return signatureAmount;
	}

	public void setSignatureAmount(double signatureAmount) {
		this.signatureAmount = twoDecimelDouble(signatureAmount);
	}

	public LocalDate getApplicableFromDate() {
		return applicableFromDate;
	}

	public void setApplicableFromDate(LocalDate applicableFromDate) {
		this.applicableFromDate = applicableFromDate;
	}

	public boolean isCurrentlyInUse() {
		return currentlyInUse;
	}

	public void setCurrentlyInUse(boolean currentlyInUse) {
		this.currentlyInUse = currentlyInUse;
	}

	public VendorModel getVendorModel() {
		return vendorModel;
	}

	public void setVendorModel(VendorModel vendorModel) {
		this.vendorModel = vendorModel;
	}

	public VendorVerificationModel getVendorVerificationModel() {
		return vendorVerificationModel;
	}

	public void setVendorVerificationModel(VendorVerificationModel vendorVerificationModel) {
		this.vendorVerificationModel = vendorVerificationModel;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int getRecentIdentifier() {
		return recentIdentifier;
	}

	public void setRecentIdentifier(int recentIdentifier) {
		this.recentIdentifier = recentIdentifier;
	}

	public int getMerchantPriceTrackerId() {
		return merchantPriceTrackerId;
	}

	public void setMerchantPriceTrackerId(int merchantPriceTrackerId) {
		this.merchantPriceTrackerId = merchantPriceTrackerId;
	}

	public EntityModel getEntityModel() {
		return entityModel;
	}

	public void setEntityModel(EntityModel entityModel) {
		this.entityModel = entityModel;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public boolean isMailSend() {
		return mailSend;
	}

	public void setMailSend(boolean mailSend) {
		this.mailSend = mailSend;
	}
	
	
	
	

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	private double twoDecimelDouble(double value) {
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places

		double roundedValue = bd.doubleValue();
		
		return roundedValue;
	}
}
