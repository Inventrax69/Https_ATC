package com.inventrax.atc.services.appupdate;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.inventrax.atc.application.AbstractApplication;
import com.inventrax.atc.common.constants.ServiceURL;
import com.inventrax.atc.util.NotificationUtils;

public class UpdateServiceUtils {

    private Context context;
    private UpdateRequest.Builder builder;
    private ServiceURL serviceURL;

    public UpdateServiceUtils(){
        this.context= AbstractApplication.get();
        builder = new UpdateRequest.Builder(context);
        serviceURL = new ServiceURL();
    }

    public void checkUpdate(){
        try
        {
            builder.setVersionCheckStrategy(buildVersionCheckStrategy())
                    .setPreDownloadConfirmationStrategy(buildPreDownloadConfirmationStrategy())
                    .setDownloadStrategy(buildDownloadStrategy())
                    .setPreInstallConfirmationStrategy(buildPreInstallConfirmationStrategy())
                    .execute();

        }catch (Exception ex){
            Log.d("test123",ex.toString());
        }
    }

    private VersionCheckStrategy buildVersionCheckStrategy() {
        return (new SimpleHttpVersionCheckStrategy(serviceURL.getServiceUrl()+"update.json"));
       // return (new SimpleHttpVersionCheckStrategy("www.inventrax.com/"));
    }

    private ConfirmationStrategy buildPreDownloadConfirmationStrategy() {
        Notification n = NotificationUtils.getUpdateNotification(context, "Update Available", "Click to download the update!");
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return (new NotificationConfirmationStrategy(n));
    }

    private DownloadStrategy buildDownloadStrategy() {
        if (Build.VERSION.SDK_INT >= 11) {
            return (new InternalHttpDownloadStrategy());
        }

        return (new SimpleHttpDownloadStrategy());
    }

    private ConfirmationStrategy buildPreInstallConfirmationStrategy() {
        Notification n = NotificationUtils.getUpdateNotification(context, "Update Ready to Install", "Click to install the update!");
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        return (new NotificationConfirmationStrategy(n));
    }
}
