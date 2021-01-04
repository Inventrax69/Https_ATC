package com.inventrax.atc.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

/**
 * Created by Padmaja.B on 6/1/2018.
 */

public class ExceptionLoggerUtils {

    public static String pathRoot = "/sdcard/";


    public static File createExceptionLog(String ex,String classCode,String methodCode,Context context) throws IOException {

        String stackString = ex + " " + classCode  + " " + methodCode;
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        Date date = new Date();

        String dateTime = dateFormat.format(date);
        File file = new File(Environment.getExternalStorageDirectory(), dateTime);
        file.createNewFile();

        if (file.exists()) {
            try {

                if (stackString.length() > 0) {
                    OutputStream fo = new FileOutputStream(file);
                    fo = new FileOutputStream(file);
                    fo.write(stackString.getBytes());
                    fo.flush();
                    fo.close();
                }
            } catch (FileNotFoundException fileNotFoundException) {
                Log.e("TAG", "File not found!", fileNotFoundException);
            } catch (IOException ioException) {
                Log.e("TAG", "Unable to write to file!", ioException);
            }

        }
        return file;
    }

    public static String readFromFile(Context contect) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        Date date = new Date();

        String dateTime = dateFormat.format(date);
        String aBuffer = "";
        try {
            File myFile = new File(pathRoot + dateTime);
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return aBuffer;
    }

    public void deleteFile(Context context){
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");
        Date date = new Date();

        String dateTime = dateFormat.format(date);

        File file = new File(pathRoot+dateTime);
        if(file.exists()) {
            if (file.delete()) {
                Log.e(TAG, "File deleted.");
            }else {
                Log.e(TAG, "Failed to delete file!");
            }
        }else {
            Log.e(TAG, "File not exist!");
        }
    }

}

