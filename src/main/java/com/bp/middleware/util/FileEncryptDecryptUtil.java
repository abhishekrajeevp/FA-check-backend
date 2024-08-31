package com.bp.middleware.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;


public class FileEncryptDecryptUtil {
	
	//CBC & ECB
	private static final String SECRET_KEY_ALGORITHM="PBKDF2WithHmacSHA256";
	private static final String CIPHER_ALGORITHM ="AES/CBC/PKCS5Padding";
	private static final int KEY_LENGTH=256;
	private static final int ITERATIONS=65536;
	private static final int SALT_LENGTH=16;
	private static final int IV_LENGTH=16;
	
	//BASE64 ENCODER
	private static final String ALGORITHM="AES";
	public static final String KEY = "99ik10b9ff5tvj9nlI9gscyd2c6r6B3A";
	
	private static SecretKeySpec generateSecretKey(char[] password,byte[] salt)throws Exception
	{
		SecretKeyFactory factory =SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
		KeySpec spec=new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
		SecretKey tmp=factory.generateSecret(spec);
		return new SecretKeySpec(tmp.getEncoded(),"AES");
	}
	
    //Encryption using CBC (Cipher Block Chaining)
	public static byte[] encrypt(MultipartFile file, char[] password) throws Exception{
		InputStream inputStream=file.getInputStream();
		byte[] salt =new byte[SALT_LENGTH];
		byte[] iv =new byte[IV_LENGTH];
		Cipher cipher;
		byte[] encrypted;
		
		try {
			inputStream.read(salt);
			inputStream.read(iv);
			
			SecretKeySpec secretKey = generateSecretKey(password, salt);
			cipher=Cipher.getInstance(CIPHER_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey,new IvParameterSpec(iv));
			
			encrypted = new byte[inputStream.available()];
			int bytesRead=inputStream.read(encrypted);
			
			//To handle the situation where MultiPartFile can be empty
			if (bytesRead == -1) {
				throw new IOException("Empty multipartfile content");
			}
			
			//Encrypt File content
			encrypted =cipher.doFinal(encrypted);
		} finally {
			inputStream.close();
		}
		return encrypted;
	}

	
	//ENCRYPTION USING ECB CIPHER ALGORITHM
	public static byte[] encryptImage(MultipartFile pic,String key) throws Exception{
		byte[] imageData= pic.getBytes();//To read image data from MultiPartFile
		
		//Generate SecretKey
		SecretKeyFactory factory = SecretKeyFactory.getInstance("AES");
     	SecretKey secretKey=new SecretKeySpec(key.getBytes(), "AES");
		
	    Cipher cipher =Cipher.getInstance(AppConstants.AES_ECB_PADDING5);
	    cipher.init(Cipher.ENCRYPT_MODE, secretKey);
	    
	    byte[] encryptedImageData=cipher.doFinal(imageData);
		return encryptedImageData;
	}
	
	
	//ENCRYPTION USING BASE64 ENCODER
	public static String encryptImageToBase64(MultipartFile file,String key)throws Exception {
		byte[] imageBytes=file.getBytes();
		byte[] salt=new byte[16];
		new SecureRandom().nextBytes(salt);
		
		SecretKeySpec secretkey=generateSecretKey(key.toCharArray(), salt);
			
		Cipher cipher=Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretkey);
		
		byte[] encryptedBytes = cipher.doFinal(imageBytes);
		byte[] encryptedDataWithSalt=new byte[salt.length+encryptedBytes.length];
		
		System.arraycopy(salt, 0, encryptedDataWithSalt, 0, salt.length);
		System.arraycopy(encryptedBytes, 0, encryptedDataWithSalt, salt.length, encryptedBytes.length);
		
		String encryptedBase64=Base64.getEncoder().encodeToString(encryptedDataWithSalt);
		
		return encryptedBase64;
	}

	public static String ecbPaddingBase64(MultipartFile image,String key){
		
        try {
               byte [] encryptKey=key.getBytes();
               byte[] imageData= image.getBytes();
               
           StringBuilder combinedBase64 = new StringBuilder();
           byte[] encryptedImage=encryptImage(imageData, encryptKey);
           
           combinedBase64.append(Base64.getEncoder().encode(encryptedImage));
               
           String str=combinedBase64.toString();
           System.out.println(str);
     

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

	

  
	private static byte[] encryptImage(byte[] imageBytes,byte [] encryptionKey)throws Exception{
		SecretKeySpec secretKeySpec =new SecretKeySpec(encryptionKey, ALGORITHM);
		Cipher cipher=Cipher.getInstance(AppConstants.AES_ECB_PADDING5);
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		return cipher.doFinal(imageBytes);
	}
	
	
//	 public static String jweEncryption(RequestModel model) throws Exception {
//		 ResponseStructure structure=new ResponseStructure();
//	       
//		 // Generate RSA key pair
//	        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//	        keyPairGenerator.initialize(2048);
//	        KeyPair keyPair = keyPairGenerator.generateKeyPair();
//	        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
//	        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
//
//	        // Generate a random 256-bit key for AES-GCM
//	        byte[] aesKeyBytes = new byte[32];
//	        SecureRandom secureRandom = new SecureRandom();
//	        secureRandom.nextBytes(aesKeyBytes);
//
//	        // Create a JSON payload
//	        JSONObject payloadJSON = new JSONObject();
//	        payloadJSON.put("source",model.getSource());
//	        payloadJSON.put("userId",model.getUserId());
//	       
//	        //The RSA-OAEP-256 algorithm is not supported by the JWE encrypter: Supported algorithms: [A256GCMKW, A256KW]
//	        // Create JWE header
//	        JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.A256GCMKW, EncryptionMethod.A256GCM).contentType("JWT").build();
//
//	        // Create JWE object
//	        JWEObject jweObject = new JWEObject(header, new Payload(payloadJSON.toString()));
//
//	        // Create an AES-GCM encrypter with the generated key
//	        AESEncrypter encrypter = new AESEncrypter(aesKeyBytes);
//
//	        // Encrypt the JWE payload
//	        jweObject.encrypt(encrypter);
//
//	        // Serialize the JWE to a compact form
//	        String jweString = jweObject.serialize();
//
//	        System.out.println("\nJWE: " + jweString);
//
//	        // Now let's decrypt the JWE
//	        JWEObject decryptedJWEObject = JWEObject.parse(jweString);
//
//        // Create an AES-GCM decrypter with the generated key
//            AESDecrypter decrypter = new AESDecrypter(aesKeyBytes);
//	        // Decrypt the JWE payload
//	        decryptedJWEObject.decrypt(decrypter);
//	        // Print the decrypted payload (as a JSON string)
//	        System.out.println("\nDecrypted Payload: " + decryptedJWEObject.getPayload().toString());
//	        
//	        return decryptedJWEObject.getPayload().toString();
//	    }

	 
	 
	 
	 
	   public static String encryptionJwe(String jsonString)throws Exception {
		   try {
			
			   // Create a secret key for AES-GCM
		        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		        keyGenerator.init(256);
		        SecretKey secretKey = keyGenerator.generateKey();
		        
		        byte[] secretKeyByte =secretKey.getEncoded();
		        
		        String base64key=Base64.getEncoder().encodeToString(secretKeyByte);
		        
//		        String key=secretKey.toString();
//		        byte[] privateKey= key.getBytes();
		        
		       
		        
		        

		        // Create JSON object (replace with your actual JSON data)
		       // String jsonStr = "{\"key\":\"value\"}";

		        // Convert JSON to bytes
		        byte[] jsonData = jsonString.getBytes();

		        // Create AES-GCM cipher instance
		        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

		        // Generate IV and parameters
		        byte[] iv = new byte[12]; // 96 bits IV for GCM
		        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

		        // Initialize AES-GCM cipher with encryption mode and parameters
		        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

		        // Encrypt the JSON data
		        byte[] encryptedData = cipher.doFinal(jsonData);

		        // Convert the encrypted data to Base64 for easy transmission
		        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData);

		        System.out.println("Encrypted Base64: " + encryptedBase64);

		        // Simulating RS256 signing using a private key (usually a private key associated with a certificate)
		        String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n" +
		        		base64key+
		                "...\n" +
		                "-----END PRIVATE KEY-----";

		        System.out.println("PRIVATE KEY PEM 1 : "+privateKeyPEM);
		        // Remove line breaks and parse private key
		        privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "")
		                .replaceAll("\\s", "");

		        System.out.println("PRIVATE KEY PEM 2 : "+privateKeyPEM);
		        
		        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM);//,arg[0].trim()
		        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
		        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		        PrivateKey privateKeyInbuild = keyFactory.generatePrivate(keySpec);

		        System.out.println("privateKeyInbuild : "+privateKeyInbuild);
		        // Perform RS256 signing using the private key
		        Signature signature = Signature.getInstance("SHA256withRSA");
		        signature.initSign(privateKeyInbuild);
		        signature.update(encryptedData);
		        byte[] signatureBytes = signature.sign();

		        // Convert the signature to Base64 for easy transmission
		        String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);

		        System.out.println("RS256 Signature Base64: " + signatureBase64);
		        
		        return null;
			   
		} catch (Exception e) {
			e.printStackTrace();
		}
		   return null;
		 
	    }
	   
	   
		

		public static String listToJsonString(List<?> data) throws Exception {

			Gson gson = new Gson();

			String jsonString = gson.toJson(data);

			return encrypt(jsonString);
		}

		public static String encrypt(String data) throws Exception {

			SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] encryptedBytes = cipher.doFinal(data.getBytes());

			return Base64.getEncoder().encodeToString(encryptedBytes);
		}

		public static String decrypt(String encryptedData) throws Exception {

			SecretKey secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));

			return new String(decryptedBytes);
		}

		public static String decryptWithCrypto(String encryptedValue) throws Exception {
			// Decode Base64 encoded encrypted value

			String secretKey = KEY;

			byte[] decodedValue = Base64.getDecoder().decode(encryptedValue);

			// Split IV and encrypted data
			byte[] iv = new byte[16];
			byte[] encryptedData = new byte[decodedValue.length - 16];
			System.arraycopy(decodedValue, 0, iv, 0, 16);
			System.arraycopy(decodedValue, 16, encryptedData, 0, encryptedData.length);

			// Derive key from password
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), iv, 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

			// Decrypt the data
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, secret, ivSpec);
			byte[] decrypted = cipher.doFinal(encryptedData);

			// Convert decrypted bytes to string
			return new String(decrypted, StandardCharsets.UTF_8);
		}

		public static String decryptWithCryptoTwo(String encryptedValue) throws Exception {
			
			if(encryptedValue!=null) {
			
			String secretKey = KEY;
			
			// Decode the base64 encoded encrypted value
			byte[] decodedValue = Base64.getDecoder().decode(encryptedValue);

			// Create the AES cipher
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

			// Initialize the cipher in decryption mode with the secret key
			SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

			// Decrypt the decoded value
			byte[] decryptedBytes = cipher.doFinal(decodedValue);

			// Convert the decrypted bytes to a string (assuming UTF-8 encoding)
			String decryptedValue = new String(decryptedBytes, "UTF-8");

			// Print or use the decrypted value
			System.out.println("Decrypted value: " + decryptedValue);
			
			return decryptedValue;
			
			}else {
				
				return "";
			}
		}
	   
	   
	   
	 
}
