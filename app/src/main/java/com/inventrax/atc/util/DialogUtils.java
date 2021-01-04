package com.inventrax.atc.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;



public class DialogUtils {

    public static int ALERT_DIALOG_STYLE = 0;
    public static int CONFIRM_DIALOG_STYLE =0;
    public static String POSITIVE_BUTTON_TEXT = "OK";
    public static String NEGATIVE_BUTTON_TEXT = "Cancel";
    public static String NEUTRAL_BUTTON_TEXT = "Cancel";
    private static AlertDialog.Builder alertDialog;

    //android.R.id.button1 for positive, android.R.id.button2 for negative, and android.R.id.button3 for neutral


    public static void showAlertDialog(Activity activity, String message) {

        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, null);
        alertDialog.show();
    }

    public static void showAlertDialog(Activity activity, String dialogTitle, String message) {

        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, null);
        alertDialog.show();
    }

    public static void showAlertDialog(Activity activity, String dialogTitle, String message, int id) {

        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setIcon(id);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, null);
        alertDialog.show();
    }

    public static void showAlertDialog(Activity activity, String dialogTitle, String message, DialogInterface.OnClickListener onClickListener) {

        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, onClickListener);
        alertDialog.show();
    }

    public static void showAlertDialog(Activity activity, String dialogTitle, String message,int id, DialogInterface.OnClickListener onClickListener) {

        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setIcon(id);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, onClickListener);
        alertDialog.show();
    }

    public static void showAlertDialog(Activity activity, String dialogTitle, String message, DialogInterface.OnClickListener onClickListener, int id) {

        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setMessage(message);
        alertDialog.setIcon(id);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, onClickListener);
        alertDialog.show();
    }

    public static void showConfirmDialog(Activity activity, String dialogTitle, String message, String positive, String negitive , DialogInterface.OnClickListener onClickListener) {
        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(positive, onClickListener);
        alertDialog.setNegativeButton(negitive, onClickListener);
        alertDialog.show();
    }

    public static void showConfirmDialog(Activity activity, String dialogTitle, String message, DialogInterface.OnClickListener onClickListener) {
        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, onClickListener);
        alertDialog.setNegativeButton(NEGATIVE_BUTTON_TEXT, onClickListener);
        alertDialog.show();
    }


    /*public static AlertDialog.Builder showInputDialog(Activity activity,View view,String dialogTitle ,String message,DialogInterface.OnClickListener onClickListener)
    {
        alertDialog = new AlertDialog.Builder(activity, CONFIRM_DIALOG_STYLE);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setView(view);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, onClickListener);
        alertDialog.setNegativeButton(NEGATIVE_BUTTON_TEXT, onClickListener);

        return  alertDialog;
    }*/

    public static void showInputDialog(Activity activity, View view, String dialogTitle, String message, DialogInterface.OnClickListener onClickListener) {
        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setView(view);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, onClickListener);
        alertDialog.setNegativeButton(NEGATIVE_BUTTON_TEXT, onClickListener);
        alertDialog.show();
    }

    /*public static AlertDialog.Builder showCustomDialog(Activity activity,View view,String dialogTitle ,String message,DialogInterface.OnClickListener onClickListener)
    {
        alertDialog = new AlertDialog.Builder(activity, CONFIRM_DIALOG_STYLE);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setView(view);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, onClickListener);
        alertDialog.setNegativeButton(NEGATIVE_BUTTON_TEXT, onClickListener);

        return  alertDialog;
    }*/

    public static void showCustomDialog(Activity activity, View view) {
        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setView(view);
        alertDialog.show();
    }


    public static void showCustomDialog(Activity activity, View view, String dialogTitle) {
        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setView(view);
        alertDialog.show();
    }

    public static void showCustomDialog(Activity activity, View view, String dialogTitle, String message) {
        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setView(view);
        alertDialog.setMessage(message);
        alertDialog.show();
    }


    public static void showCustomDialog(Activity activity, View view, DialogInterface.OnClickListener onClickListener) {
        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setView(view);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, onClickListener);
        alertDialog.setNegativeButton(NEGATIVE_BUTTON_TEXT, onClickListener);
        alertDialog.show();
    }


    public static void showCustomDialog(Activity activity, View view, String dialogTitle, DialogInterface.OnClickListener onClickListener) {
        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setView(view);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, onClickListener);
        alertDialog.setNegativeButton(NEGATIVE_BUTTON_TEXT, onClickListener);
        alertDialog.show();
    }

    public static void showCustomDialog(Activity activity, View view, String dialogTitle, String message, DialogInterface.OnClickListener onClickListener) {
        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setView(view);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, onClickListener);
        alertDialog.setNegativeButton(NEGATIVE_BUTTON_TEXT, onClickListener);
        alertDialog.show();
    }

    /*
    public static AlertDialog.Builder showCustomDialog(Activity activity,int layout,String dialogTitle ,String message,DialogInterface.OnClickListener onClickListener)
    {
        alertDialog = new AlertDialog.Builder(activity, CONFIRM_DIALOG_STYLE);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setView(layout);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, onClickListener);
        alertDialog.setNegativeButton(NEGATIVE_BUTTON_TEXT, onClickListener);

        return  alertDialog;
    }*/

    public static void showCustomDialog(Activity activity, int layout, String dialogTitle, String message, DialogInterface.OnClickListener onClickListener) {
        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setView(layout);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, onClickListener);
        alertDialog.setNegativeButton(NEGATIVE_BUTTON_TEXT, onClickListener);
        alertDialog.show();
    }


    public static void showSingleChoiceDialog(Activity activity, String dialogTitle, CharSequence[] sourceList, DialogInterface.OnClickListener onClickListener){

        alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setCancelable(false);
        alertDialog.setTitle(dialogTitle);
        alertDialog.setSingleChoiceItems(sourceList, 0, onClickListener);
        // alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, onClickListener);
        // alertDialog.setNegativeButton(NEGATIVE_BUTTON_TEXT, onClickListener);
        alertDialog.show();
    }


}