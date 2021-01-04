package com.inventrax.atc.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.inventrax.atc.R;
import com.inventrax.atc.common.Common;
import com.inventrax.atc.common.constants.EndpointConstants;
import com.inventrax.atc.common.constants.ErrorMessages;
import com.inventrax.atc.interfaces.ApiInterface;
import com.inventrax.atc.model.Model;
import com.inventrax.atc.pojos.OutbountDTO;
import com.inventrax.atc.pojos.WMSCoreMessage;
import com.inventrax.atc.pojos.WMSExceptionMessage;
import com.inventrax.atc.services.RetrofitBuilderHttpsEx;
import com.inventrax.atc.util.ExceptionLoggerUtils;
import com.inventrax.atc.util.ProgressDialogUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeletePickedItemAdapter extends RecyclerView.Adapter<DeletePickedItemAdapter.ViewHolder> {


    public ArrayList<Model> item_list;
    private Context context;
    private Common common;
    private WMSCoreMessage core;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private static final String classCode = "API_FRAG_004";
    public static String POSITIVE_BUTTON_TEXT = "OK";
    public static String NEGATIVE_BUTTON_TEXT = "Cancel";
    public static String NEUTRAL_BUTTON_TEXT = "Cancel";
    private static android.support.v7.app.AlertDialog.Builder alertDialog;
    private Gson gson;
    private String userId = null, accountId = null, OBDId = null,VLPDId = null, mCode = null;
    private FragmentActivity fragmentActivity;


    public DeletePickedItemAdapter(ArrayList<Model> arrayList, Context context, String OBDId, String VLPDId,
                                   String mCode, FragmentActivity fragmentActivity) {

        this.context = context;
        item_list = arrayList;
        this.OBDId = OBDId;
        this.VLPDId = VLPDId;
        this.mCode = mCode;
        this.fragmentActivity = fragmentActivity;
    }

    @Override
    public DeletePickedItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        SharedPreferences sp = context.getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        accountId = sp.getString("AccountId", "");
        // create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.delete_item_row, null);

        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DeletePickedItemAdapter.ViewHolder holder, int position) {

        final int pos = position;

        common = new Common();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();
        gson = new GsonBuilder().create();


        holder.tv_sku.setText(item_list.get(position).getmCode());
        holder.tv_qty.setText(item_list.get(position).getPickedQty());

        holder.chkSelected.setChecked(item_list.get(position).isSelected());

        holder.chkSelected.setTag(item_list.get(position));

       /* holder.chkSelected.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Model model = (Model) cb.getTag();

                model.setSelected(cb.isChecked());
                item_list.get(pos).setSelected(cb.isChecked());

            }
        });*/

        holder.chkSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CheckBox cb = (CheckBox) buttonView;
                Model model = (Model) cb.getTag();

             /*   model.setSelected(cb.isChecked());
                item_list.get(pos).setSelected(cb.isChecked());*/


                if (isChecked) {
                    model.setSelected(cb.isChecked());
                    item_list.get(pos).setSelected(cb.isChecked());

                } else {

                    model.setSelected(false);
                    item_list.get(pos).setSelected(false);


                }

            }
        });

        holder.btn_delete_unit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteItemFromList(v, pos);
            }
        });
    }

    private void deleteVLPDPickedItems(String PickedId, final int pos) {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, context);
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setAccountID(accountId);
            outbountDTO.setPickedId(PickedId);
            outbountDTO.setUserId(userId);
            outbountDTO.setOutboundID(OBDId);
            outbountDTO.setvLPDId(VLPDId);
            outbountDTO.setSKU(mCode);
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RetrofitBuilderHttpsEx.getInstance(context).create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.DeleteVLPDPickedItems(message);
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", context);
                    //logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ProgressDialogUtils.closeProgressDialog();
                // common.showUserDefinedAlertType(errorMessages.EMC_0002, context, context, "Error");
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
                                showAlertDialog(context, owmsExceptionMessage.getWMSMessage());
                            } else {

                                List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                OutbountDTO oOutboundDTO = null;

                                if(_lstPickitem.size()>0) {

                                    item_list.remove(pos);
                                    notifyDataSetChanged();
                                    ProgressDialogUtils.closeProgressDialog();
                                }else {
                                    item_list.remove(pos);
                                    notifyDataSetChanged();
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showUserDefinedAlertType(errorMessages.EMC_0061,fragmentActivity,context,"Error");
                                    return;
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", context);
                                // logException();
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
                        // common.showUserDefinedAlertType(errorMessages.EMC_0001, context, context, "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", context);
                    //  logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                // common.showUserDefinedAlertType(errorMessages.EMC_0001, context, context, "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_04", context);
                //logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            //  common.showUserDefinedAlertType(errorMessages.EMC_0003, context, context, "Error");
        }
    }


    public static void showAlertDialog(Context activity, String message) {

        alertDialog = new android.support.v7.app.AlertDialog.Builder(activity);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(POSITIVE_BUTTON_TEXT, null);
        alertDialog.show();
    }


    @Override
    public int getItemCount() {
        return item_list.size();
    }


    // confirmation dialog box to delete an unit
    private void deleteItemFromList(View v, final int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

        //builder.setTitle("Dlete ");
        builder.setMessage("Delete Item ?").setCancelable(false).setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {


                //Toast.makeText(context, position + " item is deleted", Toast.LENGTH_LONG).show();
                deleteVLPDPickedItems(item_list.get(position).getPickedId(), position);

                //  notifyDataSetChanged();


            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {


            }
        });

        builder.show();

    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView  tv_sku, tv_qty;
        public CheckBox chkSelected;
        public ImageButton btn_delete_unit;


        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            tv_sku = (TextView) itemLayoutView.findViewById(R.id.tv_sku);
            tv_qty = (TextView) itemLayoutView.findViewById(R.id.tv_qty);
            chkSelected = (CheckBox) itemLayoutView.findViewById(R.id.chk_selected);
            btn_delete_unit = (ImageButton) itemLayoutView.findViewById(R.id.btn_delete_unit);

        }
    }
}
