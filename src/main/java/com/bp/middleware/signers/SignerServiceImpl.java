package com.bp.middleware.signers;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.admin.AdminServiceImplementation;
import com.bp.middleware.bond.MerchantBond;
import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.signerpositiontracker.SignerPositionTracker;
import com.bp.middleware.signerpositiontracker.SignerPositionTrackerRepository;
import com.bp.middleware.signmerchant.MerchantModel;
import com.bp.middleware.signmerchant.MerchantRepository;
import com.bp.middleware.smartrouteverification.SmartRouteUtils;
import com.bp.middleware.sms.SMSEntity;
import com.bp.middleware.sms.SMSRepository;
import com.bp.middleware.sms.SMSService;
import com.bp.middleware.sms.SuperAdminSmsConfig;
import com.bp.middleware.uploadhistory.UploadRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.DateUtil;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.GetPublicIpAndLocation;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

import jakarta.mail.MessagingException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class SignerServiceImpl implements SignerService {

	@Autowired
	private SignerRepository repository;
	@Autowired
	private SMSService smsService;
	@Autowired
	private SMSRepository smsRepo;
	@Autowired
	EmailService emailService;
	@Autowired
	ServletContext context;
	@Autowired
	private MerchantRepository merchantRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private UploadRepository uploadRepository;
	@Autowired
	private AadhaarSigningVerification signingVerification;
	@Autowired
	private AadhaarOtpSignSubmitVerification otpSignSubmitVerification;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;
	@Autowired
	private SignerPositionTrackerRepository signerPositionTrackerRepository;
	@Autowired
	private SignerUtils signerUtils;

	private static final Logger LOGGER = LoggerFactory.getLogger(SignerServiceImpl.class);

	@Override
	public ResponseStructure addSigners(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			MerchantModel merchantModel = merchantRepository.findByMerchantId(model.getMerchantId());
			List<SignerModel> list = repository.findByMerchantModel(merchantModel);
			int size = list.size();

			String maskedAadhar = "";

			if (model.getAadhaarNumber() != null) {

				if (model.getAadhaarNumber().length() == 12) {

					maskedAadhar = FileUtils.getFirstFourChar(model.getAadhaarNumber()) + "XXXX"
							+ FileUtils.stringSplitter(model.getAadhaarNumber(), 8);

					SignerModel signerPresent = repository.findBymerchantModelAndSignerAadhaar(merchantModel,
							maskedAadhar);

					if (signerPresent != null) {

						structure.setMessage("A SIGNER WITH SAME AADHAR HAS ALREADY BEEN ASSIGNED");
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(null);
						structure.setFlag(3);

						return structure;
					}

				} else {

					structure.setMessage("PLEASE PROVIDE A VALID AADHAR NUMBER");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(3);

					return structure;
				}
			}

			boolean bondAmount = bondAmount(list);

			if (size < 4) {

				SignerModel sign = new SignerModel();

				sign.setSignerName(model.getSignerName());
				sign.setSignerMobile(model.getSignerMobile());
				sign.setSignerEmail(model.getSignerEmail());
				sign.setReferenceNumber(FileUtils.generateSignerReference(9));
				sign.setCreatedAt(LocalDate.now());
				sign.setCreatedBy(model.getCreatedBy());
				sign.setStatus(true);
				sign.setMerchantModel(merchantModel);
				sign.setSignerAadhaar(maskedAadhar);
				sign.setExpired(merchantModel.isExpired());
				sign.setEntityModel(merchantModel.getEntity());

				if (bondAmount == true && model.isBondPayer() == true) {

					structure.setMessage("BOND AMOUNT PAYER ALREADY PRESENT");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(3);

					return structure;

				} else {

					sign.setBondPayer(model.isBondPayer());
				}

				repository.save(sign);

				structure.setMessage("Signers Added Successfully....!!!");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(sign);
				structure.setFlag(1);

			} else {
				structure.setMessage("Signers size Exceeded....!!!");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
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

	private boolean bondAmount(List<SignerModel> list) {

		for (SignerModel signerModel : list) {

			if (signerModel.isBondPayer()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public ResponseStructure viewAllDetails() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<SignerModel> list = repository.findAll();
			if (!list.isEmpty()) {
				structure.setMessage("All Signers Details view Successfully....!!!");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(list);
				structure.setFlag(1);
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseStructure viewById(int signerId) {
		ResponseStructure structure = new ResponseStructure();
		try {
			SignerModel signerModel = repository.findBySignerId(signerId);
			if (signerModel != null) {
				structure.setMessage("Signers Details view Successfully....!!!");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(signerModel);
				structure.setFlag(1);
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseStructure updateDetails(RequestModel model, int signerId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			SignerModel signerModel = repository.findBySignerId(signerId);

			if (signerModel != null) {

				if (model.getAadhaarNumber() != null) {

					if (model.getAadhaarNumber().length() == 12) {

						String maskedAadhar = FileUtils.getFirstFourChar(model.getAadhaarNumber()) + "XXXX"
								+ FileUtils.stringSplitter(model.getAadhaarNumber(), 8);

						if (signerModel.getSignerAadhaar() != model.getAadhaarNumber()) {

							SignerModel signerPresent = repository
									.findBymerchantModelAndSignerAadhaar(signerModel.getMerchantModel(), maskedAadhar);

							if (signerPresent == null) {

								signerModel.setSignerAadhaar(maskedAadhar);

							} else {

								structure.setMessage("A SIGNER WITH SAME AADHAR HAS ALREADY BEEN ASSIGNED");
								structure.setStatusCode(HttpStatus.OK.value());
								structure.setData(null);
								structure.setFlag(3);

								return structure;
							}

						}

					} else {

						structure.setMessage("PLEASE PROVIDE A VALID AADHAR NUMBER");
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(null);
						structure.setFlag(3);

						return structure;
					}
				}

				signerModel.setSignerName(model.getSignerName());
				signerModel.setSignerEmail(model.getSignerEmail());
				signerModel.setSignerMobile(signerModel.getSignerMobile());
				repository.save(signerModel);

				structure.setMessage("Signers Details view Successfully....!!!");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(signerModel);
				structure.setFlag(1);
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
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

	private Date calculatemintus(Date d1) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		cal.add(Calendar.SECOND, 360);
		return cal.getTime();
	}

	@Override
	public ResponseStructure sentOtp(int signerId) {
		ResponseStructure structure = new ResponseStructure();
		try {
			SignerModel signerModel = repository.findBySignerId(signerId);

			if (signerModel != null) {

				if (signerModel.isOtpVerificationStatus() && !signerModel.getEntityModel().isGeneralSigning()
						&& signerModel.isExpired() && signerModel.getMerchantModel().isExpired()) {

					if (signerModel.getMerchantModel().isExpired()) {
						structure.setMessage("The Agreement has been expired");
					} else if (signerModel.isExpired()) {
						structure.setMessage("This signer has been expelled from signing this document");
					} else if (!signerModel.getEntityModel().isGeneralSigning()) {
						structure.setMessage("General signing not enabled");
					} else {
						structure.setMessage("Document Already Verified");
					}

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(signerModel);
					structure.setFlag(2);

				} else {

					ResponseStructure balanceCheck = signerUtils.balanceCheck(signerModel, "", true);

					if (balanceCheck.getFlag() == 1) {

						String otp = FileUtils.getRandomOTPnumber(6);
						signerModel.setOtpcode(otp);
						signerModel.setOtpExpiry(calculatemintus(new Date()));

						SMSEntity model = null;

						if (signerModel.getEntityModel().isSmsPresent()) {

							List<SMSEntity> smsList = smsRepo
									.getByEntityModelAndSmsTempStatus(signerModel.getEntityModel(), true);

							if (smsList.size() > 0) {

								LOGGER.info("ENTITY SMS");
								model = smsList.get(smsList.size() - 1);

							} else {
								List<SMSEntity> allAdminSms = smsRepo.allAdminSms();

								if (!allAdminSms.isEmpty()) {

									LOGGER.info("ALL ADMIN SMS 1");
									model = allAdminSms.get(allAdminSms.size() - 1);
								} else {

									LOGGER.info("SMS CONFIG NOT FOUND 1");
									return smsConfigurationNotFound();
								}
							}

						} else {

							System.err.println("HERE");
							
							List<SMSEntity> allAdminSms = smsRepo.allAdminSms();

							if (!allAdminSms.isEmpty()) {

								LOGGER.info("ALL ADMIN SMS 2");
								model = allAdminSms.get(allAdminSms.size() - 1);
							} else {

								LOGGER.info("SMS CONFIG NOT FOUND 2");
								return smsConfigurationNotFound();
							}
						}

						String newMobileNo = signerModel.getSignerMobile();
						String[] smsString = { otp, SuperAdminSmsConfig.SMS_ADMIN_CONTACTNO };

						LOGGER.info("MOBILE    -- " + newMobileNo);
						LOGGER.info("TEMP CODE -- " + model.getSmsTempCode());
						LOGGER.info("SMS URL   -- " + model.getSmsServiceUrl());
						LOGGER.info("USER NAME -- " + model.getSmsUserName());
						LOGGER.info("PASSWORD  -- " + model.getSmsPassword());
						LOGGER.info("ENABLED   -- " + model.getSmsEnabled());

						smsService.sendSMSNotification(smsString, newMobileNo, model.getSmsTempCode(),
								model.getSmsServiceUrl(), model.getSmsUserName(), model.getSmsPassword(),
								model.getSmsEnabled());

						repository.save(signerModel);

						structure.setMessage("Otp Sent Successfully....!!!");
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(signerModel);
						structure.setFlag(1);

					} else {

						return balanceCheck;
					}
				}

			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
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

	private ResponseStructure smsConfigurationNotFound() throws Exception {

		ResponseStructure structure = new ResponseStructure();

		structure.setMessage("Sms Configuration is Empty");
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setData(null);
		structure.setFlag(3);

		return structure;
	}

	@Override
	public ResponseStructure verifyotp(int signerId, RequestModel users, HttpServletRequest request) {

		ResponseStructure structure = new ResponseStructure();

		boolean sign = false;

		try {

			SignerModel signerModel = repository.findBySignerId(signerId);

			String requestIdentifier = FileUtils.generateApiKeys(11);

			if (signerModel != null && !signerModel.isExpired() && !signerModel.getMerchantModel().isExpired()
					&& signerModel.getEntityModel().isSigningRequired()
					&& signerModel.getEntityModel().isGeneralSigning()) {

				ResponseStructure balanceCheck = signerUtils.balanceCheck(signerModel, requestIdentifier, false);

				if (balanceCheck.getFlag() == 1) {

					if (signerModel.getOtpcode() != null) {
						if (users.getOtpCode().equals(signerModel.getOtpcode())) {
							if (signerModel.getOtpExpiry().after(new Date())) {

								sign = addSignatureToPdf(signerModel, users , false);

								if (sign) {

									signerModel.setOtpVerificationStatus(true);
									signerModel.setSignedAt(DateUtil.dateFormat());
									String ipAddress = ipAndLocation.publicIpAddress();
									signerModel.setIpAddress(ipAddress);
									repository.save(signerModel);

									MerchantModel merchantModel = merchantRepository
											.findByMerchantId(signerModel.getMerchantModel().getMerchantId());
									List<SignerModel> list = repository.findByMerchantModel(merchantModel);

									int signerCount = 0;
									int signedCount = 0;
									for (SignerModel signer : list) {
										if (signer.isOtpVerificationStatus()) {
											signedCount = signedCount + 1;
										}
										signerCount = signerCount + 1;
									}

									if (signerCount == signedCount) {
										reportGenerate(merchantModel);
										for (SignerModel signer : list) {
											boolean report = emailService.finishedReport(merchantModel, signer,
													signer.getEntityModel());
										}
									}

									signerUtils.setSignerRequestToSuccess(signerModel, sign, "");

									structure.setMessage(AppConstants.OTP_VERIFIED);
									structure.setData(signerModel);
									structure.setFlag(1);
								} else {
									structure.setMessage(AppConstants.OTP_NOT_VERIFIED);
									structure.setData(null);
									structure.setFlag(2);
								}
							} else {
								structure.setMessage(AppConstants.OTP_EXPIRED);
								structure.setData(null);
								structure.setFlag(2);
							}
						} else {
							structure.setMessage(AppConstants.OTP_NOT_MATCHED);
							structure.setData(null);
							structure.setFlag(3);
						}
					} else {
						structure.setMessage("OTP Not Present");
						structure.setData(null);
						structure.setFlag(4);
					}

				} else {

					return balanceCheck;
				}

			} else {

				if (signerModel == null) {
					structure.setMessage(AppConstants.NO_DATA_FOUND);
				} else if (signerModel.getMerchantModel().isExpired()) {
					structure.setMessage("The Agreement has been expired");
				} else if (!signerModel.getEntityModel().isSigningRequired()) {
					structure.setMessage("Signing is disabled");
				} else if (!signerModel.getEntityModel().isGeneralSigning()) {
					structure.setMessage("General Signing is disabled");
				} else {
					structure.setMessage("This signer has been expelled from signing this document");
				}
				structure.setData(null);
				structure.setFlag(5);
			}
			structure.setStatusCode(HttpStatus.OK.value());

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

	private void reportGenerate(MerchantModel merchantModel) {
		List<SignerModel> model = repository.findByMerchantModel(merchantModel);

		int size = model.size();
		if (size == 1) {
			singleSigner(merchantModel, model);
		} else if (size == 2) {
			doubleSigner(merchantModel, model);
		} else if (size == 3) {
			threeSigner(merchantModel, model);
		} else if (size == 4) {
			fourSigners(merchantModel, model);
		}

	}

	private boolean addSignatureToPdf(SignerModel signerModel, RequestModel users, boolean aadhaarBased) throws IOException, ParseException {

		int count = 0;
		MerchantModel merchantModel = merchantRepository
				.findByMerchantId(signerModel.getMerchantModel().getMerchantId());

		List<SignerModel> list = repository.findByMerchantModel(merchantModel);
		for (SignerModel signer : list) {
			if (signer.isOtpVerificationStatus()) {
				count = count + 1;
			}
		}

		float x = 0.0f;
		float y = 0.0f;

		if (count == 0) {
			x = 5.0f;
			y = 10.0f;
		} else if (count == 1) {
			x = 152.0f;
			y = 10.0f;
		} else if (count == 2) {
			x = 299.0f;
			y = 10.0f;
		} else if (count == 3) {
			x = 446.0f;
			y = 10.0f;
		}
		String name = signerModel.getSignerName();

		// Get the current date and time in the desired format
		// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// String currentDateTime = dateFormat.format(new Date());
		// String concat=currentDateTime;
		String concat = DateUtil.dateFormat();
		// Create an image from the input data (name and date-time)
		BufferedImage signatureImage = createSignatureImage(name, concat,aadhaarBased);

		Resource resource = resourceLoader.getResource("/WEB-INF/agreement/" + merchantModel.getPdfDocument());
		String originalPdfPath = resource.getFile().getAbsolutePath();

		try (PDDocument document = PDDocument.load(new File(originalPdfPath))) {// ERROR HERE
			for (PDPage page : document.getPages()) {
				try (PDPageContentStream contentStream = new PDPageContentStream(document, page,
						PDPageContentStream.AppendMode.APPEND, true)) {
					// Convert the BufferedImage to a PDImageXObject
					PDImageXObject imageXObject = LosslessFactory.createFromImage(document, signatureImage);

					// Set the position (x, y) for your image
					contentStream.drawImage(imageXObject, x, y);

					// You can also add text if needed
					contentStream.setFont(PDType1Font.TIMES_ITALIC, 8);
					contentStream.beginText();
					contentStream.newLineAtOffset(x, y - 30);
					contentStream.showText(name);
					contentStream.endText();
				}
			}

			document.save(originalPdfPath); // Override the original PDF
			return true;
		} catch (IllegalArgumentException e) {
			// Handle the exception, or log it for debugging
			e.printStackTrace();
			return false;
		}

	}

	// Create an image from text data with watermark, underline for date-time, and
	// custom font for the human name
	private BufferedImage createSignatureImage(String name, String dateTime, boolean aadhaarBased) {
		int imageWidth = 142;
		int imageHeight = 42;

		BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();

		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// // Load the watermark image
		try {
			//Resource resource = resourceLoader.getResource("/WEB-INF/merchantpdf/FcGeneral.png");
			
			Resource resource = null;
			
			if(aadhaarBased) {
			
			 resource = resourceLoader.getResource("/WEB-INF/merchantpdf/FcAadhaar.png");
			
			}else {
				resource = resourceLoader.getResource("/WEB-INF/merchantpdf/FcGeneral.png");
			}
			
			String path = resource.getFile().getAbsolutePath();
			BufferedImage watermark = ImageIO.read(new File(path)); // Replace with the path to your watermark image
			int watermarkWidth = imageWidth;
			int watermarkHeight = imageHeight;
			graphics.drawImage(watermark, 0, 0, watermarkWidth, watermarkHeight, null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Font settings for the human name
		Font humanNameFont = new Font("Gabriola", Font.BOLD, 9); // Replace "CustomFont" with your desired font
		graphics.setFont(humanNameFont);
		int humanNameX = 5;
		int humanNameY = 15;
		graphics.setColor(Color.BLACK); // Set the color for the human name
		graphics.drawString(name, humanNameX, humanNameY);

		// Font settings for the name
		Font nameFont = new Font("Arial", Font.BOLD, 7);
		Map<TextAttribute, Object> attributes = new HashMap<>();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		Font underlinedName = nameFont.deriveFont(attributes);
		graphics.setFont(underlinedName);
		graphics.setColor(Color.BLACK);
		int nameX = 5;
		int nameY = 28;
		graphics.drawString(name, nameX, nameY);

		// Font settings for the date-time with underline
		Font dateTimeFont = new Font("Arial", Font.ITALIC, 7);

		graphics.setFont(dateTimeFont);
		int dateTimeY = 38;
		graphics.drawString(dateTime, nameX, dateTimeY);

		String sign = "";

		Font signBy = new Font("Arial", Font.BOLD, 6); // Replace "CustomFont" with your desired font
		graphics.setFont(signBy);
		int signX = 112;
		int signY = 33;
		graphics.setColor(Color.DARK_GRAY); // Set the color for the human name
		graphics.drawString(sign, signX, signY);

		String signCompany = "";

		Font signedCompany = new Font("Arial", Font.BOLD, 6); // Replace "CustomFont" with your desired font
		graphics.setFont(signedCompany);
		int signedX = 111;
		int signedY = 40;
		graphics.setColor(Color.DARK_GRAY); // Set the color for the human name
		graphics.drawString(signCompany, signedX, signedY);

		graphics.dispose();
		return image;
	}

	@Override
	public ResponseStructure emailTrigger(int id) {
		ResponseStructure structure = new ResponseStructure();
		try {
			MerchantModel merchant = merchantRepository.findByMerchantId(id);
			String merchantId = Integer.toString(merchant.getMerchantId());
			List<SignerModel> singerModel = repository.findByMerchantModel(merchant);
			int count = 0;
			int expiredSigner = 0;

			if (!merchant.isExpired()) {

				for (SignerModel model : singerModel) {
					String signerId = Integer.toString(model.getSignerId());

					if (!model.isExpired()) {

						emailService.signerRequest(merchantId, model.getSignerName(), merchant.getDocumentTitle(),
								model.getSignerMobile(), model.getSignerEmail(), model.getReferenceNumber(),
								merchant.getEntity());

						count++;
					} else {
						expiredSigner++;
					}

				}

				System.err.println("Count : " + count);
				System.err.println("exp Signers : " + expiredSigner);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.SUCCESS);
				structure.setData(singerModel);
				structure.setFlag(1);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("This Agreement has been expired");
				structure.setData(singerModel);
				structure.setFlag(1);
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

	public void singleSigner(MerchantModel merchant, List<SignerModel> model2) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Path con = Paths.get(context.getRealPath("/WEB-INF/"));
			EntityModel admin = merchant.getEntity();
			List<SignerModel> model = repository.findByMerchantModel(merchant);
			SignerModel signer = new SignerModel();
			for (SignerModel signerModel : model) {
				signer = signerModel;
			}

			String html = "<!DOCTYPE html>\r\n" + "<html lang=\"en\">\r\n" + "<head>\r\n"
					+ "    <meta charset=\"UTF-8\"/>\r\n"
					+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\r\n"
					+ "    <title>E-sign</title>\r\n" + "</head>\r\n" + "<body>\r\n"
					+ "    <table style=\"width:30%;text-align: left;width: 100%;\">\r\n" + "        <tr>\r\n"
					+ "            <td> <img src=\"resources/facheck.jpg\" alt=\"BasispayLogo\" style=\"width: 170px;height: 80px;object-fit: cover;\"/></td>\r\n"
					+ "            <td style=\"font-size: 25px; color: green;\">Certificate	of	Signature\r\n"
					+ "                Completion</td>\r\n" + "        </tr>\r\n" + "\r\n" + "    </table>\r\n"
					+ "  <div class=\"head1\" style=\"background-color: gray;color: white; height: 37; font-size: 31px;padding-left: 33px;\"> Document	details</div> \r\n"
					+ "  <table style=\"padding-left:39px;padding-top:33px;\">\r\n"
					+ "    <tr  style=\"text-align:left;\">\r\n"
					+ "      <td style=\"font-size:25px;\">Document ID</td>\r\n"
					+ "      <td style=\"padding-left: 10px;\">:</td>\r\n"
					+ "      <td style=\"padding-left: 10px; font-family: Verdana, Geneva, Tahoma, sans-serif;\">"
					+ merchant.getDocumentId() + "</td>\r\n" + "    </tr>\r\n" + "    <tr>\r\n"
					+ "      <td style=\"font-size:25px;\"> Document	Name</td>\r\n"
					+ "      <td style=\"padding-left: 10px;\">:</td>\r\n"
					+ "      <td style=\"padding-left: 10px; font-family: Verdana, Geneva, Tahoma, sans-serif;\">"
					+ merchant.getDocumentTitle() + "</td>\r\n" + "    </tr>\r\n"
					// + " <tr>\r\n"
					// + " <td style=\"font-size:25px;\"> Signature Algorithm</td>\r\n"
					// + " <td style=\"padding-left: 10px;\">:</td>\r\n"
					// + " <td style=\"padding-left: 10px; font-family: Verdana, Geneva, Tahoma,
					// sans-serif;\"> SHA-25 With RSA</td>\r\n"
					// + " </tr>\r\n"
					+ "  </table> \r\n"
					+ "  <div class=\"head1\" style=\"background-color: gray;color: white; margin-top: 1cm; font-size: 31px;padding-left: 33px;\"> Signer	details</div> \r\n"
					+ "  <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "    <tr style=\"text-align: left;\">\r\n" + "        <th>" + signer.getSignerName() + "</th>\r\n"
					+ "        <th>Signed	Time &#38; IP</th>\r\n" + "        <th> Signature	Method</th>\r\n"
					+ "    </tr>\r\n" + "    <tr style=\"text-align:left;\">\r\n" + "        <td> "
					+ signer.getSignerEmail() + "" + signer.getSignerMobile() + "(OTP:" + signer.getOtpcode()
					+ ")</td>\r\n" + "        <td>" + signer.getSignedAt() + " " + signer.getIpAddress() + "</td>\r\n"
					+ "        <td> Electronic	Signature by Facheck.com </td>\r\n" + "    </tr>\r\n" + "</table>\r\n"
					+ "<p style=\"padding-left:38px;\"><b>Consent &#38; Consent Id</b></p>\r\n" + "\r\n"
					+ "<p style=\"padding-left:38px;\"> I	understand	that	by	clicking	the	\"Sign	Now\"	button	I	would	be	electronically	signing	the	said\r\n"
					+ "        document.	I	have	read	and	understood	the	said	document.	I	agree	to	electronically	sign	all\r\n"
					+ "        the	pages	of	the	said	document	and	agree	to	be	bound	by	them.</p>\r\n"
					+ "    <b style=\"padding-left:38px;\"> ID:" + admin.getConsentId() + "</b>\r\n"
					+ "    <div class=\"head1\" style=\"background-color: gray;color: white;margin-top:1cm; font-size: 31px;padding-left: 33px\"> History</div>\r\n"
					+ "    \r\n" + "        <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "        <tr  style=\"text-align:left;\">\r\n" + "            <th>Document Created</th>\r\n"
					+ "            <td><b>" + admin.getName() + "</b></td>\r\n" + "            <td>"
					+ merchant.getUploadDocumentAt() + "</td>\r\n" + "        </tr>\r\n"
					+ "        <tr style=\"text-align:left;\">\r\n"
					+ "            <th> Invitation	Sent to Signers<br/></th>\r\n"
					+ "            <td style=\" padding-top: 10px\"><b>" + signer.getSignerName() + "</b><br/>"
					+ signer.getSignerMobile() + "</td>\r\n" + "            <td>" + signer.getSignedAt()
					+ "</td>            \r\n" + "        </tr>\r\n" + "         <tr style=\"text-align:left;\">\r\n"
					+ "                <th>Document Signed<br/></th>\r\n"
					+ "                <td style=\" padding-top: 10px\"><b>" + signer.getSignerName() + "</b><br/>"
					+ signer.getSignerMobile() + "</td>\r\n" + "                <td>" + signer.getSignedAt()
					+ "</td>            \r\n" + "            </tr>\r\n" + "         </table> \r\n"
					+ "        </body>\r\n" + "</html>";

			String generatedString = FileUtils.getRandomOrderNumer() + ".pdf";
			File fileName = new File(con + File.separator + "/samplecheck/" + StringUtils.cleanPath(generatedString));

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
						// logger.info("IOException",e);
					}
				}
			}
			structure.setMessage(generatedString);

			Resource firstResource = resourceLoader.getResource("/WEB-INF/agreement/" + merchant.getPdfDocument());
			String firstPdfPath = firstResource.getFile().getAbsolutePath();
			Resource secondResource = resourceLoader.getResource("/WEB-INF/samplecheck/" + generatedString);
			String secondPdfPath = secondResource.getFile().getAbsolutePath();

			// Load the first PDF file
			PDDocument firstDocument = PDDocument.load(new File(firstPdfPath),
					MemoryUsageSetting.setupMainMemoryOnly());

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

		} catch (Exception e) {
			e.printStackTrace();
		}
		// return null;
	}

	public void doubleSigner(MerchantModel merchant, List<SignerModel> model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Path con = Paths.get(context.getRealPath("/WEB-INF/"));
			EntityModel admin = merchant.getEntity();
			int size = model.size();
			int count = 1;
			SignerModel signer1 = new SignerModel();
			SignerModel signer2 = new SignerModel();
			SignerModel signer3 = new SignerModel();
			SignerModel signer4 = new SignerModel();

			for (SignerModel signer : model) {
				if (count == 1) {
					signer1 = signer;
					count++;
				} else if (count == 2) {
					signer2 = signer;
					count++;
				} else if (count == 3) {
					signer3 = signer;
					count++;
				} else if (count == 4) {
					signer4 = signer;
					count++;
				}
			}

			String html = "<!DOCTYPE html>\r\n" + "<html lang=\"en\">\r\n" + "<head>\r\n"
					+ "    <meta charset=\"UTF-8\"/>\r\n"
					+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\r\n"
					+ "    <title>E-sign</title>\r\n" + "</head>\r\n" + "<body>\r\n"
					+ "    <table style=\"width:30%;text-align: left;width: 100%;\">\r\n" + "        <tr>\r\n"
					+ "            <td> <img src=\"resources/facheck.jpg\" alt=\"BasispayLogo\" style=\"width: 170px;height: 80px;object-fit: cover;\"/></td>\r\n"
					+ "            <td style=\"font-size: 25px; color: green;\">Certificate	of	Signature\r\n"
					+ "                Completion</td>\r\n" + "        </tr>\r\n" + "\r\n" + "    </table>\r\n"
					+ "  <div class=\"head1\" style=\"background-color: gray;color: white; height: 37; font-size: 31px;padding-left: 33px;\"> Document	details</div> \r\n"
					+ "  <table style=\"padding-left:39px;padding-top:33px;\">\r\n"
					+ "    <tr  style=\"text-align:left;\">\r\n"
					+ "      <td style=\"font-size:25px;\">Document ID</td>\r\n"
					+ "      <td style=\"padding-left: 10px;\">:</td>\r\n"
					+ "      <td style=\"padding-left: 10px; font-family: Verdana, Geneva, Tahoma, sans-serif;\"> "
					+ merchant.getDocumentId() + "</td>\r\n" + "    </tr>\r\n" + "    <tr>\r\n"
					+ "      <td style=\"font-size:25px;\"> Document	Name</td>\r\n"
					+ "      <td style=\"padding-left: 10px;\">:</td>\r\n"
					+ "      <td style=\"padding-left: 10px; font-family: Verdana, Geneva, Tahoma, sans-serif;\">"
					+ merchant.getDocumentTitle() + "</td>\r\n" + "    </tr>\r\n"
					// + " <tr>\r\n"
					// + " <td style=\"font-size:25px;\"> Signature Algorithm</td>\r\n"
					// + " <td style=\"padding-left: 10px;\">:</td>\r\n"
					// + " <td style=\"padding-left: 10px; font-family: Verdana, Geneva, Tahoma,
					// sans-serif;\"> SHA-25 With RSA</td>\r\n"
					// + " </tr>\r\n"
					+ "  </table> \r\n"
					+ "  <div class=\"head1\" style=\"background-color: gray;color: white; margin-top: 1cm; font-size: 31px;padding-left: 33px;\"> Signer	details</div> \r\n"
					+ "  <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "    <tr style=\"text-align: left;\">\r\n" + "        <th>" + signer1.getSignerName()
					+ "</th>\r\n" + "        <th>Signed	Time &#38; IP</th>\r\n"
					+ "        <th> Signature	Method</th>\r\n" + "    </tr>\r\n"
					+ "    <tr style=\"text-align:left;\">\r\n" + "        <td> " + signer1.getSignerEmail() + " "
					+ signer1.getSignerMobile() + "(OTP:" + signer1.getOtpcode() + ")</td>\r\n" + "        <td>"
					+ signer1.getSignedAt() + " " + signer1.getIpAddress() + "</td>\r\n"
					+ "        <td> Electronic	Signature by FaCheck.com </td>\r\n" + "    </tr>\r\n" + "</table>\r\n"
					+ "<p style=\"padding-left:38px;\"><b>Consent &#38; Consent Id</b></p>\r\n" + "\r\n"
					+ "<p style=\"padding-left:38px;\"> I	understand	that	by	clicking	the	\"Sign	Now\"	button	I	would	be	electronically	signing	the	said\r\n"
					+ "        document.	I	have	read	and	understood	the	said	document.	I	agree	to	electronically	sign	all\r\n"
					+ "        the	pages	of	the	said	document	and	agree	to	be	bound	by	them.</p>\r\n"
					+ "    <b style=\"padding-left:38px;\"> ID:" + admin.getConsentId() + "</b>\r\n" + "    \r\n"
					+ "    <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "        <tr style=\"text-align: left;\">\r\n" + "            <th>" + signer2.getSignerName()
					+ "</th>\r\n" + "            <th>Signed	Time &#38; IP</th>\r\n"
					+ "            <th> Signature	Method</th>\r\n" + "        </tr>\r\n"
					+ "        <tr style=\"text-align:left;\">\r\n" + "            <td>" + signer2.getSignerEmail()
					+ " " + signer2.getSignerMobile() + "(OTP:" + signer2.getOtpcode() + ")</td>\r\n"
					+ "            <td>" + signer2.getSignedAt() + " " + signer2.getIpAddress() + "</td>\r\n"
					+ "            <td> Electronic	Signature by Facheck.com </td>\r\n" + "        </tr>\r\n"
					+ "    </table>\r\n" + "    \r\n"
					+ "    <p style=\"padding-left:38px;\"><b>Consent &#38; Consent Id</b></p>\r\n" + "    \r\n"
					+ "    <p style=\"padding-left:38px;\"> I	understand	that	by	clicking	the	\"Sign	Now\"	button	I	would	be	electronically	signing	the	said\r\n"
					+ "        document.	I	have	read	and	understood	the	said	document.	I	agree	to	electronically	sign	all\r\n"
					+ "        the	pages	of	the	said	document	and	agree	to	be	bound	by	them.</p>\r\n"
					+ "   \r\n" + "        <b style=\"padding-left:38px;\"> ID:" + admin.getConsentId() + "</b>\r\n"
					+ "       \r\n" + "        \r\n" + "       \r\n" + "            \r\n" + "            \r\n"
					+ "       \r\n" + "            \r\n" + "    \r\n"
					+ "        <div class=\"head1\" style=\"background-color: gray;color: white; height: 37; font-size: 31px;padding-left: 33px;margin-top: 5cm;\"> History</div>\r\n"
					+ "    \r\n" + "        <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "        <tr  style=\"text-align:left;\">\r\n" + "            <th>Document Created</th>\r\n"
					+ "            <td><b>" + admin.getName() + "</b></td>\r\n" + "            <td>"
					+ merchant.getUploadDocumentAt() + "</td>\r\n" + "        </tr>\r\n"
					+ "        <tr style=\"text-align:left;\">\r\n"
					+ "            <th> Invitation	Sent to Signers<br/></th>\r\n"
					+ "            <td style=\" padding-top: 10px\"><b>" + signer1.getSignerName() + "</b><br/>"
					+ signer1.getSignerEmail() + "</td>\r\n" + "            <td>" + signer1.getSignedAt()
					+ "</td>            \r\n" + "        </tr>\r\n" + "        <tr style=\"text-align:left;\">\r\n"
					+ "            <th></th>\r\n" + "            <td style=\" padding-top: 10px\"><b>"
					+ signer2.getSignerName() + "</b><br/>" + signer2.getSignerEmail() + "</td>\r\n"
					+ "            <td>" + signer2.getSignedAt() + "</td> \r\n" + "            \r\n"
					+ "        </tr>\r\n" + "       \r\n" + "       \r\n" + "      \r\n"
					+ "            <tr style=\"text-align:left;\">\r\n"
					+ "                <th>Document Signed<br/></th>\r\n"
					+ "                <td style=\" padding-top: 10px\"><b>" + signer1.getSignerName() + "</b><br/>"
					+ signer1.getSignerEmail() + "</td>\r\n" + "                <td>" + signer1.getSignedAt()
					+ "</td>            \r\n" + "            </tr>\r\n"
					+ "            <tr style=\"text-align:left;\">\r\n" + "                <th></th>\r\n"
					+ "                <td style=\" padding-top: 10px\"><b>" + signer2.getSignerName() + "</b><br/>"
					+ signer2.getSignerEmail() + "</td>\r\n" + "                <td>" + signer2.getSignedAt()
					+ "</td> \r\n" + "                \r\n" + "            </tr>\r\n" + "          \r\n"
					+ "            \r\n" + "                \r\n" + "           \r\n" + "         </table> \r\n"
					+ "\r\n" + "       \r\n" + "    \r\n" + "    \r\n" + "\r\n" + "</body>\r\n" + "</html>";

			String generatedString = FileUtils.getRandomOrderNumer() + ".pdf";
			File fileName = new File(con + File.separator + "/samplecheck/" + StringUtils.cleanPath(generatedString));

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
						// logger.info("IOException",e);
					}
				}
			}
			structure.setMessage(generatedString);

			Resource firstResource = resourceLoader.getResource("/WEB-INF/agreement/" + merchant.getPdfDocument());
			String firstPdfPath = firstResource.getFile().getAbsolutePath();
			Resource secondResource = resourceLoader.getResource("/WEB-INF/samplecheck/" + generatedString);
			String secondPdfPath = secondResource.getFile().getAbsolutePath();

			// Load the first PDF file
			PDDocument firstDocument = PDDocument.load(new File(firstPdfPath),
					MemoryUsageSetting.setupMainMemoryOnly());

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

		} catch (Exception e) {
			e.printStackTrace();
		}
		// return null;
	}

	private void fourSigners(MerchantModel merchant, List<SignerModel> model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Path con = Paths.get(context.getRealPath("/WEB-INF/"));
			EntityModel admin = merchant.getEntity();
			// List<SignerModel> model = repository.findByMerchantModel(merchant);
			int size = model.size();
			int count = 1;
			SignerModel signer1 = new SignerModel();
			SignerModel signer2 = new SignerModel();
			SignerModel signer3 = new SignerModel();
			SignerModel signer4 = new SignerModel();

			for (SignerModel signer : model) {
				if (count == 1) {
					signer1 = signer;
					count++;
				} else if (count == 2) {
					signer2 = signer;
					count++;
				} else if (count == 3) {
					signer3 = signer;
					count++;
				} else if (count == 4) {
					signer4 = signer;
					count++;
				}
			}

			String html = "<!DOCTYPE html>\r\n" + "<html lang=\"en\">\r\n" + "<head>\r\n"
					+ "    <meta charset=\"UTF-8\"/>\r\n"
					+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\r\n"
					+ "    <title>E-sign</title>\r\n" + "</head>\r\n" + "<body>\r\n"
					+ "    <table style=\"width:30%;text-align: left;width: 100%;\">\r\n" + "        <tr>\r\n"
					+ "            <td> <img src=\"resources/facheck.jpg\" alt=\"BasispayLogo\" style=\"width: 170px;height: 80px;object-fit: cover;\"/></td>\r\n"
					+ "            <td style=\"font-size: 25px; color: green;\">Certificate	of	Signature\r\n"
					+ "                Completion</td>\r\n" + "        </tr>\r\n" + "\r\n" + "    </table>\r\n"
					+ "  <div class=\"head1\" style=\"background-color: gray;color: white; height: 37; font-size: 31px;padding-left: 33px;\"> Document	details</div> \r\n"
					+ "  <table style=\"padding-left:39px;padding-top:33px;\">\r\n"
					+ "    <tr  style=\"text-align:left;\">\r\n"
					+ "      <td style=\"font-size:25px;\">Document ID</td>\r\n"
					+ "      <td style=\"padding-left: 10px;\">:</td>\r\n"
					+ "      <td style=\"padding-left: 10px; font-family: Verdana, Geneva, Tahoma, sans-serif;\">"
					+ merchant.getDocumentId() + "</td>\r\n" + "    </tr>\r\n" + "    <tr>\r\n"
					+ "      <td style=\"font-size:25px;\"> Document	Name</td>\r\n"
					+ "      <td style=\"padding-left: 10px;\">:</td>\r\n"
					+ "      <td style=\"padding-left: 10px; font-family: Verdana, Geneva, Tahoma, sans-serif;\">"
					+ merchant.getDocumentTitle() + "</td>\r\n" + "    </tr>\r\n"
					// + " <tr>\r\n"
					// + " <td style=\"font-size:25px;\"> Signature Algorithm</td>\r\n"
					// + " <td style=\"padding-left: 10px;\">:</td>\r\n"
					// + " <td style=\"padding-left: 10px; font-family: Verdana, Geneva, Tahoma,
					// sans-serif;\"> SHA-25 With RSA</td>\r\n"
					// + " </tr>\r\n"
					+ "  </table> \r\n"
					+ "  <div class=\"head1\" style=\"background-color: gray;color: white; margin-top: 1cm; font-size: 31px;padding-left: 33px;\"> Signer	details</div> \r\n"
					+ "  <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "    <tr style=\"text-align: left;\">\r\n" + "        <th>" + signer1.getSignerName()
					+ "</th>\r\n" + "        <th>Signed	Time &#38; IP</th>\r\n"
					+ "        <th> Signature	Method</th>\r\n" + "    </tr>\r\n"
					+ "    <tr style=\"text-align:left;\">\r\n" + "        <td>" + signer1.getSignerEmail() + ""
					+ signer1.getSignerMobile() + "(OTP:" + signer1.getOtpcode() + ")</td>\r\n" + "        <td>"
					+ signer1.getSignedAt() + " " + signer1.getIpAddress() + "</td>\r\n"
					+ "        <td> Electronic	Signature by Facheck.com</td>\r\n" + "    </tr>\r\n" + "</table>\r\n"
					+ "<p style=\"padding-left:38px;\"><b>Consent &#38; Consent Id</b></p>\r\n" + "\r\n"
					+ "<p style=\"padding-left:38px;\"> I	understand	that	by	clicking	the	\"Sign	Now\"	button	I	would	be	electronically	signing	the	said\r\n"
					+ "        document.	I	have	read	and	understood	the	said	document.	I	agree	to	electronically	sign	all\r\n"
					+ "        the	pages	of	the	said	document	and	agree	to	be	bound	by	them.</p>\r\n"
					+ "    <b style=\"padding-left:38px;\"> ID:" + admin.getConsentId() + "</b>\r\n" + "    \r\n"
					+ "    <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "        <tr style=\"text-align: left;\">\r\n" + "            <th>" + signer2.getSignerName()
					+ "</th>\r\n" + "            <th>Signed	Time &#38; IP</th>\r\n"
					+ "            <th> Signature	Method</th>\r\n" + "        </tr>\r\n"
					+ "        <tr style=\"text-align:left;\">\r\n" + "            <td>" + signer2.getSignerEmail()
					+ " " + signer2.getSignerMobile() + "(OTP:" + signer2.getOtpcode() + ")</td>\r\n"
					+ "            <td>" + signer2.getSignedAt() + " " + signer2.getIpAddress() + "</td>\r\n"
					+ "            <td> Electronic	Signature by Facheck.com</td>\r\n" + "        </tr>\r\n"
					+ "    </table>\r\n" + "    \r\n"
					+ "    <p style=\"padding-left:38px;\"><b>Consent &#38; Consent Id</b></p>\r\n" + "    \r\n"
					+ "    <p style=\"padding-left:38px;\"> I	understand	that	by	clicking	the	\"Sign	Now\"	button	I	would	be	electronically	signing	the	said\r\n"
					+ "        document.	I	have	read	and	understood	the	said	document.	I	agree	to	electronically	sign	all\r\n"
					+ "        the	pages	of	the	said	document	and	agree	to	be	bound	by	them.</p>\r\n"
					+ "   \r\n" + "        <b style=\"padding-left:38px;\"> ID:" + admin.getConsentId() + "</b>\r\n"
					+ "        <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "            <tr style=\"text-align: left;\">\r\n" + "                <th>"
					+ signer3.getSignerName() + "</th>\r\n" + "                <th>Signed	Time &#38; IP</th>\r\n"
					+ "                <th> Signature	Method</th>\r\n" + "            </tr>\r\n"
					+ "            <tr style=\"text-align:left;\">\r\n" + "                <td>"
					+ signer3.getSignerEmail() + " " + signer3.getSignerMobile() + "(OTP:" + signer3.getOtpcode()
					+ ")</td>\r\n" + "                <td>" + signer3.getSignedAt() + " " + signer3.getIpAddress()
					+ "</td>\r\n" + "                <td>Electronic	Signature by Facheck.com</td>\r\n"
					+ "            </tr>\r\n" + "        </table>\r\n" + "        \r\n"
					+ "        <p style=\"padding-left:38px;\"><b>Consent &#38; Consent Id</b></p>\r\n" + "        \r\n"
					+ "        <p style=\"padding-left:38px;\">I	understand	that	by	clicking	the	\"Sign	Now\"	button	I	would	be	electronically	signing	the	said\r\n"
					+ "            document.	I	have	read	and	understood	the	said	document.	I	agree	to	electronically	sign	all\r\n"
					+ "            the	pages	of	the	said	document	and	agree	to	be	bound	by	them.</p>\r\n"
					+ "       \r\n" + "            <b style=\"padding-left:38px;\"> ID:" + admin.getConsentId()
					+ "</b>\r\n" + "            <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "                <tr style=\"text-align: left;\">\r\n" + "                    <th>"
					+ signer4.getSignerName() + "</th>\r\n" + "                    <th>Signed	Time &#38; IP</th>\r\n"
					+ "                    <th> Signature	Method</th>\r\n" + "                </tr>\r\n"
					+ "                <tr style=\"text-align:left;\">\r\n" + "                    <td>"
					+ signer4.getSignerEmail() + "" + signer4.getSignerMobile() + "(OTP:" + signer4.getOtpcode()
					+ ")</td>\r\n" + "            <td>" + signer4.getSignedAt() + " " + signer4.getIpAddress()
					+ "</td>\r\n" + "            <td>Electronic	Signature by Facheck.com</td>\r\n"
					+ "                </tr>\r\n" + "            </table>\r\n" + "            \r\n"
					+ "            <p style=\"padding-left:38px;\"><b>Consent &#38; Consent Id</b></p>\r\n"
					+ "            \r\n"
					+ "            <p style=\"padding-left:38px;\">I	understand	that	by	clicking	the	\"Sign	Now\"	button	I	would	be	electronically	signing	the	said\r\n"
					+ "                document.	I	have	read	and	understood	the	said	document.	I	agree	to	electronically	sign	all\r\n"
					+ "                the	pages	of	the	said	document	and	agree	to	be	bound	by	them.</p>\r\n"
					+ "           \r\n" + "                <b style=\"padding-left:38px;\"> ID:" + admin.getConsentId()
					+ "</b>\r\n" + "       \r\n" + "        \r\n" + "       \r\n" + "            \r\n" + "    \r\n"
					+ "        <div class=\"head1\" style=\"background-color: gray;color: white; height: 37; font-size: 31px;padding-left: 33px;margin-top: 2cm;\"> History</div>\r\n"
					+ "    \r\n" + "        <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "        <tr  style=\"text-align:left;\">\r\n" + "            <th>Document Created</th>\r\n"
					+ "            <td><b>" + admin.getName() + "</b></td>\r\n" + "            <td>"
					+ merchant.getUploadDocumentAt() + "</td>\r\n" + "        </tr>\r\n"
					+ "        <tr style=\"text-align:left;\">\r\n"
					+ "            <th> Invitation	Sent to Signers<br/></th>\r\n"
					+ "            <td style=\" padding-top: 10px\"><b>" + signer1.getSignerName() + "</b><br/>"
					+ signer1.getSignerEmail() + "</td>\r\n" + "            <td>" + signer1.getSignedAt()
					+ "</td>            \r\n" + "        </tr>\r\n" + "        <tr style=\"text-align:left;\">\r\n"
					+ "            <th></th>\r\n" + "            <td style=\" padding-top: 10px\"><b>"
					+ signer2.getSignerName() + "</b><br/>" + signer2.getSignerEmail() + "</td>\r\n"
					+ "            <td>" + signer2.getSignedAt() + "</td> \r\n" + "            \r\n"
					+ "        </tr>\r\n" + "        <tr style=\"text-align:left;\">\r\n" + "            <th></th>\r\n"
					+ "            <td style=\" padding-top: 10px\"><b>" + signer3.getSignerName() + "</b><br/>"
					+ signer3.getSignerEmail() + "</td>\r\n" + "            <td>" + signer3.getSignedAt() + "</td> \r\n"
					+ "            \r\n" + "        </tr>\r\n" + "        <tr style=\"text-align:left;\">\r\n"
					+ "            <th></th>\r\n" + "            <td style=\" padding-top: 10px\"><b>"
					+ signer4.getSignerName() + "</b><br/>" + signer4.getSignerEmail() + "</td>\r\n"
					+ "            <td>" + signer4.getSignedAt() + "</td> \r\n" + "            \r\n"
					+ "        </tr>\r\n" + "      \r\n" + "            <tr style=\"text-align:left;\">\r\n"
					+ "                <th>Document Signed<br/></th>\r\n"
					+ "                <td style=\" padding-top: 10px\"><b>" + signer1.getSignerName() + "</b><br/>"
					+ signer1.getSignerEmail() + "</td>\r\n" + "                <td>" + signer1.getSignedAt()
					+ "</td>            \r\n" + "            </tr>\r\n"
					+ "            <tr style=\"text-align:left;\">\r\n" + "                <th></th>\r\n"
					+ "                <td style=\" padding-top: 10px\"><b>" + signer2.getSignerName() + "</b><br/>"
					+ signer2.getSignerEmail() + "</td>\r\n" + "                <td>" + signer2.getSignedAt()
					+ "</td> \r\n" + "                \r\n" + "            </tr>\r\n"
					+ "            <tr style=\"text-align:left;\">\r\n" + "                <th></th>\r\n"
					+ "                <td style=\" padding-top: 10px\"><b>" + signer3.getSignerName() + "</b><br/>"
					+ signer3.getSignerEmail() + "</td>\r\n" + "                <td>" + signer3.getSignedAt()
					+ "</td> \r\n" + "                \r\n" + "            </tr>\r\n"
					+ "            <tr style=\"text-align:left;\">\r\n" + "                <th></th>\r\n"
					+ "                <td style=\" padding-top: 10px\"><b>" + signer4.getSignerName() + "</b><br/>"
					+ signer4.getSignerEmail() + "</td>\r\n" + "                <td>" + signer4.getSignedAt()
					+ "</td> \r\n" + "                \r\n" + "            </tr>\r\n" + "                \r\n"
					+ "           \r\n" + "         </table> \r\n" + "\r\n" + "       \r\n" + "    \r\n" + "    \r\n"
					+ "\r\n" + "</body>\r\n" + "</html>";

			String generatedString = FileUtils.getRandomOrderNumer() + ".pdf";
			File fileName = new File(con + File.separator + "/samplecheck/" + StringUtils.cleanPath(generatedString));

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
						// logger.info("IOException",e);
					}
				}
			}
			structure.setMessage(generatedString);

			Resource firstResource = resourceLoader.getResource("/WEB-INF/agreement/" + merchant.getPdfDocument());
			String firstPdfPath = firstResource.getFile().getAbsolutePath();
			Resource secondResource = resourceLoader.getResource("/WEB-INF/samplecheck/" + generatedString);
			String secondPdfPath = secondResource.getFile().getAbsolutePath();

			// Load the first PDF file
			PDDocument firstDocument = PDDocument.load(new File(firstPdfPath),
					MemoryUsageSetting.setupMainMemoryOnly());

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

		} catch (Exception e) {
			e.printStackTrace();
		}
		// return null;

	}

	private void threeSigner(MerchantModel merchant, List<SignerModel> model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Path con = Paths.get(context.getRealPath("/WEB-INF/"));
			EntityModel admin = merchant.getEntity();
			// List<SignerModel> model = repository.findByMerchantModel(merchant);
			int size = model.size();
			int count = 1;
			SignerModel signer1 = new SignerModel();
			SignerModel signer2 = new SignerModel();
			SignerModel signer3 = new SignerModel();
			SignerModel signer4 = new SignerModel();

			for (SignerModel signer : model) {
				if (count == 1) {
					signer1 = signer;
					count++;
				} else if (count == 2) {
					signer2 = signer;
					count++;
				} else if (count == 3) {
					signer3 = signer;
					count++;
				} else if (count == 4) {
					signer4 = signer;
					count++;
				}
			}

			String html = "<!DOCTYPE html>\r\n" + "<html lang=\"en\">\r\n" + "<head>\r\n"
					+ "    <meta charset=\"UTF-8\"/>\r\n"
					+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\r\n"
					+ "    <title>E-sign</title>\r\n" + "</head>\r\n" + "<body>\r\n"
					+ "    <table style=\"width:30%;text-align: left;width: 100%;\">\r\n" + "        <tr>\r\n"
					+ "            <td> <img src=\"resources/facheck.jpg\" alt=\"FacheckLogo\" style=\"width: 170px;height: 80px;object-fit: cover;\"/></td>\r\n"
					+ "            <td style=\"font-size: 25px; color: green;\">Certificate	of	Signature\r\n"
					+ "                Completion</td>\r\n" + "        </tr>\r\n" + "\r\n" + "    </table>\r\n"
					+ "  <div class=\"head1\" style=\"background-color: gray;color: white; height: 37; font-size: 31px;padding-left: 33px;\"> Document	details</div> \r\n"
					+ "  <table style=\"padding-left:39px;padding-top:33px;\">\r\n"
					+ "    <tr  style=\"text-align:left;\">\r\n"
					+ "      <td style=\"font-size:25px;\">Document ID</td>\r\n"
					+ "      <td style=\"padding-left: 10px;\">:</td>\r\n"
					+ "      <td style=\"padding-left: 10px; font-family: Verdana, Geneva, Tahoma, sans-serif;\">"
					+ merchant.getDocumentId() + "</td>\r\n" + "    </tr>\r\n" + "    <tr>\r\n"
					+ "      <td style=\"font-size:25px;\"> Document	Name</td>\r\n"
					+ "      <td style=\"padding-left: 10px;\">:</td>\r\n"
					+ "      <td style=\"padding-left: 10px; font-family: Verdana, Geneva, Tahoma, sans-serif;\">"
					+ merchant.getDocumentTitle() + "</td>\r\n" + "    </tr>\r\n"
					// + " <tr>\r\n"
					// + " <td style=\"font-size:25px;\"> Signature Algorithm</td>\r\n"
					// + " <td style=\"padding-left: 10px;\">:</td>\r\n"
					// + " <td style=\"padding-left: 10px; font-family: Verdana, Geneva, Tahoma,
					// sans-serif;\"> SHA-25 With RSA</td>\r\n"
					// + " </tr>\r\n"
					+ "  </table> \r\n"
					+ "  <div class=\"head1\" style=\"background-color: gray;color: white; margin-top: 1cm; font-size: 31px;padding-left: 33px;\"> Signer	details</div> \r\n"
					+ "  <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "    <tr style=\"text-align: left;\">\r\n" + "        <th>" + signer1.getSignerName()
					+ "</th>\r\n" + "        <th>Signed	Time &#38; IP</th>\r\n"
					+ "        <th> Signature	Method</th>\r\n" + "    </tr>\r\n"
					+ "    <tr style=\"text-align:left;\">\r\n" + "        <td>" + signer1.getSignerEmail() + " "
					+ signer1.getSignerMobile() + "(OTP:" + signer1.getOtpcode() + ")</td>\r\n" + "        <td>"
					+ signer1.getSignedAt() + " " + signer1.getIpAddress() + "</td>\r\n"
					+ "        <td> Electronic	Signature by Facheck.com </td>\r\n" + "    </tr>\r\n" + "</table>\r\n"
					+ "<p style=\"padding-left:38px;\"><b>Consent &#38; Consent Id</b></p>\r\n" + "\r\n"
					+ "<p style=\"padding-left:38px;\"> I	understand	that	by	clicking	the	\"Sign	Now\"	button	I	would	be	electronically	signing	the	said\r\n"
					+ "        document.	I	have	read	and	understood	the	said	document.	I	agree	to	electronically	sign	all\r\n"
					+ "        the	pages	of	the	said	document	and	agree	to	be	bound	by	them.</p>\r\n"
					+ "    <b style=\"padding-left:38px;\"> ID:" + admin.getConsentId() + "</b>\r\n" + "    \r\n"
					+ "    <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "        <tr style=\"text-align: left;\">\r\n" + "            <th>" + signer2.getSignerName()
					+ "</th>\r\n" + "            <th>Signed	Time &#38; IP</th>\r\n"
					+ "            <th> Signature	Method</th>\r\n" + "        </tr>\r\n"
					+ "        <tr style=\"text-align:left;\">\r\n" + "            <td>" + signer2.getSignerEmail()
					+ " " + signer2.getSignerMobile() + "(OTP:" + signer2.getOtpcode() + ")</td>\r\n"
					+ "            <td>" + signer2.getSignedAt() + " " + signer2.getIpAddress() + "</td>\r\n"
					+ "            <td> Electronic	Signature by Facheck.com</td>\r\n" + "        </tr>\r\n"
					+ "    </table>\r\n" + "    \r\n"
					+ "    <p style=\"padding-left:38px;\"><b>Consent &#38; Consent Id</b></p>\r\n" + "    \r\n"
					+ "    <p style=\"padding-left:38px;\"> I	understand	that	by	clicking	the	\"Sign	Now\"	button	I	would	be	electronically	signing	the	said\r\n"
					+ "        document.	I	have	read	and	understood	the	said	document.	I	agree	to	electronically	sign	all\r\n"
					+ "        the	pages	of	the	said	document	and	agree	to	be	bound	by	them.</p>\r\n"
					+ "   \r\n" + "        <b style=\"padding-left:38px;\"> ID:" + admin.getConsentId() + "</b>\r\n"
					+ "        <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "            <tr style=\"text-align: left;\">\r\n" + "                <th>"
					+ signer3.getSignerName() + "</th>\r\n" + "                <th>Signed	Time &#38; IP</th>\r\n"
					+ "                <th> Signature	Method</th>\r\n" + "            </tr>\r\n"
					+ "            <tr style=\"text-align:left;\">\r\n" + "                <td>"
					+ signer3.getSignerEmail() + " " + signer3.getSignerMobile() + "(OTP:" + signer3.getOtpcode()
					+ ")</td>\r\n" + "                <td>" + signer3.getSignedAt() + " " + signer3.getIpAddress()
					+ "</td>\r\n" + "                <td> Electronic	Signature by Facheck.com</td>\r\n"
					+ "            </tr>\r\n" + "        </table>\r\n" + "        \r\n"
					+ "        <p style=\"padding-left:38px;\"><b>Consent &#38; Consent Id</b></p>\r\n" + "        \r\n"
					+ "        <p style=\"padding-left:38px;\">I	understand	that	by	clicking	the	\"Sign	Now\"	button	I	would	be	electronically	signing	the	said\r\n"
					+ "            document.	I	have	read	and	understood	the	said	document.	I	agree	to	electronically	sign	all\r\n"
					+ "            the	pages	of	the	said	document	and	agree	to	be	bound	by	them.</p>\r\n"
					+ "       \r\n" + "            <b style=\"padding-left:38px;\"> ID:" + admin.getConsentId()
					+ "</b>\r\n" + "           \r\n" + "            \r\n" + "            \r\n" + "       \r\n"
					+ "            \r\n" + "    \r\n"
					+ "        <div class=\"head1\" style=\"background-color: gray;color: white; height: 37; font-size: 31px;padding-left: 33px;margin-top: 2cm;\"> History</div>\r\n"
					+ "    \r\n" + "        <table style=\"padding-left:39px;padding-top:33px;width: 100%;\">\r\n"
					+ "        <tr  style=\"text-align:left;\">\r\n" + "            <th>Document Created</th>\r\n"
					+ "            <td><b>" + admin.getName() + "</b></td>\r\n" + "            <td>"
					+ merchant.getUploadDocumentAt() + "</td>\r\n" + "        </tr>\r\n"
					+ "        <tr style=\"text-align:left;\">\r\n"
					+ "            <th> Invitation	Sent to Signers<br/></th>\r\n"
					+ "            <td style=\" padding-top: 10px\"><b>" + signer1.getSignerName() + "</b><br/>"
					+ signer1.getSignerEmail() + "</td>\r\n" + "            <td>" + signer1.getSignedAt()
					+ "</td>            \r\n" + "        </tr>\r\n" + "        <tr style=\"text-align:left;\">\r\n"
					+ "            <th></th>\r\n" + "            <td style=\" padding-top: 10px\"><b>"
					+ signer2.getSignerName() + "</b><br/>" + signer2.getSignerEmail() + "</td>\r\n"
					+ "            <td>" + signer2.getSignedAt() + "</td> \r\n" + "            \r\n"
					+ "        </tr>\r\n" + "        <tr style=\"text-align:left;\">\r\n" + "            <th></th>\r\n"
					+ "            <td style=\" padding-top: 10px\"><b>" + signer3.getSignerName() + "</b><br/>"
					+ signer3.getSignerEmail() + "</td>\r\n" + "            <td>" + signer3.getSignedAt() + "</td> \r\n"
					+ "            \r\n" + "        </tr>\r\n" + "       \r\n" + "      \r\n"
					+ "            <tr style=\"text-align:left;\">\r\n"
					+ "                <th>Document Signed<br/></th>\r\n"
					+ "                <td style=\" padding-top: 10px\"><b>" + signer1.getSignerName() + "</b><br/>"
					+ signer1.getSignerEmail() + "</td>\r\n" + "                <td>" + signer1.getSignedAt()
					+ "</td>            \r\n" + "            </tr>\r\n"
					+ "            <tr style=\"text-align:left;\">\r\n" + "                <th></th>\r\n"
					+ "                <td style=\" padding-top: 10px\"><b>" + signer2.getSignerName() + "</b><br/>"
					+ signer2.getSignerEmail() + "</td>\r\n" + "                <td>" + signer2.getSignedAt()
					+ "</td> \r\n" + "                \r\n" + "            </tr>\r\n"
					+ "            <tr style=\"text-align:left;\">\r\n" + "                <th></th>\r\n"
					+ "                <td style=\" padding-top: 10px\"><b>" + signer3.getSignerName() + "</b><br/>"
					+ signer3.getSignerEmail() + "</td>\r\n" + "                <td>" + signer3.getSignedAt()
					+ "</td> \r\n" + "                \r\n" + "            </tr>\r\n" + "            \r\n"
					+ "                \r\n" + "           \r\n" + "         </table> \r\n" + "\r\n" + "       \r\n"
					+ "    \r\n" + "    \r\n" + "\r\n" + "</body>\r\n" + "</html>";

			String generatedString = FileUtils.getRandomOrderNumer() + ".pdf";
			File fileName = new File(con + File.separator + "/samplecheck/" + StringUtils.cleanPath(generatedString));

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
						// logger.info("IOException",e);
					}
				}
			}
			structure.setMessage(generatedString);

			Resource firstResource = resourceLoader.getResource("/WEB-INF/agreement/" + merchant.getPdfDocument());
			String firstPdfPath = firstResource.getFile().getAbsolutePath();
			Resource secondResource = resourceLoader.getResource("/WEB-INF/samplecheck/" + generatedString);
			String secondPdfPath = secondResource.getFile().getAbsolutePath();

			// Load the first PDF file
			PDDocument firstDocument = PDDocument.load(new File(firstPdfPath),
					MemoryUsageSetting.setupMainMemoryOnly());

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

		} catch (Exception e) {
			e.printStackTrace();
		}
		// return null;

	}

	@Override
	public ResponseStructure viewByMerechantId(int merchantId) {

		ResponseStructure structure = new ResponseStructure();
		try {
			MerchantModel model = merchantRepository.findByMerchantId(merchantId);
			if (model != null) {
				List<SignerModel> signers = repository.findByMerchantModel(model);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(signers);
				structure.setMessage("Signer Details are ");
				structure.setFlag(1);
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setMessage("Merchant Details not found ");
				structure.setFlag(2);
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
	public ResponseStructure inviteSigner(int id) {

		ResponseStructure structure = new ResponseStructure();
		try {
			SignerModel model = repository.findBySignerId(id);
			MerchantModel merchant = model.getMerchantModel();
			int id2 = merchant.getMerchantId();
			String merchantId = Integer.toString(id2);
			String referenceId = model.getReferenceNumber();

			emailService.signerRequest(merchantId, model.getSignerName(), merchant.getDocumentTitle(),
					model.getSignerMobile(), model.getSignerEmail(), referenceId, merchant.getEntity());

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(model);
			structure.setMessage("Signer Invited Successfully... ");
			structure.setFlag(1);
		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
		}
		return structure;
	}

	@Override
	public ResponseStructure deleteSigner(int signerId) {

		ResponseStructure structure = new ResponseStructure();
		try {
//			SignerModel model=repository.deleteBySignerId(signerId);
////			SignerModel signerModel = repository.findBySignerId(signerId);
			repository.deleteById(signerId);
			structure.setStatusCode(HttpStatus.OK.value());
//			structure.setData(model);
			structure.setMessage("Signer Deleted Successfully... ");
			structure.setFlag(1);
		} catch (Exception e) {
			e.printStackTrace();
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
		}
		return structure;
	}

	@Override
	public ResponseStructure viewByUserId(int userId) {

		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel model = userRepository.findByUserId(userId);

			if (model != null) {

				List<SignerModel> signers = repository.findByEntityModel(model);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(signers);
				structure.setMessage("Signer Details for the given entity are");
				structure.setFlag(1);

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setMessage("Merchant Details not found ");
				structure.setFlag(2);
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
	public ResponseStructure viewByMerchantAndUser(int merchantId, int userId) {

		ResponseStructure structure = new ResponseStructure();
		try {
			MerchantModel merchant = merchantRepository.findByMerchantId(merchantId);
			EntityModel entity = userRepository.findByUserId(userId);

			if (merchant != null && entity != null) {
				List<SignerModel> signers = repository.findByMerchantModelAndEntityModel(merchant, entity);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(signers);
				structure.setMessage("Signer Details are ");
				structure.setFlag(1);
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setMessage("Merchant Details not found ");
				structure.setFlag(2);
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
	public ResponseStructure importExcelForSigner() {

		ResponseStructure structure = new ResponseStructure();

		List<String> list = new ArrayList();

		list.add("Signer_Name");
		list.add("Signer_Mobile");
		list.add("Signer_Email");
		list.add("Bond_Payer");
		list.add("Agreement_Id");

		structure.setData(list);
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setMessage("SIGNER EXCEL FIELDS CREATED SUCCESSFULLY");

		return structure;
	}

	@Override
	public ResponseStructure signerExpire(RequestModel model, int signerId) {

		ResponseStructure structure = new ResponseStructure();
		try {
			SignerModel signerModel = repository.findBySignerId(signerId);
			if (signerModel != null) {

				signerModel.setExpired(model.isExpired());
				repository.save(signerModel);

				if (model.isExpired()) {
					structure.setMessage("Signer Expiration successful");
				} else {
					structure.setMessage("Signer Expiration overturned");
				}

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(signerModel);
				structure.setFlag(1);

			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
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

	@Override
	public ResponseStructure viewBySignerReference(String referenceId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			SignerModel signerModel = repository.findByReferenceNumber(referenceId);

			if (signerModel != null) {
				structure.setMessage("Signers details are..");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(signerModel);
				structure.setFlag(1);
			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
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

	@Override
	public ResponseStructure aadhaarSigning(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<SignerModel> optional = repository.findById(model.getSignerId());

			if (optional.isPresent()) {

				SignerModel signer = optional.get();

				if (signer.getEntityModel().isSigningRequired() && signer.getEntityModel().isAadhaarBasedSigning()) {

					if (model.getAadhaarNumber().length() == 12) {

						ResponseStructure threeCombinedAmount = signerUtils.allThreeBalanceCheck(signer);

						if (threeCombinedAmount.getFlag() == 1) {

							String maskedAadhaar = FileUtils.getFirstFourChar(model.getAadhaarNumber()) + "XXXX"
									+ FileUtils.stringSplitter(model.getAadhaarNumber(), 8);

							if (maskedAadhaar.equals(signer.getSignerAadhaar())) {

								return signingVerification.aadhaarNumberVerification(signer, model);

							} else {

								structure.setMessage("Provided Aadhaar number doesn't match with the Signer Aadhaar");
								structure.setStatusCode(HttpStatus.OK.value());
								structure.setData(null);
								structure.setFlag(4);

							}
						}else {
							
							return threeCombinedAmount;
							
						}
					} else {

						structure.setMessage("Please provide a Valid Aadhaar number");
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(null);
						structure.setFlag(4);
					}

				} else {

					if (!signer.getEntityModel().isSigningRequired()) {
						structure.setMessage("Digital Signing is not enabled");
					} else {
						structure.setMessage("Aadhaar based Digital Signing is not enabled");
					}

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(4);

				}
			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(5);
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}

		return structure;
	}

	@Override
	public ResponseStructure aadhaarSigningOtpSubmit(RequestModel users, HttpServletRequest request) {

		ResponseStructure structure = new ResponseStructure();
		boolean sign = false;

		try {

			SignerModel signerModel = repository.findBySignerId(users.getSignerId());

			String requestIdentifier = FileUtils.generateApiKeys(11);

			if (signerModel != null && !signerModel.isExpired() && !signerModel.getMerchantModel().isExpired()
					&& signerModel.getEntityModel().isSigningRequired()
					&& signerModel.getEntityModel().isAadhaarBasedSigning()) {

				String randomOtp = users.getOtpCode();
				
				ResponseStructure twoCombinedAmount = signerUtils.twoBalanceCheck(signerModel,randomOtp);

				if (twoCombinedAmount.getFlag() == 1) {

					ResponseStructure otpResponse = otpSignSubmitVerification.aadhaarOtpVerification(signerModel,
							users);

					if (otpResponse.getFlag() == 1) {

						RequestModel modelData = otpResponse.getModelData();

						boolean responseStatus = modelData.isStatusFlag();
						String fullName = modelData.getFullName();

						System.err.println("aaadhaaar : " + modelData.getAadhaarNumber());

						String maskedAadhaar = "";

						boolean aadhaarMatched = false;

						if (AppConstants.SUREPASS_ROUTE) {

							maskedAadhaar = FileUtils.getFirstFourChar(modelData.getAadhaarNumber()) + "XXXX"
									+ FileUtils.stringSplitter(modelData.getAadhaarNumber(), 8);
							aadhaarMatched = maskedAadhaar.equalsIgnoreCase(signerModel.getSignerAadhaar());

						} else {

							maskedAadhaar = "XXXXXXXX" + FileUtils.stringSplitter(modelData.getAadhaarNumber(), 8);
							String signerAadhar = "XXXXXXXX"
									+ FileUtils.stringSplitter(signerModel.getSignerAadhaar(), 8);

							aadhaarMatched = maskedAadhaar.equalsIgnoreCase(signerAadhar);
						}

						String dob = modelData.getDob();
						String gender = modelData.getGender();
						boolean mobileVerified = modelData.isMobileVerified();

						if (responseStatus && maskedAadhaar != null && signerModel.getSignerAadhaar() != null
								&& aadhaarMatched) {

							sign = addSignatureToPdf(signerModel, users , true);

							if (sign) {

								signerModel.setOtpVerificationStatus(true);
								signerModel.setSignedAt(DateUtil.dateFormat());
								String ipAddress = request.getRemoteAddr();
								signerModel.setIpAddress(ipAddress);
								signerModel.setOtpcode(users.getOtpCode());
								repository.save(signerModel);

								MerchantModel merchantModel = merchantRepository
										.findByMerchantId(signerModel.getMerchantModel().getMerchantId());
								List<SignerModel> list = repository.findByMerchantModel(merchantModel);

								int signerCount = 0;
								int signedCount = 0;
								for (SignerModel signer : list) {
									if (signer.isOtpVerificationStatus()) {
										signedCount = signedCount + 1;
									}
									signerCount = signerCount + 1;
								}

								if (signerCount == signedCount) {
									reportGenerate(merchantModel);
									for (SignerModel signer : list) {
										boolean report = emailService.finishedReport(merchantModel, signer,
												signer.getEntityModel());
									}
								}

								signerUtils.setSignerRequestToSuccess(signerModel, sign, "");

								structure.setMessage(AppConstants.OTP_VERIFIED);
								structure.setData(signerModel);
								structure.setFlag(1);
							} else {
								structure.setMessage(AppConstants.OTP_NOT_VERIFIED);
								structure.setData(null);
								structure.setFlag(2);
							}

						} else {

							if (!responseStatus) {

								structure.setData(responseStatus);
								structure.setMessage("Aadhaar OTP verification failed");
							} else {
								structure.setData(maskedAadhaar + " != " + signerModel.getSignerAadhaar());
								structure.setMessage("Aadhaar number mismatch");
							}

							structure.setFlag(3);
							structure.setStatusCode(HttpStatus.OK.value());

						}

					} else {

						structure.setData(null);
						structure.setMessage(otpResponse.getMessage());
						structure.setFlag(3);
						structure.setStatusCode(HttpStatus.OK.value());

						return structure;
					}
				} else {

					return twoCombinedAmount;
				}

			} else {

				if (signerModel == null) {
					structure.setMessage(AppConstants.NO_DATA_FOUND);
				} else if (signerModel.getMerchantModel().isExpired()) {
					structure.setMessage("The Agreement has been expired");
				} else if (!signerModel.getEntityModel().isSigningRequired()) {
					structure.setMessage("Signing is disabled");
				} else if (!signerModel.getEntityModel().isSigningRequired()) {
					structure.setMessage("Aadhaar based signing is not enabled");
				} else {
					structure.setMessage("This signer has been expelled from signing this document");
				}
				structure.setData(null);
				structure.setFlag(5);
			}
			structure.setStatusCode(HttpStatus.OK.value());

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

	@Override
	public ResponseStructure signerConsent(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<SignerModel> opt = repository.findById(model.getSignerId());

			if (opt.isPresent()) {

				SignerModel signerModel = opt.get();

				signerModel.setConsent(model.isConsent());
				if (model.isConsent()) {
					signerModel.setConsentAcceptedAt(new Date());
				}

				repository.save(signerModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(signerModel);
				structure.setMessage("Accepted");
				structure.setFlag(1);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
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
	public ResponseStructure signerLocationTracker(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<SignerModel> opt = repository.findById(model.getSignerId());

			if (opt.isPresent()) {

				SignerModel signerModel = opt.get();

				String publicIp = ipAndLocation.publicIpAddress();

				signerModel.setGetLocation(model.isGetLocation());
				signerModel.setIpAddress(publicIp);

				if (model.isGetLocation() && !(publicIp.equalsIgnoreCase("Not found"))) {

					JSONObject locationDetails = ipAndLocation.getLocationUsingPublicIp(publicIp);

					if (locationDetails != null) {

						String status = locationDetails.optString("status");
						String ipAddress = locationDetails.optString("query");
						String country = locationDetails.optString("country");
						String countryCode = locationDetails.optString("countryCode");
						String regionName = locationDetails.optString("regionName");
						String regionCode = locationDetails.optString("region");
						String city = locationDetails.optString("city");
						String pincode = locationDetails.optString("zip");
						double lat = locationDetails.optDouble("lat");
						double lon = locationDetails.optDouble("lon");
						String timezone = locationDetails.optString("timezone");
						String serviceProvider = locationDetails.optString("isp");
						String organisation = locationDetails.optString("org");
						String autonomousSystemNumber = locationDetails.optString("as");

						String latitude = String.valueOf(lat);
						String longitude = String.valueOf(lon);

						SignerPositionTracker pos = new SignerPositionTracker();

						pos.setLocationStatus(status);
						pos.setPublicIp(ipAddress);
						pos.setCountry(country);
						pos.setCountryCode(countryCode);
						pos.setRegion(regionName);
						pos.setRegionCode(regionCode);
						pos.setCity(city);
						pos.setZip(pincode);
						pos.setLatitude(latitude);
						pos.setLongitude(longitude);
						pos.setTimezone(timezone);
						pos.setServiceProvider(serviceProvider);
						pos.setAutonomousSystemNumber(autonomousSystemNumber);
						pos.setTrackTime(new Date());
						pos.setSigner(signerModel);

						signerPositionTrackerRepository.save(pos);

						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(pos);
						structure.setMessage(AppConstants.SUCCESS);
						structure.setFlag(1);

					} else {

						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(null);
						structure.setMessage("Position tracking failed");
						structure.setFlag(2);
					}
				} else {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);

					if (!model.isGetLocation()) {
						structure.setMessage("Permission not granted to track position");
					} else {
						structure.setMessage("Public IP not found");
					}
					structure.setFlag(3);
				}

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(4);
			}
		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(5);
		}
		return structure;
	}

}
