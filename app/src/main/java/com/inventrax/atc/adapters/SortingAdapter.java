package com.inventrax.atc.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.inventrax.atc.R;
import com.inventrax.atc.pojos.OutbountDTO;

import java.util.List;

public class SortingAdapter extends BaseAdapter {
    private Context context;
    List<OutbountDTO> lstDto;
    private LayoutInflater inflater;
    private int selectedPosition = -1;

    public SortingAdapter(Context context, List<OutbountDTO> lstDto) {
        this.context = context;
        this.lstDto = lstDto;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return lstDto.size();
    }

    @Override
    public Object getItem(int i) {
        return lstDto.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        viewHolder = new ViewHolder();

        //inflate the layout on basis of boolean
        view = inflater.inflate(R.layout.sorting_row, viewGroup, false);


        viewHolder.tvOBDNo = (TextView) view.findViewById(R.id.tvOBDNo);
        viewHolder.tvSKU = (TextView) view.findViewById(R.id.tvSKU);
        viewHolder.tvQty = (TextView) view.findViewById(R.id.tvQty);
        // view.setTag(viewHolder);


       /* viewHolder.tvOBDNo.setText(lstDto.get(i).getBatchNo());
        viewHolder.tvSKU.setText(lstDto.get(i).getProjectRefNo());
        viewHolder.tvQty.setText(lstDto.get(i).getSerialNo());*/

        viewHolder.tvSKU.setTag(i);





        return view;
    }


    private class ViewHolder {
        private TextView tvOBDNo, tvSKU, tvQty;

    }

    //Return the selectedPosition item
    public String getSelectedItem() {
        if (selectedPosition != -1) {
            Toast.makeText(context, "Selected Item : " + lstDto.get(selectedPosition).getSerialNo(), Toast.LENGTH_SHORT).show();
            return lstDto.get(selectedPosition).getAssignedQuantity();
        }
        return "";
    }

    //Delete the selected position from the arrayList
    public void deleteSelectedPosition() {
        if (selectedPosition != -1) {
            lstDto.remove(selectedPosition);
            selectedPosition = -1;//after removing selectedPosition set it back to -1
            notifyDataSetChanged();
        }
    }
}

