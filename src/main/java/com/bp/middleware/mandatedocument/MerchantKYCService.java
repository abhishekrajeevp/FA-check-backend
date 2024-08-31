package com.bp.middleware.mandatedocument;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.util.CommonRequestDto;

import jakarta.servlet.http.HttpServletRequest;

public interface MerchantKYCService {

	ResponseStructure uploadMerchantKyc(CommonRequestDto model);

	ResponseEntity<Resource> getParticularMerchantDocs(long id, int flag, HttpServletRequest request);

	ResponseStructure getViewMerchantDocuments(int userId);

	ResponseStructure getAdminApproval(long id, CommonRequestDto dto);

	ResponseStructure getSuperAdminApproval(long id, CommonRequestDto dto);

	ResponseStructure updateKycDoc(long id, CommonRequestDto dto);

	ResponseStructure viewkyc(long id, int corporateId);

	ResponseEntity<Resource> viewImageByUserAndMandateDoc(int userId, int mandateDocId, int flag,
			HttpServletRequest request);

}
