package com.bp.middleware.prepaidpostpaid;

import java.text.ParseException;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

import jakarta.servlet.http.HttpServletRequest;

public interface PostpaidService {

	ResponseStructure addPostpaidDetails(RequestModel model);

	ResponseStructure getPostpaidDetailsById(int postpaidId);

	ResponseStructure getAllPostpaidDetails();

	ResponseStructure updatePostpaidDetails(RequestModel model);

	ResponseStructure totalHitAndCount(int userId) throws ParseException;

	ResponseStructure getPostpaidDetailsForUserId(int userId);

	ResponseStructure lastPayDetailsForPostpaidUser(int userId);

	ResponseStructure getPostpaidDetailsOfUserAfterGeneratingInvoice(int userId);

	ResponseEntity<Resource> viewPostpaidReciept(int postpaidId, HttpServletRequest request);

	ResponseEntity<Resource> viewPostpaidInvoice(int postpaidId, HttpServletRequest request);

	ResponseStructure viewByNumberOfDays(int noOfDays);

	ResponseEntity<Resource> viewPostpaidInvoiceOne(int postpaidId, HttpServletRequest request);

	ResponseEntity<Resource> viewPostpaidInvoiceTwo(int postpaidId, HttpServletRequest request);

	ResponseEntity<Resource> viewConveInvoice(int postpaidId, HttpServletRequest request);

	ResponseStructure generateGraceInvoice(RequestModel model);

	ResponseEntity<Resource> viewGraceInvoice(int postpaidId, HttpServletRequest request);

	ResponseStructure forceInvoiceGeneration(RequestModel model);

	ResponseStructure viewByUniqueId(String uniqueId);

}
