package com.inventrax.atc.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.inventrax.atc.R;
import com.inventrax.atc.common.Common;
import com.inventrax.atc.common.constants.EndpointConstants;
import com.inventrax.atc.common.constants.ErrorMessages;
import com.inventrax.atc.interfaces.ApiInterface;
import com.inventrax.atc.pojos.EntryDTO;
import com.inventrax.atc.pojos.InboundDTO;
import com.inventrax.atc.pojos.StorageLocationDTO;
import com.inventrax.atc.pojos.WMSCoreMessage;
import com.inventrax.atc.pojos.WMSExceptionMessage;
import com.inventrax.atc.searchableSpinner.SearchableSpinner;
import com.inventrax.atc.services.RetrofitBuilderHttpsEx;
import com.inventrax.atc.util.DialogUtils;
import com.inventrax.atc.util.ExceptionLoggerUtils;
import com.inventrax.atc.util.FragmentUtils;
import com.inventrax.atc.util.NetworkUtils;
import com.inventrax.atc.util.ProgressDialogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Padmaja.B on 20/12/2018.
 */

public class UnloadingFragment extends Fragment implements View.OnClickListener {

    private View rootView;
    private static final String classCode = "API_FRAG_UNLOADING";

    private TextView txtSelectStRef;
    private SearchableSpinner spinnerSelectStRef, spinnerSelectVehicle;
    private Button btnGo;

    private Gson gson;
    private WMSCoreMessage core;
    private String Storerefno = null;
    List<List<StorageLocationDTO>> sloc;
    List<String> lstStorageloc;
    List<InboundDTO> lstInbound = null;
    private Common common;
    String userId = null, scanType = null, accountId = null, inboundId = null, invoiceQty = null, receivedQty = "";
    String DefaultSloc = null;
    List<List<EntryDTO>> lstEntry;

    List<EntryDTO> entryDTOList;

    List<String> vehicles;
    String vehilceNo = "", dock = "";

    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (NetworkUtils.isInternetAvailable(getContext())) {
            rootView = inflater.inflate(R.layout.fragment_unloading, container, false);
            loadFormControls();
        } else {
            DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            return rootView;
        }
        return rootView;
    }

    public void loadFormControls() {

        if (NetworkUtils.isInternetAvailable(getContext())) {

            txtSelectStRef = (TextView) rootView.findViewById(R.id.tvSelectStRef);

            spinnerSelectStRef = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectStRef);
            spinnerSelectStRef.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Storerefno = spinnerSelectStRef.getSelectedItem().toString();

                    // getting values as per the selected store ref number
                    getInboundId();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            spinnerSelectVehicle = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectVehicle);
            spinnerSelectVehicle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    vehilceNo = "";

                    vehilceNo = spinnerSelectVehicle.getSelectedItem().toString();

                    for (List<EntryDTO> entryDTO : lstEntry) {

                        for (int k = 0; k < entryDTO.size(); k++) {

                            if (entryDTO.get(k).getVehicleNumber().equals(vehilceNo)) {

                                dock = entryDTO.get(k).getDockNumber();

                            }
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            btnGo = (Button) rootView.findViewById(R.id.btnGo);
            btnGo.setOnClickListener(this);

            gson = new GsonBuilder().create();
            core = new WMSCoreMessage();
            sloc = new ArrayList<>();
            lstStorageloc = new ArrayList<String>();
            entryDTOList = new ArrayList<EntryDTO>();

            SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
            userId = sp.getString("RefUserId", "");
            scanType = sp.getString("scanType", "");
            accountId = sp.getString("AccountId", "");

            common = new Common();
            exceptionLoggerUtils = new ExceptionLoggerUtils();
            errorMessages = new ErrorMessages();
            lstInbound = new ArrayList<InboundDTO>();

            ProgressDialogUtils.closeProgressDialog();
            common.setIsPopupActive(false);

            LoadInbounddetails();

        } else {
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0025);
            // soundUtils.alertSuccess(LoginActivity.this,getBaseContext());
            return;
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnGo:
                if (Storerefno != null) {
                    // passing values to the next screen
                    GetInboundDeatils();
                }
                break;

            default:
                break;
        }
    }

    public void GetInboundDeatils() {

        Bundle bundle = new Bundle();
        bundle.putString("Storefno", Storerefno);
        bundle.putString("inboundId", inboundId);
        bundle.putString("invoiceQty", invoiceQty);
        bundle.putString("receivedQty", receivedQty);
        bundle.putString("dock", dock);
        bundle.putString("vehilceNo", vehilceNo);
        // String SLOCjson = gson.toJson(lstStorageloc);
        //bundle.putString("SLOC", SLOCjson);

        GoodsInFragment goodsinfragment = new GoodsInFragment();
        goodsinfragment.setArguments(bundle);
        FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, goodsinfragment);

    }

    public void LoadInbounddetails() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setUserId(userId);
            inboundDTO.setAccountID(accountId);
            message.setEntityObject(inboundDTO);

            // Log.v("ABCDE",new Gson().toJson(message));

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
                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                            } else {

                                List<LinkedTreeMap<?, ?>> _lstUnloading = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstUnloading = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<InboundDTO> lstDto = new ArrayList<InboundDTO>();
                                List<String> _lstINBNames = new ArrayList<>();
                                List<String> _lstInboundId = new ArrayList<>();

                                for (int i = 0; i < _lstUnloading.size(); i++) {
                                    InboundDTO dto = new InboundDTO(_lstUnloading.get(i).entrySet());
                                    lstDto.add(dto);
                                    lstInbound.add(dto);
                                }

                                for (int i = 0; i < lstDto.size(); i++) {
                                    _lstINBNames.add(lstDto.get(i).getStoreRefNo());
                                }


                                ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, _lstINBNames);
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

    public void getInboundId() {

        vehicles = new ArrayList<>();
        lstEntry = new ArrayList<>();

        for (InboundDTO oInbound : lstInbound) {
            if (oInbound.getStoreRefNo().equals(Storerefno)) {

                inboundId = oInbound.getInboundID();
                invoiceQty = oInbound.getInvoiceQty();
                receivedQty = oInbound.getReceivedQty();
                lstEntry.add(oInbound.getEntry());                 // to get vehicle numbers and dock values

                for (int x = 0; x < lstEntry.size(); x++) {
                    for (int y = 0; y < lstEntry.get(x).size(); y++) {
                        vehicles.add(lstEntry.get(x).get(y).getVehicleNumber());     // list of vehicles assigned to the st. ref no
                    }
                }

                ArrayAdapter arrayAdapter = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, vehicles);
                spinnerSelectVehicle.setAdapter(arrayAdapter);

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
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    ProgressDialogUtils.closeProgressDialog();
                                    return;
                                }
                            } else {
                                ProgressDialogUtils.closeProgressDialog();
                                LinkedTreeMap<String, String> _lResultvalue = new LinkedTreeMap<String, String>();
                                _lResultvalue = (LinkedTreeMap<String, String>) core.getEntityObject();
                                for (Map.Entry<String, String> entry : _lResultvalue.entrySet()) {
                                    if (entry.getKey().equals("Result")) {
                                        String Result = entry.getValue();
                                        if (Result.equals("0")) {
                                            return;
                                        } else {
                                            exceptionLoggerUtils.deleteFile(getActivity());
                                            ProgressDialogUtils.closeProgressDialog();
                                            return;
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {

                            /*try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002",getContext());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            logException();*/

                            ProgressDialogUtils.closeProgressDialog();
                            Log.d("Message", core.getEntityObject().toString());
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

                // Toast.makeText(LoginActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
        }
    }


/*
    public void captureImage(){

        if (!CameraUtils.isDeviceSupportCamera(getContext())) {
            DialogUtils.showAlertDialog(getActivity(), "This device is not supported for camera");
            return;
        }
        else {
            SelectedPictureType = PictureType.Unload.toString();
            CameraUtils.captureImage(fragment);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // if the result is capturing Image
        if (requestCode == CameraUtils.CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK) {

                // successfully captured the image
                processImage();

            } else if (resultCode == getActivity().RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getActivity().getApplicationContext(), "User cancelled image capture", Toast.LENGTH_SHORT).show();
            } else {
                // failed to capture image
                Toast.makeText(getActivity().getApplicationContext(), "Sorry! Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }

    }
*/
  /*  private void processImage() {

        try {

            if (CameraUtils.fileUri != null) {

                String filePath = CameraUtils.compressImage(CameraUtils.fileUri.getPath(), getContext());

                File file = new File(CameraUtils.fileUri.getPath());

                Bitmap bitmapImage = BitmapFactory.decodeFile(filePath);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                if (PictureType.Unload.toString() == SelectedPictureType) {

                    unloadImage.setFileName(file.getName());
                    unloadImage.setImageData(encodedImage);
                    unloadImage.setType("JPEG");

                    txtCapturedImageDetails.setText(filePath);
                    txtCapturedImageDetails.setVisibility(TextView.VISIBLE);

                }


                //FileUtils.delete(CameraUtils.fileUri.getPath());
               */
  /* FileUtils.forceDelete(CameraUtils.getStoredImagesDirectory());
                FileUtils.forceDelete(CameraUtils.getCompressedImagesDirectory());


            }
        } catch (Exception ex) {
            DialogUtils.showAlertDialog(getActivity(), "Error while capturing picture");
            return;
        }

    }*/

}

