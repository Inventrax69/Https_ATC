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
import com.inventrax.atc.pojos.InventoryDTO;
import com.inventrax.atc.pojos.OutbountDTO;
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
 * Created by Prasanna ch on 06/26/2018.
 */

public class StockTransferOrderPick extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_012";

    private View rootView;
    ImageView ivScanLocation, ivScanPallet, ivScanPalletTo, tvScanRSN, ivScanRSN, ivScanRSNnew;
    Button btnMaterialSkip, btnPick, btn_Skip, btnOk, btnCloseSkip, btnClosefinal, btnGo;
    TextView lblPickListNo, lblScannedSku;
    TextView lblSKuNo, lblLocationNo, lblMopNo, lblrsnNoNew, lblMfgDate, lblExpDate, lblProjectRefNo, lblassignedQty, lblserialNo, lblBatchNo;
    CardView cvScanPallet, cvScanPalletTo, cvScanRSN, cvScanNewRSN, cvScanLocation;
    EditText lblReceivedQty;
    boolean IsStrictlycomplaince = false;
    String Mcode = null, NewMcode = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    String userId = null, scanType = null, accountId = "";
    private Common common;
    private WMSCoreMessage core;
    boolean IsuserConfirmed = false, IsUsercanceled = false;
    private String pickOBDno = "", pickobdId = "";
    int count = 0;
    private ScanValidator scanValidator;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    EditText etPallet, etPalletTo;
    EditText et_oldrsn, et_printQty, et_newrsn, et_printerIP;
    boolean isValidLocation = false;
    boolean isPalletScanned = false;
    boolean pickValidateComplete = false;
    boolean isRSNScanned = false;
    String huNo = "", huSize = "", descreption = "", mop = "", assignedId = "", cartonID = "", KitId = "", materialMasterId = "", locationId = "", soDetailsId = "", Lineno = "", POSOHeaderId = "", sLoc = "";
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    RelativeLayout rlPickListHeader, rlPickList, rlSkip;

    String printer = "Select Printer", skipReason = "", pickedQty = "";
    List<String> lstTransferIds, lstTransferNos;
    SearchableSpinner spinnerSelectReason, spinnerSelectPickList;
    String transferReqNo = "", transferreqId = "", storageLocaId = "";
    OutbountDTO Return_oOutboundDTO;


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

    public StockTransferOrderPick() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_stock_transfer_picking, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    private void loadFormControls() {

        rlPickListHeader = (RelativeLayout) rootView.findViewById(R.id.rlPickListHeader);
        rlPickList = (RelativeLayout) rootView.findViewById(R.id.rlPickList);
        rlSkip = (RelativeLayout) rootView.findViewById(R.id.rlSkip);
        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanRSN = (CardView) rootView.findViewById(R.id.cvScanRSN);
        cvScanNewRSN = (CardView) rootView.findViewById(R.id.cvScanNewRSN);
        cvScanPalletTo = (CardView) rootView.findViewById(R.id.cvScanPalletTo);
        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanRSN = (ImageView) rootView.findViewById(R.id.ivScanRSN);
        ivScanPalletTo = (ImageView) rootView.findViewById(R.id.ivScanPalletTo);

        btnPick = (Button) rootView.findViewById(R.id.btnPick);
        btn_Skip = (Button) rootView.findViewById(R.id.btn_Skip);
        btnOk = (Button) rootView.findViewById(R.id.btnOk);
        btnCloseSkip = (Button) rootView.findViewById(R.id.btnCloseSkip);
        btnGo = (Button) rootView.findViewById(R.id.btnGo);

        IsuserConfirmed = false;
        lblPickListNo = (TextView) rootView.findViewById(R.id.lblPickListNo);
        lblSKuNo = (TextView) rootView.findViewById(R.id.lblSKUSuggested);
        lblLocationNo = (TextView) rootView.findViewById(R.id.lblLocationSuggested);

        etPallet = (EditText) rootView.findViewById(R.id.etPallet);

        etPalletTo = (EditText) rootView.findViewById(R.id.etPalletTo);

        lblReceivedQty = (EditText) rootView.findViewById(R.id.lblReceivedQty);
        lblMfgDate = (TextView) rootView.findViewById(R.id.lblMfgDate);
        lblExpDate = (TextView) rootView.findViewById(R.id.lblExpDate);
        lblProjectRefNo = (TextView) rootView.findViewById(R.id.lblProjectRefNo);
        lblserialNo = (TextView) rootView.findViewById(R.id.lblserialNo);
        lblBatchNo = (TextView) rootView.findViewById(R.id.lblBatchNo);
        lblassignedQty = (TextView) rootView.findViewById(R.id.lblRequiredQty);
        spinnerSelectReason = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectReason);
        spinnerSelectReason.setOnItemSelectedListener(this);


        spinnerSelectPickList = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectPickList);
        spinnerSelectPickList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                transferReqNo = parent.getItemAtPosition(position).toString();
                transferreqId = lstTransferIds.get(position).toString().split("[.]")[0];
            }

            // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent) {

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
        btnPick.setOnClickListener(this);
        btn_Skip.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        btnCloseSkip.setOnClickListener(this);
        btnGo.setOnClickListener(this);

        common = new Common();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();
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

        Return_oOutboundDTO = new OutbountDTO();

        GetTransferReqNos();

        if (scanType.equalsIgnoreCase("Auto")) {
            lblReceivedQty.setText("1");
        } else {
            lblReceivedQty.setEnabled(true);
        }


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnPick:
                UpsertPickItem();
                break;

            case R.id.btn_Skip:
                SkipItem();
                break;

            case R.id.btnOk:
                OBDSkipItem();
                break;

            case R.id.btnCloseSkip:
                rlPickList.setVisibility(View.VISIBLE);
                rlSkip.setVisibility(View.GONE);
                break;

            case R.id.btnGo:

                if (transferReqNo.equalsIgnoreCase("")) {
                    common.showUserDefinedAlertType(errorMessages.EMC_0036, getActivity(), getContext(), "Warning");
                } else {
                    GetPickItem();
                }
                break;

        }
    }

    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {

        if(((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).isDrawerOpen(GravityCompat.START)){
            return;
        }

        if (common.isPopupActive()) {

        } else if (scannedData != null && !common.isPopupActive()) {

            if (scanValidator.isContainerScanned(scannedData)) {
                if (isValidLocation) {
                    if (!isPalletScanned) {
                        //  etPallet.setText(scannedData);
                        if (!etPallet.getText().toString().isEmpty() && etPallet.getText().toString().equalsIgnoreCase(scannedData)) {
                            isPalletScanned = true;
                            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                            ivScanPallet.setImageResource(R.drawable.check);
                        } else {
                            common.showUserDefinedAlertType(errorMessages.EMC_0009, getActivity(), getContext(), "Warning");
                            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.red));
                            ivScanPallet.setImageResource(R.drawable.warning_img);
                        }
                    } else {
                        etPalletTo.setText(scannedData);
                        ValidatePalletCode(etPalletTo.getText().toString(), "ToPallet");
                    }
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Warning");
                }
            } else if (ScanValidator.isItemScanned(scannedData)) {

                /* ----For RSN reference----
               0 Sku|1 Qty|2 husize|3 InBoundQty|4 BatchNo|5 RSN|6 MFGDate|7 EXpDate|8 ProjectRefNO|9 SerialNO|10 lneNi|11 Huno*/

                //ToyCar||||||0|001
                if (isValidLocation) {
                    //validate Picking rsn
                    if (!lblSKuNo.getText().toString().isEmpty() && lblSKuNo.getText().toString().equalsIgnoreCase(scannedData.split("[|]")[0].toString())) {

                        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                        ivScanRSN.setImageResource(R.drawable.check);
                        isPalletScanned = true;

                        if (scanType.equalsIgnoreCase("Auto")) {
                            lblReceivedQty.setText("1");
                            UpsertPickItem();
                        } else {
                            lblReceivedQty.setEnabled(true);
                        }
                    } else {
                        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.red));
                        ivScanRSN.setImageResource(R.drawable.warning_img);
                        common.showUserDefinedAlertType(errorMessages.EMC_0029, getActivity(), getContext(), "Error");
                    }
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Warning");
                }
            } else if (scanValidator.isLocationScanned(scannedData)) {
                if (!lblLocationNo.getText().toString().isEmpty() && lblLocationNo.getText().toString().equalsIgnoreCase(scannedData)) {
                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                    ivScanLocation.setImageResource(R.drawable.check);
                    isValidLocation = true;
                } else {
                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.red));
                    ivScanLocation.setImageResource(R.drawable.warning_img);
                    common.showUserDefinedAlertType(errorMessages.EMC_0033, getActivity(), getContext(), "Warning");
                }
            }
        } else {
            common.showUserDefinedAlertType(errorMessages.EMC_0030, getActivity(), getContext(), "Error");
        }
    }


    public void GetTransferReqNos() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO outbountDTO = new InventoryDTO();
            outbountDTO.setAccountId(accountId);
            message.setEntityObject(outbountDTO);
            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetTransferReqNos(message);
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
                                }
                                DialogUtils.showAlertDialog(getActivity(), owmsExceptionMessage.getWMSMessage());
                            } else {
                                List<LinkedTreeMap<?, ?>> _lPickRefNo = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lPickRefNo = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                lstTransferIds = new ArrayList<>();
                                lstTransferNos = new ArrayList<>();
                                List<InventoryDTO> lstDto = new ArrayList<InventoryDTO>();
                                for (int i = 0; i < _lPickRefNo.size(); i++) {
                                    InventoryDTO dto = new InventoryDTO(_lPickRefNo.get(i).entrySet());
                                    lstDto.add(dto);
                                }
                                for (int i = 0; i < lstDto.size(); i++) {
                                    lstTransferIds.add(String.valueOf(lstDto.get(i).getTransferRefId()));
                                    lstTransferNos.add(String.valueOf(lstDto.get(i).getTransferRefNo()));
                                }

                                if (lstTransferNos == null) {
                                    ProgressDialogUtils.closeProgressDialog();
                                    DialogUtils.showAlertDialog(getActivity(), "Transfer Request is null");
                                } else {
                                  /*  lstTransferNos.add(0, "lstTransferNos");
                                    lstTransferIds.add(0, "0");*/

                                    ProgressDialogUtils.closeProgressDialog();
                                    ArrayAdapter arrayAdapterPickList = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstTransferNos);
                                    spinnerSelectPickList.setAdapter(arrayAdapterPickList);
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


    public void OBDSkipItem() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            final OutbountDTO outbountDTO = new OutbountDTO();
            OutbountDTO oOutboundDTO = new OutbountDTO();
            oOutboundDTO.setUserId(userId);
            oOutboundDTO.setSkipReason(skipReason);
            oOutboundDTO.setSKU(lblSKuNo.getText().toString());
            oOutboundDTO.setSerialNo(lblserialNo.getText().toString());
            oOutboundDTO.setMfgDate(lblMfgDate.getText().toString());
            oOutboundDTO.setExpDate(lblExpDate.getText().toString());
            oOutboundDTO.setBatchNo(lblBatchNo.getText().toString());
            oOutboundDTO.setProjectNo(lblProjectRefNo.getText().toString());
            oOutboundDTO.setvLPDId("0");
            oOutboundDTO.setTransferRequestId(transferreqId);
            oOutboundDTO.setLocation(lblLocationNo.getText().toString());
            oOutboundDTO.setPalletNo(etPallet.getText().toString());
            oOutboundDTO.setSkipQty(pickedQty);
            oOutboundDTO.setPickedQty(lblReceivedQty.getText().toString());
            oOutboundDTO.setAssignedID(assignedId);
            oOutboundDTO.setsLoc(sLoc);
            message.setEntityObject(oOutboundDTO);
            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method2
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.OBDSkipItem(message);
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
                                    ClearFields();
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04")) {
                                        ProgressDialogUtils.closeProgressDialog();
                                        // Clearfields();
                                    }
                                } else {
                                    //Response object Success
                                    List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                    OutbountDTO oOutboundDTO = null;
                                    for (int i = 0; i < _lstPickitem.size(); i++) {
                                        oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                    }
                                    sLoc = "" + oOutboundDTO.getsLoc();
                                    POSOHeaderId = "" + oOutboundDTO.getpOSOHeaderId();
                                    Lineno = "" + oOutboundDTO.getLineno();
                                    lblSKuNo.setText(oOutboundDTO.getSKU());
                                    assignedId = "" + oOutboundDTO.getAssignedID();
                                    soDetailsId = "" + oOutboundDTO.getSODetailsID();
                                    KitId = "" + oOutboundDTO.getAssignedID();
                                    lblBatchNo.setText(oOutboundDTO.getBatchNo());
                                    lblLocationNo.setText(oOutboundDTO.getLocation());
                                    etPallet.setText(oOutboundDTO.getPalletNo());
                                    cartonID = "" + oOutboundDTO.getCartonID();
                                    lblassignedQty.setText(oOutboundDTO.getAssignedQuantity());
                                    lblMfgDate.setText(oOutboundDTO.getMfgDate());
                                    lblExpDate.setText(oOutboundDTO.getExpDate());
                                    lblProjectRefNo.setText(oOutboundDTO.getProjectNo());
                                    lblserialNo.setText(oOutboundDTO.getSerialNo());
                                    rlPickList.setVisibility(View.VISIBLE);
                                    rlSkip.setVisibility(View.GONE);
                                    ProgressDialogUtils.closeProgressDialog();
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

        isPalletScanned = false;
        isValidLocation = false;
        isRSNScanned = false;
        pickValidateComplete = false;

    }


    public void GetPickItem() {
        //To get Picked item Details
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            final OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            outbountDTO.setvLPDId("0");
            outbountDTO.setTransferRequestId(transferreqId);
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
                                    if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04")) {
                                        ProgressDialogUtils.closeProgressDialog();
                                        // Clearfields();
                                    }
                                } else {
                                    //Response object Success
                                    List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                    OutbountDTO oOutboundDTO = null;
                                    for (int i = 0; i < _lstPickitem.size(); i++) {
                                        oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                    }

                                    rlPickListHeader.setVisibility(View.GONE);
                                    rlPickList.setVisibility(View.VISIBLE);
                                    lblPickListNo.setText(transferReqNo);
                                    sLoc = "" + oOutboundDTO.getsLoc();
                                    POSOHeaderId = "" + oOutboundDTO.getpOSOHeaderId();
                                    Lineno = "" + oOutboundDTO.getLineno();
                                    lblSKuNo.setText(oOutboundDTO.getSKU());
                                    assignedId = "" + oOutboundDTO.getAssignedID();
                                    soDetailsId = "" + oOutboundDTO.getSODetailsID();
                                    KitId = "" + oOutboundDTO.getAssignedID();
                                    materialMasterId = "" + oOutboundDTO.getMaterialMasterId();
                                    locationId = "" + oOutboundDTO.getLocationId();
                                    storageLocaId = "" + oOutboundDTO.getsLocId();
                                    lblBatchNo.setText(oOutboundDTO.getBatchNo());
                                    lblLocationNo.setText(oOutboundDTO.getLocation());
                                    etPallet.setText(oOutboundDTO.getPalletNo());
                                    cartonID = "" + oOutboundDTO.getCartonID();
                                    pickedQty = oOutboundDTO.getPickedQty();
                                    lblassignedQty.setText(oOutboundDTO.getPickedQty() + "/" + oOutboundDTO.getPendingQty());
                                    lblMfgDate.setText(oOutboundDTO.getMfgDate());
                                    lblExpDate.setText(oOutboundDTO.getExpDate());
                                    lblProjectRefNo.setText(oOutboundDTO.getProjectNo());
                                    lblserialNo.setText(oOutboundDTO.getSerialNo());

                                    Return_oOutboundDTO = oOutboundDTO;
                                    ProgressDialogUtils.closeProgressDialog();
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


    public void ValidatePalletCode(String pallet, final String Palletfrom) {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO outbountDTO = new InboundDTO();
            outbountDTO.setPalletNo(pallet);
            message.setEntityObject(outbountDTO);
            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(getActivity()).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.CheckContainer(message);
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


                                    cvScanPalletTo.setCardBackgroundColor(getResources().getColor(R.color.red));
                                    ivScanPalletTo.setImageResource(R.drawable.warning_img);

                                    etPalletTo.setText("");

                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                }

                            } else {
                                List<LinkedTreeMap<?, ?>> _lPalletInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lPalletInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                if (_lPalletInventory != null) {
                                    if (_lPalletInventory.size() > 0) {

                                        cvScanPalletTo.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPalletTo.setImageResource(R.drawable.check);
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

    public void SkipItem() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO outbountDTO = new InboundDTO();
            outbountDTO.setSkipType("2");
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

                                if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_IN_DAL_001")) {
                                    //Clearfields();
                                    GetPickItem();
                                }
                                //  btnPick.setEnabled(true);
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
                            //  btnPick.setEnabled(true);
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


    public void UpsertPickItem() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO oOutboundDTO = new OutbountDTO();
            Return_oOutboundDTO = oOutboundDTO;
            oOutboundDTO.setUserId(userId);
            oOutboundDTO.setSKU(lblSKuNo.getText().toString());
            oOutboundDTO.setKitId(KitId);
            oOutboundDTO.setCartonNo(etPallet.getText().toString());
            oOutboundDTO.setSerialNo(lblserialNo.getText().toString());
            oOutboundDTO.setMfgDate(lblMfgDate.getText().toString());
            oOutboundDTO.setExpDate(lblExpDate.getText().toString());
            oOutboundDTO.setBatchNo(lblBatchNo.getText().toString());
            oOutboundDTO.setProjectNo(lblProjectRefNo.getText().toString());
            oOutboundDTO.setAssignedID(assignedId);
            oOutboundDTO.setoBDNo(pickOBDno);
            oOutboundDTO.setCartonID(cartonID);
            oOutboundDTO.setvLPDId("0");
            oOutboundDTO.setOutboundID("0");
            oOutboundDTO.setsLocId(storageLocaId);
            oOutboundDTO.setTransferRequestId(transferreqId);
            oOutboundDTO.setLocation(lblLocationNo.getText().toString());
            oOutboundDTO.setPalletNo(etPallet.getText().toString());
            oOutboundDTO.setPickedQty(lblReceivedQty.getText().toString());
            oOutboundDTO.setAssignedID(assignedId);
            oOutboundDTO.setToCartonNo(etPalletTo.getText().toString());
            oOutboundDTO.setSODetailsID(soDetailsId);
            oOutboundDTO.setLineno(Lineno);
            oOutboundDTO.setpOSOHeaderId(POSOHeaderId);
            oOutboundDTO.setMaterialMasterId(materialMasterId);
            oOutboundDTO.setLocationId(locationId);
            oOutboundDTO.setHasDis("0");
            oOutboundDTO.setIsDam("0");
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
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    ProgressDialogUtils.closeProgressDialog();
                                }

                                if (isRSNScanned) {
                                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanRSN.setImageResource(R.drawable.warning_img);
                                }

                                isRSNScanned = false;
                                cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanRSN.setImageResource(R.drawable.warning_img);

                                //addded on 19-12-2018  19:07   need to unit test because of no handheld
                                lblReceivedQty.setText("");
                                lblReceivedQty.setEnabled(false);
                                //    btnPick.setEnabled(false);

                                common.setIsPopupActive(true);
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_IN_DAL_001")) {
                                    //Clearfields();
                                    // GetPickItem();
                                }
                            } else {

                                List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                OutbountDTO oOutboundDTO = null;
                                for (int i = 0; i < _lstPickitem.size(); i++) {
                                    oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                    _lstOutboundDTO.add(oOutboundDTO);
                                }
                                for (int i = 0; i < _lstPickitem.size(); i++) {
                                    oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                    _lstOutboundDTO.add(oOutboundDTO);

                                }

                                etPalletTo.setText("");
                                lblReceivedQty.setText("");
                                lblReceivedQty.setEnabled(false);
                                //  btnPick.setEnabled(false);
                                if (isPalletScanned) {
                                    isPalletScanned = false;
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.fullscreen_img);
                                }

                                cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                ivScanRSN.setImageResource(R.drawable.fullscreen_img);

                                SoundUtils soundUtils = new SoundUtils();

                                soundUtils.alertSuccess(getActivity(), getActivity());
                                DialogUtils.showAlertDialog(getActivity(), "Success", "PIck Item Is Successful", R.drawable.success, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                // setIsPopupActive(false);
                                                GetPickItem();
                                                break;
                                        }
                                    }
                                });
                                ProgressDialogUtils.closeProgressDialog();
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.menu_internalTransfer));
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