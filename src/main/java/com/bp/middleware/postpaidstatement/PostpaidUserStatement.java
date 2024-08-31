package com.bp.middleware.postpaidstatement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;

import com.bp.middleware.user.EntityModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
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
@Table(name="postpaid_user_statement")
public class PostpaidUserStatement {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="postpaid_statement")
	private int postpaidStatementId;
	
	private double debit;
	
	private double debitGst;
	
	private double credit;
	
	private String remark;
	
	private double creditGst;
	
	private double consumedBalance;
	
	private Date entryDate;
	
	private LocalDate date;
	
	private String month;
	
	private String service;
	
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false,cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel entityModel;


	public int getPostpaidStatementId() {
		return postpaidStatementId;
	}


	public void setPostpaidStatementId(int postpaidStatementId) {
		this.postpaidStatementId = postpaidStatementId;
	}


	public double getDebit() {
		return debit;
	}


	public void setDebit(double debit) {
		this.debit = twoDecimelDouble(debit);
	}


	public double getDebitGst() {
		return debitGst;
	}


	public void setDebitGst(double debitGst) {
		this.debitGst = twoDecimelDouble(debitGst);
	}


	public double getCredit() {
		return credit;
	}


	public void setCredit(double credit) {
		this.credit = twoDecimelDouble(credit);
	}


	public String getRemark() {
		return remark;
	}


	public void setRemark(String remark) {
		this.remark = remark;
	}


	public double getCreditGst() {
		return creditGst;
	}


	public void setCreditGst(double creditGst) {
		this.creditGst = twoDecimelDouble(creditGst);
	}


	public double getConsumedBalance() {
		return consumedBalance;
	}


	public void setConsumedBalance(double consumedBalance) {
		this.consumedBalance = twoDecimelDouble(consumedBalance);
	}


	public Date getEntryDate() {
		return entryDate;
	}


	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}


	public LocalDate getDate() {
		return date;
	}


	public void setDate(LocalDate date) {
		this.date = date;
	}


	public String getMonth() {
		return month;
	}


	public void setMonth(String month) {
		this.month = month;
	}


	public EntityModel getEntityModel() {
		return entityModel;
	}


	public void setEntityModel(EntityModel entityModel) {
		this.entityModel = entityModel;
	}
	
	public String getService() {
		return service;
	}


	public void setService(String service) {
		this.service = service;
	}


	private double twoDecimelDouble(double value) {
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places

		double roundedValue = bd.doubleValue();
		
		return roundedValue;
	}
}
