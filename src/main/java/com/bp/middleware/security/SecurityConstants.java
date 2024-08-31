package com.bp.middleware.security;

public class SecurityConstants {
	
	private SecurityConstants() {
	}
	
	/** The Constant JWT_TOKEN_VALIDITY. */
	public static final long JWT_TOKEN_VALIDITY = (long)30 * 24 * 60 * 60;
	
	/** The Constant JWT_TOKEN_VALIDITY. */
	public static final long JWT_REFRESH_TOKEN_VALIDITY = (long)30 * 24 * 60 * 60;
	
	/** The Constant SECRET. */
	public static final String SECRET = "JWTCinchFuel222022021SecretKey";
	
	public static final String USER_NAME = "USER_NAME";
}
