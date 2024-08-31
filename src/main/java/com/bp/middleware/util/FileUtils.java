package com.bp.middleware.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FileUtils {

	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

	private static Random random = new SecureRandom();

	public static String getRandomOTPnumber(int length) {
		String output = "";
		try {
			final String ALPHA_NUM = "0123456789";
			StringBuilder sb = new StringBuilder(10);
			for (int i = 0; i < length; i++) {

				int ndx = random.nextInt(ALPHA_NUM.length());
				sb.append(ALPHA_NUM.charAt(ndx));
			}
			output = sb.toString();
		}

		catch (Exception e1) {

			logger.info(AppConstants.TECHNICAL_ERROR, e1);
		}

		return output;
	}
	
	
	public static String getReceiptName(int length) {
		String output = "";
		try {
			final String ALPHA_NUM = "ABCD1234EFGH5678IJKLMNOP9765QRSTUVWXYZ";
			StringBuilder sb = new StringBuilder(10);
			for (int i = 0; i < length; i++) {

				int ndx = random.nextInt(ALPHA_NUM.length());
				sb.append(ALPHA_NUM.charAt(ndx));
			}
			output = "Rec-"+sb.toString();
		}

		catch (Exception e1) {

			logger.info(AppConstants.TECHNICAL_ERROR, e1);
		}

		return output;
	}
	
	

	public static String getRandomString() {
		String output = "";
		try {
			final String ALPHA_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyhhmmss");
			String formattedDate = sdf.format(date);
			StringBuilder sb = new StringBuilder(10);
			for (int i = 0; i < 10; i++) {
				int ndx = random.nextInt(ALPHA_NUM.length());
				sb.append(ALPHA_NUM.charAt(ndx));
			}
			output = sb.toString() + formattedDate;
		}

		catch (Exception e1) {
			logger.info(AppConstants.TECHNICAL_ERROR, e1);

		}

		return output;
	}

	public String genrateFolderName(String toEncrypt) {
		String ret = "";

		for (int i = 0; i < toEncrypt.length(); i++) {
			ret += ((int) toEncrypt.charAt(i));
		}

		return ret;
	}

	public static String consentKey() {

		String name = "FA-CHECK995e006047";
		String key = PasswordUtils.getSalt(10);
		String uniqueKey = name + key;
		return uniqueKey;
	}

	public static String getRandomOrderNumer() {
		String output = "";
		try {
			final String ALPHA_NUM = "0123456789";
			StringBuilder sb = new StringBuilder(10);
			for (int i = 0; i < 5; i++) {
				int ndx = random.nextInt(ALPHA_NUM.length());
				sb.append(ALPHA_NUM.charAt(ndx));
			}
			output = sb.toString();
		}

		catch (Exception e1) {
			logger.info(AppConstants.TECHNICAL_ERROR, e1);

		}

		return output;
	}

	public static String getRandomAlphaNumericString() {
		String output = "";
		try {
			final String ALPHA_NUM = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			StringBuilder sb = new StringBuilder(10);
			for (int i = 0; i < 10; i++) {
				int ndx = random.nextInt(ALPHA_NUM.length());
				sb.append(ALPHA_NUM.charAt(ndx));
			}
			output = sb.toString();
		}

		catch (Exception e1) {
			logger.info(AppConstants.TECHNICAL_ERROR, e1);

		}

		return output;
	}

	public static String documentKey() {

		String name = "652fb525266ab6ad0d0";
		String key = PasswordUtils.getSalt(6);
		String uniqueKey = name + key;
		return uniqueKey;
	}

	public static String getIpAddress() throws UnknownHostException {
		InetAddress inetAddress = InetAddress.getLocalHost();
		String ipAddress = inetAddress.getHostAddress();
		return ipAddress;
	}

	public static String applicationIdGeneration(String enterpriseName) {

		String lowerCaseName = enterpriseName.toLowerCase();
		String withoutSpace = lowerCaseName.replaceAll("\\s", "");

		String applicationId = withoutSpace + "_ekyc";

		return applicationId;
	}

	public static String apiKeyGeneration() {

		String numbers = "0123456789";
		String letters = "abcdefghijklmnopqrstuvwxyz";
		int length = 32;

		StringBuilder randomString = new StringBuilder(length);
		SecureRandom secureRandom = new SecureRandom();

		for (int i = 0; i < 3; i++) {
			int randomIndex = secureRandom.nextInt(numbers.length());
			randomString.append(numbers.charAt(randomIndex));
		}

		for (int i = 3; i < length - 3; i++) {
			int randomIndex = secureRandom.nextInt(numbers.length() + letters.length());
			if (randomIndex < numbers.length()) {
				// Append a number
				randomString.append(numbers.charAt(randomIndex));
			} else {
				// Append a letter
				randomString.append(letters.charAt(randomIndex - numbers.length()));
			}
		}

		for (int i = length - 3; i < length; i++) {
			int randomIndex = secureRandom.nextInt(letters.length());
			randomString.append(letters.charAt(randomIndex));
		}

		return randomString.toString();
	}

	public static String stringTolocalDateStructure(String dateInString) {

		String localDateStructure = "";

		char[] givenString = dateInString.toCharArray();

		for (int i = 0; i < 10; i++) {

			localDateStructure += givenString[i];
		}

		System.out.println("LOC DATE STRUC : " + localDateStructure);

		return localDateStructure;
	}

	public static String removeExtension(String fname) {

		int pos = fname.lastIndexOf('.');
		if (pos > -1)
			return fname.substring(0, pos);
		else
			return fname;
	}
	
	
	
    public static String generateApiKeys(int length) {
    	
    	 String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
         SecureRandom secureRandom = new SecureRandom();

    	
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(index));
        }
        return builder.toString();
    }

	
    public static String generateSignerReference(int length) {
    	
   	 String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
        SecureRandom secureRandom = new SecureRandom();

   	
       StringBuilder builder = new StringBuilder();
       for (int i = 0; i < length; i++) {
           int index = secureRandom.nextInt(ALPHA_NUMERIC_STRING.length());
           builder.append(ALPHA_NUMERIC_STRING.charAt(index));
       }
       return builder.toString();
   }

    
    public static String stringSplitter(String word,int startIndex) throws Exception{
    	
    	String splitter = "";
    	
    	if(word.length()>startIndex) {
    		
    		return splitter = word.substring(startIndex);
    	}else {
    		return splitter;
    	}
    }


	public static String getFirstFourChar(String word) throws Exception{
		
		String firstFour = "";
		
		if(word.length()>4) {
			
			char [] a = word.toCharArray();
			
			for(int i=0;i<=3;i++) {
				
				firstFour+=a[i];
			}
		}
		
		return firstFour;
	}
    
	
	
	public double twoDecimelDouble(double value) {
		
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(2, RoundingMode.HALF_UP); // Rounds to 2 decimal places

		double roundedValue = bd.doubleValue();
		
		return roundedValue;
	}
	
}
