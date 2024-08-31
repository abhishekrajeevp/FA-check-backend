package com.bp.middleware.signers;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.bp.middleware.bond.MerchantBond;
import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.signerpositiontracker.SignerPositionTrackerRepository;
import com.bp.middleware.signmerchant.MerchantModel;
import com.bp.middleware.signmerchant.MerchantRepository;
import com.bp.middleware.smartrouteverification.SmartRouteUtils;
import com.bp.middleware.sms.SMSRepository;
import com.bp.middleware.sms.SMSService;
import com.bp.middleware.uploadhistory.UploadRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.GetPublicIpAndLocation;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

import jakarta.servlet.ServletContext;

@Component
public class SignerUtils {

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
	private AadhaarSigningVerification signingVerification;
	@Autowired
	private AadhaarOtpSignSubmitVerification otpSignSubmitVerification;
	@Autowired
	private GetPublicIpAndLocation ipAndLocation;
	@Autowired
	private SignerPositionTrackerRepository signerPositionTrackerRepository;
	@Autowired
	private SmartRouteUtils smartRouteUtils;

	public void setSignerRequestToSuccess(SignerModel signerModel, boolean sign, String message) throws Exception {

		Request merchantReq = reqRepository.findByMerchantAndOtp(signerModel.getMerchantModel(),
				signerModel.getOtpcode());

		List<SignerModel> amountAlreadyDeducted = repository
				.findByMerchantModelAndOtpVerificationStatus(signerModel.getMerchantModel(), true);

		if (merchantReq != null && amountAlreadyDeducted.size() == 1 || amountAlreadyDeducted.size() == 0) {

			EntityModel userModel = signerModel.getEntityModel();
			VendorVerificationModel vendorVerifyModel = vendorVerificationRepository
					.findByVerificationDocument("DIGITAL SIGNER");

			MerchantPriceModel merchantPriceModel = merchantPriceRepository
					.getByEntityModelAndVendorVerificationModelAndStatus(userModel, vendorVerifyModel, true);

			if (sign) {

				merchantReq.setStatus("Success");
				merchantReq.setMessage("Document Signing success");
				merchantReq.setSignerCount(1);
				merchantReq.setFreeHit(false);

				// Prepaid Amount Reduction
				if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

					smartRouteUtils.deductAmountForSignature(userModel, merchantPriceModel,
							merchantReq.getBondAmount());

				} else {

					smartRouteUtils.postpaidConsumedAmountForSignature(userModel, merchantPriceModel,
							merchantReq.getBondAmount());
				}

			}

			userRepository.save(userModel);
			reqRepository.save(merchantReq);
		}
	}

	public ResponseStructure balanceCheck(SignerModel signer, String requestIdentifier,
			boolean priceCheckBeforOtpSending) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		List<SignerModel> amountAlreadyDeducted = repository
				.findByMerchantModelAndOtpVerificationStatus(signer.getMerchantModel(), true);

//		if (amountAlreadyDeducted.isEmpty()) {

		MerchantModel merchant = signer.getMerchantModel();
		MerchantBond bond = signer.getMerchantModel().getMerchantBond();

		EntityModel userModel = signer.getEntityModel();

		VendorVerificationModel vendorVerifyModel = vendorVerificationRepository
				.findByVerificationDocument("DIGITAL SIGNER");

		MerchantPriceModel merchantPriceModel = merchantPriceRepository
				.getByEntityModelAndVendorVerificationModelAndStatus(userModel, vendorVerifyModel, true);

		if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

			if (merchantPriceModel != null) {

				double price = merchantPriceModel.getSignaturePrice();

				if (amountAlreadyDeducted.isEmpty() && bond !=null) {  // Bond amount should be deducted only once

						price = price + bond.getBondAmount();
				}
				
				double gst = price * 18 / 100;
				double total = price + gst;

				if (userModel.getRemainingAmount() > total) {

					if (priceCheckBeforOtpSending) {
						structure.setFlag(1);
					}
					structure.setFlag(1);

					setRequest(userModel, merchant, bond, merchantPriceModel, vendorVerifyModel, requestIdentifier,
							signer,amountAlreadyDeducted);

				} else {

					structure.setMessage("Your wallet is empty,Please recharge to continue.");
					structure.setData(null);
					structure.setFlag(5);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			} else {

				structure.setMessage("Access not granted");
				structure.setData(null);
				structure.setFlag(5);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} else if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {
			if (LocalDate.now().isEqual(userModel.getGraceDate())
					|| LocalDate.now().isBefore(userModel.getGraceDate())) {

				if (priceCheckBeforOtpSending) {
					structure.setFlag(1);
				}
				structure.setFlag(1);

				setRequest(userModel, merchant, bond, merchantPriceModel, vendorVerifyModel, requestIdentifier, signer,amountAlreadyDeducted);

			} else {

				structure.setData(null);
				structure.setFlag(5);
				structure.setMessage("Your payment is pending, Please pay the amount to continue.");
				structure.setStatusCode(HttpStatus.OK.value());
			}
		}

//		} else {
//			structure.setFlag(1);
//		}

		return structure;
	}

	private void setRequest(EntityModel user, MerchantModel merchant, MerchantBond bondModel,
			MerchantPriceModel merchantPriceModel, VendorVerificationModel vendorVerifyModel, String requestIdentifier,
			SignerModel signer, List<SignerModel> amountAlreadyDeducted) throws Exception {

		Request request = new Request();

		request.setReferenceId(FileUtils.getRandomAlphaNumericString());
		request.setRequestBy(user.getName());
		request.setRequestDateAndTime(new Date());
		request.setCompanyName(merchant.getMerchantCompanyName());
		request.setFullName(user.getName());
		request.setDocumentTitle(merchant.getDocumentTitle());
		request.setDocExpiryAt(merchant.getDocumentExpiryAt().toString());
		request.setSignerCount(0);
		request.setSignerRefId(requestIdentifier);
		request.setOtp(signer.getOtpcode());
		request.setStatus("Pending");
		request.setFreeHit(true);

		double price = merchantPriceModel.getSignaturePrice();
		double gst = price * 18 / 100;

		double total = price + gst;

		if (bondModel != null && amountAlreadyDeducted.isEmpty()) {

			request.setBond(true);
			request.setPrice(total + bondModel.getBondAmount() + bondModel.getBondAmount() * 18 / 100);
			request.setBondAmount(bondModel.getBondAmount());
		} else {

			request.setBond(false);
			request.setPrice(total);
			request.setBondAmount(0);
		}

		request.setUser(user);
		request.setVerificationModel(vendorVerifyModel);
		request.setMerchant(merchant);

		reqRepository.save(request);
	}

	public ResponseStructure allThreeBalanceCheck(SignerModel signer) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		List<SignerModel> amountAlreadyDeducted = repository
				.findByMerchantModelAndOtpVerificationStatus(signer.getMerchantModel(), true);

//		if (amountAlreadyDeducted.isEmpty()) {

			MerchantBond bond = signer.getMerchantModel().getMerchantBond();

			EntityModel userModel = signer.getEntityModel();

			VendorVerificationModel digitalSignerVerification = vendorVerificationRepository
					.findByVerificationDocument(AppConstants.DIGITAL_SIGNER);

			VendorVerificationModel aadharXmlVerification = vendorVerificationRepository
					.findByVerificationDocument(AppConstants.AADHAR_XML_VERIFY);

			VendorVerificationModel aadharOtpVerification = vendorVerificationRepository
					.findByVerificationDocument(AppConstants.AADHAR_OTP_VERIFY);

			MerchantPriceModel digitalSignMerchantPrice = merchantPriceRepository
					.getByEntityModelAndVendorVerificationModelAndStatus(userModel, digitalSignerVerification, true);

			VendorModel vendorModel = new VendorModel();

			if (AppConstants.SUREPASS_ROUTE) {
				vendorModel = vendorRepository.findByVendorName(AppConstants.SUREPASS_VENDOR);
			} else {
				vendorModel = vendorRepository.findByVendorName(AppConstants.SIGN_DESK_VENDOR);
			}

			MerchantPriceModel aadhaarXmlMerchantPrice = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel,
							aadharXmlVerification, userModel, true);
			MerchantPriceModel aadhaarOtpMerchantPrice = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel,
							aadharOtpVerification, userModel, true);

			if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

				if (digitalSignMerchantPrice != null && aadhaarXmlMerchantPrice != null
						&& aadhaarOtpMerchantPrice != null) {

					double price = digitalSignMerchantPrice.getSignaturePrice() + aadhaarXmlMerchantPrice.getIdPrice()
							+ aadhaarOtpMerchantPrice.getIdPrice();

					if (bond != null && amountAlreadyDeducted.isEmpty()) {

						price = price + bond.getBondAmount();
					}

					double gst = price * 18 / 100;
					double total = price + gst;

					if (userModel.getRemainingAmount() > total) {

						structure.setFlag(1);

					} else {

						structure.setMessage(
								"Your wallet doesn't have enough balance to proceed with Aadhaar based Digital Signing");
						structure.setData(null);
						structure.setFlag(5);
						structure.setStatusCode(HttpStatus.OK.value());
					}
				} else {

					if (digitalSignMerchantPrice == null) {
						structure.setMessage("Access not granted for Digital Signing");
					} else if (aadhaarXmlMerchantPrice == null) {
						structure.setMessage("Access not granted for Aadhar Submit");
					} else {
						structure.setMessage("Access not granted for Aadhar OTP Submit");
					}
					structure.setData(null);
					structure.setFlag(5);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {
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

//		} else {
//			structure.setFlag(1);
//		}

		return structure;
	}

	public ResponseStructure twoBalanceCheck(SignerModel signer, String randomOtp) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		List<SignerModel> amountAlreadyDeducted = repository
				.findByMerchantModelAndOtpVerificationStatus(signer.getMerchantModel(), true);

//		if (amountAlreadyDeducted.isEmpty()) {

			MerchantBond bond = signer.getMerchantModel().getMerchantBond();

			EntityModel userModel = signer.getEntityModel();

			VendorVerificationModel digitalSignerVerification = vendorVerificationRepository
					.findByVerificationDocument(AppConstants.DIGITAL_SIGNER);

			VendorVerificationModel aadharOtpVerification = vendorVerificationRepository
					.findByVerificationDocument(AppConstants.AADHAR_OTP_VERIFY);

			MerchantPriceModel digitalSignMerchantPrice = merchantPriceRepository
					.getByEntityModelAndVendorVerificationModelAndStatus(userModel, digitalSignerVerification, true);

			VendorModel vendorModel = new VendorModel();

			if (AppConstants.SUREPASS_ROUTE) {
				vendorModel = vendorRepository.findByVendorName(AppConstants.SUREPASS_VENDOR);
			} else {
				vendorModel = vendorRepository.findByVendorName(AppConstants.SIGN_DESK_VENDOR);
			}

			MerchantPriceModel aadhaarOtpMerchantPrice = merchantPriceRepository
					.getByVendorModelAndVendorVerificationModelAndEntityModelAndStatus(vendorModel,
							aadharOtpVerification, userModel, true);

			if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

				if (digitalSignMerchantPrice != null && aadhaarOtpMerchantPrice != null) {

					double price = digitalSignMerchantPrice.getSignaturePrice() + aadhaarOtpMerchantPrice.getIdPrice();

					if (bond != null && amountAlreadyDeducted.isEmpty()) {

						price = price + bond.getBondAmount();
					}

					double gst = price * 18 / 100;
					double total = price + gst;

					if (userModel.getRemainingAmount() > total) {

						structure.setFlag(1);

						signer.setOtpcode(randomOtp);

						setRequest(userModel, signer.getMerchantModel(), bond, digitalSignMerchantPrice,
								digitalSignerVerification, "", signer,amountAlreadyDeducted);

					} else {

						structure.setMessage(
								"Your wallet doesn't have enough balance to proceed with Aadhaar based Digital Signing");
						structure.setData(null);
						structure.setFlag(5);
						structure.setStatusCode(HttpStatus.OK.value());
					}
				} else {

					if (digitalSignMerchantPrice == null) {
						structure.setMessage("Access not granted for Digital Signing");
					} else {
						structure.setMessage("Access not granted for Aadhar OTP Submit");
					}
					structure.setData(null);
					structure.setFlag(5);
					structure.setStatusCode(HttpStatus.OK.value());
				}

			} else if (userModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {
				if (LocalDate.now().isEqual(userModel.getGraceDate())
						|| LocalDate.now().isBefore(userModel.getGraceDate())) {

					structure.setFlag(1);
					signer.setOtpcode(randomOtp);

					setRequest(userModel, signer.getMerchantModel(), bond, digitalSignMerchantPrice,
							digitalSignerVerification, "", signer,amountAlreadyDeducted);

				} else {

					structure.setData(null);
					structure.setFlag(5);
					structure.setMessage("Your payment is pending, Please pay the amount to continue.");
					structure.setStatusCode(HttpStatus.OK.value());
				}
			}

//		} else {
//			structure.setFlag(1);
//		}

		return structure;
	}

}
