package com.inventrax.atc.util;

import android.graphics.Typeface;
import android.widget.Button;
import android.widget.TextView;

import com.inventrax.atc.application.AbstractApplication;


public class FontUtils {

    public static void setFont(Button button){
        Typeface custom_font = Typeface.createFromAsset(AbstractApplication.get().getAssets(), "fonts/Cyclo_Trial.otf");
        button.setTypeface(custom_font, Typeface.BOLD);
    }

    public static void setActionbarFont(TextView textView){
        Typeface custom_font = Typeface.createFromAsset(AbstractApplication.get().getAssets(), "fonts/Cyclo_Trial.otf");
        textView.setTypeface(custom_font, Typeface.BOLD);
    }



    public static void setFont(TextView textView){
        Typeface custom_font = Typeface.createFromAsset(AbstractApplication.get().getAssets(), "fonts/Lucida_Grande.ttf");
        textView.setTypeface(custom_font);
    }
}
