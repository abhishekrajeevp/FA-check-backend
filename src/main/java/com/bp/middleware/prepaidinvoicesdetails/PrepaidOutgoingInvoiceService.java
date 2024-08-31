package com.bp.middleware.prepaidinvoicesdetails;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.conveniencefee.ConveniencePercentageEntity;
import com.bp.middleware.emailservice.EmailService;
import com.bp.middleware.modeofpgpayment.ModeOfPaymentPg;
import com.bp.middleware.payment.PaymentMethod;
import com.bp.middleware.payment.PaymentRepository;
import com.bp.middleware.prepaidpostpaid.PostpaidRepository;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.signers.SignerRepository;
import com.bp.middleware.signmerchant.MerchantRepository;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.transaction.TransactionRepository;
import com.bp.middleware.transactionamountmodel.TransactionAmountModel;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AmountToWords;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;
import com.bp.middleware.util.InvoiceGenerate;

import jakarta.servlet.ServletContext;

@Service
public class PrepaidOutgoingInvoiceService {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RequestRepository requestRepository;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	EmailService emailService;
	@Autowired
	InvoiceGenerate invoiceGenerate;
	@Autowired
	FileUtils fu;
	@Autowired
	private PaymentRepository paymentMethodRepository;
	@Autowired
	private PrepaidUtils prepaidUtils;

	public ResponseStructure prepaidMonthEndInvoice() {

		ResponseStructure structure = new ResponseStructure();

		try {

			PaymentMethod paymentMethod = paymentMethodRepository.findByPaymentType("Prepaid");

			List<PrepaidMonthlyInvoice> monthlyInvoList = new ArrayList<>();

			if (paymentMethod != null) {

				List<EntityModel> entityList = userRepository.findByPaymentMethod(paymentMethod);

				LocalDate now = LocalDate.now();
				String firstDay = now.with(TemporalAdjusters.firstDayOfMonth()).toString() + " 00:00:00.0000000";
				String lastDay = now.with(TemporalAdjusters.lastDayOfMonth()).toString() + " 23:59:59.9999999";

				String accNo = prepaidUtils.getAccountNumber();
				String hsnNo = prepaidUtils.getHsnNumber();

				for (EntityModel entityModel : entityList) {

					List<Request> hitLogs = requestRepository.getBetween(entityModel.getUserId(), firstDay, lastDay);

					if (!hitLogs.isEmpty()) {

						double monthHitPrice = fu.twoDecimelDouble(prepaidUtils.getMonthHitPrice(entityModel, firstDay, lastDay));
						double monthHitGst = fu.twoDecimelDouble(monthHitPrice*18/100);

						double monthHitPriceWithGst = fu
								.twoDecimelDouble(monthHitPrice+monthHitGst);
						
						int totalHits = hitLogs.size();

						List<Map<String, Object>> usedServices = prepaidUtils.getUsedServices(hitLogs, entityModel,
								firstDay, lastDay);

						PrepaidMonthlyInvoice invo = new PrepaidMonthlyInvoice();

						invo.setStartDate(now.with(TemporalAdjusters.firstDayOfMonth()));
						invo.setEndDate(now.with(TemporalAdjusters.lastDayOfMonth()));
						
						invo.setUserId(entityModel.getUserId());
						invo.setEntityName(entityModel.getName());
						invo.setEntityAddress(entityModel.getAddress());
						invo.setEntityCountry(entityModel.getCountryName());
						invo.setEntityState(entityModel.getStateName());
						invo.setEntityCity(entityModel.getCityName());
						invo.setEntityPincode(entityModel.getPincode());
						invo.setEntityGst(entityModel.getGst());
						invo.setEntityContactNumber(entityModel.getMobileNumber());
						invo.setEntityMail(entityModel.getEmail());

						invo.setIfscCode(prepaidUtils.getIfscCode());
						invo.setBankName(prepaidUtils.getBankName());
						invo.setAdminBankAccountNo(accNo);
						invo.setAdminHsnCode(hsnNo);
						invo.setMonth(LocalDate.now().getMonth().toString());
						invo.setInvoiceNumber("Inv_" + fu.getRandomAlphaNumericString());

						invo.setTotalHits(totalHits);
						invo.setUsedAmount(monthHitPrice);
						invo.setUsedAmountGst(monthHitGst);
						invo.setTotalAmount(monthHitPriceWithGst);

						invo.setUsedAmountInWords(AmountToWords.convertAmountToWords(monthHitPrice));
						invo.setUsedAmountGstInWords(AmountToWords.convertAmountToWords(monthHitGst));
						invo.setTotalAmountInWords(AmountToWords.convertAmountToWords(monthHitPriceWithGst));

						invo.setUsedServices(usedServices);

						monthlyInvoList.add(invo);
					}
				}

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(monthlyInvoList);
				structure.setFlag(1);

			} else {

				structure.setMessage("PREPAID PAYMENT METHOD IS NOT FOUND");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(4);
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

	public ResponseStructure prepaidConvenienceInvoice(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			TransactionDto trans = transactionRepository.findByPrepaidId(model.getPrepaidId());

			if (trans != null) {

				EntityModel entityModel = trans.getEntity();
				ModeOfPaymentPg modeOfPaymentPg = trans.getModeOfPaymentPg();
				ConveniencePercentageEntity convenience = trans.getConveniencePercentageEntity();
				TransactionAmountModel transactionAmountModel = trans.getTransactionAmountModel();

				String accNo = prepaidUtils.getAccountNumber();
				String hsnNo = prepaidUtils.getHsnNumber();

				if (transactionAmountModel !=null && transactionAmountModel.isConvenienceFetched()) {

					double conveniencePercentage = fu.twoDecimelDouble(convenience.getConveniencePercentage());
					
					double paidAmount = fu.twoDecimelDouble(trans.getExclusiveAmount());
					double fixedAmount = fu.twoDecimelDouble(transactionAmountModel.getFixedFee());
					double convenienceFee = fu.twoDecimelDouble(transactionAmountModel.getConvenianceFee());
					double gst = fu.twoDecimelDouble(transactionAmountModel.getOtherGst());
					
					double totalAmount = fu.twoDecimelDouble(fixedAmount+convenienceFee+gst);
					
					PrepaidConvenienceInvoice invo = new PrepaidConvenienceInvoice();

					invo.setUserId(entityModel.getUserId());
					invo.setPrepaidId(trans.getPrepaidId());
					invo.setTransactionId(trans.getTrancsactionId());
					
					invo.setEntityName(entityModel.getName());
					invo.setEntityAddress(entityModel.getAddress());
					invo.setEntityCountry(entityModel.getCountryName());
					invo.setEntityState(entityModel.getStateName());
					invo.setEntityCity(entityModel.getCityName());
					invo.setEntityPincode(entityModel.getPincode());
					invo.setEntityGst(entityModel.getGst());
					invo.setEntityContactNumber(entityModel.getMobileNumber());
					invo.setEntityMail(entityModel.getEmail());

					invo.setIfscCode(prepaidUtils.getIfscCode());
					invo.setBankName(prepaidUtils.getBankName());
					invo.setAdminBankAccountNo(accNo);
					invo.setAdminHsnCode(hsnNo);
					invo.setConveInvoiceNo("C-inv_" + fu.getRandomAlphaNumericString());
					invo.setModeOfPayment(modeOfPaymentPg.getModeOfPayment());

					invo.setPaidAmount(paidAmount);
					invo.setFixedAmount(fixedAmount);
					invo.setConvenienceAmount(convenienceFee);
					invo.setConveniencePercentage(conveniencePercentage);
					invo.setGst(gst);
					invo.setTotalAmount(totalAmount);
					invo.setTotalAmountInWords(AmountToWords.convertAmountToWords(totalAmount));
					
					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(invo);
					structure.setFlag(1);

				} else {

					structure.setMessage("NO CONVENIENCE FEE IS FETCHED");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(4);
				}
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

	
	
	
	public ResponseStructure prepaidForcedInvoice(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel entityModel = userRepository.findByUserId(model.getUserId());

			if (entityModel != null && entityModel.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

				LocalDate now = LocalDate.now();
				String firstDay = model.getStartDate() + " 00:00:00.0000000";
				String lastDay = model.getEndDate() + " 23:59:59.9999999";

				String accNo = prepaidUtils.getAccountNumber();
				String hsnNo = prepaidUtils.getHsnNumber();

				List<Request> hitLogs = requestRepository.getBetween(entityModel.getUserId(), firstDay, lastDay);

					if (!hitLogs.isEmpty()) {

						double monthHitPrice = fu.twoDecimelDouble(prepaidUtils.getMonthHitPrice(entityModel, firstDay, lastDay));
						double monthHitGst = fu.twoDecimelDouble(monthHitPrice*18/100);

						double monthHitPriceWithGst = fu
								.twoDecimelDouble(monthHitPrice+monthHitGst);

						int totalHits = hitLogs.size();

						List<Map<String, Object>> usedServices = prepaidUtils.getUsedServices(hitLogs, entityModel,
								firstDay, lastDay);

						PrepaidMonthlyInvoice invo = new PrepaidMonthlyInvoice();

						invo.setStartDate(model.getStartDate());
						invo.setEndDate(model.getEndDate());
						
						invo.setUserId(entityModel.getUserId());
						invo.setEntityName(entityModel.getName());
						invo.setEntityAddress(entityModel.getAddress());
						invo.setEntityCountry(entityModel.getCountryName());
						invo.setEntityState(entityModel.getStateName());
						invo.setEntityCity(entityModel.getCityName());
						invo.setEntityPincode(entityModel.getPincode());
						invo.setEntityGst(entityModel.getGst());
						invo.setEntityContactNumber(entityModel.getMobileNumber());
						invo.setEntityMail(entityModel.getEmail());

						invo.setIfscCode(prepaidUtils.getIfscCode());
						invo.setBankName(prepaidUtils.getBankName());
						invo.setAdminBankAccountNo(accNo);
						invo.setAdminHsnCode(hsnNo);
						invo.setMonth(LocalDate.now().getMonth().toString());
						invo.setInvoiceNumber("Inv_" + fu.getRandomAlphaNumericString());

						invo.setTotalHits(totalHits);
						invo.setUsedAmount(monthHitPrice);
						invo.setUsedAmountGst(monthHitGst);
						invo.setTotalAmount(monthHitPriceWithGst);

						invo.setUsedAmountInWords(AmountToWords.convertAmountToWords(monthHitPrice));
						invo.setUsedAmountGstInWords(AmountToWords.convertAmountToWords(monthHitGst));
						invo.setTotalAmountInWords(AmountToWords.convertAmountToWords(monthHitPriceWithGst));

						invo.setUsedServices(usedServices);
						
						structure.setMessage(AppConstants.SUCCESS);
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(invo);
						structure.setFlag(1);

					}else {
						
						structure.setMessage("No Request details found between "+model.getStartDate()+" and "+model.getEndDate());
						structure.setStatusCode(HttpStatus.OK.value());
						structure.setData(null);
						structure.setFlag(2);
					}
			} else {

				if(entityModel==null) {
					structure.setMessage(AppConstants.NO_DATA_FOUND);
				}else {
					structure.setMessage("Given entity is not a prepaid user");
				}
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(4);
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

	
	
	
	public ResponseStructure prepaidMonthEndInvoiceMailTrigger() {

		ResponseStructure structure = new ResponseStructure();

		try {

			PaymentMethod paymentMethod = paymentMethodRepository.findByPaymentType("Prepaid");

			List<PrepaidMonthlyInvoice> monthlyInvoList = new ArrayList<>();

			if (paymentMethod != null) {

				List<EntityModel> entityList = userRepository.findByPaymentMethod(paymentMethod);

				LocalDate now = LocalDate.now();
				String firstDay = now.with(TemporalAdjusters.firstDayOfMonth()).toString() + " 00:00:00.0000000";
				String lastDay = now.with(TemporalAdjusters.lastDayOfMonth()).toString() + " 23:59:59.9999999";

				String accNo = prepaidUtils.getAccountNumber();
				String hsnNo = prepaidUtils.getHsnNumber();

				for (EntityModel entityModel : entityList) {

					List<Request> hitLogs = requestRepository.getBetween(entityModel.getUserId(), firstDay, lastDay);

					if (!hitLogs.isEmpty()) {

//						double monthHitPrice = fu.twoDecimelDouble(prepaidUtils.getMonthHitPrice(entityModel, firstDay, lastDay));
//						double monthHitGst = fu.twoDecimelDouble(monthHitPrice*18/100);
//
//						double monthHitPriceWithGst = fu
//								.twoDecimelDouble(monthHitPrice+monthHitGst);
//						
//						int totalHits = hitLogs.size();

						String startDate = now.with(TemporalAdjusters.firstDayOfMonth()).toString();
						String endDate = now.with(TemporalAdjusters.lastDayOfMonth()).toString();
						String month = now.getMonth().toString();
						
						emailService.prepaidInvoiceGenerateMail(entityModel, startDate,endDate,month);
						
					}
				}

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(monthlyInvoList);
				structure.setFlag(1);

			} else {

				structure.setMessage("PREPAID PAYMENT METHOD IS NOT FOUND");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(4);
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

}
