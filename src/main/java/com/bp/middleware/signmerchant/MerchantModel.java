package com.bp.middleware.signmerchant;

import java.time.LocalDate;
import java.util.Date;

import com.bp.middleware.bond.MerchantBond;
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
import jakarta.validation.constraints.Size;

@Entity
@Table(name="merchant_details")
public class MerchantModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="merchant_id")
	private int merchantId;
	@Column(name="merchant_name")
	private String merchantName;
	@Column(name="merchant_company_name")
	private String merchantCompanyName;
	
	@Column(name="building_number")
	private String buildingNumber;
	@Column(name="street_address")
	private String streetAddress;
	@Column(name="city_pincode")
	private String cityPincode;
	
	@Column(name="company_address")
	private String companyAddress;
	@Column(name="company_website")
	private String companyWebsite;
	@Column(name="agreement_date")
	private String agreementDate;
//	@Column(name="rate_agreed")
//	private String rateAgreed;
	@Column(name="bank_name")
	private String bankName;
	@Column(name="account_number")
	private String accountNumber;
//	@Column(name="account_type")
//	private String accountType;
	@Column(name="ifsc_code")
	private String ifscCode;
//	@Column(name="micr_code")
//	private String micrCode;
	@Column(name="account_holder_name")
	private String accountHolderName;
	@Column(name="business_category")
	private String businessCategory;
	@Column(name="business_sub_category")
	private String businessSubCategory;
	@Column(name="branch_address")
	private String branchAddress;
//	@Column(name="signatory_name")
//	private String signatoryName;
	@Column(name="signatory_designation")
	private String signatoryDesignation;
//	@Column(name="signatory_date")
//	private String signatoryDate;
	@Column(name="document")
	private String document;
	@Column(name="pdf_document")
	private String pdfDocument;
	@Column(name="document_id")
	private String documentId;
	@Column(name="upload_document_at")
	private String uploadDocumentAt;
	@Column(name="document_expiry_at")
	private LocalDate documentExpiryAt;
//	@Column(name="signed_report")
//	private String singedReport;
	@Column(name="document_title")
	private String documentTitle;
	@Column(name="description")
	@Size(max=5000)
	private String description;
//	@Column(name="created_by")
//	private String createdBy;
//	@Column(name="created_at")
//	private Date createdAt;
//	@Column(name="modified_by")
//	private String modifiedBy;
//	@Column(name="modified_at")
//	private Date modifiedAt;
	@Column(name="bond")
	private boolean bond;
	@Column(name="system_or_setup_fee")
	private String systemOrSetupFee;
	@Column(name="amc_or_mmc")
	private String amcOrMmc;
	@Column(name="security_deposit")
	private String securityDeposit;
	@Column(name="Service_fee")
	private String serviceFee;
	@Column(name="net_banking")
	private String netBanking;
	@Column(name="credit_card")
	private String creditCard;
	@Column(name="debit_card")
	private String debitCard;
	@Column(name="e_collect")
	private String eCollect;
	@Column(name="emi_above5000")
	private String emiAbove5000;
	@Column(name="international_card")
	private String internationalCard;
	@Column(name="amex_card")
	private String amexCard;
	@Column(name="diners_card")
	private String dinersCard;
	@Column(name="corporate_or_commercial_card")
	private String corporateOrCommercialCard;
	@Column(name="prepaid_card")
	private String prepaidCard;
	@Column(name="wallets")
	private String wallets;
	@Column(name="upi")
	private String upi;
	@Column(name="upi_intent")
	private String upiIntent;
	@Column(name="bharath_qr")
	private String bharathQr;
	@Column(name="settlement_time_frame")
	private String settlementTimeFrame;
	@Column(name="vas")
	private String vas;
	@Column(name="others")
	private String others;
	@Column(name="emi")
	private String emi;
	@Column(name="created_at")
	private Date createdAt;
	@Column(name="expired",columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean expired;
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "user_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel entity;
	
	@ManyToOne(fetch = FetchType.LAZY,optional = true)
	@JoinColumn(name = "bond_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private MerchantBond merchantBond;
	
	
	public int getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getCompanyAddress() {
		return companyAddress;
	}
	public void setCompanyAddress(String companyAddress) {
		this.companyAddress = companyAddress;
	}
	public String getCompanyWebsite() {
		return companyWebsite;
	}
	public void setCompanyWebsite(String companyWebsite) {
		this.companyWebsite = companyWebsite;
	}
	public String getAgreementDate() {
		return agreementDate;
	}
	public void setAgreementDate(String agreementDate) {
		this.agreementDate = agreementDate;
	}

	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getIfscCode() {
		return ifscCode;
	}
	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	public String getAccountHolderName() {
		return accountHolderName;
	}
	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}
	public String getBusinessCategory() {
		return businessCategory;
	}
	public void setBusinessCategory(String businessCategory) {
		this.businessCategory = businessCategory;
	}
	public String getBusinessSubCategory() {
		return businessSubCategory;
	}
	public void setBusinessSubCategory(String businessSubCategory) {
		this.businessSubCategory = businessSubCategory;
	}
	public String getBranchAddress() {
		return branchAddress;
	}
	public void setBranchAddress(String branchAddress) {
		this.branchAddress = branchAddress;
	}
	public String getMerchantCompanyName() {
		return merchantCompanyName;
	}
	public void setMerchantCompanyName(String merchantCompanyName) {
		this.merchantCompanyName = merchantCompanyName;
	}

	public String getSignatoryDesignation() {
		return signatoryDesignation;
	}
	public void setSignatoryDesignation(String signatoryDesignation) {
		this.signatoryDesignation = signatoryDesignation;
	}

	public String getDocument() {
		return document;
	}
	public void setDocument(String document) {
		this.document = document;
	}
	public String getPdfDocument() {
		return pdfDocument;
	}
	public void setPdfDocument(String pdfDocument) {
		this.pdfDocument = pdfDocument;
	}
	public String getDocumentId() {
		return documentId;
	}
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	 
	public EntityModel getEntity() {
		return entity;
	}
	public void setEntity(EntityModel entity) {
		this.entity = entity;
	}
	public String getUploadDocumentAt() {
		return uploadDocumentAt;
	}
	public void setUploadDocumentAt(String uploadDocumentAt) {
		this.uploadDocumentAt = uploadDocumentAt;
	}


	public LocalDate getDocumentExpiryAt() {
		return documentExpiryAt;
	}
	public void setDocumentExpiryAt(LocalDate documentExpiryAt) {
		this.documentExpiryAt = documentExpiryAt;
	}
	public String getDocumentTitle() {
		return documentTitle;
	}
	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isBond() {
		return bond;
	}
	public void setBond(boolean bond) {
		this.bond = bond;
	}
	public MerchantBond getMerchantBond() {
		return merchantBond;
	}
	public void setMerchantBond(MerchantBond merchantBond) {
		this.merchantBond = merchantBond;
	}
	public String getSystemOrSetupFee() {
		return systemOrSetupFee;
	}
	public void setSystemOrSetupFee(String systemOrSetupFee) {
		this.systemOrSetupFee = systemOrSetupFee;
	}
	public String getAmcOrMmc() {
		return amcOrMmc;
	}
	public void setAmcOrMmc(String amcOrMmc) {
		this.amcOrMmc = amcOrMmc;
	}
	public String getSecurityDeposit() {
		return securityDeposit;
	}
	public void setSecurityDeposit(String securityDeposit) {
		this.securityDeposit = securityDeposit;
	}

	public String getServiceFee() {
		return serviceFee;
	}
	public void setServiceFee(String serviceFee) {
		this.serviceFee = serviceFee;
	}
	public String getNetBanking() {
		return netBanking;
	}
	public void setNetBanking(String netBanking) {
		this.netBanking = netBanking;
	}
	public String getCreditCard() {
		return creditCard;
	}
	public void setCreditCard(String creditCard) {
		this.creditCard = creditCard;
	}
	public String getDebitCard() {
		return debitCard;
	}
	public void setDebitCard(String debitCard) {
		this.debitCard = debitCard;
	}
	public String geteCollect() {
		return eCollect;
	}
	public void seteCollect(String eCollect) {
		this.eCollect = eCollect;
	}
	public String getEmiAbove5000() {
		return emiAbove5000;
	}
	public void setEmiAbove5000(String emiAbove5000) {
		this.emiAbove5000 = emiAbove5000;
	}
	public String getInternationalCard() {
		return internationalCard;
	}
	public void setInternationalCard(String internationalCard) {
		this.internationalCard = internationalCard;
	}
	public String getAmexCard() {
		return amexCard;
	}
	public void setAmexCard(String amexCard) {
		this.amexCard = amexCard;
	}
	public String getDinersCard() {
		return dinersCard;
	}
	public void setDinersCard(String dinersCard) {
		this.dinersCard = dinersCard;
	}
	public String getCorporateOrCommercialCard() {
		return corporateOrCommercialCard;
	}
	public void setCorporateOrCommercialCard(String corporateOrCommercialCard) {
		this.corporateOrCommercialCard = corporateOrCommercialCard;
	}
	public String getPrepaidCard() {
		return prepaidCard;
	}
	public void setPrepaidCard(String prepaidCard) {
		this.prepaidCard = prepaidCard;
	}
	public String getWallets() {
		return wallets;
	}
	public void setWallets(String wallets) {
		this.wallets = wallets;
	}
	public String getUpi() {
		return upi;
	}
	public void setUpi(String upi) {
		this.upi = upi;
	}
	public String getUpiIntent() {
		return upiIntent;
	}
	public void setUpiIntent(String upiIntent) {
		this.upiIntent = upiIntent;
	}
	public String getBharathQr() {
		return bharathQr;
	}
	public void setBharathQr(String bharathQr) {
		this.bharathQr = bharathQr;
	}
	public String getSettlementTimeFrame() {
		return settlementTimeFrame;
	}
	public void setSettlementTimeFrame(String settlementTimeFrame) {
		this.settlementTimeFrame = settlementTimeFrame;
	}
	public String getVas() {
		return vas;
	}
	public void setVas(String vas) {
		this.vas = vas;
	}
	public String getOthers() {
		return others;
	}
	public void setOthers(String others) {
		this.others = others;
	}
	public String getEmi() {
		return emi;
	}
	public void setEmi(String emi) {
		this.emi = emi;
	}
	public String getBuildingNumber() {
		return buildingNumber;
	}
	public void setBuildingNumber(String buildingNumber) {
		this.buildingNumber = buildingNumber;
	}
	public String getStreetAddress() {
		return streetAddress;
	}
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}
	public String getCityPincode() {
		return cityPincode;
	}
	public void setCityPincode(String cityPincode) {
		this.cityPincode = cityPincode;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public boolean isExpired() {
		return expired;
	}
	public void setExpired(boolean expired) {
		this.expired = expired;
	}
	
	

	
	
	
	
} 
