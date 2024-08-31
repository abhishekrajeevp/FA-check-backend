package com.bp.middleware.bond;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@Service
public class MerchantBondServiceImpl implements MerchantBondService{
	
	@Autowired
	private MerchantBondRepository repository;

	@Override
	public ResponseStructure addBondAmount(RequestModel model) {
		ResponseStructure structure=new ResponseStructure();
		try {
			MerchantBond entity=repository.findByBondAmount(model.getBondAmount());
			if (entity==null) {
				MerchantBond bond=new MerchantBond();
				bond.setBondAmount(model.getBondAmount());
				bond.setCreatedBy(model.getCreatedBy());
				bond.setStatus(model.isStatusFlag());
				bond.setCreatedAt(LocalDate.now());
				repository.save(bond);
				
				structure.setMessage("Bond Amount Added");
				structure.setData(bond);
				structure.setFlag(1);
			} else {
				structure.setMessage("Bond Amount Already Added/ Choose another amount");
				structure.setData(null);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure listAllBondPrice() {
		ResponseStructure structure=new ResponseStructure();
		try {
			List<MerchantBond> list = repository.findAll();
			if (!list.isEmpty()) {
				List<MerchantBond> bond =new ArrayList<>();
				for (MerchantBond merchantBond : list) {
					if (merchantBond.isStatus()) {
						bond.add(merchantBond);
					}
				}
				structure.setMessage("Bond List Details");
				structure.setData(bond);
				structure.setFlag(1);
			} else {
				structure.setMessage("Bond Details Not Found");
				structure.setData(null);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure getBondById(int bondId) {
		ResponseStructure structure=new ResponseStructure();
		try {
			MerchantBond bond = repository.findByBondId(bondId);
			if (bond!=null) {
				structure.setMessage("Bond Details are..");
				structure.setData(bond);
				structure.setFlag(1);
			} else {
				structure.setMessage("Bond Details Not Found");
				structure.setData(null);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure updateStatus(int bondId, RequestModel model) {
		ResponseStructure structure=new ResponseStructure();
		try {
			MerchantBond bond = repository.findByBondId(bondId);
			if (bond!=null) {
				bond.setStatus(model.isStatusFlag());
				repository.save(bond);
				
				structure.setMessage("Bond Status Updated");
				structure.setData(bond);
				structure.setFlag(1);
			} else {
				structure.setMessage("Bond Details Not Found");
				structure.setData(null);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setData(null);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

}
