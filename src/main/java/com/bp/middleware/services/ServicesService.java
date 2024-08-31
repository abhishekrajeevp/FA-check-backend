package com.bp.middleware.services;

import com.bp.middleware.admin.RequestModel;
import com.bp.middleware.responsestructure.ResponseStructure;

public interface ServicesService {

	ResponseStructure addServices(RequestModel model);

	ResponseStructure updateServices(RequestModel model);

	ResponseStructure getById(int id);

	ResponseStructure viewAll();

	ResponseStructure accountStatusChange(RequestModel model);


}
