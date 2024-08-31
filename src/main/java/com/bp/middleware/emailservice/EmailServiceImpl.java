package com.bp.middleware.emailservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.bp.middleware.prepaidmonthlyinvoice.PrepaidMonthlyInvoice;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.signers.SignerModel;
import com.bp.middleware.signmerchant.MerchantModel;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.ServiceImplement;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

@Service

public class EmailServiceImpl implements EmailService {

	private final Logger loggers = LoggerFactory.getLogger(this.getClass());

	JavaMailSender emailSender;

	@Autowired
	private Environment env;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private SmtpMailRepository smtpMailRepository;
	@Autowired
	private MailConfigurationUtils mailConfigurationUtils;
	@Autowired
	private FileUtils fu;

	@Override
	public boolean sendEmailAdminOTPVerification(String receipientEmail, String userName, String mobileNumber,
			String securityCode, String loginURL) throws Exception {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/middlewareadminlogin.html");// adminlogin.html
		String message = getMailBody(saveFile.getPath());
		message = message.replace(AppConstants.CUSTOMER_NAME, userName);
		message = message.replace(AppConstants.EMAIL, receipientEmail);
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);
		message = message.replace(AppConstants.VERIFICATIONPIN, securityCode);
		message = message.replace(AppConstants.MOBILENUMBER, mobileNumber);
		message = message.replace(AppConstants.LOGIN_URL, loginURL);

		return mailConfigurationUtils.sendEmail(receipientEmail, AppConstants.ON_BOARD, message, null);
	}

	@Override
	public boolean sendMonthlyReminderMail(EntityModel user, PostpaidPayment postpaid, String month, String graceDate)
			throws Exception {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/monthlypostpaidreminder.html");
		String message = getMailBody(saveFile.getPath());

		String consumedAmount = Double.toString(fu.twoDecimelDouble(postpaid.getDueAmount()));
		String totalAmount = calculateGst(fu.twoDecimelDouble(postpaid.getDueAmount()));

		String mail = user.getEmail();

		message = message.replace(AppConstants.NAME, user.getName());
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);
		message = message.replace(AppConstants.MONTH, month);
		message = message.replace(AppConstants.GRACE_DATE, graceDate);
		message = message.replace(AppConstants.FROM_DATE, postpaid.getStartDate().toString());
		message = message.replace(AppConstants.TO_DATE, postpaid.getEndDate().toString());
		message = message.replace(AppConstants.CONSUMED_AMOUNT, consumedAmount);
		message = message.replace(AppConstants.TOTAL_AMOUNT, totalAmount);
		message = message.replace(AppConstants.LOGIN_URL, "http://157.245.105.135:5030/mail-convinience");

		return mailConfigurationUtils.sendEmailWithTwoAttachment(mail, AppConstants.POSTPAID_PAYMENT_REMINDER, message,
				null, postpaid);
	}

	@Override
	public boolean sendGraceInvoice(EntityModel user, PostpaidPayment postpaid, String month, String graceDate,
			RequestModel model) throws Exception {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/monthlypostpaidreminder.html");
		String message = getMailBody(saveFile.getPath());

		String consumedAmount = Double.toString(fu.twoDecimelDouble(model.getIdPrice()));
		String totalAmount = calculateGst(fu.twoDecimelDouble(model.getIdPrice()));

		String mail = user.getEmail();

		message = message.replace(AppConstants.NAME, user.getName());
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);
		message = message.replace(AppConstants.MONTH, month);
		message = message.replace(AppConstants.GRACE_DATE, "");
		message = message.replace(AppConstants.FROM_DATE, model.getFromDate());
		message = message.replace(AppConstants.TO_DATE, model.getToDate());
		message = message.replace(AppConstants.CONSUMED_AMOUNT, consumedAmount);
		message = message.replace(AppConstants.TOTAL_AMOUNT, totalAmount);
		message = message.replace(AppConstants.LOGIN_URL, "http://157.245.105.135:5030/mail-convinience");

		return mailConfigurationUtils.sendGraceInvoice(mail, AppConstants.POSTPAID_PAYMENT_REMINDER, message, postpaid);
	}

	@Override
	public boolean sendEmailOTPVerificationForUser(String receipientEmail, String userName, String mobileNumber,
			String securityCode, String loginURL, String secretKey, EntityModel user) throws Exception {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/middlewareuserlogin.html");// middlewareuserlogin.html
																											// //login.html
		String message = getMailBody(saveFile.getPath());
		message = message.replace(AppConstants.CUSTOMER_NAME, userName);
		message = message.replace(AppConstants.EMAIL, receipientEmail);
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);
		message = message.replace(AppConstants.VERIFICATIONPIN, securityCode);
		message = message.replace(AppConstants.MOBILENUMBER, mobileNumber);
		message = message.replace(AppConstants.PUBLIC_URL, loginURL);
		message = message.replace(AppConstants.LOGIN_URL, loginURL);

		if (secretKey != null) {
			message = message.replace(AppConstants.USER_SECRET_KEY, secretKey);
		}

		if (user != null && user.isMailPresent()) {

			System.err.println("EXTERNAL MAIL");
			loggers.info("EXTERNAL MAIL");
			return mailConfigurationUtils.sendEmailFromExternals(receipientEmail, AppConstants.ON_BOARD, message, null,
					user);

		} else {

			System.err.println("DEFAULT MAIL");
			loggers.info("DEFAULT MAIL");
			return mailConfigurationUtils.sendEmail(receipientEmail, AppConstants.ON_BOARD, message, null);
		}
	}

	@Override
	public boolean sendEmailOTPVerification(String receipientEmail, String userName, String mobileNumber,
			String securityCode, String loginURL, String secretKey) throws Exception {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/middlewareuserlogin.html");// middlewareuserlogin.html
																											// //login.html
		String message = getMailBody(saveFile.getPath());
		message = message.replace(AppConstants.CUSTOMER_NAME, userName);
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);
		message = message.replace(AppConstants.VERIFICATIONPIN, securityCode);
		message = message.replace(AppConstants.MOBILENUMBER, mobileNumber);
		message = message.replace(AppConstants.PUBLIC_URL, loginURL);
		message = message.replace(AppConstants.LOGIN_URL, loginURL);

		if (secretKey != null) {
			message = message.replace(AppConstants.USER_SECRET_KEY, secretKey);
		}

		return mailConfigurationUtils.sendEmail(receipientEmail, AppConstants.ON_BOARD, message, null);
	}

	private String getMailBody(String configurationFilePath) {
		StringBuilder sb = new StringBuilder();
		InputStream is = null;
		try {
			is = new FileInputStream(new File(configurationFilePath));
			int ch;
			while ((ch = is.read()) != -1)
				sb.append((char) ch);
		} catch (Exception e) {
			loggers.info(AppConstants.TECHNICAL_ERROR, e);
		}
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				loggers.info(AppConstants.TECHNICAL_ERROR, e);
			}
		}
		return sb.toString();

	}

	@Override
	public boolean sendCustomerVerifyEmail(String email, String otpCode, EntityModel user) throws Exception {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/middlewareSendotpVerify.html");
		String message = getCustomerverifymail(saveFile.getPath());

		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);
		message = message.replace(AppConstants.VERIFICATIONPIN, otpCode);

		if (user != null && user.isMailPresent()) {

			System.err.println("EXTERNAL MAIL");
			loggers.info("EXTERNAL MAIL");
			return mailConfigurationUtils.sendVerifyEmailFromExternal(email, AppConstants.EMAIL_VERIFICATION, message,
					null, user);

		} else {

			System.err.println("DEFAULT MAIL");
			loggers.info("DEFAULT MAIL");
			return mailConfigurationUtils.sendVerifyEmail(email, AppConstants.EMAIL_VERIFICATION, message, null);

		}
	}

	private String getCustomerverifymail(String configurationFilePath) {
		StringBuffer sb = new StringBuffer();
		InputStream is = null;
//		try {
		int ch;
		try {
			is = new FileInputStream(new File(configurationFilePath));
			while ((ch = is.read()) != -1)
				sb.append((char) ch);
		} catch (IOException e) {
			loggers.info(AppConstants.TECHNICAL_ERROR, e);
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (Exception e) {
				loggers.info(AppConstants.TECHNICAL_ERROR, e);
			}
			is = null;
		}

		return sb.toString();
	}
	// @Override
	// public boolean sendCustomerEmailVerify(CustomerModel entity, CorporateDto
	// corporate, String password) throws MessagingException {
	//
	// Path emailTemplatePath = Paths.get("").toAbsolutePath();
	// File saveFile = new File(emailTemplatePath + AppConstants.RESOURCE);
	// String message = getMailBody(saveFile.getPath());
	// message = message.replace(AppConstants.CORPORATE_NAME,corporate.getName());
	// message = message.replace(AppConstants.CUSTOMER_NAME, entity.getName());
	// message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);
	// message = message.replace(AppConstants.VERIFICATIONPIN, password);
	// message = message.replace(AppConstants.MOBILENUMBER,entity.getMobileNumber()
	// );
	//
	// return sendPassword(entity,corporate,message,null);
	//
	//// return sendPassword2(entity.getEmail(),
	// corporate.getHost(),corporate.getSmtpAuth(),corporate.getSmtpConnectionTimeOut(),corporate.getStarttlsEnable(),corporate.getSocketFactoryClass(),
	//// corporate.getMailUserName(),corporate.getMailPassword(),
	// corporate.getPort(),corporate.getSmtpPort(),corporate.getSocketFactoryPort(),AppConstants.EMAIL_VERIFICATION,message,null);
	// }

	// private boolean sendPassword(CustomerModel entity, CorporateDto
	// corporate,String emailContent,String object) throws MessagingException {
	// boolean ismailSent = false;
	// Properties properties = new Properties();
	// properties.put(AppConstants.MAIL_SMTP_HOST,corporate.getHost());
	// properties.put(AppConstants.MAIL_SMTP_PORT,corporate.getPort());
	// properties.put(AppConstants.MAIL_SMPT_AUTH, corporate.getSmtpAuth());
	// properties.put(AppConstants.MAIL_TRANSPORT_PROTOCOL,corporate.getSmtpPort());
	// properties.put(AppConstants.MAIL_SMTP_CONNECTION_TIME_OUT,corporate.getSmtpConnectionTimeOut());
	// properties.put(AppConstants.MAIL_SMTP_STARRTTLS_ENABLE
	// ,corporate.getStarttlsEnable());
	// properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_PORT,corporate.getSocketFactoryPort());
	// properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_CLASS,corporate.getSocketFactoryClass());
	//
	// Session session = Session.getInstance(properties, new Authenticator() {
	// protected PasswordAuthentication getPasswordAuthentication() {
	// return new
	// PasswordAuthentication(corporate.getMailUserName(),corporate.getMailPassword());
	// }
	// });
	//
	//
	// MimeMessage message = new MimeMessage(session);
	// MimeMessageHelper helper = new MimeMessageHelper(message, true);
	// String formName = env.getProperty(AppConstants.SPRING_MAIL_PASSWORD);
	// if (formName != null) {
	// helper.setFrom(formName);
	// }
	// helper.setTo(entity.getEmail());
	// helper.setSubject(AppConstants.EMAIL_VERIFICATION);
	//
	// /** Send message with html format **/
	// Multipart multipart = new MimeMultipart();
	// BodyPart htmlBodyPart = new MimeBodyPart();
	//
	// if(object!=null) {
	// try {
	// MimeBodyPart attachPart = new MimeBodyPart();
	// Resource resource =
	// resourceLoader.getResource(AppConstants.WEB_INF_USER_MONTHLY_INVOICE+object);
	// DataSource source = new FileDataSource(resource.getFile().getPath());
	//
	// attachPart.setDataHandler(new DataHandler(source));
	// attachPart.setFileName(new File(resource.getFile().getName()).getName());
	//
	// multipart.addBodyPart(attachPart);
	// } catch (Exception e) {
	// logger.info("Send Password2 Method",e);
	// }
	// }
	//
	// htmlBodyPart.setContent(emailContent, AppConstants.TEXT_HTML);
	// multipart.addBodyPart(htmlBodyPart);
	// message.setContent(multipart);
	// Transport.send(message);
	// ismailSent = true;
	// logger.info(AppConstants.MAIL_SENT_SUCCESS);
	// return ismailSent;
	// }

	@Override
	public boolean sendCustomerEmailVerify(String email, String customerName, String mobileNumber, String host,
			String smtpAuth, String smtpConnectionTimeOut, String starttlsEnable, String socketFactoryClass,
			String mailUserName, String mailPassword, String port, String smtpPort, String socketFactoryPort,
			String corporateLegalName, String password, String string) throws MessagingException {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		// LOGGER.info("emailTemplatePath==>"+emailTemplatePath+"/resources/emailtemplates/departmentlogin.html");
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/departmentlogin.html");
		String message = getMailBody(saveFile.getPath());
		message = message.replace(AppConstants.CUSTOMER_NAME, customerName);
		message = message.replace(AppConstants.CORPORATE_NAME, corporateLegalName);
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);
		message = message.replace(AppConstants.VERIFICATIONPIN, password);
		message = message.replace(AppConstants.MOBILENUMBER, mobileNumber);

		return mailConfigurationUtils.sendPassword2(email, host, smtpAuth, smtpConnectionTimeOut, starttlsEnable,
				socketFactoryClass, mailUserName, mailPassword, port, smtpPort, socketFactoryPort,
				AppConstants.ON_BOARD, message, null);

	}

	@Override
	public boolean sendEmailVerify(String email, String otp, SmtpMailConfiguration configuration)
			throws MessagingException {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/verifyemail.html");
		String message = getCustomerverifymail(saveFile.getPath());
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);
		message = message.replace(AppConstants.VERIFICATIONPIN, otp);

		return mailConfigurationUtils.sendCustomerVerify(email, AppConstants.EMAIL_VERIFICATION, message, null,
				configuration);

	}

	@Override
	public boolean sendMemberAddition(String email, String name, String mobileNumber, String membershipNumber,
			String libraryName, String string) throws Exception {
		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/memberlogin.html");
		String message = getMailBody(saveFile.getPath());
		message = message.replace(AppConstants.CUSTOMER_NAME, name);
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);
		message = message.replace(AppConstants.VERIFICATIONPIN, membershipNumber);
		message = message.replace(AppConstants.MOBILENUMBER, mobileNumber);
//		message = message.replace(AppConstants.LIBRARY_NAME, libraryName);

		return mailConfigurationUtils.sendEmail(email, AppConstants.ON_BOARD, message, null);

	}

	@Override
	public boolean signerRequest(String merchantId, String signerName, String merchantCompanyName, String signerMobile,
			String signerEmail, String signerRefId, EntityModel user) throws Exception {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/signedrequest.html");
		String message = getMailBody(saveFile.getPath());
		message = message.replace(AppConstants.SIGNER_NAME, signerName);
		message = message.replace(AppConstants.LOGO, AppConstants.FC_EMAIL_LOGO);
		message = message.replace(AppConstants.LOGO1, AppConstants.SIGNATURE_LOGO);
		message = message.replace(AppConstants.MERCHANT_ID, merchantId);
		message = message.replace(AppConstants.COMPANY_NAME, merchantCompanyName);
		message = message.replace(AppConstants.SIGNER_REFERENCE, signerRefId);

		System.err.println(AppConstants.SIGNER_REFERENCE + "   " + signerRefId);

		if (user != null && user.isMailPresent()) {

			System.err.println("EXTERNAL MAIL");
			loggers.info("EXTERNAL MAIL");
			return mailConfigurationUtils.sendEmailFromExternals(signerEmail, AppConstants.SIGNER_REQ, message, null,
					user);

		} else {

			System.err.println("DEFAULT MAIL");
			loggers.info("DEFAULT MAIL");
			return mailConfigurationUtils.sendEmail(signerEmail, AppConstants.SIGNER_REQ, message, null);
		}
	}

	@Override
	public boolean finishedReport(MerchantModel merchantModel, SignerModel signer, EntityModel user) throws Exception {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/doccomplete.html");
		String message = getMailBody(saveFile.getPath());
		message = message.replace(AppConstants.SIGNER_NAME, signer.getSignerName());
		message = message.replace(AppConstants.LOGO, AppConstants.FC_EMAIL_LOGO);
		message = message.replace(AppConstants.LOGO1, AppConstants.SIGNATURE_SUCCESS_LOGO);
		message = message.replace(AppConstants.COMPANY_NAME, merchantModel.getDocumentTitle());

		if (user != null && user.isMailPresent()) {

			System.err.println("EXTERNAL MAIL");
			loggers.info("EXTERNAL MAIL");
			return mailConfigurationUtils.sendFinishedEmailFromExternal(signer.getSignerEmail(),
					AppConstants.FINISHED_REPORT, message, null, merchantModel, user);

		} else {

			System.err.println("DEFAULT MAIL");
			loggers.info("DEFAULT MAIL");
			return mailConfigurationUtils.sendFinishedEmail(signer.getSignerEmail(), AppConstants.FINISHED_REPORT,
					message, null, merchantModel);
		}
	}

	// USED COUNT - SIGNERS -1,

	@Override
	public boolean prepaidPaymentSuccessMail(String mail, TransactionDto transactionDto, PrepaidPayment prepaid)
			throws Exception {

		System.err.println("MAIL");

		System.err.println("PREP : " + prepaid.getPrepaidId());

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/prepaidsuccess.html");

		String message = getMailBody(saveFile.getPath());

		String paidAmount = Double.toString(fu.twoDecimelDouble(transactionDto.getPaidAmount()));
		String rechargedAmount = Double.toString(fu.twoDecimelDouble(transactionDto.getExclusiveAmount()));
		String walletBalance = Double.toString(fu.twoDecimelDouble(transactionDto.getEntity().getRemainingAmount()));

		String paymentDateTime = "";
		String paymentMode = "";
		String paymentId = "";

		if (transactionDto.getPaymentDatetime() != null) {
			paymentDateTime = transactionDto.getPaymentDatetime();
		}

		if (prepaid.getPaymentMode() != null) {
			paymentMode = prepaid.getPaymentMode();
		}

		if (transactionDto.getPaymentId() != null) {
			paymentId = transactionDto.getPaymentId();
		}

		message = message.replace(AppConstants.PAID_AMOUNT, paidAmount);
		message = message.replace(AppConstants.RECHARGED_AMOUNT, rechargedAmount);
		message = message.replace(AppConstants.WALLET_BALANCE, walletBalance);
		message = message.replace(AppConstants.PAID_DATE, paymentDateTime);
		message = message.replace(AppConstants.PAYMENT_METHOD, paymentMode);
		message = message.replace(AppConstants.TRANSACTION_ID, paymentId);
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);
		message = message.replace(AppConstants.RECEIPT_LINK,
				"http://157.245.105.135:5031/view-prepaid-receipt/" + prepaid.getUniqueId());

		if (transactionDto.getTransactionAmountModel() != null
				&& transactionDto.getTransactionAmountModel().getConvenianceFee() > 0) {
			message = message.replace(AppConstants.CONVE_INVOICE_LINK,
					"http://157.245.105.135:5031/view-convinience/" + prepaid.getUniqueId());
		} else {
			message = message.replace(AppConstants.CONVE_INVOICE_LINK, "");
		}

		// return mailConfigurationUtils.sendEmailWithTwoAttachment(mail,
		// AppConstants.PAYMENT_SUCCESS, message, prepaid, null);

		mailConfigurationUtils.sendEmail("gowrishankar.r@basispay.in", AppConstants.PAYMENT_SUCCESS, message, null);
		mailConfigurationUtils.sendEmail("abhishek.p@basispay.in", AppConstants.PAYMENT_SUCCESS, message, null);
		return mailConfigurationUtils.sendEmail(mail, AppConstants.PAYMENT_SUCCESS, message, null);

	}

	@Override
	public boolean prepaidInvoiceGenerateMail(EntityModel entityModel, String startDate, String endDate, String month)
			throws Exception {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/invoicePrepaid.html");

		String message = getMailBody(saveFile.getPath());

		String url = "http://157.245.105.135:5031/view-mailer-prepaid/" + entityModel.getSaltKey() + "/" + startDate
				+ "/" + endDate;

		message = message.replace(AppConstants.INVOICE_LINK, url);
		message = message.replace(AppConstants.MONTH, month);
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);

		mailConfigurationUtils.sendEmail("gowrishankar.r@basispay.in", AppConstants.PAYMENT_SUCCESS, message, null);
		mailConfigurationUtils.sendEmail("abhishek.p@basispay.in", AppConstants.PAYMENT_SUCCESS, message, null);
		return mailConfigurationUtils.sendEmail(entityModel.getEmail(), AppConstants.PREPAID_MONTHLY_INVOICE, message,
				null);

	}

	@Override
	public boolean postpaidPaymentSuccessMail(String mail, TransactionDto transactionDto, PostpaidPayment postpaid)
			throws Exception {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/postpaidsuccess.html");

		String message = getMailBody(saveFile.getPath());

		String startDate = postpaid.getStartDate().toString();
		String endDate = postpaid.getEndDate().toString();

		String paidAmount = Double.toString(fu.twoDecimelDouble(transactionDto.getPaidAmount()));
		String consumedAmount = Double.toString(fu.twoDecimelDouble(transactionDto.getExclusiveAmount()));

		String paymentDateTime = "";
		String paymentMode = "";
		String paymentId = "";

		if (transactionDto.getPaymentDatetime() != null) {
			paymentDateTime = transactionDto.getPaymentDatetime();
		}

		if (transactionDto.getModeOfPaymentPg() != null) {
			paymentMode = transactionDto.getModeOfPaymentPg().getModeOfPayment();
		}

		if (transactionDto.getPaymentId() != null) {
			paymentId = transactionDto.getPaymentId();
		}

		message = message.replace(AppConstants.FROM_DATE, startDate);
		message = message.replace(AppConstants.TO_DATE, endDate);
		message = message.replace(AppConstants.CONSUMED_AMOUNT, consumedAmount);
		message = message.replace(AppConstants.PAID_AMOUNT, paidAmount);
		message = message.replace(AppConstants.PAID_DATE, paymentDateTime);
		message = message.replace(AppConstants.PAYMENT_METHOD, paymentMode);
		message = message.replace(AppConstants.TRANSACTION_ID, paymentId);
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);
		message = message.replace(AppConstants.RECEIPT_LINK, postpaid.getUniqueId());

		if (transactionDto.getTransactionAmountModel() != null
				&& transactionDto.getTransactionAmountModel().getConvenianceFee() > 0) {
			message = message.replace(AppConstants.CONVE_INVOICE_LINK, postpaid.getUniqueId());
		} else {
			message = message.replace(AppConstants.CONVE_INVOICE_LINK, "");
		}

		return mailConfigurationUtils.sendEmail(mail, AppConstants.PAYMENT_SUCCESS, message, null);
	}

	private String calculateGst(double dueAmount) {

		double ceil = fu.twoDecimelDouble(dueAmount * 18 / 100);

		String totalAmount = Double.toString(ceil);

		return totalAmount;
	}

	@Override
	public boolean sendMerchantPriceUpdationMail(EntityModel entityModel) throws Exception {// here

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/merchantPriceUpdation.html");

		String message = getMailBody(saveFile.getPath());

		message = message.replace(AppConstants.LOGIN_URL,
				"http://157.245.105.135:5030/changed-price/" + entityModel.getSaltKey());
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);

		return mailConfigurationUtils.sendEmailWithTwoAttachment(entityModel.getEmail(), AppConstants.PRICE_UPDATION,
				message, null, null);

	}

	@Override
	public boolean priceAcceptedMailToAdmin(String email, String name) throws Exception {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/priceAcceptedMailAdmin.html");
		String message = getMailBody(saveFile.getPath());
		message = message.replace(AppConstants.NAME, name);
		message = message.replace(AppConstants.DATE, LocalDate.now().toString());
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);

		return mailConfigurationUtils.sendEmail(email, AppConstants.PRICE_ACCEPTED, message, null);
	}

	@Override
	public boolean priceAcceptedMailToEntity(String email, String name) throws Exception {

		Path emailTemplatePath = Paths.get("").toAbsolutePath();
		File saveFile = new File(emailTemplatePath + "/resources/emailtemplates/priceAcceptedMailEntity.html");
		String message = getMailBody(saveFile.getPath());
		message = message.replace(AppConstants.NAME, name);
		message = message.replace(AppConstants.LOGO, AppConstants.EMAIL_LOGO_PATH);

		return mailConfigurationUtils.sendEmail(email, AppConstants.PRICE_ACCEPTED, message, null);
	}

//	private String getMailBody1(String configurationFilePath) {
//		StringBuilder sb = new StringBuilder();
//		InputStream is = null;
//		try {
//			is = new FileInputStream(new File(configurationFilePath));
//			int ch;
//			while ((ch = is.read()) != -1)
//				sb.append((char) ch);
//		} catch (Exception e) {
//			loggers.info(AppConstants.TECHNICAL_ERROR,e);
//		} 
//		if (is != null) {
//			try {
//				is.close();
//			} catch (IOException e) {
//				loggers.info(AppConstants.TECHNICAL_ERROR,e);
//			}
//		}
//		return sb.toString();
//
//	}

//	private boolean sendEmail(String toMail, String subject, String emailcontent,String filename) throws MessagingException {
//		boolean ismailSent = false;
//		Properties properties = System.getProperties();
//		properties.put(AppConstants.MAIL_SMTP_HOST, env.getProperty(AppConstants.SPRING_MAIL_HOST));
//		properties.put(AppConstants.MAIL_SMTP_PORT, env.getProperty(AppConstants.SPRING_MAIL_PORT));
//		properties.put(AppConstants.MAIL_SMPT_AUTH, env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH));
//		properties.put(AppConstants.MAIL_TRANSPORT_PROTOCOL, env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_PORT));
//		properties.put(AppConstants.MAIL_SMTP_CONNECTION_TIME_OUT, env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_CONNECTION_TIME_OUT));
//		properties.put(AppConstants.MAIL_SMTP_STARRTTLS_ENABLE , env.getProperty(AppConstants.SPRING_MAIL_PROPERTIE_MAIL_SMTP_STARRTTLS_ENABLE));
//		properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_PORT,env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_SOCKETFACTORY_PORT));
//		properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_CLASS,env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_SOCKETFACTORY_CLASS));
//
//		Session session = Session.getInstance(properties, new Authenticator() {
//			@Override
//			protected PasswordAuthentication getPasswordAuthentication() {
//				return new PasswordAuthentication(env.getProperty(AppConstants.SPRING_MAIL_USERNAME),
//						env.getProperty(AppConstants.SPRING_MAIL_PASSWORD));
//			}
//		});
//
//
//		MimeMessage message = new MimeMessage(session);
//		MimeMessageHelper helper = new MimeMessageHelper(message, true);
//		helper.setFrom(env.getProperty(AppConstants.SPRING_MAIL_USERNAME));
//		helper.setTo(toMail);
//		helper.setSubject(subject);
//
//		/** Send message with html format **/
//		Multipart multipart = new MimeMultipart();
//		BodyPart htmlBodyPart = new MimeBodyPart();
//
//		if(filename!=null) {
//			try {
//				MimeBodyPart attachPart = new MimeBodyPart();
//				Resource resource = resourceLoader.getResource("classpath:orderInvoice/"+filename);	 
//				DataSource source = new FileDataSource(resource.getFile().getPath());
//
//				attachPart.setDataHandler(new DataHandler(source));
//				attachPart.setFileName(new File(resource.getFile().getName()).getName());
//
//				multipart.addBodyPart(attachPart);
//			} catch (Exception e) {
//				loggers.info(AppConstants.TECHNICAL_ERROR,e);
//			}	
//		}
//
//		htmlBodyPart.setContent(emailcontent, AppConstants.TEXT_HTML);
//		multipart.addBodyPart(htmlBodyPart);
//		message.setContent(multipart);
//		Transport.send(message);
//		ismailSent = true;
//		loggers.info(AppConstants.MAIL_SENT_SUCCESS);
//		return ismailSent;
//
//	}

}
