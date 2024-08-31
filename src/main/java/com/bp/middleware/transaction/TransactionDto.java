package com.bp.middleware.transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import com.bp.middleware.conveniencefee.ConveniencePercentageEntity;
import com.bp.middleware.modeofpgpayment.ModeOfPaymentPg;
import com.bp.middleware.payment.PaymentModel;
import com.bp.middleware.transactionamountmodel.TransactionAmountModel;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.util.JsonNodeConverter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name="transaction_table")
public class TransactionDto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="trancsaction_id")
	private int trancsactionId;
	
	@Column(name="payment_id")
	private String paymentId;
	
	@Column(name="payment_status")
	private String paymentStatus;
	
	@Column(name="paid_amount")
	private double paidAmount;
	
	@Column(name="payment_date_time")
	private String paymentDatetime;
	
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="created_date_time")
	private Date createdDatetime;
	
	@JsonProperty("etc")
	@Column(name = "etc",length=10000)
	@Convert(converter = JsonNodeConverter.class)
	private JsonNode etc;
	
	@Column(name="order_reference")
	private String orderReference;
	
	@Column(name="receipt_number")
	private String receiptNumber;
	
	@Column(name="invoice_number")
	private String invoiceNumber;
	
	@Column(name="invoice")
	private String invoice;
	
	@Column(name="conv_invoice")
	private String convInvoice;
	
	@Column(name="description")
	private String description;
	
	@Column(name="track_id")
	private String trackId;
	@Column(name="payment_mode")
	private String paymentMode;
	
	@Column(name="member_name")
	private String memberName;
	@Column(name="email")
	private String email;
	@Column(name="mobile_number")
	private String mobileNumber;
	@Column(name="address")
	private String address;
	@Column(name="postal_code")
	private String postalCode;
	@Column(name="payment_method")
	private String paymentMethod;
	@Column(name="bank_reference")
	private String bankReference;
	@Column(name="card_expiry")
	private String cardExpiry;
	@Column(name="card_no_masked")
	private String cardNoMasked;
	@Column(name="pay_number")
	private String payNumber;
	@Column(name="receipt")
	private String receipt;
	@Column(name="convenience_amount",columnDefinition = "DOUBLE DEFAULT 0")
	private double convenienceAmount;
	@Column(name="exclusive_amount",columnDefinition = "DOUBLE DEFAULT 0")
	private double exclusiveAmount;
	@Column(name="sgst_amount",columnDefinition = "DOUBLE DEFAULT 0")
	private double sgstAmount;
	@Column(name="cgst_amount",columnDefinition = "DOUBLE DEFAULT 0")
	private double cgstAmount;
	@Column(name="igst_amount",columnDefinition = "DOUBLE DEFAULT 0")
	private double igstAmount;
	@Column(name="prepaid_id",columnDefinition = "INT DEFAULT 0")
	private int prepaidId;
	@Column(name="postpaid_id",columnDefinition = "INT DEFAULT 0")
	private int postpaidId;

	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pay_id",nullable = true)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private PaymentModel payid;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id",nullable = false)
	@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
	private EntityModel entity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "mode_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private ModeOfPaymentPg modeOfPaymentPg;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "convenience_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private ConveniencePercentageEntity conveniencePercentageEntity;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trans_amount_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private TransactionAmountModel transactionAmountModel;
	
	
	@Transient
	private String month;

	@Transient
	private String year;

	public int getTrancsactionId() {
		return trancsactionId;
	}

	public void setTrancsactionId(int trancsactionId) {
		this.trancsactionId = trancsactionId;
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

	public double getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(double paidAmount) {
		this.paidAmount = twoDecimelDouble(paidAmount);
	}

	public String getPaymentDatetime() {
		return paymentDatetime;
	}

	public void setPaymentDatetime(String paymentDatetime) {
		this.paymentDatetime = paymentDatetime;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getTrackId() {
		return trackId;
	}

	public void setTrackId(String trackId) {
		this.trackId = trackId;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDatetime() {
		return createdDatetime;
	}

	public void setCreatedDatetime(Date createdDatetime) {
		this.createdDatetime = createdDatetime;
	}

	public JsonNode getEtc() {
		return etc;
	}

	public void setEtc(JsonNode etc) {
		this.etc = etc;
	}

	public String getOrderReference() {
		return orderReference;
	}

	public void setOrderReference(String orderReference) {
		this.orderReference = orderReference;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public PaymentModel getPayid() {
		return payid;
	}

	public void setPayid(PaymentModel payid) {
		this.payid = payid;
	}

	public String getMonth() {
		return month;
	}

	public String getReceipt() {
		return receipt;
	}

	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public EntityModel getEntity() {
		return entity;
	}

	public void setEntity(EntityModel entity) {
		this.entity = entity;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getBankReference() {
		return bankReference;
	}

	public void setBankReference(String bankReference) {
		this.bankReference = bankReference;
	}

	public String getCardExpiry() {
		return cardExpiry;
	}

	public void setCardExpiry(String cardExpiry) {
		this.cardExpiry = cardExpiry;
	}

	public String getCardNoMasked() {
		return cardNoMasked;
	}

	public void setCardNoMasked(String cardNoMasked) {
		this.cardNoMasked = cardNoMasked;
	}

	public String getPayNumber() {
		return payNumber;
	}

	public void setPayNumber(String payNumber) {
		this.payNumber = payNumber;
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

	public Double getIgstAmount() {
		return igstAmount;
	}

	public void setIgstAmount(Double igstAmount) {
		this.igstAmount = twoDecimelDouble(igstAmount);
	}

	public ModeOfPaymentPg getModeOfPaymentPg() {
		return modeOfPaymentPg;
	}

	public void setModeOfPaymentPg(ModeOfPaymentPg modeOfPaymentPg) {
		this.modeOfPaymentPg = modeOfPaymentPg;
	}

	public ConveniencePercentageEntity getConveniencePercentageEntity() {
		return conveniencePercentageEntity;
	}

	public void setConveniencePercentageEntity(ConveniencePercentageEntity conveniencePercentageEntity) {
		this.conveniencePercentageEntity = conveniencePercentageEntity;
	}

	public Double getConvenienceAmount() {
		return convenienceAmount;
	}

	public void setConvenienceAmount(Double convenienceAmount) {
		this.convenienceAmount = twoDecimelDouble(convenienceAmount);
	}

	public TransactionAmountModel getTransactionAmountModel() {
		return transactionAmountModel;
	}

	public void setTransactionAmountModel(TransactionAmountModel transactionAmountModel) {
		this.transactionAmountModel = transactionAmountModel;
	}

	public String getInvoice() {
		return invoice;
	}

	public void setInvoice(String invoice) {
		this.invoice = invoice;
	}

	public int getPrepaidId() {
		return prepaidId;
	}

	public void setPrepaidId(int prepaidId) {
		this.prepaidId = prepaidId;
	}

	public int getPostpaidId() {
		return postpaidId;
	}

	public void setPostpaidId(int postpaidId) {
		this.postpaidId = postpaidId;
	}

	public String getConvInvoice() {
		return convInvoice;
	}

	public void setConvInvoice(String convInvoice) {
		this.convInvoice = convInvoice;
	}

	
	
	
	
	
	
	private double twoDecimelDouble(double value) {
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places

		double roundedValue = bd.doubleValue();
		
		return roundedValue;
	}
}
