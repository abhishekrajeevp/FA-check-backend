package com.bp.middleware.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
@Table(name="payment_model")
public class PaymentModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="pay_id")
	private int payid;
	@Column(name="payment_id")
	private String paymentId;
	@Column(name="payment_status")
	private String paymentStatus;
	@Column(name="paid_amount")
	private Double paidAmount;
	@Column(name="payment_date_time")
	private String paymentDateTime;
	@Column(name="created_by")
	private String createdBy;
	@Column(name="created_date_time")
	private String createdDateTime;
	@Column(name="track_id")
	private String trackId;
	@Column(name="receipt_number")
	private String receiptNumber;
	@Column(name="invoice_number")
	private String invoiceNumber;
	@Column(name="due_date")
	private LocalDate dueDate;
	@Column(name="description")
	private String description;
	@Column(name="payment_mode")
	private String paymentMode;
	@Column(name="exclusive_amount",columnDefinition = "DOUBLE DEFAULT 0")
	private Double exclusiveAmount;
	@Column(name="sgst_amount",columnDefinition = "DOUBLE DEFAULT 0")
	private Double sgstAmount;
	@Column(name="cgst_amount",columnDefinition = "DOUBLE DEFAULT 0")
	private Double cgstAmount;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private EntityModel entity;

	public int getPayid() {
		return payid;
	}

	public void setPayid(int payid) {
		this.payid = payid;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public Double getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(Double paidAmount) {
		this.paidAmount = twoDecimelDouble(paidAmount);
	}

	public String getPaymentDateTime() {
		return paymentDateTime;
	}

	public void setPaymentDateTime(String paymentDateTime) {
		this.paymentDateTime = paymentDateTime;
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

	public String getTrackId() {
		return trackId;
	}

	public void setTrackId(String trackId) {
		this.trackId = trackId;
	}

	public String getReceiptNumber() {
		return receiptNumber;
	}

	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public EntityModel getEntity() {
		return entity;
	}

	public void setEntity(EntityModel entity) {
		this.entity = entity;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public Double getExclusiveAmount() {
		return exclusiveAmount;
	}

	public void setExclusiveAmount(Double exclusiveAmount) {
		this.exclusiveAmount = twoDecimelDouble(exclusiveAmount);
	}

	public Double getSgstAmount() {
		return sgstAmount;
	}

	public void setSgstAmount(Double sgstAmount) {
		this.sgstAmount = twoDecimelDouble(sgstAmount);
	}

	public Double getCgstAmount() {
		return cgstAmount;
	}

	public void setCgstAmount(Double cgstAmount) {
		this.cgstAmount = twoDecimelDouble(cgstAmount);
	}
	
	
	
	
	
	private double twoDecimelDouble(double value) {
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places

		double roundedValue = bd.doubleValue();
		
		return roundedValue;
	}
	
	
}
