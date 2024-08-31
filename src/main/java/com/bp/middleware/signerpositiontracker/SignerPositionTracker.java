package com.bp.middleware.signerpositiontracker;

import java.util.Date;

import com.bp.middleware.signers.SignerModel;
import com.bp.middleware.vendors.VendorVerificationModel;
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
@Table(name = "signer_position_tracker")
public class SignerPositionTracker {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="position_id")
	private int positionId;
	
	@Column(name="zip")
	private String zip;
	@Column(name="country")
	private String country;
	@Column(name="city")
	private String city;
	@Column(name="service_provider")
	private String serviceProvider;
	@Column(name="timezone")
	private String timezone;
	@Column(name="region")
	private String region;
	@Column(name="public_ip")
	private String publicIp;
	@Column(name="longitude")
	private String longitude;
	@Column(name="latitude")
	private String latitude;
	@Column(name="location_status")
	private String locationStatus;
	@Column(name="country_code")
	private String countryCode;
	@Column(name="region_code")
	private String regionCode;
	@Column(name="autonomous_system_number")
	private String autonomousSystemNumber;
	@Column(name="track_time")
	private Date trackTime;
	
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "signer_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private SignerModel signer;

	
	
	public int getPositionId() {
		return positionId;
	}

	public void setPositionId(int positionId) {
		this.positionId = positionId;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(String serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPublicIp() {
		return publicIp;
	}

	public void setPublicIp(String publicIp) {
		this.publicIp = publicIp;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLocationStatus() {
		return locationStatus;
	}

	public void setLocationStatus(String locationStatus) {
		this.locationStatus = locationStatus;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getRegionCode() {
		return regionCode;
	}

	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}

	public String getAutonomousSystemNumber() {
		return autonomousSystemNumber;
	}

	public void setAutonomousSystemNumber(String autonomousSystemNumber) {
		this.autonomousSystemNumber = autonomousSystemNumber;
	}

	public SignerModel getSigner() {
		return signer;
	}

	public void setSigner(SignerModel signer) {
		this.signer = signer;
	}

	public Date getTrackTime() {
		return trackTime;
	}

	public void setTrackTime(Date trackTime) {
		this.trackTime = trackTime;
	}

	
}
