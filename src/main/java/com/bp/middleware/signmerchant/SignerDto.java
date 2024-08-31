package com.bp.middleware.signmerchant;

public class SignerDto {

	private int merchantId;
	private String signerName;
	private String signerMobile;
	private String signerEmail;
	private String createdBy;
	private String payer;
	
	
	public int getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}
	public String getSignerName() {
		return signerName;
	}
	public void setSignerName(String signerName) {
		this.signerName = signerName;
	}
	public String getSignerMobile() {
		return signerMobile;
	}
	public void setSignerMobile(String signerMobile) {
		this.signerMobile = signerMobile;
	}
	public String getSignerEmail() {
		return signerEmail;
	}
	public void setSignerEmail(String signerEmail) {
		this.signerEmail = signerEmail;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getPayer() {
		return payer;
	}
	public void setPayer(String payer) {
		this.payer = payer;
	}

	
	
	
	
}
