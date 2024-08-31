package com.bp.middleware.businesstype;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

public interface BusinessTypeService {

	ResponseStructure addBusinessType(RequestModel model);

	ResponseStructure getBusinessTypeId(int businessTypeId);

	ResponseStructure vieAllBusinessType();

	ResponseStructure update(int businessTypeId, RequestModel model);

	ResponseStructure updateBusinessCategoryStatus(int businessTypeId, RequestModel model);

	ResponseStructure viewAllBusinessTypeActive();

}
