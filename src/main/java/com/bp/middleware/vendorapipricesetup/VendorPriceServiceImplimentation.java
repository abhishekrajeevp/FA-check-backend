package com.bp.middleware.vendorapipricesetup;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.vendorpricetracker.VendorPriceTracker;
import com.bp.middleware.vendorpricetracker.VendorPriceTrackerRepository;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

@Service
public class VendorPriceServiceImplimentation implements VendorPriceService {

	@Autowired
	private VendorPriceRepository vendorPriceRepository;

	@Autowired
	private VendorRepository vendorRepository;

	@Autowired
	private VendorVerificationRepository vendorVerificationRepository;

	@Autowired
	private VendorPriceTrackerRepository vendorPriceTrackerRepository;

	@Override
	public ResponseStructure addVendorPriceAndUrl(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<VendorModel> vendorOptional = vendorRepository.findById(model.getVendorId());
			Optional<VendorVerificationModel> vendorVerificationOptional = vendorVerificationRepository
					.findById(model.getVendorVerificationId());

			if (vendorOptional.isPresent() && vendorVerificationOptional.isPresent()) {

				VendorModel vendorModel = vendorOptional.get();
				VendorVerificationModel vendorVerificationModel = vendorVerificationOptional.get();

				VendorPriceModel vendorPatternCheck = vendorPriceRepository
						.findByVendorModelAndVendorVerificationModel(vendorModel, vendorVerificationModel);

				if (vendorPatternCheck == null) {

					VendorPriceModel vendorPriceModel = new VendorPriceModel();

					vendorPriceModel.setName(
							vendorVerificationModel.getVerificationDocument() + " " + vendorModel.getVendorName());
					vendorPriceModel.setApiLink(model.getApiLink());
					vendorPriceModel.setApplicationId(model.getApplicationId());
					vendorPriceModel.setApiKey(model.getApiKey());
					vendorPriceModel.setIdPrice(model.getIdPrice());
					vendorPriceModel.setImagePrice(model.getImagePrice());
					vendorPriceModel.setSignaturePrice(model.getSignaturePrice());
					vendorPriceModel.setStatus(true);
					vendorPriceModel.setCreatedBy(model.getCreatedBy());
					vendorPriceModel.setCreatedAt(LocalDate.now());
					vendorPriceModel.setVendorModel(vendorModel);
					vendorPriceModel.setVendorVerificationModel(vendorVerificationModel);

					vendorPriceRepository.save(vendorPriceModel);

					addVendorPriceTracker(vendorPriceModel);

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setData(vendorPriceModel);
					structure.setMessage(AppConstants.SUCCESS);

				} else {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(3);
					structure.setData(null);
					structure.setMessage("DATA ALREADY PRESENT FOR GIVEN VENDOR AND VENDOR_VERIFICATION");
				}

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} catch (Exception e) {

			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private void addVendorPriceTracker(VendorPriceModel vendorPriceModel) throws Exception {

		VendorPriceTracker priceTracker = new VendorPriceTracker();

		priceTracker.setIdAmount(vendorPriceModel.getIdPrice());
		priceTracker.setOcrAmount(vendorPriceModel.getImagePrice());
		priceTracker.setSignatureAmount(vendorPriceModel.getSignaturePrice());
		priceTracker.setApplicableFromDate(LocalDate.now());
		priceTracker.setCurrentlyInUse(true);
		priceTracker.setRemark("Added");
		priceTracker.setRecentIdentifier(1);
		priceTracker.setVendorModel(vendorPriceModel.getVendorModel());
		priceTracker.setVendorVerificationModel(vendorPriceModel.getVendorVerificationModel());

		vendorPriceTrackerRepository.save(priceTracker);
	}

	@Override
	public ResponseStructure viewByVendorPriceId(int vendorPriceId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<VendorPriceModel> optional = vendorPriceRepository.findById(vendorPriceId);
			if (optional.isPresent()) {
				VendorPriceModel vendorPriceModel = optional.get();

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorPriceModel);
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
	public ResponseStructure viewAllVendorPrice() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<VendorPriceModel> list = vendorPriceRepository.findAll();

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
	public ResponseStructure viewByVendorId(int vendorId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<VendorModel> optional = vendorRepository.findById(vendorId);

			if (optional.isPresent()) {

				VendorModel vendorModel = optional.get();

				List<VendorPriceModel> priceList = vendorPriceRepository.findByVendorModel(vendorModel);

				if (!priceList.isEmpty()) {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setData(priceList);
					structure.setMessage(AppConstants.SUCCESS);

				} else {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(2);
					structure.setData(null);
					structure.setMessage("NO API FOUND FOR THE GIVEN VENDOR");
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
	public ResponseStructure updateVendorPriceOrUrl(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<VendorPriceModel> optional = vendorPriceRepository.findById(model.getVendorPriceId());
			if (optional.isPresent()) {

				VendorPriceModel vendorPriceModel = optional.get();

				double id = vendorPriceModel.getIdPrice();
				double image = vendorPriceModel.getImagePrice();
				double sign = vendorPriceModel.getSignaturePrice();

				//vendorPriceModel.setName(model.getName());
				vendorPriceModel.setApiLink(model.getApiLink());
				vendorPriceModel.setApplicationId(model.getApplicationId());
				vendorPriceModel.setApiKey(model.getApiKey());
				vendorPriceModel.setIdPrice(model.getIdPrice());
				vendorPriceModel.setImagePrice(model.getImagePrice());
				vendorPriceModel.setSignaturePrice(model.getSignaturePrice());
				vendorPriceModel.setModifiedBy(model.getModifiedBy());
				vendorPriceModel.setModifiedAt(LocalDate.now());

				vendorPriceRepository.save(vendorPriceModel);

				if (model.getIdPrice() != id || model.getImagePrice() != image
						|| model.getSignaturePrice() != sign) {

					updateVendorPriceTracker(vendorPriceModel);
				}

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorPriceModel);
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

	private void updateVendorPriceTracker(VendorPriceModel vendorPriceModel) throws Exception {

		VendorModel vendorModel = vendorPriceModel.getVendorModel();
		VendorVerificationModel vendorVerificationModel = vendorPriceModel.getVendorVerificationModel();

		Optional<VendorPriceTracker> recentIdentifierOptOne = vendorPriceTrackerRepository
				.findByVendorModelAndVendorVerificationModelAndRecentIdentifier(vendorModel, vendorVerificationModel,
						1);
		Optional<VendorPriceTracker> recentIdentifierOptTwo = vendorPriceTrackerRepository
				.findByVendorModelAndVendorVerificationModelAndRecentIdentifier(vendorModel, vendorVerificationModel,
						2);

		if (recentIdentifierOptOne.isPresent()) {

			VendorPriceTracker vendorPriceTrackerOne = recentIdentifierOptOne.get();

			vendorPriceTrackerOne.setCurrentlyInUse(false);
			vendorPriceTrackerOne.setRemark("Outdated");
			vendorPriceTrackerOne.setRecentIdentifier(2);

			vendorPriceTrackerRepository.save(vendorPriceTrackerOne);
		}

		if (recentIdentifierOptTwo.isPresent()) {

			VendorPriceTracker vendorPriceTrackerTwo = recentIdentifierOptTwo.get();

			vendorPriceTrackerTwo.setRecentIdentifier(3);
			vendorPriceTrackerRepository.save(vendorPriceTrackerTwo);
		}

		VendorPriceTracker priceTracker = new VendorPriceTracker();

		priceTracker.setIdAmount(vendorPriceModel.getIdPrice());
		priceTracker.setOcrAmount(vendorPriceModel.getImagePrice());
		priceTracker.setSignatureAmount(vendorPriceModel.getSignaturePrice());
		priceTracker.setApplicableFromDate(LocalDate.now());
		priceTracker.setCurrentlyInUse(true);
		priceTracker.setRemark("Updated");
		priceTracker.setRecentIdentifier(1);
		priceTracker.setVendorModel(vendorPriceModel.getVendorModel());
		priceTracker.setVendorVerificationModel(vendorPriceModel.getVendorVerificationModel());

		vendorPriceTrackerRepository.save(priceTracker);
	}

	@Override
	public ResponseStructure updateVendorPriceStatus(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<VendorPriceModel> optional = vendorPriceRepository.findById(model.getVendorPriceId());
			if (optional.isPresent()) {

				VendorPriceModel vendorPriceModel = optional.get();

				vendorPriceModel.setStatus(model.isVendorPriceStatus());
				vendorPriceModel.setModifiedBy(model.getModifiedBy());
				vendorPriceModel.setModifiedAt(LocalDate.now());

				vendorPriceRepository.save(vendorPriceModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorPriceModel);
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
	public ResponseStructure viewByVendorIdAndVerificationId(int vendorId, int verificationId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			VendorModel vendorModel = vendorRepository.findByVendorId(vendorId);
			VendorVerificationModel vendorVerificationModel = vendorVerificationRepository
					.findByVendorVerificationId(verificationId);

			if (vendorModel != null && vendorVerificationModel != null) {

				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.findByVendorModelAndVendorVerificationModel(vendorModel, vendorVerificationModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorPriceModel);
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
	public ResponseStructure viewByVendorVerificationId(int vendorVerificationId) {
		ResponseStructure structure = new ResponseStructure();
		try {
			VendorVerificationModel vendorVerificationModel = vendorVerificationRepository
					.findByVendorVerificationId(vendorVerificationId);

			if (vendorVerificationModel != null) {

				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.findByVendorVerificationModel(vendorVerificationModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorPriceModel);
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
	public ResponseStructure viewByVendorPriceStatus(boolean status) {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<VendorPriceModel> list = vendorPriceRepository.findByStatus(status);

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
	public ResponseStructure updateAmount(int vendorPriceId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<VendorPriceModel> optional = vendorPriceRepository.findById(vendorPriceId);
			if (optional.isPresent()) {

				VendorPriceModel vendorPriceModel = optional.get();

				vendorPriceModel.setIdPrice(model.getIdPrice());
				vendorPriceModel.setImagePrice(model.getImagePrice());
				vendorPriceModel.setSignaturePrice(model.getSignaturePrice());
				vendorPriceModel.setModifiedBy(model.getModifiedBy());
				vendorPriceModel.setModifiedAt(LocalDate.now());

				vendorPriceRepository.save(vendorPriceModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(vendorPriceModel);
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
