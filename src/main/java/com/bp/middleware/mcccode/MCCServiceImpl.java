package com.bp.middleware.mcccode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.businesscategory.BusinessCategory;
import com.bp.middleware.businesstype.BusinessType;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@Service
public class MCCServiceImpl implements MCCService {

	private static final Logger logger = LoggerFactory.getLogger(MCCServiceImpl.class);
	@Autowired
	private MCCRepository mccRepo;

	@Override
	public ResponseStructure addMCCCode(RequestModel dto) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<MCCCodesModel> optional = mccRepo.findByMccCode(dto.getMccCode());

			MCCCodesModel model = new MCCCodesModel();

			if (optional.isEmpty()) {
				model.setMccCode(dto.getMccCode());
				model.setCorpoarteType(dto.getCorporateType());
				model.setCreatedBy(dto.getCreatedBy());
				model.setStatus(true);
				SimpleDateFormat sdf = new SimpleDateFormat();
				model.setCreatedDateTime(sdf.format(new Date()));
				mccRepo.save(model);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(model);
				structure.setFlag(1);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Mcc Code already present");
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			logger.error("MCCServiceImpl add mcc code", e);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;

	}

	@Override
	public ResponseStructure viewMCCCodeById(int mccId) {

		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<MCCCodesModel> model = mccRepo.findById(mccId);

			if (model.isPresent()) {
				MCCCodesModel entity = model.get();

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entity);
				structure.setFlag(1);
				structure.setMessage("Mcc code detail viewed by id");
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
				structure.setMessage("Mcc Code id is not exist");
			}
		} catch (Exception e) {

			logger.error("MCCServiceImpl mcc code viewed", e);
			structure.setFlag(3);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
		}
		return structure;
	}

	@Override
	public ResponseStructure viewAllMCCCode() {

		ResponseStructure structure = new ResponseStructure();
		try {
			List<MCCCodesModel> list = mccRepo.findAll();

			if (list.isEmpty()) {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
				structure.setMessage("MCC Codes not found");

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(list);
				structure.setFlag(1);
				structure.setMessage("All MCC codes viewed");
			}
		} catch (Exception e) {

			logger.error("MCCServiceImpl all mcc code viewed", e);
			structure.setFlag(3);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
		}
		return structure;
	}

	@Override
	public ResponseStructure updateMCCCodeById(int mccId, RequestModel dto) {

		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<MCCCodesModel> model = mccRepo.findById(mccId);

			if (model.isPresent()) {
				MCCCodesModel entity = model.get();

				MCCCodesModel alreadyPresent = mccRepo.getByMccCode(dto.getMccCode());

				if (alreadyPresent == null || alreadyPresent.getMccId()==entity.getMccId()) {

					entity.setMccCode(dto.getMccCode());
					entity.setCorpoarteType(dto.getCorporateType());
					entity.setModifiedBy(dto.getModifiedBy());
					SimpleDateFormat sdf = new SimpleDateFormat();
					entity.setModifiedDateAndTime(sdf.format(new Date()));
					mccRepo.save(entity);

					structure.setFlag(1);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(entity);
					structure.setMessage("MCC code status updated");
				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(3);
					structure.setMessage("MCC code already present");
				}
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
				structure.setMessage("MCC code id not present");
			}

		} catch (Exception e) {

			logger.error("MCCServiceImpl mcc code updated", e);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
		}
		return structure;
	}

	@Override
	public ResponseStructure deletMCCCodeById(int mccId) {

		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<MCCCodesModel> model = mccRepo.findById(mccId);

			if (model.isPresent()) {
				MCCCodesModel entity = model.get();
				mccRepo.deleteById(entity.getMccId());

				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entity);
				structure.setMessage("MCC code id is deleted");

			} else {
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setMessage("MCC code id not present");
			}

		} catch (Exception e) {

			logger.error("MCCServiceImpl mcc code deleted", e);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);

		}
		return structure;
	}

	@Override
	public ResponseStructure changeAccountStatus(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<MCCCodesModel> optional = mccRepo.findById(model.getMccId());
			if (optional.isPresent()) {
				
				MCCCodesModel statusChange = optional.get();
				statusChange.setStatus(model.isAccountStatus());
				statusChange.setModifiedBy(model.getModifiedBy());
				
				SimpleDateFormat sdf = new SimpleDateFormat();
				statusChange.setModifiedDateAndTime(sdf.format(new Date()));
				
				mccRepo.save(statusChange);

				structure.setMessage("Success");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(statusChange);
				structure.setFlag(1);
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);

			}
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseStructure viewAllMCCCodeActive() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<MCCCodesModel> list = mccRepo.findByStatus(true);

			if (list.isEmpty()) {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
				structure.setMessage("MCC Codes not found");

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(list);
				structure.setFlag(1);
				structure.setMessage("All MCC codes viewed");
			}
		} catch (Exception e) {

			logger.error("MCCServiceImpl all mcc code viewed", e);
			structure.setFlag(3);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
		}
		return structure;
	}

}
