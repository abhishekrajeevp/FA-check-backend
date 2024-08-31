package com.bp.middleware.rolesandpermission;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
public class MakerAndCheckerServiceImplementation implements MakerAndCheckerService{

	@Autowired
	private MakerAndCheckerRepository makerCheckerRepository;
	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private UserRepository userRepository;
	

	@Override
	public ResponseStructure addMakerAndChecker(RequestModel model) {
		
		ResponseStructure structure =new ResponseStructure();
		try {
			
			AdminDto admin=adminRepository.findByAdminId(model.getAdminId());
			MakerAndChecker makerCheckerPresent = makerCheckerRepository.findByMakerCheckerRoleNameAndAdminModel(model.getMakerCheckerRoleName(),admin);
			
			if(admin!=null && admin.getRole().getRoleId()==1 && makerCheckerPresent==null) {
			
			MakerAndChecker makerChecker=new MakerAndChecker();
			
			makerChecker.setMakerCheckeRoleCode(model.getMakerCheckeRoleCode());
			makerChecker.setMakerCheckerRoleName(model.getMakerCheckerRoleName());
			makerChecker.setMakerCheckerRoleType(model.getMakerCheckerRoleType());
			makerChecker.setRemarks(model.getRemarks());
			makerChecker.setStatus(true);
			makerChecker.setAdminModel(admin);
			
			makerCheckerRepository.save(makerChecker);
			
			structure.setData(makerChecker);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());
			
			}else {
				structure.setData(null);
				
				if(admin==null) {
					structure.setMessage("ADMIN NOT FOUND");
				}else if(makerCheckerPresent!=null) {
					structure.setMessage("MAKER & CHECKER ALREADY PRESENT");
				}else if(admin.getRole().getRoleId()!=1) {
					structure.setMessage("YOU ARE NOT SUPER ADMIN");
				}
				
				structure.setFlag(2);
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
	public ResponseStructure viewByAdminId(int adminId) {
		ResponseStructure structure=new ResponseStructure();
		try {
			
			AdminDto admin =adminRepository.findByAdminId(adminId);
			
			List<MakerAndChecker> list=makerCheckerRepository.findByAdminModel(admin);
			
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
	public ResponseStructure addMakerAndCheckerForUser(RequestModel model) {
		
		ResponseStructure structure =new ResponseStructure();
		try {
			
			EntityModel entity=userRepository.findByUserId(model.getUserId());
			MakerAndChecker makerCheckerPresent = makerCheckerRepository.findByMakerCheckerRoleNameAndUser(model.getMakerCheckerRoleName(),entity);
			
			if(entity!=null && entity.getRole().getRoleId()==3 && makerCheckerPresent==null) {
			
			MakerAndChecker makerChecker=new MakerAndChecker();
			
			makerChecker.setMakerCheckeRoleCode(model.getMakerCheckeRoleCode());
			makerChecker.setMakerCheckerRoleName(model.getMakerCheckerRoleName());
			makerChecker.setMakerCheckerRoleType(model.getMakerCheckerRoleType());
			makerChecker.setRemarks(model.getRemarks());
			makerChecker.setStatus(true);
			makerChecker.setUser(entity);
			
			makerCheckerRepository.save(makerChecker);
			
			structure.setData(makerChecker);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());
			
			}else {
				structure.setData(null);
				
				if(entity==null) {
					structure.setMessage("ENTITY NOT FOUND");
				}else if(makerCheckerPresent!=null) {
					structure.setMessage("MAKER & CHECKER ALREADY PRESENT");
				}
				
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
	public ResponseStructure viewByUserId(int userId) {
		ResponseStructure structure=new ResponseStructure();
		try {
			
			EntityModel entity =userRepository.findByUserId(userId);
			
			List<MakerAndChecker> list=makerCheckerRepository.findByUser(entity);
			
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
	public ResponseStructure viewallMakeAndChecker() {
		ResponseStructure structure=new ResponseStructure();
		try {
			
			List<MakerAndChecker> list=makerCheckerRepository.findAll();
			
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
	public ResponseStructure viewByMakerCheckerId(int makerCheckerId) {
		
		ResponseStructure structure=new ResponseStructure();
		try {
			
			Optional<MakerAndChecker> optional=makerCheckerRepository.findById(makerCheckerId);
			if (optional.isPresent()) {
				MakerAndChecker makerAndChecker=optional.get();
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(makerAndChecker);
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
	public ResponseStructure changeStatus(int makerCheckerId,RequestModel model) {
	   ResponseStructure structure =new ResponseStructure();
	   try {
				
		Optional<MakerAndChecker> optional =makerCheckerRepository.findById(makerCheckerId);
		
		if (optional.isPresent()) {
			MakerAndChecker statusChange =optional.get();
			statusChange.setStatus(model.isAccountStatus());
			makerCheckerRepository.save(statusChange);
					
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
	public ResponseStructure update(int makerCheckerId, RequestModel model) {
		
		 ResponseStructure structure =new ResponseStructure();
		   try {
					
			Optional<MakerAndChecker> optional =makerCheckerRepository.findById(makerCheckerId);
			
			if (optional.isPresent()) {
				
				MakerAndChecker update =optional.get();
				
				update.setMakerCheckeRoleCode(model.getMakerCheckeRoleCode());
				update.setMakerCheckerRoleName(model.getMakerCheckerRoleName());
				update.setMakerCheckerRoleType(model.getMakerCheckerRoleType());
				update.setRemarks(model.getRemarks());
				
				makerCheckerRepository.save(update);
						
				structure.setMessage("MAKER CHECKER UPDATED SUCCESSFULLY");
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

		List<MakerAndChecker> list = makerCheckerRepository.findByStatus(status);

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
