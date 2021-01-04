package com.inventrax.atc.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.inventrax.atc.R;
import com.inventrax.atc.common.constants.ServiceURL;
import com.inventrax.atc.util.DialogUtils;
import com.inventrax.atc.util.SharedPreferencesUtils;

/**
 * Created by Prasanna.ch on 06/06/2018.
 */

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private String classCode = "WMSCore_Android_Activity_002";

    private TextInputLayout inputLayoutServiceUrl;
    private EditText inputService;
    private Button btnSave,btnClose;
    private String url=null;

    private SharedPreferencesUtils sharedPreferencesUtils;
    ServiceURL serviceUrl = new ServiceURL();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loadFormControls();
    }

    public void loadFormControls()
    {
        btnSave=(Button)findViewById(R.id.btnSave);
        btnClose=(Button)findViewById(R.id.btnClose);
        inputLayoutServiceUrl = (TextInputLayout) findViewById(R.id.txtInputLayoutServiceUrl);
        inputService = (EditText)findViewById(R.id.etServiceUrl);

        btnSave.setOnClickListener(this);
        btnClose.setOnClickListener(this);

        sharedPreferencesUtils = new SharedPreferencesUtils("SettingsActivity", getApplicationContext());
        inputService.setText(sharedPreferencesUtils.loadPreference("url"));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSave:

                if(!inputService.getText().toString().isEmpty()) {
                    serviceUrl.setServiceUrl("");
                    SharedPreferences sp = this.getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
                    sharedPreferencesUtils.removePreferences("url");
                    sharedPreferencesUtils.savePreference("url", inputService.getText().toString());

                    DialogUtils.showAlertDialog(SettingsActivity.this,"Saved successfully");
                }else {
                    DialogUtils.showAlertDialog(SettingsActivity.this,"Service Url  not be empty");
                }


                break;

            case R.id.btnClose:
                Intent intent = new Intent(SettingsActivity.this,LoginActivity.class);
                startActivity(intent);
                break;
        }
    }
}