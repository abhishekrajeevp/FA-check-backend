package com.bp.middleware.requesthitlogs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;

@RestController
@RequestMapping("/hitLogs")
@CrossOrigin
public class HitLogsController {

	
	@Autowired
	private HitLogsService service;
	
	@GetMapping("/allfreeHits")
	public ResponseStructure  allFreeHits() {
		
		return service.allFreeHits();
	}
	
	
	@GetMapping("/allfreeHits-byentity/{userId}")
	public ResponseStructure  allFreeHitsByEntity(@PathVariable("userId")int userId) {
		
		return service.allFreeHitsByEntity(userId);
	}
	
	
	@GetMapping("/allfreeHits-byentityandVerification/{userId}/{verificationId}")
	public ResponseStructure  allFreeHitsByEntityAndVerification(@PathVariable("userId")int userId,@PathVariable("verificationId")int verificationId) {
		
		return service.allFreeHitsByEntityAndVerification(userId,verificationId);
	}
	
	
	
	
}
