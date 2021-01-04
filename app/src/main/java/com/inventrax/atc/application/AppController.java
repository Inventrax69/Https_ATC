package com.inventrax.atc.application;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;


import org.acra.ACRA;

import java.util.Map;



public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();
    public static String DEVICE_GCM_REGISTER_ID;
    private static AppController mInstance;
    private  Context appContext;
    public static Map<String,String> mapUserRoutes;




    public static synchronized AppController getInstance() {
        return mInstance;
    }


    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Initializing Acra
        mInstance = this;
        MultiDex.install(this);
        ACRA.init(this);
        AbstractApplication.CONTEXT = getApplicationContext();
        appContext= getApplicationContext();
        //LocaleHelper.onCreate(this, "en");


        //LocaleHelper.onCreate(this, "en");

    }



    @Override
    public void onLowMemory() {
        super.onLowMemory();

        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();

    }


}