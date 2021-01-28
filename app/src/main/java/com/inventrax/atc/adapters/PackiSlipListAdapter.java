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

public class PackiSlipListAdapter extends  RecyclerView.Adapter{

    private List<OutbountDTO> packingInfoList;

        Context context;
        public PackiSlipListAdapter(Context context, List<OutbountDTO> list) {
            this.context = context;
            this.packingInfoList = list;
        }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtSONumber;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtSONumber = (TextView) itemView.findViewById(R.id.txtSONumber);


        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // infalte the item Layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.packsliplist_row, parent, false);

            // set the view's size, margins, paddings and layout parameters
            return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        OutbountDTO outbountDTO = (OutbountDTO) packingInfoList.get(position);

        // set the data in items

        ((MyViewHolder) holder).txtSONumber.setText(outbountDTO.getPackingSlipNo());


    }


    @Override
    public int getItemCount() {
            return packingInfoList.size();
        }

}
