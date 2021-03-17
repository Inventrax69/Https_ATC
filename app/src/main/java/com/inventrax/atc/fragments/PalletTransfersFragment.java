package com.inventrax.atc.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.inventrax.atc.common.Common;
import com.inventrax.atc.common.constants.EndpointConstants;
import com.inventrax.atc.common.constants.ErrorMessages;
import com.inventrax.atc.interfaces.ApiInterface;
import com.inventrax.atc.pojos.HouseKeepingDTO;
import com.inventrax.atc.pojos.InventoryDTO;
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

public class PalletTransfersFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

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
    private RelativeLayout rlIPalletTransfer, rlSelect;
    private CardView cvScanFromCont, cvScanLocation;
    private ImageView ivScanFromCont, ivScanLocation;
    private SearchableSpinner spinnerSelectTenant, spinnerSelectWarehouse;
    private Button btnBinComplete, btn_clear, btnGo;

    private String Materialcode = null, Userid = null, scanType = "", accountId = "", storageloc = "";
    private int IsToLoc = 0;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    Boolean isPalletScaned = false, isLocationScaned = false, isSKUScanned = false, IsProceedForBinTransfer = false;
    private SoundUtils soundUtils;
    private String selectedTenant = "", selectedWH = "", tenantId = "", whId = "";
    List<HouseKeepingDTO> lstTenants = null;
    List<HouseKeepingDTO> lstWarehouse = null;
    TextView txtWarehousetName,txtTendentName,txtFromPallet,txtLocation;
    //ListView sku_list;
    SDKAdapter adapter;

    TableLayout tl;

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

    public PalletTransfersFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_pallet_tranfers, container, false);
        loadFormControls();
        return rootView;

    }

    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        Userid = sp.getString("RefUserId", "");
        scanType = sp.getString("scanType", "");
        accountId = sp.getString("AccountId", "");

        rlIPalletTransfer = (RelativeLayout) rootView.findViewById(R.id.rlIPalletTransfer);
        rlSelect = (RelativeLayout) rootView.findViewById(R.id.rlSelect);

        cvScanFromCont = (CardView) rootView.findViewById(R.id.cvScanFromCont);
        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);

        ivScanFromCont = (ImageView) rootView.findViewById(R.id.ivScanFromCont);
        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);

        txtWarehousetName = (TextView) rootView.findViewById(R.id.txtWarehousetName);
        txtTendentName = (TextView) rootView.findViewById(R.id.txtTendentName);

        txtFromPallet = (TextView) rootView.findViewById(R.id.txtFromPallet);
        txtLocation = (TextView) rootView.findViewById(R.id.txtLocation);

        lstTenants = new ArrayList<HouseKeepingDTO>();
        lstWarehouse = new ArrayList<HouseKeepingDTO>();

        spinnerSelectTenant = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectTenant);
        spinnerSelectWarehouse = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectWarehouse);

        spinnerSelectTenant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTenant = spinnerSelectTenant.getSelectedItem().toString();
                txtTendentName.setText(selectedTenant);
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
                txtWarehousetName.setText(selectedWH);
                getWarehouseId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnBinComplete = (Button) rootView.findViewById(R.id.btnBinComplete);
        btn_clear = (Button) rootView.findViewById(R.id.btn_clear);
        btnGo = (Button) rootView.findViewById(R.id.btnGo);
        //sku_list=(ListView)rootView.findViewById(R.id.sku_list);

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

        tl = (TableLayout) rootView.findViewById(R.id.table);

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


/*        // To get tenants
        getTenants();*/

        getWarehouse();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_clear:
                Clearfields();                       // clear the scanned fields
                break;


            case R.id.btnGo:
                if (!whId.equals("")) {
                    rlSelect.setVisibility(View.GONE);
                    rlIPalletTransfer.setVisibility(View.VISIBLE);
                    // method to get the storage locations
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0011, getActivity(), getContext(), "Error");
                }
                break;

            case R.id.btnBinComplete:
                if(!txtLocation.getText().toString().isEmpty()){
                    TransferPalletToLocation();
                }else{
                    if(!isPalletScaned)
                        common.showUserDefinedAlertType("Please scan pallet", getActivity(), getContext(), "Error");
                    else
                        common.showUserDefinedAlertType("Please scan Location", getActivity(), getContext(), "Error");
                }
                break;

        }
    }

    private void Clearfields() {

        cvScanFromCont.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanFromCont.setImageResource(R.drawable.fullscreen_img);

        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

        isPalletScaned = false;

        txtFromPallet.setText("");
        txtLocation.setText("");
        ///sku_list.setAdapter(null);
        tl.removeAllViews();
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

                if(!isPalletScaned){
                    ValidatePallet(scannedData);
                }else{
                    ValidateLocation(scannedData);
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

    public void getTenants() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.HouseKeepingDTO, getContext());
            HouseKeepingDTO houseKeepingDTO = new HouseKeepingDTO();
            houseKeepingDTO.setUserId(Userid);
            houseKeepingDTO.setTenantID(tenantId);
            houseKeepingDTO.setAccountID(accountId);
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
              //  getWarehouse();
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

    private TextView getTextView(String title, int color, int typeface, int bgColor) {
        TextView tv = new TextView(getContext());
        tv.setText(title);
        tv.setTextColor(color);
        tv.setPadding(10, 10, 10, 10);
        tv.setTypeface(Typeface.DEFAULT, typeface);
        tv.setBackgroundColor(bgColor);
        tv.setLayoutParams(getLayoutParams());
        tv.setOnClickListener(this);
        return tv;
    }

    @NonNull
    private TableRow.LayoutParams getLayoutParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(2, 0, 0, 2);
        return params;
    }

    @NonNull
    private TableLayout.LayoutParams getTblLayoutParams() {
        return new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
    }

    public void addHeaders() {

        TableRow tr = new TableRow(getContext());

        tr.setLayoutParams(getLayoutParams());
        tr.addView(getTextView("SKU", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("Batch", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("MfgDate", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("ExpDate", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("Qty.", Color.WHITE, Typeface.BOLD, Color.BLUE));
        tr.addView(getTextView("Project Ref. No", Color.WHITE, Typeface.BOLD, Color.BLUE));


        tl.addView(tr, getTblLayoutParams());
    }

    /**
     * This function add the data to the table
     **/
    public void addData(List<InventoryDTO> liveStock) {

        TableLayout tl = (TableLayout) rootView.findViewById(R.id.table);
        for (InventoryDTO data : liveStock) {
            TableRow tr = new TableRow(getContext());
            tr.setLayoutParams(getLayoutParams());

            tr.addView(getTextView(data.getMaterialCode(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.scanLabelBackground)));
            tr.addView(getTextView(data.getBatchNo().toString(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.scanLabelBackground)));
            tr.addView(getTextView(data.getMfgDate(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.scanLabelBackground)));
            tr.addView(getTextView(data.getExpDate(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.scanLabelBackground)));
            tr.addView(getTextView(data.getQuantity(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.scanLabelBackground)));
            tr.addView(getTextView(data.getProjectNo(), Color.BLACK, Typeface.BOLD, ContextCompat.getColor(getContext(), R.color.scanLabelBackground)));


            tl.addView(tr, getTblLayoutParams());
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
                            if(!isPalletScaned){
                                txtFromPallet.setText("");
                                cvScanFromCont.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanFromCont.setImageResource(R.drawable.invalid_cross);
                            }else{
                                txtLocation.setText("");
                                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanLocation.setImageResource(R.drawable.invalid_cross);
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

                                    if(!isPalletScaned){
                                        isPalletScaned=true;
                                        txtFromPallet.setText(scannedData);
                                        cvScanFromCont.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanFromCont.setImageResource(R.drawable.check);
                                        GetActiveStockData();
                                    }else{
                                        txtLocation.setText(scannedData);
                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanLocation.setImageResource(R.drawable.check);
                                    }

                                } else {
                                    if(!isPalletScaned){
                                        txtFromPallet.setText("");
                                        cvScanFromCont.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanFromCont.setImageResource(R.drawable.warning_img);
                                    }else{
                                        txtLocation.setText("");
                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanLocation.setImageResource(R.drawable.warning_img);
                                    }

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

                            txtLocation.setText("");
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
                                        txtLocation.setText(scannedData);
                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanLocation.setImageResource(R.drawable.check);

                                } else {

                                    txtLocation.setText("");
                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanLocation.setImageResource(R.drawable.warning_img);

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

    private class SDKAdapter extends BaseAdapter {
        public Context context;
        public List<InventoryDTO> inventoryDTO_list;
        public SDKAdapter(Context context, List<InventoryDTO> inventoryDTO_list) {
            this.context=context;
            this.inventoryDTO_list=inventoryDTO_list;
        }

        @Override
        public int getCount() {
            return inventoryDTO_list.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater=getActivity().getLayoutInflater();
            View rowView=inflater.inflate(R.layout.sdk_list, null,true);

            TextView sku=(TextView)rowView.findViewById(R.id.sku);
            TextView batchno=(TextView)rowView.findViewById(R.id.batchno);
            TextView qty=(TextView)rowView.findViewById(R.id.qty);

            sku.setText(inventoryDTO_list.get(i).getMaterialCode());
            batchno.setText(inventoryDTO_list.get(i).getBatchNo());
            qty.setText(inventoryDTO_list.get(i).getQuantity());

            return rowView;
        }
    }


    public void TransferPalletToLocation() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setLocationCode(txtLocation.getText().toString());
            inventoryDTO.setContainerCode(txtFromPallet.getText().toString());
            inventoryDTO.setTenantCode(selectedTenant);
            inventoryDTO.setAccountID(accountId);
            inventoryDTO.setTenantID(tenantId);
            inventoryDTO.setWarehouseId(whId);
            inventoryDTO.setWarehouse(selectedWH);
            message.setEntityObject(inventoryDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.TransferPalletToLocation(message);
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
                            } else {

                                Log.v("ABCDE",new Gson().toJson(core.getEntityObject()));

                                LinkedTreeMap<?, ?> _lInventory = new LinkedTreeMap<>();
                                _lInventory = (LinkedTreeMap<?, ?>) core.getEntityObject();

                                InventoryDTO lstInventory = new InventoryDTO();

                                if(lstInventory!=null){
                                    common.showUserDefinedAlertType("Successfully Transferred", getActivity(), getContext(), "Success");
                                    Clearfields();
                                }else{
                                    common.showUserDefinedAlertType("Error while Transfer", getActivity(), getContext(), "Error");
                                }


                                ProgressDialogUtils.closeProgressDialog();
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tl.removeAllViews();
                //sku_list.setAdapter(null);
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
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
            return;
        }
    }

    public void GetActiveStockData() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setMaterialCode("");
            inventoryDTO.setLocationCode("");
            inventoryDTO.setContainerCode(txtFromPallet.getText().toString());
            inventoryDTO.setTenantCode(selectedTenant);
            inventoryDTO.setAccountID(accountId);
            inventoryDTO.setTenantID(tenantId);
            inventoryDTO.setWarehouseId(whId);
            inventoryDTO.setWarehouse(selectedWH);
            inventoryDTO.setMaterialCode("");
            inventoryDTO.setBatchNo("");
            inventoryDTO.setSerialNo("");
            inventoryDTO.setMfgDate("");
            inventoryDTO.setExpDate("");
            inventoryDTO.setProjectNo("");
            inventoryDTO.setMRP("");
            message.setEntityObject(inventoryDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetActivestock(message);
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
                        if (response.body() != null) {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;

                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());


                                }

                                tl.removeAllViews();
                                //sku_list.setAdapter(null);

                                ProgressDialogUtils.closeProgressDialog();
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {


                                List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<InventoryDTO> lstInventory = new ArrayList<InventoryDTO>();

                                ProgressDialogUtils.closeProgressDialog();

                                if(_lInventory!=null){
                                    if (_lInventory.size() > 0) {
                                        InventoryDTO inventorydto = null;
                                        for (int i = 0; i < _lInventory.size(); i++) {
                                            inventorydto = new InventoryDTO(_lInventory.get(i).entrySet());
                                            lstInventory.add(inventorydto);
                                        }
                                        //loadList(lstInventory);
                                        addHeaders();
                                        addData(lstInventory);
                                    } else {
                                        //sku_list.setAdapter(null);
                                        tl.removeAllViews();
                                        common.showUserDefinedAlertType(errorMessages.EMC_0060, getActivity(), getContext(), "Warning");
                                    }
                                }else{
                                    tl.removeAllViews();
                                    //sku_list.setAdapter(null);
                                    common.showUserDefinedAlertType(errorMessages.EMC_0060, getActivity(), getContext(), "Warning");
                                }
                            }
                        } else {
                            tl.removeAllViews();
                            //sku_list.setAdapter(null);
                            ProgressDialogUtils.closeProgressDialog();
                            common.showUserDefinedAlertType(errorMessages.EMC_0021, getActivity(), getContext(), "Error");
                            return;
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        tl.removeAllViews();
                        // sku_list.setAdapter(null);
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                        return;
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //sku_list.setAdapter(null);
                tl.removeAllViews();
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
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
            return;
        }
    }
}