package com.bp.middleware.signmerchant;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

import jakarta.servlet.http.HttpServletRequest;

public interface MerchantService {

	ResponseStructure addMerchantDetails(RequestModel model) throws Exception;

	ResponseStructure insertAllMerchant(List<RequestModel> model);

	ResponseEntity<Resource> viewImage(int merchantId, HttpServletRequest request);

	ResponseStructure uploadMerchantAgreement(MultipartFile pdfDocument, String documentTitle, String description,int adminId) throws ParseException;

	ResponseEntity<Resource> viewPdf(int merchantId, HttpServletRequest request);

	ResponseStructure viewAllMerchants();

	String getPathName() throws IOException, ParseException;

	ResponseStructure listAll();

	ResponseStructure updateExpiryDate(int merchantId, RequestModel model);

	ResponseStructure viewByMerchantId(int merchantId);

	ResponseStructure viewMerchantByUserId(int userId);

	ResponseStructure viewMerchantByBondId(int bondId);

	ResponseStructure bulkUpload(List<SignerDto> model, int userId);

	ResponseStructure listBySpecificMerchant(int userId);

	ResponseStructure makeWholeDocExpire(int merchantId);

//	ResponseStructure updateMerchant(int merchantId, RequestModel model);


}
