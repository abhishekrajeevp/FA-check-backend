package com.bp.middleware.sms;

import java.util.Date;

import com.bp.middleware.admin.AdminDto;
import com.bp.middleware.user.EntityModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


@Entity
@Table(name="smstemplates")
public class SMSEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="sms_temp_id")
	private int smsTempId;
	
	@Column(name="sms_temp_code")
	private String smsTempCode;
	
	@Column(name="sms_temp_message")
	private String smsTempMessage;
	
	@Column(name="sms_temp_desc")
	private String smsTempDescription;
	
	@Column(name="sms_temp_modifieddate")
	private Date smsModifiedDate;
	
	@Column(name="sms_temp_modifiedby")
	private String smsModifiedBy;
	
	@Column(name="sms_temp_status")
	private boolean smsTempStatus;
	
	@Column(name="sms_entity_id")
	private String smsEntityId;
	
	@Column(name="sms_template_id")
	private String smsTemplateId;

	@Column(name="sms_service_url")
	private String  smsServiceUrl;
	
	@Column(name="sms_user_name")
	private String  smsUserName;
	
	@Column(name="sms_password")
	private String smsPassword;
	
	@Column(name="sms_enabled")
	private String smsEnabled;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel entityModel;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id",nullable = true)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler"})
	private AdminDto admin;
	

	public int getSmsTempId() {
		return smsTempId;
	}

	public void setSmsTempId(int smsTempId) {
		this.smsTempId = smsTempId;
	}

	public String getSmsTempCode() {
		return smsTempCode;
	}

	public void setSmsTempCode(String smsTempCode) {
		this.smsTempCode = smsTempCode;
	}

	public String getSmsTempMessage() {
		return smsTempMessage;
	}

	public void setSmsTempMessage(String smsTempMessage) {
		this.smsTempMessage = smsTempMessage;
	}

	public String getSmsTempDescription() {
		return smsTempDescription;
	}

	public void setSmsTempDescription(String smsTempDescription) {
		this.smsTempDescription = smsTempDescription;
	}

	public Date getSmsModifiedDate() {
		return smsModifiedDate;
	}

	public void setSmsModifiedDate(Date smsModifiedDate) {
		this.smsModifiedDate = smsModifiedDate;
	}

	public String getSmsModifiedBy() {
		return smsModifiedBy;
	}

	public void setSmsModifiedBy(String smsModifiedBy) {
		this.smsModifiedBy = smsModifiedBy;
	}

	public boolean isSmsTempStatus() {
		return smsTempStatus;
	}

	public void setSmsTempStatus(boolean smsTempStatus) {
		this.smsTempStatus = smsTempStatus;
	}

	public String getSmsEntityId() {
		return smsEntityId;
	}

	public void setSmsEntityId(String smsEntityId) {
		this.smsEntityId = smsEntityId;
	}

	public String getSmsTemplateId() {
		return smsTemplateId;
	}

	public void setSmsTemplateId(String smsTemplateId) {
		this.smsTemplateId = smsTemplateId;
	}

	public String getSmsServiceUrl() {
		return smsServiceUrl;
	}

	public void setSmsServiceUrl(String smsServiceUrl) {
		this.smsServiceUrl = smsServiceUrl;
	}

	public String getSmsUserName() {
		return smsUserName;
	}

	public void setSmsUserName(String smsUserName) {
		this.smsUserName = smsUserName;
	}

	public String getSmsPassword() {
		return smsPassword;
	}

	public void setSmsPassword(String smsPassword) {
		this.smsPassword = smsPassword;
	}

	public String getSmsEnabled() {
		return smsEnabled;
	}

	public void setSmsEnabled(String smsEnabled) {
		this.smsEnabled = smsEnabled;
	}

	public EntityModel getEntityModel() {
		return entityModel;
	}

	public void setEntityModel(EntityModel entityModel) {
		this.entityModel = entityModel;
	}

	public AdminDto getAdmin() {
		return admin;
	}

	public void setAdmin(AdminDto admin) {
		this.admin = admin;
	}

	
	
	
	
	
}
