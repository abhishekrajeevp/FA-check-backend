package com.bp.middleware.uploadhistory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.util.AppConstants;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class UploadServiceImpl implements UploadService{

	@Autowired
	private UploadRepository uploadRepository;
	
//	@Autowired
//	private LibraryRepository libraryRepository;
//	
//	@Autowired
//	private LibraianRepository libraianRepository;
	
	@Autowired
	private ResourceLoader resourceLoader;
	@Override
	public ResponseEntity<Resource> getUploadedDocument(int uploadId, HttpServletRequest request) {
//		Optional<MemberModel> member = memberRepository.findById(memberId);
		Optional<UploadModel> member = uploadRepository.findById(uploadId);
		if (member.isPresent()) {
			if (member.get().getUploadDocument() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/bulkupload/" + member.get().getUploadDocument());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
//					LOGGER.info("Could not determine file type.");

				}

				// Fallback to the default content type if type could not be determined
				if (contentType == null) {
					contentType = "application/octet-stream";
				}
				return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
						.header(HttpHeaders.CONTENT_DISPOSITION,
								"attachment; filename=\"" + resource.getFilename() + "\"")
						.body(resource);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public ResponseEntity<Resource> getFinishedDocument(int uploadId, HttpServletRequest request) {
		Optional<UploadModel> member = uploadRepository.findById(uploadId);
		if (member.isPresent()) {
			if (member.get().getFinishedDocument() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/bulkupload/" + member.get().getFinishedDocument());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
//					LOGGER.info("Could not determine file type.");

				}

				// Fallback to the default content type if type could not be determined
				if (contentType == null) {
					contentType = "application/octet-stream";
				}
				return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
						.header(HttpHeaders.CONTENT_DISPOSITION,
								"attachment; filename=\"" + resource.getFilename() + "\"")
						.body(resource);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public ResponseStructure viewAll(int libraryId) {
		ResponseStructure structure=new ResponseStructure();
		try {
//			LibraryModel libraryModel = libraryRepository.findByLibraryId(libraryId);
			
		List<UploadModel> model=uploadRepository.findAll();
		if (!model.isEmpty()) {
			structure.setData(model);
			structure.setMessage("Bulk Upload Histories are...");
			structure.setFlag(1);
		} else {
			structure.setData(model);
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	
	@Override
	public ResponseStructure viewByUploadId(int uploadId) {
		ResponseStructure structure=new ResponseStructure();
		try {
			
		UploadModel model=uploadRepository.findByUploadId(uploadId);
		if (model!=null) {
			structure.setData(model);
			structure.setMessage("Bulk Upload are...");
			structure.setFlag(1);
		} else {
			structure.setData(model);
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	
	}

	@Override
	public ResponseStructure viewAll() {
		ResponseStructure structure=new ResponseStructure();
		try {
			
		List<UploadModel> model=uploadRepository.findAll();
		if (!model.isEmpty()) {
			
			structure.setData(model);
			structure.setMessage("Bulk Upload are...");
			structure.setFlag(1);
			
		} else {
			structure.setData(model);
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

}
