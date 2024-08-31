package com.bp.middleware.user;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.merchantapipricesetup.MerchantPriceServiceImplimentation;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

@Component
public class UserAutomaticPriceSet {

	
	@Autowired
	private VendorVerificationRepository vendorVerificationRepository;
	@Autowired
	private VendorPriceRepository vendorPriceRepository;
	@Autowired
	private VendorRepository vendorRepository;
	@Autowired
	private MerchantPriceRepository merchantPriceRepository;
	@Autowired
	private MerchantPriceServiceImplimentation merchantPriceServiceImplimentation;
	@Autowired
	private FileUtils fu;
	
	public ResponseStructure digitalSigning(EntityModel entityModel) throws Exception {

		ResponseStructure structure = new ResponseStructure();

		VendorModel vendorModel = vendorRepository.findByVendorName(AppConstants.BASISPAY_VENDOR);

		VendorVerificationModel vendorVerificationModel = vendorVerificationRepository
				.findByVerificationDocument(AppConstants.DIGITAL_SIGNER);

		if (vendorModel != null && vendorVerificationModel != null) {

			int priority = 1;

			int prioritySize = vendorRepository.findAll().size();

			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, vendorVerificationModel);

			MerchantPriceModel alreadyGiven = merchantPriceRepository
					.findByEntityModelAndVendorModelAndVendorVerificationModel(entityModel, vendorModel,
							vendorVerificationModel);
			
			System.err.println("OUTER BP");

			if (alreadyGiven == null && vendorPriceModel != null && priority <= prioritySize
					&& priority != 0 && entityModel.getDocumentPrice() >= vendorPriceModel.getSignaturePrice()) {

				System.err.println("OUTER BP");
				
				MerchantPriceModel merchantPriceModel = new MerchantPriceModel();

				merchantPriceModel.setName(vendorPriceModel.getName());
				merchantPriceModel.setApiLink("");
				merchantPriceModel.setApplicationId("");
				merchantPriceModel.setApiKey("");
				merchantPriceModel.setIdPrice(0);
				merchantPriceModel.setImagePrice(0);
				merchantPriceModel.setSignaturePrice(entityModel.getDocumentPrice());
				merchantPriceModel.setStatus(true);
				merchantPriceModel.setNoSourceCheck(false);
				merchantPriceModel.setCreatedBy(entityModel.getCreatedBy());
				merchantPriceModel.setCreatedAt(LocalDate.now());
				merchantPriceModel.setVendorModel(vendorModel);
				merchantPriceModel.setVendorVerificationModel(vendorVerificationModel);
				merchantPriceModel.setEntityModel(entityModel);
				merchantPriceModel.setAccepted(true);
				merchantPriceModel.setPriority(priority);

				merchantPriceRepository.save(merchantPriceModel);

				merchantPriceServiceImplimentation.addMerchantPriceTracker(merchantPriceModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(merchantPriceModel);
				structure.setMessage(AppConstants.SUCCESS);
				
			}else if (alreadyGiven!=null && alreadyGiven.getSignaturePrice() != fu.twoDecimelDouble(entityModel.getDocumentPrice())) {
				
				alreadyGiven.setSignaturePrice(entityModel.getDocumentPrice());
				
				merchantPriceRepository.save(alreadyGiven);
				merchantPriceServiceImplimentation.updateMerchantPriceTracker(alreadyGiven);
			}
		}
		return structure;
	}

	
	
	
	public void aadhaarXMLAndOTP(EntityModel user) throws Exception{

		VendorModel vendorModel = new VendorModel();
		
		if(AppConstants.SUREPASS_ROUTE) {
			vendorModel = vendorRepository.findByVendorName(AppConstants.SUREPASS_VENDOR);
		}else {
			vendorModel = vendorRepository.findByVendorName(AppConstants.SIGN_DESK_VENDOR);
		}

		VendorVerificationModel aadharXml = vendorVerificationRepository
				.findByVerificationDocument(AppConstants.AADHAR_XML_VERIFY);

		VendorVerificationModel aadharOtp = vendorVerificationRepository
				.findByVerificationDocument(AppConstants.AADHAR_OTP_VERIFY);

		 createMerchantPriceModel(vendorModel,aadharXml,user,user.getAadhaarXmlPrice()); // AADHAAR XML
		 
		 createMerchantPriceModel(vendorModel,aadharOtp,user,user.getAadhaarOtpPrice()); // AADHAAR OTP
	}
	
	
	
	public ResponseStructure createMerchantPriceModel(VendorModel vendorModel,VendorVerificationModel vendorVerificationModel,EntityModel entityModel,double price) throws Exception{
		
		ResponseStructure structure = new ResponseStructure();
		
		if (vendorModel != null && vendorVerificationModel != null) {

			int priority = 1;

			int prioritySize = vendorRepository.findAll().size();

			VendorPriceModel vendorPriceModel = vendorPriceRepository
					.findByVendorModelAndVendorVerificationModel(vendorModel, vendorVerificationModel);

			MerchantPriceModel alreadyGiven = merchantPriceRepository.findByEntityModelAndVendorVerificationModelAndPriority(entityModel, vendorVerificationModel, prioritySize);
			
			System.err.println(vendorVerificationModel.getVerificationDocument());
			System.err.println(alreadyGiven == null);
			System.err.println(vendorPriceModel != null);
			System.err.println(priority <= prioritySize);
			System.err.println(priority !=0);
			System.err.println(price >= vendorPriceModel.getIdPrice());
			
			if (alreadyGiven == null && vendorPriceModel != null && priority <= prioritySize
					&& priority !=0 && price >= vendorPriceModel.getIdPrice()) {

				System.err.println("INNER");
				
				MerchantPriceModel merchantPriceModel = new MerchantPriceModel();

				merchantPriceModel.setName(vendorPriceModel.getName());
				merchantPriceModel.setApiLink("");
				merchantPriceModel.setApplicationId("");
				merchantPriceModel.setApiKey("");
				merchantPriceModel.setIdPrice(price);
				merchantPriceModel.setImagePrice(0);
				merchantPriceModel.setSignaturePrice(0);
				merchantPriceModel.setStatus(true);
				merchantPriceModel.setNoSourceCheck(false);
				merchantPriceModel.setCreatedBy(entityModel.getCreatedBy());
				merchantPriceModel.setCreatedAt(LocalDate.now());
				merchantPriceModel.setVendorModel(vendorModel);
				merchantPriceModel.setVendorVerificationModel(vendorVerificationModel);
				merchantPriceModel.setEntityModel(entityModel);
				merchantPriceModel.setAccepted(true);
				merchantPriceModel.setPriority(priority);

				merchantPriceRepository.save(merchantPriceModel);

				merchantPriceServiceImplimentation.addMerchantPriceTracker(merchantPriceModel);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setFlag(1);
				structure.setData(merchantPriceModel);
				structure.setMessage(AppConstants.SUCCESS);
				
			}else if (alreadyGiven!=null && alreadyGiven.getIdPrice() != fu.twoDecimelDouble(price)) {
				
				alreadyGiven.setIdPrice(price);
				
				merchantPriceRepository.save(alreadyGiven);
				merchantPriceServiceImplimentation.updateMerchantPriceTracker(alreadyGiven);
			}
		}
		
		return structure;
	}
	
}
