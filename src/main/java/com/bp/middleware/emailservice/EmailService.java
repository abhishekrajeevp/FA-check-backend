package com.bp.middleware.emailservice;

import com.bp.middleware.prepaidmonthlyinvoice.PrepaidMonthlyInvoice;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.signers.SignerModel;
import com.bp.middleware.signmerchant.MerchantModel;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;

import jakarta.mail.MessagingException;

public interface EmailService {
	
	public boolean sendEmailOTPVerification(String receipientEmail, String userName, String mobileNumber,String host, String smtpauth,String secretKey) throws Exception;
	
	public boolean sendCustomerVerifyEmail(String email, String otpCode, EntityModel entityModel) throws Exception;

	public boolean sendCustomerEmailVerify(String email, String name, String mobileNumber, String host, String smtpAuth,
			String smtpConnectionTimeOut, String starttlsEnable, String socketFactoryClass, String mailUserName,
			String mailPassword, String port, String smtpPort, String socketFactoryPort, String name2, String password,
			String string) throws Exception;
	
	public boolean sendEmailVerify(String email, String otp, SmtpMailConfiguration configuration) throws Exception;

	public boolean sendMemberAddition(String email, String name, String mobileNumber, String membershipNumber,
			String libraryName, String string) throws Exception;
	
	public boolean sendEmailAdminOTPVerification(String receipientEmail, String userName, String mobileNumber,String host, String smtpauth) throws Exception;
	
	
	public boolean sendMonthlyReminderMail(EntityModel user,PostpaidPayment postpaid, String month,String graceDate) throws Exception;
	
	public boolean sendGraceInvoice(EntityModel user,PostpaidPayment postpaid, String month,String graceDate,RequestModel model) throws Exception;
	
	
	///// COMMONLY USED
	
	public boolean sendEmailOTPVerificationForUser(String receipientEmail, String userName, String mobileNumber,
			String securityCode, String loginURL, String secretKey, EntityModel user) throws Exception;

	
	public boolean signerRequest(String merchantId, String signerName, String merchantCompanyName, String signerMobile, String signerEmail, String signerId ,EntityModel entity) throws Exception;

	public boolean finishedReport(MerchantModel merchantModel, SignerModel signer,EntityModel entityModel) throws Exception;

	public boolean prepaidPaymentSuccessMail(String string, TransactionDto transactionDto,PrepaidPayment prepaid) throws Exception;
	
//	public boolean prepaidInvoiceGenerateMail(String string,PrepaidMonthlyInvoice monthlyInvo) throws Exception;

	public boolean postpaidPaymentSuccessMail(String string, TransactionDto transactionDto, PostpaidPayment postpaid)throws Exception;

	public boolean sendMerchantPriceUpdationMail(EntityModel entityModel)throws Exception;

	public boolean priceAcceptedMailToAdmin(String email, String string) throws Exception;

	public boolean priceAcceptedMailToEntity(String email, String name)throws Exception;

	public boolean prepaidInvoiceGenerateMail(EntityModel entityModel, String startDate, String endDate, String month)throws Exception;
	
}



