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
public class PincodeServiceImpl implements PincodeService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PincodeServiceImpl.class);

	@Autowired
	private PinCodeRepository pincodeRepocitory;

	@Autowired
	private CityRepository cityRepository;

	public ResponseStructure getAllPincode() {

		ResponseStructure response = new ResponseStructure();
		try {
			List<PincodeModel> list = pincodeRepocitory.findAll();
			if (list.isEmpty()) {
				response.setFlag(2);
				response.setData(null);
				response.setMessage(AppConstants.NO_DATA_FOUND);

			} else {
				response.setFlag(1);
				response.setData(list);
				response.setMessage(AppConstants.SUCCESS);

			}
			response.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			response.setFlag(3);
			LOGGER.error("PincodeServiceImpl get all pincode method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setErrorDiscription(AppConstants.TRY_AGAIN);
			response.setMessage(e.getLocalizedMessage());
		}
		return response;

	}

	@Override
	public ResponseStructure addPincode(RequestModel commonrequestdto) {

		ResponseStructure response = new ResponseStructure();

		try {

			Optional<CityModel> model = cityRepository.findById(commonrequestdto.getCityId());

			if (model.isPresent()) {
				CityModel city = model.get();
				PincodeModel pincode = pincodeRepocitory.findByPincode(commonrequestdto.getPincode());
				if (pincode!=null) {
					response.setFlag(2);
					response.setMessage("Pincode already exists!");
				} else {
					PincodeModel entity = new PincodeModel();
					
					entity.setPincodestatus(true);
					entity.setCreateddateandtime(new Date());
					entity.setCreatedby(commonrequestdto.getCreatedBy());
					entity.setPincode(commonrequestdto.getPincode());
					
					entity.setCityModel(city);
					pincodeRepocitory.save(entity);

					response.setFlag(1);
					response.setData(entity);
					response.setMessage("Pincode added successfully!");
				}
			} else {
				response.setFlag(3);
				response.setMessage("City id not exists");
			}
			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			response.setFlag(4);
			LOGGER.error("CityServiceImpl add city method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setErrorDiscription(e.getLocalizedMessage());
			response.setMessage(AppConstants.TRY_AGAIN);
		}
		return response;

	}

	@Override
	public ResponseStructure getByPincode(int pincodeId) {
		ResponseStructure response = new ResponseStructure();
		try {
			Optional<PincodeModel> model = pincodeRepocitory.findById(pincodeId);
			if (model.isPresent()) {
				PincodeModel pinCodeModel = model.get();
				response.setFlag(1);
				response.setData(pinCodeModel);
				response.setMessage("Success");
			} else {
				response.setFlag(2);
				response.setMessage("Pincode id does not exists!");

			}
			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			response.setFlag(3);
			LOGGER.error("CityServiceImpl get city method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(AppConstants.TRY_AGAIN);
			response.setErrorDiscription(e.getLocalizedMessage());
		}
		return response;

	}

	public ResponseStructure getPincode(int id) {
		ResponseStructure response = new ResponseStructure();
		try {

			Optional<CityModel> entity = cityRepository.findById(id);
			if (entity.isPresent()) {
				CityModel model = entity.get();
				List<PincodeModel> list = pincodeRepocitory.findByCityModel(model);
				if (list.isEmpty()) {
					response.setFlag(2);
					response.setData(list);
					response.setMessage("No data Found!");
				} else {
					response.setFlag(1);
					response.setData(list);
					response.setMessage("Success");

				}
			} else {
				response.setFlag(3);
				response.setMessage("City id does not exits!");

			}

			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			response.setFlag(4);
			LOGGER.error("PincodeServiceImpl get pincode method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(AppConstants.TRY_AGAIN);
			response.setErrorDiscription(e.getLocalizedMessage());
		}
		return response;
	}

	@Override
	public ResponseStructure updatePincode(int pincodeId, RequestModel commonrequestdto) {
		ResponseStructure response = new ResponseStructure();
		try {
			Optional<PincodeModel> entity = pincodeRepocitory.findById(pincodeId);
			if (entity.isPresent()) {
				PincodeModel pincode = entity.get();

				pincode.setPincode(commonrequestdto.getPincode());
				pincode.setModifiedBy(commonrequestdto.getModifiedBy());
				pincode.setModifiedDateTime(new Date());

				pincodeRepocitory.save(pincode);
				response.setFlag(1);
				response.setMessage("Success!");
				response.setData(pincode);
			} else {
				response.setFlag(2);
				response.setMessage("pincode id does not exists!");
			}
			response.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			response.setFlag(3);
			LOGGER.error("PinCodeServiceImpl update pincode method", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			response.setMessage(AppConstants.TRY_AGAIN);
			response.setErrorDiscription(e.getLocalizedMessage());
		}
		return response;
	}

	@Override
	public ResponseStructure listAll() {
		ResponseStructure structure=new ResponseStructure();
		List<PincodeModel> list = pincodeRepocitory.findAll();
		if (list.isEmpty()) {
			
			structure.setData(null);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
		} else {
			
			structure.setData(list);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		return structure;
	}


}
