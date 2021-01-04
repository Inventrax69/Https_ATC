package com.inventrax.atc.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.inventrax.atc.common.Common;
import com.inventrax.atc.common.constants.EndpointConstants;
import com.inventrax.atc.common.constants.ErrorMessages;
import com.inventrax.atc.interfaces.ApiInterface;
import com.inventrax.atc.pojos.OutbountDTO;
import com.inventrax.atc.pojos.ScanDTO;
import com.inventrax.atc.pojos.WMSCoreMessage;
import com.inventrax.atc.pojos.WMSExceptionMessage;
import com.inventrax.atc.services.RetrofitBuilderHttpsEx;
import com.inventrax.atc.util.DialogUtils;
import com.inventrax.atc.util.ExceptionLoggerUtils;
import com.inventrax.atc.util.FragmentUtils;
import com.inventrax.atc.util.ProgressDialogUtils;
import com.inventrax.atc.util.ScanValidator;
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
 * Created by Prasanna ch on 06/26/2018.
 */

public class OutboundRevertDetailsFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_OBD_PICKING";
    private View rootView;
    ImageView ivScanCarton, ivScanRSN;
    Button  btnRevert,btnClear;
    TextView lblPickListNo, lblScannedSku,lblStatus,lblSONumber;
    TextView lblSKuNo, lblLocationNo, lblMRP, lblrsnNoNew, lblMfgDate, lblExpDate, lblProjectRefNo, lblassignedQty, lblserialNo, lblBatchNo,lblPLQty;
    CardView cvScanCarton, cvScanRSN;
    EditText lblReceivedQty;
    boolean IsStrictlycomplaince = false;
    String Mcode = null, NewMcode = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    String userId = null, scanType = null;
    private Common common;
    private WMSCoreMessage core;
    private String pickOBDno = "", pickobdId = "",soNumber="",soHeaderId="";
    int count = 0;
    private ScanValidator scanValidator;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    EditText etCarton, etSKU;
    boolean isValidLocation = false;
    boolean isPalletScanned = false;
    boolean isToPalletScanned = false;
    boolean pickValidateComplete = false;
    boolean isRSNScanned = false;
    String assignedId = "", KitId = "", soDetailsId = "", Lineno = "", POSOHeaderId = "", sLoc = "",accountId = "",Status="";
    int recQty, totalQty;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    String printer = "Select Printer", skipReason = "", pickedQty = "", location = "",BusinessType="";
    SoundUtils soundUtils;
    LinearLayout linearCon,linearSKU;

    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public OutboundRevertDetailsFragment() {

    }

    public void myScannedData(Context context, String barcode){
        try {
            ProcessScannedinfo(barcode.trim());
        }catch (Exception e){
          //  Toast.makeText(context, ""+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_outbound_revert, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    private void loadFormControls() {

        cvScanCarton = (CardView) rootView.findViewById(R.id.cvScanCarton);
        cvScanRSN = (CardView) rootView.findViewById(R.id.cvScanRSN);

        ivScanCarton = (ImageView) rootView.findViewById(R.id.ivScanCarton);
        ivScanRSN = (ImageView) rootView.findViewById(R.id.ivScanRSN);

        btnRevert = (Button) rootView.findViewById(R.id.btnRevert);
        btnClear = (Button) rootView.findViewById(R.id.btnClear);

        lblPickListNo = (TextView) rootView.findViewById(R.id.lblPickListNo);
        lblSKuNo = (TextView) rootView.findViewById(R.id.lblSKUSuggested);
        lblLocationNo = (TextView) rootView.findViewById(R.id.lblLocationSuggested);
        lblMRP = (TextView) rootView.findViewById(R.id.lblMRP);

        etCarton = (EditText) rootView.findViewById(R.id.etCarton);
        etSKU = (EditText) rootView.findViewById(R.id.etSKU);



        linearCon = (LinearLayout) rootView.findViewById(R.id.linearCon);
        linearSKU = (LinearLayout) rootView.findViewById(R.id.linearSKU);


        lblReceivedQty = (EditText) rootView.findViewById(R.id.lblReceivedQty);
        lblMfgDate = (TextView) rootView.findViewById(R.id.lblMfgDate);
        lblExpDate = (TextView) rootView.findViewById(R.id.lblExpDate);
        lblProjectRefNo = (TextView) rootView.findViewById(R.id.lblProjectRefNo);
        lblserialNo = (TextView) rootView.findViewById(R.id.lblserialNo);
        lblBatchNo = (TextView) rootView.findViewById(R.id.lblBatchNo);
        lblassignedQty = (TextView) rootView.findViewById(R.id.lblRequiredQty);
        lblStatus = (TextView) rootView.findViewById(R.id.lblStatus);
        lblSONumber = (TextView) rootView.findViewById(R.id.lblSONumber);
        lblPLQty = (TextView) rootView.findViewById(R.id.lblPLQty);


        lblReceivedQty.clearFocus();
        lblReceivedQty.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        scanType = sp.getString("scanType", "");
        accountId = sp.getString("AccountId", "");


        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        gson = new GsonBuilder().create();
        btnRevert.setOnClickListener(this);
        btnClear.setOnClickListener(this);

        common = new Common();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();
        soundUtils = new SoundUtils();
        ProgressDialogUtils.closeProgressDialog();
        Common.setIsPopupActive(false);


        pickOBDno = getArguments().getString("pickOBDno");
        pickobdId = getArguments().getString("pickobdId");
        soNumber = getArguments().getString("soNumber");
        soHeaderId = getArguments().getString("soHeaderId");
        BusinessType = getArguments().getString("BusinessType");
        Status = getArguments().getString("Status");
        lblPickListNo.setText(pickOBDno);
        lblStatus.setText(Status);
        lblSONumber.setText(soNumber);

        if(BusinessType.equals("E-Commerce")){
            linearCon.setVisibility(View.GONE);
            etCarton.setVisibility(View.INVISIBLE);
            etCarton.setText("");
            linearSKU.setVisibility(View.VISIBLE);
        }else{
            linearCon.setVisibility(View.VISIBLE);
            etCarton.setVisibility(View.VISIBLE);
            linearSKU.setVisibility(View.VISIBLE);
        }


        if(Status.toLowerCase().contains("packing")){
            lblPLQty.setText("Pack Qty");
        }else if(Status.toLowerCase().contains("loading")){
            lblPLQty.setText("load Qty");
        }else{
            lblPLQty.setText("Qty");
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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnRevert:
                if(lblReceivedQty.getText().toString().equals("")){
                    common.showUserDefinedAlertType("Please enter received qty", getActivity(), getContext(), "Warning");
                    return;
                }
                if(lblassignedQty.getText().toString().equals("")){
                    common.showUserDefinedAlertType("Qty not assigned", getActivity(), getContext(), "Warning");
                    return;
                }
                if(Integer.parseInt(lblReceivedQty.getText().toString())<=0){
                    common.showUserDefinedAlertType("Please enter valid qty", getActivity(), getContext(), "Warning");
                    return;
                }
                if(Integer.parseInt(lblReceivedQty.getText().toString()) >
                        Integer.parseInt(lblassignedQty.getText().toString().split("[.]")[0])){

                    common.showUserDefinedAlertType("Entered qty is more than given qty.", getActivity(), getContext(), "Warning");
                    return;
                }

                UpsertHHTOBDRevert();
                break;

            case R.id.btnClear:
                clearData();
                break;

        }
    }

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
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
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

    public void ClearFields() {

        lblSKuNo.setText("");
        etCarton.setText("");
        etSKU.setText("");

        lblassignedQty.setText("");
        lblBatchNo.setText("");
        lblReceivedQty.setText("");
        lblMfgDate.setText("");
        lblExpDate.setText("");
        lblProjectRefNo.setText("");
        lblserialNo.setText("");
        lblMRP.setText("");



    }

    public void clearData() {

        etSKU.setText("");
        etCarton.setText("");

        lblassignedQty.clearFocus();
        lblassignedQty.setText("");
        lblBatchNo.setText("");
        lblReceivedQty.setText("");
        lblReceivedQty.clearFocus();
        lblMfgDate.setText("");
        lblExpDate.setText("");
        lblProjectRefNo.setText("");
        lblserialNo.setText("");
        lblMRP.setText("");

        btnRevert.setEnabled(false);

        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanRSN.setImageResource(R.drawable.fullscreen_img);

        cvScanCarton.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanCarton.setImageResource(R.drawable.fullscreen_img);

    }

    public void clearData1() {

        etSKU.setText("");

        lblassignedQty.clearFocus();
        lblassignedQty.setText("");
        lblBatchNo.setText("");
        lblReceivedQty.setText("");
        lblReceivedQty.clearFocus();
        lblMfgDate.setText("");
        lblExpDate.setText("");
        lblProjectRefNo.setText("");
        lblserialNo.setText("");
        lblMRP.setText("");

        btnRevert.setEnabled(false);

        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanRSN.setImageResource(R.drawable.fullscreen_img);

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

        if (scannedData != null) {

            if(BusinessType.equals("E-Commerce")){
                // Scan SKU
                ValiDateMaterial(scannedData);
            }else{
                if(etCarton.getText().toString().isEmpty()){
                    // Scan Carton
                    GetRevertCartonCheck(scannedData);
                }else{
                    // Scan SKU
                    ValiDateMaterial(scannedData);
                }
            }

        } else {
            common.showUserDefinedAlertType(errorMessages.EMC_0030, getActivity(), getContext(), "Error");
        }
    }

    public void ValiDateMaterial(final String scannedData) {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.ScanDTO, getContext());
            ScanDTO scanDTO = new ScanDTO();
            scanDTO.setUserID(userId);
            scanDTO.setAccountID(accountId);
            // scanDTO.setTenantID(String.valueOf(tenantID));
            //scanDTO.setWarehouseID(String.valueOf(warehouseID));
            scanDTO.setScanInput(scannedData);
            scanDTO.setObdNumber(lblPickListNo.getText().toString());
            //inboundDTO.setIsOutbound("0");
            message.setEntityObject(scanDTO);

            Log.v("ABCDE",new Gson().toJson(message));

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

                            cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                            ivScanRSN.setImageResource(R.drawable.fullscreen_img);
                            lblReceivedQty.setText("");
                            lblReceivedQty.setEnabled(false);
                            lblReceivedQty.clearFocus();
                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                        } else {
                            LinkedTreeMap<?, ?>_lResult = new LinkedTreeMap<>();
                            _lResult = (LinkedTreeMap<?, ?>) core.getEntityObject();

                            ScanDTO scanDTO1=new ScanDTO(_lResult.entrySet());
                            ProgressDialogUtils.closeProgressDialog();
                            if(scanDTO1!=null){
                                if(scanDTO1.getScanResult()){

                                /* ----For RSN reference----
                                    0 Sku|1 BatchNo|2 SerialNO|3 MFGDate|4 EXpDate|5 ProjectRefNO|6 Kit Id|7 line No|8 MRP ---- For SKU with 9 MSP's

                                    0 Sku|1 BatchNo|2 SerialNO|3 KitId|4 lineNo  ---- For SKU with 5 MSP's   *//*
                                    // Eg. : ToyCar|1|bat1|ser123|12/2/2018|12/2/2019|0|001*/


                                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanRSN.setImageResource(R.drawable.check);
                                    etSKU.setText(scanDTO1.getSkuCode());
                                    lblBatchNo.setText(scanDTO1.getBatch());
                                    lblserialNo.setText(scanDTO1.getSerialNumber());
                                    lblMfgDate.setText(scanDTO1.getMfgDate());
                                    lblExpDate.setText(scanDTO1.getExpDate());
                                    lblProjectRefNo.setText(scanDTO1.getPrjRef());
                                    lblMRP.setText(scanDTO1.getMrp());

                                    GetScanqtyvalidation("1");




/*                                    if(scanDTO1.getSkuCode().equalsIgnoreCase(lblSKuNo.getText().toString().trim())){

                                        if((lblBatchNo.getText().toString().equalsIgnoreCase(scanDTO1.getBatch()) || scanDTO1.getBatch()==null
                                                || scanDTO1.getBatch().equalsIgnoreCase("") || scanDTO1.getBatch().isEmpty() )&&
                                                lblserialNo.getText().toString().equalsIgnoreCase(scanDTO1.getSerialNumber()) &&
                                                lblMfgDate.getText().toString().equalsIgnoreCase(scanDTO1.getMfgDate()) &&
                                                lblExpDate.getText().toString().equalsIgnoreCase(scanDTO1.getExpDate())
                                        ) {

*//*                                             &&
                                              lblMfgDate.getText().toString().equalsIgnoreCase(scanDTO1.getMfgDate()) &&
                                                    lblExpDate.getText().toString().equalsIgnoreCase(scanDTO1.getExpDate()
                                             lblProjectRefNo.getText().toString().equalsIgnoreCase(scanDTO1.getPrjRef())*//*





                                        }else{
                                            common.showUserDefinedAlertType(errorMessages.EMC_0079,getActivity(),getContext(),"Error");
                                        }

                                    }else {
                                        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanRSN.setImageResource(R.drawable.warning_img);
                                        common.showUserDefinedAlertType(errorMessages.EMC_0029, getActivity(), getContext(), "Error");
                                    }*/

                                } else{
                                    lblReceivedQty.setText("");
                                    lblReceivedQty.setEnabled(false);
                                    lblReceivedQty.clearFocus();
                                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanRSN.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Warning");
                                }
                            }else{
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

    public void GetRevertCartonCheck(final String scannedData) {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO=new OutbountDTO();
            outbountDTO.setUserId(userId);
            outbountDTO.setAccountID(accountId);
            outbountDTO.setOutboundID(pickobdId);
            outbountDTO.setSOHeaderID(soHeaderId);
            outbountDTO.setCartonSerialNo(scannedData);
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetRevertCartonCheck(message);
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

                            etCarton.setText("");
                            cvScanCarton.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanCarton.setImageResource(R.drawable.invalid_cross);
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

                            ProgressDialogUtils.closeProgressDialog();
                            if(lstDto.size()>0){
                                OutbountDTO outbountDTO1=lstDto.get(0);
                            if(outbountDTO1!=null){
                                if(outbountDTO1.getStatus().equalsIgnoreCase("Success")){
                                    etCarton.setText(scannedData);
                                    //ValidatePalletCode();
                                    cvScanCarton.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanCarton.setImageResource(R.drawable.check);
                                } else{
                                    etCarton.setText("");
                                    cvScanCarton.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanCarton.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Warning");
                                }
                            }else{
                                common.showUserDefinedAlertType("Error while getting data", getActivity(), getContext(), "Error");
                            }
                            }else{
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

     public void GetScanqtyvalidation(final String type) {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO=new OutbountDTO();
            outbountDTO.setUserId(userId);
            outbountDTO.setAccountID(accountId);
            outbountDTO.setOutboundID(pickobdId);
            outbountDTO.setSOHeaderID(soHeaderId);
            outbountDTO.setmCode(etSKU.getText().toString());
            outbountDTO.setBatchNo(lblBatchNo.getText().toString());
            outbountDTO.setMfgDate(lblMfgDate.getText().toString());
            outbountDTO.setExpDate(lblExpDate.getText().toString());
            outbountDTO.setProjectNo(lblProjectRefNo.getText().toString());
            outbountDTO.setCartonSerialNo(etCarton.getText().toString());
            outbountDTO.setMRP(lblMRP.getText().toString());

            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetScanqtyvalidation(message);
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

                            etSKU.setText("");
                            lblReceivedQty.setText("");
                            lblReceivedQty.setEnabled(false);
                            lblReceivedQty.clearFocus();
                            cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanRSN.setImageResource(R.drawable.invalid_cross);
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

                            ProgressDialogUtils.closeProgressDialog();
                            if(lstDto.size()>0){
                                OutbountDTO outbountDTO1=lstDto.get(0);
                                if(outbountDTO1!=null){
                                    if(outbountDTO1.getStatus().equalsIgnoreCase("Success")){

                                        lblassignedQty.setText(outbountDTO1.getSOQty());

                                        if(Integer.parseInt(lblassignedQty.getText().toString().split("[.]")[0])==0){
                                            Common.setIsPopupActive(true);
                                            soundUtils.alertSuccess(getActivity(), getActivity());
                                            DialogUtils.showAlertDialog(getActivity(), " Revert Success", outbountDTO1.getStatus(), R.drawable.success,new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which)
                                                {
                                                    switch (which) {
                                                        case DialogInterface.BUTTON_POSITIVE:
                                                            Common.setIsPopupActive(false);
                                                            FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                                                            break;
                                                    }
                                                }
                                            });
                                            return;
                                        }

                                        if (scanType.equalsIgnoreCase("Auto")) {
                                            lblReceivedQty.setText("1");


                                            if(lblReceivedQty.getText().toString().equals("")){
                                                common.showUserDefinedAlertType("Please enter received qty", getActivity(), getContext(), "Warning");
                                                return;
                                            }
                                            if(lblassignedQty.getText().toString().equals("")){
                                                common.showUserDefinedAlertType("Qty not assigned", getActivity(), getContext(), "Warning");
                                                return;
                                            }

                                            if(Integer.parseInt(lblReceivedQty.getText().toString())<=0){
                                                common.showUserDefinedAlertType("Please enter valid qty", getActivity(), getContext(), "Warning");
                                                return;
                                            }
                                            if(Integer.parseInt(lblReceivedQty.getText().toString()) >
                                                    Integer.parseInt(lblassignedQty.getText().toString().split("[.]")[0])){

                                                common.showUserDefinedAlertType("Entered qty is more than given qty.", getActivity(), getContext(), "Warning");
                                                return;
                                            }

                                            UpsertHHTOBDRevert();

                                        } else {

                                            if(type.equals("1")){
                                                lblReceivedQty.setEnabled(true);
                                                btnRevert.setEnabled(true);
                                                soundUtils.alertWarning(getActivity(), getContext());
                                                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0073);
                                            }else{
                                                lblReceivedQty.setEnabled(true);
                                                btnRevert.setEnabled(true);
                                            }

                                        }
                                    } else{
                                        etSKU.setText("");
                                        lblReceivedQty.setText("");
                                        lblReceivedQty.setEnabled(false);
                                        lblReceivedQty.clearFocus();
                                        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                        ivScanRSN.setImageResource(R.drawable.fullscreen_img);
                                        common.showUserDefinedAlertType(outbountDTO1.getStatus(), getActivity(), getContext(), "Warning");
                                    }
                                }
                                else{
                                    etSKU.setText("");
                                    lblReceivedQty.setText("");
                                    lblReceivedQty.setEnabled(false);
                                    lblReceivedQty.clearFocus();
                                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                    ivScanRSN.setImageResource(R.drawable.fullscreen_img);
                                    common.showUserDefinedAlertType("Error while getting data", getActivity(), getContext(), "Error");
                                }

                            }
                            else{
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

     public void UpsertHHTOBDRevert() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO=new OutbountDTO();
            outbountDTO.setUserId(userId);
            outbountDTO.setAccountID(accountId);
            outbountDTO.setOutboundID(pickobdId);
            outbountDTO.setSOHeaderID(soHeaderId);
            outbountDTO.setmCode(etSKU.getText().toString());
            outbountDTO.setBatchNo(lblBatchNo.getText().toString());
            outbountDTO.setMfgDate(lblMfgDate.getText().toString());
            outbountDTO.setExpDate(lblExpDate.getText().toString());
            outbountDTO.setProjectNo(lblProjectRefNo.getText().toString());
            outbountDTO.setPackedQty(lblReceivedQty.getText().toString());
            outbountDTO.setCartonSerialNo(etCarton.getText().toString());
            outbountDTO.setMRP(lblMRP.getText().toString());
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.UpsertHHTOBDRevert(message);
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

                            ProgressDialogUtils.closeProgressDialog();
                            if(lstDto.size()>0) {
                                OutbountDTO outbountDTO1 = lstDto.get(0);
                                if (outbountDTO1 != null) {
                                    if (outbountDTO1.getStatus().equalsIgnoreCase("Revert successfully done")) {
                                        Common.setIsPopupActive(true);
                                        soundUtils.alertSuccess(getActivity(), getActivity());
                                        DialogUtils.showAlertDialog(getActivity(), "Success", outbountDTO1.getStatus(), R.drawable.success,new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        Common.setIsPopupActive(false);
                                                        if(Integer.parseInt(lblReceivedQty.getText().toString()) ==
                                                                Integer.parseInt(lblassignedQty.getText().toString().split("[.]")[0])){
                                                            clearData1();
                                                        }else{
                                                            lblReceivedQty.setText("");
                                                            lblReceivedQty.clearFocus();
                                                            GetScanqtyvalidation("2");
                                                        }
                                                        break;
                                                }
                                            }
                                        });

                                    } else {
                                        common.showUserDefinedAlertType(outbountDTO1.getStatus(), getActivity(), getContext(), "Warning");
                                    }
                                } else {
                                    common.showUserDefinedAlertType("Error while getting data", getActivity(), getContext(), "Error");
                                }
                            }else {
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
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
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
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.menu_outbound_revert));
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}