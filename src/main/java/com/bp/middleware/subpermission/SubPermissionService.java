package com.bp.middleware.subpermission;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.admin.AdminRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.rolesandpermission.PermissionModel;
import com.bp.middleware.rolesandpermission.PermissionRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;

@Service
public class SubPermissionService {

	@Autowired
	private SubPermissionRepository subPermissionRepository;
	@Autowired
	private PermissionRepository permissionRepository;
	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private UserRepository userRepository;

	public ResponseStructure addSubPermission(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			PermissionModel permission = permissionRepository.findByPermissionId(model.getPermissionId());
			AdminDto admin = adminRepository.findByAdminId(model.getAdminId());

			SubPermission subPermPresent = subPermissionRepository
					.findBySubPermissionNameAndPermissionAndAdmin(model.getSubPermissionName(), permission, admin);

			if (permission != null && admin != null && subPermPresent == null && permission.getAdminModel() == admin) {

				SubPermission subPermission = new SubPermission();

				subPermission.setSubPermissionName(model.getSubPermissionName());
				subPermission.setStatus(true);
				subPermission.setCreatedBy(model.getCreatedBy());
				subPermission.setCreatedDate(LocalDate.now());
				subPermission.setPermission(permission);
				subPermission.setAdmin(admin);

				subPermissionRepository.save(subPermission);

				structure.setData(subPermission);
				structure.setMessage("SUB PERMISSION CREATED SUCCESSFULLY");
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {

				structure.setData(null);

				if (admin == null) {
					structure.setMessage("ADMIN NOT FOUND");
				} else if (permission == null) {
					structure.setMessage("PERMISSION NOT FOUND");
				} else if (subPermPresent != null) {
					structure.setMessage("SUB PERMISSION ALREADY PRESENT");
				} else if (permission.getAdminModel() != admin) {
					structure.setMessage("SUPER ADMIN DOESN'T MATCH");
				}

				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	public ResponseStructure viewByAdminId(int adminId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<AdminDto> optional = adminRepository.findById(adminId);
			if (optional.isPresent()) {
				AdminDto admin = optional.get();

				List<SubPermission> subPermission = subPermissionRepository.findByAdmin(admin);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(subPermission);
				structure.setMessage("SUB PERMISSION FOR THE GIVEN ADMIN ID FOUND SUCCESSFULLY");
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
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	public ResponseStructure addSubPermissionForUser(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			PermissionModel permission = permissionRepository.findByPermissionId(model.getPermissionId());
			EntityModel entity = userRepository.findByUserId(model.getUserId());

			SubPermission subPermPresent = subPermissionRepository
					.findBySubPermissionNameAndPermissionAndUser(model.getSubPermissionName(), permission, entity);

			if (permission != null && entity != null && subPermPresent == null && permission.getUser() == entity) {

				SubPermission subPermission = new SubPermission();

				subPermission.setSubPermissionName(model.getSubPermissionName());
				subPermission.setStatus(true);
				subPermission.setCreatedBy(model.getCreatedBy());
				subPermission.setCreatedDate(LocalDate.now());
				subPermission.setPermission(permission);
				subPermission.setUser(entity);

				subPermissionRepository.save(subPermission);

				structure.setData(subPermission);
				structure.setMessage("SUB PERMISSION CREATED SUCCESSFULLY");
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {

				structure.setData(null);

				if (entity == null) {
					structure.setMessage("ENTITY NOT FOUND");
				} else if (permission == null) {
					structure.setMessage("PERMISSION NOT FOUND");
				} else if (subPermPresent != null) {
					structure.setMessage("SUB PERMISSION ALREADY PRESENT");
				} else if (permission.getUser() != entity) {
					structure.setMessage("ENTITIES DOESN'T MATCH");
				}

				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	public ResponseStructure viewByUserId(int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> optional = userRepository.findById(userId);
			if (optional.isPresent()) {
				EntityModel entity = optional.get();

				List<SubPermission> subPermission = subPermissionRepository.findByUser(entity);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(subPermission);
				structure.setMessage("SUB PERMISSION FOR THE GIVEN ENTITY ID FOUND SUCCESSFULLY");
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
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	public ResponseStructure addSubPermissionAsList(List<RequestModel> model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			List<SubPermission> subPermissionList = new ArrayList<>();

			for (RequestModel requestModel : model) {

				PermissionModel permission = permissionRepository.findByPermissionId(requestModel.getPermissionId());
				AdminDto admin = adminRepository.findByAdminId(requestModel.getAdminId());

				if (permission != null && admin != null) {

					SubPermission subPermission = new SubPermission();

					subPermission.setSubPermissionName(requestModel.getSubPermissionName());
					subPermission.setStatus(true);
					subPermission.setCreatedBy(requestModel.getCreatedBy());
					subPermission.setCreatedDate(LocalDate.now());
					subPermission.setPermission(permission);
					subPermission.setAdmin(admin);

					subPermissionRepository.save(subPermission);

					subPermissionList.add(subPermission);
				}
			}

			structure.setData(subPermissionList);
			structure.setMessage("SUB PERMISSION  LIST CREATED SUCCESSFULLY");
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	public ResponseStructure viewBySubPermissionId(int subPermissionId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<SubPermission> optional = subPermissionRepository.findById(subPermissionId);
			if (optional.isPresent()) {
				SubPermission subPermission = optional.get();

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(subPermission);
				structure.setMessage("SUB PERMISSION FOR THE GIVEN PARTICULAR ID FOUND SUCCESSFULLY");
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

	public ResponseStructure viewall() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<SubPermission> list = subPermissionRepository.findAll();

			if (list.isEmpty()) {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			} else {

				structure.setData(list);
				structure.setMessage("ALL SUB PERMISSION LIST FOUND SUCCESSFULLY");
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

	public ResponseStructure viewByStatus(boolean status) {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<SubPermission> statusList = subPermissionRepository.findByStatus(status);
			if (!statusList.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(statusList);
				structure.setMessage("SUB PERMISSION FOR THE GIVEN PARTICULAR STATUS FOUND SUCCESSFULLY");
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

	public ResponseStructure viewByPermissionId(int permissionId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<PermissionModel> optional = permissionRepository.findById(permissionId);
			if (optional.isPresent()) {
				PermissionModel permission = optional.get();

				List<SubPermission> subPermission = subPermissionRepository.findByPermission(permission);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(subPermission);
				structure.setMessage("SUB PERMISSION FOR THE GIVEN PERMISSION ID FOUND SUCCESSFULLY");
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
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	public ResponseStructure changeStatus(int subPermissionId, RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<SubPermission> optional = subPermissionRepository.findById(subPermissionId);
			if (optional.isPresent()) {

				SubPermission statusChange = optional.get();

				statusChange.setStatus(model.isAccountStatus());
				subPermissionRepository.save(statusChange);

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

	public ResponseStructure update(int subPermissionId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<SubPermission> optional = subPermissionRepository.findById(subPermissionId);
			if (optional.isPresent()) {

				SubPermission subPermission = optional.get();

				subPermission.setSubPermissionName(model.getSubPermissionName());

				subPermissionRepository.save(subPermission);

				structure.setMessage("SUB PERMISSION UPDATED SUCCESSFULLY");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(subPermission);
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

	public ResponseStructure viewByAdminIdAndPermissionId(int adminId, int permissionId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			AdminDto admin = adminRepository.findByAdminId(adminId);
			PermissionModel permission = permissionRepository.findByPermissionId(permissionId);

			if (admin != null && permission != null) {

				List<SubPermission> adminPermision = subPermissionRepository.findByAdminAndPermission(admin,
						permission);

				if (!adminPermision.isEmpty()) {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setData(adminPermision);
					structure.setMessage("SUB PERMISSION FOR THE GIVEN ADMIN AND PERMISSION FOUND SUCCESSFULLY");

				} else {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(2);
					structure.setData(null);
					structure.setMessage(AppConstants.NO_DATA_FOUND);
				}
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(3);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	public ResponseStructure viewByUserIdIdAndPermissionId(int userId, int permissionId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			EntityModel entity = userRepository.findByUserId(userId);
			PermissionModel permission = permissionRepository.findByPermissionId(permissionId);

			if (entity != null && permission != null) {

				List<SubPermission> userPermision = subPermissionRepository.findByUserAndPermission(entity, permission);

				if (!userPermision.isEmpty()) {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setData(userPermision);
					structure.setMessage("SUB PERMISSION FOR THE GIVEN ENTITY AND PERMISSION FOUND SUCCESSFULLY");

				} else {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(2);
					structure.setData(null);
					structure.setMessage(AppConstants.NO_DATA_FOUND);
				}
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(3);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

}
