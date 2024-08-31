package com.bp.middleware.requestandresponse;

import java.util.Date;

import com.bp.middleware.user.EntityModel;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorVerificationModel;
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
@Table(name="response_table")
public class Response {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="response_id")
	private int responseId;
	@Column(name="reference_id")
	private String referenceId;
	@Column(name="source_type")
	private String sourceType;
	@Column(name="source")
	private String source;
	@Column(name="filing_status")
	private boolean filingStatus;
	@Column(name="encrypted_json")
	private String encryptedJson;
	@Column(name="request_at")
	private Date requestDateAndTime;
	@Column(name="request_by")
	private String requestBy;
	@Column(name="transaction_id")
	private String transactionId;
	@Column(name="otpcode")
	private String otp;
	@Column(name="error")
	private String error;
	@Column(name="error_code")
	private String errorCode;
	@Column(name = "response",columnDefinition = "LONGTEXT")
	private String response;
	@Column(name="response_at")
	private String responseDateAndTime;
	@Column(name="name")
	private String name;
	@Column(name="pan_number")
	private String panNumber;
	@Column(name="dob")
	private String dob;
	@Column(name="ip_address")
	private String ipAddress;

	@Column(name="gst_in")
	private String gstIn;
	@Column(name="business_name")
	private String businessName;

	@Column(name="address")
	private String address;
	@Column(name="date_of_registration")
	private String dateOfRegistration;
	@Column(name="date_of_cancellation")
	private String dateOfCancellation;

	@Column(name="full_name")
	private String fullName;
	@Column(name="aadhaar_number")
	private String aadhaarNumber;
	@Column(name="uan_number")
	private String uanNumber;

	@Column(name="message")
	private String message;
	@Column(name="status")
	private String status;

	@Column(name="client_id")
	private String clientId;
	@Column(name="extracted_data",length=20000)
	private String extractedData;
	@Column(name="common_response",columnDefinition = "LONGTEXT")
	private String commonResponse;
	
	//CIN
	@Column(name="company_id")
	private String companyId;
	@Column(name="company_type")
	private String companyType;
	//DIN
	@Column(name="email")
	private String email;
	//DL
	@Column(name="state")
	private String state;


	//ESIGN
	@Column(name="docket_id")
	private String docketId;
	@Column(name="signer_info")
	private String signerInfo;
	@Column(name="signer_ref_id")
	private String signerRefId;
	@Column(name="signer_id")
	private String signerId;
	@Column(name="document_id")
	private String documentId;
	@Column(name="reference_doc_id")
	private String referenceDocId;
	@Column(name="api_response_id")
	private String apiResponseId;

	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "user_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel user;

	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "request_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private Request request;
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "vendor_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private VendorModel vendorModel;
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "vendor_verification_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private VendorVerificationModel verificationModel;


	public int getResponseId() {
		return responseId;
	}
	public void setResponseId(int responseId) {
		this.responseId = responseId;
	}
	public String getReferenceId() {
		return referenceId;
	}
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}
	public String getSourceType() {
		return sourceType;
	}
	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}


	public String getIpAddress() {
		return ipAddress;
	}
	
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getDocketId() {
		return docketId;
	}
	public void setDocketId(String docketId) {
		this.docketId = docketId;
	}
	public String getSignerInfo() {
		return signerInfo;
	}
	public void setSignerInfo(String signerInfo) {
		this.signerInfo = signerInfo;
	}
	public String getSignerRefId() {
		return signerRefId;
	}
	public void setSignerRefId(String signerRefId) {
		this.signerRefId = signerRefId;
	}
	public String getSignerId() {
		return signerId;
	}
	public void setSignerId(String signerId) {
		this.signerId = signerId;
	}
	public String getDocumentId() {
		return documentId;
	}
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	public String getReferenceDocId() {
		return referenceDocId;
	}
	public void setReferenceDocId(String referenceDocId) {
		this.referenceDocId = referenceDocId;
	}
	public String getApiResponseId() {
		return apiResponseId;
	}
	public void setApiResponseId(String apiResponseId) {
		this.apiResponseId = apiResponseId;
	}
	public boolean isFilingStatus() {
		return filingStatus;
	}
	public void setFilingStatus(boolean filingStatus) {
		this.filingStatus = filingStatus;
	}
	public String getEncryptedJson() {
		return encryptedJson;
	}
	public void setEncryptedJson(String encryptedJson) {
		this.encryptedJson = encryptedJson;
	}
	public Date getRequestDateAndTime() {
		return requestDateAndTime;
	}
	public void setRequestDateAndTime(Date requestDateAndTime) {
		this.requestDateAndTime = requestDateAndTime;
	}
	public String getRequestBy() {
		return requestBy;
	}
	public void setRequestBy(String requestBy) {
		this.requestBy = requestBy;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}

	public EntityModel getUser() {
		return user;
	}
	public void setUser(EntityModel user) {
		this.user = user;
	}


	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPanNumber() {
		return panNumber;
	}
	public void setPanNumber(String panNumber) {
		this.panNumber = panNumber;
	}
	public String getDob() {
		return dob;
	}
	public void setDob(String dob) {
		this.dob = dob;
	}
	public String getGstIn() {
		return gstIn;
	}
	public void setGstIn(String gstIn) {
		this.gstIn = gstIn;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getDateOfCancellation() {
		return dateOfCancellation;
	}
	public void setDateOfCancellation(String dateOfCancellation) {
		this.dateOfCancellation = dateOfCancellation;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getDateOfRegistration() {
		return dateOfRegistration;
	}
	public void setDateOfRegistration(String dateOfRegistration) {
		this.dateOfRegistration = dateOfRegistration;
	}

	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getAadhaarNumber() {
		return aadhaarNumber;
	}
	public void setAadhaarNumber(String aadhaarNumber) {
		this.aadhaarNumber = aadhaarNumber;
	}


	public String getResponseDateAndTime() {
		return responseDateAndTime;
	}
	public void setResponseDateAndTime(String responseDateAndTime) {
		this.responseDateAndTime = responseDateAndTime;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Request getRequest() {
		return request;
	}
	public void setRequest(Request request) {
		this.request = request;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getCompanyId() {
		return companyId;
	}
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	public String getCompanyType() {
		return companyType;
	}
	public void setCompanyType(String companyType) {
		this.companyType = companyType;
	}
	public VendorModel getVendorModel() {
		return vendorModel;
	}
	public void setVendorModel(VendorModel vendorModel) {
		this.vendorModel = vendorModel;
	}
	public String getExtractedData() {
		return extractedData;
	}
	public void setExtractedData(String extractedData) {
		this.extractedData = extractedData;
	}
	public String getCommonResponse() {
		return commonResponse;
	}
	public void setCommonResponse(String commonResponse) {
		this.commonResponse = commonResponse;
	}
	public String getUanNumber() {
		return uanNumber;
	}
	public void setUanNumber(String uanNumber) {
		this.uanNumber = uanNumber;
	}
	public VendorVerificationModel getVerificationModel() {
		return verificationModel;
	}
	public void setVerificationModel(VendorVerificationModel verificationModel) {
		this.verificationModel = verificationModel;
	}


}
