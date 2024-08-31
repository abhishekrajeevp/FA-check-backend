package com.bp.middleware.manualpayment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.transaction.TransactionDto;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="manual_payment")
public class ManualPayment {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="manual_id")
	private int manualPaymetId;
	
	@Column(name="paid_amount")
	private double paidAmount;
	
	@Column(name="exclusive_amount")
	private double exclusiveAmount;
	
	@Column(name="sgst_9%")
	private double sgst;
	
	@Column(name="cgst_9%")
	private double cgst;
	
	@Column(name="igst_18%")
	private double igst;
	
	@Column(name="customer_name")
	private String customerName;
	
	@Column(name="customer_address")
	private String customerAddress;
	
	@Column(name="contact_number")
	private String contactNumber;
	
	@Column(name="receipt_number")
	private String receiptNumber;
	
	@Column(name="payment_date_time")
	private String paymentDateTime;
	
	@Column(name="description")
	private String description;
	
	@Column(name="track_id")
	private String trackId;
	
	@Column(name="payment_id")
	private String paymentId;
	
	@Column(name="reference_id")
	private String referenceId;
	
	@Column(name="image_proof")
	private String imageProof;
	
	@Column(name="payment_mode")
	private String paymentMode;
	
	@Column(name="created_at")
	private Date createdAt;
	
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="modified_at")
	private Date modifiedAt;
	
	@Column(name="modified_by")
	private String modifiedBy;
	
	@Column(name="receipt")
	private String receipt;
	
	@Column(name="mode_of_pay")
	private String modeOfPay;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id",nullable = false)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private EntityModel entity;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "prepaid_id",nullable = true)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private PrepaidPayment prepaid;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "postpaid_id",nullable = true)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private PostpaidPayment postpaid;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transaction_id",nullable = false,unique=true)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private TransactionDto transaction;


	public int getManualPaymetId() {
		return manualPaymetId;
	}


	public void setManualPaymetId(int manualPaymetId) {
		this.manualPaymetId = manualPaymetId;
	}


	public double getPaidAmount() {
		return paidAmount;
	}


	public void setPaidAmount(double paidAmount) {
		this.paidAmount = twoDecimelDouble(paidAmount);
	}


	public double getExclusiveAmount() {
		return exclusiveAmount;
	}


	public void setExclusiveAmount(double exclusiveAmount) {
		this.exclusiveAmount = twoDecimelDouble(exclusiveAmount);
	}


	public double getSgst() {
		return sgst;
	}


	public void setSgst(double sgst) {
		this.sgst = twoDecimelDouble(sgst);
	}


	public double getCgst() {
		return cgst;
	}


	public void setCgst(double cgst) {
		this.cgst = twoDecimelDouble(cgst);
	}


	public double getIgst() {
		return igst;
	}


	public void setIgst(double igst) {
		this.igst = twoDecimelDouble(igst);
	}


	public String getCustomerName() {
		return customerName;
	}


	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}


	public String getCustomerAddress() {
		return customerAddress;
	}


	public void setCustomerAddress(String customerAddress) {
		this.customerAddress = customerAddress;
	}


	public String getContactNumber() {
		return contactNumber;
	}


	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}


	public String getReceiptNumber() {
		return receiptNumber;
	}


	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}


	public String getPaymentDateTime() {
		return paymentDateTime;
	}


	public void setPaymentDateTime(String paymentDateTime) {
		this.paymentDateTime = paymentDateTime;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getTrackId() {
		return trackId;
	}


	public void setTrackId(String trackId) {
		this.trackId = trackId;
	}


	public String getPaymentId() {
		return paymentId;
	}


	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}


	public String getPaymentMode() {
		return paymentMode;
	}


	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}


	public Date getCreatedAt() {
		return createdAt;
	}


	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}


	public String getCreatedBy() {
		return createdBy;
	}


	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}


	public String getReceipt() {
		return receipt;
	}


	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}


	public EntityModel getEntity() {
		return entity;
	}


	public void setEntity(EntityModel entity) {
		this.entity = entity;
	}


	public PrepaidPayment getPrepaid() {
		return prepaid;
	}


	public void setPrepaid(PrepaidPayment prepaid) {
		this.prepaid = prepaid;
	}


	public PostpaidPayment getPostpaid() {
		return postpaid;
	}


	public void setPostpaid(PostpaidPayment postpaid) {
		this.postpaid = postpaid;
	}


	public TransactionDto getTransaction() {
		return transaction;
	}


	public void setTransaction(TransactionDto transaction) {
		this.transaction = transaction;
	}


	public String getModeOfPay() {
		return modeOfPay;
	}


	public void setModeOfPay(String modeOfPay) {
		this.modeOfPay = modeOfPay;
	}


	public String getReferenceId() {
		return referenceId;
	}


	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}


	public String getImageProof() {
		return imageProof;
	}


	public void setImageProof(String imageProof) {
		this.imageProof = imageProof;
	}


	public Date getModifiedAt() {
		return modifiedAt;
	}


	public void setModifiedAt(Date modifiedAt) {
		this.modifiedAt = modifiedAt;
	}


	public String getModifiedBy() {
		return modifiedBy;
	}


	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	
	
	
	
	
	
	
	
	
	private double twoDecimelDouble(double value) {
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places

		double roundedValue = bd.doubleValue();
		
		return roundedValue;
	}
	
}
