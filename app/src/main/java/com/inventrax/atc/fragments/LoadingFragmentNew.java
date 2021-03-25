package com.inventrax.atc.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.inventrax.atc.activities.MainActivity;
import com.inventrax.atc.adapters.PackiSlipListAdapter;
import com.inventrax.atc.adapters.PackingInfoAdapter;
import com.inventrax.atc.common.Common;
import com.inventrax.atc.common.constants.EndpointConstants;
import com.inventrax.atc.common.constants.ErrorMessages;
import com.inventrax.atc.interfaces.ApiInterface;
import com.inventrax.atc.pojos.HouseKeepingDTO;
import com.inventrax.atc.pojos.InventoryDTO;
import com.inventrax.atc.pojos.LoadDTO;
import com.inventrax.atc.pojos.OutbountDTO;
import com.inventrax.atc.pojos.WMSCoreMessage;
import com.inventrax.atc.pojos.WMSExceptionMessage;
import com.inventrax.atc.searchableSpinner.SearchableSpinner;
import com.inventrax.atc.services.RetrofitBuilderHttpsEx;
import com.inventrax.atc.util.DialogUtils;
import com.inventrax.atc.util.ExceptionLoggerUtils;
import com.inventrax.atc.util.FragmentUtils;
import com.inventrax.atc.util.ProgressDialogUtils;
import com.inventrax.atc.util.ScanValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Padmaja on 06/26/2018.
 */

public class LoadingFragmentNew extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_009";

    private View rootView;
    Button btnGo, btnCloseOne, btn_pending, btnLoad;
    RelativeLayout rlLoadingOne, rlLoadListTwo;
    TextView txtLoadSheetNo, txtPackSlipCount, txtScanPackSlip;
    CardView cvScanPackSlip;
    ImageView ivScanPackSlip;
    SearchableSpinner spinnerSelectLoadList;
    EditText etQty;
    TextInputLayout txtInputLayoutQty;
    String userId = "", scanType = "", accountId = "", selectedTenant = "",
            selectedWH = "", tenantId = "", warehouseId = "";
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

    private SearchableSpinner spinnerSelectTenant, spinnerSelectWH;
    List<HouseKeepingDTO> lstTenants = null;
    List<HouseKeepingDTO> lstWarehouse = null;
    private RecyclerView recyclerViewPackInfo, rv_packSLiplist;
    private LinearLayoutManager linearLayoutManager, linearLayoutManagerPackSlip;

    TableLayout tl;

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

    public LoadingFragmentNew() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_loading_new, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        scanType = sp.getString("scanType", "");
        accountId = sp.getString("AccountId", "");

        rlLoadingOne = (RelativeLayout) rootView.findViewById(R.id.rlLoadingOne);
        rlLoadListTwo = (RelativeLayout) rootView.findViewById(R.id.rlLoadListTwo);
        lstloaddata = new ArrayList<LoadDTO>();
        spinnerSelectLoadList = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectLoadList);
        spinnerSelectLoadList.setOnItemSelectedListener(this);

        btnGo = (Button) rootView.findViewById(R.id.btnGo);
        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btn_pending = (Button) rootView.findViewById(R.id.btn_pending);
        btnLoad = (Button) rootView.findViewById(R.id.btnLoad);

        recyclerViewPackInfo = (RecyclerView) rootView.findViewById(R.id.recycler_view_packinfo);
        rv_packSLiplist = (RecyclerView) rootView.findViewById(R.id.rv_packSLiplist);
        recyclerViewPackInfo.setHasFixedSize(true);
        rv_packSLiplist.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManagerPackSlip = new LinearLayoutManager(getContext());

        cvScanPackSlip = (CardView) rootView.findViewById(R.id.cvScanPackSlip);
        ivScanPackSlip = (ImageView) rootView.findViewById(R.id.ivScanPackSlip);

        // use a linear layout manager
        recyclerViewPackInfo.setLayoutManager(linearLayoutManager);
        rv_packSLiplist.setLayoutManager(linearLayoutManagerPackSlip);

        txtLoadSheetNo = (TextView) rootView.findViewById(R.id.txtLoadSheetNo);
        txtPackSlipCount = (TextView) rootView.findViewById(R.id.txtPackSlipCount);
        txtScanPackSlip = (TextView) rootView.findViewById(R.id.txtScanPackSlip);

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

        btnGo.setOnClickListener(this);
        btnCloseOne.setOnClickListener(this);
        btn_pending.setOnClickListener(this);
        btnLoad.setOnClickListener(this);

        spinnerSelectLoadList.setOnItemSelectedListener(this);
        lstTenants = new ArrayList<HouseKeepingDTO>();
        lstWarehouse = new ArrayList<HouseKeepingDTO>();

        common = new Common();
        core = new WMSCoreMessage();

        ProgressDialogUtils.closeProgressDialog();
        common.setIsPopupActive(false);

        tl = (TableLayout) rootView.findViewById(R.id.table);

        spinnerSelectTenant = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectTenant);
        spinnerSelectTenant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTenant = spinnerSelectTenant.getSelectedItem().toString();
                getTenantId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerSelectWH = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectWH);
        spinnerSelectWH.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedWH = spinnerSelectWH.getSelectedItem().toString();
                getWarehouseId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        rlLoadingOne.setVisibility(View.VISIBLE);
        rlLoadListTwo.setVisibility(View.GONE);

        getWarehouse();



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
            case R.id.btnGo:
                if (loadSheetNo.equalsIgnoreCase("")) {
                    common.showUserDefinedAlertType("No Pending Load Sheet Numbers", getActivity(), getContext(), "Warning");
                } else {
                    txtLoadSheetNo.setText(loadSheetNo);
                    GetSOCountUnderLoadSheet();
                    rlLoadingOne.setVisibility(View.GONE);
                    rlLoadListTwo.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btnCloseOne:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btn_pending:
                //FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                PendingPackSlipList();
                break;
            case R.id.btnLoad:
                if (txtScanPackSlip.getText().toString().isEmpty()) {
                    common.showUserDefinedAlertType("Please scan Packing Slip", getActivity(), getContext(), "Error");
                } else {
                    LoadPacking();
                }

                break;
        }
    }

    public void getTenantId() {

        for (HouseKeepingDTO oHouseKeeping : lstTenants) {
            if (oHouseKeeping.getTenantName().equals(selectedTenant)) {

                tenantId = oHouseKeeping.getTenantID();

            }
        }
    }

    public void getWarehouseId() {

        for (HouseKeepingDTO oHouseKeeping : lstWarehouse) {
            if (oHouseKeeping.getWarehouse().equals(selectedWH)) {

                warehouseId = oHouseKeeping.getWarehouseId();
                getTenants();
                if(!warehouseId.isEmpty()){
                    GetOpenLoadsheetList();
                }

            }
        }
    }

    public void getTenants() {

        try {


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.HouseKeepingDTO, getContext());
            HouseKeepingDTO houseKeepingDTO = new HouseKeepingDTO();
            houseKeepingDTO.setAccountID(accountId);
            houseKeepingDTO.setUserId(userId);
            houseKeepingDTO.setWarehouseId(warehouseId);
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
            houseKeepingDTO.setTenantID(tenantId);
            houseKeepingDTO.setUserId(userId);
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
                                spinnerSelectWH.setAdapter(liveStockAdapter);


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

    public void GetSOCountUnderLoadSheet() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setTenatID(userId);
            outbountDTO.setAccountID(accountId);
            outbountDTO.setLoadRefNo(txtLoadSheetNo.getText().toString());
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetSOCountUnderLoadSheet(message);
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
                                List<LinkedTreeMap<?, ?>> _lLoadSheetNo = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lLoadSheetNo = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<OutbountDTO> lstDto = new ArrayList<OutbountDTO>();

                                for (int i = 0; i < _lLoadSheetNo.size(); i++) {
                                    OutbountDTO dto = new OutbountDTO(_lLoadSheetNo.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                if (lstDto.size() > 0) {
                                    txtPackSlipCount.setText("Count: " + lstDto.get(0).getLoadCount());
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

    public void GetPackingCartonInfo(String ScannedData) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            final OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            outbountDTO.setTenatID(tenantId);
            outbountDTO.setWareHouseID(warehouseId);
            outbountDTO.setAccountID(accountId);
            outbountDTO.setSONumber(ScannedData);
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetPackingCartonInfo(message);
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
                                        recyclerViewPackInfo.setVisibility(View.VISIBLE);
                                        rv_packSLiplist.setVisibility(View.GONE);
                                        PackingInfoAdapter packingInfoAdapter = new PackingInfoAdapter(getActivity(), outbountDTOS);
                                        recyclerViewPackInfo.setAdapter(packingInfoAdapter);
                                        ProgressDialogUtils.closeProgressDialog();
                                    } else {
                                        rv_packSLiplist.setVisibility(View.GONE);
                                        common.showUserDefinedAlertType("No Data Found", getActivity(), getContext(), "Error");
                                        recyclerViewPackInfo.setAdapter(null);
                                    }

                                } else {
                                    common.showUserDefinedAlertType("No Data Found", getActivity(), getContext(), "Error");
                                    recyclerViewPackInfo.setAdapter(null);
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

        if (ProgressDialogUtils.isProgressActive() || Common.isPopupActive()) {
            common.showUserDefinedAlertType(errorMessages.EMC_082, getActivity(), getContext(), "Warning");
            return;
        }

        if (scannedData != null) {

            cvScanPackSlip.setCardBackgroundColor(getResources().getColor(R.color.white));
            ivScanPackSlip.setImageResource(R.drawable.check);
            txtScanPackSlip.setText(scannedData);

            GetPackingCartonInfo(scannedData);

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        loadSheetNo = spinnerSelectLoadList.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    public void GetOpenLoadsheetList() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setTenatID(userId);
            outbountDTO.setUserId(userId);
            outbountDTO.setWareHouseID(warehouseId);
            outbountDTO.setAccountID(accountId);
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetOpenLoadsheetList(message);
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
                                List<LinkedTreeMap<?, ?>> _lLoadSheetNo = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lLoadSheetNo = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<OutbountDTO> lstDto = new ArrayList<OutbountDTO>();
                                List<String> lstLoadSheetNo = new ArrayList<>();


                                for (int i = 0; i < _lLoadSheetNo.size(); i++) {
                                    OutbountDTO dto = new OutbountDTO(_lLoadSheetNo.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                for (int i = 0; i < lstDto.size(); i++) {
                                    lstLoadSheetNo.add(lstDto.get(i).getvLPDNumber());
                                }

                                ProgressDialogUtils.closeProgressDialog();
                                ArrayAdapter arrayAdapterLoadSheet = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstLoadSheetNo);
                                spinnerSelectLoadList.setAdapter(arrayAdapterLoadSheet);
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

    public void FetchInventoryForLoadSheet() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            outbountDTO.setSelectedLoadSheetNumber(loadSheetNo);
            message.setEntityObject(outbountDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.FetchInventoryForLoadSheet(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "006_01", getActivity());
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
                                ProgressDialogUtils.closeProgressDialog();

                                rlLoadingOne.setVisibility(View.GONE);
                                rlLoadListTwo.setVisibility(View.VISIBLE);
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "006_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "006_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();

                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "006_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();

            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
        }
    }

    public void ConfirmLoading() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setRSN(Materialcode);
            inventoryDTO.setReferenceDocumentNumber(txtLoadSheetNo.getText().toString().split("[-]", 2)[0]);
            //  inventoryDTO.setVehicleNumber(lblVehicleNo.getText().toString());
            String Qty = "1";
            inventoryDTO.setQuantity(Qty);
            message.setEntityObject(inventoryDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ConfirmLoading(message);
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
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    ProgressDialogUtils.closeProgressDialog();
                                    if (owmsExceptionMessage.isShowUserConfirmDialogue()) {
                                        DialogUtils.showConfirmDialog(getActivity(), "Confirm", owmsExceptionMessage.getWMSMessage().toString(), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:

                                                        break;
                                                    case DialogInterface.BUTTON_NEGATIVE:

                                                        break;
                                                }
                                            }
                                        });


                                        cvScanPackSlip.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPackSlip.setImageResource(R.drawable.warning_img);
                                        return;
                                    } else {
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        cvScanPackSlip.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPackSlip.setImageResource(R.drawable.warning_img);
                                    }
                                    ProgressDialogUtils.closeProgressDialog();
                                    return;
                                }

                            } else {

                                ProgressDialogUtils.closeProgressDialog();
                                List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<InventoryDTO> lstDto = new ArrayList<InventoryDTO>();
                                InventoryDTO dto = null;
                                if (_lInventory != null && _lInventory.size() > 0) {
                                    for (int i = 0; i < _lInventory.size(); i++) {

                                        dto = new InventoryDTO(_lInventory.get(i).entrySet());
                                        lstDto.add(dto);

                                    }
                                    cvScanPackSlip.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPackSlip.setImageResource(R.drawable.check);
                                    if (dto.getQuantity() != "0") {
                                        txtPackSlipCount.setText(String.valueOf(dto.getQuantity()));
                                    }
                                    // lblDesc.setText(dto.getMaterialShortDescription());
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

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
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
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
        }
    }

    public void RevertLoading() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            final InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setReferenceDocumentNumber(txtLoadSheetNo.getText().toString().split("[-]", 2)[0]);
            //inventoryDTO.setMaterialCode(lblScannedSku.getText().toString());
            inventoryDTO.setRSN(Materialcode);
            message.setEntityObject(inventoryDTO);
            if (IsUserConfirmedRedo) {
                inventoryDTO.setUserConfirmReDo(true);
            }

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.RevertLoading(message);
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
                                    ProgressDialogUtils.closeProgressDialog();

                                    if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_BL_008")) {
                                        IsUserConfirmedRedo = true;
                                        inventoryDTO.setQuantity("0");


                                    }
                                    if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_BL_009")) {
                                        etQty.setVisibility(View.VISIBLE);
                                        txtInputLayoutQty.setVisibility(View.VISIBLE);
                                    }
                                    if (owmsExceptionMessage.isShowUserConfirmDialogue()) {
                                        DialogUtils.showConfirmDialog(getActivity(), "Confirm", owmsExceptionMessage.getWMSMessage().toString(), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        if (inventoryDTO.getQuantity().equals("0")) {
                                                            RevertLoading();
                                                        }
                                                        break;
                                                    case DialogInterface.BUTTON_NEGATIVE:

                                                        break;
                                                }
                                            }
                                        });
                                        return;
                                    } else if (owmsExceptionMessage.isShowAsSuccess()) {
                                        cvScanPackSlip.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPackSlip.setImageResource(R.drawable.check);
                                        return;
                                    } else {
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    }
                                    cvScanPackSlip.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPackSlip.setImageResource(R.drawable.warning_img);
                                    return;
                                }
                            } else {
                                InventoryDTO oInventoryDTO = null;
                                List<LinkedTreeMap<?, ?>> _lLoadingList = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lLoadingList = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                for (int i = 0; i < _lLoadingList.size(); i++) {
                                    oInventoryDTO = new InventoryDTO(_lLoadingList.get(i).entrySet());
                                }
                                txtPackSlipCount.setText(oInventoryDTO.getQuantity());
                                cvScanPackSlip.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                ivScanPackSlip.setImageResource(R.drawable.fullscreen_img);
                                ProgressDialogUtils.closeProgressDialog();
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

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {

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

    public void LoadPacking() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            final OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setPackingSlipNo(txtScanPackSlip.getText().toString());
            outbountDTO.setLoadSheetNo(txtLoadSheetNo.getText().toString());
            outbountDTO.setUserId(userId);
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.LoadPacking(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "005_01", getActivity());
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
                                    if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_BL_008")) {
                                        IsUserConfirmedRedo = true;


                                    }
                                    if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_BL_009")) {
                                        etQty.setVisibility(View.VISIBLE);
                                        txtInputLayoutQty.setVisibility(View.VISIBLE);
                                    }
                                    if (owmsExceptionMessage.isShowUserConfirmDialogue()) {
                                        DialogUtils.showConfirmDialog(getActivity(), "Confirm", owmsExceptionMessage.getWMSMessage().toString(), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:

                                                        break;
                                                    case DialogInterface.BUTTON_NEGATIVE:

                                                        break;
                                                }
                                            }
                                        });
                                        return;
                                    } else if (owmsExceptionMessage.isShowAsSuccess()) {
                                        cvScanPackSlip.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPackSlip.setImageResource(R.drawable.check);
                                        return;
                                    } else {
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    }
                                    cvScanPackSlip.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPackSlip.setImageResource(R.drawable.warning_img);
                                    return;
                                }
                            } else {
                                OutbountDTO oOutboundDto = null;
                                List<LinkedTreeMap<?, ?>> _lLoadingList = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lLoadingList = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                for (int i = 0; i < _lLoadingList.size(); i++) {
                                    oOutboundDto = new OutbountDTO(_lLoadingList.get(i).entrySet());
                                }

                                if (oOutboundDto != null) {
                                    if (oOutboundDto.getStatus().equalsIgnoreCase("LOADING DONE")) {
                                        txtPackSlipCount.setText(oOutboundDto.getLoadCount());
                                        txtScanPackSlip.setText("");
                                        recyclerViewPackInfo.setAdapter(null);
                                        common.showUserDefinedAlertType(oOutboundDto.getStatus(), getActivity(), getActivity(), "Success");
                                    } else if (oOutboundDto.getStatus().equalsIgnoreCase("LOADING COMPLETED")) {
                                        txtPackSlipCount.setText(oOutboundDto.getLoadCount());
                                        txtScanPackSlip.setText("");
                                        common.showUserDefinedAlertType(oOutboundDto.getStatus(), getActivity(), getActivity(), "Success");
                                        recyclerViewPackInfo.setAdapter(null);
                                        rv_packSLiplist.setAdapter(null);
                                    } else {
                                        txtPackSlipCount.setText(oOutboundDto.getLoadCount());
                                        txtScanPackSlip.setText("");
                                        common.showUserDefinedAlertType(oOutboundDto.getStatus(), getActivity(), getActivity(), "Warning");
                                    }

                                }
                                cvScanPackSlip.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                ivScanPackSlip.setImageResource(R.drawable.fullscreen_img);
                                ProgressDialogUtils.closeProgressDialog();
                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "005_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "005_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "005_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
        }
    }


    public void PendingPackSlipList() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            final OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setLoadSheetNo(txtLoadSheetNo.getText().toString());
            outbountDTO.setUserId(userId);
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.PendingPackSlipList(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "005_01", getActivity());
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
                                    if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_BL_008")) {
                                        IsUserConfirmedRedo = true;


                                    }
                                    if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_BL_009")) {
                                        etQty.setVisibility(View.VISIBLE);
                                        txtInputLayoutQty.setVisibility(View.VISIBLE);
                                    }
                                    if (owmsExceptionMessage.isShowUserConfirmDialogue()) {
                                        DialogUtils.showConfirmDialog(getActivity(), "Confirm", owmsExceptionMessage.getWMSMessage().toString(), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:

                                                        break;
                                                    case DialogInterface.BUTTON_NEGATIVE:

                                                        break;
                                                }
                                            }
                                        });
                                        return;
                                    } else if (owmsExceptionMessage.isShowAsSuccess()) {
                                        cvScanPackSlip.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPackSlip.setImageResource(R.drawable.check);
                                        return;
                                    } else {
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    }
                                    cvScanPackSlip.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPackSlip.setImageResource(R.drawable.warning_img);
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
                                    ProgressDialogUtils.closeProgressDialog();
                                    if (outbountDTOS.size() > 0) {
                                        recyclerViewPackInfo.setVisibility(View.GONE);
                                        rv_packSLiplist.setVisibility(View.VISIBLE);
                                        PackiSlipListAdapter packiSlipListAdapter = new PackiSlipListAdapter(getActivity(), outbountDTOS);
                                        rv_packSLiplist.setAdapter(packiSlipListAdapter);
                                        ProgressDialogUtils.closeProgressDialog();
                                    } else {
                                        recyclerViewPackInfo.setVisibility(View.GONE);
                                        common.showUserDefinedAlertType("No pending packslips for this Load sheets", getActivity(), getContext(), "Error");
                                        rv_packSLiplist.setAdapter(null);
                                    }

                                } else {
                                    common.showUserDefinedAlertType("No pending packslips for this Load sheets", getActivity(), getContext(), "Error");
                                    recyclerViewPackInfo.setAdapter(null);
                                }

                            }

                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "005_02", getActivity());
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "005_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "005_04", getActivity());
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_loading));
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