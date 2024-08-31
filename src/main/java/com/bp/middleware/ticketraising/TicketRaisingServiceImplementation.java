package com.bp.middleware.ticketraising;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.ticketcategory.TicketCategory;
import com.bp.middleware.ticketcategory.TicketCategoryRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class TicketRaisingServiceImplementation implements TicketRaisingService{

private static final Logger LOGGER=LoggerFactory.getLogger(TicketRaisingServiceImplementation.class);
	
	@Autowired
	private TicketRaisingRepository raisingRepository;
	
	@Autowired
	private TicketCategoryRepository categoryRepository;
	
	@Autowired
	private UserRepository repository;
	
	@Autowired
	ServletContext context;
	
	@Autowired
	private ResourceLoader resourceLoader;
	

	

	@Override
	public ResponseStructure saveTicket(RequestModel model) {
		ResponseStructure structure =new ResponseStructure();
		try {
			
			Optional<EntityModel> entity=repository.findById(model.getUserId());
			if (entity.isPresent()) {
			EntityModel entityModel = entity.get();
			TicketRaising ticket=new TicketRaising();
			ticket.setCustomerName(entityModel.getName());
			ticket.setMobileNumber(entityModel.getMobileNumber());
			ticket.setEmail(model.getEmail());
			ticket.setRemarks("");
			TicketCategory category= categoryRepository.findByTicketCategoryId(model.getTicketCategoryId());
			ticket.setCategory(category);
			ticket.setEntityModel(entityModel);
			ticket.setUserType(model.getUserType());
			ticket.setStatus(true);
			ticket.setTicketStatus("Open");
			ticket.setReason(model.getReason());
			ticket.setDescription(model.getDescription());
			ticket.setCreatedAt(LocalDate.now());
			
			String reference=FileUtils.getRandomOTPnumber(10);
			ticket.setReferenceNumber(reference);
			
			if(model.getFileAttachment()!=null) {
			
			String folder =new FileUtils().genrateFolderName(""+model.getUserId());
			String extnsion=null;
			StringTokenizer st=new StringTokenizer(model.getFileAttachment().getOriginalFilename(),".");
			
			while(st.hasMoreElements()) {
				extnsion=st.nextElement().toString();
			}
			String fileName = FileUtils.getRandomString()+"."+extnsion;
			ticket.setAttachment(folder+"/"+fileName);
			
			Path currentWorkingDir=Paths.get(context.getRealPath("/WEB-INF/"));
			File saveFile =new File(currentWorkingDir+"/ticketraisingattachment/"+folder);
			saveFile.mkdir();
			byte[] bytes=model.getFileAttachment().getBytes();
			Path path=Paths.get(saveFile+"/"+fileName);
		    Files.write(path, bytes);
			}
			
		    raisingRepository.save(ticket);
		    
		    structure.setStatusCode(HttpStatus.OK.value());
		    structure.setMessage(AppConstants.SUCCESS);
			structure.setData(ticket);
		    structure.setFlag(1);
			}
			
		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		    structure.setMessage(AppConstants.TECHNICAL_ERROR);
		    structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
		    structure.setFlag(3);
		
		}
		return structure;
	}




	@Override
	public ResponseEntity<Resource> viewAttachment(int ticketId, HttpServletRequest request) {
		Optional<TicketRaising> attachment=raisingRepository.findById(ticketId);
		if(attachment.isPresent()) {
			if(attachment.get().getAttachment()!=null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/ticketraisingattachment/" + attachment.get().getAttachment());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
					LOGGER.info("Could not determine file type.");
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
		}else {
			return null;
		}
		}




	@Override
	public ResponseStructure getByReferenceNumber(String referenceNumber) {
ResponseStructure structure=new ResponseStructure();
		
		TicketRaising ticket  =	raisingRepository.findByReferenceNumber(referenceNumber);
		if (ticket!=null) {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(ticket);
			structure.setFlag(1);
		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
		}
		return structure;
	}




	@Override
	public ResponseStructure getByTicketId(int ticketId) {
		ResponseStructure structure=new ResponseStructure();
		Optional<TicketRaising> optional = raisingRepository.findById(ticketId);
		if (optional.isPresent()) {
			TicketRaising raising = optional.get();
			
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(raising);
			structure.setFlag(1);
		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
		}
		return structure;
	}




	@Override
	public ResponseStructure ticketVerificationChange(int ticketId, boolean status, String reason,String modifiedBy) {
		ResponseStructure structure=new ResponseStructure();
		Optional<TicketRaising> optional = raisingRepository.findById(ticketId);
		if (optional.isPresent()) {
			
			TicketRaising raising = optional.get();
			raising.setStatus(status);
			raising.setReason(reason);
			raising.setModifiedBy(modifiedBy);
			raising.setModifiedAt(LocalDate.now());
			raisingRepository.save(raising);
			
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(raising);
			structure.setFlag(1);
		} else {
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		return structure;
	}




	@Override
	public ResponseStructure updateAttachment(int ticketId, MultipartFile attachment) {
		ResponseStructure structure=new ResponseStructure();
		Optional<TicketRaising> optional = raisingRepository.findById(ticketId);
		if (optional.isPresent()) {
			TicketRaising raising = optional.get();
			return saveUploadedFiles(attachment, raising);
		} else {
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
			structure.setData(null);
			
		}
		structure.setStatusCode(HttpStatus.OK.value());
		return structure;
	}




	private ResponseStructure saveUploadedFiles(MultipartFile profilePhoto,TicketRaising model) {
		ResponseStructure structure =new ResponseStructure();
		try {
			String folder = new FileUtils().genrateFolderName("" + model.getTicketId());


			String extensionType = null;
			StringTokenizer st = new StringTokenizer(profilePhoto.getOriginalFilename(), ".");
			while (st.hasMoreElements()) {
				extensionType = st.nextElement().toString();
			}
			String fileName = FileUtils.getRandomString() + "." + extensionType;
			model.setAttachment(folder + "/" + fileName);

			Path currentWorkingDir = Paths.get(context.getRealPath("/WEB-INF/"));
			File saveFile = new File(currentWorkingDir + "/ticketraisingattachment/" + folder);
			saveFile.mkdir();

			byte[] bytes = profilePhoto.getBytes();
			Path path = Paths.get(saveFile + "/" + fileName);
			Files.write(path, bytes);
			raisingRepository.save(model);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(model);
			structure.setMessage(model.getCustomerName()+" your profile attachment has been uploaded successfully!!");		
			structure.setFileName(fileName);
		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		    structure.setMessage(AppConstants.TECHNICAL_ERROR);
		    structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
		    structure.setFlag(3);
		}
		return structure;
	}




	@Override
	public ResponseStructure getByUserId(int userId) {
		
		ResponseStructure structure=new ResponseStructure();
		try {
			
			Optional<EntityModel> optional = repository.findById(userId);
			if (optional.isPresent()) {
				EntityModel model=optional.get();
				
				List<TicketRaising> list=raisingRepository.findByEntityModel(model);
				
				if (!list.isEmpty()) {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage(AppConstants.SUCCESS);
					structure.setData(list);
					structure.setFlag(1);
				}
				
				
			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);
				
			}
			
		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		    structure.setMessage(AppConstants.TECHNICAL_ERROR);
		    structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
		    structure.setFlag(3);
		}
		return structure;
		
	}




	@Override
	public ResponseStructure getByTicketStatus(boolean status) {
		ResponseStructure structure=new ResponseStructure();
		try {
			
			List<TicketRaising> list =raisingRepository.getByStatus(status);
			
			if (!list.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.SUCCESS);
				structure.setData(list);
				structure.setFlag(1);
				
			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);
			}
			
		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		    structure.setMessage(AppConstants.TECHNICAL_ERROR);
		    structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
		    structure.setFlag(3);
		}
		
		return structure;
	}




	@Override
	public ResponseStructure updateTicketStatus(RequestModel model) {
		ResponseStructure structure=new ResponseStructure();
		try {
			
			Optional<TicketRaising> optional=raisingRepository.findById(model.getTicketId());
			if (optional.isPresent()) {
				TicketRaising ticket=optional.get();
				
				ticket.setStatus(model.isTicketStatus());
				ticket.setModifiedBy(model.getModifiedBy());
				ticket.setModifiedAt(LocalDate.now());
				raisingRepository.save(ticket);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.SUCCESS);
				structure.setData(ticket);
				structure.setFlag(1);
				
			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);
			}
			
		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		    structure.setMessage(AppConstants.TECHNICAL_ERROR);
		    structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
		    structure.setFlag(3);
		}
		return structure;
	}




	@Override
	public ResponseStructure viewAllTicketRaiser() {
		ResponseStructure structure=new ResponseStructure();
		try {
			
			List<TicketRaising> list = raisingRepository.findAll();
			if (!list.isEmpty()) {

				structure.setData(list);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
			} else {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}
			
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
		
	}




	@Override
	public ResponseStructure updateRemarks(int ticketId,RequestModel model) {
	
		ResponseStructure structure=new ResponseStructure();
		
		try {
			
		Optional<TicketRaising> optional = raisingRepository.findById(ticketId);
		
		if (optional.isPresent()) {
			
			TicketRaising raising = optional.get();
			
			raising.setRemarks(model.getRemarks());
			raising.setTicketStatus(model.getTicketCurrentStatus());
			
			if(model.getTicketCurrentStatus().equalsIgnoreCase("Closed")) {
				raising.setStatus(false);
			}
			
			raising.setModifiedBy(model.getModifiedBy());
			raising.setModifiedAt(LocalDate.now());
			
			raisingRepository.save(raising);
			
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(raising);
			structure.setFlag(1);
		} else {
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		
		} catch (Exception e) {
			
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}
	
	
	
}
