package com.bp.middleware.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.bp.middleware.responsestructure.ResponseStructure;

@RestController
@RequestMapping("/voicebox")
public class VoiceBoxCheck {

	@PostMapping("/merchantonboard")
	public ResponseStructure merchantOnboard() {
		try {
            // Secret key and IV generation
			// Convert the key to bytes
			String key="1w6bcshe0rwitrvx2l46aB41evSvrikc";
		    byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
		    SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            byte[] iv = generateIV();
            
            Timestamp instant= Timestamp.from(Instant.now());  
            String random=FileUtils.getRandomAlphaNumericString();
            // JSON object to be encrypted
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("reqRefNo", "12233");
            jsonObject.put("rrn", "123");
            jsonObject.put("tid", "100022");
            jsonObject.put("txnAmt", "100.58");
            jsonObject.put("txnTimestamp", "1701084802");
            jsonObject.put("timeStamp", 1701084802);
            jsonObject.put("deviceId", 100022);
//            jsonObject.put("meAdress2", "");
//            jsonObject.put("meAdress3", "");
//            jsonObject.put("meCity", "");
//            jsonObject.put("meState", "");
//            jsonObject.put("mePincode", "");
//            jsonObject.put("meMobileNo", "");
//            jsonObject.put("meEmail", "");
//            jsonObject.put("meContactName", "");
//            jsonObject.put("mpanVisa", "");
//            jsonObject.put("mpanMaster", "");
//            jsonObject.put("mpanRupay", "");
//            jsonObject.put("meVpa", "");
//            jsonObject.put("qrType", "");
//            jsonObject.put("qrString", "");
            

            // Convert JSON object to string
            String jsonString = jsonObject.toString();

            // Encrypt JSON string
            byte[] encryptedData = encrypt(jsonString, secretKey, iv);
            System.out.println("Encrypted: " + Base64.getEncoder().encodeToString(encryptedData));
            String encrypt=Base64.getEncoder().encodeToString(encryptedData);
            
            JSONObject json = new JSONObject();
            json.put("data", encrypt);
            
            
            RestTemplate restTemplate = new RestTemplate();

    		HttpHeaders headers = new HttpHeaders();
    		headers.setContentType(MediaType.APPLICATION_JSON);
    		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    		headers.add("Content-Type", AppConstants.CONTENT_TYPE);

    		HttpEntity<String> entity = new HttpEntity<>(json.toString(), headers);

    		ResponseEntity<String> clientResponse = restTemplate.postForEntity("https://backend.demo.cwdin.com/api/v1/bank/transactionWOMerchant", entity,
    				String.class);
    		String data = clientResponse.getBody();
    		System.out.println("Data :"+data);

    		JSONObject returnJson = new JSONObject(data);
    		
            // Decrypt and print JSON string
            String decryptedJsonString = decrypt(data.getBytes(), secretKey, iv);
            System.out.println("Decrypted: " + decryptedJsonString);

            // Convert decrypted string back to JSON object
            JSONObject decryptedJsonObject = new JSONObject(decryptedJsonString);
            System.out.println("Decrypted JSON object: " + decryptedJsonObject.toString(2));
        } catch (Exception e) {
            e.printStackTrace();
        }

		return null;
	}
	

    public static byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[12]; // GCM recommended size is 12 bytes
        random.nextBytes(iv);
        return iv;
    }

    public static byte[] encrypt(String plaintext, SecretKey secretKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        return cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    public static String decrypt(byte[] ciphertext, SecretKey secretKey, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        byte[] decryptedBytes = cipher.doFinal(ciphertext);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
	
	
	
	
	
	
	
}
