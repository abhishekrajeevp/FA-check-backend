package com.bp.middleware.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.admin.RequestModel;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.util.AppConstants;

@Service
public class ServicesServiceImplementation implements ServicesService{

	@Autowired
	private ServicesRepository serveRepository;

	@Override
	public ResponseStructure addServices(RequestModel model) {
		ResponseStructure structure =new ResponseStructure();
		try {
			ServicesEntity entity=new ServicesEntity();
			entity.setServices(model.getServices());
			entity.setStatus(true);
			entity.setCreatedBy(model.getCreatedBy());
			entity.setCreatedDate(new Date());
			serveRepository.save(entity);
			
			structure.setMessage( AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(entity);
			structure.setFlag(1);
		} catch (Exception e) {
			structure.setMessage( AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseStructure updateServices(RequestModel model) {
		ResponseStructure structure =new ResponseStructure();
		try {
			Optional<ServicesEntity> optional=serveRepository.findById(model.getServiceId());
			if (optional.isPresent()) {
				ServicesEntity entity=optional.get();
				entity.setServices(model.getServices());
				entity.setCreatedBy(model.getCreatedBy());
				entity.setModifiedBy(model.getModifiedBy());
				entity.setModifiedDate(new Date());
				
				serveRepository.save(entity);
				structure.setMessage( AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entity);
				structure.setFlag(1);
				
			} else {
				structure.setMessage( AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			structure.setMessage( AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseStructure getById(int id) {
		ResponseStructure structure =new ResponseStructure();
		try {
			Optional<ServicesEntity> optional=serveRepository.findById(id);
			if (optional.isPresent()) {
				ServicesEntity entity=optional.get();
				structure.setMessage( AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entity);
				structure.setFlag(1);
				
			} else {
				structure.setMessage( AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			structure.setMessage( AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseStructure viewAll() {
		ResponseStructure structure =new ResponseStructure();
		try {
			List<ServicesEntity> list=serveRepository.findAll();
			if (!list.isEmpty()) {
			
				structure.setMessage( AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(list);
				structure.setFlag(1);
				
				
			} else {
				structure.setMessage( AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			structure.setMessage( AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseStructure accountStatusChange(RequestModel model) {
		ResponseStructure structure =new ResponseStructure();
		try {
			Optional<ServicesEntity> optional=serveRepository.findById(model.getServiceId());
			if (optional.isPresent()) {
				ServicesEntity entity=optional.get();
				entity.setStatus(model.isStatus());
				serveRepository.save(entity);
				
				structure.setMessage( AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entity);
				structure.setFlag(1);
			} else {
				structure.setMessage( AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			structure.setMessage( AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	
	
	
	
	
	
}
