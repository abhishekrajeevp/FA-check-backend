package com.bp.middleware.technical;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.bp.middleware.bond.MerchantBond;
import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.jwt.JWTTokenProvider;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
//import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.scheduled.ScheduledServices;
import com.bp.middleware.signmerchant.MerchantModel;
import com.bp.middleware.signmerchant.MerchantRepository;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.transaction.TransactionRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AmountToWords;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.DateUtil;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.InvoiceGenerate;
import com.bp.middleware.util.JsonNodeConverter;
import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.client.json.webtoken.JsonWebToken.Payload;
import com.google.api.client.util.DateTime;
import com.google.gson.JsonObject;
import com.itextpdf.text.log.SysoCounter;
import com.itextpdf.text.pdf.BaseFont;
import com.mashape.unirest.http.Unirest;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.persistence.Entity;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
//import okhttp3.Request;
import okhttp3.RequestBody;
//import okhttp3.Response;

@Service
public class TechnicalServiceImplementation implements TechnicalService {

	@Autowired
	private TechnicalRepository techRepository;
	@Autowired
	private VendorPriceRepository vendorPriceRepository;
	@Autowired
	private VendorRepository vendorRepository;
	@Autowired
	private VendorVerificationRepository vendorVerificationRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ServletContext context;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	JWTTokenProvider tokenProvider;
	@Autowired
	private ResponseRepository respRepository;
	@Autowired
	private MerchantRepository merchantRepository;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private PostpaidRepository postpaidRepository;
	@Autowired
	private PrepaidRepository prepaidRepository;
	@Autowired
	private RequestRepository requestRepository;;
	@Autowired
	EmailService emailService;
	@Autowired
	InvoiceGenerate invoiceGenerate;
	@Autowired
	ScheduledServices scheduledServices;
	@Autowired
	FileUtils fu;

	private String attach = "attachment; filename=\'";

	@Override
	public ResponseStructure addTechnicalDetails(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			EntityModel entityModel = userRepository.findByUserId(model.getUserId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			VendorVerificationModel verificationModel = vendorVerificationRepository
					.findByVendorVerificationId(model.getVendorVerificationId());

			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			TechnicalModel techModel = new TechnicalModel();

			techModel.setApiLink(vendorPriceModel.getApiLink());
			techModel.setStatus(true);
			techModel.setIdPrice(model.getIdPrice());
			techModel.setImagePrice(model.getImagePrice());

//			techModel.setEntityModel(entityModel);
//			techModel.setVendorVerificationModel(verificationModel);
//			techModel.setVendorModel(vendorModel);

			techRepository.save(techModel);

			structure.setData(techModel);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewByTechnicalId(int techId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<TechnicalModel> optional = techRepository.findById(techId);
			if (optional.isPresent()) {
				TechnicalModel technicalModel = optional.get();

				structure.setData(technicalModel);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewAllTechnical() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<TechnicalModel> list = techRepository.findAll();
			if (!list.isEmpty()) {

				List<TechnicalModel> activeList = new ArrayList<>();
				for (TechnicalModel technicalModel : list) {
					if (technicalModel.isStatus()) {
						activeList.add(technicalModel);
					}
				}

				structure.setData(activeList);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
			} else {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure updateTechnical(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
//			Optional<TechnicalModel> optional = techRepository.findById(model.getTechnicalId());
//			if (optional.isPresent()) {
//
//				TechnicalModel technicalModel = optional.get();
//
//				VendorModel vendorModel = new VendorModel();
//				if (model.getVendorId() != 0) {
//					Optional<VendorModel> vendor = vendorRepository.findById(model.getVendorId());
//					if (vendor.isPresent()) {
//						vendorModel = vendor.get();
//					}
//					technicalModel.setVendorModel(vendorModel);
//				}
//
//				VendorVerificationModel verificationModel = new VendorVerificationModel();
//				if (model.getVendorVerificationId() != 0) {
//					verificationModel = vendorVerificationRepository
//							.findByVendorVerificationId(model.getVendorVerificationId());
//					technicalModel.setVendorVerificationModel(verificationModel);
//				}
//
//				if (vendorModel != null && verificationModel != null) {
//					VendorPriceModel vendorPriceModel = vendorPriceRepository
//							.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);
//					technicalModel.setApiLink(vendorPriceModel.getApiLink());
//				}
//
//				technicalModel.setIdPrice(model.getIdPrice());
//				technicalModel.setImagePrice(model.getImagePrice());
//
//				techRepository.save(technicalModel);
//
//				structure.setData(technicalModel);
//				structure.setMessage(AppConstants.SUCCESS);
//				structure.setFlag(1);
//				structure.setStatusCode(HttpStatus.OK.value());
//			} else {
//				structure.setData(null);
//				structure.setMessage(AppConstants.NO_DATA_FOUND);
//				structure.setFlag(2);
//				structure.setStatusCode(HttpStatus.OK.value());
//			}
//
//		
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	// @Scheduled(fixedRate = 10000)
	public void clearDataFromTable() {
		LocalDate currentDate = LocalDate.now();
		LocalDate targetDate = currentDate.minusMonths(6);

		int targetYear = targetDate.getYear();
		int targetMonth = targetDate.getMonthValue();
		int targetDay = targetDate.getDayOfMonth();

		deleteDataBeforeSixMonths(targetYear, targetMonth, targetDay);

	}

	private void deleteDataBeforeSixMonths(int targetYear, int targetMonth, int targetDay) {

		System.out.println("Target Year : " + targetYear);
		System.out.println("Target Month : " + targetMonth);
		System.out.println("Target Day : " + targetDay);

		List<TechnicalModel> list = techRepository.findAll();

		for (TechnicalModel technicalModel : list) {
			LocalDate localDateConverter = DateUtil.convertDateToLocalDateViaSql(technicalModel.getReqDate());

			int dataYear = localDateConverter.getYear();
			int dataMonth = localDateConverter.getMonthValue();
			int dataDay = localDateConverter.getDayOfMonth();

			System.out.println("Data Year : " + dataYear);
			System.out.println("Data Month : " + dataMonth);
			System.out.println("Data Day : " + dataDay);

			if (targetMonth == dataMonth) {
				techRepository.deleteById(technicalModel.getTechnicalId());
			}
		}

	}

	// @Scheduled(fixedRate = 1000)
	public void uniqueKeyWithName() {

		String name = "REFRIDGERATOR";
		int count = 1;

		int trimSize = 3;
		String trimmedString = null;

		if (trimSize <= name.length()) {

			trimmedString = name.substring(0, trimSize);
		}

		String counterPart = String.format("%04d", count++);

		String uniqueKey = trimmedString + counterPart;

		System.err.println("UNIQUE : " + uniqueKey);
	}

	@Override
	public ResponseStructure invoiceTesting() {
		ResponseStructure structure = new ResponseStructure();
		try {

			Path currentWorkingDir = Paths.get(context.getRealPath("/WEB-INF/"));

			// String userInvoicefileName
			// =InvoiceGeneratePdf.writeDataToPDF(currentWorkingDir);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setFlag(4);
		}
		return structure;
	}

//	public ResponseEntity<Resource> viewInvoicePdf(RequestModel model, HttpServletRequest request) throws IOException {
//
//		Optional<DueAmountMasterModel> model = dueRepository.findById(dueAmountId);
//		if (model.isPresent()) {
//			DueAmountMasterModel dm = model.get();
//			return getViewInvoicePdf(dm, request);
//
//		} else {
//			return null;
//		}
//
//	}

//	@Override
//	public ResponseEntity<Resource> getViewInvoicePdf(RequestModel teModel, HttpServletRequest request) {
//		TechnicalModel model = techRepository.findByTechnicalId(8);
//		if (model.getApiLink() != null) {
//
//			final Resource resource = resourceLoader
//					.getResource("/WEB-INF/invoice/" + model.getApiLink());
//			String contentType = null;
//			try {
//
//				contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
//
//			} catch (Exception e) {
////				logger.info("Could not determine file type!");
//				return null;
//			}
//			if (contentType == null && model.getApiLink().equals("Image")) {
//				contentType = "application/octet-stream";
//				return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
//						.header(HttpHeaders.CONTENT_DISPOSITION, attach + resource.getFilename() + "\"")
//						.body(resource);
//			} else if (contentType == null && model.getApiLink().equals("pdf")) {
//				contentType = "application/pdf";
//				return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
//						.header(HttpHeaders.CONTENT_DISPOSITION, attach + resource.getFilename() + "\'")
//						.body(resource);
//
//			} else if (contentType != null) {
//				return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
//						.header(HttpHeaders.CONTENT_DISPOSITION, attach + resource.getFilename() + "\"")
//						.body(resource);
//			} else {
//				return null;
//			}
//
//		} else {
//			return null;
//		}
//	}

	@Override
	public ResponseStructure ipAddress() {
		ResponseStructure structure = new ResponseStructure();
		try {
			TechnicalModel technicalModel = techRepository.findByTechnicalId(8);

			InetAddress ipAddressLocalHost = InetAddress.getLocalHost();
			String hostAddress = ipAddressLocalHost.getHostAddress();

			InetAddress loopBack = InetAddress.getLoopbackAddress();

			System.out.println("IP : " + ipAddressLocalHost);
			System.out.println("Host Address : " + hostAddress);
			System.out.println("Loop Back : " + loopBack);

			technicalModel.setApiLink(hostAddress);

			techRepository.save(technicalModel);

			Map<String, Object> map = new HashMap<>();
			map.put("IP", ipAddressLocalHost);
			map.put("Loop back", loopBack);
			map.put("Host Address", hostAddress);

			structure.setData(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return structure;
	}

	@Override
	public ResponseStructure justCheckSprintOkHttp(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			JSONObject object = new JSONObject(model);

			JSONObject data = object.getJSONObject("data");

			System.err.println("Data : " + data);

//			String jwt = jwtHs256Token();
//
//			OkHttpClient client = new OkHttpClient();
//
//			MediaType mediaType = MediaType.parse("application/json");
//			RequestBody body = RequestBody.create(mediaType,"{\"id_number\":\"TN1220190010000\",\"dob\":\"28-03-2001\"}");
//			Request request = new Request.Builder()
//					.url("https://uat.paysprint.in/sprintverify-uat/api/v1/verification/drivinglicense_verify").post(body)
//					.addHeader("accept", "application/json").addHeader("Token", jwt)
//					.addHeader("authorisedkey", "TVRJek5EVTJOelUwTnpKRFQxSlFNREF3TURFPQ==")
//					.addHeader("content-type", "application/json").build();
//
//			Response response = client.newCall(request).execute();
//
//			System.err.println("RespB : " + response);
//
//			JSONObject responseA = new JSONObject(response);
//
//			System.err.println("RespA : " + responseA);
//
//			structure.setData(response);
//			structure.setMessage(AppConstants.SUCCESS);
//			structure.setFlag(1);
//			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseEntity<Resource> getViewInvoicePdf(RequestModel model, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	public String jwtHs256Token() {
		try {
			String secretKey = "UTA5U1VEQXdNREF4VFZSSmVrNUVWVEpPZWxVd1RuYzlQUT09";

			JWSSigner signer = new MACSigner(secretKey);
			// JWSHeader header=new JWSHeader.Builder(JWSAlgorithm.HS256).build();

			Map<String, Object> map = new HashMap<>();
			map.put("timestamp", new Date());
			map.put("partnerId", "CORP00001");
			map.put("reqid", 12345);

			JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().claim("timestamp", map.get("timestamp"))
					.claim("partnerId", map.get("partnerId")).claim("reqid", map.get("reqid")).build();

			SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaims);
			signedJwt.sign(signer);

			String jwtToken = signedJwt.serialize();

			System.err.println("JWT : " + jwtToken);

			return jwtToken;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public ResponseStructure justCheckSprintHttpNet(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {

			String jwt = jwtHs256Token();

			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(
							"https://uat.paysprint.in/sprintverify-uat/api/v1/verification/drivinglicense_verify"))
					.header("accept", "application/json").header("Content-Type", "application/json")
					.header("Token", jwt).header("authorisedkey", "TVRJek5EVTJOelUwTnpKRFQxSlFNREF3TURFPQ==")
					.method("POST", HttpRequest.BodyPublishers
							.ofString("{\"id_number\":\"TN1220190010000\",\"dob\":\"2001-03-28\"}"))
					.build();
			HttpResponse<String> response = HttpClient.newHttpClient().send(request,
					HttpResponse.BodyHandlers.ofString());

			System.out.println(response.body());

			System.err.println("RespB : " + response);

			JSONObject responseA = new JSONObject(response);

			System.err.println("RespA : " + responseA);

			structure.setData(response);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure justCheckSprintOurMethod(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String jwt = jwtHs256Token();

			RestTemplate restTemplate = new RestTemplate();

			// create headers
			HttpHeaders headers = new HttpHeaders();
			headers.add("content-type", "application/json");
			headers.add("authorisedkey", "TVRJek5EVTJOelUwTnpKRFQxSlFNREF3TURFPQ==");
			headers.add("Token", jwt);
			headers.add("accept", "application/json");

			HttpEntity<String> entity = new HttpEntity<>("{\"id_number\":\"632894611441\"}", headers);

			ResponseEntity<String> clientResponse = restTemplate.postForEntity(
					"https://uat.paysprint.in/sprintverify-uat/api/v1/verification/aadhaar_without_otp", entity,
					String.class);

			System.err.println("Client Resp : " + clientResponse);

			String data = clientResponse.getBody();

			System.err.println("Data : " + data);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure justCheckSprintUnirest(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
//			String jwt = jwtHs256Token();
//
//			HttpResponse<String> response = Unirest
//					.post("https://uat.paysprint.in/sprintverify-uat/api/v1/verification/drivinglicense_verify")
//					.header("accept", "application/json").header("Content-Type", "application/json")
//					.header("Token",
//							"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0aW1lc3RhbXAiOjE2ODAwNjc3MjcsInBhcnRuZXJJZCI6IkNPUlAwMDAwMSIsInJlcWlkIjoia2V5NTg3MDQwIn0.uSFJwpuFC2a0vaybRHGZ2RI1C9fvzF2pqJ0Qr7qa1Nk")
//					.header("authorisedkey", "MTIzNDU2NzU0NzJDT1JQMDAwMDE=")
//					.body("{\"id_number\":\"string\",\"dob\":\"string\"}").asString();
//
//			System.err.println("Data : " + data);
//
		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure justCheckSprintVJavaNetHttp() {

		String jwt = jwtHs256Token();

		ResponseStructure structure = new ResponseStructure();
		// Replace with the actual Sprint Verify API endpoint URL
		String apiEndpoint = "YOUR_API_ENDPOINT";

		// Create an HttpClient
		HttpClient httpClient = HttpClient.newBuilder().build();

		// Define your request payload as a string (replace with actual data)
		String requestBody = "Your request data here";

		// Create an HttpRequest to make a POST request with a request body
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("https://uat.paysprint.in/sprintverify-uat/api/v1/verification/drivinglicense_verify"))
				.header("accept", "application/json").header("Content-Type", "application/json").header("Token", jwt)
				.header("authorisedkey", "TVRJek5EVTJOelUwTnpKRFQxSlFNREF3TURFPQ==")
				.method("POST", HttpRequest.BodyPublishers
						.ofString("{\"id_number\":\"TN1220190010000\",\"dob\":\"2001-03-28\"}"))
				.build();

		// Send the HTTP POST request asynchronously
		CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(request,
				HttpResponse.BodyHandlers.ofString());

		HttpResponse<String> response = responseFuture.join();

		int statusCode = response.statusCode();
		String responseBody = response.body();

		System.out.println("Response Code: " + statusCode);
		System.err.println("Response Body: " + responseBody + "/n");

		return structure;
	}

	@Override
	public ResponseStructure justCheck(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		double d = model.getAmount();
		long id = model.getId();

		try {

			if (id == 1) {

				double twoDecimelDouble = fu.twoDecimelDouble(d);

				structure.setData(twoDecimelDouble);
				structure.setMessage(AmountToWords.convertAmountToWords(twoDecimelDouble));

			} else if (id == 2) {

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

//		LocalDate currentDate = LocalDate.now();
//		List<Response> responseList = respRepository.getNotDeletedStatus();
//
//		for (Response response : responseList) {
//			LocalDate localDateConverter = DateUtil.convertDateToLocalDateViaSql(response.getRequestDateAndTime());
//
//			long monthsBetween = ChronoUnit.MONTHS.between(localDateConverter,currentDate);
//
//			System.err.println("SD: "+localDateConverter+" ---  ED : "+currentDate+ "::  AND MONTH BETWEEN : "+monthsBetween);
//			
//			if (monthsBetween>3) {
//				System.err.println("DELETED ");
//				
//				response.setStatus("Deleted");
//				respRepository.save(response);
//			}
//			
//			structure.setData("SD: "+localDateConverter+" ---  ED : "+currentDate+ "::  AND MONTH BETWEEN : "+monthsBetween);
//		}
//	

		return structure;
//
//		Date currentDate = new Date();
//		Calendar calender = Calendar.getInstance();
//
//		calender.setTime(currentDate);
//		calender.add(Calendar.MONTH, -1);
//
//		Date oneMonthBefore = calender.getTime();
//		System.err.println("Today : " + currentDate + "           " + "One Month Before : " + oneMonthBefore);
//
//		List<Response> ResponseList = respRepository.findAll();
//		List<VendorModel> vendorList = vendorRepository.findAll();
//		VendorVerificationModel vendorVerify = vendorVerificationRepository.findByVendorVerificationId(1);
//
//		long vendor1 = 0;
//		long vendor2 = 0;
//		long vendor3 = 0;
//		long totalResp = 0;
//		long totalSucccessPan = 0;
//		long totalPan = 0;
//
//		for (Response response : ResponseList) {
//
//			if (currentDate.after(response.getRequestDateAndTime())
//					&& oneMonthBefore.before(response.getRequestDateAndTime())) {
//
//				totalResp++;
//
//				if (response.getRequest().getVerificationModel().getVendorVerificationId() == vendorVerify
//						.getVendorVerificationId()) {
//
//					System.out.println("Resp Id : " + response.getResponseId() + "   STATUS : " + response.getStatus());
//					System.err
//							.println("RRVV  : " + response.getRequest().getVerificationModel().getVendorVerificationId()
//									+ "     " + "VV : " + vendorVerify.getVendorVerificationId());
//
//					totalPan++;
//
//					if (response.getStatus().equalsIgnoreCase("success")) {
//
//						totalSucccessPan++;
//
//						if (response.getVendorModel().getVendorId() == 1) {
//							vendor1++;
//						} else if (response.getVendorModel().getVendorId() == 2) {
//							vendor2++;
//						} else if (response.getVendorModel().getVendorId() == 3) {
//							vendor3++;
//						}
//					}
//				}
//
//			}
//		}
//
//		System.err.println("Total : " + totalResp);
//		System.err.println("Total Pan: " + totalSucccessPan);
//		System.err.println("Total Pan: " + totalPan);
//		System.err.println("SIGN DESK : " + vendor1);
//		System.err.println("SPRINT V : " + vendor2);
//		System.err.println("SIGN Z : " + vendor3);
//
//		VendorModel vendorModel = new VendorModel();
//
//		if (vendor1 >= vendor2 && vendor1 >= vendor3) {
//
//			vendorModel = vendorRepository.findByVendorId(1);
//
//		} else if (vendor2 >= vendor1 && vendor2 >= vendor3) {
//
//			vendorModel = vendorRepository.findByVendorId(2);
//
//		} else if (vendor3 >= vendor1 && vendor3 >= vendor2) {
//
//			vendorModel = vendorRepository.findByVendorId(3);
//		}
//
//		System.err.println("O K");
//
//		structure.setData(null);
//
//		return structure;
//
//		// return vendorSuccessRate();
	}

	@Override
	public ResponseStructure vendorByName(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			VendorVerificationModel vendorVerify = vendorVerificationRepository
					.findByVerificationDocument(model.getVerificationDocument());

			structure.setData(vendorVerify);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(null);
		}

		return structure;
	}

	@Override
	public ResponseStructure clientIpAddress(HttpServletRequest request, HttpServletResponse response) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String clientIp = request.getRemoteAddr();

			response.setContentType("text/plain");
			response.getWriter().write(clientIp);

			System.out.println("CLIENT IP : " + clientIp);

			structure.setData(clientIp);
			structure.setMessage("SUCCESS");

		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage("ERROR");
		}

		return structure;
	}

	@Override
	public ResponseStructure agreementCheck() {

		ResponseStructure structure = new ResponseStructure();
		try {

			Path con = Paths.get(context.getRealPath("/WEB-INF/"));

			MerchantModel merchantModel = merchantRepository.findByMerchantId(1);

			EntityModel admin = merchantModel.getEntity();

			MerchantBond merchantBond = merchantModel.getMerchantBond();

			String html = "";

			/*
			 * String html = "<!DOCTYPE html>\r\n" + "<html lang=\"en\">\r\n" + "\r\n" +
			 * "<head>\r\n" + "    <meta charset=\"UTF-8\" />\r\n" +
			 * "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\r\n"
			 * + "    <title>Agreement2</title>\r\n" + "    <style>\r\n" +
			 * "        .agreed {\r\n" + "            border: 1px solid black;\r\n" +
			 * "        }\r\n" + "\r\n" + "        #table2 {\r\n" +
			 * "            border-collapse: collapse;\r\n" + "        }\r\n" +
			 * "    </style>\r\n" + "</head>\r\n" + "\r\n" + "<body>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;margin-top: 10px;\">\r\n"
			 * + "        <p><strong>16.</strong></p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-top: 10px;\">\r\n" +
			 * "        <p><strong> USE OF INTELLECTUAL PROPERTY RIGHTS AND PROTECTION OF SOFTWARE APPLICATION\r\n"
			 * + "            </strong>\r\n" + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>16.1</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>The Merchant hereby grants to Tyche the right to use, display and reproduce its name, brand\r\n"
			 * +
			 * "            name, logo, wordmark, trademark, service marks <strong>(“Marks”)</strong> on a non-exclusive, royalty-free\r\n"
			 * +
			 * "            basis, solely in connection with the sales, marketing and advertising Tyche Services provided\r\n"
			 * +
			 * "            to the Merchant to the public. The Merchant hereby release Tyche from all liability relating to\r\n"
			 * +
			 * "            the publication or use of the Marks for such purpose. The Merchant hereby confirms that the\r\n"
			 * +
			 * "            Merchant has the requisite right to use the said Marks and to grant permission to use as stated\r\n"
			 * +
			 * "            herein. The Merchant shall retain all intellectual property rights in such marks.\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>16.2</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>The Merchant shall prominently display on its website and/or in other online marketing\r\n"
			 * +
			 * "            materials, a statement/logo/marks/image provided by Tyche relating to TycheServices and that\r\n"
			 * +
			 * "            of the respective Acquiring Banks providing the Payment Mechanism. The Merchant must only\r\n"
			 * +
			 * "            use the logos/marks/images provided by Tyche and no other.</p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>16.3</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Nothing contained herein shall authorize the Parties to use, apply, invade or in any manner\r\n"
			 * +
			 * "            exploit or infringe the intellectual property rights of the other Parties without prior written\r\n"
			 * +
			 * "            consent of the other Party, and the usage shall be in compliance with this Agreement and such\r\n"
			 * +
			 * "            approval and policies as may be notified from time to time. In addition, the Parties undertake not\r\n"
			 * +
			 * "            to infringe the intellectual property rights of any third party.</p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>16.4</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>The Merchant undertakes not to infringe the intellectual property rights of Acquiring Banks\r\n"
			 * +
			 * "            and/or Tyche respectively, whether directly or indirectly through any third party in the\r\n"
			 * +
			 * "            Acquiring Banks Services and software and/or Tyche Services and Software Application. The\r\n"
			 * +
			 * "            Merchant warrants that it shall only use the Tyche’s Software Application and the Acquiring\r\n"
			 * + "            Banks software for the purposes of this Agreement.\r\n" +
			 * "            The Merchant, its employees, contractors,\r\n" +
			 * "            agents or any other person empowered by the Merchant shall not use the Tyche Software\r\n"
			 * +
			 * "            Application and/or Acquiring Banks software in any form whatsoever, so as to:\r\n"
			 * + "        </p>\r\n" + "        <div style=\" width: 6%;float: left;\">\r\n"
			 * + "            <p>(a)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p> design, realize, distribute or market a similar or equivalent software program;\r\n"
			 * + "            </p>\r\n" + "        </div>\r\n" + "\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:110px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>25</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 100%;float: left;margin-left: 60px;\">\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(b)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p> adapt, modify, transform or rearrange the Software Application or the Acquiring Banks\r\n"
			 * +
			 * "                software for any reason whatsoever, including for the purpose, among other things, of\r\n"
			 * +
			 * "                creating a new software program or a derivative software program;\r\n"
			 * + "            </p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(c)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p> allow unauthorized use of or access to the Software Application and/or Acquiring Banks\r\n"
			 * + "                software;\r\n" + "            </p>\r\n" +
			 * "        </div>\r\n" + "        <div style=\" width: 6%;float: left;\">\r\n"
			 * + "            <p>(d)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p> disassemble, reverse engineer, decompile, decode or attempt to decode the Software\r\n"
			 * + "                Application and/or Acquiring Banks software;\r\n" +
			 * "            </p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "            <p>(e)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p> allow the Software Application and/or Acquiring Banks software to be disassembled,\r\n"
			 * + "                reverse engineered, decompiled or decoded; and/or\r\n" +
			 * "            </p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "            <p>(f)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p> in any way override or break down any protection system integrated into the Software\r\n"
			 * + "                Application and/or Acquiring Banks software.\r\n" +
			 * "            </p>\r\n" + "        </div>\r\n" + "\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>16.5</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>The Merchant fully understands that due to use of the Customer of the Internet Payment\r\n"
			 * +
			 * "            Gateway through Tyche Site, Tyche may create or generate database in respect of such\r\n"
			 * +
			 * "            Customers. All rights and ownership with respect to such database shall vest with Tyche.</p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;margin-top: 10px;\">\r\n"
			 * + "        <p><strong>17.</strong></p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-top: 10px;\">\r\n" +
			 * "        <p><strong> CONFIDENTIALITY\r\n" + "            </strong>\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>17.1</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>The Parties agree to maintain the confidentiality of the Confidential Information and to protect\r\n"
			 * +
			 * "            all portions of the other Party’s Confidential Information by preventing any unauthorized\r\n"
			 * +
			 * "            disclosure, copying, use,distribution, or transfer ofpossession of suchinformation.Dissemination\r\n"
			 * +
			 * "            of Confidential Information by each Party shall be limited to those employees with the need to\r\n"
			 * +
			 * "            such access for the advancement of the goals anticipated under this Agreement.</p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>17.2</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>The Parties shall at no time disclose or allow its officers, directors, employees, representatives\r\n"
			 * +
			 * "            or subcontractors to disclose the other Party’s Confidential Information to any third party\r\n"
			 * + "            without\r\n" + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:90px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>26</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" + "\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-left:60px\">\r\n" +
			 * "        <p>\r\n" +
			 * "            the prior written consent of the other Party. The Parties agree to protect the Confidential\r\n"
			 * +
			 * "            Information of the other with the same standard of care and procedures used by themselves to\r\n"
			 * +
			 * "            protect their own Confidential Information of similar importance but at all times using at least a\r\n"
			 * + "            reasonable degree of care.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>17.3</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>The obligations set out in this Clause shall not apply to Confidential Information that:</p>\r\n"
			 * + "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(a)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p> is or becomes publicly known other than through breach of this Clause 17;\r\n"
			 * + "            </p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(b)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>is in possession of the receiving Party prior to disclosure by the other Party;\r\n"
			 * + "            </p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(c)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>is independently developed by the receiving Party;</p>\r\n" +
			 * "        </div>\r\n" + "        <div style=\" width: 6%;float: left;\">\r\n"
			 * + "            <p>(d)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>needs to be disclosed to professional advisers or in accordance with the order of a\r\n"
			 * + "                competent court or administrative authority;</p>\r\n" +
			 * "\r\n" + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(e)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>is thereafter rightfully furnished to such receiving Party by a third party without\r\n"
			 * + "                restriction by that third party on disclosure; or</p>\r\n"
			 * + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(f)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>is required by law, judicial court, recognized stock exchange, government department\r\n"
			 * +
			 * "                or agency or other regulatory authority, provided that sufficient notice is given of any such\r\n"
			 * +
			 * "                requirement, by the receiving Party to the disclosing Party, in order that the disclosing\r\n"
			 * +
			 * "                Party may seek for an appropriate protective order or exemption from such requirement,\r\n"
			 * +
			 * "                prior to any disclosure being made by the receiving Party and/or its Affiliates.\r\n"
			 * + "            </p>\r\n" + "        </div>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;\">\r\n" + "        <p>17.4</p>\r\n"
			 * + "    </div>\r\n" + "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Such obligation of confidentiality shall continue for a period of 1 (one) year after the\r\n"
			 * + "            termination or expiry of this Agreement.</p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height: 110px;\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>27</h3>\r\n" + "    </div>\r\n" + "\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p><strong>18.</strong></p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p><strong> RELATIONSHIP BETWEEN THE PARTIES\r\n" +
			 * "            </strong>\r\n" + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>18.1</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>The relationship between Tyche and the Merchant is on principal-to-principal basis. Nothing\r\n"
			 * +
			 * "            contained herein shall be deemed to create any association, partnership, joint venture or\r\n"
			 * +
			 * "            relationship of principal and agent or master and servant, or employer and employee between\r\n"
			 * +
			 * "            the Parties hereto or any affiliates or subsidiaries thereof or to provide either Party with the\r\n"
			 * +
			 * "            right, power or authority, whether express or implied to create any such duty or obligation on\r\n"
			 * + "            behalf of the other Party.</p>\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>18.2</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Tyche has no connection or interest of whatsoever nature in the business of the Merchant or the\r\n"
			 * +
			 * "            Products offered/ marketed on the Merchant Site. Tyche does not in any manner take part in the\r\n"
			 * +
			 * "            business of the Merchant, directly or indirectly. Tyche shall only provide Tyche Services to the\r\n"
			 * +
			 * "            Merchant in relation to the Merchants, the Customers and the Acquiring Bank, as an independent\r\n"
			 * +
			 * "            entity and under the terms and conditions of this Agreement. For the TycheServices provided by\r\n"
			 * +
			 * "            Tyche, it shall be paid an agreed service fee by the Merchant as stipulated in this Agreement and\r\n"
			 * +
			 * "            Tyche is nowhere connected or concerned about the revenues of the Merchant or the Acquiring\r\n"
			 * + "            Banks.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>18.3</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Tyche has no relationship with the Customers and all actions under this Agreement which may\r\n"
			 * +
			 * "            affect the Customers are instructed by the Merchant. The Merchant alone shall be responsible to\r\n"
			 * +
			 * "            the Customers and neither Tyche nor the Acquiring Bank or anybody connected to Tyche or\r\n"
			 * +
			 * "            Acquiring Bank shall have any responsibility or liability towards the Customers and the\r\n"
			 * +
			 * "            Merchant shall keep Tyche and Acquiring Bank fully indemnified for all times to come in this\r\n"
			 * + "            respect.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>18.4</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Tyche is neither concerned nor required to monitor in any manner the use of the payment modes\r\n"
			 * +
			 * "            by the Customers for procuring / availing the Products of the Merchant. The Customers should\r\n"
			 * +
			 * "            be required to use the payment modes at their sole option and risks. The Merchant shallbe\r\n"
			 * +
			 * "            required to notify this responsibility to all its Customers under the instructions provided by\r\n"
			 * + "            Tyche.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p><strong>19.</strong></p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p><strong> INDEMNITY\r\n" + "            </strong>\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>19.1</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> The Merchant hereby undertakes and agrees to indemnify, defend and hold harmless Tyche\r\n"
			 * +
			 * "            and/or the Acquiring Banks including their officers, directors and agents from and against all\r\n"
			 * + "            actions, </p>\r\n" + "\r\n" + "\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height: 90px;\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>28</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-left:60px\">\r\n" +
			 * "        <p>\r\n" +
			 * "            proceedings, claims (including third party claims), liabilities (including statutory liability),\r\n"
			 * +
			 * "            penalties, demands and costs (including without limitation, legal costs), awards,\r\n"
			 * +
			 * "            damages, losses and/or expenses however arising directly or indirectly, including but notlimited\r\n"
			 * + "            to, as a result of:</p>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(a)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p> breach or non-performance by the Merchant of any of its undertakings, warranties,\r\n"
			 * +
			 * "                covenants, declarations or obligations under this Agreement;\r\n"
			 * + "            </p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(b)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p> breach of confidentiality and intellectual property rights obligations by the Merchant;\r\n"
			 * + "            </p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(c)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p> any claim or proceeding brought by the Customer or any third party against Tyche and/or\r\n"
			 * +
			 * "                the Acquiring Banks in respect of any Products or Services offered by the Merchant;</p>\r\n"
			 * + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(d)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p> any claim or proceeding brought by the Customer or any third party against Tyche and/\r\n"
			 * +
			 * "                or the Acquiring Banks in respect of Tyche Services;</p>\r\n"
			 * + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(e)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p> any act, deed, negligence, omission, misrepresentation, default, misconduct, nonperformance or fraud by\r\n"
			 * +
			 * "                the Merchant, its employees, contractors, agents, Customers or\r\n"
			 * + "                any third party;</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(f)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>any hacking or lapse in security of the Merchant Site or the Customer data;</p>\r\n"
			 * + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(g)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>Chargebacks or refunds relating to the Transactions contemplated under this Agreement;</p>\r\n"
			 * + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(h)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>breach of law, rules regulations, legal requirements (including RBI regulations, Card\r\n"
			 * +
			 * "                Association Rules, Acquiring Bank rules) in force in India and/or in any place from where\r\n"
			 * +
			 * "                the Customers is making the Transaction and/or where the Product is or to be Delivered\r\n"
			 * +
			 * "                and/or where the respective Issuing Institution is incorporated/registered/ established;\r\n"
			 * + "                or\r\n" + "            </p>\r\n" + "        </div>\r\n" +
			 * "\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height: 120px;\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>29</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 100%;float: left;margin-left:60px\">\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(i)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>any fines, penalties or interest imposed directly or indirectly on Tyche on account of\r\n"
			 * +
			 * "                Merchant’s or Transactions conducted through Merchant Site under these Terms and\r\n"
			 * + "                Conditions.</p>\r\n" + "        </div>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>19.2</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>The indemnities under this Clause are in addition to and without prejudice to the indemnities\r\n"
			 * + "            given elsewhere in this Agreement.</p>\r\n" + "    </div>\r\n"
			 * + "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>19.3</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>The indemnities provided herein shall survive the termination of this Agreement.</p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p><strong>20.</strong></p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p><strong> LIMITATION OF LIABILITY\r\n" +
			 * "            </strong>\r\n" + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>20.1</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Notwithstanding anything stated under this Agreement, the aggregate liability of Tyche to the\r\n"
			 * +
			 * "            Merchant from any cause whatsoever shall not in any event exceed the sum equivalent to the\r\n"
			 * +
			 * "            preceding one month’s aggregate Service Fee earned by Tyche under this agreement from the\r\n"
			 * +
			 * "            date of occurrence of such liability. Provided that Tyche shall not be liable to the Merchant for\r\n"
			 * +
			 * "            any special, incidental, indirect or consequential damages, damages from loss of profits or\r\n"
			 * +
			 * "            business opportunities even if the Merchant shall have been advised in advance of the possibility\r\n"
			 * +
			 * "            of such loss, cost or damages. In no event shall Tyche be liable to the Customers or any third\r\n"
			 * +
			 * "            party. In no event shall the Nodal Bank or the Acquiring Bank be liable to the Merchant in any\r\n"
			 * + "            way under this Agreement.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p><strong>21.</strong></p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p><strong> DISCLAIMER\r\n" + "            </strong>\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>21.1</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Tyche will make all reasonable efforts to provide uninterrupted service subject to down\r\n"
			 * +
			 * "            time and regular maintenance. However, notwithstanding anything in this Agreement, the\r\n"
			 * +
			 * "            Merchant acknowledges that Tyche Site, Tyche Services and the Acquiring Bank’s Services maynot be\r\n"
			 * +
			 * "            uninterrupted or error free or free from any virus or other malicious, destructive or\r\n"
			 * +
			 * "            corruptingcode, program or macro and Tyche and the Acquiring Banks disclaim all warranties,\r\n"
			 * +
			 * "            expressor implied, written or oral, including but not limited to warranties of merchantability and\r\n"
			 * +
			 * "            fitness of the services for a particular purpose. The Merchant also acknowledges that the\r\n"
			 * +
			 * "            arrangement between one or more Acquiring Banks and Tyche may terminate at any time and services of such\r\n"
			 * +
			 * "            Acquiring Banks may be withdrawn. Although Tyche adopts security measures it\r\n"
			 * + "\r\n" + "        </p>\r\n" + "\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:110px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>30</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 100%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>considers appropriate for the offer of the Tyche Service, it does not assure or guarantee that no\r\n"
			 * +
			 * "            person will overcome or subvert the security measures and gain unauthorized access to the\r\n"
			 * +
			 * "            Tyche Service or the Merchant/Customer data. Tyche shall not be responsible or liable if any\r\n"
			 * +
			 * "            unauthorized person hacks into or gains access to the Tyche Service or to the Merchant’s Tyche\r\n"
			 * +
			 * "            Account. In event of incorrect settlement in the Merchant’s Account due to error on the part of\r\n"
			 * +
			 * "            Tyche or theBank, Tyche shall have the right to reverse the extra funds from the Merchant\r\n"
			 * +
			 * "            Bank Account.In addition, the Merchant shall be fully liable to return the extra funds settled\r\n"
			 * +
			 * "            within 7 (seven) days of intimation by Tyche. Subject to the other clauses of this Agreement, in\r\n"
			 * +
			 * "            the event that the Settlement Amounts to be transferred to the Merchant have not been\r\n"
			 * +
			 * "            transferred, Tyche shall endeavor to settle the relevant Settlement Amount to the Merchant’s\r\n"
			 * +
			 * "            account within 7 (seven) days of notification from the Merchant. In addition Tyche shall not be\r\n"
			 * +
			 * "            liable to the Merchant forany loss or damage whatsoever or howsoever caused or arising,\r\n"
			 * +
			 * "            directly orindirectly, includingwithout limitation, as a result of loss of data; interruption or\r\n"
			 * +
			 * "            stoppage to the Customer’s access to and/or use of the Merchant Site, Tyche Services and/or the\r\n"
			 * +
			 * "            Payment Mechanism, interruption or stoppage of Tyche Site, hacking or unauthorized access to the Tyche\r\n"
			 * +
			 * "            Services,SoftwareApplication and Internet Payment Gateway, non-availability of connectivity between\r\n"
			 * +
			 * "            the Merchant Site and Tyche Site, etc. Any material/information downloaded or otherwise obtained\r\n"
			 * +
			 * "            through the use ofthe Tyche Services is done at the Merchant’s own discretion and risk and the Merchant\r\n"
			 * +
			 * "            will be solely responsible for any damage to its computer system or loss of data that results from the\r\n"
			 * +
			 * "            download of any such material. No advice or information, whether oral or written, obtained by the\r\n"
			 * +
			 * "            Merchant from Tyche or through or from the use of Tyche Services shall create any warranty.Tyche shall\r\n"
			 * + "            have no liability in this respect.</p>\r\n" + "    </div>\r\n"
			 * + "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>21.2</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Tyche’s sole obligation and the Merchant’s sole and exclusive remedy in the event of interruption\r\n"
			 * +
			 * "            in Tyche Site, or loss of use and/or access to Tyche Site, the Acquiring banks Services and the\r\n"
			 * +
			 * "            Payment Mechanism and services, shall be to use all reasonable endeavors to restore the\r\n"
			 * +
			 * "            Services and/or access to the Payment Mechanism as soon as reasonably possible.</p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>21.3</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Tyche or Acquiring Bank obligations under this Agreement are subject to following limitations:</p>\r\n"
			 * + "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(a)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>messages that originate from the server of the Merchant or the server of a third party\r\n"
			 * +
			 * "                designated by Merchant (e.g., a host) shall be deemed to be authorized by the Merchant,\r\n"
			 * +
			 * "                and Tyche shall not be liable for processing such messages;</p>\r\n"
			 * + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(b)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>messages that originate from the cardholder are deemed to be authorized by the card\r\n"
			 * +
			 * "                holder and Tyche shall not be required to check its veracity and Tyche shall not be liable\r\n"
			 * + "                for processing such messages;</p>\r\n" +
			 * "        </div>\r\n" + "\r\n" + "\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:80px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>31</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-left: 60px;\">\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(c)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>Tyche or the Acquiring Bank are not responsible for the security of data residing on the\r\n"
			 * +
			 * "                server of the Merchant or a third party designated by the Merchant (e.g., a host) or on the\r\n"
			 * +
			 * "                server of a cardholder or a third party designated by a Merchant/cardholder (e.g., a host);\r\n"
			 * + "                and</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(d)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>Tyche and/or the Acquiring Banks shall have no liability for any failure or delay in\r\n"
			 * +
			 * "                performing its obligations under this facility if such failure or delay: (i) is caused by the\r\n"
			 * +
			 * "                Merchant’s acts or omissions; (ii) results from actions taken by Tyche or the Acquiring\r\n"
			 * +
			 * "                Banks in a reasonable good faith to avoid violating a law, rule or regulation of any\r\n"
			 * +
			 * "                governmental authority or to prevent fraud on cardholders/accounts; or (iii)is caused by\r\n"
			 * +
			 * "                circumstances beyond Tyche control, including but not limited to vandalism, hacking,\r\n"
			 * +
			 * "                theft, phone service disruptions, Internet disruptions, loss of data, extreme or severe\r\n"
			 * +
			 * "                weather conditions or any other causes in the nature of Force Majeure event.</p>\r\n"
			 * + "\r\n" + "        </div>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;margin-top: 10px;\">\r\n"
			 * + "        <p><strong>22.</strong></p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-top: 10px;\">\r\n" +
			 * "        <p><strong> TERMINATION\r\n" + "            </strong>\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>22.1</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>This Agreement may be terminated by either Party by\r\n" +
			 * "            giving 30 (thirty) days prior written notice to the other Party.</p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>22.2</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Either Party may terminate this Agreement forthwith in the event:</p>\r\n"
			 * + "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(a)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>the Party discovers at any stage that the other Party is in violation of any law or\r\n"
			 * + "                regulation;</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(b)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>the other Party is adjudicated as bankrupt, or if a receiver or as a trustee is appointed\r\n"
			 * +
			 * "                for it or for a substantial portion of its assets, or if any assignment for the benefit of its\r\n"
			 * +
			 * "                creditors is made and such adjudication appointment or assignment is not set aside\r\n"
			 * + "                within 90 (ninety) Business Days;</p>\r\n" +
			 * "        </div>\r\n" + "\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:130px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>32</h3>\r\n" + "    </div>\r\n" + "\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" + "\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-left: 60px;\">\r\n" + "\r\n"
			 * + "\r\n" + "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(c)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>the other Party goes into liquidation either voluntarily or compulsorily;</p>\r\n"
			 * + "        </div>\r\n" +
			 * "        <div style=\" width: 6%;float: left;\">\r\n" +
			 * "            <p>(d)</p>\r\n" + "        </div>\r\n" +
			 * "        <div style=\"width: 90%;float: left;\">\r\n" +
			 * "            <p>the other Party is prohibited by any regulatory or statutory restriction from continuing\r\n"
			 * + "                to provide services under this Agreement.</p>\r\n" +
			 * "        </div>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>22.3</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Tyche shall terminate this Agreement forthwith, if the Merchant fails to perform its obligations\r\n"
			 * +
			 * "            hereunder or is in breach of any terms and conditions of this Agreement.</p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>22.4</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>The termination under this Clause is in addition to and without prejudice to the termination\r\n"
			 * +
			 * "            rights given to the Parties under any other Clause in this Agreement.</p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p><strong>23</strong></p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p><strong> CONSEQUENCES OF TERMINATION\r\n" +
			 * "            </strong>\r\n" + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>23.1</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>The termination of this Agreement shall not affect the rights or liabilities of either Party incurred\r\n"
			 * +
			 * "            prior to such termination. In addition, any act performed during the term of this Agreement\r\n"
			 * +
			 * "            which may result in a dispute post termination or any provision expressed to survive this\r\n"
			 * +
			 * "            Agreement or to be effective on termination or the obligations set out in this Clause shall remain\r\n"
			 * +
			 * "            in full force and effect notwithstanding termination. Subject to other Clauses of this Agreement,\r\n"
			 * +
			 * "            both Parties shall undertake to settle all outstanding charges within 30 (thirty) daysof the\r\n"
			 * + "            termination taking effect.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>23.2</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Where any payments claimed by Tyche exceeds the Settlement Amount due to the Merchant the\r\n"
			 * +
			 * "            difference thereof shall be a debt due from the Merchant to Tyche and be forthwith recoverable\r\n"
			 * +
			 * "            by appropriate legal action, as deemed fit by Tyche. Without prejudice to Tyche’s rights and\r\n"
			 * +
			 * "            remedies, in the event that the Merchant does not make any payments to Tyche by its due date or\r\n"
			 * +
			 * "            on demand as required under this Agreement, Tyche shall be entitled to charge daily\r\n"
			 * +
			 * "            compounded interest on such overdue amount from the due date until the date of Settlement\r\n"
			 * +
			 * "            Amount in full, at the rate of 2.5% per month. This section shall not preclude Tyche from\r\n"
			 * +
			 * "            recourse to any other remedies available to it under any statute or otherwise, at law or in equity.</p>\r\n"
			 * + "    </div>\r\n" + "\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:140px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>33</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>23.3</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>All materials, documentation, instruction manuals, guidelines, letters and writings and other\r\n"
			 * +
			 * "            materials issued by Tyche from time to time in respect of this Agreement, whether in respect of\r\n"
			 * +
			 * "            the utilization of the Internet Payment Gateway or otherwise shall be returned by the Merchant\r\n"
			 * + "            to Tyche upon termination.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>23.4</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>The Merchant agrees and confirms that the Merchant shall remain solely liable after the\r\n"
			 * +
			 * "            termination of this Agreement for all Chargebacks, refunds, penalties, loss, damages or cost\r\n"
			 * +
			 * "            incurred by Tyche, Acquiring Banks, Card Associations and/or Customers and for all claims and\r\n"
			 * +
			 * "            proceedings arising against Tyche and/or Acquiring Banks with respect to this Agreement. At\r\n"
			 * +
			 * "            the time of termination, Tyche may retain such amount from the Reserve (if any) and Settlement\r\n"
			 * +
			 * "            Amount payable to the Merchant (including Settlement Amounts withheld) as may be\r\n"
			 * +
			 * "            determined by Tyche to cover chargeback risk, refund risk or any potential loss, damages,\r\n"
			 * +
			 * "            penalties, cost that may be incurred by Tyche, Acquiring Banks, Card Associations and/ or Customers for a\r\n"
			 * +
			 * "            period of 210 Business Days. Subject to this Clause and any other Clause of this\r\n"
			 * +
			 * "            Agreement, all settlement to the Merchant after notice of termination shall be done post\r\n"
			 * +
			 * "            termination.In the event that such retained amount is not sufficient to cover all Outstanding\r\n"
			 * +
			 * "            Amounts of the Merchant post termination, the Merchant shall ensure that it pays Tyche all\r\n"
			 * +
			 * "            pending amounts within 10 (ten) days of receiving the demand notice and shall atall times\r\n"
			 * +
			 * "            keep Tyche indemnified in this respect. This Clause survives the termination of this Agreement.</p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;margin-top: 10px;\">\r\n"
			 * + "        <p><strong>24.</strong></p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-top: 10px;\">\r\n" +
			 * "        <p><strong> GENERAL PROVISIONS\r\n" + "            </strong>\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>24.1</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong>Assignments:</strong> Tyche may assign, in whole or in part, the benefits or obligations of this\r\n"
			 * +
			 * "            Agreement by providing a thirty (30) days prior intimation of such assignment to the Merchant,\r\n"
			 * +
			 * "            which shall be binding on the Parties to this Agreement.</p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>24.2</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong> Force Majeure:</strong>Tyche shall not be liable for its failure to perform under this Agreement as\r\n"
			 * +
			 * "            a result of any event of force majeure events like acts of god, fire, wars, sabotage, civil unrest, labour\r\n"
			 * +
			 * "            unrest, action of Statutory Authorities or local or Central Governments, change in Laws,\r\n"
			 * +
			 * "            Rules and Regulations, affecting the performance of Tyche or the Acquiring Banks.\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>24.3</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong>Governing Law, Settlement of Disputes and Jurisdiction :</strong>Tyche shall not be liable for its\r\n"
			 * + "            failure to perform under this Agreement as a\r\n" +
			 * "            This Agreement(and any dispute or\r\n" +
			 * "            claim relating to it, its enforceability or its termination) is to be governed by and construed in\r\n"
			 * + "            accordance with the </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:90px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>34</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-left: 60px;\">\r\n" +
			 * "        <p> laws of India. Each ofthe Parties agrees that, if any dispute(s) or difference(s)\r\n"
			 * +
			 * "            shall arise between the Parties in connection with or arising out of this Agreement, the Parties\r\n"
			 * +
			 * "            shall attempt, for a period of 30 (thirty) days from the receipt of a notice from the other Party\r\n"
			 * +
			 * "            of the existence of a dispute(s), to settle such dispute(s) by mutual discussions between the\r\n"
			 * +
			 * "            Parties. If the said dispute(s) cannot be settled by mutual discussions within the thirty-day\r\n"
			 * +
			 * "            period provided above, either Party may refer the matter to a sole arbitrator to be mutually\r\n"
			 * +
			 * "            appointed in accordance with the Arbitration and Conciliation Act, 1996. The arbitration\r\n"
			 * +
			 * "            proceedings shall be held under the provisions of the Arbitration and Conciliation Act, 1996. The\r\n"
			 * +
			 * "            arbitration proceedings shall be held in English language at New Delhi. The courts atNew\r\n"
			 * +
			 * "            Delhi shall have the exclusive jurisdiction over any disputes relating to the subject matter of this\r\n"
			 * + "            Agreement.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>24.4</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong>Waiver :</strong>Unless otherwise expressly stated in this Agreement, the failure to exercise or\r\n"
			 * + "            delay\r\n" +
			 * "            in exercising a right or remedy under this Agreement shall not constitute a waiver of the right or\r\n"
			 * +
			 * "            remedy or a waiver of any other rights or remedies, and no single or partial exercise of any right\r\n"
			 * +
			 * "            or remedy under this Agreement shall prevent any further exercise of the right or remedyor the\r\n"
			 * + "            exercise of any other right or remedy.</p>\r\n" +
			 * "    </div>\r\n" + "\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>24.5</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong> Survival of Provisions :</strong> The terms and provisions of this Agreement that by their nature\r\n"
			 * + "            and\r\n" +
			 * "            content are intended to survive the performance hereof by any or all Parties hereto shall so\r\n"
			 * + "            survive the completion and termination of this Agreement.\r\n"
			 * + "            .</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>24.6</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong>Severability :</strong> If any provision of this Agreement is or becomes, in whole or in part,\r\n"
			 * + "            invalid or\r\n" +
			 * "            un enforceable but would be valid or enforceable if some part of that provision was deleted, that\r\n"
			 * +
			 * "            provision shall apply with such deletions as may be necessary to make it valid. If any Court/\r\n"
			 * +
			 * "            Tribunal of competent jurisdiction holds any of the provisions of this Agreement unlawful or\r\n"
			 * +
			 * "            otherwise ineffective, the remainder of this Agreement shall remain in full force and the unlawful\r\n"
			 * +
			 * "            or otherwise ineffective provision shall be substituted by a new provision reflecting the intent of\r\n"
			 * + "            the provision so substituted\r\n" + "            .</p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>24.7</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong>Non-Exclusivity :</strong> It is agreed and clarified that this Agreement is on a non-exclusive\r\n"
			 * + "            basis and\r\n" +
			 * "            the Parties are at liberty to enter into similar Agreements with others\r\n"
			 * + "            .</p>\r\n" + "    </div>\r\n" + "\r\n" +
			 * "    <div style=\"width: 100%;float: left;height: 120px;\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>35</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>24.8</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong> Entire Agreement :</strong> This Agreement constitutes the entire Agreement and understanding\r\n"
			 * +
			 * "            between the Parties, and supersedes any previous agreement or understanding or promise\r\n"
			 * +
			 * "            between the Parties, relating to the subject matter of this Agreement. All Schedules, Recitals\r\n"
			 * +
			 * "            and Annexure to this Agreement shall be an integral part of this Agreement and will be in full\r\n"
			 * +
			 * "            force and effect as though they were expressly set out in the body of this Agreement\r\n"
			 * + "            .\r\n" + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>24.9</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong>Notices:</strong>: All notices, requests, demands, waivers and other communications required or\r\n"
			 * +
			 * "            permitted to be given under the Agreement shall be in writing through certified or registered\r\n"
			 * +
			 * "            mail, courier, email, facsimile or telegram to be sent to the following addresses:\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;margin-left: 60px;\">\r\n" +
			 * "        <p><strong>For Tyche:</strong></p>\r\n" +
			 * "        <p>Tyche Payment Solutions Private Limited</p>\r\n" +
			 * "        <p>New # 9, Old # 11, 1st Floor,</p>\r\n" +
			 * "        <p>Palayakaran Street, Kalaimagal Nagar,</p>\r\n" +
			 * "        <p>Ekkaduthangal, Chennai – 600032</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;margin-left: 60px;\">\r\n" +
			 * "        <p><strong>For</strong></p>\r\n" +
			 * "        <p style=\"color: red;\">Tyche Payment Solutions Private Limited</p>\r\n"
			 * + "        <p style=\"color: red;\">New # 9, Old # 11, 1st Floor,</p>\r\n" +
			 * "        <p style=\"color: red;\">Palayakaran Street, Kalaimagal Nagar,</p>\r\n"
			 * + "        <p style=\"color: red;\">Ekkaduthangal, Chennai – 600032</p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;margin-left: 60px;\">\r\n" +
			 * "        <p>\r\n" +
			 * "            Or, in each case, at such other address as may be specified in writing to the other Parties in\r\n"
			 * +
			 * "            accordance with the requirements of this Clause. All such notices, requests, demands, waivers\r\n"
			 * +
			 * "            and other communications shall be deemed duly given (i)if by personal delivery, on the day after\r\n"
			 * + "            such delivery, (ii) if by certified or\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:110px;\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>36</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-left: 60px;\">\r\n" +
			 * "        <p>\r\n" +
			 * "            registered mail, on the10th (tenth) day after the mailing\r\n" +
			 * "            thereof, (iii) if by courier service or similar service, on the day delivered, or (iv) if by email,\r\n"
			 * +
			 * "            facsimile or telegram, on the day following the day on which such email, facsimile or telegram\r\n"
			 * +
			 * "            was sent, provided that a copy is also sent by registered mail and, in the case of a facsimile,\r\n"
			 * + "            electronic confirmation of receipt is received.\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>24.10</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong>Amendment :</strong>This Agreement shall not be varied, amended or modified by any of the Parties\r\n"
			 * +
			 * "            in any manner whatsoever unless such variation, amendment or modification is mutually\r\n"
			 * +
			 * "            discussed and agreed to in writing and duly executed by both the Parties.\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>24.11</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong>Counterparts :</strong>This Agreement may be executed in two or more counterparts, each of which,\r\n"
			 * +
			 * "            when executed and delivered, is an original, but all the counterparts taken together shall\r\n"
			 * + "            constitute one document.\r\n" + "        </p>\r\n" +
			 * "        <p style=\"margin-top: 1cm;\"> <strong>IN WITNESS WHERE OF</strong> the Parties hereto have executed this\r\n"
			 * + "            Agreement through their Authorized\r\n" +
			 * "            Signatories on the day, month and year first herein mentioned above:\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-left: 60px;\">\r\n" +
			 * "        <span style=\"color:red\">On Behalf of</span>\r\n" +
			 * "        <span style=\"border-bottom: 1px dotted red;\">\"+model.getBillingAddress()+\"\r\n"
			 * + "        </span>\r\n" +
			 * "        <span>On Behalf of Tyche Payment Solutions Private Limited</span>\r\n"
			 * + "\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;margin-left: 60px;float:left;\">\r\n" +
			 * "        <p><strong>Authorized Signatory</strong> &nbsp;</p>\r\n" +
			 * "        <p><strong>Name:</strong> </p>\r\n" +
			 * "        <p><strong>Designation:</strong> </p>\r\n" +
			 * "        <p><strong>Date:</strong> </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;margin-left: 60px;float:left;\">\r\n" +
			 * "        <p><strong>Authorized Signatory</strong> &nbsp;</p>\r\n" +
			 * "        <p><strong>Name:</strong> </p>\r\n" +
			 * "        <p><strong>Designation:</strong> </p>\r\n" +
			 * "        <p><strong>Date:</strong></p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:90px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>37</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 100%;float: left;text-align:center;\">\r\n" +
			 * "        <p><strong>SCHEDULE A</strong></p>\r\n" +
			 * "        <p><strong>AGREED COMMERCIALS</strong></p>\r\n" + "    </div>\r\n" +
			 * "\r\n" +
			 * "    <div style=\"width: 100%;float: left;text-align: center;\">\r\n" +
			 * "        <p><strong> Part I </strong> </p>\r\n" +
			 * "        <p><strong> The Merchant shall pay to Tyche: </strong> </p>\r\n" +
			 * "    </div>\r\n" + "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <table id=\"table2\" class=\"agreed\" style=\"margin-left:auto;margin-right: auto;\">\r\n"
			 * + "            <tr class=\"agreed\">\r\n" +
			 * "                <th class=\"agreed\">Particulars</th>\r\n" +
			 * "                <th class=\"agreed\">(Exclusive of Applicable Taxes)</th>\r\n"
			 * + "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\"><strong>A. System Integration Fee/Set up Fee <br /><span\r\n"
			 * + "                            style=\"font-size: 10px;\">(One\r\n" +
			 * "                            time payable upfront at the time of Signing of\r\n"
			 * +
			 * "                            the Services Agreement, non-refundable.) *</span></strong></td>\r\n"
			 * + "                <td class=\"agreed\">NA</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\"><strong>B. AMC/MMC*</strong></td>\r\n"
			 * + "                <td class=\"agreed\">NA</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\"><strong>C. Security Deposit (If applicable)</strong></td>\r\n"
			 * + "                <td class=\"agreed\">NA</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\"><strong>D. Service Fee*(Per Transaction)</strong></td>\r\n"
			 * + "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n"
			 * + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">1) Net Banking All</td>\r\n" +
			 * "                <td class=\"agreed\">Rs.25/-</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">2) Credit Card (Visa/Master)</td>\r\n"
			 * + "                <td class=\"agreed\">1.35%</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">3)Debit Card (Rupay,Master, Visa, Maestro\r\n"
			 * + "                    (SBI), Maestro (non-SBI)</td>\r\n" +
			 * "                <td class=\"agreed\">1.35%</td>\r\n" +
			 * "            </tr>\r\n" + "\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">4) e-Collect</td>\r\n" +
			 * "                <td class=\"agreed\">NA</td>\r\n" + "            </tr>\r\n"
			 * + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">5)EMI Above 5000</td>\r\n" +
			 * "                <td class=\"agreed\">1.9%</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">6) International Card</td>\r\n" +
			 * "                <td class=\"agreed\">NA</td>\r\n" + "            </tr>\r\n"
			 * + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">7) Amex Card</td>\r\n" +
			 * "                <td class=\"agreed\">NA</td>\r\n" + "            </tr>\r\n"
			 * + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">8) Dinners Card</td>\r\n" +
			 * "                <td class=\"agreed\">1.90%</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">9) Corporate/Commercial Card ( Master Visa)</td>\r\n"
			 * + "                <td class=\"agreed\">2.85%</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">10) Prepaid card</td>\r\n" +
			 * "                <td class=\"agreed\">2.85%</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">11) Wallets (Except ITZ Cash)</td>\r\n"
			 * + "                <td class=\"agreed\">2.00%</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">\r\n" +
			 * "                    <div>12) UPI</div>\r\n" + "                </td>\r\n" +
			 * "                <td class=\"agreed\">0.55%</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">13)UPI Intent</td>\r\n" +
			 * "                <td class=\"agreed\">NA</td>\r\n" + "            </tr>\r\n"
			 * + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">14) Bharat QR</td>\r\n" +
			 * "                <td class=\"agreed\">0.55%</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">15)Settlement Timeframe (Td+1, (wherein d\r\n"
			 * + "                    refers to Delivery date assumed as T+0/1/2)</td>\r\n"
			 * + "                <td class=\"agreed\">Various Time Frames</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">16) VAS (mention if any)</td>\r\n" +
			 * "                <td class=\"agreed\">NA</td>\r\n" + "            </tr>\r\n"
			 * + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">17) Others Please specify</td>\r\n" +
			 * "                <td class=\"agreed\">NA</td>\r\n" + "            </tr>\r\n"
			 * + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">16) EMI</td>\r\n" +
			 * "                <td class=\"agreed\"> &gt;5000 - 1.90%</td>\r\n" +
			 * "            </tr>\r\n" + "        </table>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:100px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>38</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 100%;float: left;margin-top: 15px;\"> <strong>Explanation:</strong>\r\n"
			 * + "    </div>\r\n" + "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <ul>\r\n" +
			 * "            <li>Whenever Tyche offers a new bank gateway or a new payment option or makes any revision of\r\n"
			 * +
			 * "                charges, the terms and commercials for such payment gateway or payment option, shall be\r\n"
			 * +
			 * "                communicatedbyTyche totheMerchant through email or dashboard notification;</li>\r\n"
			 * +
			 * "            <li style=\"margin-top: 10px;\">It is clarified that the Merchant shall bear and be liable for\r\n"
			 * + "                the payment of all relevant taxes</li>\r\n" +
			 * "            <li style=\"margin-top: 10px;\">including without limitation GST in relation to the Customer\r\n"
			 * + "                Charge under this Agreement;</li>\r\n" +
			 * "            <li style=\"margin-top: 10px;\">The Service Fee is exclusive of all applicable taxes, including without\r\n"
			 * + "                limitation, GST; and</li>\r\n" +
			 * "            <li style=\"margin-top: 10px;\">The Service Fee charged by Tyche on Rupay Debit Cards &amp; UPI are reflective\r\n"
			 * + "                of non-levy of MDR\r\n" +
			 * "                by the Acquiring Banks and only represents the convenience fee payable by the Merchant to\r\n"
			 * + "                Tyche for providing Tyche Services.</li>\r\n" +
			 * "        </ul>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;text-align: center;\">\r\n" +
			 * "        <p><strong> Part II </strong> </p>\r\n" +
			 * "        <p><strong> Mode of payment to Merchant: </strong> </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"margin-top: 10px;width: 100%;float: left;\">\r\n" +
			 * "        <table id=\"table2\" class=\"agreed\" style=\"margin-left:auto;margin-right: auto;\">\r\n"
			 * + "            <tr class=\"agreed\">\r\n" +
			 * "                <th class=\"agreed\">ParticularsDetails</th>\r\n" +
			 * "                <th class=\"agreed\">Details</th>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">Bank Name</td>\r\n" +
			 * "                <td class=\"agreed\">HDFC BANK</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">Bank Account No.</td>\r\n" +
			 * "                <td class=\"agreed\">23007620000016</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">Branch Address</td>\r\n" +
			 * "                <td class=\"agreed\">Dalhousie</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">IFSC Code</td>\r\n" +
			 * "                <td class=\"agreed\">HDFC0002300</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">Account Holder Name</td>\r\n" +
			 * "                <td class=\"agreed\">Guru Nanak Public School</td>\r\n" +
			 * "            </tr>\r\n" + "        </table>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;text-align:center;\">\r\n" +
			 * "        <p><strong>SCHEDULE B</strong></p>\r\n" +
			 * "        <p><strong>BUSINESS CATEGORY AND PURPOSE</strong></p>\r\n" +
			 * "    </div>\r\n" + "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <p><strong> Tyche Services Product opted for:</strong> </p>\r\n" +
			 * "    </div>\r\n" + "    <div style=\"float:left;width: 100%;\">\r\n" +
			 * "        <span><strong>Business Category :</strong> </span>\r\n" +
			 * "        <span style=\"border-bottom: 1px dotted red;margin-left:20px;\">Education\r\n"
			 * + "        </span>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <span><strong>Business Sub-Category :</strong> </span>\r\n" +
			 * "        <span style=\"border-bottom: 1px dotted red;margin-left:20px;\">School\r\n"
			 * + "        </span>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:120px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" + "\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>39</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 90%;float: left;text-align:center;\">\r\n" +
			 * "        <p><strong>SCHEDULE C</strong></p>\r\n" + "    </div>\r\n" + "\r\n"
			 * + "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <p> Banned list of Products referred to in this Agreement is as mentioned herein below:\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>1.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Adult goods and services which includes pornography and other sexually suggestive materials\r\n"
			 * +
			 * "            (including literature, imagery and other media); escort or prostitution services;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>2.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Alcohol which includes Alcohol or alcoholic beverages such as beer, liquor, wine, or champagne;</p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>3.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Body parts which includes organs or other body parts;\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>4.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Bulk marketing tools which include email lists, software, or other products enabling\r\n"
			 * + "            unsolicited\r\n" + "            email messages (spam);\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>5.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Cable descramblers and black boxes which includes devices intended to obtain cable and\r\n"
			 * + "            satellite signals for free;\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>6.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Child pornography which includes pornographic materials involving minors;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>7.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Copyright unlocking devices which include Mod chips or other devices designed to circumvent\r\n"
			 * + "            copyright protection;\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>8.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Copyrighted media which includes unauthorized copies of books, music, movies, and other\r\n"
			 * +
			 * "            licensed or protected materials; Copyright infringing merchandise;\r\n"
			 * + "        </p>\r\n" + "\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>9.</p>\r\n" + "    </div>\r\n" + "\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Copyrighted software which includes unauthorized copies of software, video games and other\r\n"
			 * +
			 * "            licensed or protected materials, including OEM or bundled software\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:120px\">\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>40</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>10.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Products labeled as “tester,” “not for retail sale,” or “not intended for resale”;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>11.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Counterfeit and unauthorized goods which includes replicas or imitations of designer goods;\r\n"
			 * +
			 * "            items without a celebrity endorsement that would normally require such an association; fake\r\n"
			 * +
			 * "            autographs, counterfeit stamps, and other potentially unauthorized goods;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>12.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Products that have been altered to change the product’s performance, safety specifications,\r\n"
			 * + "            orindications of use;\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>13.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Drugs and drug paraphernalia which includes hallucinogenic substances, illegal drugs and\r\n"
			 * + "            drug\r\n" +
			 * "            accessories, including herbal drugs like salvia and magic mushrooms;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>14.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Drug test circumvention aids which include drug cleansing shakes, urine test additives, and\r\n"
			 * + "            related items;\r\n" + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>15.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Endangered species which includes plants, animals or other organisms (including product\r\n"
			 * + "            derivatives)in danger of extinction;\r\n" + "        </p>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>16.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Government IDs or documents which includes fake IDs, passports, diplomas, and noble titles;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>17.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Hacking and cracking materials which includes manuals, how-to guides, information, or\r\n"
			 * +
			 * "            equipment enabling illegal access to software, servers, websites, or other protected property;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>18.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Illegal goods which include materials, products, or information promoting illegal goods or\r\n"
			 * + "            enabling illegal acts;\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>19.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Miracle cures which include unsubstantiated cures, remedies or other items marketed as\r\n"
			 * + "            quick health fixes;\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" + "\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:120px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>41</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>20.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Offensive goods which include literature, products or other materials that: a) Defame\r\n"
			 * + "            orslander\r\n" +
			 * "            any person or groups of people based on race, ethnicity, national origin, religion, sex, orother\r\n"
			 * +
			 * "            factors b) Encourage or incite violent acts c) Promote intolerance or hatred;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>21.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Offensive goods, crime which includes crime scene photos or items, such as personal\r\n"
			 * + "            belongings, associated with criminals;\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>22.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Pyrotechnic devices (apart from the ones mentioned in the Restricted category), hazardous\r\n"
			 * + "            materials and radioactive materials and substances;\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>23.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Tobacco and cigarettes which includes e-cigarettes, cigars, chewing tobacco, and related\r\n"
			 * + "            products;\r\n" + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>24.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong></strong>Traffic devices which include radar detectors/jammers, license plate covers, traffic signal\r\n"
			 * + "            changers, and related products;\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>25.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong></strong>Weapons which include firearms, ammunition, knives, brass knuckles, gun parts, and other\r\n"
			 * + "            armaments;\r\n" + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>26.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Matrix sites or sites using matrix scheme approach/Ponzi/Pyramid schemes;\r\n"
			 * + "        </p>\r\n" + "\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>27.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Work-at-home information;\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>28.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Any product or service which is not in compliance with all applicable laws and regulations\r\n"
			 * +
			 * "            whether federal, state, local or international including the laws of India;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>29.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong></strong>Merchant who deals in BPO services;\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>30.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <strong></strong>Merchant who deals in surgical products on B2C model;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:90px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>42</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>31.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Merchant who deals in immigration services (only consultancy is doable);\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>32.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Merchant who deals in loose diamonds;\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>33.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Merchant who deals in guaranteed employment services;\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>34.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Religious products which are making false claims or hurting someone’s religious feelings;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;margin-top:15px\">\r\n"
			 * + "        <p>35.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-top:15px\">\r\n" +
			 * "        <p> Merchant who deals in adoption agencies;\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>36.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Merchant who deals in pawnshop;\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>37.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Merchant who deals in esoteric pages, psychic consultations;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>38.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Merchant who deals in telemarketing (Calling list, selling by phone for example travel\r\n"
			 * + "            service,overall sales);\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>39.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Merchant who deals in credit Counselling/Credit Repair Services;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>40.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Merchant who deals in get rich businesses;\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>41.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Merchant who deals in bankruptcy services;\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>42.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Merchant who deals in websites depicting violence and extreme sexual violence;\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" + "\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>43.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Bestiality\r\n" + "        </p>\r\n" + "    </div>\r\n" + "\r\n"
			 * + "    <div style=\"width: 100%;float: left;height:90px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>43</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>44.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Crypto currency or Bitcoin.\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>45.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Gaming/gambling which includes lottery tickets, sports bets, memberships/ enrolment in\r\n"
			 * + "            online gambling sites, and related content. </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>46.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Any product or service which is not in compliance with all applicable laws and regulations\r\n"
			 * +
			 * "            whether federal, state, local or international, including the laws of India.\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;text-align:center;\">\r\n" +
			 * "        <p><strong>SCHEDULE D</strong></p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <p><strong>CONFIRMATION ON ANTI-BRIBERY AND ANTI-CORRUPTION, ANTI-MONEY\r\n"
			 * +
			 * "                LAUNDERING AND EXPORT CONTROLS (“Anti-financial Crimes Laws Confirmation”)\r\n"
			 * + "            </strong></p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 100%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p> <strong>Statement and Purpose:</strong>The Merchant and its group companies are committed to operating\r\n"
			 * +
			 * "            its businesses conforming to the highestmoral and ethical standards. The Merchant has astringent\r\n"
			 * +
			 * "            code of conduct and confirms hereby that is committed to acting professionally, fairly and with\r\n"
			 * +
			 * "            integrity in all its business transactions and relationships wherever it operates. The Merchant\r\n"
			 * +
			 * "            undertakes to comply and implement in its processes all legal requirements relevant tocounter\r\n"
			 * +
			 * "            ‘bribery and corruption’, ‘money laundering’ and ‘restricted export-import transactions’\r\n"
			 * +
			 * "            applicable in the conduct of its business, the minimum requirements adopted by the US/OFAC,\r\n"
			 * +
			 * "            UK, UN and the EU in regards to anti-bribery and anti-corruption, anti-money laundering/\r\n"
			 * +
			 * "            combating the financing of terrorism and other applicable export control laws and regulations\r\n"
			 * +
			 * "            including but not limited to any restrictions on the export or import dealings with the‘SanctionedPersons’\r\n"
			 * + "            <strong>(“Anti-Financial Crimes Laws”).</strong>\r\n" +
			 * "        </p>\r\n" +
			 * "        <p> <strong>This Anti-Financial Crimes Laws Confirmation constitutes a minimum standard.</strong>The\r\n"
			 * +
			 * "            Merchant undertakes to be compliant and shall continue to abide to the relevant Anti-Financial\r\n"
			 * +
			 * "            Crimes Laws: By signing the main services agreement, Merchant acknowledges and agrees that it:\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>1.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> will comply with all applicable laws, regulations and sanctions relating to anti-bribery,\r\n"
			 * +
			 * "            anticorruption, anti-money laundering and export controls including but notlimited to the minimum\r\n"
			 * + "            requirements of the Anti-Financial Crimes Laws.\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:120px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>44</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>2.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> is prohibited from dealing with any acts which may be deemed as money laundering under any\r\n"
			 * + "            applicable law, regulations or restrictions.\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>3.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> will prohibit the entering into agreements with any person who has been identified in the\r\n"
			 * +
			 * "            sanctions list maintained by different state authorities or organizations.\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>4.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>has implemented an internal compliance programme, to ensure compliance with and detect\r\n"
			 * + "            violations of all applicable Anti-Financial Crimes Laws.\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>5.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> in the event the Merchant does not honor these commitments, the Merchant agrees that this\r\n"
			 * +
			 * "            will be considered as a material breach of the Agreement. Therefore, Tyche may immediately\r\n"
			 * + "            terminate the Agreement.\r\n" + "        </p>\r\n" +
			 * "        <p> <strong></strong>(‘Sanction Persons’ refer to those persons who are identified in the sanctions list\r\n"
			 * + "            maintainedby\r\n" +
			 * "            the US (OFAC), UN and EU)\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;text-align:center;\">\r\n" +
			 * "        <p><strong>SCHEDULE D POLICIES AND PROCEDURES</strong></p>\r\n" +
			 * "    </div>\r\n" + "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <p><strong><u>Grievances Redressal Mechanism</u></strong></p>\r\n" +
			 * "    </div>\r\n" + "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <p> <strong>Basispay customer support team is working all days 24/7.</strong></p>\r\n"
			 * + "    </div>\r\n" + "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <ul>\r\n" +
			 * "            <li>In case of grievances, customers may lodge complaint through\r\n"
			 * + "                Customer care number, Email or through\r\n" +
			 * "                Website given below. Complaints are forwarded to the concerned departments for redressal by the\r\n"
			 * + "                Customer Service head.</li>\r\n" + "        </ul>\r\n" +
			 * "    </div>\r\n" + "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Contact no: 7358084444 / <a href=\"\">Support@ basispay.in</a> / <a\r\n"
			 * + "                href=\"\">www.tychepayment.com/contacts.php</a>\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height: 130px;\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>45</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> <u>Evaluation -</u> A statement of complaints will be submitted to the Customer Service Committee of board\r\n"
			 * +
			 * "            along with detailed analysis of the individual complaints received. The complaints will be analysed</p>\r\n"
			 * +
			 * "        <p>(i) to identify customer service areas in which the complaints are frequently received.</p>\r\n"
			 * + "        <p>(ii) to identify frequent sources of complaint.</p>\r\n" +
			 * "        <p>(iii) to identify systemic deficiencies.</p>\r\n" +
			 * "        <p>(iv) for initiating appropriate action to make the grievance redressal mechanism more effective.\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-top: 20px;\">\r\n" +
			 * "        <p> <u>Escalation -</u> The complaint lodged by a customer is first assigned to the customer care for redressal.\r\n"
			 * + "            If the\r\n" +
			 * "            complaint is not redressed within 24 Hours or if the customer is not satisfied with the reply, he may\r\n"
			 * +
			 * "            escalate the complaint to the 2nd level after 24hours. If the complaint is not redressed within the next 48\r\n"
			 * +
			 * "            Hours (2) days, the customer may further escalate the complaint to Nodal officer (Details will be updated in\r\n"
			 * + "            our website periodically)</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;margin-top: 15px;\">\r\n" +
			 * "        <u><strong>Chargeback or dispute resolution mechanism:</strong></u>\r\n"
			 * + "    </div>\r\n" + "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>Chargeback or dispute can be due to fraudulent activity, but also happen when a customer does not receive\r\n"
			 * +
			 * "            the products/services they ordered or when they are not satisfied with services. Customer will raise a\r\n"
			 * +
			 * "            complaint with his/her Payment Service Provider (Banks in case of card transaction, Wallet Provider in\r\n"
			 * + "            case of Wallet transaction etc)</p>\r\n" + "        <ul>\r\n"
			 * +
			 * "            <li style=\"margin-top: 10px;\"> We shall receive complaint details from Customers Payment Service Provider\r\n"
			 * + "                via e-Mail or through\r\n" +
			 * "                respective dashboards along with Target date/TAT</li>\r\n" +
			 * "            <li style=\"margin-top: 10px;\">Amount to the extent of Complaint will be debited from us by Payment Service\r\n"
			 * + "                Provider before/after\r\n" +
			 * "                sending us the Complaint details</li>\r\n" +
			 * "            <li style=\"margin-top: 10px;\"> TAT (Turn Around Time) will differ from each Payment Service Provider and\r\n"
			 * + "                will range from 3-10\r\n" +
			 * "                Days.</li>\r\n" +
			 * "            <li style=\"margin-top: 15px;\"> Once Complaint details are received, we shall update the details at our\r\n"
			 * + "                dashboard and a debit entry\r\n" +
			 * "                will be created in system and in next settlement cycle this amount shall be debited from the amount\r\n"
			 * + "                to be settled to merchant.</li>\r\n" + "        </ul>\r\n"
			 * + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height: 100px;\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>46</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" + "        <ul>\r\n" +
			 * "            <li>After updating details at our dashboard, a mail with following details shall be shared to merchants,\r\n"
			 * +
			 * "                requesting them to share the necessary documents to contest/defend the complaint</li>\r\n"
			 * + "            <ul>\r\n" + "\r\n" +
			 * "                <li><strong style=\"font-size: 23px;\"> &#10004;</strong>Unique Transaction Reference Number</li>\r\n"
			 * +
			 * "                <li><strong style=\"font-size: 23px;\">&#10003;</strong>Transaction date</li>\r\n"
			 * +
			 * "                <li><strong style=\"font-size: 23px;\">&#10003;</strong>Transaction amount</li>\r\n"
			 * +
			 * "                <li><strong style=\"font-size: 23px;\">&#10003;</strong>Complaint amount</li>\r\n"
			 * +
			 * "                <li><strong style=\"font-size: 23px;\">&#10003;</strong>Target Date/TAT</li>\r\n"
			 * + "            </ul>\r\n" +
			 * "            <li> A series of Mails or calls shall be made to follow up with merchant to get the required\r\n"
			 * + "                details/documents.</li>\r\n" +
			 * "            <li style=\"margin-top: 10px;\"> Once the details/documents are received from merchant end, same shall be\r\n"
			 * + "                shared over an e-Mail or\r\n" +
			 * "                upload the same at Dashboard of Payment Service Provider and request Payment service provider to\r\n"
			 * + "                contest the complaint</li>\r\n" +
			 * "            <li style=\"margin-top: 10px;\">Once details are shared, Payment Service Provider will take 90-180 days to\r\n"
			 * + "                close the complaint.</li>\r\n" +
			 * "            <li style=\"margin-top: 10px;\"> Complaint might be closed in favour of merchant or in favour of customer and\r\n"
			 * + "                it is at whole discretion\r\n" +
			 * "                of Payment Service Provider. And same will informed to us over an e-Mail or through dashboard.</li>\r\n"
			 * +
			 * "            <li style=\"margin-top: 10px;\"> If the Compliant is closed in favour of Merchant, the amount will be credited\r\n"
			 * + "                back to us.</li>\r\n" + "\r\n" +
			 * "            <li style=\"margin-top: 10px;\"> Once we receive details of closure of complaint and amount is Credited to our\r\n"
			 * + "                account, we shall create\r\n" +
			 * "                a Credit entry at our dashboard and the amount will be credited to merchant account in next\r\n"
			 * + "                settlement cycle.</li>\r\n" +
			 * "            <li style=\"margin-top: 10px;\"> We shall communicate the same to merchants</li>\r\n"
			 * +
			 * "            <li style=\"margin-top: 10px;\"> In very rare case Merchants agree to Refund the amount to customer and same\r\n"
			 * + "                shall be communicated\r\n" +
			 * "                by us to Payment Service provider.</li>\r\n" +
			 * "        </ul>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p><strong>As per our Company Policies, if no transactions are happening in a merchant account, company\r\n"
			 * +
			 * "                shall Hold some of Merchant’s pending settlement funds to cover up the possible future\r\n"
			 * + "                Chargeback cases.</strong>\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:120px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>47</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <p><strong><u>Refund process:</u></strong></p>\r\n" +
			 * "    </div>\r\n" + "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p>\r\n" +
			 * "            Merchant has been be provided with an option to initiate the refund for a particular transaction &amp;\r\n"
			 * +
			 * "            merchant either can initiate Full refund / partial refund based on their requirements.\r\n"
			 * +
			 * "            Based on the funds availability our system will accept the refund request &amp; send the same to\r\n"
			 * + "            bank/service\r\n" +
			 * "            provider. And we reflect the bank reference number in merchant dashboard which will be helpful for the\r\n"
			 * +
			 * "            merchant to share the same with customer for tracking purpose.\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" + "        <ul>\r\n" +
			 * "            <li>Merchant will refund the amount in following cases</li>\r\n"
			 * + "            <ul>\r\n" +
			 * "                <li style=\"margin-top: 6px;\"> If Services/goods are not provided</li>\r\n"
			 * +
			 * "                <li style=\"margin-top: 6px;\"> If Services/goods are partially provided</li>\r\n"
			 * +
			 * "                <li style=\"margin-top: 6px;\"> If the Customer has done payment/transaction more than once for single\r\n"
			 * + "                    Service</li>\r\n" +
			 * "                <li style=\"margin-top: 6px;\"> If the Payment turned success at later point of time in Reconciliation,\r\n"
			 * + "                    but merchant collected\r\n" +
			 * "                    amount through some other way before the payment turned success.</li>\r\n"
			 * + "            </ul>\r\n" +
			 * "            <li style=\"margin-top: 6px;\"> Merchants shall be provided with option to Refund either through API or\r\n"
			 * + "                through Merchant\r\n" +
			 * "                Dashboard</li>\r\n" +
			 * "            <li style=\"margin-top: 6px;\"> Merchants shall be provided with option to initiate Full/Partial Refund</li>\r\n"
			 * +
			 * "            <li style=\"margin-top: 6px;\"> At the time of Request by merchant, System shall check for Fund Availability\r\n"
			 * + "                for that specific\r\n" +
			 * "                merchant. If Funds are available Refund request shall get submitted otherwise Refund Request shall\r\n"
			 * + "                get failed.</li>\r\n" +
			 * "            <li style=\"margin-top: 6px;\"> Once we receive the Refund Request either through API or through Merchant\r\n"
			 * + "                dashboard, a Debit\r\n" +
			 * "                Entry will be created in system and in next settlement cycle same shall be debited from amount to\r\n"
			 * + "                be settled to merchant</li>\r\n" +
			 * "            <li style=\"margin-top: 10px;\"> We shall request the Payment Service Provider to process the refund through\r\n"
			 * + "                API</li>\r\n" +
			 * "            <li style=\"margin-top: 10px;\"> Refund Request to Payment Service Provider shall be done at Regular\r\n"
			 * + "                Intervals, like Hourly/30 mins\r\n" +
			 * "                once.</li>\r\n" +
			 * "            <li style=\"margin-top: 6px;\"> Payment Service Provider will check the fund availability and if funds are\r\n"
			 * + "                available, will accept the\r\n" +
			 * "                Refund request and the amount will be debited from us </li>\r\n"
			 * +
			 * "            <li style=\"margin-top: 6px;\"> The Refund amount will reach the intended Customer anywhere between 1-10\r\n"
			 * + "                Working days</li>\r\n" +
			 * "            <li style=\"margin-top: 6px;\"> Merchant can’t initiate Refund for the transaction happened before 180 days,\r\n"
			 * + "                as no Payment Service\r\n" +
			 * "                Provider accepts the same.</li>\r\n" + "        </ul>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:100px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>48</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 100%;float: left;text-align: center;\">\r\n" +
			 * "        <p><strong><u>TAT for Complaints:</u></strong></p>\r\n" +
			 * "    </div>\r\n" + "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <table id=\"table2\" class=\"agreed\" style=\"text-align: center;margin-left: auto;margin-right: auto;\">\r\n"
			 * + "            <tr class=\"agreed\">\r\n" +
			 * "                <th class=\"agreed\">S No</th>\r\n" +
			 * "                <th class=\"agreed\">Queries</th>\r\n" +
			 * "                <th class=\"agreed\">TAT</th>\r\n" + "            </tr>\r\n"
			 * + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">1</td>\r\n" +
			 * "                <td class=\"agreed\">General support &amp; Transaction\r\n"
			 * + "                    related queries</td>\r\n" +
			 * "                <td class=\"agreed\">16 Working Hours</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">2</td>\r\n" +
			 * "                <td class=\"agreed\">Risk issues, Chargeback and\r\n" +
			 * "                    disputes</td>\r\n" +
			 * "                <td class=\"agreed\">32 Working hours</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">3</td>\r\n" +
			 * "                <td class=\"agreed\">Merchant Fraud</td>\r\n" +
			 * "                <td class=\"agreed\">32 Working hours</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">4</td>\r\n" +
			 * "                <td class=\"agreed\">Sales &amp; Onboarding</td>\r\n" +
			 * "                <td class=\"agreed\">32 Working hours</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">5</td>\r\n" +
			 * "                <td class=\"agreed\">Documentation Compliance</td>\r\n" +
			 * "                <td class=\"agreed\">16 Working Hours</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">6</td>\r\n" +
			 * "                <td class=\"agreed\">Technical Queries</td>\r\n" +
			 * "                <td class=\"agreed\">16 Working Hours</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">7</td>\r\n" +
			 * "                <td class=\"agreed\">Rejections &amp; Deactivations</td>\r\n"
			 * + "                <td class=\"agreed\">16 Working Hours</td>\r\n" +
			 * "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">8</td>\r\n" +
			 * "                <td class=\"agreed\">Settlement &amp; Financial issues</td>\r\n"
			 * + "                <td class=\"agreed\">32 Working hours</td>\r\n" +
			 * "            </tr>\r\n" + "        </table>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <p><strong><u>Other Notes:</u></strong></p>\r\n" + "    </div>\r\n"
			 * + "    <div style=\"width: 90%;float: left;\">\r\n" + "        <ul>\r\n" +
			 * "            <li> Tyche Payment Solutions Pvt Ltd is the aggregator having the in-principle aggregator license and\r\n"
			 * +
			 * "                the products are marketed &amp; promoted under the Brand name of Basispay.</li>\r\n"
			 * +
			 * "            <li style=\"margin-top: 6px;\"> All Rates are exclusive of GST. Transactions charges are fixed as per RBI\r\n"
			 * + "                regulations, and the same\r\n" +
			 * "                is subjected to change any time as per the regulatory authorities and scheme orders.</li>\r\n"
			 * +
			 * "            <li style=\"margin-top: 6px;\"> Setup fees once paid will not be refunded except for cases rejected by\r\n"
			 * + "                Basispay due to KYC/Risk\r\n" +
			 * "                validation which will be refunded after Validation charges of Rs.150+GST.</li>\r\n"
			 * + "            <li style=\"margin-top: 6px;\">\r\n" +
			 * "                KYC and Risk validation can also be reviewed anytime by Basispay team even after\r\n"
			 * +
			 * "                commencement of the services and will be re-validated at least once in every year. Any charges\r\n"
			 * +
			 * "                pertain to this will be collected from the merchant with prior notification. Settlement will not be\r\n"
			 * +
			 * "                released in case of chargebacks, disputes or any discrepancies in KYC validation.\r\n"
			 * + "            </li>\r\n" + "\r\n" +
			 * "            <li style=\"margin-top: 6px;\">\r\n" +
			 * "                UPI &amp; Rupay cards include NIL MDR but Convenience fee / Service provider charges on all\r\n"
			 * +
			 * "                transactions for the additional services offered by Basispay (Instant notifications through the\r\n"
			 * +
			 * "                mobile application, Multiple user interfaces, Instant Invoice generation, Inbuild-SMS &amp; Email\r\n"
			 * +
			 * "                engine, connected with 3rd party applications, Billing software in SAAS model, Phone &amp; Email\r\n"
			 * + "                Support)\r\n" + "            </li>\r\n" +
			 * "            <li style=\"margin-top: 6px;\"> Signed hardcopies of KYC, Applications (Merchant &#38;#38; Authorized person)\r\n"
			 * + "                along with Merchant\r\n" +
			 * "                Agreement is mandatory.</li>\r\n" + "\r\n" +
			 * "        </ul>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height: 110px;\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>49</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <p><strong><u>Instructions:</u></strong></p>\r\n" + "    </div>\r\n"
			 * + "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>1.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> This agreement to be filled in a valid Rs.100 stamp paper.\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>2.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Red colored fields must be filled by the merchant\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>3.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Also note, required fields to be filled, printed then only need to be signed.\r\n"
			 * + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 6%;float: left;margin-left: 15px;\">\r\n" +
			 * "        <p>4.</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;\">\r\n" +
			 * "        <p> Soft copy can be received at the time of onboarding, hard copies should reach us within 7 working\r\n"
			 * + "            days from the date of onboarding.\r\n" + "        </p>\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"width: 90%;float: left;margin-top: 10px;\">\r\n" +
			 * "        <p>\r\n" +
			 * "        <p style=\"float: left; width: 10px;border: 1px solid black;height:10px;margin-top:4px\"></p> &nbsp; I /We accept\r\n"
			 * +
			 * "        that all Information given to Tyche Payment Solutions Pvt. Ltd.\r\n"
			 * +
			 * "        under the brand name Basispay is correct and accurate. We would like to have an exclusive acquiring\r\n"
			 * + "        relationship. I/We\r\n" +
			 * "        agree and accept that Tyche Payment Solutions Pvt. Ltd. shall at its sole discretion, may reject/accept\r\n"
			 * +
			 * "        my application at any processing stage. Further I/We understand and agree that use of TychePayment\r\n"
			 * +
			 * "        Solutions Pvt. Ltd. shall be deemed to be unconditional and irrevocable acceptance of the terms and\r\n"
			 * + "        Conditions mentioned in the attached agreement.\r\n" +
			 * "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\" width: 30%;float: left;margin-left: 15px;margin-top: 100px;\">\r\n"
			 * + "        <p><strong>Date:</strong> 11/2/2023</p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 60%;float: left;margin-top: 100px;text-align: center;\">\r\n"
			 * + "        <p>Saravanan chandrasekaran</p>\r\n" +
			 * "        <p><strong> Authorized person’s Signature\r\n" +
			 * "            </strong>\r\n" + "        </p>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:180px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>50</h3>\r\n" + "    </div>\r\n" +
			 * "    <table style=\"width: 100%;float: left;\">\r\n" + "        <tr>\r\n" +
			 * "            <td>\r\n" +
			 * "                <div><strong> version 1.9/072023</strong> </div>\r\n" +
			 * "            </td>\r\n" +
			 * "            <td style=\"padding-left: 10cm;\"> <img src=\"resources/image/fachecklogo.png\" alt=\"BasispayLogo\"\r\n"
			 * +
			 * "                    style=\"width: 170px;height: 80px;object-fit: cover;\" /></td>\r\n"
			 * + "        </tr>\r\n" + "    </table>\r\n" +
			 * "    <div style=\"width: 100%;float: left;text-align: center;\">\r\n" +
			 * "        <p><strong><u>COMMERCIALS REVISION (Office Use Only):</u></strong></p>\r\n"
			 * + "    </div>\r\n" + "    <div style=\"width: 100%;float: left;\">\r\n" +
			 * "        <table id=\"table2\" class=\"agreed\" style=\"margin-left: auto;margin-right: auto;\">\r\n"
			 * + "            <tr class=\"agreed\">\r\n" +
			 * "                <th class=\"agreed\">Particulars</th>\r\n" +
			 * "                <th class=\"agreed\">Revision-1 (Exclusive of Applicable Taxes)</th>\r\n"
			 * +
			 * "                <th class=\"agreed\">Revision-2 (Exclusive of Applicable Taxes)</th>\r\n"
			 * + "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\"><strong> A. System Integration Fee/Set up Fee (One\r\n"
			 * +
			 * "                        time payable upfront at the time of Signing of\r\n"
			 * +
			 * "                        the Services Agreement, non-refundable.)*</strong></td>\r\n"
			 * + "\r\n" + "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\"><strong>B. AMC/MMC*</strong></td>\r\n"
			 * + "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\"><strong>C. Security Deposit (If applicable)</strong></td>\r\n"
			 * + "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\"><strong>D. Service Fee*(Per Transaction)</strong></td>\r\n"
			 * + "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">1) Net Banking\r\n" +
			 * "                    SBI/Axis/ICICI/HDFC -\r\n" +
			 * "                    Others -</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">2) Credit Card (Visa/Master)</td>\r\n"
			 * + "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">3) Debit Card </td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\"> Rupay Debit Card</td>\r\n" +
			 * "                <td class=\"agreed\">&lt; 2000 &nbsp;&nbsp;&nbsp; &gt;2000</td>\r\n"
			 * +
			 * "                <td class=\"agreed\">&lt; 2000 &nbsp;&nbsp;&nbsp; &gt;2000</td>\r\n"
			 * + "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">Other Debit Cards</td>\r\n" +
			 * "                <td class=\"agreed\">&lt; 2000 &nbsp;&nbsp;&nbsp; &gt;2000</td>\r\n"
			 * +
			 * "                <td class=\"agreed\">&lt; 2000 &nbsp;&nbsp;&nbsp; &gt;2000</td>\r\n"
			 * + "            </tr>\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">4) e-Collect</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">5) Disbursement API</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "\r\n" + "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">6) International Card (specify)</td>\r\n"
			 * + "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">7) Amex Card</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">8) Dinners Card</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">9) Corporate/Commercial Card</td>\r\n"
			 * + "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">10) Prepaid card</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">11) Wallets (Except ITZ Cash)</td>\r\n"
			 * + "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">12)UPI</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">13) Dynamic QR</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">14) Settlement Timeframe</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">15) VAS (mention if any)</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\">16) Others Please specify</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\"> Volume Committed</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\"> Effective Date</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\"> Approver Name</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\">\r\n" +
			 * "                <td class=\"agreed\"> Employee ID</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "            <tr class=\"agreed\" style=\"height:40px\">\r\n" +
			 * "                <td class=\"agreed\"> ApproverSignature</td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" +
			 * "                <td class=\"agreed\"></td>\r\n" + "            </tr>\r\n" +
			 * "        </table>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"float: left;width: 100%;margin-top: 10px;\">\r\n" +
			 * "        <table>\r\n" + "            <tr>\r\n" + "                <td>\r\n" +
			 * "                    <table>\r\n" + "                        <tr>\r\n" +
			 * "                            <td><strong>MID</strong></td>\r\n" +
			 * "                        </tr>\r\n" + "                    </table>\r\n" +
			 * "                </td>\r\n" + "                <td>\r\n" +
			 * "                    <table style=\"border: 1px solid;width: 200px;height: 20px;\">\r\n"
			 * + "                        <tr>\r\n" +
			 * "                            <td></td>\r\n" +
			 * "                        </tr>\r\n" + "                    </table>\r\n" +
			 * "                </td>\r\n" + "                <td>\r\n" +
			 * "                    <table style=\"width: 50px;height: 20px;\">\r\n" +
			 * "                        <tr>\r\n" +
			 * "                            <td></td>\r\n" +
			 * "                        </tr>\r\n" + "                    </table>\r\n" +
			 * "                </td>\r\n" + "                <td>\r\n" +
			 * "                    <table>\r\n" + "                        <tr>\r\n" +
			 * "                            <td><strong>Remarks :</strong> </td>\r\n" +
			 * "                        </tr>\r\n" + "                    </table>\r\n" +
			 * "                </td>\r\n" + "                <td>\r\n" +
			 * "                    <table style=\"border: 1px solid;width: 250px;height: 20px;\">\r\n"
			 * + "                        <tr>\r\n" +
			 * "                            <td></td>\r\n" +
			 * "                        </tr>\r\n" + "                    </table>\r\n" +
			 * "                </td>\r\n" + "            </tr>\r\n" +
			 * "        </table>\r\n" + "    </div>\r\n" +
			 * "    <div style=\"width: 100%;float: left;height:30px\">\r\n" + "\r\n" +
			 * "    </div>\r\n" +
			 * "    <div style=\"text-align: center;float: left;width: 100%;\">\r\n" +
			 * "        <h3>51</h3>\r\n" + "    </div>\r\n" + "</body>\r\n" + "\r\n" +
			 * "</html>";
			 */

			String randomAlphaNumericString = FileUtils.getRandomAlphaNumericString();

			String generatedString = randomAlphaNumericString + ".pdf";

			String fileName = con + File.separator + "agreecheck/" + StringUtils.cleanPath(generatedString);

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(fileName);

				ITextRenderer renderer = new ITextRenderer();
				renderer.setDocumentFromString(html);
				renderer.layout();
				renderer.createPDF(fos, true);

			} finally {
				if (fos != null) {
					try {
						fos.close();

					} catch (IOException e) {
						e.printStackTrace();
//						logger.info("IOException", e);
					}
				}
			}

			structure.setMessage("PDF SAVED");
			structure.setData(randomAlphaNumericString);

			return structure;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("PDF writing error: " + e.getMessage());
		}
	}

	@Override
	public ResponseStructure base64ToPdf(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			String name = model.getSource();
			int count = model.getUserId();
			int trimSize = 3;
			String trimmedString = null;
			if (trimSize <= name.length()) {
				trimmedString = name.substring(0, trimSize);
			}
			String counterPart = String.format("%08d", count);
			String uniqueKey = trimmedString + counterPart;
			System.out.println("UNIQUE : " + uniqueKey);

//			System.out.println("START : ");
//			
//			String base64String = model.getSource();
//			
//			byte[] pdfBytes = Base64.getDecoder().decode(base64String);
//			
//			String outputPath ="C:/Users/DELL/Documents/Middleware Table dumps/output.pdf";
//			
//			FileOutputStream fos= new FileOutputStream(outputPath);
//			
//			fos.write(pdfBytes);
//			
//			structure.setFlag(1);
//			structure.setMessage("PDF FILE CREATED");

		} catch (Exception e) {
			e.printStackTrace();

			structure.setFlag(1);
			structure.setMessage("PDF FILE CREATION FAILED");
			throw new RuntimeException("PDF writing error: " + e.getMessage());
		}
		return null;
	}

	@Override
	public ResponseStructure applicationId(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

//	     String originalString = model.getSource();
//	     String withoutSpace = originalString.replaceAll("\\s", "");

//	     structure.setData(FileUtils.applicationIdGeneration(model.getSource()));

			String numbers = "0123456789";
			String letters = "abcdefghijklmnopqrstuvwxyz";
			int length = 32;

			StringBuilder randomString = new StringBuilder(length);
			SecureRandom secureRandom = new SecureRandom();

			for (int i = 0; i < 3; i++) {
				int randomIndex = secureRandom.nextInt(numbers.length());
				randomString.append(numbers.charAt(randomIndex));
			}

			for (int i = 3; i < length - 3; i++) {
				int randomIndex = secureRandom.nextInt(numbers.length() + letters.length());
				if (randomIndex < numbers.length()) {
					// Append a number
					randomString.append(numbers.charAt(randomIndex));
				} else {
					// Append a letter
					randomString.append(letters.charAt(randomIndex - numbers.length()));
				}
			}

			for (int i = length - 3; i < length; i++) {
				int randomIndex = secureRandom.nextInt(letters.length());
				randomString.append(letters.charAt(randomIndex));
			}

			structure.setData(randomString.toString());

		} catch (Exception e) {
			e.printStackTrace();
			structure.setFlag(1);
			structure.setMessage("ERROR");
		}

		return structure;
	}

	@Override
	public ResponseStructure aadhaarOcr(File file, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();

		try {

			String token = AppConstants.SUREPASS_TOKEN;

			String filePath = "C:/Users/DELL/Pictures/Aadhar/SekarAadhar.JPG";
			String fileInProjectPath = "src/main/webapp/WEB-INF/ocrcheck/SekarAadhar.JPG";
			String url = "https://sandbox.surepass.io/api/v1/ocr/aadhaar";

			MediaType mediaTypePng = MediaType.parse("image/png");
			OkHttpClient client = new OkHttpClient();

			RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
					.addFormDataPart("image", "image.png", RequestBody.create(mediaTypePng, file)).build();

			Headers headers = new Headers.Builder().add("Authorization", token)
					.add("Content-Type", "multipart/form-data") // Or adjust content type as per requirement
					.build();

			Request request = new Request.Builder().url(url).headers(headers) // Adding headers to the request
					.post(requestBody).build();

//			client.newCall(request).enqueue(new Callback() {
//				@Override
//				public void onFailure(Call call, IOException e) {
//					e.printStackTrace();
//				}
//
//				@Override
//				public void onResponse(Call call, Response response) throws IOException {
//					if (!response.isSuccessful()) {
//						throw new IOException("Unexpected code " + response);
//					} else {
//						// Handle successful response
//						System.out.println(response.body().string());
//					}
//				}
//			});

		} catch (Exception e) {
			e.printStackTrace();
			structure.setData(e);
		}

		return structure;
	}

	@Override
	public ResponseStructure thymeLeaf() {
		ResponseStructure structure = new ResponseStructure();

		try {

			TemplateEngine templateEngine = new TemplateEngine();

			ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
			templateResolver.setPrefix("template-engines/");
			templateResolver.setSuffix(".html");
			templateResolver.setTemplateMode("HTML");
			templateResolver.setCacheable(false);

			templateEngine.setTemplateResolver(templateResolver);
			Context contextThyme = new Context();

			String amounString = AmountToWords.convertAmountToWords(222);

			contextThyme.setVariable("receiptNo", "1234567");
			contextThyme.setVariable("invoiceNumber", "1234567");
			contextThyme.setVariable("receiptIssuanceDate", LocalDate.now());
			contextThyme.setVariable("transactionType", "Prepaid");
			contextThyme.setVariable("transactionRefefrenceNo", "Quwerty2345");
			contextThyme.setVariable("paymentType", "Cash");
			contextThyme.setVariable("paymentDate", "10-02-2024");
			contextThyme.setVariable("paidBy", "Abhi");
			contextThyme.setVariable("amount", "100.0");
			contextThyme.setVariable("sgst", "9.0");
			contextThyme.setVariable("cgst", "9.0");
			contextThyme.setVariable("igst", "9.0");
			contextThyme.setVariable("convenienceFee", "10.0");
			contextThyme.setVariable("convSgst", "2.0");
			contextThyme.setVariable("convCgst", "2.0");
			contextThyme.setVariable("convIgst", "2.0");
			contextThyme.setVariable("totalAmount", "118.0");
			contextThyme.setVariable("totalAmountInWords", amounString);

			ITextRenderer renderer = new ITextRenderer();
			// renderer.getFontResolver().addFont("resources/font/Latha.ttf",
			// BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

			String html = templateEngine.process("renewablefeereceipt", contextThyme);

			String generatedString = "Z-Rec" + FileUtils.getRandomOTPnumber(5) + ".pdf";
			System.err.println("NAME : " + generatedString);

			Path con = Paths.get(context.getRealPath("/WEB-INF/"));

			File fileName = new File(con + File.separator + "/agreecheck/" + StringUtils.cleanPath(generatedString));

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(fileName);

//				ITextRenderer renderer = new ITextRenderer();
				renderer.setDocumentFromString(html);
				renderer.layout();
				renderer.createPDF(fos, true);

			} finally {
				if (fos != null) {
					try {
						fos.close();

					} catch (IOException e) {
					}
				}
			}

			structure.setData("Done");

		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getLocalizedMessage());
		}
		return structure;
	}

	@Override
	public ResponseStructure jsonObjReturn() {

		ResponseStructure structure = new ResponseStructure();

		try {

			TechnicalModel model = new TechnicalModel();
			model.setApiLink("Abi");
			model.setIdPrice(200);

			ObjectMapper mapper = new ObjectMapper();
//			ObjectNode obj = mapper.createObjectNode();

//			JsonNode node =JsonNodeCo

			String json = mapper.writeValueAsString(model);

			JSONObject obj = new JSONObject(json);
			obj.put("aa", "aa");

			structure.setData(obj);
//			structure.setJsonNode();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return structure;
	}

	@Override
	public ResponseStructure thymeLeafTamil() {
		ResponseStructure structure = new ResponseStructure();

		try {

			TemplateEngine templateEngine = new TemplateEngine();

			ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
			templateResolver.setPrefix("template-engines/");
			templateResolver.setSuffix(".html");
			templateResolver.setTemplateMode("HTML");
			templateResolver.setCacheable(false);

			templateEngine.setTemplateResolver(templateResolver);
			Context contextThyme = new Context();

			String imageUrl = "src/main/webapp/WEB-INF/adminprofilepictures/49/FYHKV7IIIW07202023113853.jpg";
			contextThyme.setVariable("imageUrl", imageUrl);

			ITextRenderer renderer = new ITextRenderer();
			renderer.getFontResolver().addFont("resources/font/Latha.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);

			String html = templateEngine.process("memberformtamil.html", contextThyme);

			String generatedString = FileUtils.getRandomString() + ".pdf";
			System.err.println("NAME : " + generatedString);

			Path con = Paths.get(context.getRealPath("/WEB-INF/"));

			File fileName = new File(con + File.separator + "/agreement/" + StringUtils.cleanPath(generatedString));

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(fileName);

//				ITextRenderer renderer = new ITextRenderer();
				renderer.setDocumentFromString(html);
				renderer.layout();
				renderer.createPDF(fos, true);

			} finally {
				if (fos != null) {
					try {
						fos.close();

					} catch (IOException e) {
					}
				}
			}

			structure.setData("Done");

		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getLocalizedMessage());
		}
		return structure;
	}

	@Override
	public ResponseStructure thymeLeafTamilItextCheck() {
		ResponseStructure structure = new ResponseStructure();

		try {

			TemplateEngine engine = new TemplateEngine();

			Context contextThyme = new Context();
			contextThyme.setVariable("a", "எது");

			String htmlContent = engine.process("memberformtamil.html", contextThyme);

			ITextRenderer renderer = new ITextRenderer();
			renderer.getFontResolver().addFont("resources/font/Latha.ttf", true);

			String generatedString = FileUtils.getRandomString() + ".pdf";

			System.err.println("NM : " + generatedString);

			String path = "WEB-INF/agreement/";

			try (OutputStream outputStream = new FileOutputStream(generatedString)) {

				renderer.setDocumentFromString(htmlContent);
				renderer.layout();
				renderer.createPDF(outputStream);
			}

			System.err.println("PDF GENERATED @ + " + path);

			structure.setMessage(generatedString);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getLocalizedMessage());
		}

		return structure;
	}

	@Override
	public ResponseStructure exceptionCheck() {

		ResponseStructure structure = new ResponseStructure();

		try {

			throw new ArithmeticException("Person is not eligible to vote");

		} catch (Exception e) {

			System.err.println("MESSAGE  : " + e.getMessage());
			System.err.println("CAUSE  : " + e.getCause());
			System.err.println("toString  : " + e.toString());
			System.err.println("LOCAL MESSAGE  : " + e.getLocalizedMessage());
			System.err.println("CLASS  : " + e.getClass());

			StackTraceElement[] stackTraceElements = e.getStackTrace();
			if (stackTraceElements.length > 0) {
				System.err.println("STK TRACE  LN: " + stackTraceElements[0].getLineNumber());
				System.err.println("STK TRACE  CN: " + stackTraceElements[0].getClassName());
				System.err.println("STK TRACE  CLN: " + stackTraceElements[0].getClassLoaderName());
				System.err.println("STK TRACE  MN: " + stackTraceElements[0].getMethodName());
				System.err.println("STK TRACE  FN: " + stackTraceElements[0].getFileName());
				System.err.println("STK TRACE  MN: " + stackTraceElements[0].getModuleName());
				System.err.println("STK TRACE  MV: " + stackTraceElements[0].getModuleVersion());
			}
		}

		return structure;
	}

	@Override
	public ResponseStructure byteImage(MultipartFile image) {
		ResponseStructure structure = new ResponseStructure();

		try {

			byte[] bytes = image.getBytes();

			TechnicalModel tech = new TechnicalModel();

			tech.setApiLink("");
			tech.setIdPrice(0);
			tech.setImageData(bytes);
			tech.setImagePrice(0);
			tech.setReqDate(new Date());
			tech.setStatus(false);

			techRepository.save(tech);

			structure.setData(tech);
			structure.setMessage("Success");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return structure;
	}

	@Override
	public ResponseStructure amountToWords(int amount) {
		ResponseStructure structure = new ResponseStructure();

		try {

			String amountInWords = AmountToWords.convertAmountToWords(amount);

			structure.setData(amountInWords);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return structure;
	}

	@Override
	public ResponseStructure mailTemplateCheck(int id) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<TransactionDto> trans = transactionRepository.findByTrancsactionId(id);

			TransactionDto transactionDto = trans.get();

			Optional<PrepaidPayment> opt = prepaidRepository.findById(25);
			PrepaidPayment prep = opt.get();

			System.err.println(prep.getPrepaidId());

			boolean emailSent = emailService.prepaidPaymentSuccessMail("abhishek.p@basispay.in", transactionDto, prep);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage("ERROR");
		}

		return structure;
	}

	@Override
	public ResponseStructure mailTemplateCheckPostpaid(int id) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<TransactionDto> trans = transactionRepository.findByTrancsactionId(id);

			TransactionDto transactionDto = trans.get();

			PostpaidPayment postpaid = postpaidRepository.findByPayId(transactionDto.getPayid().getPayid());

			// boolean emailSent =
			// emailService.postpaidPaymentSuccessMail("abhishek.p@basispay.in",transactionDto,postpaid);

			boolean emailSent = emailService.sendMonthlyReminderMail(transactionDto.getEntity(), postpaid, "JUNE",
					LocalDate.now().toString());

		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage("ERROR");
		}

		return structure;
	}

	@Override
	public ResponseStructure invoiceCheck() {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<TransactionDto> trans = transactionRepository.findByTrancsactionId(12);

			TransactionDto transactionDto = trans.get();

			PostpaidPayment postpaid = postpaidRepository.findById(1).get();
			PrepaidPayment prepaid = prepaidRepository.findById(1).get();

			Path con = Paths.get(context.getRealPath("/WEB-INF/"));

			String invoice = invoiceGenerate.postpaidInvoiceCgst(con, transactionDto, postpaid);

			System.err.println(invoice);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage("ERROR");
		}

		return structure;
	}

	@Override
	public ResponseStructure jsonView(int id) {

		ResponseStructure structure = new ResponseStructure();
		try {

			TechnicalModel transactionDto = techRepository.findById(id).get();

			structure.setData(transactionDto);
			structure.setMessage("Success");

		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage("ERROR");
		}

		return structure;
	}

	@Override
	public ResponseStructure getPublicIpAddress() {
		ResponseStructure structure = new ResponseStructure();

		try {

			// URL to get the public IP address
			String ipServiceUrl = "https://api.ipify.org?format=json";

			// Create a connection to the URL
			HttpURLConnection ipConnection = (HttpURLConnection) new URL(ipServiceUrl).openConnection();
			ipConnection.setRequestMethod("GET");

			// Check the response code
			int ipResponseCode = ipConnection.getResponseCode();

			if (ipResponseCode == 200) {
				// Read the response from the input stream
				BufferedReader ipIn = new BufferedReader(new InputStreamReader(ipConnection.getInputStream()));
				StringBuilder ipResponse = new StringBuilder();
				String ipInputLine;

				while ((ipInputLine = ipIn.readLine()) != null) {
					ipResponse.append(ipInputLine);
				}
				ipIn.close();

				// Parse the JSON response
				JSONObject ipJsonResponse = new JSONObject(ipResponse.toString());
				String publicIPAddress = ipJsonResponse.getString("ip");
				System.out.println("Public IP Address: " + publicIPAddress);

				structure.setData(publicIPAddress);
				structure.setFlag(1);
				structure.setMessage(AppConstants.SUCCESS);

			} else {

				structure.setData("Unable to get Public IP");
				structure.setFlag(2);
				structure.setMessage(AppConstants.SUCCESS);
				System.out.println("Error: Unable to get public IP address");
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setFlag(4);
			structure.setMessage(e.getLocalizedMessage());
			structure.setStatusCode(HttpStatus.OK.INTERNAL_SERVER_ERROR.value());
			;
		}

		return structure;
	}

	@Override
	public ResponseStructure getGps() {

		ResponseStructure structure = new ResponseStructure();

		try {

			// Get the local IP address
			InetAddress inetAddress = InetAddress.getLocalHost();
			String ipAddress = inetAddress.getHostAddress();
			System.out.println("Local IP Address: " + ipAddress);

			// URL for ip-api.com with the local IP address
			String url = "http://ip-api.com/json/" + "157.51.76.135";// "136.185.16.33"

			// Create a connection to the URL
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");

			// Check the response code
			int responseCode = connection.getResponseCode();
			if (responseCode == 200) {
				// Read the response from the input stream
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuilder response = new StringBuilder();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				// Parse the JSON response
				JSONObject jsonResponse = new JSONObject(response.toString());

				System.err.println("------------->" + jsonResponse);

				// Display the parsed information
				System.out.println("IP Address: " + jsonResponse.getString("query"));
				System.out.println("Country: " + jsonResponse.getString("country"));
				System.out.println("Country Code: " + jsonResponse.getString("countryCode"));
				System.out.println("Region: " + jsonResponse.getString("regionName"));
				System.out.println("City: " + jsonResponse.getString("city"));
				System.out.println("ZIP: " + jsonResponse.getString("zip"));
				System.out.println("Latitude: " + jsonResponse.getDouble("lat"));
				System.out.println("Longitude: " + jsonResponse.getDouble("lon"));
				System.out.println("Timezone: " + jsonResponse.getString("timezone"));
				System.out.println("ISP: " + jsonResponse.getString("isp"));
				System.out.println("Organization: " + jsonResponse.getString("org"));
				System.out.println("AS: " + jsonResponse.getString("as"));
			} else {
				System.out.println("Error: Unable to get location information");
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setFlag(4);
			structure.setMessage(e.getLocalizedMessage());
			structure.setStatusCode(HttpStatus.OK.INTERNAL_SERVER_ERROR.value());
		}

		return structure;

	}

	@Override
	public ResponseStructure diffTwoDates() {

		ResponseStructure st = new ResponseStructure();

		EntityModel entity = userRepository.findByUserId(1);

		Instant inst1 = entity.getCreatedDate().toInstant();
		Instant inst2 = new Date().toInstant();

		long minute = Duration.between(inst1, inst2).toMinutes();

		System.err.println(minute);

		st.setData(minute);

		return st;

	}

	@Override
	public ResponseStructure specialCharacterCheck(RequestModel model) {

		ResponseStructure st = new ResponseStructure();

		String str = model.getName();

		Pattern pattern = Pattern.compile("[A-Za-z0-9/.:-]*\\z");

		Matcher matcher = pattern.matcher(str);

		st.setData(matcher.matches());

		return st;

	}

	@Override
	public ResponseStructure schedulerTrigger1(int id) {

		ResponseStructure structure = new ResponseStructure();

		try {

			if (id == 1) {
				scheduledServices.prepaidInvoiceAtMonthEnd();

				structure.setData("prepaidInvoiceAtMonthEnd : 1");
			} else if (id == 2) {
				scheduledServices.sendPostPaidInvoice();

				structure.setData("sendPostPaidInvoice : 2");
			} else if (id == 3) {
				scheduledServices.agreementExpiring();

				structure.setData("agreementExpiring : 3");
			} else if (id == 4) {
				scheduledServices.deleteFieldsSixMonthsFromResponse();

				structure.setData("deleteFieldsSixMonthsFromResponse : 4");
			} else {

				structure.setData("Please provide a valid input number : below 5");
			}

			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {

			e.printStackTrace();
			structure.setFlag(4);
			structure.setMessage(e.getLocalizedMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;

	}

	@Override
	public ResponseStructure postpaidFlagChange(int id) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<PostpaidPayment> opt = postpaidRepository.findById(id);

			if (opt.isPresent()) {

				PostpaidPayment postpaidPayment = opt.get();

				postpaidPayment.setPaymentFlag(false);
				postpaidPayment.setEndDate(LocalDate.now().minusDays(3));
				postpaidPayment.setEndDate(LocalDate.now());

				EntityModel entityModel = postpaidPayment.getEntityModel();

				entityModel.setStartDate(LocalDate.now().minusDays(3));
				entityModel.setEndDate(LocalDate.now().minusDays(1));
				entityModel.setGraceDate(LocalDate.now());
				entityModel.setPostpaidFlag(true);

				userRepository.save(entityModel);

				postpaidRepository.save(postpaidPayment);

				structure.setData(postpaidPayment);
				structure.setFlag(1);
				structure.setMessage(AppConstants.NO_DATA_FOUND);

			} else {

				structure.setData(null);
				structure.setFlag(2);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setFlag(4);
			structure.setMessage(e.getLocalizedMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;

	}

	@Override
	public ResponseStructure conveInvoiceCheck() {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<TransactionDto> trans = transactionRepository.findByTrancsactionId(11);

			TransactionDto transactionDto = trans.get();

			PostpaidPayment postpaid = postpaidRepository.findById(1).get();

			Path con = Paths.get(context.getRealPath("/WEB-INF/"));

			String invoice = invoiceGenerate.postpaidConveInvoiceIgst(con, transactionDto, postpaid);

			System.err.println(invoice);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage("ERROR");
		}

		return structure;
	}

	@Override
	public ResponseStructure listCheck(int num) {
		ResponseStructure structure = new ResponseStructure();

		try {

			ArrayList<Integer> errorCodes = new ArrayList<>();
			errorCodes.add(401);
			errorCodes.add(403);
			errorCodes.add(500);

			if (errorCodes.contains(num)) {
				structure.setData("Present");
			} else {

				structure.setData("Not Present");
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage("ERROR");
		}

		return structure;
	}

}