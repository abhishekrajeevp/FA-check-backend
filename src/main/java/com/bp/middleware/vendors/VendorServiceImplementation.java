package com.bp.middleware.vendors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.DateUtil;

@Service
public class VendorServiceImplementation implements VendorService {

	@Autowired
	private VendorRepository vendorRepository;
	@Autowired
	private ResponseRepository responseRepository;

	@Override
	public ResponseStructure addVendors(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			VendorModel vendor = vendorRepository.findByVendorName(model.getVendorName());

			if (vendor == null) {

				VendorModel vendorModel = new VendorModel();
				vendorModel.setVendorName(model.getVendorName());
				vendorModel.setStatus(true);
				vendorModel.setCreatedBy(model.getCreatedBy());
				vendorModel.setCreatedAt(LocalDate.now());

				vendorRepository.save(vendorModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorModel);
				structure.setMessage(AppConstants.SUCCESS);

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage("SAME VENDOR ALREADY PRESENT.");
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

	@Override
	public ResponseStructure getVendorById(int vendorId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<VendorModel> optional = vendorRepository.findById(vendorId);
			if (optional.isPresent()) {
				VendorModel vendorModel = optional.get();

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorModel);
				structure.setMessage(AppConstants.SUCCESS);
			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
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

	@Override
	public ResponseStructure getAllVendors() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<VendorModel> list = vendorRepository.findAll();
			if (!list.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(list);
				structure.setMessage(AppConstants.SUCCESS);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
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

	@Override
	public ResponseStructure updateVendors(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<VendorModel> optional = vendorRepository.findById(model.getVendorId());
			if (optional.isPresent()) {
				VendorModel vendorModel = optional.get();

				vendorModel.setVendorName(model.getVendorName());
				vendorModel.setModifiedBy(model.getModifiedBy());
				vendorModel.setModifiedAt(LocalDate.now());
				vendorRepository.save(vendorModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorModel);
				structure.setMessage(AppConstants.SUCCESS);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
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

	@Override
	public ResponseStructure updateVendorStatus(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<VendorModel> optional = vendorRepository.findById(model.getVendorId());
			if (optional.isPresent()) {
				VendorModel vendorModel = optional.get();

				vendorModel.setStatus(model.isVendorStatus());
				vendorModel.setModifiedBy(model.getModifiedBy());
				vendorModel.setModifiedAt(LocalDate.now());

				vendorRepository.save(vendorModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorModel);
				structure.setMessage(AppConstants.SUCCESS);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
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

	@Override
	public void monthlyVendorHitCount() {

		List<VendorModel> vendorList = vendorRepository.findAll();
		int i = 1;
		for (VendorModel vendorModel : vendorList) {

			List<Response> list = responseRepository.findByVendorModel(vendorModel);

			int vendorReqCount = 0;

			LocalDate currentDate = LocalDate.now();
			int currentMonth = currentDate.getMonthValue();
			int currentYear = currentDate.getYear();

			for (Response response : list) {
				LocalDate reqDate = DateUtil.convertUtilDateToLocalDate(response.getRequestDateAndTime());

				int responseTableMonth = reqDate.getMonthValue();
				int responseTableYear = reqDate.getYear();

				if (currentMonth == responseTableMonth && currentYear == responseTableYear) {
					vendorReqCount++;
				}
			}

			vendorModel.setMonthlyCount(vendorReqCount);
			vendorRepository.save(vendorModel);

			System.out.println("Vendor : " + i++);
		}

	}

	@Override
	public ResponseStructure getAllActiveVendors() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<VendorModel> list = vendorRepository.findBystatus(true);
			if (!list.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(list);
				structure.setMessage(AppConstants.SUCCESS);


			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
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
