package com.bp.middleware.transactionamountmodel;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="transaction_amount_model")
public class TransactionAmountModel {

	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="trancsaction_amount_id")
	private int trancsactionAmountId;
	
	@Column(name="exclusive_amount")
	private double exclusiveAmount;
	@Column(name="igst")
	private double igst;
	@Column(name="cgst")
	private double cgst;
	@Column(name="sgst")
	private double sgst;
	@Column(name="exclus_with_gst")
	private double exclusiveWithGst;
	@Column(name="conveniance_fee")
	private double convenianceFee;
	@Column(name="fixed_fee")
	private double fixedFee;
	@Column(name="other_gst")
	private double otherGst;
	@Column(name="over_all_total_amount")
	private double overAllTotalAmount;
	@Column(name="remarks")
	private String remarks;
	@Column(name="convenience_fetched",columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean convenienceFetched;
	
	
	public int getTrancsactionAmountId() {
		return trancsactionAmountId;
	}
	public void setTrancsactionAmountId(int trancsactionAmountId) {
		this.trancsactionAmountId = trancsactionAmountId;
	}
	public double getExclusiveAmount() {
		return exclusiveAmount;
	}
	public void setExclusiveAmount(double exclusiveAmount) {
		this.exclusiveAmount = twoDecimelDouble(exclusiveAmount);
	}
	public double getIgst() {
		return igst;
	}
	public void setIgst(double igst) {
		this.igst = twoDecimelDouble(igst);
	}
	public double getCgst() {
		return cgst;
	}
	public void setCgst(double cgst) {
		this.cgst = twoDecimelDouble(cgst);
	}
	public double getSgst() {
		return sgst;
	}
	public void setSgst(double sgst) {
		this.sgst = twoDecimelDouble(sgst);
	}
	public double getExclusiveWithGst() {
		return exclusiveWithGst;
	}
	public void setExclusiveWithGst(double exclusiveWithGst) {
		this.exclusiveWithGst = twoDecimelDouble(exclusiveWithGst);
	}
	public double getConvenianceFee() {
		return convenianceFee;
	}
	public void setConvenianceFee(double convenianceFee) {
		this.convenianceFee = twoDecimelDouble(convenianceFee);
	}
	public double getFixedFee() {
		return fixedFee;
	}
	public void setFixedFee(double fixedFee) {
		this.fixedFee = twoDecimelDouble(fixedFee);
	}
	public double getOtherGst() {
		return otherGst;
	}
	public void setOtherGst(double otherGst) {
		this.otherGst = twoDecimelDouble(otherGst);
	}
	public double getOverAllTotalAmount() {
		return overAllTotalAmount;
	}
	public void setOverAllTotalAmount(double overAllTotalAmount) {
		this.overAllTotalAmount = twoDecimelDouble(overAllTotalAmount);
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
	
	public boolean isConvenienceFetched() {
		return convenienceFetched;
	}
	public void setConvenienceFetched(boolean convenienceFetched) {
		this.convenienceFetched = convenienceFetched;
	}
	
	
	
	
	private double twoDecimelDouble(double value) {
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places

		double roundedValue = bd.doubleValue();
		
		return roundedValue;
	}
	
}
