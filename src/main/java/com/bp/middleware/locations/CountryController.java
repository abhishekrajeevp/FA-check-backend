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
@RequestMapping(path ="/countryMaster")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class CountryController {
	
	@Autowired
	private CountryService countryService;

	@GetMapping(path ="/viewall")
	public ResponseStructure getAllCountry() {
		return countryService.getAllCountry();
	}

	@PostMapping(path = "/add")
	public ResponseStructure addCountry(@RequestBody RequestModel dto) {
		return countryService.addCountry(dto);
	}

	@PutMapping(path = "/update/{countryId}")
	public ResponseStructure updateCountry(@RequestBody RequestModel dto, @PathVariable("countryId") int countryId) {
		return countryService.updateCountry(dto, countryId);
	}

}
