package com.bp.middleware.util;

import java.net.URI;
import java.util.Collection;

public final class AppConstants {


	private AppConstants() {

	}
	
	
	public static final int SGST = 9;
	public static final int CGST = 9;
	public static final double IGST = 0.18; // 18%
	
	public static final String PAYMENT_TYPE = "MANUAL";
	
	public static final String DATE_ONLY ="yyyy-MM-dd";  
	public static final String CURRENCY ="currency";
	public static final String RETURNURL = "returnUrl";
	public static final String ONLINE_PAYMENT="Online Payment";
	public static final String DESCRIPTION = "description";
	public static final String CUSTOM_FIELDS = "customFields";
	public static final String Payment_Link_Gen_Url ="https://staging-api.basispay.in/rest/paymentLink";
	
	
	public static final String PG_TRACK_URL = "https://staging-connect.basispay.in/api/payment/track";
	public static final String MERCHANT_ORDER_NO = "merchantOrderNo";
	public static final String PG_PROD_TRACK_URL = "https://connect.basispay.in/api/payment/track";
	public static final String AMOUNT = "amount";
	public static final String EXPIRY = "expiry";
	public static final String APPLICATION_JSON="application/json";
	public static final String REDIRECT = "redirect";
	public static final String CUSTOMERNAME = "customerName";
	public static final String CUSTOMERMOBILE = "customerMobile";
	public static final String WEB_INF="/WEB-INF/";

	public static final String PaymentID = "paymentId";
	public static final String RequestID = "requestId";
	public static final String Amount = "amount";
	public static final String REFUNDS = "refunds";
	public static final String REQUEST_TIME = "requestTime";
	
	// SUPPORT MAIL
	public static final String SUPPORT_MAIL = " techsupport@gmail.com ";

	// PAYMENT
	public static final String ORDERAMOUNT = "orderAmount";

	public static final String ERROR_MESSAGE_RESPONSE = "An issue occured at server-side.";
	public static final String ERROR_DESCRIPTION_RESPONSE = "There was a server-side issue. Please contact"
			+ AppConstants.SUPPORT_MAIL + "with the Error ID for further support.";

	public static final String HEADER_ISSUE_MESSAGE = "The header value provided is not accurate or appropriate.";
	public static final String HEADER_ISSUE_ERRORDESCRIPTION = "The API-key or Application-ID is deemed invalid.Please ensure the correct value is passed to proceed further.";

	
	public static final String ACCOUNT_INACTIVE_MESSAGE = "Your account is currently inactive";
	public static final String ACCOUNT_INACTIVE_ERRORDESC = "Your account is presently not active.Please contact support for further assistance.";
	
	public static final String NO_ACCESS_MESSAGE = "Access not granted.";
	public static final String NO_ACCESS_DESCRIPTION = "You do not have access to this API scope.Please contact"
			+ AppConstants.SUPPORT_MAIL + "to request access.";

	
	public static final String NOT_AVAILABLE = "Verification not available at the moment";
	public static final String NOT_AVAILABLE_DESCRIPTION = "The verification you are trying to access is not available at the moment.Please contact"
			+ AppConstants.SUPPORT_MAIL + "for further information.";
	
	public static final String REQUEST_TO_SERVER_FAILED = "The request to the server failed.Please contact"
			+ AppConstants.SUPPORT_MAIL + "if this issue continues.";
	public static final String CONNECTION_TO_SERVER_FAILED = "Connection to the server failed.";

	// VENDORS
	public static final String SIGN_DESK_VENDOR = "SIGN DESK";
	public static final String SPRINT_VERIFY_VENDOR = "SPRINT VERIFY";
	public static final String SUREPASS_VENDOR = "SUREPASS";
	public static final String BASISPAY_VENDOR = "BASISPAY";

//	public static final String SUREPASS_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJmcmVzaCI6ZmFsc2UsImlhdCI6MTcxMDkzMjQzNywianRpIjoiZmExYzUxNTktMzZjYy00OGIwLTgxMGYtODNlOWI0NWZjNTNmIiwidHlwZSI6ImFjY2VzcyIsImlkZW50aXR5IjoiZGV2LnNhcmF2YW5hbjEyQHN1cmVwYXNzLmlvIiwibmJmIjoxNzEwOTMyNDM3LCJleHAiOjE3MTE3OTY0MzcsInVzZXJfY2xhaW1zIjp7InNjb3BlcyI6WyJ1c2VyIl19fQ.uXsIPYkkDsDo_HepZ4r6T7Q0NcM7BoU2D3beEfR6JBM";

	public static final String SUREPASS_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJmcmVzaCI6ZmFsc2UsImlhdCI6MTcxNTY3Mjg0MywianRpIjoiNjJmYzUyZWItODE1My00OTg3LWFiNDQtMzE3Njk0MjBlOTlhIiwidHlwZSI6ImFjY2VzcyIsImlkZW50aXR5IjoiZGV2LnNhcmF2YW5hbjEyQHN1cmVwYXNzLmlvIiwibmJmIjoxNzE1NjcyODQzLCJleHAiOjIwMzEwMzI4NDMsImVtYWlsIjoic2FyYXZhbmFuMTJAc3VyZXBhc3MuaW8iLCJ0ZW5hbnRfaWQiOiJtYWluIiwidXNlcl9jbGFpbXMiOnsic2NvcGVzIjpbInVzZXIiXX19.dzhPBfq_tpt73tVMK6OwojJ_OUdI6sRcTsU44ocBcU8";
	
	// VERIFICATIONS
	
	public static final String DIGITAL_SIGNER = "DIGITAL SIGNER";
	
	public static final String PAN_VERIFY = "PAN";
	public static final String PAN_COMPREHENSIVE = "PAN COMPREHENSIVE";
	public static final String GST_VERIFY = "GST";
	public static final String PASSPORT_VERIFY = "PASSPORT";
	public static final String DL_VERIFY = "DRIVING LICENSE";
	public static final String AADHAR_VERIFY = "AADHAAR";
	public static final String AADHAR_XML_VERIFY = "AADHAAR XML";
	public static final String AADHAR_OTP_VERIFY = "AADHAAR OTP";
	public static final String MSME_UDYAMAADHAR_VERIFY = "MSME UDYAM AADHAR";
	public static final String RC_VERIFY = "RC";
	public static final String CIN_VERIFY = "CIN";
	public static final String DIN_VERIFY = "DIN";
	public static final String BANK1_VERIFY = "BAV 1";
	public static final String BANK2_VERIFY = "BAV 2";
	public static final String BAVPENNYLESS_VERIFY = "BAV PENNYLESS";
	public static final String BAVPENNYDROP_V1_VERIFY = "BAV PENNYDROP V1";
	public static final String BAVPENNYDROP_V2_VERIFY = "BAV PENNYDROP V2";
	public static final String VOTERID_VERIFY = "VOTER ID";
	public static final String EMAIL_VERIFY = "EMAIL";
	public static final String ITR_COMPILANCE = "ITR COMPILANCE";
	public static final String FSSAI = "FSSAI";
	public static final String UPI = "UPI";
	public static final String AADHAAR_TO_UAN = "AADHAAR TO UAN";
	public static final String PAN_TO_UAN = "PAN TO UAN";
	public static final String MOBILE_TO_UAN = "MOBILE TO UAN";
	public static final String EMPLOYMENT_DETAILS = "EMPLOYMENT DETAILS";
	public static final String DIRECTOR_MOBILE = "DIRECTOR MOBILE";
	public static final String ICAI = "ICAI";
	public static final String IEC = "IEC";
	public static final String IEC_ADVANCED = "IEC ADVANCED";
	public static final String ESIC = "ESIC";
	public static final String ESIC_ADVANCED = "ESIC ADVANCED";

	public static final String PAN_OCR_ADVANCED = "PAN OCR ADVANCED";
	public static final String PAN_IMAGE = "PAN OCR";
	public static final String GST_IMAGE = "GST OCR";
	public static final String DL_IMAGE = "DL OCR";
	public static final String PASSPORT_IMAGE = "PASSPORT OCR";
	
	public static final String AADHAAR_OCR = "Aadhaar OCR";
	public static final String CHEQUE_OCR = "Cheque OCR";
	public static final String DOCUMENTDETECT_OCR = "Document Detect OCR";
	public static final String INTL_PASSPORT = "INTERNATIONAL PASSPORT OCR";
	public static final String ITR_OCR = "ITR OCR";
	public static final String DL_OCR = "License OCR";
	public static final String VOTERID_OCR = "VOTER ID OCR";
	public static final String CIN_OCR = "Cin OCR";
	

	public static final String CROSS_ORIGIN = "";

	public static final String EMAIL_VERIFICATION = "Facheck Email Verification !";
	public static final String ON_BOARD = "Onboarding Mail";
	public static final String PRICE_ACCEPTED = "New Service Price Accepted";
	public static final String POSTPAID_PAYMENT_REMINDER = "Payment Reminder";
	public static final String SIGNER_REQ = "Signer request";
	public static final String PREPAID_MONTHLY_INVOICE = "Monthly Invoice";
	public static final String PRICE_UPDATION = "Service Price Updation";
	
	public static final String PAYMENT_SUCCESS = "Payment Successful";

	public static final String VERIFICATIONPIN = "{verificationPin}";
	public static final String LOGO = "{Logo}";
//	public static final String EMAIL_LOGO_PATH="http://cinchfuel.in/assets/img/cinch_fuel_logo.jpg";
	public static final String URL_LABEL = "{urlLabel}";
	public static final String URLNAME = "{urlName}";
	public static final String URL = "{url}";
	public static final String CORPORATE_NAME = "{CorporateName}";
	public static final String MONTH = "{month}";
	public static final String GRACE_DATE = "{graceDate}";
	public static final String RECEIPT_LINK = "{receiptLink}";
	public static final String CONVE_INVOICE_LINK = "{conveInvoLink}";
	
	public static final String RETURN_URL = "http://64.227.149.125:9087/returns/returnpager";
	public static final String END_URL = "/enach/pgmode/merchant";

	public static final String PG_URL = "https://staging-connect.basispay.in/api/payment/order";
	public static final String PG_RES_URL = "https://staging-connect.basispay.in/api/payment/receipt";
	public static final String PG_PRODUCTION_URL = "https://connect.basispay.in/api/payment/receipt";
	public static final String Refund_Request_Url = "https://staging-api.basispay.in/rest/request/refund";
	public static final String STATUS = "status";

	public static final String PAYMENT_INITIATE_STATUS = "Initiated";
	public static final String PAYMENT_NOT_INITIATE_STATUS = "Payment not Initiated";
	public static final String BAD_REQUEST = "BAD REQUEST";
	public static final String NO_DATA_FOUND = "NO DATA FOUND";
	public static final String TECHNICAL_ERROR = "TECHNICAL ERROR";

	public static final String INVALID_USER = "INVALID USER";
	public static final String SUCCESS = "SUCCESS";
	public static final String TICKET_WORKERS_VERIFY_OTP = "TICKET WORKER VERIFY OTP";
	public static final String ERROR_MESSAGE = "Something went wrong while accessing request please try again!";
	public static final String ADVERTISEMENT_UPDATED = "Advertisements has been added successfully!!";
	public static final String NEW_USER = "New User";
	public static final String EXISTING_USER = "Existing User";
	public static final String OPEN = "Open";

	// ENCRYPTION & DECRYPTION
	public static final String AES_ECB_PADDING5 = "AES/ECB/PKCS5Padding";

	// LOCATIONS
	public static final String TRY_AGAIN = "Something went wrong while accessing request please try again!";

	public static final String DOCUMENT_NOT_FOUND = "DOCUMENT NOT FOUND";
	public static final String UPDATED = "Updated";
	public static final String INVALID_PASSWORD = "INVALID PASSWORD";
	public static final String INVALID_CURRENT_PASSWORD = "INVALID CURRENT PASSWORD";
	public static final String INTERRUPTED = "Interrupted!";
	public static final String APPROVED = "Approved";
	public static final String PENDING = "Pending";
	public static final String DATE_FORMATE = "yyyy-MM-dd hh:mm:ss a";
	public static final String SMS_TEMP = "SMS_TEMP_021";
	public static final String OTP_NOT_VERIFIED = "Your OTP not verified!";
	public static final String OTP_VERIFIED = "Your OTP verified!";
	public static final String OTP_EXPIRED = "Your OTP Expired!";
	public static final String ID_NOT_FOUND = "Id Not Found";
	public static final String ORDER_REFERENCE = "orderReference";
	public static final String USER_SECRET_KEY = "{encryptionKey}";

	public static final String FIXXUP = "FixxUp";
	public static final String EMAIL_TEMPLATE_PATH = "emailTemplatePath==>";
	public static final String PAYMENT_MODE_LOADED = "Payment mode has loaded";
	public static final String PAYMENT_MODE_NOT_FOUND = "Payment mode not found";
	public static final String API_HASH = "Api-Hash";
	public static final String PG_API_KEY = "Api-Key";
	public static final String ADMIN_IN_ACTIVE = "Admin is Inactive";
	public static final String USER_IN_ACTIVE = "Account Inactive";
	public static final String ROLE_ID_NOT_MATCHED = "Role Id not Matched";
	public static final String INVALID_CREDENTIALS = "Invalid Credentials..!!";
	public static final String INVALID_USERNAME = "Invalid Username !";
	public static final String ACCOUNT_BLOCKED = "Account is Blocked !";
	public static final String INVALID_USERNAME_OR_PASSWORD = "Invalid Username or Password !";
	public static final String OTP_SENT_SUCCESSFULLY = "OTP has been sent successfully..!!";
	public static final String OTP_SENT_NOT_SUCCESSFULLY = "OTP not sent successfully..!!";
	public static final String PASSWORD_RESET_SUCCESSFULLY = "Password Reset Successfully..!!!";
	public static final String PASSWORD_RESET_NOT_SUCCESSFULLY = "Password Reset Not Successfully..!!!";
	public static final String PASSWORD_CHANGE_SUCCESSFULLY = "Password changed successfully..!!!";
	public static final String PASSWORD_NOT_MATCHED = "Password Not Matched";
	public static final String OTP_NOT_MATCHED = "OTP Not Matched";
	public static final String ALREADY_EXCIST = "Email Address / mobile number already exists";
	public static final String MOBILENUMBER = "{mobileNumber}";
	public static final String CUSTOMER_NAME = "{CustomerName}";
	public static final String EMAIL = "{email}";
	public static final String NAME = "{Name}";
	public static final String DATE = "{Date}";
	public static final String PUBLIC_URL = "{urlLabel}";
	public static final String LOGIN_URL = "{url}";
	public static final String GET_MAIL_BODY = "Get Mail Body Method";
	
	public static final String PAID_AMOUNT = "{paidAmount}";
	public static final String RECHARGED_AMOUNT = "{rechargedAmount}";
	public static final String WALLET_BALANCE = "{walletBalance}";
	public static final String PAID_DATE = "{paidDate}";
	public static final String PAYMENT_METHOD = "{paymentMethod}";
	public static final String TRANSACTION_ID = "{transactionId}";
	
	public static final String TOTAL_HITS = "{totalHits}";
	public static final String USED_AMOUNT = "{usedAmount}";
	public static final String USED_SERVICES ="{usedServices}";
	
	
	public static final String FROM_DATE = "{fromDate}";
	public static final String TO_DATE = "{toDate}";
	public static final String CONSUMED_AMOUNT = "{consumedAmount}";
	public static final String TOTAL_AMOUNT = "{totalAmount}";


	public static final String MAIL_SMTP_HOST = "mail.smtp.host";
	public static final String MAIL_SMTP_PORT = "mail.smtp.port";
	public static final String SPRING_MAIL_HOST = "spring.mail.host";
	public static final String SPRING_MAIL_PORT = "spring.mail.port";
	public static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";
	public static final String MAIL_SMPT_AUTH = "mail.smtp.auth";
	public static final String SPRING_MAIL_PROPERTIES_MAIL_SMTP_PORT = "spring.mail.properties.mail.smtp.port";
	public static final String SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH = "spring.mail.properties.mail.smtp.auth";
	public static final String SPRING_MAIL_PROPERTIES_MAIL_SMTP_CONNECTION_TIME_OUT = "spring.mail.properties.mail.smtp.connectiontimeout";
	public static final String MAIL_SMTP_CONNECTION_TIME_OUT = "mail.smtp.connectiontimeout";
	public static final String MAIL_SMTP_STARRTTLS_ENABLE = "mail.smtp.starttls.enable";
	public static final String SPRING_MAIL_PROPERTIE_MAIL_SMTP_STARRTTLS_ENABLE = "spring.mail.properties.mail.smtp.starttls.enable";
	public static final String MAIL_SMTP_SOCKETFACTORY_PORT = "mail.smtp.socketFactory.port";
	public static final String SPRING_MAIL_PROPERTIES_MAIL_SMTP_SOCKETFACTORY_PORT = "spring.mail.properties.mail.smtp.socketFactory.port";
	public static final String MAIL_SMTP_SOCKETFACTORY_CLASS = "mail.smtp.socketFactory.class";
	public static final String SPRING_MAIL_PROPERTIES_MAIL_SMTP_SOCKETFACTORY_CLASS = "spring.mail.properties.mail.smtp.socketFactory.class";
	public static final String SPRING_MAIL_USERNAME = "spring.mail.username";
	public static final String SPRING_MAIL_PASSWORD = "spring.mail.password";
	public static final String CLASSPATH_ORDER_INVOICE = "classpath:orderInvoice/";
	public static final String TEXT_HTML = "text/html";
	public static final String MAIL_SENT_SUCCESS = "Mail has been sent successfully!";
	public static final String WEB_INF_USER_MONTHLY_INVOICE = "/WEB-INF/userMonthlyinvoice/";
	public static final String RESOURCE = "/resources/emailtemplates/departmentlogin.html";
	public static final String CUSTOMER_VERIFY_EMAIL = "Send sendCustomerVerifyEmail Method";
	public static final String GET_MAIL_BODY3 = "Send getMailBody3 Method";
	public static final String WITHIN_LIMIT = "Login count with in limit";

	// ESIGN
	public static final String ESIGN_X_PARSE_APPLICATION_ID = "baabujiventuresprivatelimited_esign_uat";
	public static final String ESIGN_X_PARSE_REST_API_KEY = "58840536b1420e4a05faf919f87c4802";

	/** SMS Parameters **/
	public static final String SMS_ADMIN_CONTACTNO = "support@cinchfuel.com";

	public static final String REFERENCE_ID = "reference_id";
	public static final String SOURCE = "source";

	// FACHECK CONSTANTS
	public static final String SIGNER_NAME = "{signerName}";
	public static final String LOGO1 = "{Logo1}";

	// Production Facheck Logo

	public static final String FACHECK_LOGO = "/resources/image/fachecklogo.png";
	public static final String FC_EMAIL_LOGO = "http://157.245.105.135:5031/assets/fachecklogo.png";
	public static final String SIGNATURE_LOGO = "http://157.245.105.135:5031/assets/image.png";
	public static final String SIGNATURE_SUCCESS_LOGO = "http://157.245.105.135:5031/assets/signdone.png";
	
	public static final String EMAIL_LOGO_PATH = "http://157.245.105.135:5031/assets/fachecklogo.png";//"http://157.245.105.135:5001/assets/facheck.png";
	public static final String EMAIL_LOGO_PATH1 = "http://157.245.105.135:5001/assets/mail1.jpg";
	public static final String EMAIL_LOGO_PATH2 = "http://157.245.105.135:5001//assets/check1.jpg";
	public static final String MERCHANT_ID = "{merchantId}";
	public static final String COMPANY_NAME = "{companyName}";
	public static final String SIGNER_REFERENCE = "{signerReference}";
	public static final String SIGNER_ID = "{signerId}";
	public static final String PDF_NAME = "{pdfName}";
	public static final String FINISHED_REPORT = "Facheck Signature Completion Report";
//		public static final String URL_LABEL = "{urlLabel}";
//		public static final String URLNAME = "{urlName}";
//		public static final String URL = "{url}";
//		public static final String CORPORATE_NAME ="{CorporateName}";
//		public static final String RETURN_URL = "http://157.245.105.135:9085/enach/pgmode/merchant";
//		public static final String END_URL = "/enach/pgmode/merchant";
//		public static final String CROSS_ORIGIN = "http://app.facheck.biz/";

	// Signdesk API's

	public static final String SIGN_PAN_URL = "https://kyc-uat.signdesk.in/api/sandbox/panVerification";
	public static final String SIGN_GST_URL = "https://kyc-uat.signdesk.in/api/sandbox/gstVerification";
	public static final String SIGN_PASSPORT_URL = "https://kyc-uat.signdesk.in/api/sandbox/passportVerification";
	public static final String SIGN_DL_URL = "https://kyc-uat.signdesk.in/api/sandbox/drivingLicenseVerification";
	public static final String SIGN_AADHARXMLSUBMITOTP_URL = "https://kyc-uat.signdesk.in/api/sandbox/aadhaarXMLSubmitOTP";
	public static final String SIGN_AADHARXMLVERIFICATION_URL = "https://kyc-uat.signdesk.in/api/sandbox/aadhaarXMLVerification";
	public static final String SIGN_MSME_URL = "https://kyc-uat.signdesk.in/api/sandbox/msmeVerification";
	public static final String SIGN_RC_URL = "https://in-rc-verify.staging-signdesk.com/api/rcVerification";
	public static final String SIGN_CIN_URL = "https://kyc-uat.signdesk.in/api/sandbox/cinVerification";
	public static final String SIGN_DIN_URL = "https://kyc-uat.signdesk.in/api/sandbox/dinVerification";
	public static final String SIGN_ESIGN_URL = "https://uat.signdesk.in/api/sandbox/signRequest";

	public static final String X_PARSE_APPLICATION_ID = "baabujiventuresprivatelimited_kyc_uat";
	public static final String X_PARSE_REST_API_KEY = "000bed65968338d193c06a8bb0b9b74b";

	public static final String CONTENT_TYPE = "application/json";
	public static final String ENCRYPTION_KEY = "qPNNbUwcSOhXgWGwdBOXopjgSNPynDkC";

//	public static final String FREE_TRIAL_APIKEY = "916vyd0meh5yekpltset663b7p6esac";
//	public static final String FREE_TRIAL_APPLICATION_ID = "testcase_ekyc_uat";
//	public static final String FREE_TRIAL_SECRET_KEY = "zfpIrc7io7d0hye77y7eSew7h0reoysx";

	public static final String PAN_PATTERN = "[A-Z]{5}[0-9]{4}[A-Z]{1}";
	public static final String GST_PATTERN = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[0-9]{1}[A-Z]{1}[0-9A-Z]{1}$";
	public static final String AADHAAR_PATTERN = "\\d{12}";
	public static final String AADHAAR_OTP_PATTERN = "\\d{6}";
	public static final String BANKACCOUNT_PATTERN = "\\d{11,16}";
	public static final String IFSC_PATTERN = "^[A-Z]{4}0[A-Z0-9]{6}$";
	public static final String CIN_PATTERN = "^[LU][0-9]{5}[A-Z]{2}[0-9]{4}[A-Z]{3}[0-9]{6}$";
	public static final String DIN_PATTERN = "^[0-9]{8}$";
	public static final String DL_PATTERN = "^[A-Z]{2}[0-9]{2}\\d{11}";
	public static final String DL_PATTERN_2 = "^[A-Z]{2}[0-9]{2}[A-Z]{1,2}\\d{11}";
	public static final String PASSPORT_PATTERN = "^[A-Z]{3}[0-9]{9}$";
	public static final String VOTERID_PATTERN = "^[a-zA-Z]{3}[0-9]{7}$";
	public static final String MSME_PATTERN = "^[A-Z]{5}-[A-Z]{2}-\\d{2}-\\d{3,7}$";
	public static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
	public static final String ESIC_PATTERN = "^\\d{17}$";
	public static final String UAN_PATTERN = "/^\\d{12}$/";
	public static final String IEC_PATTERN = "^[A-Z]{3}[0-9]{7}$";
	public static final String PHONENUMBER_PATTERN = "\\d{10}";
	public static final String UPI_PATTERN = "^\\d{10}@[a-zA-Z]{3,}$";
	public static final String RC_PATTERN = "^[A-Z]{2}[0-9]{2}[A-Z]{2}[0-9]{4}$";
	public static final String FSSAI_PATTERN = "^\\d{14}$";
	public static final String MEMBERSHIP_PATTERN = "\\d{6}";

	public static final String DUMMY_SUCCESS_MESSAGE = "Success";
	public static final String DUMMY_FAILED_MESSAGE = "Invalid source.";
	public static final String DUMMY_ERROR_MESSAGE = "Error";

	public static final String FILE_MISSING_OR_EMPTY = "The file is either missing or contains no data.";
	public static final String NOT_SUPPORTED = "The file format is not supported.";
	public static final String TOO_LARGE = "The file format is too large,Please use an image below 2MB.";
	public static final String RAILWAY_DATE_FORMAT ="yyyy-MM-dd HH:mm:ss";
	public static final boolean SUREPASS_ROUTE = false;
	public static final String INVOICE_LINK = "{invoiceLink}";

}
