package com.bp.middleware.technical;

import java.util.Date;

import com.bp.middleware.user.EntityModel;
import com.bp.middleware.vendors.VendorModel;
import com.bp.middleware.vendors.VendorVerificationModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="technical_model")
public class TechnicalModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="technical_id")
	private int technicalId;
	
	@Column(name="api_link")
	private String apiLink;
	
	@Column(name="id_price")
	private double idPrice;
	
	@Column(name="image_price")
	private double imagePrice;
	
	@Column(name="status")
	private boolean status;
	
	@Column(name="req_date")
	private Date reqDate;
	
	@Lob
	@Column(name="img_byte")
	private byte[] imageData;
	
//	@ManyToOne(fetch = FetchType.LAZY, optional = false)
//	@JoinColumn(name = "vendor_id")
//	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
//	private VendorModel vendorModel;
//	
//	@ManyToOne(fetch = FetchType.LAZY, optional = false)
//	@JoinColumn(name = "vendor_verification_id")
//	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
//	private VendorVerificationModel vendorVerificationModel;
//	
//	@ManyToOne(fetch = FetchType.LAZY, optional = false)
//	@JoinColumn(name = "user_id")
//	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
//	private EntityModel entityModel;

	public int getTechnicalId() {
		return technicalId;
	}

	public void setTechnicalId(int technicalId) {
		this.technicalId = technicalId;
	}

	public String getApiLink() {
		return apiLink;
	}

	public void setApiLink(String apiLink) {
		this.apiLink = apiLink;
	}

	public double getIdPrice() {
		return idPrice;
	}

	public void setIdPrice(double idPrice) {
		this.idPrice = idPrice;
	}

	public double getImagePrice() {
		return imagePrice;
	}

	public void setImagePrice(double imagePrice) {
		this.imagePrice = imagePrice;
	}

//	public VendorModel getVendorModel() {
//		return vendorModel;
//	}
//
//	public void setVendorModel(VendorModel vendorModel) {
//		this.vendorModel = vendorModel;
//	}
//
//	public VendorVerificationModel getVendorVerificationModel() {
//		return vendorVerificationModel;
//	}
//
//	public void setVendorVerificationModel(VendorVerificationModel vendorVerificationModel) {
//		this.vendorVerificationModel = vendorVerificationModel;
//	}
//
//	public EntityModel getEntityModel() {
//		return entityModel;
//	}
//
//	public void setEntityModel(EntityModel entityModel) {
//		this.entityModel = entityModel;
//	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public Date getReqDate() {
		return reqDate;
	}

	public void setReqDate(Date reqDate) {
		this.reqDate = reqDate;
	}

	public byte[] getImageData() {
		return imageData;
	}

	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}
	
	
	
}
