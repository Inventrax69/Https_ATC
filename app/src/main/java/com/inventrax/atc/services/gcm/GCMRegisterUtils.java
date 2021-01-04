package com.inventrax.atc.services.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.inventrax.atc.activities.MainActivity;
import com.inventrax.atc.application.AbstractApplication;
import com.inventrax.atc.application.AppController;
import com.inventrax.atc.util.NetworkUtils;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by Naresh on 29-Jan-16.
 */
public class GCMRegisterUtils {

    public static final String REG_ID = "regId";
    static final String TAG = "RegisterUtils";
    private static final String APP_VERSION = "appVersion";
    private GoogleCloudMessaging gcm;
    private Context context;
    private String regId;

    public GCMRegisterUtils() {
        this.context = AbstractApplication.get();
        registerGCM();

    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //Log.d("RegisterActivity","I never expected this! Going down, going down!" + e);
            throw new RuntimeException(e);
        }
    }

    public String registerGCM() {

        gcm = GoogleCloudMessaging.getInstance(context);
        regId = getRegistrationId(context);


        if (TextUtils.isEmpty(regId)) {

            if (NetworkUtils.getConnectivityStatusAsBoolean(context)) {
                registerInBackground();
            } else {
               // Toast.makeText(context, "Please enable internet", Toast.LENGTH_LONG).show();
            }

        } else {

            AppController.DEVICE_GCM_REGISTER_ID = regId;

        }
        return regId;
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String registrationId = prefs.getString(REG_ID, "");
        if (registrationId.isEmpty()) {
            //Log.i(TAG, "Registration not found.");
            return "";
        }
        int registeredVersion = prefs.getInt(APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            //Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private void registerInBackground() {
        new GCMAsyncTask().execute();
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = context.getSharedPreferences(
                MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        int appVersion = getAppVersion(context);
        //Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, regId);
        editor.putInt(APP_VERSION, appVersion);
        editor.commit();
    }

    public String getRegistrationId() {
        return regId;
    }


    private  class GCMAsyncTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            String msg = "";
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                regId = gcm.register(Config.GOOGLE_PROJECT_ID);
                // Log.d("RegisterActivity", "registerInBackground - regId: "+ regId);
                msg = "Device registered, registration ID=" + regId;

                storeRegistrationId(context, regId);

                AppController.DEVICE_GCM_REGISTER_ID = regId;

            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();

            }

            return msg;
        }


        @Override
        protected void onPostExecute(String msg) {

        }


    }

}
