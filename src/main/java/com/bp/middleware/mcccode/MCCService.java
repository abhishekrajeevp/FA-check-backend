package com.bp.middleware.mcccode;


import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;


public interface MCCService {

	ResponseStructure addMCCCode(RequestModel dto);
	
	ResponseStructure viewMCCCodeById(int mccId);
	
	ResponseStructure viewAllMCCCode();


	ResponseStructure updateMCCCodeById(int mccId,RequestModel dto);



	ResponseStructure deletMCCCodeById(int mccId);

	ResponseStructure changeAccountStatus(RequestModel model);

	ResponseStructure viewAllMCCCodeActive();
	
	
	

}
