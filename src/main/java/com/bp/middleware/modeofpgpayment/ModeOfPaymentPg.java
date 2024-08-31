package com.bp.middleware.modeofpgpayment;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="mode_of_payment_pg")
public class ModeOfPaymentPg {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "mode_id")
	private int modeId;
	
	@Column(name = "mode_of_payment")
	private String modeOfPayment;
	
	@Column(name="payment_code")
	private String paymentCode;
	
	@Column(name="status")
	private boolean status;
	
	@Column(name="convenience_fetch",columnDefinition = "BOOLEAN DEFAULT TRUE")
	private boolean convenienceFetch;
	
	@Column(name="created_by")
	private String createdBy;
	@Column(name="created_date") 
	private Date createdDate;
	
	@Column(name="modified_by")
	private String modifiedBy;
	@Column(name="modified_date") 
	private Date modifiedDate;
	
	
	
	public int getModeId() {
		return modeId;
	}
	public void setModeId(int modeId) {
		this.modeId = modeId;
	}
	public String getModeOfPayment() {
		return modeOfPayment;
	}
	public void setModeOfPayment(String modeOfPayment) {
		this.modeOfPayment = modeOfPayment;
	}
	public String getPaymentCode() {
		return paymentCode;
	}
	public void setPaymentCode(String paymentCode) {
		this.paymentCode = paymentCode;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public boolean isConvenienceFetch() {
		return convenienceFetch;
	}
	public void setConvenienceFetch(boolean convenienceFetch) {
		this.convenienceFetch = convenienceFetch;
	}
	
	
	
	
}
