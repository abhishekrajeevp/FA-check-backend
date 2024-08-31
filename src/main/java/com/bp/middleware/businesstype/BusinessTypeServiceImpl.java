package com.bp.middleware.businesstype;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@Service
public class BusinessTypeServiceImpl implements BusinessTypeService {

	@Autowired
	private BusinessTypeRepository repository;

	@Override
	public ResponseStructure addBusinessType(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		Optional<BusinessType> optional = repository.findByBusinessType(model.getBusinessType());

		if (optional.isEmpty()) {
			BusinessType type = new BusinessType();
			type.setBusinessType(model.getBusinessType());
			type.setCreatedBy(model.getCreatedBy());
			type.setCreatedDateAndTime(new Date());
			type.setStatus(true);

			repository.save(type);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(type);
			structure.setFlag(1);
		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Business Type already present");
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseStructure getBusinessTypeId(int businessTypeId) {

		ResponseStructure structure = new ResponseStructure();
		Optional<BusinessType> optional = repository.findById(businessTypeId);
		if (optional.isPresent()) {
			BusinessType type = optional.get();

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(type);
			structure.setFlag(1);

		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseStructure vieAllBusinessType() {

		ResponseStructure structure = new ResponseStructure();

		List<BusinessType> list = repository.findAll();
		if (!list.isEmpty()) {

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(list);
			structure.setFlag(1);

		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseStructure update(int businessTypeId, RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<BusinessType> optional = repository.findById(businessTypeId);

			if (optional.isPresent()) {
				BusinessType category = optional.get();

				BusinessType alreadyPresent = repository.getByBusinessType(model.getBusinessType());

				if (alreadyPresent == null || alreadyPresent.getBusinessTypeId()==category.getBusinessTypeId()) {

					category.setBusinessType(model.getBusinessType());
					category.setModifiedBy(model.getModifiedBy());
					category.setModifiedDateAndTime(new Date());
					repository.save(category);

					structure.setData(category);
					structure.setMessage(AppConstants.SUCCESS);
					structure.setFlag(1);
				} else {
					structure.setData(null);
					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setFlag(3);
				}
			} else {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure updateBusinessCategoryStatus(int businessTypeId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<BusinessType> optional = repository.findById(businessTypeId);

			if (optional.isPresent()) {
				BusinessType category = optional.get();

					category.setStatus(model.isAccountStatus());
					category.setModifiedBy(model.getModifiedBy());
					category.setModifiedDateAndTime(new Date());
					repository.save(category);

					structure.setData(category);
					structure.setMessage(AppConstants.SUCCESS);
					structure.setFlag(1);
					
			} else {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure viewAllBusinessTypeActive() {
	
		ResponseStructure structure = new ResponseStructure();

		List<BusinessType> list = repository.findByStatus(true);
		if (!list.isEmpty()) {

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(list);
			structure.setFlag(1);

		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

}
