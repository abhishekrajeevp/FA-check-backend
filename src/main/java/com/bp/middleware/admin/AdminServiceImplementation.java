package com.bp.middleware.admin;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


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

import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.emailserviceadmin.EmailAdmin;
import com.bp.middleware.emailserviceadmin.EmailAdminRepository;
import com.bp.middleware.jwt.JWTTokenProvider;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.role.RoleDto;
import com.bp.middleware.role.RoleRepository;
import com.bp.middleware.rolesandpermission.ActionModel;
import com.bp.middleware.rolesandpermission.ActionRepository;
import com.bp.middleware.security.SecurityEnum;
import com.bp.middleware.sms.SMSEntity;
import com.bp.middleware.sms.SMSRepository;
import com.bp.middleware.sms.SMSService;
import com.bp.middleware.sms.SuperAdminSmsConfig;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.PasswordUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;


@Service
public class AdminServiceImplementation implements AdminService{

	private static final Logger LOGGER=LoggerFactory.getLogger(AdminServiceImplementation.class);

	@Autowired
    JWTTokenProvider tokenProvider;
	@Autowired
    EmailService emailService;
	@Autowired
	private SMSService smsService;
	@Autowired
	private SMSRepository smsRepository;
	@Autowired
    private AdminRepository adminRepository;
	@Autowired
	private AdminPasswordHistoryRepository adminPasswordHistoryRepository;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	ServletContext context;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private ActionRepository actionRepository;
	@Autowired
	private EmailAdminRepository emailAdminRepository;
	
	
	@Override
	public ResponseEntity<ResponseStructure> createSuperAdmin(RequestModel entity, HttpServletRequest servletRequest) {
		ResponseStructure structure=new ResponseStructure();

		try {
		Optional<RoleDto> role=roleRepository.findById(entity.getRoleId());
			if (adminRepository.findByEmail(entity.getEmail())==null
					&& adminRepository.findByMobileNumber(entity.getMobileNumber())==null){
	
				if (role.isPresent() && role.get().getRoleId()==1) {
					
					AdminDto admin= new AdminDto();
					RoleDto roleDto=role.get();
					
					admin.setRole(roleDto);
				    admin.setName(entity.getName());
					admin.setEmail(entity.getEmail());
					admin.setAddress(entity.getAddress());
					admin.setMobileNumber(entity.getMobileNumber());
					//admin.setLastLogin(new Date());
					
					InetAddress inetAddress=InetAddress.getLocalHost();
					String ipAddress = inetAddress.getHostAddress();
					admin.setIpAddress(ipAddress);
					
					admin.setCreatedBy(entity.getCreatedBy());
					admin.setCreatedDate(new Date());
					admin.setAccountStatus(true);
					admin.setLoginFailedCount(0);
					admin.setOtpVerificationStatus(true);
					String otp=FileUtils.getRandomOTPnumber(6);
					admin.setOtpCode(otp);

					List<AdminPasswordHistory> history =new ArrayList<>();
					AdminPasswordHistory passwordModel=new AdminPasswordHistory();
					
					passwordModel.setAdmin(admin);
					history.add(passwordModel);
					admin.setPasswordHistory(history);
					String salt= PasswordUtils.getSalt(30);
					String mySecurePassword = PasswordUtils.generateSecurePassword(entity.getPassword(), salt);
					passwordModel.setUserPassword(mySecurePassword);
					passwordModel.setUserSaltKey(salt);
					passwordModel.setCurrentPasswordStatus(1);
					passwordModel.setReqDeviceType(entity.getReqDeviceType());
					admin.setOtpExpiryOn(calculatemintus(new Date()));
					admin.setAccountStatus(true);
					adminRepository.save(admin);
					
					emailService.sendEmailAdminOTPVerification(admin.getEmail(), admin.getName(),admin.getMobileNumber(), entity.getPassword(), "http://157.245.105.135:5030/login-page");
					
//					SMSEntity smsEntity=smsRepository.findBySmsTempId(1);
					
					String otpSms=FileUtils.getRandomOTPnumber(6);
					
					String [] emailOtp= {otpSms,SuperAdminSmsConfig.SMS_ADMIN_CONTACTNO};
					smsService.sendSMSNotification(emailOtp, entity.getMobileNumber(), SuperAdminSmsConfig.SMS_TEMP_CODE, SuperAdminSmsConfig.SMS_SERVICE_URL,
							SuperAdminSmsConfig.SMS_USERNAME, SuperAdminSmsConfig.SMS_PASSWORD, SuperAdminSmsConfig.SMS_ENABLED);
					
					structure.setMessage("SUPER ADMIN CREATED SUCCESSFULLY");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(admin);
					structure.setFlag(1);
					return new ResponseEntity<>(structure,HttpStatus.OK);
					
				}else {
					structure.setMessage(AppConstants.ROLE_ID_NOT_MATCHED);
					structure.setData(null);
					structure.setFlag(3);
					return new ResponseEntity<>(structure,HttpStatus.OK);
				}
				
			} else {
				structure.setMessage("Email Address / Mobile number already exists");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
				return new ResponseEntity<>(structure,HttpStatus.OK);
			}
		} catch (Exception e) {
			LOGGER.info("AdminServiceImpl createPost method", e);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			return new ResponseEntity<>(structure,HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

	@Override
	public ResponseEntity<ResponseStructure> createUser(RequestModel entity,HttpServletRequest servletRequest) {

		ResponseStructure structure=new ResponseStructure();

		try {
		Optional<RoleDto> role=roleRepository.findById(entity.getRoleId());
			if (adminRepository.findByEmail(entity.getEmail())==null
					&& adminRepository.findByMobileNumber(entity.getMobileNumber())==null){
	
				if (role.isPresent() && role.get().getRoleId()==2) {
					
					EmailAdmin adminMail = emailAdminRepository.findByCurrentlyActive(true);
					
					List<SMSEntity> allAdminSms = smsRepository.allAdminSms();
					
					if(adminMail == null) {
						
						structure.setMessage("Please Configure Admin Mail");//
						structure.setData(null);
						structure.setFlag(3);
						return new ResponseEntity<>(structure,HttpStatus.OK);
					}
					
					if(allAdminSms.isEmpty()) {
						
						structure.setMessage("Please Configure Admin Sms");//
						structure.setData(null);
						structure.setFlag(3);
						return new ResponseEntity<>(structure,HttpStatus.OK);
					}
					
					AdminDto admin= new AdminDto();
					
					RoleDto roleDto=role.get();
					admin.setRole(roleDto);
				    admin.setName(entity.getName());
					admin.setEmail(entity.getEmail());
					admin.setAddress(entity.getAddress());
					admin.setMobileNumber(entity.getMobileNumber());
					//admin.setLastLogin(new Date());
					
					
					InetAddress inetAddress=InetAddress.getLocalHost();
					String ipAddress = inetAddress.getHostAddress();
					admin.setIpAddress(ipAddress);
					
					admin.setCreatedBy(entity.getCreatedBy());
					admin.setCreatedDate(new Date());
					admin.setAccountStatus(true);
					admin.setLoginFailedCount(0);
					admin.setOtpVerificationStatus(true);
					String otp=FileUtils.getRandomOTPnumber(6);
					admin.setOtpCode(otp);

					List<AdminPasswordHistory> history =new ArrayList<>();
					AdminPasswordHistory passwordModel=new AdminPasswordHistory();
					passwordModel.setAdmin(admin);
					history.add(passwordModel);
					admin.setPasswordHistory(history);
					String salt= PasswordUtils.getSalt(30);
					String mySecurePassword = PasswordUtils.generateSecurePassword(entity.getPassword(), salt);
					passwordModel.setUserPassword(mySecurePassword);
					passwordModel.setUserSaltKey(salt);
					passwordModel.setCurrentPasswordStatus(1);
					passwordModel.setReqDeviceType(entity.getReqDeviceType());
					admin.setOtpExpiryOn(calculatemintus(new Date()));
					admin.setAccountStatus(true);
					
					adminRepository.save(admin);
					
					emailService.sendEmailAdminOTPVerification(admin.getEmail(), admin.getName(),admin.getMobileNumber(), entity.getPassword(), "http://157.245.105.135:5030/login-page");
					
					SMSEntity smsEntity=allAdminSms.get(0);
					
					String otpSms=FileUtils.getRandomOTPnumber(6);
					
					String [] emailOtp= {otpSms,SuperAdminSmsConfig.SMS_ADMIN_CONTACTNO};
					smsService.sendSMSNotification(emailOtp, entity.getMobileNumber(), smsEntity.getSmsTempCode(), smsEntity.getSmsServiceUrl(),
							                       smsEntity.getSmsUserName(), smsEntity.getSmsPassword(), smsEntity.getSmsEnabled());
					
					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(admin);
					structure.setFlag(1);
					return new ResponseEntity<>(structure,HttpStatus.OK);
					
				}else {
					structure.setMessage(AppConstants.ROLE_ID_NOT_MATCHED);
					structure.setData(null);
					structure.setFlag(3);
					return new ResponseEntity<>(structure,HttpStatus.OK);
				}
				
			} else {
				structure.setMessage("Email Address / mobile number already exists");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
				return new ResponseEntity<>(structure,HttpStatus.OK);
			}
		} catch (Exception e) {
			LOGGER.info("AdminServiceImpl createPost method", e);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(4);
			return new ResponseEntity<>(structure,HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}
	private Date calculatemintus(Date d1) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		cal.add(Calendar.SECOND, 360);
		return cal.getTime();
	}

	@Override
	public ResponseStructure login(RequestModel entity) {

		AdminDto admin=adminRepository.findByEmail(entity.getEmail());
		ResponseStructure structure=new ResponseStructure();
		try {
			if (admin != null) {
				if (admin.getRole().getRoleId()==2 || admin.getRole().getRoleId()==1) {
					return getAdminLogin(admin,entity);

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
			
			LOGGER.info("AdminServiceImpl login method", e);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}
	private ResponseStructure getAdminLogin(AdminDto admin, RequestModel entity) {

		ResponseStructure structure=new ResponseStructure();
		try {
			if (admin.getLoginFailedCount()<6) {
				if (admin.isAccountStatus()) {
					return getAdminLoginFinal(admin,entity);

				} else {
					structure.setMessage(AppConstants.ADMIN_IN_ACTIVE);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(5);
					return structure;
				}		
			} else {
				structure.setMessage("Your account is blocked");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
				return structure;
			}


		} catch (Exception e) {
			
			LOGGER.info("AdminServiceImpl getAdminLogin method", e);
			structure.setMessage( AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(7);
			return structure;
		}

	}
	private ResponseStructure getAdminLoginFinal(AdminDto admin, RequestModel entity) {
		ResponseStructure structure=new ResponseStructure();
		try {
			AdminPasswordHistory model=new AdminPasswordHistory();
			for (AdminPasswordHistory password : admin.getPasswordHistory()) {
				if (password.getCurrentPasswordStatus()==1) {
					model=password;
				}
			}
			boolean passwordMatch = PasswordUtils.verifyUserPassword(entity.getPassword(),
					model.getUserPassword().trim(), model.getUserSaltKey());

			if (passwordMatch) {

				Map<String, Object> mapnew = new HashMap<>();
				String jwtToken = tokenProvider.generateToken(entity.getEmail());
				mapnew.put(SecurityEnum.JWT_TOKEN.status(), jwtToken);
				mapnew.put("expires_in", tokenProvider.getExpirationDateFromToken(jwtToken).toString());
				
				admin.setLoginFailedCount(0);
				admin.setJwtResponse(mapnew);

				adminRepository.save(admin);
				
				List<ActionModel> adminAction = actionRepository.findByAdminModelAndAccountStatus(admin,true);

				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				
				if(admin.getRole().getRoleId()==1) {
					
					structure.setData(admin);
					structure.setMessage("LOGIN SUCCESSFULL");
					
				}else {
					structure.setData(adminAction);
					
					if(adminAction.size()>0) {
					    structure.setMessage("LOGIN SUCCESSFULL");
					}else {
						structure.setMessage("NO ROLES OR PERMISSIONS GIVEN");
					}
					
				}
				
			}else {
				
				int failedLoginCount = admin.getLoginFailedCount();
				
				if(admin.getRole().getRoleId()!=1) {
					failedLoginCount++;
				}else {
					failedLoginCount=0;
				}
				
				admin.setLoginFailedCount(failedLoginCount);
				admin.setLastLogin(new Date());
				adminRepository.save(admin);

				structure.setMessage(AppConstants.INVALID_USERNAME_OR_PASSWORD);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(4);

			}
		} catch (Exception e) {
			LOGGER.info("AdminServiceImpl getAdminLoginFinal method", e);
			structure.setMessage( AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(7);
		}
		return structure;
	}
	
	
	@Override
	public ResponseStructure verifyEmail(String email) {
		ResponseStructure structure = new ResponseStructure();
		try {
			AdminDto entity = adminRepository.findByEmail(email);
			
			if (entity != null && entity.getLoginFailedCount()<6 && entity.isAccountStatus()) {
				
				String otp = FileUtils.getRandomOTPnumber(6);
				entity.setOtpCode(otp);
				entity.setOtpExpiryOn(calculatemintus(new Date()));
				adminRepository.save(entity);
				boolean sent = emailService.sendCustomerVerifyEmail(entity.getEmail(), otp,null);
				
				if (sent) {
					
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage(AppConstants.OTP_SENT_SUCCESSFULLY);
					structure.setData(entity);
					structure.setFlag(1);
					return structure;
					
				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage(AppConstants.OTP_SENT_NOT_SUCCESSFULLY);
					structure.setData(null);
					structure.setFlag(2);
					return structure;
				}

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				
				if(entity==null) {
					structure.setMessage(AppConstants.INVALID_USERNAME);
				}else if(!entity.isAccountStatus()){
					structure.setMessage(AppConstants.ADMIN_IN_ACTIVE);
				}else {
					structure.setMessage(AppConstants.ACCOUNT_BLOCKED);
				}
				
				structure.setData(null);
				structure.setFlag(3);
				return structure;
			}
			
		} catch (Exception e) {
			LOGGER.info("AdminServiceImpl verifyEmail method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setData(null);
			structure.setFlag(4);
			structure.setErrorDiscription(e.getMessage());
			return structure;
		}

	}

	@Override
	public ResponseStructure verifyotp(int adminId,RequestModel user) {
		ResponseStructure structure = new ResponseStructure();
		Optional<AdminDto> entity = (adminRepository.findById(adminId));
		if (entity.isPresent()) {
			AdminDto model=entity.get();
			if (model.getOtpCode() != null) {
				if (user.getOtpCode().equals(model.getOtpCode()) && model.getOtpExpiryOn().after(new Date())) {

					model.setOtpCode(FileUtils.getRandomOTPnumber(6));
					model.setOtpVerificationStatus(true);
					adminRepository.save(model);
					
					structure.setMessage("OTP Verified Successfully");
					structure.setMessage(AppConstants.OTP_VERIFIED);
					structure.setData(model);
					structure.setFlag(1);

				} else {
					structure.setMessage(AppConstants.OTP_NOT_MATCHED);
					structure.setData(null);
					structure.setFlag(2);
				}

			} else {
				structure.setMessage("OTP Not Present");
				structure.setData(null);
				structure.setFlag(3);
			}
		} else {
			structure.setMessage("Admin Id Not Present");
			structure.setData(null);
			structure.setFlag(4);
		}
		structure.setStatusCode(HttpStatus.OK.value());
		return structure;
	}
	
	
	@Override
	public ResponseStructure resndOtp(int adminId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<AdminDto> model = (adminRepository.findById(adminId));
			if (model.isPresent()) {
				AdminDto entity= model.get();
				String otpCode = FileUtils.getRandomOTPnumber(6);
				entity.setOtpCode(otpCode);
				entity.setAdminId(adminId);
				boolean sent = emailService.sendCustomerVerifyEmail(entity.getEmail(), otpCode,null);

				if (sent) {
					entity.setOtpExpiryOn(calculatemintus(new Date()));
					entity.setOtpVerificationStatus(sent);
					adminRepository.save(entity);
					
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage(AppConstants.OTP_SENT_SUCCESSFULLY);
					structure.setData(entity);
					structure.setFlag(1);
				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage(AppConstants.OTP_SENT_NOT_SUCCESSFULLY);
					structure.setData(null);
					structure.setFlag(2);
				}
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.INVALID_USER);
				structure.setData(null);
				structure.setFlag(3);
			}

		} catch (Exception e) {
			LOGGER.info("AdminServiceImpl resent OTP method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage("Error Ocurred..!!!");
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure reset(RequestModel model) {
		ResponseStructure structure=new ResponseStructure();
		try {
			AdminDto entity=adminRepository.findByEmail(model.getEmail());
			if (entity!=null) {
				List<AdminPasswordHistory> history=entity.getPasswordHistory();
				if (!history.isEmpty()) {
					return getReset(model,entity,history); 
				} else { 
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setData(null);
					structure.setFlag(2);
					return structure;
				}
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(3);
				return structure;
			}	

		} catch (Exception e) {
			LOGGER.info("AdminServiceImpl reset method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
			return structure;
		}
	}
	private ResponseStructure getReset(RequestModel model, AdminDto entity, List<AdminPasswordHistory> history) {
		ResponseStructure structure=new ResponseStructure();
		try {
			for (AdminPasswordHistory adminPasswordHistory : history) {
				if (adminPasswordHistory.getCurrentPasswordStatus()==1) {
					adminPasswordHistory.setCurrentPasswordStatus(0); 
					adminPasswordHistoryRepository.save(adminPasswordHistory);
				}
			} 
			return resetPassword(entity,model);

		} catch (Exception e) {
			LOGGER.info("AdminServiceImpl reset method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(4);
			return structure;
		}

	}
	private ResponseStructure resetPassword( AdminDto entity, RequestModel model) {
		ResponseStructure structure=new ResponseStructure();
		try {
			AdminPasswordHistory pswd=new AdminPasswordHistory();
			
				String salt=PasswordUtils.getSalt(30);
				String myecurePassword=PasswordUtils.generateSecurePassword(model.getPassword(), salt);
				pswd.setUserPassword(myecurePassword);
				pswd.setUserSaltKey(salt);
				pswd.setCurrentPasswordStatus(1);
				pswd.setAdmin(entity);
				adminPasswordHistoryRepository.save(pswd);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage( AppConstants.PASSWORD_RESET_SUCCESSFULLY);
				structure.setData(entity);
				structure.setFlag(1);

		} catch (Exception e) {
			LOGGER.info("AdminServiceImpl resetPassword method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure changePassword(RequestModel model) {
		ResponseStructure structure=new ResponseStructure();

		try {
			Optional<AdminDto> admin=(adminRepository.findById(model.getAdminId()));
			if (admin.isPresent()) {//
				AdminDto entity=admin.get();
				AdminPasswordHistory historyModel=new AdminPasswordHistory();
				for (AdminPasswordHistory history : entity.getPasswordHistory()) {
					if (history.getCurrentPasswordStatus()==1) {
						historyModel=history;
					}
				}
				boolean passwordMatch=PasswordUtils.verifyUserPassword(model.getPassword(),
						historyModel.getUserPassword().trim(),historyModel.getUserSaltKey());
				if (passwordMatch) {
					
					if(model.getPassword().equalsIgnoreCase(model.getNewPassword())) {
						
						structure.setData(null);
						structure.setMessage("New password should not be same as old password");
						structure.setFlag(2);
						structure.setStatusCode(HttpStatus.OK.value());
						
						return structure;
					}
					
					historyModel.setCurrentPasswordStatus(0);
					adminPasswordHistoryRepository.save(historyModel);
					AdminPasswordHistory AdminPasswordHistory=new AdminPasswordHistory();

					String salt= PasswordUtils.getSalt(30);
					String mySecurePassword = PasswordUtils.generateSecurePassword(model.getNewPassword(), salt);
					AdminPasswordHistory.setAdmin(entity);
					AdminPasswordHistory.setUserPassword(mySecurePassword);
					AdminPasswordHistory.setUserSaltKey(salt);
					AdminPasswordHistory.setCurrentPasswordStatus(1);
					AdminPasswordHistory.setModifiedBy(model.getModifyBy());
					AdminPasswordHistory.setModifiedDate(new Date());
					AdminPasswordHistory.setReasonForChange(model.getReasonForChange());
					AdminPasswordHistory.setReqDeviceType(model.getReqDeviceType());
					adminPasswordHistoryRepository.save(AdminPasswordHistory);

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setFlag(1);
				} else {
					structure.setMessage(AppConstants.INVALID_CURRENT_PASSWORD);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(3);
			}

		} catch (Exception e) {
			LOGGER.info("AdminServiceImpl resetPassword method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(4);
		}
		return structure;
	}

	@Override
	public ResponseStructure accountStatusChange(RequestModel model) {

		ResponseStructure structure=new ResponseStructure();
		try {
			Optional<AdminDto> adminOpt=(adminRepository.findById(model.getAdminId()));
			if (adminOpt.isPresent()) {
				
				AdminDto admin=adminOpt.get();
				
				admin.setAccountStatus(model.isAccountStatus());
				admin.setModifyBy(model.getModifyBy());
				admin.setModifyDate(new Date());
				
//				if(model.isAccountStatus() && !admin.isMailTriggered()) {
//					
//					
//				}
				
				adminRepository.save(admin);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.SUCCESS);
				structure.setData(admin);
				structure.setFlag(1);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {
			LOGGER.info("AdminServiceImpl Account Status Changed method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.BAD_REQUEST);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	@Override
	public ResponseStructure activeAccounts(boolean accountStatus) {

		ResponseStructure structure=new ResponseStructure();
		try {
			List<AdminDto> accounts=adminRepository.getByAccountStatus(accountStatus);
			if (!accounts.isEmpty()) {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Active Account Details are..!!!");
				structure.setData(accounts);
				structure.setFlag(1);
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {
			LOGGER.info("AdminServiceImpl Active  method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
		}
		return structure;
	}

	@Override
	public ResponseStructure fetchById(int adminId) {

		ResponseStructure structure=new ResponseStructure();
		try {
			Optional<AdminDto> model=(adminRepository.findById(adminId));
			if (model.isPresent()) {
				AdminDto entity=model.get();
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Admin Details are..!!!");
				structure.setData(entity);
				structure.setFlag(1);
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			LOGGER.info("AdminServiceImpl fetchById method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
		}
		return structure;
	}

	@Override
	public ResponseStructure updateDetails(int adminId, RequestModel entity) {
		ResponseStructure structure=new ResponseStructure();

		try {

			Optional<AdminDto> model= adminRepository.findById(adminId);
			if (model.isPresent()) {
				AdminDto admin=model.get();
				admin.setName(entity.getName());
				admin.setAddress(entity.getAddress());
				admin.setMobileNumber(entity.getMobileNumber());
				//admin.setLastLogin(new Date());
				
				InetAddress inetAddress=InetAddress.getLocalHost();
				String ipAddress = inetAddress.getHostAddress();
				admin.setIpAddress(ipAddress);
				
				admin.setCreatedDate(new Date());
				admin.setAccountStatus(true);
				admin.setModifyBy(entity.getModifyBy());
				admin.setModifyDate(new Date());
				admin.setLoginFailedCount(0);
//				admin.setOtpVerificationStatus(entity.isOtpVerificationStatus());
//				String otp=FileUtils.getRandomOTPnumber(6);
//				admin.setOtpCode(otp);

				adminRepository.save(admin);
				structure.setMessage("Update Successfully....!!!");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(admin);
				structure.setFlag(1);
			} else {
				structure.setMessage(AppConstants.INVALID_USER);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			LOGGER.error("AdminServiceImpl Update Detailes method", e);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
		}
		return structure;
	}

	@Override
	public ResponseStructure uploadAdminProfilePicture(int adminId, MultipartFile profilePhoto) {
		ResponseStructure structure=new ResponseStructure();
		try {
			Optional<AdminDto> adminModel = adminRepository.findById(adminId);
			if (adminModel.isPresent()) {
				AdminDto model = adminModel.get();
				return saveUploadedFiles(profilePhoto, model);

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(2);
				structure.setMessage(AppConstants.NO_DATA_FOUND);		
			}
		} catch (IOException e) {
			LOGGER.info("AdminServiceImpl Upload Admin Profile Picture method", e);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setData(structure);
		}
		return structure;
	}

	private ResponseStructure saveUploadedFiles(MultipartFile  profilePhoto, AdminDto model) throws IOException {
		ResponseStructure structure=new ResponseStructure();
		String folder = new FileUtils().genrateFolderName("" + model.getAdminId());


		String extensionType = null;
		StringTokenizer st = new StringTokenizer(profilePhoto.getOriginalFilename(), ".");
		while (st.hasMoreElements()) {
			extensionType = st.nextElement().toString();
		}
		String fileName = FileUtils.getRandomString() + "." + extensionType;
		model.setProfilePhoto(folder + "/" + fileName);

		Path currentWorkingDir = Paths.get(context.getRealPath("/WEB-INF/"));
		
		File saveFile = new File(currentWorkingDir + "/adminprofilepictures/" + folder);
		saveFile.mkdir();

		byte[] bytes = profilePhoto.getBytes();
		Path path = Paths.get(saveFile + "/" + fileName);
		Files.write(path, bytes);
		adminRepository.save(model);

		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(model);
		structure.setMessage(model.getName()+" your profile picture has been uploaded successfully!!");		
		structure.setFileName(fileName);

		return structure;
	}

	@Override
	public ResponseEntity<Resource> viewImage(int adminId, HttpServletRequest request) {

		Optional<AdminDto> admin=adminRepository.findById(adminId);
		if(admin.isPresent()) {
			if(admin.get().getProfilePhoto()!=null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/adminprofilepictures/" + admin.get().getProfilePhoto());
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
		}else {
			return null;
		}
	}
	
	@Override
	public ResponseStructure viewAllAdmin() {
		ResponseStructure structure=new ResponseStructure();
		List<AdminDto> list = adminRepository.findAll();
		
		if (!list.isEmpty()) {
			
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(list);
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
	public ResponseStructure decodeValue(int passwordId) {

		ResponseStructure structure=new ResponseStructure();
		try {
			Optional<AdminDto> optional = adminRepository.findById(passwordId);
			if (optional.isPresent()) {
//				PasswordHistoryModel model = optional.get();
				String salt= PasswordUtils.getSalt(16);
				String password="Test@123";
				String mySecurePassword = PasswordUtils.encryptWithHashSalt(password,salt);
				System.out.println("Password = "+ password);
				System.out.println("Salt = "+ salt );
				System.out.println("Secure Date = "+ mySecurePassword );
				
				String decodeData=PasswordUtils.decryptWithHashSalt(mySecurePassword, salt);
				System.out.println("User Password ="+mySecurePassword);
				System.out.println("Salt ="+salt);
				System.out.println("Decoded Date ="+decodeData);
				
				
//				String panCard="CADPB6543G";
//				String encrypt=PasswordUtils.Encryption(panCard);
//				System.out.println("panCard = "+ panCard);
//				System.out.println("Encryption Data = " + encrypt);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.SUCCESS);
//				structure.setData(encrypt);
				structure.setFlag(1);
			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.ERROR_MESSAGE);
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
	public ResponseStructure blockUnblock(int adminId, boolean blockUnblockStatus) {
          
		ResponseStructure structure = new ResponseStructure();
		
		try {
			
			AdminDto admin = adminRepository.findByAdminId(adminId);
			
			if(admin!=null) {
				
				if(blockUnblockStatus) {
					
					admin.setLoginFailedCount(0);
					
					structure.setMessage("ACCOUNT UNBLOCKED SUCCESSFULLY");
					
				}else {
					
					admin.setLoginFailedCount(6);
					
					structure.setMessage("ACCOUNT BLOCKED SUCCESSFULLY");
				}
				
				adminRepository.save(admin);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(admin);
				structure.setFlag(1);
				
			}else {
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
	

}
