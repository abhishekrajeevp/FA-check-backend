package com.bp.middleware.security;

public enum SecurityEnum {
	/** The order create. */
	///JWT_TOKEN("Authorization"),
	JWT_TOKEN("X_ACCESS_TOKEN"),
	JWT_REFRESH_TOKEN("REFRESH_TOKEN"),
	USER_NAME("X_USER_NAME");
	
	/** The status. */
	private final String status;
	/**
	 * Instantiates a new order status enum.
	 * 
	 * @param value the value
	 * @param status the status
	 */
	SecurityEnum(String status) {
		this.status = status;
	}
	/**
	 * Return the integer value of this status code.
	 *
	 * @return the int
	 */
	public String status() {
		return this.status;
	}

}
