package com.bp.middleware.useraccesskeys;

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
import jakarta.persistence.Table;

@Entity
@Table(name="user_access_keys")
public class EntityAccessKeys {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="access_key_id")
	private int accessKeyId;
	
	@Column(name="api_sandbox_key")
	private String sandboxApiKey;
	
	@Column(name="live_api_key")
	private String liveApiKey;
	
	@Column(name="application_id")
	private String applicationId;
	
	@Column(name="secret_key")
	private String secretKey;
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "user_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel user;

	public int getAccessKeyId() {
		return accessKeyId;
	}

	public void setAccessKeyId(int accessKeyId) {
		this.accessKeyId = accessKeyId;
	}

	public String getSandboxApiKey() {
		return sandboxApiKey;
	}

	public void setSandboxApiKey(String sandboxApiKey) {
		this.sandboxApiKey = sandboxApiKey;
	}

	public String getLiveApiKey() {
		return liveApiKey;
	}

	public void setLiveApiKey(String liveApiKey) {
		this.liveApiKey = liveApiKey;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public EntityModel getUser() {
		return user;
	}

	public void setUser(EntityModel user) {
		this.user = user;
	}
	
}
