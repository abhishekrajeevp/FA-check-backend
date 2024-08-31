package com.bp.middleware.mandatedocument;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bp.middleware.businesscategory.BusinessCategory;
import com.bp.middleware.businesscategory.BusinessCategoryRepository;
import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

@Service
public class MandateDocServiceImplementation implements MandateDocumentServices {

	@Autowired
	private MandateDocumentRepository mandateDocRepository;
	@Autowired
	private BusinessCategoryRepository businessCategoryRepository;

	@Override
	public ResponseStructure addMandateDocDetails(RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {

			BusinessCategory findByBusinessCategoryId = businessCategoryRepository
					.findByBusinessCategoryId(model.getBusinessCategoryId());

			if (findByBusinessCategoryId != null) {

				Optional<MandateDocumentModel> mandatePresent = mandateDocRepository
						.findByKycDocNameAndBusinessCategory(model.getKycDocName(), findByBusinessCategoryId);

				if (mandatePresent.isEmpty()) {

					MandateDocumentModel mandateDoc = new MandateDocumentModel();

					mandateDoc.setKycDocName(model.getKycDocName());
					mandateDoc.setDocType(model.getDocType());
					mandateDoc.setBusinessCategory(findByBusinessCategoryId);
					mandateDoc.setCreatedBy(model.getCreatedBy());
					mandateDoc.setStatus(true);

					SimpleDateFormat sdf = new SimpleDateFormat();
					mandateDoc.setCreatedDateTime(sdf.format(new Date()));

					mandateDocRepository.save(mandateDoc);

					structure.setFlag(1);
					structure.setMessage(AppConstants.SUCCESS);
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(mandateDoc);

				} else {

					structure.setFlag(2);
					structure.setMessage("This Mandate document is already present for the given Business category");
					structure.setStatusCode(HttpStatus.OK.value());
					structure.setData(null);
				}
			} else {

				structure.setFlag(3);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setStatusCode(HttpStatus.OK.value());
				structure.setData(null);
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewMandateDocDetailsById(long id) {
		ResponseStructure structure = new ResponseStructure();
		try {

			Optional<MandateDocumentModel> optional = mandateDocRepository.findById(id);
			if (optional.isPresent()) {

				MandateDocumentModel mandateModel = optional.get();

				structure.setData(mandateModel);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewAllMandateDoc() {
		ResponseStructure structure = new ResponseStructure();
		try {
			List<MandateDocumentModel> listMandateDoc = mandateDocRepository.findAll();

			if (!listMandateDoc.isEmpty()) {
				structure.setData(listMandateDoc);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
			} else {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure updateMandateDoc(long id, RequestModel model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<MandateDocumentModel> optional = mandateDocRepository.findById(id);
			BusinessCategory findByBusinessCategoryId = businessCategoryRepository
					.findByBusinessCategoryId(model.getBusinessCategoryId());

			if (optional.isPresent()) {
				MandateDocumentModel mandateDoc = optional.get();

				mandateDoc.setKycDocName(model.getKycDocName());
				mandateDoc.setDocType(model.getDocType());

				if (findByBusinessCategoryId != null) {
					mandateDoc.setBusinessCategory(findByBusinessCategoryId);
				}
				mandateDoc.setModifiedBy(model.getModifiedBy());

				SimpleDateFormat sdf = new SimpleDateFormat();
				mandateDoc.setModifiedDateAndTime(sdf.format(new Date()));

				mandateDocRepository.save(mandateDoc);

				structure.setData(mandateDoc);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure deleteMandateDoc(long id) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<MandateDocumentModel> optional = mandateDocRepository.findById(id);

			if (optional.isPresent()) {
				MandateDocumentModel mandateModel = optional.get();
				mandateDocRepository.deleteById(mandateModel.getMandateId());

				structure.setData(mandateModel);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());
			} else {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}
		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure viewAllMandateDocByBusinessCategory(int businessCategoryId) {
		ResponseStructure structure = new ResponseStructure();
		try {

			BusinessCategory businessCateg = businessCategoryRepository
					.findByBusinessCategoryId(businessCategoryId);

			if (businessCateg != null) {
				
				List<MandateDocumentModel> listMandateDoc = mandateDocRepository.findByBusinessCategory(businessCateg);

				if (!listMandateDoc.isEmpty()) {

					structure.setData(listMandateDoc);
					structure.setMessage(AppConstants.SUCCESS);
					structure.setFlag(1);
					structure.setStatusCode(HttpStatus.OK.value());

				} else {

					structure.setData(null);
					structure.setMessage(AppConstants.NO_DATA_FOUND);
					structure.setFlag(2);
					structure.setStatusCode(HttpStatus.OK.value());
				}
			} else {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(3);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(4);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}

	@Override
	public ResponseStructure updateMandateDocStatus(long id, RequestModel model) {
		
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<MandateDocumentModel> optional = mandateDocRepository.findById(id);

			if (optional.isPresent()) {
				MandateDocumentModel mandateDoc = optional.get();

				mandateDoc.setStatus(model.isAccountStatus());
				mandateDoc.setModifiedBy(model.getModifiedBy());

				SimpleDateFormat sdf = new SimpleDateFormat();
				mandateDoc.setModifiedDateAndTime(sdf.format(new Date()));

				mandateDocRepository.save(mandateDoc);

				structure.setData(mandateDoc);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {
				structure.setData(null);
				structure.setMessage(AppConstants.NO_DATA_FOUND);
				structure.setFlag(2);
				structure.setStatusCode(HttpStatus.OK.value());
			}

		} catch (Exception e) {
			structure.setData(null);
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setFlag(3);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return structure;
	}
}
