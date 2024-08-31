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
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.admin.RequestModel;


@Service
public class CountryServiceImpl implements CountryService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CountryServiceImpl.class);

	@Autowired
	private CountryRepository countryRepository;

	@Override
	public ResponseStructure getAllCountry() {
		ResponseStructure response = new ResponseStructure();
		try {
			List<CountryModel> list = countryRepository.findAll();
			if (!list.isEmpty()) {
				
				response.setFlag(1);
				response.setData(list);
				response.setMessage(AppConstants.SUCCESS);
				response.setStatusCode(HttpStatus.OK.value());
				
			} else {
				response.setFlag(2);
				response.setData(null);
				response.setStatusCode(HttpStatus.OK.value());
				response.setMessage("No data found!");
			}
			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			
			LOGGER.error("CountryServiceImpl get all country method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(AppConstants.TRY_AGAIN);
			response.setErrorDiscription(e.getLocalizedMessage());
		}
		return response;
	}

	@Override
	public ResponseStructure addCountry(RequestModel dto) {
		ResponseStructure response = new ResponseStructure();
		try {
			CountryModel countryModel = new CountryModel();

			countryModel.setCountryName(dto.getCountryName());
			countryModel.setCountryCode(dto.getCountryCode());
			countryModel.setCountryCurrency(dto.getCountryCurrency());

			countryModel.setCountryStatus(true);
			countryModel.setCreatedBy(dto.getCreatedBy());
			countryModel.setCreatedDatetime(new Date());
			countryModel.setCountryTimezone(dto.getCountryTimezone());

			countryRepository.save(countryModel);
			response.setFlag(1);
			response.setData(countryModel);
			response.setMessage(AppConstants.SUCCESS);
			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			LOGGER.error("CountryServiceImpl add country method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(AppConstants.TRY_AGAIN);
			response.setErrorDiscription(e.getLocalizedMessage());
		}
		return response;
	}

	@Override
	public ResponseStructure updateCountry(RequestModel dto, int id) {
		ResponseStructure response = new ResponseStructure();
		try {
			Optional<CountryModel> countryModel = countryRepository.findById(id);
			if (countryModel.isPresent()) {
				CountryModel entity = countryModel.get();

				entity.setModifiedDatetime(new Date());
				if (dto.getCountryName() != null) {
					entity.setCountryName(dto.getCountryName());
				}
				if (dto.getCountryCode() != null) {
					entity.setCountryCode(dto.getCountryCode());
				}
				if (dto.getCountryCurrency() != null) {
					entity.setCountryCurrency(dto.getCountryCurrency());
				}
				if (dto.getCountryTimezone() != null) {
					entity.setCountryTimezone(dto.getCountryTimezone());
				}
				if (dto.getModifiedBy() != null) {
					entity.setModifiedBy(dto.getModifiedBy());
				}
				countryRepository.save(entity);
				response.setFlag(1);
				response.setData(entity);
				response.setMessage(AppConstants.SUCCESS);
			} else {
				response.setFlag(2);
				response.setMessage("Country id does not exists!");
			}

			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			LOGGER.error("CountryServiceImpl update country method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(AppConstants.TRY_AGAIN);
			response.setErrorDiscription(e.getLocalizedMessage());
		}
		return response;
	}

}
