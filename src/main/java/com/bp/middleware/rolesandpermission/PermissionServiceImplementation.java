package com.bp.middleware.rolesandpermission;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Admin;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.admin.AdminRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;

@Service
public class PermissionServiceImplementation implements PermissionService{

	@Autowired
	private PermissionRepository permissionRepository;
	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private UserRepository userRepository;

	@Override
	public ResponseStructure addPermission(RequestModel dto) {

		ResponseStructure userResponse=new ResponseStructure();
		
		try {
			
			AdminDto admin=adminRepository.findByAdminId(dto.getAdminId());
			PermissionModel permPresent=permissionRepository.findByPermissionAndAdminModel(dto.getPermission(),admin);
			
			if(admin!=null && admin.getRole().getRoleId()==1 && permPresent==null) {
			
			PermissionModel model=new PermissionModel();

			model.setPermission(dto.getPermission());
			model.setCreatedBy(dto.getCreatedBy());
			SimpleDateFormat sdf=new SimpleDateFormat();
			model.setCreatedDateTime(sdf.format(new Date()));
			model.setStatus(true);
			model.setAdminModel(admin);
			permissionRepository.save(model);

			userResponse.setFlag(1);
			userResponse.setMessage(AppConstants.SUCCESS);
			userResponse.setStatusCode(HttpStatus.OK.value());
			userResponse.setData(model);

			}else {
				userResponse.setFlag(1);
				
				if(admin==null) {
				    userResponse.setMessage("ADMIN NOT FOUND");
				}else if(permPresent!=null) {
					userResponse.setMessage("PERMISSION ALREADY PRESENT");
				}
				
				userResponse.setStatusCode(HttpStatus.OK.value());
				userResponse.setData(null);
			}
			
		} catch (Exception e) {
			
			userResponse.setData(null);
			userResponse.setErrorDiscription(e.getMessage());
			userResponse.setMessage(AppConstants.TECHNICAL_ERROR);
			userResponse.setFlag(3);
			userResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return userResponse;
	}

	
	@Override
	public ResponseStructure viewByAdminId(int adminId) {
		ResponseStructure structure=new ResponseStructure();
		try {
			
			AdminDto admin =adminRepository.findByAdminId(adminId);
			
			List<PermissionModel> list=permissionRepository.findByAdminModel(admin);
			
			if (!list.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(list);
				structure.setMessage(AppConstants.SUCCESS);
				
			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}
			
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}
	
	
	
	@Override
	public ResponseStructure addPermissionForUser(RequestModel dto) {

		ResponseStructure userResponse=new ResponseStructure();
		
		try {
			
			EntityModel entity=userRepository.findByUserId(dto.getUserId());
			PermissionModel permPresent=permissionRepository.findByPermissionAndUser(dto.getPermission(),entity);
			
			if(entity!=null && entity.getRole().getRoleId()==3 && permPresent==null) {
			
			PermissionModel model=new PermissionModel();

			model.setPermission(dto.getPermission());
			model.setCreatedBy(dto.getCreatedBy());
			SimpleDateFormat sdf=new SimpleDateFormat();
			model.setCreatedDateTime(sdf.format(new Date()));
			model.setStatus(true);
			model.setUser(entity);
			permissionRepository.save(model);

			userResponse.setFlag(1);
			userResponse.setMessage(AppConstants.SUCCESS);
			userResponse.setStatusCode(HttpStatus.OK.value());
			userResponse.setData(model);

			}else {
				userResponse.setFlag(1);
				
				if(entity==null) {
				    userResponse.setMessage("ENTITY NOT FOUND");
				}else if(permPresent!=null) {
					userResponse.setMessage("PERMISSION ALREADY PRESENT");
				}
				
				userResponse.setStatusCode(HttpStatus.OK.value());
				userResponse.setData(null);
			}
			
		} catch (Exception e) {
			
			userResponse.setData(null);
			userResponse.setErrorDiscription(e.getMessage());
			userResponse.setMessage(AppConstants.TECHNICAL_ERROR);
			userResponse.setFlag(3);
			userResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return userResponse;
	}
	

	@Override
	public ResponseStructure viewByUserId(int userId) {
		ResponseStructure structure=new ResponseStructure();
		try {
			
			EntityModel entity =userRepository.findByUserId(userId);
			
			List<PermissionModel> list=permissionRepository.findByUser(entity);
			
			if (!list.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(list);
				structure.setMessage(AppConstants.SUCCESS);
				
			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}
			
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	
	
	@Override
	public ResponseStructure viewByPermissionId(int permissionId) {
		ResponseStructure structure=new ResponseStructure();
		try {
			
			Optional<PermissionModel> optional=permissionRepository.findById(permissionId);
			if (optional.isPresent()) {
				PermissionModel permissionModel=optional.get();
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(permissionModel);
				structure.setMessage(AppConstants.SUCCESS);
			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}
			
		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewall() {
		ResponseStructure structure=new ResponseStructure();
		try {
			
			List<PermissionModel> list=permissionRepository.findAll();
			
			if (list.isEmpty()) {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			} else {
			
				structure.setData(list);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
			}
			
		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}
	
	
	@Override
	public ResponseStructure changeStatus(int permissionId,RequestModel model) {
	   ResponseStructure structure =new ResponseStructure();
	   try {
				
		Optional<PermissionModel> optional =permissionRepository.findById(permissionId);
		if (optional.isPresent()) {
			
			PermissionModel statusChange =optional.get();
			statusChange.setStatus(model.isAccountStatus());
			permissionRepository.save(statusChange);
					
			structure.setMessage("Account status updated");
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(statusChange);
			structure.setFlag(1);
		} else {
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(null);
			structure.setFlag(2);

		}
	   } catch (Exception e) {
		structure.setMessage(AppConstants.TECHNICAL_ERROR);
		structure.setErrorDiscription(e.getMessage());
		structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		structure.setData(null);
		structure.setFlag(4);
	   }
		return structure;
	}

	

	@Override
	public ResponseStructure update(int permissionId, RequestModel model) {
		
		 ResponseStructure structure =new ResponseStructure();
		   try {
					
			Optional<PermissionModel> optional =permissionRepository.findById(permissionId);
			if (optional.isPresent()) {
				
				PermissionModel update =optional.get();
				
				update.setPermission(model.getPermission());
				permissionRepository.save(update);
						
				structure.setMessage("UPDATED SUCCESSFULLY");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(update);
				structure.setFlag(1);
				
			} else {
				
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);

			}
		   } catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		   }
			return structure;
			
	}

	@Override
	public ResponseStructure viewByStatus(boolean status) {
		ResponseStructure structure = new ResponseStructure();

		List<PermissionModel> list = permissionRepository.findByStatus(status);

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

		return structure;
	}


}
