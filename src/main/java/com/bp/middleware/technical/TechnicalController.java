package com.bp.middleware.technical;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
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

import com.bp.middleware.jwt.JWTTokenProvider;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.RecieptCheck;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/technical")//NOT NECESSARY
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class TechnicalController {

	@Autowired
	private TechnicalService service;
	
	@Autowired
	private ServletContext context;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	@Autowired
	private TechnicalRepository technicalRepository;
	@Autowired
	private RequestRepository requestRepository;
	@Autowired
	private PrepaidRepository prepaidRepository;
	
	@Autowired
	private ImageUploader imgService;
	
	
	@PostMapping("/schedulerTrigger1/{id}")
	public ResponseStructure schedulerTrigger1(@PathVariable("id")int id) {
		return service.schedulerTrigger1(id);
	}
	
	
	@PostMapping("/check")
	public ResponseStructure justCheck(@RequestBody RequestModel model) {
		return service.justCheck(model);
	}
	
	@GetMapping("/listCheck/{num}")
	public ResponseStructure listCheck(@PathVariable("num")int num) {
		return service.listCheck(num);
	}
	
	@PostMapping("/addtech")
	public ResponseStructure addTechnicalDetails(@RequestBody RequestModel model) {
		return service.addTechnicalDetails(model);
	}
	
	
	
	@PostMapping("/byteimage")
	public ResponseStructure byteImage(@RequestParam("image") MultipartFile image) {
		return service.byteImage(image);
	}
	
	
	@GetMapping("/getbyid/{techId}")
	public ResponseStructure viewByTechnicalId(@PathVariable("techId")int techId) {
		return service.viewByTechnicalId(techId);
	}
	
	@GetMapping("/getbyall")
	public ResponseStructure viewAllTechnical() {
		return service.viewAllTechnical();
	}
	
	
	@GetMapping("/paramserviceCheck")
	public ResponseStructure paramArrayCheck(@RequestParam("arr")String[] arr,@RequestParam("arr1")String[] arr1,
			@RequestParam("arr2")String[] arr2) {
		
		int count1 =0;
		int count2 =0;
		int count3 =0;
		
		int arr1Size=arr.length;
		
		for(int i=0;i<arr1Size;i++) {
		
		String s=arr[i];
		String s2=arr1[i];
		String s3=arr2[i];
			
		System.out.println("arr1 : "+i+"    "+s);
		System.out.println("arr2 : "+i+"    "+s2);
		System.out.println("arr2 : "+i+"    "+s3);
		
		}
		
		return null;
	}
	
	@PutMapping("/update")
	public ResponseStructure updateTechnical(@RequestBody RequestModel model) {
		return service.updateTechnical(model);
	}
	
//	@PostMapping("/extract")
//    public String extractPanNumber(@RequestParam("file") MultipartFile file) throws IOException {
//        // Create a temporary file to store the uploaded image
//        File imageFile = File.createTempFile("pan_", ".png");
//        file.transferTo(imageFile);
//
//        // Initialize Tesseract OCR
//        ITesseract tesseract = new Tesseract();
//        tesseract.setDatapath("path/to/tesseract/data"); // Set the path to Tesseract's data directory
//
//        // Perform OCR on the image
//        String extractedText = tesseract.doOCR(imageFile);
//
//        // Parse the extracted text to find the PAN number
//        String panNumber = extractPanFromText(extractedText);
//
//        return panNumber;
//    }
//
//    private String extractPanFromText(String text) {
//        // Implement logic to extract the PAN number from the OCR result
//        // This may involve using regular expressions or custom logic based on the PAN card format
//        // Return the extracted PAN number as a string
//        return "Extracted PAN Number";
//    }
//	
	
//	@GetMapping("/invoicetesting")
//	public ResponseStructure invoiceTesting(  ) {
//		return service.invoiceTesting();
//	}
	
	
//	@SuppressWarnings("static-access")
//	@GetMapping("/get")
//	public ResponseStructure getPdf(){
//		ResponseStructure structure =new ResponseStructure();
//		try {
//			TechnicalModel model=technicalRepository.findByTechnicalId(8);
//			
//			String receipt =null;
//		    Path con=	Paths.get(context.getRealPath("/WEB-INF/"));
//			
//			receipt = RecieptCheck.receiptGenerator(con,context);
//			structure.setData(receipt);
////			model.setApiLink(receipt);
////			technicalRepository.save(model);
//			 
//		}catch(Exception e) {
//			e.printStackTrace();
//			structure.setFlag(3);
//			structure.setErrorDiscription("Technical Error");
//			
//		}
//		return structure;
//	}
	
	
	@GetMapping("/viewinvoice")
	public ResponseEntity<Resource> viewInvoicePdf(RequestModel model, HttpServletRequest request) throws IOException {
		return service.getViewInvoicePdf(model, request);
	}
	
	@GetMapping("/ipaddress")
	public ResponseStructure ipAddress() {
		return service.ipAddress();
	}

	@PostMapping("/sprintOk")
	public ResponseStructure justCheckSprintOkHttp(@RequestBody RequestModel model) {
		return service.justCheckSprintOkHttp(model);
	}
		
	@PostMapping("/sprintNet")
	public ResponseStructure justCheckSprintHttpNet(@RequestBody RequestModel model) {
		return service.justCheckSprintHttpNet(model);
	}
	
	@PostMapping("/sprintOur")
	public ResponseStructure justCheckSprintOurMethod(@RequestBody RequestModel model) {
		return service.justCheckSprintOurMethod(model);
	}
	
	@PostMapping("/sprintUNIREST")
	public ResponseStructure justCheckSprintUnirest(@RequestBody RequestModel model) {
		return service.justCheckSprintUnirest(model);
	}
	
	@GetMapping("/javanethttpsv")
	public ResponseStructure justCheckSprintVJavaNetHttp() {
		return service.justCheckSprintVJavaNetHttp();
	}
	
	
	@GetMapping("/vendorbyname")
	public ResponseStructure vendorByName(@RequestBody RequestModel model) {
		return service.vendorByName(model);
	}
	
	
	@GetMapping("/clientip")
	public ResponseStructure clientIpAddress(HttpServletRequest request,HttpServletResponse response) {
		return service.clientIpAddress(request,response);
	}
	
	@GetMapping("/agreement")
	public ResponseStructure agreementCheck() {
		return service.agreementCheck();
	}
	
	@GetMapping("/base64topdf")
	public ResponseStructure base64ToPdf(@RequestBody RequestModel model) {
		return service.base64ToPdf(model);
	}
	
	@GetMapping("/applicationid")
	public ResponseStructure applicationId(@RequestBody RequestModel model) {
		return service.applicationId(model);
	}
	
	
	@PostMapping("/aadhaarOcr")
	public ResponseStructure aadhaarOcr(@RequestParam("file")File file,HttpServletRequest servletRequest) {
		return service.aadhaarOcr(file,servletRequest);
	}
	
	@PostMapping("/uploadImageOcr")
	public ResponseStructure uploadImageOcr(@RequestParam("file")MultipartFile file) {
		return imgService.uploadImage(file);
	}
	
	@PostMapping("/unirest")
	public ResponseStructure OcrUniRest(@RequestParam("file")MultipartFile file) {
		return imgService.OcrUniRest(file);
	}
	
	@PostMapping("/invoice")
	public ResponseStructure invoiceCheck() {
		return service.invoiceCheck();
	}
	
	@PostMapping("/conve-invoice")
	public ResponseStructure conveInvoiceCheck() {
		return service.conveInvoiceCheck();
	}
	
	
	@PostMapping("/thymeleaf")
	public ResponseStructure thymeLeaf() {
		return service.thymeLeaf();
	}
	
	@PostMapping("/thymeleafTamil")
	public ResponseStructure thymeLeafTamil() {
		return service.thymeLeafTamil();
	}
	
	@PostMapping("/thymeleafTamilItextCheck")
	public ResponseStructure thymeLeafTamilItextCheck() {
		return service.thymeLeafTamilItextCheck();
	}
	
	@GetMapping("/jsonobjreturn")
	public ResponseStructure jsonObjReturn() {
		return service.jsonObjReturn();
	}
	
	
	@GetMapping("/exceptioncheck")
	public ResponseStructure exceptionCheck() {
		return service.exceptionCheck();
	}
	
	
	@GetMapping("/tokenExpire")
	public ResponseStructure tokenExpireFounder(@RequestBody RequestModel model, HttpServletRequest request) throws IOException {


		ResponseStructure structure = new ResponseStructure();
		
		try {
			
			System.err.println("S K : "+model.getSecretKey());
			
			Claims claims = Jwts.parser().setSigningKey(model.getSecretKey()).parseClaimsJwt(model.getAccessToken()).getBody();
			
			Date expireDate = claims.getExpiration();
			
			structure.setData(expireDate);
			structure.setMessage("Expiration Date is");
			
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage("Exception");
		}
		
		return structure;
	}
	
	
	
	@GetMapping("/amountTowords")
	public ResponseStructure amountToWords(@RequestParam("amount") int amount) {
		return service.amountToWords(amount);
	}
	
	@GetMapping("/firstDayOfMonth")
	public ResponseStructure firstDayOfMonth() {
		
		ResponseStructure st = new ResponseStructure();
		
		LocalDate minusDays = LocalDate.now().minusDays(LocalDate.now().getDayOfMonth()-1);
		
		st.setData(minusDays);
		return st;
	}
	
	
	@GetMapping("/splitter")
	public ResponseStructure splitString(@RequestBody RequestModel model) {
		
		ResponseStructure st = new ResponseStructure();
		
		String split = model.getAadhaarNumber().substring(8);
		
		st.setData(split);
		
		return st;
	}
	
	
	
	@GetMapping("/usedServices")
	public ResponseStructure usedServices() {
		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate =  "2024-06-01"+" 00:00:00.0000000";
			String endDate = "2024-06-22"+" 23:59:59.9999999";
			
			List<Request> hitLogs = requestRepository.getBetween(5,startDate,endDate);
			
			String usedServices = "";
			
			Set<VendorVerificationModel> set = new HashSet<>();
			
			for (Request r : hitLogs) {
				
				set.add(r.getVerificationModel());
			}
			
			int count =set.size();
			
			for (VendorVerificationModel vendorVerificationModel : set) {
				
				count--;
				
				if(usedServices.equalsIgnoreCase("") ) {
					
					usedServices+=vendorVerificationModel.getVerificationDocument();
				}else {
					
					usedServices+=","+vendorVerificationModel.getVerificationDocument();
				}
			}
			
			structure.setData(usedServices);
			structure.setFlag(1);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage("ERROR");	
		}

		return structure;
	}
	
	
	
	@GetMapping("/queryCheck")
	public ResponseStructure queryCheck() {
		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate =  "2024-06-01"+" 00:00:00.0000000";
			String endDate = "2024-06-22"+" 23:59:59.9999999";
			
			//double price = Math.ceil(requestRepository.getSummedAmount(5, startDate, endDate));
			
			List<PrepaidPayment> otherEntriesThisMonth = prepaidRepository.getByPrepaidIdAndMonth("Success","JUNE",5,11);
			
			structure.setData(otherEntriesThisMonth);
			structure.setFlag(1);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage("ERROR");	
		}

		return structure;
	}
	
	
	
	@GetMapping("/mailtempcheck/{id}")
	public ResponseStructure mailTemplateCheck(@PathVariable("id") int id) {
		return service.mailTemplateCheck(id);
	}
	
	@GetMapping("/mailtempcheckpostpaid/{id}")
	public ResponseStructure mailTemplateCheckPostpaid(@PathVariable("id") int id) {
		return service.mailTemplateCheckPostpaid(id);
	}
	
	
	
	@GetMapping("/jsonview/{id}")
	public ResponseStructure jsonView(@PathVariable("id") int id) {
		return service.jsonView(id);
	}
	
	
	
	@GetMapping("getpublic-ipaddress")
	public ResponseStructure getPublicIpAddress() {
		return service.getPublicIpAddress();
	}
	
	
	@GetMapping("get-gps")
	public ResponseStructure getGps() {
		return service.getGps();
	}
	
	
	@GetMapping("/differenceBetweenTwoDates")
	public ResponseStructure diffTwoDates() {
		return service.diffTwoDates();
	}
	
	
	@GetMapping("/specialcharactercheck")
	public ResponseStructure specialCharacterCheck(@RequestBody RequestModel model) {
		return service.specialCharacterCheck(model);
	}
	
	@GetMapping("/postpaid-flagchange/{id}")
	public ResponseStructure postpaidFlagChange(@PathVariable("id")int id) {
		return service.postpaidFlagChange(id);
	}
	
	
}
