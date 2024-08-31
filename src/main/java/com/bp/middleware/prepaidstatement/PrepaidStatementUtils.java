package com.bp.middleware.prepaidstatement;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bp.middleware.user.EntityModel;
import com.bp.middleware.util.FileUtils;

@Component
public class PrepaidStatementUtils {

	
	@Autowired
	private PrepaidUserStatementRepository prepaidUserStatementRepository;
	@Autowired
	private FileUtils fu;
	
	
	
	public void getSummedValues(Map<String, Object> map, EntityModel entityModel) throws Exception{
		
		int userId = entityModel.getUserId();
		
		String creditedTotal = prepaidUserStatementRepository.getCreditedBalance(userId);
		String debitedTotal  = prepaidUserStatementRepository.getDebitedBalance(userId);
		String debitGstTotal = prepaidUserStatementRepository.getDebitGstBalance(userId);
		
		double overallCreditedAmount = (creditedTotal==null)?0.0:stringToDouble(creditedTotal);
		double overallDebitedAmount = (debitedTotal==null)?0.0:stringToDouble(debitedTotal);
		double overallDebitedGstAmount = (debitGstTotal==null)?0.0:stringToDouble(debitGstTotal);
		double overallConsumedAmount = overallDebitedAmount+overallDebitedGstAmount;
		
		map.put("overallConsumedAmount", overallConsumedAmount);
		map.put("overallCreditedAmount", overallCreditedAmount);
		map.put("overallDebitedAmount", overallDebitedAmount);
		map.put("overallDebitedGstAmount", overallDebitedGstAmount);
	}


	public void getSummedValuesBetweenDates(Map<String, Object> map, EntityModel entityModel, LocalDate startDate,
			LocalDate endDate) throws Exception{
		
		int userId = entityModel.getUserId();
		
		String creditedTotal = prepaidUserStatementRepository.getCreditedBalanceBetweenDates(userId,startDate,endDate);
		String debitedTotal  = prepaidUserStatementRepository.getDebitedBalanceBetweenDates(userId,startDate,endDate);
		String debitGstTotal = prepaidUserStatementRepository.getDebitGstBalanceBetweenDates(userId,startDate,endDate);
		
		double overallCreditedAmount = (creditedTotal==null)?0.0:stringToDouble(creditedTotal);
		double overallDebitedAmount = (debitedTotal==null)?0.0:stringToDouble(debitedTotal);
		double overallDebitedGstAmount = (debitGstTotal==null)?0.0:stringToDouble(debitGstTotal);
		double overallConsumedAmount = overallDebitedAmount+overallDebitedGstAmount;
		
		map.put("overallConsumedAmount", overallConsumedAmount);
		map.put("overallCreditedAmount", overallCreditedAmount);
		map.put("overallDebitedAmount", overallDebitedAmount);
		map.put("overallDebitedGstAmount", overallDebitedGstAmount);
		
	}



	public void getSummedValuesForMonth(Map<String, Object> map, EntityModel entityModel, String month,
			LocalDate startDate, LocalDate endDate) throws Exception{

		int userId = entityModel.getUserId();
		
		String creditedTotal = prepaidUserStatementRepository.getCreditedBalanceBetweenMonth(userId,month,startDate,endDate);
		String debitedTotal  = prepaidUserStatementRepository.getDebitedBalanceBetweenMonth(userId,month,startDate,endDate);
		String debitGstTotal = prepaidUserStatementRepository.getDebitGstBalanceBetweenMonth(userId,month,startDate,endDate);
		
		
		double overallCreditedAmount = (creditedTotal==null)?0.0:stringToDouble(creditedTotal);
		double overallDebitedAmount = (debitedTotal==null)?0.0:stringToDouble(debitedTotal);
		double overallDebitedGstAmount = (debitGstTotal==null)?0.0:stringToDouble(debitGstTotal);
		double overallConsumedAmount = overallDebitedAmount+overallDebitedGstAmount;
		
		
		map.put("overallConsumedAmount", overallConsumedAmount);
		map.put("overallCreditedAmount", overallCreditedAmount);
		map.put("overallDebitedAmount", overallDebitedAmount);
		map.put("overallDebitedGstAmount", overallDebitedGstAmount);
		
	}
	
	

	public double stringToDouble(String consumedTotal) throws Exception{
		
		double parseDouble = Double.parseDouble(consumedTotal);
		
		return fu.twoDecimelDouble(parseDouble);
	}
}
