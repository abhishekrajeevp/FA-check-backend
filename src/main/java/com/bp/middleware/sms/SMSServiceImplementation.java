package com.bp.middleware.sms;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.user.RequestModel;
import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.admin.AdminRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;

import java.net.URLEncoder;
import java.util.*;

@Service
public class SMSServiceImplementation implements SMSService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SMSRepository smsRepository;

	@Autowired
	private UserRepository repository;
	@Autowired
	private AdminRepository adminRepository;

	@Override
	public ResponseStructure addSMS(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<EntityModel> optional = repository.findById(model.getUserId());
			if (optional.isPresent()) {

				EntityModel entity = optional.get();

				if (entity.isSmsPresent()) {

					SMSEntity smsEntity = new SMSEntity();

					smsEntity.setEntityModel(entity);
					smsEntity.setSmsTempCode(model.getSmsTempCode());
					smsEntity.setSmsTempMessage(model.getSmsTempMessage());
					smsEntity.setSmsTempDescription(model.getSmsTempDescription());
					smsEntity.setSmsTempStatus(true);
					smsEntity.setSmsEntityId(model.getSmsEntityId());
					smsEntity.setSmsTemplateId(model.getSmsTemplateId());
					smsEntity.setSmsServiceUrl(model.getSmsServiceUrl());
					smsEntity.setSmsUserName(model.getSmsUserName());
					smsEntity.setSmsPassword(model.getSmsPassword());
					smsEntity.setSmsEnabled("ON");

					smsRepository.save(smsEntity);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage(AppConstants.SUCCESS);
					structure.setFlag(1);
					structure.setData(smsEntity);

				} else {

					structure.setMessage("ONLY DEFAULT SMS CAN BE USED FOR THIS ENTITY.");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(2);
					structure.setData(null);
				}

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(3);
				structure.setData(null);
			}

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure createSMSByAdmin(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<AdminDto> optional = adminRepository.findById(model.getAdminId());

			if (optional.isPresent()) {

				AdminDto entity = optional.get();

				SMSEntity smsEntity = new SMSEntity();

				smsEntity.setAdmin(entity);
				smsEntity.setSmsTempCode(model.getSmsTempCode());
				smsEntity.setSmsTempMessage(model.getSmsTempMessage());
				smsEntity.setSmsTempDescription(model.getSmsTempDescription());
				smsEntity.setSmsTempStatus(true);
				smsEntity.setSmsEntityId(model.getSmsEntityId());
				smsEntity.setSmsTemplateId(model.getSmsTemplateId());
				smsEntity.setSmsServiceUrl(model.getSmsServiceUrl());
				smsEntity.setSmsUserName(model.getSmsUserName());
				smsEntity.setSmsPassword(model.getSmsPassword());
				smsEntity.setSmsEnabled("ON");

				smsRepository.save(smsEntity);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("COMMON SMS CREATED BY ADMIN SUCCESSFULLY");
				structure.setFlag(1);
				structure.setData(smsEntity);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(3);
				structure.setData(null);
			}

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure addGetByTemplateId(int smsId) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<SMSEntity> optional = smsRepository.findById(smsId);
			if (optional.isPresent()) {
				SMSEntity entity = optional.get();
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setData(entity);
				structure.setStatusCode(HttpStatus.OK.value());
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setData(null);
				structure.setStatusCode(HttpStatus.OK.value());
			}
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure listAll() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<SMSEntity> list = smsRepository.findAll();
			if (!list.isEmpty()) {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.SUCCESS);
				structure.setData(list);
				structure.setFlag(1);
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setData(null);
			}
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure updateDetails(int smsId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<SMSEntity> optional = smsRepository.findById(smsId);
			if (optional.isPresent()) {

				SMSEntity entity = optional.get();

//				entity.setEntityModel(entity.getEntityModel());
				entity.setSmsTempCode(model.getSmsTempCode());
				entity.setSmsTempMessage(model.getSmsTempMessage());
				entity.setSmsTempDescription(model.getSmsTempDescription());
				entity.setSmsEntityId(model.getSmsEntityId());
				entity.setSmsTemplateId(model.getSmsTemplateId());
				entity.setSmsServiceUrl(model.getSmsServiceUrl());
				entity.setSmsUserName(model.getSmsUserName());
				entity.setSmsPassword(model.getSmsPassword());
				entity.setSmsEnabled("ON");
				entity.setSmsModifiedDate(new Date());
				entity.setSmsModifiedBy(model.getSmsModifiedBy());

				smsRepository.save(entity);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.SUCCESS);
				structure.setData(entity);
				structure.setFlag(1);
				
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setData(null);
			}
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure changeTemplateStatus(int smsId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<SMSEntity> optional = smsRepository.findById(smsId);
			if (optional.isPresent()) {
				SMSEntity entity = optional.get();
				entity.setSmsTempStatus(model.isSmsTempStatus());
				smsRepository.save(entity);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.SUCCESS);
				structure.setData(entity);
				structure.setFlag(1);
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setData(null);
			}
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure viewByAdmin(int adminId) {

		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<AdminDto> optional = adminRepository.findById(adminId);
			if (optional.isPresent()) {

				AdminDto admin = optional.get();

				List<SMSEntity> smsEntity = smsRepository.getByAdmin(admin);

				if (!smsEntity.isEmpty()) {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage(AppConstants.SUCCESS);
					structure.setData(smsEntity);
					structure.setFlag(1);
				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setFlag(2);
					structure.setData(null);
				}
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(3);
				structure.setData(null);

			}
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure getTemplateByUser(int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<EntityModel> optional = repository.findById(userId);
			if (optional.isPresent()) {

				EntityModel entity = optional.get();

				List<SMSEntity> smsEntity = smsRepository.getByEntityModel(entity);
				if (!smsEntity.isEmpty()) {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage(AppConstants.SUCCESS);
					structure.setData(smsEntity);
					structure.setFlag(1);
				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setFlag(2);
					structure.setData(null);
				}
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(3);
				structure.setData(null);

			}
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure deleteById(int smsId) {
		ResponseStructure structure = new ResponseStructure();
		try {
			smsRepository.deleteById(smsId);

			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(smsId);
			structure.setFlag(1);
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public boolean sendSMSNotification(String[] smsObj, String receiverMobileNumber, String smsTempCode,
			String smsServiceUrl, String smsUserName, String smsPassword, String smsEnabled) {
		boolean isSMSContent = false;

		try {

			System.err.println("smsTempCode: " + smsTempCode);

			List<SMSEntity> smsTemplateObj = smsRepository.findBySmsTempCode(smsTempCode);

			System.err.println("mess: " + smsTemplateObj.get(0).getSmsTempMessage());
			System.err.println("appro: " + smsTemplateObj.get(0).isSmsTempStatus());
			System.err.println("entityId: " + smsTemplateObj.get(0).getSmsEntityId());
			System.err.println("tempId: " + smsTemplateObj.get(0).getSmsTempId());

			// Checking smsTemplateObj is any template id available. If available send SMS
			// otherwise throw info message.
			if (!smsTemplateObj.isEmpty()) {
				String smsFinalContent = null;
				boolean isTemplateApproved = false;
				String entityId = null;
				String templateId = null;

				for (SMSEntity smsContent : smsTemplateObj) {
					System.err.println("281");
					smsFinalContent = smsContent.getSmsTempMessage();
					isTemplateApproved = smsContent.isSmsTempStatus();
					entityId = smsContent.getSmsEntityId();
					templateId = smsContent.getSmsTemplateId();

				}

				// If SMS Template approved then trigger SMS.
				if (isTemplateApproved) {
					// Check smsObj contains SMS parameters has provided. If not send SMS without
					// parameters.
					if (smsObj.length > 0) {
						System.err.println("293");
						int index = 0;
						for (String smsParam : smsObj) {
							System.err.println("297");
							smsFinalContent = smsFinalContent.replace("{" + index + "}", smsParam);
							index++;
						}

						System.err.println("302");
					}
					// Call SMSIntegra code to send sms with final replaces message with mobile
					// number.
					System.err.println("304");
					RequestModel dto = new RequestModel();
					dto.setRecieverMessage(smsFinalContent);
					dto.setReceiverMobileNumber(receiverMobileNumber);
					dto.setEntityId(entityId);
					dto.setTemplateId(templateId);
					dto.setSmsUrl(smsServiceUrl);
					dto.setSmsUser(smsUserName);
					dto.setSmsPwd(smsPassword);
					dto.setSmsEnabled(smsEnabled);
					System.err.println(316);
					isSMSContent = sendSMS(dto);
					System.err.println("341");
				}
			}
		} catch (Exception e) {
			logger.info(AppConstants.TECHNICAL_ERROR, e);
		}
		return isSMSContent;
	}

	/**
	 * The following method used send SMS from RMLCONNECT-ROUTE MOBILE
	 * 
	 * @param recieverMessage
	 * @param receiverMobileNumber
	 * @param entityId
	 * @param templateId
	 * @return
	 */

	@SuppressWarnings("deprecation")
	private boolean sendSMS(RequestModel dto) {
		boolean isSMSSent = false;
		StringBuilder urlParameters = null;
		try {

			if (dto.getSmsEnabled().equals("ON"))

			{

				System.err.println("1 : " + dto.getSmsUrl());
				System.err.println("2 : " + dto.getSmsUser());
				System.err.println("3 : " + dto.getSmsPwd());
				System.err.println("4 : " + dto.getReceiverMobileNumber());
				System.err.println("5 : " + dto.getRecieverMessage());
				System.err.println("6 : " + dto.getEntityId());
				System.err.println("7 : " + dto.getTemplateId());

				HttpClient client = new HttpClient();
				urlParameters = new StringBuilder().append(dto.getSmsUrl()).append("username=").append(dto.getSmsUser())
						.append("&").append("password=").append(dto.getSmsPwd()).append("&").append("type=").append("0")
						.append("&").append("dlr=").append("1").append("&").append("destination=")
						.append(dto.getReceiverMobileNumber()).append("&").append("source=").append("CINCHF")
						.append("&").append("message=").append(URLEncoder.encode(dto.getRecieverMessage())).append("&")
						.append("entityid=").append(dto.getEntityId()).append("&").append("tempid=")
						.append(dto.getTemplateId());
				PostMethod method = new PostMethod(urlParameters.toString());

				System.err.println("URL PARA : " + urlParameters.toString());

				client.executeMethod(method);
				System.out.println(urlParameters);
				isSMSSent = true;
				logger.info("SMS has been sent successfully!");
			} else {
				logger.info(
						"Sorry! SMS configuration not enabled..Please update ON to enable sms in properties files.");
			}
		} catch (Exception e) {
			logger.error("General exception occurent in SMSServieImpl, SendSMS Method", e);
		}
		return isSMSSent;
	}

	@Override
	public ResponseStructure deleteAll() {
		ResponseStructure structure = new ResponseStructure();
		try {

			smsRepository.deleteAll();

			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData("ALL SMS DETAILS DELETED");
			structure.setFlag(1);
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure truncate() {

		ResponseStructure structure = new ResponseStructure();
		try {

//			smsRepository.truncateAll();

			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData("ALL SMS DETAILS TRUNCATED SUCCESSFULLY");
			structure.setFlag(1);
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure allAdminSms() {
		
		ResponseStructure structure = new ResponseStructure();
		try {
			
			List<SMSEntity> adminSmsList = smsRepository.allAdminSms();

			if(!adminSmsList.isEmpty()) {
				
				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(adminSmsList);
				structure.setFlag(1);
				
			}else {
				
				structure.setMessage("No Active Admin SMS found");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}
}
