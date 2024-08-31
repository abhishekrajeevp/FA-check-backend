package com.bp.middleware.rolesandpermission;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface MakerAndCheckerService {

	ResponseStructure addMakerAndChecker(RequestModel model);

	ResponseStructure viewallMakeAndChecker();

	ResponseStructure viewByMakerCheckerId(int makerCheckerId);

	ResponseStructure changeStatus(int makerCheckerId, RequestModel model);

	ResponseStructure viewByAdminId(int adminId);

	ResponseStructure update(int makerCheckerId, RequestModel model);

	ResponseStructure viewByStatus(boolean status);

	ResponseStructure addMakerAndCheckerForUser(RequestModel model);

	ResponseStructure viewByUserId(int userId);



}
