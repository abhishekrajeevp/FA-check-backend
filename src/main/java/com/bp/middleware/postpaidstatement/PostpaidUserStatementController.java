package com.bp.middleware.postpaidstatement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.prepaidstatement.PrepaidUserStatementService;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

@RestController
@RequestMapping("/postpaidstatement")
@CrossOrigin
public class PostpaidUserStatementController {

	
	@Autowired
	private PostpaidUserStatementService service;
	
	
	@GetMapping("/viewByEntity/{userId}")
	public ResponseStructure viewByEntity(@PathVariable("userId") int userId) {
		return service.viewByEntity(userId);
	}
	
	
	@GetMapping("/viewByMonth/{userId}/{month}")
	public ResponseStructure viewByMonth(@PathVariable("userId") int userId,@PathVariable("month") String month) {
		return service.viewByMonth(userId,month);
	}
	
	
	@GetMapping("/viewByRemark/{userId}/{remark}")
	public ResponseStructure viewByRemark(@PathVariable("userId") int userId,@PathVariable("remark") String remark) {
		return service.viewByRemark(userId,remark);
	}
	
	
	@PostMapping("/filterby-date")
	public ResponseStructure filterByDates(@RequestBody RequestModel model) {
		return service.filterByDates(model);
	}
	
	
	@PostMapping("/byDays")
	public ResponseStructure byDays(@RequestBody RequestModel model) {
		return service.byDays(model);
	}
}
