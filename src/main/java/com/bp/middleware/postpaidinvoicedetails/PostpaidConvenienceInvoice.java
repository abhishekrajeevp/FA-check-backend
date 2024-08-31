package com.bp.middleware.postpaidinvoicedetails;

import java.util.List;
import java.util.Map;

public class PostpaidConvenienceInvoice {

	private int userId;
	private int transactionId;
	private int postpaidId;
	
	private String entityName;
	private String entityAddress;
	private String entityCountry;
	private String entityState;
	private String entityCity;
	private String entityPincode;
	private String entityGst;
	private String entityMail;
	private String entityContactNumber;
	
	private String ifscCode;
	private String bankName;
	private String adminBankAccountNo;
	private String adminHsnCode;
	
	private String conveInvoiceNo;
	private double conveniencePercentage;
	private String modeOfPayment;
	private String paidDate;
	
	private double paidAmount;
	private double fixedAmount;
	private double convenienceAmount;
	private double gst;
	private double totalAmount;
	
	private String totalAmountInWords;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}


	
	
	public int getPostpaidId() {
		return postpaidId;
	}

	public void setPostpaidId(int postpaidId) {
		this.postpaidId = postpaidId;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getEntityAddress() {
		return entityAddress;
	}

	public void setEntityAddress(String entityAddress) {
		this.entityAddress = entityAddress;
	}

	public String getEntityCountry() {
		return entityCountry;
	}

	public void setEntityCountry(String entityCountry) {
		this.entityCountry = entityCountry;
	}

	public String getEntityState() {
		return entityState;
	}

	public void setEntityState(String entityState) {
		this.entityState = entityState;
	}

	public String getEntityCity() {
		return entityCity;
	}

	public void setEntityCity(String entityCity) {
		this.entityCity = entityCity;
	}

	public String getEntityPincode() {
		return entityPincode;
	}

	public void setEntityPincode(String entityPincode) {
		this.entityPincode = entityPincode;
	}

	public String getAdminBankAccountNo() {
		return adminBankAccountNo;
	}

	public void setAdminBankAccountNo(String adminBankAccountNo) {
		this.adminBankAccountNo = adminBankAccountNo;
	}

	public String getAdminHsnCode() {
		return adminHsnCode;
	}

	public void setAdminHsnCode(String adminHsnCode) {
		this.adminHsnCode = adminHsnCode;
	}

	public String getConveInvoiceNo() {
		return conveInvoiceNo;
	}

	public void setConveInvoiceNo(String conveInvoiceNo) {
		this.conveInvoiceNo = conveInvoiceNo;
	}

	public double getConveniencePercentage() {
		return conveniencePercentage;
	}

	public void setConveniencePercentage(double conveniencePercentage) {
		this.conveniencePercentage = conveniencePercentage;
	}

	public String getModeOfPayment() {
		return modeOfPayment;
	}

	public void setModeOfPayment(String modeOfPayment) {
		this.modeOfPayment = modeOfPayment;
	}

	public String getPaidDate() {
		return paidDate;
	}

	public void setPaidDate(String paidDate) {
		this.paidDate = paidDate;
	}

	public double getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(double paidAmount) {
		this.paidAmount = paidAmount;
	}

	public double getFixedAmount() {
		return fixedAmount;
	}

	public void setFixedAmount(double fixedAmount) {
		this.fixedAmount = fixedAmount;
	}

	public double getConvenienceAmount() {
		return convenienceAmount;
	}

	public void setConvenienceAmount(double convenienceAmount) {
		this.convenienceAmount = convenienceAmount;
	}

	public double getGst() {
		return gst;
	}

	public void setGst(double gst) {
		this.gst = gst;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getTotalAmountInWords() {
		return totalAmountInWords;
	}

	public void setTotalAmountInWords(String totalAmountInWords) {
		this.totalAmountInWords = totalAmountInWords;
	}

	public String getEntityGst() {
		return entityGst;
	}

	public void setEntityGst(String entityGst) {
		this.entityGst = entityGst;
	}

	public String getEntityMail() {
		return entityMail;
	}

	public void setEntityMail(String entityMail) {
		this.entityMail = entityMail;
	}

	public String getEntityContactNumber() {
		return entityContactNumber;
	}

	public void setEntityContactNumber(String entityContactNumber) {
		this.entityContactNumber = entityContactNumber;
	}

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	
	
		
	
}
