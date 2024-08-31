package com.bp.middleware.customexception;

import com.bp.middleware.erroridentifier.VendorSideIssues;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.vendors.VendorVerificationModel;

public class FacheckSideException extends Exception{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int statusCode;
	private VendorSideIssues vendorIssue;
	private String errorCode;
	private EntityModel entity;
	private VendorVerificationModel verification;
	private String source;
	
	public FacheckSideException() {
		
		super();
	}
	
	
	public FacheckSideException(String message,int statusCode,VendorSideIssues vendorIssue,EntityModel entity,VendorVerificationModel verification) {
		
		super(message);
		
		this.statusCode=statusCode;
		this.vendorIssue=vendorIssue;
		this.entity=entity;
		this.verification=verification;
	}


	public FacheckSideException(String message,int statusCode, String errorCode, VendorSideIssues vendorIssue,EntityModel entity,VendorVerificationModel verification) {

		super(message);
		
		this.statusCode=statusCode;
		this.vendorIssue=vendorIssue;
		this.errorCode=errorCode;
		this.entity=entity;
		this.verification=verification;

	}


	public int getStatusCode() {
		return statusCode;
	}


	public VendorSideIssues getVendorIssue() {
		return vendorIssue;
	}


	public void setVendorIssue(VendorSideIssues vendorIssue) {
		this.vendorIssue = vendorIssue;
	}


	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}


	public String getErrorCode() {
		return errorCode;
	}


	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	public EntityModel getEntity() {
		return entity;
	}


	public void setEntity(EntityModel entity) {
		this.entity = entity;
	}


	public VendorVerificationModel getVerification() {
		return verification;
	}


	public void setVerification(VendorVerificationModel verification) {
		this.verification = verification;
	}


	public String getSource() {
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}
	
	
	
	
}
