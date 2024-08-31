package com.bp.middleware.bankaccountandhsncode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="admin_bank_account")
public class AdminBankAccount {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="bankAcc_id")
	private int bankAccId;
	
	@Column(name="account_number")
	private String accountNumber;
	
	@Column(name="bank_name")
	private String bankName;
	
	@Column(name="ifsc_code")
	private String ifscCode;
	
	@Column(name="status")
	private boolean status;

	public int getBankAccId() {
		return bankAccId;
	}

	public void setBankAccId(int bankAccId) {
		this.bankAccId = bankAccId;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}
	
	
	
	
}
