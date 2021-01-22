package com.inventrax.atc.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inventrax.atc.R;
import com.inventrax.atc.adapters.NavigationDrawerAdapter;
import com.inventrax.atc.adapters.NewExpandableListAdapter;
import com.inventrax.atc.model.NavDrawerItem;
import com.inventrax.atc.util.DialogUtils;
import com.inventrax.atc.util.FragmentUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Padmaja.B on 20/12/2018.
 */

public class DrawerFragment extends Fragment implements View.OnClickListener {

    private static String TAG = DrawerFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationDrawerAdapter adapter;
    private View containerView;
    private FragmentDrawerListener drawerListener;
    private View layout;
    private TextView txtLoginUser,txtHome;
    private AppCompatActivity appCompatActivity;
    private List<NavDrawerItem> menuItemList;
    private IntentFilter mIntentFilter;
    private CounterBroadcastReceiver counterBroadcastReceiver;

    private String userName = "",scanType="",UserRole="";
    List<String> listDataParent;
    HashMap<String, List<String>> listDataChild;
    ExpandableListView expandable_list_view;
    NewExpandableListAdapter.OnItemClick onItemClick;

    RelativeLayout rr;
    boolean isSupervisor=false;


    public void setDrawerListener(FragmentDrawerListener listener) {
        this.drawerListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflating view layout
        layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        appCompatActivity = (AppCompatActivity) getActivity();

        menuItemList = new ArrayList<>();

        loadFormControls();

        return layout;
    }

    private void createListData() {


        listDataParent = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataParent.add("Inbound");
        listDataParent.add("Outbound");
        listDataParent.add("House Keeping");

        // Adding child data List one
        List<String> mainListInbound = new ArrayList<String>();
        mainListInbound.add("Receiving");
        mainListInbound.add("Putaway");
        //mainListInbound.add("Pallet Transfers");

        // Adding child data List two
        List<String> mainListOutbound  = new ArrayList<String>();
        mainListOutbound.add("OBD Picking");
        //mainListOutbound.add("Packing");
        mainListOutbound.add("Packing Info");
        //mainListOutbound.add("Load Generation");
        mainListOutbound.add("Loading");
        mainListOutbound.add("Outbound Revert");

        // Adding child data List three
        List<String> mainListHouseKeeping = new ArrayList<String>();

        if(isSupervisor){
            mainListHouseKeeping.add("Material Transfers");
        }

        mainListHouseKeeping.add("Bin to Bin");
        mainListHouseKeeping.add("Live Stock");
        mainListHouseKeeping.add("Cycle Count");

        listDataChild.put(listDataParent.get(0), mainListInbound); // Header, Child data
        listDataChild.put(listDataParent.get(1), mainListOutbound); // Header, Child data
        listDataChild.put(listDataParent.get(2), mainListHouseKeeping); // Header, Child data

        NewExpandableListAdapter listAdapter = new NewExpandableListAdapter(getActivity(), listDataParent, listDataChild, new NewExpandableListAdapter.OnItemClick() {

            @Override
            public void onItemClick(int gpos, int cpos, String text) {
                mDrawerLayout.closeDrawer(containerView);
                setNavigationPage(text);
            }
        });

        expandable_list_view.setAdapter(listAdapter);
        expandable_list_view.expandGroup(0);
        expandable_list_view.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = 0;

            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != previousGroup)
                    expandable_list_view.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }
        });
    }

    private void createListDataAuto() {


        listDataParent = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
       // listDataParent.add("Inbound");
        listDataParent.add("Outbound");
      //  listDataParent.add("House Keeping");

        // Adding child data List one
        List<String> mainListInbound = new ArrayList<String>();
        mainListInbound.add("Receiving");
        mainListInbound.add("Putaway");
        //mainListInbound.add("Pallet Transfers");

        // Adding child data List two
        List<String> mainListOutbound  = new ArrayList<String>();
       // mainListOutbound.add("OBD Picking");
        //mainListOutbound.add("Packing");
        mainListOutbound.add("Packing Info");
        //mainListOutbound.add("Load Generation");
        mainListOutbound.add("Loading");

        // Adding child data List three
        List<String> mainListHouseKeeping = new ArrayList<String>();
        if(isSupervisor){
            mainListHouseKeeping.add("Material Transfers");
        }
        mainListHouseKeeping.add("Bin to Bin");
        mainListHouseKeeping.add("Live Stock");
        mainListHouseKeeping.add("Cycle Count");

      //  listDataChild.put(listDataParent.get(0), mainListInbound); // Header, Child data
        listDataChild.put(listDataParent.get(0), mainListOutbound); // Header, Child data
       // listDataChild.put(listDataParent.get(2), mainListHouseKeeping); // Header, Child data

        NewExpandableListAdapter listAdapter = new NewExpandableListAdapter(getActivity(), listDataParent, listDataChild, new NewExpandableListAdapter.OnItemClick() {

            @Override
            public void onItemClick(int gpos, int cpos, String text) {
                mDrawerLayout.closeDrawer(containerView);
                setNavigationPage(text);
            }
        });

        expandable_list_view.setAdapter(listAdapter);
        expandable_list_view.expandGroup(0);
        expandable_list_view.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = 0;

            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != previousGroup)
                    expandable_list_view.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }
        });
    }

    public void loadFormControls(){
        try {

            SharedPreferences sp = getContext().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
            userName = sp.getString("UserName", "");
            scanType = sp.getString("scanType", "");
            UserRole = sp.getString("UserRole", "");


            if(!UserRole.isEmpty()){
                String[] stringsUserRole=UserRole.split("[,]");
                for (String s : stringsUserRole) {
                    if (s.trim().equals("6") || s.trim().equals("11") ) {
                        isSupervisor = true;
                        break;
                    } else {
                        isSupervisor = false;
                    }
                }
            }else{
                isSupervisor=false;
            }


            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction("com.example.broadcast.counter");

            txtLoginUser = (TextView) layout.findViewById(R.id.txtLoginUser);
            txtHome = (TextView) layout.findViewById(R.id.txtHome);

            rr = (RelativeLayout) layout.findViewById(R.id.rr);

            txtLoginUser.setText(userName);

/*            Fragment fragment = new HomeFragment();
            if(fragment != null && fragment.isVisible() && fragment instanceof HomeFragment ){
                rr.setBackgroundColor(Color.parseColor("#000000"));
            }else{
                rr.setBackgroundColor(Color.parseColor("#FF0000"));
            }*/

            txtHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.closeDrawer(containerView);
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                }
            });

            expandable_list_view = (ExpandableListView) layout.findViewById(R.id.expandable_list_view);

            if(scanType.equals("Auto")){
                createListDataAuto();
            }else{
                createListData();
            }


        }catch (Exception ex) {
            DialogUtils.showAlertDialog(getActivity(), "Error while loading menu list");
            return;
        }
    }

    /*    public void loadFormControls() {
        try {

            SharedPreferences sp = getContext().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
            userName = sp.getString("UserName", "");
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction("com.example.broadcast.counter");

            counterBroadcastReceiver = new CounterBroadcastReceiver();

            menuItemList = getMenuItemsByUserType("1");

            new ProgressDialogUtils(getContext());

            recyclerView = (RecyclerView) layout.findViewById(R.id.drawerList);
            txtLoginUser = (TextView) layout.findViewById(R.id.txtLoginUser);

            txtLoginUser.setText(userName);


            adapter = new NavigationDrawerAdapter(getActivity(), menuItemList);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
                @Override
                public void onClick(View view, int position) {

                    NavigationDrawerAdapter.setSelectedItemPosition(position);
                    recyclerView.getAdapter().notifyDataSetChanged();
                    drawerListener.onDrawerItemSelected(view, position, menuItemList.get(position));
                    mDrawerLayout.closeDrawer(containerView);

                }

                @Override
                public void onLongClick(View view, int position) {

                }
            }));


        } catch (Exception ex) {
            //Logger.Log(DrawerFragment.class.getName(), ex);
            DialogUtils.showAlertDialog(getActivity(), "Error while loading menu list");
            return;
        }
    }*/

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*try
        {
            if ( user != null ) {

                appCompatActivity.getSupportActionBar().setTitle(StringUtils.toCamelCase(user.getFirstName()));
                appCompatActivity.getSupportActionBar().setSubtitle(user.getUserType().toUpperCase() + "  " + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) );

            }

        }catch (Exception ex){
            Logger.Log(DrawerFragment.class.getName(),ex);
            DialogUtils.showAlertDialog(getActivity(), "Error while loading menu list");
            return;
        }*/

    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {

        try {
            containerView = getActivity().findViewById(fragmentId);
            mDrawerLayout = drawerLayout;
            mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getActivity().invalidateOptionsMenu();
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    getActivity().invalidateOptionsMenu();
                }

                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    super.onDrawerSlide(drawerView, slideOffset);
                    toolbar.setAlpha(1 - slideOffset / 2);
                }
            };



            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerLayout.post(new Runnable() {
                @Override
                public void run() {
                    mDrawerToggle.syncState();
                }
            });
        } catch (Exception ex) {
            // Logger.Log(DrawerFragment.class.getName(),ex);
            return;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) { }
    }

    @Override
    public void onResume() {
        super.onResume();
        // getActivity().registerReceiver(counterBroadcastReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // getActivity().unregisterReceiver(counterBroadcastReceiver);
    }


    public void setNavigationPage(String menuChildText){
        switch (menuChildText) {
            case "Receiving": {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new UnloadingFragment());
            }
            break;
            case "Putaway": {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PalletTransfersFragment());
            }
            break;
            case "Pallet Transfers": {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PalletTransfersFragment());
            }
            break;
            case "OBD Picking": {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new OBDPickingHeaderFragment());
            }
            break;
            case "Packing": {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PackingFragment());
            }
            break;
            case "Packing Info": {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PackingInfoFragment());
            }
            break;
            case "Load Generation": {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new LoadGenerationFragment());
            }
            break;
            case "Loading": {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new LoadingFragmentNew());
            }
            break;
            case "Outbound Revert": {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new OutboundRevertHeaderFragment());
            }
            break;
            case "Material Transfers": {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new MaterialTransferFragment());
            }
            break;
           case "Bin to Bin": {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new InternalTransferFragment());
            }
            break;
            case "Live Stock": {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new LiveStockFragment());
            }
            break;
            case "Cycle Count": {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new CycleCountHeaderFragment());
            }
            break;
            default:
            break;
        }
    }

    public List<NavDrawerItem> getMenuItemsByUserType(String userType) {

        List<NavDrawerItem> menuList = new ArrayList<>();

        switch (userType) {

            case "1": {
                menuList.add(new NavDrawerItem("Home", R.drawable.go));
                // Inbound
                menuList.add(new NavDrawerItem("Receiving", R.drawable.go));
                menuList.add(new NavDrawerItem("Putaway", R.drawable.go));
                //menuList.add(new NavDrawerItem("Pallet Transfers", R.drawable.go));
                // Outbound
                menuList.add(new NavDrawerItem("OBD Picking", R.drawable.go));
                //menuList.add(new NavDrawerItem("Packing", R.drawable.go));
                //menuList.add(new NavDrawerItem("Load Generation", R.drawable.go));
                menuList.add(new NavDrawerItem("Loading", R.drawable.go));
                // HouseKeeping
                menuList.add(new NavDrawerItem("Bin to Bin", R.drawable.go));
                menuList.add(new NavDrawerItem("Cycle Count", R.drawable.go));
                menuList.add(new NavDrawerItem("Live Stock", R.drawable.go));

                // menuList.add(new NavDrawerItem("Delete OBD Picked Items", R.drawable.go));
                // menuList.add(new NavDrawerItem("VLPD Picking", R.drawable.go));
                // menuList.add(new NavDrawerItem("Delete VLPD Picked Items", R.drawable.go));
                // menuList.add(new NavDrawerItem("Transfer Order Pick", R.drawable.go));
                // menuList.add(new NavDrawerItem("Transfer Order PutAway", R.drawable.go));

            }

            break;

            case "2": { }
            break;

            case "3": { }
            break;

            case "4": { }
            break;

            case "5": { }
            break;

            case "6": { }
            break;
        }

        return menuList;
    }



    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public interface FragmentDrawerListener {
        void onDrawerItemSelected(View view, int position, NavDrawerItem menuItem);
    }

    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            try {

                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                    clickListener.onClick(child, rv.getChildPosition(child));
                }
                return false;

            } catch (Exception ex) {
                // Logger.Log(DrawerFragment.class.getName(),ex);
                return false;

            }
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public class CounterBroadcastReceiver extends BroadcastReceiver {

        public CounterBroadcastReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {

            try {

                if (menuItemList != null) menuItemList.clear();

                menuItemList = getMenuItemsByUserType("1");

                adapter.setNavigationDrawerAdapter(getActivity(), menuItemList);
                adapter.notifyDataSetChanged();

            } catch (Exception ex) {
                //  Logger.Log(DrawerFragment.class.getName(),ex);
                return;
            }

        }

    }

}