package com.inventrax.atc.util;

import android.content.Context;



public class MaterialDialogUtils {

    private Context context;
    //private MaterialDialog.Builder builder;
   // private MaterialDialog materialDialog;
    public String positiveText="Ok";
    public String negativeText="Cancel";

    public MaterialDialogUtils(Context context){
        this.context=context;
        //builder=new MaterialDialog.Builder(context);
    }

    public void showAlertDialog(String message){
       /* builder.content(message);
        builder.positiveText(positiveText);
        materialDialog=builder.build();
        materialDialog.show();*/
    }


}
