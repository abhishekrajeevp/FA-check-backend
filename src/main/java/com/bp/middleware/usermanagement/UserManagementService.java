package com.bp.middleware.usermanagement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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

import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.jwt.JWTTokenProvider;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.rolesandpermission.ActionModel;
import com.bp.middleware.rolesandpermission.ActionRepository;
import com.bp.middleware.security.SecurityEnum;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.PasswordModelHistory;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.PasswordUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class UserManagementService {

	@Autowired
	private UserManagementRepository userManagementRepository;
	@Autowired
	private UserManagementPasswordRepository userManagementPasswordRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	JWTTokenProvider tokenProvider;
	@Autowired
	private EmailService emailService;
	@Autowired
	ServletContext context;
	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private ActionRepository actionRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementService.class);

	public ResponseStructure createUserManagement(RequestModel model, HttpServletRequest servletRequest) {

		ResponseStructure structure = new ResponseStructure();

		try {

			if (userManagementRepository.findByEmail(model.getEmail()) == null
					&& userManagementRepository.findByMobileNumber(model.getMobileNumber()) == null) {

				UserManagement user = new UserManagement();

				user.setName(model.getName());
				user.setEmail(model.getEmail());
				user.setAddress(model.getAddress());
				user.setMobileNumber(model.getMobileNumber());
				//user.setLastLogin(new Date());
				user.setLogPeriod(30);
				user.setPan(model.getPan());
				user.setAadhaarNumber(model.getAadhaarNumber());
				user.setIpAddress(FileUtils.getIpAddress());
				user.setCreatedBy(model.getCreatedBy());
				user.setCreatedDate(new Date());
				user.setAccountStatus(true);
				user.setLoginFailedCount(0);
				user.setUserManagementFlag(2);
				user.setOtpVerificationStatus(true);
				String otp = FileUtils.getRandomOTPnumber(6);
				user.setOtpCode(otp);
				user.setOtpExpiryOn(calculatemintus(new Date()));
				user.setRoleName(model.getRoleName());

				EntityModel entity = userRepository.findByUserId(model.getUserId());
				user.setUser(entity);

				userManagementRepository.save(user);

				UserManagementPasswordHistory passwordHistory = new UserManagementPasswordHistory();

				passwordHistory.setUserManagement(user);

				String salt = PasswordUtils.getSalt(30);
				String mySecurePassword = PasswordUtils.generateSecurePassword(model.getPassword(), salt);
				passwordHistory.setUserPassword(mySecurePassword);
				passwordHistory.setUserSaltKey(salt);
				passwordHistory.setCurrentPasswordStatus(1);
				passwordHistory.setReqDeviceType(model.getReqDeviceType());
				userManagementPasswordRepository.save(passwordHistory);

				emailService.sendEmailOTPVerificationForUser(user.getEmail(), user.getName(), user.getMobileNumber(),
						model.getPassword(), "/admin/login", null, entity);

				structure.setMessage("USER MANAGEMENT CREATED SUCCESSFULLY");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(user);
				structure.setFlag(1);

			} else {
				structure.setMessage("Phone number or Email Already present");
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

	private Date calculatemintus(Date d1) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d1);
		cal.add(Calendar.SECOND, 360);
		return cal.getTime();
	}

	public ResponseStructure login(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		UserManagement user = userManagementRepository.findByEmail(model.getEmail());

		try {
			if (user != null) {

				return getUserLoginDetails(user, model);

			} else {
				structure.setMessage("No User details found");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	private ResponseStructure getUserLoginDetails(UserManagement user, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			System.err.println("Login fail Count : " + user.getLoginFailedCount());
			System.err.println("Accont  : " + user.isAccountStatus());

			if (user.getLoginFailedCount() < 6) {
				if (user.isAccountStatus()) {
					
					return getUserLoginFinal(user, model);
					
				}else {
					structure.setMessage(AppConstants.USER_IN_ACTIVE);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(3);
					return structure;
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

	private ResponseStructure getUserLoginFinal(UserManagement user, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			UserManagementPasswordHistory history = new UserManagementPasswordHistory();
			List<UserManagementPasswordHistory> historyList = userManagementPasswordRepository
					.findByUserManagement(user);

			for (UserManagementPasswordHistory userManagementPasswordHistory : historyList) {
				if (userManagementPasswordHistory.getCurrentPasswordStatus() == 1) {
					history = userManagementPasswordHistory;
				}
			}

			System.err.println("History : " + history.getPasswordId());
			System.err.println("History UserPass : " + history.getUserPassword());

			boolean passwordStatus = PasswordUtils.verifyUserPassword(model.getPassword(),
					history.getUserPassword().trim(), history.getUserSaltKey());

			System.err.println("Pass stat : " + passwordStatus);
			if (passwordStatus) {

				Map<String, Object> mapnew = new HashMap<>();
				String jwtToken = tokenProvider.generateToken(model.getEmail());
				mapnew.put(SecurityEnum.JWT_TOKEN.status(), jwtToken);
				mapnew.put("expires_in", tokenProvider.getExpirationDateFromToken(jwtToken).toString());

				user.setJwtResponse(mapnew);
				user.setLoginFailedCount(0);

				userManagementRepository.save(user);

				List<ActionModel> userManagementAction = actionRepository.findByUserManagementAndAccountStatus(user,true);

				if (userManagementAction.size() > 0) {
					structure.setMessage("LOGIN SUCCESSFULL");
				} else {
					structure.setMessage("NO ROLES OR PERMISSIONS GIVEN");
				}

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(userManagementAction);
				structure.setFlag(1);

			} else {

				int failedLoginCount = user.getLoginFailedCount();
				failedLoginCount++;
				user.setLoginFailedCount(failedLoginCount);
				user.setLastLogin(new Date());
				userManagementRepository.save(user);

				structure.setMessage("Password is Invalid");
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

	public ResponseStructure changePassword(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<UserManagement> option = userManagementRepository.findById(model.getUserManagementId());

			if (option.isPresent()) {

				UserManagement userManagement = option.get();

				UserManagementPasswordHistory passwordModel = new UserManagementPasswordHistory();
				List<UserManagementPasswordHistory> historyList = userManagementPasswordRepository
						.findByUserManagement(userManagement);

				for (UserManagementPasswordHistory history : historyList) {
					if (history.getCurrentPasswordStatus() == 1) {
						passwordModel = history;
					}
				}

				boolean passwordMatch = PasswordUtils.verifyUserPassword(model.getPassword(),
						passwordModel.getUserPassword().trim(), passwordModel.getUserSaltKey());

				if (passwordMatch) {
					
					
					if(model.getPassword().equalsIgnoreCase(model.getNewPassword())) {
						
						structure.setData(null);
						structure.setMessage("New password should not be same as old password");
						structure.setFlag(2);
						structure.setStatusCode(HttpStatus.OK.value());
						
						return structure;
					}

					
					passwordModel.setCurrentPasswordStatus(0);
					userManagementPasswordRepository.save(passwordModel);

					UserManagementPasswordHistory PasswordModelHistory = new UserManagementPasswordHistory();
					String salt = PasswordUtils.getSalt(30);
					String newPassword = PasswordUtils.generateSecurePassword(model.getNewPassword(), salt);//

					PasswordModelHistory.setUserManagement(userManagement);
					PasswordModelHistory.setUserPassword(newPassword);
					PasswordModelHistory.setUserSaltKey(salt);
					PasswordModelHistory.setCurrentPasswordStatus(1);
					PasswordModelHistory.setModifiedBy(model.getModifyBy());
					PasswordModelHistory.setModifiedDate(new Date());
					PasswordModelHistory.setReasonForChange(model.getReasonForChange());
					PasswordModelHistory.setReqDeviceType(model.getReqDeviceType());
					userManagementPasswordRepository.save(PasswordModelHistory);

					structure.setMessage("New Password Updated");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(PasswordModelHistory);
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
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	public ResponseStructure changeAccountStatus(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<UserManagement> option = userManagementRepository.findById(model.getUserManagementId());

			if (option.isPresent()) {
				UserManagement userManagement = option.get();
				userManagement.setAccountStatus(model.isAccountStatus());
				userManagementRepository.save(userManagement);

				structure.setMessage("Account status updated");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(userManagement);
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

	public ResponseStructure activeAccounts(boolean accountStatus) {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<UserManagement> accountDetail = userManagementRepository.getByAccountStatus(accountStatus);

			if (!accountDetail.isEmpty()) {

				if (accountStatus) {
					structure.setMessage("Active account details");
				} else {
					structure.setMessage("InActive account details");
				}

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
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	public ResponseStructure viewById(int userManagementId) {
		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<UserManagement> option = userManagementRepository.findById(userManagementId);

			if (option.isPresent()) {
				UserManagement userManagement = option.get();

				structure.setMessage("User Management details");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(userManagement);
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

	public ResponseStructure updateDetails(int userManagementId, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<UserManagement> option = userManagementRepository.findById(userManagementId);

			if (option.isPresent()) {

				UserManagement userManagement = option.get();

				EntityModel user = userRepository.findByUserId(model.getUserId());

				userManagement.setName(model.getName());
				userManagement.setAddress(model.getAddress());
				userManagement.setPan(model.getPan());
				userManagement.setAadhaarNumber(model.getAadhaarNumber());
				userManagement.setModifyBy(model.getModifiedBy());
				userManagement.setModifyDate(new Date());
				userManagement.setRoleName(model.getRoleName());;

				userManagementRepository.save(userManagement);

				structure.setMessage("Details Updated");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(userManagement);
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

	public ResponseStructure uploadAdminProfilePicture(int userManagementId, MultipartFile profilePhoto) {
		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<UserManagement> option = userManagementRepository.findById(userManagementId);
			if (option.isPresent()) {
				UserManagement entity = option.get();

				return uploadedFile(profilePhoto, entity);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
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
			structure.setFlag(7);
		}
		return structure;
	}

	private ResponseStructure uploadedFile(MultipartFile profilePhoto, UserManagement entity) {

		ResponseStructure structure = new ResponseStructure();
		try {

			String folder = new FileUtils().genrateFolderName("" + entity.getUserManagementId());
			String extension = null;

			StringTokenizer token = new StringTokenizer(profilePhoto.getOriginalFilename(), ".");

			while (token.hasMoreElements()) {
				extension = token.nextElement().toString();
			}

			String fileName = FileUtils.getRandomString() + "." + extension;
			entity.setProfilePhoto(folder + "/" + fileName);
			Path currentWorkDirectory = Paths.get(context.getRealPath("/WEB-INF/"));

			File saveFile = new File(currentWorkDirectory + "/usermanagementprofilepic/" + folder);
			saveFile.mkdir();

			byte[] fileBytes = profilePhoto.getBytes();
			Path path = Paths.get(saveFile + "/" + fileName);

			Files.write(path, fileBytes);
			userManagementRepository.save(entity);

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

	public ResponseEntity<Resource> viewImage(int userManagementId, HttpServletRequest request) {

		Optional<UserManagement> user = userManagementRepository.findById(userManagementId);

		if (user.isPresent()) {
			if (user.get().getProfilePhoto() != null) {

				final Resource resource = resourceLoader
						.getResource("/WEB-INF/usermanagementprofilepic/" + user.get().getProfilePhoto());
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

	public ResponseStructure viewAll() {

		ResponseStructure structure = new ResponseStructure();
		try {
			List<UserManagement> entity = userManagementRepository.findAll();

			if (entity.isEmpty()) {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			} else {

				structure.setMessage("ALL USER MANAGEMENT LIST FOUND SUCCESSFULLY");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(entity);
				structure.setFlag(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(3);
		}
		return structure;
	}

	public ResponseStructure viewByUser(int userId) {

		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<EntityModel> optional = userRepository.findById(userId);

			if (optional.isPresent()) {

				EntityModel user = optional.get();

				List<UserManagement> userManagement = userManagementRepository.findByUser(user);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(userManagement);
				structure.setMessage("USER MANAGEMENT FOR THE GIVEN ENTITY FOUND SUCCESSFULLY");
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
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	public ResponseStructure blockUnblock(int userManagementId, boolean blockUnblockStatus) {
		ResponseStructure structure = new ResponseStructure();

		try {

			UserManagement user = userManagementRepository.findByUserManagementId(userManagementId);

			if (user != null) {

				if (blockUnblockStatus) {

					user.setLoginFailedCount(0);

					structure.setMessage("ACCOUNT UNBLOCKED SUCCESSFULLY");

				} else {

					user.setLoginFailedCount(6);

					structure.setMessage("ACCOUNT BLOCKED SUCCESSFULLY");
				}

				userManagementRepository.save(user);

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

	public ResponseStructure emailVerify(String email) {

		ResponseStructure structure = new ResponseStructure();
		try {
			UserManagement user = userManagementRepository.findByEmail(email);
			if (user != null  && user.getLoginFailedCount()<6 && user.isAccountStatus()) {

				String generatedOtp = FileUtils.getRandomOTPnumber(6);

				user.setOtpCode(generatedOtp);
				user.setOtpExpiryOn(calculatemintus(new Date()));

				userManagementRepository.save(user);

				boolean sent = emailService.sendCustomerVerifyEmail(user.getEmail(), generatedOtp,user.getUser());

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
				if(user==null) {
					structure.setMessage(AppConstants.INVALID_USERNAME);
				}else if(!user.isAccountStatus()){
					structure.setMessage(AppConstants.USER_IN_ACTIVE);
				}else {
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

	public ResponseStructure verifyOtp(int userManagementId, RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<UserManagement> entity = (userManagementRepository.findById(userManagementId));
			if (entity.isPresent()) {

				UserManagement user = entity.get();

				if (user.getOtpCode().equals(model.getOtpCode()) && user.getOtpExpiryOn().after(new Date())) {

					user.setOtpCode(FileUtils.getRandomOTPnumber(6));
					user.setOtpVerificationStatus(true);

					userManagementRepository.save(user);

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("OTP verified succesfully");
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
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(7);
		}
		return structure;
	}

	public ResponseStructure resendOtp(int userManagementId) {

		ResponseStructure structure = new ResponseStructure();

		try {
			Optional<UserManagement> model = (userManagementRepository.findById(userManagementId));

			if (model.isPresent()) {

				UserManagement entity = model.get();

				String otpCode = FileUtils.getRandomOTPnumber(6);
				entity.setOtpCode(otpCode);

				boolean sent = emailService.sendCustomerVerifyEmail(entity.getEmail(), otpCode,entity.getUser());
				if (sent) {

					entity.setOtpExpiryOn(calculatemintus(new Date()));
					entity.setOtpVerificationStatus(sent);
					userManagementRepository.save(entity);

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("OTP SENT SUCCESSFULLY");
					structure.setData(entity);
					structure.setFlag(1);
				} else {
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("OTP SENT NOT SUCCESSFULLY");
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

	public ResponseStructure reset(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();
		try {
			UserManagement entity = userManagementRepository.findByEmail(model.getEmail());

			if (entity != null) {
				List<UserManagementPasswordHistory> historyList = userManagementPasswordRepository
						.findByUserManagement(entity);

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

	private ResponseStructure getReset(RequestModel model, UserManagement entity,
			List<UserManagementPasswordHistory> historyList) throws Exception {

		for (UserManagementPasswordHistory passwordModelHistory : historyList) {
			if (passwordModelHistory.getCurrentPasswordStatus() == 1) {

				passwordModelHistory.setCurrentPasswordStatus(0);
				userManagementPasswordRepository.save(passwordModelHistory);
			}
		}
		return resetPassword(entity, model);
	}

	private ResponseStructure resetPassword(UserManagement entity, RequestModel model) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		UserManagementPasswordHistory newPass = new UserManagementPasswordHistory();

		String salt = PasswordUtils.getSalt(30);
		String myecurePassword = PasswordUtils.generateSecurePassword(model.getPassword(), salt);

		newPass.setUserPassword(myecurePassword);
		newPass.setUserSaltKey(salt);
		newPass.setCurrentPasswordStatus(1);
		newPass.setUserManagement(entity);

		userManagementPasswordRepository.save(newPass);

		structure.setMessage("Password Reset Succesfull");
		structure.setStatusCode(HttpStatus.OK.value());
		structure.setFlag(1);
		structure.setData(newPass);

		return structure;
	}
}
