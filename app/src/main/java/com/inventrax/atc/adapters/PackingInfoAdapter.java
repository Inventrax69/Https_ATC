package com.inventrax.atc.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.atc.R;
import com.inventrax.atc.pojos.OutbountDTO;

import java.util.List;

public class PackingInfoAdapter extends  RecyclerView.Adapter{

    private List<OutbountDTO> packingInfoList;

        Context context;
        public PackingInfoAdapter(Context context, List<OutbountDTO> list) {
            this.context = context;
            this.packingInfoList = list;
        }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtSONumber,txtSKU,txtQTY,txtDescription,txtBatch,txtMfg,txtPrjRef,txtExp,txtMrp,txtSerialNo,txtHU;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtSONumber = (TextView) itemView.findViewById(R.id.txtSONumber);
            txtSKU = (TextView) itemView.findViewById(R.id.txtSKU);
            txtQTY = (TextView) itemView.findViewById(R.id.txtQTY);
            txtDescription = (TextView) itemView.findViewById(R.id.txtDescription);
            txtBatch = (TextView) itemView.findViewById(R.id.txtBatch);
            txtPrjRef = (TextView) itemView.findViewById(R.id.txtPrjRef);
            txtMfg = (TextView) itemView.findViewById(R.id.txtMfg);
            txtExp = (TextView) itemView.findViewById(R.id.txtExp);
            txtSerialNo = (TextView) itemView.findViewById(R.id.txtSerialNo);
            txtMrp = (TextView) itemView.findViewById(R.id.txtMrp);
            txtHU = (TextView) itemView.findViewById(R.id.txtHU);

        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // infalte the item Layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.packinginfo_row, parent, false);

            // set the view's size, margins, paddings and layout parameters
            return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        OutbountDTO outbountDTO = (OutbountDTO) packingInfoList.get(position);

        // set the data in items

        ((MyViewHolder) holder).txtSONumber.setText(outbountDTO.getSONumber());
        ((MyViewHolder) holder).txtQTY.setText("Qty:   " +outbountDTO.getPickedQty().split("[.]")[0]);
        ((MyViewHolder) holder).txtSKU.setText("Material : "+outbountDTO.getmCode());
        ((MyViewHolder) holder).txtDescription.setText(outbountDTO.getMaterialDescription());
        ((MyViewHolder) holder).txtExp.setText("Exp:   " + outbountDTO.getExpDate());
        ((MyViewHolder) holder).txtMfg.setText("Mfg:   " + outbountDTO.getMfgDate());
        ((MyViewHolder) holder).txtBatch.setText("Batch:   " +outbountDTO.getBatchNo());
        ((MyViewHolder) holder).txtDescription.setText(outbountDTO.getMaterialDescription());
        ((MyViewHolder) holder).txtPrjRef.setText("Prj. Ref.#:   " + outbountDTO.getProjectNo());
        ((MyViewHolder) holder).txtSerialNo.setText("Serial No.:   " + outbountDTO.getSerialNo());
        ((MyViewHolder) holder).txtMrp.setText("MRP:   " + outbountDTO.getMRP());
        ((MyViewHolder) holder).txtHU.setText("HU:   " + outbountDTO.getHUSize());


    }


    @Override
    public int getItemCount() {
            return packingInfoList.size();
        }

}
