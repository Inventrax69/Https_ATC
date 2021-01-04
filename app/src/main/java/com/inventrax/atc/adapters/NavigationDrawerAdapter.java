package com.inventrax.atc.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.inventrax.atc.R;

import com.inventrax.atc.model.NavDrawerItem;

import java.util.Collections;
import java.util.List;

/**
 * Author   : Prasanna ch
 *
 * Purpose	: Navigation Drawer Adapter
 */


public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.MyViewHolder> {

    private static int selectedItemPosition;
    List<NavDrawerItem> data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;

    public NavigationDrawerAdapter(Context context, List<NavDrawerItem> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    public static void setSelectedItemPosition(int selectedItemPosition) {
        NavigationDrawerAdapter.selectedItemPosition = selectedItemPosition;
    }

    public void setNavigationDrawerAdapter(Context context, List<NavDrawerItem> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    public void delete(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.nav_drawer_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        try {

            NavDrawerItem current = data.get(position);

            holder.title.setText(current.getTitle());

            if (current.getImageView() != 0) {
                holder.imageView.setImageResource(current.getImageView());
                holder.imageView.setVisibility(ImageView.VISIBLE);
            }

            holder.txtCounter.setVisibility(TextView.GONE);

            if (!TextUtils.isEmpty(current.getCounter()) && !current.getCounter().equalsIgnoreCase("0")) {
                holder.txtCounter.setText("" + current.getCounter());
                holder.txtCounter.setVisibility(TextView.VISIBLE);
            }

            if (position == selectedItemPosition) {
                // holder.title.setTextColor(Color.BLUE);
                holder.relativeLayout.setSelected(true);

            } else {
                // holder.title.setTextColor(Color.BLACK);
                holder.relativeLayout.setSelected(false);

            }
        } catch (Exception ex) {
            //Logger.Log(NavigationDrawerAdapter.class.getName(), ex);
        }


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView txtCounter;
        ImageView imageView;
        RelativeLayout relativeLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imgTitleIcon);
            title = (TextView) itemView.findViewById(R.id.title);
            txtCounter = (TextView) itemView.findViewById(R.id.txtCounter);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.nav_drawer_row_relative_layout);
        }
    }
}