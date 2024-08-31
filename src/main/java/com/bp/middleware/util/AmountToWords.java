package com.bp.middleware.util;

public class AmountToWords {

    private static final String[] unitsInt = {"", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
    private static final String[] teensInt = {"", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
    private static final String[] tensInt = {"", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};

    
    private static final String[] ones = {
            "", "one", "two", "three", "four",
            "five", "six", "seven", "eight", "nine"
    };

    private static final String[] tens = {
            "", "ten", "twenty", "thirty", "forty",
            "fifty", "sixty", "seventy", "eighty", "ninety"
    };

    private static final String[] teens = {
            "ten", "eleven", "twelve", "thirteen", "fourteen",
            "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
    };

    private static final String[] thousands = {
            "", "thousand", "million", "billion", "trillion"
    };
    
    
    
    public static String convertAmountToWords(int amount) {
        if (amount == 0) {
            return "zero";
        }

        String words = "";

        if (amount < 0) {
            words += "minus ";
            amount = -amount;
        }

        if ((amount / 1000000) > 0) {
            words += convertAmountToWords(amount / 1000000) + " million ";
            amount %= 1000000;
        }

        if ((amount / 1000) > 0) {
            words += convertAmountToWords(amount / 1000) + " thousand ";
            amount %= 1000;
        }

        if ((amount / 100) > 0) {
            words += convertAmountToWords(amount / 100) + " hundred ";
            amount %= 100;
        }

        if (amount > 0) {
            if (amount < 10) {
                words += unitsInt[amount];
            } else if (amount < 20) {
                words += teensInt[amount - 10];
            } else {
                words += tensInt[amount / 10] + " " + unitsInt[amount % 10];
            }
        }

        return words.trim();
    }
    
    
    
    
    
    
    
    
    public static String convertAmountToWords(double amount) {
    	
        long rupees = (long) amount;
        int paise = (int) Math.round((amount - rupees) * 100); // Convert decimal to paise

        String rupeesInWords = convertToWords(rupees);
        String paiseInWords = convertTwoDigitNumber(paise);

        String currency = "Rupees"; // Currency name

        // Handling singular and plural
        if (rupees == 1) {
            currency = "Rupee";
        }

        // Combine rupees and paise into a single string
        String amountInWords = rupeesInWords + " " + currency + " ";
        if (paise != 0) {
            amountInWords += "and " + paiseInWords + " Paise";
        }

        return amountInWords.trim();
    }
    
    
    private static String convertTwoDigitNumber(int num) {
        if (num == 0) {
            return "";
        } else if (num < 10) {
            return ones[num];
        } else if (num < 20) {
            return teens[num - 10];
        } else {
            return tens[num / 10] + " " + ones[num % 10];
        }
    }
    
    
    
    private static String convertToWords(long num) {
        if (num == 0) {
            return "Zero";
        }

        String words = "";
        int index = 0;
        do {
            long n = num % 1000;
            if (n != 0){
                String s = convertThreeDigitNumber((int)n);
                words = s + " " + thousands[index] + " " + words;
            }
            index++;
            num /= 1000;
        } while (num > 0);

        return words.trim();
    }
    
    
    private static String convertThreeDigitNumber(int num) {
        if (num == 0) {
            return "";
        }
        String result = "";
        if (num >= 100) {
            result += ones[num / 100] + " hundred ";
            num %= 100;
        }
        result += convertTwoDigitNumber(num);
        return result.trim();
    }
    
    
}
