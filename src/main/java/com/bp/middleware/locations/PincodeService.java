package com.bp.middleware.locations;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.admin.RequestModel;

public interface PincodeService {

	ResponseStructure getAllPincode();


	ResponseStructure getByPincode(int pincodeId);


	ResponseStructure getPincode(int id);


	ResponseStructure updatePincode(int pincodeId, RequestModel commonRequestDto);

	ResponseStructure addPincode(RequestModel commonRequestDto);


	ResponseStructure listAll();
		
}
