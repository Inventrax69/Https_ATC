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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.inventrax.atc.common.Common;
import com.inventrax.atc.common.constants.EndpointConstants;
import com.inventrax.atc.common.constants.ErrorMessages;
import com.inventrax.atc.interfaces.ApiInterface;
import com.inventrax.atc.pojos.InboundDTO;
import com.inventrax.atc.pojos.OutbountDTO;
import com.inventrax.atc.pojos.ScanDTO;
import com.inventrax.atc.pojos.WMSCoreMessage;
import com.inventrax.atc.pojos.WMSExceptionMessage;
import com.inventrax.atc.searchableSpinner.SearchableSpinner;
import com.inventrax.atc.services.RetrofitBuilderHttpsEx;
import com.inventrax.atc.util.DialogUtils;
import com.inventrax.atc.util.ExceptionLoggerUtils;
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


public class VLPDPickingDetailsFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_VLPD_PICKING";
    private View rootView;
    ImageView ivScanLocation, ivScanPallet, ivScanPalletTo, ivScanRSN;
    Button btnPick, btn_Skip, btnOk, btnCloseSkip;
    TextView lblPickListNo;
    TextView lblSKuNo, lblLocationNo, lblMfgDate, lblExpDate, lblProjectRefNo, lblassignedQty,
            lblserialNo, lblBatchNo, lblMRP;
    CardView cvScanPallet, cvScanPalletTo, cvScanRSN, cvScanNewRSN, cvScanLocation;
    EditText lblReceivedQty;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    String userId = null, scanType = null;

    private Common common;
    private WMSCoreMessage core;
    boolean IsuserConfirmed = false;
    private String pickOBDno = "", pickobdId = "";
    private ScanValidator scanValidator;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    EditText etPallet, etPalletTo;
    boolean isValidLocation = false;
    boolean isPalletScanned = false;
    boolean isToPalletScanned = false;
    boolean pickValidateComplete = false;
    boolean isRSNScanned = false;
    String Lineno = "", assignedId = "", SLocId = "", locationId = "", outboundID = "",
            KitId = "", TransferRequestDetailsId = "", TransferRequestId = "", sLoc = "",
            MaterialMasterId = "", GoodsmomentDeatilsId = "", soDetailsId = "",accountId = "",
            cartonId = null, POSOHeaderId = "", location = "";
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    RelativeLayout rlPickList, rlSkip;

    String skipReason = "", pickedQty = "";
    int recQty, totalQty;
    SoundUtils soundUtils;
    SearchableSpinner spinnerSelectReason;


    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public void myScannedData(Context context, String barcode){
        try {
            ProcessScannedinfo(barcode.trim());
        }catch (Exception e){
            //  Toast.makeText(context, ""+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public VLPDPickingDetailsFragment() { }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_obd_picking, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    private void loadFormControls() {

        rlPickList = (RelativeLayout) rootView.findViewById(R.id.rlPickList);
        rlSkip = (RelativeLayout) rootView.findViewById(R.id.rlSkip);
        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanRSN = (CardView) rootView.findViewById(R.id.cvScanRSN);
        cvScanNewRSN = (CardView) rootView.findViewById(R.id.cvScanNewRSN);
        cvScanPalletTo = (CardView) rootView.findViewById(R.id.cvScanPalletTo);
        ivScanPalletTo = (ImageView) rootView.findViewById(R.id.ivScanPalletTo);
        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanRSN = (ImageView) rootView.findViewById(R.id.ivScanRSN);

        btnPick = (Button) rootView.findViewById(R.id.btnPick);
        btn_Skip = (Button) rootView.findViewById(R.id.btn_Skip);
        btnOk = (Button) rootView.findViewById(R.id.btnOk);
        btnCloseSkip = (Button) rootView.findViewById(R.id.btnCloseSkip);

        IsuserConfirmed = false;
        lblPickListNo = (TextView) rootView.findViewById(R.id.lblPickListNo);
        lblSKuNo = (TextView) rootView.findViewById(R.id.lblSKUSuggested);
        lblLocationNo = (TextView) rootView.findViewById(R.id.lblLocationSuggested);

        etPallet = (EditText) rootView.findViewById(R.id.etPallet);
        // etRSN = (EditText) rootView.findViewById(R.id.etRSN);
        etPalletTo = (EditText) rootView.findViewById(R.id.etPalletTo);
        //  etRSNnew = (EditText) rootView.findViewById(R.id.etRSNnew);
        lblReceivedQty = (EditText) rootView.findViewById(R.id.lblReceivedQty);
        lblMfgDate = (TextView) rootView.findViewById(R.id.lblMfgDate);
        lblExpDate = (TextView) rootView.findViewById(R.id.lblExpDate);
        lblProjectRefNo = (TextView) rootView.findViewById(R.id.lblProjectRefNo);
        lblserialNo = (TextView) rootView.findViewById(R.id.lblserialNo);
        lblBatchNo = (TextView) rootView.findViewById(R.id.lblBatchNo);
        lblassignedQty = (TextView) rootView.findViewById(R.id.lblRequiredQty);
        lblMRP = (TextView) rootView.findViewById(R.id.lblMRP);

        spinnerSelectReason = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectReason);
        spinnerSelectReason.setOnItemSelectedListener(this);

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
        btnPick.setOnClickListener(this);
        btn_Skip.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        btnCloseSkip.setOnClickListener(this);
        cvScanPallet.setOnClickListener(this);
        cvScanPalletTo.setOnClickListener(this);

        common = new Common();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();
        soundUtils = new SoundUtils();
        ProgressDialogUtils.closeProgressDialog();
        common.setIsPopupActive(false);

        pickOBDno = getArguments().getString("pickRefNo");
        pickobdId = getArguments().getString("pickobdId");
        lblPickListNo.setText(pickOBDno);

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

        if (scanType.equals("Auto")) {

            btnPick.setEnabled(false);
            btnPick.setTextColor(getResources().getColor(R.color.black));
            btnPick.setBackgroundResource(R.drawable.button_hide);
        } else {
            btnPick.setEnabled(false);
            btnPick.setTextColor(getResources().getColor(R.color.white));
            btnPick.setBackgroundResource(R.drawable.button_shape);
        }

        GetItemToPick();



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {


            case R.id.cvScanPallet:
                isPalletScanned=true;
                cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanPallet.setImageResource(R.drawable.check);
                break;

            case R.id.cvScanPalletTo:
                isToPalletScanned=true;
                cvScanPalletTo.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanPalletTo.setImageResource(R.drawable.check);
                break;

            case R.id.btnPick:
                if (!lblLocationNo.getText().toString().isEmpty()) {

                    if (!lblReceivedQty.getText().toString().isEmpty() && !lblReceivedQty.getText().toString().equals("0")) {

                        int reqQty = totalQty - recQty;
                        int qty = Integer.parseInt(lblReceivedQty.getText().toString().split("[.]")[0]);

                        if (reqQty < qty) {

                            common.showUserDefinedAlertType(errorMessages.EMC_0068, getActivity(), getContext(), "Error");

                        } else {
                            cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                            ivScanRSN.setImageResource(R.drawable.fullscreen_img);
                            UpsertPickItem();
                            return;
                        }

                    }

                }
                break;

            case R.id.btn_Skip:

                // To get skip reason list

                if (isValidLocation) {

                    if (lblSKuNo.getText().toString().isEmpty()) {

                        common.showUserDefinedAlertType(errorMessages.EMC_0070, getActivity(), getContext(), "Error");

                    } else {
                        SkipItem();
                    }
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Error");
                    return;
                }


                break;

            case R.id.btnOk:
                DialogUtils.showConfirmDialog(getActivity(), "Confirm", "Are you sure to skip this Location? ", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                common.setIsPopupActive(false);

                                if (!skipReason.equals("")) {

                                    // To skip the item and regenerating suggestions
                                    OBDSkipItem();
                                } else {
                                    common.showUserDefinedAlertType(errorMessages.EMC_0056, getActivity(), getContext(), "Error");
                                    return;
                                }

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:

                                common.setIsPopupActive(false);
                                break;
                        }

                    }
                });


                break;

            case R.id.btnCloseSkip:
                rlPickList.setVisibility(View.VISIBLE);
                rlSkip.setVisibility(View.GONE);
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
        etPallet.setText("");
        etPalletTo.setText("");


        lblassignedQty.setText("");
        lblBatchNo.setText("");
        lblReceivedQty.setText("");
        lblMfgDate.setText("");
        lblExpDate.setText("");
        lblProjectRefNo.setText("");
        lblserialNo.setText("");

        isToPalletScanned = false;
        isPalletScanned = false;
        isValidLocation = false;
        isRSNScanned = false;
        pickValidateComplete = false;


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

        if (common.isPopupActive() && rlPickList.getVisibility() != View.VISIBLE) {

        }
        else if (scannedData != null && !common.isPopupActive()) {

/*            if (scanValidator.isContainerScanned(scannedData)) {

                if (isValidLocation) {

                    if (!etPallet.getText().toString().isEmpty()) {

                        if (!isPalletScanned) {

                            if (scannedData.equals(etPallet.getText().toString())) {
                                isPalletScanned = true;
                                //ValidatePalletCode(etPallet.getText().toString());
                                cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanPallet.setImageResource(R.drawable.check);
                            } else {
                                common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Error");
                                return;
                            }

                        } else {
                            common.showUserDefinedAlertType(errorMessages.EMC_0034, getActivity(), getContext(), "Error");
                            return;

                        }
                    } else {

                        etPalletTo.setText(scannedData);
                        ValidatePalletCode(etPalletTo.getText().toString());
                    }
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Warning");

                }
            }
            else if (ScanValidator.isItemScanned(scannedData)) {

                *//* ----For RSN reference----
               0 Sku|1 Qty|2 husize|3 InBoundQty|4 BatchNo|5 RSN|6 MFGDate|7 EXpDate|8 ProjectRefNO|9 SerialNO|10 lneNi|11 Huno*//*

                //ToyCar||||||0|001
                if (isValidLocation) {
                    //validate Picking rsn
                    if (!lblSKuNo.getText().toString().isEmpty() && lblSKuNo.getText().toString().equalsIgnoreCase(scannedData.split("[|]")[0].toString())) {

                        if (lblBatchNo.getText().toString().equalsIgnoreCase(scannedData.split("[|]")[1]) &&
                                lblserialNo.getText().toString().equalsIgnoreCase(scannedData.split("[|]")[2]) &&
                                lblMfgDate.getText().toString().equalsIgnoreCase(scannedData.split("[|]")[3]) &&
                                lblExpDate.getText().toString().equalsIgnoreCase(scannedData.split("[|]")[4]) &&
                                lblProjectRefNo.getText().toString().equalsIgnoreCase(scannedData.split("[|]")[5])) {

                            cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                            ivScanRSN.setImageResource(R.drawable.fullscreen_img);

                            if (scanType.equalsIgnoreCase("Auto")) {
                                lblReceivedQty.setText("1");
                                UpsertPickItem();
                                return;
                            } else {
                                lblReceivedQty.setEnabled(true);
                                btnPick.setEnabled(true);

                                soundUtils.alertWarning(getActivity(), getContext());
                                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0073);
                            }
                        } else {
                            common.showUserDefinedAlertType(errorMessages.EMC_0053, getActivity(), getContext(), "Error");
                        }
                    } else {
                        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.red));
                        ivScanRSN.setImageResource(R.drawable.warning_img);
                        common.showUserDefinedAlertType(errorMessages.EMC_0029, getActivity(), getContext(), "Error");
                    }
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Warning");
                }
            }
            else if (scanValidator.isLocationScanned(scannedData)) {
                if (!lblLocationNo.getText().toString().isEmpty() && lblLocationNo.getText().toString().equalsIgnoreCase(scannedData)) {
                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanLocation.setImageResource(R.drawable.check);
                    isValidLocation = true;
                    location = scannedData;

                } else {
                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.red));
                    ivScanLocation.setImageResource(R.drawable.warning_img);
                    common.showUserDefinedAlertType(errorMessages.EMC_0033, getActivity(), getContext(), "Warning");
                }
            }*/


            if (!lblLocationNo.getText().toString().isEmpty()) {

                if(!isValidLocation){
                    if (!lblLocationNo.getText().toString().isEmpty() && lblLocationNo.getText().toString().equalsIgnoreCase(scannedData)) {
                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanLocation.setImageResource(R.drawable.check);
                        location = scannedData;
                        isValidLocation = true;
                    } else {
                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanLocation.setImageResource(R.drawable.warning_img);
                        common.showUserDefinedAlertType(errorMessages.EMC_0033, getActivity(), getContext(), "Warning");
                    }
                }else{
                    if(!isPalletScanned){
                        if (scannedData.equals(etPallet.getText().toString())) {
                            isPalletScanned = true;
                            //ValidatePalletCode(etPallet.getText().toString());
                            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanPallet.setImageResource(R.drawable.check);
                        } else {
                            common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Error");
                        }
                    }else{

                        if(!isToPalletScanned){
                            ValidatePallet(scannedData);
                        }else{
                            ValiDateMaterial(scannedData);
                        }
                    }
                }
            }
            else {
                common.showUserDefinedAlertType(errorMessages.EMC_0012, getActivity(), getContext(), "Error");
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
            scanDTO.setVlpdNumber(lblPickListNo.getText().toString());
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
                        Log.v("ABCDE",new Gson().toJson(core));

                        if ((core.getType().toString().equals("Exception"))) {
                            List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                            _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                            WMSExceptionMessage owmsExceptionMessage = null;
                            for (int i = 0; i < _lExceptions.size(); i++) {

                                owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                            }

                            cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                            ivScanRSN.setImageResource(R.drawable.fullscreen_img);
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

                                    if(scanDTO1.getSkuCode().equalsIgnoreCase(lblSKuNo.getText().toString().trim())){

                                        if((lblBatchNo.getText().toString().equalsIgnoreCase(scanDTO1.getBatch()) || scanDTO1.getBatch()==null
                                                || scanDTO1.getBatch().equalsIgnoreCase("") || scanDTO1.getBatch().isEmpty() )&&
                                                lblserialNo.getText().toString().equalsIgnoreCase(scanDTO1.getSerialNumber()) &&
                                                lblMfgDate.getText().toString().equalsIgnoreCase(scanDTO1.getMfgDate()) &&
                                                lblExpDate.getText().toString().equalsIgnoreCase(scanDTO1.getExpDate()) &&
                                                lblProjectRefNo.getText().toString().equalsIgnoreCase(scanDTO1.getPrjRef())) {


                                            cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                            ivScanRSN.setImageResource(R.drawable.fullscreen_img);


                                            if (scanType.equalsIgnoreCase("Auto")) {
                                                lblReceivedQty.setText("1");
                                                UpsertPickItem();

                                            } else {
                                                lblReceivedQty.setEnabled(true);
                                                btnPick.setEnabled(true);
                                                soundUtils.alertWarning(getActivity(), getContext());
                                                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0073);
                                            }
                                        }else{
                                            common.showUserDefinedAlertType(errorMessages.EMC_0079,getActivity(),getContext(),"Error");
                                        }


                                    }else {
                                        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanRSN.setImageResource(R.drawable.warning_img);
                                        common.showUserDefinedAlertType(errorMessages.EMC_0029, getActivity(), getContext(), "Error");
                                    }



                                } else{
                                    // lblScannedSku.setText("");
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

    public void ValidatePallet(final String scannedData) {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.ScanDTO, getContext());
            ScanDTO scanDTO = new ScanDTO();
            scanDTO.setUserID(userId);
            scanDTO.setAccountID(accountId);
            // scanDTO.setTenantID(String.valueOf(tenantID));
            //scanDTO.setWarehouseID(String.valueOf(warehouseID));
            scanDTO.setScanInput(scannedData);
            scanDTO.setVlpdNumber(lblPickListNo.getText().toString());
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

                            etPalletTo.setText("");
                            cvScanPalletTo.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanPalletTo.setImageResource(R.drawable.invalid_cross);
                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                        } else {
                            LinkedTreeMap<?, ?>_lResult = new LinkedTreeMap<>();
                            _lResult = (LinkedTreeMap<?, ?>) core.getEntityObject();

                            ScanDTO scanDTO1=new ScanDTO(_lResult.entrySet());
                            ProgressDialogUtils.closeProgressDialog();
                            if(scanDTO1!=null){
                                if(scanDTO1.getScanResult()){
                                    etPalletTo.setText(scannedData);
                                    //ValidatePalletCode();
                                    isToPalletScanned = true;
                                    cvScanPalletTo.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPalletTo.setImageResource(R.drawable.check);
                                } else{
                                    isToPalletScanned=false;
                                    etPalletTo.setText("");
                                    cvScanPalletTo.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPalletTo.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Warning");
                                }
                            }else{
                                isToPalletScanned=false;
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

    //To get Picking Suggestions
    public void GetItemToPick() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            final OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            outbountDTO.setvLPDId(pickobdId);
            outbountDTO.setAccountID(accountId);
            message.setEntityObject(outbountDTO);
            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method2
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetItemToPick(message);
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_01", getActivity());
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
                            if (response.body() != null) {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                                Log.v("ANIL2",new Gson().toJson(core));
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {
                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    }
                                    ClearFields();
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02")
                                            || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03")
                                            || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01")
                                            || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04")) {
                                        ProgressDialogUtils.closeProgressDialog();
                                        // Clearfields();
                                    }
                                    ProgressDialogUtils.closeProgressDialog();
                                } else {
                                    //Response object Success
                                    List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                    OutbountDTO oOutboundDTO = null;



                                    for (int i = 0; i < _lstPickitem.size(); i++) {
                                        oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                    }
                                    ProgressDialogUtils.closeProgressDialog();

                                    // Picking suggestions

                                    sLoc = "" + oOutboundDTO.getsLoc();
                                    MaterialMasterId = "" + oOutboundDTO.getMaterialMasterId();

                                    assignedId = "" + oOutboundDTO.getAssignedID();
                                    SLocId = "" + oOutboundDTO.getsLocId();
                                    soDetailsId = "" + oOutboundDTO.getSODetailsID();
                                    GoodsmomentDeatilsId = "" + oOutboundDTO.getSODetailsID();

                                    TransferRequestDetailsId = "" + oOutboundDTO.getTransferRequestDetailsId().split("[.]")[0];
                                    TransferRequestId = "" + oOutboundDTO.getTransferRequestId().split("[.]")[0];

                                    locationId = "" + oOutboundDTO.getLocationId();
                                    outboundID = "" + oOutboundDTO.getOutboundID();
                                    etPallet.setText(oOutboundDTO.getPalletNo());

                                    lblSKuNo.setText(oOutboundDTO.getSKU());
                                    cartonId = oOutboundDTO.getCartonID();

                                    lblBatchNo.setText(oOutboundDTO.getBatchNo());
                                    lblLocationNo.setText(oOutboundDTO.getLocation());

                                    pickedQty = oOutboundDTO.getPickedQty();

                                    if(lblLocationNo.getText().toString().equals("")){
                                        common.showUserDefinedAlertType(errorMessages.EMC_0063 + lblPickListNo.getText().toString(), getActivity(), getContext(), "Success");
                                        return;
                                    }

                                    lblassignedQty.setText(oOutboundDTO.getPickedQty().split("[.]")[0] + "/" + oOutboundDTO.getAssignedQuantity().split("[.]")[0]);

                                    recQty = Integer.parseInt(oOutboundDTO.getPickedQty().split("[.]")[0]);
                                    totalQty = Integer.parseInt(oOutboundDTO.getAssignedQuantity().split("[.]")[0]);

                                    lblMfgDate.setText(oOutboundDTO.getMfgDate());
                                    lblExpDate.setText(oOutboundDTO.getExpDate());
                                    lblProjectRefNo.setText(oOutboundDTO.getProjectNo());
                                    lblserialNo.setText(oOutboundDTO.getSerialNo());
                                    lblMRP.setText(oOutboundDTO.getMRP());


                                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                    ivScanRSN.setImageResource(R.drawable.fullscreen_img);



                                    if (!lblLocationNo.getText().toString().equals(location)) {           // Suggested location and scanned location is equal

                                        isValidLocation = false;
                                        isPalletScanned = false;
                                        location = "";

                                        btnPick.setEnabled(false);

                                        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                        ivScanRSN.setImageResource(R.drawable.fullscreen_img);

                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

                                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

                                        cvScanPalletTo.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                        ivScanPalletTo.setImageResource(R.drawable.fullscreen_img);

                                    } else if (lblLocationNo.getText().toString().equals(location)) {            // Suggested location and scanned location is equal

                                        isPalletScanned = false;

                                        btnPick.setEnabled(false);

                                        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                        ivScanRSN.setImageResource(R.drawable.fullscreen_img);

                                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

                                        cvScanPalletTo.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                        ivScanPalletTo.setImageResource(R.drawable.fullscreen_img);
                                    }


                                    if (oOutboundDTO.getPickedQty().equals(oOutboundDTO.getAssignedQuantity())) {   // Pending and Assigned qty is equal then it means outbound is completed

                                        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                        ivScanRSN.setImageResource(R.drawable.fullscreen_img);

                                        lblassignedQty.setText(oOutboundDTO.getPickedQty().split("[.]")[0] + "/" + oOutboundDTO.getAssignedQuantity().split("[.]")[0]);

                                        ClearFields();
                                        clearData();

                                        common.showUserDefinedAlertType(errorMessages.EMC_0071, getActivity(), getContext(), "Success");

                                        ProgressDialogUtils.closeProgressDialog();
                                        return;

                                    }


                                }
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    public void ValidatePalletCode(String pallet) {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setPalletNo(pallet);
            outbountDTO.setOutboundID(outboundID);
            outbountDTO.setAccountID(accountId);
            message.setEntityObject(outbountDTO);
            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.CheckContainerOBD(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_01", getActivity());
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
                        if (response.body() != null) {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                    if (isPalletScanned) {
                                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPallet.setImageResource(R.drawable.invalid_cross);
                                        etPallet.setText("");
                                        isPalletScanned = false;

                                    } else {
                                        cvScanPalletTo.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPalletTo.setImageResource(R.drawable.invalid_cross);
                                        etPalletTo.setText("");
                                    }
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                }

                            } else {
                                List<LinkedTreeMap<?, ?>> _lPalletInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lPalletInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                if (_lPalletInventory != null) {
                                    if (_lPalletInventory.size() > 0) {


                                        if (isPalletScanned && etPalletTo.getText().toString().isEmpty()) {
                                            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanPallet.setImageResource(R.drawable.check);
                                        } else {

                                            cvScanPalletTo.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanPalletTo.setImageResource(R.drawable.check);
                                        }
                                        ProgressDialogUtils.closeProgressDialog();


                                    } else {
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showUserDefinedAlertType(errorMessages.EMC_0028, getActivity(), getContext(), "Warning");
                                        return;
                                    }
                                }
                            }
                        } else {
                            ProgressDialogUtils.closeProgressDialog();
                            common.showUserDefinedAlertType(errorMessages.EMC_0021, getActivity(), getContext(), "Error");
                            return;
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                        return;
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
            return;
        }
    }

    public void UpsertPickItem() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO oOutboundDTO = new OutbountDTO();
            oOutboundDTO.setUserId(userId);
            oOutboundDTO.setAccountID(accountId);
            oOutboundDTO.setOutboundID(outboundID);
            oOutboundDTO.setSerialNo(lblserialNo.getText().toString());
            oOutboundDTO.setMfgDate(lblMfgDate.getText().toString());
            oOutboundDTO.setExpDate(lblExpDate.getText().toString());
            oOutboundDTO.setBatchNo(lblBatchNo.getText().toString());
            oOutboundDTO.setProjectNo(lblProjectRefNo.getText().toString());
            oOutboundDTO.setvLPDId(pickobdId);
            oOutboundDTO.setSKU(lblSKuNo.getText().toString());
            oOutboundDTO.setLocation(lblLocationNo.getText().toString());
            oOutboundDTO.setAssignedID(assignedId);
            oOutboundDTO.setsLocId(SLocId);
            oOutboundDTO.setToCartonNo(etPalletTo.getText().toString());
            oOutboundDTO.setSODetailsID(soDetailsId);
            oOutboundDTO.setPickedQty(lblReceivedQty.getText().toString());
            oOutboundDTO.setMaterialMasterId(MaterialMasterId);
            oOutboundDTO.setLocationId(locationId);
            oOutboundDTO.setCartonID(cartonId);
            oOutboundDTO.setTransferRequestId(TransferRequestId);
            oOutboundDTO.setTransferRequestDetailsId(TransferRequestDetailsId);
            oOutboundDTO.setPalletNo(etPallet.getText().toString());
            oOutboundDTO.setMRP(lblMRP.getText().toString());
            message.setEntityObject(oOutboundDTO);
            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.UpsertPickItem(message);

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }

            try {
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            Log.v("ANIL1",new Gson().toJson(core));
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    ProgressDialogUtils.closeProgressDialog();
                                }


                                //addded on 19-12-2018  19:07   need to unit test because of no handheld
                                lblReceivedQty.setText("");
                                lblReceivedQty.setEnabled(false);
                                btnPick.setEnabled(false);

                                common.setIsPopupActive(true);
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_IN_DAL_001")) {
                                    //Clearfields();
                                    // GetPickItem();
                                }
                                if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_010")){

                                    cvScanPalletTo.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                    ivScanPalletTo.setImageResource(R.drawable.fullscreen_img);

                                    etPalletTo.setText("");

                                }

                            } else {

                                List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                OutbountDTO oOutboundDTO = null;
                                for (int i = 0; i < _lstPickitem.size(); i++) {
                                    oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());

                                }

                                ProgressDialogUtils.closeProgressDialog();

                                if (oOutboundDTO.getPendingQty().equals("0")) { // If Pending Qty. is 0 then outbound is completed
                                                                                // else again suggests next picking suggestions
                                    lblassignedQty.setText(oOutboundDTO.getPendingQty());
                                    lblReceivedQty.setText("");
                                    lblSKuNo.setText("");

                                    // Added to clear data after completion of the outbound
                                    ClearFields();
                                    clearData();

                                    common.showUserDefinedAlertType(errorMessages.EMC_0071, getActivity(), getContext(), "Success");


                                } else {

                                    lblReceivedQty.setText("");

                                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanRSN.setImageResource(R.drawable.check);

                                    lblassignedQty.setText(oOutboundDTO.getPickedQty().split("[.]")[0] + "/" + oOutboundDTO.getAssignedQuantity().split("[.]")[0]);

                                    soundUtils.alertSuccess(getActivity(), getContext());

                                    GetItemToPick();

                                }

                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    public void clearData() {

        etPalletTo.setText("");
        etPallet.setText("");
        lblLocationNo.setText("");

        isValidLocation = false;
        isPalletScanned = false;
        isToPalletScanned =false;

        btnPick.setEnabled(false);
        lblassignedQty.setText("");

        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanRSN.setImageResource(R.drawable.fullscreen_img);

        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

        cvScanPalletTo.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPalletTo.setImageResource(R.drawable.fullscreen_img);
    }

    public void SkipItem() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO outbountDTO = new InboundDTO();
            outbountDTO.setSkipType("2");
            outbountDTO.setAccountID(accountId);
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetSkipReasonList(message);

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_01", getActivity());
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
                                    ProgressDialogUtils.closeProgressDialog();

                                }
                                common.setIsPopupActive(true);
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                                if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02") ||
                                        owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03") ||
                                        owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01") ||
                                        owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04") ||
                                        owmsExceptionMessage.getWMSExceptionCode().equals("EMC_IN_DAL_001")) {
                                    //Clearfields();
                                    GetItemToPick();
                                }
                                btnPick.setEnabled(true);
                            } else {
                                List<LinkedTreeMap<?, ?>> _lPickRefNo = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lPickRefNo = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<String> lstPickRefNo = new ArrayList<>();

                                List<OutbountDTO> lstDto = new ArrayList<OutbountDTO>();
                                for (int i = 0; i < _lPickRefNo.size(); i++) {
                                    OutbountDTO dto = new OutbountDTO(_lPickRefNo.get(i).entrySet());
                                    lstDto.add(dto);
                                }
                                for (int i = 0; i < lstDto.size(); i++) {
                                    lstPickRefNo.add(String.valueOf(lstDto.get(i).getSkipReason()));
                                }

                                if (lstPickRefNo == null) {
                                    ProgressDialogUtils.closeProgressDialog();
                                    DialogUtils.showAlertDialog(getActivity(), "Picklist is null");
                                } else {
                                    ProgressDialogUtils.closeProgressDialog();
                                    ArrayAdapter arrayAdapterPickList = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstPickRefNo);
                                    spinnerSelectReason.setAdapter(arrayAdapterPickList);
                                }

                                rlSkip.setVisibility(View.VISIBLE);
                                rlPickList.setVisibility(View.GONE);
                            }
                            btnPick.setEnabled(true);
                        } catch (Exception ex) {

                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    public void OBDSkipItem() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            final OutbountDTO oOutboundDTO = new OutbountDTO();

            int reqQty = totalQty - recQty;

            oOutboundDTO.setUserId(userId);
            oOutboundDTO.setAccountID(accountId);
            oOutboundDTO.setSkipReason(skipReason);
            oOutboundDTO.setSKU(lblSKuNo.getText().toString());
            oOutboundDTO.setSerialNo(lblserialNo.getText().toString());
            oOutboundDTO.setMfgDate(lblMfgDate.getText().toString());
            oOutboundDTO.setExpDate(lblExpDate.getText().toString());
            oOutboundDTO.setBatchNo(lblBatchNo.getText().toString());
            oOutboundDTO.setProjectNo(lblProjectRefNo.getText().toString());
            oOutboundDTO.setvLPDId(pickobdId);
            oOutboundDTO.setLocation(lblLocationNo.getText().toString());
            oOutboundDTO.setPalletNo(etPallet.getText().toString());
            oOutboundDTO.setSkipQty(String.valueOf(reqQty));
            oOutboundDTO.setPickedQty(lblReceivedQty.getText().toString());
            oOutboundDTO.setAssignedID(assignedId);
            oOutboundDTO.setsLoc(sLoc);
            oOutboundDTO.setMRP(lblMRP.getText().toString());
            message.setEntityObject(oOutboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method2
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.VLPDSkipItem(message);
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_01", getActivity());
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
                            if (response.body() != null) {
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
                                    if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02")
                                            || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03")
                                            || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01")
                                            || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04")) {

                                        ProgressDialogUtils.closeProgressDialog();

                                    }
                                    rlPickList.setVisibility(View.VISIBLE);
                                    rlSkip.setVisibility(View.GONE);

                                    GetItemToPick();
                                } else {

                                    //Response object Success
                                    List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                    OutbountDTO oOutboundDTO = null;
                                    for (int i = 0; i < _lstPickitem.size(); i++) {
                                        oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                    }

                                    location = "";
                                    isValidLocation = false;
                                    isPalletScanned = false;
                                    etPalletTo.setText("");

                                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                    ivScanRSN.setImageResource(R.drawable.fullscreen_img);

                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                    ivScanLocation.setImageResource(R.drawable.fullscreen_img);

                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                    ivScanPallet.setImageResource(R.drawable.fullscreen_img);

                                    cvScanPalletTo.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                    ivScanPalletTo.setImageResource(R.drawable.fullscreen_img);

                                    //Next suggestion after skipping that location
                                    sLoc = "" + oOutboundDTO.getsLoc();
                                    MaterialMasterId = "" + oOutboundDTO.getMaterialMasterId();
                                    POSOHeaderId = "" + oOutboundDTO.getpOSOHeaderId();
                                    Lineno = "" + oOutboundDTO.getLineno();
                                    lblSKuNo.setText(oOutboundDTO.getSKU());
                                    assignedId = "" + oOutboundDTO.getAssignedID();
                                    soDetailsId = "" + oOutboundDTO.getSODetailsID();
                                    GoodsmomentDeatilsId = "" + oOutboundDTO.getSODetailsID();
                                    KitId = "" + oOutboundDTO.getAssignedID();
                                    etPallet.setText(oOutboundDTO.getPalletNo());
                                    lblBatchNo.setText(oOutboundDTO.getBatchNo());
                                    lblLocationNo.setText(oOutboundDTO.getLocation());
                                    pickedQty = oOutboundDTO.getPickedQty();
                                    lblassignedQty.setText(oOutboundDTO.getPickedQty() + "/" + oOutboundDTO.getAssignedQuantity());
                                    lblMfgDate.setText(oOutboundDTO.getMfgDate());
                                    lblExpDate.setText(oOutboundDTO.getExpDate());
                                    lblProjectRefNo.setText(oOutboundDTO.getProjectNo());
                                    lblserialNo.setText(oOutboundDTO.getSerialNo());
                                    rlPickList.setVisibility(View.VISIBLE);
                                    rlSkip.setVisibility(View.GONE);
                                    lblMRP.setText(oOutboundDTO.getMRP());

                                    common.showUserDefinedAlertType(errorMessages.EMC_0077, getActivity(), getContext(), "Success");

                                    ProgressDialogUtils.closeProgressDialog();
                                }
                            }

                            ProgressDialogUtils.closeProgressDialog();
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.menu_vlpdPick));
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
        skipReason = spinnerSelectReason.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}