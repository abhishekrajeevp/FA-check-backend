package com.bp.middleware.user.approvalrejection;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.admin.AdminRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.subpermission.SubPermission;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;

@Service
public class ApprovalRejectionService {

	@Autowired
	private ApprovalRejectionRepository approvalRejectionRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AdminRepository adminRepository;
	
	
	
	public ResponseStructure viewall() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<ApprovalRejectionHistory> list = approvalRejectionRepository.findAll();

			if (list.isEmpty()) {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			} else {

				structure.setData(list);
				structure.setMessage("ALL APPROVAL REJECTION HISTORY FOUND SUCCESSFULLY");
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



	public ResponseStructure viewById(int approvalRejectionId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<ApprovalRejectionHistory> optional = approvalRejectionRepository.findById(approvalRejectionId);
			if (optional.isPresent()) {
				
				ApprovalRejectionHistory approve = optional.get();

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(approve);
				structure.setMessage("APPROVE-REJECTION HISTORY FOR THE GIVEN PARTICULAR ID FOUND SUCCESSFULLY");
				
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



	public ResponseStructure adminTotalApprovedOrRejectedCount(int adminId, boolean approvedStatus) {
		ResponseStructure structure = new ResponseStructure();
		try {

			AdminDto admin=adminRepository.findByAdminId(adminId);
			
			List<ApprovalRejectionHistory> historyList=approvalRejectionRepository.findByAdminAndApprovedStatus(admin,approvedStatus);
			if (!historyList.isEmpty()) {
				
				int count=historyList.size();

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setCount(count);
				structure.setData(historyList);
				
				if(approvedStatus) {
					structure.setMessage("APPROVAL COUNT : "+count);
				}else {
					structure.setMessage("REJECTED COUNT : "+count);
				}
				
			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setCount(0);
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



	public ResponseStructure adminRejectedCountForAnEntity(int adminId, int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {

		AdminDto admin=adminRepository.findByAdminId(adminId);
		EntityModel user=userRepository.findByUserId(userId);
		
		List<ApprovalRejectionHistory> rejectedList=approvalRejectionRepository.findByAdminAndUserAndApprovedStatus(admin,user,false);
		
		if(!rejectedList.isEmpty()) {
			
			int count=rejectedList.size();

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setCount(count);
			structure.setData(rejectedList);
			structure.setMessage(user.getName()+" has been rejected "+count+" times by the given admin");
			
		}else {
			
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(2);
			structure.setCount(0);
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
	
	
}
