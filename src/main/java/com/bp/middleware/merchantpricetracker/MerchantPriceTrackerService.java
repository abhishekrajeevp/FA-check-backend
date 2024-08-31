package com.bp.middleware.merchantpricetracker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.vendorpricetracker.VendorPriceTracker;

@Service
public class MerchantPriceTrackerService {

	@Autowired
	private MerchantPriceRepository merchantPriceRepository;
	@Autowired
	private MerchantPriceTrackerRepository merchantPriceTrackerRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	EmailService emailService;

	public ResponseStructure viewAll() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<MerchantPriceTracker> tracker = merchantPriceTrackerRepository.findAll();

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
			Optional<MerchantPriceTracker> tracker = merchantPriceTrackerRepository.findById(id);

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

	public ResponseStructure viewByCurrentlyActive() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<MerchantPriceTracker> tracker = merchantPriceTrackerRepository.findByCurrentlyInUse(true);

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
			List<MerchantPriceTracker> tracker = merchantPriceTrackerRepository.findByRecentIdentifier(2);

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

	public ResponseStructure viewByActivatedToday() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<MerchantPriceTracker> tracker = merchantPriceTrackerRepository
					.findByApplicableFromDateAndCurrentlyInUse(LocalDate.now(), true);

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

	public ResponseStructure viewByCurrentlyActiveForUser(int userId) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> opt = userRepository.findById(userId);

			if (opt.isPresent()) {

				List<MerchantPriceTracker> tracker = merchantPriceTrackerRepository
						.findByCurrentlyInUseAndEntityModel(true, opt.get());

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
			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	public ResponseStructure viewByLastlyUsedForUser(int userId) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> opt = userRepository.findById(userId);
			if (opt.isPresent()) {

				List<MerchantPriceTracker> tracker = merchantPriceTrackerRepository
						.findByRecentIdentifierAndEntityModel(2, opt.get());

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
			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	public ResponseStructure viewByActivatedTodayForUser(int userId) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> opt = userRepository.findById(userId);

			if (opt.isPresent()) {

				List<MerchantPriceTracker> tracker = merchantPriceTrackerRepository
						.findByApplicableFromDateAndCurrentlyInUseAndEntityModel(LocalDate.now(), true, opt.get());

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
			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	public ResponseStructure mailTriggerAllTogether() {
		ResponseStructure structure = new ResponseStructure();

		try {

			List<MerchantPriceTracker> list = merchantPriceTrackerRepository.findByCurrentlyInUseAndMailSend(true,
					false);

			if (!list.isEmpty()) {

				List<EntityModel> mailSent = new ArrayList<>();
				List<EntityModel> mailNotSent = new ArrayList<>();

				System.err.println("CURRENTLY USE : " + list.size());

				Set<EntityModel> set = new HashSet<>();

				for (MerchantPriceTracker merchantPriceTracker : list) {

					if (!set.contains(merchantPriceTracker.getEntityModel())) {

						set.add(merchantPriceTracker.getEntityModel());
						boolean sent = emailService
								.sendMerchantPriceUpdationMail(merchantPriceTracker.getEntityModel());

						if (sent) {

							mailSent.add(merchantPriceTracker.getEntityModel());
						} else {

							mailNotSent.add(merchantPriceTracker.getEntityModel());
						}

						merchantPriceTracker.setMailSend(sent);
						merchantPriceTrackerRepository.save(merchantPriceTracker);
					}
				}

				Map<String, Object> map = new LinkedHashMap<>();
				map.put("mailSent", mailSent);
				map.put("mailNotSent", mailNotSent);

				structure.setMessage("Mail sent to " + mailSent.size() + " Entities and failed to sent to "
						+ mailNotSent.size() + " Entities");
				structure.setData(map);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {

				structure.setMessage("No price updation found");
				structure.setData(null);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure findByRecentIdentifierAndEntity(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<MerchantPriceTracker> tracker = merchantPriceTrackerRepository
					.getByRecentIdentifierAndEntity(model.getRecentIdentifier(), model.getUserId());
//			

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

	public ResponseStructure findByRecentIdentifierAndEntityAndPriority(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {
			List<MerchantPriceTracker> tracker = merchantPriceTrackerRepository
					.getByRecentIdentifierAndEntityAndPriority(model.getRecentIdentifier(), model.getUserId(), 1);

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

	public ResponseStructure mailTriggerIndividually(int userId) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> opt = userRepository.findById(userId);

			if (opt.isPresent()) {

				EntityModel entityModel = opt.get();

				List<MerchantPriceModel> priceModel = merchantPriceRepository.findByEntityModelAndAccepted(entityModel,
						false);

				if (priceModel.isEmpty()) {

					structure.setMessage("This Entity Already Accepted all the updated price");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				} else {

					boolean sent = emailService.sendMerchantPriceUpdationMail(entityModel);

					if (sent) {
						structure.setMessage("Mail sent successfully");
					} else {
						structure.setMessage("Mail not sent");
					}

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(sent);
					structure.setFlag(1);
				}
			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	
	
	public ResponseStructure mailTriggerByMultiSelect(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Set<Integer> multipleUserId = model.getMultipleUserId();
			

			if (!multipleUserId.isEmpty()) {

				List<EntityModel> mailSentEntity = new ArrayList<>();
				List<EntityModel> mailNotSentEntity = new ArrayList<>();
				
				for (Integer userId : multipleUserId) {
					
					EntityModel entity = userRepository.findByUserId(userId);
					
					if(entity!=null) {
						
						List<MerchantPriceModel> priceModel = merchantPriceRepository.findByEntityModelAndAccepted(entity,
								false);
						
						if (!priceModel.isEmpty()) {

							boolean sent = emailService.sendMerchantPriceUpdationMail(entity);

							if (sent) {
								mailSentEntity.add(entity);
							} else {
								mailNotSentEntity.add(entity);
							}
						} 
					}
				}
				
				Map<String, List<EntityModel>> map = new LinkedHashMap<>();
				map.put("mailSentEntity", mailSentEntity);
				map.put("mailNotSentEntity", mailNotSentEntity);
				
				structure.setMessage("Mail successfully send to "+mailSentEntity.size()+" Entity and failed to send for "+mailNotSentEntity.size()+" entity");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(map);
				structure.setFlag(1);
				
			} else {

				structure.setMessage("Should select atleast one Entity");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

}
