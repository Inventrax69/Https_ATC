package com.inventrax.atc.util;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

public class SpinnerUtils {

    public static Spinner getSpinner(Activity activity, String title , Spinner spinner , ArrayList<String> stringArrayList ){
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, stringArrayList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setPrompt(title);

        return spinner;
    }


}
