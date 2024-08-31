package com.bp.middleware.jwt;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.bp.middleware.security.SecurityConstants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;



@SuppressWarnings("serial")
@Component
public class JWTTokenProvider implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Gets the ID token from jwt token.
	 *
	 * @param token the token
	 * @return the ID token from jwt token
	 */
	//retrieve username from jwt token
	public String getIDTokenFromJwtToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}
	
	/**
	 * Gets the expiration date from token.
	 *
	 * @param token the token
	 * @return the expiration date from token
	 */
	//retrieve expiration date from jwt token
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}
	
	/**
	 * Gets the claim from token.
	 *
	 * @param <T> the generic type
	 * @param token the token
	 * @param claimsResolver the claims resolver
	 * @return the claim from token
	 */
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}
	
	/**
	 * Gets the all claims from token.
	 *
	 * @param token the token
	 * @return the all claims from token
	 */
	//for retrieveing any information from token we will need the secret key
	public Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(SecurityConstants.SECRET).parseClaimsJws(token).getBody();
	}
	
	/**
	 * Checks if is token expired.
	 *
	 * @param token the token
	 * @return the boolean
	 */
	//check if the token has expired
	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}
	
	/**
	 * Generate token.
	 *
	 * @param idToken the id token
	 * @return the string
	 */
	//generate token for user
	public String generateToken(String idToken) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(SecurityConstants.USER_NAME, idToken);
		return doGenerateToken(claims, idToken);
	}
	/**
	 * Do generate token.
	 *
	 * @param claims the claims
	 * @param subject the subject
	 * @return the string
	 */
	//   compaction of the JWT to a URL-safe string 
	private String doGenerateToken(Map<String, Object> claims, String subject) {
				return Jwts.builder()
						.setClaims(claims)
						.setSubject(subject)
						.setIssuedAt(new Date(System.currentTimeMillis()))
						.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.JWT_TOKEN_VALIDITY * 1000))
						.signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET)
						.compact();
	}
	
	/**
	 * Generate refresh token.
	 *
	 * @param idToken the id token
	 * @return the string
	 */
	public String generateRefreshToken(String idToken) {
		Map<String, Object> claims = new HashMap<>();
		claims.put(SecurityConstants.USER_NAME, idToken);
		return doGenerateRefreshToken(claims, idToken);
	}
	
	/**
	 * Do generate refresh token.
	 *
	 * @param claims the claims
	 * @param subject the subject
	 * @return the string
	 */
	
	private String doGenerateRefreshToken(Map<String, Object> claims, String subject) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.JWT_REFRESH_TOKEN_VALIDITY * 1000))
				.signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET).compact();
	}
	
	/**
	 * Validate jwt token.
	 *
	 * @param token the token
	 * @param idToken the id token
	 * @return the boolean
	 */
	public Boolean validateJwtToken(String token, String idToken) {
		final String idTokenFromJwt = getIDTokenFromJwtToken(token);
		return (idTokenFromJwt.equals(idToken) && !isTokenExpired(token));
	}
}
