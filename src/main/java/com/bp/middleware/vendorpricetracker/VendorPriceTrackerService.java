package com.bp.middleware.vendorpricetracker;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.usermanagement.UserManagement;
import com.bp.middleware.util.AppConstants;

@Service
public class VendorPriceTrackerService {

	
	@Autowired
	private VendorPriceTrackerRepository vendorPriceTrackerRepository;

	public ResponseStructure viewAll() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<VendorPriceTracker> tracker = vendorPriceTrackerRepository.findAll();

			if (tracker.isEmpty()) {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			} else {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(tracker);
				structure.setFlag(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	public ResponseStructure viewByCurrentlyActive() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<VendorPriceTracker> tracker = vendorPriceTrackerRepository.findByCurrentlyInUse(true);

			if (tracker.isEmpty()) {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			} else {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(tracker);
				structure.setFlag(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	public ResponseStructure viewByLastlyUsed() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<VendorPriceTracker> tracker = vendorPriceTrackerRepository.findByRecentIdentifier(2);

			if (tracker.isEmpty()) {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			} else {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(tracker);
				structure.setFlag(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	public ResponseStructure viewById(int id) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<VendorPriceTracker> tracker = vendorPriceTrackerRepository.findById(id);

			if (tracker.isEmpty()) {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			} else {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(tracker.get());
				structure.setFlag(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	
	
	public ResponseStructure viewByActivatedToday() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<VendorPriceTracker> tracker = vendorPriceTrackerRepository.findByApplicableFromDateAndCurrentlyInUse(LocalDate.now(),true);

			if (tracker.isEmpty()) {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			} else {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(tracker);
				structure.setFlag(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}
	
}

