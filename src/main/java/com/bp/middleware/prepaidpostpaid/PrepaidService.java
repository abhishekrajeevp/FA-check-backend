package com.bp.middleware.prepaidpostpaid;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

import jakarta.servlet.http.HttpServletRequest;

public interface PrepaidService {

	ResponseStructure addPrepaidDetails(RequestModel model);

	ResponseStructure getPrepaidDetailsById(int prepaidId);

	ResponseStructure getAllPrepaidDetails();

	ResponseStructure getPrepaidDetailsForUserId(int userId);

	ResponseStructure lastRechargedDetailsForPrepaidUser(int userId);

	ResponseEntity<Resource> viewPrepaidReciept(int prepaidId, HttpServletRequest request);

	ResponseStructure viewByNumberOfDays(int noOfDays);

	ResponseEntity<Resource> viewPrepaidInvoice(int prepaidId, HttpServletRequest request);

	ResponseEntity<Resource> viewPrepaidInvoiceTwo(int prepaidId, HttpServletRequest request);

	ResponseEntity<Resource> viewPrepaidInvoiceThree(int prepaidId, HttpServletRequest request);

	ResponseStructure viewByMonth(RequestModel model);

	ResponseStructure viewByUniqueId(String uniqueId);

}
