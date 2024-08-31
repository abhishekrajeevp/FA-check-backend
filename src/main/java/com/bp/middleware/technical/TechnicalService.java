package com.bp.middleware.technical;

import java.io.File;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface TechnicalService {

	ResponseStructure addTechnicalDetails(RequestModel model);

	ResponseStructure viewByTechnicalId(int techId);

	ResponseStructure viewAllTechnical();

	ResponseStructure updateTechnical(RequestModel model);

	ResponseStructure invoiceTesting( );
	
	ResponseEntity<Resource> getViewInvoicePdf(RequestModel model, HttpServletRequest request);

	ResponseStructure ipAddress();

	ResponseStructure justCheckSprintOkHttp(RequestModel model);

	ResponseStructure justCheckSprintHttpNet(RequestModel model);

	ResponseStructure justCheckSprintOurMethod(RequestModel model);

	ResponseStructure justCheckSprintUnirest(RequestModel model);

	ResponseStructure justCheckSprintVJavaNetHttp();

	ResponseStructure justCheck(RequestModel model);

	ResponseStructure vendorByName(RequestModel model);

	ResponseStructure clientIpAddress(HttpServletRequest request, HttpServletResponse response);

	ResponseStructure agreementCheck();

	ResponseStructure base64ToPdf(RequestModel model);

	ResponseStructure applicationId(RequestModel model);

	ResponseStructure aadhaarOcr(File file, HttpServletRequest servletRequest);

	ResponseStructure thymeLeaf();

	ResponseStructure jsonObjReturn();

	ResponseStructure thymeLeafTamil();

	ResponseStructure thymeLeafTamilItextCheck();

	ResponseStructure exceptionCheck();

	ResponseStructure byteImage(MultipartFile image);

	ResponseStructure amountToWords(int amount);

	ResponseStructure mailTemplateCheck(int id);

	ResponseStructure mailTemplateCheckPostpaid(int id);

	ResponseStructure invoiceCheck();

	ResponseStructure jsonView(int id);

	ResponseStructure getPublicIpAddress();

	ResponseStructure getGps();

	ResponseStructure diffTwoDates();

	ResponseStructure specialCharacterCheck(RequestModel model);

	ResponseStructure schedulerTrigger1(int id);

	ResponseStructure postpaidFlagChange(int id);

	ResponseStructure conveInvoiceCheck();

	ResponseStructure listCheck(int num);


}
