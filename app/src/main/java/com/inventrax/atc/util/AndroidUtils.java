package com.inventrax.atc.util;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;


import com.inventrax.atc.application.AbstractApplication;
import com.inventrax.atc.common.collections.Lists;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AndroidUtils {

    public static Boolean isEmulator() {
        return "google_sdk".equals(Build.PRODUCT);
    }

    public static String getAndroidId() {
        return Secure.getString(AbstractApplication.get().getContentResolver(), Secure.ANDROID_ID);
    }

    /**
     * @return The version name of the application
     */
    public static String getVersionName() {
        return getPackageInfo().versionName;
    }

    /**
     * @return The version code of the application
     */
    public static Integer getVersionCode() {
        return getPackageInfo().versionCode;
    }

    /**
     * @return The application id of the application
     */
    public static String getApplicationId() {
        return getPackageInfo().packageName;
    }

    /**
     * @return The name of the application
     */
    public static String getApplicationName() {
        Context context = AbstractApplication.get();
        ApplicationInfo applicationInfo = AndroidUtils.getApplicationInfo();
        return context.getPackageManager().getApplicationLabel(applicationInfo).toString();
    }

    public static PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            Context context = AbstractApplication.get();
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            // Do Nothing
        }
        return info;
    }

    public static ApplicationInfo getApplicationInfo() {
        ApplicationInfo info = null;
        try {
            Context context = AbstractApplication.get();
            info = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            // Do Nothing
        }
        return info;
    }

    public static void showSoftInput(Activity activity) {
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static void hideSoftInput(View view) {
        ((InputMethodManager) AbstractApplication.get().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                view.getWindowToken(), 0);
    }

    public static void scrollToBottom(final ScrollView scroll) {
        if (scroll != null) {
            scroll.post(new Runnable() {

                @Override
                public void run() {
                    scroll.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    public static String getNetworkOperatorName() {
        TelephonyManager manager = (TelephonyManager) AbstractApplication.get().getSystemService(
                Context.TELEPHONY_SERVICE);
        return manager.getNetworkOperatorName();
    }

    public static String getSimOperatorName() {
        TelephonyManager manager = (TelephonyManager) AbstractApplication.get().getSystemService(
                Context.TELEPHONY_SERVICE);
        return manager.getSimOperatorName();
    }

    /**
     * @return The HEAP size in MegaBytes
     */
    public static Integer getHeapSize() {
        return ((ActivityManager) AbstractApplication.get().getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    }

    /**
     * @return The available storage in MegaBytes
     */
    @SuppressWarnings("deprecation")
    public static Long getAvailableInternalDataSize() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long size = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        return size / FileUtils.BYTES_TO_MB;
    }

    /**
     * @return The total storage in MegaBytes
     */
    @SuppressWarnings("deprecation")
    public static Long getTotalInternalDataSize() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long size = (long) stat.getBlockCount() * (long) stat.getBlockSize();
        return size / FileUtils.BYTES_TO_MB;
    }

    /**
     * Checks if the application is installed on the SD card.
     *
     * @return <code>true</code> if the application is installed on the sd card
     */
    public static Boolean isInstalledOnSdCard() {
        return (getPackageInfo().applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE;
    }

    public static Boolean isMediaMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static String getDeviceModel() {
        return Build.MODEL;
    }

    public static String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    public static Integer getApiLevel() {
        return Build.VERSION.SDK_INT;
    }

    public static Boolean isPreKitkat() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT;
    }

    public static Boolean isPreLollipop() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    public static String getPlatformVersion() {
        return Build.VERSION.RELEASE;
    }

    public static Boolean hasCamera() {
        return IntentUtils.isIntentAvailable(MediaStore.ACTION_IMAGE_CAPTURE);
    }

    public static String getDeviceName() {
        String manufacturer = getDeviceManufacturer();
        String model = getDeviceModel();
        if ((model != null) && model.startsWith(manufacturer)) {
            return StringUtils.capitalize(model);
        } else if (manufacturer != null) {
            return StringUtils.capitalize(manufacturer) + " " + model;
        } else {
            return "Unknown";
        }
    }


    public static List<String> getAccountsEmails() {
        List<String> emails = Lists.newArrayList();
        for (Account account : AccountManager.get(AbstractApplication.get()).getAccounts()) {
            if (ValidationUtils.isValidEmail(account.name) && !emails.contains(account.name)) {
                emails.add(account.name);
            }
        }
        return emails;
    }


    public static String getMacAddress(Activity activity) {
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getConnectionInfo().getMacAddress();
    }

    public static String getDeviceType() {
        if (ScreenUtils.is10Inches()) {
            return "10\" tablet";
        } else if (ScreenUtils.isBetween7And10Inches()) {
            return "7\" tablet";
        } else {
            return "phone";
        }
    }

    public static Boolean hasPermission(Context context, String permission) {
        PackageManager pm = context.getPackageManager();
        int hasPerm = pm.checkPermission(permission, context.getPackageName());
        return hasPerm != PackageManager.PERMISSION_GRANTED;
    }

    public static String getIMEINumber(Activity activity) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null)
                return telephonyManager.getDeviceId();
            else
                return "";
        } catch (Exception ex) {
            Log.d("Error", ex.toString());

        }
        return "";
    }


    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    public static String getDeviceSerialNumber(){
        return Build.SERIAL;
    }

    public static View getToolbarLogoIcon(Toolbar toolbar){
        //check if contentDescription previously was set
        boolean hadContentDescription = android.text.TextUtils.isEmpty(toolbar.getLogoDescription());
        String contentDescription = String.valueOf(!hadContentDescription ? toolbar.getLogoDescription() : "logoContentDescription");
        toolbar.setLogoDescription(contentDescription);
        ArrayList<View> potentialViews = new ArrayList<View>();
        //find the view based on it's content description, set programatically or with android:contentDescription
        toolbar.findViewsWithText(potentialViews,contentDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
        //Nav icon is always instantiated at this point because calling setLogoDescription ensures its existence
        View logoIcon = null;
        if(potentialViews.size() > 0){
            logoIcon = potentialViews.get(0);
        }
        //Clear content description if not previously present
        if(hadContentDescription)
            toolbar.setLogoDescription(null);
        return logoIcon;
    }

}