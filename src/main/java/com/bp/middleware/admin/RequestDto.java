package com.bp.middleware.admin;

import org.springframework.web.multipart.MultipartFile;

public class RequestDto {
	private String workerName; 
	private String mobileNumber; 
	private Double ratings;
	private String totalWorkingHouse;
	private String status;
	private String fileType;
	private String availableTime;
	private int societyId;
	private String workingTime;
	private String panCardNumber;
	private String bankName;
	private String bankAccountName;
	private String bankAccountNumber;
	private String iFSCCode;
	private String bankBranch; 
	private String contactName; 
	private String emergencyContactNumber; 
	private int dailyhelpId;
	private String relationshipStatus;
	private String bloodGroup;
	private String hospitalName;
	private String hospitalNumber;
	private MultipartFile uploadfiles;
	private String aadhaarCardNumber;
	private int addflatid;
	private String visitorsName;
	private String visitorsEmail;
	private String visitorsMobileNumber;
	private String visitorsType;
	private int profileid;
	private int flatId;
	private int societySecurityId;
	private MultipartFile visitorsPhoto;
	private String assignedByName;
	private String password;
	private String address; 
	private String email;
	private int raiseCategoryId;
	private String registrationNumber;
	private int dailHelpKycDocumentId;
	private int dailyHelpWorkerId;
	private String cardNumber;
	private int ticketWorkerDocumentId;
	private int ticketWorkerId;
	private int documentId;
	private int homeId;
	private int userId;
	private String recieverMessage;
	private String receiverMobileNumber;
	private String entityId;
	private String templateId;
	private String smsUrl;
	private String smsUser;
	private String smsPwd;
	private String smsEnable;
	
	public String getWorkerName() {
		return workerName;
	}
	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public Double getRatings() {
		return ratings;
	}
	public void setRatings(Double ratings) {
		this.ratings = ratings;
	}
	public String getTotalWorkingHouse() {
		return totalWorkingHouse;
	}
	public void setTotalWorkingHouse(String totalWorkingHouse) {
		this.totalWorkingHouse = totalWorkingHouse;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getAvailableTime() {
		return availableTime;
	}
	public void setAvailableTime(String availableTime) {
		this.availableTime = availableTime;
	}
	public int getSocietyId() {
		return societyId;
	}
	public void setSocietyId(int societyId) {
		this.societyId = societyId;
	}
	public String getWorkingTime() {
		return workingTime;
	}
	public void setWorkingTime(String workingTime) {
		this.workingTime = workingTime;
	}
	public String getPanCardNumber() {
		return panCardNumber;
	}
	public void setPanCardNumber(String panCardNumber) {
		this.panCardNumber = panCardNumber;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getBankAccountName() {
		return bankAccountName;
	}
	public void setBankAccountName(String bankAccountName) {
		this.bankAccountName = bankAccountName;
	}
	public String getBankAccountNumber() {
		return bankAccountNumber;
	}
	public void setBankAccountNumber(String bankAccountNumber) {
		this.bankAccountNumber = bankAccountNumber;
	}
	public String getiFSCCode() {
		return iFSCCode;
	}
	public void setiFSCCode(String iFSCCode) {
		this.iFSCCode = iFSCCode;
	}
	public String getBankBranch() {
		return bankBranch;
	}
	public void setBankBranch(String bankBranch) {
		this.bankBranch = bankBranch;
	}
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public String getEmergencyContactNumber() {
		return emergencyContactNumber;
	}
	public void setEmergencyContactNumber(String emergencyContactNumber) {
		this.emergencyContactNumber = emergencyContactNumber;
	}
	public int getDailyhelpId() {
		return dailyhelpId;
	}
	public void setDailyhelpId(int dailyhelpId) {
		this.dailyhelpId = dailyhelpId;
	}
	public String getRelationshipStatus() {
		return relationshipStatus;
	}
	public void setRelationshipStatus(String relationshipStatus) {
		this.relationshipStatus = relationshipStatus;
	}
	public String getBloodGroup() {
		return bloodGroup;
	}
	public void setBloodGroup(String bloodGroup) {
		this.bloodGroup = bloodGroup;
	}
	public String getHospitalName() {
		return hospitalName;
	}
	public void setHospitalName(String hospitalName) {
		this.hospitalName = hospitalName;
	}
	public String getHospitalNumber() {
		return hospitalNumber;
	}
	public void setHospitalNumber(String hospitalNumber) {
		this.hospitalNumber = hospitalNumber;
	}
	public MultipartFile getUploadfiles() {
		return uploadfiles;
	}
	public void setUploadfiles(MultipartFile uploadfiles) {
		this.uploadfiles = uploadfiles;
	}
	
	public String getAadhaarCardNumber() {
		return aadhaarCardNumber;
	}
	public void setAadhaarCardNumber(String aadhaarCardNumber) {
		this.aadhaarCardNumber = aadhaarCardNumber;
	}
	public int getAddflatid() {
		return addflatid;
	}
	public void setAddflatid(int addflatid) {
		this.addflatid = addflatid;
	}
	public String getVisitorsName() {
		return visitorsName;
	}
	public void setVisitorsName(String visitorsName) {
		this.visitorsName = visitorsName;
	}
	public String getVisitorsEmail() {
		return visitorsEmail;
	}
	public void setVisitorsEmail(String visitorsEmail) {
		this.visitorsEmail = visitorsEmail;
	}
	public String getVisitorsMobileNumber() {
		return visitorsMobileNumber;
	}
	public void setVisitorsMobileNumber(String visitorsMobileNumber) {
		this.visitorsMobileNumber = visitorsMobileNumber;
	}
	public String getVisitorsType() {
		return visitorsType;
	}
	public void setVisitorsType(String visitorsType) {
		this.visitorsType = visitorsType;
	}
	public int getProfileid() {
		return profileid;
	}
	public void setProfileid(int profileid) {
		this.profileid = profileid;
	}
	public int getFlatId() {
		return flatId;
	}
	public void setFlatId(int flatId) {
		this.flatId = flatId;
	}
	public int getSocietySecurityId() {
		return societySecurityId;
	}
	public void setSocietySecurityId(int societySecurityId) {
		this.societySecurityId = societySecurityId;
	}
	public MultipartFile getVisitorsPhoto() {
		return visitorsPhoto;
	}
	public void setVisitorsPhoto(MultipartFile visitorsPhoto) {
		this.visitorsPhoto = visitorsPhoto;
	}
	public String getAssignedByName() {
		return assignedByName;
	}
	public void setAssignedByName(String assignedByName) {
		this.assignedByName = assignedByName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public int getRaiseCategoryId() {
		return raiseCategoryId;
	}
	public void setRaiseCategoryId(int raiseCategoryId) {
		this.raiseCategoryId = raiseCategoryId;
	}
	public String getRegistrationNumber() {
		return registrationNumber;
	}
	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}
	public int getDailHelpKycDocumentId() {
		return dailHelpKycDocumentId;
	}
	public void setDailHelpKycDocumentId(int dailHelpKycDocumentId) {
		this.dailHelpKycDocumentId = dailHelpKycDocumentId;
	}
	public int getDailyHelpWorkerId() {
		return dailyHelpWorkerId;
	}
	public void setDailyHelpWorkerId(int dailyHelpWorkerId) {
		this.dailyHelpWorkerId = dailyHelpWorkerId;
	}
	public String getCardNumber() {
		return cardNumber;
	}
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public int getTicketWorkerDocumentId() {
		return ticketWorkerDocumentId;
	}
	public void setTicketWorkerDocumentId(int ticketWorkerDocumentId) {
		this.ticketWorkerDocumentId = ticketWorkerDocumentId;
	}
	public int getTicketWorkerId() {
		return ticketWorkerId;
	}
	public void setTicketWorkerId(int ticketWorkerId) {
		this.ticketWorkerId = ticketWorkerId;
	}
	public int getDocumentId() {
		return documentId;
	}
	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}
	public int getHomeId() {
		return homeId;
	}
	public void setHomeId(int homeId) {
		this.homeId = homeId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getRecieverMessage() {
		return recieverMessage;
	}
	public void setRecieverMessage(String recieverMessage) {
		this.recieverMessage = recieverMessage;
	}
	public String getReceiverMobileNumber() {
		return receiverMobileNumber;
	}
	public void setReceiverMobileNumber(String receiverMobileNumber) {
		this.receiverMobileNumber = receiverMobileNumber;
	}
	public String getEntityId() {
		return entityId;
	}
	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}
	public String getTemplateId() {
		return templateId;
	}
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
	public String getSmsUrl() {
		return smsUrl;
	}
	public void setSmsUrl(String smsUrl) {
		this.smsUrl = smsUrl;
	}
	public String getSmsUser() {
		return smsUser;
	}
	public void setSmsUser(String smsUser) {
		this.smsUser = smsUser;
	}
	public String getSmsPwd() {
		return smsPwd;
	}
	public void setSmsPwd(String smsPwd) {
		this.smsPwd = smsPwd;
	}
	public String getSmsEnable() {
		return smsEnable;
	}
	public void setSmsEnable(String smsEnable) {
		this.smsEnable = smsEnable;
	}
	

}
