package com.bp.middleware.postpaidstatement;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bp.middleware.prepaidstatement.PrepaidStatementUtils;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.FileUtils;

@Component
public class PostpaidStatementUtils {

	@Autowired
	private PostpaidUserStatementRepository postpaidUserStatementRepository;
	@Autowired
	private FileUtils fu;

	public void getSummedValues(Map<String, Object> map, EntityModel entityModel) throws Exception {

		int userId = entityModel.getUserId();

		String creditedTotal = postpaidUserStatementRepository.getCreditedBalance(userId);
		String debitedTotal = postpaidUserStatementRepository.getDebitedBalance(userId);
		String debitGstTotal = postpaidUserStatementRepository.getDebitGstBalance(userId);
		String creditGstTotal = postpaidUserStatementRepository.getCreditGstBalance(userId);

		double overallCreditedAmount = (creditedTotal == null) ? 0.0 : stringToDouble(creditedTotal);
		double overallDebitedAmount = (debitedTotal == null) ? 0.0 : stringToDouble(debitedTotal);
		double overallDebitedGstAmount = (debitGstTotal == null) ? 0.0 : stringToDouble(debitGstTotal);
		double overallCreditGstAmount = (creditGstTotal == null) ? 0.0 : stringToDouble(creditGstTotal);
		double overallConsumedAmount = overallDebitedAmount+overallDebitedGstAmount;
		
		map.put("overallConsumedAmount", overallConsumedAmount);
		map.put("overallCreditedAmount", overallCreditedAmount);
		map.put("overallDebitedAmount", overallDebitedAmount);
		map.put("overallDebitedGstAmount", overallDebitedGstAmount);
		map.put("overallCreditGstAmount", overallCreditGstAmount);

	}

	public double stringToDouble(String consumedTotal) throws Exception {

		double parseDouble = Double.parseDouble(consumedTotal);

		return fu.twoDecimelDouble(parseDouble);
	}

	public void getSummedValuesBetweenDates(Map<String, Object> map, EntityModel entityModel, LocalDate startDate,
			LocalDate endDate) throws Exception {

		int userId = entityModel.getUserId();

		String creditedTotal = postpaidUserStatementRepository.getCreditedBalanceBetweenDates(userId, startDate,endDate);
		String creditedGstTotal = postpaidUserStatementRepository.getCreditGstBalanceBetweenDates(userId, startDate,endDate);
		String debitedTotal = postpaidUserStatementRepository.getDebitedBalanceBetweenDates(userId, startDate, endDate);
		String debitGstTotal = postpaidUserStatementRepository.getDebitGstBalanceBetweenDates(userId, startDate,endDate);

		double overallCreditedAmount = (creditedTotal == null) ? 0.0 : stringToDouble(creditedTotal);
		double overallDebitedAmount = (debitedTotal == null) ? 0.0 : stringToDouble(debitedTotal);
		double overallDebitedGstAmount = (debitGstTotal == null) ? 0.0 : stringToDouble(debitGstTotal);
		double overallCreditGstAmount = (creditedGstTotal == null) ? 0.0 : stringToDouble(creditedGstTotal);
		double overallConsumedAmount = overallDebitedAmount+overallDebitedGstAmount;
		
		map.put("overallConsumedAmount", overallConsumedAmount);
		map.put("overallCreditedAmount", overallCreditedAmount);
		map.put("overallDebitedAmount", overallDebitedAmount);
		map.put("overallDebitedGstAmount", overallDebitedGstAmount);
		map.put("overallCreditGstAmount", overallCreditGstAmount);
	}

	
	
	
	public void getSummedValuesForMonth(Map<String, Object> map, EntityModel entityModel, String month,
			LocalDate startDate, LocalDate endDate) throws Exception{
		
		int userId = entityModel.getUserId();

		String creditedTotal = postpaidUserStatementRepository.getCreditedBalanceMonth(userId,month,startDate,endDate);
		String debitedTotal = postpaidUserStatementRepository.getDebitedBalanceMonth(userId,month,startDate,endDate);
		String debitGstTotal = postpaidUserStatementRepository.getDebitGstBalanceMonth(userId,month,startDate,endDate);
		String creditGstTotal = postpaidUserStatementRepository.getCreditGstBalanceMonth(userId,month,startDate,endDate);

		double overallCreditedAmount = (creditedTotal == null) ? 0.0 : stringToDouble(creditedTotal);
		double overallDebitedAmount = (debitedTotal == null) ? 0.0 : stringToDouble(debitedTotal);
		double overallDebitedGstAmount = (debitGstTotal == null) ? 0.0 : stringToDouble(debitGstTotal);
		double overallCreditGstAmount = (creditGstTotal == null) ? 0.0 : stringToDouble(creditGstTotal);
		double overallConsumedAmount = overallDebitedAmount+overallDebitedGstAmount;
		
		map.put("overallConsumedAmount", overallConsumedAmount);
		map.put("overallCreditedAmount", overallCreditedAmount);
		map.put("overallDebitedAmount", overallDebitedAmount);
		map.put("overallDebitedGstAmount", overallDebitedGstAmount);
		map.put("overallCreditGstAmount", overallCreditGstAmount);
	}

}
