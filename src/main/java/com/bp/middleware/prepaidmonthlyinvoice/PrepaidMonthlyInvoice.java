package com.bp.middleware.prepaidmonthlyinvoice;

import java.time.LocalDate;
import java.util.Date;

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
@Table(name="prepaid_Monthly_Invoice")
public class PrepaidMonthlyInvoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="monthly_Invoice")
	private int monthlyInvoiceId;
	
	@Column(name="invoice")
	private String invoice;
	@Column(name="from_date")
	private LocalDate fromDate;
	@Column(name="to_date")
	private LocalDate toDate;
	@Column(name="date_time")
	private Date dateTime;
	@Column(name="month")
	private String month;
	@Column(name="year")
	private int year;
	@Column(name = "unique_id")
	private String uniqueId;
	
	
	@Column(name="mail_sent",columnDefinition ="BOOLEAN DEFAULT FALSE")
	private boolean mailSent;
	
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "user_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel entity;


	public int getMonthlyInvoiceId() {
		return monthlyInvoiceId;
	}


	public void setMonthlyInvoiceId(int monthlyInvoiceId) {
		this.monthlyInvoiceId = monthlyInvoiceId;
	}



	public LocalDate getFromDate() {
		return fromDate;
	}


	public void setFromDate(LocalDate fromDate) {
		this.fromDate = fromDate;
	}


	public LocalDate getToDate() {
		return toDate;
	}


	public void setToDate(LocalDate toDate) {
		this.toDate = toDate;
	}

	public Date getDateTime() {
		return dateTime;
	}


	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}


	public String getMonth() {
		return month;
	}


	public void setMonth(String month) {
		this.month = month;
	}


	public int getYear() {
		return year;
	}


	public void setYear(int year) {
		this.year = year;
	}


	public EntityModel getEntity() {
		return entity;
	}


	public void setEntity(EntityModel entity) {
		this.entity = entity;
	}


	public String getInvoice() {
		return invoice;
	}


	public void setInvoice(String invoice) {
		this.invoice = invoice;
	}


	public boolean isMailSent() {
		return mailSent;
	}


	public void setMailSent(boolean mailSent) {
		this.mailSent = mailSent;
	}


	public String getUniqueId() {
		return uniqueId;
	}


	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	
	
}
