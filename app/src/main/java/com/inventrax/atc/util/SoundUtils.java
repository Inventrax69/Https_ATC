package com.inventrax.atc.util;

import android.app.Activity;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.inventrax.atc.R;

/**
 * Created by karthik.m on 07/20/2018.
 */

public class SoundUtils {
    public void alertConfirm(Activity activity, Context context){

        try {
            String uri = "android.resource://" + activity.getPackageName() + "/" + R.raw.success;
            //Strign uri = "http://bla-bla-bla.com/bla-bla.wav"
            Uri notification = Uri.parse(uri);
            Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void alertError(Activity activity, Context context){

        try {
            String uri = "android.resource://" + activity.getPackageName() + "/" + R.raw.error;
            //Strign uri = "http://bla-bla-bla.com/bla-bla.wav"
            Uri notification = Uri.parse(uri);
            Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void alertSuccess(Activity activity, Context context){

        try {
            String uri = "android.resource://" + activity.getPackageName() + "/" + R.raw.success;
            //Strign uri = "http://bla-bla-bla.com/bla-bla.wav"
            Uri notification = Uri.parse(uri);
            Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void alertCriticalError(Activity activity, Context context){

        try {
            String uri = "android.resource://" + activity.getPackageName() + "/" + R.raw.critical;
            //Strign uri = "http://bla-bla-bla.com/bla-bla.wav"
            Uri notification = Uri.parse(uri);
            Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void alertWarning(Activity activity, Context context){

        try {
            String uri = "android.resource://" + activity.getPackageName() + "/" + R.raw.warning;
            //Strign uri = "http://bla-bla-bla.com/bla-bla.wav"
            Uri notification = Uri.parse(uri);
            Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
