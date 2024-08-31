package com.bp.middleware.businesscategory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.businesstype.BusinessType;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@Service
public class BusinessCategoryServiceImpl implements BusinessCategoryService {

	@Autowired
	private BusinessCategoryRepository repository;

	@Override
	public ResponseStructure addcategory(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		Optional<BusinessCategory> optional = repository.findByBusinessCategoryName(model.getBusinessCategoryName());

		if (optional.isEmpty()) {
			BusinessCategory category = new BusinessCategory();
			category.setBusinessCategoryName(model.getBusinessCategoryName());
			category.setCreatedBy(model.getCreatedBy());
			category.setCreatedDateAndTime(new Date());
			category.setStatus(true);
			repository.save(category);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(category);
			structure.setFlag(1);
		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Business Categoy already present");
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseStructure getByBusinessId(int businessCategoryId) {

		ResponseStructure structure = new ResponseStructure();
		Optional<BusinessCategory> optional = repository.findById(businessCategoryId);
		if (optional.isPresent()) {
			BusinessCategory category = optional.get();

			structure.setData(category);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
		} else {
			structure.setData(null);
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		return structure;
	}

	@Override
	public ResponseStructure viewAll() {
		ResponseStructure structure = new ResponseStructure();
		List<BusinessCategory> list = repository.findAll();

		if (!list.isEmpty()) {

			structure.setData(list);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);

		} else {
			structure.setData(null);
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		return structure;
	}

	@Override
	public ResponseStructure update(int businessCategoryId, RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<BusinessCategory> optional = repository.findById(businessCategoryId);

			if (optional.isPresent()) {

				BusinessCategory category = optional.get();

				BusinessCategory alreadyPresent = repository.getByBusinessCategoryName(model.getBusinessCategoryName());

				if (alreadyPresent == null
						|| alreadyPresent.getBusinessCategoryId() == category.getBusinessCategoryId()) {

					category.setBusinessCategoryName(model.getBusinessCategoryName());
					category.setModifiedBy(model.getModifiedBy());
					category.setModifiedDateAndTime(new Date());
					repository.save(category);

					structure.setData(category);
					structure.setMessage(AppConstants.SUCCESS);
					structure.setFlag(1);
				} else {

					structure.setData(null);
					structure.setMessage("Business category already exist");
					structure.setFlag(2);
				}
			} else {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(3);
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
	public ResponseStructure updateBusinessCategoryStatus(int businessCategoryId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<BusinessCategory> optional = repository.findById(businessCategoryId);

			if (optional.isPresent()) {

				BusinessCategory category = optional.get();

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
				structure.setFlag(3);
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
	public ResponseStructure viewAllActive() {
		ResponseStructure structure = new ResponseStructure();
		List<BusinessCategory> list = repository.findByStatus(true);

		if (!list.isEmpty()) {

			structure.setData(list);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);

		} else {
			structure.setData(null);
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		return structure;
	}
}

//	
//	public ResponseStructure update(int businessCategoryId, RequestModel model) {
//
//		ResponseStructure structure =new ResponseStructure();
//		
//		try {
//			Optional<BusinessCategory> optional = repository.findById(businessCategoryId);
//			if (optional.isPresent()) {
//				BusinessCategory category = optional.get();
//				category.setBusinessCategoryName(model.getBusinessCategoryName());
//				category.setModifiedBy(model.getModifiedBy());
//				category.setModifiedDateAndTime(new Date());
//				repository.save(category);
//				
//				structure.setData(category);
//				structure.setMessage(AppConstants.SUCCESS);
//				structure.setFlag(1);
//			} else {
//				structure.setData(null);
//				structure.setMessage(AppConstants.NO_DATA_FOUND);
//				structure.setFlag(2);
//			}
//			structure.setStatusCode(HttpStatus.OK.value());
//		} catch (Exception e) {
//			structure.setData(null);
//			structure.setErrorDiscription(e.getMessage());
//			structure.setMessage(AppConstants.TECHNICAL_ERROR);
//			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
//			structure.setFlag(3);
//		}
//		return structure;
//	}
