package com.bp.middleware.locations;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "country_table")
public class CountryModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "country_id")
	private int countryId;

	@Column(name = "country_name")
	private String countryName;

	@Column(name = "country_code")
	private String countryCode;

	@Column(name = "country_currency")
	private String countryCurrency;

	@Column(name = "country_timezone")
	private String countryTimezone;

	@Column(name = "country_status")
	private boolean countryStatus;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "created_datetime")
	private Date createdDatetime;

	@Column(name = "modified_datetime")
	private Date modifiedDatetime;

	@Column(name = "modified_by")
	private String modifiedBy;


	public int getCountryId() {
		return countryId;
	}

	public void setCountryId(int countryId) {
		this.countryId = countryId;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCountryCurrency() {
		return countryCurrency;
	}

	public void setCountryCurrency(String countryCurrency) {
		this.countryCurrency = countryCurrency;
	}

	public String getCountryTimezone() {
		return countryTimezone;
	}

	public void setCountryTimezone(String countryTimezone) {
		this.countryTimezone = countryTimezone;
	}

	public boolean getCountryStatus() {
		return countryStatus;
	}

	public void setCountryStatus(boolean countryStatus) {
		this.countryStatus = countryStatus;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	

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

	public Date getModifiedDatetime() {
		return modifiedDatetime;
	}

	public void setModifiedDatetime(Date modifiedDatetime) {
		this.modifiedDatetime = modifiedDatetime;
	}
	

}
