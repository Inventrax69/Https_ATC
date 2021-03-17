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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.inventrax.atc.pojos.ScanDTO;
import com.inventrax.atc.pojos.WMSCoreMessage;
import com.inventrax.atc.pojos.WMSExceptionMessage;
import com.inventrax.atc.searchableSpinner.SearchableSpinner;
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
 * Created by Padmaja Rani.B on 19/12/2018
 */

public class GoodsInFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener, View.OnLongClickListener {

    private static final String classCode = "API_FRAG_GOODSIN";
    private View rootView;
    private TextView lblStoreRefNo, lblInboundQty, lblScannedSku, lblDock, lblHu;
    private CardView cvScanPallet, cvScanSku, cvScanDock;
    private ImageView ivScanPallet, ivScanSku, ivScanDock;
    private TextInputLayout txtInputLayoutPallet, txtInputLayoutSerial, txtInputLayoutMfgDate, txtInputLayoutExpDate,
            txtInputLayoutBatch, txtInputLayoutPrjRef, txtInputLayoutQty, txtInputLayoutKitID, txtInputLayoutMRP, txtInputLayoutDock;
    private EditText etPallet, etSerial, etMfgDate, etExpDate, etBatch, etPrjRef, etQty, etKidID, etMRP, etDock;
    private CheckBox cbDescripency;
    private SearchableSpinner spinnerSelectSloc;
    private Button btnClear, btnReceive;
    DialogUtils dialogUtils;
    FragmentUtils fragmentUtils;
    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    private String Materialcode = null, huNo = "", huSize = "";
    private String userId = null, scanType = null, accountId = null, lineNo = null,
            receivedQty = null, pendingQty = null, dock = "", vehicleNo = "";
    String storageLoc = "", inboundId = null, invoiceQty = null, recQty = "";
    int warehouseID = 0, tenantID = 0;
    ArrayList<String> sloc;
    SoundUtils sound = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private boolean isInboundCompleted = false, isDockScanned = false, isContanierScanned = false, isRsnScanned = false;
    SoundUtils soundUtils;
    String supplierInvoiceDetailsId = "";
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;

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

    public GoodsInFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_goodsin, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;

    }

    // Form controls
    private void loadFormControls() {

        lblStoreRefNo = (TextView) rootView.findViewById(R.id.lblStoreRefNo);
        lblInboundQty = (TextView) rootView.findViewById(R.id.lblInboundQty);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);
        lblDock = (TextView) rootView.findViewById(R.id.lblDock);
        lblHu = (TextView) rootView.findViewById(R.id.lblHu);

        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanSku = (CardView) rootView.findViewById(R.id.cvScanSku);
        cvScanDock = (CardView) rootView.findViewById(R.id.cvScanDock);

        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanSku = (ImageView) rootView.findViewById(R.id.ivScanSku);
        ivScanDock = (ImageView) rootView.findViewById(R.id.ivScanDock);

        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);
        txtInputLayoutSerial = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutSerial);
        txtInputLayoutMfgDate = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMfgDate);
        txtInputLayoutBatch = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutBatch);
        txtInputLayoutPrjRef = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutProjectRef);
        txtInputLayoutExpDate = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutExpDate);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);
        txtInputLayoutKitID = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutKitID);
        txtInputLayoutMRP = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMRP);
        txtInputLayoutDock = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutDock);

        etPallet = (EditText) rootView.findViewById(R.id.etPallet);
        etSerial = (EditText) rootView.findViewById(R.id.etSerial);
        etMfgDate = (EditText) rootView.findViewById(R.id.etMfgDate);
        etBatch = (EditText) rootView.findViewById(R.id.etBatch);
        etPrjRef = (EditText) rootView.findViewById(R.id.etProjectRef);
        etExpDate = (EditText) rootView.findViewById(R.id.etExpDate);
        etQty = (EditText) rootView.findViewById(R.id.etQty);
        etKidID = (EditText) rootView.findViewById(R.id.etKidID);
        etMRP = (EditText) rootView.findViewById(R.id.etMRP);
        etDock = (EditText) rootView.findViewById(R.id.etDock);

        spinnerSelectSloc = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectSloc);
        spinnerSelectSloc.setOnItemSelectedListener(this);

        cbDescripency = (CheckBox) rootView.findViewById(R.id.cbDescripency);

        btnClear = (Button) rootView.findViewById(R.id.btnClear);
        btnReceive = (Button) rootView.findViewById(R.id.btnReceive);

        etQty.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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


        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        scanType = sp.getString("scanType", "");
        accountId = sp.getString("AccountId", "");
        warehouseID = sp.getInt("WarehouseID", 0);
        tenantID = sp.getInt("TenantID", 0);

        btnClear.setOnClickListener(this);
        btnReceive.setOnClickListener(this);
        cvScanPallet.setOnClickListener(this);

        if (scanType.equals("Auto")) {
            btnReceive.setEnabled(false);
            btnReceive.setTextColor(getResources().getColor(R.color.black));
            btnReceive.setBackgroundResource(R.drawable.button_hide);
        } else {
            btnReceive.setEnabled(true);
            btnReceive.setTextColor(getResources().getColor(R.color.white));
            btnReceive.setBackgroundResource(R.drawable.button_shape);
        }

        exceptionLoggerUtils = new ExceptionLoggerUtils();
        sound = new SoundUtils();
        sloc = new ArrayList<>();
        common = new Common();
        errorMessages = new ErrorMessages();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();
        soundUtils = new SoundUtils();
        ProgressDialogUtils.closeProgressDialog();
        common.setIsPopupActive(false);

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        //For Honey well
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

        lblStoreRefNo.setText(getArguments().getString("Storefno"));
        inboundId = getArguments().getString("inboundId");
        invoiceQty = getArguments().getString("invoiceQty");
        recQty = getArguments().getString("receivedQty");
        dock = getArguments().getString("dock");
        vehicleNo = getArguments().getString("vehilceNo");

        lblDock.setText(dock);

        lblInboundQty.setText(recQty + "/" + invoiceQty);

        if (recQty.equals(invoiceQty)) {
            common.showUserDefinedAlertType(errorMessages.EMC_0069 + "" + lblStoreRefNo.getText().toString(), getActivity(), getContext(), "Success");
            isInboundCompleted = true;
            btnReceive.setEnabled(false);
            return;
        }

        lblHu.setText("");

        // To get Storage Locations
        getSLocs();


    }

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnClear:
                clearFields();
                break;

            case R.id.cvScanPallet:
                if (isContanierScanned) {
                    etPallet.setText("");
                    //ValidatePalletCode();
                    isContanierScanned = false;
                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                    ivScanPallet.setImageResource(R.drawable.fullscreen_img);
                    etExpDate.setText("");
                    etMfgDate.setText("");
                    etBatch.setText("");
                    etQty.setText("");
                    etPrjRef.setText("");
                    etSerial.setText("");
                    etKidID.setText("");
                    etMRP.setText("");
                    huNo = "";
                    huSize = "";
                    lblHu.setText("");
                    Materialcode = "";
                    lblScannedSku.setText("");
                    isRsnScanned = false;
                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                    ivScanSku.setImageResource(R.drawable.fullscreen_img);
                }
                break;

            case R.id.btnReceive:

                if (!lblScannedSku.getText().toString().isEmpty() && !Materialcode.equals("")) {

                    if (Integer.parseInt(receivedQty.split("[.]")[0]) < Integer.parseInt(pendingQty.split("[.]")[0])) {

                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                        ivScanSku.setImageResource(R.drawable.fullscreen_img);
                        if(!storageLoc.isEmpty()){
                            ValidateRSNAndReceive();
                        }else {
                            common.showUserDefinedAlertType(errorMessages.EMC_0052, getActivity(), getContext(), "Warning");
                        }



                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0075, getActivity(), getContext(), "Warning");
                        return;
                    }
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0028, getActivity(), getContext(), "Warning");
                    return;
                }

                break;

            default:
                break;
        }
    }

    public void clearFields() {

        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSku.setImageResource(R.drawable.fullscreen_img);

        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

        cvScanDock.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanDock.setImageResource(R.drawable.fullscreen_img);

        etPallet.setText("");
        etExpDate.setText("");
        etMfgDate.setText("");
        etBatch.setText("");
        etQty.setText("");
        etPrjRef.setText("");
        etSerial.setText("");
        lblScannedSku.setText("");
        etKidID.setText("");
        etMRP.setText("");
        lblHu.setText("");
        cbDescripency.setChecked(false);

        etQty.setEnabled(false);
        isDockScanned = false;

        etDock.setText("");

        isDockScanned = false;
        isContanierScanned = false;

        /*
        btnReceive.setEnabled(false);
        btnReceive.setTextColor(getResources().getColor(R.color.black));
        btnReceive.setBackgroundResource(R.drawable.button_hide);
        */


    }


    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //update UI to reflect the data
                //List<String> list = new ArrayList<String>();
                //list.add("Barcode data: " + barcodeReadEvent.getBarcodeData());

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
            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, true);
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

        if (((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).isDrawerOpen(GravityCompat.START)) {
            return;
        }

        if (ProgressDialogUtils.isProgressActive() || Common.isPopupActive()) {
            common.showUserDefinedAlertType(errorMessages.EMC_082, getActivity(), getContext(), "Warning");
            return;
        }

        if (scannedData != null && !Common.isPopupActive() && !isInboundCompleted) {

            if (!ProgressDialogUtils.isProgressActive()) {

                if (!isDockScanned) {
                    ValidateLocation(scannedData);
                } else {
                    if (!isContanierScanned) {
                        ValidatePallet(scannedData);
                    } else {
                        ValiDateMaterial(scannedData);
                    }
                }

            } else {
                if (!Common.isPopupActive()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_080, getActivity(), getContext(), "Error");
                }
                sound.alertWarning(getActivity(), getContext());
            }


/*            if (ScanValidator.isContainerScanned(scannedData)) {
                if (isDockScanned) {

                    etPallet.setText(scannedData);
                    // Validating pallet whether the pallet belongs to same warehouse or not
                    ValidatePalletCode();
                    return;
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0018, getActivity(), getContext(), "Error");
                    return;
                }


            } else if (ScanValidator.isItemScanned(scannedData)) {

                if (isDockScanned) {

                *//* ----For RSN reference----
               0 Sku|1 BatchNo|2 SerialNO|3 MFGDate|4 EXpDate|5 ProjectRefNO|6 Kit Id|7 line No|8 MRP ---- For SKU with 9 MSP's

               0 Sku|1 BatchNo|2 SerialNO|3 KitId|4 lineNo  ---- For SKU with 5 MSP's   *//*
                    // Eg. : ToyCar|1|bat1|ser123|12/2/2018|12/2/2019|0|001


                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                    ivScanSku.setImageResource(R.drawable.fullscreen_img);

                    if (scannedData.split("[|]").length != 5) {

                        Materialcode = scannedData.split("[|]")[0];
                        etBatch.setText(scannedData.split("[|]")[1]);
                        etSerial.setText(scannedData.split("[|]")[2]);
                        etMfgDate.setText(scannedData.split("[|]")[3]);
                        etExpDate.setText(scannedData.split("[|]")[4]);
                        etPrjRef.setText(scannedData.split("[|]")[5]);
                        etKidID.setText(scannedData.split("[|]")[6]);

                        if (etKidID.getText().toString().equals("0")) {
                            etKidID.setText("");
                        }

                        etMRP.setText(scannedData.split("[|]")[7]);
                        lineNo = scannedData.split("[|]")[8];

                    } else {
                        Materialcode = scannedData.split("[|]")[0];
                        etBatch.setText(scannedData.split("[|]")[1]);
                        etSerial.setText(scannedData.split("[|]")[2]);
                        etKidID.setText(scannedData.split("[|]")[3]);
                        lineNo = scannedData.split("[|]")[4];
                    }

                    lblScannedSku.setText(Materialcode);


                    if (scanType.equals("Auto")) {
                        etQty.setText("1");

                        getReceivedQty();          // To get the pending and received quantities
                        return;
                    } else {

                        // for Manual mode
                        etQty.setEnabled(true);
                        btnReceive.setEnabled(true);
                        btnReceive.setTextColor(getResources().getColor(R.color.white));
                        btnReceive.setBackgroundResource(R.drawable.button_shape);

                        lblInboundQty.setText("");

                        getreceivedQty();           // To get the pending and received quantities

                    }
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0018, getActivity(), getContext(), "Error");
                    return;
                }

            } else if (ScanValidator.isDockLocationScanned(scannedData)) {

                if (scannedData.equals(lblDock.getText().toString())) {  //if suggested dock and scanned dock is same

                    if (!isDockScanned) {   // once dock scanned user does not have any chance to scan dock again
                        etDock.setText(scannedData);

                        isDockScanned = true;

                        cvScanDock.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanDock.setImageResource(R.drawable.check);
                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0010, getActivity(), getContext(), "Warning");
                    }
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Warning");
                }

            } else {

                common.showUserDefinedAlertType(errorMessages.EMC_0030, getActivity(), getContext(), "Error");
                return;
            }*/


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
            scanDTO.setInboundID(inboundId);
            //inboundDTO.setIsOutbound("0");
            message.setEntityObject(scanDTO);

            Log.v("ABCDE", new Gson().toJson(message));

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

                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                            ivScanSku.setImageResource(R.drawable.fullscreen_img);
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


                                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                    ivScanSku.setImageResource(R.drawable.fullscreen_img);

                                    /*    if (scannedData.split("[|]").length != 5) {*/

                                    Materialcode = scanDTO1.getSkuCode();
                                    etBatch.setText(scanDTO1.getBatch());
                                    etSerial.setText(scanDTO1.getSerialNumber());
                                    etMfgDate.setText(scanDTO1.getMfgDate());
                                    etExpDate.setText(scanDTO1.getExpDate());
                                    etPrjRef.setText(scanDTO1.getPrjRef());
                                    etKidID.setText(scanDTO1.getKitID());
                                    etMRP.setText(scanDTO1.getMrp());
                                    lineNo = scanDTO1.getLineNumber();
                                    supplierInvoiceDetailsId = scanDTO1.getSupplierInvoiceDetailsID();
                                    huNo = scanDTO1.getHUNo();
                                    huSize = scanDTO1.getHUSize();
                                    if (!huSize.equals("1")) {
                                        lblHu.setText("Hu: " + "" + huNo + "/" + huSize);
                                    }else {
                                        lblHu.setText("");
                                    }

                                    lblScannedSku.setText(Materialcode);

                                    if (etKidID.getText().toString().equals("0")) {
                                        etKidID.setText("");
                                    }

                                    //   etMRP.setText(scannedData.split("[|]")[7]);


/*                                    } else {
                                        Materialcode = scannedData.split("[|]")[0];
                                        etBatch.setText(scannedData.split("[|]")[1]);
                                        etSerial.setText(scannedData.split("[|]")[2]);
                                        etKidID.setText(scannedData.split("[|]")[3]);
                                        lineNo = scannedData.split("[|]")[4];
                                    }*/


                                    if (scanType.equals("Auto")) {
                                        etQty.setText("1");
                                        getReceivedQty();          // To get the pending and received quantities
                                        return;
                                    } else {
                                        // for Manual mode
                                        etQty.setEnabled(true);
                                        btnReceive.setEnabled(true);
                                        btnReceive.setTextColor(getResources().getColor(R.color.white));
                                        btnReceive.setBackgroundResource(R.drawable.button_shape);
                                        lblInboundQty.setText("");
                                        getreceivedQty();           // To get the pending and received quantities
                                    }
                                } else {
                                    isRsnScanned = true;
                                    lblScannedSku.setText("");
                                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanSku.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Warning");
                                }
                            } else {
                                isRsnScanned = true;
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
            // scanDTO.setTenantID(String.valueOf(tenantID));
            // scanDTO.setWarehouseID(String.valueOf(warehouseID));
            scanDTO.setScanInput(scannedData);
            scanDTO.setInboundID(inboundId);
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

                            isDockScanned = false;
                            etDock.setText("");
                            cvScanDock.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanDock.setImageResource(R.drawable.invalid_cross);
                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                        } else {
                            LinkedTreeMap<?, ?> _lResult = new LinkedTreeMap<>();
                            _lResult = (LinkedTreeMap<?, ?>) core.getEntityObject();

                            ScanDTO scanDTO1 = new ScanDTO(_lResult.entrySet());

                            if (scanDTO1 != null) {
                                if (scanDTO1.getScanResult()) {
                                    etDock.setText(scannedData);
                                    isDockScanned = true;
                                    cvScanDock.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanDock.setImageResource(R.drawable.check);
                                } else {
                                    isDockScanned = false;
                                    etDock.setText("");
                                    cvScanDock.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanDock.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0010, getActivity(), getContext(), "Warning");
                                }
                            } else {
                                isDockScanned = false;
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
            // scanDTO.setTenantID(String.valueOf(tenantID));
            //scanDTO.setWarehouseID(String.valueOf(warehouseID));
            scanDTO.setScanInput(scannedData);
            scanDTO.setInboundID(inboundId);
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

                            etPallet.setText("");
                            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanPallet.setImageResource(R.drawable.invalid_cross);
                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                        } else {
                            LinkedTreeMap<?, ?> _lResult = new LinkedTreeMap<>();
                            _lResult = (LinkedTreeMap<?, ?>) core.getEntityObject();

                            ScanDTO scanDTO1 = new ScanDTO(_lResult.entrySet());
                            ProgressDialogUtils.closeProgressDialog();
                            if (scanDTO1 != null) {
                                if (scanDTO1.getScanResult()) {
                                    etPallet.setText(scannedData);
                                    //ValidatePalletCode();
                                    isContanierScanned = true;
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.check);
                                } else {
                                    isContanierScanned = false;
                                    etPallet.setText("");
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Warning");
                                }
                            } else {
                                isContanierScanned = false;
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

    public void getSLocs() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setAccountID(accountId);
            inboundDTO.setInboundID(inboundId);
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetStorageLocations(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
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
                                DialogUtils.showAlertDialog(getActivity(), owmsExceptionMessage.getWMSMessage());
                            } else {

                                List<LinkedTreeMap<?, ?>> _lstSLoc = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstSLoc = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<InboundDTO> lstDto = new ArrayList<InboundDTO>();
                                List<String> _lstSLocNames = new ArrayList<>();

                                for (int i = 0; i < _lstSLoc.size(); i++) {
                                    InboundDTO dto = new InboundDTO(_lstSLoc.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                for (int i = 0; i < lstDto.size(); i++) {
                                    _lstSLocNames.add(lstDto.get(i).getStorageLocation());
                                }


                                ArrayAdapter arrayAdapterSLoc = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, _lstSLocNames);
                                spinnerSelectSloc.setAdapter(arrayAdapterSLoc);
                                /*int getPostion = _lstSLocNames.indexOf("OK");
                                String compareValue = String.valueOf(_lstSLocNames.get(getPostion).toString());
                                if (compareValue != null) {
                                    int spinnerPosition = arrayAdapterSLoc.getPosition(compareValue);
                                    spinnerSelectSloc.setSelection(spinnerPosition);
                                }*/

                                ProgressDialogUtils.closeProgressDialog();

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

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
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
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
        }

    }


    // for auto mode
    public void getReceivedQty() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setMcode(Materialcode);
            inboundDTO.setStoreRefNo(lblStoreRefNo.getText().toString());
            inboundDTO.setInboundID(inboundId);
            inboundDTO.setVehicleNo(vehicleNo);
            inboundDTO.setLineNo(lineNo);
            inboundDTO.setBatchNo(etBatch.getText().toString());
            inboundDTO.setSerialNo(etSerial.getText().toString());
            inboundDTO.setMfgDate(etMfgDate.getText().toString());
            inboundDTO.setExpDate(etExpDate.getText().toString());
            inboundDTO.setProjectRefno(etPrjRef.getText().toString());
            inboundDTO.setMRP(etMRP.getText().toString());
            inboundDTO.setHUSize(huSize);
            inboundDTO.setHUNo(huNo);
            inboundDTO.setAccountID(accountId);
            inboundDTO.setUserId(userId);
            if (supplierInvoiceDetailsId == null || supplierInvoiceDetailsId.equals("") || supplierInvoiceDetailsId.isEmpty())
                inboundDTO.setSupplierInvoiceDetailsID("0");
            else
                inboundDTO.setSupplierInvoiceDetailsID(supplierInvoiceDetailsId);
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetReceivedQty(message);
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

                        try {
                            if (core != null) {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.warning_img);

                                        etQty.setText("");

                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        return;
                                    }

                                } else {
                                    List<LinkedTreeMap<?, ?>> _lINB = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lINB = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    InboundDTO dto = null;
                                    ProgressDialogUtils.closeProgressDialog();

                                    for (int i = 0; i < _lINB.size(); i++) {

                                        dto = new InboundDTO(_lINB.get(i).entrySet());

                                        receivedQty = dto.getReceivedQty().split("[.]")[0];
                                        pendingQty = dto.getItemPendingQty().split("[.]")[0];

                                        // preventing excess receiving in auto mode
                                        lblInboundQty.setText(receivedQty + "/" + pendingQty);

                                        if (receivedQty.equals(pendingQty)) {

                                            etQty.setText("");
                                            common.showUserDefinedAlertType(errorMessages.EMC_0075, getActivity(), getContext(), "Success");

                                        } else {

                                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanSku.setImageResource(R.drawable.check);

                                            ValidateRSNAndReceive();
                                        }

                                    }
                                }
                            }
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

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
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
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
        }

    }

    // Separated for getReceivedQty() method for manual mode
    public void getreceivedQty() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setMcode(Materialcode);
            inboundDTO.setStoreRefNo(lblStoreRefNo.getText().toString());
            inboundDTO.setInboundID(inboundId);
            inboundDTO.setLineNo(lineNo);
            inboundDTO.setBatchNo(etBatch.getText().toString());
            inboundDTO.setSerialNo(etSerial.getText().toString());
            inboundDTO.setMfgDate(etMfgDate.getText().toString());
            inboundDTO.setExpDate(etExpDate.getText().toString());
            inboundDTO.setProjectRefno(etPrjRef.getText().toString());
            inboundDTO.setMRP(etMRP.getText().toString());
            inboundDTO.setAccountID(accountId);
            inboundDTO.setUserId(userId);
            inboundDTO.setHUNo(huNo);
            inboundDTO.setHUSize(huSize);
            if (supplierInvoiceDetailsId == null || supplierInvoiceDetailsId.equals("") || supplierInvoiceDetailsId.isEmpty())
                inboundDTO.setSupplierInvoiceDetailsID("0");
            else
                inboundDTO.setSupplierInvoiceDetailsID(supplierInvoiceDetailsId);
            inboundDTO.setVehicleNo(vehicleNo);

            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetReceivedQty(message);
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

                        try {
                            if (core != null) {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.warning_img);

                                        etQty.setText("");

                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        return;
                                    }

                                } else {
                                    List<LinkedTreeMap<?, ?>> _lINB = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lINB = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    InboundDTO dto = null;
                                    ProgressDialogUtils.closeProgressDialog();

                                    for (int i = 0; i < _lINB.size(); i++) {

                                        dto = new InboundDTO(_lINB.get(i).entrySet());

                                        receivedQty = dto.getReceivedQty().split("[.]")[0];
                                        pendingQty = dto.getItemPendingQty().split("[.]")[0];

                                        // preventing excess receiving in auto mode

                                        lblInboundQty.setText(receivedQty + "/" + pendingQty);


                                        if (receivedQty.equals(pendingQty)) {

                                            isRsnScanned = false;
                                            etQty.setText("");
                                            common.showUserDefinedAlertType(errorMessages.EMC_0075, getActivity(), getContext(), "Success");

                                        } else {

                                            isRsnScanned = true;

                                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanSku.setImageResource(R.drawable.check);

                                            soundUtils.alertWarning(getActivity(), getContext());
                                            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0073);
                                        }
                                    }
                                }
                            }
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

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
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
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
        }

    }

    public void ValidateRSNAndReceive() {


        if (!etSerial.getText().toString().isEmpty()) {
            if (!etQty.getText().toString().equals("1")) {
                common.showUserDefinedAlertType(errorMessages.EMC_0066, getActivity(), getContext(), "Warning");
                return;
            }
        }

        if (etQty.getText().toString().isEmpty()) {
            common.showUserDefinedAlertType(errorMessages.EMC_0067, getActivity(), getContext(), "Error");
            return;
        }

        if (etQty.getText().toString().equals("0")) {
            common.showUserDefinedAlertType(errorMessages.EMC_0068, getActivity(), getContext(), "Error");
            return;
        }

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setMcode(Materialcode);
            inboundDTO.setStoreRefNo(lblStoreRefNo.getText().toString());
            inboundDTO.setCartonNo(etPallet.getText().toString());
            inboundDTO.setStorageLocation(storageLoc);
            inboundDTO.setIsDam(String.valueOf(cbDescripency.isChecked()));
            inboundDTO.setUserId(userId);
            inboundDTO.setAccountID(accountId);
            if (scanType.equals("Manual")) {
                inboundDTO.setQty(etQty.getText().toString());
            } else {
                inboundDTO.setQty("1");
            }
            inboundDTO.setBatchNo(etBatch.getText().toString());
            inboundDTO.setSerialNo(etSerial.getText().toString());
            inboundDTO.setMfgDate(etMfgDate.getText().toString());
            inboundDTO.setExpDate(etExpDate.getText().toString());
            inboundDTO.setProjectRefno(etPrjRef.getText().toString());
            inboundDTO.setHUNo(huNo);
            inboundDTO.setHUSize(huSize);
            if (String.valueOf(cbDescripency.isChecked()).equals("true")) {
                inboundDTO.setHasDisc("1");
            } else {
                inboundDTO.setHasDisc("0");
            }
            if (supplierInvoiceDetailsId == null || supplierInvoiceDetailsId.equals("") || supplierInvoiceDetailsId.isEmpty())
                inboundDTO.setSupplierInvoiceDetailsID("0");
            else
                inboundDTO.setSupplierInvoiceDetailsID(supplierInvoiceDetailsId);
            inboundDTO.setLineNo(lineNo);
            inboundDTO.setInboundID(inboundId);
            inboundDTO.setMRP(etMRP.getText().toString());
            inboundDTO.setIsDam("0");
            inboundDTO.setDock(etDock.getText().toString());
            inboundDTO.setVehicleNo(vehicleNo);

            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.UpdateReceiveItemForHHT(message);
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

                        try {
                            if (core != null) {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.warning_img);

                                        etQty.setText("");

                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        return;
                                    }

                                } else {
                                    List<LinkedTreeMap<?, ?>> _lINB = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lINB = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    InboundDTO dto = null;
                                    ProgressDialogUtils.closeProgressDialog();

                                    for (int i = 0; i < _lINB.size(); i++) {

                                        dto = new InboundDTO(_lINB.get(i).entrySet());

                                        if (dto.getResult().equals("Success")) {

                                            receivedQty = dto.getReceivedQty().split("[.]")[0];
                                            pendingQty = dto.getItemPendingQty().split("[.]")[0];

                                            lblInboundQty.setText(receivedQty + "/" + pendingQty);

                                            etExpDate.setText("");
                                            etMfgDate.setText("");
                                            etBatch.setText("");
                                            etQty.setText("");
                                            etPrjRef.setText("");
                                            etSerial.setText("");
                                            etKidID.setText("");
                                            etMRP.setText("");

                                            Materialcode = "";
                                            lblScannedSku.setText("");
                                            etQty.setEnabled(false);
                                            etQty.clearFocus();
                                            etQty.setText("");

                                            lblHu.setText("");
                                            huSize = "";
                                            huNo = "";

                                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                            ivScanSku.setImageResource(R.drawable.fullscreen_img);

                                            if (receivedQty.equals(pendingQty)) {          // if inbound completes for the single line item

                                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                ivScanSku.setImageResource(R.drawable.check);
                                                common.showUserDefinedAlertType(errorMessages.EMC_0075, getActivity(), getContext(), "Success");

                                            } else {

                                                soundUtils.alertSuccess(getActivity(), getContext());
                                                return;
                                            }

                                        } else {
                                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanSku.setImageResource(R.drawable.invalid_cross);
                                            common.showUserDefinedAlertType(dto.getResult(), getActivity(), getContext(), "Error");
                                        }
                                    }
                                }
                            }
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

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
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
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_04", getActivity());
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
                ProgressDialogUtils.closeProgressDialog();
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);


                        } catch (Exception ex) {

                            /*try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002",getContext());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            logException();*/


                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        ProgressDialogUtils.closeProgressDialog();
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
                    }
                });
            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                // Toast.makeText(LoginActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
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
            // notifications while paused.
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_goodsIn));
    }

    //Barcode scanner API
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (barcodeReader != null) {
            // unregister barcode event listener honeywell
            barcodeReader.removeBarcodeListener((BarcodeReader.BarcodeListener) this);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener((BarcodeReader.TriggerListener) this);
        }

        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", false);
        getActivity().sendBroadcast(RTintent);
        getActivity().unregisterReceiver(this.myDataReceiver);
        super.onDestroyView();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        storageLoc = spinnerSelectSloc.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    @Override
    public boolean onLongClick(View view) {
        return false;
    }

}