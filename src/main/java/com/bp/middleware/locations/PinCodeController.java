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
@RequestMapping(path = "/pincodeMaster")
@CrossOrigin(origins= {AppConstants.CROSS_ORIGIN})
public class PinCodeController {
	@Autowired
	private PincodeService pincodeservice;

	@GetMapping(path = "/view")
	public ResponseStructure getAllPincode() {
		return pincodeservice.getAllPincode();
	}

	@PostMapping(path = "/add")
	public ResponseStructure addPincode(@RequestBody RequestModel commonRequestDto) {
		return pincodeservice.addPincode(commonRequestDto);
	}

	@GetMapping(path = "/getById/{pincodeId}")
	public ResponseStructure getByPincode(@PathVariable int pincodeId) {
		return pincodeservice.getByPincode(pincodeId);

	}

	@GetMapping(path = "/getPincode/{cityId}")
	public ResponseStructure getPinCodeByCity(@PathVariable("cityId") int cityId) {
		return pincodeservice.getPincode(cityId);
	}

	@PutMapping(path = "/update/{pincodeId}")
	public ResponseStructure updatePinCode(@PathVariable("pincodeId") int pincodeId,
			@RequestBody RequestModel commonRequestDto) {
		return pincodeservice.updatePincode(pincodeId, commonRequestDto);
	}
	@GetMapping("/viewall")
	public ResponseStructure listAll() {
		return pincodeservice.listAll();
	}

}
