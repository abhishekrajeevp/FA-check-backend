package com.bp.middleware.emailservice;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.admin.AdminRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;

@Service
public class SmtpMailServiceImpl implements SmtpMailService {

	@Autowired
	private SmtpMailRepository repository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AdminRepository adminRepository;

	@Override
	public ResponseStructure addSmptMailConfiguration(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<EntityModel> optional = userRepository.findById(model.getUserId());
			if (optional.isPresent()) {
				EntityModel entity = optional.get();

				SmtpMailConfiguration mail = new SmtpMailConfiguration();

				if (entity.isMailPresent()) {

					mail.setHost(model.getHost());
					mail.setPort(model.getPort());
					mail.setMailUserName(model.getMailUserName());
					mail.setMailPassword(model.getMailPassword());
					mail.setCreatedBy(model.getCreatedBy());
					mail.setCreatedDate(new Date());
					mail.setEntity(entity);

					mail.setProtocol("smtp");
					mail.setSocketFactoryPort(model.getPort());
					mail.setSmtpPort(model.getPort());
					mail.setSmtpAuth("true");
					mail.setSmtpConnectionTimeOut("5000");
					mail.setStarttlsEnable("true");
					mail.setSocketFactoryClass("javax.net.ssl.SSLSocketFactory");
					mail.setSmtpTimeOut("5000");
					mail.setSmtpWriteTimeOut("5000");

					repository.save(mail);

				} else {

					structure.setMessage("ONLY DEFAULT MAIL CAN BE USED FOR THIS ENTITY.");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(2);
					structure.setData(mail);
				}

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(mail);
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(3);
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

	@Override
	public ResponseStructure getByMailId(int mailId) {

		ResponseStructure structure = new ResponseStructure();
		Optional<SmtpMailConfiguration> optional = repository.findById(mailId);
		if (optional.isPresent()) {
			SmtpMailConfiguration configuration = optional.get();

			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setData(configuration);
		} else {
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
			structure.setData(null);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		return structure;
	}

	@Override
	public ResponseStructure viewAll() {

		ResponseStructure structure = new ResponseStructure();
		List<SmtpMailConfiguration> list = repository.findAll();
		if (!list.isEmpty()) {

			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setData(list);
		} else {
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
			structure.setData(null);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		return structure;
	}

	@Override
	public ResponseStructure updateEamil(RequestModel model, int mailId) {
		ResponseStructure structure = new ResponseStructure();

		Optional<SmtpMailConfiguration> mailPresent = repository.findById(mailId);
		if (mailPresent.isPresent()) {

			SmtpMailConfiguration mail = mailPresent.get();

			mail.setHost(model.getHost());
			mail.setPort(model.getPort());
			mail.setMailUserName(model.getMailUserName());
			mail.setMailPassword(model.getMailPassword());
			mail.setModifyBy(model.getModifyBy());
			mail.setModifyDate(new Date());

			mail.setProtocol("smtp");
			mail.setSocketFactoryPort(model.getPort());
			mail.setSmtpPort(model.getPort());
			mail.setSmtpAuth("true");
			mail.setSmtpConnectionTimeOut("5000");
			mail.setStarttlsEnable("true");
			mail.setSocketFactoryClass("javax.net.ssl.SSLSocketFactory");
			mail.setSmtpTimeOut("5000");
			mail.setSmtpWriteTimeOut("5000");

			repository.save(mail);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(mail);
			structure.setFlag(1);
		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Details updation Failed...!");
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseStructure viewByUser(int userid) {

		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<EntityModel> optional = userRepository.findById(userid);

			if (optional.isPresent()) {
				EntityModel entity = optional.get();

				Optional<SmtpMailConfiguration> mailUserOpt = repository.findByEntity(entity);

				if (mailUserOpt.isPresent()) {

					SmtpMailConfiguration smtpMailConfiguration = mailUserOpt.get();

					structure.setMessage(AppConstants.SUCCESS);
					structure.setFlag(1);
					structure.setData(smtpMailConfiguration);

				} else {

					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setFlag(2);
					structure.setData(null);
				}
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(3);
				structure.setData(null);
			}
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

}
