package com.bp.middleware.emailservice;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface SmtpMailService {

	ResponseStructure addSmptMailConfiguration(RequestModel model);

	ResponseStructure getByMailId(int mailId);

	ResponseStructure viewAll();

	ResponseStructure updateEamil(RequestModel model, int mailId);

	ResponseStructure viewByUser(int userid);

}
