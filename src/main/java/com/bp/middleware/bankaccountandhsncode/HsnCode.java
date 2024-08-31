package com.bp.middleware.bankaccountandhsncode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="hsn_code")
public class HsnCode {

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="hsn_id")
	private int hsnId;
	
	@Column(name="hsn_number")
	private String hsnNumber;
	
	@Column(name="status")
	private boolean status;

	
	public int getHsnId() {
		return hsnId;
	}

	public void setHsnId(int hsnId) {
		this.hsnId = hsnId;
	}

	public String getHsnNumber() {
		return hsnNumber;
	}

	public void setHsnNumber(String hsnNumber) {
		this.hsnNumber = hsnNumber;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
	
	
}
