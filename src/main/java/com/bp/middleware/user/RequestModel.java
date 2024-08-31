package com.bp.middleware.user;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.mandatedocument.MandateDocumentModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.modeofpgpayment.ModeOfPaymentPg;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.role.RoleDto;
import com.bp.middleware.signers.SignerModel;
import com.bp.middleware.signmerchant.MerchantModel;
import com.bp.middleware.util.MerchantDto;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class RequestModel {
	
	private int manualPaymentId;
	private int convenienceId;
	private String referenceName;
	private String convenienceType;
	private double convenienceAmount;
	private double conveniencePercentage;
	private double startAmount;
	private double endAmount;
	private ModeOfPaymentPg modeOfPaymentPg;
	private String linkExpiry;
	private String returnUrl;
	private String referenceNo;
	private double fixedAmount;
	private double thresholdAmount;
	private double exclusiveAmount;
	private boolean nativeLocal;
	private boolean showLiveKeys;
	private String invoice;
	private boolean aadhaarBasedSigning;
	private String aadhaarRequestId;
	private LocalDate aadhaarRequestTime;
	private String usedServices;
	private int recentIdentifier;
	private boolean accepted;
	private boolean consent;
	private String modeOfPay;
	private String complaintResponse;
	private boolean getLocation;
	private int positionId;
	private int adminMailId;
	private boolean currentlyActive;
	private String ticketCurrentStatus;
	private String postpaidPaymentCycle;
	private String service;
	
	private int prepaidStatementId;
	private double debit;
	private double credit;
	private double closingBalance;
	private double consumedBalance;
	private Date entryDate;
	private double debitGst;
	private double creditGst;
	
	private int bankAccId;
	private boolean generalSigning;
	private double aadhaarXmlPrice;
	private double aadhaarOtpPrice;
	
	private PrepaidPayment prepaid;
	private Set<VendorVerificationModel> verificationModelSet;
	
	private Set<Integer> multipleUserId;
	
	private boolean deleteInvoiceTwo;
	private boolean deleteInvoiceThree;
	
	private MerchantPriceModel merchantPriceModel;
	
	private int refundId;
	private String refundStatus;
	private double refundAmount;
	private LocalDate refundInitiatedAt;
	private String activityId;
	
	private boolean convenienceFetch;
	private boolean expired;
	private int hsnId;
	private String hsnNumber;
	
	LocalDate generatedDate;
	int totalHits;
	double walletBalance ;
	double usedAmount;
	double grandTotal;
	String entityName;
	String country;
	String gstNo;
	
	
	private double sgst;
	private double cgst;
	private double igst;
	
	private int noRestriction;
	private boolean freeHit;
	private JSONObject companyInfo;
	private String cin;
	private String rocCode;
	private String companyCategory;
	private String classOfCompany;
	private String companySubCategory;
	private String authorizedCapital;
	private String paidUpCapital;
	private String numberOfMembers;
	private String dateOfIncorporation;
	private String registeredAddress;
	private String addressOtherThanRo;
	private String listedStatus;
	private String activeCompilance;
	private String suspendedAtStockExchange;
	private String lastAgmDate;
	private String lastBsDate;
	private String companyStatus;
	private String statusUnderCirp;
	private JSONArray directors;
	private JSONArray charges;
	private List<String> datePeriod; 
	private boolean mailPresent;
	private boolean smsPresent;
	private boolean postpaidDueAllow;
	
	private int modeId;
	private String modeOfPayment;
	private String paymentCode;
	
	private boolean bulkUploadAccess;
	private boolean createPdfAccess;
	private double documentPrice;
	
	private String statePremesis;
	private int districtPremesis;
	private int talikPremesis;
	private String appType;
	
	private String dinNumber;
	private String nationality;
	private String dinStatus;
	private JSONArray companiesAssociated;
	private JSONArray enterprizeTypeList;
	
	private String registeredDate;
	private String ownerName;
	private String presentAddress;
	private String permanentAddress;
	private String vehicleCategory;
	private String vehicleChasisNumber;
	private String vehicleEngineNumber;
	private String makerDescription;
	private String makerModel;
	private String bodyType;
	private String fuelType;
	private String color;
	private String normsType;
	private String fitUpTo;
	private String financer;
	private String insuranceCompany;
	private String insurancePolicyNumber;
	private String insuranceUpto;
	private String manufacturingDate;
	private String manufacturingDateForma;
	private String registeredAt;
	private String latestBy;
	private String taxUpto;
	private String taxPaidUpto;
	private String cubicCapacity;
	private String vehicleGrossWeight;
	private String noOfCylinders;
	private String seatCapacity;
	private String sleeperCapacity;
	private String standingCapacity;
	private String wheelBase;
	private String unladenWeight;
	private String vehicleCategoryDescription;
	private String puccNumber;
	private String puccUpto;
	private String permitNumber;
	private String permitIssueDate;
	private String permitValidFrom;
	private String permitValidUpto;
	private String permitType;
	private String nationalPermitNumber;
	private String nationalPermitUpto;
	private String nationalPermitIssuedBy;
	private String nonUseStatus;
	private String nonUseFrom;
	private String nonUseTo;
	private String blackListStatus;
	private String nocDetails;
	private String ownerNumber;
	private String rcStatus;
	private String variant;
	private String challanDetails;
	private boolean financed;
	private boolean maskedNamePresent;
	private MerchantModel merchantDoc;
	
	private String firmName;
	private String iecIssuanceDate;
	private String iecStatus;
	private String delStatus;
	private String iecCancelledDate;
	private String iecSuspendedDate;
	private String fileDate;
	private String dgftraOffice;
	private String natureOfConcern;
	private String categoryOfExporters;
	private String firmMobileNo;
	private String firmEmailId;
	private String payer;
	private int statusCodeNumber;
	private String accessToken;
	
	private int errorIdentifierId;
	private String errorReferenceNumber;
	private Date occuredDate;
	private String apiName;
	
	private String ageRange;
	private String lastDigits;
	private boolean mobilePresent;
	private String inputDob;
	private boolean dobVerified;
	private boolean dobCheck;
	private boolean aadhaarLinked;
	private JSONArray splitName;
	private String maskedAadhaar;
	private JSONObject addressInJson;
	
	private JSONArray branchDetails;
	private JSONArray remcDetails;
	private JSONArray directorDetails;
	private JSONArray filingStatusJsonList;

	private String applicationNumber;
	private String addressPresis;
	private String statusDesc;
	private String distrctName;
	private String displayRefId;
	private String talukName;
	private String appTypeDesc;
	private String licenseCategoryName;
	private String appSubmitionDate;
	private String lastUpdatedOn;
	private int fboId;
	private int referId;
	private boolean accountExists;
	private String pfUan;
	private JSONArray employmentHistory;
	
	private String membershipNumber;
	private JSONObject details;
	
	private String ifsc;
	private String micr;
	private String iso3166;
	private String swift;
	private String bank;
	private String bankCode;
	private String branch;
	private String centre;
	private String district;
	private String city;
	private String contact;
	
	private boolean imps ;
	private boolean rtgs;
	private boolean neft;
	private boolean micrCheck;
	private boolean upiPresent;
	
	private int alreadyExistingUserId;
	private int newComerUserId;
	private String domain;
	private String emailStatus;
	private boolean acceptsMail;
	private boolean isCatchAll;
	private boolean valid;
	private boolean validSyntax;
	private boolean smtpConnected;
	private boolean isTemporary;
	private boolean disabled;
	private boolean results;
	private JSONArray mxRecords;
	private String panAllotmentDate;
	private String maskedName;
	private String panAadhaarLinked;
	private String specifiedPersonUnder206;
	private String panStatus;
	private boolean validPan;
	private boolean complaint;
	
	//VOTER ID
	private String inputVoterId ;
	private String stCode ;
	private String assemblyContituencyNumber ;
	private String relationNameV2 ;
	private String relationNameV1 ;
	private String relationNameV3 ;
	private String relationType ;
	private String namev1;
	private String namev2;
	private String namev3;
	private String epicNo;
	private String psLatLong;
	private String assemblyConstituency;
	private String area;
	private String parliamentaryName;
	private boolean multiple;
	private String parliamentaryConstituency;
	private String parliamentaryNumber;
	private String houseNo;
	private String partNumber;
	private String pollingStation;
	private String sectionNo;
	private String slnoInpart;
	private String relationName;
	private String partName;
//	private String idNumber;

	//password History
	private String ipaddress;
	private List<MerchantDto> merchant;
    //private boolean status;
	private String status;
	//Member Model
	private int memberId;
	private String memberShipNumber;
	private String fatherName;
	private String motherName;
	private String spouseName;
	private LocalDate dateOfBirth;
	private int age;
	private String addressLine1;
	private String addressLine2;
	private LocalDate modifieddAt;
	private boolean firstPayment;
	private String documentTitle;
	private double signaturePrice;
	private int userManagementId;
	private String reference;
	private boolean success;
	private String buildingNumber;
	private String streetAddress;
	private String cityPincode;
	private boolean verificationRequired;
	private boolean signingRequired;
	private List<RequestModel> merchantPriceList; 
	
	private JSONObject ocrData;
	
	//Admin Model
	private int loginFailedCount;


	//City Model
	private String cityName;
	private Date createdDatetime;

	//Pincode Model
	private String createdby;
	private Date createddateandtime;
	private boolean pincodestatus;

	// COMMON
	private boolean statusFlag;

	// CATEGORY OF MEMBERSHIP
	private int categoryMembershipId;
	private String categoryMembership;
	private double membershipFee;
	private double annualFee;

	// FAMILY
	private int familyId;
	private String relation;
	private String martialStatus;

	// EDUCATIONAL QUALIFICATION
	private int educationQualificationId;
	private String education;
	private String institute;
	private String yearOfPassout;
	private String marksInPercentage;
	private String board;
	private String medium;

	// PREFFERED SECTION
	private int preferredSectionId;
	private String preferredSection;

	//PROOF ID
	private int proofId;
	private String proofDocName;
	private MultipartFile thumbnailImage;
	private MultipartFile[] sctionImages;

	//Account Type
	private String ifscNumber;

	//sms
	private String smsEnable;

	//Library Model
	private int libraryId;
	private String libraryName;
	private String libraryCode;
	private String landLineNumber;

	//payment Model
	private int payid;
	private String paymentDateTime;
	private String trackId;
	private String receiptNumber;
	private String invoiceNumber;
	private LocalDate dueDate;

	//Transaction Model
	private Date paymentDatetime;
	private String orderReference;

	//Renewable Model
	private int renewableId;
	private String rewableDate;
	private double renewableAmount;
	private String nextRenewableDate;
	private double nextRenewableAmount;

	//Plan Model
	private int planTypeId;
	private String planTypeName;
	private int activeStatus;

	//Price Model
	private int planId;
	private String planName;
	private String planDescription;

	//slot Model
	private int userTimeSlotId;
	private String startTime;
	private String endTime;
	private String slotCreatedAt;

	//Request Auditorium Model
	private String requesterName;
	private String alternativeMobileNumber;
	private String fromDate;
	private String toDate;
	private long audianceCount;
	private boolean approveStatus;
	private String requestAt;
	private String modifiedAt;
	private int days;

	//Time Slot
	private int timeRequestId;
	private String fromTime;
	private String toTime;
	private double hrs;
	private int day;

	//Request Book
	private int requestBookId;
	private String bookName;
	private String journalType;
	private String author;
	private String edition;
	private String publisher;

	//merchant Model
	private String companyAddress;
	private String companyWebsite;
	private String agreementDate;
	private String rateAgreed;
	private String businessCategory;
	private String businessSubCategory;
	private String monthName;
	private List<MerchantModel> model;

	//Bond Model
	private int bondId;
	private double bondAmount;
	private int uploadCount;
	private int usedCount;
	private int remainingCount;
	private int bondDetailId;
	private String sealedDate;
	private MultipartFile document;
	private int bondStatus;
	private String bondNumber;
	
	private String docExpiryAt;
	private int signerCount;

	//Department  Model
	private int departmentId;
	private String departmentName;
	private String departmentHeadName;
	private String branchAddress;
	private String signatoryName;
	private String signatoryDesignation;
	private String signatoryDate;
	private String merchantCompanyName;
	private int count;

	//Signer Model
	private int signerId;
	private String signerName;
	private String SignerMobile;
	private String signerEmail;
	private LocalDate signedAt;
	private MultipartFile pdfDocument;
	private List<SignerModel> signer;
	private boolean bondPayer;

	//Merchant Model
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
	private boolean bond;


	//....../////

	//SprintV
	private String aadharNumber;
	private String udyamAadharNumber;
	private String clientId;	
	private String voterId;
	private String drivingLicenceId;
	private String passportId;
	private String acknowledgementNumber;
	private String mcaId;
	private String taxDeductionIdNumber;
	private String stateCode;
	private String shopNumber;
	private String rcNumber;
	private String iecNumber;
	private String fssaiNumber;
	private String leiNumber;
	private String qrText;
	private String epfoNumber;
	private String uanNumber;
	private String documentType;
	private String idNumber;
	private String authFactorType ;
	private String authFactor ;
	private String threshold ;
	private String image1 ;
	private String image2Url;
	private String videoFile;
	private String videoUrl;
	private String upiId; 
	private String vehicleId;
	private String chassis;
	private String file;
	private String link;
	private String backImage;
	private int extendedData;
	private String latitude;
	private String longitude;
	private long mobile;
	private String itrId;
	private String tdsId;
	private String companyName;
	private String cityState;
	private String rtoCode;
	private String refId ;
	private String redirectUrl;
	private String uri;
	private String errorCode;
	private String Gender;
	private boolean mobileVerified;
	private String requestTag;
	private String ticketSize;
	private boolean crimeWatch;
	private boolean reportMode;

	// ADMIN MODEL
	private int adminId;
	private int superAdminId;

	// User Model
	private int userId;
	private int responseId;
	private String name;
	private String email;
	private String address;
	private String password;
	private String newPassword;
	private String mobileNumber;
	private Date lastLogin;
	private String ipAddress;
	private String createdBy;
	private Date createdDate;
	private boolean accountStatus;
	private String modifyBy;
	private Date modifyDate;
	private int loginFailCount;
	private String profilePhoto;
	private boolean otpVerificationStatus;
	private String otpCode;
	private Date otpExpiryOn;
	private List<PasswordModelHistory> passwordHistory;
	private double amount;
	private int approvalRejectionId;
	private String approveReject;
	private long rejectionCount;
	private boolean approvedStatus;
	private String comments;
	private Long rejectedCount;
	private String approvalStatus;
	private LocalDate date;


	// LOCATIONS
	// Country
	private int countryId;
	private String countryName;
	private String countryCode;
	private String countryCurrency;
	private String countryTimezone;
	private boolean countryStatus;

	// State
	private int stateId;
	private String stateName;
	private boolean stateStatus;

	// City
	private int cityId;
	private boolean cityStatus;

	// Pincode
	private int pincodeId;
	private String pincode;

	// Password History
	private int passwordId;
	private String userPassword;
	private String userSaltKey;
	private int currentPasswordStatus;
	private Date modifiedDate;
	private String reasonForChange;
	private String modifiedBy;

	private String reqDeviceType;
	private EntityModel user;

	// LOG
	private int logPeriod;
	private LocalDate logUpdatedAt;
	private String logUpdatedBy;

	// Other All
	private String accountNumber;
	private String ifscCode;
	private String accountHolderName;
	private String branchName;
	private String micrCode;
	private String bankName;
	private boolean bankAccountStatus;
	private String accountType;
	private String websiteIntegration;
	private String apiKey;
	private String plugin;
	private String secretKey;
	private String publicUrl;
	private String licenceNumber;
	private String licenceType;
	private String licenceIssueDate;
	private String licenceExpirationDate;
	private int attempt;
	private String category;
	private String error;
	private boolean sourcePresent;
	private String legalName;
	private String centerJurisdiction; 
	private String stateJurisdiction ;
	private String constitutionOfBusiness ;
	private String taxpayerType ;
	private String gstInStatus ;
	private String fieldVisitConducted ;
	private String coreBusinessActivityCode ;
	private String coreBusinessActivityDescription ;
	private String aadharValidation;
	private String aadharValidatedDate ;
	private String dateOfCancellation;
	private String dateOfApplication;
	private String fileNumber;
	private String verificationMessage;
	private boolean noSourceCheck;
	private boolean complaintActive;
	
	private String zip;
	private String mobileHash;
	private String emailHash;
	private String rawXml;
	private String zipData;
	private String careOf;
	private String shareCode;
	private String aadharReferenceId;
	private String aadharStatus;
	private String uniquenessId;
	private boolean faceStatus; 
	private boolean hasImage;
	private int faceScore;
	
	//DC
    private int dataComplaintId;
	private String merchantEmail;
	private String verificationType;
	private LocalDate fetchedDate;
	private LocalDate lastFetchedDate;
	private String comment;
	private LocalDate submittedDate;
	
	private String companyInfoJsonObject;
	private String directorsJsonArray;
	private String companyId;
	private String companyType;
	
	private String permanentZip;
	private String temporaryAddress;
	private String temporaryZip;
	private String citizenShip;
	private String olaName;
	private String olaCode;
	private String gender;
	private String fatherOrHusbandName;
	private String doe;
	private String transportDoe;
	private String doi;
	private String transportDoi;
	private String profileImage;
	private String initialDoi;
	private String currentStatus;
	private boolean lessInfo;
	private String vehicleClasses;
	private String additionalCheck;

	private String nameOfEnterprise;
	private String majorActivity;
	private String socialCategory;
	private String dateOfCommencement;
	private String dicName;
	private String state;
	private String appliedDate;
	private String enterpriseType;
	private String uan;
	private String locationOfPlantDetails;
	private String nicCode;
	private int dataMatchFullName;
	private int dataMatchPanNumber;
	private int dataMatchAggregate;
	private String fathersName;
	
	private String tradeName;
	private String hsnInfo;
	private String filingStatusList;
	private String natureBusActivity;
	private int dataMatchTaxpayerType;
	private int dataMatchBusinessNamer;
	private int dataMatchAddress;
	private int dataMatchCostitutionOfBusiness;
	private int dataMatchDateOfRegistration;
	private int dataMatchGstIn;
	private int dataMatchTradeName;
	private boolean validGst; 
	List bulkVerify = new ArrayList<>();
	private String refid;

	// Role Model
	private int roleId;
	private int roleCode;
	private String roleName;
	private boolean roleStatus;
	private String profilePicture;
	private int subPermissionId;
	private String subPermissionName;
	List<Integer> subPermissions = new ArrayList<>();

	// Ticket Category Type
	private int ticketCategoryId;
	private String ticketType;

	// Ticket Raising
	private int ticketId;
	private String customerName;
	private String priorityType;
	private String userType;
	private String description;
	private String attachment;
	private Date createdAt;
	private String reason;
	private boolean ticketStatus;
	private MultipartFile fileAttachment;

	// MAIL
	private String host;
	private String port;
	private String smtpAuth;
	private String smtpPort;
	private String smtpConnectionTimeOut;
	private String starttlsEnable;
	private String socketFactoryPort;
	private String socketFactoryClass;
	private String protocol;
	private String mailUserName;
	private String mailPassword;
	private String smtpTimeOut;
	private String smtpWriteTimeOut;

	// SMS
	private int smsTempId;
	private String smsTempCode;
	private String smsTempMessage;
	private String smsTempDescription;
	private Date smsModifiedDate;
	private String smsModifiedBy;
	private boolean smsTempStatus;
	private String smsEntityId;
	private String smsTemplateId;
	private String smsServiceUrl;
	private String smsUserName;
	private String smsPassword;
	private String smsEnabled;

	// Verifications
	private int requestId;
	private String referenceId;
	private String sourceType;
	private String source;
	private boolean filingStatus;
	private String encryptedJson;
	private Date requestDateAndTime;
	private String requestBy;
	private String transactionId;
	private int transactId;
	private String otp;
	private JsonNode response;
	private String responseDateAndTime;
	private String panNumber;
	private String dob;
	private String gstIn;
	private String businessName;
	private String dateOfRegistration;
	private String fullName;
	private String aadhaarNumber;
	private String message;

	// Services
	private int serviceId;
	private String services;

	private String encrypted_data;
	private String reference_id;
	private String source_type;

	private String pan;
	private String gst;
	private String contactPersonName;
	private String contactPersonMobile;
	private String contactPersonEmail;

	// MANDATE DOCUMENTS
	private String kycDocName;

	// MERCHANT KYC
	private int merchantId;
	private String cardNumber;
	private long id;
	private int priority;

	private String workerName;
	// private String mobileNumber;
	private Double ratings;
	private String totalWorkingHouse;
//	private String status;
	// private String fileType;
	private String availableTime;
	private int societyId;
	private String workingTime;
	private String panCardNumber;
	// private String bankName;
	private String bankAccountName;
	private String bankAccountNumber;

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
	// private String password;
	// private String address;
	// private String email;
	private int raiseCategoryId;
	private String registrationNumber;
	private int dailHelpKycDocumentId;
	private int dailyHelpWorkerId;
	// private String cardNumber;
	private int ticketWorkerDocumentId;
	private int ticketWorkerId;
	private int documentId;
	private int homeId;
	// private int userId;
	private String recieverMessage;
	private String receiverMobileNumber;
	private String entityId;
	private String templateId;
	private String smsUrl;
	private String smsUser;
	private String smsPwd;

	// ROLES & PERMISSIONS
	private String permission;
	// private List<Action> actionList;
	private RoleDto roleModel;
	//	private List<Permission> permissionList;
	private String action;
	// private Permission permissionObject;
	private int permissionId;
	private int actionId;
	// private Action actionModel;
	private int rolesAndPermissionId;

	// VENDOR & VERIFICATION
	private int vendorId;
	private String vendorName;
	private int vendorVerificationId;
	private String verificationDocument;
	private String type;
	private VendorModel vendorModel;
	private boolean vendorStatus;
	private boolean vendorVerifyStatus;
	private double vendorSuccessRate;

	private boolean merchantStatus;
	private int merchantPriceId;

	// VENDOR PRICE & URL
	private int vendorPriceId;
	private String apiLink;
	private double idPrice;
	private double imagePrice;
	private boolean vendorPriceStatus;
	private String applicationId;
	private VendorVerificationModel vendorVerificationModel;

	// PAYMENT
	private int paymentId;
	private String paymentType;
	private String paymentMode;
	private String payingId;
	private JsonNode etc;
	// PREPAID
	private int prepaidId;
	private String merchantName;
	private double rechargedAmount;
	private double consumedAmount;
	private double remainingAmount;
	private int totalHit;
	private String remark;
	private LocalDate paidDate;
	private LocalDate updateDate;
	


	private String updatedDate;
	private double paidAmount;

	private String commonResponse;

	public LocalDate getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(LocalDate updateDate) {
		this.updateDate = updateDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	// POSTPAID
	private int postpaidId;
	private double totalAmount;
	private LocalDate period;
	private int duration;
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDate graceDate;
	private int gracePeriod;

	// BUSINESS TYPE & CATEGORY & MCC CODE
	private int businessTypeId;
	private String businessType;
	private int businessCategoryId;
	private String businessCategoryName;
	private int mccId;
	private String corporateType;
	private String mccCode;

	// DASHBOARD
	private int month;
	private String paymentStatus;

	// TECHNICAL
	private int technicalId;

	// MAKER AND CHECKER
	private int makerCheckerId;
	private String makerCheckerRoleName;
	private String makerCheckeRoleCode;
	private String remarks;
	private String makerCheckerRoleType;

	private String paymentDate;





	// ----------///






	public String getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate;
	}

	public int getMonth() {
		return month;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
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

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public LocalDate getPaidDate() {
		return paidDate;
	}

	public void setPaidDate(LocalDate paidDate) {
		this.paidDate = paidDate;
	}

	public boolean isMerchantStatus() {
		return merchantStatus;
	}

	public void setMerchantStatus(boolean merchantStatus) {
		this.merchantStatus = merchantStatus;
	}





	public List<String> getDatePeriod() {
		return datePeriod;
	}

	public void setDatePeriod(List<String> datePeriod) {
		this.datePeriod = datePeriod;
	}


	public void setMonth(int month) {
		this.month = month;
	}

	public double getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(double paidAmount) {
		this.paidAmount = paidAmount;
	}

	public int getRolesAndPermissionId() {
		return rolesAndPermissionId;
	}

	public void setRolesAndPermissionId(int rolesAndPermissionId) {
		this.rolesAndPermissionId = rolesAndPermissionId;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}

	public int getPermissionId() {
		return permissionId;
	}

	public int getVendorPriceId() {
		return vendorPriceId;
	}

	public int getMerchantPriceId() {
		return merchantPriceId;
	}

	public void setMerchantPriceId(int merchantPriceId) {
		this.merchantPriceId = merchantPriceId;
	}

	public void setVendorPriceId(int vendorPriceId) {
		this.vendorPriceId = vendorPriceId;
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

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public String getGst() {
		return gst;
	}

	public void setGst(String gst) {
		this.gst = gst;
	}

	public String getContactPersonName() {
		return contactPersonName;
	}

	public void setContactPersonName(String contactPersonName) {
		this.contactPersonName = contactPersonName;
	}

	public String getContactPersonMobile() {
		return contactPersonMobile;
	}

	public void setContactPersonMobile(String contactPersonMobile) {
		this.contactPersonMobile = contactPersonMobile;
	}

	public String getContactPersonEmail() {
		return contactPersonEmail;
	}

	public void setContactPersonEmail(String contactPersonEmail) {
		this.contactPersonEmail = contactPersonEmail;
	}

	public void setBusinessCategoryName(String businessCategoryName) {
		this.businessCategoryName = businessCategoryName;
	}

	public String getApiLink() {
		return apiLink;
	}

	public void setApiLink(String apiLink) {
		this.apiLink = apiLink;
	}

	public double getIdPrice() {
		return idPrice;
	}

	public void setIdPrice(double idPrice) {
		this.idPrice = idPrice;
	}

	public double getImagePrice() {
		return imagePrice;
	}

	public void setImagePrice(double imagePrice) {
		this.imagePrice = imagePrice;
	}

	public boolean isVendorPriceStatus() {
		return vendorPriceStatus;
	}

	public void setVendorPriceStatus(boolean vendorPriceStatus) {
		this.vendorPriceStatus = vendorPriceStatus;
	}

	public String getApplicationId() {
		return applicationId;
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

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public VendorVerificationModel getVendorVerificationModel() {
		return vendorVerificationModel;
	}

	public void setVendorVerificationModel(VendorVerificationModel vendorVerificationModel) {
		this.vendorVerificationModel = vendorVerificationModel;
	}

	public void setPermissionId(int permissionId) {
		this.permissionId = permissionId;
	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public void setRoleModel(RoleDto roleModel) {
		this.roleModel = roleModel;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public MultipartFile getDocBackPath() {
		return docBackPath;
	}

	public MultipartFile getDocFrontPath() {
		return docFrontPath;
	}


	public int getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
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

	public String getApprovalDateTime() {
		return approvalDateTime;
	}

	public void setApprovalDateTime(String approvalDateTime) {
		this.approvalDateTime = approvalDateTime;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	// public String getDocFrontPath() {
	// return docFrontPath;
	// }
	//
	// public void setDocFrontPath(String docFrontPath) {
	// this.docFrontPath = docFrontPath;
	// }

	// public String getDocBackPath() {
	// return docBackPath;
	// }
	//
	// public void setDocBackPath(String docBackPath) {
	// this.docBackPath = docBackPath;
	// }

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getModifiedDateTime() {
		return modifiedDateTime;
	}

	public void setModifiedDateTime(String modifiedDateTime) {
		this.modifiedDateTime = modifiedDateTime;
	}

	public int getDeletedFlag() {
		return deletedFlag;
	}

	public void setDeletedFlag(int deletedFlag) {
		this.deletedFlag = deletedFlag;
	}

	public String getApprovalByL1() {
		return approvalByL1;
	}

	public String getSmsEnabled() {
		return smsEnabled;
	}

	public void setSmsEnabled(String smsEnabled) {
		this.smsEnabled = smsEnabled;
	}

	public void setApprovalByL1(String approvalByL1) {
		this.approvalByL1 = approvalByL1;
	}

	public String getApprovalByL2() {
		return approvalByL2;
	}

	public void setApprovalByL2(String approvalByL2) {
		this.approvalByL2 = approvalByL2;
	}

	public EntityModel getEntityModel() {
		return entityModel;
	}

	public void setEntityModel(EntityModel entityModel) {
		this.entityModel = entityModel;
	}

	public MandateDocumentModel getMandateDocumentModel() {
		return mandateDocumentModel;
	}

	public void setMandateDocumentModel(MandateDocumentModel mandateDocumentModel) {
		this.mandateDocumentModel = mandateDocumentModel;
	}

	private String adminApproval;
	private String superAdminApproval;
	private String approvalDateTime;
	private String fileType;
	// private String docFrontPath;
	// private String docBackPath;
	private String flag;
	private String modifiedDateTime;
	private int deletedFlag;
	private String approvalByL1;
	private String approvalByL2;

	private MultipartFile docBackPath;
	private MultipartFile docFrontPath;

	private EntityModel entityModel;

	private MandateDocumentModel mandateDocumentModel;

	public String getKycDocName() {
		return kycDocName;
	}

	public void setKycDocName(String kycDocName) {
		this.kycDocName = kycDocName;
	}

	public String getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(String createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public String getModifiedDateAndTime() {
		return modifiedDateAndTime;
	}

	public void setModifiedDateAndTime(String modifiedDateAndTime) {
		this.modifiedDateAndTime = modifiedDateAndTime;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public void setDocBackPath(MultipartFile docBackPath) {
		this.docBackPath = docBackPath;
	}

	public void setDocFrontPath(MultipartFile docFrontPath) {
		this.docFrontPath = docFrontPath;
	}

	private String createdDateTime;
	private String modifiedDateAndTime;
	private String docType;



	public boolean isFilingStatus() {
		return filingStatus;
	}

	public void setFilingStatus(boolean filingStatus) {
		this.filingStatus = filingStatus;
	}

	public String getEncrypted_data() {
		return encrypted_data;
	}

	public void setEncrypted_data(String encrypted_data) {
		this.encrypted_data = encrypted_data;
	}

	public String getReference_id() {
		return reference_id;
	}

	public void setReference_id(String reference_id) {
		this.reference_id = reference_id;
	}

	public String getSource_type() {
		return source_type;
	}

	public void setSource_type(String source_type) {
		this.source_type = source_type;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
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

	public int getLoginFailCount() {
		return loginFailCount;
	}

	public void setLoginFailCount(int loginFailCount) {
		this.loginFailCount = loginFailCount;
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

	public Date getOtpExpiryOn() {
		return otpExpiryOn;
	}

	public void setOtpExpiryOn(Date otpExpiryOn) {
		this.otpExpiryOn = otpExpiryOn;
	}

	public List<PasswordModelHistory> getPasswordHistory() {
		return passwordHistory;
	}

	public void setPasswordHistory(List<PasswordModelHistory> passwordHistory) {
		this.passwordHistory = passwordHistory;
	}

	public int getPasswordId() {
		return passwordId;
	}

	public void setPasswordId(int passwordId) {
		this.passwordId = passwordId;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getUserSaltKey() {
		return userSaltKey;
	}

	public void setUserSaltKey(String userSaltKey) {
		this.userSaltKey = userSaltKey;
	}

	public int getCurrentPasswordStatus() {
		return currentPasswordStatus;
	}

	public void setCurrentPasswordStatus(int currentPasswordStatus) {
		this.currentPasswordStatus = currentPasswordStatus;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
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

	public EntityModel getUser() {
		return user;
	}

	public void setUser(EntityModel user) {
		this.user = user;
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

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getMicrCode() {
		return micrCode;
	}

	public void setMicrCode(String micrCode) {
		this.micrCode = micrCode;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public boolean isBankAccountStatus() {
		return bankAccountStatus;
	}

	public void setBankAccountStatus(boolean bankAccountStatus) {
		this.bankAccountStatus = bankAccountStatus;
	}

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getWebsiteIntegration() {
		return websiteIntegration;
	}

	public void setWebsiteIntegration(String websiteIntegration) {
		this.websiteIntegration = websiteIntegration;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getPlugin() {
		return plugin;
	}

	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}

	public int getResponseId() {
		return responseId;
	}

	public void setResponseId(int responseId) {
		this.responseId = responseId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getPublicUrl() {
		return publicUrl;
	}

	public void setPublicUrl(String publicUrl) {
		this.publicUrl = publicUrl;
	}

	public String getLicenceNumber() {
		return licenceNumber;
	}

	public void setLicenceNumber(String licenceNumber) {
		this.licenceNumber = licenceNumber;
	}

	public String getLicenceType() {
		return licenceType;
	}

	public void setLicenceType(String licenceType) {
		this.licenceType = licenceType;
	}

	public String getLicenceIssueDate() {
		return licenceIssueDate;
	}

	public void setLicenceIssueDate(String licenceIssueDate) {
		this.licenceIssueDate = licenceIssueDate;
	}

	public String getLicenceExpirationDate() {
		return licenceExpirationDate;
	}

	public void setLicenceExpirationDate(String licenceExpirationDate) {
		this.licenceExpirationDate = licenceExpirationDate;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getSmtpAuth() {
		return smtpAuth;
	}

	public void setSmtpAuth(String smtpAuth) {
		this.smtpAuth = smtpAuth;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public String getSmtpConnectionTimeOut() {
		return smtpConnectionTimeOut;
	}

	public void setSmtpConnectionTimeOut(String smtpConnectionTimeOut) {
		this.smtpConnectionTimeOut = smtpConnectionTimeOut;
	}

	public String getStarttlsEnable() {
		return starttlsEnable;
	}

	public void setStarttlsEnable(String starttlsEnable) {
		this.starttlsEnable = starttlsEnable;
	}

	public String getSocketFactoryPort() {
		return socketFactoryPort;
	}

	public void setSocketFactoryPort(String socketFactoryPort) {
		this.socketFactoryPort = socketFactoryPort;
	}

	public String getSocketFactoryClass() {
		return socketFactoryClass;
	}

	public void setSocketFactoryClass(String socketFactoryClass) {
		this.socketFactoryClass = socketFactoryClass;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getMailUserName() {
		return mailUserName;
	}

	public void setMailUserName(String mailUserName) {
		this.mailUserName = mailUserName;
	}

	public String getMailPassword() {
		return mailPassword;
	}

	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}

	public String getSmtpTimeOut() {
		return smtpTimeOut;
	}

	public void setSmtpTimeOut(String smtpTimeOut) {
		this.smtpTimeOut = smtpTimeOut;
	}

	public String getSmtpWriteTimeOut() {
		return smtpWriteTimeOut;
	}

	public void setSmtpWriteTimeOut(String smtpWriteTimeOut) {
		this.smtpWriteTimeOut = smtpWriteTimeOut;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
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

	public MultipartFile getFileAttachment() {
		return fileAttachment;
	}

	public void setFileAttachment(MultipartFile fileAttachment) {
		this.fileAttachment = fileAttachment;
	}

	public boolean isTicketStatus() {
		return ticketStatus;
	}

	public void setTicketStatus(boolean ticketStatus) {
		this.ticketStatus = ticketStatus;
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

	public int getSmsTempId() {
		return smsTempId;
	}

	public void setSmsTempId(int smsTempId) {
		this.smsTempId = smsTempId;
	}

	public String getSmsTempCode() {
		return smsTempCode;
	}

	public void setSmsTempCode(String smsTempCode) {
		this.smsTempCode = smsTempCode;
	}

	public String getSmsTempMessage() {
		return smsTempMessage;
	}

	public void setSmsTempMessage(String smsTempMessage) {
		this.smsTempMessage = smsTempMessage;
	}

	public String getSmsTempDescription() {
		return smsTempDescription;
	}

	public void setSmsTempDescription(String smsTempDescription) {
		this.smsTempDescription = smsTempDescription;
	}

	public boolean isSmsTempStatus() {
		return smsTempStatus;
	}

	public void setSmsTempStatus(boolean smsTempStatus) {
		this.smsTempStatus = smsTempStatus;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
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

	public JsonNode getResponse() {
		return response;
	}

	public void setResponse(JsonNode response) {
		this.response = response;
	}

	public String getResponseDateAndTime() {
		return responseDateAndTime;
	}

	public void setResponseDateAndTime(String responseDateAndTime) {
		this.responseDateAndTime = responseDateAndTime;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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

	public String getWorkerName() {
		return workerName;
	}

	public void setWorkerName(String workerName) {
		this.workerName = workerName;
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

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public int getVendorId() {
		return vendorId;
	}

	public void setVendorId(int vendorId) {
		this.vendorId = vendorId;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public int getVendorVerificationId() {
		return vendorVerificationId;
	}

	public void setVendorVerificationId(int vendorVerificationId) {
		this.vendorVerificationId = vendorVerificationId;
	}

	public String getVerificationDocument() {
		return verificationDocument;
	}

	public void setVerificationDocument(String verificationDocument) {
		this.verificationDocument = verificationDocument;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public VendorModel getVendorModel() {
		return vendorModel;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public void setVendorModel(VendorModel vendorModel) {
		this.vendorModel = vendorModel;
	}

	public boolean isVendorStatus() {
		return vendorStatus;
	}

	public void setVendorStatus(boolean vendorStatus) {
		this.vendorStatus = vendorStatus;
	}

	public boolean isVendorVerifyStatus() {
		return vendorVerifyStatus;
	}

	public void setVendorVerifyStatus(boolean vendorVerifyStatus) {
		this.vendorVerifyStatus = vendorVerifyStatus;
	}

	public int getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(int paymentId) {
		this.paymentId = paymentId;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public int getPrepaidId() {
		return prepaidId;
	}

	public void setPrepaidId(int prepaidId) {
		this.prepaidId = prepaidId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public double getRechargedAmount() {
		return rechargedAmount;
	}

	public void setRechargedAmount(double rechargedAmount) {
		this.rechargedAmount = rechargedAmount;
	}

	public double getConsumedAmount() {
		return consumedAmount;
	}

	public void setConsumedAmount(double consumedAmount) {
		this.consumedAmount = consumedAmount;
	}

	public int getLogPeriod() {
		return logPeriod;
	}

	public void setLogPeriod(int logPeriod) {
		this.logPeriod = logPeriod;
	}

	public LocalDate getLogUpdatedAt() {
		return logUpdatedAt;
	}

	public void setLogUpdatedAt(LocalDate logUpdatedAt) {
		this.logUpdatedAt = logUpdatedAt;
	}

	public String getLogUpdatedBy() {
		return logUpdatedBy;
	}

	public void setLogUpdatedBy(String logUpdatedBy) {
		this.logUpdatedBy = logUpdatedBy;
	}

	public double getRemainingAmount() {
		return remainingAmount;
	}

	public void setRemainingAmount(double remainingAmount) {
		this.remainingAmount = remainingAmount;
	}

	public int getTotalHit() {
		return totalHit;
	}

	public void setTotalHit(int totalHit) {
		this.totalHit = totalHit;
	}

	public int getTechnicalId() {
		return technicalId;
	}

	public void setTechnicalId(int technicalId) {
		this.technicalId = technicalId;
	}

	public int getMakerCheckerId() {
		return makerCheckerId;
	}

	public void setMakerCheckerId(int makerCheckerId) {
		this.makerCheckerId = makerCheckerId;
	}

	public String getMakerCheckerRoleName() {
		return makerCheckerRoleName;
	}

	public void setMakerCheckerRoleName(String makerCheckerRoleName) {
		this.makerCheckerRoleName = makerCheckerRoleName;
	}

	public int getAdminId() {
		return adminId;
	}

	public void setAdminId(int adminId) {
		this.adminId = adminId;
	}

	public String getMakerCheckeRoleCode() {
		return makerCheckeRoleCode;
	}

	public void setMakerCheckeRoleCode(String makerCheckeRoleCode) {
		this.makerCheckeRoleCode = makerCheckeRoleCode;
	}

	public String getMakerCheckerRoleType() {
		return makerCheckerRoleType;
	}

	public void setMakerCheckerRoleType(String makerCheckerRoleType) {
		this.makerCheckerRoleType = makerCheckerRoleType;
	}

	public RoleDto getRoleModel() {
		return roleModel;
	}

	public int getPostpaidId() {
		return postpaidId;
	}

	public void setPostpaidId(int postpaidId) {
		this.postpaidId = postpaidId;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public LocalDate getPeriod() {
		return period;
	}

	public void setPeriod(LocalDate period) {
		this.period = period;
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

	public LocalDate getGraceDate() {
		return graceDate;
	}

	public void setGraceDate(LocalDate graceDate) {
		this.graceDate = graceDate;
	}

	public int getGracePeriod() {
		return gracePeriod;
	}

	public void setGracePeriod(int gracePeriod) {
		this.gracePeriod = gracePeriod;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public int getTransactId() {
		return transactId;
	}

	public void setTransactId(int transactId) {
		this.transactId = transactId;
	}

	public String getPayingId() {
		return payingId;
	}

	public void setPayingId(String payingId) {
		this.payingId = payingId;
	}

	public JsonNode getEtc() {
		return etc;
	}

	public void setEtc(JsonNode etc) {
		this.etc = etc;
	}

	public int getCountryId() {
		return countryId;
	}

	public void setCountryId(int countryId) {
		this.countryId = countryId;
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

	public boolean isCountryStatus() {
		return countryStatus;
	}

	public void setCountryStatus(boolean countryStatus) {
		this.countryStatus = countryStatus;
	}

	public int getStateId() {
		return stateId;
	}

	public void setStateId(int stateId) {
		this.stateId = stateId;
	}

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public boolean isStateStatus() {
		return stateStatus;
	}

	public void setStateStatus(boolean stateStatus) {
		this.stateStatus = stateStatus;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public boolean isCityStatus() {
		return cityStatus;
	}

	public void setCityStatus(boolean cityStatus) {
		this.cityStatus = cityStatus;
	}

	public int getPincodeId() {
		return pincodeId;
	}

	public void setPincodeId(int pincodeId) {
		this.pincodeId = pincodeId;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}


	public String getAadharNumber() {
		return aadharNumber;
	}

	public void setAadharNumber(String aadharNumber) {
		this.aadharNumber = aadharNumber;
	}

	public String getUdyamAadharNumber() {
		return udyamAadharNumber;
	}

	public void setUdyamAadharNumber(String udyamAadharNumber) {
		this.udyamAadharNumber = udyamAadharNumber;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getVoterId() {
		return voterId;
	}

	public void setVoterId(String voterId) {
		this.voterId = voterId;
	}

	public String getDrivingLicenceId() {
		return drivingLicenceId;
	}

	public void setDrivingLicenceId(String drivingLicenceId) {
		this.drivingLicenceId = drivingLicenceId;
	}

	public String getPassportId() {
		return passportId;
	}

	public void setPassportId(String passportId) {
		this.passportId = passportId;
	}

	public String getAcknowledgementNumber() {
		return acknowledgementNumber;
	}

	public void setAcknowledgementNumber(String acknowledgementNumber) {
		this.acknowledgementNumber = acknowledgementNumber;
	}

	public String getMcaId() {
		return mcaId;
	}

	public void setMcaId(String mcaId) {
		this.mcaId = mcaId;
	}

	public String getTaxDeductionIdNumber() {
		return taxDeductionIdNumber;
	}

	public void setTaxDeductionIdNumber(String taxDeductionIdNumber) {
		this.taxDeductionIdNumber = taxDeductionIdNumber;
	}

	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

	public String getShopNumber() {
		return shopNumber;
	}

	public void setShopNumber(String shopNumber) {
		this.shopNumber = shopNumber;
	}

	public String getRcNumber() {
		return rcNumber;
	}

	public void setRcNumber(String rcNumber) {
		this.rcNumber = rcNumber;
	}

	public String getIecNumber() {
		return iecNumber;
	}

	public void setIecNumber(String iecNumber) {
		this.iecNumber = iecNumber;
	}

	public String getFssaiNumber() {
		return fssaiNumber;
	}

	public void setFssaiNumber(String fssaiNumber) {
		this.fssaiNumber = fssaiNumber;
	}

	public String getLeiNumber() {
		return leiNumber;
	}

	public void setLeiNumber(String leiNumber) {
		this.leiNumber = leiNumber;
	}

	public String getQrText() {
		return qrText;
	}

	public void setQrText(String qrText) {
		this.qrText = qrText;
	}

	public String getEpfoNumber() {
		return epfoNumber;
	}

	public void setEpfoNumber(String epfoNumber) {
		this.epfoNumber = epfoNumber;
	}

	public String getUanNumber() {
		return uanNumber;
	}

	public void setUanNumber(String uanNumber) {
		this.uanNumber = uanNumber;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getIdNumber() {
		return idNumber;
	}

	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}

	public String getAuthFactorType() {
		return authFactorType;
	}

	public void setAuthFactorType(String authFactorType) {
		this.authFactorType = authFactorType;
	}

	public String getAuthFactor() {
		return authFactor;
	}

	public void setAuthFactor(String authFactor) {
		this.authFactor = authFactor;
	}

	public String getThreshold() {
		return threshold;
	}

	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}

	public String getImage1() {
		return image1;
	}

	public void setImage1(String image1) {
		this.image1 = image1;
	}

	public String getImage2Url() {
		return image2Url;
	}

	public void setImage2Url(String image2Url) {
		this.image2Url = image2Url;
	}

	public String getVideoFile() {
		return videoFile;
	}

	public void setVideoFile(String videoFile) {
		this.videoFile = videoFile;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getUpiId() {
		return upiId;
	}

	public void setUpiId(String upiId) {
		this.upiId = upiId;
	}

	public String getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(String vehicleId) {
		this.vehicleId = vehicleId;
	}

	public String getChassis() {
		return chassis;
	}

	public void setChassis(String chassis) {
		this.chassis = chassis;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getBackImage() {
		return backImage;
	}

	public void setBackImage(String backImage) {
		this.backImage = backImage;
	}

	public int getExtendedData() {
		return extendedData;
	}

	public void setExtendedData(int extendedData) {
		this.extendedData = extendedData;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public long getMobile() {
		return mobile;
	}

	public void setMobile(long mobile) {
		this.mobile = mobile;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getItrId() {
		return itrId;
	}

	public void setItrId(String itrId) {
		this.itrId = itrId;
	}

	public String getTdsId() {
		return tdsId;
	}

	public void setTdsId(String tdsId) {
		this.tdsId = tdsId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCityState() {
		return cityState;
	}

	public void setCityState(String cityState) {
		this.cityState = cityState;
	}

	public String getRtoCode() {
		return rtoCode;
	}

	public void setRtoCode(String rtoCode) {
		this.rtoCode = rtoCode;
	}

	public String getRefId() {
		return refId;
	}

	public void setRefId(String refId) {
		this.refId = refId;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getAttempt() {
		return attempt;
	}

	public void setAttempt(int attempt) {
		this.attempt = attempt;
	}

	public double getVendorSuccessRate() {
		return vendorSuccessRate;
	}

	public void setVendorSuccessRate(double vendorSuccessRate) {
		this.vendorSuccessRate = vendorSuccessRate;
	}

	public String getGender() {
		return Gender;
	}

	public void setGender(String gender) {
		Gender = gender;
	}

	public boolean isMobileVerified() {
		return mobileVerified;
	}

	public void setMobileVerified(boolean mobileVerified) {
		this.mobileVerified = mobileVerified;
	}

	public int getSubPermissionId() {
		return subPermissionId;
	}

	public void setSubPermissionId(int subPermissionId) {
		this.subPermissionId = subPermissionId;
	}

	public String getSubPermissionName() {
		return subPermissionName;
	}

	public int getDataComplaintId() {
		return dataComplaintId;
	}

	public void setDataComplaintId(int dataComplaintId) {
		this.dataComplaintId = dataComplaintId;
	}

	public String getMerchantEmail() {
		return merchantEmail;
	}

	public void setMerchantEmail(String merchantEmail) {
		this.merchantEmail = merchantEmail;
	}

	public String getVerificationType() {
		return verificationType;
	}

	public void setVerificationType(String verificationType) {
		this.verificationType = verificationType;
	}

	public LocalDate getFetchedDate() {
		return fetchedDate;
	}

	public void setFetchedDate(LocalDate fetchedDate) {
		this.fetchedDate = fetchedDate;
	}

	public LocalDate getLastFetchedDate() {
		return lastFetchedDate;
	}

	public void setLastFetchedDate(LocalDate lastFetchedDate) {
		this.lastFetchedDate = lastFetchedDate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public LocalDate getSubmittedDate() {
		return submittedDate;
	}

	public void setSubmittedDate(LocalDate submittedDate) {
		this.submittedDate = submittedDate;
	}

	public void setSubPermissionName(String subPermissionName) {
		this.subPermissionName = subPermissionName;
	}

	public List<Integer> getSubPermissions() {
		return subPermissions;
	}

	public void setSubPermissions(List<Integer> subPermissions) {
		this.subPermissions = subPermissions;
	}

	public int getSuperAdminId() {
		return superAdminId;
	}

	public void setSuperAdminId(int superAdminId) {
		this.superAdminId = superAdminId;
	}

	public String getCommonResponse() {
		return commonResponse;
	}

	public void setCommonResponse(String commonResponse) {
		this.commonResponse = commonResponse;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean isSourcePresent() {
		return sourcePresent;
	}

	public void setSourcePresent(boolean sourcePresent) {
		this.sourcePresent = sourcePresent;
	}

	public String getLegalName() {
		return legalName;
	}

	public void setLegalName(String legalName) {
		this.legalName = legalName;
	}

	public String getCenterJurisdiction() {
		return centerJurisdiction;
	}

	public void setCenterJurisdiction(String centerJurisdiction) {
		this.centerJurisdiction = centerJurisdiction;
	}

	public String getStateJurisdiction() {
		return stateJurisdiction;
	}

	public void setStateJurisdiction(String stateJurisdiction) {
		this.stateJurisdiction = stateJurisdiction;
	}

	public String getConstitutionOfBusiness() {
		return constitutionOfBusiness;
	}

	public void setConstitutionOfBusiness(String constitutionOfBusiness) {
		this.constitutionOfBusiness = constitutionOfBusiness;
	}

	public String getTaxpayerType() {
		return taxpayerType;
	}

	public void setTaxpayerType(String taxpayerType) {
		this.taxpayerType = taxpayerType;
	}

	public String getGstInStatus() {
		return gstInStatus;
	}

	public void setGstInStatus(String gstInStatus) {
		this.gstInStatus = gstInStatus;
	}

	public String getFieldVisitConducted() {
		return fieldVisitConducted;
	}

	public void setFieldVisitConducted(String fieldVisitConducted) {
		this.fieldVisitConducted = fieldVisitConducted;
	}

	public String getCoreBusinessActivityCode() {
		return coreBusinessActivityCode;
	}

	public void setCoreBusinessActivityCode(String coreBusinessActivityCode) {
		this.coreBusinessActivityCode = coreBusinessActivityCode;
	}

	public String getCoreBusinessActivityDescription() {
		return coreBusinessActivityDescription;
	}

	public void setCoreBusinessActivityDescription(String coreBusinessActivityDescription) {
		this.coreBusinessActivityDescription = coreBusinessActivityDescription;
	}

	public String getAadharValidation() {
		return aadharValidation;
	}

	public void setAadharValidation(String aadharValidation) {
		this.aadharValidation = aadharValidation;
	}

	public String getAadharValidatedDate() {
		return aadharValidatedDate;
	}

	public void setAadharValidatedDate(String aadharValidatedDate) {
		this.aadharValidatedDate = aadharValidatedDate;
	}

	public String getDateOfCancellation() {
		return dateOfCancellation;
	}

	public void setDateOfCancellation(String dateOfCancellation) {
		this.dateOfCancellation = dateOfCancellation;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getMobileHash() {
		return mobileHash;
	}

	public void setMobileHash(String mobileHash) {
		this.mobileHash = mobileHash;
	}

	public String getEmailHash() {
		return emailHash;
	}

	public void setEmailHash(String emailHash) {
		this.emailHash = emailHash;
	}

	public String getRawXml() {
		return rawXml;
	}

	public void setRawXml(String rawXml) {
		this.rawXml = rawXml;
	}

	public String getZipData() {
		return zipData;
	}

	public void setZipData(String zipData) {
		this.zipData = zipData;
	}

	public String getCareOf() {
		return careOf;
	}

	public void setCareOf(String careOf) {
		this.careOf = careOf;
	}

	public String getShareCode() {
		return shareCode;
	}

	public void setShareCode(String shareCode) {
		this.shareCode = shareCode;
	}

	public String getAadharReferenceId() {
		return aadharReferenceId;
	}

	public void setAadharReferenceId(String aadharReferenceId) {
		this.aadharReferenceId = aadharReferenceId;
	}

	public String getAadharStatus() {
		return aadharStatus;
	}

	public void setAadharStatus(String aadharStatus) {
		this.aadharStatus = aadharStatus;
	}

	public String getUniquenessId() {
		return uniquenessId;
	}

	public void setUniquenessId(String uniquenessId) {
		this.uniquenessId = uniquenessId;
	}

	public boolean isFaceStatus() {
		return faceStatus;
	}

	public void setFaceStatus(boolean faceStatus) {
		this.faceStatus = faceStatus;
	}

	public int getFaceScore() {
		return faceScore;
	}

	public void setFaceScore(int faceScore) {
		this.faceScore = faceScore;
	}

	public boolean isHasImage() {
		return hasImage;
	}

	public void setHasImage(boolean hasImage) {
		this.hasImage = hasImage;
	}

	public String getCompanyInfoJsonObject() {
		return companyInfoJsonObject;
	}

	public void setCompanyInfoJsonObject(String companyInfoJsonObject) {
		this.companyInfoJsonObject = companyInfoJsonObject;
	}

	public String getDirectorsJsonArray() {
		return directorsJsonArray;
	}

	public void setDirectorsJsonArray(String directorsJsonArray) {
		this.directorsJsonArray = directorsJsonArray;
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

	public String getPermanentZip() {
		return permanentZip;
	}

	public void setPermanentZip(String permanentZip) {
		this.permanentZip = permanentZip;
	}

	public String getTemporaryAddress() {
		return temporaryAddress;
	}

	public void setTemporaryAddress(String temporaryAddress) {
		this.temporaryAddress = temporaryAddress;
	}

	public String getTemporaryZip() {
		return temporaryZip;
	}

	public void setTemporaryZip(String temporaryZip) {
		this.temporaryZip = temporaryZip;
	}

	public String getCitizenShip() {
		return citizenShip;
	}

	public void setCitizenShip(String citizenShip) {
		this.citizenShip = citizenShip;
	}

	public String getOlaName() {
		return olaName;
	}

	public void setOlaName(String olaName) {
		this.olaName = olaName;
	}

	public String getOlaCode() {
		return olaCode;
	}

	public void setOlaCode(String olaCode) {
		this.olaCode = olaCode;
	}

	public String getFatherOrHusbandName() {
		return fatherOrHusbandName;
	}

	public void setFatherOrHusbandName(String fatherOrHusbandName) {
		this.fatherOrHusbandName = fatherOrHusbandName;
	}

	public String getDoe() {
		return doe;
	}

	public void setDoe(String doe) {
		this.doe = doe;
	}

	public String getTransportDoe() {
		return transportDoe;
	}

	public void setTransportDoe(String transportDoe) {
		this.transportDoe = transportDoe;
	}

	public String getDoi() {
		return doi;
	}

	public void setDoi(String doi) {
		this.doi = doi;
	}

	public String getTransportDoi() {
		return transportDoi;
	}

	public void setTransportDoi(String transportDoi) {
		this.transportDoi = transportDoi;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public String getInitialDoi() {
		return initialDoi;
	}

	public void setInitialDoi(String initialDoi) {
		this.initialDoi = initialDoi;
	}

	public String getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	public boolean isLessInfo() {
		return lessInfo;
	}

	public void setLessInfo(boolean lessInfo) {
		this.lessInfo = lessInfo;
	}

	public String getVehicleClasses() {
		return vehicleClasses;
	}

	public void setVehicleClasses(String vehicleClasses) {
		this.vehicleClasses = vehicleClasses;
	}

	public String getAdditionalCheck() {
		return additionalCheck;
	}

	public void setAdditionalCheck(String additionalCheck) {
		this.additionalCheck = additionalCheck;
	}

	public int getApprovalRejectionId() {
		return approvalRejectionId;
	}

	public void setApprovalRejectionId(int approvalRejectionId) {
		this.approvalRejectionId = approvalRejectionId;
	}

	public String getApproveReject() {
		return approveReject;
	}

	public void setApproveReject(String approveReject) {
		this.approveReject = approveReject;
	}

	public long getRejectionCount() {
		return rejectionCount;
	}

	public void setRejectionCount(long rejectionCount) {
		this.rejectionCount = rejectionCount;
	}

	public boolean isApprovedStatus() {
		return approvedStatus;
	}

	public void setApprovedStatus(boolean approvedStatus) {
		this.approvedStatus = approvedStatus;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Long getRejectedCount() {
		return rejectedCount;
	}

	public void setRejectedCount(Long rejectedCount) {
		this.rejectedCount = rejectedCount;
	}

	public String getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(String approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getDateOfApplication() {
		return dateOfApplication;
	}

	public void setDateOfApplication(String dateOfApplication) {
		this.dateOfApplication = dateOfApplication;
	}

	public String getFileNumber() {
		return fileNumber;
	}

	public void setFileNumber(String fileNumber) {
		this.fileNumber = fileNumber;
	}

	public String getVerificationMessage() {
		return verificationMessage;
	}

	public void setVerificationMessage(String verificationMessage) {
		this.verificationMessage = verificationMessage;
	}

	public String getNameOfEnterprise() {
		return nameOfEnterprise;
	}

	public void setNameOfEnterprise(String nameOfEnterprise) {
		this.nameOfEnterprise = nameOfEnterprise;
	}

	public String getMajorActivity() {
		return majorActivity;
	}

	public void setMajorActivity(String majorActivity) {
		this.majorActivity = majorActivity;
	}

	public String getSocialCategory() {
		return socialCategory;
	}

	public void setSocialCategory(String socialCategory) {
		this.socialCategory = socialCategory;
	}

	public String getDateOfCommencement() {
		return dateOfCommencement;
	}

	public void setDateOfCommencement(String dateOfCommencement) {
		this.dateOfCommencement = dateOfCommencement;
	}

	public String getDicName() {
		return dicName;
	}

	public void setDicName(String dicName) {
		this.dicName = dicName;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getAppliedDate() {
		return appliedDate;
	}

	public void setAppliedDate(String appliedDate) {
		this.appliedDate = appliedDate;
	}


	public String getEnterpriseType() {
		return enterpriseType;
	}

	public void setEnterpriseType(String enterpriseType) {
		this.enterpriseType = enterpriseType;
	}

	public String getUan() {
		return uan;
	}

	public void setUan(String uan) {
		this.uan = uan;
	}

	public String getLocationOfPlantDetails() {
		return locationOfPlantDetails;
	}

	public void setLocationOfPlantDetails(String locationOfPlantDetails) {
		this.locationOfPlantDetails = locationOfPlantDetails;
	}

	public String getNicCode() {
		return nicCode;
	}

	public void setNicCode(String nicCode) {
		this.nicCode = nicCode;
	}

	public int getDataMatchFullName() {
		return dataMatchFullName;
	}

	public void setDataMatchFullName(int dataMatchFullName) {
		this.dataMatchFullName = dataMatchFullName;
	}

	public int getDataMatchPanNumber() {
		return dataMatchPanNumber;
	}

	public void setDataMatchPanNumber(int dataMatchPanNumber) {
		this.dataMatchPanNumber = dataMatchPanNumber;
	}

	public int getDataMatchAggregate() {
		return dataMatchAggregate;
	}

	public void setDataMatchAggregate(int dataMatchAggregate) {
		this.dataMatchAggregate = dataMatchAggregate;
	}

	public String getFathersName() {
		return fathersName;
	}

	public void setFathersName(String fathersName) {
		this.fathersName = fathersName;
	}

	public String getIpaddress() {
		return ipaddress;
	}

	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}

	public List<MerchantDto> getMerchant() {
		return merchant;
	}

	public void setMerchant(List<MerchantDto> merchant) {
		this.merchant = merchant;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getMemberShipNumber() {
		return memberShipNumber;
	}

	public void setMemberShipNumber(String memberShipNumber) {
		this.memberShipNumber = memberShipNumber;
	}

	public String getFatherName() {
		return fatherName;
	}

	public void setFatherName(String fatherName) {
		this.fatherName = fatherName;
	}

	public String getMotherName() {
		return motherName;
	}

	public void setMotherName(String motherName) {
		this.motherName = motherName;
	}

	public String getSpouseName() {
		return spouseName;
	}

	public void setSpouseName(String spouseName) {
		this.spouseName = spouseName;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public LocalDate getModifieddAt() {
		return modifieddAt;
	}

	public void setModifieddAt(LocalDate modifieddAt) {
		this.modifieddAt = modifieddAt;
	}

	public boolean isFirstPayment() {
		return firstPayment;
	}

	public void setFirstPayment(boolean firstPayment) {
		this.firstPayment = firstPayment;
	}

	public String getDocumentTitle() {
		return documentTitle;
	}

	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}

	public int getLoginFailedCount() {
		return loginFailedCount;
	}

	public void setLoginFailedCount(int loginFailedCount) {
		this.loginFailedCount = loginFailedCount;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public Date getCreatedDatetime() {
		return createdDatetime;
	}

	public void setCreatedDatetime(Date createdDatetime) {
		this.createdDatetime = createdDatetime;
	}

	public String getCreatedby() {
		return createdby;
	}

	public void setCreatedby(String createdby) {
		this.createdby = createdby;
	}

	public Date getCreateddateandtime() {
		return createddateandtime;
	}

	public void setCreateddateandtime(Date createddateandtime) {
		this.createddateandtime = createddateandtime;
	}

	public boolean isPincodestatus() {
		return pincodestatus;
	}

	public void setPincodestatus(boolean pincodestatus) {
		this.pincodestatus = pincodestatus;
	}

	public boolean isStatusFlag() {
		return statusFlag;
	}

	public void setStatusFlag(boolean statusFlag) {
		this.statusFlag = statusFlag;
	}

	public int getCategoryMembershipId() {
		return categoryMembershipId;
	}

	public void setCategoryMembershipId(int categoryMembershipId) {
		this.categoryMembershipId = categoryMembershipId;
	}

	public String getCategoryMembership() {
		return categoryMembership;
	}

	public void setCategoryMembership(String categoryMembership) {
		this.categoryMembership = categoryMembership;
	}

	public double getMembershipFee() {
		return membershipFee;
	}

	public void setMembershipFee(double membershipFee) {
		this.membershipFee = membershipFee;
	}

	public double getAnnualFee() {
		return annualFee;
	}

	public void setAnnualFee(double annualFee) {
		this.annualFee = annualFee;
	}

	public int getFamilyId() {
		return familyId;
	}

	public void setFamilyId(int familyId) {
		this.familyId = familyId;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getMartialStatus() {
		return martialStatus;
	}

	public void setMartialStatus(String martialStatus) {
		this.martialStatus = martialStatus;
	}

	public int getEducationQualificationId() {
		return educationQualificationId;
	}

	public void setEducationQualificationId(int educationQualificationId) {
		this.educationQualificationId = educationQualificationId;
	}

	public String getEducation() {
		return education;
	}

	public void setEducation(String education) {
		this.education = education;
	}

	public String getInstitute() {
		return institute;
	}

	public void setInstitute(String institute) {
		this.institute = institute;
	}

	public String getYearOfPassout() {
		return yearOfPassout;
	}

	public void setYearOfPassout(String yearOfPassout) {
		this.yearOfPassout = yearOfPassout;
	}

	public String getMarksInPercentage() {
		return marksInPercentage;
	}

	public void setMarksInPercentage(String marksInPercentage) {
		this.marksInPercentage = marksInPercentage;
	}

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		this.board = board;
	}

	public String getMedium() {
		return medium;
	}

	public void setMedium(String medium) {
		this.medium = medium;
	}

	public int getPreferredSectionId() {
		return preferredSectionId;
	}

	public void setPreferredSectionId(int preferredSectionId) {
		this.preferredSectionId = preferredSectionId;
	}

	public String getPreferredSection() {
		return preferredSection;
	}

	public void setPreferredSection(String preferredSection) {
		this.preferredSection = preferredSection;
	}

	public int getProofId() {
		return proofId;
	}

	public void setProofId(int proofId) {
		this.proofId = proofId;
	}

	public String getProofDocName() {
		return proofDocName;
	}

	public void setProofDocName(String proofDocName) {
		this.proofDocName = proofDocName;
	}

	public MultipartFile getThumbnailImage() {
		return thumbnailImage;
	}

	public void setThumbnailImage(MultipartFile thumbnailImage) {
		this.thumbnailImage = thumbnailImage;
	}

	public MultipartFile[] getSctionImages() {
		return sctionImages;
	}

	public void setSctionImages(MultipartFile[] sctionImages) {
		this.sctionImages = sctionImages;
	}

	public String getIfscNumber() {
		return ifscNumber;
	}

	public void setIfscNumber(String ifscNumber) {
		this.ifscNumber = ifscNumber;
	}

	public String getSmsEnable() {
		return smsEnable;
	}

	public void setSmsEnable(String smsEnable) {
		this.smsEnable = smsEnable;
	}

	public int getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(int libraryId) {
		this.libraryId = libraryId;
	}

	public String getLibraryName() {
		return libraryName;
	}

	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}

	public String getLibraryCode() {
		return libraryCode;
	}

	public void setLibraryCode(String libraryCode) {
		this.libraryCode = libraryCode;
	}

	public String getLandLineNumber() {
		return landLineNumber;
	}

	public void setLandLineNumber(String landLineNumber) {
		this.landLineNumber = landLineNumber;
	}

	public int getPayid() {
		return payid;
	}

	public void setPayid(int payid) {
		this.payid = payid;
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

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public Date getPaymentDatetime() {
		return paymentDatetime;
	}

	public void setPaymentDatetime(Date paymentDatetime) {
		this.paymentDatetime = paymentDatetime;
	}

	public String getOrderReference() {
		return orderReference;
	}

	public void setOrderReference(String orderReference) {
		this.orderReference = orderReference;
	}

	public int getRenewableId() {
		return renewableId;
	}

	public void setRenewableId(int renewableId) {
		this.renewableId = renewableId;
	}

	public String getRewableDate() {
		return rewableDate;
	}

	public void setRewableDate(String rewableDate) {
		this.rewableDate = rewableDate;
	}

	public double getRenewableAmount() {
		return renewableAmount;
	}

	public void setRenewableAmount(double renewableAmount) {
		this.renewableAmount = renewableAmount;
	}

	public String getNextRenewableDate() {
		return nextRenewableDate;
	}

	public void setNextRenewableDate(String nextRenewableDate) {
		this.nextRenewableDate = nextRenewableDate;
	}

	public double getNextRenewableAmount() {
		return nextRenewableAmount;
	}

	public void setNextRenewableAmount(double nextRenewableAmount) {
		this.nextRenewableAmount = nextRenewableAmount;
	}

	public int getPlanTypeId() {
		return planTypeId;
	}

	public void setPlanTypeId(int planTypeId) {
		this.planTypeId = planTypeId;
	}

	public String getPlanTypeName() {
		return planTypeName;
	}

	public void setPlanTypeName(String planTypeName) {
		this.planTypeName = planTypeName;
	}

	public int getActiveStatus() {
		return activeStatus;
	}

	public void setActiveStatus(int activeStatus) {
		this.activeStatus = activeStatus;
	}

	public int getPlanId() {
		return planId;
	}

	public void setPlanId(int planId) {
		this.planId = planId;
	}

	public String getPlanName() {
		return planName;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}

	public String getPlanDescription() {
		return planDescription;
	}

	public void setPlanDescription(String planDescription) {
		this.planDescription = planDescription;
	}

	public int getUserTimeSlotId() {
		return userTimeSlotId;
	}

	public void setUserTimeSlotId(int userTimeSlotId) {
		this.userTimeSlotId = userTimeSlotId;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getSlotCreatedAt() {
		return slotCreatedAt;
	}

	public void setSlotCreatedAt(String slotCreatedAt) {
		this.slotCreatedAt = slotCreatedAt;
	}

	public String getRequesterName() {
		return requesterName;
	}

	public void setRequesterName(String requesterName) {
		this.requesterName = requesterName;
	}

	public String getAlternativeMobileNumber() {
		return alternativeMobileNumber;
	}

	public void setAlternativeMobileNumber(String alternativeMobileNumber) {
		this.alternativeMobileNumber = alternativeMobileNumber;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public long getAudianceCount() {
		return audianceCount;
	}

	public void setAudianceCount(long audianceCount) {
		this.audianceCount = audianceCount;
	}

	public boolean isApproveStatus() {
		return approveStatus;
	}

	public void setApproveStatus(boolean approveStatus) {
		this.approveStatus = approveStatus;
	}

	public String getRequestAt() {
		return requestAt;
	}

	public void setRequestAt(String requestAt) {
		this.requestAt = requestAt;
	}

	public String getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(String modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public int getTimeRequestId() {
		return timeRequestId;
	}

	public void setTimeRequestId(int timeRequestId) {
		this.timeRequestId = timeRequestId;
	}

	public String getFromTime() {
		return fromTime;
	}

	public void setFromTime(String fromTime) {
		this.fromTime = fromTime;
	}

	public String getToTime() {
		return toTime;
	}

	public void setToTime(String toTime) {
		this.toTime = toTime;
	}

	public double getHrs() {
		return hrs;
	}

	public void setHrs(double hrs) {
		this.hrs = hrs;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getRequestBookId() {
		return requestBookId;
	}

	public void setRequestBookId(int requestBookId) {
		this.requestBookId = requestBookId;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public String getJournalType() {
		return journalType;
	}

	public void setJournalType(String journalType) {
		this.journalType = journalType;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
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

	public String getRateAgreed() {
		return rateAgreed;
	}

	public void setRateAgreed(String rateAgreed) {
		this.rateAgreed = rateAgreed;
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

	public List<MerchantModel> getModel() {
		return model;
	}

	public void setModel(List<MerchantModel> model) {
		this.model = model;
	}

	public int getBondId() {
		return bondId;
	}

	public void setBondId(int bondId) {
		this.bondId = bondId;
	}

	public double getBondAmount() {
		return bondAmount;
	}

	public void setBondAmount(double bondAmount) {
		this.bondAmount = bondAmount;
	}

	public int getUploadCount() {
		return uploadCount;
	}

	public void setUploadCount(int uploadCount) {
		this.uploadCount = uploadCount;
	}

	public int getUsedCount() {
		return usedCount;
	}

	public void setUsedCount(int usedCount) {
		this.usedCount = usedCount;
	}

	public int getRemainingCount() {
		return remainingCount;
	}

	public void setRemainingCount(int remainingCount) {
		this.remainingCount = remainingCount;
	}

	public int getBondDetailId() {
		return bondDetailId;
	}

	public void setBondDetailId(int bondDetailId) {
		this.bondDetailId = bondDetailId;
	}

	public String getSealedDate() {
		return sealedDate;
	}

	public void setSealedDate(String sealedDate) {
		this.sealedDate = sealedDate;
	}

	public MultipartFile getDocument() {
		return document;
	}

	public void setDocument(MultipartFile document) {
		this.document = document;
	}

	public int getBondStatus() {
		return bondStatus;
	}

	public void setBondStatus(int bondStatus) {
		this.bondStatus = bondStatus;
	}

	public String getBondNumber() {
		return bondNumber;
	}

	public void setBondNumber(String bondNumber) {
		this.bondNumber = bondNumber;
	}

	public int getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(int departmentId) {
		this.departmentId = departmentId;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getDepartmentHeadName() {
		return departmentHeadName;
	}

	public void setDepartmentHeadName(String departmentHeadName) {
		this.departmentHeadName = departmentHeadName;
	}

	public String getBranchAddress() {
		return branchAddress;
	}

	public void setBranchAddress(String branchAddress) {
		this.branchAddress = branchAddress;
	}

	public String getSignatoryName() {
		return signatoryName;
	}

	public void setSignatoryName(String signatoryName) {
		this.signatoryName = signatoryName;
	}

	public String getSignatoryDesignation() {
		return signatoryDesignation;
	}

	public void setSignatoryDesignation(String signatoryDesignation) {
		this.signatoryDesignation = signatoryDesignation;
	}

	public String getSignatoryDate() {
		return signatoryDate;
	}

	public void setSignatoryDate(String signatoryDate) {
		this.signatoryDate = signatoryDate;
	}

	public String getMerchantCompanyName() {
		return merchantCompanyName;
	}

	public void setMerchantCompanyName(String merchantCompanyName) {
		this.merchantCompanyName = merchantCompanyName;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getSignerId() {
		return signerId;
	}

	public void setSignerId(int signerId) {
		this.signerId = signerId;
	}

	public String getSignerName() {
		return signerName;
	}

	public void setSignerName(String signerName) {
		this.signerName = signerName;
	}

	public String getSignerMobile() {
		return SignerMobile;
	}

	public void setSignerMobile(String signerMobile) {
		SignerMobile = signerMobile;
	}

	public String getSignerEmail() {
		return signerEmail;
	}

	public void setSignerEmail(String signerEmail) {
		this.signerEmail = signerEmail;
	}

	public LocalDate getSignedAt() {
		return signedAt;
	}

	public void setSignedAt(LocalDate signedAt) {
		this.signedAt = signedAt;
	}

	public MultipartFile getPdfDocument() {
		return pdfDocument;
	}

	public void setPdfDocument(MultipartFile pdfDocument) {
		this.pdfDocument = pdfDocument;
	}

	public List<SignerModel> getSigner() {
		return signer;
	}

	public void setSigner(List<SignerModel> signer) {
		this.signer = signer;
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

	public boolean isBond() {
		return bond;
	}

	public void setBond(boolean bond) {
		this.bond = bond;
	}

	public double getSignaturePrice() {
		return signaturePrice;
	}

	public void setSignaturePrice(double signaturePrice) {
		this.signaturePrice = signaturePrice;
	}

	public String getTradeName() {
		return tradeName;
	}

	public void setTradeName(String tradeName) {
		this.tradeName = tradeName;
	}

	public String getHsnInfo() {
		return hsnInfo;
	}

	public void setHsnInfo(String hsnInfo) {
		this.hsnInfo = hsnInfo;
	}

	public String getFilingStatusList() {
		return filingStatusList;
	}

	public void setFilingStatusList(String filingStatusList) {
		this.filingStatusList = filingStatusList;
	}

	public String getNatureBusActivity() {
		return natureBusActivity;
	}

	public void setNatureBusActivity(String natureBusActivity) {
		this.natureBusActivity = natureBusActivity;
	}

	public int getDataMatchTaxpayerType() {
		return dataMatchTaxpayerType;
	}

	public void setDataMatchTaxpayerType(int dataMatchTaxpayerType) {
		this.dataMatchTaxpayerType = dataMatchTaxpayerType;
	}

	public int getDataMatchBusinessNamer() {
		return dataMatchBusinessNamer;
	}

	public void setDataMatchBusinessNamer(int dataMatchBusinessNamer) {
		this.dataMatchBusinessNamer = dataMatchBusinessNamer;
	}

	public int getDataMatchAddress() {
		return dataMatchAddress;
	}

	public void setDataMatchAddress(int dataMatchAddress) {
		this.dataMatchAddress = dataMatchAddress;
	}

	public int getDataMatchCostitutionOfBusiness() {
		return dataMatchCostitutionOfBusiness;
	}

	public void setDataMatchCostitutionOfBusiness(int dataMatchCostitutionOfBusiness) {
		this.dataMatchCostitutionOfBusiness = dataMatchCostitutionOfBusiness;
	}

	public int getDataMatchDateOfRegistration() {
		return dataMatchDateOfRegistration;
	}

	public void setDataMatchDateOfRegistration(int dataMatchDateOfRegistration) {
		this.dataMatchDateOfRegistration = dataMatchDateOfRegistration;
	}

	public int getDataMatchGstIn() {
		return dataMatchGstIn;
	}

	public void setDataMatchGstIn(int dataMatchGstIn) {
		this.dataMatchGstIn = dataMatchGstIn;
	}

	public int getDataMatchTradeName() {
		return dataMatchTradeName;
	}

	public void setDataMatchTradeName(int dataMatchTradeName) {
		this.dataMatchTradeName = dataMatchTradeName;
	}

	public boolean isValidGst() {
		return validGst;
	}

	public void setValidGst(boolean validGst) {
		this.validGst = validGst;
	}

	public List getBulkVerify() {
		return bulkVerify;
	}

	public void setBulkVerify(List bulkVerify) {
		this.bulkVerify = bulkVerify;
	}

	public String getRefid() {
		return refid;
	}

	public void setRefid(String refid) {
		this.refid = refid;
	}

	public String getDocExpiryAt() {
		return docExpiryAt;
	}

	public void setDocExpiryAt(String docExpiryAt) {
		this.docExpiryAt = docExpiryAt;
	}

	public int getSignerCount() {
		return signerCount;
	}

	public void setSignerCount(int signerCount) {
		this.signerCount = signerCount;
	}

	public String getRequestTag() {
		return requestTag;
	}

	public void setRequestTag(String requestTag) {
		this.requestTag = requestTag;
	}

	public String getTicketSize() {
		return ticketSize;
	}

	public void setTicketSize(String ticketSize) {
		this.ticketSize = ticketSize;
	}

	public boolean isCrimeWatch() {
		return crimeWatch;
	}

	public void setCrimeWatch(boolean crimeWatch) {
		this.crimeWatch = crimeWatch;
	}

	public boolean isReportMode() {
		return reportMode;
	}

	public void setReportMode(boolean reportMode) {
		this.reportMode = reportMode;
	}

	public boolean isBondPayer() {
		return bondPayer;
	}

	public void setBondPayer(boolean bondPayer) {
		this.bondPayer = bondPayer;
	}

	public int getUserManagementId() {
		return userManagementId;
	}

	public void setUserManagementId(int userManagementId) {
		this.userManagementId = userManagementId;
	}

	public boolean isNoSourceCheck() {
		return noSourceCheck;
	}

	public void setNoSourceCheck(boolean noSourceCheck) {
		this.noSourceCheck = noSourceCheck;
	}

	public boolean isComplaintActive() {
		return complaintActive;
	}

	public void setComplaintActive(boolean complaintActive) {
		this.complaintActive = complaintActive;
	}

	public String getReference() {
		return reference;
	}

	

	public boolean isVerificationRequired() {
		return verificationRequired;
	}

	public void setVerificationRequired(boolean verificationRequired) {
		this.verificationRequired = verificationRequired;
	}

	public boolean isSigningRequired() {
		return signingRequired;
	}

	public void setSigningRequired(boolean signingRequired) {
		this.signingRequired = signingRequired;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
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

	public List<RequestModel> getMerchantPriceList() {
		return merchantPriceList;
	}

	public void setMerchantPriceList(List<RequestModel> merchantPriceList) {
		this.merchantPriceList = merchantPriceList;
	}

	public String getInputVoterId() {
		return inputVoterId;
	}

	public void setInputVoterId(String inputVoterId) {
		this.inputVoterId = inputVoterId;
	}

	public String getStCode() {
		return stCode;
	}

	public void setStCode(String stCode) {
		this.stCode = stCode;
	}

	public String getAssemblyContituencyNumber() {
		return assemblyContituencyNumber;
	}

	public void setAssemblyContituencyNumber(String assemblyContituencyNumber) {
		this.assemblyContituencyNumber = assemblyContituencyNumber;
	}

	public String getRelationNameV2() {
		return relationNameV2;
	}

	public void setRelationNameV2(String relationNameV2) {
		this.relationNameV2 = relationNameV2;
	}

	public String getRelationNameV1() {
		return relationNameV1;
	}

	public void setRelationNameV1(String relationNameV1) {
		this.relationNameV1 = relationNameV1;
	}

	public String getRelationNameV3() {
		return relationNameV3;
	}

	public void setRelationNameV3(String relationNameV3) {
		this.relationNameV3 = relationNameV3;
	}

	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

	public String getNamev1() {
		return namev1;
	}

	public void setNamev1(String namev1) {
		this.namev1 = namev1;
	}

	public String getNamev2() {
		return namev2;
	}

	public void setNamev2(String namev2) {
		this.namev2 = namev2;
	}

	public String getNamev3() {
		return namev3;
	}

	public void setNamev3(String namev3) {
		this.namev3 = namev3;
	}

	public String getEpicNo() {
		return epicNo;
	}

	public void setEpicNo(String epicNo) {
		this.epicNo = epicNo;
	}

	public String getPsLatLong() {
		return psLatLong;
	}

	public void setPsLatLong(String psLatLong) {
		this.psLatLong = psLatLong;
	}

	public String getAssemblyConstituency() {
		return assemblyConstituency;
	}

	public void setAssemblyConstituency(String assemblyConstituency) {
		this.assemblyConstituency = assemblyConstituency;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getParliamentaryName() {
		return parliamentaryName;
	}

	public void setParliamentaryName(String parliamentaryName) {
		this.parliamentaryName = parliamentaryName;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public String getParliamentaryConstituency() {
		return parliamentaryConstituency;
	}

	public void setParliamentaryConstituency(String parliamentaryConstituency) {
		this.parliamentaryConstituency = parliamentaryConstituency;
	}

	public String getParliamentaryNumber() {
		return parliamentaryNumber;
	}

	public void setParliamentaryNumber(String parliamentaryNumber) {
		this.parliamentaryNumber = parliamentaryNumber;
	}

	public String getHouseNo() {
		return houseNo;
	}

	public void setHouseNo(String houseNo) {
		this.houseNo = houseNo;
	}

	public String getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}

	public String getPollingStation() {
		return pollingStation;
	}

	public void setPollingStation(String pollingStation) {
		this.pollingStation = pollingStation;
	}

	public String getSectionNo() {
		return sectionNo;
	}

	public void setSectionNo(String sectionNo) {
		this.sectionNo = sectionNo;
	}

	public String getSlnoInpart() {
		return slnoInpart;
	}

	public void setSlnoInpart(String slnoInpart) {
		this.slnoInpart = slnoInpart;
	}

	public String getRelationName() {
		return relationName;
	}

	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}

	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		this.partName = partName;
	}

	public JSONObject getOcrData() {
		return ocrData;
	}

	public void setOcrData(JSONObject ocrData) {
		this.ocrData = ocrData;
	}

	public int getAlreadyExistingUserId() {
		return alreadyExistingUserId;
	}

	public void setAlreadyExistingUserId(int alreadyExistingUserId) {
		this.alreadyExistingUserId = alreadyExistingUserId;
	}

	public int getNewComerUserId() {
		return newComerUserId;
	}

	public void setNewComerUserId(int newComerUserId) {
		this.newComerUserId = newComerUserId;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public boolean isAccountExists() {
		return accountExists;
	}

	public void setAccountExists(boolean accountExists) {
		this.accountExists = accountExists;
	}

	public String getEmailStatus() {
		return emailStatus;
	}

	public void setEmailStatus(String emailStatus) {
		this.emailStatus = emailStatus;
	}

	public boolean isAcceptsMail() {
		return acceptsMail;
	}

	public void setAcceptsMail(boolean acceptsMail) {
		this.acceptsMail = acceptsMail;
	}

	public boolean isCatchAll() {
		return isCatchAll;
	}

	public void setCatchAll(boolean isCatchAll) {
		this.isCatchAll = isCatchAll;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public boolean isValidSyntax() {
		return validSyntax;
	}

	public void setValidSyntax(boolean validSyntax) {
		this.validSyntax = validSyntax;
	}

	public boolean isSmtpConnected() {
		return smtpConnected;
	}

	public void setSmtpConnected(boolean smtpConnected) {
		this.smtpConnected = smtpConnected;
	}

	public boolean isTemporary() {
		return isTemporary;
	}

	public void setTemporary(boolean isTemporary) {
		this.isTemporary = isTemporary;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isResults() {
		return results;
	}

	public void setResults(boolean results) {
		this.results = results;
	}

	public JSONArray getMxRecords() {
		return mxRecords;
	}

	public void setMxRecords(JSONArray mxRecords) {
		this.mxRecords = mxRecords;
	}

	public String getPanAllotmentDate() {
		return panAllotmentDate;
	}

	public void setPanAllotmentDate(String panAllotmentDate) {
		this.panAllotmentDate = panAllotmentDate;
	}

	public String getMaskedName() {
		return maskedName;
	}

	public void setMaskedName(String maskedName) {
		this.maskedName = maskedName;
	}

	public String getPanAadhaarLinked() {
		return panAadhaarLinked;
	}

	public void setPanAadhaarLinked(String panAadhaarLinked) {
		this.panAadhaarLinked = panAadhaarLinked;
	}

	public String getSpecifiedPersonUnder206() {
		return specifiedPersonUnder206;
	}

	public void setSpecifiedPersonUnder206(String specifiedPersonUnder206) {
		this.specifiedPersonUnder206 = specifiedPersonUnder206;
	}

	public String getPanStatus() {
		return panStatus;
	}

	public void setPanStatus(String panStatus) {
		this.panStatus = panStatus;
	}

	public boolean isValidPan() {
		return validPan;
	}

	public void setValidPan(boolean validPan) {
		this.validPan = validPan;
	}

	public boolean isComplaint() {
		return complaint;
	}

	public void setComplaint(boolean complaint) {
		this.complaint = complaint;
	}

	public String getApplicationNumber() {
		return applicationNumber;
	}

	public void setApplicationNumber(String applicationNumber) {
		this.applicationNumber = applicationNumber;
	}

	public String getAddressPresis() {
		return addressPresis;
	}

	public void setAddressPresis(String addressPresis) {
		this.addressPresis = addressPresis;
	}

	public String getStatusDesc() {
		return statusDesc;
	}

	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}

	public String getDistrctName() {
		return distrctName;
	}

	public void setDistrctName(String distrctName) {
		this.distrctName = distrctName;
	}

	public String getDisplayRefId() {
		return displayRefId;
	}

	public void setDisplayRefId(String displayRefId) {
		this.displayRefId = displayRefId;
	}

	public String getTalukName() {
		return talukName;
	}

	public void setTalukName(String talukName) {
		this.talukName = talukName;
	}

	public String getAppTypeDesc() {
		return appTypeDesc;
	}

	public void setAppTypeDesc(String appTypeDesc) {
		this.appTypeDesc = appTypeDesc;
	}

	public String getLicenseCategoryName() {
		return licenseCategoryName;
	}

	public void setLicenseCategoryName(String licenseCategoryName) {
		this.licenseCategoryName = licenseCategoryName;
	}

	public String getAppSubmitionDate() {
		return appSubmitionDate;
	}

	public void setAppSubmitionDate(String appSubmitionDate) {
		this.appSubmitionDate = appSubmitionDate;
	}

	public String getLastUpdatedOn() {
		return lastUpdatedOn;
	}

	public void setLastUpdatedOn(String lastUpdatedOn) {
		this.lastUpdatedOn = lastUpdatedOn;
	}

	public int getFboId() {
		return fboId;
	}

	public void setFboId(int fboId) {
		this.fboId = fboId;
	}

	public int getReferId() {
		return referId;
	}

	public void setReferId(int referId) {
		this.referId = referId;
	}

	public String getIfsc() {
		return ifsc;
	}

	public void setIfsc(String ifsc) {
		this.ifsc = ifsc;
	}

	public String getMicr() {
		return micr;
	}

	public void setMicr(String micr) {
		this.micr = micr;
	}

	public String getIso3166() {
		return iso3166;
	}

	public void setIso3166(String iso3166) {
		this.iso3166 = iso3166;
	}

	public String getSwift() {
		return swift;
	}

	public void setSwift(String swift) {
		this.swift = swift;
	}

	public String getBank() {
		return bank;
	}

	public void setBank(String bank) {
		this.bank = bank;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getCentre() {
		return centre;
	}

	public void setCentre(String centre) {
		this.centre = centre;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public boolean isImps() {
		return imps;
	}

	public void setImps(boolean imps) {
		this.imps = imps;
	}

	public boolean isRtgs() {
		return rtgs;
	}

	public void setRtgs(boolean rtgs) {
		this.rtgs = rtgs;
	}

	public boolean isNeft() {
		return neft;
	}

	public void setNeft(boolean neft) {
		this.neft = neft;
	}

	public boolean isMicrCheck() {
		return micrCheck;
	}

	public void setMicrCheck(boolean micrCheck) {
		this.micrCheck = micrCheck;
	}

	public boolean isUpiPresent() {
		return upiPresent;
	}

	public void setUpiPresent(boolean upiPresent) {
		this.upiPresent = upiPresent;
	}


	public String getPfUan() {
		return pfUan;
	}

	public void setPfUan(String pfUan) {
		this.pfUan = pfUan;
	}

	public JSONArray getEmploymentHistory() {
		return employmentHistory;
	}

	public void setEmploymentHistory(JSONArray employmentHistory) {
		this.employmentHistory = employmentHistory;
	}

	public String getMembershipNumber() {
		return membershipNumber;
	}

	public void setMembershipNumber(String membershipNumber) {
		this.membershipNumber = membershipNumber;
	}

	public JSONObject getDetails() {
		return details;
	}

	public void setDetails(JSONObject details) {
		this.details = details;
	}

	public String getFirmName() {
		return firmName;
	}

	public void setFirmName(String firmName) {
		this.firmName = firmName;
	}

	public String getIecIssuanceDate() {
		return iecIssuanceDate;
	}

	public void setIecIssuanceDate(String iecIssuanceDate) {
		this.iecIssuanceDate = iecIssuanceDate;
	}

	public String getIecStatus() {
		return iecStatus;
	}

	public void setIecStatus(String iecStatus) {
		this.iecStatus = iecStatus;
	}

	public String getDelStatus() {
		return delStatus;
	}

	public void setDelStatus(String delStatus) {
		this.delStatus = delStatus;
	}

	public String getIecCancelledDate() {
		return iecCancelledDate;
	}

	public void setIecCancelledDate(String iecCancelledDate) {
		this.iecCancelledDate = iecCancelledDate;
	}

	public String getIecSuspendedDate() {
		return iecSuspendedDate;
	}

	public void setIecSuspendedDate(String iecSuspendedDate) {
		this.iecSuspendedDate = iecSuspendedDate;
	}

	public String getFileDate() {
		return fileDate;
	}

	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}

	public String getDgftraOffice() {
		return dgftraOffice;
	}

	public void setDgftraOffice(String dgftraOffice) {
		this.dgftraOffice = dgftraOffice;
	}

	public String getNatureOfConcern() {
		return natureOfConcern;
	}

	public void setNatureOfConcern(String natureOfConcern) {
		this.natureOfConcern = natureOfConcern;
	}

	public String getCategoryOfExporters() {
		return categoryOfExporters;
	}

	public void setCategoryOfExporters(String categoryOfExporters) {
		this.categoryOfExporters = categoryOfExporters;
	}

	public String getFirmMobileNo() {
		return firmMobileNo;
	}

	public void setFirmMobileNo(String firmMobileNo) {
		this.firmMobileNo = firmMobileNo;
	}

	public String getFirmEmailId() {
		return firmEmailId;
	}

	public void setFirmEmailId(String firmEmailId) {
		this.firmEmailId = firmEmailId;
	}

	public JSONArray getBranchDetails() {
		return branchDetails;
	}

	public void setBranchDetails(JSONArray branchDetails) {
		this.branchDetails = branchDetails;
	}

	public JSONArray getRemcDetails() {
		return remcDetails;
	}

	public void setRemcDetails(JSONArray remcDetails) {
		this.remcDetails = remcDetails;
	}

	public JSONArray getDirectorDetails() {
		return directorDetails;
	}

	public void setDirectorDetails(JSONArray directorDetails) {
		this.directorDetails = directorDetails;
	}

	public String getPayer() {
		return payer;
	}

	public void setPayer(String payer) {
		this.payer = payer;
	}

	public String getAgeRange() {
		return ageRange;
	}

	public void setAgeRange(String ageRange) {
		this.ageRange = ageRange;
	}

	public String getLastDigits() {
		return lastDigits;
	}

	public void setLastDigits(String lastDigits) {
		this.lastDigits = lastDigits;
	}

	public String getInputDob() {
		return inputDob;
	}

	public void setInputDob(String inputDob) {
		this.inputDob = inputDob;
	}

	public boolean isDobVerified() {
		return dobVerified;
	}

	public void setDobVerified(boolean dobVerified) {
		this.dobVerified = dobVerified;
	}

	public boolean isDobCheck() {
		return dobCheck;
	}

	public void setDobCheck(boolean dobCheck) {
		this.dobCheck = dobCheck;
	}

	public boolean isAadhaarLinked() {
		return aadhaarLinked;
	}

	public void setAadhaarLinked(boolean aadhaarLinked) {
		this.aadhaarLinked = aadhaarLinked;
	}

	public JSONArray getSplitName() {
		return splitName;
	}

	public void setSplitName(JSONArray splitName) {
		this.splitName = splitName;
	}

	public String getMaskedAadhaar() {
		return maskedAadhaar;
	}

	public void setMaskedAadhaar(String maskedAadhaar) {
		this.maskedAadhaar = maskedAadhaar;
	}

	public JSONObject getAddressInJson() {
		return addressInJson;
	}

	public void setAddressInJson(JSONObject addressInJson) {
		this.addressInJson = addressInJson;
	}

	public JSONArray getFilingStatusJsonList() {
		return filingStatusJsonList;
	}

	public void setFilingStatusJsonList(JSONArray filingStatusJsonList) {
		this.filingStatusJsonList = filingStatusJsonList;
	}

	public boolean isMobilePresent() {
		return mobilePresent;
	}

	public void setMobilePresent(boolean mobilePresent) {
		this.mobilePresent = mobilePresent;
	}

	public int getErrorIdentifierId() {
		return errorIdentifierId;
	}

	public void setErrorIdentifierId(int errorIdentifierId) {
		this.errorIdentifierId = errorIdentifierId;
	}

	public String getErrorReferenceNumber() {
		return errorReferenceNumber;
	}

	public void setErrorReferenceNumber(String errorReferenceNumber) {
		this.errorReferenceNumber = errorReferenceNumber;
	}

	public Date getOccuredDate() {
		return occuredDate;
	}

	public void setOccuredDate(Date occuredDate) {
		this.occuredDate = occuredDate;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public int getStatusCodeNumber() {
		return statusCodeNumber;
	}

	public void setStatusCodeNumber(int statusCodeNumber) {
		this.statusCodeNumber = statusCodeNumber;
	}

	public String getRegisteredDate() {
		return registeredDate;
	}

	public void setRegisteredDate(String registeredDate) {
		this.registeredDate = registeredDate;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getPresentAddress() {
		return presentAddress;
	}

	public void setPresentAddress(String presentAddress) {
		this.presentAddress = presentAddress;
	}

	public String getPermanentAddress() {
		return permanentAddress;
	}

	public void setPermanentAddress(String permanentAddress) {
		this.permanentAddress = permanentAddress;
	}

	public String getVehicleCategory() {
		return vehicleCategory;
	}

	public void setVehicleCategory(String vehicleCategory) {
		this.vehicleCategory = vehicleCategory;
	}

	public String getVehicleChasisNumber() {
		return vehicleChasisNumber;
	}

	public void setVehicleChasisNumber(String vehicleChasisNumber) {
		this.vehicleChasisNumber = vehicleChasisNumber;
	}

	public String getVehicleEngineNumber() {
		return vehicleEngineNumber;
	}

	public void setVehicleEngineNumber(String vehicleEngineNumber) {
		this.vehicleEngineNumber = vehicleEngineNumber;
	}

	public String getMakerDescription() {
		return makerDescription;
	}

	public void setMakerDescription(String makerDescription) {
		this.makerDescription = makerDescription;
	}

	public String getMakerModel() {
		return makerModel;
	}

	public void setMakerModel(String makerModel) {
		this.makerModel = makerModel;
	}

	public String getBodyType() {
		return bodyType;
	}

	public void setBodyType(String bodyType) {
		this.bodyType = bodyType;
	}

	public String getFuelType() {
		return fuelType;
	}

	public void setFuelType(String fuelType) {
		this.fuelType = fuelType;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getNormsType() {
		return normsType;
	}

	public void setNormsType(String normsType) {
		this.normsType = normsType;
	}

	public String getFitUpTo() {
		return fitUpTo;
	}

	public void setFitUpTo(String fitUpTo) {
		this.fitUpTo = fitUpTo;
	}

	public String getFinancer() {
		return financer;
	}

	public void setFinancer(String financer) {
		this.financer = financer;
	}

	public String getInsuranceCompany() {
		return insuranceCompany;
	}

	public void setInsuranceCompany(String insuranceCompany) {
		this.insuranceCompany = insuranceCompany;
	}

	public String getInsurancePolicyNumber() {
		return insurancePolicyNumber;
	}

	public void setInsurancePolicyNumber(String insurancePolicyNumber) {
		this.insurancePolicyNumber = insurancePolicyNumber;
	}

	public String getInsuranceUpto() {
		return insuranceUpto;
	}

	public void setInsuranceUpto(String insuranceUpto) {
		this.insuranceUpto = insuranceUpto;
	}

	public String getManufacturingDate() {
		return manufacturingDate;
	}

	public void setManufacturingDate(String manufacturingDate) {
		this.manufacturingDate = manufacturingDate;
	}

	public String getManufacturingDateForma() {
		return manufacturingDateForma;
	}

	public void setManufacturingDateForma(String manufacturingDateForma) {
		this.manufacturingDateForma = manufacturingDateForma;
	}

	public String getRegisteredAt() {
		return registeredAt;
	}

	public void setRegisteredAt(String registeredAt) {
		this.registeredAt = registeredAt;
	}

	public String getLatestBy() {
		return latestBy;
	}

	public void setLatestBy(String latestBy) {
		this.latestBy = latestBy;
	}

	public String getTaxUpto() {
		return taxUpto;
	}

	public void setTaxUpto(String taxUpto) {
		this.taxUpto = taxUpto;
	}

	public String getTaxPaidUpto() {
		return taxPaidUpto;
	}

	public void setTaxPaidUpto(String taxPaidUpto) {
		this.taxPaidUpto = taxPaidUpto;
	}

	public String getCubicCapacity() {
		return cubicCapacity;
	}

	public void setCubicCapacity(String cubicCapacity) {
		this.cubicCapacity = cubicCapacity;
	}

	public String getVehicleGrossWeight() {
		return vehicleGrossWeight;
	}

	public void setVehicleGrossWeight(String vehicleGrossWeight) {
		this.vehicleGrossWeight = vehicleGrossWeight;
	}

	public String getNoOfCylinders() {
		return noOfCylinders;
	}

	public void setNoOfCylinders(String noOfCylinders) {
		this.noOfCylinders = noOfCylinders;
	}

	public String getSeatCapacity() {
		return seatCapacity;
	}

	public void setSeatCapacity(String seatCapacity) {
		this.seatCapacity = seatCapacity;
	}

	public String getSleeperCapacity() {
		return sleeperCapacity;
	}

	public void setSleeperCapacity(String sleeperCapacity) {
		this.sleeperCapacity = sleeperCapacity;
	}

	public String getStandingCapacity() {
		return standingCapacity;
	}

	public void setStandingCapacity(String standingCapacity) {
		this.standingCapacity = standingCapacity;
	}

	public String getWheelBase() {
		return wheelBase;
	}

	public void setWheelBase(String wheelBase) {
		this.wheelBase = wheelBase;
	}

	public String getUnladenWeight() {
		return unladenWeight;
	}

	public void setUnladenWeight(String unladenWeight) {
		this.unladenWeight = unladenWeight;
	}

	public String getVehicleCategoryDescription() {
		return vehicleCategoryDescription;
	}

	public void setVehicleCategoryDescription(String vehicleCategoryDescription) {
		this.vehicleCategoryDescription = vehicleCategoryDescription;
	}

	public String getPuccNumber() {
		return puccNumber;
	}

	public void setPuccNumber(String puccNumber) {
		this.puccNumber = puccNumber;
	}

	public String getPuccUpto() {
		return puccUpto;
	}

	public void setPuccUpto(String puccUpto) {
		this.puccUpto = puccUpto;
	}

	public String getPermitNumber() {
		return permitNumber;
	}

	public void setPermitNumber(String permitNumber) {
		this.permitNumber = permitNumber;
	}

	public String getPermitIssueDate() {
		return permitIssueDate;
	}

	public void setPermitIssueDate(String permitIssueDate) {
		this.permitIssueDate = permitIssueDate;
	}

	public String getPermitValidFrom() {
		return permitValidFrom;
	}

	public void setPermitValidFrom(String permitValidFrom) {
		this.permitValidFrom = permitValidFrom;
	}

	public String getPermitValidUpto() {
		return permitValidUpto;
	}

	public void setPermitValidUpto(String permitValidUpto) {
		this.permitValidUpto = permitValidUpto;
	}

	public String getPermitType() {
		return permitType;
	}

	public void setPermitType(String permitType) {
		this.permitType = permitType;
	}

	public String getNationalPermitNumber() {
		return nationalPermitNumber;
	}

	public void setNationalPermitNumber(String nationalPermitNumber) {
		this.nationalPermitNumber = nationalPermitNumber;
	}

	public String getNationalPermitUpto() {
		return nationalPermitUpto;
	}

	public void setNationalPermitUpto(String nationalPermitUpto) {
		this.nationalPermitUpto = nationalPermitUpto;
	}

	public String getNationalPermitIssuedBy() {
		return nationalPermitIssuedBy;
	}

	public void setNationalPermitIssuedBy(String nationalPermitIssuedBy) {
		this.nationalPermitIssuedBy = nationalPermitIssuedBy;
	}

	public String getNonUseStatus() {
		return nonUseStatus;
	}

	public void setNonUseStatus(String nonUseStatus) {
		this.nonUseStatus = nonUseStatus;
	}

	public String getNonUseFrom() {
		return nonUseFrom;
	}

	public void setNonUseFrom(String nonUseFrom) {
		this.nonUseFrom = nonUseFrom;
	}

	public String getNonUseTo() {
		return nonUseTo;
	}

	public void setNonUseTo(String nonUseTo) {
		this.nonUseTo = nonUseTo;
	}

	public String getBlackListStatus() {
		return blackListStatus;
	}

	public void setBlackListStatus(String blackListStatus) {
		this.blackListStatus = blackListStatus;
	}

	public String getNocDetails() {
		return nocDetails;
	}

	public void setNocDetails(String nocDetails) {
		this.nocDetails = nocDetails;
	}

	public String getOwnerNumber() {
		return ownerNumber;
	}

	public void setOwnerNumber(String ownerNumber) {
		this.ownerNumber = ownerNumber;
	}

	public String getRcStatus() {
		return rcStatus;
	}

	public void setRcStatus(String rcStatus) {
		this.rcStatus = rcStatus;
	}

	public String getVariant() {
		return variant;
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}

	public String getChallanDetails() {
		return challanDetails;
	}

	public void setChallanDetails(String challanDetails) {
		this.challanDetails = challanDetails;
	}

	public boolean isFinanced() {
		return financed;
	}

	public void setFinanced(boolean financed) {
		this.financed = financed;
	}

	public boolean isMaskedNamePresent() {
		return maskedNamePresent;
	}

	public void setMaskedNamePresent(boolean maskedNamePresent) {
		this.maskedNamePresent = maskedNamePresent;
	}

	public JSONObject getCompanyInfo() {
		return companyInfo;
	}

	public void setCompanyInfo(JSONObject companyInfo) {
		this.companyInfo = companyInfo;
	}

	public String getCin() {
		return cin;
	}

	public void setCin(String cin) {
		this.cin = cin;
	}

	public String getRocCode() {
		return rocCode;
	}

	public void setRocCode(String rocCode) {
		this.rocCode = rocCode;
	}

	public String getCompanyCategory() {
		return companyCategory;
	}

	public void setCompanyCategory(String companyCategory) {
		this.companyCategory = companyCategory;
	}

	public String getClassOfCompany() {
		return classOfCompany;
	}

	public void setClassOfCompany(String classOfCompany) {
		this.classOfCompany = classOfCompany;
	}

	public String getCompanySubCategory() {
		return companySubCategory;
	}

	public void setCompanySubCategory(String companySubCategory) {
		this.companySubCategory = companySubCategory;
	}

	public String getAuthorizedCapital() {
		return authorizedCapital;
	}

	public void setAuthorizedCapital(String authorizedCapital) {
		this.authorizedCapital = authorizedCapital;
	}

	public String getPaidUpCapital() {
		return paidUpCapital;
	}

	public void setPaidUpCapital(String paidUpCapital) {
		this.paidUpCapital = paidUpCapital;
	}

	public String getNumberOfMembers() {
		return numberOfMembers;
	}

	public void setNumberOfMembers(String numberOfMembers) {
		this.numberOfMembers = numberOfMembers;
	}

	public String getDateOfIncorporation() {
		return dateOfIncorporation;
	}

	public void setDateOfIncorporation(String dateOfIncorporation) {
		this.dateOfIncorporation = dateOfIncorporation;
	}

	public String getRegisteredAddress() {
		return registeredAddress;
	}

	public void setRegisteredAddress(String registeredAddress) {
		this.registeredAddress = registeredAddress;
	}

	public String getAddressOtherThanRo() {
		return addressOtherThanRo;
	}

	public void setAddressOtherThanRo(String addressOtherThanRo) {
		this.addressOtherThanRo = addressOtherThanRo;
	}

	public String getListedStatus() {
		return listedStatus;
	}

	public void setListedStatus(String listedStatus) {
		this.listedStatus = listedStatus;
	}

	public String getActiveCompilance() {
		return activeCompilance;
	}

	public void setActiveCompilance(String activeCompilance) {
		this.activeCompilance = activeCompilance;
	}

	public String getSuspendedAtStockExchange() {
		return suspendedAtStockExchange;
	}

	public void setSuspendedAtStockExchange(String suspendedAtStockExchange) {
		this.suspendedAtStockExchange = suspendedAtStockExchange;
	}

	public String getLastAgmDate() {
		return lastAgmDate;
	}

	public void setLastAgmDate(String lastAgmDate) {
		this.lastAgmDate = lastAgmDate;
	}

	public String getLastBsDate() {
		return lastBsDate;
	}

	public void setLastBsDate(String lastBsDate) {
		this.lastBsDate = lastBsDate;
	}

	public String getCompanyStatus() {
		return companyStatus;
	}

	public void setCompanyStatus(String companyStatus) {
		this.companyStatus = companyStatus;
	}

	public String getStatusUnderCirp() {
		return statusUnderCirp;
	}

	public void setStatusUnderCirp(String statusUnderCirp) {
		this.statusUnderCirp = statusUnderCirp;
	}

	public JSONArray getDirectors() {
		return directors;
	}

	public void setDirectors(JSONArray directors) {
		this.directors = directors;
	}

	public JSONArray getCharges() {
		return charges;
	}

	public void setCharges(JSONArray charges) {
		this.charges = charges;
	}

	public String getDinNumber() {
		return dinNumber;
	}

	public void setDinNumber(String dinNumber) {
		this.dinNumber = dinNumber;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public String getDinStatus() {
		return dinStatus;
	}

	public void setDinStatus(String dinStatus) {
		this.dinStatus = dinStatus;
	}

	public JSONArray getCompaniesAssociated() {
		return companiesAssociated;
	}

	public void setCompaniesAssociated(JSONArray companiesAssociated) {
		this.companiesAssociated = companiesAssociated;
	}

	public String getComplaintResponse() {
		return complaintResponse;
	}

	public void setComplaintResponse(String complaintResponse) {
		this.complaintResponse = complaintResponse;
	}

	public JSONArray getEnterprizeTypeList() {
		return enterprizeTypeList;
	}

	public void setEnterprizeTypeList(JSONArray enterprizeTypeList) {
		this.enterprizeTypeList = enterprizeTypeList;
	}

	public String getStatePremesis() {
		return statePremesis;
	}

	public void setStatePremesis(String statePremesis) {
		this.statePremesis = statePremesis;
	}

	public int getDistrictPremesis() {
		return districtPremesis;
	}

	public void setDistrictPremesis(int districtPremesis) {
		this.districtPremesis = districtPremesis;
	}

	public int getTalikPremesis() {
		return talikPremesis;
	}

	public void setTalikPremesis(int talikPremesis) {
		this.talikPremesis = talikPremesis;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public int getNoRestriction() {
		return noRestriction;
	}

	public void setNoRestriction(int noRestriction) {
		this.noRestriction = noRestriction;
	}

	public boolean isMailPresent() {
		return mailPresent;
	}

	public void setMailPresent(boolean mailPresent) {
		this.mailPresent = mailPresent;
	}

	public boolean isSmsPresent() {
		return smsPresent;
	}

	public void setSmsPresent(boolean smsPresent) {
		this.smsPresent = smsPresent;
	}

	public String getMccCode() {
		return mccCode;
	}

	public void setMccCode(String mccCode) {
		this.mccCode = mccCode;
	}

	public boolean isBulkUploadAccess() {
		return bulkUploadAccess;
	}

	public void setBulkUploadAccess(boolean bulkUploadAccess) {
		this.bulkUploadAccess = bulkUploadAccess;
	}

	public boolean isCreatePdfAccess() {
		return createPdfAccess;
	}

	public void setCreatePdfAccess(boolean createPdfAccess) {
		this.createPdfAccess = createPdfAccess;
	}

	public double getDocumentPrice() {
		return documentPrice;
	}

	public void setDocumentPrice(double documentPrice) {
		this.documentPrice = documentPrice;
	}

	public boolean isFreeHit() {
		return freeHit;
	}

	public void setFreeHit(boolean freeHit) {
		this.freeHit = freeHit;
	}

	public boolean isPostpaidDueAllow() {
		return postpaidDueAllow;
	}

	public void setPostpaidDueAllow(boolean postpaidDueAllow) {
		this.postpaidDueAllow = postpaidDueAllow;
	}

	public int getModeId() {
		return modeId;
	}

	public void setModeId(int modeId) {
		this.modeId = modeId;
	}

	public String getModeOfPayment() {
		return modeOfPayment;
	}

	public void setModeOfPayment(String modeOfPayment) {
		this.modeOfPayment = modeOfPayment;
	}

	public String getPaymentCode() {
		return paymentCode;
	}

	public void setPaymentCode(String paymentCode) {
		this.paymentCode = paymentCode;
	}

	public int getConvenienceId() {
		return convenienceId;
	}

	public void setConvenienceId(int convenienceId) {
		this.convenienceId = convenienceId;
	}

	public String getReferenceName() {
		return referenceName;
	}

	public void setReferenceName(String referenceName) {
		this.referenceName = referenceName;
	}

	public String getConvenienceType() {
		return convenienceType;
	}

	public void setConvenienceType(String convenienceType) {
		this.convenienceType = convenienceType;
	}

	public Double getConvenienceAmount() {
		return convenienceAmount;
	}

	public void setConvenienceAmount(Double convenienceAmount) {
		this.convenienceAmount = convenienceAmount;
	}

	public Double getConveniencePercentage() {
		return conveniencePercentage;
	}

	public void setConveniencePercentage(Double conveniencePercentage) {
		this.conveniencePercentage = conveniencePercentage;
	}

	public Double getStartAmount() {
		return startAmount;
	}

	public void setStartAmount(Double startAmount) {
		this.startAmount = startAmount;
	}

	public Double getEndAmount() {
		return endAmount;
	}

	public void setEndAmount(Double endAmount) {
		this.endAmount = endAmount;
	}

	public ModeOfPaymentPg getModeOfPaymentPg() {
		return modeOfPaymentPg;
	}

	public void setModeOfPaymentPg(ModeOfPaymentPg modeOfPaymentPg) {
		this.modeOfPaymentPg = modeOfPaymentPg;
	}

	public String getLinkExpiry() {
		return linkExpiry;
	}

	public void setLinkExpiry(String linkExpiry) {
		this.linkExpiry = linkExpiry;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public String getReferenceNo() {
		return referenceNo;
	}

	public void setReferenceNo(String referenceNo) {
		this.referenceNo = referenceNo;
	}

	public Double getFixedAmount() {
		return fixedAmount;
	}

	public void setFixedAmount(Double fixedAmount) {
		this.fixedAmount = fixedAmount;
	}

	public Double getThresholdAmount() {
		return thresholdAmount;
	}

	public void setThresholdAmount(Double thresholdAmount) {
		this.thresholdAmount = thresholdAmount;
	}

	public double getSgst() {
		return sgst;
	}

	public void setSgst(double sgst) {
		this.sgst = sgst;
	}

	public double getCgst() {
		return cgst;
	}

	public void setCgst(double cgst) {
		this.cgst = cgst;
	}

	public double getIgst() {
		return igst;
	}

	public void setIgst(double igst) {
		this.igst = igst;
	}

	public void setConvenienceAmount(double convenienceAmount) {
		this.convenienceAmount = convenienceAmount;
	}

	public void setConveniencePercentage(double conveniencePercentage) {
		this.conveniencePercentage = conveniencePercentage;
	}

	public void setStartAmount(double startAmount) {
		this.startAmount = startAmount;
	}

	public boolean isExpired() {
		return expired;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	public void setEndAmount(double endAmount) {
		this.endAmount = endAmount;
	}

	public void setFixedAmount(double fixedAmount) {
		this.fixedAmount = fixedAmount;
	}

	public void setThresholdAmount(double thresholdAmount) {
		this.thresholdAmount = thresholdAmount;
	}

	public double getExclusiveAmount() {
		return exclusiveAmount;
	}

	public void setExclusiveAmount(double exclusiveAmount) {
		this.exclusiveAmount = exclusiveAmount;
	}

	public boolean isNativeLocal() {
		return nativeLocal;
	}

	public void setNativeLocal(boolean nativeLocal) {
		this.nativeLocal = nativeLocal;
	}

	public boolean isShowLiveKeys() {
		return showLiveKeys;
	}

	public void setShowLiveKeys(boolean showLiveKeys) {
		this.showLiveKeys = showLiveKeys;
	}

	public boolean isConvenienceFetch() {
		return convenienceFetch;
	}

	public void setConvenienceFetch(boolean convenienceFetch) {
		this.convenienceFetch = convenienceFetch;
	}

	public int getRefundId() {
		return refundId;
	}

	public void setRefundId(int refundId) {
		this.refundId = refundId;
	}

	public String getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(String refundStatus) {
		this.refundStatus = refundStatus;
	}

	public double getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(double refundAmount) {
		this.refundAmount = refundAmount;
	}

	public LocalDate getRefundInitiatedAt() {
		return refundInitiatedAt;
	}

	public void setRefundInitiatedAt(LocalDate refundInitiatedAt) {
		this.refundInitiatedAt = refundInitiatedAt;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public String getInvoice() {
		return invoice;
	}

	public void setInvoice(String invoice) {
		this.invoice = invoice;
	}

	public LocalDate getGeneratedDate() {
		return generatedDate;
	}

	public void setGeneratedDate(LocalDate generatedDate) {
		this.generatedDate = generatedDate;
	}

	public int getTotalHits() {
		return totalHits;
	}

	public void setTotalHits(int totalHits) {
		this.totalHits = totalHits;
	}

	public double getWalletBalance() {
		return walletBalance;
	}

	public void setWalletBalance(double walletBalance) {
		this.walletBalance = walletBalance;
	}

	public double getUsedAmount() {
		return usedAmount;
	}

	public void setUsedAmount(double usedAmount) {
		this.usedAmount = usedAmount;
	}

	public double getGrandTotal() {
		return grandTotal;
	}

	public int getAdminMailId() {
		return adminMailId;
	}

	public void setAdminMailId(int adminMailId) {
		this.adminMailId = adminMailId;
	}

	public void setGrandTotal(double grandTotal) {
		this.grandTotal = grandTotal;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getGstNo() {
		return gstNo;
	}

	public void setGstNo(String gstNo) {
		this.gstNo = gstNo;
	}

	public String getMonthName() {
		return monthName;
	}

	public void setMonthName(String monthName) {
		this.monthName = monthName;
	}

	public boolean isDeleteInvoiceTwo() {
		return deleteInvoiceTwo;
	}

	public void setDeleteInvoiceTwo(boolean deleteInvoiceTwo) {
		this.deleteInvoiceTwo = deleteInvoiceTwo;
	}

	public boolean isDeleteInvoiceThree() {
		return deleteInvoiceThree;
	}

	public void setDeleteInvoiceThree(boolean deleteInvoiceThree) {
		this.deleteInvoiceThree = deleteInvoiceThree;
	}

	public boolean isAadhaarBasedSigning() {
		return aadhaarBasedSigning;
	}

	public void setAadhaarBasedSigning(boolean aadhaarBasedSigning) {
		this.aadhaarBasedSigning = aadhaarBasedSigning;
	}

	public String getAadhaarRequestId() {
		return aadhaarRequestId;
	}

	public void setAadhaarRequestId(String aadhaarRequestId) {
		this.aadhaarRequestId = aadhaarRequestId;
	}

	public LocalDate getAadhaarRequestTime() {
		return aadhaarRequestTime;
	}

	public void setAadhaarRequestTime(LocalDate aadhaarRequestTime) {
		this.aadhaarRequestTime = aadhaarRequestTime;
	}

	public MerchantPriceModel getMerchantPriceModel() {
		return merchantPriceModel;
	}

	public void setMerchantPriceModel(MerchantPriceModel merchantPriceModel) {
		this.merchantPriceModel = merchantPriceModel;
	}

	public PrepaidPayment getPrepaid() {
		return prepaid;
	}

	public void setPrepaid(PrepaidPayment prepaid) {
		this.prepaid = prepaid;
	}

	public Set<VendorVerificationModel> getVerificationModelSet() {
		return verificationModelSet;
	}

	public void setVerificationModelSet(Set<VendorVerificationModel> verificationModelSet) {
		this.verificationModelSet = verificationModelSet;
	}

	public String getUsedServices() {
		return usedServices;
	}

	public void setUsedServices(String usedServices) {
		this.usedServices = usedServices;
	}

	public int getRecentIdentifier() {
		return recentIdentifier;
	}

	public void setRecentIdentifier(int recentIdentifier) {
		this.recentIdentifier = recentIdentifier;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public boolean isConsent() {
		return consent;
	}

	public void setConsent(boolean consent) {
		this.consent = consent;
	}

	public String getModeOfPay() {
		return modeOfPay;
	}

	public void setModeOfPay(String modeOfPay) {
		this.modeOfPay = modeOfPay;
	}

	public boolean isGetLocation() {
		return getLocation;
	}

	public void setGetLocation(boolean getLocation) {
		this.getLocation = getLocation;
	}

	public int getPositionId() {
		return positionId;
	}

	public void setPositionId(int positionId) {
		this.positionId = positionId;
	}

	public boolean isCurrentlyActive() {
		return currentlyActive;
	}

	public void setCurrentlyActive(boolean currentlyActive) {
		this.currentlyActive = currentlyActive;
	}

	public int getManualPaymentId() {
		return manualPaymentId;
	}

	public void setManualPaymentId(int manualPaymentId) {
		this.manualPaymentId = manualPaymentId;
	}

	public String getTicketCurrentStatus() {
		return ticketCurrentStatus;
	}

	public void setTicketCurrentStatus(String ticketCurrentStatus) {
		this.ticketCurrentStatus = ticketCurrentStatus;
	}

	public int getBankAccId() {
		return bankAccId;
	}

	public void setBankAccId(int bankAccId) {
		this.bankAccId = bankAccId;
	}

	public int getHsnId() {
		return hsnId;
	}

	public String getPostpaidPaymentCycle() {
		return postpaidPaymentCycle;
	}

	public void setPostpaidPaymentCycle(String postpaidPaymentCycle) {
		this.postpaidPaymentCycle = postpaidPaymentCycle;
	}

	public void setHsnId(int hsnId) {
		this.hsnId = hsnId;
	}

	public String getHsnNumber() {
		return hsnNumber;
	}

	public void setHsnNumber(String hsnNumber) {
		this.hsnNumber = hsnNumber;
	}

	public int getPrepaidStatementId() {
		return prepaidStatementId;
	}

	public void setPrepaidStatementId(int prepaidStatementId) {
		this.prepaidStatementId = prepaidStatementId;
	}

	public double getDebit() {
		return debit;
	}

	public void setDebit(double debit) {
		this.debit = debit;
	}

	public double getCredit() {
		return credit;
	}

	public void setCredit(double credit) {
		this.credit = credit;
	}

	public double getClosingBalance() {
		return closingBalance;
	}

	public void setClosingBalance(double closingBalance) {
		this.closingBalance = closingBalance;
	}

	public double getConsumedBalance() {
		return consumedBalance;
	}

	public void setConsumedBalance(double consumedBalance) {
		this.consumedBalance = consumedBalance;
	}

	public Date getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}

	public double getDebitGst() {
		return debitGst;
	}

	public void setDebitGst(double debitGst) {
		this.debitGst = debitGst;
	}

	public double getCreditGst() {
		return creditGst;
	}

	public void setCreditGst(double creditGst) {
		this.creditGst = creditGst;
	}

	public boolean isGeneralSigning() {
		return generalSigning;
	}

	public void setGeneralSigning(boolean generalSigning) {
		this.generalSigning = generalSigning;
	}

	public double getAadhaarXmlPrice() {
		return aadhaarXmlPrice;
	}

	public void setAadhaarXmlPrice(double aadhaarXmlPrice) {
		this.aadhaarXmlPrice = aadhaarXmlPrice;
	}

	public double getAadhaarOtpPrice() {
		return aadhaarOtpPrice;
	}

	public void setAadhaarOtpPrice(double aadhaarOtpPrice) {
		this.aadhaarOtpPrice = aadhaarOtpPrice;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public Set<Integer> getMultipleUserId() {
		return multipleUserId;
	}

	public void setMultipleUserId(Set<Integer> multipleUserId) {
		this.multipleUserId = multipleUserId;
	}

	public MerchantModel getMerchantDoc() {
		return merchantDoc;
	}

	public void setMerchantDoc(MerchantModel merchantDoc) {
		this.merchantDoc = merchantDoc;
	}



}
