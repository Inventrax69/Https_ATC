package com.inventrax.atc.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.inventrax.atc.R;
import com.inventrax.atc.util.FragmentUtils;


public class HomeFragment extends Fragment implements View.OnClickListener {

    private View rootView;
    Fragment fragment = null;
    LinearLayout ll_receive, ll_putaway, ll_picking, ll_VLPDPicking;
    LinearLayout  ll_houseKeeping, ll_cycleCount, ll_livestock;
    LinearLayout ll_packing,ll_loading,ll_load_generation;
    LinearLayout ll_linear1,ll_linear2;
    private String userId = null, scanType = null, accountId = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_home,container,false);
        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        scanType = sp.getString("scanType", "");
        accountId = sp.getString("AccountId", "");

        ll_receive = (LinearLayout) rootView.findViewById(R.id.ll_receive);
        ll_putaway = (LinearLayout) rootView.findViewById(R.id.ll_putaway);
        ll_picking = (LinearLayout) rootView.findViewById(R.id.ll_picking);
        ll_VLPDPicking = (LinearLayout) rootView.findViewById(R.id.ll_VLPDPicking);
        ll_houseKeeping = (LinearLayout) rootView.findViewById(R.id.ll_houseKeeping);
        ll_cycleCount = (LinearLayout) rootView.findViewById(R.id.ll_cycleCount);
        ll_livestock = (LinearLayout) rootView.findViewById(R.id.ll_livestock);

        ll_packing = (LinearLayout) rootView.findViewById(R.id.ll_packing);
        ll_load_generation = (LinearLayout) rootView.findViewById(R.id.ll_load_generation);
        ll_loading = (LinearLayout) rootView.findViewById(R.id.ll_loading);

        ll_linear1 = (LinearLayout) rootView.findViewById(R.id.ll_linear1);
        ll_linear2 = (LinearLayout) rootView.findViewById(R.id.ll_linear2);

        ll_receive.setOnClickListener(this);
        ll_putaway.setOnClickListener(this);
        ll_picking.setOnClickListener(this);

        ll_VLPDPicking.setOnClickListener(this);

        ll_houseKeeping.setOnClickListener(this);
        ll_cycleCount.setOnClickListener(this);

        ll_livestock.setOnClickListener(this);

        ll_packing.setOnClickListener(this);
        ll_load_generation.setOnClickListener(this);
        ll_loading.setOnClickListener(this);

        if (scanType.equals("Auto")) {
            ll_linear1.setVisibility(View.GONE);
            ll_linear2.setVisibility(View.VISIBLE);
        } else {
            ll_linear1.setVisibility(View.VISIBLE);
            ll_linear2.setVisibility(View.GONE);
        }


    }



    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Home");
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.ll_receive:
                UnloadingFragment unloadingFragment = new UnloadingFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, unloadingFragment);
                break;

            case R.id.ll_putaway:
                PalletTransfersFragment palletTransfersFragment = new PalletTransfersFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, palletTransfersFragment);
                break;

            case R.id.ll_picking:
                OBDPickingHeaderFragment obdPickingHeaderFragment = new OBDPickingHeaderFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, obdPickingHeaderFragment);
                break;

            case R.id.ll_VLPDPicking:
                VLPDPickingHeaderFragment vlpdPickingHeaderFragment = new VLPDPickingHeaderFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, vlpdPickingHeaderFragment);
                break;

            case R.id.ll_houseKeeping:
                InternalTransferFragment internalTransferheaderFragment = new InternalTransferFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, internalTransferheaderFragment);
                break;

            case R.id.ll_cycleCount:
                CycleCountHeaderFragment cycleCountHeaderFragment = new CycleCountHeaderFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, cycleCountHeaderFragment);
                break;

            case R.id.ll_livestock:
                LiveStockFragment liveStockFragment_ = new LiveStockFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, liveStockFragment_);
                break;

            case R.id.ll_packing:
                PackingFragment packingFragment = new PackingFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, packingFragment);
                break;

            case R.id.ll_load_generation:
                LoadGenerationFragment loadGenerationFragment = new LoadGenerationFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, loadGenerationFragment);
                break;

            case R.id.ll_loading:
                NewLoadSheetFragment newLoadSheetFragment = new NewLoadSheetFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, newLoadSheetFragment);
                break;

        }

    }


}