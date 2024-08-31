package com.bp.middleware.businesscategory;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface BusinessCategoryService {

	ResponseStructure addcategory(RequestModel model);

	ResponseStructure getByBusinessId(int businessCategoryId);

	ResponseStructure viewAll();

	ResponseStructure update(int businessCategoryId, RequestModel model);

	ResponseStructure updateBusinessCategoryStatus(int businessCategoryId, RequestModel model);

	ResponseStructure viewAllActive();

}
