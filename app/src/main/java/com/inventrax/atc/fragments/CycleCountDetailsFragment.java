package com.inventrax.atc.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cipherlab.barcode.GeneralString;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;
import com.inventrax.atc.R;
import com.inventrax.atc.activities.MainActivity;
import com.inventrax.atc.adapters.CCExportAdapter;
import com.inventrax.atc.common.Common;
import com.inventrax.atc.common.constants.EndpointConstants;
import com.inventrax.atc.common.constants.ErrorMessages;
import com.inventrax.atc.interfaces.ApiInterface;
import com.inventrax.atc.pojos.CycleCountDTO;
import com.inventrax.atc.pojos.ScanDTO;
import com.inventrax.atc.pojos.WMSCoreMessage;
import com.inventrax.atc.pojos.WMSExceptionMessage;
import com.inventrax.atc.searchableSpinner.SearchableSpinner;
import com.inventrax.atc.services.RetrofitBuilderHttpsEx;
import com.inventrax.atc.util.DialogUtils;
import com.inventrax.atc.util.ExceptionLoggerUtils;
import com.inventrax.atc.util.ProgressDialogUtils;
import com.inventrax.atc.util.SoundUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Padmaja on 06/27/2018.
 */

public class CycleCountDetailsFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_CYCLE COUNT";
    private View rootView;
    DialogUtils dialogUtils;

    private Button btnConfirm, btnBinComplete, btnClear, btnExportCC, btnCloseExport;
    private TextView lblCycleCount, lblScannedSku;
    private CardView cvScanLocation, cvScanContainer, cvScanSKU;
    private TextInputLayout txtInputLayoutLocation, txtInputLayoutContainer, txtInputLayoutSerial, txtInputLayoutBatch, txtInputLayoutMfgDate,
            txtInputLayoutExpDate, txtInputLayoutProjectRef, txtInputLayoutCCQty, txtInputLayoutMRP;

    private EditText etLocation, etContainer, etSerial, etBatch, etMfgDate, etExpDate, etProjectRef, etCCQty, etCCMRP;
    private ImageView ivScanLocation, ivScanContainer, ivScanSKU;
    private RelativeLayout rlCC, rlCCExport;
    private RecyclerView rvPendingCC;

    String scanner = null;
    String getScanner = null;

    private IntentFilter filter;
    private Gson gson;
    private WMSCoreMessage core;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;

    String materialCode = "", warehouseId = "", tenantId = "";
    private Common common = null;

    String userId = null, scanType = null, accountId = null;

    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private SoundUtils soundUtils;
    private LinearLayoutManager linearLayoutManager;
    boolean isValidLocation = false;
    boolean isPalletScanned = false;
    boolean isRSNScanned = false;
    String Rack = "", Column = "", Level = "",CycleCountSeqCode="";
    TextView tvRack, tvColumn, tvLevel;
    SearchableSpinner spinnerSelectSloc;
    String storageLoc;

    public void myScannedData(Context context, String barcode){
        try {
            ProcessScannedinfo(barcode.trim());
        }catch (Exception e){
            //  Toast.makeText(context, ""+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public CycleCountDetailsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_cyclecount_details, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        scanType = sp.getString("scanType", "");
        accountId = sp.getString("AccountId", "");

        rlCC = (RelativeLayout) rootView.findViewById(R.id.rlCC);
        rlCCExport = (RelativeLayout) rootView.findViewById(R.id.rlCCExport);

        rvPendingCC = (RecyclerView) rootView.findViewById(R.id.rvPendingCC);
        rvPendingCC.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());

        // use a linear layout manager
        rvPendingCC.setLayoutManager(linearLayoutManager);

        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanContainer = (CardView) rootView.findViewById(R.id.cvScanContainer);
        cvScanSKU = (CardView) rootView.findViewById(R.id.cvScanSKU);

        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanContainer = (ImageView) rootView.findViewById(R.id.ivScanContainer);
        ivScanSKU = (ImageView) rootView.findViewById(R.id.ivScanSKU);

        btnBinComplete = (Button) rootView.findViewById(R.id.btnBinComplete);
        btnCloseExport = (Button) rootView.findViewById(R.id.btnCloseExport);
        btnExportCC = (Button) rootView.findViewById(R.id.btnExportCC);
        btnConfirm = (Button) rootView.findViewById(R.id.btnConfirm);
        btnClear = (Button) rootView.findViewById(R.id.btnClear);

        lblCycleCount = (TextView) rootView.findViewById(R.id.lblCycleCount);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);

        tvRack = (TextView) rootView.findViewById(R.id.tvRack);
        tvColumn = (TextView) rootView.findViewById(R.id.tvColumn);
        tvLevel = (TextView) rootView.findViewById(R.id.tvLevel);

        etLocation = (EditText) rootView.findViewById(R.id.etLocation);
        etContainer = (EditText) rootView.findViewById(R.id.etContainer);
        etSerial = (EditText) rootView.findViewById(R.id.etSerial);
        etMfgDate = (EditText) rootView.findViewById(R.id.etMfgDate);
        etBatch = (EditText) rootView.findViewById(R.id.etBatch);
        etExpDate = (EditText) rootView.findViewById(R.id.etExpDate);
        etProjectRef = (EditText) rootView.findViewById(R.id.etProjectRef);
        etCCQty = (EditText) rootView.findViewById(R.id.etCCQty);
        etCCMRP = (EditText) rootView.findViewById(R.id.etCCMRP);

        etCCQty.clearFocus();
        etCCQty.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    MainActivity mainActivity=(MainActivity)getActivity();
                    mainActivity.barcode="";
                    return  true;
                }
                return false;
            }
        });

        txtInputLayoutLocation = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutLocation);
        txtInputLayoutContainer = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutContainer);
        txtInputLayoutBatch = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutBatch);
        txtInputLayoutSerial = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutSerial);
        txtInputLayoutMfgDate = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMfgDate);
        txtInputLayoutExpDate = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutExpDate);
        txtInputLayoutProjectRef = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutProjectRef);
        txtInputLayoutCCQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutCCQty);
        txtInputLayoutMRP = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMRP);

        spinnerSelectSloc = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectSloc);
        spinnerSelectSloc.setOnItemSelectedListener(this);

        lblCycleCount.setText(getArguments().getString("CCname"));
        warehouseId = getArguments().getString("warehouseId");
        tenantId = getArguments().getString("tenantId");
        Rack = getArguments().getString("Rack");
        Column = getArguments().getString("Column");
        Level = getArguments().getString("Level");
        CycleCountSeqCode = getArguments().getString("CycleCountSeqCode");

        tvRack.setText("Rack : " + Rack);
        tvColumn.setText("Col : " + Column);
        tvLevel.setText("Level : " + Level);

        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();
        soundUtils = new SoundUtils();

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        gson = new GsonBuilder().create();
        common = new Common();
        ProgressDialogUtils.closeProgressDialog();
        common.setIsPopupActive(false);

        btnBinComplete.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnCloseExport.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        btnExportCC.setOnClickListener(this);
        cvScanContainer.setOnClickListener(this);

        if (scanType.equals("Auto")) {
            btnConfirm.setEnabled(false);
            btnConfirm.setTextColor(getResources().getColor(R.color.black));
            btnConfirm.setBackgroundResource(R.drawable.button_hide);
        } else {
            btnConfirm.setEnabled(true);
            btnConfirm.setTextColor(getResources().getColor(R.color.white));
            btnConfirm.setBackgroundResource(R.drawable.button_shape);
        }


        //For Honeywell
        AidcManager.create(getActivity(), new AidcManager.CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {

                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
                try {
                    barcodeReader.claim();
                    HoneyWellBarcodeListeners();

                } catch (ScannerUnavailableException e) {
                    e.printStackTrace();
                }
            }
        });


        // To get Storage Locations
        getSLocs();




    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBinComplete:
                if (!etLocation.getText().toString().isEmpty()) {
                    DialogUtils.showConfirmDialog(getActivity(), "Confirm", "Are you sure to complete this bin? ", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    common.setIsPopupActive(false);
                                    releaseCycleCountLocation();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    common.setIsPopupActive(false);
                                    break;
                            }

                        }
                    });
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Error");
                    return;
                }
                break;

            case R.id.btnConfirm:
                if (!materialCode.equals("")) {
                    if (etCCQty.getText().toString().isEmpty()) {
                        common.showUserDefinedAlertType("Please enter quantity", getActivity(), getContext(), "Error");
                        return;
                    } else {
                        DialogUtils.showConfirmDialog(getActivity(), "Confirm", "Are you sure to submit this sku? ", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        common.setIsPopupActive(false);
                                        upsertCycleCount();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        common.setIsPopupActive(false);
                                        break;
                                }

                            }
                        });

                    }
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0065, getActivity(), getContext(), "Error");
                    return;
                }
                break;

            case R.id.btnCloseExport:
                rlCC.setVisibility(View.VISIBLE);
                rlCCExport.setVisibility(View.GONE);
                break;

            case R.id.btnClear:
                clearFields();
                break;
            case R.id.cvScanContainer:
                if (isPalletScanned) {
                    clearFields1();
                } else {
                    clearFields1();
                    isPalletScanned = true;
                    cvScanContainer.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanContainer.setImageResource(R.drawable.check);
                }

                break;

            case R.id.btnExportCC:

                if (!etLocation.getText().toString().isEmpty()) {

                    rlCC.setVisibility(View.GONE);
                    rlCCExport.setVisibility(View.VISIBLE);

                    rvPendingCC.setAdapter(null);

                    getCycleCountInformation();

                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Error");
                    return;
                }
                break;
        }
    }

    public void getSLocs() {


        List<String> _lstSLocNames = new ArrayList<>();
        _lstSLocNames.add("OK");
        _lstSLocNames.add("Damage");

        ArrayAdapter arrayAdapterSLoc = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, _lstSLocNames);
        spinnerSelectSloc.setAdapter(arrayAdapterSLoc);
        int getPostion = _lstSLocNames.indexOf("OK");
        String compareValue = String.valueOf(_lstSLocNames.get(getPostion).toString());
        if (compareValue != null) {
            int spinnerPosition = arrayAdapterSLoc.getPosition(compareValue);
            spinnerSelectSloc.setSelection(spinnerPosition);
        }

    }

    // honeywell
    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // update UI to reflect the data
                getScanner = barcodeReadEvent.getBarcodeData();
                ProcessScannedinfo(getScanner);

            }

        });
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }

    @Override
    public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent) {

    }

    //Honeywell Barcode reader Properties
    public void HoneyWellBarcodeListeners() {

        barcodeReader.addTriggerListener(this);

        if (barcodeReader != null) {
            // set the trigger mode to client control
            barcodeReader.addBarcodeListener(this);
            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
            } catch (UnsupportedPropertyException e) {
                // Toast.makeText(this, "Failed to apply properties", Toast.LENGTH_SHORT).show();
            }

            Map<String, Object> properties = new HashMap<String, Object>();
            // Set Symbologies On/Off
            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Enable bad read response
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);
            // Apply the settings
            barcodeReader.setProperties(properties);
        }

    }


    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {

        if(((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).isDrawerOpen(GravityCompat.START)){
            return;
        }

        if (ProgressDialogUtils.isProgressActive() || Common.isPopupActive()) {
            common.showUserDefinedAlertType(errorMessages.EMC_082, getActivity(), getContext(), "Warning");
            return;
        }

        if (rlCC.getVisibility() == View.VISIBLE) {

            if (scannedData != null && !common.isPopupActive) {

                if (!isValidLocation) {
                    ValidateLocation(scannedData);
                } else {
                    if (!isPalletScanned) {
                        ValidatePallet(scannedData);
                    } else {
                        ValiDateMaterial(scannedData);
                    }
                }



/*                if (ScanValidator.isLocationScanned(scannedData)) {

                    etLocation.setText(scannedData);

                    isBlockedLocation();

                    return;

                }
                else if (ScanValidator.isItemScanned(scannedData) && !etLocation.getText().toString().isEmpty()) {
                    if (etLocation.getText().toString().equals(String.valueOf(R.string.hintLocation))) {
                        common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Error");
                    } else {

                        materialCode = scannedData.split("[|]")[0];

                        lblScannedSku.setText(materialCode);
                        etBatch.setText(scannedData.split("[|]")[1]);
                        etSerial.setText(scannedData.split("[|]")[2]);
                        etMfgDate.setText(scannedData.split("[|]")[3]);
                        etExpDate.setText(scannedData.split("[|]")[4]);
                        etProjectRef.setText(scannedData.split("[|]")[5]);
                        etCCMRP.setText(scannedData.split("[|]")[7]);

                        cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                        ivScanSKU.setImageResource(R.drawable.fullscreen_img);

                        checkMaterialAvailablilty();

                        return;
                    }


                }
                else if (ScanValidator.isContainerScanned(scannedData)) {

                    if (etLocation.getText().toString().isEmpty()) {
                        common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Error");

                    } else {
                        etContainer.setText(scannedData);

                        chekPalletLocation();

                        return;
                    }
                }*/
            }
        }
    }

    public void chekPalletLocation() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getContext());
            CycleCountDTO cycleCountDTO = new CycleCountDTO();
            cycleCountDTO.setUserId(userId);
            cycleCountDTO.setAccountID(accountId);
            cycleCountDTO.setWarehouseID(warehouseId);
            cycleCountDTO.setTenantId(tenantId);
            cycleCountDTO.setLocation(etLocation.getText().toString());
            cycleCountDTO.setPalletNo(etContainer.getText().toString());

            message.setEntityObject(cycleCountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ChekPalletLocation(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        if (response.body() != null) {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                    etContainer.setText("");
                                    cvScanContainer.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanContainer.setImageResource(R.drawable.invalid_cross);
                                    isPalletScanned = false;

                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    return;
                                }
                            } else {
                                List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();


                                List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                CycleCountDTO dto = null;
                                ProgressDialogUtils.closeProgressDialog();

                                for (int i = 0; i < _lResult.size(); i++) {

                                    dto = new CycleCountDTO(_lResult.get(i).entrySet());
                                    if (dto.getResult().equals("1")) {
                                        isPalletScanned = true;
                                        cvScanContainer.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanContainer.setImageResource(R.drawable.check);
                                    }
                                }
                            }
                        } else {
                            ProgressDialogUtils.closeProgressDialog();
                        }

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
        }
    }

    public void isBlockedLocation() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getActivity());
            final CycleCountDTO cycleCountDTO = new CycleCountDTO();
            cycleCountDTO.setUserId(userId);
            cycleCountDTO.setAccountID(accountId);
            cycleCountDTO.setCCName(lblCycleCount.getText().toString());
            cycleCountDTO.setLocation(etLocation.getText().toString());
            cycleCountDTO.setWarehouseID(warehouseId);
            cycleCountDTO.setTenantId(tenantId);
            message.setEntityObject(cycleCountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.IsBlockedLocation(message);

                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {


                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());


                                }

                                etLocation.setText("");
                                isValidLocation = false;
                                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanLocation.setImageResource(R.drawable.invalid_cross);

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                List<LinkedTreeMap<?, ?>> _lstCC = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstCC = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<CycleCountDTO> lstDto = new ArrayList<CycleCountDTO>();
                                List<String> _lstCCNames = new ArrayList<>();

                                for (int i = 0; i < _lstCC.size(); i++) {
                                    CycleCountDTO dto = new CycleCountDTO(_lstCC.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                ProgressDialogUtils.closeProgressDialog();

                                for (int i = 0; i < lstDto.size(); i++) {

                                    if (lstDto.get(i).getResult().equals("-1")) {
                                        isValidLocation = true;
                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanLocation.setImageResource(R.drawable.check);
                                        return;
                                    } else {
                                        isValidLocation = false;
                                        etLocation.setText("");
                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanLocation.setImageResource(R.drawable.warning_img);
                                        common.showUserDefinedAlertType(lstDto.get(i).getResult(), getActivity(), getContext(), "Error");
                                        return;
                                    }

                                }


                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
        }
    }

    public void checkMaterialAvailablilty() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getActivity());
            final CycleCountDTO cycleCountDTO = new CycleCountDTO();
            cycleCountDTO.setUserId(userId);
            cycleCountDTO.setAccountID(accountId);
            cycleCountDTO.setCCName(lblCycleCount.getText().toString());
            cycleCountDTO.setLocation(etLocation.getText().toString());
            cycleCountDTO.setMaterialCode(lblScannedSku.getText().toString());
            cycleCountDTO.setWarehouseID(warehouseId);
            cycleCountDTO.setTenantId(tenantId);
            message.setEntityObject(cycleCountDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.CheckMaterialAvailablilty(message);

                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                }

                                cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanSKU.setImageResource(R.drawable.warning_img);

                                materialCode = "";
                                lblScannedSku.setText("");
                                isRSNScanned = false;

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                List<LinkedTreeMap<?, ?>> _lstCC = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstCC = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<CycleCountDTO> lstDto = new ArrayList<CycleCountDTO>();
                                List<String> _lstCCNames = new ArrayList<>();

                                for (int i = 0; i < _lstCC.size(); i++) {
                                    CycleCountDTO dto = new CycleCountDTO(_lstCC.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                ProgressDialogUtils.closeProgressDialog();

                                for (int i = 0; i < lstDto.size(); i++) {

                                    if (lstDto.get(i).getResult().equals("1")) {
                                        isRSNScanned = true;
                                        cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSKU.setImageResource(R.drawable.check);

                                        if (scanType.equals("Auto")) {

                                            etCCQty.setText("1");

                                            upsertCycleCount();

                                        } else {

                                            etCCQty.setEnabled(true);
                                            btnConfirm.setEnabled(true);

                                            btnConfirm.setTextColor(getResources().getColor(R.color.white));
                                            btnConfirm.setBackgroundResource(R.drawable.button_shape);

                                            common.showUserDefinedAlertType(errorMessages.EMC_0064, getActivity(), getContext(), "Warning");
                                        }


                                    } else {
                                        common.showUserDefinedAlertType(lstDto.get(i).getResult(), getActivity(), getContext(), "Error");
                                        return;
                                    }

                                }


                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
        }
    }

    public void upsertCycleCount() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getActivity());

            final CycleCountDTO cycleCountDTO = new CycleCountDTO();
            cycleCountDTO.setUserId(userId);
            cycleCountDTO.setAccountID(accountId);
            cycleCountDTO.setWarehouseID(warehouseId);
            cycleCountDTO.setTenantId(tenantId);
            cycleCountDTO.setCCName(lblCycleCount.getText().toString());
            cycleCountDTO.setLocation(etLocation.getText().toString());
            cycleCountDTO.setPalletNo(etContainer.getText().toString());
            cycleCountDTO.setMaterialCode(lblScannedSku.getText().toString());
            cycleCountDTO.setCCQty(etCCQty.getText().toString());
            cycleCountDTO.setBatchNo(etBatch.getText().toString());
            cycleCountDTO.setSerialNo(etSerial.getText().toString());
            cycleCountDTO.setProjectRefNo(etProjectRef.getText().toString());
            cycleCountDTO.setMfgDate(etMfgDate.getText().toString());
            cycleCountDTO.setExpDate(etExpDate.getText().toString());
            //cycleCountDTO.setCount(tvCount.getText().toString());
            cycleCountDTO.setMRP(etCCMRP.getText().toString());
            cycleCountDTO.setStorageLocation(storageLoc);
            message.setEntityObject(cycleCountDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.UpsertCycleCount(message);

                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                }

                                cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanSKU.setImageResource(R.drawable.warning_img);

                                lblScannedSku.setText("");

                                materialCode = "";

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                List<LinkedTreeMap<?, ?>> _lstCC = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstCC = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<CycleCountDTO> lstDto = new ArrayList<CycleCountDTO>();
                                List<String> _lstCCNames = new ArrayList<>();

                                for (int i = 0; i < _lstCC.size(); i++) {
                                    CycleCountDTO dto = new CycleCountDTO(_lstCC.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                ProgressDialogUtils.closeProgressDialog();



                                for (int i = 0; i < lstDto.size(); i++) {

                                    if (lstDto.get(i).getResult().equals("Confirmed successfully")) {

                                        cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                        ivScanSKU.setImageResource(R.drawable.fullscreen_img);

                                        materialCode = "";
                                        lblScannedSku.setText("");
                                        etSerial.setText("");
                                        etBatch.setText("");
                                        etMfgDate.setText("");
                                        etExpDate.setText("");
                                        etProjectRef.setText("");
                                        etCCQty.setText("");

                                        soundUtils.alertSuccess(getActivity(), getContext());

                                        etCCQty.setEnabled(false);
                                        etCCQty.clearFocus();

                                        getSLocs();

                                    } else {
                                        common.showUserDefinedAlertType(lstDto.get(i).getResult(), getActivity(), getContext(), "Error");
                                        return;
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
        }
    }

    public void getCycleCountInformation() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getActivity());
            final CycleCountDTO cycleCountDTO = new CycleCountDTO();
            cycleCountDTO.setUserId(userId);
            cycleCountDTO.setAccountID(accountId);
            cycleCountDTO.setCCName(lblCycleCount.getText().toString());
            cycleCountDTO.setLocation(etLocation.getText().toString());
            cycleCountDTO.setWarehouseID(warehouseId);
            cycleCountDTO.setTenantId(tenantId);
            message.setEntityObject(cycleCountDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetCycleCountInformation(message);

                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                }

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                List<LinkedTreeMap<?, ?>> _lCCExport = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lCCExport = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<CycleCountDTO> lstCCExport = new ArrayList<CycleCountDTO>();

                                ProgressDialogUtils.closeProgressDialog();

                                if (_lCCExport.size() > 0) {
                                    CycleCountDTO ccdto = null;
                                    for (int i = 0; i < _lCCExport.size(); i++) {

                                        ccdto = new CycleCountDTO(_lCCExport.get(i).entrySet());
                                        lstCCExport.add(ccdto);
                                    }

                                    CCExportAdapter ccExportAdapter = new CCExportAdapter(getActivity(), lstCCExport);
                                    rvPendingCC.setAdapter(ccExportAdapter);
                                } else {
                                    common.showUserDefinedAlertType(errorMessages.EMC_0060, getActivity(), getContext(), "Warning");
                                    return;
                                }

                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
        }
    }

    public void releaseCycleCountLocation() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getActivity());
            final CycleCountDTO cycleCountDTO = new CycleCountDTO();
            cycleCountDTO.setUserId(userId);
            cycleCountDTO.setAccountID(accountId);
            cycleCountDTO.setCCName(lblCycleCount.getText().toString());
            cycleCountDTO.setLocation(etLocation.getText().toString());
            cycleCountDTO.setWarehouseID(warehouseId);
            cycleCountDTO.setTenantId(tenantId);
            cycleCountDTO.setCycleCountSeqCode(CycleCountSeqCode);
            message.setEntityObject(cycleCountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ReleaseCycleCountLocation(message);

                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                }

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                List<LinkedTreeMap<?, ?>> _lstCC = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstCC = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<CycleCountDTO> lstDto = new ArrayList<CycleCountDTO>();
                                List<String> _lstCCNames = new ArrayList<>();

                                for (int i = 0; i < _lstCC.size(); i++) {
                                    CycleCountDTO dto = new CycleCountDTO(_lstCC.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                ProgressDialogUtils.closeProgressDialog();

                                for (int i = 0; i < lstDto.size(); i++) {

                                    if (lstDto.get(i).getResult().equals("Closed successfully")) {

                                        common.showUserDefinedAlertType(lstDto.get(i).getResult(), getActivity(), getContext(), "Success");
                                        clearFields();

                                    } else {
                                        common.showUserDefinedAlertType(lstDto.get(i).getResult(), getActivity(), getContext(), "Error");
                                        return;
                                    }

                                }


                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
        }
    }

    public void clearFields1() {

        ProgressDialogUtils.closeProgressDialog();
        common.setIsPopupActive(false);

        cvScanContainer.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanContainer.setImageResource(R.drawable.fullscreen_img);

        cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSKU.setImageResource(R.drawable.fullscreen_img);

        lblScannedSku.setText("");
        materialCode = "";

        etContainer.setText("");
        etExpDate.setText("");
        etMfgDate.setText("");
        etSerial.setText("");
        etBatch.setText("");
        etProjectRef.setText("");
        etCCQty.setText("");
        // To get Storage Locations
        getSLocs();

        etCCQty.setEnabled(false);
        etCCQty.clearFocus();

        rvPendingCC.setAdapter(null);

        isPalletScanned = false;
        isRSNScanned = false;

    }

    public void clearFields() {

        ProgressDialogUtils.closeProgressDialog();
        common.setIsPopupActive(false);

        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

        cvScanContainer.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanContainer.setImageResource(R.drawable.fullscreen_img);

        cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSKU.setImageResource(R.drawable.fullscreen_img);

        lblScannedSku.setText("");
        materialCode = "";

        etLocation.setText("");
        etContainer.setText("");
        etExpDate.setText("");
        etMfgDate.setText("");
        etSerial.setText("");
        etBatch.setText("");
        etProjectRef.setText("");
        etCCQty.setText("");

        rvPendingCC.setAdapter(null);

        etCCQty.setEnabled(false);
        etCCQty.clearFocus();

        isValidLocation = false;
        isPalletScanned = false;
        isRSNScanned = false;
        // To get Storage Locations
        getSLocs();

    }

    public void blockLocationForCycleCount() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.CycleCount, getActivity());
            final CycleCountDTO cycleCountDTO = new CycleCountDTO();
            cycleCountDTO.setUserId(userId);
            cycleCountDTO.setAccountID(accountId);
            cycleCountDTO.setCCName(lblCycleCount.getText().toString());
            cycleCountDTO.setLocation(etLocation.getText().toString());
            message.setEntityObject(cycleCountDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.BlockLocationForCycleCount(message);

                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());


                                }

                                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanLocation.setImageResource(R.drawable.warning_img);

                                etLocation.setText("");

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                List<LinkedTreeMap<?, ?>> _lstCC = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstCC = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<CycleCountDTO> lstDto = new ArrayList<CycleCountDTO>();
                                List<String> _lstCCNames = new ArrayList<>();

                                for (int i = 0; i < _lstCC.size(); i++) {
                                    CycleCountDTO dto = new CycleCountDTO(_lstCC.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                ProgressDialogUtils.closeProgressDialog();

                                for (int i = 0; i < lstDto.size(); i++) {

                                    if (lstDto.get(i).getResult().equals("")) {

                                        //tvCount.setText(lstDto.get(i).getCount());

                                        //common.showUserDefinedAlertType(errorMessages.EMC_0063, getActivity(), getContext(), "Warning");

                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanLocation.setImageResource(R.drawable.check);

                                    } else {
                                        common.showUserDefinedAlertType(lstDto.get(i).getResult(), getActivity(), getContext(), "Error");
                                        return;
                                    }

                                }


                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
        }
    }

    // sending exception to the database
    public void logException() {
        try {

            String textFromFile = exceptionLoggerUtils.readFromFile(getActivity());

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Exception, getActivity());
            WMSExceptionMessage wmsExceptionMessage = new WMSExceptionMessage();
            wmsExceptionMessage.setWMSMessage(textFromFile);
            message.setEntityObject(wmsExceptionMessage);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.LogException(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                            // if any Exception throws
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    return;
                                }
                            } else {
                                LinkedTreeMap<String, String> _lResultvalue = new LinkedTreeMap<String, String>();
                                _lResultvalue = (LinkedTreeMap<String, String>) core.getEntityObject();
                                for (Map.Entry<String, String> entry : _lResultvalue.entrySet()) {
                                    if (entry.getKey().equals("Result")) {
                                        String Result = entry.getValue();
                                        if (Result.equals("0")) {
                                            ProgressDialogUtils.closeProgressDialog();
                                            return;
                                        } else {
                                            ProgressDialogUtils.closeProgressDialog();
                                            exceptionLoggerUtils.deleteFile(getActivity());
                                            return;
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {

                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            ProgressDialogUtils.closeProgressDialog();
                            //Log.d("Message", core.getEntityObject().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
            }
            barcodeReader.release();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                // Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_cycle_count));
    }

    @Override
    public void onDestroyView() {

        // Honeywell onDestroyView
        if (barcodeReader != null) {
            // unregister barcode event listener honeywell
            barcodeReader.removeBarcodeListener((BarcodeReader.BarcodeListener) this);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener((BarcodeReader.TriggerListener) this);
        }

        // Cipher onDestroyView
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", false);
        getActivity().sendBroadcast(RTintent);
        getActivity().unregisterReceiver(this.myDataReceiver);
        super.onDestroyView();

    }

    public void ValiDateMaterial(final String scannedData) {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.ScanDTO, getContext());
            ScanDTO scanDTO = new ScanDTO();
            scanDTO.setUserID(userId);
            scanDTO.setAccountID(accountId);
            scanDTO.setTenantID(String.valueOf(tenantId));
            scanDTO.setWarehouseID(String.valueOf(warehouseId));
            scanDTO.setScanInput(scannedData);
            //  scanDTO.setInboundID(inboundId);
            //inboundDTO.setIsOutbound("0");
            message.setEntityObject(scanDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.ValiDateMaterial(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                        if ((core.getType().toString().equals("Exception"))) {
                            List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                            _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                            WMSExceptionMessage owmsExceptionMessage = null;
                            for (int i = 0; i < _lExceptions.size(); i++) {

                                owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                            }
                            isRSNScanned = false;
                            cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                            ivScanSKU.setImageResource(R.drawable.fullscreen_img);
                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                        } else {
                            LinkedTreeMap<?, ?> _lResult = new LinkedTreeMap<>();
                            _lResult = (LinkedTreeMap<?, ?>) core.getEntityObject();

                            Log.v("ABCDE", new Gson().toJson(_lResult));

                            ScanDTO scanDTO1 = new ScanDTO(_lResult.entrySet());
                            ProgressDialogUtils.closeProgressDialog();
                            if (scanDTO1 != null) {
                                if (scanDTO1.getScanResult()) {

                                /* ----For RSN reference----
                                    0 Sku|1 BatchNo|2 SerialNO|3 MFGDate|4 EXpDate|5 ProjectRefNO|6 Kit Id|7 line No|8 MRP ---- For SKU with 9 MSP's

                                    0 Sku|1 BatchNo|2 SerialNO|3 KitId|4 lineNo  ---- For SKU with 5 MSP's   *//*
                                    // Eg. : ToyCar|1|bat1|ser123|12/2/2018|12/2/2019|0|001*/

                                    cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                    ivScanSKU.setImageResource(R.drawable.fullscreen_img);

                                    /*    if (scannedData.split("[|]").length != 5) {*/

                                    /*Materialcode = scanDTO1.getSkuCode();
                                    etBatch.setText(scanDTO1.getBatch());
                                    etSerial.setText(scanDTO1.getSerialNumber());
                                    etMfgDate.setText(scanDTO1.getMfgDate());
                                    etExpDate.setText(scanDTO1.getExpDate());
                                    etPrjRef.setText(scanDTO1.getPrjRef());
                                    etKidID.setText(scanDTO1.getKitID());
                                    etMRP.setText(scanDTO1.getMrp());
                                    lineNo = scanDTO1.getLineNumber();*/
                                    //supplierInvoiceDetailsId = scanDTO1.getSupplierInvoiceDetailsID();

                                    materialCode = scanDTO1.getSkuCode();

                                    lblScannedSku.setText(materialCode);
                                    etBatch.setText(scanDTO1.getBatch());
                                    etSerial.setText(scanDTO1.getSerialNumber());
                                    etMfgDate.setText(scanDTO1.getMfgDate());
                                    etExpDate.setText(scanDTO1.getExpDate());
                                    etProjectRef.setText(scanDTO1.getPrjRef());
                                    etCCMRP.setText(scanDTO1.getMrp());

                                    isRSNScanned = true;
                                    cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanSKU.setImageResource(R.drawable.check);

                                    checkMaterialAvailablilty();

                                } else {
                                    isRSNScanned = false;
                                    cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanSKU.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Warning");
                                }
                            } else {
                                common.showUserDefinedAlertType("Error while getting data", getActivity(), getContext(), "Error");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
        }
    }

    public void ValidateLocation(final String scannedData) {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.ScanDTO, getContext());
            ScanDTO scanDTO = new ScanDTO();
            scanDTO.setUserID(userId);
            scanDTO.setAccountID(accountId);
            scanDTO.setTenantID(String.valueOf(tenantId));
            scanDTO.setWarehouseID(String.valueOf(warehouseId));
            scanDTO.setScanInput(scannedData);
            scanDTO.setCycleCount(true);
            // scanDTO.setInboundID(inboundId);
            // inboundDTO.setIsOutbound("0");
            message.setEntityObject(scanDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.ValidateLocation(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                        if ((core.getType().toString().equals("Exception"))) {
                            List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                            _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                            WMSExceptionMessage owmsExceptionMessage = null;
                            for (int i = 0; i < _lExceptions.size(); i++) {
                                owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                            }

                            isValidLocation = false;
                            cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanLocation.setImageResource(R.drawable.invalid_cross);
                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                        } else {
                            LinkedTreeMap<?, ?> _lResult = new LinkedTreeMap<>();
                            _lResult = (LinkedTreeMap<?, ?>) core.getEntityObject();

                            ScanDTO scanDTO1 = new ScanDTO(_lResult.entrySet());

                            if (scanDTO1 != null) {
                                if (scanDTO1.getScanResult()) {
                                    isValidLocation = true;
                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanLocation.setImageResource(R.drawable.check);
                                    etLocation.setText(scannedData);
                                    isBlockedLocation();
                                } else {
                                    isValidLocation = false;
                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanLocation.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0010, getActivity(), getContext(), "Warning");
                                }
                            } else {
                                common.showUserDefinedAlertType("Error while getting data", getActivity(), getContext(), "Error");
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
        }
    }

    public void ValidatePallet(final String scannedData) {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.ScanDTO, getContext());
            ScanDTO scanDTO = new ScanDTO();
            scanDTO.setUserID(userId);
            scanDTO.setAccountID(accountId);
            scanDTO.setTenantID(String.valueOf(tenantId));
            scanDTO.setWarehouseID(String.valueOf(warehouseId));
            scanDTO.setScanInput(scannedData);
            // scanDTO.setInboundID(inboundId);
            //inboundDTO.setIsOutbound("0");
            message.setEntityObject(scanDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.ValidatePallet(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                        if ((core.getType().toString().equals("Exception"))) {
                            List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                            _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                            WMSExceptionMessage owmsExceptionMessage = null;
                            for (int i = 0; i < _lExceptions.size(); i++) {
                                owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                            }

                            isPalletScanned = false;
                            cvScanContainer.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanContainer.setImageResource(R.drawable.invalid_cross);
                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                        } else {

                            LinkedTreeMap<?, ?> _lResult = new LinkedTreeMap<>();
                            _lResult = (LinkedTreeMap<?, ?>) core.getEntityObject();

                            ScanDTO scanDTO1 = new ScanDTO(_lResult.entrySet());
                            ProgressDialogUtils.closeProgressDialog();
                            
                            if (scanDTO1 != null) {
                                if (scanDTO1.getScanResult()) {
                                    isPalletScanned = true;
                                    cvScanContainer.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanContainer.setImageResource(R.drawable.check);
                                    etContainer.setText(scannedData);
                                    chekPalletLocation();
                                } else {
                                    isPalletScanned = false;
                                    cvScanContainer.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanContainer.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Warning");
                                }
                            } else {
                                isPalletScanned = false;
                                common.showUserDefinedAlertType("Error while getting data", getActivity(), getContext(), "Error");
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        storageLoc = spinnerSelectSloc.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}