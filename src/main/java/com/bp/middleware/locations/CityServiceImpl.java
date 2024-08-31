package com.bp.middleware.locations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.admin.RequestModel;
import com.bp.middleware.util.AppConstants;


@Service
public class CityServiceImpl implements CityService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CityServiceImpl.class);

	@Autowired
	private CityRepository cityRepository;

	@Autowired
	private StateRepository stateRepository;

	@Override
	public ResponseStructure getAllCity() {

		ResponseStructure response = new ResponseStructure();
		try {
			List<CityModel> list = cityRepository.findAll();
			if (!list.isEmpty()) {
				
				response.setFlag(1);
				response.setData(list);
				response.setStatusCode(HttpStatus.OK.value());
				response.setMessage(AppConstants.SUCCESS);
			} else {
				response.setFlag(2);
				response.setData(null);
				response.setStatusCode(HttpStatus.OK.value());
				response.setMessage(AppConstants.NO_DATA_FOUND);
			}
			response.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			LOGGER.error("CityServiceImpl get all city method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setErrorDiscription(e.getMessage());
			response.setMessage(AppConstants.TRY_AGAIN);
		}
		return response;
	}

	@Override
	public ResponseStructure addCity(RequestModel dto) {

		ResponseStructure response = new ResponseStructure();
		try {
			CityModel model = new CityModel();

			model.setCityName(dto.getCityName());
			model.setCityStatus(true);
			model.setCreatedBy(dto.getCreatedBy());
			model.setCreatedDatetime(new Date());

			Optional<StateModel> optional = stateRepository.findById(dto.getStateId());
			if (optional.isPresent()) {
				StateModel state = optional.get();
				model.setStateModel(state);
			}
			cityRepository.save(model);

			response.setFlag(1);
			response.setData(model);
			response.setMessage(AppConstants.SUCCESS);
			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			LOGGER.error("StateServiceImpl add state method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setErrorDiscription(e.getMessage());
			response.setMessage(AppConstants.TRY_AGAIN);
		}
		
		return response;
	}

	@Override
	public ResponseStructure getByCity(int id) {
		ResponseStructure response = new ResponseStructure();
		try {
			Optional<CityModel> model = cityRepository.findById(id);
			if (model.isPresent()) {
				CityModel city=model.get();
				
				response.setFlag(1);
				response.setData(city);
				response.setMessage(AppConstants.SUCCESS);
			} else {
				response.setFlag(2);
				response.setData(null);
				response.setMessage(AppConstants.NO_DATA_FOUND);
			}
			response.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			LOGGER.error("CityServiceImpl get city method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setErrorDiscription(e.getMessage());
			response.setMessage(AppConstants.TRY_AGAIN);
		}
		return response;
	}

	@Override
	public ResponseStructure getCity(int stateId) {
		ResponseStructure response = new ResponseStructure();
		try {
			Optional<StateModel> model = stateRepository.findById(stateId);
			if (model.isPresent()) {
				StateModel sm = model.get();
				List<CityModel> list = cityRepository.findByStateModel(sm);
				if (list.isEmpty()) {
					response.setFlag(2);
					response.setData(null);
					response.setMessage(AppConstants.NO_DATA_FOUND);

				} else {
					response.setFlag(1);
					response.setData(list);
					response.setMessage(AppConstants.SUCCESS);
				}
			} else {
				response.setFlag(3);
				response.setData(null);
				response.setMessage(AppConstants.NO_DATA_FOUND);
			}
			response.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			LOGGER.error("CityServiceImpl get city method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setErrorDiscription(e.getMessage());
			response.setMessage(AppConstants.TRY_AGAIN);
		}
		return response;
	}

	@Override
	public ResponseStructure updateCity(int id, RequestModel dto) {

		ResponseStructure response = new ResponseStructure();
		try {
			Optional<CityModel> cityModel = cityRepository.findById(id);
			if (cityModel.isPresent()) {
				CityModel cm = cityModel.get();
				cm.setCityName(dto.getCityName());
				cm.setModifiedBy(dto.getModifiedBy());
				cm.setModifiedDatetime(new Date());
				cityRepository.save(cm);

				response.setFlag(1);
				response.setData(cm);
				response.setMessage(AppConstants.SUCCESS);
			} else {

				response.setFlag(2);
				response.setData(null);
				response.setMessage(AppConstants.NO_DATA_FOUND);
			}
			response.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			LOGGER.error("CityServiceImpl update city method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setErrorDiscription(e.getMessage());
			response.setMessage(AppConstants.TRY_AGAIN);
		}
		return response;
	}


}
