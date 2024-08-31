 package com.bp.middleware.locations;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="pincode_table")
public class PincodeModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="pincode_id")
	private int pincodeId;

	@Column(name="pin_code")
	private String pincode;

	@Column(name="created_by")
	private String createdby;

	@Column(name = "created_date_and_time")
	private Date createddateandtime;
	
	@Column(name = "modified_by")
	private String modifiedBy;
	
	@Column(name = "modified_date_and_time")
	private Date modifiedDateTime;
	
	@Column(name = "pincode_status")
	private boolean pincodestatus;

	@ManyToOne(fetch = FetchType.LAZY, optional=false)
	@JoinColumn(name="city_id")
	@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
	private CityModel cityModel;


	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public boolean getPincodestatus() {
		return pincodestatus;
	}

	public void setPincodestatus(boolean pincodestatus) {
		this.pincodestatus = pincodestatus;
	}
	
	public CityModel getCityModel() {
		return cityModel;
	}

	public void setCityModel(CityModel cityModel) {
		this.cityModel = cityModel;
	}


	public int getPincodeId() {
		return pincodeId;
	}

	public void setPincodeId(int pincodeId) {
		this.pincodeId = pincodeId;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getCreatedby() {
		return createdby;
	}

	public void setCreatedby(String createdby) {
		this.createdby = createdby;
	}

	public Date getCreateddateandtime() {
		return createddateandtime;
	}

	public void setCreateddateandtime(Date createddateandtime) {
		this.createddateandtime = createddateandtime;
	}

	public Date getModifiedDateTime() {
		return modifiedDateTime;
	}

	public void setModifiedDateTime(Date modifiedDateTime) {
		this.modifiedDateTime = modifiedDateTime;
	}
	
}
