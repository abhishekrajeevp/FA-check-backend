package com.bp.middleware.locations;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.admin.RequestModel;

public interface StateService {

	ResponseStructure getAllState();
	
	ResponseStructure addState(RequestModel dto);
	
	ResponseStructure updateState(RequestModel dto, int stateId);

	ResponseStructure getViewById(int stateId);

	ResponseStructure getStateByCountry(int countryId);
}
