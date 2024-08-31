 package com.bp.middleware.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bp.middleware.responsestructure.GenericResponseDTO;
import com.bp.middleware.security.SecurityEnum;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component

public class JWTTokenFilter extends OncePerRequestFilter {

	/** The token provider. */
	@Autowired
	private JWTTokenProvider tokenProvider;

	/** The Constant CSRF_COOKIE_NAME. */
	public static final String CSRF_COOKIE_NAME = "XSRF-TOKEN";

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * Do filter internal.
	 *
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @param filterChain
	 *            the filter chain
	 * @throws ServletException
	 *             the servlet exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */


	protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String requestURI = request.getServletPath();
			System.out.println(requestURI);
			if (isAllowURLWithoutAuthentication(requestURI)) {
				filterChain.doFilter(request, response);
			} else {
				String jwtToken = request.getHeader(SecurityEnum.JWT_TOKEN.status());
				if (null == jwtToken || jwtToken.isEmpty()) {
					response = errorResponse(response,"AccessToken is not present in Request Header!",
							HttpStatus.NOT_ACCEPTABLE.value());
					return;
				}

				jwtToken = jwtToken.replace("Bearer ", "");
				String jwtIdToken = tokenProvider.getIDTokenFromJwtToken(jwtToken);

					if ((jwtIdToken != null && !jwtIdToken.isEmpty())
							&& tokenProvider.validateJwtToken(jwtToken, jwtIdToken)) {
							request.setAttribute("userName", jwtIdToken);
							filterChain.doFilter(request, response);
					} else {
						response = errorResponse(response,"Invalid AccessToken present in Request Header",
								HttpStatus.NOT_ACCEPTABLE.value());
						return;
					}
			}
		} catch (Exception e) {
			String message = "";
			String cause = e.toString();
			logger.error(String.format("JWTException:%s",cause));
			if (cause.contains("ExpiredJwtException")) {
				message = "Access Token Expired!";
				response = errorResponse(response,message, HttpStatus.NOT_ACCEPTABLE.value());
			} else if (cause.contains("SignatureException")) {
				message = "Invalid Access Token!";
				response = errorResponse(response,message, HttpStatus.NOT_ACCEPTABLE.value());
			} else if(cause.contains("MalformedJwtException")) {
				message = "Invalid Header Information!";
				response = errorResponse(response,message, HttpStatus.NOT_ACCEPTABLE.value());
			}
			else {
				message = "Something went wrong while accessing request!";
				response = errorResponse(response,message, HttpStatus.INTERNAL_SERVER_ERROR.value());
			}
		}
	}
	

	/**
	 * Error response.
	 *
	 * @param response
	 *            the response
	 * @param errorMessage
	 *            the error message
	 * @param errorCode
	 *            the error code
	 * @return the http servlet response
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private HttpServletResponse errorResponse(HttpServletResponse response, String errorMessage, int errorCode)
			throws IOException {

		GenericResponseDTO responseDTO = new GenericResponseDTO();
		responseDTO.setErrorMessage(errorMessage);
		responseDTO.setErrorCode(errorCode);
		response.setStatus(errorCode);
		ObjectMapper mapper = new ObjectMapper();
		response.getWriter().write(mapper.writeValueAsString(responseDTO));
		
		return response;
	}


	/**
	 * Gets the accessable URLS for the user.
	 *
	 * @param userName
	 *            the user name
	 * @return the accessable URLS for the user     
	 */


	public boolean isAllowURLWithoutAuthentication(String urlValue) {
		boolean authentication;
		if (urlValue.contains("/role") || urlValue.contains("/admin/addadmin") || urlValue.contains("/admin/createsuperadmin")|| urlValue.contains("/admin/adminlogin") ||
				urlValue.contains("/admin/emailverify") || urlValue.contains("/admin/otpverify") || urlValue.contains("/admin/resendotp")
				|| urlValue.contains("/admin/reset") || urlValue.contains("/user/create") || urlValue.contains("/user/login")
				|| urlValue.contains("/user/verifyEmail") || urlValue.contains("/user/otpverification") || urlValue.contains("/user/resendotp") || urlValue.contains("/user/reset")
				|| urlValue.contains("/ekyc") || urlValue.contains("/verification") || urlValue.contains("/technical") || urlValue.contains("/google")
				||urlValue.contains("role") ||  urlValue.contains("admin/addadmin") || urlValue.contains("admin/adminlogin")|| urlValue.contains("admin/emailverify")
				|| urlValue.contains("admin/otpverify")|| urlValue.contains("admin/resendotp")|| urlValue.contains("admin/reset")
				|| urlValue.contains("merchant/viewpdf")|| urlValue.contains("signer/signerbymerchant")|| urlValue.contains("signer/viewbyid")
				|| urlValue.contains("signer/otpverify")|| urlValue.contains("signer/otpsent")|| urlValue.contains("merchant/viewbyid") || urlValue.contains("/erroridentifier")
				|| urlValue.contains("payment/returnpageres") || urlValue.contains("/facheck-sprintdocumentation") || urlValue.contains("facheck-sureverification")
				|| urlValue.contains("/facheck-signauthentication")|| urlValue.contains("/facheck-authentication")|| urlValue.contains("/uploads") || urlValue.contains("/returns")||urlValue.contains("/admin/changepassword")
				||urlValue.contains("/user/changepassword")||urlValue.contains("facheck-ocr") || urlValue.contains("transaction/view")  || urlValue.contains("transaction/viewByTrackId")
				||urlValue.contains("conveniencefee/viewall") 
				||urlValue.contains("conveniencefee/viewby-mod") || urlValue.contains("/user/findBySalt")
				||urlValue.contains("/prepaidMonthlyInvoice/viewByUniqueId") || urlValue.contains("/prepaid/viewByUniqueId")
				||urlValue.contains("postpaid/viewByUniqueId") ||urlValue.contains("/receipt") || urlValue.contains("/postpaidInvoice") 
				){
			
			authentication=true;
			
		} else {
			authentication= false;
		}
		return authentication;
	}
}