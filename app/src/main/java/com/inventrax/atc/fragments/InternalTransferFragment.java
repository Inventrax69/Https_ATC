package com.inventrax.atc.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import com.inventrax.atc.pojos.HouseKeepingDTO;
import com.inventrax.atc.pojos.InventoryDTO;
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

/**
 * Created by Prasann on 05/08/2018.
 */

public class InternalTransferFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_0011";
    private View rootView;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    private ScanValidator scanValidator;
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    Common common;
    private WMSCoreMessage core;
    private RelativeLayout rlInternalTransfer, rlSelect;
    private CardView cvScanFromLoc, cvScanFromCont, cvScanSku, cvScanToLoc;
    private ImageView ivScanFromLoc, ivScanFromCont, ivScanSku, ivScanToLoc;
    EditText etLocationFrom, etLocationTo, etPalletFrom, etSku, etQty;
    private SearchableSpinner spinnerSelectSloc, spinnerSelectTenant, spinnerSelectWarehouse;
    private Button btnBinComplete, btn_clear, btnGo;

    private String Materialcode = null, Userid = null, scanType = "", accountId = "", storageloc = "";
    private int IsToLoc = 0;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    Boolean isPalletScaned = false, isLocationScaned = false, isSKUScanned = false, IsProceedForBinTransfer = false;
    private TextView lblMfgDate, lblExpDate, lblProjectRefNo, lblserialNo, lblBatchNo, lblMRP;
    private SoundUtils soundUtils;
    private String selectedTenant = "", selectedWH = "", tenantId = "", whId = "";
    List<HouseKeepingDTO> lstTenants = null;
    List<HouseKeepingDTO> lstWarehouse = null;


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


    public InternalTransferFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_internaltransfer, container, false);
        loadFormControls();
        return rootView;
    }

    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        Userid = sp.getString("RefUserId", "");
        scanType = sp.getString("scanType", "");
        accountId = sp.getString("AccountId", "");

        rlInternalTransfer = (RelativeLayout) rootView.findViewById(R.id.rlInternalTransfer);
        rlSelect = (RelativeLayout) rootView.findViewById(R.id.rlSelect);

        etLocationFrom = (EditText) rootView.findViewById(R.id.etLocationFrom);
        etLocationTo = (EditText) rootView.findViewById(R.id.etLocationTo);
        etPalletFrom = (EditText) rootView.findViewById(R.id.etPalletFrom);
        etSku = (EditText) rootView.findViewById(R.id.etSku);
        etQty = (EditText) rootView.findViewById(R.id.etQty);

        cvScanFromLoc = (CardView) rootView.findViewById(R.id.cvScanFromLoc);
        cvScanFromCont = (CardView) rootView.findViewById(R.id.cvScanFromCont);
        cvScanSku = (CardView) rootView.findViewById(R.id.cvScanSku);
        cvScanToLoc = (CardView) rootView.findViewById(R.id.cvScanToLoc);

        ivScanFromLoc = (ImageView) rootView.findViewById(R.id.ivScanFromLoc);
        ivScanFromCont = (ImageView) rootView.findViewById(R.id.ivScanFromCont);
        ivScanToLoc = (ImageView) rootView.findViewById(R.id.ivScanToLoc);
        ivScanSku = (ImageView) rootView.findViewById(R.id.ivScanSku);


        lblMfgDate = (TextView) rootView.findViewById(R.id.lblMfgDate);
        lblExpDate = (TextView) rootView.findViewById(R.id.lblExpDate);
        lblProjectRefNo = (TextView) rootView.findViewById(R.id.lblProjectRefNo);
        lblserialNo = (TextView) rootView.findViewById(R.id.lblserialNo);
        lblBatchNo = (TextView) rootView.findViewById(R.id.lblBatchNo);
        lblMRP = (TextView) rootView.findViewById(R.id.lblMRP);

        lstTenants = new ArrayList<HouseKeepingDTO>();
        lstWarehouse = new ArrayList<HouseKeepingDTO>();

        spinnerSelectSloc = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectSloc);
        spinnerSelectSloc.setEnabled(false);

        spinnerSelectTenant = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectTenant);
        spinnerSelectWarehouse = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectWarehouse);

        spinnerSelectSloc.setOnItemSelectedListener(this);
        spinnerSelectTenant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTenant = spinnerSelectTenant.getSelectedItem().toString();
                getTenantId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerSelectWarehouse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedWH = spinnerSelectWarehouse.getSelectedItem().toString();
                getWarehouseId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnBinComplete = (Button) rootView.findViewById(R.id.btnBinComplete);
        btn_clear = (Button) rootView.findViewById(R.id.btn_clear);
        btnGo = (Button) rootView.findViewById(R.id.btnGo);

        btnBinComplete.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        btnGo.setOnClickListener(this);
        cvScanFromCont.setOnClickListener(this);

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

        common = new Common();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();

        ProgressDialogUtils.closeProgressDialog();
        common.setIsPopupActive(false);

        //For Honeywell Broadcast receiver intiation
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


        // To get tenants
        //getTenants();

        getWarehouse();




    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_clear:
                Clearfields();                       // clear the scanned fields
                break;
            case R.id.cvScanFromCont:
                isPalletScaned = true;
                cvScanFromCont.setCardBackgroundColor(getResources().getColor(R.color.white));
                ivScanFromCont.setImageResource(R.drawable.check);
                break;

            case R.id.btnGo:

                if (!whId.equals("") && !tenantId.equals("")) {
                    rlSelect.setVisibility(View.GONE);
                    rlInternalTransfer.setVisibility(View.VISIBLE);
                    // method to get the storage locations
                    GetBinToBinStorageLocations();

                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0011, getActivity(), getContext(), "Error");
                }
                break;

            case R.id.btnBinComplete:

                if(!etLocationFrom.getText().toString().isEmpty()){

                    if(!etPalletFrom.getText().toString().isEmpty()){

                        if (!etSku.getText().toString().isEmpty()){

                            if (!etLocationTo.getText().toString().isEmpty()) {

                                    if (!storageloc.isEmpty() && !storageloc.equalsIgnoreCase("SLOC")) {

                                        if(!etQty.getText().toString().isEmpty()){

                                        if(!etQty.getText().toString().equalsIgnoreCase("0")){
                                            UpsertBinToBinTransfer();
                                        }else {
                                            common.showUserDefinedAlertType(errorMessages.EMC_0068, getActivity(), getContext(), "Error");
                                        }

                                        }else {
                                            common.showUserDefinedAlertType(errorMessages.EMC_0067, getActivity(), getContext(), "Error");
                                        }

                                    }else {
                                        common.showUserDefinedAlertType(errorMessages.EMC_0052, getActivity(), getContext(), "Error");
                                    }


                            } else {
                                common.showUserDefinedAlertType(errorMessages.EMC_0020, getActivity(), getContext(), "Error");
                            }

                        }else {
                            common.showUserDefinedAlertType(errorMessages.EMC_0028, getActivity(), getContext(), "Error");
                        }

                    }else {
                        common.showUserDefinedAlertType(errorMessages.EMC_088, getActivity(), getContext(), "Error");
                    }

                }else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0026, getActivity(), getContext(), "Error");
                }


                break;

        }
    }

    private void Clearfields() {

        cvScanFromCont.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanFromCont.setImageResource(R.drawable.fullscreen_img);

        cvScanFromLoc.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanFromLoc.setImageResource(R.drawable.fullscreen_img);

        cvScanToLoc.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanToLoc.setImageResource(R.drawable.fullscreen_img);

        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSku.setImageResource(R.drawable.fullscreen_img);

        etLocationTo.setText("");
        etLocationFrom.setText("");
        etPalletFrom.setText("");

        etSku.setText("");
        etQty.setText("");

        lblBatchNo.setText("");
        lblserialNo.setText("");
        lblExpDate.setText("");
        lblMfgDate.setText("");
        lblProjectRefNo.setText("");
        lblMRP.setText("");

        isLocationScaned = false;
        isPalletScaned = false;
        isSKUScanned = false;

        spinnerSelectSloc.setEnabled(false);
        spinnerSelectSloc.setAdapter(arrayAdapter1);

        //GetBinToBinStorageLocations();
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

        if (common.isPopupActive()) {

        } else if (scannedData != null && !scannedData.equalsIgnoreCase("")) {

            if (!ProgressDialogUtils.isProgressActive()) {

                if (!isLocationScaned) {
                    ValidateLocation(scannedData);
                } else {
                    if (!isPalletScaned) {
                        ValidatePallet(scannedData);
                    } else {
                        if (etSku.getText().toString().isEmpty()) {
                            ValiDateMaterial(scannedData);
                        } else {
                            if (etLocationTo.getText().toString().isEmpty()) {
                                ValidateLocation(scannedData);
                            }
                        }
                    }

                }
            } else {
                if (!Common.isPopupActive()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_080, getActivity(), getContext(), "Error");

                }
                soundUtils.alertWarning(getActivity(), getContext());

            }


            //Before We process first we need to scan From Location
            // check for SKU Scanned
/*            if (ScanValidator.isItemScanned(scannedData)) {
                if (!(etLocationFrom.getText().toString().isEmpty())) {
                    etSku.setText(scannedData.split("[|]")[0]);
                    if (scannedData.split("[|]").length != 5) {
                        Materialcode = scannedData.split("[|]")[0];
                        lblBatchNo.setText(scannedData.split("[|]")[1]);
                        lblserialNo.setText(scannedData.split("[|]")[2]);
                        lblMfgDate.setText(scannedData.split("[|]")[3]);
                        lblExpDate.setText(scannedData.split("[|]")[4]);
                        lblProjectRefNo.setText(scannedData.split("[|]")[5]);
                        lblMRP.setText(scannedData.split("[|]")[7]);
                        //   etKidID.setText(scannedData.split("[|]")[6]);
                        // lineNo = scannedData.split("[|]")[7];
                    } else {
                        Materialcode = scannedData.split("[|]")[0];
                        lblBatchNo.setText(scannedData.split("[|]")[1]);
                        lblserialNo.setText(scannedData.split("[|]")[2]);
                        // etKidID.setText(scannedData.split("[|]")[3]);
                        // lineNo = scannedData.split("[|]")[4];
                    }

                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                    ivScanSku.setImageResource(R.drawable.fullscreen_img);


                    // To get the qty of sku from the scanned location
                    GetAvailbleQtyList();

                    if (scanType.equalsIgnoreCase("Auto")) {
                        etQty.setEnabled(false);
                    } else {
                        etQty.setEnabled(true);
                    }
                } else {
                    common.setIsPopupActive(true);
                    common.showUserDefinedAlertType(errorMessages.EMC_0026, getActivity(), getContext(), "Warning");
                    return;
                }
                // check for Pallet Scanned
            }
            else*/ /*if (ScanValidator.isContainerScanned(scannedData)) {

                if (!(etLocationFrom.getText().toString().isEmpty())) {
                    //isPalletScaned is boolean key for check is to pallet scan  or  From Pallet Scan
                    if (!isPalletScaned) {

                        if (!isSKUScanned) {

                            etPalletFrom.setText(scannedData);
                            ValidatePalletCode(etPalletFrom.getText().toString(), "from");
                            return;
                        }

                    }

                    if (!etLocationTo.getText().toString().isEmpty()) {

                        if (!etPalletFrom.getText().toString().equalsIgnoreCase(scannedData)) {
                            etPalletTo.setText(scannedData);
                            ValidatePalletCode(etPalletTo.getText().toString(), "to");
                        } else {

                            etPalletTo.setText("");
                            common.showUserDefinedAlertType(errorMessages.EMC_0034, getActivity(), getContext(), "Warning");
                        }

                    } else {
                        common.setIsPopupActive(true);
                        common.showUserDefinedAlertType(errorMessages.EMC_0020, getActivity(), getContext(), "Warning");
                    }

                } else {
                    common.setIsPopupActive(true);
                    common.showUserDefinedAlertType(errorMessages.EMC_0026, getActivity(), getContext(), "Warning");
                }
                // check for Location Scanned
            }
*//*            else if (ScanValidator.isLocationScanned(scannedData)) {
                // From Location
                if (!isLocationScaned) {
                    etLocationFrom.setText(scannedData);
                    validateLocationCode(etLocationFrom.getText().toString(), "from");

                } else {
                    //To Location
                    if (!etLocationFrom.getText().toString().isEmpty()) {

                        if (!etPalletFrom.getText().toString().isEmpty() || !etSku.getText().toString().isEmpty()) {
                            if (!etLocationFrom.getText().toString().equalsIgnoreCase(scannedData)) {
                                etLocationTo.setText(scannedData);
                                validateLocationCode(etLocationTo.getText().toString(), "to");
                            } else {
                                etLocationTo.setText("");
                                etPalletTo.setText("");
                                isPalletScaned = false;
                                common.showUserDefinedAlertType(errorMessages.EMC_0032, getActivity(), getContext(), "Warning");
                            }
                        } else {
                            common.setIsPopupActive(true);
                            common.showUserDefinedAlertType(errorMessages.EMC_0030, getActivity(), getContext(), "Warning");
                        }
                    } else {
                        common.setIsPopupActive(true);
                        common.showUserDefinedAlertType(errorMessages.EMC_0026, getActivity(), getContext(), "Warning");
                    }
                }
            }*//*
            else{*/
/*                if((etPalletFrom.getText().toString().isEmpty() && !etLocationFrom.getText().toString().isEmpty()) || (etPalletTo.getText().toString().isEmpty() && !etLocationTo.getText().toString().isEmpty() ))
                    ValidatePallet(scannedData);
                else{
                    // From Location
                    if (!isLocationScaned) {

                        if (!(etLocationFrom.getText().toString().isEmpty())) {
                            ValidateLocation(scannedData);
                        } else {
                            common.setIsPopupActive(true);
                            common.showUserDefinedAlertType(errorMessages.EMC_0026, getActivity(), getContext(), "Warning");
                            return;
                        }

                    }else if(!etPalletFrom.getText().toString().isEmpty() && etSku.getText().toString().isEmpty()){
                        ValiDateMaterial(scannedData);
                    } else {
                        //To Location
                        if (!etLocationFrom.getText().toString().isEmpty()) {

                            if (!etPalletFrom.getText().toString().isEmpty() || !etSku.getText().toString().isEmpty()) {
                                if (!etLocationFrom.getText().toString().equalsIgnoreCase(scannedData)) {
                                    ValidateLocation(scannedData);
                                } else {
                                    etLocationTo.setText("");
                                    etPalletTo.setText("");
                                    isPalletScaned = false;
                                    common.showUserDefinedAlertType(errorMessages.EMC_0032, getActivity(), getContext(), "Warning");
                                }
                            } else {
                                common.setIsPopupActive(true);
                                common.showUserDefinedAlertType(errorMessages.EMC_0030, getActivity(), getContext(), "Warning");
                            }
                        } else {
                            common.setIsPopupActive(true);
                            common.showUserDefinedAlertType(errorMessages.EMC_0026, getActivity(), getContext(), "Warning");
                        }
                    }
                }*/

        } else {

            common.showUserDefinedAlertType(errorMessages.EMC_0030, getActivity(), getContext(), "Error");
        }
    }


    public void ValiDateMaterial(final String scannedData) {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.ScanDTO, getContext());
            ScanDTO scanDTO = new ScanDTO();
            scanDTO.setUserID(Userid);
            scanDTO.setAccountID(accountId);
            scanDTO.setTenantID(String.valueOf(tenantId));
            scanDTO.setWarehouseID(String.valueOf(whId));
            scanDTO.setScanInput(scannedData);
            //scanDTO.setInboundID(inboundId);
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
                                    lblBatchNo.setText(scanDTO1.getBatch());
                                    lblserialNo.setText(scanDTO1.getSerialNumber());
                                    lblMfgDate.setText(scanDTO1.getMfgDate());
                                    lblExpDate.setText(scanDTO1.getExpDate());
                                    lblProjectRefNo.setText(scanDTO1.getPrjRef());
                                    // etKidID.setText(scanDTO1.getKitID());
                                    lblMRP.setText(scanDTO1.getMrp());
                                    //lineNo = scanDTO1.getLineNumber();
                                    etSku.setText(Materialcode);

                                    //   etKidID.setText(scannedData.split("[|]")[6]);
                                    // lineNo = scannedData.split("[|]")[7];





/*                                    } else {
                                        Materialcode = scannedData.split("[|]")[0];
                                        etBatch.setText(scannedData.split("[|]")[1]);
                                        etSerial.setText(scannedData.split("[|]")[2]);
                                        etKidID.setText(scannedData.split("[|]")[3]);
                                        lineNo = scannedData.split("[|]")[4];
                                    }*/


                                    // To get the qty of sku from the scanned location
                                    GetAvailbleQtyList();

                                    if (scanType.equalsIgnoreCase("Auto")) {
                                        etQty.setEnabled(false);
                                    } else {
                                        etQty.setEnabled(true);
                                    }
                                } else {

                                    etSku.setText("");
                                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanSku.setImageResource(R.drawable.warning_img);
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

    public void getTenants() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.HouseKeepingDTO, getContext());
            HouseKeepingDTO houseKeepingDTO = new HouseKeepingDTO();
            houseKeepingDTO.setAccountID(accountId);
            houseKeepingDTO.setUserId(Userid);
            houseKeepingDTO.setTenantID(tenantId);
            houseKeepingDTO.setWarehouseId(whId);
            message.setEntityObject(houseKeepingDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetTenants(message);
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

                                List<LinkedTreeMap<?, ?>> _lstActiveStock = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstActiveStock = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<HouseKeepingDTO> lstDto = new ArrayList<HouseKeepingDTO>();
                                List<String> _lstStock = new ArrayList<>();

                                ProgressDialogUtils.closeProgressDialog();

                                for (int i = 0; i < _lstActiveStock.size(); i++) {
                                    HouseKeepingDTO dto = new HouseKeepingDTO(_lstActiveStock.get(i).entrySet());
                                    lstDto.add(dto);
                                    lstTenants.add(dto);
                                }

                                for (int i = 0; i < lstDto.size(); i++) {

                                    _lstStock.add(lstDto.get(i).getTenantName());

                                }


                                ArrayAdapter liveStockAdapter = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, _lstStock);
                                spinnerSelectTenant.setAdapter(liveStockAdapter);


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

    public void getWarehouse() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.HouseKeepingDTO, getContext());
            HouseKeepingDTO houseKeepingDTO = new HouseKeepingDTO();
            houseKeepingDTO.setAccountID(accountId);
            houseKeepingDTO.setUserId(Userid);
            houseKeepingDTO.setTenantID(tenantId);
            message.setEntityObject(houseKeepingDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetWarehouse(message);
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

                                List<LinkedTreeMap<?, ?>> _lstActiveStock = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstActiveStock = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<HouseKeepingDTO> lstDto = new ArrayList<HouseKeepingDTO>();
                                List<String> _lstStock = new ArrayList<>();

                                ProgressDialogUtils.closeProgressDialog();

                                for (int i = 0; i < _lstActiveStock.size(); i++) {
                                    HouseKeepingDTO dto = new HouseKeepingDTO(_lstActiveStock.get(i).entrySet());
                                    lstDto.add(dto);
                                    lstWarehouse.add(dto);
                                }

                                for (int i = 0; i < lstDto.size(); i++) {

                                    _lstStock.add(lstDto.get(i).getWarehouse());

                                }


                                ArrayAdapter liveStockAdapter = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, _lstStock);
                                spinnerSelectWarehouse.setAdapter(liveStockAdapter);


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

    public void getTenantId() {

        for (HouseKeepingDTO oHouseKeeping : lstTenants) {
            // iterating housekeeping list to get tenant id of selected tenant
            if (oHouseKeeping.getTenantName().equals(selectedTenant)) {

                tenantId = oHouseKeeping.getTenantID();   // Te

                // get warehouses of selected tenant
               // getWarehouse();
            }
        }
    }

    public void getWarehouseId() {

        for (HouseKeepingDTO oHouseKeeping : lstWarehouse) {
            if (oHouseKeeping.getWarehouse().equals(selectedWH)) {

                whId = oHouseKeeping.getWarehouseId();    // Warehouse Id of selected warehouse
                getTenants();
            }
        }
    }

    ArrayAdapter arrayAdapter1;

    public void GetBinToBinStorageLocations() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO inboundDTO = new InventoryDTO();
            inboundDTO.setUserId(Userid);
            inboundDTO.setAccountID(accountId);
            inboundDTO.setWarehouseId(whId);
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetBinToBinStorageLocations(message);
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
                                DialogUtils.showAlertDialog(getActivity(), owmsExceptionMessage.getWMSMessage());
                            } else {
                                ProgressDialogUtils.closeProgressDialog();
                                List<LinkedTreeMap<?, ?>> _lstInbound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstInbound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<InventoryDTO> lstDto = new ArrayList<>();
                                List<String> lstInboundNo = new ArrayList<>();
                                for (int i = 0; i < _lstInbound.size(); i++) {
                                    InventoryDTO oInbound = new InventoryDTO(_lstInbound.get(i).entrySet());
                                    lstDto.add(oInbound);
                                }

                                lstInboundNo.add("SLOC");

                                for (int i = 0; i < lstDto.size(); i++) {
                                    lstInboundNo.add(lstDto.get(i).getLocationCode());
                                }


                                arrayAdapter1 = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstInboundNo);
                                spinnerSelectSloc.setAdapter(arrayAdapter1);
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
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

  /*  public void validateLocationCode(String location, final String from) {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getActivity());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setAccountID(accountId);
            inventoryDTO.setTenantID(tenantId);
            inventoryDTO.setWarehouse(selectedWH);
            inventoryDTO.setWarehouseId(whId);
            inventoryDTO.setLocationCode(location);
            message.setEntityObject(inventoryDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.CheckLocationForLiveStock(message);
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

                            // if any Exception throws
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                ProgressDialogUtils.closeProgressDialog();
                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    if (from.equalsIgnoreCase("from")) {
                                        etLocationFrom.setText("");
                                        isLocationScaned = false;
                                        cvScanFromLoc.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanFromLoc.setImageResource(R.drawable.warning_img);
                                    } else {
                                        etLocationTo.setText("");
                                        cvScanToLoc.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanToLoc.setImageResource(R.drawable.warning_img);
                                    }
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    return;
                                }
                            } else {
                                List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                InventoryDTO dto = null;
                                ProgressDialogUtils.closeProgressDialog();

                                for (int i = 0; i < _lResult.size(); i++) {

                                    dto = new InventoryDTO(_lResult.get(i).entrySet());
                                    if (dto.getResult().equals("1")) {

                                        if (from.equalsIgnoreCase("from")) {
                                            cvScanFromLoc.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanFromLoc.setImageResource(R.drawable.check);
                                            isLocationScaned = true;
                                        } else {
                                            cvScanToLoc.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanToLoc.setImageResource(R.drawable.check);
                                        }
                                    } else {
                                        if (from.equalsIgnoreCase("from")) {
                                            cvScanFromLoc.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanFromLoc.setImageResource(R.drawable.invalid_cross);
                                            isLocationScaned = true;
                                        } else {
                                            cvScanToLoc.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanToLoc.setImageResource(R.drawable.invalid_cross);
                                        }
                                        common.showUserDefinedAlertType(errorMessages.EMC_0016, getActivity(), getContext(), "Error");
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
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
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
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }
*/
    public void GetAvailbleQtyList() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO inventoryDTO = new InventoryDTO();

            inventoryDTO.setUserId(Userid);
            inventoryDTO.setAccountId(accountId);
            inventoryDTO.setMaterialCode(Materialcode);
            if (storageloc.equalsIgnoreCase("SLOC")) {
                inventoryDTO.setSLOC("");
            } else {
                inventoryDTO.setSLOC(storageloc);
            }
            inventoryDTO.setLocationCode(etLocationFrom.getText().toString());
            inventoryDTO.setContainerCode(etPalletFrom.getText().toString());
            inventoryDTO.setMfgDate(lblMfgDate.getText().toString());
            inventoryDTO.setExpDate(lblExpDate.getText().toString());
            inventoryDTO.setSerialNo(lblserialNo.getText().toString());
            inventoryDTO.setBatchNo(lblBatchNo.getText().toString());
            inventoryDTO.setProjectNo(lblProjectRefNo.getText().toString());
            inventoryDTO.setMRP(lblMRP.getText().toString());
            inventoryDTO.setTenantID(tenantId);
            inventoryDTO.setWarehouseId(whId);

            message.setEntityObject(inventoryDTO);
            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetAvailbleQtyList(message);
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

                            if (response.body() != null) {

                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {
                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        ProgressDialogUtils.closeProgressDialog();

                                        etQty.setEnabled(false);
                                        etQty.setText("");

                                        //isSKUScanned = false;
                                        //etSku.setText("");
                                        spinnerSelectSloc.setEnabled(true);
                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.invalid_cross);

                                        lblMRP.setText("");
                                        lblserialNo.setText("");
                                        lblBatchNo.setText("");
                                        lblMfgDate.setText("");
                                        lblExpDate.setText("");
                                        lblProjectRefNo.setText("");
                                        etSku.setText("");

                                        GetBinToBinStorageLocations();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        return;
                                    }
                                } else {
                                    List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                    InventoryDTO oOutboundDTO = null;
                                    for (int i = 0; i < _lstPickitem.size(); i++) {
                                        oOutboundDTO = new InventoryDTO(_lstPickitem.get(i).entrySet());
                                    }
                                    etQty.setText(oOutboundDTO.getQuantity());


                                    if (scanType.equalsIgnoreCase("Auto")) {
                                        etQty.setText("1");
                                        isSKUScanned = true;
                                        UpsertBinToBinTransfer();
                                    } else {
                                        etQty.setEnabled(true);
                                        isSKUScanned = true;
                                    }


                                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanSku.setImageResource(R.drawable.check);

                                    spinnerSelectSloc.setEnabled(true);

                                    ProgressDialogUtils.closeProgressDialog();


                                }
                            } else {
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

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
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
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0013);
        }
    }

    public void ValidatePalletCode(final String pallet, final String from) {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setUserId(Userid);
            inventoryDTO.setAccountID(accountId);
            inventoryDTO.setWarehouseId(whId);
            inventoryDTO.setTenantID(tenantId);

            if (from.equalsIgnoreCase("from")) {
                inventoryDTO.setLocationCode(etLocationFrom.getText().toString());
                inventoryDTO.setContainerCode(pallet);

            } else {
                inventoryDTO.setLocationCode(etLocationTo.getText().toString());
                inventoryDTO.setContainerCode(pallet);

            }

            message.setEntityObject(inventoryDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ChekContainerLocation(message);
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
                                    //etPallet.setText("");
                                    if (from.equalsIgnoreCase("from")) {
                                        isPalletScaned = false;
                                        etPalletFrom.setText("");
                                        cvScanFromCont.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanFromCont.setImageResource(R.drawable.warning_img);
                                    }
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    return;
                                }
                            } else {
                                List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();


                                List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                InventoryDTO dto = null;
                                ProgressDialogUtils.closeProgressDialog();

                                for (int i = 0; i < _lResult.size(); i++) {

                                    dto = new InventoryDTO(_lResult.get(i).entrySet());
                                    if (dto.getResult().equals("1")) {
                                        if (from.equalsIgnoreCase("from")) {

                                            etQty.setText(dto.getQuantity());

                                            isPalletScaned = true;
                                            cvScanFromCont.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanFromCont.setImageResource(R.drawable.check);

                                        }
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

    public void UpsertBinToBinTransfer() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setRSN(etSku.getText().toString());
            inventoryDTO.setLocationCode(etLocationFrom.getText().toString());
            inventoryDTO.setMfgDate(lblMfgDate.getText().toString());
            inventoryDTO.setExpDate(lblExpDate.getText().toString());
            inventoryDTO.setSerialNo(lblserialNo.getText().toString());
            inventoryDTO.setBatchNo(lblBatchNo.getText().toString());
            inventoryDTO.setProjectNo(lblProjectRefNo.getText().toString());
            inventoryDTO.setUserId(Userid);
            inventoryDTO.setAccountID(accountId);
            inventoryDTO.setContainerCode(etPalletFrom.getText().toString());
            inventoryDTO.setToContainerCode("");
            inventoryDTO.setMaterialCode(Materialcode);
            inventoryDTO.setToLocationCode(etLocationTo.getText().toString());
            inventoryDTO.setQuantity(etQty.getText().toString());
            if (storageloc.equalsIgnoreCase("SLOC")) {
                if (!isSKUScanned) {
                    inventoryDTO.setSLOC("");
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0052, getActivity(), getContext(), "Warning");
                    return;
                }
            } else {
                inventoryDTO.setSLOC(storageloc);
            }
            inventoryDTO.setMRP(lblMRP.getText().toString());
            inventoryDTO.setTenantID(tenantId);
            inventoryDTO.setWarehouseId(whId);
            message.setEntityObject(inventoryDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.UpsertBinToBinTransferItem(message);
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
                                        return;
                                    }
                                } else {

                                    List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    InventoryDTO dto = null;
                                    ProgressDialogUtils.closeProgressDialog();

                                    for (int i = 0; i < _lResult.size(); i++) {

                                        dto = new InventoryDTO(_lResult.get(i).entrySet());

                                        if (dto.getResult().equals("1")) {

                                            // Clearing data after completion of transfer

                                            // From location is not cleared here as per requirement

                                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                            ivScanSku.setImageResource(R.drawable.fullscreen_img);

                                            cvScanFromCont.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                            ivScanFromCont.setImageResource(R.drawable.fullscreen_img);

                                            cvScanToLoc.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                            ivScanToLoc.setImageResource(R.drawable.fullscreen_img);

                                            etLocationTo.setText("");
                                            etPalletFrom.setText("");
                                            etSku.setText("");
                                            etQty.setText("");

                                            lblBatchNo.setText("");
                                            lblserialNo.setText("");
                                            lblExpDate.setText("");
                                            lblMfgDate.setText("");
                                            lblProjectRefNo.setText("");
                                            lblMRP.setText("");
                                            etQty.setText("");

                                            isSKUScanned = false;
                                            isPalletScaned = false;

                                            GetBinToBinStorageLocations();

                                            soundUtils.alertSuccess(getActivity(), getContext());

                                            return;

                                        } else {
                                            common.showUserDefinedAlertType(dto.getResult(), getActivity(), getContext(), "Error");

                                            ProgressDialogUtils.closeProgressDialog();
                                        }

                                    }

                                }
                            }
                            ProgressDialogUtils.closeProgressDialog();
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
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
        }
    }


    // honeywell Barcode reader
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_internal_transfer));
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        storageloc = spinnerSelectSloc.getSelectedItem().toString();

        if (!storageloc.equalsIgnoreCase("SLOC")) {
            GetAvailbleQtyList();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    public void ValidatePallet(final String scannedData) {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.ScanDTO, getContext());
            ScanDTO scanDTO = new ScanDTO();
            scanDTO.setUserID(Userid);
            scanDTO.setAccountID(accountId);
            // scanDTO.setTenantID(String.valueOf(tenantID));
            scanDTO.setWarehouseID(whId);
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

                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                        } else {
                            LinkedTreeMap<?, ?> _lResult = new LinkedTreeMap<>();
                            _lResult = (LinkedTreeMap<?, ?>) core.getEntityObject();

                            ScanDTO scanDTO1 = new ScanDTO(_lResult.entrySet());
                            ProgressDialogUtils.closeProgressDialog();
                            if (scanDTO1 != null) {
                                if (scanDTO1.getScanResult()) {
                                    if (!(etLocationFrom.getText().toString().isEmpty())) {
                                        //isPalletScaned is boolean key for check is to pallet scan  or  From Pallet Scan
                                        if (!isPalletScaned) {

                                            if (!isSKUScanned) {
                                                isPalletScaned = true;
                                                etPalletFrom.setText(scannedData);
                                                ValidatePalletCode(etPalletFrom.getText().toString(), "from");
                                                return;
                                            }

                                        }

                                    } else {
                                        common.setIsPopupActive(true);
                                        common.showUserDefinedAlertType(errorMessages.EMC_0026, getActivity(), getContext(), "Warning");
                                    }
                                } else {
/*                                    isContanierScanned=false;
                                    etPallet.setText("");
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.warning_img);*/
                                    common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Warning");
                                }
                            } else {
                                //isContanierScanned=false;
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
            scanDTO.setUserID(Userid);
            scanDTO.setAccountID(accountId);
            // scanDTO.setTenantID(String.valueOf(tenantID));
            scanDTO.setWarehouseID(String.valueOf(whId));
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

                            if (!isLocationScaned) {
                                etLocationFrom.setText("");
                                cvScanFromLoc.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanFromLoc.setImageResource(R.drawable.invalid_cross);
                            } else {
                                etLocationTo.setText("");
                                cvScanToLoc.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanToLoc.setImageResource(R.drawable.invalid_cross);
                            }
                            ProgressDialogUtils.closeProgressDialog();
                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                        } else {
                            LinkedTreeMap<?, ?> _lResult = new LinkedTreeMap<>();
                            _lResult = (LinkedTreeMap<?, ?>) core.getEntityObject();

                            ScanDTO scanDTO1 = new ScanDTO(_lResult.entrySet());

                            if (scanDTO1 != null) {
                                if (scanDTO1.getScanResult()) {
                                    if (!isLocationScaned) {
                                        etLocationFrom.setText(scannedData);
                                        cvScanFromLoc.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanFromLoc.setImageResource(R.drawable.check);
                                        isLocationScaned = true;
                                        //validateLocationCode(etLocationFrom.getText().toString(), "from");
                                    } else {
                                        etLocationTo.setText(scannedData);
                                        cvScanToLoc.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanToLoc.setImageResource(R.drawable.check);
                                        //validateLocationCode(etLocationTo.getText().toString(), "to");
                                    }
                                } else {
                                    if (!isLocationScaned) {
                                        etLocationFrom.setText("");
                                        cvScanFromLoc.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanFromLoc.setImageResource(R.drawable.warning_img);
                                        isLocationScaned = false;
                                    } else {
                                        etLocationTo.setText("");
                                        cvScanToLoc.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanToLoc.setImageResource(R.drawable.warning_img);
                                    }
                                    common.showUserDefinedAlertType(errorMessages.EMC_0016, getActivity(), getContext(), "Warning");
/*                                    etLocationTo.setText("");
                                    cvScanToLoc.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanToLoc.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0010, getActivity(), getContext(), "Warning");*/
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
}