package com.bp.middleware.advanceddashboard;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.merchantapipricesetup.MerchantPriceRepository;
import com.bp.middleware.requestandresponse.Request;
import com.bp.middleware.requestandresponse.RequestRepository;
import com.bp.middleware.requestandresponse.Response;
import com.bp.middleware.requestandresponse.ResponseRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.signers.SignerModel;
import com.bp.middleware.signers.SignerRepository;
import com.bp.middleware.signmerchant.MerchantModel;
import com.bp.middleware.signmerchant.MerchantRepository;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.vendorapipricesetup.VendorPriceRepository;
import com.bp.middleware.vendors.VendorRepository;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.bp.middleware.vendors.VendorVerificationRepository;

@Service
public class DashboardServiceImplementation implements DashboardService {

	@Autowired
	private ResponseRepository respRepository;
	@Autowired
	private RequestRepository reqRepository;
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
	private SignerRepository signerRepository;
	@Autowired
	private MerchantRepository merchantRepository;

	@Override
	public ResponseStructure allMerchantSuccessFailureCount() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<Request> requestList = reqRepository.findAll();

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			int totalCount = 0;
			int successCount = 0;
			int failureCount = 0;
			int totalMonthCount = 0;
			int weeklyCount = 0;

			Map<String, Integer> map = new LinkedHashMap<>();
			
			if (!requestList.isEmpty()) {

				for (Request request : requestList) {
					totalCount++;
					if (request.getStatus().equals("success")) {
						successCount++;
					}
					if (request.getStatus().equals("failed")) {
						failureCount++;
					}

					LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						totalMonthCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after & before) {
						weeklyCount++;
					}
				}

				map.put("totalCount", totalCount);
				map.put("successCount", successCount);
				map.put("failedCount", failureCount);
				map.put("monthlyCount", totalMonthCount);
				map.put("weeklyCount", weeklyCount);

				structure.setData(map);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
			} else {
				
				map.put("totalCount", totalCount);
				map.put("successCount", successCount);
				map.put("failedCount", failureCount);
				map.put("monthlyCount", totalMonthCount);
				map.put("weeklyCount", weeklyCount);
				
				structure.setData(map);
				structure.setMessage("NO VERIFICATION DONE YET.");
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	@Override
	public ResponseStructure allVendorSuccessFailureCount() {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<Response> responseList = respRepository.findAll();

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			int totalCount = 0;
			int successCount = 0;
			int failureCount = 0;
			int totalMonthCount = 0;
			int weeklyCount = 0;
			
			Map<String, Integer> map = new LinkedHashMap<>();

			if (!responseList.isEmpty()) {

				for (Response response : responseList) {
					totalCount++;
					if (response.getStatus().equals("success")) {
						successCount++;
					}
					if (response.getStatus().equals("failed")) {
						failureCount++;
					}

					LocalDate date = LocalDate.ofInstant(response.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						totalMonthCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after & before) {
						weeklyCount++;
					}
				}

				map.put("totalCount", totalCount);
				map.put("successCount", successCount);
				map.put("failedCount", failureCount);
				map.put("monthlyCount", totalMonthCount);
				map.put("weeklyCount", weeklyCount);

				structure.setData(map);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
			} else {
				
				map.put("totalCount", totalCount);
				map.put("successCount", successCount);
				map.put("failedCount", failureCount);
				map.put("monthlyCount", totalMonthCount);
				map.put("weeklyCount", weeklyCount);
				
				structure.setData(map);
				structure.setMessage("NO VERIFICATION DONE YET.");
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;

	}

	@Override
	public ResponseStructure specificMerchantSuccessFailureCount(int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<Request> requestList = reqRepository.findByUser(userId);

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			int totalCount = 0;
			int successCount = 0;
			int failureCount = 0;
			int totalMonthCount = 0;
			int weeklyCount = 0;
			
			Map<String, Integer> map = new LinkedHashMap<>();

			if (!requestList.isEmpty()) {

				for (Request request : requestList) {
					totalCount++;
					if (request.getStatus().equals("success")) {
						successCount++;
					}
					if (request.getStatus().equals("failed")) {
						failureCount++;
					}

					LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						totalMonthCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after & before) {
						weeklyCount++;
					}
				}

				
				map.put("totalCount", totalCount);
				map.put("successCount", successCount);
				map.put("failedCount", failureCount);
				map.put("monthlyCount", totalMonthCount);
				map.put("weeklyCount", weeklyCount);

				structure.setData(map);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {
				
				map.put("totalCount", totalCount);
				map.put("successCount", successCount);
				map.put("failedCount", failureCount);
				map.put("monthlyCount", totalMonthCount);
				map.put("weeklyCount", weeklyCount);
				
				structure.setData(map);
				structure.setMessage("NO VERIFICATION DONE YET.");
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	@Override
	public ResponseStructure specificVendorSuccessFailureCount(int vendorId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			List<Response> responseList = respRepository.findByVendorModel(vendorId);

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			int totalCount = 0;
			int successCount = 0;
			int failureCount = 0;
			int totalMonthCount = 0;
			int weeklyCount = 0;
			
			Map<String, Integer> map = new LinkedHashMap<>();

			if (!responseList.isEmpty()) {

				totalCount = responseList.size();

				for (Response response : responseList) {

					if (response.getStatus().equals("success")) {
						successCount++;
					}
					if (response.getStatus().equals("failed")) {
						failureCount++;
					}

					LocalDate date = LocalDate.ofInstant(response.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						totalMonthCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after && before) {
						weeklyCount++;
					}
				}

				
				map.put("totalCount", totalCount);
				map.put("successCount", successCount);
				map.put("failedCount", failureCount);
				map.put("monthlyCount", totalMonthCount);
				map.put("weeklyCount", weeklyCount);

				structure.setData(map);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
			} else {
				
				map.put("totalCount", totalCount);
				map.put("successCount", successCount);
				map.put("failedCount", failureCount);
				map.put("monthlyCount", totalMonthCount);
				map.put("weeklyCount", weeklyCount);
				
				structure.setData(map);
				structure.setMessage("NO VERIFICATION DONE YET.");
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	@Override
	public ResponseStructure filterByEntityAndVerificationDocument(int userId, int verificationId) {
		ResponseStructure structure = new ResponseStructure();

		EntityModel entity = userRepository.findByUserId(userId);
		VendorVerificationModel verifyDoc = verificationRepository.findByVendorVerificationId(verificationId);

		List<Request> list = reqRepository.findByUserAndVerificationModel(entity, verifyDoc);

		if (!list.isEmpty()) {

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(list);
			structure.setCount(list.size());
			structure.setMessage(AppConstants.SUCCESS);

		} else {

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(2);
			structure.setData(null);
			structure.setCount(list.size());
			structure.setMessage(AppConstants.NO_DATA_FOUND);
		}

		return structure;
	}

	@Override
	public ResponseStructure dashboardForEntityAndVerificationDocument(int userId, int verificationId) {
		ResponseStructure structure = new ResponseStructure();

		EntityModel entity = userRepository.findByUserId(userId);
		VendorVerificationModel verifyDoc = verificationRepository.findByVendorVerificationId(verificationId);

		List<Request> list = reqRepository.findByUserAndVerificationModel(entity, verifyDoc);

		int currentMonth = LocalDate.now().getMonthValue();
		LocalDate weekStartDate = LocalDate.now().minusDays(7);
		
		int totalHit = list.size();
		int successCount = 0;
		int failureCount = 0;
		int totalMonthCount = 0;
		int weeklyCount = 0;

		Map<String, Integer> map = new LinkedHashMap<>();
		
		if (!list.isEmpty()) {

			for (Request response : list) {

				if (response.getStatus().equals("success")) {
					successCount++;
				}
				if (response.getStatus().equals("failed")) {
					failureCount++;
				}

				LocalDate date = LocalDate.ofInstant(response.getRequestDateAndTime().toInstant(),
						ZoneId.systemDefault());
				if (date.getMonthValue() == currentMonth) {
					totalMonthCount++;
				}

				boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
				boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

				if (after && before) {
					weeklyCount++;
				}
			}

			map.put("totalCount", totalHit);
			map.put("successCount", successCount);
			map.put("failedCount", failureCount);
			map.put("monthlyCount", totalMonthCount);
			map.put("weeklyCount", weeklyCount);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(1);
			structure.setData(map);
			structure.setCount(list.size());
			structure.setMessage(AppConstants.SUCCESS);

		} else {

			map.put("totalCount", totalHit);
			map.put("successCount", successCount);
			map.put("failedCount", failureCount);
			map.put("monthlyCount", totalMonthCount);
			map.put("weeklyCount", weeklyCount);
			
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setFlag(2);
			structure.setData(null);
			structure.setCount(list.size());
			structure.setMessage(AppConstants.NO_DATA_FOUND);
		}

		return structure;
	}

	@Override
	public ResponseStructure merchantDetailsByDays(int numberOfDays) {

		ResponseStructure structure = new ResponseStructure();

		try {

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			String toDate = LocalDate.now().toString() + " 23:59:59.999999";
			String fromDate = LocalDate.now().minusDays(numberOfDays).toString() + " 00:00:00.000000";

			List<Request> reqList = reqRepository.getByRequestDateAndTime(fromDate, toDate);

			long totalCount = reqList.size();
			
			Map<String, Long> dashboard = new LinkedHashMap<>();
			
			long successCount = 0;
			long failedCount = 0;
			long monthlyCount = 0;
			long weeklyCount = 0;

			if (!reqList.isEmpty()) {

				for (Request request : reqList) {

					if (request.getStatus().equalsIgnoreCase("success")) {
						successCount++;
					} else {
						failedCount++;
					}

					LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						monthlyCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after && before) {
						weeklyCount++;
					}
				}

				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Merchant Request details Between " + LocalDate.now().minusDays(numberOfDays)
						+ " and " + LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(1);
				structure.setCount(totalCount);

			} else {
				
				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Merchant Request found Between " + LocalDate.now().minusDays(numberOfDays)
						+ " and " + LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(2);
				structure.setCount(totalCount);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure merchantDetailsForLastSevenDays() {

		ResponseStructure structure = new ResponseStructure();

		try {

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			String toDate = LocalDate.now().toString() + " 23:59:59.999999";
			String fromDate = LocalDate.now().minusDays(7).toString() + " 00:00:00.000000";

			List<Request> reqList = reqRepository.getByRequestDateAndTime(fromDate, toDate);

			long totalCount = reqList.size();
			
			Map<String, Long> dashboard = new LinkedHashMap<>();

			long successCount = 0;
			long failedCount = 0;
			long monthlyCount = 0;
			long weeklyCount = 0;
			
			if (!reqList.isEmpty()) {

				for (Request request : reqList) {

					if (request.getStatus().equalsIgnoreCase("success")) {
						successCount++;
					} else {
						failedCount++;
					}

					LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						monthlyCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after && before) {
						weeklyCount++;
					}
				}

				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(
						"Merchant Request details Between " + LocalDate.now().minusDays(7) + " and " + LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(1);
				structure.setCount(totalCount);

			} else {
				
				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Merchant Request found Between " + LocalDate.now().minusDays(7) + " and "
						+ LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(2);
				structure.setCount(totalCount);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure merchantDetailsForLastFifteenDays() {

		ResponseStructure structure = new ResponseStructure();

		try {

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			String toDate = LocalDate.now().toString() + " 23:59:59.999999";
			String fromDate = LocalDate.now().minusDays(15).toString() + " 00:00:00.000000";

			List<Request> reqList = reqRepository.getByRequestDateAndTime(fromDate, toDate);

			long totalCount = reqList.size();
			
			Map<String, Long> dashboard = new LinkedHashMap<>();

			long successCount = 0;
			long failedCount = 0;
			long monthlyCount = 0;
			long weeklyCount = 0;
			
			if (!reqList.isEmpty()) {


				for (Request request : reqList) {

					if (request.getStatus().equalsIgnoreCase("success")) {
						successCount++;
					} else {
						failedCount++;
					}

					LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						monthlyCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after && before) {
						weeklyCount++;
					}
				}

				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Merchant Request details Between " + LocalDate.now().minusDays(15) + " and "
						+ LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(1);
				structure.setCount(totalCount);

			} else {
				
				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Merchant Request found Between " + LocalDate.now().minusDays(15) + " and "
						+ LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(2);
				structure.setCount(totalCount);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure merchantDetailsForLastThirtyDays() {
		ResponseStructure structure = new ResponseStructure();

		try {

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			String toDate = LocalDate.now().toString() + " 23:59:59.999999";
			String fromDate = LocalDate.now().minusDays(30).toString() + " 00:00:00.000000";

			List<Request> reqList = reqRepository.getByRequestDateAndTime(fromDate, toDate);

			long totalCount = reqList.size();
			
			long successCount = 0;
			long failedCount = 0;
			long monthlyCount = 0;
			long weeklyCount = 0;
			
			Map<String, Long> dashboard = new LinkedHashMap<>();

			if (!reqList.isEmpty()) {


				for (Request request : reqList) {

					if (request.getStatus().equalsIgnoreCase("success")) {
						successCount++;
					} else {
						failedCount++;
					}

					LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						monthlyCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after && before) {
						weeklyCount++;
					}
				}

				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Merchant Request details Between " + LocalDate.now().minusDays(30) + " and "
						+ LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(1);
				structure.setCount(totalCount);

			} else {
				
				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Merchant Request found Between " + LocalDate.now().minusDays(30) + " and "
						+ LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(2);
				structure.setCount(totalCount);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure merchantDetailsBetweenTwoDates(String startDate, String endDate) {
		ResponseStructure structure = new ResponseStructure();

		try {

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			String fromDate = startDate + " 00:00:00.000000";
			String toDate = endDate + " 23:59:59.999999";

			List<Request> reqList = reqRepository.getByRequestDateAndTime(fromDate, toDate);

			long totalCount = reqList.size();
			
			long successCount = 0;
			long failedCount = 0;
			long monthlyCount = 0;
			long weeklyCount = 0;
			
			Map<String, Long> dashboard = new LinkedHashMap<>();

			if (!reqList.isEmpty()) {


				for (Request request : reqList) {

					if (request.getStatus().equalsIgnoreCase("success")) {
						successCount++;
					} else {
						failedCount++;
					}

					LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						monthlyCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after && before) {
						weeklyCount++;
					}
				}


				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Merchant Request details Between " + startDate + " and " + endDate);
				structure.setData(dashboard);
				structure.setFlag(1);
				structure.setCount(totalCount);

			} else {
				
				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Merchant Request found Between " + startDate + " and " + endDate);
				structure.setData(dashboard);
				structure.setFlag(2);
				structure.setCount(totalCount);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure vendorDetailsByDays(int numberOfDays) {
		ResponseStructure structure = new ResponseStructure();

		try {

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			String toDate = LocalDate.now().toString() + " 23:59:59.999999";
			String fromDate = LocalDate.now().minusDays(numberOfDays).toString() + " 00:00:00.000000";

			List<Response> reqList = respRepository.getByRequestDateAndTime(fromDate, toDate);

			long totalCount = reqList.size();
			
			long successCount = 0;
			long failedCount = 0;
			long monthlyCount = 0;
			long weeklyCount = 0;

			Map<String, Long> dashboard = new LinkedHashMap<>();
			
			if (!reqList.isEmpty()) {

				for (Response request : reqList) {

					if (request.getStatus().equalsIgnoreCase("success")) {
						successCount++;
					} else {
						failedCount++;
					}

					LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						monthlyCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after && before) {
						weeklyCount++;
					}
				}

				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Vendor Request details Between " + LocalDate.now().minusDays(numberOfDays)
						+ " and " + LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(1);
				structure.setCount(totalCount);

			} else {
				
				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Vendor Request found Between " + LocalDate.now().minusDays(numberOfDays)
						+ " and " + LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(2);
				structure.setCount(totalCount);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure vendorDetailsForLastSevenDays() {
		ResponseStructure structure = new ResponseStructure();

		try {

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			String toDate = LocalDate.now().toString() + " 23:59:59.999999";
			String fromDate = LocalDate.now().minusDays(7).toString() + " 00:00:00.000000";

			List<Response> reqList = respRepository.getByRequestDateAndTime(fromDate, toDate);

			long totalCount = reqList.size();
			long successCount = 0;
			long failedCount = 0;
			long monthlyCount = 0;
			long weeklyCount = 0;
			
			Map<String, Long> dashboard = new LinkedHashMap<>();

			if (!reqList.isEmpty()) {

				for (Response request : reqList) {

					if (request.getStatus().equalsIgnoreCase("success")) {
						successCount++;
					} else {
						failedCount++;
					}

					LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						monthlyCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after && before) {
						weeklyCount++;
					}
				}

				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(
						"Vendor Request details Between " + LocalDate.now().minusDays(7) + " and " + LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(1);
				structure.setCount(totalCount);

			} else {
				
				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(
						"No Vendor Request found Between " + LocalDate.now().minusDays(7) + " and " + LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(2);
				structure.setCount(totalCount);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure vendorDetailsForLastFifteenDays() {
		ResponseStructure structure = new ResponseStructure();

		try {

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			String toDate = LocalDate.now().toString() + " 23:59:59.999999";
			String fromDate = LocalDate.now().minusDays(15).toString() + " 00:00:00.000000";

			List<Response> reqList = respRepository.getByRequestDateAndTime(fromDate, toDate);

			long totalCount = reqList.size();
			
			long successCount = 0;
			long failedCount = 0;
			long monthlyCount = 0;
			long weeklyCount = 0;
			
			Map<String, Long> dashboard = new LinkedHashMap<>();

			if (!reqList.isEmpty()) {

				for (Response request : reqList) {

					if (request.getStatus().equalsIgnoreCase("success")) {
						successCount++;
					} else {
						failedCount++;
					}

					LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						monthlyCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after && before) {
						weeklyCount++;
					}
				}

				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(
						"Vendor Request details Between " + LocalDate.now().minusDays(15) + " and " + LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(1);
				structure.setCount(totalCount);

			} else {
				
				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(
						"No Vendor Request found Between " + LocalDate.now().minusDays(15) + " and " + LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(2);
				structure.setCount(totalCount);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure vendorDetailsForLastThirtyDays() {
		ResponseStructure structure = new ResponseStructure();

		try {

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			String toDate = LocalDate.now().toString() + " 23:59:59.999999";
			String fromDate = LocalDate.now().minusDays(30).toString() + " 00:00:00.000000";

			List<Response> reqList = respRepository.getByRequestDateAndTime(fromDate, toDate);

			long totalCount = reqList.size();
			
			long successCount = 0;
			long failedCount = 0;
			long monthlyCount = 0;
			long weeklyCount = 0;
			
			Map<String, Long> dashboard = new LinkedHashMap<>();

			if (!reqList.isEmpty()) {

				for (Response request : reqList) {

					if (request.getStatus().equalsIgnoreCase("success")) {
						successCount++;
					} else {
						failedCount++;
					}

					LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						monthlyCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after && before) {
						weeklyCount++;
					}
				}

				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(
						"Vendor Request details Between " + LocalDate.now().minusDays(30) + " and " + LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(1);
				structure.setCount(totalCount);

			} else {
				
				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(
						"No Vendor Request found Between " + LocalDate.now().minusDays(30) + " and " + LocalDate.now());
				structure.setData(dashboard);
				structure.setFlag(2);
				structure.setCount(totalCount);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure vendorDetailsBetweenTwoDates(String startDate, String endDate) {
		ResponseStructure structure = new ResponseStructure();

		try {

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			String fromDate = startDate + " 00:00:00.000000";
			String toDate = endDate + " 23:59:59.999999";

			List<Response> reqList = respRepository.getByRequestDateAndTime(fromDate, toDate);

			long totalCount = reqList.size();
			long successCount = 0;
			long failedCount = 0;
			long monthlyCount = 0;
			long weeklyCount = 0;

			Map<String, Long> dashboard = new LinkedHashMap<>();
			
			if (!reqList.isEmpty()) {

				for (Response request : reqList) {

					if (request.getStatus().equalsIgnoreCase("success")) {
						successCount++;
					} else {
						failedCount++;
					}

					LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
							ZoneId.systemDefault());
					if (date.getMonthValue() == currentMonth) {
						monthlyCount++;
					}

					boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
					boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

					if (after && before) {
						weeklyCount++;
					}
				}

				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Vendor Request details Between " + startDate + " and " + endDate);
				structure.setData(dashboard);
				structure.setFlag(1);
				structure.setCount(totalCount);

			} else {
				
				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Vendor Request found Between " + startDate + " and " + endDate);
				structure.setData(dashboard);
				structure.setFlag(2);
				structure.setCount(totalCount);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	// SIGNERS DASHBOARD

	@Override
	public ResponseStructure overAllSigners() {
		ResponseStructure structure = new ResponseStructure();

		try {

			List<SignerModel> wholeSigners = signerRepository.findAll();

			long totalSigners = wholeSigners.size();
			long signed = 0;
			long notSigned = 0;
			
			Map<String, Long> dashboard = new LinkedHashMap<>();

			if (!wholeSigners.isEmpty()) {

				for (SignerModel signerModel : wholeSigners) {

					if (signerModel.isOtpVerificationStatus()) {

						signed++;

					} else {
						notSigned++;
					}
				}


				dashboard.put("total_signers", totalSigners);
				dashboard.put("signed", signed);
				dashboard.put("not_signed", notSigned);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Total signers details.");
				structure.setData(dashboard);
				structure.setFlag(1);
				structure.setCount(totalSigners);

			} else {
				
				dashboard.put("total_signers", totalSigners);
				dashboard.put("signed", signed);
				dashboard.put("not_signed", notSigned);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("No Signers Present");
				structure.setData(dashboard);
				structure.setFlag(2);
				structure.setCount(totalSigners);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure signersByEntity(int userId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel entity = userRepository.findByUserId(userId);

			List<SignerModel> wholeSigners = signerRepository.findByEntityModel(entity);

			long totalSigners = wholeSigners.size();
			long signed = 0;
			long notSigned = 0;
			
			Map<String, Long> dashboard = new LinkedHashMap<>();

			if (entity != null) {

				if (!wholeSigners.isEmpty()) {

					for (SignerModel signerModel : wholeSigners) {

						if (signerModel.isOtpVerificationStatus()) {

							signed++;

						} else {
							notSigned++;
						}
					}


					dashboard.put("total_signers", totalSigners);
					dashboard.put("signed", signed);
					dashboard.put("not_signed", notSigned);

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("Total signers details for the given entity");
					structure.setData(dashboard);
					structure.setFlag(1);
					structure.setCount(totalSigners);

				} else {
					
					dashboard.put("total_signers", totalSigners);
					dashboard.put("signed", signed);
					dashboard.put("not_signed", notSigned);
					
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("No Signers Present");
					structure.setData(dashboard);
					structure.setFlag(2);
					structure.setCount(totalSigners);
				}

			} else {
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Thee given entity is not found");
				structure.setData(null);
				structure.setFlag(3);
				structure.setCount(totalSigners);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	@Override
	public ResponseStructure merchantDetailsBetweenTwoDatesAsSingleDate(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			List<String> dates = model.getDatePeriod();

			System.err.println("Date Period : " + model.getDatePeriod());
			Map<String, Long> dashboard = new LinkedHashMap<>();
			
			long totalCount = 0;
			long successCount = 0;
			long failedCount = 0;
			long monthlyCount = 0;
			long weeklyCount = 0;
			

			if (dates.size() > 1) {

				System.err.println("DS 1 : " + dates.get(0));
				System.err.println("DS 2 : " + dates.get(1));

				String startDate = dateStructureConverter(dates.get(0));
				String endDate = dateStructureConverter(dates.get(1));

				String fromDate = startDate + " 00:00:00.000000";
				String toDate = endDate + " 23:59:59.999999";

				List<Request> reqList = reqRepository.getByRequestDateAndTime(fromDate, toDate);

				 totalCount = reqList.size();

				if (!reqList.isEmpty()) {

					for (Request request : reqList) {

						if (request.getStatus().equalsIgnoreCase("success")) {
							successCount++;
						} else {
							failedCount++;
						}

						LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
								ZoneId.systemDefault());
						if (date.getMonthValue() == currentMonth) {
							monthlyCount++;
						}

						boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
						boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

						if (after && before) {
							weeklyCount++;
						}
					}

					dashboard.put("totalCount", totalCount);
					dashboard.put("successCount", successCount);
					dashboard.put("failedCount", failedCount);
					dashboard.put("monthlyCount", monthlyCount);
					dashboard.put("weeklyCount", weeklyCount);

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("Merchant Request details Between " + startDate + " and " + endDate);
					structure.setData(dashboard);
					structure.setFlag(1);
					structure.setCount(totalCount);

				} else {
					
					dashboard.put("totalCount", totalCount);
					dashboard.put("successCount", successCount);
					dashboard.put("failedCount", failedCount);
					dashboard.put("monthlyCount", monthlyCount);
					dashboard.put("weeklyCount", weeklyCount);
					
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("No Merchant Request found Between " + startDate + " and " + endDate);
					structure.setData(dashboard);
					structure.setFlag(2);
					structure.setCount(totalCount);
				}

			} else {

				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Please choose both the Dates");
				structure.setData(dashboard);
				structure.setFlag(3);
			}

		} catch (Exception e) {
			e.printStackTrace();
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	@Override
	public ResponseStructure vendorDetailsBetweenTwoDatesAsSingleDate(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			int currentMonth = LocalDate.now().getMonthValue();
			LocalDate weekStartDate = LocalDate.now().minusDays(7);

			List<String> dates = model.getDatePeriod();

			System.err.println("Date Period : " + model.getDatePeriod());
			
			Map<String, Long> dashboard = new LinkedHashMap<>();
			
			long totalCount = 0; 
			long successCount = 0;
			long failedCount = 0;
			long monthlyCount = 0;
			long weeklyCount = 0;

			if (dates.size() > 1) {

				System.err.println("DS 1 : " + dates.get(0));
				System.err.println("DS 2 : " + dates.get(1));

				String startDate = dateStructureConverter(dates.get(0));
				String endDate = dateStructureConverter(dates.get(1));

				String fromDate = startDate + " 00:00:00.000000";
				String toDate = endDate + " 23:59:59.999999";

				List<Response> reqList = respRepository.getByRequestDateAndTime(fromDate, toDate);

			    totalCount = reqList.size();

				if (!reqList.isEmpty()) {

					for (Response request : reqList) {

						if (request.getStatus().equalsIgnoreCase("success")) {
							successCount++;
						} else {
							failedCount++;
						}

						LocalDate date = LocalDate.ofInstant(request.getRequestDateAndTime().toInstant(),
								ZoneId.systemDefault());
						if (date.getMonthValue() == currentMonth) {
							monthlyCount++;
						}

						boolean after = date.isAfter(weekStartDate) || date.equals(weekStartDate);
						boolean before = date.isBefore(LocalDate.now()) || date.equals(LocalDate.now());

						if (after && before) {
							weeklyCount++;
						}
					}

					dashboard.put("totalCount", totalCount);
					dashboard.put("successCount", successCount);
					dashboard.put("failedCount", failedCount);
					dashboard.put("monthlyCount", monthlyCount);
					dashboard.put("weeklyCount", weeklyCount);

					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("Vendor Request details Between " + startDate + " and " + endDate);
					structure.setData(dashboard);
					structure.setFlag(1);
					structure.setCount(totalCount);

				} else {
					
					dashboard.put("totalCount", totalCount);
					dashboard.put("successCount", successCount);
					dashboard.put("failedCount", failedCount);
					dashboard.put("monthlyCount", monthlyCount);
					dashboard.put("weeklyCount", weeklyCount);
					
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setMessage("No Vendor Request found Between " + startDate + " and " + endDate);
					structure.setData(dashboard);
					structure.setFlag(2);
					structure.setCount(totalCount);
				}
			} else {

				dashboard.put("totalCount", totalCount);
				dashboard.put("successCount", successCount);
				dashboard.put("failedCount", failedCount);
				dashboard.put("monthlyCount", monthlyCount);
				dashboard.put("weeklyCount", weeklyCount);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage("Please choose both the Dates");
				structure.setData(dashboard);
				structure.setFlag(3);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(4);
		}

		return structure;
	}

	private String dateStructureConverter(String date) throws Exception {

		DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		LocalDate localDate = LocalDate.parse(date, inputFormat);

		DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		String formattedDate = localDate.format(outputFormat);

		System.err.println("D-----> " + formattedDate);

		return formattedDate;
	}

	@Override
	public ResponseStructure overAllMerchantAgreement() {

		ResponseStructure structure = new ResponseStructure();

		try {

			List<MerchantModel> totalMerchantDocs = merchantRepository.findAll();

			long totalDocs = totalMerchantDocs.size();
			long docWithBond = merchantRepository.getByBondPresent();
			long docsWithOutBond = totalDocs - docWithBond;

			long noSignerAdded = 0;
			long fullySigned = 0;
			long partiallySigned = 0;
			long notYetSigned = 0;
			
			Map<String, Long> dashboard = new LinkedHashMap<>();

			for (MerchantModel merchantModel : totalMerchantDocs) {

				List<SignerModel> allSigner = signerRepository.findByMerchantModel(merchantModel);
				List<SignerModel> signed = signerRepository.findByMerchantModelAndOtpVerificationStatus(merchantModel,
						true);
				List<SignerModel> notSigned = signerRepository
						.findByMerchantModelAndOtpVerificationStatus(merchantModel, false);

				long totalSigner = allSigner.size();
				long signedSigner = signed.size();
				long notSignedSigner = notSigned.size();

				if (totalSigner == 0) {

					noSignerAdded++;
					
				} else if (totalSigner == signedSigner) {

					fullySigned++;
				} else if (totalSigner == notSignedSigner) {

					notYetSigned++;
				} else {

					partiallySigned++;
				}
			}

			dashboard.put("total_documents", totalDocs);
			dashboard.put("with_bond", docWithBond);
			dashboard.put("without_bond", docsWithOutBond);
			dashboard.put("fully_signed", fullySigned);
			dashboard.put("partially_signed", partiallySigned);
			dashboard.put("not_yet_signed", notYetSigned);
			dashboard.put("no_signer_added",noSignerAdded);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(dashboard);
			structure.setFlag(1);
			structure.setCount(totalDocs);

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure merchantAgreementByEntity(int userId) {
		ResponseStructure structure = new ResponseStructure();

		try {

			EntityModel user = userRepository.findByUserId(userId);
			
			long fullySigned = 0;
			long partiallySigned = 0;
			long notYetSigned = 0;
			long noSignerAdded = 0;
			long docWithBond = 0;
			long totalDocs = 0;
			long docsWithOutBond = 0;
			
			Map<String, Long> dashboard = new LinkedHashMap<>();

			if (user != null) {

				List<MerchantModel> totalMerchantDocs = merchantRepository.findByEntity(user);

				totalDocs = totalMerchantDocs.size();
				
				docWithBond = merchantRepository.getByBondPresentAndEntity(userId);
			    docsWithOutBond = totalDocs - docWithBond;
				

				for (MerchantModel merchantModel : totalMerchantDocs) {

					List<SignerModel> allSigner = signerRepository.findByMerchantModel(merchantModel);
					List<SignerModel> signed = signerRepository
							.findByMerchantModelAndOtpVerificationStatus(merchantModel, true);
					List<SignerModel> notSigned = signerRepository
							.findByMerchantModelAndOtpVerificationStatus(merchantModel, false);

					long totalSigner = allSigner.size();
					long signedSigner = signed.size();
					long notSignedSigner = notSigned.size();

					if (totalSigner == 0) {

						noSignerAdded++;
						
					} else if (totalSigner == signedSigner) {

						fullySigned++;
					} else if (totalSigner == notSignedSigner) {

						notYetSigned++;
					} else {

						partiallySigned++;
					}
				}

				dashboard.put("total_documents", totalDocs);
				dashboard.put("with_bond", docWithBond);
				dashboard.put("without_bond", docsWithOutBond);
				dashboard.put("fully_signed", fullySigned);
				dashboard.put("partially_signed", partiallySigned);
				dashboard.put("not_yet_signed", notYetSigned);
				dashboard.put("no_signer_added",noSignerAdded);

				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.SUCCESS);
				structure.setData(dashboard);
				structure.setFlag(1);
				structure.setCount(totalDocs);

			} else {

				dashboard.put("total_documents", totalDocs);
				dashboard.put("with_bond", docWithBond);
				dashboard.put("without_bond", docsWithOutBond);
				dashboard.put("fully_signed", fullySigned);
				dashboard.put("partially_signed", partiallySigned);
				dashboard.put("not_yet_signed", notYetSigned);
				dashboard.put("no_signer_added",noSignerAdded);
				
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setData(dashboard);
				structure.setFlag(2);
				structure.setCount(0);
			}

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;

	}

	@Override
	public ResponseStructure overAllMerchantAgreementFilterBetweenDates(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate = model.getStartDate() + " 00:00:00.0000000";
			String endDate = model.getEndDate() + " 23:59:59.9999999";

			List<MerchantModel> totalMerchantDocs = merchantRepository.getBetweencreatedAt(startDate, endDate);

			long totalDocs = totalMerchantDocs.size();
			
			long docWithBond = merchantRepository.getByBetweencreatedAtAndBondPresent(startDate, endDate);
			
			long docsWithOutBond = totalDocs - docWithBond;

			long fullySigned = 0;
			long partiallySigned = 0;
			long notYetSigned = 0;
			long noSignerAdded = 0;

			for (MerchantModel merchantModel : totalMerchantDocs) {

				List<SignerModel> allSigner = signerRepository.findByMerchantModel(merchantModel);
				List<SignerModel> signed = signerRepository
						.findByMerchantModelAndOtpVerificationStatus(merchantModel, true);
				List<SignerModel> notSigned = signerRepository
						.findByMerchantModelAndOtpVerificationStatus(merchantModel, false);

				long totalSigner = allSigner.size();
				long signedSigner = signed.size();
				long notSignedSigner = notSigned.size();

				if (totalSigner == 0) {

					noSignerAdded++;
					
				} else if (totalSigner == signedSigner) {

					fullySigned++;
				} else if (totalSigner == notSignedSigner) {

					notYetSigned++;
				} else {

					partiallySigned++;
				}
			}

			Map<String, Long> dashboard = new LinkedHashMap<>();

			dashboard.put("total_documents", totalDocs);
			dashboard.put("with_bond", docWithBond);
			dashboard.put("without_bond", docsWithOutBond);
			dashboard.put("fully_signed", fullySigned);
			dashboard.put("partially_signed", partiallySigned);
			dashboard.put("not_yet_signed", notYetSigned);
			dashboard.put("no_signer_added",noSignerAdded);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(dashboard);
			structure.setFlag(1);
			structure.setCount(totalDocs);

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

	@Override
	public ResponseStructure merchantAgreementByEntityFilterBetweenDates(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			String startDate = model.getStartDate() + " 00:00:00.0000000";
			String endDate = model.getEndDate() + " 23:59:59.9999999";

			List<MerchantModel> totalMerchantDocs = merchantRepository.getBetweencreatedAtAndEntityId(startDate, endDate,model.getUserId());

			long totalDocs = totalMerchantDocs.size();
			long docWithBond = merchantRepository.getByBetweencreatedAtAndBondPresentAndEntity(startDate, endDate,model.getUserId());
			
			System.err.println(""+totalDocs+"  "+docWithBond);
			long docsWithOutBond = totalDocs - docWithBond;

			long fullySigned = 0;
			long partiallySigned = 0;
			long notYetSigned = 0;
			long noSignerAdded = 0;

			for (MerchantModel merchantModel : totalMerchantDocs) {

				List<SignerModel> allSigner = signerRepository.findByMerchantModel(merchantModel);
				List<SignerModel> signed = signerRepository
						.findByMerchantModelAndOtpVerificationStatus(merchantModel, true);
				List<SignerModel> notSigned = signerRepository
						.findByMerchantModelAndOtpVerificationStatus(merchantModel, false);

				long totalSigner = allSigner.size();
				long signedSigner = signed.size();
				long notSignedSigner = notSigned.size();

				if (totalSigner == 0) {

					noSignerAdded++;
					
				} else if (totalSigner == signedSigner) {

					fullySigned++;
				} else if (totalSigner == notSignedSigner) {

					notYetSigned++;
				} else {

					partiallySigned++;
				}
			}

			Map<String, Long> dashboard = new LinkedHashMap<>();

			dashboard.put("total_documents", totalDocs);
			dashboard.put("with_bond", docWithBond);
			dashboard.put("without_bond", docsWithOutBond);
			dashboard.put("fully_signed", fullySigned);
			dashboard.put("partially_signed", partiallySigned);
			dashboard.put("not_yet_signed", notYetSigned);
			dashboard.put("no_signer_added",noSignerAdded);

			structure.setStatusCode(HttpStatus.OK.value());
			structure.setMessage(AppConstants.SUCCESS);
			structure.setData(dashboard);
			structure.setFlag(1);
			structure.setCount(totalDocs);

		} catch (Exception e) {
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
		}

		return structure;
	}

}
