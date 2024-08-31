package com.bp.middleware.postpaidinvoicedetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PostpaidReminderInvoice {

	private int userId;
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

	private String invoiceNumber;
	private int totalHits;

	private double usedAmount;
	private double usedAmountGst;
	private double totalAmount;

	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDate dueDate;

	private String usedAmountInWords;
	private String usedAmountGstInWords;
	private String totalAmountInWords;

	private List<Map<String, Object>> usedServices;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
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

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public int getTotalHits() {
		return totalHits;
	}

	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}

	public double getUsedAmount() {
		return usedAmount;
	}

	public void setUsedAmount(double usedAmount) {
		this.usedAmount = usedAmount;
	}

	public double getUsedAmountGst() {
		return usedAmountGst;
	}

	public void setUsedAmountGst(double usedAmountGst) {
		this.usedAmountGst = usedAmountGst;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
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

	public String getUsedAmountInWords() {
		return usedAmountInWords;
	}

	public void setUsedAmountInWords(String usedAmountInWords) {
		this.usedAmountInWords = usedAmountInWords;
	}

	public String getUsedAmountGstInWords() {
		return usedAmountGstInWords;
	}

	public void setUsedAmountGstInWords(String usedAmountGstInWords) {
		this.usedAmountGstInWords = usedAmountGstInWords;
	}

	public String getTotalAmountInWords() {
		return totalAmountInWords;
	}

	public void setTotalAmountInWords(String totalAmountInWords) {
		this.totalAmountInWords = totalAmountInWords;
	}

	public List<Map<String, Object>> getUsedServices() {
		return usedServices;
	}

	public void setUsedServices(List<Map<String, Object>> usedServices) {
		this.usedServices = usedServices;
	}

	public int getPostpaidId() {
		return postpaidId;
	}

	public void setPostpaidId(int postpaidId) {
		this.postpaidId = postpaidId;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
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
