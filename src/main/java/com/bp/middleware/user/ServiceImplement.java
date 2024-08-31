package com.bp.middleware.user;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

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
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.admin.AdminRepository;
import com.bp.middleware.businesscategory.BusinessCategory;
import com.bp.middleware.businesscategory.BusinessCategoryRepository;
import com.bp.middleware.businesstype.BusinessType;
import com.bp.middleware.businesstype.BusinessTypeRepository;
import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.emailservice.SmtpMailConfiguration;
import com.bp.middleware.emailservice.SmtpMailConfigurationRepository;
import com.bp.middleware.emailservice.SmtpMailRepository;
import com.bp.middleware.emailserviceadmin.EmailAdmin;
import com.bp.middleware.emailserviceadmin.EmailAdminRepository;
import com.bp.middleware.jwt.JWTTokenProvider;
import com.bp.middleware.locations.CityModel;
import com.bp.middleware.locations.CityRepository;
import com.bp.middleware.locations.CountryModel;
import com.bp.middleware.locations.CountryRepository;
import com.bp.middleware.locations.PinCodeRepository;
import com.bp.middleware.locations.PincodeModel;
import com.bp.middleware.locations.StateModel;
import com.bp.middleware.locations.StateRepository;
import com.bp.middleware.mcccode.MCCCodesModel;
import com.bp.middleware.mcccode.MCCRepository;
import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.merchantapipricesetup.MerchantPriceService;
import com.bp.middleware.merchantapipricesetup.MerchantPriceServiceImplimentation;
import com.bp.middleware.merchantpricetracker.MerchantPriceTrackerService;
import com.bp.middleware.payment.PaymentMethod;
import com.bp.middleware.payment.PaymentRepository;
import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.role.RoleDto;
import com.bp.middleware.role.RoleRepository;
import com.bp.middleware.rolesandpermission.ActionModel;
import com.bp.middleware.rolesandpermission.ActionRepository;
import com.bp.middleware.security.SecurityEnum;
import com.bp.middleware.sms.SMSEntity;
import com.bp.middleware.sms.SMSRepository;
import com.bp.middleware.user.approvalrejection.ApprovalRejectionHistory;
import com.bp.middleware.user.approvalrejection.ApprovalRejectionRepository;
import com.bp.middleware.useraccesskeys.EntityAccessKeyRepository;
import com.bp.middleware.useraccesskeys.EntityAccessKeys;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileEncryptDecryptUtil;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.PasswordUtils;
import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class ServiceImplement implements UserService {

	@Autowired
	private UserRepository repository;
	@Autowired
	JWTTokenProvider tokenProvider;
	@Autowired
	private PasswordRepository passwordRep;
	@Autowired
	private EmailService emailService;
	@Autowired
	ServletContext context;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private BusinessCategoryRepository businessCategoryRepository;
	@Autowired
	private BusinessTypeRepository businessTypeRepository;
	@Autowired
	private MCCRepository mccRepository;
	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private PostpaidRepository postpaidRepository;
	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private ApprovalRejectionRepository approvalRejectionRepository;
	@Autowired
	private RequestRepository requestRepository;
	@Autowired
	private EntityAccessKeyRepository accessKeyRepository;
	@Autowired
	private SmtpMailRepository mailRepository;
	@Autowired
	private SMSRepository smsRepository;
	@Autowired
	private EmailAdminRepository emailAdminRepository;
	@Autowired
    private UserAutomaticPriceSet automaticPriceSet;

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceImplement.class);

	@Override
	public ResponseStructure createUser(RequestModel model, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<RoleDto> role = roleRepository.findById(model.getRoleId());
			
			Optional<AdminDto> admin = adminRepository.findById(model.getAdminId());

			if (repository.findByEmail(model.getEmail()) == null
					&& repository.findByMobileNumber(model.getMobileNumber()) == null && admin.isPresent()) {

				if (role.isPresent() && role.get().getRoleId() == 3) {

					RoleDto roleDto = role.get();
					EntityModel user = new EntityModel();

					user.setRole(roleDto);
					user.setAdminId(model.getAdminId());
					user.setName(model.getName());
					user.setEmail(model.getEmail());
					user.setAddress(model.getAddress());
					user.setMobileNumber(model.getMobileNumber());
					user.setVerificationRequired(model.isVerificationRequired());
					user.setSigningRequired(model.isSigningRequired());

					String password = FileEncryptDecryptUtil.encrypt(model.getPassword());
					user.setTempPass(password);

					if (model.isSigningRequired()) {

						user.setGeneralSigning(model.isGeneralSigning());
						user.setAadhaarBasedSigning(model.isAadhaarBasedSigning());
						user.setBulkUploadAccess(model.isBulkUploadAccess());
						user.setCreatePdfAccess(model.isCreatePdfAccess());
						user.setDocumentPrice(model.getDocumentPrice());
						user.setAadhaarOtpPrice(model.getAadhaarOtpPrice());
						user.setAadhaarXmlPrice(model.getAadhaarXmlPrice());

					} else {
						user.setAadhaarBasedSigning(false);
					}

					user.setRequestCount(0);
					user.setResponseCount(0);
					user.setLogPeriod(30);
					user.setConsentId(FileUtils.consentKey());
					user.setCountryName(model.getCountryName());
					user.setStateName(model.getStateName());
					user.setCityName(model.getCityName());
					user.setPincode(model.getPincode());
					user.setMailPresent(model.isMailPresent());
					user.setSmsPresent(model.isSmsPresent());
					user.setShowLiveKeys(false);
					user.setInvoGenerated(false);

					PaymentMethod method = paymentRepository.getByPaymentId(model.getPaymentId());
					user.setPaymentMethod(method);

					if (method.getPaymentType().equalsIgnoreCase("Postpaid")) {

						LocalDate startDate = LocalDate.now();

						LocalDate endDate = endDateByPaymentCycle(model);
						LocalDate graceDate = endDate.plusDays(model.getGracePeriod());

						user.setPostpaidPaymentCycle(model.getPostpaidPaymentCycle());
						user.setGracePeriod(model.getGracePeriod());
						user.setStartDate(startDate);
						user.setEndDate(endDate);
						user.setDuration(model.getDuration());
						user.setGraceDate(graceDate);
						user.setPostpaidFlag(false);
					}

					MCCCodesModel mccCode = mccRepository.findByMccId(model.getMccId());
					user.setMccCodesModel(mccCode);
					Optional<BusinessCategory> category = businessCategoryRepository
							.findById(model.getBusinessCategoryId());
					if (category.isPresent()) {
						BusinessCategory businessCategory = category.get();
						user.setCategory(businessCategory);
					}

					BusinessType type = businessTypeRepository.getByBusinessTypeId(model.getBusinessTypeId());
					user.setType(type);

					user.setPan(model.getPan());
					user.setGst(model.getGst());
					user.setContactPersonName(model.getContactPersonName());
					user.setContactPersonMobile(model.getContactPersonMobile());
					user.setContactPersonEmail(model.getContactPersonEmail());
					

					InetAddress inetAddress = InetAddress.getLocalHost();
					String ipAddress = inetAddress.getHostAddress();

					user.setIpAddress(ipAddress);
					user.setCreatedBy(model.getCreatedBy());
					user.setCreatedDate(new Date());

					user.setAccountStatus(false);
					user.setRejectedCount(0l);
					user.setApprovalStatus("Pending");
					user.setLoginFailedCount(0);
					user.setOtpVerificationStatus(true);
					String otp = FileUtils.getRandomOTPnumber(6);
					user.setOtpCode(otp);
					user.setNoRestriction(0);

					// BANK DETAILS
					user.setAccountNumber(model.getAccountNumber());
					user.setIfscCode(model.getIfscCode());
					user.setAccountHolderName(model.getAccountHolderName());
					user.setBranchName(model.getBranchName());
					user.setMicrCode(model.getMicrCode());
					user.setBankName(model.getBankName());
					user.setBankAccountStatus(false);
					user.setAccountType(model.getAccountType());

					user.setWebsiteIntegration(model.getWebsiteIntegration());
					user.setPlugin(model.getPlugin());
					user.setPublicUrl(model.getPublicUrl());
					user.setLicenceNumber(model.getLicenceNumber());
					user.setLicenceType(model.getLicenceType());

					user.setLicenceIssueDate(null);
					user.setLicenceExpirationDate(null);

					user.setApiKey("LIVE_" + FileUtils.generateApiKeys(27));
					user.setApiSandboxKey("SANDBOX_" + FileUtils.generateApiKeys(24));
					user.setApplicationId(FileUtils.applicationIdGeneration(model.getName()));
					user.setSecretKey(PasswordUtils.getSalt(32));
					user.setSaltKey(PasswordUtils.getSalt(32));
					user.setOtpExpiryOn(calculatemintus(new Date()));
					repository.save(user);

					if (!user.isMailPresent()) {
						mailSettingsAdd(user);
					}

					if (!user.isSmsPresent()) {
						smsSettingsAdd(user);
					}

					if (method.getPaymentType().equalsIgnoreCase("Postpaid")) {// for postpaid table

						PostpaidPayment postPayment = new PostpaidPayment();

						postPayment.setTotalAmount(0);
						postPayment.setStartDate(user.getStartDate());
						postPayment.setEndDate(user.getEndDate());
						postPayment.setPaymentFlag(false);
						postPayment.setEntityModel(user);

						postpaidRepository.save(postPayment);
					}

					PasswordModelHistory passwordHistory = new PasswordModelHistory();
					passwordHistory.setUser(user);
					String salt = PasswordUtils.getSalt(30);
					String mySecurePassword = PasswordUtils.generateSecurePassword(model.getPassword(), salt);
					passwordHistory.setUserPassword(mySecurePassword);
					passwordHistory.setUserSaltKey(salt);
					passwordHistory.setCurrentPasswordStatus(1);
					passwordHistory.setReqDeviceType(model.getReqDeviceType());
					passwordRep.save(passwordHistory);

					EntityAccessKeys accessKeys = new EntityAccessKeys();
					accessKeys.setApplicationId(user.getApplicationId());
					accessKeys.setSandboxApiKey(user.getApiSandboxKey());
					accessKeys.setLiveApiKey(user.getApiKey());
					accessKeys.setSecretKey(user.getSecretKey());
					accessKeys.setUser(user);

					accessKeyRepository.save(accessKeys);

					if (user.isSigningRequired()) {
						
						automaticPriceSet.digitalSigning(user);
						
						if(user.isAadhaarBasedSigning()) {
							
							automaticPriceSet.aadhaarXMLAndOTP(user);
						}
					}

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(user);
					structure.setFlag(1);
				} else {
					structure.setMessage("Role ID not matched");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(3);
				}
			} else {
				
				if(admin.isEmpty()) {
					structure.setMessage("Admin not found");
				}else {
					structure.setMessage("Phone number or Email Already present");
				}
				
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

	private LocalDate endDateByPaymentCycle(RequestModel model) throws Exception {

		LocalDate now = LocalDate.now();

		int dayOfMonth = now.getDayOfMonth();

		if (dayOfMonth == 1) {

			if (model.getPostpaidPaymentCycle().equalsIgnoreCase("Monthly")) {

				return now.plusDays(now.lengthOfMonth());

			} else if (model.getPostpaidPaymentCycle().equalsIgnoreCase("Quarterly")) {

				return now.plusMonths(3);

			} else if (model.getPostpaidPaymentCycle().equalsIgnoreCase("Half Yearly")) {
				return now.plusMonths(3);
			} else {
				return now.plusYears(1);
			}

		} else {

			if (model.getPostpaidPaymentCycle().equalsIgnoreCase("Monthly")) {

				return now.plusDays(now.lengthOfMonth() - dayOfMonth);

			} else if (model.getPostpaidPaymentCycle().equalsIgnoreCase("Quarterly")) {

				LocalDate startDate = now.minusDays(now.getDayOfMonth() - 1);

				return startDate.plusMonths(2);

			} else if (model.getPostpaidPaymentCycle().equalsIgnoreCase("Half Yearly")) {

				LocalDate startDate = now.minusDays(now.getDayOfMonth() - 1);
				return startDate.plusMonths(5);
			} else {

				LocalDate startDate = now.minusDays(now.getDayOfMonth() - 1);
				return startDate.plusYears(1);
			}
		}

	}

	private boolean smsSettingsAdd(EntityModel user) throws Exception {

		List<SMSEntity> allSms = smsRepository.allAdminSms();

		if (!allSms.isEmpty()) {

			SMSEntity sms = allSms.get(0);

			SMSEntity smsEntity = new SMSEntity();

			smsEntity.setEntityModel(user);
			smsEntity.setSmsTempCode(sms.getSmsTempCode());
			smsEntity.setSmsTempMessage(sms.getSmsTempMessage());
			smsEntity.setSmsTempDescription(sms.getSmsTempDescription());
			smsEntity.setSmsTempStatus(true);
			smsEntity.setSmsEntityId(sms.getSmsEntityId());
			smsEntity.setSmsTemplateId(sms.getSmsTemplateId());
			smsEntity.setSmsServiceUrl(sms.getSmsServiceUrl());
			smsEntity.setSmsUserName(sms.getSmsUserName());
			smsEntity.setSmsPassword(sms.getSmsPassword());
			smsEntity.setSmsEnabled(sms.getSmsEnabled());

			smsRepository.save(smsEntity);

			return true;

		} else {
			return false;
		}

	}

	private boolean mailSettingsAdd(EntityModel user) throws Exception {

		EmailAdmin ourMail = emailAdminRepository.findByCurrentlyActive(true);

		if (ourMail != null) {

			SmtpMailConfiguration mail = new SmtpMailConfiguration();

			mail.setHost(ourMail.getHost());
			mail.setPort(ourMail.getPort());
			mail.setSmtpAuth(ourMail.getSmtpAuth());
			mail.setSmtpPort(ourMail.getSmtpPort());
			mail.setSmtpConnectionTimeOut(ourMail.getSmtpConnectionTimeOut());
			mail.setStarttlsEnable(ourMail.getStarttlsEnable());
			mail.setSocketFactoryPort(ourMail.getSocketFactoryPort());
			mail.setSocketFactoryClass(ourMail.getSocketFactoryClass());
			mail.setProtocol(ourMail.getProtocol());
			mail.setMailUserName(ourMail.getMailUserName());
			mail.setMailPassword(ourMail.getMailPassword());
			mail.setSmtpTimeOut(ourMail.getSmtpTimeOut());
			mail.setSmtpWriteTimeOut(ourMail.getSmtpWriteTimeOut());
			mail.setCreatedBy(user.getCreatedBy());
			mail.setCreatedDate(new Date());
			mail.setEntity(user);

			mailRepository.save(mail);

			return true;

		} else {
			return false;
		}
	}

	private Date calculatemintus(Date d1) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		cal.add(Calendar.SECOND, 360);
		return cal.getTime();
	}

	@Override
	public ResponseStructure login(RequestModel model) {

		EntityModel user = repository.findByEmail(model.getEmail());
		ResponseStructure structure = new ResponseStructure();

		try {
			if (user != null) {
				if (user.getRole().getRoleId() == 3) {
					return getUserLoginDetails(user, model);
				} else {

					structure.setMessage(AppConstants.ROLE_ID_NOT_MATCHED);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(6);

				}
			} else {
				structure.setMessage(AppConstants.INVALID_USERNAME_OR_PASSWORD);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	private ResponseStructure getUserLoginDetails(EntityModel user, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {
			if (user.getLoginFailedCount() < 6) {
				if (user.isAccountStatus()) {
					return getUserLoginFinal(user, model);
				} else {
					structure.setMessage(AppConstants.USER_IN_ACTIVE);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(5);
				}
			} else {
				structure.setMessage("Account is Blocked");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
				return structure;
			}
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}

		return structure;
	}

	private ResponseStructure getUserLoginFinal(EntityModel user, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			PasswordModelHistory history = new PasswordModelHistory();
			List<PasswordModelHistory> historyList = passwordRep.findByUser(user);

			for (PasswordModelHistory passwordModelHistory : historyList) {
				if (passwordModelHistory.getCurrentPasswordStatus() == 1) {
					history = passwordModelHistory;
				}
			}

			System.out.println("History UserPass : " + history.getUserPassword());
			boolean passwordStatus = PasswordUtils.verifyUserPassword(model.getPassword(),
					history.getUserPassword().trim(), history.getUserSaltKey());

			if (passwordStatus) {

				Map<String, Object> mapnew = new HashMap<>();
				String jwtToken = tokenProvider.generateToken(model.getEmail());
				mapnew.put(SecurityEnum.JWT_TOKEN.status(), jwtToken);
				mapnew.put("expires_in", tokenProvider.getExpirationDateFromToken(jwtToken).toString());
				user.setJwtResponse(mapnew);
				user.setLoginFailedCount(0);

				repository.save(user);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(user);
				structure.setFlag(1);
			} else {
				int failedLoginCount = user.getLoginFailedCount();
				failedLoginCount++;
				user.setLoginFailedCount(failedLoginCount);
				user.setLastLogin(new Date());
				repository.save(user);

				structure.setMessage(AppConstants.INVALID_USERNAME_OR_PASSWORD);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(4);
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
	public ResponseStructure emailVerify(String email) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel user = repository.findByEmail(email);

			if (user != null && user.getLoginFailedCount() < 6 && user.isAccountStatus()) {

				String generatedOtp = FileUtils.getRandomOTPnumber(6);
				user.setOtpCode(generatedOtp);
				user.setOtpExpiryOn(calculatemintus(new Date()));
				repository.save(user);

				boolean sent = emailService.sendCustomerVerifyEmail(user.getEmail(), generatedOtp, null);
				if (sent) {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("OTP SENT SUCCESSFULLY");
					structure.setData(user);
					structure.setFlag(1);
				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("OTP NOT SENT");
					structure.setData(user);
					structure.setFlag(3);
				}
			} else {
				structure.setStatusCode(HttpStatus.OK.value());

				if (user == null) {
					structure.setMessage(AppConstants.INVALID_USERNAME);
				} else if (!user.isAccountStatus()) {
					structure.setMessage(AppConstants.USER_IN_ACTIVE);
				} else {
					structure.setMessage(AppConstants.ACCOUNT_BLOCKED);
				}

				structure.setData(null);
				structure.setFlag(3);
			}

		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseStructure verifyotp(int userId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<EntityModel> entity = (repository.findById(userId));
			if (entity.isPresent()) {
				EntityModel user = entity.get();

				if (user.getOtpCode().equals(model.getOtpCode()) && user.getOtpExpiryOn().after(new Date())) {

					user.setOtpCode(FileUtils.getRandomOTPnumber(6));
					user.setOtpVerificationStatus(true);
					repository.save(user);

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("OTP verified Succesfully");
					structure.setData(user);
					structure.setFlag(1);

				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("OTP Mismatch");
					structure.setData(null);
					structure.setFlag(2);
				}
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No data found for the given ID");
				structure.setData(null);
				structure.setFlag(4);
			}
			// return structure;
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseStructure resendOtp(int userId) {
		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<EntityModel> model = (repository.findById(userId));
			if (model.isPresent()) {
				EntityModel entity = model.get();
				String otpCode = FileUtils.getRandomOTPnumber(6);
				entity.setOtpCode(otpCode);

				boolean sent = emailService.sendCustomerVerifyEmail(entity.getEmail(), otpCode, null);
				if (sent) {
					entity.setOtpExpiryOn(calculatemintus(new Date()));
					entity.setOtpVerificationStatus(sent);
					repository.save(entity);

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("OTP_SENT_SUCCESSFULLY");
					structure.setData(entity);
					structure.setFlag(1);
				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("OTP_SENT_NOT_SUCCESSFULLY");
					structure.setData(null);
					structure.setFlag(1);
				}

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("User data is invalid");
				structure.setData(null);
				structure.setFlag(3);
			}
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}

		return structure;
	}

	@Override
	public ResponseStructure reset(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel entity = repository.findByEmail(model.getEmail());
			if (entity != null) {
				List<PasswordModelHistory> historyList = passwordRep.findByUser(entity);
				if (!historyList.isEmpty()) {
					return getReset(model, entity, historyList);
				}

			} else {
				structure.setMessage("No Data found");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	private ResponseStructure getReset(RequestModel model, EntityModel entity, List<PasswordModelHistory> historyList) {
		ResponseStructure structure = new ResponseStructure();
		try {
			for (PasswordModelHistory passwordModelHistory : historyList) {
				if (passwordModelHistory.getCurrentPasswordStatus() == 1) {
					passwordModelHistory.setCurrentPasswordStatus(0);
					passwordRep.save(passwordModelHistory);
				}
			}
			return resetPassword(entity, model);
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
			return structure;
		}

	}

	private ResponseStructure resetPassword(EntityModel entity, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			PasswordModelHistory newPass = new PasswordModelHistory();

			String salt = PasswordUtils.getSalt(30);
			String myecurePassword = PasswordUtils.generateSecurePassword(model.getPassword(), salt);
			newPass.setUserPassword(myecurePassword);
			newPass.setUserSaltKey(salt);
			newPass.setCurrentPasswordStatus(1);
			newPass.setUser(entity);
			passwordRep.save(newPass);

			structure.setMessage("Password Reset Succesfull");
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(newPass);

		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseStructure changePassword(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> entityModel = (repository.findById(model.getUserId()));
			if (entityModel.isPresent()) {
				EntityModel entity = entityModel.get();
				PasswordModelHistory passwordModel = new PasswordModelHistory();
				List<PasswordModelHistory> historyList = passwordRep.findByUser(entity);

				for (PasswordModelHistory history : historyList) {
					if (history.getCurrentPasswordStatus() == 1) {
						passwordModel = history;
					}
				}
				boolean passwordMatch = PasswordUtils.verifyUserPassword(model.getPassword(),
						passwordModel.getUserPassword().trim(), passwordModel.getUserSaltKey());

				if (passwordMatch) {

					if (model.getPassword().equalsIgnoreCase(model.getNewPassword())) {

						structure.setData(null);
						structure.setMessage("New password should not be same as old password");
						structure.setFlag(2);
						structure.setStatusCode(HttpStatus.OK.value());

						return structure;
					}

					passwordModel.setCurrentPasswordStatus(0);
					passwordRep.save(passwordModel);

					PasswordModelHistory passwordModelHistory = new PasswordModelHistory();
					String salt = PasswordUtils.getSalt(30);
					String newPassword = PasswordUtils.generateSecurePassword(model.getNewPassword(), salt);//

					passwordModelHistory.setUser(entity);
					passwordModelHistory.setUserPassword(newPassword);
					passwordModelHistory.setUserSaltKey(salt);
					passwordModelHistory.setCurrentPasswordStatus(1);
					passwordModelHistory.setModifiedBy(model.getModifyBy());
					passwordModelHistory.setModifiedDate(new Date());
					passwordModelHistory.setReasonForChange(model.getReasonForChange());
					passwordModelHistory.setReqDeviceType(model.getReqDeviceType());
					passwordRep.save(passwordModelHistory);

					structure.setMessage("New Password Updated");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(passwordModelHistory);
					structure.setFlag(1);

				} else {

					structure.setMessage(AppConstants.INVALID_CURRENT_PASSWORD);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}

			} else {
				structure.setMessage("No Data found");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
			}

		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	
	
	@Override
	public ResponseStructure findBySalt(String saltKey) {
		
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> opt = repository.findBySaltKey(saltKey);
			
			if (opt.isPresent()) {
				
				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(opt.get());
				structure.setCount(opt.get().getUserId());
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
			structure.setFlag(7);
		}
		return structure;
	}
	
	
	
	@Override
	public ResponseStructure changeAccountStatus(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> entityModel = repository.findById(model.getUserId());
			if (entityModel.isPresent()) {

				EntityModel entity = entityModel.get();

				entity.setAccountStatus(model.isAccountStatus());

				if (!entity.isMailTriggered() && model.isAccountStatus()) {

					String password = FileEncryptDecryptUtil.decryptWithCryptoTwo(entity.getTempPass());
					entity.setTempPass("");

					boolean sent = emailService.sendEmailOTPVerificationForUser(entity.getEmail(), entity.getName(),
							entity.getMobileNumber(), password, "http://157.245.105.135:5031/login-page",
							entity.getSecretKey(), null);

					System.err.println(sent);

					entity.setMailTriggered(sent);
				}

				if (model.getModifyBy() != null) {
					entity.setModifyBy(model.getModifyBy());
				}
				entity.setModifyDate(new Date());

				repository.save(entity);

				structure.setMessage("Account status updated");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entity);
				structure.setFlag(1);

			} else {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
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
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseStructure updateBankAccountStatus(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> entityModel = repository.findById(model.getUserId());
			if (entityModel.isPresent()) {

				EntityModel entity = entityModel.get();

				entity.setBankAccountStatus(model.isBankAccountStatus());
				entity.setModifyBy(model.getModifyBy());
				entity.setModifyDate(new Date());

				repository.save(entity);

				structure.setMessage("Bank account status updated");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entity);
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
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseStructure approvalStatus(int userId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			AdminDto admin = adminRepository.findByAdminId(model.getAdminId());

			Optional<EntityModel> entityModel = repository.findById(userId);
			if (entityModel.isPresent()) {

				EntityModel entity = entityModel.get();

				if (model.isAccountStatus()) {

					entity.setAccountStatus(model.isAccountStatus());
					entity.setApprovalStatus("Approved");

					repository.save(entity);

					ApprovalRejectionHistory approvalRejection = new ApprovalRejectionHistory();

					approvalRejection.setApproveReject("Approved");
					approvalRejection.setApprovedStatus(true);
					approvalRejection.setRejectionCount(entity.getRejectedCount());
					approvalRejection.setComments(model.getComments());
					approvalRejection.setDate(LocalDate.now());
					approvalRejection.setUser(entity);
					approvalRejection.setAdmin(admin);

					approvalRejectionRepository.save(approvalRejection);

					structure.setMessage("Account approved successfully");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(entity);
					structure.setFlag(1);

				} else {

					entity.setAccountStatus(model.isAccountStatus());
					entity.setApprovalStatus("Rejected");
					entity.setRejectedCount(entity.getRejectedCount() + 1);
					repository.save(entity);

					ApprovalRejectionHistory approvalRejection = new ApprovalRejectionHistory();

					approvalRejection.setApproveReject("Rejected");
					approvalRejection.setApprovedStatus(false);
					approvalRejection.setRejectionCount(entity.getRejectedCount());
					approvalRejection.setComments(model.getComments());
					approvalRejection.setDate(LocalDate.now());
					approvalRejection.setUser(entity);
					approvalRejection.setAdmin(admin);

					approvalRejectionRepository.save(approvalRejection);

					structure.setMessage("Account rejected");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(entity);
					structure.setFlag(2);
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
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseStructure activeAccounts(boolean accountStatus) {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<EntityModel> accountDetail = repository.getByAccountStatus(accountStatus);

			if (!accountDetail.isEmpty()) {

				structure.setMessage("Active account details");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(accountDetail);
				structure.setFlag(1);
			} else {
				structure.setMessage("No data found");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseStructure fetchById(int userId) {
		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<EntityModel> entityModel = (repository.findById(userId));

			if (entityModel.isPresent()) {
				EntityModel entity = entityModel.get();

				structure.setMessage("User details");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entity);
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
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseStructure updateDetails(int userId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<EntityModel> entityModel = repository.findById(userId);

			if (entityModel.isPresent()) {

				EntityModel entity = entityModel.get();

				MCCCodesModel mccCode = mccRepository.findByMccId(model.getMccId());
				entity.setMccCodesModel(mccCode);
				BusinessCategory businessCategory = businessCategoryRepository
						.findByBusinessCategoryId(model.getBusinessCategoryId());
				entity.setCategory(businessCategory);
				BusinessType businessType = businessTypeRepository.findByBusinessTypeId(model.getBusinessTypeId());
				entity.setType(businessType);

				PaymentMethod paymentMethod = paymentRepository.findByPaymentId(model.getPaymentId());
				if (paymentMethod != null) {
					entity.setPaymentMethod(paymentMethod);

					if (paymentMethod.getPaymentType().equalsIgnoreCase("Postpaid")) {

						LocalDate startDate = LocalDate.now();
						LocalDate endDate = endDateByPaymentCycle(model);
						LocalDate graceDate = endDate.plusDays(model.getGracePeriod());

						entity.setPostpaidPaymentCycle(model.getPostpaidPaymentCycle());
						entity.setGracePeriod(model.getGracePeriod());
						entity.setStartDate(startDate);
						entity.setEndDate(endDate);
						entity.setDuration(model.getDuration());
						entity.setGraceDate(graceDate);
						entity.setPostpaidFlag(false);

						PostpaidPayment postPayment = new PostpaidPayment();

						postPayment.setTotalAmount(0);
						postPayment.setStartDate(entity.getStartDate());
						postPayment.setEndDate(entity.getEndDate());
						postPayment.setPaymentFlag(false);
						postPayment.setEntityModel(entity);

						postpaidRepository.save(postPayment);
					}
				}

				entity.setName(model.getName());
				entity.setAddress(model.getAddress());
				entity.setModifyBy(model.getModifyBy());
				entity.setModifyDate(new Date());
				entity.setPan(model.getPan());
				entity.setGst(model.getGst());
				entity.setContactPersonName(model.getContactPersonName());
				entity.setContactPersonMobile(model.getContactPersonMobile());
				entity.setContactPersonEmail(model.getContactPersonEmail());
				entity.setWebsiteIntegration(model.getWebsiteIntegration());
				entity.setPlugin(model.getPlugin());
				entity.setVerificationRequired(model.isVerificationRequired());

				entity.setSigningRequired(model.isSigningRequired());

				if (model.isSigningRequired()) {

					entity.setGeneralSigning(model.isGeneralSigning());
					entity.setAadhaarBasedSigning(model.isAadhaarBasedSigning());
					entity.setBulkUploadAccess(model.isBulkUploadAccess());
					entity.setCreatePdfAccess(model.isCreatePdfAccess());
					entity.setDocumentPrice(model.getDocumentPrice());
					entity.setAadhaarOtpPrice(model.getAadhaarOtpPrice());
					entity.setAadhaarXmlPrice(model.getAadhaarXmlPrice());

				} else {
					entity.setAadhaarBasedSigning(false);
					entity.setGeneralSigning(false);
					entity.setBulkUploadAccess(false);
					entity.setCreatePdfAccess(false);
				}

				entity.setPublicUrl(model.getPublicUrl());
				entity.setLicenceNumber(model.getLicenceNumber());
				entity.setLicenceType(model.getLicenceType());
				// entity.setLastLogin(entity.getLastLogin());
				entity.setCountryName(model.getCountryName());
				entity.setStateName(model.getStateName());
				entity.setCityName(model.getCityName());
				entity.setPincode(model.getPincode());

				InetAddress inetAddress = InetAddress.getLocalHost();
				String ipAddress = inetAddress.getHostAddress();
				entity.setIpAddress(ipAddress);

				entity.setLoginFailedCount(0);
				entity.setOtpVerificationStatus(entity.isOtpVerificationStatus());
				entity.setAccountStatus(true);

				String otp = FileUtils.getRandomOTPnumber(6);
				entity.setOtpCode(otp);

//				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//				LocalDate issueDate = LocalDate.parse(model.getLicenceIssueDate(), formatter);
//				entity.setLicenceIssueDate(issueDate);
//
//				LocalDate expiryDate = LocalDate.parse(model.getLicenceExpirationDate(), formatter);
//				entity.setLicenceExpirationDate(expiryDate);

				// BANK DETAILS
				entity.setAccountNumber(model.getAccountNumber());
				entity.setIfscCode(model.getIfscCode());
				entity.setAccountHolderName(model.getAccountHolderName());
				entity.setBranchName(model.getBranchName());
				entity.setMicrCode(model.getMicrCode());
				entity.setBankName(model.getBankName());
				// entity.setBankAccountStatus(model.isBankAccountStatus());
				entity.setAccountType(model.getAccountType());

				if (entity.isMailPresent() != model.isMailPresent() && !model.isMailPresent()) {
					mailSettingsUpdate(entity);
				}

				if (entity.isSmsPresent() != model.isSmsPresent() && !model.isSmsPresent()) {
					smsSettingsUpdate(entity);
				}

				entity.setMailPresent(model.isMailPresent());
				entity.setSmsPresent(model.isSmsPresent());

				repository.save(entity);
				
				if (entity.isSigningRequired()) {
					
					automaticPriceSet.digitalSigning(entity);
					
					if(entity.isAadhaarBasedSigning()) {
						
						automaticPriceSet.aadhaarXMLAndOTP(entity);
					}
				}

				structure.setMessage("Details Updated");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entity);
				structure.setFlag(1);

			} else {
				structure.setMessage("Invalid User");
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
			structure.setFlag(7);
		}

		return structure;
	}

	private boolean smsSettingsUpdate(EntityModel user) throws Exception {

		List<SMSEntity> allSms = smsRepository.allAdminSms();

		List<SMSEntity> alreadyPresentUserMail = smsRepository.getByEntityModelAndSmsTempStatus(user, true);

		if (!allSms.isEmpty() && alreadyPresentUserMail.size() > 0) {

			for (SMSEntity smsEntity : alreadyPresentUserMail) {

				smsEntity.setSmsTempStatus(false);
			}

//			SMSEntity sms = optional.get();
//
//			SMSEntity smsEntity = alreadyPresentUserMail.get();
//			
//			smsEntity.setEntityModel(user);
//			smsEntity.setSmsTempCode(sms.getSmsTempCode());
//			smsEntity.setSmsTempMessage(sms.getSmsTempMessage());
//			smsEntity.setSmsTempDescription(sms.getSmsTempDescription());
//			smsEntity.setSmsTempStatus(true);
//			smsEntity.setSmsEntityId(sms.getSmsEntityId());
//			smsEntity.setSmsTemplateId(sms.getSmsTemplateId());
//			smsEntity.setSmsServiceUrl(sms.getSmsServiceUrl());
//			smsEntity.setSmsUserName(sms.getSmsUserName());
//			smsEntity.setSmsPassword(sms.getSmsPassword());
//			smsEntity.setSmsEnabled(sms.getSmsEnabled());

			smsRepository.saveAll(alreadyPresentUserMail);

			return true;

		} else {
			return false;
		}

	}

	private boolean mailSettingsUpdate(EntityModel user) throws Exception {

		EmailAdmin ourMail = emailAdminRepository.findByCurrentlyActive(true);
		Optional<SmtpMailConfiguration> alreadyPresentUserSms = mailRepository.findByEntity(user);

		if (ourMail != null && alreadyPresentUserSms.isPresent()) {

			SmtpMailConfiguration mail = alreadyPresentUserSms.get();

			mail.setHost(ourMail.getHost());
			mail.setPort(ourMail.getPort());
			mail.setSmtpAuth(ourMail.getSmtpAuth());
			mail.setSmtpPort(ourMail.getSmtpPort());
			mail.setSmtpConnectionTimeOut(ourMail.getSmtpConnectionTimeOut());
			mail.setStarttlsEnable(ourMail.getStarttlsEnable());
			mail.setSocketFactoryPort(ourMail.getSocketFactoryPort());
			mail.setSocketFactoryClass(ourMail.getSocketFactoryClass());
			mail.setProtocol(ourMail.getProtocol());
			mail.setMailUserName(ourMail.getMailUserName());
			mail.setMailPassword(ourMail.getMailPassword());
			mail.setSmtpTimeOut(ourMail.getSmtpTimeOut());
			mail.setSmtpWriteTimeOut(ourMail.getSmtpWriteTimeOut());
			mail.setCreatedBy(user.getCreatedBy());
			mail.setCreatedDate(new Date());
			mail.setEntity(user);

			mailRepository.save(mail);

			return true;

		} else {
			return false;
		}
	}

	@Override
	public ResponseStructure uploadAdminProfilePicture(int userId, MultipartFile profilePhoto) {
		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<EntityModel> entityModel = repository.findById(userId);
			if (entityModel.isPresent()) {
				EntityModel entity = entityModel.get();
				return uploadedFile(profilePhoto, entity);

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
			structure.setFlag(7);
		}
		return structure;
	}

	private ResponseStructure uploadedFile(MultipartFile profilePhoto, EntityModel entity) {
		ResponseStructure structure = new ResponseStructure();
		try {

			String folder = new FileUtils().genrateFolderName("" + entity.getUserId());
			String extension = null;

			StringTokenizer token = new StringTokenizer(profilePhoto.getOriginalFilename(), ".");

			while (token.hasMoreElements()) {
				extension = token.nextElement().toString();
			}

			String fileName = FileUtils.getRandomString() + "." + extension;
			entity.setProfilePhoto(folder + "/" + fileName);
			Path currentWorkDirectory = Paths.get(context.getRealPath("/WEB-INF/"));

			File saveFile = new File(currentWorkDirectory + "/userprofilepictures/" + folder);
			saveFile.mkdir();

			byte[] fileBytes = profilePhoto.getBytes();
			Path path = Paths.get(saveFile + "/" + fileName);

			Files.write(path, fileBytes);
			repository.save(entity);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(entity);
			structure.setFileName(fileName);
			structure.setMessage(entity.getName() + " Profile picture has been uploaded succesfully");

		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	@Override
	public ResponseEntity<Resource> viewImage(int adminId, HttpServletRequest request) {

		Optional<EntityModel> user = repository.findById(adminId);
		if (user.isPresent()) {
			if (user.get().getProfilePhoto() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/userprofilepictures/" + user.get().getProfilePhoto());
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
	public ResponseStructure viewAllAdmin() {

		ResponseStructure structure = new ResponseStructure();
		try {
			List<EntityModel> list = repository.findAll();

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

	@Override // CBC
	public ResponseStructure encrypt(MultipartFile pic, String pswd) {
		ResponseStructure structure = new ResponseStructure();
		try {

			char[] password = pswd.toCharArray();

			byte[] encrypted = FileEncryptDecryptUtil.encrypt(pic, password);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(encrypted);
			structure.setFlag(1);

		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	// ECB ENCRYPTION ALGORITHM
	@Override
	public ResponseStructure encryptECB(MultipartFile pic) {
		ResponseStructure structure = new ResponseStructure();
		try {
			// GENERATE SECRET KEY
			String secretKey = PasswordUtils.getSalt(20);
			byte[] encryptedImageData = FileEncryptDecryptUtil.encryptImage(pic, secretKey);

			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(encryptedImageData);
			structure.setFlag(1);
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	@Override // BASE64 ENCRYPTION
	public ResponseStructure encryptBase64(MultipartFile pic) {
		ResponseStructure structure = new ResponseStructure();
		try {
			String password = "qPNNbUwcSOhXgWGwdBOXopjgSNPynDkC";
//			String base64Encoder=FileEncryptDecryptUtil.encryptImageToBase64(pic, password);
			String cc = FileEncryptDecryptUtil.ecbPaddingBase64(pic, password);

			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(cc);
			structure.setFlag(1);

		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

//	@Override
//	public String salt(String salt) {
//		System.out.println((PasswordUtils.getSalt(32)));
//		return null;
//	}
	@Override
	public ResponseStructure addAmount(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<EntityModel> optional = repository.findById(model.getUserId());
			if (optional.isPresent()) {
				EntityModel entityModel = optional.get();

				// entityModel.setAmount(model.getAmount());
				repository.save(entityModel);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entityModel);
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
	public ResponseStructure updateLogDetails(int userId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<EntityModel> entityModel = repository.findById(userId);

			if (entityModel.isPresent()) {
				EntityModel entity = entityModel.get();

				entity.setLogPeriod(model.getLogPeriod());
				entity.setLogUpdatedBy(model.getLogUpdatedBy());
				entity.setLogUpdatedAt(LocalDate.now());

				repository.save(entity);

				structure.setMessage("Details Updated");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entity);
				structure.setFlag(1);

			} else {
				structure.setMessage("Invalid User");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);

			}

		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}

		return structure;
	}

	@Override
	public ResponseStructure getPrepaidPostpaidUsers(int paymentId) {
		ResponseStructure structure = new ResponseStructure();

		PaymentMethod paymentMethod = paymentRepository.findByPaymentId(paymentId);

		List<EntityModel> paymentUsers = repository.findByPaymentMethod(paymentMethod);

		if (!paymentUsers.isEmpty()) {

			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(paymentUsers);
			structure.setFlag(1);

		} else {
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(null);
			structure.setFlag(2);
		}

		return structure;
	}

	@Override
	public ResponseStructure getPostpaidUsersByFlag(boolean flagStatus) {
		ResponseStructure structure = new ResponseStructure();

		List<EntityModel> flagUsers = repository.findByPostpaidFlag(flagStatus);

		if (!flagUsers.isEmpty()) {

			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(flagUsers);
			structure.setFlag(1);

		} else {
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(null);
			structure.setFlag(2);
		}

		return structure;

	}

	@Override
	public ResponseStructure getPrepaidPostpaidCount(int paymentId) {

		ResponseStructure structure = new ResponseStructure();

		PaymentMethod paymentMethod = paymentRepository.findByPaymentId(paymentId);

		int paymentCount = 0;

		List<EntityModel> paymentList = repository.findByPaymentMethod(paymentMethod);

		if (!paymentList.isEmpty()) {
			for (EntityModel entityModel : paymentList) {
				paymentCount++;
			}

			structure.setCount(paymentCount);
			structure.setFlag(1);
			structure.setMessage(AppConstants.SUCCESS);
			structure.setStatusCode(HttpStatus.OK.value());

		} else {
			structure.setCount(paymentCount);
			structure.setFlag(2);
			structure.setMessage(AppConstants.NO_DATA_FOUND);
			structure.setStatusCode(HttpStatus.OK.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure updateBankDetails(int userId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> optional = repository.findById(userId);
			if (optional.isPresent()) {
				EntityModel entity = optional.get();

				// BANK DETAILS
				entity.setAccountNumber(model.getAccountNumber());
				entity.setIfscCode(model.getIfscCode());
				entity.setAccountHolderName(model.getAccountHolderName());
				entity.setBranchName(model.getBranchName());
				entity.setMicrCode(model.getMicrCode());
				entity.setBankName(model.getBankName());
				entity.setBankAccountStatus(model.isBankAccountStatus());
				entity.setAccountType(model.getAccountType());
				entity.setModifyBy(model.getModifyBy());
				entity.setModifyDate(new Date());

				repository.save(entity);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(entity);
				structure.setMessage(AppConstants.SUCCESS);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure requestHistoryAccordingToLogPeriod(int userId) {

		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel entity = repository.findByUserId(userId);

			if (entity != null) {

				String startDate = LocalDate.now().minusDays(entity.getLogPeriod()).toString();
				String endDate = LocalDate.now().toString();

				List<Request> req = requestRepository.getBetween(userId, startDate, endDate);

				if (!req.isEmpty()) {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setData(req);
					structure.setCount(req.size());
					structure.setMessage(AppConstants.SUCCESS);

				} else {

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
					structure.setData(null);
					structure.setMessage("REQUEST HISTORY NOT FOUND FOR THE GIVEN USER");
				}

			} else {

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setData(null);
				structure.setMessage("ENTITY NOT FOUND");
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return structure;
	}

	@Override
	public ResponseStructure viewBySigningActive() {

		ResponseStructure structure = new ResponseStructure();
		try {
			List<EntityModel> list = repository.findBySigningRequired(true);

			if (list.isEmpty()) {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			} else {

				structure.setMessage("ENTITY WITH DOCUMENT SIGNING ACTIVE");
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

	@Override
	public ResponseStructure viewByVerificationActive() {

		ResponseStructure structure = new ResponseStructure();
		try {
			List<EntityModel> list = repository.findByVerificationRequired(true);

			if (list.isEmpty()) {
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			} else {

				structure.setMessage("ENTITY WITH VERIFICATION SERVICES ACTIVE");
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

	@Override
	public ResponseStructure blockUnblock(int userId, boolean blockUnblockStatus) {
		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel user = repository.findByUserId(userId);

			if (user != null) {

				if (blockUnblockStatus) {

					user.setLoginFailedCount(0);

					structure.setMessage("ACCOUNT UNBLOCKED SUCCESSFULLY");

				} else {

					user.setLoginFailedCount(6);

					structure.setMessage("ACCOUNT BLOCKED SUCCESSFULLY");
				}

				repository.save(user);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(user);
				structure.setFlag(1);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure forVerificationWithoutRestriction(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<EntityModel> entityModel = repository.findById(model.getUserId());

			if (entityModel.isPresent()) {

				EntityModel entity = entityModel.get();

				entity.setNoRestriction(entity.getNoRestriction() + model.getCount());
				entity.setModifyBy(model.getModifyBy());
				entity.setModifyDate(new Date());

				repository.save(entity);

				structure
						.setMessage("This user is authorized to make up to " + entity.getNoRestriction() + " attempts");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entity);
				structure.setFlag(1);

			} else {
				structure.setMessage("Invalid User");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}

		return structure;
	}

	@Override
	public ResponseStructure liveKeysShowOrHide(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> entityModel = repository.findById(model.getUserId());
			if (entityModel.isPresent()) {

				EntityModel entity = entityModel.get();

				entity.setShowLiveKeys(model.isShowLiveKeys());
				entity.setModifyBy(model.getModifyBy());
				entity.setModifyDate(new Date());

				repository.save(entity);

				if (model.isShowLiveKeys()) {
					structure.setMessage("LIVE KEYS WILL APPEAR FOR ENTITY");
				} else {
					structure.setMessage("LIVE KEYS WILL BE HIDDEN FROM ENTITY");
				}

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entity);
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
			structure.setFlag(7);
		}
		return structure;
	}

}
