package com.bp.middleware.postpaidstatement;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;

@Service
public class PostpaidUserStatementService {

	@Autowired
	private PostpaidUserStatementRepository postpaidUserStatementRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private FileUtils fu;
	@Autowired
	private PostpaidStatementUtils postpaidUtils;

	public void statementEntry(EntityModel entity, RequestModel model) {

		try {

			if (entity.getPaymentMethod().getPaymentType().equalsIgnoreCase("Postpaid")) {

				PostpaidUserStatement statement = new PostpaidUserStatement();

				statement.setCredit(model.getCredit());
				statement.setDebit(model.getDebit());
				statement.setConsumedBalance(entity.getConsumedAmount());
				statement.setCreditGst(model.getCreditGst());
				statement.setEntryDate(new Date());
				statement.setDate(LocalDate.now());
				statement.setDebitGst(model.getDebitGst());
				statement.setRemark(model.getRemark());
				statement.setMonth(LocalDate.now().getMonth().toString());
				statement.setService(model.getService());
				statement.setEntityModel(entity);

				postpaidUserStatementRepository.save(statement);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ResponseStructure viewByEntity(int userId) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<EntityModel> opt = userRepository.findById(userId);

			if (opt.isPresent()) {

				EntityModel entityModel = opt.get();

				List<PostpaidUserStatement> list = postpaidUserStatementRepository.findByEntityModel(entityModel);

				if (!list.isEmpty()) {

					Map<String, Object> map = new LinkedHashMap<>();
					map.put("data", list);

					postpaidUtils.getSummedValues(map, entityModel);
					
					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(map);
					structure.setFlag(1);

				} else {

					structure.setMessage("NO STATEMENT FOUND");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
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

	public ResponseStructure viewByMonth(int userId, String month) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<EntityModel> opt = userRepository.findById(userId);

			if (opt.isPresent()) {

				EntityModel entityModel = opt.get();

				LocalDate startDate = LocalDate.now().withDayOfYear(1);
				LocalDate endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfYear());

				List<PostpaidUserStatement> list = postpaidUserStatementRepository.getUsingCurrentYearMonth(userId, month,
						startDate, endDate);

				if (!list.isEmpty()) {

					Map<String, Object> map = new LinkedHashMap<>();
					map.put("data", list);

					postpaidUtils.getSummedValuesForMonth(map, entityModel,month,startDate,endDate);

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(map);
					structure.setFlag(1);

				} else {

					structure.setMessage("NO STATEMENT FOUND");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
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

	public ResponseStructure viewByRemark(int userId, String remark) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<EntityModel> opt = userRepository.findById(userId);

			if (opt.isPresent()) {

				EntityModel entityModel = opt.get();

				List<PostpaidUserStatement> list = postpaidUserStatementRepository
						.findByEntityModelAndRemark(entityModel, remark);

				if (!list.isEmpty()) {

					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(list);
					structure.setFlag(1);

				} else {

					structure.setMessage("NO STATEMENT FOUND");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
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

	
	
	
	public ResponseStructure filterByDates(RequestModel model) {
		
		ResponseStructure structure = new ResponseStructure();
		
		try {
			
			Optional<EntityModel> opt = userRepository.findById(model.getUserId());
			
			if(opt.isPresent()) {
				
				EntityModel entityModel = opt.get();
				
				List<PostpaidUserStatement>  list = postpaidUserStatementRepository.betweenDates(entityModel.getUserId(),model.getStartDate(),model.getEndDate());
				
				if(!list.isEmpty()) {
					

					Map<String, Object> map = new LinkedHashMap<>();

					map.put("data", list);

					postpaidUtils.getSummedValuesBetweenDates(map, entityModel, model.getStartDate(),
							model.getEndDate());
					
					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(map);
					structure.setFlag(1);
					
				}else {
					
					structure.setMessage("NO STATEMENT FOUND");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}
				
			}else {
				
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
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

	
	
	
	public ResponseStructure byDays(RequestModel model) {
		
		ResponseStructure structure = new ResponseStructure();
		
		try {
			
			Optional<EntityModel> opt = userRepository.findById(model.getUserId());
			
			if(opt.isPresent()) {
				
				EntityModel entityModel = opt.get();
				
				int days = model.getDays()-1;
				
				LocalDate endDate = LocalDate.now();
				LocalDate startDate = LocalDate.now().minusDays(days);
				
				List<PostpaidUserStatement>  list = postpaidUserStatementRepository.betweenDates(entityModel.getUserId(),startDate,endDate);
				
				if(!list.isEmpty()) {

					Map<String, Object> map = new LinkedHashMap<>();

					map.put("data", list);

					postpaidUtils.getSummedValuesBetweenDates(map, entityModel, startDate,endDate);
					
					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(map);
					structure.setFlag(1);
					
				}else {
					
					structure.setMessage("NO STATEMENT FOUND");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
					structure.setFlag(2);
				}
				
			}else {
				
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(3);
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

}
