package com.bp.middleware.requestandresponse;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.CommonRequestDto;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/ekyc")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class VerificationController {
	
	@Autowired
	private VerificationService service;
	
	
	@GetMapping("/entity-onlyonce")
	public ResponseStructure reqByOneEntity() {
		return service.reqByOneEntity();
	}
	
	
	@PostMapping("/byentity-verification")
	public ResponseStructure byEntityAndVerificationType(@RequestBody RequestModel model) {
		return service.byEntityAndVerificationType(model);
	}
	
	
	
	@GetMapping("/vendor-onlyonce")
	public ResponseStructure reqByOneVendor() {
		return service.reqByOneVendor();
	}
	
	
	@PostMapping("/byVendor-verification")
	public ResponseStructure byVendorAndVerificationType(@RequestBody RequestModel model) {
		return service.byVendorAndVerificationType(model);
	}
	
	
	
	
	
	@PostMapping("/panverification")//Done
	public ResponseStructure panVerification(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return service.verification(dto,servletRequest);
		
	}
	@PostMapping("/gstverification")
	public ResponseStructure gstVerification(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return service.gstVerification(dto,servletRequest);
		
	}
	@PostMapping("/imagepanverification")
	public ResponseStructure panImageVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest ) {
		return service.panImageVerification(model,servletRequest);
		
	}
	@PostMapping("/imagegstverification")
	public ResponseStructure gstImageVerification(@RequestBody RequestModel model,HttpServletRequest servletRequest ) {
		return service.gstImageVerification(model,servletRequest);
		
	}
	@PostMapping("/aadharxmlverification")
	public ResponseStructure aadharXmlVerification(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return service.aadharXmlVerification(dto,servletRequest);
		
	}
	@PostMapping("/aadharotpsubmit")
	public ResponseStructure aadharotpVerification(@RequestBody RequestModel dto,HttpServletRequest servletRequest) {
		return service.aadhaarOtpVerification(dto,servletRequest);
		
	}
	
	@PostMapping("/esigndemo")
	public ResponseStructure eSignDemo(@RequestParam("content") MultipartFile content,@RequestParam("docket_title") String docket_title,
	@RequestParam("reference_doc_id") String reference_doc_id,@RequestParam("content_type") String content_type,@RequestParam("signature_sequence") String signature_sequence,
	@RequestParam("document_to_be_signed") String document_to_be_signed,@RequestParam("appearance") String appearance,@RequestParam("signer_ref_id") String signer_ref_id,
	@RequestParam("signer_email") String signer_email,@RequestParam("signer_name") String signer_name,@RequestParam("sequence") int sequence,
	@RequestParam("page_number") String page_number,@RequestParam("esign_type") String esign_type,@RequestParam("signer_mobile") String signer_mobile,
	@RequestParam("signature_type") String signature_type,@RequestParam("name_as_per_aadhaar") String name_as_per_aadhaar,@RequestParam("userId") int userId) {

	CommonRequestDto dto=new CommonRequestDto();

	dto.setContent(content);
	dto.setDocket_title(docket_title);
	dto.setReference_doc_id(reference_doc_id);
	dto.setContent_type(content_type);
	dto.setSignature_sequence(signature_sequence);
	dto.setDocument_to_be_signed(document_to_be_signed);
	dto.setAppearance(appearance);
	dto.setSigner_ref_id(signer_ref_id);
	dto.setSigner_email(signer_email);
	dto.setSigner_name(signer_name);
	dto.setSequence(sequence);
	dto.setPage_number(page_number);
	dto.setEsign_type(esign_type);
	dto.setSigner_mobile(signer_mobile);
	dto.setSignature_type(signature_type);
	dto.setName_as_per_aadhaar(name_as_per_aadhaar);
	dto.setUserId(userId);

	return service.eSignDemo(dto);

	}
	
	@PostMapping("/esign")
	public ResponseStructure esign(@RequestBody RequestModel model) {
		return service.esignValidate(model);
	}
	

	@PostMapping("/cinverification")
	public ResponseStructure cinVerification(@RequestBody RequestModel dto ,HttpServletRequest servletRequest) {
		return service.cinVerification(dto,servletRequest);
		
	}
	
	@PostMapping("/dinverification")
	public ResponseStructure dinVerification(@RequestBody RequestModel dto ,HttpServletRequest servletRequest) {
		return service.dinVerification(dto,servletRequest);
		
	}
	
	@PostMapping("/msmeverification")
	public ResponseStructure msmeVerification(@RequestBody RequestModel dto ,HttpServletRequest servletRequest) {
		return service.msmeVerification(dto,servletRequest);
		
	}
	
	@PostMapping("/rcverification")
	public ResponseStructure rcVerification(@RequestBody RequestModel dto ,HttpServletRequest servletRequest) {
		return service.rcVerification(dto,servletRequest);
		
	}
	
	@PostMapping("/drivingliceneceid")
	public ResponseStructure drivingLicenceId(@RequestBody RequestModel dto ,HttpServletRequest servletRequest) {
		return service.drivingLicenceId(dto,servletRequest);
		
	}
	
	@PostMapping("/passportid")
	public ResponseStructure passportId(@RequestBody RequestModel dto ,HttpServletRequest servletRequest) {
		return service.passportId(dto,servletRequest);
		
	}
	
	@PostMapping("/passportimage")
	public ResponseStructure passportImage(@RequestBody RequestModel dto ,HttpServletRequest servletRequest) {
		return service.passportImage(dto,servletRequest);
		
	}
	
	@PostMapping("/drivinglicenceimage")
	public ResponseStructure drivingLicenceImage(@RequestBody RequestModel dto ,HttpServletRequest servletRequest) {
		return service.drivingLicenceImage(dto,servletRequest);
		
	}
	
	///////

	@GetMapping("/getbyid/{requestId}")
	public ResponseStructure findById(@PathVariable("requestId")int requestId) {
		return service.findById(requestId);
	}
	
	@PostMapping("/demo")
	public ResponseStructure demoController(@RequestBody RequestModel model) {
		return service.demoController(model);
	}
	
	
	@PostMapping("/demoGst")
	public ResponseStructure demoGstController(@RequestBody RequestModel model) {
		return service.demoGstController(model);
	}
	
	@PostMapping("/demomultipart")
	public ResponseStructure demoImageController(@RequestParam("image") MultipartFile image,@RequestParam("userId")int userId) {
		return service.demoImageController(image,userId);
	}
	
	@PostMapping("/demomultipartfortwoimages")
	public ResponseStructure demoImageControllerForTwoImages(@RequestParam("front") MultipartFile front,@RequestParam("rear") MultipartFile rear,@RequestParam("userId")int userId) {
		return service.demoImageControllerForTwoImages(front,rear,userId);
	}
	
	@PostMapping("/otpdemo")
	public ResponseStructure demoOtpController(@RequestBody RequestModel model) {
		return service.demoOtpController(model);
	}
	
//	@PostMapping("/jwedemo")
//	public ResponseStructure jweDemoController(@RequestBody RequestModel model) {
//		return service.jweDemoController(model);
//	}
	///COUNT
	
	@GetMapping("/totalcount")//req table (Merchant)
	public ResponseStructure totalHitCount() {
		return service.totalHitCount();
	}
	
	@GetMapping("/hitcountbyentity/{userId}")
	public ResponseStructure hitCountByEntity(@PathVariable("userId")int userId) {
		return service.hitCountByEntity(userId);
	}
	
	@GetMapping("/hitcountfortoday")
	public ResponseStructure hitForToday() {
		return service.hitForToday();
	}
	
	@GetMapping("/hitcountformonth/{month}")
	public ResponseStructure hitForThisMonth(@PathVariable("month")int month) {
		return service.hitForThisMonth(month);
	}
	
	@GetMapping("/hitcountforweek")
	public ResponseStructure hitForThisWeek(@RequestParam("date1")String date1,@RequestParam("date2")String date2) {
		return service.hitForThisWeek(date1,date2);
	}
	
	//
	@GetMapping("/totalresponsecount")//resp table (Vendor)
	public ResponseStructure totalResponseHitCount() {
		return service.totalResponseHitCount();
	}
	@GetMapping("/responsehitCountByEntity/{userId}")
	public ResponseStructure hitResponseCountByEntity(@PathVariable("userId")int userId) {
		return service.responsehitCountByEntity(userId);
	}
	@GetMapping("/responsehitcountfortoday")
	public ResponseStructure reponseHitForToday() {
		return service.responseHitCountForToday();
	}
	@GetMapping("/responsehitcountformonth/{month}")
	public ResponseStructure responseHitForThisMonth(@PathVariable("month")int month) {
		return service.hitResponseForThisMonth(month);
	}
	@GetMapping("/responsehitcountforweek")
	public ResponseStructure responseHitForThisWeek(@RequestParam("date1")String date1,@RequestParam("date2")String date2) {
		return service.responseHitForThisWeek(date1,date2);
	}
	
	@GetMapping("/responsehitCountByrequest/{requestId}")
	public ResponseStructure hitResponseCountByRequest(@PathVariable("requestId")int requestId) {
		return service.hitResponseCountByRequest(requestId);
	}
	
	@GetMapping("/successresponsecount")
	public ResponseStructure successAndFailureResponseCount() {
		return service.successResponseCount();
	}
	
	@GetMapping("/databasesuccessresponse")
	public ResponseStructure databaseSuccessResponse() {
		return service.databaseSuccessResponse();
	}
	
	
	//ADMIN DASHBOARD ALL IN ONE API PENDING 2
	@GetMapping("/adminallinonedashboard")
	public ResponseStructure adminAllInOneDashboard() {
		return service.adminAllInOneDashboard();
	}
	
	
	//MERCHANT DASHBOARD ALL IN ONE API PENDING 2
	@GetMapping("/merchantallinonedashboard")
	public ResponseStructure merchantAllInOneDashboard(@RequestBody RequestModel model) {
		return service.merchantAllInOneDashboard(model);
	}
	
	//To Manually change the status in response table
	@PutMapping("/tomanuallychangestatus")
	public ResponseStructure toManuallyChangeStatus(@RequestBody RequestModel model) {
		return service.toManuallyChangeStatus(model);
	}
	
	
	@GetMapping("/viewrequestbynumberofdays/{noOfDays}")
	public ResponseStructure viewMerchantRequestByNumberOfDays(@PathVariable("noOfDays") int noOfDays){
		return service.viewMerchantRequestByNumberOfDays(noOfDays);
	}
	
	@GetMapping("/viewvendorbynumberofdays/{noOfDays}")
	public ResponseStructure viewVendorRequestByNumberOfDays(@PathVariable("noOfDays") int noOfDays){
		return service.viewVendorRequestByNumberOfDays(noOfDays);
	}
	
	
	@GetMapping("/viewall/reqresp-replica")
	public ResponseStructure viewAllRequestResponseReplica(){
		return service.viewAllRequestResponseReplica();
	}
	
	@GetMapping("/viewbyid/reqresp-replica/{replicaId}")
	public ResponseStructure viewByIdRequestResponseReplica(@PathVariable("replicaId") int replicaId){
		return service.viewByIdRequestResponseReplica(replicaId);
	}
	
	
	@GetMapping("/viewbyuser/reqresp-replica/{userId}")
	public ResponseStructure viewByEntityRequestResponseReplicaCombined(@PathVariable("userId")int userId){
		return service.viewByEntityRequestResponseReplicaCombined(userId);
	}
	
}
