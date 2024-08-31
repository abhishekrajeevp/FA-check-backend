package com.bp.middleware.rolesandpermission;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.admin.AdminRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.subpermission.SubPermission;
import com.bp.middleware.subpermission.SubPermissionRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.usermanagement.UserManagement;
import com.bp.middleware.usermanagement.UserManagementRepository;
import com.bp.middleware.util.AppConstants;

@Service
public class ActionServiceImplementation implements ActionService {

	@Autowired
	private ActionRepository actionRepository;
	@Autowired
	private PermissionRepository permissionRepository;
	@Autowired
	private SubPermissionRepository subPermissionRepository;
	@Autowired
	private MakerAndCheckerRepository makerAndCheckerRepository;
	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserManagementRepository userManagementRepository;

	@Override
	public ResponseStructure addActionForAdmin(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {

			PermissionModel permission = permissionRepository.findByPermissionId(model.getPermissionId());
			MakerAndChecker makerChecker = makerAndCheckerRepository.findByMakerCheckerId(model.getMakerCheckerId());

			AdminDto superAdmin = adminRepository.findByAdminId(model.getSuperAdminId());
			AdminDto admin = adminRepository.findByAdminId(model.getAdminId());

			int size = model.getSubPermissions().size();

			List<ActionModel> roleActionList = new ArrayList<>();
			List<Integer> list = model.getSubPermissions();

			int count = 0;
			String createdBy = model.getCreatedBy();

			for (int i = 0; i < size; i++) {

				SubPermission subPermission = subPermissionRepository.findBySubPermissionId(list.get(i));

				ActionModel actionPresent = actionRepository
						.findBySubPermissionAndPermissionModelAndCheckerAndAdminModel(subPermission, permission,
								makerChecker, admin);

				if (permission.getAdminModel() != null && subPermission.getAdmin() != null
						&& makerChecker.getAdminModel() != null && actionPresent == null) {

					if (superAdmin != null && admin != null && subPermission.getPermission() == permission) {

						ActionModel roleAction = new ActionModel();

						roleAction.setAction(permission.getPermission() + " - " + subPermission.getSubPermissionName());
						roleAction.setAccountStatus(true);
						roleAction.setChecker(makerChecker);
						roleAction.setPermissionModel(permission);
						roleAction.setSubPermission(subPermission);
						roleAction.setAdminModel(admin);
						roleAction.setSuperAdmin(superAdmin);
						roleAction.setCreatedBy(createdBy);
						roleAction.setCreatedAt(LocalDate.now());

						actionRepository.save(roleAction);

						count++;
						roleActionList.add(roleAction);
					}
				}
			}

			structure.setMessage(count + " ACTION ROLE CREATED SUCCESSFULLY FOR GIVEN ADMIN");
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(roleActionList);
			structure.setFlag(1);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure addActionForUser(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			PermissionModel permission = permissionRepository.findByPermissionId(model.getPermissionId());
			MakerAndChecker makerChecker = makerAndCheckerRepository.findByMakerCheckerId(model.getMakerCheckerId());

			EntityModel entity = userRepository.findByUserId(model.getUserId());
			UserManagement userManage = userManagementRepository.findByUserManagementId(model.getUserManagementId());

			int size = model.getSubPermissions().size();

			List<ActionModel> roleActionList = new ArrayList<>();
			List<Integer> list = model.getSubPermissions();

			int count = 0;
			String createdBy = model.getCreatedBy();

			for (int i = 0; i < size; i++) {

				SubPermission subPermission = subPermissionRepository.findBySubPermissionId(list.get(i));

				ActionModel actionPresent = actionRepository
						.findBySubPermissionAndPermissionModelAndCheckerAndUserAndUserManagement(subPermission,
								permission, makerChecker, entity, userManage);

				boolean entityMatch = (permission.getUser() == entity && makerChecker.getUser() == entity
						&& subPermission.getUser() == entity && userManage.getUser() == entity);

				if (permission.getUser() != null && subPermission.getUser() != null && makerChecker.getUser() != null
						&& actionPresent == null && entityMatch) {

					if (entity != null && userManage != null && subPermission.getPermission() == permission) {

						ActionModel roleAction = new ActionModel();

						roleAction.setAction(permission.getPermission() + " - " + subPermission.getSubPermissionName());
						roleAction.setAccountStatus(true);
						roleAction.setChecker(makerChecker);
						roleAction.setPermissionModel(permission);
						roleAction.setSubPermission(subPermission);
						roleAction.setUser(entity);
						roleAction.setUserManagement(userManage);
						roleAction.setCreatedBy(createdBy);
						roleAction.setCreatedAt(LocalDate.now());

						actionRepository.save(roleAction);

						count++;
						roleActionList.add(roleAction);
					}
				}
			}

			structure.setMessage(count + " ACTION ROLE CREATED SUCCESSFULLY FOR GIVEN ENTITY");
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(roleActionList);
			structure.setFlag(1);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure viewActionById(int actionId) {
		ResponseStructure structure = new ResponseStructure();

		Optional<ActionModel> optional = actionRepository.findById(actionId);

		if (optional.isPresent()) {
			ActionModel actionModel = optional.get();

			structure.setData(actionModel);
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

	@Override
	public ResponseStructure viewall() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<ActionModel> list = actionRepository.findAll();

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
	public ResponseStructure viewByStatus(boolean status) {
		ResponseStructure structure = new ResponseStructure();

		List<ActionModel> list = actionRepository.findByAccountStatus(status);

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

	@Override
	public ResponseStructure viewByPermissionId(int permissionId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<PermissionModel> optional = permissionRepository.findById(permissionId);
			if (optional.isPresent()) {

				PermissionModel entityModel = optional.get();
				List<ActionModel> action = actionRepository.findByPermissionModel(entityModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(action);
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
	public ResponseStructure viewBySubPermissionId(int subPermissionId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<SubPermission> optional = subPermissionRepository.findById(subPermissionId);
			if (optional.isPresent()) {

				SubPermission sub = optional.get();
				List<ActionModel> action = actionRepository.findBySubPermission(sub);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(action);
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
	public ResponseStructure viewByMakerId(int makerId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<MakerAndChecker> optional = makerAndCheckerRepository.findById(makerId);
			if (optional.isPresent()) {

				MakerAndChecker maker = optional.get();
				List<ActionModel> action = actionRepository.findByChecker(maker);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(action);
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
	public ResponseStructure viewBySuperAdminId(int superAdminId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<AdminDto> optional = adminRepository.findById(superAdminId);
			if (optional.isPresent()) {

				AdminDto adminDto = optional.get();
				List<ActionModel> action = actionRepository.findBySuperAdmin(adminDto);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(action);
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
	public ResponseStructure viewByUserId(int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> optional = userRepository.findById(userId);
			if (optional.isPresent()) {

				EntityModel entity = optional.get();
				List<ActionModel> action = actionRepository.findByUser(entity);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(action);
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
	public ResponseStructure viewByAdminId(int adminId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<AdminDto> optional = adminRepository.findById(adminId);
			if (optional.isPresent()) {

				AdminDto adminDto = optional.get();
				List<ActionModel> action = actionRepository.findByAdminModel(adminDto);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(action);
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
	public ResponseStructure viewByUserManagement(int userManagementId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<UserManagement> optional = userManagementRepository.findById(userManagementId);
			if (optional.isPresent()) {

				UserManagement userManagement = optional.get();
				List<ActionModel> action = actionRepository.findByUserManagement(userManagement);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(action);
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
	public ResponseStructure viewByAdminAndPermission(int adminId, int permissionId) {
		ResponseStructure structure = new ResponseStructure();

		Optional<AdminDto> optional = adminRepository.findById(adminId);
		Optional<PermissionModel> optional2 = permissionRepository.findById(permissionId);
		if (optional.isPresent() && optional2.isPresent()) {

			AdminDto entityModel = optional.get();
			PermissionModel model = optional2.get();
			List<ActionModel> makerAndChecker = actionRepository.findByAdminModelAndPermissionModel(entityModel, model);

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
		return structure;
	}

	@Override
	public ResponseStructure viewByAdminAndMakerChecker(int adminId, int makerId) {
		ResponseStructure structure = new ResponseStructure();

		Optional<AdminDto> optional = adminRepository.findById(adminId);
		Optional<MakerAndChecker> optional2 = makerAndCheckerRepository.findById(makerId);
		if (optional.isPresent() && optional2.isPresent()) {

			AdminDto entityModel = optional.get();
			MakerAndChecker model = optional2.get();
			List<ActionModel> makerAndChecker = actionRepository.findByAdminModelAndChecker(entityModel, model);

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
		return structure;
	}

	@Override
	public ResponseStructure viewByAdminAndMakerCheckerAndPermission(int adminId, int makerId, int permissionId) {
		ResponseStructure structure = new ResponseStructure();
		Optional<AdminDto> optional = adminRepository.findById(adminId);

		Optional<MakerAndChecker> optional2 = makerAndCheckerRepository.findById(makerId);
		Optional<PermissionModel> optional3 = permissionRepository.findById(permissionId);

		if (optional.isPresent() && optional2.isPresent() && optional3.isPresent()) {

			AdminDto entityModel = optional.get();
			MakerAndChecker makerAndChecker = optional2.get();
			PermissionModel permissionModel = optional3.get();

			List<ActionModel> list = actionRepository.findByAdminModelAndCheckerAndPermissionModel(entityModel,
					makerAndChecker, permissionModel);

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
		return structure;
	}

	@Override
	public ResponseStructure copyRolesAndPermissionWithEmail(int adminId, RequestModel model) {
		ResponseStructure userResponse = new ResponseStructure();
		try {

			AdminDto newAdmin = adminRepository.findByAdminId(adminId);
			AdminDto oldAdmin = adminRepository.findByEmail(model.getEmail());

			if (newAdmin != null && oldAdmin != null) {

				List<ActionModel> actionList = actionRepository.findByAdminModel(oldAdmin);
				List<ActionModel> newActionList = new ArrayList<>();

				for (ActionModel copyAction : actionList) {

					ActionModel newAdminAction = new ActionModel();

					newAdminAction.setAccountStatus(true);
					newAdminAction.setAction(copyAction.getAction());
					newAdminAction.setCreatedBy(model.getCreatedBy());
					newAdminAction.setCreatedAt(LocalDate.now());
					newAdminAction.setPermissionModel(copyAction.getPermissionModel());
					newAdminAction.setSubPermission(copyAction.getSubPermission());
					newAdminAction.setSuperAdmin(copyAction.getSuperAdmin());
					newAdminAction.setAdminModel(newAdmin);
					newAdminAction.setChecker(copyAction.getChecker());

					actionRepository.save(newAdminAction);

					newActionList.add(newAdminAction);
				}
				
				userResponse.setFlag(1);
				userResponse.setMessage(AppConstants.SUCCESS);
				userResponse.setStatusCode(HttpStatus.OK.value());
				userResponse.setData(newActionList);
				
			}else {
				
				userResponse.setFlag(2);
				if (oldAdmin == null) {
					userResponse.setMessage("NO ADMIN FOUND FOR THE GIVEN EMAIL");
				} else {
					userResponse.setMessage("NO ADMIN FOUND FOR THE GIVEN ID");
				}
				userResponse.setStatusCode(HttpStatus.OK.value());
				userResponse.setData(null);
			}


		} catch (Exception e) {
			e.printStackTrace();
			userResponse.setFlag(3);
			userResponse.setErrorDiscription(e.getMessage());
			userResponse.setMessage(AppConstants.TECHNICAL_ERROR);
			userResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			userResponse.setData(null);
		}

		return userResponse;
	}

	@Override
	public ResponseStructure copyRolesAndPermissionWithEmailForUserManagement(int userManagementId,
			RequestModel model) {

		ResponseStructure userResponse = new ResponseStructure();
		try {

			UserManagement newEntityUser = userManagementRepository.findByUserManagementId(userManagementId);
			UserManagement oldEntityUser = userManagementRepository.findByEmail(model.getEmail());

			List<ActionModel> newActionList = new ArrayList<>();

			if (oldEntityUser != null && newEntityUser != null) {

				List<ActionModel> actionList = actionRepository.findByUserManagement(oldEntityUser);

				for (ActionModel copyAction : actionList) {
					ActionModel newAction = new ActionModel();

					newAction.setAccountStatus(true);
					newAction.setAction(copyAction.getAction());
					newAction.setCreatedBy(model.getCreatedBy());
					newAction.setCreatedAt(LocalDate.now());
					newAction.setPermissionModel(copyAction.getPermissionModel());
					newAction.setSubPermission(copyAction.getSubPermission());
					newAction.setSuperAdmin(copyAction.getSuperAdmin());
					newAction.setUserManagement(newEntityUser);
					newAction.setChecker(copyAction.getChecker());

					actionRepository.save(newAction);
					newActionList.add(newAction);
				}

				userResponse.setFlag(1);
				userResponse.setMessage(AppConstants.SUCCESS);
				userResponse.setStatusCode(HttpStatus.OK.value());
				userResponse.setData(newActionList);

			} else {

				userResponse.setFlag(2);
				if (oldEntityUser == null) {
					userResponse.setMessage("NO ENTITY FOUND FOR THE GIVEN EMAIL");
				} else {
					userResponse.setMessage("NO ENTITY FOUND FOR THE GIVEN ID");
				}
				userResponse.setStatusCode(HttpStatus.OK.value());
				userResponse.setData(null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			userResponse.setFlag(3);
			userResponse.setErrorDiscription(e.getMessage());
			userResponse.setMessage(AppConstants.TECHNICAL_ERROR);
			userResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			userResponse.setData(null);
		}
		return userResponse;
	}

	@Override
	public ResponseStructure changeStatus(int actionId, RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<ActionModel> optional = actionRepository.findById(actionId);
			if (optional.isPresent()) {

				ActionModel statusChange = optional.get();

				statusChange.setAccountStatus(model.isAccountStatus());

				statusChange.setModifiedBy(model.getModifiedBy());
				statusChange.setModifiedAt(LocalDate.now());

				actionRepository.save(statusChange);

				structure.setMessage("STATUS UPDATED SUCCESSFULLY");
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

}
