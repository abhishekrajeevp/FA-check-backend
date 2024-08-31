package com.bp.middleware.prepaidstatement;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.postpaidstatement.PostpaidUserStatement;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;

@Service
public class PrepaidUserStatementService {

	@Autowired
	private PrepaidUserStatementRepository prepaidUserStatementRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private FileUtils fu;
	@Autowired
	private PrepaidStatementUtils prepaidUtils;

	public void statementEntry(EntityModel entity, RequestModel model) {

		try {

			if (entity.getPaymentMethod().getPaymentType().equalsIgnoreCase("Prepaid")) {

				PrepaidUserStatement statement = new PrepaidUserStatement();

				statement.setCredit(model.getCredit());
				statement.setDebit(model.getDebit());
				statement.setConsumedBalance(entity.getConsumedAmount());
				statement.setClosingBalance(entity.getRemainingAmount());
				statement.setEntryDate(new Date());
				statement.setDate(LocalDate.now());
				statement.setDebitGst(model.getDebitGst());
				statement.setRemark(model.getRemark());
				statement.setService(model.getService());
				statement.setMonth(LocalDate.now().getMonth().toString());
				statement.setEntityModel(entity);

				prepaidUserStatementRepository.save(statement);
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

				List<PrepaidUserStatement> list = prepaidUserStatementRepository.findByEntityModel(entityModel);

				if (!list.isEmpty()) {

					Map<String, Object> map = new LinkedHashMap<>();

					map.put("data", list);
					map.put("closingBalance", fu.twoDecimelDouble(entityModel.getRemainingAmount()));

					prepaidUtils.getSummedValues(map, entityModel);

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

				LocalDate firstDayOfYear = LocalDate.now().withDayOfYear(1);
				LocalDate lastDayOfYear = LocalDate.now().with(TemporalAdjusters.lastDayOfYear());

				List<PrepaidUserStatement> list = prepaidUserStatementRepository.getUsingCurrentYearMonth(userId, month,
						firstDayOfYear, lastDayOfYear);

				if (!list.isEmpty()) {

					Map<String, Object> map = new LinkedHashMap<>();

					map.put("data", list);
					map.put("closingBalance", fu.twoDecimelDouble(entityModel.getRemainingAmount()));

					prepaidUtils.getSummedValuesForMonth(map, entityModel,month,firstDayOfYear,lastDayOfYear);

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

				List<PrepaidUserStatement> list = prepaidUserStatementRepository.findByEntityModelAndRemark(entityModel,
						remark);

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

			if (opt.isPresent()) {

				EntityModel entityModel = opt.get();

				List<PrepaidUserStatement> list = prepaidUserStatementRepository.betweenDates(entityModel.getUserId(),
						model.getStartDate(), model.getEndDate());

				if (!list.isEmpty()) {

					Map<String, Object> map = new LinkedHashMap<>();

					map.put("data", list);
					map.put("closingBalance", fu.twoDecimelDouble(entityModel.getRemainingAmount()));

					prepaidUtils.getSummedValuesBetweenDates(map, entityModel, model.getStartDate(),
							model.getEndDate());

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

	
	
	
	
	public ResponseStructure filterByDays(RequestModel model) {

		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<EntityModel> opt = userRepository.findById(model.getUserId());

			if (opt.isPresent()) {

				EntityModel entityModel = opt.get();
				
				int days = model.getDays()-1;
				
				LocalDate endDate = LocalDate.now();
				LocalDate startDate = LocalDate.now().minusDays(days);

				System.err.println("SD : "+startDate+"   ED :"+endDate );
				
				List<PrepaidUserStatement> list = prepaidUserStatementRepository.betweenDates(entityModel.getUserId(),
						startDate, endDate);

				if (!list.isEmpty()) {

					Map<String, Object> map = new LinkedHashMap<>();

					map.put("data", list);
					map.put("closingBalance", fu.twoDecimelDouble(entityModel.getRemainingAmount()));

					prepaidUtils.getSummedValuesBetweenDates(map, entityModel,startDate,endDate);

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

}
