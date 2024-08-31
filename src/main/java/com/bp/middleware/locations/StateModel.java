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
@Table(name = "state_table")
public class StateModel {
	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name = "state_id")
	private int stateId;

	@Column(name = "state_name")
	private String stateName;

	@Column(name = "state_status")
	private boolean stateStatus;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "created_datetime")
	private Date createdDateTime;

	@Column(name = "modified_datetime")
	private Date modifiedDatetime;

	@Column(name = "modified_by")
	private String modifiedBy;

	@Column(name = "is_deleted")
	private int deletedFlag;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "country_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private CountryModel countryModel;

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public boolean isStateStatus() {
		return stateStatus;
	}

	public void setStateStatus(boolean stateStatus) {
		this.stateStatus = stateStatus;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
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

	public int getDeletedFlag() {
		return deletedFlag;
	}

	public void setDeletedFlag(int deletedFlag) {
		this.deletedFlag = deletedFlag;
	}

//	@JsonProperty(access = Access.WRITE_ONLY)
	public CountryModel getCountryModel() {
		return countryModel;
	}

	public void setCountryModel(CountryModel countryModel) {
		this.countryModel = countryModel;
	}

	public int getStateId() {
		return stateId;
	}

	public void setStateId(int stateId) {
		this.stateId = stateId;
	}

}
