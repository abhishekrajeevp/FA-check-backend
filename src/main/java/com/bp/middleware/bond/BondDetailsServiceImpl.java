package com.bp.middleware.bond;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;
import com.bp.middleware.util.FileUtils;

import jakarta.servlet.ServletContext;

@Service
public class BondDetailsServiceImpl implements BondDetailsService{

	@Autowired
	ServletContext context;

	@Autowired
	private MerchantBondRepository merchantBondRepository;

	@Autowired
	private BondDetailRepository bondDetailRepository;

	@Override
	public ResponseStructure uploadBondDetails(RequestModel model) {
		ResponseStructure structure=new ResponseStructure();
		try {
			MerchantBond merchantBond = merchantBondRepository.findByBondId(model.getBondId());
			BondDetails bond=new BondDetails();
			LocalDate sealDate = LocalDate.parse(model.getSealedDate(), DateTimeFormatter.ISO_DATE);
			bond.setSealedDate(sealDate);
			bond.setBondNumber(model.getBondNumber());
			bond.setBondStatus(model.getBondStatus());
			bond.setCreatedBy(model.getCreatedBy());
			bond.setCreatedAt(LocalDate.now());
		
			bond.setStatus(model.isStatusFlag());
			bond.setBond(merchantBond);
			bond.setDocument(saveImage(model.getDocument()));

			bondDetailRepository.save(bond);

			merchantBond.setUploadCount(merchantBond.getUploadCount()+1);
			merchantBond.setRemainingCount(merchantBond.getRemainingCount()+1);

			merchantBondRepository.save(merchantBond);

			structure.setMessage("BOND DETAILS ADDED SUCCESSFULLY");
			structure.setStatusCode(HttpStatus.OK.value());
			structure.setData(bond);
			structure.setFlag(1);

		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;

	}

	@Override
	public ResponseStructure listAllUploadedBond(int bondId) {
		ResponseStructure structure=new ResponseStructure();
		try {
			MerchantBond bond = merchantBondRepository.findByBondId(bondId);
			List<BondDetails> list = bondDetailRepository.findByBond(bond);
			if (!list.isEmpty()) {
				List<BondDetails> details =new ArrayList<>();
				for (BondDetails bondDetails : list) {
					if (bondDetails.isStatus()) {
						details.add(bondDetails);	
					}
				}
				structure.setMessage("Bond Details list are");
				structure.setData(details);
				structure.setFlag(1);
			} else {
				structure.setMessage("Bond Details list not available");
				structure.setData(null);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseStructure getBondBySealedDate(RequestModel model) {
		ResponseStructure structure=new ResponseStructure();
		try {
			LocalDate sealDate = LocalDate.parse(model.getSealedDate(), DateTimeFormatter.ISO_DATE);

			List<BondDetails> details =	bondDetailRepository.findBySealedDate(sealDate);
			if (!details.isEmpty()) {
				structure.setMessage("Bond Details list are");
				structure.setData(details);
				structure.setFlag(1);
			} else {
				structure.setMessage("Bond Details list not available");
				structure.setData(null);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			e.printStackTrace();
			structure.setErrorDiscription(e.getMessage());
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}

	@Override
	public ResponseStructure listByBondNumber(String bondNumber) {
		ResponseStructure structure=new ResponseStructure();
		try {
			BondDetails details =	bondDetailRepository.findByBondNumber(bondNumber);
			if (details!=null) {
				structure.setMessage("Bond Details are");
				structure.setData(details);
				structure.setFlag(1);
			} else {
				structure.setMessage("Bond Details not available");
				structure.setData(null);
				structure.setFlag(2);
			}
			structure.setStatusCode(HttpStatus.OK.value());
		} catch (Exception e) {
			e.printStackTrace();
			structure.setMessage(AppConstants.TECHNICAL_ERROR);
			structure.setErrorDiscription(e.getMessage());
			structure.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			structure.setData(null);
			structure.setFlag(2);
		}
		return structure;
	}
	private String saveImage(MultipartFile profilePhoto) {
		try {
			String extensionType = null;
			StringTokenizer st = new StringTokenizer(profilePhoto.getOriginalFilename(), ".");
			while (st.hasMoreElements()) {
				extensionType = st.nextElement().toString();
			}
			String fileName = FileUtils.getRandomString() + "." + extensionType;

			Path currentWorkingDir = Paths.get(context.getRealPath("/WEB-INF/"));
			File saveFile = new File(currentWorkingDir + "/bonddocuments/");
			saveFile.mkdir();

			byte[] bytes = profilePhoto.getBytes();
			Path path = Paths.get(saveFile + "/" + fileName);
			Files.write(path, bytes);
			return fileName;
		} catch (Exception e) {
			return null;
		}
	}

}
