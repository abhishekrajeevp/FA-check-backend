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
@RequestMapping(path = "/cityMaster")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class CityController {
	
	@Autowired
	private CityService cityService;

	@GetMapping(path = "/view")
	public ResponseStructure getAllCity() {
		return cityService.getAllCity();
	}

	@PostMapping(path = "/add")
	public ResponseStructure addCity(@RequestBody RequestModel model) {
		return cityService.addCity(model);
	}

	@GetMapping(path = "/getById/{cityId}")
	public ResponseStructure getByCity(@PathVariable("cityId") int cityId) {
		return cityService.getByCity(cityId);
	}

	@GetMapping(path = "/getCity/{stateId}")
	public ResponseStructure getCity(@PathVariable("stateId") int stateId) {
		return cityService.getCity(stateId);
	}

	@PutMapping(path = "/update/{cityId}")
	public ResponseStructure updateCity(@PathVariable("cityId") int cityId, @RequestBody RequestModel model) {
		return cityService.updateCity(cityId, model);
	}

}
