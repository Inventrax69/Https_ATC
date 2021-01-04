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
import com.inventrax.atc.common.Common;
import com.inventrax.atc.common.constants.EndpointConstants;
import com.inventrax.atc.common.constants.ErrorMessages;
import com.inventrax.atc.interfaces.ApiInterface;
import com.inventrax.atc.pojos.InboundDTO;
import com.inventrax.atc.pojos.PutawayDTO;
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
 * Created by Padmaja.B on 20/12/2018.
 */

public class PutawayFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private View rootView;
    private static final String classCode = "API_FRAG_PUTAWAY";

    private RelativeLayout rlStRefSelect, rlLocationScan, rlPutaway, rlSkip;
    private SearchableSpinner spinnerSelectStRef, spinnerSelectReason;
    private TextInputLayout txtInputLayoutBatch, txtInputLayoutSerial, txtInputLayoutMfgDate,
            txtInputLayoutExpDate, txtInputLayoutProjectRef, txtInputLayoutQty, txtInputLayoutMRP;
    private EditText etBatch, etSerial, etMfgDate, etExpDate, etProjectRef, etQty, etMRP,etScannedLocation;
    private ImageView ivScanLocation, ivScanPallet, ivScanSku, ivScanDock;
    private Button btnGo, btnClear, btnSkip,
            btnOk, btnCloseSkip, btnPutaway;
    private CardView cvScanPallet, cvScanLocation, cvScanSku, cvScanDock;
    private TextView lblRefNo, lblSuggestedLoc, lblScannedLocation, lblStoreRefNo,
            lblPutawayQty, lblContainer, lblSKU, lblScannedDock, lblDock,lblScannedLoc;

    private ScanValidator scanValidator;
    String scanner = null, SuggestedId = null, inboundId = null,
            suggestedQty = null, suggestedReceivedQty = null, totalQty = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;

    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private WMSCoreMessage core;
    private Common common = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    List<String> lstLocMaterialQty = null;
    private String refNo = null, SKU = "", skipReason = null,
            loc = null, materialMasterId = null, kitId = null, lineNo = null;
    private String userId = null, scanType = null, accountId = null, suggestedPutawayId = null;
    public List<String> lstPalletnumberHeader = null;
    List<InboundDTO> lstInbound = null;
    private boolean isContainerScanned = false, isDockScanned = false, restrictScan = false,isLocationScanned=false;
    SoundUtils soundUtils;
    PutawayDTO returningObj;


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

    public PutawayFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_putaway, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    /// Loading form Controls
    private void loadFormControls() {


        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        scanType = sp.getString("scanType", "");
        accountId = sp.getString("AccountId", "");

        rlStRefSelect = (RelativeLayout) rootView.findViewById(R.id.rlStRefSelect);
        rlLocationScan = (RelativeLayout) rootView.findViewById(R.id.rlLocationScan);
        rlPutaway = (RelativeLayout) rootView.findViewById(R.id.rlPutaway);
        rlSkip = (RelativeLayout) rootView.findViewById(R.id.rlSkip);

        txtInputLayoutSerial = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutSerial);
        txtInputLayoutMfgDate = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMfgDate);
        txtInputLayoutExpDate = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutExpDate);
        txtInputLayoutBatch = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutBatch);
        txtInputLayoutProjectRef = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutProjectRef);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);
        txtInputLayoutMRP = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMRP);

        etSerial = (EditText) rootView.findViewById(R.id.etSerial);
        etMfgDate = (EditText) rootView.findViewById(R.id.etMfgDate);
        etExpDate = (EditText) rootView.findViewById(R.id.etExpDate);
        etBatch = (EditText) rootView.findViewById(R.id.etBatch);
        etProjectRef = (EditText) rootView.findViewById(R.id.etProjectRef);
        etQty = (EditText) rootView.findViewById(R.id.etQty);
        etMRP = (EditText) rootView.findViewById(R.id.etMRP);
        etScannedLocation = (EditText) rootView.findViewById(R.id.etScannedLocation);


        etQty.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanSku = (CardView) rootView.findViewById(R.id.cvScanSku);
        cvScanDock = (CardView) rootView.findViewById(R.id.cvScanDock);

        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanSku = (ImageView) rootView.findViewById(R.id.ivScanSku);
        ivScanDock = (ImageView) rootView.findViewById(R.id.ivScanDock);

        btnGo = (Button) rootView.findViewById(R.id.btnGo);
        btnClear = (Button) rootView.findViewById(R.id.btnClear);
        btnSkip = (Button) rootView.findViewById(R.id.btnSkip);
        btnOk = (Button) rootView.findViewById(R.id.btnOk);
        btnCloseSkip = (Button) rootView.findViewById(R.id.btnCloseSkip);
        btnPutaway = (Button) rootView.findViewById(R.id.btnPutaway);

        lstInbound = new ArrayList<InboundDTO>();
        returningObj = new PutawayDTO();

        spinnerSelectStRef = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectStRef);
        spinnerSelectStRef.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refNo = spinnerSelectStRef.getSelectedItem().toString();
                lblStoreRefNo.setText(refNo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        spinnerSelectReason = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectReason);
        spinnerSelectReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                skipReason = spinnerSelectReason.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        lblRefNo = (TextView) rootView.findViewById(R.id.lblRefNo);
        lblSuggestedLoc = (TextView) rootView.findViewById(R.id.lblSuggestedLoc);
        lblScannedLocation = (TextView) rootView.findViewById(R.id.lblScannedLocation);
        lblStoreRefNo = (TextView) rootView.findViewById(R.id.lblStoreRefNo);
        lblPutawayQty = (TextView) rootView.findViewById(R.id.lblPutawayQty);
        lblContainer = (TextView) rootView.findViewById(R.id.lblContainer);
        lblSKU = (TextView) rootView.findViewById(R.id.lblSKU);
        lblScannedDock = (TextView) rootView.findViewById(R.id.lblScannedDock);
        lblDock = (TextView) rootView.findViewById(R.id.lblDock);

        btnGo.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        btnCloseSkip.setOnClickListener(this);
        btnPutaway.setOnClickListener(this);
        cvScanPallet.setOnClickListener(this);

        if (scanType.equals("Auto")) {                      // Disabling Putaway button in Auto mode
            btnPutaway.setEnabled(false);
            btnPutaway.setTextColor(getResources().getColor(R.color.black));
            btnPutaway.setBackgroundResource(R.drawable.button_hide);
        } else {
            btnPutaway.setEnabled(true);
            btnPutaway.setTextColor(getResources().getColor(R.color.white));
            btnPutaway.setBackgroundResource(R.drawable.button_shape);
        }

        common = new Common();
        gson = new GsonBuilder().create();
        errorMessages = new ErrorMessages();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        soundUtils = new SoundUtils();

        ProgressDialogUtils.closeProgressDialog();
        common.setIsPopupActive(false);

        if (getArguments() != null) {
            if (getArguments().getString("SuggestedId") != null) {
                try {
                    SuggestedId = getArguments().getString("SuggestedId");
                } catch (Exception ex) {
                }
            }
        }
        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);


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


        // To get Store Ref#
        getStoreRefNo();

    }

    public void getInboundId() {
        for (InboundDTO oInbound : lstInbound) {
            if (oInbound.getStoreRefNo().equals(refNo)) {                // Gets selected inbound id of ref# from the list
                // if the selected ref# equals
                // to the list of ref no
                inboundId = oInbound.getInboundID();
            }
        }
    }

    //Button clicks
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnCloseTwo:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

          case R.id.cvScanPallet:
              if(!isContainerScanned){
                  isContainerScanned = true;
                  cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                  ivScanPallet.setImageResource(R.drawable.check);
              }else{
                  isContainerScanned = false;
                  cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                  ivScanPallet.setImageResource(R.drawable.fullscreen_img);
              }

              break;


            case R.id.btnGo:

                if (refNo != null) {
                    rlStRefSelect.setVisibility(View.GONE);
                    rlPutaway.setVisibility(View.VISIBLE);
                    rlSkip.setVisibility(View.GONE);
                    rlLocationScan.setVisibility(View.GONE);

                    lblRefNo.setText(refNo);

                    // To get Inbound Id of the selected Store Ref#
                    getInboundId();

                    // To get Putaway suggestions
                    getItemTOPutAway();

                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0051, getActivity(), getContext(), "Error");
                    return;
                }
                break;

            case R.id.btnClear:

                // Clears the UI

                isLocationScanned=false;
                isContainerScanned=false;

                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                ivScanLocation.setImageResource(R.drawable.fullscreen_img);

                cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                ivScanPallet.setImageResource(R.drawable.fullscreen_img);

                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                ivScanSku.setImageResource(R.drawable.fullscreen_img);

                etQty.setText("");
                etScannedLocation.setText("");

                break;

            case R.id.btnSkip:

                // Skip reasons list method calling

                if (!lblSKU.getText().toString().isEmpty()) {

                    if (!etQty.getText().toString().isEmpty() && !etQty.getText().toString().equals("0")) {

                        rlStRefSelect.setVisibility(View.GONE);
                        rlPutaway.setVisibility(View.GONE);
                        rlLocationScan.setVisibility(View.GONE);
                        rlSkip.setVisibility(View.VISIBLE);

                        getSkipReasonList();

                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0072, getActivity(), getContext(), "Error");
                        return;
                    }


                }

                break;

            case R.id.btnPutaway:

                if (SKU != null && SKU != "" && SKU.equalsIgnoreCase(lblSKU.getText().toString())) {

                    if (!etQty.getText().toString().equals("0") && !etQty.getText().toString().isEmpty()) {
                        // Inserting Item into the location
                        UpsertPutAwayItem();
                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0068, getActivity(), getContext(), "Error");
                        return;
                    }

                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0023, getActivity(), getContext(), "Error");
                    return;
                }

                break;

            case R.id.btnOk:

                // Skipping Location

                DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                        "Are you sure to skip this Location? ", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        common.setIsPopupActive(false);

                                        if (!skipReason.equals("")) {
                                            // To skip the item and regenerating suggestions
                                            skipItem();
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

                rlStRefSelect.setVisibility(View.GONE);
                rlLocationScan.setVisibility(View.GONE);
                rlSkip.setVisibility(View.GONE);
                rlPutaway.setVisibility(View.VISIBLE);

                break;

            default:
                break;
        }


    }


    // Honeywell Barcode read event
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

        if (scannedData != null && !common.isPopupActive() && !restrictScan) {

            if(!ProgressDialogUtils.isProgressActive()){

                if(rlPutaway.getVisibility() == View.VISIBLE && !lblSKU.getText().toString().isEmpty()){

                    if(isDockScanned){

                        if(!isLocationScanned)
                            ValidateLocation(scannedData);
                        else{
                            if(!isContainerScanned)
                                ValidatePallet(scannedData);
                            else
                                ValiDateMaterial(scannedData);

                        }



/*                    if (lblSuggestedLoc.getText().toString().equals(scannedData)) {
                        lblScannedLocation.setText(scannedData);
                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanLocation.setImageResource(R.drawable.check);

                        rlStRefSelect.setVisibility(View.GONE);
                        rlLocationScan.setVisibility(View.GONE);
                        rlSkip.setVisibility(View.GONE);
                        rlPutaway.setVisibility(View.VISIBLE);

                        lblStoreRefNo.setText(refNo);

                        return;
                    } else {
                        lblScannedLocation.setText("");
                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanLocation.setImageResource(R.drawable.invalid_cross);
                        common.showUserDefinedAlertType(errorMessages.EMC_0013, getActivity(), getContext(), "Error");
                        return;
                    }*/

                    }
                    else{
                        if (lblDock.getText().toString().equals(scannedData)) {

                            lblScannedDock.setText(scannedData);
                            isDockScanned = true;

                            cvScanDock.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanDock.setImageResource(R.drawable.check);

                        } else {
                            common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                        }

                    }

                }
            }
            else {
                if(!Common.isPopupActive())
                {
                    common.showUserDefinedAlertType(errorMessages.EMC_080, getActivity(), getContext(), "Error");

                }
                soundUtils.alertWarning(getActivity(),getContext());

            }



/*            if (rlPutaway.getVisibility() == View.VISIBLE && !lblSKU.getText().toString().isEmpty()) {





                if (ScanValidator.isItemScanned(scannedData)) {       // Item Scan

                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                    ivScanSku.setImageResource(R.drawable.fullscreen_img);

                    etQty.setText("");

                    if (scannedData.split("[|]").length != 5) {

                        if (scannedData.split("[|]")[0].equals(lblSKU.getText().toString())) {
                            SKU = scannedData.split("[|]")[0];
                            etBatch.setText(scannedData.split("[|]")[1]);
                            etSerial.setText(scannedData.split("[|]")[2]);
                            etMfgDate.setText(scannedData.split("[|]")[3]);
                            etExpDate.setText(scannedData.split("[|]")[4]);
                            etProjectRef.setText(scannedData.split("[|]")[5]);
                            kitId = scannedData.split("[|]")[6];
                            etMRP.setText(scannedData.split("[|]")[7]);
                            lineNo = scannedData.split("[|]")[8];
                        } else {
                            common.showUserDefinedAlertType(errorMessages.EMC_0053, getActivity(), getContext(), "Error");
                            return;
                        }

                    } else {
                        SKU = scannedData.split("[|]")[0];
                        etBatch.setText(scannedData.split("[|]")[1]);
                        etSerial.setText(scannedData.split("[|]")[2]);
                        kitId = scannedData.split("[|]")[3];
                        lineNo = scannedData.split("[|]")[4];
                    }

                    if (SKU.equals(lblSKU.getText().toString())) {

                        if (scanType.equals("Auto")) {

                            etQty.setText("1");

                            // Checking putaway Qty. against to the putaway
                            checkPutAwayItemQty();

                            return;
                        } else {

                            // Checking putaway Qty. against to the putaway
                            checkPutAwayItemQtyforManualMode();

                        }
                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0053, getActivity(), getContext(), "Error");
                        return;
                    }


                }
                else if (rlPutaway.getVisibility() == View.VISIBLE && ScanValidator.isContainerScanned(scannedData) && !lblScannedLocation.getText().toString().isEmpty()) {

                    if (lblContainer.getText().toString().isEmpty() || lblContainer.getText().toString().equals("0")) {

                        lblContainer.setText(scannedData);
                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanPallet.setImageResource(R.drawable.check);
                        // Validating scanned Pallet is there in that location or not
                        validatePalletCode();

                    } else if (lblContainer.getText().toString().equals(scannedData)) {

                        lblContainer.setText(scannedData);
                       *//* cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanPallet.setImageResource(R.drawable.check);

                        isContainerScanned = true;*//*


                        validatePalletCode();

                    } else {
                        lblContainer.setText("");
                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanPallet.setImageResource(R.drawable.invalid_cross);
                        common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Error");
                        return;
                    }
                }

            }
            else if (rlLocationScan.getVisibility() == View.VISIBLE) {


*//*                if (ScanValidator.isDockLocationScanned(scannedData)) {

                    if (lblDock.getText().toString().equals(scannedData)) {

                        lblScannedDock.setText(scannedData);
                        isDockScanned = true;

                        cvScanDock.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanDock.setImageResource(R.drawable.check);

                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
                    }

                }
                else if (ScanValidator.isLocationScanned(scannedData)) {

                    if (isDockScanned) {

                        // User should scan the suggested location only
                        // if suggested and scanned locations are equal then only it goes to next screen
                        if (lblSuggestedLoc.getText().toString().equals(scannedData)) {
                            lblScannedLocation.setText(scannedData);
                            cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanLocation.setImageResource(R.drawable.check);

                            rlStRefSelect.setVisibility(View.GONE);
                            rlLocationScan.setVisibility(View.GONE);
                            rlSkip.setVisibility(View.GONE);
                            rlPutaway.setVisibility(View.VISIBLE);

                            lblStoreRefNo.setText(refNo);

                            return;
                        } else {
                            lblScannedLocation.setText("");
                            cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanLocation.setImageResource(R.drawable.invalid_cross);
                            common.showUserDefinedAlertType(errorMessages.EMC_0013, getActivity(), getContext(), "Error");
                            return;
                        }
                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0018, getActivity(), getContext(), "Error");
                    }
                }
                else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Error");
                    return;
                }*//*
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

                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                            ivScanSku.setImageResource(R.drawable.fullscreen_img);
                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                        } else {
                            LinkedTreeMap<?, ?>_lResult = new LinkedTreeMap<>();
                            _lResult = (LinkedTreeMap<?, ?>) core.getEntityObject();

                            Log.v("ABCDE",new Gson().toJson(_lResult));

                            ScanDTO scanDTO1=new ScanDTO(_lResult.entrySet());
                            ProgressDialogUtils.closeProgressDialog();
                            if(scanDTO1!=null){
                                if(scanDTO1.getScanResult()){

                                /* ----For RSN reference----
                                    0 Sku|1 BatchNo|2 SerialNO|3 MFGDate|4 EXpDate|5 ProjectRefNO|6 Kit Id|7 line No|8 MRP ---- For SKU with 9 MSP's

                                    0 Sku|1 BatchNo|2 SerialNO|3 KitId|4 lineNo  ---- For SKU with 5 MSP's   *//*
                                    // Eg. : ToyCar|1|bat1|ser123|12/2/2018|12/2/2019|0|001*/


                                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                    ivScanSku.setImageResource(R.drawable.fullscreen_img);

                                    /*    if (scannedData.split("[|]").length != 5) {*/

                                    SKU=scanDTO1.getSkuCode();
                                    etBatch.setText(scanDTO1.getBatch());
                                    etSerial.setText(scanDTO1.getSerialNumber());
                                    etMfgDate.setText(scanDTO1.getMfgDate());
                                    etExpDate.setText(scanDTO1.getExpDate());
                                    etProjectRef.setText(scanDTO1.getPrjRef());
                                    kitId=scanDTO1.getKitID();
                                    etMRP.setText(scanDTO1.getMrp());

                                    lineNo = scanDTO1.getLineNumber();
                                    //.setText(Materialcode);


                                    //   etMRP.setText(scannedData.split("[|]")[7]);


/*                                    } else {
                                        Materialcode = scannedData.split("[|]")[0];
                                        etBatch.setText(scannedData.split("[|]")[1]);
                                        etSerial.setText(scannedData.split("[|]")[2]);
                                        etKidID.setText(scannedData.split("[|]")[3]);
                                        lineNo = scannedData.split("[|]")[4];
                                    }*/

                                    if (SKU.equals(lblSKU.getText().toString())) {

                                        if (scanType.equals("Auto")) {

                                            etQty.setText("1");

                                            // Checking putaway Qty. against to the putaway
                                            checkPutAwayItemQty();

                                            return;
                                        } else {

                                            // Checking putaway Qty. against to the putaway
                                            checkPutAwayItemQtyforManualMode();

                                        }
                                    }

                                } else{
                                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanSku.setImageResource(R.drawable.warning_img);
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

                            lblContainer.setText("");
                            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanPallet.setImageResource(R.drawable.invalid_cross);
                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                        } else {
                            LinkedTreeMap<?, ?>_lResult = new LinkedTreeMap<>();
                            _lResult = (LinkedTreeMap<?, ?>) core.getEntityObject();

                            ScanDTO scanDTO1=new ScanDTO(_lResult.entrySet());
                            ProgressDialogUtils.closeProgressDialog();
                            if(scanDTO1!=null){
                                if(scanDTO1.getScanResult()){
                                    lblContainer.setText(scannedData);
                                    //ValidatePalletCode();
                                    isContainerScanned = true;
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.check);
                                } else{
                                    isContainerScanned=false;
                                    lblContainer.setText("");
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Warning");
                                }
                            }else{
                                isContainerScanned=false;
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

                            isLocationScanned=false;
                            etScannedLocation.setText("");
                            cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanLocation.setImageResource(R.drawable.invalid_cross);
                           // common.showUserDefinedAlertType(errorMessages.EMC_0013, getActivity(), getContext(), "Error");
                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                        } else {
                            LinkedTreeMap<?, ?>_lResult = new LinkedTreeMap<>();
                            _lResult = (LinkedTreeMap<?, ?>) core.getEntityObject();

                            ScanDTO scanDTO1=new ScanDTO(_lResult.entrySet());

                            if(scanDTO1!=null){
                                if(scanDTO1.getScanResult()){
                                    etScannedLocation.setText(scannedData);
                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanLocation.setImageResource(R.drawable.check);
                                    isLocationScanned=true;
                                    rlStRefSelect.setVisibility(View.GONE);
                                    rlLocationScan.setVisibility(View.GONE);
                                    rlSkip.setVisibility(View.GONE);
                                    rlPutaway.setVisibility(View.VISIBLE);


                                } else{
                                    isLocationScanned=false;
                                    etScannedLocation.setText("");
                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanLocation.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0013, getActivity(), getContext(), "Warning");
                                }
                            }else{
                                isDockScanned=false;
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

    public void getStoreRefNo() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setAccountID(accountId);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetStoreRefNos(message);
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
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                List<LinkedTreeMap<?, ?>> _lstPutaway = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstPutaway = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<InboundDTO> lstDto = new ArrayList<InboundDTO>();
                                List<String> _lstRefNo = new ArrayList<>();


                                for (int i = 0; i < _lstPutaway.size(); i++) {
                                    InboundDTO dto = new InboundDTO(_lstPutaway.get(i).entrySet());
                                    lstDto.add(dto);
                                    lstInbound.add(dto);
                                }

                                for (int i = 0; i < lstDto.size(); i++) {

                                    // List of store ref no.
                                    _lstRefNo.add(lstDto.get(i).getStoreRefNo());


                                }


                                ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, _lstRefNo);
                                spinnerSelectStRef.setAdapter(arrayAdapter);
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

   /* public void validatePalletCode() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setAccountID(accountId);
            inboundDTO.setPalletNo(lblContainer.getText().toString());
            inboundDTO.setInboundID(inboundId);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.CheckContainer(message);
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

                            lblContainer.setText("");
                            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanPallet.setImageResource(R.drawable.invalid_cross);
                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                        } else {
                            List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                            _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                            InboundDTO dto = null;
                            ProgressDialogUtils.closeProgressDialog();

                            for (int i = 0; i < _lResult.size(); i++) {

                                dto = new InboundDTO(_lResult.get(i).entrySet());
                                // if result 1 then its a valid pallet
                                if (dto.getResult().equals("1")) {

                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.check);
                                    isContainerScanned = true;
                                } else {
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.invalid_cross);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Error");
                                }
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
    }*/

    public void getItemTOPutAway() {


        WMSCoreMessage message = new WMSCoreMessage();
        message = common.SetAuthentication(EndpointConstants.PutAwayDTO, getContext());
        PutawayDTO putawayDTO = new PutawayDTO();
        putawayDTO.setTransferRequestDetailsId(refNo);
        putawayDTO.setUserID(userId);
        putawayDTO.setInboundId(inboundId);

        message.setEntityObject(putawayDTO);


        Call<String> call = null;
        ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

        try {
            //Checking for Internet Connectivity
            // if (NetworkUtils.isInternetAvailable()) {
            // Calling the Interface method
            ProgressDialogUtils.showProgressDialog("Please Wait");
            call = apiService.GetItemTOPutAway(message);
            // } else {
            // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            // return;
            // }kn 0.

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

                        if (response.body() != null) {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;

                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                }


                            } else {
                                List<LinkedTreeMap<?, ?>> _lstPutaway = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstPutaway = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<PutawayDTO> lstDto = new ArrayList<PutawayDTO>();

                                ProgressDialogUtils.closeProgressDialog();

                                for (int i = 0; i < _lstPutaway.size(); i++) {
                                    PutawayDTO dto = new PutawayDTO(_lstPutaway.get(i).entrySet());

                                    if (dto.getResult().equals("3")) {

                                        // Putaway suggestions

                                        if (!dto.getCartonCode().equals("0") && !dto.getCartonCode().equals("")) {

                                            lblContainer.setText(dto.getCartonCode());
                                        }
                                        lblSuggestedLoc.setText(dto.getLocation());
                                        lblSKU.setText(dto.getMCode());
                                        etBatch.setText(dto.getBatchNo());
                                        etSerial.setText(dto.getSerialNo());
                                        etMfgDate.setText(dto.getMfgDate());
                                        etExpDate.setText(dto.getExpDate());
                                        etProjectRef.setText(dto.getProjectRefNo());
                                        etMRP.setText(dto.getMRP());


                                        lblDock.setText(dto.getDock());
                                        materialMasterId = dto.getMaterialMasterId();

                                        suggestedQty = dto.getSuggestedQty();
                                        suggestedReceivedQty = dto.getSuggestedReceivedQty();


                                        totalQty = String.valueOf(Integer.parseInt(suggestedQty.split("[.]")[0]) - Integer.parseInt(suggestedReceivedQty.split("[.]")[0]));

                                        lblPutawayQty.setText(dto.getSuggestedReceivedQty() + "/" + dto.getSuggestedQty());

                                        suggestedPutawayId = dto.getSuggestedPutawayID();

                                        returningObj = dto;

                                    } else if (dto.getResult().equals("1")) {
                                        // Putasway completed
                                        common.showUserDefinedAlertType(errorMessages.EMC_0074 + "" + refNo, getActivity(), getContext(), "Success");
                                        restrictScan = true;
                                    } else if (dto.getResult().equals("2")) {
                                        // Putaway not yet initiated
                                        common.showUserDefinedAlertType(errorMessages.EMC_0078, getActivity(), getContext(), "Warning");
                                        restrictScan = true;
                                    }
                                }
                            }
                        } else {
                            ProgressDialogUtils.closeProgressDialog();
                            //common.showUserDefinedAlertType(errorMessages.EMC_0024, getActivity(), getContext(), "Warning");
                            return;
                        }
                    } catch (Exception ex) {
                        try {
                            exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_02", getActivity());
                            logException();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable throwable) {

                    ProgressDialogUtils.closeProgressDialog();
                    common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                }
            });
        } catch (
                Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }

    }

    public void checkPutAwayItemQty() {


        WMSCoreMessage message = new WMSCoreMessage();
        message = common.SetAuthentication(EndpointConstants.PutAwayDTO, getContext());
        PutawayDTO putawayDTO = new PutawayDTO();

        putawayDTO = returningObj;

        putawayDTO.setUserID(userId);
        putawayDTO.setTransferRequestDetailsId(refNo);
        putawayDTO.setInboundId(inboundId);
        putawayDTO.setMaterialMasterId(materialMasterId);
        putawayDTO.setDock(lblScannedDock.getText().toString());

        message.setEntityObject(putawayDTO);


        Call<String> call = null;
        ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

        try {
            //Checking for Internet Connectivity
            // if (NetworkUtils.isInternetAvailable()) {
            // Calling the Interface method
            ProgressDialogUtils.showProgressDialog("Please Wait");
            call = apiService.CheckPutAwayItemQty(message);
            // } else {
            // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            // return;
            // }kn 0.

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

                        if (response.body() != null) {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;

                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                }

                                SKU = "";
                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanSku.setImageResource(R.drawable.warning_img);

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                            } else {
                                List<LinkedTreeMap<?, ?>> _lstPutaway = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstPutaway = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<PutawayDTO> lstDto = new ArrayList<PutawayDTO>();

                                ProgressDialogUtils.closeProgressDialog();

                                for (int i = 0; i < _lstPutaway.size(); i++) {
                                    PutawayDTO dto = new PutawayDTO(_lstPutaway.get(i).entrySet());

                                    if (dto.getResult().equals("1")) {

                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.check);

                                        // Inserting Item into the location
                                        UpsertPutAwayItem();

                                    } else {

                                    }
                                }

                            }
                        } else {
                            ProgressDialogUtils.closeProgressDialog();
                            common.showUserDefinedAlertType(errorMessages.EMC_0021, getActivity(), getContext(), "Error");
                            return;
                        }
                    } catch (
                            Exception ex) {
                        try {
                            exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_02", getActivity());
                            logException();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable throwable) {

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
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
        }

    }

    public void checkPutAwayItemQtyforManualMode() {


        WMSCoreMessage message = new WMSCoreMessage();
        message = common.SetAuthentication(EndpointConstants.PutAwayDTO, getContext());
        PutawayDTO putawayDTO = new PutawayDTO();
        putawayDTO = returningObj;
        putawayDTO.setUserID(userId);
        putawayDTO.setTransferRequestDetailsId(refNo);
        putawayDTO.setInboundId(inboundId);
        putawayDTO.setMaterialMasterId(materialMasterId);
        putawayDTO.setDock(lblScannedDock.getText().toString());

        message.setEntityObject(putawayDTO);


        Call<String> call = null;
        ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

        try {
            //Checking for Internet Connectivity
            // if (NetworkUtils.isInternetAvailable()) {
            // Calling the Interface method
            ProgressDialogUtils.showProgressDialog("Please Wait");
            call = apiService.CheckPutAwayItemQty(message);
            // } else {
            // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            // return;
            // }kn 0.

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

                        if (response.body() != null) {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;

                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                }

                                SKU = "";
                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanSku.setImageResource(R.drawable.warning_img);

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                            } else {
                                List<LinkedTreeMap<?, ?>> _lstPutaway = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstPutaway = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<PutawayDTO> lstDto = new ArrayList<PutawayDTO>();

                                ProgressDialogUtils.closeProgressDialog();

                                for (int i = 0; i < _lstPutaway.size(); i++) {
                                    PutawayDTO dto = new PutawayDTO(_lstPutaway.get(i).entrySet());

                                    if (dto.getResult().equals("1")) {

                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.check);

                                        // Enabling putaway button for Manual mode
                                        etQty.setEnabled(true);
                                        btnPutaway.setEnabled(true);

                                        btnPutaway.setTextColor(getResources().getColor(R.color.white));
                                        btnPutaway.setBackgroundResource(R.drawable.button_shape);

                                        soundUtils.alertWarning(getActivity(), getContext());
                                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0073);

                                    } else {

                                    }
                                }

                            }
                        } else {
                            ProgressDialogUtils.closeProgressDialog();
                            common.showUserDefinedAlertType(errorMessages.EMC_0021, getActivity(), getContext(), "Error");
                            return;
                        }
                    } catch (
                            Exception ex) {
                        try {
                            exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_02", getActivity());
                            logException();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable throwable) {

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
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
        }

    }

    public void UpsertPutAwayItem() {


        WMSCoreMessage message = new WMSCoreMessage();
        message = common.SetAuthentication(EndpointConstants.PutAwayDTO, getContext());
        PutawayDTO putawayDTO = new PutawayDTO();
        putawayDTO = returningObj;
        putawayDTO.setTransferRequestDetailsId(lblStoreRefNo.getText().toString());
        putawayDTO.setInboundId(inboundId);
        putawayDTO.setMCode(SKU);
        putawayDTO.setMfgDate(etMfgDate.getText().toString());
        putawayDTO.setExpDate(etExpDate.getText().toString());
        putawayDTO.setBatchNo(etBatch.getText().toString());
        putawayDTO.setProjectRefNo(etProjectRef.getText().toString());
        putawayDTO.setMRP(etMRP.getText().toString());
        putawayDTO.setSerialNo(etSerial.getText().toString());
        putawayDTO.setPutAwayQty(etQty.getText().toString());
        putawayDTO.setUserID(userId);
        putawayDTO.setCartonCode(lblContainer.getText().toString());
        putawayDTO.setLocation(lblSuggestedLoc.getText().toString());
        putawayDTO.setScannedLocation(etScannedLocation.getText().toString());
        putawayDTO.setTotalQty(totalQty);
        putawayDTO.setDock(lblScannedDock.getText().toString());

        message.setEntityObject(putawayDTO);


        Call<String> call = null;
        ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

        try {
            //Checking for Internet Connectivity
            // if (NetworkUtils.isInternetAvailable()) {
            // Calling the Interface method
            ProgressDialogUtils.showProgressDialog("Please Wait");
            call = apiService.UpsertPutAwayItem(message);
            // } else {
            // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            // return;
            // }kn 0.

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

                        if (response.body() != null) {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {

                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;

                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanSku.setImageResource(R.drawable.warning_img);

                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                }

                                getItemTOPutAway();

                                SKU = "";

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                            } else {
                                List<LinkedTreeMap<?, ?>> _lstPutaway = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstPutaway = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<PutawayDTO> lstDto = new ArrayList<PutawayDTO>();

                                ProgressDialogUtils.closeProgressDialog();

                                for (int i = 0; i < _lstPutaway.size(); i++) {

                                    PutawayDTO dto = new PutawayDTO(_lstPutaway.get(i).entrySet());

                                    materialMasterId = dto.getMaterialMasterId();

                                    /*cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanSku.setImageResource(R.drawable.check);*/

                                    suggestedQty = dto.getSuggestedQty();
                                    suggestedReceivedQty = dto.getSuggestedReceivedQty();

                                    etQty.clearFocus();

                                    totalQty = String.valueOf(Integer.parseInt(suggestedQty.split("[.]")[0]) - Integer.parseInt(suggestedReceivedQty.split("[.]")[0]));
                                    etQty.setText(totalQty);
                                    lblPutawayQty.setText(dto.getSuggestedReceivedQty() + "/" + dto.getSuggestedQty());
                                    suggestedPutawayId = dto.getSuggestedPutawayID();

                                    // if the suggested location is not same as previous suggested location
                                    if (!lblSuggestedLoc.getText().toString().equals(dto.getLocation())) {

                                        soundUtils.alertSuccess(getActivity(), getContext());

                                        rlLocationScan.setVisibility(View.GONE);
                                        rlStRefSelect.setVisibility(View.GONE);
                                        rlPutaway.setVisibility(View.VISIBLE);
                                        rlSkip.setVisibility(View.GONE);

                                        SKU = "";

                                        lblContainer.setText("");

                                        if (!dto.getCartonCode().equals("0") && !dto.getCartonCode().equals("") && !dto.getCartonCode().equals("null")) {

                                            lblContainer.setText(dto.getCartonCode());
                                        }

                                        lblSuggestedLoc.setText(dto.getLocation());

                                        lblScannedLocation.setText("");
                                        etScannedLocation.setText("");
                                        lblSKU.setText(dto.getMCode());
                                        etBatch.setText(dto.getBatchNo());
                                        etSerial.setText(dto.getSerialNo());
                                        etMfgDate.setText(dto.getMfgDate());
                                        etExpDate.setText(dto.getExpDate());
                                        etMRP.setText(dto.getMRP());

                                        isLocationScanned=false;
                                        isContainerScanned=false;


                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

                                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                        ivScanSku.setImageResource(R.drawable.fullscreen_img);

                                        returningObj = dto;
                                        return;

                                        // if  suggested location is same as previous suggested location
                                    }
                                    else if (lblSuggestedLoc.getText().toString().equals(dto.getLocation())) {

                                        soundUtils.alertSuccess(getActivity(), getContext());
                                        rlSkip.setVisibility(View.GONE);
                                        rlStRefSelect.setVisibility(View.GONE);
                                        rlPutaway.setVisibility(View.VISIBLE);
                                        rlLocationScan.setVisibility(View.GONE);


                                        SKU = "";

                                        lblContainer.setText("");

                                        if (!dto.getCartonCode().equals("0") && !dto.getCartonCode().equals("")) {

                                            lblContainer.setText(dto.getCartonCode());
                                        }

                                        lblSKU.setText(dto.getMCode());
                                        etBatch.setText(dto.getBatchNo());
                                        etSerial.setText(dto.getSerialNo());
                                        etMfgDate.setText(dto.getMfgDate());
                                        etExpDate.setText(dto.getExpDate());
                                        etMRP.setText(dto.getMRP());

                                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                        ivScanSku.setImageResource(R.drawable.fullscreen_img);

                                        returningObj = dto;

                                        if (dto.getSuggestedReceivedQty().equals("0") && dto.getSuggestedQty().equals("0")) {

                                            // if suggested received qty and suggested qty both equals to 0 ,, then putaway is completed
                                            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                            ivScanPallet.setImageResource(R.drawable.fullscreen_img);

                                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                            ivScanSku.setImageResource(R.drawable.fullscreen_img);

                                            cvScanDock.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                            ivScanDock.setImageResource(R.drawable.fullscreen_img);

                                            cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                            ivScanLocation.setImageResource(R.drawable.fullscreen_img);

                                            etScannedLocation.setText("");
                                            lblContainer.setText("");
                                            SKU = "";

                                            btnPutaway.setEnabled(false);

                                            etQty.setText("");
                                            etBatch.setText("");
                                            etSerial.setText("");
                                            etMfgDate.setText("");
                                            etExpDate.setText("");
                                            etMRP.setText("");
                                            etProjectRef.setText("");



                                            common.showUserDefinedAlertType(errorMessages.EMC_0074 + "" + refNo, getActivity(), getContext(), "Success");
                                            return;

                                        }

                                        return;

                                    }
                                }
                            }
                        } else {

                            ProgressDialogUtils.closeProgressDialog();
                            common.showUserDefinedAlertType(errorMessages.EMC_0021, getActivity(), getContext(), "Error");
                            return;

                        }
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
                }

                @Override
                public void onFailure(Call<String> call, Throwable throwable) {

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
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
        }

    }

    public void getSkipReasonList() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setSkipType("1");
            inboundDTO.setAccountID(accountId);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetSkipReasonList(message);
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

                                List<LinkedTreeMap<?, ?>> _lstPutaway = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstPutaway = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<InboundDTO> lstDto = new ArrayList<InboundDTO>();
                                List<String> _lstSkipReason = new ArrayList<>();


                                for (int i = 0; i < _lstPutaway.size(); i++) {
                                    InboundDTO dto = new InboundDTO(_lstPutaway.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                for (int i = 0; i < lstDto.size(); i++) {

                                    _lstSkipReason.add(lstDto.get(i).getSkipReason());

                                }


                                ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, _lstSkipReason);
                                spinnerSelectReason.setAdapter(arrayAdapter);
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

    public void skipItem() {

        WMSCoreMessage message = new WMSCoreMessage();
        message = common.SetAuthentication(EndpointConstants.PutAwayDTO, getContext());
        PutawayDTO putawayDTO = new PutawayDTO();

        putawayDTO = returningObj;

        putawayDTO.setMCode(lblSKU.getText().toString());
        putawayDTO.setMfgDate(etMfgDate.getText().toString());
        putawayDTO.setExpDate(etExpDate.getText().toString());
        putawayDTO.setBatchNo(etBatch.getText().toString());
        putawayDTO.setSerialNo(etSerial.getText().toString());
        putawayDTO.setProjectRefNo(etProjectRef.getText().toString());
        putawayDTO.setMRP(etMRP.getText().toString());
        putawayDTO.setUserID(userId);
        putawayDTO.setCartonCode(lblContainer.getText().toString());
        putawayDTO.setLocation(lblScannedLocation.getText().toString());
        putawayDTO.setInboundId(inboundId);
        putawayDTO.setSkipQty(etQty.getText().toString());
        putawayDTO.setSuggestedPutawayID(suggestedPutawayId);
        putawayDTO.setSuggestedReceivedQty(suggestedReceivedQty);
        putawayDTO.setSkipReason(skipReason);
        putawayDTO.setTransferRequestDetailsId(lblStoreRefNo.getText().toString());
        putawayDTO.setInboundId(inboundId);

        message.setEntityObject(putawayDTO);


        Call<String> call = null;
        ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

        try {
            //Checking for Internet Connectivity
            // if (NetworkUtils.isInternetAvailable()) {
            // Calling the Interface method
            ProgressDialogUtils.showProgressDialog("Please Wait");
            call = apiService.SkipItem(message);
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
                            List<LinkedTreeMap<?, ?>> _lstPutaway = new ArrayList<LinkedTreeMap<?, ?>>();
                            _lstPutaway = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                            List<PutawayDTO> lstDto = new ArrayList<PutawayDTO>();

                            ProgressDialogUtils.closeProgressDialog();

                            for (int i = 0; i < _lstPutaway.size(); i++) {
                                PutawayDTO dto = new PutawayDTO(_lstPutaway.get(i).entrySet());

                                if (!dto.getMCode().equals("")) {


                                    materialMasterId = dto.getMaterialMasterId();
                                    lblSKU.setText(dto.getMCode());

                                    if (!dto.getCartonCode().equals("0") && !dto.getCartonCode().equals("")) {

                                        lblContainer.setText(dto.getCartonCode());
                                    } else {
                                        lblContainer.setText("");
                                    }

                                    SKU = "";

                                    suggestedQty = dto.getSuggestedQty();
                                    suggestedReceivedQty = dto.getSuggestedReceivedQty();
                                    suggestedPutawayId = dto.getSuggestedPutawayID();

                                    totalQty = String.valueOf(Integer.parseInt(suggestedQty.split("[.]")[0]) - Integer.parseInt(suggestedReceivedQty.split("[.]")[0]));

                                    lblPutawayQty.setText(dto.getSuggestedReceivedQty() + "/" + dto.getSuggestedQty());

                                    etQty.setText(totalQty);


                                    // If suggested location is same as previous suggested location
                                    if (dto.getLocation().equals(lblScannedLocation.getText().toString())) {
                                        rlSkip.setVisibility(View.GONE);
                                        rlStRefSelect.setVisibility(View.GONE);
                                        rlPutaway.setVisibility(View.VISIBLE);
                                        rlLocationScan.setVisibility(View.GONE);

                                        SKU = "";

                                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                        ivScanSku.setImageResource(R.drawable.fullscreen_img);

                                        returningObj = dto;

                                    } else {
                                        rlSkip.setVisibility(View.GONE);
                                        rlStRefSelect.setVisibility(View.GONE);
                                        rlPutaway.setVisibility(View.GONE);
                                        rlLocationScan.setVisibility(View.VISIBLE);

                                        // For new suggestion
                                        lblSuggestedLoc.setText(dto.getLocation());

                                        lblScannedLocation.setText("");
                                        lblContainer.setText("");
                                        SKU = "";

                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

                                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                        ivScanSku.setImageResource(R.drawable.fullscreen_img);
                                        isContainerScanned = false;

                                        returningObj = dto;
                                    }

                                } else {

                                }
                            }
                        }

                    } catch (Exception ex) {
                        try {
                            exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_02", getActivity());
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
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);

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
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002", getContext());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            logException();


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
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_putaway));
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
}
