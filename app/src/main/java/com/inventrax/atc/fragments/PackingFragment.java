package com.inventrax.atc.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.inventrax.atc.adapters.SkuListAdapter;
import com.inventrax.atc.common.Common;
import com.inventrax.atc.common.constants.EndpointConstants;
import com.inventrax.atc.common.constants.ErrorMessages;
import com.inventrax.atc.interfaces.ApiInterface;
import com.inventrax.atc.pojos.OutbountDTO;
import com.inventrax.atc.pojos.SKUListDTO;
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
 * Created by ch.anil on 01/06/20120.
 */

public class PackingFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {


    private static final String classCode = "API_FRAG_PackingFragment";
    private View rootView;
    private TextView tvOrderInfromation, tvOrderInfromationSuccess, tvScanPallet, lblScannedData, lblHu;
    private SearchableSpinner spinnerSelectTenant, spinnerSelectWH;
    private CardView cvScanSONumber, cvScanPallet, cvScanSku;
    private ImageView ivScanSONumber, ivScanSku, ivScanPallet;
    private Button btnPackComplete, btnBackPacking, btnPack, btnClear;
    private RecyclerView recyclerViewLiveStock;
    private LinearLayoutManager linearLayoutManager;
    private RadioButton radioLocation, radioPallet, radioSKU;
    RadioGroup radioGroup;

    String scanner = null;
    private IntentFilter filter;
    private WMSCoreMessage core;
    String getScanner = null;
    private Gson gson;
    private ScanValidator scanValidator;
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private Common common;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private Boolean isLocationScanned = false, isSKUScanned = false, isPalletScanned = false;
    private String scannedLocation = null, scannedPallet = "", scannedSKU = null;
    private String userId = "", scanType = "", accountId = "", selectedTenant = "",
            selectedWH = "", tenantId = "", warehouseId = "";
    private String batch = "", serialNo = "", prjRef = "", mfg = "", exp = "", mrp = "";
    private RecyclerView recycler_view_sku_list, recycler_view_sku_listSuccess;
    RelativeLayout relativeOne, relativeTwo, relativeThree, relativeFour;
    TextView lblBatchNo, lblserialNo, lblMfgDate, lblExpDate, lblMRP, lblScannedSku, lblProjectRefNo, tvSONumber, lblContainer, lblQty;
    EditText lblReceivedQty, lblPackingType;
    LinearLayout linearCon, linearSKU;
    String SONumber = "", SODetailID = "", OutBoundId = "", packedQty = "", pickedQty = "", OBDNumber = "", PSNID = "0", PSNDetailsID = "", SOHeaderID = "",
            huNo = "", huSize = "";
    boolean isPallet = false, isSKU = false;
    int pickedQty_1 = 0, packedQty_1 = 0;

    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public PackingFragment() {
    }

    public void myScannedData(Context context, String barcode) {
        try {
            ProcessScannedinfo(barcode.trim());
        } catch (Exception e) {
            //  Toast.makeText(context, ""+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_packing, container, false);
        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        scanType = sp.getString("scanType", "");
        accountId = sp.getString("AccountId", "");


        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanSku = (CardView) rootView.findViewById(R.id.cvScanSku);
        cvScanSONumber = (CardView) rootView.findViewById(R.id.cvScanSONumber);
        ivScanSONumber = (ImageView) rootView.findViewById(R.id.ivScanSONumber);
        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanSku = (ImageView) rootView.findViewById(R.id.ivScanSku);

        relativeOne = (RelativeLayout) rootView.findViewById(R.id.relativeOne);
        relativeTwo = (RelativeLayout) rootView.findViewById(R.id.relativeTwo);
        relativeThree = (RelativeLayout) rootView.findViewById(R.id.relativeThree);
        relativeFour = (RelativeLayout) rootView.findViewById(R.id.relativeFour);

        linearCon = (LinearLayout) rootView.findViewById(R.id.linearCon);
        linearSKU = (LinearLayout) rootView.findViewById(R.id.linearSKU);

        btnPackComplete = (Button) rootView.findViewById(R.id.btnPackComplete);
        btnBackPacking = (Button) rootView.findViewById(R.id.btnBackPacking);
        btnPack = (Button) rootView.findViewById(R.id.btnPack);
        btnClear = (Button) rootView.findViewById(R.id.btnClear);

        tvOrderInfromation = (TextView) rootView.findViewById(R.id.tvOrderInfromation);
        tvOrderInfromationSuccess = (TextView) rootView.findViewById(R.id.tvOrderInfromationSuccess);
        tvSONumber = (TextView) rootView.findViewById(R.id.tvSONumber);
        lblQty = (TextView) rootView.findViewById(R.id.lblQty);

        lblBatchNo = (TextView) rootView.findViewById(R.id.lblBatchNo);
        lblserialNo = (TextView) rootView.findViewById(R.id.lblserialNo);
        lblMfgDate = (TextView) rootView.findViewById(R.id.lblMfgDate);
        lblExpDate = (TextView) rootView.findViewById(R.id.lblExpDate);
        lblMRP = (TextView) rootView.findViewById(R.id.lblMRP);
        lblProjectRefNo = (TextView) rootView.findViewById(R.id.lblProjectRefNo);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);
        lblContainer = (TextView) rootView.findViewById(R.id.lblContainer);
        lblHu = (TextView) rootView.findViewById(R.id.lblHu);

        lblHu.setText("");

        lblReceivedQty = (EditText) rootView.findViewById(R.id.lblReceivedQty);
        lblPackingType = (EditText) rootView.findViewById(R.id.lblPackingType);

        lblReceivedQty.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        lblPackingType.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        recycler_view_sku_list = (RecyclerView) rootView.findViewById(R.id.recycler_view_sku_list);
        recycler_view_sku_list.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());
        // use a linear layout manager
        recycler_view_sku_list.setLayoutManager(linearLayoutManager);

        recycler_view_sku_listSuccess = (RecyclerView) rootView.findViewById(R.id.recycler_view_sku_listSuccess);
        recycler_view_sku_listSuccess.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());
        // use a linear layout manager
        recycler_view_sku_listSuccess.setLayoutManager(linearLayoutManager);


        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);


        common = new Common();
        gson = new GsonBuilder().create();
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


        cvScanSONumber.setOnClickListener(this);
        btnPackComplete.setOnClickListener(this);
        btnBackPacking.setOnClickListener(this);
        btnPack.setOnClickListener(this);
        btnClear.setOnClickListener(this);


        relativeOne.setVisibility(View.VISIBLE);
        relativeTwo.setVisibility(View.GONE);
        relativeThree.setVisibility(View.GONE);
        relativeFour.setVisibility(View.GONE);


        if (scanType.equals("Auto")) {
            btnPack.setVisibility(View.INVISIBLE);
            ClearFileds();
        } else {
            btnPack.setVisibility(View.VISIBLE);
            ClearFileds1();
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {


            case R.id.btnPackComplete:
                UpdatePackComplete();
                break;

            case R.id.btnBackPacking:
                relativeOne.setVisibility(View.VISIBLE);
                relativeTwo.setVisibility(View.GONE);
                relativeThree.setVisibility(View.GONE);
                relativeFour.setVisibility(View.GONE);
                //  FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnPack:
                UpsertPackItem();
                break;

            case R.id.btnCloseOne:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnClear:
                ClearFileds();
                break;

        }
    }

    private void ClearFileds() {

        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSku.setImageResource(R.drawable.fullscreen_img);

        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

        lblContainer.setText("");

        isSKU = false;
        isPallet = false;

        lblReceivedQty.setText("");
        lblReceivedQty.clearFocus();
        lblReceivedQty.setEnabled(false);

        lblPackingType.setText("");
        lblPackingType.clearFocus();
        lblPackingType.setEnabled(false);



    }


    private void ClearFileds1() {

        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSku.setImageResource(R.drawable.fullscreen_img);

        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

        lblContainer.setText("");

        isSKU = false;
        isPallet = false;

        lblReceivedQty.setText("");
        lblReceivedQty.clearFocus();
        lblReceivedQty.setEnabled(false);

        lblPackingType.setText("");
        lblPackingType.clearFocus();
        lblPackingType.setEnabled(false);

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

        if (((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).isDrawerOpen(GravityCompat.START)) {
            return;
        }


        if (scannedData != null) {

            if (ProgressDialogUtils.isProgressActive() || Common.isPopupActive()) {
                common.showUserDefinedAlertType(errorMessages.EMC_082, getActivity(), getContext(), "Warning");
                return;
            }


            if (relativeOne.getVisibility() == View.VISIBLE) {
                ScanSONumberForPacking(scannedData, 0);
            }

            if (relativeThree.getVisibility() == View.VISIBLE) {
                if (linearCon.getVisibility() == View.VISIBLE) {
                    if (!isPallet) {
                        ValidateCarton(scannedData);
                    } else {
                        ValiDateMaterial(scannedData);
                    }
                } else {
                    ValiDateMaterial(scannedData);
                }
            }

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
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
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

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Packing");
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

    public void UpdatePackComplete() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            final OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setTenatID(userId);
            outbountDTO.setAccountID(accountId);
            outbountDTO.setSONumber(SONumber);
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.UpdatePackComplete(message);
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
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    return;
                                }
                            } else {
                                List<LinkedTreeMap<?, ?>> _lOutBound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lOutBound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                if (_lOutBound != null) {
                                    final List<OutbountDTO> outbountDTOS = new ArrayList<>();
                                    for (int i = 0; i < _lOutBound.size(); i++) {
                                        OutbountDTO dto = new OutbountDTO(_lOutBound.get(i).entrySet());
                                        outbountDTOS.add(dto);
                                    }

                                    if (outbountDTOS.size() > 0) {

                                        if (outbountDTOS.get(0).getPackComplete().equals("true")) {
                                            relativeOne.setVisibility(View.GONE);
                                            relativeTwo.setVisibility(View.GONE);
                                            relativeFour.setVisibility(View.VISIBLE);
                                            ScanSONumberForPacking(SONumber, 3);
                                        } else {
                                            common.showUserDefinedAlertType("Pack complete failed", getActivity(), getActivity(), "Error");
                                        }

                                        ProgressDialogUtils.closeProgressDialog();
                                    }

                                }

                            }

                            ProgressDialogUtils.closeProgressDialog();

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

/*    public void GETMSPsForPacking(final String mCode) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            final OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setTenatID(userId);
            outbountDTO.setAccountID(accountId);
            outbountDTO.setSODetailsID(SODetailID);
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GETMSPsForPacking(message);
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
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    return;
                                }
                            } else {
                                List<LinkedTreeMap<?, ?>> _lOutBound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lOutBound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                if(_lOutBound!=null){
                                    final List<OutbountDTO> outbountDTOS=new ArrayList<>();
                                    for (int i = 0; i < _lOutBound.size(); i++) {
                                        OutbountDTO dto = new OutbountDTO(_lOutBound.get(i).entrySet());
                                        outbountDTOS.add(dto);
                                    }

                                    if(outbountDTOS.size()>0){

                                        lblScannedSku.setText(mCode);
                                        lblBatchNo.setText(outbountDTOS.get(0).getBatchNo());
                                        lblserialNo.setText(outbountDTOS.get(0).getSerialNo());
                                        lblMfgDate.setText(outbountDTOS.get(0).getMfgDate());
                                        lblExpDate.setText(outbountDTOS.get(0).getExpDate());
                                        lblProjectRefNo.setText(outbountDTOS.get(0).getProjectNo());
                                        lblMRP.setText(outbountDTOS.get(0).getMRP());
                                        tvSONumber.setText(SONumber);

                                        ProgressDialogUtils.closeProgressDialog();
                                    }

                                }

                            }

                            ProgressDialogUtils.closeProgressDialog();

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
    }*/

    public void UpsertPackItem() {

        try {

            if (lblReceivedQty.getText().toString().isEmpty()) {
                common.showUserDefinedAlertType("Please Enter Qty", getActivity(), getActivity(), "Warning");
                return;
            }
/*                if(lblPackingType.getText().toString().isEmpty()){
                    common.showUserDefinedAlertType("Please Enter Packing Type", getActivity(), getActivity(), "Warning");
                    return;
                }*/
            if ((Integer.parseInt(lblReceivedQty.getText().toString()) + packedQty_1) > pickedQty_1) {
                common.showUserDefinedAlertType("Qty is more than picked qty", getActivity(), getActivity(), "Warning");
                return;
            }

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            final OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setTenatID(userId);
            outbountDTO.setAccountID(accountId);
            outbountDTO.setOutboundID(OutBoundId);
            outbountDTO.setPSNID(PSNID);
            outbountDTO.setSODetailsID(SODetailID);
            outbountDTO.setSONumber(SONumber);
            outbountDTO.setPSNDetailsID(PSNDetailsID);
            outbountDTO.setPackedQty(lblReceivedQty.getText().toString());
            outbountDTO.setPickedQty(pickedQty);
            outbountDTO.setPackType(lblPackingType.getText().toString());
            outbountDTO.setCartonSerialNo(lblContainer.getText().toString());
            outbountDTO.setmCode(lblScannedSku.getText().toString());
            outbountDTO.setBatchNo(lblBatchNo.getText().toString());
            outbountDTO.setSerialNo(lblserialNo.getText().toString());
            outbountDTO.setMfgDate(lblMfgDate.getText().toString());
            outbountDTO.setExpDate(lblExpDate.getText().toString());
            outbountDTO.setMRP(lblMRP.getText().toString());
            outbountDTO.setProjectNo(lblProjectRefNo.getText().toString());
            outbountDTO.setHUNo(huNo);
            outbountDTO.setHUSize(huSize);
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.UpsertPackItem(message);
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
                                    ProgressDialogUtils.closeProgressDialog();

                                    if (owmsExceptionMessage.getWMSMessage().toString().equals("You are exceeding the qty.")) {
                                        common.setIsPopupActive(true);
                                        new SoundUtils().alertWarning(getActivity(), getActivity());
                                        DialogUtils.showAlertDialog(getActivity(), "Warning", owmsExceptionMessage.getWMSMessage().toString(), R.drawable.warning_img, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        relativeOne.setVisibility(View.GONE);
                                                        relativeTwo.setVisibility(View.VISIBLE);
                                                        relativeThree.setVisibility(View.GONE);
                                                        relativeFour.setVisibility(View.GONE);
                                                        ClearFileds1();
                                                        ScanSONumberForPacking(SONumber, 1);
                                                        common.setIsPopupActive(false);
                                                        break;
                                                }
                                            }
                                        });
                                    } else {
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    }
                                    return;
                                }
                                ProgressDialogUtils.closeProgressDialog();
                            } else {
                                List<LinkedTreeMap<?, ?>> _lOutBound = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lOutBound = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                if (_lOutBound != null) {
                                    final List<OutbountDTO> outbountDTOS = new ArrayList<>();
                                    for (int i = 0; i < _lOutBound.size(); i++) {
                                        OutbountDTO dto = new OutbountDTO(_lOutBound.get(i).entrySet());
                                        outbountDTOS.add(dto);
                                    }
                                    ProgressDialogUtils.closeProgressDialog();
                                    if (outbountDTOS.size() > 0) {
                                        if (Integer.parseInt(outbountDTOS.get(0).getPSNDetailsID()) > 0) {
                                            // ClearFileds();
                                            if (scanType.equals("Auto")) {
                                                packedQty_1 += 1;
                                                if (packedQty_1 == pickedQty_1) {
                                                    ScanSONumberForPacking(SONumber, 1);
                                                } else {
                                                    lblQty.setText(packedQty_1 + " / " + pickedQty_1);
                                                }
                                            } else {
                                                relativeOne.setVisibility(View.GONE);
                                                relativeTwo.setVisibility(View.VISIBLE);
                                                relativeThree.setVisibility(View.GONE);
                                                relativeFour.setVisibility(View.GONE);
                                                ClearFileds1();
                                                ScanSONumberForPacking(SONumber, 1);
                                            }
                                            //
                                        }

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

                    // response object fails
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
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
        }
    }

    public void ScanSONumberForPacking(final String scannedData, final int value) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setTenatID(userId);
            outbountDTO.setAccountID(accountId);
            outbountDTO.setSONumber(scannedData);
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ScanSONumberForPacking(message);
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
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    return;
                                }
                            } else {
                                List<LinkedTreeMap<?, ?>> _lSKUList = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lSKUList = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                if (_lSKUList != null) {
                                    final List<SKUListDTO> skuListDTOS = new ArrayList<>();
                                    for (int i = 0; i < _lSKUList.size(); i++) {
                                        SKUListDTO dto = new SKUListDTO(_lSKUList.get(i).entrySet());
                                        skuListDTOS.add(dto);
                                    }

                                    if (skuListDTOS.size() > 0) {

                                        cvScanSONumber.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSONumber.setImageResource(R.drawable.check);

                                        if (value == 0) {
                                            new CountDownTimer(1000, 1000) {

                                                public void onTick(long millisUntilFinished) {

                                                }

                                                public void onFinish() {
                                                    setData(skuListDTOS, scannedData);
                                                }
                                            }.start();
                                        } else {
                                            if (value == 3) {
                                                setDataScuess(skuListDTOS, scannedData);
                                            } else {
                                                setData(skuListDTOS, scannedData);
                                            }

                                        }

                                        ProgressDialogUtils.closeProgressDialog();
                                    } else {
                                        common.showUserDefinedAlertType("No SKU List Found", getActivity(), getActivity(), "Warning");
                                    }

                                }
                            }

                            ProgressDialogUtils.closeProgressDialog();


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

    public void setDataScuess(final List<SKUListDTO> skuListDTOS, String scannedData) {

        SONumber = scannedData;
        relativeOne.setVisibility(View.GONE);
        relativeTwo.setVisibility(View.GONE);
        relativeThree.setVisibility(View.GONE);
        relativeFour.setVisibility(View.VISIBLE);
        tvOrderInfromationSuccess.setText(skuListDTOS.get(0).getCustomerName());

        SkuListAdapter skuListAdapter = new SkuListAdapter(getActivity(), skuListDTOS, relativeOne, relativeTwo, relativeThree, relativeFour, new SkuListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {

            }
        });

        recycler_view_sku_listSuccess.setAdapter(skuListAdapter);
    }

    public void setData(final List<SKUListDTO> skuListDTOS, String scannedData) {

        SONumber = scannedData;
        relativeOne.setVisibility(View.GONE);
        relativeTwo.setVisibility(View.VISIBLE);
        relativeThree.setVisibility(View.GONE);
        relativeFour.setVisibility(View.GONE);
        tvOrderInfromation.setText(skuListDTOS.get(0).getCustomerName());
        SkuListAdapter skuListAdapter = new SkuListAdapter(getActivity(), skuListDTOS, relativeOne, relativeTwo, relativeThree, relativeFour, new SkuListAdapter.OnItemClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemClick(int pos) {


                if (Integer.parseInt(skuListDTOS.get(pos).getPickedQty().split("[.]")[0]) != Integer.parseInt(skuListDTOS.get(pos).getPackedQty().split("[.]")[0])) {

                    SODetailID = skuListDTOS.get(pos).getSODetailsID();
                    OutBoundId = skuListDTOS.get(pos).getOutboundID();
                    OBDNumber = skuListDTOS.get(pos).getOBDNumber();
                    packedQty = skuListDTOS.get(pos).getPackedQty();
                    pickedQty = skuListDTOS.get(pos).getPickedQty();
                    PSNID = skuListDTOS.get(pos).getPSNID();
                    PSNDetailsID = skuListDTOS.get(pos).getPSNDetailsID();
                    lblScannedSku.setText(skuListDTOS.get(pos).getMCode());
                    lblBatchNo.setText(skuListDTOS.get(pos).getBatchNo());
                    lblserialNo.setText(skuListDTOS.get(pos).getSerialNo());
                    lblMfgDate.setText(skuListDTOS.get(pos).getMfgDate());
                    lblExpDate.setText(skuListDTOS.get(pos).getExpDate());
                    lblProjectRefNo.setText(skuListDTOS.get(pos).getProjectNo());
                    lblMRP.setText(skuListDTOS.get(pos).getMRP());
                    tvSONumber.setText(SONumber);

                    huNo = skuListDTOS.get(pos).getHUNo();
                    huSize = skuListDTOS.get(pos).getHUSize();
                    if (!huSize.equals("1")) {
                        lblHu.setText("Hu: " + "" + huNo + "/" + huSize);
                    }else {
                        lblHu.setText("");
                    }

                    pickedQty_1 = Integer.parseInt(pickedQty.split("[.]")[0]);
                    packedQty_1 = Integer.parseInt(packedQty.split("[.]")[0]);

                    lblQty.setText(packedQty_1 + " / " + pickedQty_1);

                    if (skuListDTOS.get(pos).getBusinessType().equals("E-Commerce")) {
                        linearCon.setVisibility(View.GONE);
                        linearSKU.setVisibility(View.VISIBLE);
                    } else {
                        linearCon.setVisibility(View.VISIBLE);
                        linearSKU.setVisibility(View.VISIBLE);
                    }

                    ProgressDialogUtils.closeProgressDialog();

                    //GETMSPsForPacking(skuListDTOS.get(pos).getMCode());

                    relativeOne.setVisibility(View.GONE);
                    relativeTwo.setVisibility(View.GONE);
                    relativeThree.setVisibility(View.VISIBLE);
                    relativeFour.setVisibility(View.GONE);

                } else {
                    common.showUserDefinedAlertType("Already Packed", getActivity(), getActivity(), "Warning");
                }

            }
        });
        recycler_view_sku_list.setAdapter(skuListAdapter);
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
            scanDTO.setObdNumber(OBDNumber);
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
                            isSKU = false;
                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                            ivScanSku.setImageResource(R.drawable.fullscreen_img);
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

                                    if (scanDTO1.getSkuCode().equalsIgnoreCase(lblScannedSku.getText().toString().trim())) {

                                        if ((lblBatchNo.getText().toString().equalsIgnoreCase(scanDTO1.getBatch()) || scanDTO1.getBatch() == null
                                                || scanDTO1.getBatch().equalsIgnoreCase("") || scanDTO1.getBatch().isEmpty()) &&
                                                lblserialNo.getText().toString().equalsIgnoreCase(scanDTO1.getSerialNumber()) &&
                                                lblMfgDate.getText().toString().equalsIgnoreCase(scanDTO1.getMfgDate()) &&
                                                lblExpDate.getText().toString().equalsIgnoreCase(scanDTO1.getExpDate())
                                        ) {

/*                                             &&
                                              lblMfgDate.getText().toString().equalsIgnoreCase(scanDTO1.getMfgDate()) &&
                                                    lblExpDate.getText().toString().equalsIgnoreCase(scanDTO1.getExpDate()
                                             lblProjectRefNo.getText().toString().equalsIgnoreCase(scanDTO1.getPrjRef())*/


                                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanSku.setImageResource(R.drawable.check);
                                            isSKU = true;

                                            if (scanType.equals("Auto")) {

                                                lblReceivedQty.setText("1");
                                                lblReceivedQty.clearFocus();
                                                lblReceivedQty.setEnabled(false);

                                                lblPackingType.setText("");
                                                lblPackingType.clearFocus();
                                                lblPackingType.setEnabled(false);

                                                UpsertPackItem();

                                            } else {
                                                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0073);
                                                lblReceivedQty.setText("");
                                                lblReceivedQty.clearFocus();
                                                lblReceivedQty.setEnabled(true);

                                                lblPackingType.setText("");
                                                lblPackingType.clearFocus();
                                                lblPackingType.setEnabled(true);

                                            }

                                        } else {
                                            common.showUserDefinedAlertType(errorMessages.EMC_0079, getActivity(), getContext(), "Error");
                                        }

                                    } else {
                                        isSKU = false;
                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.warning_img);
                                        common.showUserDefinedAlertType(errorMessages.EMC_0029, getActivity(), getContext(), "Error");
                                    }

                                } else {
                                    // lblScannedSku.setText("");
                                    isSKU = false;
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

    public void ValidateCarton(final String scannedData) {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.ScanDTO, getContext());
            ScanDTO scanDTO = new ScanDTO();
            scanDTO.setUserID(userId);
            scanDTO.setAccountID(accountId);
            // scanDTO.setTenantID(String.valueOf(tenantID));
            //scanDTO.setWarehouseID(String.valueOf(warehouseID));
            scanDTO.setScanInput(scannedData);
            scanDTO.setObdNumber(SONumber);
            //inboundDTO.setIsOutbound("0");
            message.setEntityObject(scanDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.ValidateCarton(message);
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

                            isPallet = false;
                            lblContainer.setText("");
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
                                    lblContainer.setText(scannedData);
                                    //ValidatePalletCode();
                                    isPallet = true;
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.check);
                                } else {
                                    isPallet = false;
                                    lblContainer.setText("");
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.warning_img);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Warning");
                                }
                            } else {
                                isPallet = false;
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

}