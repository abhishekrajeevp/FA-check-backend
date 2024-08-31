package com.bp.middleware.signers;

import java.io.IOException;
import java.text.ParseException;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

public interface SignerService {

	ResponseStructure addSigners(RequestModel model);

	ResponseStructure viewAllDetails();

	ResponseStructure viewById(int signerId);

	ResponseStructure updateDetails(RequestModel model, int signerId);

	ResponseStructure sentOtp(int signerId);

	ResponseStructure verifyotp(int signerId, RequestModel users, HttpServletRequest request) throws IOException, MessagingException, ParseException;

	ResponseStructure emailTrigger(int merchantId);

	ResponseStructure viewByMerechantId(int merchantId);

	ResponseStructure inviteSigner(int signerId);

	ResponseStructure deleteSigner(int signerId);

	ResponseStructure viewByUserId(int userId);

	ResponseStructure viewByMerchantAndUser(int merchantId, int userId);

	ResponseStructure importExcelForSigner();

	ResponseStructure signerExpire(RequestModel model, int signerId);

	ResponseStructure viewBySignerReference(String referenceId);

	ResponseStructure aadhaarSigning(RequestModel model);

	ResponseStructure aadhaarSigningOtpSubmit(RequestModel model, HttpServletRequest request);

	ResponseStructure signerConsent(RequestModel model);

	ResponseStructure signerLocationTracker(RequestModel model);



}
