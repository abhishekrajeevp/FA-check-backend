package com.bp.middleware.conveniencefee;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.modeofpgpayment.ModeOfPaymentPg;
import com.bp.middleware.modeofpgpayment.ModeOfPaymentPgRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@Service
public class ConvenienceService {

	@Autowired
	private ConveniencePercentageRepository conveniencePercentageRepository;
	@Autowired
	private ModeOfPaymentPgRepository modeOfPaymentPgRepository;

	public ResponseStructure addConveniencePercentage(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<ModeOfPaymentPg> opt = modeOfPaymentPgRepository.findById(model.getModeId());

			if (opt.isPresent()) {

				ModeOfPaymentPg modeOfPaymentPg = opt.get();

				Optional<ConveniencePercentageEntity> optional = conveniencePercentageRepository
						.findByThresholdAmountAndModeOfPaymentPg(model.getThresholdAmount(), modeOfPaymentPg);

				if (optional.isPresent()) {

					structure.setMessage("Given Threshold amount already exist for this Mode of Payment");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(3);

				} else {

					return addConvenienceFeeForModeOfPayment(modeOfPaymentPg, model);
				}

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(4);
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(5);
		}
		return structure;
	}

	private ResponseStructure addConvenienceFeeForModeOfPayment(ModeOfPaymentPg modeOfPaymentPg, RequestModel model)
			throws Exception {

		ResponseStructure structure = new ResponseStructure();

		if (modeOfPaymentPg.isConvenienceFetch()) {

			ConveniencePercentageEntity fee = new ConveniencePercentageEntity();

			fee.setThresholdAmount(model.getThresholdAmount());
			fee.setActiveStatus(1);
			fee.setConveniencePercentage(model.getConveniencePercentage());
			fee.setFixedAmount(model.getFixedAmount());
			fee.setModeOfPaymentPg(modeOfPaymentPg);

			SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.RAILWAY_DATE_FORMAT);
			fee.setCreatedDateTime(sdf.format(new Date()));
			fee.setCreatedBy(model.getCreatedBy());

			conveniencePercentageRepository.save(fee);

			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(fee);
			structure.setFlag(1);

		} else {

			structure.setMessage("Convenience Fetch is not enabled for "+modeOfPaymentPg.getModeOfPayment());
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

	public ResponseStructure viewAll() {
		ResponseStructure structure = new ResponseStructure();

		try {

			List<ConveniencePercentageEntity> list = conveniencePercentageRepository.findAll();

			if (!list.isEmpty()) {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(list);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure viewById(int convenienceId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<ConveniencePercentageEntity> opt = conveniencePercentageRepository.findById(convenienceId);

			if (opt.isPresent()) {

				ConveniencePercentageEntity conv = opt.get();

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(conv);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure viewByModeOfPay(int modeId) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<ModeOfPaymentPg> opt = modeOfPaymentPgRepository.findById(modeId);

			if (opt.isPresent()) {

				ModeOfPaymentPg conv = opt.get();

				List<ConveniencePercentageEntity> listWithMode = conveniencePercentageRepository
						.findByModeOfPaymentPg(conv);

				if (!listWithMode.isEmpty()) {

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(listWithMode);
					structure.setFlag(1);

				} else {

					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}
			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure update(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<ConveniencePercentageEntity> opt = conveniencePercentageRepository
					.findById(model.getConvenienceId());

			if (opt.isPresent()) {

				ConveniencePercentageEntity conv = opt.get();

				Optional<ConveniencePercentageEntity> optional = conveniencePercentageRepository
						.findByThresholdAmountAndModeOfPaymentPg(model.getThresholdAmount(), conv.getModeOfPaymentPg());

				if (optional.isEmpty() || conv.getConvenienceId() == optional.get().getConvenienceId()) {

					conv.setThresholdAmount(model.getThresholdAmount());
					conv.setConveniencePercentage(model.getConveniencePercentage());
					conv.setFixedAmount(model.getFixedAmount());

					SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.RAILWAY_DATE_FORMAT);
					conv.setModifeidDateTime(sdf.format(new Date()));
					conv.setModifiedBy(model.getModifiedBy());

					conveniencePercentageRepository.save(conv);

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(conv);
					structure.setFlag(1);

				} else {

					structure.setMessage("Given Threshold amount is overlapping with another.");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}
		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure updateStatus(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<ConveniencePercentageEntity> opt = conveniencePercentageRepository
					.findById(model.getConvenienceId());

			if (opt.isPresent()) {

				ConveniencePercentageEntity conv = opt.get();

				conv.setActiveStatus(model.getActiveStatus());

				SimpleDateFormat sdf = new SimpleDateFormat(AppConstants.RAILWAY_DATE_FORMAT);
				conv.setModifeidDateTime(sdf.format(new Date()));
				conv.setModifiedBy(model.getModifiedBy());

				conveniencePercentageRepository.save(conv);

				structure.setMessage("Status updated successfully");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(conv);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}
		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure viewByStatus(int status) {
		ResponseStructure structure = new ResponseStructure();

		try {

			List<ConveniencePercentageEntity> list = conveniencePercentageRepository.findByActiveStatus(status);

			if (!list.isEmpty()) {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(list);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure viewByModeOfPayAndAmount(int modeId, int amount) {
		ResponseStructure structure = new ResponseStructure();

		try {

			List<ConveniencePercentageEntity> convFee = conveniencePercentageRepository
					.getAllActiveLessThresholdAmountIDesc(amount, modeId);

			System.err.println(convFee == null);

			if (!convFee.isEmpty()) {

				ConveniencePercentageEntity thresholdMatch = convFee.get(0);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(thresholdMatch);
				structure.setFlag(1);

			} else {

//				ConveniencePercentageEntity  lowestThreshold = conveniencePercentageRepository.getLowestActiveThresholdByMode(modeId);
//				
//				structure.setMessage(AppConstants.SUCCESS);
//				structure.setStatusCode(HttpStatus.OK.value());
//				structure.setData(lowestThreshold);
//				structure.setFlag(1);
//				
//				if(lowestThreshold == null) {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
//				}
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure viewByModeOfPayAndCurrentlyActive(int modeId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			if (modeOfPaymentPgRepository.findById(modeId).isPresent()) {

				List<ConveniencePercentageEntity> listWithMode = conveniencePercentageRepository
						.getByModeIdAndActive(modeId);

				if (!listWithMode.isEmpty()) {

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(listWithMode);
					structure.setFlag(1);

				} else {

					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}
			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

}
