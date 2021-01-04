package com.inventrax.atc.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class RestUtils{

    public static String syncToServer(String requestURL){
        BufferedReader bufferedReader =null;
        HttpURLConnection jsonRequestConnection=null;
        try {

            URL jsonRequestURL = new URL(requestURL);
            jsonRequestConnection =(HttpURLConnection) jsonRequestURL.openConnection();
            jsonRequestConnection.setRequestMethod("GET");
            //int responseCode= jsonRequestConnection.getResponseCode();
            bufferedReader = new BufferedReader(new InputStreamReader(jsonRequestConnection.getInputStream()));
            String strResponse="";
            StringBuffer stringBufferJSONResponse=new StringBuffer();

            while((strResponse=bufferedReader.readLine())!=null){
                stringBufferJSONResponse.append(strResponse);
            }
            strResponse=stringBufferJSONResponse.toString();

            return strResponse;

        } catch (Exception ex) {
            return null;
        }finally {
            if (bufferedReader!=null){
                try {
                    bufferedReader.close();
                }catch (Exception ex){}
            }

            if (jsonRequestConnection!=null){
                jsonRequestConnection.disconnect();
            }
        }
    }

}
