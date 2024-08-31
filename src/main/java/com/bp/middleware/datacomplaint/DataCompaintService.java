package com.bp.middleware.datacomplaint;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.DateUtil;

@Service
public class DataCompaintService {

	@Autowired
	private DataComplaintRepository dataComplaintRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ResponseRepository responseRepository;

	public ResponseStructure addDataComplaint(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel user = userRepository.findByUserId(model.getUserId());

			if (user != null) {

				DataComplaint complaint = new DataComplaint();

				complaint.setVerificationType(model.getVerificationType());
				complaint.setFileNumber(model.getFileNumber());
				complaint.setLastFetchedDate(model.getLastFetchedDate());
				complaint.setMerchantName(user.getName());
				complaint.setMerchantEmail(user.getEmail());
				complaint.setSubmittedDate(LocalDate.now());
				complaint.setComment(model.getComment());
				complaint.setComplaintActive(true);
				complaint.setUser(user);

				dataComplaintRepository.save(complaint);

				structure.setMessage("DATA COMPLAINT SUCCESSFULLY RAISED");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(complaint);
				structure.setFlag(1);

			} else {

				structure.setMessage("ENTITY NOT FOUND");
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

	public ResponseStructure resolveDataComplaint(int dataComplaintId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<DataComplaint> option = dataComplaintRepository.findById(dataComplaintId);

			if (option.isPresent()) {

				DataComplaint complaint = option.get();

				Response inAccurateResponse = inAccurateResponse(complaint);

				System.out.println("IN ACC ID : " + inAccurateResponse.getResponseId());

				complaint.setCustomerName(inAccurateResponse.getFullName());
				complaint.setResponse(model.getComplaintResponse());
				complaint.setResolvedDate(LocalDate.now());
				complaint.setRemarks(model.getRemarks());
				complaint.setModifiedBy(model.getModifiedBy());

				if (model.getComplaintResponse().equalsIgnoreCase("Closed") || model.getRemarks().equalsIgnoreCase("Closed")) {
					complaint.setComplaintActive(false);
					complaint.setResolvedDate(LocalDate.now());
				}

				dataComplaintRepository.save(complaint);

                if(model.getComplaintResponse().equalsIgnoreCase("Closed") || model.getRemarks().equalsIgnoreCase("Closed")) {
                	structure.setMessage("DATA COMPLAINT SUCCESSFULLY RESOLVED");
                }else {
                	
                	structure.setMessage("DATA COMPLAINT NOT RESOLVED YET");
                }				
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(complaint);
				structure.setFlag(1);

			} else {

				structure.setMessage("NO COMPLAINT FOUND FOR THE GIVEN ID");
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

	private Response inAccurateResponse(DataComplaint complaint) throws Exception {

		List<Response> responseList = responseRepository.findBySourceAndStatus(complaint.getFileNumber(), "Success");

		System.out.println("+" + responseList.size());
		Response inAccurateResponse = new Response();

		for (Response response : responseList) {

			LocalDate dateResponse = DateUtil.stringToLocalDate(response.getResponseDateAndTime());
			System.out.println("DATE : " + dateResponse);

			response.setStatus("failed");
			responseRepository.save(response);

			if (dateResponse.isEqual(complaint.getLastFetchedDate())) {
				System.out.println(true);
				inAccurateResponse = response;
			}
		}
		return inAccurateResponse;
	}

	public ResponseStructure viewAll() {

		ResponseStructure structure = new ResponseStructure();

		try {

			List<DataComplaint> list = dataComplaintRepository.findAll();

			if (!list.isEmpty()) {

				structure.setMessage("DATA COMPLAINT LIST FOUND SUCCESSFULY");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(list);
				structure.setFlag(1);
			} else {

				structure.setMessage("NO DATA FOUND");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure viewById(int dataComplaintId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<DataComplaint> option = dataComplaintRepository.findById(dataComplaintId);

			if (option.isPresent()) {

				DataComplaint complaint = option.get();

				structure.setMessage("DATA COMPLAINT FOUND SUCCESSFULY FOR THE GIVEN ID");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(complaint);
				structure.setFlag(1);
			} else {

				structure.setMessage("NO DATA FOUND");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure viewByUserId(int userId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel entity = userRepository.findByUserId(userId);

			if (entity != null) {

				List<DataComplaint> list = dataComplaintRepository.findByUser(entity);

				if (!list.isEmpty()) {
					structure.setMessage("DATA COMPLAINT LIST FOUND SUCCESSFULY FOR THE GIVEN ENTITY");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(list);
					structure.setFlag(1);
				} else {

					structure.setMessage("NO DATA COMPLAINTS FOUND FOR THE GIVEN ENTITY");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}

			} else {

				structure.setMessage("GIVEN ENTITY NOT FOUND");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure viewByComplaintStatus(boolean complaintActive) {
		ResponseStructure structure = new ResponseStructure();

		try {

			List<DataComplaint> complaint = dataComplaintRepository.findByComplaintActive(complaintActive);

			if (!complaint.isEmpty()) {

				structure.setMessage("DATA COMPLAINT FOUND SUCCESSFULY FOR THE GIVEN COMPLAINT STATUS");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(complaint);
				structure.setFlag(1);

			} else {

				structure.setMessage("NO DATA FOUND");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

}
