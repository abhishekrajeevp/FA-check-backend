package com.bp.middleware.prepaidpostpaid;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;

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
@Table(name="prepaid_payment")
public class PrepaidPayment {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="prepaid_id")
	private int prepaidId;
	@Column(name="recharged_amount")
	private double rechargedAmount;
	@Column(name="payment_mode")
	private String paymentMode;
	@Column(name="remark")
	private String remark ;
	@Column(name="paid_date")
	private LocalDate paidDate;
	@Column(name="updated_date")
	private LocalDate updatedDate;
	@Column(name="transaction_id")
	private String transactionId;
	@Column(name="receipt")
	private String receipt;
	@Column(name="invoice")
	private String invoice;
	@Column(name="invoice_two")
	private String invoiceTwo;
	@Column(name="invoice_three")
	private String invoiceThree;
	@Column(name="payment_process")
	private String paymentProcess;
	@Column(name="pay_id")
	private int payId;
	
	@Column(name="invoice_generated_date")
	private LocalDate invoiceGeneratedDate;
	@Column(name="total_hits",columnDefinition = "INT DEFAULT 0")
	private int totalHits;
	@Column(name="wallet_balance",columnDefinition = "DOUBLE DEFAULT 0")
	private double walletBalance;
	@Column(name="used_amount",columnDefinition = "DOUBLE DEFAULT 0")
	private double usedAmount;
	@Column(name="month")
	private String month;
	@Column(name="used_services")
	private String usedServices;
	
	@Column(name="bank_reference")
	private String bankReference;
	@Column(name="card_expiry")
	private String cardExpiry;
	@Column(name="cardno_masked")
	private String cardNoMasked;
	@Column(name="postal_code")
	private String postalCode;
	@Column(name="address")
	private String address;
	@Column(name="payment_date_time")
	private String paymentDateTime;
	
	@Column(name = "unique_id")
	private String uniqueId;
	
	@Column(name="conveinvo_generateddate")
	private LocalDate conveInvoGeneratedDate;
	@Column(name="receipt_generateddate")
	private LocalDate receiptGeneratedDate;
	
	
	@ManyToOne(fetch = FetchType.EAGER,optional = false)
	@JoinColumn(name = "user_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel entityModel;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trans_amount_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private TransactionAmountModel transactionAmountModel;
	
	
	public int getPrepaidId() {
		return prepaidId;
	}


	public void setPrepaidId(int prepaidId) {
		this.prepaidId = prepaidId;
	}

	public double getRechargedAmount() {
		return rechargedAmount;
	}


	public void setRechargedAmount(double rechargedAmount) {
		this.rechargedAmount = twoDecimelDouble(rechargedAmount);
	}
	

	public EntityModel getEntityModel() {
		return entityModel;
	}


	public void setEntityModel(EntityModel entityModel) {
		this.entityModel = entityModel;
	}


	public LocalDate getUpdatedDate() {
		return updatedDate;
	}


	public void setUpdatedDate(LocalDate updatedDate) {
		this.updatedDate = updatedDate;
	}


	public TransactionAmountModel getTransactionAmountModel() {
		return transactionAmountModel;
	}


	public void setTransactionAmountModel(TransactionAmountModel transactionAmountModel) {
		this.transactionAmountModel = transactionAmountModel;
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


	public String getReceipt() {
		return receipt;
	}


	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}


	public String getPaymentProcess() {
		return paymentProcess;
	}


	public void setPaymentProcess(String paymentProcess) {
		this.paymentProcess = paymentProcess;
	}


	public int getPayId() {
		return payId;
	}


	public void setPayId(int payId) {
		this.payId = payId;
	}


	public String getInvoice() {
		return invoice;
	}


	public void setInvoice(String invoice) {
		this.invoice = invoice;
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


	public int getTotalHits() {
		return totalHits;
	}


	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}


	public double getWalletBalance() {
		return walletBalance;
	}


	public void setWalletBalance(double walletBalance) {
		this.walletBalance = twoDecimelDouble(walletBalance);
	}


	public double getUsedAmount() {
		return usedAmount;
	}


	public void setUsedAmount(double usedAmount) {
		this.usedAmount =twoDecimelDouble(usedAmount);
	}


	public String getMonth() {
		return month;
	}


	public void setMonth(String month) {
		this.month = month;
	}


	public String getUsedServices() {
		return usedServices;
	}


	public void setUsedServices(String usedServices) {
		this.usedServices = usedServices;
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


	public String getPaymentDateTime() {
		return paymentDateTime;
	}


	public void setPaymentDateTime(String paymentDateTime) {
		this.paymentDateTime = paymentDateTime;
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
