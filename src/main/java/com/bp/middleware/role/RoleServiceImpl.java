package com.bp.middleware.role;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

import com.bp.middleware.department.DepartmentModel;
import com.bp.middleware.department.DepartmentRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class RoleServiceImpl implements RoleService {

	private static final Logger LOGGER=LoggerFactory.getLogger(RoleServiceImpl.class);
	@Autowired
	private RoleRepository repository;

	@Autowired
	ServletContext context;

	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private DepartmentRepository departmentRepository;

	@Override
	public ResponseStructure createRoles(RequestModel role) {
		ResponseStructure structure=new ResponseStructure();
		
		RoleDto model=new RoleDto();
		DepartmentModel department = departmentRepository.findByDepartmentId(role.getDepartmentId());
		model.setRoleName(role.getRoleName());
		model.setRoleCode(role.getRoleCode());
		model.setCreatedBy(role.getCreatedBy());
		model.setCreatedDate(new Date());
		model.setRoleStatus(true);
		model.setDepartment(department);
		repository.save(model);
		
		structure.setMessage(AppConstants.SUCCESS);
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setData(model);
		structure.setFlag(1);
		return structure;

	}

	@Override
	public ResponseStructure getAllRoles() {

		ResponseStructure structure=new ResponseStructure();
		List<RoleDto> entities=repository.findAll();
		if (!entities.isEmpty()) {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Role Details are..!!!");
			structure.setData(entities);
			structure.setFlag(1);
			return structure;
		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setData(null);
			structure.setFlag(2);
			return structure;
		}
	}

	@Override
	public ResponseStructure getById(int roleId) {

		ResponseStructure structure=new ResponseStructure();
		try {
			Optional<RoleDto> entity=(repository.findById(roleId));
			if (entity.isPresent()) {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Role Details are..!!!");
				structure.setData(entity);
				structure.setFlag(1);
			}else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			LOGGER.info("RoleServiceImpl getById Method",e);
			structure.setStatusCode(HttpStatus.BAD_REQUEST.value());
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	@Override
	public ResponseStructure updateRoleStatus(RequestModel dto) {
		ResponseStructure structure=new ResponseStructure();
		try {
			Optional<RoleDto> model=(repository.findById(dto.getRoleId()));
			if (model.isPresent()) {
				RoleDto entity=model.get();
				entity.setRoleStatus(dto.isRoleStatus());
				repository.save(entity);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setData(entity);
				structure.setFlag(1);
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			LOGGER.info("RoleServiceImpl updateRoleStatus Method",e);
			structure.setStatusCode(HttpStatus.BAD_REQUEST.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	@Override
	public ResponseStructure activeAccounts(boolean roleStatus) {
		ResponseStructure structure=new ResponseStructure();
		try {
			List<RoleDto> accounts= repository.getByAccountStatus(roleStatus);
			if (!accounts.isEmpty()) {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Active Account Details are..!!!");
				structure.setData(accounts);
				structure.setFlag(1);
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {
			LOGGER.info("RoleServiceImpl activeAccounts Method",e);
			structure.setStatusCode(HttpStatus.BAD_REQUEST.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	@Override
	public ResponseStructure updateRoleDetails(RequestModel entity,int roleId) {

		ResponseStructure structure=new ResponseStructure();
		try {
			Optional<RoleDto> role=(repository.findById(roleId));

			if (role.isPresent()) {
				RoleDto model=role.get();
				if (model.isRoleStatus()) {
					model.setRoleCode(entity.getRoleCode());
					model.setRoleName(entity.getRoleName());
					model.setModifyBy(entity.getModifyBy());
					model.setModifyDate(new Date());
					model.setRoleStatus(true);
					repository.save(model);

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("Account updated Successfully..!!!");
					structure.setData(model);
					structure.setFlag(1);
				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("Account Currently Not Active..!!!");
					structure.setData(null);
					structure.setFlag(2);
				}

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(3);
			}
		} catch (Exception e) {
			LOGGER.info("RoleServiceImpl updateRoleDetails Method",e);
			structure.setStatusCode(HttpStatus.BAD_REQUEST.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;

	}



	@Override
	public ResponseStructure uploadAdminProfilePicture(int roleId, MultipartFile profilePhoto) {
		ResponseStructure structure=new ResponseStructure();
		try {
			Optional<RoleDto> roleModel = repository.findById(roleId);
			if (roleModel.isPresent()) {
				RoleDto model = roleModel.get();
				return saveUploadedFiles(profilePhoto, model);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setMessage(AppConstants.NO_DATA_FOUND);		
			}
		} catch (IOException e) {
			LOGGER.info("RoleServiceImpl uploadAdminProfilePicture Method",e);
			structure.setStatusCode(HttpStatus.BAD_REQUEST.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setData(null);
		}
		return structure;
	}
	private ResponseStructure saveUploadedFiles(MultipartFile  profilePhoto, RoleDto model) throws IOException {
		ResponseStructure structure=new ResponseStructure();
		
		String folder = new FileUtils().genrateFolderName("" + model.getRoleId());


		String extensionType = null;
		StringTokenizer st = new StringTokenizer(profilePhoto.getOriginalFilename(), ".");
		while (st.hasMoreElements()) {
			extensionType = st.nextElement().toString();
		}
		String fileName = FileUtils.getRandomString() + "." + extensionType;
		model.setProfilePicture(folder + "/" + fileName);

		Path currentWorkingDir = Paths.get(context.getRealPath("/WEB-INF/"));
		File saveFile = new File(currentWorkingDir + "/roleprofilepictures/" + folder);
		saveFile.mkdir();

		byte[] bytes = profilePhoto.getBytes();
		Path path = Paths.get(saveFile + "/" + fileName);
		Files.write(path, bytes);
		repository.save(model);
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setMessage(model.getRoleName()+" your profile picture has been uploaded successfully!!");
		structure.setData(model);
		structure.setFileName(fileName);
		structure.setFlag(1);
		
		return structure;
	}

	@Override
	public ResponseEntity<Resource> viewImage(int roleId, HttpServletRequest request) {
		Optional<RoleDto> role=repository.findById(roleId);
		if(role.isPresent()) {
			RoleDto entity=role.get();
			if(entity.getProfilePicture()!=null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/roleprofilepictures/" + entity.getProfilePicture());
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




}
