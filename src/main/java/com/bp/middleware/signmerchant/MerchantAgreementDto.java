package com.bp.middleware.signmerchant;

import com.bp.middleware.bond.MerchantBond;
import com.bp.middleware.user.EntityModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size;

public class MerchantAgreementDto {

	
	private String merchantName;
	private String merchantCompanyName;
	private String buildingNumber;
	private String streetAddress;
	private String cityPincode;
	private String companyAddress;
	private String companyWebsite;
	private String agreementDate;
	private String bankName;
	private String accountNumber;
	private String ifscCode;
	private String accountHolderName;
	private String businessCategory;
	private String businessSubCategory;
	private String branchAddress;
	private String signatoryDesignation;
	private String document;
	private String pdfDocument;
	private String documentId;
	private String uploadDocumentAt;
	private String documentExpiryAt;
	private String documentTitle;
	private String description;
	private String bond;
	private String systemOrSetupFee;
	private String amcOrMmc;
	private String securityDeposit;
	private String serviceFee;
	private String netBanking;
	private String creditCard;
	private String debitCard;
	private String eCollect;
	private String emiAbove5000;
	private String internationalCard;
	private String amexCard;
	private String dinersCard;
	private String corporateOrCommercialCard;
	private String prepaidCard;
	private String wallets;
	private String upi;
	private String upiIntent;
	private String bharathQr;
	private String settlementTimeFrame;
	private String vas;
	private String others;
	private String emi;

	private MerchantBond merchantBond;
}
