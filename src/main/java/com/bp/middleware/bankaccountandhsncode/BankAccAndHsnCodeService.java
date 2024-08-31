package com.bp.middleware.bankaccountandhsncode;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@Service
public class BankAccAndHsnCodeService {

	@Autowired
	private AdminBankAccRepository adminBankAccRepository;
	@Autowired
	private HsnCodeRepository hsnCodeRepository;

	public ResponseStructure addBankDetails(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			AdminBankAccount alreadyPresent = adminBankAccRepository.findByAccountNumber(model.getAccountNumber());

			if (alreadyPresent == null) {

				AdminBankAccount bank = new AdminBankAccount();

				bank.setAccountNumber(model.getAccountNumber());
				bank.setBankName(model.getBankName());
				bank.setIfscCode(model.getIfscCode());
				bank.setStatus(false);

				adminBankAccRepository.save(bank);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(bank);
				structure.setFlag(1);

			} else {

				structure.setMessage("Account Number already present");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure addHsn(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			HsnCode alreadyPresent = hsnCodeRepository.findByHsnNumber(model.getHsnNumber());

			if (alreadyPresent == null) {

				HsnCode hsn = new HsnCode();

				hsn.setHsnNumber(model.getHsnNumber());
				hsn.setStatus(false);

				hsnCodeRepository.save(hsn);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(hsn);
				structure.setFlag(1);

			} else {

				structure.setMessage("Hsn Number already present");
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure viewAllBankAcc() {

		ResponseStructure structure = new ResponseStructure();

		try {

			List<AdminBankAccount> alreadyPresent = adminBankAccRepository.findAll();

			if (!alreadyPresent.isEmpty()) {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(alreadyPresent);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}

		return structure;

	}

	public ResponseStructure viewAllHsn() {

		ResponseStructure structure = new ResponseStructure();

		try {

			List<HsnCode> alreadyPresent = hsnCodeRepository.findAll();

			if (!alreadyPresent.isEmpty()) {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(alreadyPresent);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure updateHsn(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			HsnCode alreadyPresent = hsnCodeRepository.findByHsnNumber(model.getHsnNumber());
			Optional<HsnCode> opt = hsnCodeRepository.findById(model.getHsnId());

			if (opt.isPresent() && alreadyPresent == null) {

				HsnCode hsnCode = opt.get();

				hsnCode.setHsnNumber(model.getHsnNumber());

				hsnCodeRepository.save(hsnCode);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(hsnCode);
				structure.setFlag(1);

			} else {

				if (opt.isEmpty()) {
					structure.setMessage(AppConstants.NO_DATA_FOUND);
				}else {
					structure.setMessage("Hsn Number already present");
				}
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure updateBankAcc(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			// AdminBankAccount alreadyPresent =
			// adminBankAccRepository.findByAccountNumber(model.getAccountNumber());
			Optional<AdminBankAccount> opt = adminBankAccRepository.findById(model.getBankAccId());

			if (opt.isPresent()) {

				AdminBankAccount bank = opt.get();

				bank.setAccountNumber(model.getAccountNumber());
				bank.setBankName(model.getBankName());
				bank.setIfscCode(model.getIfscCode());

				adminBankAccRepository.save(bank);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(bank);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}

		return structure;
	}

	public ResponseStructure viewByActiveBankAcc() {

		ResponseStructure structure = new ResponseStructure();

		try {

			AdminBankAccount alreadyPresent = adminBankAccRepository.findByStatus(true);

			if (alreadyPresent!=null) {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(alreadyPresent);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}

		return structure;

	}

	
	public ResponseStructure viewByActiveHsn() {

		ResponseStructure structure = new ResponseStructure();

		try {

			HsnCode alreadyPresent = hsnCodeRepository.findByStatus(true);

			if (alreadyPresent!=null) {

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(alreadyPresent);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}

		return structure;
	}

	
	
	public ResponseStructure updateStatusHsn(RequestModel model) {
		
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<HsnCode> opt = hsnCodeRepository.findById(model.getHsnId());

			if (opt.isPresent()) {

				HsnCode hsn = opt.get();
				List<HsnCode> alreadyPresent = hsnCodeRepository.findAll();
				
				for (HsnCode hsnCode : alreadyPresent) {
					hsnCode.setStatus(false);
					hsnCodeRepository.save(hsnCode);
				}
				
				hsn.setStatus(true);

				hsnCodeRepository.save(hsn);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(hsn);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}

		return structure;
	}

	
	
	
	public ResponseStructure updateStatusBankAcc(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();

		try {

			Optional<AdminBankAccount> opt = adminBankAccRepository.findById(model.getBankAccId());

			if (opt.isPresent()) {

				AdminBankAccount bank = opt.get();

				List<AdminBankAccount> alreadyPresent = adminBankAccRepository.findAll();
				
				for (AdminBankAccount adminBankAccount : alreadyPresent) {
					
					adminBankAccount.setStatus(false);
					
					adminBankAccRepository.save(adminBankAccount);
				}
				
				bank.setStatus(true);

				adminBankAccRepository.save(bank);

				structure.setMessage(AppConstants.SUCCESS);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(bank);
				structure.setFlag(1);

			} else {

				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
				structure.setFlag(2);
			}

		} catch (Exception e) {

			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setFlag(4);
		}

		return structure;
	}

}
