package com.bp.middleware.prepaidinvoicesdetails;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.bankaccountandhsncode.AdminBankAccRepository;
import com.bp.middleware.bankaccountandhsncode.AdminBankAccount;
import com.bp.middleware.bankaccountandhsncode.HsnCode;
import com.bp.middleware.bankaccountandhsncode.HsnCodeRepository;
import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.payment.PaymentRepository;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.signers.SignerRepository;
import com.bp.middleware.signmerchant.MerchantRepository;
import com.bp.middleware.transaction.TransactionRepository;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.InvoiceGenerate;
import com.bp.middleware.vendors.VendorVerificationModel;

import jakarta.servlet.ServletContext;

@Component
public class PrepaidUtils {

	
	@Autowired
	private RequestRepository requestRepository;
	@Autowired
	private ServletContext context;
	@Autowired
	EmailService emailService;
	@Autowired
	InvoiceGenerate invoiceGenerate;
	@Autowired
	FileUtils fu;
	@Autowired
	private AdminBankAccRepository bankAccRepository;
	@Autowired
	private HsnCodeRepository hsnCodeRepository;
	
	
	public List<Map<String, Object>> getUsedServices(List<Request> hitLogs, EntityModel entityModel, String firstDay,
			String lastDay) throws Exception {

		Set<VendorVerificationModel> verificationSet = new LinkedHashSet<>();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (Request req : hitLogs) {
			verificationSet.add(req.getVerificationModel());
		}

		for (VendorVerificationModel vendorVerificationModel : verificationSet) {

			List<Request> betweenVerificationtype = requestRepository.getBetweenVerificationtype(
					entityModel.getUserId(), vendorVerificationModel.getVendorVerificationId(), firstDay, lastDay);
			String verifyPrice = requestRepository.getSummedAmountWithVerification(entityModel.getUserId(),
					vendorVerificationModel.getVendorVerificationId(), firstDay, lastDay);

			if (verifyPrice == null) {
				verifyPrice = "0";
			}

			double usedAmount = fu.twoDecimelDouble(Double.parseDouble(verifyPrice));
			
//			double baseAmount = getBaseAmount(totalAmount);
//			double amountUsed = fu.twoDecimelDouble(baseAmount);
			double amountUsedGst = fu.twoDecimelDouble(usedAmount*18/100);
			double totalAmount = fu.twoDecimelDouble(usedAmount+amountUsedGst);
			
			Map<String, Object> data = new LinkedHashMap<>();

			data.put("verificationService", vendorVerificationModel.getVerificationDocument());
			data.put("hitCount", betweenVerificationtype.size());
			data.put("amountUsed", usedAmount);
			data.put("amountUsedGst", amountUsedGst);
			data.put("totalAmount", totalAmount);

			list.add(data);
		}

		return list;
	}
	
	
	
	public double getMonthHitPrice(EntityModel entityModel, String startDate, String endDate) throws Exception {

		String stringPrice = requestRepository.getSummedAmount(entityModel.getUserId(), startDate, endDate);

		if (stringPrice == null) {
			stringPrice = "0";
		}

		return fu.twoDecimelDouble(Double.parseDouble(stringPrice));
	}
	
	
	
	
	public String getHsnNumber() throws Exception{
		
		String hsnNo ="";
        HsnCode hsn = hsnCodeRepository.findByStatus(true);
		
        if(hsn!=null) {
        	hsnNo=hsn.getHsnNumber();
        }
        return hsnNo;
	}
	
	
	
	
	public String getAccountNumber() throws Exception{
		
		String accNo = "";
        AdminBankAccount bankAcc = bankAccRepository.findByStatus(true);
        
        if(bankAcc!=null) {
        	accNo=bankAcc.getAccountNumber();
        }
        return accNo;
	}
	
	
	
	public double getBaseAmount(double amount) throws Exception{
		
		double baseAmount = amount/(1+(18.0/100));
		
		return fu.twoDecimelDouble(baseAmount) ;
	}
	
	
	
	
	public String saveMonthlyInvo(MultipartFile profilePhoto) {

		try {
			String extensionType = null;
			
			StringTokenizer st = new StringTokenizer(profilePhoto.getOriginalFilename(), ".");
			while (st.hasMoreElements()) {
				extensionType = st.nextElement().toString();
			}
			String fileName = profilePhoto.getOriginalFilename();
			Path currentWorkingDir = Paths.get(context.getRealPath("/WEB-INF/"));
			File saveFile = new File(currentWorkingDir + "/PrepaidMonthlyInvoice/");
			saveFile.mkdir();

			byte[] bytes = profilePhoto.getBytes();
			Path path = Paths.get(saveFile + "/" + fileName);
			Files.write(path, bytes);
			
			return fileName;

		} catch (Exception e) {
			return null;
		}
	}
	
	
	
	public String saveConveInvo(MultipartFile profilePhoto) {

		try {
			String extensionType = null;
			StringTokenizer st = new StringTokenizer(profilePhoto.getOriginalFilename(), ".");
			while (st.hasMoreElements()) {
				extensionType = st.nextElement().toString();
			}
			String fileName = profilePhoto.getOriginalFilename();
			Path currentWorkingDir = Paths.get(context.getRealPath("/WEB-INF/"));
			File saveFile = new File(currentWorkingDir + "/PrepaidConveInvoice/");
			saveFile.mkdir();

			byte[] bytes = profilePhoto.getBytes();
			Path path = Paths.get(saveFile + "/" + fileName);
			Files.write(path, bytes);
			return fileName;

		} catch (Exception e) {
			return null;
		}
	}



	public String getIfscCode() {
		
		String ifsc = "";
        AdminBankAccount bankAcc = bankAccRepository.findByStatus(true);
        
        if(bankAcc!=null) {
        	ifsc=bankAcc.getIfscCode();
        }
        return ifsc;
	}



	public String getBankName() {
		
		String name = "";
        AdminBankAccount bankAcc = bankAccRepository.findByStatus(true);
        
        if(bankAcc!=null) {
        	name=bankAcc.getBankName();
        }
        return name;
	}
	
	
}
