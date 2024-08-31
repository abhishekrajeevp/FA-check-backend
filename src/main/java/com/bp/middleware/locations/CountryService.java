package com.bp.middleware.locations;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.admin.RequestModel;

public interface CountryService {

	ResponseStructure getAllCountry();

	
	ResponseStructure addCountry(RequestModel dto);
	
	ResponseStructure updateCountry(RequestModel dto, int id);

}
