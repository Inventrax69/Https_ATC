package com.inventrax.atc.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
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
import com.inventrax.atc.adapters.LoadSheetSOListAdapter;
import com.inventrax.atc.common.Common;
import com.inventrax.atc.common.constants.EndpointConstants;
import com.inventrax.atc.common.constants.ErrorMessages;
import com.inventrax.atc.interfaces.ApiInterface;
import com.inventrax.atc.pojos.LoadDTO;
import com.inventrax.atc.pojos.OutbountDTO;
import com.inventrax.atc.pojos.ScanDTO;
import com.inventrax.atc.pojos.WMSCoreMessage;
import com.inventrax.atc.pojos.WMSExceptionMessage;
import com.inventrax.atc.services.RetrofitBuilderHttpsEx;
import com.inventrax.atc.util.DialogUtils;
import com.inventrax.atc.util.ExceptionLoggerUtils;
import com.inventrax.atc.util.FragmentUtils;
import com.inventrax.atc.util.ProgressDialogUtils;
import com.inventrax.atc.util.SoundUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by ch Anil on 27/03/2020.
 */

public class LoadGenerationFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_009";
    private View rootView;
    Button  btnCloseTwo, btnCloseThree, btnCreateNew, btnCreate;
    RelativeLayout rlLoadingTwo, rlLoadListThree;
    TextView lblLoadSheetNo;
    CardView cvScanSONumber;
    ImageView ivScanSONumber;
    EditText lblDrName, lblDrNo, lblVehicleNo, lblVehicleType, lblReceivedQty;
    TextInputLayout txtInputLayoutQty;
    String userId = null, scanType = null, accountId = "";
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private Common common;
    private WMSCoreMessage core;
    List<LoadDTO> lstloaddata = null;
    String loadSheetNo = "", loadNoCustomerCode = null;
    private String Materialcode = null;
    private boolean IsUserConfirmedRedo = false;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    RecyclerView recycler_view_so;
    LinearLayoutManager linearLayoutManager;
    Button btnDeleteSO, btnGenerate, btnClear;
    SoundUtils soundUtils;
    TextView lblScannedSku, lblBatchNo, lblserialNo, lblMfgDate, lblExpDate, lblProjectRefNo, lblMRP;
    LoadSheetSOListAdapter loadSheetSOListAdapter;
    List<OutbountDTO> outbountDTOS;
    String SONumber="";
    boolean isSOEnabled=true;

    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public void myScannedData(Context context, String barcode) {
        try {
            ProcessScannedinfo(barcode.trim());
        } catch (Exception e) {
            //  Toast.makeText(context, ""+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public LoadGenerationFragment() { }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_load_sheet_generation, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;

    }

    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        scanType = sp.getString("scanType", "");
        accountId = sp.getString("AccountId", "");

        btnDeleteSO=(Button) rootView.findViewById(R.id.btnDeleteSO);
        btnGenerate=(Button) rootView.findViewById(R.id.btnGenerate);
        btnClear=(Button) rootView.findViewById(R.id.btnClear);

        lblDrName = (EditText) rootView.findViewById(R.id.lblDrName);
        lblDrNo = (EditText) rootView.findViewById(R.id.lblDrNo);
        lblVehicleNo = (EditText) rootView.findViewById(R.id.lblVehicleNo);
        lblVehicleType = (EditText) rootView.findViewById(R.id.lblVehicleType);

        lblDrName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.barcode = "";
                    return true;
                }
                return false;
            }
        });

        lblDrNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.barcode = "";
                    return true;
                }
                return false;
            }
        });

        lblVehicleNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.barcode = "";
                    return true;
                }
                return false;
            }
        });

        lblVehicleType.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.barcode = "";
                    return true;
                }
                return false;
            }
        });

        recycler_view_so = (RecyclerView) rootView.findViewById(R.id.recycler_view_obd);
        recycler_view_so.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        // use a linear layout manager
        recycler_view_so.setLayoutManager(linearLayoutManager);

        cvScanSONumber = (CardView) rootView.findViewById(R.id.cvScanSONumber);
        ivScanSONumber = (ImageView) rootView.findViewById(R.id.ivScanSONumber);


        soundUtils = new SoundUtils();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        gson = new GsonBuilder().create();
        common = new Common();
        core = new WMSCoreMessage();

        ProgressDialogUtils.closeProgressDialog();
        common.setIsPopupActive(false);

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


        btnDeleteSO.setOnClickListener(this);
        btnGenerate.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        cvScanSONumber.setOnClickListener(this);

        outbountDTOS=new ArrayList<>();
        loadSheetSOListAdapter = new LoadSheetSOListAdapter(getActivity(), outbountDTOS, new LoadSheetSOListAdapter.OnCheckChangeListner() {
            @Override
            public void onCheckChange(int pos, boolean isChecked) {
                outbountDTOS.get(pos).setChecked(isChecked);
            }
        });

        recycler_view_so.setAdapter(loadSheetSOListAdapter);

        cvScanSONumber.setCardBackgroundColor(getResources().getColor(R.color.grey));
        ivScanSONumber.setImageResource(R.drawable.fullscreen_img);
        lblDrName.setEnabled(true);
        lblDrNo.setEnabled(true);
        lblVehicleNo.setEnabled(true);
        lblVehicleType.setEnabled(true);
        lblDrName.clearFocus();
        lblDrNo.clearFocus();
        lblVehicleNo.clearFocus();
        lblVehicleType.clearFocus();

        lblDrName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!lblDrName.isEnabled()){
                    common.showUserDefinedAlertType("Please enable by clicking on scan SO number icon", getActivity(), getActivity(), "Warning");
                }
            }
        });

        lblDrNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!lblDrNo.isEnabled()){
                    common.showUserDefinedAlertType("Please enable by clicking on scan SO number icon", getActivity(), getActivity(), "Warning");
                }
            }
        });

        lblVehicleNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!lblVehicleNo.isEnabled()){
                    common.showUserDefinedAlertType("Please enable by clicking on scan SO number icon", getActivity(), getActivity(), "Warning");
                }
            }
        });

        lblVehicleType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!lblVehicleType.isEnabled()){
                    common.showUserDefinedAlertType("Please enable by clicking on scan SO number icon", getActivity(), getActivity(), "Warning");
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.cvScanSONumber:

                if(isSOEnabled){

                    isSOEnabled=false;
                    cvScanSONumber.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                    ivScanSONumber.setImageResource(R.drawable.fullscreen_img);
                    lblDrName.setEnabled(false);
                    lblDrNo.setEnabled(false);
                    lblVehicleNo.setEnabled(false);
                    lblVehicleType.setEnabled(false);
                    lblDrName.clearFocus();
                    lblDrNo.clearFocus();
                    lblVehicleNo.clearFocus();
                    lblVehicleType.clearFocus();

                }else{

                    isSOEnabled=true;
                    cvScanSONumber.setCardBackgroundColor(getResources().getColor(R.color.grey));
                    ivScanSONumber.setImageResource(R.drawable.fullscreen_img);
                    lblDrName.setEnabled(true);
                    lblDrNo.setEnabled(true);
                    lblVehicleNo.setEnabled(true);
                    lblVehicleType.setEnabled(true);
                    lblDrName.clearFocus();
                    lblDrNo.clearFocus();
                    lblVehicleNo.clearFocus();
                    lblVehicleType.clearFocus();

                }

                break;

            case R.id.btnDeleteSO:

                Iterator<OutbountDTO> iterator = outbountDTOS.iterator();
                while (iterator.hasNext()) {
                    OutbountDTO p = iterator.next();
                    if (p.isChecked()) {
                        iterator.remove();
                    }
                }

                loadSheetSOListAdapter.notifyDataSetChanged();

                break;

            case R.id.btnClear:
                lblDrName.setText("");
                lblDrNo.setText("");
                lblVehicleNo.setText("");
                lblVehicleType.setText("");
                lblDrName.clearFocus();
                lblDrNo.clearFocus();
                lblVehicleNo.clearFocus();
                lblVehicleType.clearFocus();
                break;

            case R.id.btnGenerate:

                if(lblDrName.getText().toString().isEmpty()){
                    common.showUserDefinedAlertType("Enter Driver Name", getActivity(), getActivity(), "Warning");
                    return;
                }

                if(lblDrNo.getText().toString().isEmpty()){
                    common.showUserDefinedAlertType("Enter Driver Number", getActivity(), getActivity(), "Warning");
                    return;
                }

                if(lblVehicleNo.getText().toString().isEmpty()){
                    common.showUserDefinedAlertType("Enter Vehicle Number", getActivity(), getActivity(), "Warning");
                    return;
                }

                if(lblVehicleType.getText().toString().isEmpty()){
                    common.showUserDefinedAlertType("Enter Vehicle Type", getActivity(), getActivity(), "Warning");
                    return;
                }
                
                SONumber="";
                for(int i=0;i<outbountDTOS.size();i++){
                        SONumber += outbountDTOS.get(i).getSONumber()+",";
                }


                if(SONumber.isEmpty()){
                    common.showUserDefinedAlertType("Please scan atleast one SO number", getActivity(), getActivity(), "Warning");
                    return;
                }else{
                    SONumber = SONumber.substring(0, SONumber.length() - 1);
                }

                GenerateLoadSheet(SONumber) ;
                break;

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
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) { }

    @Override
    public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent) { }

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

        if (scannedData != null) {

            if (ProgressDialogUtils.isProgressActive() || Common.isPopupActive()) {
                common.showUserDefinedAlertType(errorMessages.EMC_082, getActivity(), getContext(), "Warning");
                return;
            }

            if (!ProgressDialogUtils.isProgressActive()) {

                if(!isSOEnabled){
                    ValidateSO(scannedData);
                }else{
                    common.showUserDefinedAlertType("Please enable by clicking on scan SO number icon", getActivity(), getActivity(), "Warning");
                }

            }

        }
    }

    private void ScanData(String scannedData) {
        OutbountDTO outbountDTO=new OutbountDTO();
        outbountDTO.setSONumber(scannedData);
        outbountDTOS.add(outbountDTO);
        loadSheetSOListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void ValidateSO(final String scannedData) {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.ScanDTO, getContext());
            ScanDTO scanDTO = new ScanDTO();
            scanDTO.setUserID(userId);
            scanDTO.setAccountID(accountId);
            // scanDTO.setTenantID(String.valueOf(tenantID));
            // scanDTO.setWarehouseID(String.valueOf(warehouseID));
            scanDTO.setScanInput(scannedData);
            // inboundDTO.setIsOutbound("0");
            message.setEntityObject(scanDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);
            try {
                // Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.ValidateSO(message);
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

                            cvScanSONumber.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanSONumber.setImageResource(R.drawable.warning_img);
                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                        } else {
                            LinkedTreeMap<?, ?> _lResult = new LinkedTreeMap<>();
                            _lResult = (LinkedTreeMap<?, ?>) core.getEntityObject();

                            ScanDTO scanDTO1 = new ScanDTO(_lResult.entrySet());
                            ProgressDialogUtils.closeProgressDialog();
                            if (scanDTO1 != null) {
                                if (scanDTO1.getScanResult()) {

                                /* ----For RSN reference----
                                    0 Sku|1 BatchNo|2 SerialNO|3 MFGDate|4 EXpDate|5 ProjectRefNO|6 Kit Id|7 line No|8 MRP ---- For SKU with 9 MSP's

                                    0 Sku|1 BatchNo|2 SerialNO|3 KitId|4 lineNo  ---- For SKU with 5 MSP's   *//*
                                    // Eg. : ToyCar|1|bat1|ser123|12/2/2018|12/2/2019|0|001*/

                                    cvScanSONumber.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanSONumber.setImageResource(R.drawable.check);

                                    for(int i=0 ;i<outbountDTOS.size();i++){
                                        if(outbountDTOS.get(i).getSONumber().equals(scannedData)){
                                            common.showUserDefinedAlertType("Already Scanned SO Number", getActivity(), getContext(), "Warning");
                                            return;
                                        }
                                    }

                                    OutbountDTO outbountDTO=new OutbountDTO();
                                    outbountDTO.setSONumber(scannedData);
                                    outbountDTOS.add(outbountDTO);
                                    loadSheetSOListAdapter.notifyDataSetChanged();


                                } else {
                                    // lblScannedSku.setText("");
                                    cvScanSONumber.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanSONumber.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType("Invalid SO Number", getActivity(), getContext(), "Warning");
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Load generation");
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

    public  void GenerateLoadSheet(String SONumber) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound,getActivity());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            outbountDTO.setTenatID(userId);
            outbountDTO.setAccountID(accountId);
            outbountDTO.setVehicle(lblVehicleNo.getText().toString());
            outbountDTO.setSONumber(SONumber);
            outbountDTO.setDriverNo(lblDrNo.getText().toString());
            outbountDTO.setDriverName(lblDrName.getText().toString());
            outbountDTO.setLRnumber(lblVehicleType.getText().toString());
            message.setEntityObject(outbountDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GenerateLoadSheet(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_01",getActivity());
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
                                List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<OutbountDTO> lstDto = new ArrayList<OutbountDTO>();

                                for (int i = 0; i < _lResult.size(); i++) {
                                    OutbountDTO dto = new OutbountDTO(_lResult.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                if(lstDto.size()>0){
                                    NewLoadSheetFragment fragment = new NewLoadSheetFragment();
                                    Bundle args = new Bundle();
                                    args.putString("LoadSheetNo", lstDto.get(0).getLoadRefNo());
                                    FragmentUtils.replaceFragmentWithBackStackWithArguments(getActivity(), R.id.container_body, fragment ,args);
                                }else{
                                    common.showUserDefinedAlertType("Load No. Not Created", getActivity(), getActivity(), "Warning");
                                }
                            }

                            ProgressDialogUtils.closeProgressDialog();
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_02",getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }

                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        ProgressDialogUtils.closeProgressDialog();
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_03",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_04",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
        }
    }

}