package com.bp.middleware.requestandresponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.bp.middleware.merchantapipricesetup.MerchantPriceModel;
import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.sprintverify.SprintVerifyGenerator;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.vendorapipricesetup.VendorPriceModel;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

public class  SprintVerificationServiceImplement {

	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MerchantPriceRepository merchantPriceRepository;

	@Autowired
	private VendorRepository vendorRepository;

	@Autowired
	private VendorVerificationRepository verificationRepository;

	@Autowired
	private VendorPriceRepository vendorPriceRepository;
	
	@Autowired
	private ResponseRepository respRepository;

	@Autowired
	private RequestRepository reqRepository;
	
	
	
     public static String sprintVerifyDocument(JSONObject obj) {
		
		String jwtToken = SprintVerifyGenerator.sprintVJwtToken();
		
        String apiEndpoint ="https://uat.paysprint.in/sprintverify-uat/api/v1/verification/pan_verify";

        HttpClient httpClient = HttpClient.newBuilder().build();

        String requestBody = obj.toString();

        HttpRequest request = HttpRequest.newBuilder()
        		.uri(URI.create(apiEndpoint))
				.header("accept", "application/json").header("Content-Type", "application/json")
				.header("Token", jwtToken).header("authorisedkey", "TVRJek5EVTJOelUwTnpKRFQxSlFNREF3TURFPQ==")
				.method("POST", HttpRequest.BodyPublishers
						.ofString(requestBody))
				.build();

        CompletableFuture<HttpResponse<String>> responseFuture =
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> response=responseFuture.join();
        
        int statusCode=response.statusCode();
        String responseBody = response.body();
        
        System.out.println("Response Code: " + statusCode);
		
        return responseBody;
	}
	
	public ResponseStructure balanceCheck(EntityModel userModel, MerchantPriceModel merchantPriceModel,
			VendorVerificationModel verificationModel) {

		ResponseStructure structure = new ResponseStructure();

		if (userModel.getPaymentMethod().getPaymentId() == 2) {
			if (userModel.getRemainingAmount() > merchantPriceModel.getIdPrice()) {

				structure.setCount(1);

			} else {
				userModel.setPaymentStatus("Dues");
				userRepository.save(userModel);

				structure.setCount(0);
				structure.setMessage("Please Recharge Amount");
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} else if (userModel.getPaymentMethod().getPaymentId() == 1) {
			if (LocalDate.now().isEqual(userModel.getGraceDate())
					|| LocalDate.now().isBefore(userModel.getGraceDate())) {

				structure.setCount(1);

			} else {
				structure.setCount(0);
				structure.setMessage("Please pay the Amount");
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}
		} else {
			structure.setCount(0);
			structure.setMessage("PAYMENT METHOD NOT AVAILABLE");
		}
		return structure;
	}
	
	
	public  ResponseStructure sprintVPan(RequestModel model, EntityModel userModel2, VendorVerificationModel vendorVerifyModel, VendorModel vendorModel2, MerchantPriceModel merchantPriceModel2, VendorPriceModel vendorPrice, String userSecretKey, String userDecryption) {
		ResponseStructure structure=new ResponseStructure();
		try {
			EntityModel userModel = userRepository.findByUserId(model.getUserId());
			VendorVerificationModel verificationModel = verificationRepository.findByVendorVerificationId(model.getVendorVerificationId());
			VendorModel vendorModel = vendorRepository.findByVendorId(model.getVendorId());
			MerchantPriceModel merchantPriceModel = merchantPriceRepository.findByVendorModelAndVendorVerificationModelAndEntityModel(vendorModel, verificationModel, userModel);
			VendorPriceModel vendorPriceModel = vendorPriceRepository.findByVendorModelAndVendorVerificationModel(vendorModel, verificationModel);

			ResponseStructure balance=balanceCheck(userModel,merchantPriceModel,verificationModel);

			if(balance.getCount()==1) {
				if (verificationModel.getVendorVerificationId() == 1) {
					
					return sprintVPanVerificatoin(model,userModel,verificationModel,vendorModel,merchantPriceModel,vendorPriceModel);
					
				} else {
					structure.setMessage("PLEASE SELECT THE APPROPRIATE VERIFICATION NUMBER");
				}
			}else if(balance.getCount()==0) {
				return balance;
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(5);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	private ResponseStructure sprintVPanVerificatoin(RequestModel model, EntityModel userModel,VendorVerificationModel verificationModel, VendorModel vendorModel, MerchantPriceModel merchantPriceModel,
			  VendorPriceModel vendorPriceModel) {
		
		ResponseStructure structure=new ResponseStructure();
		try {
			Request request = new Request();
			Date reqDate = new Date();
			
			
			String panNumber = model.getPanNumber();
			
			JSONObject obj = new JSONObject();
			obj.put("pannumber", panNumber);
			
			String sprintVerifyResponse = SprintVerifyGenerator.sprintVerifyDocument(obj);
			
			
			
			
			//String commonResponse=CommonResponseStructure.commonResponsePan(request,response,userModel);
			
			Map<String, Object> mapNew = new HashMap<>();
			//mapNew.put("return_response", commonResponse);
			 
			structure.setData(mapNew);
			
		} catch (Exception e) {
			structure.setData(null);
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		
		return structure;
		
	}
	
}
