package com.bp.middleware.receiptdetails;

import java.util.Date;

public class ReceiptDetails {

	
	private int prepaidId;
	private int postpaidId;
	private int transactionId;
	
	private String receiptNo;
	private String receiptName;
	private Date receiptIssuanceDate;
	private String planType;
	private String bankReferenceNo;
	
	private String paymentType;
	private String paymentDate;
	
	private String paidBy;
	
	private double paidAmount;
	private double gst;
	
	private double totalPaidAmount;
	
	private String amountInWords;

	
	
	
	
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

	public String getReceiptNo() {
		return receiptNo;
	}

	public void setReceiptNo(String receiptNo) {
		this.receiptNo = receiptNo;
	}

	public Date getReceiptIssuanceDate() {
		return receiptIssuanceDate;
	}

	public void setReceiptIssuanceDate(Date receiptIssuanceDate) {
		this.receiptIssuanceDate = receiptIssuanceDate;
	}

	public String getPlanType() {
		return planType;
	}

	public void setPlanType(String planType) {
		this.planType = planType;
	}

	public String getBankReferenceNo() {
		return bankReferenceNo;
	}

	public void setBankReferenceNo(String bankReferenceNo) {
		this.bankReferenceNo = bankReferenceNo;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}

	public String getPaidBy() {
		return paidBy;
	}

	public void setPaidBy(String paidBy) {
		this.paidBy = paidBy;
	}

	public double getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(double paidAmount) {
		this.paidAmount = paidAmount;
	}

	public double getGst() {
		return gst;
	}

	public void setGst(double gst) {
		this.gst = gst;
	}

	public double getTotalPaidAmount() {
		return totalPaidAmount;
	}

	public void setTotalPaidAmount(double totalPaidAmount) {
		this.totalPaidAmount = totalPaidAmount;
	}

	public String getAmountInWords() {
		return amountInWords;
	}

	public void setAmountInWords(String amountInWords) {
		this.amountInWords = amountInWords;
	}

	public String getReceiptName() {
		return receiptName;
	}

	public void setReceiptName(String receiptName) {
		this.receiptName = receiptName;
	}

	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}
	
	
	
	
	
	
	
	
}
