package com.inventrax.atc.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.atc.R;
import com.inventrax.atc.pojos.CycleCountDTO;

import java.util.List;

public class CCExportAdapter extends RecyclerView.Adapter {

    private List<CycleCountDTO> cycleCountList;

    Context context;

    public CCExportAdapter(Context context, List<CycleCountDTO> list) {
        this.context = context;
        this.cycleCountList = list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtQty, txtCCName, txtLocationCode, txtMCode, txtMfg, txtExp;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtQty = (TextView) itemView.findViewById(R.id.txtQty);
            txtCCName = (TextView) itemView.findViewById(R.id.txtCCName);
            txtLocationCode = (TextView) itemView.findViewById(R.id.txtLocationCode);
            txtMCode = (TextView) itemView.findViewById(R.id.txtMCode);
            txtMfg = (TextView) itemView.findViewById(R.id.txtMfg);
            txtExp = (TextView) itemView.findViewById(R.id.txtExp);


        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cc_export_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        CycleCountDTO cycleCountDTO = (CycleCountDTO) cycleCountList.get(position);

        // set the data in items

        ((MyViewHolder) holder).txtCCName.setText(cycleCountDTO.getCCName());
        ((MyViewHolder) holder).txtQty.setText(cycleCountDTO.getCCQty());
        ((MyViewHolder) holder).txtLocationCode.setText(cycleCountDTO.getLocation());
        ((MyViewHolder) holder).txtMCode.setText(cycleCountDTO.getMaterialCode());
        ((MyViewHolder) holder).txtMfg.setText(cycleCountDTO.getMfgDate());
        ((MyViewHolder) holder).txtExp.setText(cycleCountDTO.getExpDate());


    }


    @Override
    public int getItemCount() {
        return cycleCountList.size();
    }

}
