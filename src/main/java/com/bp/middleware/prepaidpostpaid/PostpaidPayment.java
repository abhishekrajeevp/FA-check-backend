package com.bp.middleware.prepaidpostpaid;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.bp.middleware.transactionamountmodel.TransactionAmountModel;
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
@Table(name = "postpaid_payment")
public class PostpaidPayment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "postpaid_id")
	private int postpaidId;
	@Column(name = "total_amount")
	private double totalAmount;
	@Column(name = "due_amount", columnDefinition = "DOUBLE DEFAULT 0")
	private double dueAmount;
	@Column(name = "exclusive_amount", columnDefinition = "DOUBLE DEFAULT 0")
	private double exclusiveAmount;
	@Column(name = "total_hits", columnDefinition = "INTEGER DEFAULT 0")
	private int totalHits;
	@Column(name = "period")
	private LocalDate period;
	@Column(name = "start_date")
	private LocalDate startDate;
	@Column(name = "end_date")
	private LocalDate endDate;
	@Column(name = "payment_flag")
	private boolean paymentFlag;
	@Column(name = "payment_mode")
	private String paymentMode;
	@Column(name = "remark")
	private String remark;
	@Column(name = "paid_date")
	private LocalDate paidDate;
	@Column(name = "updated_date")
	private LocalDate updatedDate;
	@Column(name = "transaction_id")
	private String transactionId;
	@Column(name = "paid_amount")
	private double paidAmount;
	@Column(name = "invoice")
	private String invoice;
	@Column(name = "conv_invoice")
	private String convInvoice;
	@Column(name = "receipt")
	private String receipt;
	@Column(name = "pay_id", columnDefinition = "INT DEFAULT 1")
	private int payId;

	@Column(name = "invoice_number")
	private String invoiceNumber;
	@Column(name = "invoice_two")
	private String invoiceTwo;
	@Column(name = "invoice_three")
	private String invoiceThree;
	@Column(name = "invoice_generated_date")
	private LocalDate invoiceGeneratedDate;
//	@Column(name="used_services")
//	private List<Map<String, Object>> usedServices;

	@Column(name = "grace_invoice")
	private String graceInvoice;
	@Column(name = "grace_invoice_generated_date")
	private LocalDate graceInvoiceGeneratedDate;
	@Column(name = "grace_invo_payment", columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean graceInvoPayment;

	@Column(name = "bank_reference")
	private String bankReference;
	@Column(name = "card_expiry")
	private String cardExpiry;
	@Column(name = "cardno_masked")
	private String cardNoMasked;
	@Column(name = "postal_code")
	private String postalCode;
	@Column(name = "address")
	private String address;
	@Column(name = "payment_date_time")
	private String paymentDateTime;
	
	@Column(name = "unique_id")
	private String uniqueId;

	@Column(name = "conveinvo_generateddate")
	private LocalDate conveInvoGeneratedDate;
	@Column(name = "receipt_generateddate")
	private LocalDate receiptGeneratedDate;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel entityModel;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trans_amount_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private TransactionAmountModel transactionAmountModel;

	public EntityModel getEntityModel() {
		return entityModel;
	}

	public void setEntityModel(EntityModel entityModel) {
		this.entityModel = entityModel;
	}

	public int getPostpaidId() {
		return postpaidId;
	}

	public void setPostpaidId(int postpaidId) {
		this.postpaidId = postpaidId;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = twoDecimelDouble(totalAmount);
	}

	public LocalDate getPeriod() {
		return period;
	}

	public void setPeriod(LocalDate period) {
		this.period = period;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public boolean isPaymentFlag() {
		return paymentFlag;
	}

	public void setPaymentFlag(boolean paymentFlag) {
		this.paymentFlag = paymentFlag;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public LocalDate getPaidDate() {
		return paidDate;
	}

	public void setPaidDate(LocalDate paidDate) {
		this.paidDate = paidDate;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public LocalDate getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(LocalDate updatedDate) {
		this.updatedDate = updatedDate;
	}

	public double getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(double paidAmount) {
		this.paidAmount = twoDecimelDouble(paidAmount);
	}

	public String getInvoice() {
		return invoice;
	}

	public void setInvoice(String invoice) {
		this.invoice = invoice;
	}

	public String getReceipt() {
		return receipt;
	}

	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}

	public int getPayId() {
		return payId;
	}

	public void setPayId(int payId) {
		this.payId = payId;
	}

	public double getExclusiveAmount() {
		return exclusiveAmount;
	}

	public void setExclusiveAmount(double exclusiveAmount) {
		this.exclusiveAmount = twoDecimelDouble(exclusiveAmount);
	}

	public double getDueAmount() {
		return dueAmount;
	}

	public void setDueAmount(double dueAmount) {
		this.dueAmount = twoDecimelDouble(dueAmount);
	}

	public int getTotalHits() {
		return totalHits;
	}

	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getInvoiceTwo() {
		return invoiceTwo;
	}

	public void setInvoiceTwo(String invoiceTwo) {
		this.invoiceTwo = invoiceTwo;
	}

	public String getInvoiceThree() {
		return invoiceThree;
	}

	public void setInvoiceThree(String invoiceThree) {
		this.invoiceThree = invoiceThree;
	}

	public LocalDate getInvoiceGeneratedDate() {
		return invoiceGeneratedDate;
	}

	public void setInvoiceGeneratedDate(LocalDate invoiceGeneratedDate) {
		this.invoiceGeneratedDate = invoiceGeneratedDate;
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

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	//
//	public List<Map<String, Object>> getUsedServices() {
//		return usedServices;
//	}
//	public void setUsedServices(List<Map<String, Object>> usedServices) {
//		this.usedServices = usedServices;
//	}
	public String getConvInvoice() {
		return convInvoice;
	}

	public void setConvInvoice(String convInvoice) {
		this.convInvoice = convInvoice;
	}

	public String getGraceInvoice() {
		return graceInvoice;
	}

	public void setGraceInvoice(String graceInvoice) {
		this.graceInvoice = graceInvoice;
	}

	public LocalDate getGraceInvoiceGeneratedDate() {
		return graceInvoiceGeneratedDate;
	}

	public void setGraceInvoiceGeneratedDate(LocalDate graceInvoiceGeneratedDate) {
		this.graceInvoiceGeneratedDate = graceInvoiceGeneratedDate;
	}

	public boolean isGraceInvoPayment() {
		return graceInvoPayment;
	}

	public void setGraceInvoPayment(boolean graceInvoPayment) {
		this.graceInvoPayment = graceInvoPayment;
	}

	public String getPaymentDateTime() {
		return paymentDateTime;
	}

	public void setPaymentDateTime(String paymentDateTime) {
		this.paymentDateTime = paymentDateTime;
	}

	
	
	
	
	
	public TransactionAmountModel getTransactionAmountModel() {
		return transactionAmountModel;
	}

	public void setTransactionAmountModel(TransactionAmountModel transactionAmountModel) {
		this.transactionAmountModel = transactionAmountModel;
	}

	public LocalDate getConveInvoGeneratedDate() {
		return conveInvoGeneratedDate;
	}

	public void setConveInvoGeneratedDate(LocalDate conveInvoGeneratedDate) {
		this.conveInvoGeneratedDate = conveInvoGeneratedDate;
	}

	public LocalDate getReceiptGeneratedDate() {
		return receiptGeneratedDate;
	}

	public void setReceiptGeneratedDate(LocalDate receiptGeneratedDate) {
		this.receiptGeneratedDate = receiptGeneratedDate;
	}
	
	
	
	
	

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	private double twoDecimelDouble(double value) {

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places

		double roundedValue = bd.doubleValue();

		return roundedValue;
	}

}
