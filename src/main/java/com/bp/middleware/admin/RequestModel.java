package com.bp.middleware.admin;

import java.time.LocalDate;
import java.util.Date;



public class RequestModel {

	private int corporateId;
	private int adminId;
	private String name;
	private String email;
	private String address;
	private String mobileNumber;
	private String password;
	private Date lastLogin;
	private String ipAddress;
	private String createdBy;
	private Date createdDate;
	private boolean accountStatus; //Active/Inactive
	private String modifyBy;
	private Date modifyDate;
	private int loginFailedCount;
	private String profilePhoto;
	private boolean otpVerificationStatus; //success/Failure
	private String otpCode;
	private String  reasonForChange ;
	private String modifiedBy;
	private String reqDeviceType;
	private String newPassword;
	private int customerId;
	private String invoiceNumber;
	

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	//Role Model
	private int roleId;
	private int roleCode;
	private String roleName;
	private boolean roleStatus;
	private String profilePicture;

	//Scheme Model
	private int schemeId;
	private String schemeName;
	private boolean status; //Active , Inactive
	private int tenure;
	private double tenureYear;
	private double rateOfIntrest;
	private String minimumPayableAmount;
	private double minimumAmount;
	private double amount;

	//product Category
	private int categoryId;
	private String categoryType;
	private Date createdDateTime;
	private Date modifiedDateTime;
	private boolean activeStatus;

	//Category Type
	private int typeId;
	private String type;
	private Date createdDateAndTime;
	private Date modifiedDateAndTime;	

	//Loan Details
	private int loanId;
	private String modelName;
	private String registerNumber;
	private String chassisNumber;
	private String rcNumber;
	private String engineNumber;
	private String color;
	private String launchedYear;
	private String company;
	private String ownerName;
	private String serialNumber;
	private double value;
	private double premiumPercentage;
	private double totalValue;
	private double downPayment;
	private double loanSanctioned;
	private double documentationFee;
	private double processingFee;
	private double otherCharges;
	private double sumOfCharges;
	private double gst;
	private double sumOfIncludingGst;
	private double intrest;
	private double emi;
	private LocalDate emiStartDate;
	private LocalDate emiEndDate;
	private double loanAfterDeduction;
	private String discription;
	private double igst;
	private double cgst;
	private double sgst;
	private String state;
	private String city;

	//Ticket Category Type
	private int ticketCategoryId;
	private String ticketType;

	//Ticket Raising 
	private int ticketId;
	private String customerName;
	private String priorityType;
	private String userType;
	private String description;
	private String attachment;
	private Date createdAt;
	private String reason;
	//PaymentMode
	private int modeId;
	private String modeName;
	private int payid;
	//Manual Payment
	private int manualPaymetId;
	private double payAmount;
	private Date createAt;
	private String chequeNumber;
	private String date;
	private String payeeName;
	private String bankName;
	private String accountHolderName;
	private String accountNumber;
	private String micrCode;
	private String signature;
	private String demandDraftNumber;
	private String issuingBank;
	private String branch;
	private String customerAddress;
	private String receiptNumber;
	private String contactInformation;
	private String paymentDateTime;
	
	//Emi Table
	private int emiId;
	private LocalDate emaiPayDate;
	private int dueNo;
	private double loanAmount;
	private double intrestPerMonth;
	private double emiPerMonth;
	private double balance;
	private double payableAmount;
	private double payableAmountThisMonth;
	private boolean dueStatus;

	//Business Type 
	private int businessTypeId;
	private String businessType;
	
	//Business Category
	private int businessCategoryId;
	private String businessCategoryName;
	
	//Mcc Code
	private int mccId;
	private String corporateType;
	private long mccCode;
	private String createdDateAt;
	
	//AccountType
	private int accountTypeId;
	private String accountType;
	
	//SMS
	private int smsId;
	private String smsCode;
	private String smsMessage;
	private String smsDescription;
	private Date smsModifiedDate;
	private String smsModifiedBy;
	private boolean smsStatus;
	private String smsEntityId;
	private String smsTemplateId;
	private String  smsServiceUrl;
	private String  smsUserName;
	private String smsPassword;
	private String smsEnabled;
	
	//country Model
	private int countryId;
	private String countryName;
	private String countryCode;
	private String countryCurrency;
	private String countryTimezone;
	private boolean countryStatus;
	private Date modifiedDateAt;
	private int deletedFlag;
	
	//State Model
	private int stateId;
	private String stateName;
	private boolean stateStatus;

	//City Model
	private int cityId;
	private String cityName;
	private boolean cityStatus;
	
	//Pincode Model
	private int pincodeId;
	private String pincode;
	
	private boolean pincodeStatus;

	//Services Model
	private int serviceId;
	private String services;
	
	public int getCorporateId() {
		return corporateId;
	}

	public void setCorporateId(int corporateId) {
		this.corporateId = corporateId;
	}

	public int getAccountTypeId() {
		return accountTypeId;
	}

	public void setAccountTypeId(int accountTypeId) {
		this.accountTypeId = accountTypeId;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public int getBusinessCategoryId() {
		return businessCategoryId;
	}

	public void setBusinessCategoryId(int businessCategoryId) {
		this.businessCategoryId = businessCategoryId;
	}

	public String getBusinessCategoryName() {
		return businessCategoryName;
	}

	public void setBusinessCategoryName(String businessCategoryName) {
		this.businessCategoryName = businessCategoryName;
	}

	public int getBusinessTypeId() {
		return businessTypeId;
	}

	public void setBusinessTypeId(int businessTypeId) {
		this.businessTypeId = businessTypeId;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public int getEmiId() {
		return emiId;
	}

	public void setEmiId(int emiId) {
		this.emiId = emiId;
	}

	public int getPayid() {
		return payid;
	}

	public void setPayid(int payid) {
		this.payid = payid;
	}

	public LocalDate getEmaiPayDate() {
		return emaiPayDate;
	}

	public void setEmaiPayDate(LocalDate emaiPayDate) {
		this.emaiPayDate = emaiPayDate;
	}

	public int getDueNo() {
		return dueNo;
	}

	public void setDueNo(int dueNo) {
		this.dueNo = dueNo;
	}

	public double getLoanAmount() {
		return loanAmount;
	}

	public void setLoanAmount(double loanAmount) {
		this.loanAmount = loanAmount;
	}

	public double getIntrestPerMonth() {
		return intrestPerMonth;
	}

	public void setIntrestPerMonth(double intrestPerMonth) {
		this.intrestPerMonth = intrestPerMonth;
	}

	public double getEmiPerMonth() {
		return emiPerMonth;
	}

	public void setEmiPerMonth(double emiPerMonth) {
		this.emiPerMonth = emiPerMonth;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public double getPayableAmount() {
		return payableAmount;
	}

	public void setPayableAmount(double payableAmount) {
		this.payableAmount = payableAmount;
	}

	public double getPayableAmountThisMonth() {
		return payableAmountThisMonth;
	}

	public void setPayableAmountThisMonth(double payableAmountThisMonth) {
		this.payableAmountThisMonth = payableAmountThisMonth;
	}

	public boolean isDueStatus() {
		return dueStatus;
	}

	public void setDueStatus(boolean dueStatus) {
		this.dueStatus = dueStatus;
	}

	public int getAdminId() {
		return adminId;
	}
	public void setAdminId(int adminId) {
		this.adminId = adminId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Date getLastLogin() {
		return lastLogin;
	}
	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public boolean isAccountStatus() {
		return accountStatus;
	}
	public void setAccountStatus(boolean accountStatus) {
		this.accountStatus = accountStatus;
	}
	public String getModifyBy() {
		return modifyBy;
	}
	public void setModifyBy(String modifyBy) {
		this.modifyBy = modifyBy;
	}
	
	public Date getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}
	public int getLoginFailedCount() {
		return loginFailedCount;
	}
	public void setLoginFailedCount(int loginFailedCount) {
		this.loginFailedCount = loginFailedCount;
	}
	public String getProfilePhoto() {
		return profilePhoto;
	}
	public void setProfilePhoto(String profilePhoto) {
		this.profilePhoto = profilePhoto;
	}
	public boolean isOtpVerificationStatus() {
		return otpVerificationStatus;
	}
	public void setOtpVerificationStatus(boolean otpVerificationStatus) {
		this.otpVerificationStatus = otpVerificationStatus;
	}
	public String getOtpCode() {
		return otpCode;
	}
	public void setOtpCode(String otpCode) {
		this.otpCode = otpCode;
	}
	public String getReasonForChange() {
		return reasonForChange;
	}
	public void setReasonForChange(String reasonForChange) {
		this.reasonForChange = reasonForChange;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public String getReqDeviceType() {
		return reqDeviceType;
	}
	public void setReqDeviceType(String reqDeviceType) {
		this.reqDeviceType = reqDeviceType;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	public int getCustomerId() {
		return customerId;
	}
	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}
	public int getRoleId() {
		return roleId;
	}
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
	public int getRoleCode() {
		return roleCode;
	}
	public void setRoleCode(int roleCode) {
		this.roleCode = roleCode;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public boolean isRoleStatus() {
		return roleStatus;
	}
	public void setRoleStatus(boolean roleStatus) {
		this.roleStatus = roleStatus;
	}
	public String getProfilePicture() {
		return profilePicture;
	}
	public void setProfilePicture(String profilePicture) {
		this.profilePicture = profilePicture;
	}
	public int getSchemeId() {
		return schemeId;
	}
	public void setSchemeId(int schemeId) {
		this.schemeId = schemeId;
	}
	public String getSchemeName() {
		return schemeName;
	}
	public void setSchemeName(String schemeName) {
		this.schemeName = schemeName;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public int getTenure() {
		return tenure;
	}
	public void setTenure(int tenure) {
		this.tenure = tenure;
	}
	public double getTenureYear() {
		return tenureYear;
	}
	public void setTenureYear(double tenureYear) {
		this.tenureYear = tenureYear;
	}
	public double getRateOfIntrest() {
		return rateOfIntrest;
	}
	public void setRateOfIntrest(double rateOfIntrest) {
		this.rateOfIntrest = rateOfIntrest;
	}
	public String getMinimumPayableAmount() {
		return minimumPayableAmount;
	}
	public void setMinimumPayableAmount(String minimumPayableAmount) {
		this.minimumPayableAmount = minimumPayableAmount;
	}
	public double getMinimumAmount() {
		return minimumAmount;
	}
	public void setMinimumAmount(double minimumAmount) {
		this.minimumAmount = minimumAmount;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public String getCategoryType() {
		return categoryType;
	}
	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}
	
	public Date getModifiedDatetime() {
		return modifiedDateTime;
	}
	public void setModifiedDateTime(Date modifiedDateTime) {
		this.modifiedDateTime = modifiedDateTime;
	}
	public boolean isActiveStatus() {
		return activeStatus;
	}
	public void setActiveStatus(boolean activeStatus) {
		this.activeStatus = activeStatus;
	}
	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getCreatedDateAndTime() {
		return createdDateAndTime;
	}
	public void setCreatedDateAndTime(Date createdDateAndTime) {
		this.createdDateAndTime = createdDateAndTime;
	}
	public Date getModifiedDateAndTime() {
		return modifiedDateAndTime;
	}
	public void setModifiedDateAndTime(Date modifiedDateAndTime) {
		this.modifiedDateAndTime = modifiedDateAndTime;
	}
	public int getLoanId() {
		return loanId;
	}
	public void setLoanId(int loanId) {
		this.loanId = loanId;
	}
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public String getRegisterNumber() {
		return registerNumber;
	}
	public void setRegisterNumber(String registerNumber) {
		this.registerNumber = registerNumber;
	}
	public String getChassisNumber() {
		return chassisNumber;
	}
	public Date getCreatedDatetime() {
		return createdDateTime;
	}

	public void setCreatedDatetime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public String getCreatedDateAt() {
		return createdDateAt;
	}

	public void setCreatedDateAt(String createdDateAt) {
		this.createdDateAt = createdDateAt;
	}

	public void setChassisNumber(String chassisNumber) {
		this.chassisNumber = chassisNumber;
	}
	public String getRcNumber() {
		return rcNumber;
	}
	public void setRcNumber(String rcNumber) {
		this.rcNumber = rcNumber;
	}
	public String getEngineNumber() {
		return engineNumber;
	}
	public void setEngineNumber(String engineNumber) {
		this.engineNumber = engineNumber;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getLaunchedYear() {
		return launchedYear;
	}
	public void setLaunchedYear(String launchedYear) {
		this.launchedYear = launchedYear;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public double getPremiumPercentage() {
		return premiumPercentage;
	}
	public void setPremiumPercentage(double premiumPercentage) {
		this.premiumPercentage = premiumPercentage;
	}
	public double getTotalValue() {
		return totalValue;
	}
	public void setTotalValue(double totalValue) {
		this.totalValue = totalValue;
	}
	public double getDownPayment() {
		return downPayment;
	}
	public void setDownPayment(double downPayment) {
		this.downPayment = downPayment;
	}
	public double getLoanSanctioned() {
		return loanSanctioned;
	}
	public void setLoanSanctioned(double loanSanctioned) {
		this.loanSanctioned = loanSanctioned;
	}
	public double getDocumentationFee() {
		return documentationFee;
	}
	public void setDocumentationFee(double documentationFee) {
		this.documentationFee = documentationFee;
	}
	public double getProcessingFee() {
		return processingFee;
	}
	public void setProcessingFee(double processingFee) {
		this.processingFee = processingFee;
	}
	public double getOtherCharges() {
		return otherCharges;
	}
	public void setOtherCharges(double otherCharges) {
		this.otherCharges = otherCharges;
	}
	public double getSumOfCharges() {
		return sumOfCharges;
	}
	public void setSumOfCharges(double sumOfCharges) {
		this.sumOfCharges = sumOfCharges;
	}
	public double getGst() {
		return gst;
	}
	public void setGst(double gst) {
		this.gst = gst;
	}
	public double getSumOfIncludingGst() {
		return sumOfIncludingGst;
	}
	public void setSumOfIncludingGst(double sumOfIncludingGst) {
		this.sumOfIncludingGst = sumOfIncludingGst;
	}
	public double getIntrest() {
		return intrest;
	}
	public void setIntrest(double intrest) {
		this.intrest = intrest;
	}
	public double getEmi() {
		return emi;
	}
	public void setEmi(double emi) {
		this.emi = emi;
	}
	public LocalDate getEmiStartDate() {
		return emiStartDate;
	}
	public void setEmiStartDate(LocalDate emiStartDate) {
		this.emiStartDate = emiStartDate;
	}
	public LocalDate getEmiEndDate() {
		return emiEndDate;
	}
	public void setEmiEndDate(LocalDate emiEndDate) {
		this.emiEndDate = emiEndDate;
	}
	public double getLoanAfterDeduction() {
		return loanAfterDeduction;
	}
	public void setLoanAfterDeduction(double loanAfterDeduction) {
		this.loanAfterDeduction = loanAfterDeduction;
	}
	public String getDiscription() {
		return discription;
	}
	public void setDiscription(String discription) {
		this.discription = discription;
	}
	public double getIgst() {
		return igst;
	}
	public void setIgst(double igst) {
		this.igst = igst;
	}
	public double getCgst() {
		return cgst;
	}
	public void setCgst(double cgst) {
		this.cgst = cgst;
	}
	public double getSgst() {
		return sgst;
	}
	public void setSgst(double sgst) {
		this.sgst = sgst;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public int getTicketCategoryId() {
		return ticketCategoryId;
	}
	public void setTicketCategoryId(int ticketCategoryId) {
		this.ticketCategoryId = ticketCategoryId;
	}
	public String getTicketType() {
		return ticketType;
	}
	public void setTicketType(String ticketType) {
		this.ticketType = ticketType;
	}
	public int getTicketId() {
		return ticketId;
	}
	public void setTicketId(int ticketId) {
		this.ticketId = ticketId;
	}
	
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getPriorityType() {
		return priorityType;
	}
	public void setPriorityType(String priorityType) {
		this.priorityType = priorityType;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAttachment() {
		return attachment;
	}
	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public int getModeId() {
		return modeId;
	}
	public void setModeId(int modeId) {
		this.modeId = modeId;
	}
	public String getModeName() {
		return modeName;
	}
	public void setModeName(String modeName) {
		this.modeName = modeName;
	}
	public int getManualPaymetId() {
		return manualPaymetId;
	}
	public void setManualPaymetId(int manualPaymetId) {
		this.manualPaymetId = manualPaymetId;
	}
	public double getPayAmount() {
		return payAmount;
	}
	public void setPayAmount(double payAmount) {
		this.payAmount = payAmount;
	}
	public Date getCreateAt() {
		return createAt;
	}
	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}
	public String getChequeNumber() {
		return chequeNumber;
	}
	public void setChequeNumber(String chequeNumber) {
		this.chequeNumber = chequeNumber;
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getPayeeName() {
		return payeeName;
	}
	public void setPayeeName(String payeeName) {
		this.payeeName = payeeName;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getAccountHolderName() {
		return accountHolderName;
	}
	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getMicrCode() {
		return micrCode;
	}
	public void setMicrCode(String micrCode) {
		this.micrCode = micrCode;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getDemandDraftNumber() {
		return demandDraftNumber;
	}
	public void setDemandDraftNumber(String demandDraftNumber) {
		this.demandDraftNumber = demandDraftNumber;
	}
	public String getIssuingBank() {
		return issuingBank;
	}
	public void setIssuingBank(String issuingBank) {
		this.issuingBank = issuingBank;
	}
	public String getBranch() {
		return branch;
	}
	public void setBranch(String branch) {
		this.branch = branch;
	}
	public String getCustomerAddress() {
		return customerAddress;
	}
	public void setCustomerAddress(String customerAddress) {
		this.customerAddress = customerAddress;
	}
	public String getReceiptNumber() {
		return receiptNumber;
	}
	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}
	public String getContactInformation() {
		return contactInformation;
	}
	public void setContactInformation(String contactInformation) {
		this.contactInformation = contactInformation;
	}
	public String getPaymentDateTime() {
		return paymentDateTime;
	}
	public void setPaymentDateTime(String paymentDateTime) {
		this.paymentDateTime = paymentDateTime;
	}

	public int getMccId() {
		return mccId;
	}

	public void setMccId(int mccId) {
		this.mccId = mccId;
	}

	public String getCorporateType() {
		return corporateType;
	}

	public void setCorporateType(String corporateType) {
		this.corporateType = corporateType;
	}

	public long getMccCode() {
		return mccCode;
	}

	public void setMccCode(long mccCode) {
		this.mccCode = mccCode;
	}

	

	public int getSmsId() {
		return smsId;
	}

	public void setSmsId(int smsTempId) {
		this.smsId = smsTempId;
	}

	public String getSmsCode() {
		return smsCode;
	}

	public void setSmsCode(String smsTempCode) {
		this.smsCode = smsTempCode;
	}

	public String getSmsMessage() {
		return smsMessage;
	}

	public void setSmsMessage(String smsTempMessage) {
		this.smsMessage = smsTempMessage;
	}

	public String getSmsDescription() {
		return smsDescription;
	}

	public void setSmsDescription(String smsTempDescription) {
		this.smsDescription = smsTempDescription;
	}

	public Date getSmsModifiedDate() {
		return smsModifiedDate;
	}

	public void setSmsModifiedDate(Date smsModifiedDate) {
		this.smsModifiedDate = smsModifiedDate;
	}

	public String getSmsModifiedBy() {
		return smsModifiedBy;
	}

	public void setSmsModifiedBy(String smsModifiedBy) {
		this.smsModifiedBy = smsModifiedBy;
	}

	public boolean isSmsTempStatus() {
		return smsStatus;
	}

	public void setSmsTempStatus(boolean smsTempStatus) {
		this.smsStatus = smsTempStatus;
	}

	public String getSmsEntityId() {
		return smsEntityId;
	}

	public void setSmsEntityId(String smsEntityId) {
		this.smsEntityId = smsEntityId;
	}

	public String getSmsTemplateId() {
		return smsTemplateId;
	}

	public void setSmsTemplateId(String smsTemplateId) {
		this.smsTemplateId = smsTemplateId;
	}

	public String getSmsServiceUrl() {
		return smsServiceUrl;
	}

	public void setSmsServiceUrl(String smsServiceUrl) {
		this.smsServiceUrl = smsServiceUrl;
	}

	public String getSmsUserName() {
		return smsUserName;
	}

	public void setSmsUserName(String smsUserName) {
		this.smsUserName = smsUserName;
	}

	public String getSmsPassword() {
		return smsPassword;
	}

	public void setSmsPassword(String smsPassword) {
		this.smsPassword = smsPassword;
	}

	public String getSmsEnabled() {
		return smsEnabled;
	}

	public void setSmsEnabled(String smsEnabled) {
		this.smsEnabled = smsEnabled;
	}



	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCountryCurrency() {
		return countryCurrency;
	}

	public void setCountryCurrency(String countryCurrency) {
		this.countryCurrency = countryCurrency;
	}

	public String getCountryTimezone() {
		return countryTimezone;
	}

	public void setCountryTimezone(String countryTimezone) {
		this.countryTimezone = countryTimezone;
	}

	

	public Date getModifiedDateAt() {
		return modifiedDateAt;
	}

	public void setModifiedDateAt(Date modifiedDateAt) {
		this.modifiedDateAt = modifiedDateAt;
	}

	public int getDeletedFlag() {
		return deletedFlag;
	}

	public void setDeletedFlag(int deletedFlag) {
		this.deletedFlag = deletedFlag;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}


	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public long getPincodeId() {
		return pincodeId;
	}

	

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}



	

	public boolean isCountryStatus() {
		return countryStatus;
	}

	public void setCountryStatus(boolean countryStatus) {
		this.countryStatus = countryStatus;
	}

	public boolean isStateStatus() {
		return stateStatus;
	}

	public void setStateStatus(boolean stateStatus) {
		this.stateStatus = stateStatus;
	}

	public boolean isCityStatus() {
		return cityStatus;
	}

	public void setCityStatus(boolean cityStatus) {
		this.cityStatus = cityStatus;
	}

	public boolean isPincodeStatus() {
		return pincodeStatus;
	}

	public void setPincodeStatus(boolean pincodeStatus) {
		this.pincodeStatus = pincodeStatus;
	}



	public boolean isSmsStatus() {
		return smsStatus;
	}

	public void setSmsStatus(boolean smsStatus) {
		this.smsStatus = smsStatus;
	}

	public int getServiceId() {
		return serviceId;
	}

	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}

	public String getServices() {
		return services;
	}

	public void setServices(String services) {
		this.services = services;
	}

	public int getCountryId() {
		return countryId;
	}

	public void setCountryId(int countryId) {
		this.countryId = countryId;
	}

	public int getStateId() {
		return stateId;
	}

	public void setStateId(int stateId) {
		this.stateId = stateId;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public void setPincodeId(int pincodeId) {
		this.pincodeId = pincodeId;
	}

}
