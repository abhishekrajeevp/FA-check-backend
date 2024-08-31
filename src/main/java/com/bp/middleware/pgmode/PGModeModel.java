package com.bp.middleware.pgmode;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;



	@Entity
	@Table(name = "pg_payment_mode")
	public class PGModeModel {

		  @Id
		  @GeneratedValue(strategy =GenerationType.IDENTITY )
		  @Column(name = "id") 
		  private long id;
		  
		  @Column(name = "pg_mode")
		  private String pgMode;
		  
		  @Column(name = "pg_on_off_status")
		  private int pgOnoffStatus;
		  
		  @Column(name = "modified_by")
		  private String modifiedBy;
		  
		  @Column(name = "modified_date_time")
		  private Date modifiedDatetime;
		  
		  @Column(name = "pg_switch")
		  private int pgSwitch;						//1 - Track and Pay, 2 - Basispay
		  
		  @Column(name = "api_key")
		  private String apikey;
		  
		  @Column(name = "secret_key")
		  private String secretKey;
		  

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getPgMode() {
			return pgMode;
		}

		public void setPgMode(String pgMode) {
			this.pgMode = pgMode;
		}

		public int getPgOnoffStatus() {
			return pgOnoffStatus;
		}

		public void setPgOnoffStatus(int pgOnoffStatus) {
			this.pgOnoffStatus = pgOnoffStatus;
		}

		public String getModifiedBy() {
			return modifiedBy;
		}

		public void setModifiedBy(String modifiedBy) {
			this.modifiedBy = modifiedBy;
		}

		public Date getModifiedDatetime() {
			return modifiedDatetime;
		}

		public void setModifiedDatetime(Date modifiedDatetime) {
			this.modifiedDatetime = modifiedDatetime;
		}

		public int getPgSwitch() {
			return pgSwitch;
		}

		public void setPgSwitch(int pgSwitch) {
			this.pgSwitch = pgSwitch;
		}

		public String getApikey() {
			return apikey;
		}

		public void setApikey(String apikey) {
			this.apikey = apikey;
		}

		public String getSecretKey() {
			return secretKey;
		}

		public void setSecretKey(String secretKey) {
			this.secretKey = secretKey;
		}
	
}
