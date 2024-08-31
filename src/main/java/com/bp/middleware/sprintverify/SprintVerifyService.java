package com.bp.middleware.sprintverify;

import java.util.List;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

import jakarta.servlet.http.HttpServletRequest;

public interface SprintVerifyService {

	ResponseStructure aadharDirect(RequestModel model);

	ResponseStructure aadharWithOtp(RequestModel model);

	ResponseStructure aadharOtpValidate(RequestModel model);

	ResponseStructure voterId(RequestModel model, HttpServletRequest servletRequest);

	ResponseStructure drivingLicence(RequestModel model);

	ResponseStructure gst(RequestModel model);

	ResponseStructure passport(RequestModel model);

	ResponseStructure udyamAadhar(RequestModel model);

	ResponseStructure itrComplianceCheck(RequestModel model);

	ResponseStructure itrAcknowledgement(RequestModel model);

	ResponseStructure mcaCompanyDetails(RequestModel model);

	ResponseStructure taxDeductionAccountNumber(RequestModel model);

	ResponseStructure stateListGet(RequestModel model);

	ResponseStructure shopEstablishmentDetails(RequestModel model);

	ResponseStructure registerCertificate(RequestModel model);

	ResponseStructure iecCheck(RequestModel model);

	ResponseStructure fssaiCheck(RequestModel model);

	ResponseStructure emailCheck(RequestModel model);

	ResponseStructure legalEntityIdentifier(RequestModel model);

	ResponseStructure aadharQrSearch(RequestModel model);

	ResponseStructure epfoOtpSend(RequestModel model);

	ResponseStructure epfoOtpVerify(RequestModel model);

	ResponseStructure passbookDownload(RequestModel model);

	ResponseStructure epfoKycDetailsGet(RequestModel model);

	ResponseStructure epfoWithoutOtp(RequestModel model);

	ResponseStructure ckycSearch(RequestModel model);

	ResponseStructure ckycDownload(RequestModel model);

	ResponseStructure faceMatch(RequestModel model);

	ResponseStructure livenessCheck(RequestModel model);

	ResponseStructure upiIndex(RequestModel model);

	ResponseStructure vehicleChallan(RequestModel model);

	ResponseStructure opticalCharacterRecognition(RequestModel model);

	ResponseStructure bankAccountVerificationOne(RequestModel model);

	ResponseStructure bankAccountVerificationTwo(RequestModel model);

	ResponseStructure bavPennyLess(RequestModel model);

	ResponseStructure bavPennydropVOne(RequestModel model, HttpServletRequest servletRequest);

	ResponseStructure bavPennydropVTwo(RequestModel model);

	ResponseStructure revereseGeoLocation(RequestModel model);

	ResponseStructure ipAddressLookUp(RequestModel model);

	ResponseStructure mobileOperatorCheck(RequestModel model);

	ResponseStructure sprintVPan(RequestModel model);

	ResponseStructure itrCreateClient(RequestModel model);

	ResponseStructure itrForgetPassword(RequestModel model);

	ResponseStructure itrOtpSubmit(RequestModel model);

	ResponseStructure itrProfileGet(RequestModel model);

	ResponseStructure getItrList(RequestModel model);

	ResponseStructure getSingleItrDetails(RequestModel model);

	ResponseStructure get26AsList(RequestModel model);

	ResponseStructure getSingle26AsList(RequestModel model);

	ResponseStructure courtCaseStatus(RequestModel model);

	ResponseStructure companyTanLookup(RequestModel model);

	ResponseStructure fuelPriceFetch(RequestModel model);

	ResponseStructure panToGst(RequestModel model);

	ResponseStructure stockPriceVerify(RequestModel model);

	ResponseStructure regionalTransport(RequestModel model);

	ResponseStructure mobileNumberCase(RequestModel model);

	ResponseStructure panDetailedInfo(RequestModel model);

	ResponseStructure panComprehensive(RequestModel model);

	ResponseStructure companyNameToCin(RequestModel model);

	ResponseStructure telecomSendOtp(RequestModel model);

	ResponseStructure telecomGetDetails(RequestModel model);

	ResponseStructure digiLockerInitiateSession(RequestModel model);

	ResponseStructure digiLockerAccessTokenGeneration(RequestModel model);

	ResponseStructure digiLockerGetIssuedFiles(RequestModel model);

	ResponseStructure digiLockerDownloadDocInPdf(RequestModel model);

	ResponseStructure digiLockerDownloadDocInXml(RequestModel model);

	ResponseStructure digiLockerEaadhaarDocInXml(RequestModel model);

	ResponseStructure mcaDin(RequestModel model);

	ResponseStructure mcaCin(RequestModel model);

	ResponseStructure sprintVBulkPan(List<RequestModel> model, HttpServletRequest servletRequest);

	ResponseStructure individualCrimeCheck(RequestModel model, HttpServletRequest servletRequest);

	ResponseStructure demoSprintVController(RequestModel model);

	ResponseStructure crimeCheckPdfDownload(RequestModel model, HttpServletRequest servletRequest);

	ResponseStructure crimeCheckJsonReportDownload(RequestModel model, HttpServletRequest servletRequest);

}
