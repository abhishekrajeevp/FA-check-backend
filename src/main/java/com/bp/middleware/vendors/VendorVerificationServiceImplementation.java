package com.bp.middleware.vendors;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@Service
public class VendorVerificationServiceImplementation implements VendorVerificationService {

	@Autowired
	private VendorVerificationRepository vendorVerifyRepository;

	@Autowired
	private VendorRepository vendorRepository;

	@Override
	public ResponseStructure addVendorVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			VendorVerificationModel alreadyPresent = vendorVerifyRepository
					.findByVerificationDocument(model.getVerificationDocument());

			if (alreadyPresent == null) {

				VendorVerificationModel vendorverification = new VendorVerificationModel();

				vendorverification.setVerificationDocument(model.getVerificationDocument());
				vendorverification.setStatus(true);
				vendorverification.setCreatedBy(model.getCreatedBy());
				vendorverification.setCreatedAt(LocalDate.now());

				vendorVerifyRepository.save(vendorverification);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorverification);
				structure.setMessage(AppConstants.SUCCESS);

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage("SAME VERIFICATION ALREADY PRESENT.");
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
	public ResponseStructure viewVendorVerification(int vendorVerifyId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<VendorVerificationModel> optional = vendorVerifyRepository.findById(vendorVerifyId);
			if (optional.isPresent()) {
				VendorVerificationModel vendorVerificationModel = optional.get();

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorVerificationModel);
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
	public ResponseStructure viewAllVendorVerify() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<VendorVerificationModel> list = vendorVerifyRepository.findAll();
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
	public ResponseStructure updateVendorVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<VendorVerificationModel> optional = vendorVerifyRepository
					.findById(model.getVendorVerificationId());
			if (optional.isPresent()) {

				VendorVerificationModel vendorVerificationModel = optional.get();

				vendorVerificationModel.setVerificationDocument(model.getVerificationDocument());
				// vendorVerificationModel.setType(model.getType());
				vendorVerificationModel.setModifiedby(model.getModifiedBy());
				vendorVerificationModel.setModifiedAt(LocalDate.now());

				vendorVerifyRepository.save(vendorVerificationModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorVerificationModel);
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
	public ResponseStructure updateVendorVerificationStatus(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<VendorVerificationModel> optional = vendorVerifyRepository
					.findById(model.getVendorVerificationId());
			if (optional.isPresent()) {

				VendorVerificationModel vendorVerificationModel = optional.get();

				vendorVerificationModel.setStatus(model.isVendorVerifyStatus());
				vendorVerificationModel.setModifiedby(model.getModifiedBy());
				vendorVerificationModel.setModifiedAt(LocalDate.now());

				vendorVerifyRepository.save(vendorVerificationModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorVerificationModel);
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
	public ResponseStructure viewAllVendorVerificationsByStatus(boolean status) {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<VendorVerificationModel> list = vendorVerifyRepository.findByStatus(status);
			if (!list.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(list);
				
				if(status) {
					structure.setMessage("Active verifications list");
				}else {
					structure.setMessage("Inactive verifications list");
				}
				

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
	public ResponseStructure activeVerifications() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<VendorVerificationModel> list = vendorVerifyRepository.findByStatus(true);
			if (!list.isEmpty()) {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(list);
				structure.setMessage("All active verifications list");

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
