package com.bp.middleware.mcccode;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="mcc_codes")
public class MCCCodesModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "mcc_id")
	private int mccId;
	
	@Column(name ="merchant_type")
	private String corpoarteType;
	@Column(name = "mcc_code")
	private String mccCode;
	@Column(name ="modified_By")
	private String modifiedBy;
	@Column(name = "modified_date_time")
	private String modifiedDateAndTime;
	@Column(name = "created_By")
	private String createdBy;
	@Column(name ="created_date_time")
	private String createdDateTime;
	@Column(name="status")
	private boolean status;

	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getCreatedDateTime() {
		return createdDateTime;
	}
	public void setCreatedDateTime(String createdDateTime) {
		this.createdDateTime = createdDateTime;
	}
	public String getModifiedDateAndTime() {
		return modifiedDateAndTime;
	}
	public void setModifiedDateAndTime(String modifiedDateAndTime) {
		this.modifiedDateAndTime = modifiedDateAndTime;
	}
	public int getMccId() {
		return mccId;
	}
	public void setMccId(int mccId) {
		this.mccId = mccId;
	}
	public String getCorpoarteType() {
		return corpoarteType;
	}
	public void setCorpoarteType(String corpoarteType) {
		this.corpoarteType = corpoarteType;
	}

	public String getMccCode() {
		return mccCode;
	}
	public void setMccCode(String mccCode) {
		this.mccCode = mccCode;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	
}
