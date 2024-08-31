package com.bp.middleware.refund;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

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
@Table(name="refund_model")
public class RefundModel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="refund_id")
	private int refundId;
	
	@Column(name="payment_id")
	private String paymentId;
	
	@Column(name="refundStatus")
	private String refundStatus;
	
	@Column(name="remarks")
	private String remarks;
	
	@Column(name="refund_amount")
	private double refundAmount;
	
	@Column(name="paid_amount")
	private double paidAmount;
	
	@Column(name="refund_initiated_at")
	private LocalDate refundInitiatedAt;
	
	@Column(name="request_id")
	private String requestId;
	
	@Column(name="activity_id")
	private String activityId;
	
	@ManyToOne(fetch = FetchType.LAZY,optional = false)
	@JoinColumn(name = "user_id",nullable = false)
	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	private EntityModel user;

	public int getRefundId() {
		return refundId;
	}

	public void setRefundId(int refundId) {
		this.refundId = refundId;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(String refundStatus) {
		this.refundStatus = refundStatus;
	}

	public double getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(double refundAmount) {
		this.refundAmount = twoDecimelDouble(refundAmount);
	}

	public double getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(double paidAmount) {
		this.paidAmount = twoDecimelDouble(paidAmount);
	}

	public LocalDate getRefundInitiatedAt() {
		return refundInitiatedAt;
	}

	public void setRefundInitiatedAt(LocalDate refundInitiatedAt) {
		this.refundInitiatedAt = refundInitiatedAt;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getActivityId() {
		return activityId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

	public EntityModel getUser() {
		return user;
	}

	public void setUser(EntityModel user) {
		this.user = user;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	
	
	
	private double twoDecimelDouble(double value) {
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places

		double roundedValue = bd.doubleValue();
		
		return roundedValue;
	}
	
}
