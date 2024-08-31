package com.bp.middleware.uploadhistory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/uploads")
@CrossOrigin
public class UploadController {
	
	@Autowired
	private UploadService service;

	@GetMapping("/viewuploaddocument/{uploadId}")
	public ResponseEntity<Resource> getUploadedDocument(@PathVariable("uploadId") int uploadId, HttpServletRequest request){
		return service.getUploadedDocument(uploadId, request);
	}
	
	@GetMapping("/viewfinisheddocument/{uploadId}")
	public ResponseEntity<Resource> getFinishedDocument(@PathVariable("uploadId") int uploadId, HttpServletRequest request){
		return service.getFinishedDocument(uploadId, request);
	}
	
	@GetMapping("/viewallbylibrary/{libraryId}")
	public ResponseStructure viewAll(@PathVariable("libraryId") int libraryId) {
		return service.viewAll(libraryId);
	}
	
	@GetMapping("/viewById/{uploadId}")
	public ResponseStructure viewById(@PathVariable("uploadId") int uploadId) {
		return service.viewByUploadId(uploadId);
	}	
	
	@GetMapping("/viewAll")
	public ResponseStructure viewAll() {
		return service.viewAll();
	}
}
