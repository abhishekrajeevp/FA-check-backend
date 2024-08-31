package com.bp.middleware.util;

import java.sql.Timestamp;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;


public class CommonRequestDto {
	

	public CommonRequestDto() {
		super();
	}
	

	public CommonRequestDto(double igst, double cgst, double sgst, long prodCategoryId, Double paidAmount,
			String pgMode, int pgOnOffStatus, int pgSwitch, long mandateDocId, int businessCategoryId,
			int businessTypeId, int corporateId, int customerId, String paymentId, String paymentStatus,
			String paymentDateTime, long merchantProdId, String trackId, String receiptNumber, String invoiceNumber,
			String description, String receiptFile, Double cgstPercentage, Double sgstPercentage, Double igstPercentage,
			String kycDocName, String accountType, String bankName, String branch, String accountNumber,
			String confirmAccountNumber, long aadharNo, String ifscCode, String beneficiaryName,
			String approvalStatusL1, String approvalStatusL2, String approvedByL1, String approvedByL2,
			String approvalDateTime, long id, String userName, String userPassword, String password,
			String emailAddress, String mobileNumber, String otpCode, int accountStatus, int roleId, String address,
			String pincode, long pincodeId, Timestamp createddateandtime, int pincodestatus, long cityid, int userId,
			String createdBy, String createdDateTime, String modifiedBy, Timestamp modifiedDateTime, int deletedFlag,
			String categoryName, String flag, String cardNumber, String adminApproval, String superAdminApproval,
			Long merchantId, String approvalBy, String newPassword, String entityName, String merchantLegalName,
			String accountDisplayName, String referenceNo, String website, String gstIn, String contactName,
			String contactMobile, String contactEmail, String billingAddress, String area, String zipcode, String city,
			String firebaseId, String ipaddress, String deviceType, String deviceModel, String deviceNumber,
			String deviceName, long stateId, long categoryId, MultipartFile docBackPath, MultipartFile docFrontPath,
			String cityName, int cityStatus, String productName, double setupFee, double settlementFee, double mmcFee,
			double txnFee, double amcFee, int productActiveStatus, String countryName, Timestamp createdDatetime2,
			String countryTimezone, String roleName, String stateName, String productPaymentStatus, int productQuantity,
			String productCategoryName, double productStandAmount, double perTransactionFee, double prepaidMDR,
			double creditCard, double netBanking, double wallet, double instantEmi,
			double internationalCCAndDCTransactions, double amex, String effectiveDate, long hsnCode,
			String flatOrPercentage, String docType, String fileType, double debitCardTransactionBelowPercent,
			double debitCardTransactionAbovePercent, double upiBelowPercentage, double upiAbovePercentage,
			double upiBelowAmount, double upiAboveAmount, double debitCardTransactionBelowAmount,
			double debitCardTransactionAboveAmount, String taxType, long transactionId, int mccId, String merchantType,
			long mccCode, String modifiedDateAndTime, String comment, String paymentMethod, String bankReference,
			String merchantOrderNo, String cardNoMasked, String cardExpiry, String customerName, String customerEmail,
			String customerMobile, String adderess, String postalCode, String settlementStatus,
			String settlementDateTime, String settlementReason, String settlementUTRNumber, JsonNode etc, String apikey,
			String secretkey, String orderReference, String approvedDateTimeL1, String approvedDateTimeL2,
			String roleCode, String countryCode, String countryCurrency) {
		super();
		this.igst = igst;
		this.cgst = cgst;
		this.sgst = sgst;
		this.prodCategoryId = prodCategoryId;
		this.paidAmount = paidAmount;
		this.pgMode = pgMode;
		this.pgOnOffStatus = pgOnOffStatus;
		this.pgSwitch = pgSwitch;
		this.mandateDocId = mandateDocId;
		this.businessCategoryId = businessCategoryId;
		this.businessTypeId = businessTypeId;
		this.corporateId = corporateId;
		this.customerId = customerId;
		this.paymentId = paymentId;
		this.paymentStatus = paymentStatus;
		this.paymentDateTime = paymentDateTime;
		this.merchantProdId = merchantProdId;
		this.trackId = trackId;
		this.receiptNumber = receiptNumber;
		this.invoiceNumber = invoiceNumber;
		this.description = description;
		this.receiptFile = receiptFile;
		this.cgstPercentage = cgstPercentage;
		this.sgstPercentage = sgstPercentage;
		this.igstPercentage = igstPercentage;
		this.kycDocName = kycDocName;
		this.accountType = accountType;
		this.bankName = bankName;
		this.branch = branch;
		this.accountNumber = accountNumber;
		this.confirmAccountNumber = confirmAccountNumber;
		this.aadharNo = aadharNo;
		this.ifscCode = ifscCode;
		this.beneficiaryName = beneficiaryName;
		this.approvalStatusL1 = approvalStatusL1;
		this.approvalStatusL2 = approvalStatusL2;
		this.approvedByL1 = approvedByL1;
		this.approvedByL2 = approvedByL2;
		this.approvalDateTime = approvalDateTime;
		this.id = id;
		this.userName = userName;
		this.userPassword = userPassword;
		this.password = password;
		this.emailAddress = emailAddress;
		this.mobileNumber = mobileNumber;
		this.otpCode = otpCode;
		this.accountStatus = accountStatus;
		this.roleId = roleId;
		this.address = address;
		this.pincode = pincode;
		this.pincodeId = pincodeId;
		this.createddateandtime = createddateandtime;
		this.pincodestatus = pincodestatus;
		this.cityid = cityid;
		this.userId = userId;
		this.createdBy = createdBy;
		this.createdDateTime = createdDateTime;
		this.modifiedBy = modifiedBy;
		this.modifiedDateTime = modifiedDateTime;
		this.deletedFlag = deletedFlag;
		this.categoryName = categoryName;
		this.flag = flag;
		this.cardNumber = cardNumber;
		this.adminApproval = adminApproval;
		this.superAdminApproval = superAdminApproval;
		this.merchantId = merchantId;
		this.approvalBy = approvalBy;
		this.newPassword = newPassword;
		this.entityName = entityName;
		this.merchantLegalName = merchantLegalName;
		this.accountDisplayName = accountDisplayName;
		this.referenceNo = referenceNo;
		this.website = website;
		this.gstIn = gstIn;
		this.contactName = contactName;
		this.contactMobile = contactMobile;
		this.contactEmail = contactEmail;
		this.billingAddress = billingAddress;
		this.area = area;
		this.zipcode = zipcode;
		this.city = city;
		this.firebaseId = firebaseId;
		this.ipaddress = ipaddress;
		this.deviceType = deviceType;
		this.deviceModel = deviceModel;
		this.deviceNumber = deviceNumber;
		this.deviceName = deviceName;
		this.stateId = stateId;
		this.categoryId = categoryId;
		this.docBackPath = docBackPath;
		this.docFrontPath = docFrontPath;
		this.cityName = cityName;
		this.cityStatus = cityStatus;
		this.productName = productName;
		this.setupFee = setupFee;
		this.settlementFee = settlementFee;
		this.mmcFee = mmcFee;
		this.txnFee = txnFee;
		this.amcFee = amcFee;
		this.productActiveStatus = productActiveStatus;
		this.countryName = countryName;
		this.createdAt = createdDatetime2;
		this.countryTimezone = countryTimezone;
		this.roleName = roleName;
		this.stateName = stateName;
		this.productPaymentStatus = productPaymentStatus;
		this.productQuantity = productQuantity;
		this.productCategoryName = productCategoryName;
		this.productStandAmount = productStandAmount;
		this.perTransactionFee = perTransactionFee;
		this.prepaidMDR = prepaidMDR;
		this.creditCard = creditCard;
		this.netBanking = netBanking;
		this.wallet = wallet;
		this.instantEmi = instantEmi;
		this.internationalCCAndDCTransactions = internationalCCAndDCTransactions;
		this.amex = amex;
		this.effectiveDate = effectiveDate;
		this.hsnCode = hsnCode;
		this.flatOrPercentage = flatOrPercentage;
		this.docType = docType;
		this.fileType = fileType;
		this.debitCardTransactionBelowPercent = debitCardTransactionBelowPercent;
		this.debitCardTransactionAbovePercent = debitCardTransactionAbovePercent;
		this.upiBelowPercentage = upiBelowPercentage;
		this.upiAbovePercentage = upiAbovePercentage;
		this.upiBelowAmount = upiBelowAmount;
		this.upiAboveAmount = upiAboveAmount;
		this.debitCardTransactionBelowAmount = debitCardTransactionBelowAmount;
		this.debitCardTransactionAboveAmount = debitCardTransactionAboveAmount;
		this.taxType = taxType;
		this.transactionId = transactionId;
		this.mccId = mccId;
		this.merchantType = merchantType;
		this.mccCode = mccCode;
		this.modifiedDateAndTime = modifiedDateAndTime;
		this.comment = comment;
		this.paymentMethod = paymentMethod;
		this.bankReference = bankReference;
		this.merchantOrderNo = merchantOrderNo;
		this.cardNoMasked = cardNoMasked;
		this.cardExpiry = cardExpiry;
		this.customerName = customerName;
		this.customerEmail = customerEmail;
		this.customerMobile = customerMobile;
		this.adderess = adderess;
		this.postalCode = postalCode;
		this.settlementStatus = settlementStatus;
		this.settlementDateTime = settlementDateTime;
		this.settlementReason = settlementReason;
		this.settlementUTRNumber = settlementUTRNumber;
		this.etc = etc;
		this.apikey = apikey;
		this.secretkey = secretkey;
		this.orderReference = orderReference;
		this.approvedDateTimeL1 = approvedDateTimeL1;
		this.approvedDateTimeL2 = approvedDateTimeL2;
		this.roleCode = roleCode;
		this.countryCode = countryCode;
		this.countryCurrency = countryCurrency;
	}


	private double igst;
	private double cgst;
	private double sgst;
	private long prodCategoryId;
	private Double paidAmount;
	private String pgMode;
	private int pgOnOffStatus;
	private int pgSwitch;
	private long mandateDocId;
	private int businessCategoryId;
	private int businessTypeId;
	private int corporateId;
	private int customerId;



	private String paymentId;
	private String paymentStatus;
	private String paymentDateTime;
    private long merchantProdId;
	private String trackId;
	private String receiptNumber;
	private String invoiceNumber;
	private String description;
	private String receiptFile;
	private Double cgstPercentage;
	private Double sgstPercentage;
	private Double igstPercentage;

	private String kycDocName;

	private String accountType;
	private String bankName;
	private String branch;
	private String accountNumber;
	private String confirmAccountNumber;
	private long aadharNo;

	private String ifscCode;
	private String beneficiaryName;

	private String approvalStatusL1;
	private String approvalStatusL2;
	private String approvedByL1;
	private String approvedByL2;

	private String approvalDateTime;
	private long id;
	private String userName;
	private String userPassword;
	private String password;
	private String emailAddress;
	private String mobileNumber;
	private String otpCode;
	private int accountStatus;
	private int roleId;
	private String address;
	private String pincode;
	private long pincodeId;
	private Timestamp createddateandtime;
	private int pincodestatus;
	private long cityid;
	private int userId;
	private String createdBy;
	private String createdDateTime;
	private String modifiedBy;
	private Timestamp modifiedDateTime;
	private int deletedFlag;
	private String categoryName;
	private String flag;
	private String cardNumber;
	private String adminApproval;
	private String superAdminApproval;
	private Long merchantId;
	private String approvalBy;
	private String newPassword;
	private String entityName;
	private String merchantLegalName;
	private String accountDisplayName;
	private String referenceNo;
	private String website;
	private String gstIn;
	private String contactName;
	private String contactMobile;
	private String contactEmail;
	private String billingAddress;
	private String area;
	private String zipcode;
	private String city;
	private String firebaseId;
	private String ipaddress;
	private String deviceType;
	private String deviceModel;
	private String deviceNumber;
	private String deviceName;
	private long stateId;
	private long categoryId;
	private MultipartFile docBackPath;
	private MultipartFile docFrontPath;
	private String cityName;
	private int cityStatus;
	private String productName;
	private double setupFee;
	private double settlementFee;
	private double mmcFee;
	private double txnFee;
	private double amcFee;
	private int productActiveStatus;
	private String countryName;
	private Timestamp createdAt;
	private String countryTimezone;
	private String roleName;
	private String stateName;
	private String productPaymentStatus;
	private int productQuantity;
	private String productCategoryName;

	private double productStandAmount;
	private double perTransactionFee;
	private double prepaidMDR;
	private double creditCard;
	private double netBanking;
	private double wallet;
	private double instantEmi;
	private double internationalCCAndDCTransactions;
	private double amex;

	private String effectiveDate;
	private long hsnCode;
	private String flatOrPercentage;

	private String docType;
	private String fileType;

	private double debitCardTransactionBelowPercent;
	private double debitCardTransactionAbovePercent;
	private double upiBelowPercentage;
	private double upiAbovePercentage;
	private double upiBelowAmount;
	private double upiAboveAmount;
	private double debitCardTransactionBelowAmount;
	private double debitCardTransactionAboveAmount;
	private String taxType;
    private long transactionId;
	private int mccId;
	private String merchantType;
	private long mccCode;
	private String modifiedDateAndTime;
	private String comment;
	
	private String paymentMethod;
	private String bankReference;
	private String merchantOrderNo;

	private String cardNoMasked;
	private String cardExpiry;
	private String customerName;
	private String customerEmail;
	private String customerMobile;
	private String adderess;
	private String postalCode;
	private String settlementStatus;
	private String settlementDateTime;
	private String settlementReason;
	private String settlementUTRNumber;
	private JsonNode etc;
	private String apikey;
	private String secretkey;
	
	//ESIGN
	private MultipartFile content;
	private String docket_title;
	private String reference_doc_id;
	private String content_type;
	private String signature_sequence;
	private String signer_ref_id;
	private String appearance;
	private String document_to_be_signed;
	private String signer_email;
	private String signer_name;
	private int sequence;
	private String page_number;
	private String esign_type;
	private String signer_mobile;
	private String signature_type;
	private String name_as_per_aadhaar;
	
	
	
	
	public MultipartFile getContent() {
		return content;
	}


	public void setContent(MultipartFile content) {
		this.content = content;
	}


	public String getDocket_title() {
		return docket_title;
	}


	public void setDocket_title(String docket_title) {
		this.docket_title = docket_title;
	}


	public String getReference_doc_id() {
		return reference_doc_id;
	}


	public void setReference_doc_id(String reference_doc_id) {
		this.reference_doc_id = reference_doc_id;
	}


	public String getContent_type() {
		return content_type;
	}


	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}


	public String getSignature_sequence() {
		return signature_sequence;
	}


	public void setSignature_sequence(String signature_sequence) {
		this.signature_sequence = signature_sequence;
	}


	public String getSigner_ref_id() {
		return signer_ref_id;
	}


	public void setSigner_ref_id(String signer_ref_id) {
		this.signer_ref_id = signer_ref_id;
	}


	public String getAppearance() {
		return appearance;
	}


	public void setAppearance(String appearance) {
		this.appearance = appearance;
	}


	public String getDocument_to_be_signed() {
		return document_to_be_signed;
	}


	public void setDocument_to_be_signed(String document_to_be_signed) {
		this.document_to_be_signed = document_to_be_signed;
	}


	public String getSigner_email() {
		return signer_email;
	}


	public void setSigner_email(String signer_email) {
		this.signer_email = signer_email;
	}


	public String getSigner_name() {
		return signer_name;
	}


	public void setSigner_name(String signer_name) {
		this.signer_name = signer_name;
	}


	public int getSequence() {
		return sequence;
	}


	public void setSequence(int sequence) {
		this.sequence = sequence;
	}


	public String getPage_number() {
		return page_number;
	}


	public void setPage_number(String page_number) {
		this.page_number = page_number;
	}


	public String getEsign_type() {
		return esign_type;
	}


	public void setEsign_type(String esign_type) {
		this.esign_type = esign_type;
	}


	public String getSigner_mobile() {
		return signer_mobile;
	}


	public void setSigner_mobile(String signer_mobile) {
		this.signer_mobile = signer_mobile;
	}


	public String getSignature_type() {
		return signature_type;
	}


	public void setSignature_type(String signature_type) {
		this.signature_type = signature_type;
	}


	public String getName_as_per_aadhaar() {
		return name_as_per_aadhaar;
	}


	public void setName_as_per_aadhaar(String name_as_per_aadhaar) {
		this.name_as_per_aadhaar = name_as_per_aadhaar;
	}


	public int getPgSwitch() {
		return pgSwitch;
	}

	public void setPgSwitch(int pgSwitch) {
		this.pgSwitch = pgSwitch;
	}
	public int getPgOnOffStatus() {
		return pgOnOffStatus;
	}

	public void setPgOnOffStatus(int pgOnOffStatus) {
		this.pgOnOffStatus = pgOnOffStatus;
	}

	
	public long getMerchantProdId() {
		return merchantProdId;
	}

	public void setMerchantProdId(long merchantProdId) {
		this.merchantProdId = merchantProdId;
	}

	public JsonNode getEtc() {
		return etc;
	}

	public void setEtc(JsonNode etc) {
		this.etc = etc;
	}

	public String getOrderReference() {
		return orderReference;
	}

	public void setOrderReference(String orderReference) {
		this.orderReference = orderReference;
	}

	private String orderReference;
	
	
	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getBankReference() {
		return bankReference;
	}

	public void setBankReference(String bankReference) {
		this.bankReference = bankReference;
	}

	public String getMerchantOrderNo() {
		return merchantOrderNo;
	}

	public void setMerchantOrderNo(String merchantOrderNo) {
		this.merchantOrderNo = merchantOrderNo;
	}

	public String getCardNoMasked() {
		return cardNoMasked;
	}

	public void setCardNoMasked(String cardNoMasked) {
		this.cardNoMasked = cardNoMasked;
	}

	public String getCardExpiry() {
		return cardExpiry;
	}

	public void setCardExpiry(String cardExpiry) {
		this.cardExpiry = cardExpiry;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public String getCustomerMobile() {
		return customerMobile;
	}

	public void setCustomerMobile(String customerMobile) {
		this.customerMobile = customerMobile;
	}

	public String getAdderess() {
		return adderess;
	}

	public void setAdderess(String adderess) {
		this.adderess = adderess;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getSettlementStatus() {
		return settlementStatus;
	}

	public void setSettlementStatus(String settlementStatus) {
		this.settlementStatus = settlementStatus;
	}

	public String getSettlementDateTime() {
		return settlementDateTime;
	}

	public void setSettlementDateTime(String settlementDateTime) {
		this.settlementDateTime = settlementDateTime;
	}

	public String getSettlementReason() {
		return settlementReason;
	}

	public void setSettlementReason(String settlementReason) {
		this.settlementReason = settlementReason;
	}

	public String getSettlementUTRNumber() {
		return settlementUTRNumber;
	}

	public void setSettlementUTRNumber(String settlementUTRNumber) {
		this.settlementUTRNumber = settlementUTRNumber;
	}

	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	

	public Double getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(Double paidAmount) {
		this.paidAmount = paidAmount;
	}

	private String approvedDateTimeL1;

	private String approvedDateTimeL2;

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getPaymentDateTime() {
		return paymentDateTime;
	}

	public void setPaymentDateTime(String paymentDateTime) {
		this.paymentDateTime = paymentDateTime;
	}

	public String getTrackId() {
		return trackId;
	}

	public void setTrackId(String trackId) {
		this.trackId = trackId;
	}

	public String getReceiptNumber() {
		return receiptNumber;
	}

	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReceiptFile() {
		return receiptFile;
	}

	public void setReceiptFile(String receiptFile) {
		this.receiptFile = receiptFile;
	}

	public Double getCgstPercentage() {
		return cgstPercentage;
	}

	public void setCgstPercentage(Double cgstPercentage) {
		this.cgstPercentage = cgstPercentage;
	}

	public Double getSgstPercentage() {
		return sgstPercentage;
	}

	public void setSgstPercentage(Double sgstPercentage) {
		this.sgstPercentage = sgstPercentage;
	}

	public Double getIgstPercentage() {
		return igstPercentage;
	}

	public void setIgstPercentage(Double igstPercentage) {
		this.igstPercentage = igstPercentage;
	}

	public String getApprovedDateTimeL1() {
		return approvedDateTimeL1;
	}

	public void setApprovedDateTimeL1(String approvedDateTimeL1) {
		this.approvedDateTimeL1 = approvedDateTimeL1;
	}

	public String getApprovedDateTimeL2() {
		return approvedDateTimeL2;
	}

	public void setApprovedDateTimeL2(String approvedDateTimeL2) {
		this.approvedDateTimeL2 = approvedDateTimeL2;
	}

	public String getApprovalStatusL1() {
		return approvalStatusL1;
	}

	public void setApprovalStatusL1(String approvalStatusL1) {
		this.approvalStatusL1 = approvalStatusL1;
	}

	public String getApprovalStatusL2() {
		return approvalStatusL2;
	}

	public void setApprovalStatusL2(String approvalStatusL2) {
		this.approvalStatusL2 = approvalStatusL2;
	}

	public String getApprovedByL1() {
		return approvedByL1;
	}

	public void setApprovedByL1(String approvedByL1) {
		this.approvedByL1 = approvedByL1;
	}

	public String getApprovedByL2() {
		return approvedByL2;
	}

	public void setApprovedByL2(String approvedByL2) {
		this.approvedByL2 = approvedByL2;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public long getAadharNo() {
		return aadharNo;
	}

	public void setAadharNo(long aadharNo) {
		this.aadharNo = aadharNo;
	}

	public String getConfirmAccountNumber() {
		return confirmAccountNumber;
	}

	public void setConfirmAccountNumber(String confirmAccountNumber) {
		this.confirmAccountNumber = confirmAccountNumber;
	}

	public String getBeneficiaryName() {
		return beneficiaryName;
	}

	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}

	public String getKycDocName() {
		return kycDocName;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public void setKycDocName(String kycDocName) {
		this.kycDocName = kycDocName;
	}

	public long getProdCategoryId() {
		return prodCategoryId;
	}

	public void setProdCategoryId(long prodCategoryId) {
		this.prodCategoryId = prodCategoryId;
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

	public String getModifiedDateAndTime() {
		return modifiedDateAndTime;
	}

	public void setModifiedDateAndTime(String modifiedDateAndTime) {
		this.modifiedDateAndTime = modifiedDateAndTime;
	}

	public int getMccId() {
		return mccId;
	}

	public void setMccId(int mccId) {
		this.mccId = mccId;
	}

	public String getMerchantType() {
		return merchantType;
	}

	public void setMerchantType(String merchantType) {
		this.merchantType = merchantType;
	}

	public long getMccCode() {
		return mccCode;
	}

	public void setMccCode(long mccCode) {
		this.mccCode = mccCode;
	}

	public String getFlatOrPercentage() {
		return flatOrPercentage;
	}

	public void setFlatOrPercentage(String flatOrPercentage) {
		this.flatOrPercentage = flatOrPercentage;
	}

	public String getTaxType() {
		return taxType;
	}

	public void setTaxType(String taxType) {
		this.taxType = taxType;
	}

	public double getUpiBelowAmount() {
		return upiBelowAmount;
	}

	public void setUpiBelowAmount(double upiBelowAmount) {
		this.upiBelowAmount = upiBelowAmount;
	}

	public double getUpiAboveAmount() {
		return upiAboveAmount;
	}

	public void setUpiAboveAmount(double upiAboveAmount) {
		this.upiAboveAmount = upiAboveAmount;
	}

	public double getDebitCardTransactionBelowAmount() {
		return debitCardTransactionBelowAmount;
	}

	public void setDebitCardTransactionBelowAmount(double debitCardTransactionBelowAmount) {
		this.debitCardTransactionBelowAmount = debitCardTransactionBelowAmount;
	}

	public double getDebitCardTransactionAboveAmount() {
		return debitCardTransactionAboveAmount;
	}

	public void setDebitCardTransactionAboveAmount(double debitCardTransactionAboveAmount) {
		this.debitCardTransactionAboveAmount = debitCardTransactionAboveAmount;
	}

	public double getDebitCardTransactionBelowPercent() {
		return debitCardTransactionBelowPercent;
	}

	public void setDebitCardTransactionBelowPercent(double debitCardTransactionBelowPercent) {
		this.debitCardTransactionBelowPercent = debitCardTransactionBelowPercent;
	}

	public double getDebitCardTransactionAbovePercent() {
		return debitCardTransactionAbovePercent;
	}

	public void setDebitCardTransactionAbovePercent(double debitCardTransactionAbovePercent) {
		this.debitCardTransactionAbovePercent = debitCardTransactionAbovePercent;
	}

	public double getUpiBelowPercentage() {
		return upiBelowPercentage;
	}

	public void setUpiBelowPercentage(double upiBelowPercentage) {
		this.upiBelowPercentage = upiBelowPercentage;
	}

	public double getUpiAbovePercentage() {
		return upiAbovePercentage;
	}

	public void setUpiAbovePercentage(double upiAbovePercentage) {
		this.upiAbovePercentage = upiAbovePercentage;
	}

	public String getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public long getHsnCode() {
		return hsnCode;
	}

	public void setHsnCode(long hsnCode) {
		this.hsnCode = hsnCode;
	}

	public double getProductStandAmount() {
		return productStandAmount;
	}

	public void setProductStandAmount(double productStandAmount) {
		this.productStandAmount = productStandAmount;
	}

	public double getPerTransactionFee() {
		return perTransactionFee;
	}

	public void setPerTransactionFee(double perTransactionFee) {
		this.perTransactionFee = perTransactionFee;
	}

	public double getPrepaidMDR() {
		return prepaidMDR;
	}

	public void setPrepaidMDR(double prepaidMDR) {
		this.prepaidMDR = prepaidMDR;
	}

	public double getCreditCard() {
		return creditCard;
	}

	public void setCreditCard(double creditCard) {
		this.creditCard = creditCard;
	}

	public double getNetBanking() {
		return netBanking;
	}

	public void setNetBanking(double netBanking) {
		this.netBanking = netBanking;
	}

	public double getWallet() {
		return wallet;
	}

	public void setWallet(double wallet) {
		this.wallet = wallet;
	}

	public double getInstantEmi() {
		return instantEmi;
	}

	public void setInstantEmi(double instantEmi) {
		this.instantEmi = instantEmi;
	}

	public double getInternationalCCAndDCTransactions() {
		return internationalCCAndDCTransactions;
	}

	public void setInternationalCCAndDCTransactions(double internationalCCAndDCTransactions) {
		this.internationalCCAndDCTransactions = internationalCCAndDCTransactions;
	}

	public double getAmex() {
		return amex;
	}

	public void setAmex(double amex) {
		this.amex = amex;
	}

	public String getProductCategoryName() {
		return productCategoryName;
	}

	public void setProductCategoryName(String productCategoryName) {
		this.productCategoryName = productCategoryName;
	}

	public int getProductQuantity() {
		return productQuantity;
	}

	public void setProductQuantity(int productQuantity) {
		this.productQuantity = productQuantity;
	}

	public String getProductPaymentStatus() {
		return productPaymentStatus;
	}

	public void setProductPaymentStatus(String productPaymentStatus) {
		this.productPaymentStatus = productPaymentStatus;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleCode() {
		return roleCode;
	}

	public void setRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}

	private String roleCode;

	public String getCountryTimezone() {
		return countryTimezone;
	}

	public void setCountryTimezone(String countryTimezone) {
		this.countryTimezone = countryTimezone;
	}

	
	public Timestamp getCreatedAt() {
		return createdAt;
	}


	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
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

	private String countryCode;
	private String countryCurrency;

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public int getCityStatus() {
		return cityStatus;
	}

	public void setCityStatus(int cityStatus) {
		this.cityStatus = cityStatus;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public double getSetupFee() {
		return setupFee;
	}

	public void setSetupFee(double setupFee) {
		this.setupFee = setupFee;
	}

	public double getSettlementFee() {
		return settlementFee;
	}

	public void setSettlementFee(double settlementFee) {
		this.settlementFee = settlementFee;
	}

	public double getMmcFee() {
		return mmcFee;
	}

	public void setMmcFee(double mmcFee) {
		this.mmcFee = mmcFee;
	}

	public double getTxnFee() {
		return txnFee;
	}

	public void setTxnFee(double txnFee) {
		this.txnFee = txnFee;
	}

	public double getAmcFee() {
		return amcFee;
	}

	public void setAmcFee(double amcFee) {
		this.amcFee = amcFee;
	}

	public int getProductActiveStatus() {
		return productActiveStatus;
	}

	public void setProductActiveStatus(int productActiveStatus) {
		this.productActiveStatus = productActiveStatus;
	}

	public void setModifiedDateTime(Timestamp modifiedDateTime) {
		this.modifiedDateTime = modifiedDateTime;
	}

	public Timestamp getModifiedDateTime() {
		return modifiedDateTime;
	}

	public MultipartFile getDocBackPath() {
		return docBackPath;
	}

	public void setDocBackPath(MultipartFile docBackPath) {
		this.docBackPath = docBackPath;
	}

	public MultipartFile getDocFrontPath() {
		return docFrontPath;
	}

	public void setDocFrontPath(MultipartFile docFrontPath) {
		this.docFrontPath = docFrontPath;
	}

	public Long getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(Long merchantId) {
		this.merchantId = merchantId;
	}

	public String getAdminApproval() {
		return adminApproval;
	}

	public void setAdminApproval(String adminApproval) {
		this.adminApproval = adminApproval;
	}

	public String getSuperAdminApproval() {
		return superAdminApproval;
	}

	public void setSuperAdminApproval(String superAdminApproval) {
		this.superAdminApproval = superAdminApproval;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getApprovalBy() {
		return approvalBy;
	}

	public void setApprovalBy(String approvalBy) {
		this.approvalBy = approvalBy;
	}

	public String getApprovalDateTime() {
		return approvalDateTime;
	}

	public void setApprovalDateTime(String approvalDateTime) {
		this.approvalDateTime = approvalDateTime;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(String createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public int getDeletedFlag() {
		return deletedFlag;
	}

	public void setDeletedFlag(int deletedFlag) {
		this.deletedFlag = deletedFlag;
	}

	

	public long getPincodeId() {
		return pincodeId;
	}

	public void setPincodeId(long pincodeId) {
		this.pincodeId = pincodeId;
	}

	public Timestamp getCreateddateandtime() {
		return createddateandtime;
	}

	public void setCreateddateandtime(Timestamp createddateandtime) {
		this.createddateandtime = createddateandtime;
	}

	public int getPincodestatus() {
		return pincodestatus;
	}

	public void setPincodestatus(int pincodestatus) {
		this.pincodestatus = pincodestatus;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getOtpCode() {
		return otpCode;
	}

	public void setOtpCode(String otpCode) {
		this.otpCode = otpCode;
	}

	public int getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(int accountStatus) {
		this.accountStatus = accountStatus;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getMerchantLegalName() {
		return merchantLegalName;
	}

	public void setMerchantLegalName(String merchantLegalName) {
		this.merchantLegalName = merchantLegalName;
	}

	public String getAccountDisplayName() {
		return accountDisplayName;
	}

	public void setAccountDisplayName(String accountDisplayName) {
		this.accountDisplayName = accountDisplayName;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public String getGstIn() {
		return gstIn;
	}

	public void setGstIn(String gstIn) {
		this.gstIn = gstIn;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactMobile() {
		return contactMobile;
	}

	public void setContactMobile(String contactMobile) {
		this.contactMobile = contactMobile;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(String billingAddress) {
		this.billingAddress = billingAddress;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getFirebaseId() {
		return firebaseId;
	}

	public void setFirebaseId(String firebaseId) {
		this.firebaseId = firebaseId;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	public String getDeviceNumber() {
		return deviceNumber;
	}

	public void setDeviceNumber(String deviceNumber) {
		this.deviceNumber = deviceNumber;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public long getStateId() {
		return stateId;
	}

	public void setStateId(long stateId) {
		this.stateId = stateId;
	}

	public long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(long categoryId) {
		this.categoryId = categoryId;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public long getCityid() {
		return cityid;
	}

	
	public void setCityid(long cityid) {
		this.cityid = cityid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
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
	
	public String getPgMode() {
		return pgMode;
	}

	public void setPgMode(String pgMode) {
		this.pgMode = pgMode;
	}

	public String getApikey() {
		return apikey;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public String getSecretkey() {
		return secretkey;
	}

	public void setSecretkey(String secretkey) {
		this.secretkey = secretkey;
	}

	public long getMandateDocId() {
		return mandateDocId;
	}

	public void setMandateDocId(long mandateDocId) {
		this.mandateDocId = mandateDocId;
	}

	public int getBusinessCategoryId() {
		return businessCategoryId;
	}

	public void setBusinessCategoryId(int businessCategoryId) {
		this.businessCategoryId = businessCategoryId;
	}

	public int getBusinessTypeId() {
		return businessTypeId;
	}

	public void setBusinessTypeId(int businessTypeId) {
		this.businessTypeId = businessTypeId;
	}

	public int getCorporateId() {
		return corporateId;
	}

	public void setCorporateId(int corporateId) {
		this.corporateId = corporateId;
	}

	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}
	
	

}
