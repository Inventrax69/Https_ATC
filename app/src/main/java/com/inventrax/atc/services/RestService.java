package com.inventrax.atc.services;


import android.util.Log;

import com.inventrax.atc.common.constants.ServiceURL;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Prasanna ch on 7/2/2015.
 */
public class RestService {


    //public static final String BASE_URL = "http://192.168.1.15/FalconWMSCore_Endpoint/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        try {
            retrofit = null;
            if (retrofit == null) {

                OkHttpClient client = new OkHttpClient.Builder()
                        .protocols(Arrays.asList(Protocol.HTTP_1_1))
                        .connectTimeout(180, TimeUnit.SECONDS)
                        .readTimeout(180,TimeUnit.SECONDS).build();


                         retrofit = new Retrofit.Builder()
                        .baseUrl(ServiceURL.getServiceUrl()).client(client)

                        // .baseUrl("http://192.168.59.7/API/").client(client)
                       //  .baseUrl("http://192.168.1.72/VIP_API/").client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return retrofit;
        } catch (Exception ex) {
            Log.d("Exceptionerror", ex.toString());
        }
        return retrofit;
    }
}