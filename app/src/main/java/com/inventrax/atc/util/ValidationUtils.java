package com.inventrax.atc.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Validation Utils
 */
public class ValidationUtils {

    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[_A-Za-z0-9\\+-]+(\\.[_A-Za-z0-9\\+-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z]{2,}){1}$)");

    /**
     * Validate the email address
     *
     * @param value The email address to validate
     * @return True if the value is a valid email address
     */
    public static boolean isValidEmail(String value) {
        return ValidationUtils.match(value, ValidationUtils.EMAIL_PATTERN);
    }

    private static boolean match(String value, Pattern pattern) {
        return StringUtils.isNotEmpty(value) && pattern.matcher(value).matches();
    }

    /**
     * Validate the URL
     *
     * @param value The URL to validate
     * @return True if the value is a valid URL
     */
    public static boolean isValidURL(String value) {
        try {
            new URL(value);
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    /**
     * Validate the URL without protocol
     *
     * @param value The URL to validate
     * @return True if the value is a valid URL
     */
    public static boolean isValidURLWithoutProtocol(String value) {
        return isValidURL("http://" + value);
    }

    public static boolean isValidDouble(String number) {
        try {
            Double.parseDouble(number);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static boolean isString(String inputText) {

        return Pattern.matches("[A-Za-z]+", inputText);
    }

    public static boolean isInteger(String inputText) {
        return Pattern.matches("[0-9]+", inputText);
    }

    public static boolean isDigit(String inputText) {
        return Pattern.matches("[0-9]+", inputText);
    }

    public static boolean isCharecter(String inputText) {
        return Pattern.matches("[A-Za-z]", inputText);
    }

    public static boolean isMobileNumber(String inputText) {
        return Pattern.matches("\\d{10}", inputText);
    }

    public static boolean isPincode(String inputText) {
        return Pattern.matches("\\d{6}", inputText);
    }

    public static boolean validatePhoneNumber(String phoneNo) {
        //validate phone numbers of format "1234567890"
        if (phoneNo.matches("\\d{10}")) return true;
            //validating phone number with -, . or spaces
        else if(phoneNo.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}")) return true;
            //validating phone number with extension length from 3 to 5
        else if(phoneNo.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}")) return true;
            //validating phone number where area code is in braces ()
        else if(phoneNo.matches("\\(\\d{3}\\)-\\d{3}-\\d{4}")) return true;
            //return false if nothing matches the input
        else return false;

    }

}
