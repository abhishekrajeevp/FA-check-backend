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
public class StateServiceImpl implements StateService {
	private static final Logger LOGGER = LoggerFactory.getLogger(StateServiceImpl.class);

	@Autowired
	private StateRepository stateRepository;
	
	@Autowired
	private CountryRepository countryRepository;

	@Override
	public ResponseStructure getAllState() {
		ResponseStructure response = new ResponseStructure();
		try {
			List<StateModel> list = stateRepository.findAll();
			if (!list.isEmpty()) {
				
				response.setStatusCode(HttpStatus.OK.value());
				response.setFlag(1);
				response.setData(list);
				response.setMessage("Success");
				
			} else {
				response.setFlag(2);
				response.setData(list);
				response.setStatusCode(HttpStatus.OK.value());
				response.setMessage("No data found!");
			}
			response.setStatusCode(HttpStatus.OK.value());
			
		} catch (Exception e) {
			LOGGER.error("StateServiceImpl get all state method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(AppConstants.TRY_AGAIN);
			response.setErrorDiscription(e.getLocalizedMessage());
		}
		return response;
	}

	@Override
	public ResponseStructure addState(RequestModel dto) {
		ResponseStructure response = new ResponseStructure();
		try {
			StateModel model = new StateModel();

			model.setStateName(dto.getStateName());
			model.setCreatedBy(dto.getCreatedBy());
			model.setStateStatus(true);
			model.setCreatedDateTime(new Date());

			Optional<CountryModel> optional = countryRepository.findById(dto.getCountryId());
			if (optional.isPresent()) {
				CountryModel country = optional.get();
				model.setCountryModel(country);
			}
			stateRepository.save(model);
			
			response.setFlag(1);
			response.setData(model);
			response.setMessage(AppConstants.SUCCESS);
			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			LOGGER.error("StateServiceImpl add state method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(AppConstants.TRY_AGAIN);
			response.setErrorDiscription(e.getLocalizedMessage());
		}
		return response;
	}

	@Override
	public ResponseStructure getViewById(int id) {

		ResponseStructure response = new ResponseStructure();
		try {
			Optional<StateModel> model = stateRepository.findById(id);
			if (model.isPresent()) {
				StateModel entity = model.get();
				
				response.setFlag(1);
				response.setData(entity);
				response.setMessage("Success");
				response.setStatusCode(HttpStatus.OK.value());

			} else {
				response.setFlag(2);
				response.setMessage("State id not found!");
			}
			response.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			LOGGER.error("StateServiceImpl get state by id method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(AppConstants.TRY_AGAIN);
			response.setErrorDiscription(e.getLocalizedMessage());

		}
		return response;
	}

	@Override
	public ResponseStructure updateState(RequestModel dto, int id) {
		ResponseStructure response = new ResponseStructure();
		try {
			Optional<StateModel> stateModel = stateRepository.findById(id);
			if (stateModel.isPresent()) {
				StateModel entity = stateModel.get();
				entity.setModifiedBy(dto.getModifiedBy());
				entity.setModifiedDatetime(new Date());
				if (dto.getStateName() != null) {
					entity.setStateName(dto.getStateName());
				}

				stateRepository.save(entity);

				response.setFlag(1);
				response.setData(entity);
				response.setMessage(AppConstants.SUCCESS);
				response.setStatusCode(HttpStatus.OK.value());
			} else {
				response.setFlag(2);
				response.setMessage("State id does not exits!");
			}
			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			LOGGER.error("StateServiceImpl update state method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(AppConstants.TRY_AGAIN);
			response.setErrorDiscription(e.getLocalizedMessage());
		}
		return response;
	}

	@Override
	public ResponseStructure getStateByCountry(int countryId) {
		ResponseStructure response = new ResponseStructure();
		try {
			CountryModel id = countryRepository.findByCountryId(countryId);
			List<StateModel> stateModel = stateRepository.findByCountryModel(id);
			if (!stateModel.isEmpty()) {
				response.setFlag(1);
				response.setData(stateModel);
				response.setMessage(AppConstants.SUCCESS);
				response.setStatusCode(HttpStatus.OK.value());
			} else {
				response.setFlag(2);
				response.setMessage("States does not exist!");
			}
			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			LOGGER.error("StateServiceImpl update state method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(AppConstants.TRY_AGAIN);
			response.setErrorDiscription(e.getLocalizedMessage());
		}
		return response;
	}



}
