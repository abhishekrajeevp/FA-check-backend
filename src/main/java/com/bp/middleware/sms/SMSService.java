package com.bp.middleware.sms;

import com.bp.middleware.user.RequestModel;
import com.bp.middleware.responsestructure.ResponseStructure;

public interface SMSService {

	ResponseStructure addSMS(RequestModel model);

	ResponseStructure addGetByTemplateId(int smsId);

	ResponseStructure listAll();

	ResponseStructure updateDetails(int smsId, RequestModel model);

	ResponseStructure changeTemplateStatus(int smsId, RequestModel model);

	ResponseStructure getTemplateByUser(int userId);

	ResponseStructure deleteById(int smsId);
	
	public boolean sendSMSNotification(String[] smsObj, String receiverMobileNumber,String smsTempCode ,String smsServiceUrl,String smsUserName,String smsPassword,String smsEnabled);

	ResponseStructure createSMSByAdmin(RequestModel model);

	ResponseStructure viewByAdmin(int adminId);

	ResponseStructure deleteAll();

	ResponseStructure truncate();

	ResponseStructure allAdminSms();

}
