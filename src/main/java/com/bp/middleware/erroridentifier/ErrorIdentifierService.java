package com.bp.middleware.erroridentifier;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.customexception.FacheckSideException;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.smartrouteverification.SmartRouteUtils;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

@Service
public class ErrorIdentifierService {

	@Autowired
	private ErrorIdentifierRepository errorIdentifierRepository;
	@Autowired
	private VendorSideIssuesRepository vendorSideIssuesRepository;
	@Autowired
	private SmartRouteUtils smartRouteUtils;
	@Autowired
	private VendorVerificationRepository vendorVerificationRepository;

	public String errorSaver(Exception e, String apiName, EntityModel entity) {

		try {

			VendorVerificationModel verification = vendorVerificationRepository
					.findByVerificationDocument(apiName);
			
			ErrorIdentifier error = new ErrorIdentifier();

			String errorReference = generateErrorReferenceNumber();

			error.setErrorReferenceNumber(errorReference);
			error.setApiName(apiName);
			error.setOccuredDate(new Date());
			error.setReason(e.getMessage());
			error.setVendorSideIssues(null);

			if (e instanceof FacheckSideException) {
				
				FacheckSideException facheckException = (FacheckSideException) e;
				VendorSideIssues issue = facheckException.getVendorIssue();

				error.setVendorSideIssues(issue);

				RequestModel model = new RequestModel();

				model.setMessage(facheckException.getMessage());
				model.setError(facheckException.getMessage());
				model.setErrorCode(facheckException.getErrorCode());
				model.setSource(facheckException.getSource());
				model.setSourceType("Image/Id");
				model.setReferenceId(errorReference);

				if(facheckException.getEntity()!=null && facheckException.getVerification()!=null) {
				
				smartRouteUtils.trackEveryRequest(model, facheckException.getEntity(),
						facheckException.getVerification());
				
				}else if(entity!=null && verification!=null){
					
					smartRouteUtils.trackEveryRequest(model, entity,
							verification);
				}
				
			} else if(entity!=null && verification!=null){

				RequestModel model = new RequestModel();

				model.setMessage("An issue occured at server-side.");
				model.setError("There was a server-side issue." );
				model.setErrorCode("fc_"+HttpStatus.INTERNAL_SERVER_ERROR.value());
				model.setSource("");
				model.setSourceType("Image/Id");
				model.setReferenceId(errorReference);

				smartRouteUtils.trackEveryRequest(model, entity,
						verification);
			}

			StackTraceElement[] stackTraceElements = e.getStackTrace();
			if (stackTraceElements.length > 0) {

				error.setLineNumber(stackTraceElements[0].getLineNumber());
				error.setClassName(stackTraceElements[0].getClassName());
				error.setMethodName(stackTraceElements[0].getMethodName());

			} else {
				error.setLineNumber(0);
				error.setClassName("");
				error.setMethodName("");
			}

			errorIdentifierRepository.save(error);

			return errorReference;

		} catch (Exception e2) {
			e2.printStackTrace();
			return "-";
		}

	}

	public String generateErrorReferenceNumber() {

		UUID uuid = UUID.randomUUID();

		return uuid.toString().substring(0, 10);
	}

	public ResponseStructure viewAll() {

		ResponseStructure structure = new ResponseStructure();
		try {
			List<ErrorIdentifier> list = errorIdentifierRepository.allErrors();

			if (list.isEmpty()) {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);

			} else {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(list);
				structure.setFlag(1);

			}
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	public ResponseStructure viewByErrorReferenceId(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			ErrorIdentifier list = errorIdentifierRepository
					.findByErrorReferenceNumber(model.getErrorReferenceNumber());

			if (list == null) {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);

			} else {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(list);
				structure.setFlag(1);

			}
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	// -------------------------------------------------------------------------------------------------------------------------------//

	public void vendorIssues(RequestModel temporary) {

		try {

			VendorSideIssues issues = new VendorSideIssues();

			issues.setStatusCode(temporary.getStatusCodeNumber());
			issues.setMessage(temporary.getMessage());
			issues.setDate(new Date());
			issues.setVendorModel(temporary.getVendorModel());

			vendorSideIssuesRepository.save(issues);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ResponseStructure viewAllVendorSideIssues() {

		ResponseStructure st = new ResponseStructure();

		try {

			List<VendorSideIssues> findAll = vendorSideIssuesRepository.getVendorSidedIssue();

			if (!findAll.isEmpty()) {

				st.setMessage("SUCCESS");
				st.setData(findAll);

			} else {

				st.setMessage("NO DATA FOUND");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return st;
	}

	public ResponseStructure lastVendorIssue() {

		ResponseStructure st = new ResponseStructure();

		try {

			List<VendorSideIssues> findAll = vendorSideIssuesRepository.findAll();

			if (!findAll.isEmpty()) {

				st.setMessage("SUCCESS");
				st.setData(findAll.get(findAll.size() - 1));

			} else {

				st.setMessage("NO DATA FOUND");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return st;
	}

}
