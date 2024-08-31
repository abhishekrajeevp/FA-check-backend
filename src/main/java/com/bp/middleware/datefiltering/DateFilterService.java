package com.bp.middleware.datefiltering;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.asn1.ocsp.ResponderID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.manualpayment.ManualPayment;
import com.bp.middleware.manualpayment.ManualPaymentRepository;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.RequestResponseReplica;
import com.bp.middleware.requestandresponse.RequestResponseReplicaRepository;
import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.transaction.TransactionRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;

@Service
public class DateFilterService {

	private static final Logger LOGGER=LoggerFactory.getLogger(DateFilterService.class);
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private PrepaidRepository prepaidRepository;
	@Autowired
	private PostpaidRepository postpaidRepository;
	@Autowired
	private ManualPaymentRepository manualPaymentRepository;
	@Autowired
	private ResponseRepository responseRepository;
	@Autowired
	private RequestRepository requestRepository;
	@Autowired
	private RequestResponseReplicaRepository replicaRepository;
	
	
	public ResponseStructure transactionDateFiltering(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate = model.getStartDate()+" 00:00:00.0000000";
			String endDate = model.getEndDate()+" 23:59:59.9999999";
			
			List<TransactionDto> transBetweenDates= transactionRepository.getByCreatedDateTime(startDate,endDate);
			
			if(!transBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Transaction details Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(transBetweenDates);
				structure.setFlag(1);
				structure.setCount(transBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Transaction done Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("TRANSACTION DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}
	
	

	public ResponseStructure transactionDateFilteringForEntity(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {
			
			String startDate = model.getStartDate()+" 00:00:00.0000000";
			String endDate = model.getEndDate()+" 23:59:59.9999999";
			
			List<TransactionDto> transBetweenDates= transactionRepository.getByCreatedDateTimeAndEntity(startDate,endDate,model.getUserId());
			
			if(!transBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Transaction details Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(transBetweenDates);
				structure.setFlag(1);
				structure.setCount(transBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Transaction done Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("TRANSACTION DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}



	public ResponseStructure prepaidDateFiltering(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {
			
			String startDate = model.getStartDate().toString();
			String endDate = model.getEndDate().toString();
			
			List<PrepaidPayment> prepaidBetweenDates= prepaidRepository.getByPaidDate(startDate,endDate);
			
			if(!prepaidBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Prepaid Transaction details Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(prepaidBetweenDates);
				structure.setFlag(1);
				structure.setCount(prepaidBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Prepaid Transaction done Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("PREPAID TRANSACTION DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}



	public ResponseStructure prepaidDateFilteringForEntity(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {
			
			String startDate = model.getStartDate().toString();
			String endDate = model.getEndDate().toString();
			
			List<PrepaidPayment> prepaidBetweenDates= prepaidRepository.getByPaidDateAndEntityModel(startDate,endDate,model.getUserId());
			
			if(!prepaidBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Prepaid Transaction details Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(prepaidBetweenDates);
				structure.setFlag(1);
				structure.setCount(prepaidBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Prepaid Transaction done Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("PREPAID TRANSACTION DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}



	public ResponseStructure postaidDateFiltering(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {
			
			String startDate = model.getStartDate().toString();
			String endDate = model.getEndDate().toString();
			
			List<PostpaidPayment> prepaidBetweenDates= postpaidRepository.getByStartDate(startDate,endDate);
			
			if(!prepaidBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Postpaid details Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(prepaidBetweenDates);
				structure.setFlag(1);
				structure.setCount(prepaidBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Postpaid  details Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("POSTPAID TRANSACTION DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}



	public ResponseStructure postaidDateFilteringForEntity(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {
			
			String startDate = model.getStartDate().toString();
			String endDate = model.getEndDate().toString();
			
			List<PostpaidPayment> prepaidBetweenDates= postpaidRepository.getByStartDateAndEntityModel(startDate,endDate,model.getUserId());
			
			if(!prepaidBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Postpaid details Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(prepaidBetweenDates);
				structure.setFlag(1);
				structure.setCount(prepaidBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Postpaid  details Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("POSTPAID TRANSACTION DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}



	public ResponseStructure manualPaymentDateFiltering(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate = model.getStartDate()+" 00:00:00.0000000";
			String endDate = model.getEndDate()+" 23:59:59.9999999";
			
			List<ManualPayment> manualPayBetweenDates= manualPaymentRepository.getByCreatedAt(startDate,endDate);
			
			if(!manualPayBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Manual payment details Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(manualPayBetweenDates);
				structure.setFlag(1);
				structure.setCount(manualPayBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Manual payment done Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("MANUAL PAYMENT DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}



	public ResponseStructure manualPaymentDateFilteringForEntity(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate = model.getStartDate()+" 00:00:00.0000000";
			String endDate = model.getEndDate()+" 23:59:59.9999999";
			
			List<ManualPayment> manualPayBetweenDates= manualPaymentRepository.getByCreatedAtAndEntity(startDate,endDate,model.getUserId());
			
			if(!manualPayBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Manual payment details Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(manualPayBetweenDates);
				structure.setFlag(1);
				structure.setCount(manualPayBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Manual payment done Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("MANUAL PAYMENT DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}



	public ResponseStructure vendorHitsDateFiltering(RequestModel model) {
		
		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate = model.getStartDate()+" 00:00:00.0000000";
			String endDate = model.getEndDate()+" 23:59:59.9999999";
			
			List<Response> vendorHitBetweenDates= responseRepository.getByRequestDateAndTime(startDate, endDate);
			
			if(!vendorHitBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Vendor hits Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(vendorHitBetweenDates);
				structure.setFlag(1);
				structure.setCount(vendorHitBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No vendor hits done Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("VENDOR DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}



	public ResponseStructure merchantHitsDateFiltering(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate = model.getStartDate()+" 00:00:00.0000000";
			String endDate = model.getEndDate()+" 23:59:59.9999999";
			
			List<Request> merchantHitBetweenDates= requestRepository.getByRequestDateAndTime(startDate, endDate);
			
			if(!merchantHitBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Merchant hits Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(merchantHitBetweenDates);
				structure.setFlag(1);
				structure.setCount(merchantHitBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Merchant hits done Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("MERCHANT DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}



	public ResponseStructure merchantHitsByParticularEntityDateFiltering(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate = model.getStartDate()+" 00:00:00.0000000";
			String endDate = model.getEndDate()+" 23:59:59.9999999";
			
			List<Request> merchantHitBetweenDates= requestRepository.getBetween(model.getUserId(),startDate, endDate);
			
			if(!merchantHitBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Merchant hits Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(merchantHitBetweenDates);
				structure.setFlag(1);
				structure.setCount(merchantHitBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Merchant hits done Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("MERCHANT DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}



	public ResponseStructure merchantUatHits(RequestModel model) {
		
		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate = model.getStartDate()+" 00:00:00.0000000";
			String endDate = model.getEndDate()+" 23:59:59.9999999";
			
			List<RequestResponseReplica> merchantDummyHitBetweenDates= replicaRepository.getByRequestDateAndTime(startDate, endDate);
//			List<Request> freeHits= requestRepository.getBetweenFreeHits(model.getUserId(),startDate, endDate);
			
			
			if(!merchantDummyHitBetweenDates.isEmpty()) {// || !freeHits.isEmpty()
				
				
				List<Object> allFreeHits = new ArrayList<>();
				allFreeHits.addAll(merchantDummyHitBetweenDates);
//				allFreeHits.addAll(freeHits);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Hit logs Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(allFreeHits);
				structure.setFlag(1);
				structure.setCount(merchantDummyHitBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No hit logs Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("MERCHANT(PARTICULAR) DUMMY HIT DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
		
	}



	public ResponseStructure merchantUatHitsByParticularEntity(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate = model.getStartDate()+" 00:00:00.0000000";
			String endDate = model.getEndDate()+" 23:59:59.9999999";
			
			
			List<RequestResponseReplica> merchantDummyHitBetweenDates= replicaRepository.getBetween(model.getUserId(),startDate, endDate);
			
			if(!merchantDummyHitBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Merchant hits Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(merchantDummyHitBetweenDates);
				structure.setFlag(1);
				structure.setCount(merchantDummyHitBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Merchant hits done Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("MERCHANT DUMMY HIT DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}



	public ResponseStructure vendorHitsDateFilteringBySpecificVendor(RequestModel model) {
		
		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate = model.getStartDate()+" 00:00:00.0000000";
			String endDate = model.getEndDate()+" 23:59:59.9999999";
			
			List<Response> vendorHitBetweenDates= responseRepository.getByRequestDateAndTimeAndVendor(startDate, endDate,model.getVendorId());
			
			if(!vendorHitBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Vendor hits Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(vendorHitBetweenDates);
				structure.setFlag(1);
				structure.setCount(vendorHitBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No vendor hits done Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("VENDOR DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}



	public ResponseStructure vendorHitsDateFilteringBySpecificVendorAndVerification(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate = model.getStartDate()+" 00:00:00.0000000";
			String endDate = model.getEndDate()+" 23:59:59.9999999";
			
			List<Response> vendorHitBetweenDates= responseRepository.getByRequestDateAndTimeAndVendorAndVendorVerify(startDate, endDate,model.getVendorId(),model.getVendorVerificationId());
			
			if(!vendorHitBetweenDates.isEmpty()) {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Vendor hits Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(vendorHitBetweenDates);
				structure.setFlag(1);
				structure.setCount(vendorHitBetweenDates.size());
				
			}else {
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No vendor hits done Between "+model.getStartDate()+" and "+model.getEndDate());
				structure.setData(null);
				structure.setFlag(2);
				structure.setCount(0);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("VENDOR DATE FILTER : ",e);
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

}
