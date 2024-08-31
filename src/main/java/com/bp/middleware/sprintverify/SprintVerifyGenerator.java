package com.bp.middleware.sprintverify;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.json.JSONObject;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

public class SprintVerifyGenerator {
	
	
	public static String sprintVerifyDocument(JSONObject obj) {
		
		String jwtToken = SprintVerifyGenerator.sprintVJwtToken();
		
        String apiEndpoint ="https://uat.paysprint.in/sprintverify-uat/api/v1/verification/mca_verify";

        HttpClient httpClient = HttpClient.newBuilder().build();

        String requestBody = obj.toString();

        HttpRequest request = HttpRequest.newBuilder()
        		.uri(URI.create(apiEndpoint))
				.header("accept", "application/json").header("Content-Type", "application/json")
				.header("Token", jwtToken).header("authorisedkey", "TVRJek5EVTJOelUwTnpKRFQxSlFNREF3TURFPQ==")
				.method("POST", HttpRequest.BodyPublishers
						.ofString(requestBody))
				.build();

        CompletableFuture<HttpResponse<String>> responseFuture =
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        HttpResponse<String> response=responseFuture.join();
        
        int statusCode=response.statusCode();
        String responseBody = response.body();
        
        System.out.println("Response Code: " + statusCode);
		
        return responseBody;
	}
	
	

	public static String sprintVJwtToken() {
		try {
			String secretKey = "UTA5U1VEQXdNREF4VFZSSmVrNUVWVEpPZWxVd1RuYzlQUT09";

			JWSSigner signer = new MACSigner(secretKey);

			Map<String, Object> map = new HashMap<>();
			map.put("timestamp", new Date());
			map.put("partnerId", "CORP00001");
			map.put("reqid", 12345);

			JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder().claim("timestamp", map.get("timestamp"))
					.claim("partnerId", map.get("partnerId")).claim("reqid", map.get("reqid")).build();

			SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaims);
			signedJwt.sign(signer);

			String jwtToken = signedJwt.serialize();
			System.err.println("JWT : " + jwtToken);

			return jwtToken;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
