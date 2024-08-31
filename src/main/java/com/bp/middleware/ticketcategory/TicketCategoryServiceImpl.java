package com.bp.middleware.ticketcategory;

import java.util.ArrayList;
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
public class TicketCategoryServiceImpl implements TicketCategoryService {
	
	@Autowired
	private TicketCategoryRepository repository;

	@Override
	public ResponseStructure saveCategoryType(RequestModel model) {
		
		ResponseStructure structure=new ResponseStructure();
		
		TicketCategory category=new TicketCategory();
		
		category.setTicketType(model.getTicketType());
		category.setCreatedDateAndTime(new Date());
		category.setCreatedBy(model.getCreatedBy());
		category.setStatus(true);
		repository.save(category);
		
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setMessage(AppConstants.SUCCESS);
		structure.setData(category);
		structure.setFlag(1);
		
		return structure;
	}

	@Override
	public ResponseStructure updateTicket(RequestModel model, int ticketCategoryId) {

		ResponseStructure structure=new ResponseStructure();
		Optional<TicketCategory> optional = repository.findById(ticketCategoryId);
		if (optional.isPresent()) {
			TicketCategory category = optional.get();
			category.setTicketType(model.getTicketType());
			category.setModifiedBy(model.getModifiedBy());
			category.setModifiedDateAndTime(new Date());
			
			repository.save(category);
			
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(category);
			structure.setFlag(1);
		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseStructure viewAllTicketCategories() {
		ResponseStructure structure=new ResponseStructure();
		try {
			
			List<TicketCategory> list = repository.findAll();
			if (!list.isEmpty()) {

				structure.setData(list);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
			} else {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}
			
		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

}
