package com.inventrax.atc.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NetworkUtils {

    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;
    private static ConnectivityManager connectivityManager;
    private static LocationManager locationManager;
    private static boolean isInternetAvailable=false;

    public static int getConnectivityStatus(Context context) {
        connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusAsString(Context context) {
        int conn = NetworkUtils.getConnectivityStatus(context);
        String status = null;
        if (conn == NetworkUtils.TYPE_WIFI) {
            status = "Wifi enabled";
        } else if (conn == NetworkUtils.TYPE_MOBILE) {
            status = "Mobile data enabled";
        } else if (conn == NetworkUtils.TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }

    public static boolean getConnectivityStatusAsBoolean(Context context) {
        int connectionType = NetworkUtils.getConnectivityStatus(context);
        boolean status = false;
        if (connectionType == NetworkUtils.TYPE_WIFI) {
            status = true;
        } else if (connectionType == NetworkUtils.TYPE_MOBILE) {
            status = true;
        } else if (connectionType == NetworkUtils.TYPE_NOT_CONNECTED) {
            status = false;
        }
        return status;
    }



    public static boolean isWiFiEnabled(Context context) {
        return NetworkUtils.getConnectivityStatus(context) == NetworkUtils.TYPE_WIFI;
    }

    public static void setForceWiFiEnabled(Context context, boolean enabled) {
        try {

            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(enabled);

        } catch (Exception ex) {

        }
    }


    public static void setWiFiEnabled(Activity activity, Context context, boolean enabled) {
        try {

            buildAlertMessageNoWiFi(activity, context, enabled);


        } catch (Exception ex) {

        }
    }

    private static void buildAlertMessageNoWiFi(final Activity activity, final Context context, final boolean enabled) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Your Wi-Fi seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {

                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        wifiManager.setWifiEnabled(enabled);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }

    public static boolean isMobileDataEnabled(Context context) {
        return NetworkUtils.getConnectivityStatus(context) == NetworkUtils.TYPE_MOBILE;
    }

    public static void setMobileDataEnabled(Context context, boolean enabled) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        /*final ConnectivityManager conman = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
        connectivityManagerField.setAccessible(true);
        final Object connectivityManager = connectivityManagerField.get(conman);
        final Class connectivityManagerClass =  Class.forName(connectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        setMobileDataEnabledMethod.setAccessible(true);
        setMobileDataEnabledMethod.invoke(connectivityManager, enabled);*/

        Method dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
        dataMtd.setAccessible(true);
        dataMtd.invoke(connectivityManager, true);
    }



    public static boolean isInternetAvailable(Context context) {

        try {

         /*   if(getConnectivityStatusAsBoolean(AbstractApplication.get())){

                return new InternetAvailabilityCheckTask().execute(ServiceURLConstants.METHOD_CHECK_INTERNET).get();

            }
            else
            {

                return false;

            }
*/          final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
            return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();


        } catch (Exception ex) {
            ex.printStackTrace();
            return  false;
        }

    }

    public static void openSettingActivity(Activity activity) {
        activity.startActivity(new Intent(Settings.ACTION_SETTINGS));
    }


    public static boolean isGPSEnabled(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        return gps_enabled;
    }


    public static void enableGPS(Activity activity) {
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps(activity);
        }
    }

    private static void buildAlertMessageNoGps(final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }

    static class InternetAvailabilityCheckTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {



            try {




                } catch (Exception ex) {
                    ex.printStackTrace();
                }



            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            NetworkUtils.isInternetAvailable=aBoolean;
        }
    }


}
