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
@Table(name = "city_table")
public class CityModel {
	@Id 
	@Column(name = "city_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private int cityId;

	@Column(name = "city_name")
	private String cityName;

	@Column(name = "city_status")
	private boolean cityStatus;

	@Column(name = "modified_datetime")
	private Date modifiedDatetime;

	@Column(name = "modified_by")
	private String modifiedBy;
	
	@Column(name = "created_by")
	private String createdBy;
	
	@Column(name = "created_datetime")
	private Date createdDatetime;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "state_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private StateModel stateModel;

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}


	public Date getCreatedDatetime() {
		return createdDatetime;
	}

	public void setCreatedDatetime(Date createdDatetime) {
		this.createdDatetime = createdDatetime;
	}

	public StateModel getStateModel() {
		return stateModel;
	}

	public void setStateModel(StateModel stateModel) {
		this.stateModel = stateModel;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public boolean getCityStatus() {
		return cityStatus;
	}

	public void setCityStatus(boolean cityStatus) {
		this.cityStatus = cityStatus;
	}

	public Date getModifiedDatetime() {
		return modifiedDatetime;
	}

	public void setModifiedDatetime(Date modifiedDatetime) {
		this.modifiedDatetime = modifiedDatetime;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}


}
