package com.bp.middleware.emailservice;

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
@Table(name="mail_table")
public class SmtpMailConfiguration {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="mail_id")
	private int mailId;
	
	@Column(name="mail_host")
	private String host;
	
	@Column(name="mail_port")
	private String port;
	
	@Column(name="mail_smtauth")
	private String smtpAuth;
	
	@Column(name="mail_smtp_port")
	private String smtpPort;
	
	@Column(name="mail_smtp_connection_timeout")
	private String smtpConnectionTimeOut;
	
	@Column(name="mail_starttls_enable")
	private String starttlsEnable;
	
	@Column(name="mail_socket_factory_port")
	private String socketFactoryPort;
	
	@Column(name="mail_socket_factory_class")
	private String socketFactoryClass;
	
	@Column(name="mail_protocol")
	private String protocol;
	
	@Column(name="mail_user_name")
	private String mailUserName;
	
	@Column(name="mail_password")
	private String mailPassword;
	
	@Column(name="mail_smtp_timeout")
	private String smtpTimeOut;
	
	@Column(name="mail_smtp_write_timeout")
	private String smtpWriteTimeOut;
	
	@Column(name="created_by")
	private String createdBy;
	
	@Column(name="created_date")
	private Date createdDate;
		
	@Column(name="modify_by")
	private String modifyBy;
	
	@Column(name="modify_date")
	private Date modifyDate;
	
	@OneToOne(fetch = FetchType.LAZY,optional=false)
	@JoinColumn(name = "user_id")
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel entity;
	

	public int getMailId() {
		return mailId;
	}

	public void setMailId(int mailId) {
		this.mailId = mailId;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getSmtpAuth() {
		return smtpAuth;
	}

	public void setSmtpAuth(String smtpAuth) {
		this.smtpAuth = smtpAuth;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public String getSmtpConnectionTimeOut() {
		return smtpConnectionTimeOut;
	}

	public void setSmtpConnectionTimeOut(String smtpConnectionTimeOut) {
		this.smtpConnectionTimeOut = smtpConnectionTimeOut;
	}

	public String getStarttlsEnable() {
		return starttlsEnable;
	}

	public void setStarttlsEnable(String starttlsEnable) {
		this.starttlsEnable = starttlsEnable;
	}

	public String getSocketFactoryPort() {
		return socketFactoryPort;
	}

	public void setSocketFactoryPort(String socketFactoryPort) {
		this.socketFactoryPort = socketFactoryPort;
	}

	public String getSocketFactoryClass() {
		return socketFactoryClass;
	}

	public void setSocketFactoryClass(String socketFactoryClass) {
		this.socketFactoryClass = socketFactoryClass;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getMailUserName() {
		return mailUserName;
	}

	public void setMailUserName(String mailUserName) {
		this.mailUserName = mailUserName;
	}

	public String getMailPassword() {
		return mailPassword;
	}

	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}

	public String getSmtpTimeOut() {
		return smtpTimeOut;
	}

	public void setSmtpTimeOut(String smtpTimeOut) {
		this.smtpTimeOut = smtpTimeOut;
	}

	public String getSmtpWriteTimeOut() {
		return smtpWriteTimeOut;
	}

	public void setSmtpWriteTimeOut(String smtpWriteTimeOut) {
		this.smtpWriteTimeOut = smtpWriteTimeOut;
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

	public String getModifyBy() {
		return modifyBy;
	}

	public void setModifyBy(String modifyBy) {
		this.modifyBy = modifyBy;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public EntityModel getEntity() {
		return entity;
	}

	public void setEntity(EntityModel entity) {
		this.entity = entity;
	}

}
