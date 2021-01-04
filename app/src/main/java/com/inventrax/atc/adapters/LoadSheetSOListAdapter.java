package com.inventrax.atc.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.inventrax.atc.R;
import com.inventrax.atc.pojos.OutbountDTO;

import java.util.List;

public class LoadSheetSOListAdapter extends RecyclerView.Adapter {

    private static final String classCode = "API_FRAG_009";
    private List<OutbountDTO> outbountDTOS;
    Context context;
    OnCheckChangeListner onCheckChangeListner;

    public LoadSheetSOListAdapter(Context context, List<OutbountDTO> outbountDTOS,OnCheckChangeListner onCheckChangeListner){
        this.context=context;
        this.outbountDTOS=outbountDTOS;
        this.onCheckChangeListner=onCheckChangeListner;
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

         OutbountDTO outbountDTO = (OutbountDTO) outbountDTOS.get(position);
        ((MyViewHolder) holder).sno.setText(""+(position+1));
        ((MyViewHolder) holder).obd_name_check.setText(""+outbountDTO.getSONumber());

        if(outbountDTO.isChecked()){
            ((MyViewHolder) holder).obd_name_check.setChecked(true);
        }else{
            ((MyViewHolder) holder).obd_name_check.setChecked(false);
        }


        ((MyViewHolder) holder).obd_name_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckChangeListner.onCheckChange(position,isChecked);
            }
        });

    }


    @Override
    public int getItemCount() {
        return outbountDTOS.size();
    }

    // Item click listener interface
    public interface OnCheckChangeListner {
        void onCheckChange(int pos,boolean isChecked);
    }

}
