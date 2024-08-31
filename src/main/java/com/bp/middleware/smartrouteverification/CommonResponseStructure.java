package com.bp.middleware.smartrouteverification;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bp.middleware.erroridentifier.ErrorIdentifierService;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.PasswordUtils;

@Component
public class CommonResponseStructure {
	
	@Autowired
	private  ErrorIdentifierService errorIdentifierService;

	public  JSONObject commonResponsePan(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("pan_number", temporary.getSource());
				validatedData.put("full_name", temporary.getFullName());
				validatedData.put("category", temporary.getCategory());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	

	
	public  JSONObject commonResponsePanComprehensive(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("pan_number", temporary.getSource());
				validatedData.put("full_name", temporary.getFullName());
				validatedData.put("category", temporary.getCategory());
				
				validatedData.put("email", temporary.getEmail());
				validatedData.put("phone_number", temporary.getMobileNumber());
				validatedData.put("gender", temporary.getGender());
				validatedData.put("dob", temporary.getDob());
				validatedData.put("input_dob", temporary.getInputDob());
				validatedData.put("dob_verified", temporary.isDobVerified());
				validatedData.put("dob_check", temporary.isDobCheck());
				validatedData.put("less_info", temporary.isLessInfo());
				validatedData.put("aadhaar_linked", temporary.isAadhaarLinked());
				validatedData.put("masked_aadhaar", temporary.getMaskedAadhaar());
				validatedData.put("split_name", temporary.getSplitName());
				validatedData.put("address", temporary.getAddressInJson());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	public  JSONObject commonResponsePanImage(RequestModel temporary) {
		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());
			
			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();
				JSONObject dataMatch = new JSONObject();

				validatedData.put("pan_number", temporary.getPan());
				validatedData.put("full_name", temporary.getFullName());
				validatedData.put("name", temporary.getName());
				validatedData.put("dob", temporary.getDob());
				validatedData.put("fathers_name", temporary.getFathersName());
				validatedData.put("category", temporary.getCategory());
				validatedData.put("client_id", temporary.getClientId());

				dataMatch.put("datamatch_aggregate", temporary.getDataMatchAggregate());
				dataMatch.put("datamatch_full_name", temporary.getDataMatchFullName());
				dataMatch.put("datamatch_pan_number", temporary.getDataMatchPanNumber());
				
				response.put("validated_data", validatedData);
				response.put("data_match", dataMatch);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	

	public  JSONObject commonResponseGst(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("gstin", temporary.getSource());
				validatedData.put("pan_number", temporary.getPanNumber());
				validatedData.put("business_name", temporary.getBusinessName());
				validatedData.put("legal_name", temporary.getLegalName());
				validatedData.put("center_jurisdiction", temporary.getCenterJurisdiction());
				validatedData.put("state_jurisdiction", temporary.getStateJurisdiction());
				validatedData.put("date_of_registration", temporary.getDateOfRegistration());
				validatedData.put("date_of_cancellation", temporary.getDateOfCancellation());
				validatedData.put("constitution_of_business", temporary.getConstitutionOfBusiness());
				validatedData.put("taxpayer_type", temporary.getTaxpayerType());
				validatedData.put("gstin_status", temporary.getGstInStatus());
				validatedData.put("field_visit_conducted", temporary.getFieldVisitConducted());
				validatedData.put("nature_of_core_business_activity_code", temporary.getCoreBusinessActivityCode());
				validatedData.put("nature_of_core_business_activity_description",
						temporary.getCoreBusinessActivityDescription());
				validatedData.put("aadhaar_validation", temporary.getAadharValidation());
				validatedData.put("aadhaar_validation_date", temporary.getAadharValidatedDate());
				validatedData.put("address", temporary.getAddress());
				validatedData.put("filing_status", temporary.getFilingStatusJsonList());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());

				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	public  JSONObject commonResponseGstImage(RequestModel temporary) {
		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());
			
			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();
				JSONObject dataMatch = new JSONObject();
				
				JSONObject hsnInfo = new JSONObject(temporary.getHsnInfo());
				JSONArray filingStatus = new JSONArray(temporary.getFilingStatusList());
				JSONArray natureBusActivity = new JSONArray(temporary.getNatureBusActivity());
				
				validatedData.put("gstin", temporary.getGstIn());
				validatedData.put("business_name", temporary.getBusinessName());
				validatedData.put("date_of_registration", temporary.getDateOfRegistration());
				validatedData.put("date_of_cancellation", temporary.getDateOfCancellation());
				validatedData.put("taxpayer_type", temporary.getTaxpayerType());
				validatedData.put("address", temporary.getAddress());
				validatedData.put("corebusiness_activity_code", temporary.getCoreBusinessActivityCode());
				validatedData.put("corebusiness_activity_description", temporary.getCoreBusinessActivityDescription());
				validatedData.put("center_jurisdiction", temporary.getCenterJurisdiction());
				validatedData.put("constitution_of_business", temporary.getConstitutionOfBusiness());
				validatedData.put("gstin_status", temporary.getGstInStatus());
				validatedData.put("pan_number", temporary.getPanNumber());
				validatedData.put("state_jurisdiction", temporary.getStateJurisdiction());
				validatedData.put("aadhar_validated_date", temporary.getAadharValidatedDate());
				validatedData.put("field_visit_conducted", temporary.getFieldVisitConducted());
				validatedData.put("legal_name", temporary.getLegalName());
				validatedData.put("aadhar_validation", temporary.getAadharValidation());
				validatedData.put("dio", temporary.getDoi());
				validatedData.put("trade_name", temporary.getTradeName());
				validatedData.put("valid_gst", temporary.isValidGst());
				validatedData.put("hsn_info", hsnInfo);
				validatedData.put("filing_status", filingStatus);
				validatedData.put("nature_bus_activity", natureBusActivity);

				dataMatch.put("datamatch_aggregate", temporary.getDataMatchAggregate());
				dataMatch.put("datamatch_taxpayertype", temporary.getDataMatchTaxpayerType());
				dataMatch.put("datamatch_businessname", temporary.getDataMatchBusinessNamer());
				dataMatch.put("datamatch_address", temporary.getDataMatchAddress());
				dataMatch.put("datamatch_constitution_of_business", temporary.getDataMatchCostitutionOfBusiness());
				dataMatch.put("datamatch_date_of_registration", temporary.getDataMatchDateOfRegistration());
				dataMatch.put("datamatch_gstin", temporary.getDataMatchGstIn());
				dataMatch.put("datamatch_tradename", temporary.getDataMatchTradeName());
				
				response.put("validated_data", validatedData);
				response.put("data_match", dataMatch);

			} else {

				response.put("error", temporary.getError());

				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	public  JSONObject commonResponseDirectAadhaar(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("aadhaar_number", temporary.getAadhaarNumber());
				validatedData.put("age_range", temporary.getAgeRange());
				validatedData.put("state", temporary.getState());
				validatedData.put("gender", temporary.getGender());
				validatedData.put("last_digits", temporary.getLastDigits());
				validatedData.put("remarks", temporary.getRemarks());
				validatedData.put("is_mobile", temporary.isMobilePresent());
				validatedData.put("less_info", temporary.isLessInfo());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	public  JSONObject commonResponseAadharwithOtp(RequestModel temporary) {
		try {

			JSONObject response = new JSONObject();

			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("status", "success");
				response.put("message", "Document processed successfully");
				response.put("otp_generated", true);

			} else {

				response.put("status", "failed");
				response.put("otp_generated", false);
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public  JSONObject commonResponseAadharOtpSubmit(RequestModel temporary) {
		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("full_name", temporary.getFullName());
				validatedData.put("aadhaar_number", temporary.getAadhaarNumber());
				validatedData.put("dob", temporary.getDob());
				validatedData.put("address", temporary.getAddress());
				validatedData.put("gender", temporary.getGender());
				validatedData.put("mobile_verified", temporary.isMobileVerified());

				validatedData.put("face_status", temporary.isFaceStatus());
				validatedData.put("face_score", temporary.getFaceScore());
				validatedData.put("zip", temporary.getZip());
				validatedData.put("has_image", temporary.isHasImage());
				validatedData.put("mobile_hash", temporary.getMobileHash());
				validatedData.put("email_hash", temporary.getEmailHash());
				validatedData.put("raw_xml", temporary.getRawXml());
				validatedData.put("zip_data", temporary.getZipData());
				validatedData.put("care_of", temporary.getCareOf());
				validatedData.put("share_code", temporary.getShareCode());
				validatedData.put("reference_id", temporary.getAadharReferenceId());
				validatedData.put("status", temporary.getAadharStatus());
				validatedData.put("uniqueness_id", temporary.getUniquenessId());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", "error");
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public  JSONObject commonResponseCin(RequestModel temporary) {
		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());
				
				JSONObject companyInfo = new JSONObject();
				
				companyInfo.put("cin", temporary.getCin());
				companyInfo.put("roc_code", temporary.getRocCode());
				companyInfo.put("registration_number", temporary.getRegistrationNumber());
				companyInfo.put("company_category", temporary.getCompanyCategory());
				companyInfo.put("class_of_company", temporary.getClassOfCompany());
				companyInfo.put("company_sub_category", temporary.getCompanySubCategory());
				companyInfo.put("authorized_capital", temporary.getAuthorizedCapital());
				companyInfo.put("paid_up_capital", temporary.getPaidUpCapital());
				companyInfo.put("number_of_members", temporary.getNumberOfMembers());
				companyInfo.put("date_of_incorporation", temporary.getDateOfIncorporation());
				companyInfo.put("registered_address", temporary.getRegisteredAddress());
				companyInfo.put("address_other_than_ro", temporary.getAddressOtherThanRo());
				companyInfo.put("email", temporary.getEmail());
				companyInfo.put("listed_status", temporary.getListedStatus());
				companyInfo.put("active_compilance", temporary.getActiveCompilance());
				companyInfo.put("suspended_at_stock", temporary.getSuspendedAtStockExchange());
				companyInfo.put("last_agm_date", temporary.getLastAgmDate());
				companyInfo.put("last_bs_date", temporary.getLastBsDate());
				companyInfo.put("company_status", temporary.getCompanyStatus());
				companyInfo.put("status_under_cirp", temporary.getStatusUnderCirp());
				
				JSONObject validatedData = new JSONObject();

				validatedData.put("company_id", temporary.getCompanyId());
				validatedData.put("company_type", temporary.getCompanyType());
				validatedData.put("company_name", temporary.getCompanyName());
				validatedData.put("company_info", companyInfo);
				validatedData.put("directors", temporary.getDirectors());
				validatedData.put("charges", temporary.getCharges());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());

				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}

	public  JSONObject commonResponseDrivingLicense(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());
			response.put("messsage", temporary.getMessage());


			if (temporary.getStatus().equalsIgnoreCase("success")) {

				JSONArray vehicleClass=new JSONArray(temporary.getVehicleClasses());
				JSONArray additionalCheck= new JSONArray(temporary.getAdditionalCheck());

				JSONObject validatedData = new JSONObject();

				validatedData.put("license_number", temporary.getLicenceNumber());
				validatedData.put("name", temporary.getFullName());
				validatedData.put("permanent_address", temporary.getAddress());
				validatedData.put("dob", temporary.getDob());
				validatedData.put("state", temporary.getStateName());
				validatedData.put("gender", temporary.getGender());
				validatedData.put("permanent_zip", temporary.getPermanentZip());
				validatedData.put("temporary_address", temporary.getTemporaryAddress());
				validatedData.put("temporary_zip", temporary.getTemporaryZip());
				validatedData.put("citizenship", temporary.getCitizenShip());
				validatedData.put("ola_name", temporary.getOlaName());
				validatedData.put("ola_code", temporary.getOlaCode());
				validatedData.put("father_or_husband_name", temporary.getFatherOrHusbandName());
				validatedData.put("doe", temporary.getDoe());
				validatedData.put("transport_doe", temporary.getTransportDoe());
				validatedData.put("doi", temporary.getDoi());
				validatedData.put("transport_doi", temporary.getTransportDoi());
				validatedData.put("has_image", temporary.isHasImage());
				validatedData.put("blood_group", temporary.getBloodGroup());
				validatedData.put("less_info", temporary.isLessInfo());
				validatedData.put("initial_doi", temporary.getInitialDoi());
				validatedData.put("current_status", temporary.getCurrentStatus());
				validatedData.put("vehicle_classes", vehicleClass);
				validatedData.put("additional_check", additionalCheck);
				validatedData.put("profile_image", temporary.getProfileImage());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}

	public  JSONObject commonResponsePassport(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("passport_number", temporary.getPassportId());
				validatedData.put("full_name", temporary.getFullName());
				validatedData.put("dob", temporary.getDob());
				validatedData.put("date_of_application", temporary.getDateOfApplication());
				validatedData.put("file_number", temporary.getFileNumber());
				validatedData.put("verification_message", temporary.getVerificationMessage());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());

				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}

	//SHOULD BE CHANGED ACCORDING TO COMMON FIELDS ----- done changed
	public  JSONObject commonResponseDin(RequestModel temporary) {
		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());
				
				JSONObject validatedData = new JSONObject();

				validatedData.put("din_number", temporary.getDinNumber());
				validatedData.put("father_name", temporary.getFatherName());
				validatedData.put("full_name", temporary.getFullName());
				validatedData.put("dob", temporary.getDob());
				validatedData.put("nationality", temporary.getNationality());
				validatedData.put("present_address", temporary.getPresentAddress());
				validatedData.put("permanent_address", temporary.getPermanentAddress());
				validatedData.put("email", temporary.getEmail());
				validatedData.put("pan_number", temporary.getPan());
				validatedData.put("din_status", temporary.getDinStatus());
				validatedData.put("companies_associated", temporary.getCompaniesAssociated());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());

				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	
	
	public  JSONObject commonResponseRc(RequestModel temporary) {
		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());
				
				JSONObject validatedData = new JSONObject();

				validatedData.put("rc_number", temporary.getRcNumber());
				validatedData.put("date_of_registration", temporary.getDateOfRegistration());
				validatedData.put("owner_name", temporary.getOwnerName());
				validatedData.put("father_name", temporary.getFatherName());
				validatedData.put("present_address", temporary.getPresentAddress());
				validatedData.put("permanent_address", temporary.getPermanentAddress());
				validatedData.put("mobile_number", temporary.getMobileNumber());
				validatedData.put("vehicle_category", temporary.getVehicleCategory());
				validatedData.put("vehicle_chasis_number", temporary.getVehicleChasisNumber());
				validatedData.put("engine_number", temporary.getVehicleEngineNumber());
				validatedData.put("maker_description", temporary.getMakerDescription());
				validatedData.put("maker_model", temporary.getMakerModel());
				validatedData.put("body_type", temporary.getBodyType());
				validatedData.put("fuel_type", temporary.getFuelType());
				validatedData.put("color", temporary.getColor());
				validatedData.put("norms_type", temporary.getNormsType());
				validatedData.put("fit_upto", temporary.getFitUpTo());
				validatedData.put("financer", temporary.getFinancer());
				validatedData.put("insurance_company", temporary.getInsuranceCompany());
				validatedData.put("insurance_policy_number", temporary.getInsurancePolicyNumber());
				validatedData.put("insurance_upto", temporary.getInsuranceUpto());
				validatedData.put("manufacturing_date", temporary.getManufacturingDate());
				validatedData.put("manufacturing_date_format", temporary.getManufacturingDateForma());
				validatedData.put("registered_at", temporary.getRegisteredAt());
				validatedData.put("latest_by", temporary.getLatestBy());
				validatedData.put("tax_upto", temporary.getTaxUpto());
				validatedData.put("tax_paid_upto", temporary.getTaxPaidUpto());
				validatedData.put("cubic_capacity", temporary.getCubicCapacity());
				validatedData.put("vehicle_gross_weight", temporary.getVehicleGrossWeight());
				validatedData.put("no_of_cylinders", temporary.getNoOfCylinders());
				validatedData.put("seat_capacity", temporary.getSeatCapacity());
				validatedData.put("sleeper_capacity", temporary.getSleeperCapacity());
				validatedData.put("standing_capacity", temporary.getStandingCapacity());
				validatedData.put("wheel_base", temporary.getWheelBase());
				validatedData.put("unladen_weight", temporary.getUnladenWeight());
				validatedData.put("vehicle_category_description", temporary.getVehicleCategoryDescription());
				validatedData.put("pucc_number", temporary.getPuccNumber());
				validatedData.put("pucc_upto", temporary.getPuccUpto());
				validatedData.put("permit_number", temporary.getPermitNumber());
				validatedData.put("permit_issue_date", temporary.getPermitIssueDate());
				validatedData.put("permit_valid_from", temporary.getPermitValidFrom());
				validatedData.put("permit_valid_upto", temporary.getPermitValidUpto());
				validatedData.put("permit_type", temporary.getPermitType());
				validatedData.put("national_permit_number", temporary.getNationalPermitNumber());
				validatedData.put("national_permit_upto", temporary.getNationalPermitUpto());
				validatedData.put("national_permit_issuedby", temporary.getNationalPermitIssuedBy());
				validatedData.put("non_use_status", temporary.getNonUseStatus());
				validatedData.put("non_use_form", temporary.getNonUseFrom());
				validatedData.put("non_use_to", temporary.getNonUseTo());
				validatedData.put("black_list_status", temporary.getBlackListStatus());
				validatedData.put("noc_details", temporary.getNocDetails());
				validatedData.put("owner_name", temporary.getOwnerNumber());
				validatedData.put("rc_status", temporary.getRcStatus());
				validatedData.put("variant", temporary.getVariant());
				validatedData.put("challan_details", temporary.getChallanDetails());
				validatedData.put("financed", temporary.isFinanced());
				validatedData.put("less_info", temporary.isLessInfo());
				validatedData.put("masked_name", temporary.isMaskedNamePresent());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());

				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}

	public  JSONObject commonResponseUdyamMsme(RequestModel temporary) {
		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				JSONArray LocationOfPlant=new JSONArray(temporary.getLocationOfPlantDetails());
				JSONArray nicCode= new JSONArray(temporary.getNicCode());
				
				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();
				
				validatedData.put("uan", temporary.getUan());
				validatedData.put("name_of_enterprise", temporary.getNameOfEnterprise());
				validatedData.put("major_activity", temporary.getMajorActivity());
				validatedData.put("social_category", temporary.getSocialCategory());
				validatedData.put("enterprise_type", temporary.getEnterpriseType());
				validatedData.put("date_of_commencement", temporary.getDateOfCommencement());
				validatedData.put("dic_name", temporary.getDicName());
				validatedData.put("state", temporary.getState());
				validatedData.put("applied_date", temporary.getAppliedDate());
				validatedData.put("location_of_plant_details", LocationOfPlant);
				validatedData.put("nic_code", nicCode);
				
				if(temporary.getEnterprizeTypeList().length()!=0) {
					validatedData.put("enterprise_type_list", temporary.getEnterprizeTypeList());
				}else {
					validatedData.put("enterprise_type_list", new JSONArray());
				}

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());

				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}



	public  JSONObject commonResponseVoterId(RequestModel temporary) {
		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("message", temporary.getMessage());

				JSONObject validatedData = new JSONObject();
				
				validatedData.put("name_v1", temporary.getNamev1());
				validatedData.put("rln_name_v1", temporary.getRelationNameV1());
				validatedData.put("relation_type", temporary.getRelationType());
				validatedData.put("epic_no", temporary.getSource());
				validatedData.put("gender", temporary.getGender());
				validatedData.put("assembly_constituency_number", temporary.getAssemblyContituencyNumber());
				validatedData.put("client_id", temporary.getClientId());
				validatedData.put("state", temporary.getState());
				validatedData.put("ps_lat_long", temporary.getPsLatLong());
				validatedData.put("id", temporary.getIdNumber());
				//
				validatedData.put("assembly_constituency", temporary.getAssemblyConstituency());
				validatedData.put("area", temporary.getArea());
				validatedData.put("multiple", temporary.isMultiple());
				validatedData.put("parliamentary_constituency", temporary.getParliamentaryConstituency());
				validatedData.put("part_number", temporary.getPartNumber());
				validatedData.put("name", temporary.getFullName());
				validatedData.put("polling_station", temporary.getPollingStation());
				validatedData.put("section_no", temporary.getSectionNo());
				validatedData.put("slno_inpart",temporary.getSlnoInpart());
				validatedData.put("relation_name", temporary.getRelationName());
				validatedData.put("age", temporary.getAge());
				validatedData.put("part_name", temporary.getPartName());
				
				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());

				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}



	public  JSONObject commonResponseAadhaarOcr(RequestModel temporary) {
		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("message", temporary.getMessage());
				response.put("data", temporary.getOcrData());

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	public  JSONObject commonResponseEmail(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("name", temporary.getName());
				validatedData.put("email", temporary.getEmail());
				validatedData.put("domain", temporary.getDomain());
				validatedData.put("email_status", temporary.getEmailStatus());
				validatedData.put("accepts_mail", temporary.isAcceptsMail());
				validatedData.put("is_catch_all", temporary.isCatchAll());
				validatedData.put("valid", temporary.isValid());
				validatedData.put("valid_syntax", temporary.isValidSyntax());
				validatedData.put("smtp_connected", temporary.isSmtpConnected());
				validatedData.put("temporary", temporary.isTemporary());
				validatedData.put("disabled", temporary.isDisabled());
				validatedData.put("results", temporary.isResults());
				validatedData.put("mx_records", temporary.getMxRecords());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}

	
	public  JSONObject commonResponseItrCompilanceCheck(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("pan_number", temporary.getPanNumber());
				validatedData.put("pan_allotment_date", temporary.getPanAllotmentDate());
				validatedData.put("masked_name", temporary.getMaskedName());
				validatedData.put("pan_aadhaar_linked", temporary.getPanAadhaarLinked());
				validatedData.put("specified_person_under_206", temporary.getSpecifiedPersonUnder206());
				validatedData.put("pan_status", temporary.getPanStatus());
				validatedData.put("valid_pan", temporary.isValidPan());
				validatedData.put("complaint", temporary.isComplaint());
			
				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	public  JSONObject commonResponseFssai(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("fssai_number", temporary.getFssaiNumber());
				validatedData.put("application_number", temporary.getApplicationNumber());
				validatedData.put("address_premesis", temporary.getAddressPresis());
				validatedData.put("status_desc", temporary.getStatusDesc());
				validatedData.put("district_name", temporary.getDistrctName());
				validatedData.put("display_ref_id", temporary.getDisplayRefId());
				validatedData.put("taluk_name", temporary.getTalukName());
				validatedData.put("company_name", temporary.getCompanyName());
				validatedData.put("app_type_desc", temporary.getAppTypeDesc());
				validatedData.put("state", temporary.getStateName());
				validatedData.put("license_category_name", temporary.getLicenseCategoryName());
				validatedData.put("app_submition_date", temporary.getAppSubmitionDate());
				validatedData.put("last_updated_on", temporary.getLastUpdatedOn());
				validatedData.put("fbo_id", temporary.getFboId());
				validatedData.put("ref_id", temporary.getReferId());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	public  JSONObject commonResponseUpi(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("upi_id", temporary.getUpiId());
				validatedData.put("full_name", temporary.getFullName());
				validatedData.put("remarks", temporary.getRemarks());
				validatedData.put("account_exists", temporary.isAccountExists());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	public  JSONObject commonResponseBank(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("account_exists", temporary.isAccountExists());
				validatedData.put("full_name", temporary.getFullName());
				validatedData.put("upi_id", temporary.getUpiId());
				validatedData.put("remarks", temporary.getRemarks());
				
				
				JSONObject ifscDetails = new JSONObject();
				
				ifscDetails.put("id", temporary.getId());
				ifscDetails.put("ifsc", temporary.getIfsc());
				ifscDetails.put("micr", temporary.getMicr());
				ifscDetails.put("iso3166", temporary.getIso3166());
				ifscDetails.put("bank", temporary.getBank());
				ifscDetails.put("bank_code", temporary.getBankCode());
				ifscDetails.put("bank_name", temporary.getBankName());
				ifscDetails.put("branch", temporary.getBranch());
				ifscDetails.put("centre", temporary.getCentre());
				ifscDetails.put("district", temporary.getDistrict());
				ifscDetails.put("state", temporary.getState());
				ifscDetails.put("city", temporary.getCity());
				ifscDetails.put("address", temporary.getAddress());
				ifscDetails.put("imps", temporary.isImps());
				ifscDetails.put("rtgs", temporary.isRtgs());
				ifscDetails.put("upi", temporary.isUpiPresent());
				ifscDetails.put("neft", temporary.isNeft());
				ifscDetails.put("micr_check", temporary.isMicrCheck());
				ifscDetails.put("swift", temporary.getSwift());
				ifscDetails.put("contact", temporary.getContact());
				
				
				validatedData.put("ifsc_details", ifscDetails);

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());

				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	public  JSONObject commonResponseBankPennyless(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("account_exists", temporary.isAccountExists());
				validatedData.put("full_name", temporary.getFullName());
				validatedData.put("upi_id", temporary.getUpiId());
				validatedData.put("remarks", temporary.getRemarks());
				
				JSONObject ifscDetails = new JSONObject();
				
				ifscDetails.put("id", temporary.getId());
				ifscDetails.put("ifsc", temporary.getIfsc());
				ifscDetails.put("micr", temporary.getMicr());
				ifscDetails.put("iso3166", temporary.getIso3166());
				ifscDetails.put("bank", temporary.getBank());
				ifscDetails.put("bank_code", temporary.getBankCode());
				ifscDetails.put("bank_name", temporary.getBankName());
				ifscDetails.put("branch", temporary.getBranch());
				ifscDetails.put("centre", temporary.getCentre());
				ifscDetails.put("district", temporary.getDistrict());
				ifscDetails.put("state", temporary.getState());
				ifscDetails.put("city", temporary.getCity());
				ifscDetails.put("address", temporary.getAddress());
				ifscDetails.put("imps", temporary.isImps());
				ifscDetails.put("rtgs", temporary.isRtgs());
				ifscDetails.put("upi", temporary.isUpiPresent());
				ifscDetails.put("neft", temporary.isNeft());
				ifscDetails.put("micr_check", temporary.isMicrCheck());
				ifscDetails.put("swift", temporary.getSwift());
				ifscDetails.put("contact", temporary.getContact());
				

				validatedData.put("ifsc_details", ifscDetails);

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());

				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	public  JSONObject commonResponseAadhaarToUan(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("aadhaar_number", temporary.getAadhaarNumber());
				validatedData.put("pf_uan", temporary.getPfUan());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	public  JSONObject commonResponsePanToUan(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("pan_number", temporary.getPanNumber());
				validatedData.put("pf_uan", temporary.getPfUan());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	public  JSONObject commonResponseMobileToUan(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("mobile_number", temporary.getMobileNumber());
				validatedData.put("pf_uan", temporary.getPfUan());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	public  JSONObject commonResponseEmploymentHistory(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				
				validatedData.put("employment_history", temporary.getEmploymentHistory());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	public  JSONObject commonResponseDirectorMobile(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("din_number", temporary.getSource());
				validatedData.put("phone_number", temporary.getMobileNumber());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	
	public  JSONObject commonResponseIcai(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("membership_number", temporary.getMembershipNumber());
				validatedData.put("details", temporary.getDetails());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	public  JSONObject commonResponseIec(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("iec_number", temporary.getIecNumber());
				validatedData.put("details", temporary.getDetails());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	public  JSONObject commonResponseIecAdvanced(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("iec_number", temporary.getIecNumber());
				validatedData.put("firm_name", temporary.getFirmName());
				validatedData.put("pan_number", temporary.getPanNumber());
				validatedData.put("dob", temporary.getDob());
				validatedData.put("iec_issuance_date", temporary.getIecIssuanceDate());
				validatedData.put("iec_status", temporary.getIecStatus());
				validatedData.put("del_status", temporary.getDelStatus());
				validatedData.put("iec_cancelled_date", temporary.getIecCancelledDate());
				validatedData.put("iec_suspended_date", temporary.getIecSuspendedDate());
				validatedData.put("file_number", temporary.getFileNumber());
				validatedData.put("file_date", temporary.getFileDate());
				validatedData.put("dgft_ra_office", temporary.getDgftraOffice());
				validatedData.put("nature_of_concern", temporary.getNatureOfConcern());
				validatedData.put("category_of_exporters", temporary.getCategoryOfExporters());
				validatedData.put("address", temporary.getAddress());
				validatedData.put("firm_mobileno", temporary.getFirmMobileNo());
				validatedData.put("firm_email_id", temporary.getFirmEmailId());
				validatedData.put("branch_details", temporary.getBranchDetails());
				validatedData.put("remc_details", temporary.getRemcDetails());
				validatedData.put("director_details", temporary.getDirectorDetails());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	public  JSONObject commonResponseEsic(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("id_number", temporary.getIdNumber());
				validatedData.put("name", temporary.getName());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
	
	
	public  JSONObject commonResponseEsicAdvanced(RequestModel temporary) {

		try {

			JSONObject response = new JSONObject();

			response.put("status", temporary.getStatus());
			response.put("response_time", temporary.getResponseDateAndTime());
			response.put("reference_id", temporary.getReferenceId());

			if (temporary.getStatus().equalsIgnoreCase("success")) {

				response.put("messsage", temporary.getMessage());

				JSONObject validatedData = new JSONObject();

				validatedData.put("id_number", temporary.getIdNumber());
				validatedData.put("name", temporary.getName());
				validatedData.put("mobile_number", temporary.getMobileNumber());

				response.put("validated_data", validatedData);

			} else {

				response.put("error", temporary.getError());
				
				if(temporary.getStatusCodeNumber()==401 || temporary.getStatusCodeNumber()==403) {
					 response.put("messsage", AppConstants.REQUEST_TO_SERVER_FAILED);
					 response.put("error", AppConstants.CONNECTION_TO_SERVER_FAILED);
					 
					 errorIdentifierService.vendorIssues(temporary);
				}
				else if(temporary.getMessage()==null) {
				    response.put("messsage", "Document processing failed");
				}else {
					response.put("messsage",temporary.getMessage());
				}
			}

			return response;

		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}
	
}
