package com.bp.middleware.merchantapipricesetup;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.admin.AdminRepository;
import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.merchantpricetracker.MerchantPriceTracker;
import com.bp.middleware.merchantpricetracker.MerchantPriceTrackerRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

@Service
public class MerchantPriceServiceImplimentation implements MerchantPriceService {

	@Autowired
	private MerchantPriceRepository merchantPriceRepository;
	@Autowired
	private VendorRepository vendorRepository;
	@Autowired
	private VendorVerificationRepository vendorVerificationRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private VendorPriceRepository vendorPriceRepository;
	@Autowired
	private MerchantPriceTrackerRepository merchantPriceTrackerRepository;
	@Autowired
    EmailService emailService;
	@Autowired
	private AdminRepository adminRepository;
	

	@Override
	public ResponseStructure addMerchantPriceAndUrl(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<VendorModel> vendorOptional = vendorRepository.findById(model.getVendorId());
			Optional<VendorVerificationModel> vendorVerificationOptional = vendorVerificationRepository
					.findById(model.getVendorVerificationId());

			Optional<EntityModel> entityOptional = userRepository.findById(model.getUserId());

			if (vendorOptional.isPresent() && vendorVerificationOptional.isPresent() && entityOptional.isPresent()) {

				VendorModel vendorModel = vendorOptional.get();
				VendorVerificationModel vendorVerificationModel = vendorVerificationOptional.get();

				int prioritySize = vendorRepository.findAll().size();

				EntityModel entityModel = entityOptional.get();

				VendorPriceModel vendorPriceModel = vendorPriceRepository
						.findByVendorModelAndVendorVerificationModel(vendorModel, vendorVerificationModel);

				MerchantPriceModel merchantPatternCheck = merchantPriceRepository
						.findByEntityModelAndVendorModelAndVendorVerificationModel(entityModel, vendorModel,
								vendorVerificationModel);
				MerchantPriceModel priorityCheck = merchantPriceRepository
						.findByEntityModelAndVendorVerificationModelAndPriority(entityModel, vendorVerificationModel,
								model.getPriority());

				if (vendorPriceModel != null && merchantPatternCheck == null && priorityCheck == null
						&& model.getPriority() <= prioritySize && model.getPriority() != 0
						&& model.getIdPrice() >= vendorPriceModel.getIdPrice()
						&& model.getImagePrice() >= vendorPriceModel.getImagePrice()
						&& model.getSignaturePrice() >= vendorPriceModel.getSignaturePrice()) {

					MerchantPriceModel merchantPriceModel = new MerchantPriceModel();

					merchantPriceModel.setName(vendorPriceModel.getName());
					merchantPriceModel.setApiLink(model.getApiLink());
					merchantPriceModel.setApplicationId(model.getApplicationId());
					merchantPriceModel.setApiKey(model.getApiKey());
					merchantPriceModel.setIdPrice(model.getIdPrice());
					merchantPriceModel.setImagePrice(model.getImagePrice());
					merchantPriceModel.setSignaturePrice(model.getSignaturePrice());
					merchantPriceModel.setStatus(true);
					merchantPriceModel.setNoSourceCheck(model.isNoSourceCheck());
					merchantPriceModel.setCreatedBy(model.getCreatedBy());
					merchantPriceModel.setCreatedAt(LocalDate.now());
					merchantPriceModel.setVendorModel(vendorModel);
					merchantPriceModel.setVendorVerificationModel(vendorVerificationModel);
					merchantPriceModel.setEntityModel(entityModel);
					merchantPriceModel.setAccepted(true);
					merchantPriceModel.setPriority(model.getPriority());

					merchantPriceRepository.save(merchantPriceModel);

					addMerchantPriceTracker(merchantPriceModel);

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setData(merchantPriceModel);
					structure.setMessage(AppConstants.SUCCESS);

				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(3);
					structure.setData(null);

					if (vendorPriceModel == null) {
						structure.setMessage(
								"VENDOR PRICE MODEL IS NOT FOUND FOR THE GIVEN VENDOR WITH THE SPECIFIED VERIFICATION");

					} else if (merchantPatternCheck != null) {
						structure.setMessage(
								"THIS MERCHANT HAS ALREADY BEEN GRANTED ACCESS TO THE PROVIDED VERIFICATION BY THE SPECIFIED VENDOR");
					} else if (priorityCheck != null) {
						structure.setMessage(
								"THIS PRIORITY HAS BEEN ALREADY ASSIGNED FOR THE SPECIFIED VERIFICATION TYPE");
					} else if (model.getPriority() > prioritySize) {

						int extendedValue = prioritySize + 1;
						structure.setMessage("PRIORITY SHOULD BE LESS THAN " + extendedValue);

					} else if (model.getPriority() == 0) {
						structure.setMessage("PRIORITY SHOULDN'T BE 0");
					} else if (model.getIdPrice() < vendorPriceModel.getIdPrice()
							|| model.getSignaturePrice() < vendorPriceModel.getSignaturePrice()
							|| model.getImagePrice() < vendorPriceModel.getImagePrice()) {
						structure.setMessage(
								"MERCHANT VERIFICATION PRICE SHOULD BE EQUAL OR GREATER THAN VENDOR VERIFICATION PRICE");
					}
				}

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);

				if (vendorOptional.isEmpty()) {
					structure.setMessage("VENDOR NOT FOUND");
				} else if (entityOptional.isEmpty()) {
					structure.setMessage("ENTITY NOT FOUND");
				} else if (vendorVerificationOptional.isEmpty()) {
					structure.setMessage("VENDOR VERIFICATION NOT FOUND");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	public void addMerchantPriceTracker(MerchantPriceModel merchantPriceModel) throws Exception {

		MerchantPriceTracker priceTracker = new MerchantPriceTracker();

		priceTracker.setIdAmount(merchantPriceModel.getIdPrice());
		priceTracker.setOcrAmount(merchantPriceModel.getImagePrice());
		priceTracker.setSignatureAmount(merchantPriceModel.getSignaturePrice());
		priceTracker.setApplicableFromDate(LocalDate.now());
		priceTracker.setCurrentlyInUse(true);
		priceTracker.setMailSend(true);
		priceTracker.setRemark("Added");
		priceTracker.setRecentIdentifier(1);
		priceTracker.setVendorModel(merchantPriceModel.getVendorModel());
		priceTracker.setVendorVerificationModel(merchantPriceModel.getVendorVerificationModel());
		priceTracker.setEntityModel(merchantPriceModel.getEntityModel());
		priceTracker.setPriority(merchantPriceModel.getPriority());

		merchantPriceTrackerRepository.save(priceTracker);
	}

	@Override
	public ResponseStructure addMerchantPriceInList(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<VendorVerificationModel> vendorVerificationOptional = vendorVerificationRepository
					.findById(model.getVendorVerificationId());
			Optional<EntityModel> entityOptional = userRepository.findById(model.getUserId());

			List<RequestModel> merchantPriceAsList = model.getMerchantPriceList();
			List<MerchantPriceModel> savedList = new ArrayList<>();

			List<VendorModel> totalActiveVendor = vendorRepository.findBystatus(true);
			int prioritySize = totalActiveVendor.size();

			int savedCount = 0;
			int neglectedCount = 0;

			if (vendorVerificationOptional.isPresent() && entityOptional.isPresent()) {

				VendorVerificationModel vendorVerificationModel = vendorVerificationOptional.get();
				EntityModel entityModel = entityOptional.get();

				List<MerchantPriceModel> priorityList = merchantPriceRepository
						.findByEntityModelAndVendorVerificationModel(entityModel, vendorVerificationModel);
				int priorityCount = priorityList.size() + 1;

				for (RequestModel merch : merchantPriceAsList) {

					VendorModel vendor = vendorRepository.findByVendorId(merch.getVendorId());
					VendorPriceModel vendorPriceModel = vendorPriceRepository
							.findByVendorModelAndVendorVerificationModel(vendor, vendorVerificationModel);

					MerchantPriceModel merchantPatternCheck = merchantPriceRepository
							.findByEntityModelAndVendorModelAndVendorVerificationModel(entityModel, vendor,
									vendorVerificationModel);

					// System.err.println("Merch Id : "+merchantPatternCheck.getMerchantPriceId());

					MerchantPriceModel priorityCheck = merchantPriceRepository
							.findByEntityModelAndVendorVerificationModelAndPriority(entityModel,
									vendorVerificationModel, priorityCount);

					if (merchantPatternCheck == null && priorityCheck == null && prioritySize >= priorityCount) {

						MerchantPriceModel merchantPriceModel = new MerchantPriceModel();

						merchantPriceModel.setName(vendorPriceModel.getName());
						merchantPriceModel.setApiLink(merch.getApiLink());
						merchantPriceModel.setApplicationId(merch.getApplicationId());
						merchantPriceModel.setApiKey(merch.getApiKey());
						merchantPriceModel.setIdPrice(merch.getIdPrice());
						merchantPriceModel.setImagePrice(merch.getImagePrice());
						merchantPriceModel.setSignaturePrice(merch.getSignaturePrice());
						merchantPriceModel.setStatus(true);
						merchantPriceModel.setNoSourceCheck(model.isNoSourceCheck());
						merchantPriceModel.setCreatedBy(model.getCreatedBy());
						merchantPriceModel.setCreatedAt(LocalDate.now());
						merchantPriceModel.setAccepted(true);
						merchantPriceModel.setVendorModel(vendor);
						merchantPriceModel.setVendorVerificationModel(vendorVerificationModel);
						merchantPriceModel.setEntityModel(entityModel);
						merchantPriceModel.setPriority(priorityCount);

						merchantPriceRepository.save(merchantPriceModel);

						savedList.add(merchantPriceModel);

						savedCount++;
						priorityCount++;
					} else {
						neglectedCount++;

						if (merchantPatternCheck != null) {
							System.out.println("MerchantPatterncheckNull");
						} else if (priorityCheck != null) {
							System.out.println("priorityCheck");
						} else if (!(merch.getPriority() <= 3)) {
							System.out.println("merch.getPriority() <= 3");
						}
					}
				}

				int totalCount = savedCount + neglectedCount;

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(savedList);
				structure.setMessage(savedCount + " MerchantPrice details added and " + neglectedCount
						+ " MerchantPrice details neglected out of " + totalCount);

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);

				if (entityOptional.isEmpty()) {
					structure.setMessage("ENTITY NOT FOUND");
				} else if (vendorVerificationOptional.isEmpty()) {
					structure.setMessage("VENDOR VERIFICATION NOT FOUND");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewByMerchantPriceId(int vendorPriceId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<MerchantPriceModel> optional = merchantPriceRepository.findById(vendorPriceId);
			if (optional.isPresent()) {
				MerchantPriceModel MerchantPriceModel = optional.get();

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(MerchantPriceModel);
				structure.setMessage(AppConstants.SUCCESS);

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
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewAllMerchantPrice() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<MerchantPriceModel> list = merchantPriceRepository.findAll();

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
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
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

				List<MerchantPriceModel> priceList = merchantPriceRepository.findByVendorModel(vendorModel);

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
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure updateMerchantPriceOrUrl(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<MerchantPriceModel> optional = merchantPriceRepository.findById(model.getMerchantPriceId());
			if (optional.isPresent()) {

				MerchantPriceModel merchantPriceModel = optional.get();

				VendorPriceModel vendorPrice = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(
						merchantPriceModel.getVendorModel(), merchantPriceModel.getVendorVerificationModel());

				if (vendorPrice != null) {

					if (model.getIdPrice() >= vendorPrice.getIdPrice()
							&& model.getImagePrice() >= vendorPrice.getImagePrice()
							&& model.getSignaturePrice() >= vendorPrice.getSignaturePrice()) {

						double id = merchantPriceModel.getIdPrice();
						double image = merchantPriceModel.getImagePrice();
						double sign = merchantPriceModel.getSignaturePrice();

						merchantPriceModel.setApiLink(model.getApiLink());
						merchantPriceModel.setApplicationId(model.getApplicationId());
						merchantPriceModel.setApiKey(model.getApiKey());
						merchantPriceModel.setIdPrice(model.getIdPrice());
						merchantPriceModel.setImagePrice(model.getImagePrice());
						merchantPriceModel.setSignaturePrice(model.getSignaturePrice());
						merchantPriceModel.setNoSourceCheck(model.isNoSourceCheck());
						merchantPriceModel.setModifiedBy(model.getModifiedBy());
						merchantPriceModel.setModifiedAt(LocalDate.now());

						merchantPriceRepository.save(merchantPriceModel);

						if (model.getIdPrice() != id || model.getImagePrice() != image
								|| model.getSignaturePrice() != sign) {

							merchantPriceModel.setAccepted(false);
							merchantPriceRepository.save(merchantPriceModel);
							updateMerchantPriceTracker(merchantPriceModel);

						}
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setFlag(1);
						structure.setData(merchantPriceModel);
						structure.setMessage(AppConstants.SUCCESS);

					} else {
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setFlag(2);
						structure.setData(null);
						structure.setMessage(
								"MERCHANT VERIFICATION PRICE SHOULD BE EQUAL OR GREATER THAN VENDOR VERIFICATION PRICE");
					}
				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(3);
					structure.setData(null);
					structure.setMessage("VENDOR PRICE IS NOT FOUND FOR "
							+ merchantPriceModel.getVendorModel().getVendorName() + " WITH "
							+ merchantPriceModel.getVendorVerificationModel().getVerificationDocument() + ".");
				}

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(4);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	public void updateMerchantPriceTracker(MerchantPriceModel merchantPriceModel) throws Exception {

		VendorModel vendorModel = merchantPriceModel.getVendorModel();
		VendorVerificationModel vendorVerificationModel = merchantPriceModel.getVendorVerificationModel();
		EntityModel user = merchantPriceModel.getEntityModel();

		Optional<MerchantPriceTracker> recentIdentifierOptOne = merchantPriceTrackerRepository
				.findByVendorModelAndVendorVerificationModelAndEntityModelAndRecentIdentifier(vendorModel,
						vendorVerificationModel, user, 1);
		Optional<MerchantPriceTracker> recentIdentifierOptTwo = merchantPriceTrackerRepository
				.findByVendorModelAndVendorVerificationModelAndEntityModelAndRecentIdentifier(vendorModel,
						vendorVerificationModel, user, 2);

		if (recentIdentifierOptOne.isPresent()) {

			MerchantPriceTracker merchantPriceTrackerOne = recentIdentifierOptOne.get();

			merchantPriceTrackerOne.setCurrentlyInUse(false);
			merchantPriceTrackerOne.setRemark("Outdated");
			merchantPriceTrackerOne.setRecentIdentifier(2);
			merchantPriceTrackerOne.setEndDate(LocalDate.now().minusDays(1));

			merchantPriceTrackerRepository.save(merchantPriceTrackerOne);
		}

		if (recentIdentifierOptTwo.isPresent()) {

			MerchantPriceTracker merchantPriceTrackerTwo = recentIdentifierOptTwo.get();

			merchantPriceTrackerTwo.setRecentIdentifier(3);
			merchantPriceTrackerRepository.save(merchantPriceTrackerTwo);
		}

		MerchantPriceTracker priceTracker = new MerchantPriceTracker();

		priceTracker.setIdAmount(merchantPriceModel.getIdPrice());
		priceTracker.setOcrAmount(merchantPriceModel.getImagePrice());
		priceTracker.setSignatureAmount(merchantPriceModel.getSignaturePrice());
		priceTracker.setApplicableFromDate(LocalDate.now());
		priceTracker.setCurrentlyInUse(true);
		priceTracker.setRemark("Updated");
		priceTracker.setRecentIdentifier(1);
		priceTracker.setMailSend(false);
		priceTracker.setPriority(merchantPriceModel.getPriority());
		priceTracker.setVendorModel(merchantPriceModel.getVendorModel());
		priceTracker.setVendorVerificationModel(merchantPriceModel.getVendorVerificationModel());
		priceTracker.setEntityModel(merchantPriceModel.getEntityModel());

		merchantPriceTrackerRepository.save(priceTracker);
	}

	@Override
	public ResponseStructure updateMerchantPriceStatus(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<MerchantPriceModel> optional = merchantPriceRepository.findById(model.getMerchantPriceId());
			if (optional.isPresent()) {

				MerchantPriceModel merchantPriceModel = optional.get();

				merchantPriceModel.setStatus(model.isMerchantStatus());
				merchantPriceModel.setModifiedBy(model.getModifiedBy());
				merchantPriceModel.setModifiedAt(LocalDate.now());

				merchantPriceRepository.save(merchantPriceModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(merchantPriceModel);
				structure.setMessage(AppConstants.SUCCESS);
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

				MerchantPriceModel MerchantPriceModel = merchantPriceRepository
						.findByVendorModelAndVendorVerificationModel(vendorModel, vendorVerificationModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(MerchantPriceModel);
				structure.setMessage(AppConstants.SUCCESS);

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

				MerchantPriceModel MerchantPriceModel = merchantPriceRepository
						.findByVendorVerificationModel(vendorVerificationModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(MerchantPriceModel);
				structure.setMessage(AppConstants.SUCCESS);

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
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewByMerchantPriceStatus(boolean status) {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<MerchantPriceModel> list = merchantPriceRepository.findByStatus(status);

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
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;

	}

	@Override
	public ResponseStructure viewByUserId(int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> optional = userRepository.findById(userId);

			if (optional.isPresent()) {

				EntityModel userModel = optional.get();

				List<MerchantPriceModel> priceList = merchantPriceRepository.findByEntityModelAndStatus(userModel,
						true);

				if (!priceList.isEmpty()) {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setData(priceList);
					structure.setMessage(AppConstants.SUCCESS);

				} else {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(2);
					structure.setData(null);
					structure.setMessage("NO VERIFICATION ACCESS FOUND FOR THE GIVEN ENTITY");
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
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewByVendorIdAndVerificationIdAndUser(int vendorId, int verificationId, int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			VendorModel vendorModel = vendorRepository.findByVendorId(vendorId);
			VendorVerificationModel vendorVerificationModel = vendorVerificationRepository
					.findByVendorVerificationId(verificationId);
			EntityModel entityModel = userRepository.findByUserId(userId);

			if (vendorModel != null && vendorVerificationModel != null && entityModel != null) {

				MerchantPriceModel MerchantPriceModel = merchantPriceRepository
						.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, vendorVerificationModel,
								entityModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(MerchantPriceModel);
				structure.setMessage(AppConstants.SUCCESS);

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
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure updateMerchantPriceStatusByUserAndVerification(RequestModel model, int userId,
			int verificationId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			VendorVerificationModel vendorVerificationModel = vendorVerificationRepository
					.findByVendorVerificationId(verificationId);
			EntityModel entityModel = userRepository.findByUserId(userId);

			List<MerchantPriceModel> merchantList = merchantPriceRepository
					.findByEntityModelAndVendorVerificationModel(entityModel, vendorVerificationModel);

			List<MerchantPriceModel> transporterList = new LinkedList<>();
			int count = 0;

			if (!merchantList.isEmpty()) {

				for (MerchantPriceModel merchantPriceModel : merchantList) {

					merchantPriceModel.setStatus(model.isAccountStatus());
					merchantPriceRepository.save(merchantPriceModel);

					count++;
					transporterList.add(merchantPriceModel);
				}

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(transporterList);
				structure.setMessage("STATUS UPDATED SUCCESSFULLY FOR " + count + " MERCHANT PRICE MODEL");

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
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure copyMerchantPriceFromOneUserToAnother(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel alreadyExistingUser = userRepository.findByUserId(model.getAlreadyExistingUserId());
			EntityModel newComerUser = userRepository.findByUserId(model.getNewComerUserId());

			List<MerchantPriceModel> existingUserMerchantList = merchantPriceRepository
					.findByEntityModel(alreadyExistingUser);

			int count = 0;

			List<MerchantPriceModel> newList = new ArrayList<>();

			if (alreadyExistingUser != null && newComerUser != null && !(existingUserMerchantList.isEmpty())) {

				for (MerchantPriceModel merchantPrice : existingUserMerchantList) {

					MerchantPriceModel merchantPriceModel = new MerchantPriceModel();

					merchantPriceModel.setName(merchantPrice.getName());
					merchantPriceModel.setApiLink(merchantPrice.getApiLink());
					merchantPriceModel.setApplicationId(merchantPrice.getApplicationId());
					merchantPriceModel.setApiKey(merchantPrice.getApiKey());
					merchantPriceModel.setIdPrice(merchantPrice.getIdPrice());
					merchantPriceModel.setImagePrice(merchantPrice.getImagePrice());
					merchantPriceModel.setSignaturePrice(merchantPrice.getSignaturePrice());
					merchantPriceModel.setStatus(true);
					merchantPriceModel.setNoSourceCheck(model.isNoSourceCheck());
					merchantPriceModel.setCreatedBy(model.getCreatedBy());
					merchantPriceModel.setCreatedAt(LocalDate.now());
					merchantPriceModel.setVendorModel(merchantPrice.getVendorModel());
					merchantPriceModel.setVendorVerificationModel(merchantPrice.getVendorVerificationModel());
					merchantPriceModel.setEntityModel(newComerUser);
					merchantPriceModel.setPriority(merchantPrice.getPriority());

					merchantPriceRepository.save(merchantPriceModel);

					count++;

					newList.add(merchantPriceModel);
				}

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setCount(count);
				structure.setData(newList);
				structure.setMessage(count + " Merchant price model added for User ID " + newComerUser.getUserId());

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);

				if (alreadyExistingUser == null) {
					structure.setMessage("Existing User not found");
				} else if (newComerUser == null) {
					structure.setMessage("New User not found");
				} else if (existingUserMerchantList.isEmpty()) {
					structure.setMessage("No Merchant price list found for the given user");
				}
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure updateMerchantSourceCheck(int userId, int vendorVerificationId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			VendorVerificationModel vendorVerificationModel = vendorVerificationRepository
					.findByVendorVerificationId(vendorVerificationId);
			EntityModel entityModel = userRepository.findByUserId(userId);

			if (vendorVerificationModel != null && entityModel != null) {

				List<MerchantPriceModel> merchantPriceModel = merchantPriceRepository
						.findByEntityModelAndVendorVerificationModel(entityModel, vendorVerificationModel);

				for (MerchantPriceModel merchantPriceModelSourceUpdate : merchantPriceModel) {

					merchantPriceModelSourceUpdate.setNoSourceCheck(model.isNoSourceCheck());
					merchantPriceModelSourceUpdate.setModifiedBy(model.getModifiedBy());
					merchantPriceModelSourceUpdate.setModifiedAt(LocalDate.now());

					merchantPriceRepository.save(merchantPriceModelSourceUpdate);
				}

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(merchantPriceModel);
				structure.setCount(merchantPriceModel.size());
				structure.setMessage(AppConstants.SUCCESS);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);

				if (entityModel == null) {
					structure.setMessage("ENTITY NOT PRESENT");
				} else if (vendorVerificationModel == null) {
					structure.setMessage("VERIFICATION MODEL NOT PRESENT");
				}
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewByUserAndVerification(int userId, int vendorVerificationId) {

		ResponseStructure structure = new ResponseStructure();
		try {

			VendorVerificationModel vendorVerificationModel = vendorVerificationRepository
					.findByVendorVerificationId(vendorVerificationId);
			EntityModel entityModel = userRepository.findByUserId(userId);

			if (vendorVerificationModel != null && entityModel != null) {

				List<MerchantPriceModel> merchantPriceModel = merchantPriceRepository
						.findByEntityModelAndVendorVerificationModel(entityModel, vendorVerificationModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(merchantPriceModel);
				structure.setCount(merchantPriceModel.size());
				structure.setMessage(AppConstants.SUCCESS);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);

				if (entityModel == null) {
					structure.setMessage("ENTITY NOT PRESENT");
				} else if (vendorVerificationModel == null) {
					structure.setMessage("VERIFICATION MODEL NOT PRESENT");
				}
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewAllForUser(int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> optional = userRepository.findById(userId);

			if (optional.isPresent()) {

				EntityModel userModel = optional.get();

				List<MerchantPriceModel> priceList = merchantPriceRepository.findByEntityModel(userModel);

				if (!priceList.isEmpty()) {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setData(priceList);
					structure.setMessage(AppConstants.SUCCESS);

				} else {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(2);
					structure.setData(null);
					structure.setMessage("NO VERIFICATION ACCESS FOUND FOR THE GIVEN ENTITY");
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
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure addMerchantPriceForAadhaarBasedSigning(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> entityOptional = userRepository.findById(model.getUserId());

			VendorModel vendor = vendorRepository.findByVendorName(AppConstants.SUREPASS_VENDOR);

			VendorVerificationModel aadhaarXml = vendorVerificationRepository
					.findByVerificationDocument(AppConstants.AADHAR_XML_VERIFY);

			VendorVerificationModel aadhaarOtp = vendorVerificationRepository
					.findByVerificationDocument(AppConstants.AADHAR_OTP_VERIFY);

			model.setPriority(1);

			if (vendor != null && aadhaarXml != null && aadhaarOtp != null && entityOptional.isPresent()) {

				EntityModel user = entityOptional.get();

				ResponseStructure xml = aadhaarSigningCreate(user, vendor, aadhaarXml, model);
				ResponseStructure otp = aadhaarSigningCreate(user, vendor, aadhaarOtp, model);

				if (xml.getFlag() == 1 && otp.getFlag() == 1) {

					List<MerchantPriceModel> merchPriceList = new ArrayList<>();

					RequestModel xmlMerch = xml.getModelData();
					RequestModel otpMerch = otp.getModelData();

					merchPriceList.add(xmlMerch.getMerchantPriceModel());
					merchPriceList.add(otpMerch.getMerchantPriceModel());

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setData(merchPriceList);

				} else if (xml.getFlag() != 1) {
					return xml;
				} else {
					return otp;
				}

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);

				if (vendor == null) {
					structure.setMessage("VENDOR NOT FOUND");
				} else if (entityOptional.isEmpty()) {
					structure.setMessage("ENTITY NOT FOUND");
				} else if (aadhaarXml == null) {
					structure.setMessage("AADHAAR XML VERIFICATION NOT FOUND");
				} else {
					structure.setMessage("AADHAAR OTP VERIFICATION NOT FOUND");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	public ResponseStructure aadhaarSigningCreate(EntityModel entityModel, VendorModel vendorModel,
			VendorVerificationModel vendorVerificationModel, RequestModel model) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		int prioritySize = vendorRepository.findAll().size();

		VendorPriceModel vendorPriceModel = vendorPriceRepository
				.findByVendorModelAndVendorVerificationModel(vendorModel, vendorVerificationModel);

		MerchantPriceModel merchantPatternCheck = merchantPriceRepository
				.findByEntityModelAndVendorModelAndVendorVerificationModel(entityModel, vendorModel,
						vendorVerificationModel);
		MerchantPriceModel priorityCheck = merchantPriceRepository
				.findByEntityModelAndVendorVerificationModelAndPriority(entityModel, vendorVerificationModel,
						model.getPriority());

		if (vendorPriceModel != null && merchantPatternCheck == null && priorityCheck == null
				&& model.getPriority() <= prioritySize && model.getPriority() != 0
				&& model.getIdPrice() >= vendorPriceModel.getIdPrice()
				&& model.getImagePrice() >= vendorPriceModel.getImagePrice()
				&& model.getSignaturePrice() >= vendorPriceModel.getSignaturePrice()) {

			MerchantPriceModel merchantPriceModel = new MerchantPriceModel();

			merchantPriceModel.setName(vendorPriceModel.getName());
			merchantPriceModel.setApiLink("");
			merchantPriceModel.setApplicationId("");
			merchantPriceModel.setApiKey("");
			merchantPriceModel.setIdPrice(model.getIdPrice());
			merchantPriceModel.setImagePrice(0);
			merchantPriceModel.setSignaturePrice(0);
			merchantPriceModel.setStatus(true);
			merchantPriceModel.setNoSourceCheck(true);
			merchantPriceModel.setCreatedBy(model.getCreatedBy());
			merchantPriceModel.setCreatedAt(LocalDate.now());
			merchantPriceModel.setVendorModel(vendorModel);
			merchantPriceModel.setVendorVerificationModel(vendorVerificationModel);
			merchantPriceModel.setEntityModel(entityModel);
			merchantPriceModel.setPriority(model.getPriority());

			merchantPriceRepository.save(merchantPriceModel);

			addMerchantPriceTracker(merchantPriceModel);

			model.setMerchantPriceModel(merchantPriceModel);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(merchantPriceModel);
			structure.setModelData(model);
			structure.setMessage(AppConstants.SUCCESS);

		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(3);
			structure.setData(null);

			if (vendorPriceModel == null) {
				structure.setMessage(
						"VENDOR PRICE MODEL IS NOT FOUND FOR THE GIVEN VENDOR WITH THE SPECIFIED VERIFICATION");

			} else if (merchantPatternCheck != null) {
				structure.setMessage(
						"THIS MERCHANT HAS ALREADY BEEN GRANTED ACCESS TO THE PROVIDED VERIFICATION BY THE SPECIFIED VENDOR");
			} else if (priorityCheck != null) {
				structure.setMessage("THIS PRIORITY HAS BEEN ALREADY ASSIGNED FOR THE SPECIFIED VERIFICATION TYPE");
			} else if (model.getPriority() > prioritySize) {

				int extendedValue = prioritySize + 1;
				structure.setMessage("PRIORITY SHOULD BE LESS THAN " + extendedValue);

			} else if (model.getPriority() == 0) {
				structure.setMessage("PRIORITY SHOULDN'T BE 0");
			} else if (model.getIdPrice() < vendorPriceModel.getIdPrice()
					|| model.getSignaturePrice() < vendorPriceModel.getSignaturePrice()
					|| model.getImagePrice() < vendorPriceModel.getImagePrice()) {
				structure.setMessage(
						"MERCHANT VERIFICATION PRICE SHOULD BE EQUAL OR GREATER THAN VENDOR VERIFICATION PRICE");
			}
		}

		return structure;

	}

	@Override
	public ResponseStructure acceptNewPrice(int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> optional = userRepository.findById(userId);

			if (optional.isPresent()) {

				EntityModel userModel = optional.get();

				List<MerchantPriceModel> priceList = merchantPriceRepository.findByEntityModelAndAccepted(userModel,
						false);

				if (!priceList.isEmpty()) {

					for (MerchantPriceModel merchantPriceModel : priceList) {

						merchantPriceModel.setAccepted(true);
						merchantPriceRepository.save(merchantPriceModel);
					}

					AdminDto adminDto = adminRepository.findByAdminId(userModel.getAdminId());
					
					if(adminDto!=null) {
						
						String adminMail = adminDto.getEmail();
						emailService.priceAcceptedMailToAdmin(adminMail,userModel.getName());
					}
					
					emailService.priceAcceptedMailToEntity(userModel.getEmail(),userModel.getName());
					
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setData(priceList);
					structure.setMessage(AppConstants.SUCCESS);

				} else {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(2);
					structure.setData(null);
					structure.setMessage("NO PRICE UPDATION NEEDS TO BE ACCEPTED");
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
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure updatePriority(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<MerchantPriceModel> optional = merchantPriceRepository.findById(model.getMerchantPriceId());
			if (optional.isPresent()) {

				MerchantPriceModel merchantPriceModel = optional.get();

				VendorPriceModel vendorPrice = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(
						merchantPriceModel.getVendorModel(), merchantPriceModel.getVendorVerificationModel());

				if (vendorPrice != null) {

					MerchantPriceModel byPriority = merchantPriceRepository
							.findByEntityModelAndVendorVerificationModelAndPriority(merchantPriceModel.getEntityModel(),
									merchantPriceModel.getVendorVerificationModel(), model.getPriority());

					if (byPriority != null) {

						byPriority.setPriority(merchantPriceModel.getPriority());
						merchantPriceModel.setPriority(model.getPriority());

						merchantPriceRepository.save(merchantPriceModel);
						merchantPriceRepository.save(byPriority);
						
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setFlag(1);
						structure.setData(merchantPriceModel);
						structure.setMessage(AppConstants.SUCCESS);

					}else {
						
						merchantPriceModel.setPriority(model.getPriority());
						merchantPriceRepository.save(merchantPriceModel);
						
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setFlag(1);
						structure.setData(merchantPriceModel);
						structure.setMessage("Priority updated Successfully");
					}

				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(3);
					structure.setData(null);
					structure.setMessage("VENDOR PRICE IS NOT FOUND FOR "
							+ merchantPriceModel.getVendorModel().getVendorName() + " WITH "
							+ merchantPriceModel.getVendorVerificationModel().getVerificationDocument() + ".");
				}

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(4);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	
	
	
	
	@Override
	public ResponseStructure entityVerificationDropDown(int userId) {
		
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> optional = userRepository.findById(userId);

			if (optional.isPresent()) {

				EntityModel userModel = optional.get();

				List<MerchantPriceModel> priceList = merchantPriceRepository.findByEntityModel(userModel);

				if (!priceList.isEmpty()) {

					
					Set<VendorVerificationModel> set = new LinkedHashSet<>();
					
					for (MerchantPriceModel mpm : priceList) {

                            if(!set.contains(mpm.getVendorVerificationModel())) {
                            	set.add(mpm.getVendorVerificationModel());
                            }
					}
					
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setData(set);
					structure.setMessage(AppConstants.SUCCESS);

				} else {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(2);
					structure.setData(null);
					structure.setMessage("This entity has no access for any verification");
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
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

}
