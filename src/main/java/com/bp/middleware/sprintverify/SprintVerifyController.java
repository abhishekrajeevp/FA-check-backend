package com.bp.middleware.sprintverify;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/facheck-sprintdocumentation")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class SprintVerifyController {

	@Autowired
	private SprintVerifyService service;
	
	
	/* CURRENTLY USING INDIVIDUAL API's */
	
	@PostMapping("/bavpennydrop1")
	public ResponseStructure bavPennydropVOne(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return service.bavPennydropVOne(model,servletRequest);
	}
	
	@PostMapping("/voterid")
	public ResponseStructure voterId(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return service.voterId(model,servletRequest);
	}
	
	@PostMapping("/individualcrimecheck")
	public  ResponseStructure individualCrimeCheck(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return service.individualCrimeCheck(model,servletRequest);
	}
	
	@PostMapping("/crimecheckpdfdownload")
	public  ResponseStructure crimeCheckPdfDownload(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return service.crimeCheckPdfDownload(model,servletRequest);
	}
	
	@PostMapping("/crimecheckjsonreportdownload")
	public  ResponseStructure crimeCheckJsonReportDownload(@RequestBody RequestModel model,HttpServletRequest servletRequest) {
		return service.crimeCheckJsonReportDownload(model,servletRequest);
	}
	
	
	
	/* ------------------------------------------------------------------------------------------------------------------------------------------ */
	
	@PostMapping("/aadhar")
	public ResponseStructure aadharDirect(@RequestBody RequestModel model) {
		return service.aadharDirect(model);
	}
	
	@PostMapping("/aadharwithotp")
	public ResponseStructure aadharWithOtp(@RequestBody RequestModel model) {
		return service.aadharWithOtp(model);
	}
	
	@PostMapping("/aadharotpvalidate")
	public ResponseStructure aadharOtpValidate(@RequestBody RequestModel model) {
		return service.aadharOtpValidate(model);
	}
	
	@PostMapping("/pan")
	public  ResponseStructure sprintVPan(@RequestBody RequestModel model) {
		return service.sprintVPan(model);
	}
	
	@PostMapping("/panbulk")
	public  ResponseStructure sprintVBulkPan(@RequestBody List<RequestModel> model,HttpServletRequest servletRequest) {
		return service.sprintVBulkPan(model,servletRequest);
	}
	
	@PostMapping("/gst")
	public ResponseStructure gst(@RequestBody RequestModel model) {
		return service.gst(model);
	}
	
	@PostMapping("/passport")
	public ResponseStructure passport(@RequestBody RequestModel model) {
		return service.passport(model);
	}
	
	@PostMapping("/rcverify")
	public ResponseStructure registerCertificate(@RequestBody RequestModel model) {
		return service.registerCertificate(model);
	}
	
	@PostMapping("/drivinglicence")
	public ResponseStructure drivingLicence(@RequestBody RequestModel model) {
		return service.drivingLicence(model);
	}
	
	@PostMapping("/udyamaadhar")
	public ResponseStructure udyamAadhar(@RequestBody RequestModel model) {
		return service.udyamAadhar(model);
	}
	
	@PostMapping("/mcacin")
	public ResponseStructure mcaCin(@RequestBody RequestModel model) {
		return service.mcaCin(model);
	}
	
	@PostMapping("/mcadin")
	public ResponseStructure mcaDin(@RequestBody RequestModel model) {
		return service.mcaDin(model);
	}
	
	@PostMapping("/bav1")
	public ResponseStructure bankAccountVerificationOne(@RequestBody RequestModel model) {
		return service.bankAccountVerificationOne(model);
	}
	
	@PostMapping("/bav2")
	public ResponseStructure bankAccountVerificationTwo(@RequestBody RequestModel model) {
		return service.bankAccountVerificationTwo(model);
	}
	
	@PostMapping("/bavpennyless")
	public ResponseStructure bavPennyLess(@RequestBody RequestModel model) {
		return service.bavPennyLess(model);
	}
	
	@PostMapping("/bavpennydrop2")
	public ResponseStructure bavPennydropVTwo(@RequestBody RequestModel model) {
		return service.bavPennydropVTwo(model);
	}
	
	
	@PostMapping("/emailcheck")
	public ResponseStructure emailCheck(@RequestBody RequestModel model) {
		return service.emailCheck(model);
	}
	
	@PostMapping("/itrcompliance")
	public ResponseStructure itrComplianceCheck(@RequestBody RequestModel model) {
		return service.itrComplianceCheck(model);
	}
	
	@PostMapping("/itracknowledgement")
	public ResponseStructure itrAcknowledgement(@RequestBody RequestModel model) {
		return service.itrAcknowledgement(model);
	}
	
	@PostMapping("/mcacompany")
	public ResponseStructure mcaCompanyDetails(@RequestBody RequestModel model) {
		return service.mcaCompanyDetails(model);
	}
	
	@PostMapping("/tan")
	public ResponseStructure taxDeductionAccountNumber(@RequestBody RequestModel model) {
		return service.taxDeductionAccountNumber(model);
	}
	
	@PostMapping("/statelist")
	public ResponseStructure stateListGet(@RequestBody RequestModel model) {
		return service.stateListGet(model);
	}
	
	@PostMapping("/shopestablishmentdetails")
	public ResponseStructure shopEstablishmentDetails(@RequestBody RequestModel model) {
		return service.shopEstablishmentDetails(model);
	}
	
	
	@PostMapping("/ieccheck")
	public ResponseStructure iecCheck(@RequestBody RequestModel model) {
		return service.iecCheck(model);
	}
	
	@PostMapping("/fssaicheck")
	public ResponseStructure fssaiCheck(@RequestBody RequestModel model) {
		return service.fssaiCheck(model);
	}
	
	@PostMapping("/leicheck")
	public ResponseStructure legalEntityIdentifier(@RequestBody RequestModel model) {
		return service.legalEntityIdentifier(model);
	}
	
	@PostMapping("/aadharqrsearch")
	public ResponseStructure aadharQrSearch(@RequestBody RequestModel model) {
		return service.aadharQrSearch(model);
	}
	
	@PostMapping("/epfootpsend")
	public ResponseStructure epfoOtpSend(@RequestBody RequestModel model) {
		return service.epfoOtpSend(model);
	}
	
	@PostMapping("/epfootpverify")
	public ResponseStructure epfoOtpVerify(@RequestBody RequestModel model) {
		return service.epfoOtpVerify(model);
	}
	
	@PostMapping("/epfopassbookdownload")
	public ResponseStructure passbookDownload(@RequestBody RequestModel model) {
		return service.passbookDownload(model);
	}
	
	@PostMapping("/epfokycdetails")
	public ResponseStructure epfoKycDetailsGet(@RequestBody RequestModel model) {
		return service.epfoKycDetailsGet(model);
	}
	
	@PostMapping("/epfowithoutotp")
	public ResponseStructure epfoWithoutOtp(@RequestBody RequestModel model) {
		return service.epfoWithoutOtp(model);
	}
	
	@PostMapping("/ckycsearch")
	public ResponseStructure ckycSearch(@RequestBody RequestModel model) {
		return service.ckycSearch(model);
	}
	
	@PostMapping("/ckycdownload")
	public ResponseStructure ckycDownload(@RequestBody RequestModel model) {
		return service.ckycDownload(model);
	}
	
	@PostMapping("/facematch")
	public ResponseStructure faceMatch(@RequestBody RequestModel model) {
		return service.faceMatch(model);
	}
	
	@PostMapping("/livenesscheck")
	public ResponseStructure livenessCheck(@RequestBody RequestModel model) {
		return service.livenessCheck(model);
	}
	
	@PostMapping("/upiverification")
	public ResponseStructure upiIndex(@RequestBody RequestModel model) {
		return service.upiIndex(model);
	}
	
	@PostMapping("/vehiclechallan")
	public ResponseStructure vehicleChallan(@RequestBody RequestModel model) {
		return service.vehicleChallan(model);
	}
	
	@PostMapping("/ocr")
	public ResponseStructure opticalCharacterRecognition(@RequestBody RequestModel model) {
		return service.opticalCharacterRecognition(model);
	}
	
	@PostMapping("/geolocation")
	public ResponseStructure revereseGeoLocation(@RequestBody RequestModel model) {
		return service.revereseGeoLocation(model);
	}
	
	@PostMapping("/iplookup")
	public ResponseStructure ipAddressLookUp(@RequestBody RequestModel model) {
		return service.ipAddressLookUp(model);
	}
	
	@PostMapping("/mobileoperator")
	public ResponseStructure mobileOperatorCheck(@RequestBody RequestModel model) {
		return service.mobileOperatorCheck(model);
	}
	
	@PostMapping("/itrcreateclient")
	public  ResponseStructure itrCreateClient(@RequestBody RequestModel model) {
		return service.itrCreateClient(model);
	}
	
	@PostMapping("/itrforgetpasssword")
	public  ResponseStructure itrForgetPassword(@RequestBody RequestModel model) {
		return service.itrForgetPassword(model);
	}
	
	@PostMapping("/itrotpsubmit")
	public  ResponseStructure itrOtpSubmit(@RequestBody RequestModel model) {
		return service.itrOtpSubmit(model);
	}
	
	@PostMapping("/itrprofileget")
	public  ResponseStructure itrProfileGet(@RequestBody RequestModel model) {
		return service.itrProfileGet(model);
	}
	
	@PostMapping("/getitrlist")
	public  ResponseStructure getItrList(@RequestBody RequestModel model) {
		return service.getItrList(model);
	}
	
	@PostMapping("/getsingleitrdetails")
	public  ResponseStructure getSingleItrDetails(@RequestBody RequestModel model) {
		return service.getSingleItrDetails(model);
	}
	
	@PostMapping("/get26aslist")
	public  ResponseStructure get26AsList(@RequestBody RequestModel model) {
		return service.get26AsList(model);
	}
	
	@PostMapping("/getsingle26aslist")
	public  ResponseStructure getSingle26AsList(@RequestBody RequestModel model) {
		return service.getSingle26AsList(model);
	}
	
	@PostMapping("/courtcasestatus")
	public  ResponseStructure courtCaseStatus(@RequestBody RequestModel model) {
		return service.courtCaseStatus(model);
	}
	
	@PostMapping("/tanlookup")
	public  ResponseStructure companyTanLookup(@RequestBody RequestModel model) {
		return service.companyTanLookup(model);
	}
	
	@PostMapping("/fuelprice")
	public  ResponseStructure fuelPriceFetch(@RequestBody RequestModel model) {
		return service.fuelPriceFetch(model);
	}
	
	@PostMapping("/pantogst")
	public  ResponseStructure panToGst(@RequestBody RequestModel model) {
		return service.panToGst(model);
	}
	
	@PostMapping("/stockprice")
	public  ResponseStructure stockPriceVerify(@RequestBody RequestModel model) {
		return service.stockPriceVerify(model);
	}
	
	@PostMapping("/rto")
	public  ResponseStructure regionalTransport(@RequestBody RequestModel model) {
		return service.regionalTransport(model);
	}
	
	@PostMapping("/mobilenumbercase")
	public  ResponseStructure mobileNumberCase(@RequestBody RequestModel model) {
		return service.mobileNumberCase(model);
	}
	
	
	@PostMapping("/pandetailedinfo")
	public  ResponseStructure panDetailedInfo(@RequestBody RequestModel model) {
		return service.panDetailedInfo(model);
	}
	
	@PostMapping("/pancomprehensive")
	public  ResponseStructure panComprehensive(@RequestBody RequestModel model) {
		return service.panComprehensive(model);
	}
	
	@PostMapping("/companynamecin")
	public  ResponseStructure companyNameToCin(@RequestBody RequestModel model) {
		return service.companyNameToCin(model);
	}
	
	@PostMapping("/telecomsendotp")
	public  ResponseStructure telecomSendOtp(@RequestBody RequestModel model) {
		return service.telecomSendOtp(model);
	}
	
	@PostMapping("/telecomgetdetails")
	public  ResponseStructure telecomGetDetails(@RequestBody RequestModel model) {
		return service.telecomGetDetails(model);
	}
	
	@PostMapping("/digilockerinitiatesession")
	public  ResponseStructure digiLockerInitiateSession(@RequestBody RequestModel model) {
		return service.digiLockerInitiateSession(model);
	}
	
	@PostMapping("/digilockeraccesstokengeneration")
	public  ResponseStructure digiLockerAccessTokenGeneration(@RequestBody RequestModel model) {
		return service.digiLockerAccessTokenGeneration(model);
	}
	
	@PostMapping("/digilockergetissuedfiles")
	public  ResponseStructure digiLockerGetIssuedFiles(@RequestBody RequestModel model) {
		return service.digiLockerGetIssuedFiles(model);
	}
	
	@PostMapping("/digilockerdownloaddocinpdf")
	public  ResponseStructure digiLockerDownloadDocInPdf(@RequestBody RequestModel model) {
		return service.digiLockerDownloadDocInPdf(model);
	}
	
	@PostMapping("/digilockerdownloaddocinxml")
	public  ResponseStructure digiLockerDownloadDocInXml(@RequestBody RequestModel model) {
		return service.digiLockerDownloadDocInXml(model);
	}
	
	@PostMapping("/digilockereaadhardocinxml")
	public  ResponseStructure digiLockerEaadhaarDocInXml(@RequestBody RequestModel model) {
		return service.digiLockerEaadhaarDocInXml(model);
	}
	
	@PostMapping("/demosprintv")
	public ResponseStructure demoSprintVController(@RequestBody RequestModel model) {
		return service.demoSprintVController(model);
	}
}
