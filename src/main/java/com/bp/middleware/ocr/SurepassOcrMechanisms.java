package com.bp.middleware.ocr;

import java.io.File;
import java.io.FileOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

@Component
public class SurepassOcrMechanisms {

	@Autowired
	private ResponseRepository respRepository;
	@Autowired
	private RequestRepository reqRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private VendorPriceRepository vendorPriceRepository;

	
	public String ocrMechanisms(EntityModel entityModel, MerchantPriceModel merchantPriceModel, MultipartFile file)
			throws Exception {

		VendorPriceModel vendorPriceModel = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(
				merchantPriceModel.getVendorModel(), merchantPriceModel.getVendorVerificationModel());

		String token = AppConstants.SUREPASS_TOKEN;
		String url = vendorPriceModel.getApiLink();

		File path = convertMultiartToFile(file);

		HttpResponse<String> response = Unirest.post(url).header("Authorization", token).field("file", path).asString();
		
		return response.getBody();

	}

	
	public String ocrMechanismsWithTwoMultiparts(EntityModel entityModel, MerchantPriceModel merchantPriceModel, MultipartFile file,MultipartFile file2)
			throws Exception {

		VendorPriceModel vendorPriceModel = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(
				merchantPriceModel.getVendorModel(), merchantPriceModel.getVendorVerificationModel());

		String token = AppConstants.SUREPASS_TOKEN;
		String url = vendorPriceModel.getApiLink();

		File front = convertMultiartToFile(file);
		File back = convertMultiartToFile(file2);

		HttpResponse<String> response = Unirest.post(url).header("Authorization", token).field("front", front).field("back", back).asString();
		
		return response.getBody();

	}
	
	private File convertMultiartToFile(MultipartFile file) throws Exception {

		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;

	}
}
