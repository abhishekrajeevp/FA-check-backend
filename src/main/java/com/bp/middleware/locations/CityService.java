package com.bp.middleware.locations;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.admin.RequestModel;

public interface CityService {

	ResponseStructure getAllCity();

	ResponseStructure addCity(RequestModel model);

	ResponseStructure getByCity(int id);

	ResponseStructure getCity(int stateId);

	ResponseStructure updateCity(int id, RequestModel model);

}
