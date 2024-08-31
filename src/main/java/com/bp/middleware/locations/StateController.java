package com.bp.middleware.locations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.admin.RequestModel;


@RestController
@RequestMapping(path = "/stateMaster")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class StateController {

	@Autowired
	private StateService stateService;

	@PostMapping(path = "/add")
	public ResponseStructure addState(@RequestBody RequestModel dto) {
		return stateService.addState(dto);
	}
	@GetMapping(path = "/view")
	public ResponseStructure getAllState() {
		return stateService.getAllState();
	}
	@GetMapping(path = "/viewById/{stateId}")
	public ResponseStructure getViewById(@PathVariable("stateId") int stateId) {
		return stateService.getViewById(stateId);
	}
	@PutMapping(path = "/update/{stateId}")
	public ResponseStructure updateState(@RequestBody RequestModel dto, @PathVariable("stateId") int stateId) {
		return stateService.updateState(dto, stateId);
	}
	@GetMapping("/getbycountry/{countryId}")
	public ResponseStructure getStateByCountry(@PathVariable("countryId") int countryId) {
		return stateService.getStateByCountry(countryId);
		
	}

}
