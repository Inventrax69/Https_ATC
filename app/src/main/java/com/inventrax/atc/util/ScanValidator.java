package com.inventrax.atc.util;

/**
 * Created by karthik.m on 05/11/2018.
 */

public class ScanValidator {

    public static boolean IsItemScanned(String scannedData) {
        if (scannedData.split("[-]").length == 2) {
            return true;
        } else {
            return false;
        }
    }




    // Scan Validator

    public static boolean isItemScanned(String scannedData) {
        if (scannedData.split("[|]").length == 9 || scannedData.split("[|]").length == 5 || scannedData.split("[|]", -1).length == 9) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isContainerScanned(String scanneddata) {
        if (scanneddata.length() == 14 && (isNumeric(scanneddata.substring(0, 3)) || isNumeric(scanneddata.substring(8, 10)))) {
            return true;
        } else {
            return false;
        }
    }


    public static boolean isLocationScanned(String scannedData) {
        if ((scannedData.length() == 7 || scannedData.length() == 9) && isNumeric(scannedData.substring(5, 7))) {
            return true;
        } else {
            return false;
        }
    }


    public static boolean isDockLocationScanned(String scannedData) {
        if (scannedData.length() == 9 && scannedData.startsWith("DZ")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isStoreRefNumberScanned(String scannedData) {
        if (scannedData.length() == 10 && scannedData.startsWith("DZ")) {
            return true;
        } else {
            return false;
        }
    }


    public static boolean isNumeric(String ValueToCheck) {

        try {
            Double result = Double.parseDouble(ValueToCheck);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}