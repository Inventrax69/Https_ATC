package com.inventrax.atc.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.atc.R;
import com.inventrax.atc.pojos.InventoryDTO;

import java.util.List;

public class LiveStockAdapter extends  RecyclerView.Adapter{

    private List<InventoryDTO> liveStockList;

        Context context;
        public LiveStockAdapter(Context context, List<InventoryDTO> list) {
            this.context = context;
            this.liveStockList = list;
        }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtQty,txtSloc,tvSerialNo,txtBatch,txtExp,txtMfg,txtMCode,txtLocation,txtPrjRef,txtMRP;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtQty = (TextView) itemView.findViewById(R.id.txtQty);
            txtSloc = (TextView) itemView.findViewById(R.id.txtSloc);
            tvSerialNo = (TextView) itemView.findViewById(R.id.txtSerialNo);
            txtBatch = (TextView) itemView.findViewById(R.id.txtBatch);
            txtExp = (TextView) itemView.findViewById(R.id.txtExp);
            txtMfg = (TextView) itemView.findViewById(R.id.txtMfg);
            txtMCode = (TextView) itemView.findViewById(R.id.txtMCode);
            txtLocation = (TextView) itemView.findViewById(R.id.txtLocation);
            txtPrjRef = (TextView) itemView.findViewById(R.id.txtPrjRef);
            txtMRP = (TextView) itemView.findViewById(R.id.txtMRP);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // infalte the item Layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.livestock_row_rsn, parent, false);

            // set the view's size, margins, paddings and layout parameters
            return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        InventoryDTO inventoryDTO = (InventoryDTO) liveStockList.get(position);

        // set the data in items

        ((MyViewHolder) holder).txtSloc.setText(inventoryDTO.getSLOC());
        ((MyViewHolder) holder).txtQty.setText(inventoryDTO.getQuantity().split("[.]")[0]);
        ((MyViewHolder) holder).tvSerialNo.setText("Serial No.:   " + inventoryDTO.getSerialNo());
        ((MyViewHolder) holder).txtBatch.setText("Batch:   " + inventoryDTO.getBatchNo());
        ((MyViewHolder) holder).txtExp.setText("Exp:   " + inventoryDTO.getExpDate());
        ((MyViewHolder) holder).txtMfg.setText("Mfg:   " + inventoryDTO.getMfgDate());
        ((MyViewHolder) holder).txtLocation.setText(inventoryDTO.getLocationCode());
        ((MyViewHolder) holder).txtMCode.setText(inventoryDTO.getMaterialCode());
        ((MyViewHolder) holder).txtPrjRef.setText("Prj. Ref.#:   " + inventoryDTO.getProjectNo());
        ((MyViewHolder) holder).txtMRP.setText("MRP:   " + inventoryDTO.getMRP());


    }


    @Override
    public int getItemCount() {
            return liveStockList.size();
        }

}
