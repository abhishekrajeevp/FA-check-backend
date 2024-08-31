package com.bp.middleware.uploadhistory;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import com.bp.middleware.responsestructure.ResponseStructure;

import jakarta.servlet.http.HttpServletRequest;

public interface UploadService {

	ResponseEntity<Resource> getUploadedDocument(int uploadId, HttpServletRequest request);

	ResponseEntity<Resource> getFinishedDocument(int uploadId, HttpServletRequest request);

	ResponseStructure viewAll(int libraryId);

	ResponseStructure viewByUploadId(int uploadId);

	ResponseStructure viewAll();

}
