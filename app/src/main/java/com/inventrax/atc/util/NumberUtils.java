package com.inventrax.atc.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;

public class NumberUtils {

    public static Float getFloat(String value) {
        return getFloat(value, null);
    }

    public static Float getFloat(String value, Float defaultValue) {
        return StringUtils.isNotEmpty(value) ? Float.valueOf(value) : defaultValue;
    }

    public static Integer getInteger(String value) {
        return getInteger(value, null);
    }

    public static Integer getInteger(String value, Integer defaultValue) {
        return StringUtils.isNotEmpty(value) ? Integer.valueOf(value) : defaultValue;
    }

    public static Integer getSafeInteger(String value) {
        try {
            return getInteger(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long getLong(String value) {
        return StringUtils.isNotEmpty(value) ? Long.valueOf(value) : null;
    }

    public static Long getSafeLong(String value) {
        try {
            return getLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double getDouble(String value) {
        return StringUtils.isNotEmpty(value) ? Double.valueOf(value) : null;
    }

    public static Boolean getBooleanFromNumber(String value) {
        return StringUtils.isNotEmpty(value) ? "1".equals(value) : null;
    }

    public static Boolean getBoolean(String value) {
        return getBoolean(value, null);
    }

    public static Boolean getBoolean(String value, Boolean defaultValue) {
        return StringUtils.isNotEmpty(value) ? Boolean.parseBoolean(value) : defaultValue;
    }

    public static String getString(Integer value) {
        return value != null ? value.toString() : null;
    }

    public static String formatThousand(String number) {
        if (StringUtils.isBlank(number)) {
            return StringUtils.EMPTY;
        }

        DecimalFormat decimalFormatter = new DecimalFormat();
        return decimalFormatter.format(Double.valueOf(number));
    }

    public static String getOrdinalSuffix(int n) {
        // REVIEW Add internationalization support
        if ((n >= 11) && (n <= 13)) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static String formatDoubleValue(double value){

        try {

            DecimalFormat decimalFormat= new DecimalFormat("#.###");

            decimalFormat.setRoundingMode(RoundingMode.CEILING);

            Number number = value;

            Double doubleValue = number.doubleValue();

            return decimalFormat.format(doubleValue);

        }catch (Exception ex){
            return  "";
        }

    }

    public static String formatValue(double value){

        try {

            NumberFormat formatter = new DecimalFormat("#0.00");

            return formatter.format(value);

        }catch (Exception ex){
            return  "";
        }
    }

    public static String formatValueUptoThreeDecimals(double value){

        try {

            NumberFormat formatter = new DecimalFormat("#0.000");

            return formatter.format(value);

        }catch (Exception ex){
            return  "";
        }
    }

    public static String getIntValue(double value){

        try {

            NumberFormat formatter = new DecimalFormat("#0");

            return formatter.format(value);

        }catch (Exception ex){
            return  "";
        }
    }


    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }
    public static boolean IsNumeric(String ValueToCheck)
    {

        try
        {
            Double result = Double.parseDouble(ValueToCheck);
            return true;
        }
        catch(Exception ex)
        {
            return false;
        }
    }

}
