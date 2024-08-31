package com.bp.middleware.requesthitlogs;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.payment.PaymentRepository;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.RequestResponseReplicaRepository;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.smartrouteverification.CommonResponseStructure;
import com.bp.middleware.smartrouteverification.SmartRouteUtils;
import com.bp.middleware.surepass.SurepassCommons;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

@Service
public class HitLogsService {

	@Autowired
	private ResponseRepository respRepository;
	@Autowired
	private RequestRepository reqRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MerchantPriceRepository merchantPriceRepository;
	@Autowired
	private VendorRepository vendorRepository;
	@Autowired
	private VendorVerificationRepository verificationRepository;
	@Autowired
	private VendorPriceRepository vendorPriceRepository;
	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private PostpaidRepository postpaidRepository;
	@Autowired
	private SurepassCommons surepassCommons;
	@Autowired
	private SmartRouteUtils smartRouteUtils;
	@Autowired
	private CommonResponseStructure CommonResponseStructure;
	@Autowired
	private RequestResponseReplicaRepository replicaRepository;
	@Autowired
	FileUtils fu;

	public ResponseStructure allFreeHits() {
		ResponseStructure structure = new ResponseStructure();

		try {

			List<Request> freeRequest = reqRepository.findByFreeHit(true);

			if (!freeRequest.isEmpty()) {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(freeRequest);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}
		return structure;
	}

	public ResponseStructure allFreeHitsByEntity(int userId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<EntityModel> opt = userRepository.findById(userId);

			if (opt.isPresent()) {
				
				List<Request> freeRequest = reqRepository.findByUserAndFreeHit(opt.get(),true);

				if (!freeRequest.isEmpty()) {

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(freeRequest);
					structure.setFlag(1);

				} else {

					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(3);
				}

			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}
		return structure;
	}

	
	public ResponseStructure allFreeHitsByEntityAndVerification(int userId, int verificationId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<EntityModel> opt = userRepository.findById(userId);
			Optional<VendorVerificationModel> opt2 = verificationRepository.findById(verificationId);

			if (opt.isPresent() && opt2.isPresent()) {
				
				List<Request> freeRequest = reqRepository.findByUserAndVerificationModelAndFreeHit(opt.get(),opt2.get(),true);

				if (!freeRequest.isEmpty()) {

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(freeRequest);
					structure.setFlag(1);

				} else {

					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(3);
				}

			} else {
				
				if(opt.isEmpty()) {
					structure.setMessage("ENTITY NOT FOUND");
				}else {
					structure.setMessage("VERIFICATION NOT FOUND");
				}
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}
		return structure;
	}

}
