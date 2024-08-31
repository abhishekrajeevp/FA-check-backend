package com.bp.middleware.mandatedocument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.UserRepository;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.CommonRequestDto;
import com.bp.middleware.util.FileUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class MerchantKYCServiceImplement implements MerchantKYCService {

	@Autowired
	private MerchantKYCRepository kycRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private MandateDocumentRepository mandateDocRepository;
	@Autowired
	ServletContext context;
	@Autowired
	ResourceLoader resourceLoader;

	@Override
	public ResponseStructure uploadMerchantKyc(CommonRequestDto model) {
		ResponseStructure structure = new ResponseStructure();
		try {
			EntityModel entity = new EntityModel();
			Optional<EntityModel> optionalUser = userRepository.findById(model.getUserId());
			if (optionalUser.isPresent()) {
				entity = optionalUser.get();
			}
			MandateDocumentModel mandateDoc = new MandateDocumentModel();
			Optional<MandateDocumentModel> optionalDoc = mandateDocRepository.findById(model.getId());
			if (optionalDoc.isPresent()) {
				mandateDoc = optionalDoc.get();
			}

			MerchantKYCModel kycModel = kycRepository.getByMandateDocumentModelAndEntityModel(mandateDoc, entity);

			if (kycModel == null) {
				MerchantKYCModel merchantModel = new MerchantKYCModel();

				String folder = new FileUtils().genrateFolderName("" + model.getUserId());

				String extensionType = null;
				StringTokenizer st = new StringTokenizer(model.getDocFrontPath().getOriginalFilename());
				StringTokenizer pc = null;

				while (st.hasMoreElements()) {
					extensionType = st.nextElement().toString();
				}

				String fileName = FileUtils.getRandomString() + "." + extensionType;
				merchantModel.setDocFrontPath(folder + "/" + fileName);

				Path currentworkingDir = Paths.get(context.getRealPath("/WEB-INF"));
				File saveFile = new File(currentworkingDir + "/merchantkycdocument/" + folder);
				saveFile.mkdir();

				if (model.getDocBackPath() != null) {
					pc = new StringTokenizer(model.getDocBackPath().getOriginalFilename(), ".");
					while (pc.hasMoreElements()) {
						extensionType = pc.nextElement().toString();
					}

					String panfileName = FileUtils.getRandomString() + "." + extensionType;
					merchantModel.setDocBackPath(folder + "/" + panfileName);

					byte[] bytes1 = model.getDocBackPath().getBytes();
					Path path1 = Paths.get(saveFile + "/" + panfileName);
					Files.write(path1, bytes1);
				} else {
					merchantModel.setDocBackPath("null");
				}
				byte[] bytes = model.getDocFrontPath().getBytes();
				Path path = Paths.get(saveFile + "/" + fileName);
				Files.write(path, bytes);

				merchantModel.setEntityModel(entity);
				merchantModel.setCardNumber(model.getCardNumber());
				merchantModel.setFileType(model.getFileType());
				merchantModel.setDocType(model.getDocType());
				merchantModel.setCreatedBy(model.getCreatedBy());
				merchantModel.setAdminApproval(AppConstants.PENDING);
				merchantModel.setSuperAdminApproval(AppConstants.PENDING);
				merchantModel.setDeletedFlag(0);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss a");
				merchantModel.setCreatedDateTime(sdf.format(new Date()));
				merchantModel.setMandateDocumentModel(mandateDoc);

				kycRepository.save(merchantModel);

				structure.setData(merchantModel);
				structure.setMessage(AppConstants.SUCCESS);
				structure.setFlag(1);
				structure.setStatusCode(HttpStatus.OK.value());

			} else {
				structure.setData(null);
				structure.setMessage("KYC MODEL ALREARY PRESENT FOR GIVEN ENTITY");
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
	public ResponseEntity<Resource> getParticularMerchantDocs(long id, int flag, HttpServletRequest request) {

		ResponseStructure response = new ResponseStructure();

		Optional<MerchantKYCModel> merchantDocModel = kycRepository.findById(id);
		if (merchantDocModel.isPresent()) {
			String docPath = null;
			if (flag == 1)
				docPath = merchantDocModel.get().getDocFrontPath();
			else if (flag == 2)
				docPath = merchantDocModel.get().getDocBackPath();

			final Resource resource = resourceLoader.getResource("/WEB-INF/merchantkycdocument/" + docPath);
			String contentType = null;
			try {
				contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

			} catch (IOException ex) {

			}

			if (contentType == null) {
				contentType = "application/octet-stream";
			}
			response.setFlag(1);

			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);
		} else {
			return null;
		}

	}

	@Override
	public ResponseStructure getViewMerchantDocuments(int userId) {
		ResponseStructure structure = new ResponseStructure();
		try {
			Optional<EntityModel> optional = userRepository.findById(userId);

			if (optional.isPresent()) {
				EntityModel entity = optional.get();
				
				List<MerchantKYCModel> list = kycRepository.findByEntityModel(entity);
				structure.setFlag(1);
				structure.setData(list);
				structure.setMessage("user documents list viewed");
			} else {
				structure.setFlag(2);
				structure.setMessage("user id not exists");
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
	public ResponseStructure getAdminApproval(long id, CommonRequestDto dto) {
		ResponseStructure response = new ResponseStructure();

		try {
			Optional<MerchantKYCModel> model = kycRepository.findById(id);

			if (model.isPresent()) {
				MerchantKYCModel entity = model.get();
				entity.setAdminApproval(dto.getAdminApproval());
				entity.setApprovalByL1(dto.getApprovalBy());
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
				entity.setApprovalDateTime(sdf.format(new Date()));

				entity.setRemarks(dto.getComment());
				kycRepository.save(entity);

				response.setFlag(1);
				response.setMessage("Merchant kyc has been " + dto.getAdminApproval());
				response.setData(entity);
			} else {
				response.setFlag(2);
				response.setMessage("Invalid merchant please contact technical support team");
			}

			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			response.setFlag(3);
			response.setErrorDiscription(e.getMessage());
			response.setMessage(AppConstants.TECHNICAL_ERROR);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	@Override
	public ResponseStructure getSuperAdminApproval(long id, CommonRequestDto dto) {

		ResponseStructure response = new ResponseStructure();

		try {
			Optional<MerchantKYCModel> model = kycRepository.findById(id);

			if (model.isPresent()) {
				MerchantKYCModel entity = model.get();
				String approval1 = entity.getAdminApproval();

				if (approval1.equals("Approved")) {
					entity.setSuperAdminApproval(dto.getSuperAdminApproval());
					entity.setApprovalByL2(dto.getApprovalBy());

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy:mm:dd hh:mm:ss a");
					entity.setApprovalDateTime(sdf.format(new Date()));
					entity.setRemarks(dto.getComment());

					kycRepository.save(entity);

					response.setFlag(1);
					response.setMessage("Merchant has been " + dto.getSuperAdminApproval());
					response.setData(entity);
				}

			} else {
				response.setFlag(2);
				response.setMessage(AppConstants.NO_DATA_FOUND);
			}

			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			response.setFlag(3);
			response.setErrorDiscription(e.getMessage());
			response.setMessage(AppConstants.TECHNICAL_ERROR);
			// LOGGER.info("MerchantKYCServiceImpl Approval", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;

	}

	@Override
	public ResponseStructure updateKycDoc(long id, CommonRequestDto dto) {

		ResponseStructure response = new ResponseStructure();

		try {
			Optional<MerchantKYCModel> kycdoc = kycRepository.findById(id);

			if (kycdoc.isPresent()) {
				MerchantKYCModel model = kycdoc.get();
				String folder = new FileUtils().genrateFolderName("" + dto.getCorporateId());

				String extensionType = null;
				StringTokenizer st = new StringTokenizer(dto.getDocFrontPath().getOriginalFilename(), ".");
				StringTokenizer pc = null;

				while (st.hasMoreElements()) {
					extensionType = st.nextElement().toString();
				}

				String fileName = FileUtils.getRandomString() + "." + extensionType;
				model.setDocFrontPath(folder + "/" + fileName);

				Path currentWorkingDir = Paths.get(context.getRealPath("/WEB-INF"));
				File saveFile = new File(currentWorkingDir + "/merchantkycdocument/" + folder);
				saveFile.mkdir();

				if (dto.getDocBackPath() != null) {
					pc = new StringTokenizer(dto.getDocBackPath().getOriginalFilename(), ".");
					while (pc.hasMoreElements()) {
						extensionType = pc.nextElement().toString();
					}

					String panfileName = FileUtils.getRandomString() + "." + extensionType;
					model.setDocBackPath(folder + "/" + panfileName);

					byte[] bytes1 = dto.getDocBackPath().getBytes();
					Path path1 = Paths.get(saveFile + "/" + panfileName);
					Files.write(path1, bytes1);
				} else {
					model.setDocBackPath("null");
				}
				byte[] bytes = dto.getDocFrontPath().getBytes();
				Path path = Paths.get(saveFile + "/" + fileName);
				Files.write(path, bytes);

				model.setCardNumber(dto.getCardNumber());
				model.setFileType(dto.getFileType());
				model.setDocType(dto.getDocType());
				model.setModifiedBy(dto.getModifiedBy());
				model.setAdminApproval(AppConstants.PENDING);
				model.setSuperAdminApproval(AppConstants.PENDING);
				model.setDeletedFlag(0);

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss a");
				model.setModifiedDateTime(sdf.format(new Date()));

				kycRepository.save(model);
				response.setFlag(1);
				response.setMessage("Merchant kyc document has been updated");
				response.setData(model);
			} else {
				response.setFlag(2);
				response.setMessage(AppConstants.NO_DATA_FOUND);
			}
			response.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			response.setFlag(3);
			response.setErrorDiscription(e.getMessage());
			response.setMessage(AppConstants.TECHNICAL_ERROR);
			// LOGGER.info("MerchantKYCServiceImpl update", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}

		return response;
	}

	@Override
	public ResponseStructure viewkyc(long id, int userId) {

		ResponseStructure response = new ResponseStructure();

		try {
			Optional<EntityModel> optional = userRepository.findById(userId);
			if (optional.isPresent()) {
				EntityModel model = optional.get();
				MandateDocumentModel mandatedoc = new MandateDocumentModel();
				mandatedoc.setMandateId(id);
				Optional<MerchantKYCModel> entityModel = kycRepository
						.findByMandateDocumentModelAndEntityModel(mandatedoc, model);
				if (entityModel.isPresent()) {
					MerchantKYCModel kycModel = entityModel.get();
					response.setData(kycModel);
					response.setFlag(1);
					response.setMessage("Success");
				}

				response.setStatusCode(HttpStatus.OK.value());

			}

		} catch (Exception e) {
			response.setFlag(3);
			response.setErrorDiscription(e.getMessage());
			response.setMessage(AppConstants.TECHNICAL_ERROR);
			// LOGGER.info("MerchantKycServiceImpl user document viewed", e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return response;
	}

	@Override
	public ResponseEntity<Resource> viewImageByUserAndMandateDoc(int userId, int mandateDocId, int flag,
			HttpServletRequest request) {

		try {

			ResponseStructure response = new ResponseStructure();

			EntityModel entity = userRepository.findByUserId(userId);
			MandateDocumentModel mandateDoc = mandateDocRepository.findByMandateId(mandateDocId);
			
			Optional<MerchantKYCModel> merchantDocModel = kycRepository.findByMandateDocumentModelAndEntityModel(mandateDoc,entity);
			
			if (merchantDocModel.isPresent()) {
				String docPath = null;
				if (flag == 1)
					docPath = merchantDocModel.get().getDocFrontPath();
				else if (flag == 2)
					docPath = merchantDocModel.get().getDocBackPath();

				final Resource resource = resourceLoader.getResource("/WEB-INF/merchantkycdocument/" + docPath);
				String contentType = null;
				try {
					contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());

				} catch (IOException ex) {

				}

				if (contentType == null) {
					contentType = "application/octet-stream";
				}
				response.setFlag(1);

				return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
						.header(HttpHeaders.CONTENT_DISPOSITION,
								"attachment; filename=\"" + resource.getFilename() + "\"")
						.body(resource);
			} else {
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
