
package com.bp.middleware.signmerchant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.bp.middleware.admin.AdminRepository;
import com.bp.middleware.bond.BondDetailRepository;
import com.bp.middleware.bond.BondDetails;
import com.bp.middleware.bond.MerchantBond;
import com.bp.middleware.bond.MerchantBondRepository;
import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.signers.SignerModel;
import com.bp.middleware.signers.SignerRepository;
import com.bp.middleware.uploadhistory.UploadModel;
import com.bp.middleware.uploadhistory.UploadRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.DateUtil;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.Trail;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;
import com.opencsv.CSVWriter;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class MerchantServiceImple implements MerchantService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MerchantServiceImple.class);

	@Autowired
	private MerchantRepository merchantRepository;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private SignerRepository signerRepository;
	@Autowired
	private ServletContext context;
	@Autowired
	private MerchantBondRepository merchantBondRepository;
	@Autowired
	private BondDetailRepository bondDetailRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MerchantPriceRepository merchantPriceRepository;
	@Autowired
	private VendorRepository vendorRepository;
	@Autowired
	private VendorVerificationRepository vendorVerificationRepository;
	@Autowired
	private UploadRepository uploadRepository;
	@Autowired
	private RequestRepository reqRepository;
	@Autowired
	FileUtils fu;

	public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public ResponseStructure addMerchantDetails(RequestModel model) throws Exception {
		ResponseStructure structure = new ResponseStructure();
		try {

			EntityModel admin = userRepository.findByUserId(model.getUserId());

//			ResponseStructure balanceCheck = balanceCheck(admin);

//			if (balanceCheck.getFlag() == 1) {

				if (model.isBond()) {
					MerchantBond bondModel = merchantBondRepository.findByBondId(model.getBondId());
					List<BondDetails> list = bondDetailRepository.findByBondStatus(1);

					if (list.size() != 0) {

						MerchantModel merchant = insertMerchantDetails(model, admin, bondModel);
						// agreementGeneration1(merchant);

						//amountDeduction(admin);// Amount Deduction

						for (BondDetails bond : list) {
							bond.setBondStatus(2);
							bond.setStatus(false);
							bond.setBond(bondModel);
							mergeBondAndFirstAgreement(bond, merchant);
							bondDetailRepository.save(bond);

							bondModel.setOnProcess(bondModel.getOnProcess() + 1);
							bondModel.setRemainingCount(bondModel.getRemainingCount() - 1);
							merchantBondRepository.save(bondModel);
							break;
						}

						// setRequest(admin, merchant, bondModel);

						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(merchant);
						structure.setMessage("Merchant Details added");
						structure.setFlag(1);

					} else {
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(null);
						structure.setMessage("Bond Not Available");
						structure.setFlag(3);
						return structure;
					}

				} else {
					MerchantModel merchant = insertMerchantDetails(model, admin, null);
					// agreementGeneration1(merchant);

					//amountDeduction(admin); // Amount Deduction
					// setRequest(admin, merchant, null);

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(merchant);
					structure.setMessage("Merchant Details added");
					structure.setFlag(1);
				}
//			} else {
//			return balanceCheck;
//			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
		}
		return structure;
	}

	private MerchantModel insertMerchantDetails(RequestModel model, EntityModel admin, MerchantBond bondModel)
			throws ParseException {

		MerchantModel merchant = new MerchantModel();

		merchant.setMerchantName(model.getMerchantName());
		merchant.setBuildingNumber(model.getBuildingNumber());
		merchant.setStreetAddress(model.getStreetAddress());
		merchant.setCityPincode(model.getCityPincode());

		merchant.setCompanyAddress(
				model.getBuildingNumber() + ", " + model.getStreetAddress() + ", " + model.getCityPincode());
		merchant.setCompanyWebsite(model.getCompanyWebsite());
		merchant.setAgreementDate(model.getAgreementDate());
		merchant.setBankName(model.getBankName());
		merchant.setAccountNumber(model.getAccountNumber());
		merchant.setIfscCode(model.getIfscCode());
		merchant.setAccountHolderName(model.getAccountHolderName());
		merchant.setBusinessCategory(model.getBusinessCategory());
		merchant.setBusinessSubCategory(model.getBusinessSubCategory());
		merchant.setBranchAddress(model.getBranchAddress());
		merchant.setMerchantCompanyName(model.getMerchantCompanyName());
		merchant.setSignatoryDesignation(model.getSignatoryDesignation());
		merchant.setDocumentId(FileUtils.documentKey());
		merchant.setDescription(model.getDescription());
		merchant.setBond(model.isBond());
		merchant.setSystemOrSetupFee(model.getSystemOrSetupFee());
		merchant.setAmcOrMmc(model.getAmcOrMmc());
		merchant.setSecurityDeposit(model.getSecurityDeposit());
		merchant.setServiceFee(model.getServiceFee());
		merchant.setNetBanking("Rs." + model.getNetBanking() + "/-");
		merchant.setCreditCard(model.getCreditCard() + "%");
		merchant.setDebitCard(model.getDebitCard() + "%");
		merchant.seteCollect(model.geteCollect());
		merchant.setEmiAbove5000(model.getEmiAbove5000() + "%");
		merchant.setInternationalCard(model.getInternationalCard());
		merchant.setAmexCard(model.getAmexCard());
		merchant.setDinersCard(model.getDinersCard() + "%");
		merchant.setCorporateOrCommercialCard(model.getCorporateOrCommercialCard() + "%");
		merchant.setPrepaidCard(model.getPrepaidCard() + "%");
		merchant.setWallets(model.getWallets() + "%");
		merchant.setUpi(model.getUpi() + "%");
		merchant.setUpiIntent(model.getUpiIntent());
		merchant.setBharathQr(model.getBharathQr() + "%");
		merchant.setSettlementTimeFrame(model.getSettlementTimeFrame());
		merchant.setVas(model.getVas());
		merchant.setOthers(model.getOthers());
		merchant.setEmi(model.getEmi());
		merchant.setEntity(admin);

		Path con = Paths.get(context.getRealPath("/WEB-INF/"));
		String document = agreementGeneration(model, con);
		System.err.println("Document :" + document);

		merchant.setPdfDocument(document);
		merchant.setUploadDocumentAt(DateUtil.dateFormat());
		merchant.setCreatedAt(new Date());
		//merchant.setDocumentExpiryAt(DateUtil.addDate(15, new Date()));
		merchant.setDocumentExpiryAt(LocalDate.now().plusDays(15));
		merchant.setExpired(false);
		merchant.setDocumentTitle(model.getDocumentTitle());

		if (model.isBond()) {
			merchant.setMerchantBond(bondModel);
		}
		merchantRepository.save(merchant);

		return merchant;
	}

	private boolean mergeBondAndFirstAgreement(BondDetails bondDetails, MerchantModel merchant) {

		try {

			Resource firstResource = resourceLoader.getResource("/WEB-INF/bonddocuments/" + bondDetails.getDocument());
			String firstPdfPath = firstResource.getFile().getAbsolutePath();
			System.out.println("First Pdf Path :" + firstPdfPath);

			Resource secondResource = resourceLoader.getResource("/WEB-INF/agreement/" + merchant.getPdfDocument());
			String secondPdfPath = secondResource.getFile().getAbsolutePath();
			System.out.println("Second Pdf Path :" + secondPdfPath);

			// Load the first PDF file
			PDDocument firstDocument = PDDocument.load(new File(firstPdfPath),
					MemoryUsageSetting.setupMainMemoryOnly());
			firstDocument.setAllSecurityToBeRemoved(true);
			System.out.println(firstDocument.getNumberOfPages());

			// Load the second PDF file
			PDDocument secondDocument = PDDocument.load(new File(secondPdfPath),
					MemoryUsageSetting.setupMainMemoryOnly());

			// Iterate through the pages of the second PDF and add them to the first PDF
			for (int i = 0; i < secondDocument.getNumberOfPages(); i++) {
				firstDocument.addPage(secondDocument.getPage(i));
			}

			// Save the merged PDF to the storage location, overwriting the first PDF
			firstDocument.save(new File(secondPdfPath));
			firstDocument.close();
			secondDocument.close();

			System.out.println("Pdf Merged Successfully");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public ResponseStructure insertAllMerchant(List<RequestModel> modelList) {

		ResponseStructure structure = new ResponseStructure();
		try {
			List<Object> list = new ArrayList<>();

			for (RequestModel request : modelList) {

				ResponseStructure addMerchantDetails = addMerchantDetails(request);

//				MerchantModel merchant = new MerchantModel();
//				// AdminDto admin=adminRepository.findByAdminId(request.getAdminId());
//				EntityModel admin = userRepository.findByUserId(request.getUserId());
//				merchant.setMerchantName(request.getMerchantName());
//
//				merchant.setBuildingNumber(request.getBuildingNumber());
//				merchant.setStreetAddress(request.getStreetAddress());
//				merchant.setCityPincode(request.getCityPincode());
//				merchant.setCompanyAddress(request.getBuildingNumber() + ", " + request.getStreetAddress() + ", "
//						+ request.getCityPincode());
//				merchant.setCompanyWebsite(request.getCompanyWebsite());
//				merchant.setAgreementDate(request.getAgreementDate());
//				// merchant.setRateAgreed(request.getRateAgreed());
//				merchant.setBankName(request.getBankName());
//				merchant.setAccountNumber(request.getAccountNumber());
//				// merchant.setAccountType(request.getAccountType());
//				merchant.setIfscCode(request.getIfscCode());
//				// merchant.setMicrCode(request.getMicrCode());
//				merchant.setAccountHolderName(request.getAccountHolderName());
//				merchant.setBusinessCategory(request.getBusinessCategory());
//				merchant.setBusinessSubCategory(request.getBusinessSubCategory());
//				merchant.setBranchAddress(request.getBranchAddress());
//				merchant.setMerchantCompanyName(request.getMerchantCompanyName());
//				// merchant.setSignatoryName(request.getSignatoryName());
//				merchant.setSignatoryDesignation(request.getSignatoryDesignation());
//				// merchant.setSignatoryDate(request.getSignatoryDate());
//				merchant.setEntity(admin);
//				merchantRepository.save(merchant);

				list.add(addMerchantDetails.getData());
			}

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(list);
			structure.setMessage("Merchant Details added");
			structure.setFlag(1);

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseEntity<Resource> viewImage(int merchantId, HttpServletRequest request) {

		Optional<MerchantModel> admin = merchantRepository.findById(merchantId);
		if (admin.isPresent()) {
			if (admin.get().getDocument() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/merchantpdf/" + admin.get().getDocument());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
					LOGGER.info("Could not determine file type.");

				}

				// Fallback to the default content type if type could not be determined
				if (contentType == null) {
					contentType = "application/octet-stream";
				}
				return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
						.header(HttpHeaders.CONTENT_DISPOSITION,
								"attachment; filename=\"" + resource.getFilename() + "\"")
						.body(resource);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public ResponseStructure uploadMerchantAgreement(MultipartFile pdfDocument, String documentTitle,
			String description, int userId) throws ParseException {
		ResponseStructure structure = new ResponseStructure();
		try {
			return saveUploadedFiles(pdfDocument, documentTitle, description, userId);
		} catch (IOException e) {
			LOGGER.info("MerchantServiceImple Upload merchant pdf Document method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setData(null);
		}
		return structure;
	}

	private ResponseStructure saveUploadedFiles(MultipartFile profilePhoto, String documentTitle, String description,
			int adminId) throws IOException, ParseException {

		ResponseStructure structure = new ResponseStructure();

		MerchantModel model = new MerchantModel();
		// AdminDto dto = adminRepository.findByAdminId(adminId);
		EntityModel dto = userRepository.findByUserId(adminId);

		String folder = new FileUtils().genrateFolderName("" + model.getMerchantId());

		String extensionType = null;
		StringTokenizer st = new StringTokenizer(profilePhoto.getOriginalFilename(), ".");
		while (st.hasMoreElements()) {
			extensionType = st.nextElement().toString();
		}
		String fileName = documentTitle + "." + extensionType;
		model.setPdfDocument(folder + "/" + fileName);
		model.setUploadDocumentAt(DateUtil.dateFormat());
		model.setCreatedAt(new Date());
		model.setDocumentExpiryAt(LocalDate.now().plusDays(15));
		model.setExpired(false);
		model.setDocumentTitle(documentTitle + "." + extensionType);
		model.setDescription(description);
		model.setDocumentId(FileUtils.documentKey());
		model.setEntity(dto);
		Path currentWorkingDir = Paths.get(context.getRealPath("/WEB-INF/"));
		File saveFile = new File(currentWorkingDir + "/agreement/" + folder);
		saveFile.mkdir();

		byte[] bytes = profilePhoto.getBytes();
		Path path = Paths.get(saveFile + "/" + fileName);
		Files.write(path, bytes);
		merchantRepository.save(model);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(model);
		structure.setMessage("Merchant document uploaded successfully!!");
		structure.setFileName(fileName);

		return structure;
	}

	@Override
	public ResponseEntity<Resource> viewPdf(int merchantId, HttpServletRequest request) {
		Optional<MerchantModel> admin = merchantRepository.findById(merchantId);
		if (admin.isPresent()) {
			if (admin.get().getPdfDocument() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/agreement/" + admin.get().getPdfDocument());
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
				} catch (IOException ex) {
					LOGGER.info("Could not determine file type.");

				}

				// Fallback to the default content type if type could not be determined
				if (contentType == null) {
					contentType = "application/octet-stream";
				}
				return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
						.header(HttpHeaders.CONTENT_DISPOSITION,
								"attachment; filename=\"" + resource.getFilename() + "\"")
						.body(resource);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public String getPathName() throws IOException, ParseException {

		// 1.working
		// File file=new
		// File("src/main/webapp/WEB-INF/merchantpdf/49/3ND73LXXX310192023060916.pdf");
		// String path=file.getAbsolutePath();

		// Resource resource = resourceLoader
		// .getResource("/WEB-INF/merchantpdf/49/3ND73LXXX310192023060916.pdf");
		// File file = resource.getFile();
		// String path=resource.getFile().getAbsolutePath();
		// System.out.println("Result : "+path);
		// System.out.println("Needed :
		// "+"C:\\Users\\Basispay\\Documents\\SpringBootProjects\\mercant_esign\\src\\main\\webapp\\WEB-INF\\merchantpdf\\49\\3ND73LXXX310192023060916.pdf");

		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
		// HH:mm:ss");
		// LocalDateTime now=LocalDateTime.now();
		// String formatDateTime = now.format(formatter);
		// LocalTime time=now.toLocalTime();
		// System.out.println("After : " + formatDateTime);
		// System.out.println(("Before date: "+now));
		// System.out.println("Get time :"+time);
		// System.out.println("Get time: "+date.);
		// System.out.println("Added date :"+ date.);

		// working
		// String aRevisedDate = null;
		// final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		// sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // This line converts the given
		// date into UTC time zone
		// DateFormat dateFormat =sdf;
		// String strDate = dateFormat.format(new Date());
		// Date dateObj=sdf.parse(strDate);
		// aRevisedDate = new SimpleDateFormat("MM/dd/yyyy KK:mm:ss a").format(dateObj);
		// System.out.println(aRevisedDate);
		// ......................//
		String aRevisedDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Calendar c = Calendar.getInstance();
		c.setTime(new Date()); // Using today's date
		c.add(Calendar.DATE, 5); // Adding 5 days
		String output = sdf.format(c.getTime());
		Date dateObj = sdf.parse(output);
		aRevisedDate = new SimpleDateFormat("MM/dd/yyyy KK:mm:ss a").format(dateObj);
		System.out.println(output);

		return aRevisedDate;
	}

	@Override
	public ResponseStructure viewAllMerchants() {
		ResponseStructure structure = new ResponseStructure();
		List<MerchantModel> list = merchantRepository.findAll();
		if (!list.isEmpty()) {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(list);
			structure.setMessage("Merchant Details are.... ");
			structure.setFlag(1);
		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(null);
			structure.setMessage("Merchan details are Not Found....!!! ");
			structure.setFlag(2);
		}

		return structure;
	}

	@Override
	public ResponseStructure listAll() {
		ResponseStructure structure = new ResponseStructure();
		// Map<String, Object> map = new HashMap<>();
		// Map<String, Object> mapNew = new HashMap<>();
		List<Object> listall = new ArrayList<>();

		List<MerchantModel> list = merchantRepository.findAll();
		int merchantCount = list.size();
		// Trail merchant=new Trail();
		if (!list.isEmpty()) {

			// int merchantCount=0;
			for (MerchantModel merchantModel : list) {
				// List<Object> objects=new ArrayList<>();
				Trail dto = new Trail();
				List<SignerModel> signer = signerRepository.findByMerchantModel(merchantModel);
				int signerCount = signer.size();
				System.out.println(signerCount);
				int signed = 0;
				for (SignerModel signers : signer) {
					if (signers.isOtpVerificationStatus()) {
						signed = signed + 1;
					}
				}
				// merchantCount++;
				// mapNew.put("signer_count", signerCount);
				// mapNew.put("signed_count", signed);
				// mapNew.put("merchant_details", merchantModel);

				dto.setSignerCount(signerCount);
				dto.setSignedCount(signed);
				dto.setMerchant(merchantModel);
				dto.setMerchantCount(merchantCount);
				// objects.add(si)
				listall.add(dto);

				// map.put("merchant_signers_details", mapNew);
			}
			// map.put("total_merchant_count", merchantCount);
			// merchant.setMerchantCount(merchantCount);

			// listall.add(merchant);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(listall);
			structure.setFlag(1);

		} else {
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseStructure updateExpiryDate(int merchantId, RequestModel request) {
		ResponseStructure structure = new ResponseStructure();
		try {

			MerchantModel model = merchantRepository.findByMerchantId(merchantId);
			model.setDocumentExpiryAt(LocalDate.now().plusDays(request.getDays()));
			model.setExpired(false);
			
			List<SignerModel> signerList = signerRepository.findByMerchantModel(model);
			
			for (SignerModel signerModel : signerList) {
				signerModel.setExpired(false);
				signerRepository.save(signerModel);
			}
			
			merchantRepository.save(model);

			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(model);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setData(null);
		}
		return structure;
	}

	@Override
	public ResponseStructure viewByMerchantId(int merchantId) {
		ResponseStructure structure = new ResponseStructure();
		try {
			MerchantModel merchantModel = merchantRepository.findByMerchantId(merchantId);
			if (merchantModel != null) {
				structure.setMessage(AppConstants.SUCCESS);
				structure.setData(merchantModel);
				structure.setFlag(1);
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setData(null);
		}
		return structure;
	}

	// @Override
	// public ResponseStructure updateMerchant(int merchantId, RequestModel model) {
	// ResponseStructure structure=new ResponseStructure();
	// try {
	// MerchantModel merchantModel =
	// merchantRepository.findByMerchantId(merchantId);
	// if (merchantModel!=null) {
	// merchantModel.setDescription(model.getDescription());
	// merchantModel.setDocumentTitle(model.getDocumentTitle());
	// final Resource resource = resourceLoader
	// .getResource("/WEB-INF/merchantpdf/" + merchantModel.getPdfDocument());
	// String name=resource.getFile().getName();
	// System.out.println("Pdf Name : "+name);
	//
	// File file = resource.getFile();
	//
	// FileInputStream input = new FileInputStream(file);
	// MultipartFile multipartFile = new MockMultipartFile(name,
	// name, "text/plain", IOUtils.toByteArray(input));
	//
	//
	// merchantRepository.save(merchantModel);
	//
	////
	////// File file = new File("src/test/resources/validation.txt");
	//// DiskFileItem fileItem = new DiskFileItem("file", "text/plain", false,
	// file.getName(), (int) file.length() , file.getParentFile());
	//// fileItem.getOutputStream();
	//// MultipartFile multipartFile = new CommonsMultipartFile(fileItem);
	////
	//
	//
	// structure.setMessage("Merchant Details Updated SuccessFully...");
	// structure.setData(merchantModel);
	// structure.setFlag(1);
	// } else {
	// structure.setMessage(AppConstants.NO_DATA_FOUND);
	// structure.setData(null);
	// structure.setFlag(2);
	// }
	// structure.setStatusCode(HttpStatus.OK.value());
	// } catch (Exception e) {
	// structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
	// structure.setMessage(AppConstants.TECHNICAL_ERROR);
	// structure.setFlag(3);
	// structure.setData(structure);
	// }
	// return structure;
	// }
	//
	// private String saveUploadedFiles(MultipartFile profilePhoto,MerchantModel
	// model) throws IOException {
	//// ResponseStructure structure=new ResponseStructure();
	// String folder = new FileUtils().genrateFolderName("" +
	// model.getMerchantId());
	//
	//
	// String extensionType = null;
	// StringTokenizer st = new StringTokenizer(profilePhoto.getOriginalFilename(),
	// ".");
	// while (st.hasMoreElements()) {
	// extensionType = st.nextElement().toString();
	// }
	// String fileName = FileUtils.getRandomString() + "." + extensionType;
	//// model.setProfilePhoto(folder + "/" + fileName);
	//
	// Path currentWorkingDir = Paths.get(context.getRealPath("/WEB-INF/"));
	// File saveFile = new File(currentWorkingDir + "/adminprofilepictures/" +
	// folder);
	// saveFile.mkdir();
	//
	// byte[] bytes = profilePhoto.getBytes();
	// Path path = Paths.get(saveFile + "/" + fileName);
	// Files.write(path, bytes);
	//// adminRepository.save(model);
	//
	//// structure.setStatusCode(HttpStatus.OK.value());
	//// structure.setFlag(1);
	//// structure.setData(model);
	//// structure.setMessage(model.getName()+" your profile picture has been
	// uploaded successfully!!");
	//// structure.setFileName(fileName);
	//
	// return structure;
	// }

	public String agreementGeneration1(MerchantModel merchantModel) {

		try {
			Path con = Paths.get(context.getRealPath("/WEB-INF/"));

			EntityModel admin = merchantModel.getEntity();

			MerchantBond merchantBond = merchantModel.getMerchantBond();

			TemplateEngine templateEngine = new TemplateEngine();
			ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
			templateResolver.setPrefix("template-engines/");
			templateResolver.setSuffix(".html");
			templateResolver.setTemplateMode("HTML");
			templateResolver.setCacheable(false);

			templateEngine.setTemplateResolver(templateResolver);

			// Create a context with dynamic values
			Context context = new Context();
			context.setVariable("agreementDate", merchantModel.getAgreementDate());
			context.setVariable("merchantCompanyName", merchantModel.getMerchantCompanyName());
			context.setVariable("companyAddress", merchantModel.getCompanyAddress());
			context.setVariable("buildingNumber", merchantModel.getBuildingNumber());
			context.setVariable("streetAddress", merchantModel.getStreetAddress());
			context.setVariable("cityPincode", merchantModel.getCityPincode());
			context.setVariable("companyWebsite", merchantModel.getCompanyWebsite());
			context.setVariable("merchantName", merchantModel.getMerchantName());
			context.setVariable("designation", merchantModel.getSignatoryDesignation());
			context.setVariable("systemIntegration", merchantModel.getSystemOrSetupFee());
			context.setVariable("amcMmc", merchantModel.getAmcOrMmc());
			context.setVariable("securityDeposit", merchantModel.getSecurityDeposit());
			context.setVariable("serviceFee", merchantModel.getServiceFee());
			context.setVariable("netBankingAll", merchantModel.getNetBanking());
			context.setVariable("creditCard", merchantModel.getCreditCard());
			context.setVariable("debitCard", merchantModel.getDebitCard());
			context.setVariable("eCollect", merchantModel.geteCollect());
			context.setVariable("emiAbove5K", merchantModel.getEmiAbove5000());
			context.setVariable("internationalCard", merchantModel.getInternationalCard());
			context.setVariable("amexCard", merchantModel.getAmexCard());
			context.setVariable("dinnersCard", merchantModel.getDinersCard());
			context.setVariable("corporateCommercialCard", merchantModel.getCorporateOrCommercialCard());
			context.setVariable("prepaidCard", merchantModel.getPrepaidCard());
			context.setVariable("wallets", merchantModel.getWallets());
			context.setVariable("upi", merchantModel.getUpi());
			context.setVariable("upiIntent", merchantModel.getUpiIntent());
			context.setVariable("bharatQr", merchantModel.getBharathQr());
			context.setVariable("settlementTimeFrame", merchantModel.getSettlementTimeFrame());
			context.setVariable("vas", merchantModel.getVas());
			context.setVariable("othersPleaseSpecify", merchantModel.getOthers());
			context.setVariable("emi", merchantModel.getEmi());
			context.setVariable("bankName", merchantModel.getBankName());
			context.setVariable("bankAccountNumber", merchantModel.getAccountNumber());
			context.setVariable("branchAddress", merchantModel.getBranchAddress());
			context.setVariable("ifscCode", merchantModel.getIfscCode());
			context.setVariable("accountHolderName", merchantModel.getAccountHolderName());
			context.setVariable("businessCategory", merchantModel.getBusinessCategory());
			context.setVariable("businessSubCategory", merchantModel.getBusinessSubCategory());

			String html = templateEngine.process("agreement.html", context);

			String generatedString = merchantModel.getDocumentTitle() + ".pdf";

			merchantModel.setPdfDocument(generatedString);

			System.err.println("SECOND AGREE " + generatedString);

			File fileName = new File(con + File.separator + "/merchantpdf/" + StringUtils.cleanPath(generatedString));

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
					}
				}
			}
			Resource firstResource = resourceLoader.getResource("/WEB-INF/agreement/" + merchantModel.getPdfDocument());
			String firstPdfPath = firstResource.getFile().getAbsolutePath();
			Resource secondResource = resourceLoader.getResource("/WEB-INF/secondhalfagreement/" + generatedString);
			String secondPdfPath = secondResource.getFile().getAbsolutePath();

			// Load the first PDF file
			PDDocument firstDocument = PDDocument.load(new File(firstPdfPath),
					MemoryUsageSetting.setupMainMemoryOnly());
			firstDocument.setAllSecurityToBeRemoved(true);

			// Load the second PDF file
			PDDocument secondDocument = PDDocument.load(new File(secondPdfPath),
					MemoryUsageSetting.setupMainMemoryOnly());

			// Iterate through the pages of the second PDF and add them to the first PDF
			for (int i = 0; i < secondDocument.getNumberOfPages(); i++) {
				firstDocument.addPage(secondDocument.getPage(i));
			}

			// Save the merged PDF to the storage location, overwriting the first PDF
			firstDocument.save(new File(firstPdfPath));
			firstDocument.close();
			secondDocument.close();
			System.out.println("Second Pdf Merged successfully");

			return firstPdfPath;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public String agreementGeneration(RequestModel merchantModel, Path con) {

		try {

			TemplateEngine templateEngine = new TemplateEngine();

			ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
			templateResolver.setPrefix("template-engines/");
			templateResolver.setSuffix(".html");
			templateResolver.setTemplateMode("HTML");
			templateResolver.setCacheable(false);

			templateEngine.setTemplateResolver(templateResolver);

			// Create a context with dynamic values
			Context context = new Context();
			context.setVariable("agreementDate", merchantModel.getAgreementDate());
			context.setVariable("merchantCompanyName", merchantModel.getMerchantCompanyName());
			context.setVariable("companyAddress", merchantModel.getCompanyAddress());
			context.setVariable("buildingNumber", merchantModel.getBuildingNumber());
			context.setVariable("streetAddress", merchantModel.getStreetAddress());
			context.setVariable("cityPincode", merchantModel.getCityPincode());
			context.setVariable("companyWebsite", merchantModel.getCompanyWebsite());
			context.setVariable("merchantName", merchantModel.getMerchantName());
			context.setVariable("designation", merchantModel.getSignatoryDesignation());
			context.setVariable("systemIntegration", merchantModel.getSystemOrSetupFee());
			context.setVariable("amcMmc", merchantModel.getAmcOrMmc());
			context.setVariable("securityDeposit", merchantModel.getSecurityDeposit());
			context.setVariable("serviceFee", merchantModel.getServiceFee());
			context.setVariable("netBankingAll", merchantModel.getNetBanking());
			context.setVariable("creditCard", merchantModel.getCreditCard());
			context.setVariable("debitCard", merchantModel.getDebitCard());
			context.setVariable("eCollect", merchantModel.geteCollect());
			context.setVariable("emiAbove5K", merchantModel.getEmiAbove5000());
			context.setVariable("internationalCard", merchantModel.getInternationalCard());
			context.setVariable("amexCard", merchantModel.getAmexCard());
			context.setVariable("dinnersCard", merchantModel.getDinersCard());
			context.setVariable("corporateCommercialCard", merchantModel.getCorporateOrCommercialCard());
			context.setVariable("prepaidCard", merchantModel.getPrepaidCard());
			context.setVariable("wallets", merchantModel.getWallets());
			context.setVariable("upi", merchantModel.getUpi());
			context.setVariable("upiIntent", merchantModel.getUpiIntent());
			context.setVariable("bharatQr", merchantModel.getBharathQr());
			context.setVariable("settlementTimeFrame", merchantModel.getSettlementTimeFrame());
			context.setVariable("vas", merchantModel.getVas());
			context.setVariable("othersPleaseSpecify", merchantModel.getOthers());
			context.setVariable("emi", merchantModel.getEmi());
			context.setVariable("bankName", merchantModel.getBankName());
			context.setVariable("bankAccountNumber", merchantModel.getAccountNumber());
			context.setVariable("branchAddress", merchantModel.getBranchAddress());
			context.setVariable("ifscCode", merchantModel.getIfscCode());
			context.setVariable("accountHolderName", merchantModel.getAccountHolderName());
			context.setVariable("businessCategory", merchantModel.getBusinessCategory());
			context.setVariable("businessSubCategory", merchantModel.getBusinessSubCategory());

			String html = templateEngine.process("agreement.html", context);

			String generatedString = merchantModel.getDocumentTitle() + ".pdf";

			File fileName = new File(con + File.separator + "/agreement/" + StringUtils.cleanPath(generatedString));

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
					}
				}
			}

			return generatedString;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public ResponseStructure balanceCheck(EntityModel userModel) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		VendorVerificationModel vendorVerifyModel = vendorVerificationRepository
				.findByVerificationDocument("DIGITAL SIGNER");

		System.out.println("USER : " + userModel.getUserId());
		System.out.println("vendorVerifyModel : " + vendorVerifyModel.getVendorVerificationId());

		MerchantPriceModel merchantPriceModel = merchantPriceRepository
				.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 1);

		if (userModel.getPaymentMethod().getPaymentId() == 2) {
			if (merchantPriceModel!=null && userModel.getRemainingAmount() > merchantPriceModel.getSignaturePrice()) {

				structure.setFlag(1);

			} else {
				if(merchantPriceModel == null) {
					structure.setMessage("Access not granted");
				}else {
					structure.setMessage("Your wallet is empty,Please recharge to continue.");
				}
				
				structure.setData(null);
				structure.setFlag(5);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
			if (LocalDate.now().isEqual(userModel.getGraceDate())
					|| LocalDate.now().isBefore(userModel.getGraceDate())) {

				structure.setFlag(1);

			} else {

				structure.setData(null);
				structure.setFlag(5);
				structure.setMessage("Your payment is pending, Please pay the amount to continue.");
				structure.setStatusCode(HttpStatus.OK.value());
			}
		}
		return structure;
	}

	private void amountDeduction(EntityModel userModel) {

		VendorVerificationModel vendorVerifyModel = vendorVerificationRepository
				.findByVerificationDocument("DIGITAL SIGNER");
		MerchantPriceModel merchantPriceModel = merchantPriceRepository
				.findByEntityModelAndVendorVerificationModelAndPriority(userModel, vendorVerifyModel, 1);

		if (userModel.getPaymentMethod().getPaymentId() == 2) {
			double remainingAmount = userModel.getRemainingAmount() - merchantPriceModel.getSignaturePrice();
			double consumedAmount = userModel.getConsumedAmount() + merchantPriceModel.getSignaturePrice();

			userModel.setRemainingAmount(fu.twoDecimelDouble(remainingAmount));
			userModel.setConsumedAmount(fu.twoDecimelDouble(consumedAmount));
			userModel.setPaymentStatus("No Dues");

			userRepository.save(userModel);
		}

		// Still Some pending
	}

	private void setRequest(EntityModel user, MerchantModel merchant, MerchantBond bondModel) {

		VendorVerificationModel vendorVerifyModel = vendorVerificationRepository
				.findByVerificationDocument("DIGITAL SIGNER");
		MerchantPriceModel merchantPriceModel = merchantPriceRepository
				.getByEntityModelAndVendorVerificationModelAndStatus(user, vendorVerifyModel,true);

		Request request = new Request();

		request.setReferenceId(FileUtils.getRandomAlphaNumericString());
		request.setRequestBy(user.getName());
		request.setRequestDateAndTime(new Date());
		request.setCompanyName(merchant.getMerchantCompanyName());
		request.setFullName(user.getName());
		request.setDocumentTitle(merchant.getDocumentTitle());
		request.setDocExpiryAt(merchant.getDocumentExpiryAt().toString());
		request.setPrice(merchantPriceModel.getSignaturePrice());
		request.setSignerCount(0);

		if (bondModel != null) {

			request.setBond(true);
			request.setBondAmount(bondModel.getBondAmount());
		} else {

			request.setBond(false);
			request.setBondAmount(0);
		}

		request.setUser(user);
		request.setVerificationModel(vendorVerifyModel);
		request.setMerchant(merchant);

		reqRepository.save(request);
	}

	@Override
	public ResponseStructure viewMerchantByUserId(int userId) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<EntityModel> optional = userRepository.findById(userId);

			if (optional.isPresent()) {
				EntityModel entity = optional.get();

				List<MerchantModel> merchantByUser = merchantRepository.findByEntity(entity);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(merchantByUser);
				structure.setMessage("SIGNING MERCHANT DETAILS BY ENTITY ID");

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure viewMerchantByBondId(int bondId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<MerchantBond> optional = merchantBondRepository.findById(bondId);

			if (optional.isPresent()) {
				MerchantBond bond = optional.get();

				List<MerchantModel> merchantByBond = merchantRepository.findByMerchantBond(bond);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(merchantByBond);
				structure.setMessage("SIGNING MERCHANT DETAILS BY BOND USED");
			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure bulkUpload(List<SignerDto> dto, int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel entityModel = userRepository.findByUserId(userId);
			UploadModel uploadModel = new UploadModel();
			List<String[]> success = new ArrayList<>();

			String[] header = { "Singner_Name", "Signer_Mobile", "Signer_Email", "Bond_Payer", "Aggreement_Id" };
			success.add(header);
			for (SignerDto model : dto) {
				String aggreementId = String.valueOf(model.getMerchantId());
				String[] value = { model.getSignerName(), model.getSignerMobile(), model.getSignerEmail(),
						model.getPayer(), aggreementId };
				success.add(value);
			}
			String order = FileUtils.getRandomString();
			Resource resource = resourceLoader.getResource("/WEB-INF/bulkupload/" + order + ".csv");
			String absolutePath = resource.getFile().getAbsolutePath();

			System.out.println("absolute path :" + absolutePath);
			try (CSVWriter writer = new CSVWriter(new FileWriter(absolutePath))) {
				writer.writeAll(success);
			} catch (IOException e) {
				e.printStackTrace();
			}

			int totalCount = dto.size();
			int failedCount = 0;
			int successCount = 0;

			List<String[]> finishedReport = new ArrayList<>();

			String[] reportHeader = { "Singner_Name", "Signer_Mobile", "Signer_Email", "Bond_Payer", "Aggreement_Id",
					"Remarks"};
			finishedReport.add(reportHeader);

			String createdBy = "";

			for (SignerDto model : dto) {
				RequestModel details = new RequestModel();

				details.setSignerName(model.getSignerName());
				details.setSignerMobile(model.getSignerMobile());
				details.setSignerEmail(model.getSignerEmail());
				details.setPayer(model.getPayer());
				details.setMerchantId(model.getMerchantId());
				Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(model.getSignerEmail());

				int length = model.getSignerMobile().length();
				String aggreementId = String.valueOf(model.getMerchantId());
				if (model.getSignerName() != null && model.getSignerMobile() != null && model.getSignerEmail() != null
						&& model.getPayer() != null && aggreementId != null) {
					if (matcher.matches() && length == 10) {
						MerchantModel merchantModel = merchantRepository.findByMerchantId(model.getMerchantId());
						List<SignerModel> list = signerRepository.findByMerchantModel(merchantModel);
						if (list.size() < 4) {
							SignerModel signerModel = new SignerModel();
							signerModel.setSignerName(model.getSignerName());
							signerModel.setSignerMobile(model.getSignerMobile());
							signerModel.setSignerEmail(model.getSignerEmail());
							signerModel.setCreatedAt(LocalDate.now());
							signerModel.setReferenceNumber(FileUtils.generateSignerReference(9));
							signerModel.setStatus(true);
							if (model.getPayer().equalsIgnoreCase("yes")) {
								signerModel.setBondPayer(true);
							} else {
								signerModel.setBondPayer(false);
							}
							signerModel.setMerchantModel(merchantModel);
							signerModel.setEntityModel(entityModel);
							signerRepository.save(signerModel);

							successCount++;
							details.setRemarks("Uploaded Successfully");
						} else {
							failedCount++;
							details.setRemarks("Signer Details Exceeded For This Aggrement");
						}
					} else {
						failedCount++;
						if (!matcher.matches()) {
							details.setRemarks("Invalid Email Address");
						} else if (length != 10) {
							details.setRemarks("Invalid Mobile Number");
						}
					}
				} else {
					if (model.getSignerName() == null) {
						details.setRemarks("Signer Name Fieled Empty");
					} else if (model.getSignerMobile() == null) {
						details.setRemarks("Signer Mobile Field Empty");
					} else if (model.getSignerEmail() == null) {
						details.setRemarks("Signer Email Field Empty");
					} else if (model.getPayer() == null) {
						details.setRemarks("Payer Field is Empty");
					} else if (aggreementId == null) {
						details.setRemarks("Merchant Id Can't be Empty");
					}
					failedCount++;

				}
				String[] values = { details.getSignerName(), details.getSignerMobile(), details.getSignerEmail(),
						details.getPayer(), aggreementId, details.getRemarks() };
				finishedReport.add(values);
			}

			String random = FileUtils.getRandomString();
			Resource source = resourceLoader.getResource("/WEB-INF/bulkupload/" + random + ".csv");
			String path = source.getFile().getAbsolutePath();

			System.out.println("absolute path :" + path);
			try (CSVWriter writer = new CSVWriter(new FileWriter(path))) {
				writer.writeAll(finishedReport);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Total Count :" + totalCount);
			System.out.println("Success Count :" + successCount);
			System.out.println("Failed Count :" + failedCount);

			uploadModel.setUploadCount(totalCount);
			uploadModel.setSuccessCount(successCount);
			uploadModel.setFailedCount(failedCount);
			uploadModel.setUploadDocument(order + ".csv");
			uploadModel.setFinishedDocument(random + ".csv");
			uploadModel.setCreatedAt(new Date());
			uploadModel.setCreatedBy(createdBy);
			uploadModel.setEntity(entityModel);
			uploadModel.setIpAddress(FileUtils.getIpAddress());
			uploadModel.setCategory("Signers");

			uploadRepository.save(uploadModel);

			structure.setData(finishedReport);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage("Uploaded Successfully...");

		} catch (Exception e) {

			e.printStackTrace();
			// LOGGER.info(AppConstants.MEMBER_CREATE, e);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getLocalizedMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(5);

		}
		return structure;
	}

	@Override
	public ResponseStructure listBySpecificMerchant(int userId) {

		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel user = userRepository.findByUserId(userId);

			if (user != null) {
				
				List<Object> listall = new ArrayList<>();

				List<MerchantModel> list = merchantRepository.findByEntity(user);
				int merchantCount = list.size();

				if (!list.isEmpty()) {

					for (MerchantModel merchantModel : list) {
						Trail dto = new Trail();
						List<SignerModel> signer = signerRepository.findByMerchantModel(merchantModel);
						int signerCount = signer.size();
						System.out.println(signerCount);
						int signed = 0;
						for (SignerModel signers : signer) {
							if (signers.isOtpVerificationStatus()) {
								signed = signed + 1;
							}
						}

						dto.setSignerCount(signerCount);
						dto.setSignedCount(signed);
						dto.setMerchant(merchantModel);
						dto.setMerchantCount(merchantCount);

						listall.add(dto);
					}

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage(AppConstants.SUCCESS);
					structure.setData(listall);
					structure.setFlag(1);

				} else {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setData(null);
					structure.setFlag(2);
				}

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("ENTITY NOT FOUND");
				structure.setData(null);
				structure.setFlag(3);
			}

		} catch (Exception e) {

			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	@Override
	public ResponseStructure makeWholeDocExpire(int merchantId) {
		
		ResponseStructure structure = new ResponseStructure();
		try {

			MerchantModel model = merchantRepository.findByMerchantId(merchantId);
			
			model.setDocumentExpiryAt(LocalDate.now().minusDays(1));
			model.setExpired(true);
			
			List<SignerModel> signerList = signerRepository.findByMerchantModel(model);
			
			for (SignerModel signerModel : signerList) {
				signerModel.setExpired(true);
				signerRepository.save(signerModel);
			}
			
			merchantRepository.save(model);

			structure.setMessage("Agreement link expiration successful");
			structure.setData(model);
			structure.setFlag(1);
			structure.setStatusCode(HttpStatus.OK.value());

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setData(null);
		}
		return structure;
	}

}
