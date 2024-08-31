package com.bp.middleware.signerpositiontracker;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.signers.SignerModel;
import com.bp.middleware.signers.SignerRepository;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@Service
public class SignerPositionTrackerService {

	@Autowired
	private SignerPositionTrackerRepository signerPositionTrackerRepository;
	@Autowired
	private SignerRepository signerRepository;

	public ResponseStructure viewAll() {

		ResponseStructure structure = new ResponseStructure();
		try {
			List<SignerPositionTracker> list = signerPositionTrackerRepository.findAll();

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
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;

	}

	public ResponseStructure viewById(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<SignerPositionTracker> opt = signerPositionTrackerRepository.findById(model.getPositionId());

			if (opt.isPresent()) {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(opt.get());
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;

	}

	public ResponseStructure viewBySigner(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {

			SignerModel signer = signerRepository.findBySignerId(model.getSignerId());

			if (signer != null) {

				List<SignerPositionTracker> list = signerPositionTrackerRepository.findBySigner(signer);

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
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}

		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;

	}

}
