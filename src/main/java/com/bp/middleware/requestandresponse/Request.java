package com.bp.middleware.requestandresponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import com.bp.middleware.signmerchant.MerchantModel;
import com.bp.middleware.user.EntityModel;
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
@Table(name="request_table")
public class Request {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="request_id")
	private int requestId;
	@Column(name="attempt")
	private int attempt;
	@Column(name="reference_id")
	private String referenceId;
	@Column(name="source_type")
	private String sourceType;
	@Column(name="source")
	private String source;
	@Column(name="filing_status")
	private boolean filingStatus;
	@Column(name="request_at")
	private Date requestDateAndTime;
	@Column(name="request_by")
	private String requestBy;
	@Column(name="transaction_id")
	private String transactionId;
	@Column(name="otpcode")
	private String otp;	
	@Column(name="response_at")
	private String responseDateAndTime;
	@Column(name="message")
	private String message;
	@Column(name="status")
	private String status;
	@Column(name="error")
	private String error;
	@Column(name="error_code")
	private String errorCode;
	@Column(name="client_id")
	private String clientId;
	@Column(name="extracted_data")
	private String extractedData;
	@Column(name="full_name")
	private String fullName;
	@Column(name="aadhar_number")
	private String aadharNumber;
	@Column(name="dob")
	private String dob;
	@Column(name="price")
	private double price;
	@Column(name="ip_address")
	private String ipAddress;
	@Column(name="free_hit",columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean freeHit;
	@Column(name="consider",columnDefinition = "BOOLEAN DEFAULT TRUE")
	private boolean consider;
	
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
	
	//BP DIGITAL SIGN
	@Column(name="document_title")
	private String documentTitle;
	@Column(name="bond")
	private boolean bond;
	@Column(name="doc_expiry_at")
	private String docExpiryAt;
	@Column(name="bond_amount")
	private double bondAmount;
	@Column(name="signer_count")
	private int signerCount;
	
	
	//CIN
	@Column(name="company_name")
	private String companyName;
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
		
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "user_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel user;
	
	@ManyToOne(fetch = FetchType.EAGER,optional = false)
	@JoinColumn(name = "vendor_verification_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private VendorVerificationModel verificationModel;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "merchant_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private MerchantModel merchant;
	
	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
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

	public String getEmail() {
		return email;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getSourceType() {
		return sourceType;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getAadharNumber() {
		return aadharNumber;
	}

	public void setAadharNumber(String aadharNumber) {
		this.aadharNumber = aadharNumber;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
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
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getExtractedData() {
		return extractedData;
	}

	public void setExtractedData(String extractedData) {
		this.extractedData = extractedData;
	}

	public boolean isFilingStatus() {
		return filingStatus;
	}

	public void setFilingStatus(boolean filingStatus) {
		this.filingStatus = filingStatus;
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

	public EntityModel getUser() {
		return user;
	}

	public void setUser(EntityModel user) {
		this.user = user;
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

	public VendorVerificationModel getVerificationModel() {
		return verificationModel;
	}

	public void setVerificationModel(VendorVerificationModel verificationModel) {
		this.verificationModel = verificationModel;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = twoDecimelDouble(price);
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
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

	public int getAttempt() {
		return attempt;
	}

	public void setAttempt(int attempt) {
		this.attempt = attempt;
	}

	public String getDocumentTitle() {
		return documentTitle;
	}

	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}

	public boolean isBond() {
		return bond;
	}

	public void setBond(boolean bond) {
		this.bond = bond;
	}

	public String getDocExpiryAt() {
		return docExpiryAt;
	}

	public void setDocExpiryAt(String docExpiryAt) {
		this.docExpiryAt = docExpiryAt;
	}

	public double getBondAmount() {
		return bondAmount;
	}

	public void setBondAmount(double bondAmount) {
		this.bondAmount = twoDecimelDouble(bondAmount);
	}

	public int getSignerCount() {
		return signerCount;
	}

	public void setSignerCount(int signerCount) {
		this.signerCount = signerCount;
	}

	public MerchantModel getMerchant() {
		return merchant;
	}

	public void setMerchant(MerchantModel merchant) {
		this.merchant = merchant;
	}

	public boolean isFreeHit() {
		return freeHit;
	}

	public void setFreeHit(boolean freeHit) {
		this.freeHit = freeHit;
	}
	
	public boolean isConsider() {
		return consider;
	}

	public void setConsider(boolean consider) {
		this.consider = consider;
	}

	private double twoDecimelDouble(double value) {
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places

		double roundedValue = bd.doubleValue();
		
		return roundedValue;
	}
}
