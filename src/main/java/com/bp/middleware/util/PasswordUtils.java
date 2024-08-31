package com.bp.middleware.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

public class PasswordUtils {
	
	private PasswordUtils() {
	}

	private static final Random RANDOM = new SecureRandom();
    private static final String ALPHABET = "BASISPAY9876543210abcdefghijklmnopqrstuvwxyz";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    
    public static String getSalt(int length) {
        StringBuilder returnValue = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return new String(returnValue);
    }
    
    public static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }
    public static String generateSecurePassword(String password, String salt) {
        String returnValue = null;
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
        returnValue = Base64.getEncoder().encodeToString(securePassword);
        

        return returnValue;
        
    }
    
    public static boolean verifyUserPassword(String providedPassword,String securedPassword, String salt)
    {
        boolean returnValue = false;
        // Generate New secure password with the same salt
        String newSecurePassword = generateSecurePassword(providedPassword, salt);
        
        // Check if two passwords are equal
        returnValue = newSecurePassword.equalsIgnoreCase(securedPassword);
        return returnValue;
    }
    
    public static String getUserPassword(String providedPassword,String salt)
    {
        String newSecurePassword = null;
        newSecurePassword = generateSecurePassword(providedPassword, salt);
        return newSecurePassword;
    }
    
    
    
    
    public static String encrypt(String data, String key) throws Exception {
//    	String hashCode=getHashCodeFromString(key);
//    	System.out.println("Encrypt Hash Code Data = "+hashCode);
    	SecretKeySpec keySpec=new SecretKeySpec(key.getBytes(), "AES");
    	Cipher cipher=Cipher.getInstance("AES");
    	cipher.init(cipher.ENCRYPT_MODE, keySpec);
    	byte[] encryptedBytes=cipher.doFinal(data.getBytes());
    	
		return Base64.getEncoder().encodeToString(encryptedBytes);
    	
    }
    public static String decrypt(String encryptData, String key) throws Exception {
//    	String hashCode=getHashCodeFromString(key);
//    	System.out.println("Decrypt Hash Code Data = "+hashCode);
    	SecretKeySpec keySpec=new SecretKeySpec(key.getBytes(), "AES");
    	Cipher cipher=Cipher.getInstance("AES");
    	cipher.init(cipher.DECRYPT_MODE, keySpec);
    	byte[] decodedBytes=Base64.getDecoder().decode(encryptData);
    	byte[] decryptedBytes=cipher.doFinal(decodedBytes);
    	
		return new String(decryptedBytes);
    }

    
    
//    private static String getHashCodeFromString(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
//
//		MessageDigest md = MessageDigest.getInstance("SHA-512");
//		md.update(str.getBytes("UTF-8"));
//		byte byteData[] = md.digest();
//
//		//convert the byte to hex format method 1
//		StringBuffer hashCodeBuffer = new StringBuffer();
//		for (int i = 0; i < byteData.length; i++) {
//			hashCodeBuffer.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
//		}
//		return hashCodeBuffer.toString().toUpperCase();
//	}
    
    public static String encryptWithHashSalt(String data , String salt) throws Exception {
    	
    	//Hash the salt key
    	byte[] hashedSaltKey=getHashSaltKey(salt);
    	System.out.println("Encryption Hash ="+new String(hashedSaltKey) );
    	
    	//Generate a random initialization vector (IV)
    	SecureRandom random=new SecureRandom();
    	byte[] iv =new byte[16];
    	random.nextBytes(iv);
    	System.out.println(new String(iv));
    	//Create the AES cipher object
    	SecretKeySpec keySpec=new SecretKeySpec(hashedSaltKey, "AES");
    	Cipher cipher= Cipher.getInstance("AES/CBC/PKCS5Padding");
    	cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
    	
    	//Encrypt the data
    	byte[] encryptedData=cipher.doFinal(data.getBytes("UTF-8"));
    	
    	//Combine the IV and encrypted data
    	byte[] combined =new byte[iv.length + encryptedData.length];
    	System.arraycopy(iv, 0, combined, 0, iv.length);
    	System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
    	
    	//Return the Base64-encoded encrypted data
    	return Base64.getEncoder().encodeToString(combined);
    	    	
    }
    
    public static String decryptWithHashSalt(String encryptedData,String salt) throws Exception {
    	
    	//Hash the salt key
    	byte[] hashedSaltKey=getHashSaltKey(salt);
    	System.out.println("Decryption Hash ="+ hashedSaltKey);
    	
    	//Decode the Base64-encode encrypted data
    	byte[] combined =Base64.getDecoder().decode(encryptedData);
    	
    	//Extract the IV from the combined data
    	byte[] iv=new byte[16];
    	System.arraycopy(combined, 0, iv, 0, iv.length);
    	System.out.println(" DECRYPTION IV = "+iv);
    	
    	//Create the AES cipher object
    	SecretKeySpec secretKeySpec=new SecretKeySpec(hashedSaltKey, "AES");
    	Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
    	cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
    	
    	//Decrypt the data
    	byte[] decryptedData=cipher.doFinal(combined,iv.length,combined.length-iv.length);
    	
    	//Return the decrypted data as a String
		return new String(decryptedData,"UTF-8");
    }
    
    
    public static byte[] getHashSaltKey(String salt) throws NoSuchAlgorithmException {
    	MessageDigest digest=MessageDigest.getInstance("SHA-256");
    	return digest.digest(salt.getBytes(StandardCharsets.UTF_8));
    }
    
    
//  public static String encryptionWithHashSalt(String data , String salt) throws Exception {
//    	
//    	//Hash the salt key
//    	String hashedSalt=getHashCodeFromString(salt);
//    	byte[] hashedSaltKey=hashedSalt.getBytes(StandardCharsets.UTF_8);
//    	System.out.println("Encryption Hash ="+ hashedSaltKey);
//    	
//    	//Generate a random initialization vector (IV)
//    	SecureRandom random=new SecureRandom();
//    	byte[] iv =new byte[16];
//    	random.nextBytes(iv);
//    	System.out.println(" ENCRYPTION IV = "+iv);
//    	//Create the AES cipher object
//    	SecretKeySpec keySpec=new SecretKeySpec(hashedSaltKey, "AES");
//    	Cipher cipher= Cipher.getInstance("AES/CTR/NoPadding");
//    	cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
//    	
//    	//Encrypt the data
//    	byte[] encryptedData=cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
//    	
//    	//Combine the IV and encrypted data
////    	byte[] combined =new byte[iv.length + encryptedData.length];
////    	System.arraycopy(iv, 0, combined, 0, iv.length);
////    	System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
//    	
//    	//Return the Base64-encoded encrypted data
//    	return Base64.getEncoder().encodeToString(encryptedData);
//    	    	
//    }
//    
//    public static String decryptionWithHashSalt(String encryptedData,String salt) throws Exception {
//    	
//    	//Hash the salt key
//    	String hashedSalt=getHashCodeFromString(salt);
//    	byte[] hashedSaltKey=hashedSalt.getBytes(StandardCharsets.UTF_8);
//    	System.out.println("Decryption Hash ="+ hashedSaltKey);
//    	
//    	//Decode the Base64-encode encrypted data
//    	byte[] combined =Base64.getDecoder().decode(encryptedData);
//    	
//    	//Extract the IV from the combined data
//    	byte[] iv=new byte[16];
//    	System.arraycopy(combined, 0, iv, 0, iv.length);
//    	System.out.println(" DECRYPTION IV = "+iv);
//    	
//    	//Create the AES cipher object
//    	SecretKeySpec secretKeySpec=new SecretKeySpec(hashedSaltKey, "AES");
//    	Cipher cipher=Cipher.getInstance("AES/CTR/NoPadding");
//    	cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
//    	
//    	//Decrypt the data
//    	byte[] decryptedData=cipher.doFinal(combined,iv.length,combined.length-iv.length);
//    	
//    	//Return the decrypted data as a String
//		return new String(decryptedData,"UTF-8");
//    }
//    private static final string ALGORITHM ="AES";
//    private static final string TRANSFORMATION ="AES/ECB/PKCS5Padding";
//    private static final string HASH_ALGORITHM ="SHA-256";
    
    public static byte[] generateHash(byte[] salt) throws Exception {
    	MessageDigest messageDigest=MessageDigest.getInstance("SHA-256");
		return messageDigest.digest(salt);
    	
    }
    public static byte[] generateSalt() {
    	byte[] salt =new byte[16];
    	SecureRandom random=new SecureRandom();
    	random.nextBytes(salt);
    	return salt;
    }

	public static String Encryption(String panCard) throws Exception {
		byte[] panBytes=panCard.getBytes();
		byte[] salt=generateSalt();
		System.out.println("Salt = "+ Arrays.toString(salt) );
		
		byte[] hashedSalt=generateHash(salt);
//		System.out.println("Secure Date = "+ hashedSalt.toString() );
		System.out.println(new String(hashedSalt));
		
		SecretKeySpec secretKeySpec=new SecretKeySpec(hashedSalt, "AES");
		Cipher cipher=Cipher.getInstance(AppConstants.AES_ECB_PADDING5);
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		byte[] encryptedData=cipher.doFinal(panBytes);
		String encrypt=Base64.getEncoder().encodeToString(encryptedData);
		return encrypt;
	}
	
	
	//My File
	public static String decryptString(String encryptedString, String key) throws Exception {
	    // Decode the Base64 encoded encrypted data
	    byte[] encryptedData = Base64.getDecoder().decode(encryptedString);

	    // Convert the key to bytes
	    byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
	    SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

	    // Create the cipher instance for AES with ECB mode and PKCS7 padding
	    Cipher cipher = Cipher.getInstance(AppConstants.AES_ECB_PADDING5);
	    cipher.init(Cipher.DECRYPT_MODE, secretKey);

	    // Decrypt the data
	    byte[] decryptedData = cipher.doFinal(encryptedData);

	    // Remove the PKCS7 padding
	    byte[] unpaddedData = unpadData(decryptedData);

	    // Convert the decrypted bytes back to a string
	    return new String(decryptedData, StandardCharsets.UTF_8);
	}
	private static byte[] unpadData(byte[] data) {
	    int paddingSize = data[data.length - 1];
	    byte[] unpaddedData = new byte[data.length - paddingSize];
	    System.arraycopy(data, 0, unpaddedData, 0, unpaddedData.length);
	    return unpaddedData;
	}
	
	public static String demoEncryptionECB(JSONObject jsonObject) throws Exception {
		
	    // Convert the JSON object to a string
	    String jsonString = jsonObject.toString();
	    // Get the UTF-8 encoded bytes of the JSON string
	    byte[] data = jsonString.getBytes(StandardCharsets.UTF_8);
		String key=AppConstants.ENCRYPTION_KEY;
		byte[] keyByte=key.getBytes(StandardCharsets.UTF_8);
		SecretKeySpec secretKeySpec=new SecretKeySpec(keyByte, "AES");
		Cipher cipher=Cipher.getInstance(AppConstants.AES_ECB_PADDING5);
		cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
		byte[] encryptedByte=cipher.doFinal(data);
		return Base64.getEncoder().encodeToString(encryptedByte);
		
	}
	
	public static String demoImageEncryption(MultipartFile image) throws Exception {
		
		byte[] fileContent = image.getBytes();
		String base64Image=Base64.getEncoder().encodeToString(fileContent);
		return base64Image;
	}
	
	
public static String demoEncryption(JSONObject jsonObject,String encryptionKey) throws Exception {
		
	    // Convert the JSON object to a string
	    String jsonString = jsonObject.toString();
	    // Get the UTF-8 encoded bytes of the JSON string
	    byte[] data = jsonString.getBytes(StandardCharsets.UTF_8);
	    
		String key=encryptionKey;
		byte[] keyByte=key.getBytes(StandardCharsets.UTF_8);
		
		SecretKeySpec secretKeySpec=new SecretKeySpec(keyByte, "AES");
		Cipher cipher=Cipher.getInstance(AppConstants.AES_ECB_PADDING5);
		cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
		byte[] encryptedByte=cipher.doFinal(data);
		
		return Base64.getEncoder().encodeToString(encryptedByte);
		
	}

public static String demoDecrypt(String encryptedString, String key) throws Exception {
	
    // Decode the Base64 encoded encrypted data
    byte[] encryptedData = Base64.getDecoder().decode(encryptedString);

    // Convert the key to bytes
    byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
    SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

    // Create the cipher instance for AES with ECB mode and PKCS7 padding
    Cipher cipher = Cipher.getInstance(AppConstants.AES_ECB_PADDING5);
    cipher.init(Cipher.DECRYPT_MODE, secretKey);

    // Decrypt the data
    byte[] decryptedData = cipher.doFinal(encryptedData);

    // Convert the decrypted bytes back to a string
    return new String(decryptedData, StandardCharsets.UTF_8);
}


}
