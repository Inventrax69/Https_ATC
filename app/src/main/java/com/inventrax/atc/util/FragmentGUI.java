package com.inventrax.atc.util;

import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class FragmentGUI {

    public static View view;

    public static void setView(View view) {
        FragmentGUI.view = view;
    }

    public static TextView getTextView(int resId){
        return (TextView) view.findViewById(resId);
    }

    public static EditText getEditText(int resId){
        return (EditText)view.findViewById(resId);
    }

    public static TextInputLayout getTextInputLayout(int resId){
        return (TextInputLayout)view.findViewById(resId);
    }

    public static Button getButton(int resId){
        return (Button)view.findViewById(resId);
    }

    public static RadioGroup getRadioGroup(int resId){
        return (RadioGroup)view.findViewById(resId);
    }

    public static RadioButton getRadioButton(int resId){
        return (RadioButton)view.findViewById(resId);
    }

    public static CheckBox getCheckBox(int resId){
        return (CheckBox)view.findViewById(resId);
    }

    public static ImageView getImageView(int resId){
        return (ImageView)view.findViewById(resId);
    }

    public static ImageButton getImageButton(int resId){
        return (ImageButton)view.findViewById(resId);
    }

    public static Spinner getSpinner(int resId){
        return (Spinner)view.findViewById(resId);
    }

    public static WebView getWebView(int resId){
        return (WebView)view.findViewById(resId);
    }




    public static LinearLayout getLinearLayout(int resId){
        return (LinearLayout)view.findViewById(resId);
    }

    public static RelativeLayout getRelativeLayout(int resId){
        return (RelativeLayout)view.findViewById(resId);
    }

    public static FrameLayout getFrameLayout(int resId){
        return (FrameLayout)view.findViewById(resId);
    }

    public static TableLayout getTableLayout(int resId){
        return (TableLayout)view.findViewById(resId);
    }

    public static TableRow getTableRow(int resId){
        return (TableRow)view.findViewById(resId);
    }

    public static GridLayout getGridLayout(int resId){
        return (GridLayout)view.findViewById(resId);
    }


}
