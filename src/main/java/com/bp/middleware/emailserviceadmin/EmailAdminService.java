package com.bp.middleware.emailserviceadmin;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Admin;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.admin.AdminRepository;
import com.bp.middleware.emailservice.SmtpMailConfiguration;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@Service
public class EmailAdminService {

	@Autowired
	private EmailAdminRepository emailAdminRepository;
	@Autowired
	private AdminRepository adminRepository;

	public ResponseStructure addMail(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<AdminDto> optional = adminRepository.findById(model.getAdminId());
			if (optional.isPresent()) {

				AdminDto admin = optional.get();

				EmailAdmin mail = new EmailAdmin();

				mail.setHost(model.getHost());
				mail.setPort(model.getPort());
				mail.setMailUserName(model.getMailUserName());
				mail.setMailPassword(model.getMailPassword());
				mail.setCreatedBy(model.getCreatedBy());
				mail.setCreatedDate(new Date());
				mail.setAdmin(admin);
				mail.setCurrentlyActive(false);

				mail.setProtocol("smtp");
				mail.setSocketFactoryPort(model.getPort());
				mail.setSmtpPort(model.getPort());
				mail.setSmtpAuth("true");
				mail.setSmtpConnectionTimeOut(model.getSmtpConnectionTimeOut());
				mail.setStarttlsEnable("true");
				mail.setSocketFactoryClass("javax.net.ssl.SSLSocketFactory");
				mail.setSmtpTimeOut(model.getSmtpConnectionTimeOut());
				mail.setSmtpWriteTimeOut(model.getSmtpConnectionTimeOut());

				emailAdminRepository.save(mail);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(mail);

			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	public ResponseStructure activateOtherMail(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EmailAdmin> opt = emailAdminRepository.findById(model.getAdminMailId());

			if (opt.isPresent() && model.isCurrentlyActive()) {

				EmailAdmin emailAdmin = opt.get();

				List<EmailAdmin> allMail = emailAdminRepository.findAll();

				for (EmailAdmin mail : allMail) {

					mail.setCurrentlyActive(false);
				}
				emailAdminRepository.saveAll(allMail);
				
				emailAdmin.setCurrentlyActive(true);
				
				emailAdminRepository.save(emailAdmin);
				
				structure.setMessage("Mail activation success");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(emailAdmin);

			} else {

				if (opt.isEmpty()) {
					structure.setMessage(AppConstants.NO_DATA_FOUND);
				} else {
					structure.setMessage("Can't inactive mail directly");
				}

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(3);
				structure.setData(null);
			}

		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;

	}

	public ResponseStructure viewAll() {

		ResponseStructure structure = new ResponseStructure();
		try {
			
			List<EmailAdmin> allMail = emailAdminRepository.findAll();
			
			if(!allMail.isEmpty()) {
				
				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(allMail);
				
			}else {
				
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
			}
			
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;

	}

	
	public ResponseStructure viewById(int id) {

		ResponseStructure structure = new ResponseStructure();
		try {
			
			Optional<EmailAdmin> opt = emailAdminRepository.findById(id);
			
			if(opt.isPresent()) {
				
				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(opt.get());
				
			}else {
				
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
			}
			
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;

	}

	public ResponseStructure viewCurrentlyActive() {

		ResponseStructure structure = new ResponseStructure();
		try {
			
			EmailAdmin currentlyActive = emailAdminRepository.findByCurrentlyActive(true);
			
			if(currentlyActive!=null) {
				
				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(currentlyActive);
				
			}else {
				
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
			}
			
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;

	}

	public ResponseStructure update(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EmailAdmin> opt = emailAdminRepository.findById(model.getAdminMailId());

			if (opt.isPresent() ) {
				
				EmailAdmin mail = opt.get();

				mail.setHost(model.getHost());
				mail.setPort(model.getPort());
				mail.setMailUserName(model.getMailUserName());
				mail.setMailPassword(model.getMailPassword());
				mail.setModifyBy(model.getModifiedBy());
				mail.setModifyDate(new Date());
				mail.setProtocol("smtp");
				mail.setSocketFactoryPort(model.getPort());
				mail.setSmtpPort(model.getPort());
				mail.setSmtpAuth("true");
				mail.setSmtpConnectionTimeOut(model.getSmtpConnectionTimeOut());
				mail.setStarttlsEnable("true");
				mail.setSocketFactoryClass("javax.net.ssl.SSLSocketFactory");
				mail.setSmtpTimeOut(model.getSmtpConnectionTimeOut());
				mail.setSmtpWriteTimeOut(model.getSmtpConnectionTimeOut());

				emailAdminRepository.save(mail);

				structure.setMessage("Updated Successfully");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(mail);
				
			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(3);
				structure.setData(null);
			}

		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;

	}

}
