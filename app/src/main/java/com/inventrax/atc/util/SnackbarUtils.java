package com.inventrax.atc.util;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

/**
 * Created by nareshp on 07/01/2016.
 */
public class SnackbarUtils {

    public static void showSnackbar(CoordinatorLayout coordinatorLayout, String message, int time) {
        Snackbar.make(coordinatorLayout, message, time).show();
    }

    public static void showSnackbarLengthShort(CoordinatorLayout coordinatorLayout, String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    public static void showSnackbarLengthLong(CoordinatorLayout coordinatorLayout, String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    public static void showSnackbarLengthShort(CoordinatorLayout coordinatorLayout, String message, int snackbarBackgroundColor, int messageColor) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(snackbarBackgroundColor);
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(messageColor);
        snackbar.show();
    }

    public static void showSnackbarLengthLong(CoordinatorLayout coordinatorLayout, String message, int snackbarBackgroundColor, int messageColor) {
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(snackbarBackgroundColor);
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(messageColor);
        snackbar.show();
    }

    public static void showSnackbarLengthLong(CoordinatorLayout coordinatorLayout, String message, int messageColor) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(messageColor);
        snackbar.show();
    }

    public static void showSnackbarLengthShort(CoordinatorLayout coordinatorLayout, String message, int messageColor) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(messageColor);
        snackbar.show();
    }

    public static void showSnackbarWithActionLengthShort(CoordinatorLayout coordinatorLayout, String message, String actionButtonName, View.OnClickListener listener) {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_SHORT)
                .setAction(actionButtonName, listener);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackbar.show();
    }


    public static void showSnackbarWithActionLengthShort(CoordinatorLayout coordinatorLayout, String message, String actionButtonName, View.OnClickListener listener, int messageColor) {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_SHORT)
                .setAction(actionButtonName, listener);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(messageColor);
        snackbar.show();
    }

    public static void showSnackbarWithActionLengthShort(CoordinatorLayout coordinatorLayout, String message, String actionButtonName, View.OnClickListener listener, int actionButtonTextColor, int snackbarBackgroundColor, int messageColor) {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_SHORT)
                .setAction(actionButtonName, listener);
        snackbar.setActionTextColor(actionButtonTextColor);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(snackbarBackgroundColor);
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(messageColor);
        snackbar.show();
    }

    public static void showSnackbarWithActionLengthLong(CoordinatorLayout coordinatorLayout, String message, String actionButtonName, View.OnClickListener listener) {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                .setAction(actionButtonName, listener);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        snackbar.show();
    }

    public static void showSnackbarWithActionLengthLong(CoordinatorLayout coordinatorLayout, String message, String actionButtonName, View.OnClickListener listener, int messageColor) {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                .setAction(actionButtonName, listener);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(messageColor);
        snackbar.show();
    }

    public static void showSnackbarWithActionLengthLong(CoordinatorLayout coordinatorLayout, String message, String actionButtonName, View.OnClickListener listener, int actionButtonTextColor, int snackbarBackgroundColor, int messageColor) {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                .setAction(actionButtonName, listener);
        snackbar.setActionTextColor(actionButtonTextColor);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(snackbarBackgroundColor);
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(messageColor);
        snackbar.show();
    }


}
