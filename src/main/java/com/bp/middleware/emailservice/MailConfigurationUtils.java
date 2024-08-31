package com.bp.middleware.emailservice;

import java.io.File;
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
import org.springframework.stereotype.Component;

import com.bp.middleware.emailserviceadmin.EmailAdmin;
import com.bp.middleware.emailserviceadmin.EmailAdminRepository;
import com.bp.middleware.prepaidmonthlyinvoice.PrepaidMonthlyInvoice;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.signmerchant.MerchantModel;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.util.AppConstants;

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

@Component
public class MailConfigurationUtils {

	private final Logger loggers = LoggerFactory.getLogger(this.getClass());

	JavaMailSender emailSender;

	@Autowired
	private Environment env;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private SmtpMailRepository smtpMailRepository;
	@Autowired
	private EmailAdminRepository emailAdminRepository;

	public boolean sendEmail(String toMail, String subject, String emailcontent, String filename) throws Exception {

		boolean ismailSent = false;

		Session session = mailConfiguration();

		MimeMessage message = new MimeMessage(session);
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom(env.getProperty(AppConstants.SPRING_MAIL_USERNAME));
		helper.setTo(toMail);
		helper.setSubject(subject);

		/** Send message with html format **/
		Multipart multipart = new MimeMultipart();
		BodyPart htmlBodyPart = new MimeBodyPart();

		if (filename != null) {
			try {
				MimeBodyPart attachPart = new MimeBodyPart();
				DataSource source = new FileDataSource(filename);

				attachPart.setDataHandler(new DataHandler(source));
				attachPart.setFileName(new File(filename).getName());

				multipart.addBodyPart(attachPart);

			} catch (Exception e) {
				loggers.info(AppConstants.TECHNICAL_ERROR, e);
			}
		}

		htmlBodyPart.setContent(emailcontent, AppConstants.TEXT_HTML);
		multipart.addBodyPart(htmlBodyPart);
		message.setContent(multipart);
		Transport.send(message);
		ismailSent = true;
		loggers.info(AppConstants.MAIL_SENT_SUCCESS);
		return ismailSent;

	}

	public boolean sendGraceInvoice(String toMail, String subject, String emailcontent, PostpaidPayment postpaid)
			throws Exception {

		System.err.println("MAIL 2");

		boolean ismailSent = false;
		Session session = mailConfiguration();

		MimeMessage message = new MimeMessage(session);
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom(env.getProperty(AppConstants.SPRING_MAIL_USERNAME));
		helper.setTo(toMail);
		helper.setSubject(subject);

		/** Send message with html format **/
		Multipart multipart = new MimeMultipart();
		BodyPart htmlBodyPart = new MimeBodyPart();

		if (postpaid != null && postpaid.getGraceInvoice() != null) {

			try {

				MimeBodyPart attachPart2 = new MimeBodyPart();

				Resource resource2 = resourceLoader.getResource("/WEB-INF/graceInvoice/" + postpaid.getGraceInvoice());
				DataSource source2 = new FileDataSource(resource2.getFile().getPath());
				attachPart2.setDataHandler(new DataHandler(source2));
				attachPart2.setFileName(new File(resource2.getFile().getName()).getName());

				multipart.addBodyPart(attachPart2);

			} catch (Exception e) {
				loggers.info(AppConstants.TECHNICAL_ERROR, e);
			}
		}

		htmlBodyPart.setContent(emailcontent, AppConstants.TEXT_HTML);
		multipart.addBodyPart(htmlBodyPart);
		message.setContent(multipart);
		Transport.send(message);
		ismailSent = true;
		loggers.info(AppConstants.MAIL_SENT_SUCCESS);
		return ismailSent;

	}

	public boolean sendEmailWithTwoAttachment(String toMail, String subject, String emailcontent,
			PrepaidPayment prepaid, PostpaidPayment postpaid) throws Exception {

		System.err.println("MAIL 2");

		boolean ismailSent = false;
		Session session = mailConfiguration();

		MimeMessage message = new MimeMessage(session);
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom(env.getProperty(AppConstants.SPRING_MAIL_USERNAME));
		helper.setTo(toMail);
		helper.setSubject(subject);

		/** Send message with html format **/
		Multipart multipart = new MimeMultipart();
		BodyPart htmlBodyPart = new MimeBodyPart();

		System.err.println(prepaid != null);

		if (prepaid != null) {

			try {

				if (prepaid.getInvoice() != null && prepaid.getConveInvoGeneratedDate().isEqual(LocalDate.now())) {

					MimeBodyPart attachPart2 = new MimeBodyPart();

					Resource resource2 = resourceLoader
							.getResource("/WEB-INF/PrepaidConveInvoice/" + prepaid.getInvoice());
					DataSource source2 = new FileDataSource(resource2.getFile().getPath());
					attachPart2.setDataHandler(new DataHandler(source2));
					attachPart2.setFileName(new File(resource2.getFile().getName()).getName());

					multipart.addBodyPart(attachPart2);

				}

				if (prepaid.getReceipt() != null && prepaid.getReceiptGeneratedDate().isEqual(LocalDate.now())) {

					MimeBodyPart attachPart = new MimeBodyPart();

					Resource resource = resourceLoader.getResource("/WEB-INF/receipt/" + prepaid.getReceipt());
					DataSource source = new FileDataSource(resource.getFile().getPath());
					attachPart.setDataHandler(new DataHandler(source));
					attachPart.setFileName(new File(resource.getFile().getName()).getName());

					multipart.addBodyPart(attachPart);
				}

			} catch (Exception e) {
				loggers.info(AppConstants.TECHNICAL_ERROR, e);
			}
		} else if (postpaid != null && postpaid.getReceipt() != null && postpaid.getReceiptGeneratedDate().isEqual(LocalDate.now())) {

			try {

				MimeBodyPart attachPart = new MimeBodyPart();

				Resource resource = resourceLoader.getResource("/WEB-INF/receipt/" + prepaid.getReceipt());
				DataSource source = new FileDataSource(resource.getFile().getPath());
				attachPart.setDataHandler(new DataHandler(source));
				attachPart.setFileName(new File(resource.getFile().getName()).getName());

				multipart.addBodyPart(attachPart);

				if (postpaid.getConvInvoice()!=null && postpaid.getConveInvoGeneratedDate().isEqual(LocalDate.now())) {
					
					MimeBodyPart attachPart2 = new MimeBodyPart();

					Resource resource2 = resourceLoader.getResource("/WEB-INF/conveInvoice/" + postpaid.getConvInvoice());
					DataSource source2 = new FileDataSource(resource2.getFile().getPath());
					attachPart2.setDataHandler(new DataHandler(source2));
					attachPart2.setFileName(new File(resource2.getFile().getName()).getName());

					multipart.addBodyPart(attachPart2);
				}

			} catch (Exception e) {
				loggers.info(AppConstants.TECHNICAL_ERROR, e);
			}
		}

		htmlBodyPart.setContent(emailcontent, AppConstants.TEXT_HTML);
		multipart.addBodyPart(htmlBodyPart);
		message.setContent(multipart);
		Transport.send(message);
		ismailSent = true;
		loggers.info(AppConstants.MAIL_SENT_SUCCESS);
		return ismailSent;

	}

	public boolean sendEmailFromExternals(String toMail, String subject, String emailcontent, String filename,
			EntityModel user) throws Exception {

		boolean ismailSent = false;

		Optional<SmtpMailConfiguration> optional = smtpMailRepository.findByEntity(user);

		if (optional.isPresent()) {

			SmtpMailConfiguration mail = optional.get();

			Properties properties = System.getProperties();

			properties.put(AppConstants.MAIL_SMTP_HOST, mail.getHost());// env.getProperty(AppConstants.SPRING_MAIL_HOST)
			properties.put(AppConstants.MAIL_SMTP_PORT, mail.getPort());// env.getProperty(AppConstants.SPRING_MAIL_PORT)
			properties.put(AppConstants.MAIL_SMPT_AUTH, mail.getSmtpAuth()); // env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH)
			properties.put(AppConstants.MAIL_TRANSPORT_PROTOCOL, mail.getSmtpPort()); // env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_PORT)
			properties.put(AppConstants.MAIL_SMTP_CONNECTION_TIME_OUT, mail.getSmtpConnectionTimeOut()); // env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_CONNECTION_TIME_OUT)
			properties.put(AppConstants.MAIL_SMTP_STARRTTLS_ENABLE, mail.getStarttlsEnable()); // env.getProperty(AppConstants.SPRING_MAIL_PROPERTIE_MAIL_SMTP_STARRTTLS_ENABLE)
			properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_PORT, mail.getSocketFactoryPort()); // env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_SOCKETFACTORY_PORT)
			properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_CLASS, mail.getSocketFactoryClass()); // env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_SOCKETFACTORY_CLASS)

			Session session = Session.getInstance(properties, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(mail.getMailUserName(), mail.getMailPassword()); // env.USER NAME
																										// & PASSWORD
				}
			});

			MimeMessage message = new MimeMessage(session);
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(mail.getMailUserName()); // env.getProperty(AppConstants.SPRING_MAIL_USERNAME)
			helper.setTo(toMail);
			helper.setSubject(subject);

			/** Send message with html format **/
			Multipart multipart = new MimeMultipart();
			BodyPart htmlBodyPart = new MimeBodyPart();

			if (filename != null) {
				try {
					MimeBodyPart attachPart = new MimeBodyPart();
					DataSource source = new FileDataSource(filename);

					attachPart.setDataHandler(new DataHandler(source));
					attachPart.setFileName(new File(filename).getName());

					multipart.addBodyPart(attachPart);
				} catch (Exception e) {
					loggers.info(AppConstants.TECHNICAL_ERROR, e);
				}
			}

			htmlBodyPart.setContent(emailcontent, AppConstants.TEXT_HTML);
			multipart.addBodyPart(htmlBodyPart);
			message.setContent(multipart);
			Transport.send(message);
			ismailSent = true;
			loggers.info(AppConstants.MAIL_SENT_SUCCESS);

		}

		return ismailSent;

	}

	public boolean sendVerifyEmailFromExternal(String toMail, String subject, String emailcontent, String filename,
			EntityModel user) throws Exception {
		boolean ismailSent = false;

		Optional<SmtpMailConfiguration> optional = smtpMailRepository.findByEntity(user);

		if (optional.isPresent()) {

			SmtpMailConfiguration mail = optional.get();
			Properties properties = System.getProperties();

			properties.put(AppConstants.MAIL_SMTP_HOST, mail.getHost());
			properties.put(AppConstants.MAIL_SMTP_PORT, mail.getPort());
			properties.put(AppConstants.MAIL_SMPT_AUTH, mail.getSmtpAuth());
			properties.put(AppConstants.MAIL_TRANSPORT_PROTOCOL, mail.getSmtpPort());
			properties.put(AppConstants.MAIL_SMTP_CONNECTION_TIME_OUT, mail.getSmtpConnectionTimeOut());
			properties.put(AppConstants.MAIL_SMTP_STARRTTLS_ENABLE, mail.getStarttlsEnable());
			properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_PORT, mail.getSocketFactoryPort());
			properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_CLASS, mail.getSocketFactoryClass());

			Session session = Session.getInstance(properties, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(mail.getMailUserName(), mail.getMailPassword());
				}
			});

			MimeMessage message = new MimeMessage(session);
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(mail.getMailUserName());
			helper.setTo(toMail);
			helper.setSubject(subject);

			/** Send message with html format **/
			Multipart multipart = new MimeMultipart();
			BodyPart htmlBodyPart = new MimeBodyPart();

			if (filename != null) {
				try {
					MimeBodyPart attachPart = new MimeBodyPart();
					Resource resource = resourceLoader.getResource("classpath:orderInvoice/" + filename);
					DataSource source = new FileDataSource(resource.getFile().getPath());

					attachPart.setDataHandler(new DataHandler(source));
					attachPart.setFileName(new File(resource.getFile().getName()).getName());

					multipart.addBodyPart(attachPart);
				} catch (Exception e) {
					loggers.info(AppConstants.TECHNICAL_ERROR, e);
				}
			}

			htmlBodyPart.setContent(emailcontent, AppConstants.TEXT_HTML);
			multipart.addBodyPart(htmlBodyPart);
			message.setContent(multipart);
			Transport.send(message);
			ismailSent = true;
			loggers.info(AppConstants.MAIL_SENT_SUCCESS);
		}

		return ismailSent;
	}

	public boolean sendPassword2(String toMail, String host, String smtpauth, String smtpconnectiontimeout,
			String starttlsenable, String socketfactoryclass, String smtpfromaddress, String smtppassword, String port,
			String smtpport, String socketfactoryport, String subject, String emailcontent, String filename)
			throws MessagingException {

		boolean ismailSent = false;
		Properties properties = new Properties();
		properties.put(AppConstants.MAIL_SMTP_HOST, host);
		properties.put(AppConstants.MAIL_SMTP_PORT, port);
		properties.put(AppConstants.MAIL_SMPT_AUTH, smtpauth);
		properties.put(AppConstants.MAIL_TRANSPORT_PROTOCOL, smtpport);
		properties.put(AppConstants.MAIL_SMTP_CONNECTION_TIME_OUT, smtpconnectiontimeout);
		properties.put(AppConstants.MAIL_SMTP_STARRTTLS_ENABLE, starttlsenable);
		properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_PORT, socketfactoryport);
		properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_CLASS, socketfactoryclass);

		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(smtpfromaddress, smtppassword);
			}
		});

		MimeMessage message = new MimeMessage(session);
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom(smtpfromaddress);
		helper.setTo(toMail);
		// helper.setCc("care@cinchfuel.com");
		helper.setSubject(subject);
		// helper.setText(emailcontent);
		// Transport.send(message);

		/** Send message with html format **/
		Multipart multipart = new MimeMultipart();
		BodyPart htmlBodyPart = new MimeBodyPart();

		if (filename != null) {
			try {
				MimeBodyPart attachPart = new MimeBodyPart();
				Resource resource = resourceLoader.getResource("/WEB-INF/userMonthlyinvoice/" + filename);
				DataSource source = new FileDataSource(resource.getFile().getPath());

				attachPart.setDataHandler(new DataHandler(source));
				attachPart.setFileName(new File(resource.getFile().getName()).getName());

				multipart.addBodyPart(attachPart);
			} catch (Exception e) {
				loggers.info(AppConstants.TECHNICAL_ERROR, e);
			}
		}

		htmlBodyPart.setContent(emailcontent, AppConstants.TEXT_HTML);
		multipart.addBodyPart(htmlBodyPart);
		message.setContent(multipart);
		Transport.send(message);
		ismailSent = true;
		loggers.info(AppConstants.MAIL_SENT_SUCCESS);
		return ismailSent;

	}

	public boolean sendCustomerVerify(String email, String subject, String emailcontent, String filename,
			SmtpMailConfiguration configuration) throws MessagingException {

		boolean ismailSent = false;
		Properties properties = System.getProperties();
		properties.put(AppConstants.MAIL_SMTP_HOST, configuration.getHost());
		properties.put(AppConstants.MAIL_SMTP_PORT, configuration.getPort());
		properties.put(AppConstants.MAIL_SMPT_AUTH, configuration.getSmtpAuth());
		properties.put(AppConstants.MAIL_TRANSPORT_PROTOCOL, configuration.getSmtpPort());
		properties.put(AppConstants.MAIL_SMTP_CONNECTION_TIME_OUT, configuration.getSmtpConnectionTimeOut());
		properties.put(AppConstants.MAIL_SMTP_STARRTTLS_ENABLE, configuration.getStarttlsEnable());
		properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_PORT, configuration.getSocketFactoryPort());
		properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_CLASS, configuration.getSocketFactoryClass());

		Session session = Session.getInstance(properties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(configuration.getMailUserName(), configuration.getMailPassword());
			}
		});
		MimeMessage message = new MimeMessage(session);
		MimeMessageHelper helper = new MimeMessageHelper(message, true);

		helper.setFrom(configuration.getMailUserName());
		helper.setTo(email);
		helper.setSubject(subject);

		/** Send message with html format **/
		Multipart multipart = new MimeMultipart();
		BodyPart htmlBodyPart = new MimeBodyPart();

		if (filename != null) {
			try {
				MimeBodyPart attachPart = new MimeBodyPart();
				Resource resource = resourceLoader.getResource("classpath:orderInvoice/" + filename);
				DataSource source = new FileDataSource(resource.getFile().getPath());

				attachPart.setDataHandler(new DataHandler(source));
				attachPart.setFileName(new File(resource.getFile().getName()).getName());

				multipart.addBodyPart(attachPart);
			} catch (Exception e) {
				loggers.info(AppConstants.TECHNICAL_ERROR, e);
			}
		}

		htmlBodyPart.setContent(emailcontent, AppConstants.TEXT_HTML);
		multipart.addBodyPart(htmlBodyPart);
		message.setContent(multipart);
		Transport.send(message);
		ismailSent = true;
		loggers.info(AppConstants.MAIL_SENT_SUCCESS);
		return ismailSent;
	}

	public boolean sendFinishedEmail(String signerEmail, String emailVerification, String emailcontent, String filename,
			MerchantModel merchantModel) throws Exception {

		boolean ismailSent = false;

		Session session = mailConfiguration();

		MimeMessage message = new MimeMessage(session);
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom(env.getProperty(AppConstants.SPRING_MAIL_USERNAME));
		helper.setTo(signerEmail);
		helper.setSubject(emailVerification);
		Multipart multipart = new MimeMultipart();

		try {
			MimeBodyPart attachPart = new MimeBodyPart();
			Resource resource = resourceLoader.getResource("/WEB-INF/agreement/" + merchantModel.getPdfDocument());

			DataSource source = new FileDataSource(resource.getFile().getPath());

			attachPart.setDataHandler(new DataHandler(source));
			attachPart.setFileName(new File(resource.getFile().getName()).getName());

			multipart.addBodyPart(attachPart);
		} catch (Exception e) {
			loggers.info(AppConstants.TECHNICAL_ERROR, e);
		}

		/** Send message with html format **/

		BodyPart htmlBodyPart = new MimeBodyPart();

		htmlBodyPart.setContent(emailcontent, AppConstants.TEXT_HTML);
		multipart.addBodyPart(htmlBodyPart);
		message.setContent(multipart);
		Transport.send(message);
		ismailSent = true;
		loggers.info(AppConstants.MAIL_SENT_SUCCESS);
		return ismailSent;

	}

	public boolean sendFinishedEmailFromExternal(String signerEmail, String emailVerification, String emailcontent,
			String filename, MerchantModel merchantModel, EntityModel user) throws MessagingException {

		boolean ismailSent = false;

		Optional<SmtpMailConfiguration> optional = smtpMailRepository.findByEntity(user);

		if (optional.isPresent()) {

			SmtpMailConfiguration mail = optional.get();

			Properties properties = System.getProperties();

			properties.put(AppConstants.MAIL_SMTP_HOST, mail.getHost());
			properties.put(AppConstants.MAIL_SMTP_PORT, mail.getPort());
			properties.put(AppConstants.MAIL_SMPT_AUTH, mail.getSmtpAuth());
			properties.put(AppConstants.MAIL_TRANSPORT_PROTOCOL, mail.getSmtpPort());
			properties.put(AppConstants.MAIL_SMTP_CONNECTION_TIME_OUT, mail.getSmtpConnectionTimeOut());
			properties.put(AppConstants.MAIL_SMTP_STARRTTLS_ENABLE, mail.getStarttlsEnable());
			properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_PORT, mail.getSocketFactoryPort());
			properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_CLASS, mail.getSocketFactoryClass());

			Session session = Session.getInstance(properties, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(mail.getMailUserName(), mail.getMailPassword());
				}
			});

			MimeMessage message = new MimeMessage(session);
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(mail.getMailUserName());
			helper.setTo(signerEmail);
			helper.setSubject(emailVerification);
			Multipart multipart = new MimeMultipart();

			try {
				MimeBodyPart attachPart = new MimeBodyPart();
				Resource resource = resourceLoader.getResource("/WEB-INF/agreement/" + merchantModel.getPdfDocument());

				DataSource source = new FileDataSource(resource.getFile().getPath());

				attachPart.setDataHandler(new DataHandler(source));
				attachPart.setFileName(new File(resource.getFile().getName()).getName());

				multipart.addBodyPart(attachPart);
			} catch (Exception e) {
				loggers.info(AppConstants.TECHNICAL_ERROR, e);
			}

			/** Send message with html format **/

			BodyPart htmlBodyPart = new MimeBodyPart();

			htmlBodyPart.setContent(emailcontent, AppConstants.TEXT_HTML);
			multipart.addBodyPart(htmlBodyPart);
			message.setContent(multipart);
			Transport.send(message);
			ismailSent = true;
			loggers.info(AppConstants.MAIL_SENT_SUCCESS);

		}

		return ismailSent;

	}

	public boolean sendVerifyEmail(String toMail, String subject, String emailcontent, String filename)
			throws Exception {

		boolean ismailSent = false;

		Session session = mailConfiguration();

		MimeMessage message = new MimeMessage(session);
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom(env.getProperty(AppConstants.SPRING_MAIL_USERNAME));
		helper.setTo(toMail);
		helper.setSubject(subject);

		/** Send message with html format **/
		Multipart multipart = new MimeMultipart();
		BodyPart htmlBodyPart = new MimeBodyPart();

		if (filename != null) {
			try {
				MimeBodyPart attachPart = new MimeBodyPart();
				Resource resource = resourceLoader.getResource("classpath:orderInvoice/" + filename);
				DataSource source = new FileDataSource(resource.getFile().getPath());

				attachPart.setDataHandler(new DataHandler(source));
				attachPart.setFileName(new File(resource.getFile().getName()).getName());

				multipart.addBodyPart(attachPart);
			} catch (Exception e) {
				loggers.info(AppConstants.TECHNICAL_ERROR, e);
			}
		}

		htmlBodyPart.setContent(emailcontent, AppConstants.TEXT_HTML);
		multipart.addBodyPart(htmlBodyPart);
		message.setContent(multipart);
		Transport.send(message);
		ismailSent = true;
		loggers.info(AppConstants.MAIL_SENT_SUCCESS);
		return ismailSent;
	}

	public Session mailConfiguration() throws Exception {

		Properties properties = System.getProperties();

		EmailAdmin adminMail = emailAdminRepository.findByCurrentlyActive(true);

		if (adminMail != null) {

			properties.put(AppConstants.MAIL_SMTP_HOST, adminMail.getHost());
			properties.put(AppConstants.MAIL_SMTP_PORT, adminMail.getPort());
			properties.put(AppConstants.MAIL_SMPT_AUTH, adminMail.getSmtpAuth());
			properties.put(AppConstants.MAIL_TRANSPORT_PROTOCOL, adminMail.getSmtpPort());
			properties.put(AppConstants.MAIL_SMTP_CONNECTION_TIME_OUT, adminMail.getSmtpConnectionTimeOut());
			properties.put(AppConstants.MAIL_SMTP_STARRTTLS_ENABLE, adminMail.getStarttlsEnable());
			properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_PORT, adminMail.getSocketFactoryPort());
			properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_CLASS, adminMail.getSocketFactoryClass());

			properties.put(AppConstants.MAIL_SMTP_HOST, adminMail.getHost());
			properties.put(AppConstants.MAIL_SMTP_PORT, adminMail.getPort());
			properties.put(AppConstants.MAIL_SMPT_AUTH, adminMail.getSmtpAuth());
			properties.put(AppConstants.MAIL_TRANSPORT_PROTOCOL, adminMail.getSmtpPort());
			properties.put(AppConstants.MAIL_SMTP_CONNECTION_TIME_OUT, adminMail.getSmtpConnectionTimeOut());
			properties.put(AppConstants.MAIL_SMTP_STARRTTLS_ENABLE, adminMail.getStarttlsEnable());
			properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_PORT, adminMail.getSocketFactoryPort());
			properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_CLASS, adminMail.getSocketFactoryClass());

			return Session.getInstance(properties, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(adminMail.getMailUserName(), adminMail.getMailPassword());
				}
			});

		} else {

			properties.put(AppConstants.MAIL_SMTP_HOST, env.getProperty(AppConstants.SPRING_MAIL_HOST));
			properties.put(AppConstants.MAIL_SMTP_PORT, env.getProperty(AppConstants.SPRING_MAIL_PORT));
			properties.put(AppConstants.MAIL_SMPT_AUTH,
					env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH));
			properties.put(AppConstants.MAIL_TRANSPORT_PROTOCOL,
					env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_PORT));
			properties.put(AppConstants.MAIL_SMTP_CONNECTION_TIME_OUT,
					env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_CONNECTION_TIME_OUT));
			properties.put(AppConstants.MAIL_SMTP_STARRTTLS_ENABLE,
					env.getProperty(AppConstants.SPRING_MAIL_PROPERTIE_MAIL_SMTP_STARRTTLS_ENABLE));
			properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_PORT,
					env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_SOCKETFACTORY_PORT));
			properties.put(AppConstants.MAIL_SMTP_SOCKETFACTORY_CLASS,
					env.getProperty(AppConstants.SPRING_MAIL_PROPERTIES_MAIL_SMTP_SOCKETFACTORY_CLASS));

			return Session.getInstance(properties, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(env.getProperty(AppConstants.SPRING_MAIL_USERNAME),
							env.getProperty(AppConstants.SPRING_MAIL_PASSWORD));
				}
			});
		}
	}

	public boolean sendMonthlyWithOneAttachment(String toMail, String subject, String emailcontent,
			PrepaidMonthlyInvoice prepaid) throws Exception {

		System.err.println("MAIL 2");

		boolean ismailSent = false;
		Session session = mailConfiguration();

		MimeMessage message = new MimeMessage(session);
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setFrom(env.getProperty(AppConstants.SPRING_MAIL_USERNAME));
		helper.setTo(toMail);
		helper.setSubject(subject);

		/** Send message with html format **/
		Multipart multipart = new MimeMultipart();
		BodyPart htmlBodyPart = new MimeBodyPart();

		System.err.println(prepaid != null);

		if (prepaid != null) {

			try {
				System.err.println("1." + prepaid.getInvoice());

				if (prepaid.getInvoice() != null) {

					MimeBodyPart attachPart2 = new MimeBodyPart();

					Resource resource2 = resourceLoader
							.getResource("/WEB-INF/PrepaidMonthlyInvoice/" + prepaid.getInvoice());
					DataSource source2 = new FileDataSource(resource2.getFile().getPath());
					attachPart2.setDataHandler(new DataHandler(source2));
					attachPart2.setFileName(new File(resource2.getFile().getName()).getName());

					multipart.addBodyPart(attachPart2);
				}

			} catch (Exception e) {
				loggers.info(AppConstants.TECHNICAL_ERROR, e);
			}
		}

		htmlBodyPart.setContent(emailcontent, AppConstants.TEXT_HTML);
		multipart.addBodyPart(htmlBodyPart);
		message.setContent(multipart);
		Transport.send(message);
		ismailSent = true;
		loggers.info(AppConstants.MAIL_SENT_SUCCESS);
		return ismailSent;

	}

}
