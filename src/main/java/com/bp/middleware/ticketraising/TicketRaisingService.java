package com.bp.middleware.ticketraising;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

import jakarta.servlet.http.HttpServletRequest;

public interface TicketRaisingService {


	ResponseStructure saveTicket(RequestModel model);

	ResponseEntity<Resource> viewAttachment(int ticketId, HttpServletRequest request);

	ResponseStructure getByReferenceNumber(String referenceNumber);

	ResponseStructure getByTicketId(int ticketId);

	ResponseStructure ticketVerificationChange(int ticketId, boolean status, String reason, String modifiedBy);//

	ResponseStructure updateAttachment(int ticketId, MultipartFile attachment);

	ResponseStructure getByUserId(int userId);

	ResponseStructure getByTicketStatus(boolean status);

	ResponseStructure updateTicketStatus(RequestModel model);

	ResponseStructure viewAllTicketRaiser();

	ResponseStructure updateRemarks(int ticketId,RequestModel model);//

}
