package com.inventrax.atc.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.inventrax.atc.R;
import com.inventrax.atc.common.Common;
import com.inventrax.atc.common.constants.EndpointConstants;
import com.inventrax.atc.common.constants.ErrorMessages;
import com.inventrax.atc.interfaces.ApiInterface;
import com.inventrax.atc.pojos.OutbountDTO;
import com.inventrax.atc.pojos.WMSCoreMessage;
import com.inventrax.atc.pojos.WMSExceptionMessage;
import com.inventrax.atc.services.RetrofitBuilderHttpsEx;
import com.inventrax.atc.util.DialogUtils;
import com.inventrax.atc.util.ExceptionLoggerUtils;
import com.inventrax.atc.util.ProgressDialogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadSheetOBDpendingAdapter extends RecyclerView.Adapter {

    private static final String classCode = "API_FRAG_009";
    private List<OutbountDTO> outbountDTOList;
    Context context;
    Button btnCreate;
    private Common common;
    private WMSCoreMessage core;
    EditText lblDrName,lblDrNo,lblVehicleNo,lblVehicleType;
    String ObdSting="";
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private Gson gson;
    String userId = null, scanType = null,accountId = "";
    RelativeLayout rlLoadingOne,rlLoadingTwo,rlLoadListThree;
    TextView lblLoadSheetNo;

    public LoadSheetOBDpendingAdapter(Context context,Button btnCreate, List<OutbountDTO> list,EditText lblDrName,EditText lblDrNo, EditText lblVehicleNo,EditText lblVehicleType,TextView lblLoadSheetNo
    ,RelativeLayout rlLoadingOne,RelativeLayout rlLoadingTwo,RelativeLayout rlLoadListThree) {
        this.context = context;
        this.outbountDTOList = list;
        this.btnCreate=btnCreate;
        common = new Common();
        core= new WMSCoreMessage();
        gson=new Gson();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();
        this.lblDrName=lblDrName;
        this.lblDrNo=lblDrNo;
        this.lblVehicleNo=lblVehicleNo;
        this.lblVehicleType=lblVehicleType;
        this.lblLoadSheetNo=lblLoadSheetNo;
        this.rlLoadingOne=rlLoadingOne;
        this.rlLoadingTwo=rlLoadingTwo;
        this.rlLoadListThree=rlLoadListThree;

        SharedPreferences sp =(context).getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        scanType = sp.getString("scanType", "");
        accountId = sp.getString("AccountId", "");
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView sno;// init the item view's
        CheckBox obd_name_check;

        public MyViewHolder(View itemView) {
            super(itemView);
            // get the reference of item view's
            sno = (TextView) itemView.findViewById(R.id.sno);
            obd_name_check = (CheckBox) itemView.findViewById(R.id.obd_name_check);

        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_sheet_obd_list, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

         OutbountDTO outbountDTO = (OutbountDTO) outbountDTOList.get(position);
        ((MyViewHolder) holder).sno.setText(""+(position+1));
        ((MyViewHolder) holder).obd_name_check.setText(""+outbountDTO.getOBDNumber());

        if(outbountDTO.isChecked()){
            ((MyViewHolder) holder).obd_name_check.setChecked(true);
        }else{
            ((MyViewHolder) holder).obd_name_check.setChecked(false);
        }


        ((MyViewHolder) holder).obd_name_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                outbountDTOList.get(position).setChecked(isChecked);
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lblDrName.getText().toString().isEmpty()){
                    common.showUserDefinedAlertType("Enter Driver Name", (Activity)context, context, "Warning");
                    return;
                }
                if(lblDrNo.getText().toString().isEmpty()){
                    common.showUserDefinedAlertType("Enter Driver number", (Activity)context, context, "Warning");
                    return;
                }
                if(lblVehicleNo.getText().toString().isEmpty()){
                    common.showUserDefinedAlertType("Enter Vehicle number", (Activity)context, context, "Warning");
                    return;
                }
                if(lblVehicleType.getText().toString().isEmpty()){
                    common.showUserDefinedAlertType("Enter Vehicle Type", (Activity)context, context, "Warning");
                    return;
                }

                ObdSting="";
                for(int i=0;i<outbountDTOList.size();i++){
                    if(outbountDTOList.get(i).isChecked()){
                        ObdSting+=outbountDTOList.get(i).getOBDNumber()+",";
                    }
                }

                if(ObdSting.isEmpty()){
                    common.showUserDefinedAlertType("Please check aleast one obd number", (Activity)context, context, "Warning");
                    return;
                }else{
                    ObdSting = ObdSting.substring(0, ObdSting.length() - 1);
                }

                UpsertLoadCreated(ObdSting) ;




/*                rlLoadingOne.setVisibility(View.GONE);
                rlLoadingTwo.setVisibility(View.GONE);
                rlLoadListThree.setVisibility(View.VISIBLE);*/
                
            }
        });


        // set the data in items

/*        ((MyViewHolder) holder).txtSloc.setText(inventoryDTO.getSLOC());
        ((MyViewHolder) holder).txtQty.setText(inventoryDTO.getQuantity().split("[.]")[0]);*/

    }

    public void logException() {
        try {

            String textFromFile = exceptionLoggerUtils.readFromFile((Activity)context);

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Exception, (Activity)context);
            WMSExceptionMessage wmsExceptionMessage = new WMSExceptionMessage();
            wmsExceptionMessage.setWMSMessage(textFromFile);
            message.setEntityObject(wmsExceptionMessage);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(context).create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.LogException(message);
                // } else {
                // DialogUtils.showAlertDialog((Activity)context, "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_01",(Activity)context);
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog((Activity)context, errorMessages.EMC_0002);
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
                                    common.showAlertType(owmsExceptionMessage, (Activity)context,context);
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
                                            exceptionLoggerUtils.deleteFile((Activity)context);
                                            return;
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {

                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_02",(Activity)context);
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
                        DialogUtils.showAlertDialog((Activity)context, errorMessages.EMC_0001);
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_03",(Activity)context);
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog((Activity)context, errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_04",(Activity)context);
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog((Activity)context, errorMessages.EMC_0003);
        }
    }

    public  void UpsertLoadCreated(String ObdSting) {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound,context);
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            outbountDTO.setTenatID(userId);
            outbountDTO.setVehicle(lblVehicleNo.getText().toString());
            outbountDTO.setOBDNumber(ObdSting);
            outbountDTO.setDriverNo(lblDrNo.getText().toString());
            outbountDTO.setDriverName(lblDrName.getText().toString());
            outbountDTO.setLRnumber(lblVehicleType.getText().toString());
            message.setEntityObject(outbountDTO);


            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(context).create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.UpsertLoadCreated(message);
                // } else {
                // DialogUtils.showAlertDialog((Activity)context, "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_01",(Activity)context);
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog((Activity)context, errorMessages.EMC_0002);

            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                            List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                            _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                            List<OutbountDTO> lstDto = new ArrayList<OutbountDTO>();

                            for (int i = 0; i < _lResult.size(); i++) {
                                OutbountDTO dto = new OutbountDTO(_lResult.get(i).entrySet());
                                lstDto.add(dto);
                            }

                            if(lstDto.size()>0){

                                lblLoadSheetNo.setText(lstDto.get(0).getResult());
                                rlLoadingOne.setVisibility(View.GONE);
                                rlLoadingTwo.setVisibility(View.GONE);
                                rlLoadListThree.setVisibility(View.VISIBLE);

                            }else{
                                common.showUserDefinedAlertType("Load No. Not Created", (Activity)context, context, "Warning");
                            }

                            ProgressDialogUtils.closeProgressDialog();
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_02",(Activity)context);
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
                        DialogUtils.showAlertDialog((Activity)context, errorMessages.EMC_0001);
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_03",(Activity)context);
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog((Activity)context, errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_04",(Activity)context);
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog((Activity)context, errorMessages.EMC_0003);
        }
    }


    @Override
    public int getItemCount() {
        return outbountDTOList.size();
    }

}
