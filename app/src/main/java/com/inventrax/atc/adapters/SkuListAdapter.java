package com.inventrax.atc.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inventrax.atc.R;
import com.inventrax.atc.pojos.SKUListDTO;

import java.util.List;

public class SkuListAdapter extends  RecyclerView.Adapter{

    Context context;
    RelativeLayout relativeOne,relativeTwo,relativeThree,relativeFour;
    boolean isSet=false;
    private List<SKUListDTO> skuListDTOList;
    OnItemClickListener listener;

    public SkuListAdapter(Context context, List<SKUListDTO> skuListDTOList,RelativeLayout relativeOne,RelativeLayout relativeTwo,RelativeLayout relativeThree,RelativeLayout relativeFour,OnItemClickListener listener) {
        this.context = context;
        this.skuListDTOList = skuListDTOList;
        this.relativeOne=relativeOne;
        this.relativeTwo=relativeTwo;
        this.relativeThree=relativeThree;
        this.relativeFour=relativeFour;
        this.listener=listener;
        isSet=true;
    }

    public SkuListAdapter(Context context, List<SKUListDTO> skuListDTOList) {
        this.context = context;
        this.skuListDTOList = skuListDTOList;
        isSet=false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // infalte the item Layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sku_list_iem_view, parent, false);

            // set the view's size, margins, paddings and layout parameters
            return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        SKUListDTO skuListDTO = (SKUListDTO) skuListDTOList.get(position);

        // set the data in items

        ((MyViewHolder) holder).txtMCode.setText(skuListDTO.getMCode());

       // ((MyViewHolder) holder).txtQty.setText(((skuListDTO.getPackedQty().split("[.]") !=null && skuListDTO.getPackedQty().split("[.]").length > 0 ) ? skuListDTO.getPackedQty().split("[.]")[0] : skuListDTO.getPackedQty()) + " / " +
       //         ((skuListDTO.getSOQty().split("[.]") !=null && skuListDTO.getSOQty().split("[.]").length > 0 ) ? skuListDTO.getSOQty().split("[.]")[0] : skuListDTO.getSOQty()) );

        if(skuListDTO.getPackedQty() != null && skuListDTO.getPickedQty() != null)
          ((MyViewHolder) holder).txtQty.setText(skuListDTO.getPackedQty().split("[.]")[0] +" / " + skuListDTO.getPickedQty().split("[.]")[0] );
/*
        ((MyViewHolder) holder).view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSet){



                }
            }
        });*/


    }

    @Override
    public int getItemCount() {
            return skuListDTOList.size();
        }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtQty,txtMCode;// init the item view's
        View view;

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtQty = (TextView) itemView.findViewById(R.id.txtQty);
            txtMCode = (TextView) itemView.findViewById(R.id.txtMCode);
            view=itemView;

            //on item click
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.onItemClick(pos);
                        }
                    }
                }

            });

        }
    }

    // Item click listener interface
    public interface OnItemClickListener {
        void onItemClick(int pos);
    }

}
