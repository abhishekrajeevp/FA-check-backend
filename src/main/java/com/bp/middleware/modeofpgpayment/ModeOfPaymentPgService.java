package com.bp.middleware.modeofpgpayment;

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
public class ModeOfPaymentPgService {

	@Autowired
	private ModeOfPaymentPgRepository modeOfPaymentPgRepository;

	public ResponseStructure addModeOfPayment(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<ModeOfPaymentPg> opt = modeOfPaymentPgRepository.findByModeOfPayment(model.getPaymentMode());
			Optional<ModeOfPaymentPg> opt2 = modeOfPaymentPgRepository.findByPaymentCode(model.getPaymentCode());

			if (opt.isEmpty() && opt2.isEmpty()) {

				ModeOfPaymentPg mode = new ModeOfPaymentPg();

				mode.setModeOfPayment(model.getModeOfPayment());
				mode.setPaymentCode(model.getPaymentCode());
				mode.setStatus(true);
				mode.setCreatedBy(model.getCreatedBy());
				mode.setConvenienceFetch(model.isConvenienceFetch());
				mode.setCreatedDate(new Date());

				modeOfPaymentPgRepository.save(mode);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(mode);
				structure.setFlag(1);

			} else {

				if (opt.isPresent()) {
					structure.setMessage("GIVEN PAYMENT MODE ALREADY PRESENT");
				} else {
					structure.setMessage("GIVEN PAYMENT CODE ALREADY PRESENT");
				}
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

	public ResponseStructure viewAll() {

		ResponseStructure structure = new ResponseStructure();

		try {

			List<ModeOfPaymentPg> list = modeOfPaymentPgRepository.findAll();

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

	public ResponseStructure viewById(int modeId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<ModeOfPaymentPg> opt = modeOfPaymentPgRepository.findById(modeId);

			if (opt.isPresent()) {

				ModeOfPaymentPg modeOfPaymentPg = opt.get();

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(modeOfPaymentPg);
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

	public ResponseStructure update(int modeId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<ModeOfPaymentPg> opt = modeOfPaymentPgRepository.findById(modeId);

			if (opt.isPresent()) {

				ModeOfPaymentPg mode = opt.get();

				mode.setModeOfPayment(model.getModeOfPayment());
				mode.setPaymentCode(model.getPaymentCode());
				mode.setConvenienceFetch(model.isConvenienceFetch());
				mode.setModifiedBy(model.getModifiedBy());
				mode.setModifiedDate(new Date());

				modeOfPaymentPgRepository.save(mode);
				
				structure.setMessage("UPDATED SUCCESSFULLY");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(mode);
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

	public ResponseStructure updateStatus(int modeId, RequestModel model) {
		
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<ModeOfPaymentPg> opt = modeOfPaymentPgRepository.findById(modeId);

			if (opt.isPresent()) {

				ModeOfPaymentPg mode = opt.get();

				mode.setStatus(model.isStatusFlag());
				mode.setModifiedBy(model.getModifiedBy());
				mode.setModifiedDate(new Date());

				modeOfPaymentPgRepository.save(mode);
				
				if(model.isStatusFlag()) {
					structure.setMessage(mode.getModeOfPayment()+" PAYMENT IS ACTIVE NOW");
				}else {
					structure.setMessage(mode.getModeOfPayment()+" PAYMENT IS INACTIVE NOW");
				}
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(mode);
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

	public ResponseStructure viewByStatus(boolean status) {
		ResponseStructure structure = new ResponseStructure();

		try {

			List<ModeOfPaymentPg> list = modeOfPaymentPgRepository.findByStatus(status);

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

}
