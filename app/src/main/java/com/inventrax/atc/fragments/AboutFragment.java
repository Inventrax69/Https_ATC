package com.inventrax.atc.fragments;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.inventrax.atc.R;
import com.inventrax.atc.services.appupdate.UpdateServiceUtils;
import com.inventrax.atc.util.DialogUtils;
import com.inventrax.atc.util.ProgressDialogUtils;


public class AboutFragment extends Fragment implements View.OnClickListener {

    private View rootView;
    private TextView txtVersion;
    private Button btnCheckUpdate;
    private TextView txtHelpLine;
    private TextView txtYouTubeLink;
    private TextView txtLocation;
    private TextView txtWebSite, lblReleaseDate, txtReleaseDate;
    private UpdateServiceUtils updateServiceUtils;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_about, container, false);
        loadFormControls();
        return rootView;

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    private void loadFormControls() {
        try {

            txtVersion = (TextView) rootView.findViewById(R.id.txtVersion);

           // txtVersion.setText(AndroidUtils.getVersionName().toString());

            try {
                PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                String version = pInfo.versionName;
                txtVersion.setText("" + version);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            btnCheckUpdate = (Button) rootView.findViewById(R.id.btnCheckUpdate);
            btnCheckUpdate.setOnClickListener(this);

            txtHelpLine = (TextView) rootView.findViewById(R.id.txtHelpLine);
            txtHelpLine.setOnClickListener(this);

            txtYouTubeLink = (TextView) rootView.findViewById(R.id.txtYouTubeLink);
            txtYouTubeLink.setOnClickListener(this);
            txtYouTubeLink.setVisibility(View.GONE);
            //txtLocation=(TextView)rootView.findViewById(R.id.txtLocation);
            //txtLocation.setVisibility(TextView.GONE);

            txtWebSite = (TextView) rootView.findViewById(R.id.txtWebSite);
            txtWebSite.setOnClickListener(this);

            lblReleaseDate = (TextView) rootView.findViewById(R.id.lblReleaseDate);
            txtReleaseDate = (TextView) rootView.findViewById(R.id.txtReleaseDate);
            txtReleaseDate.setText("25-01-2020");

        } catch (Exception ex) {
            Log.d(AboutFragment.class.getName(), ex.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //sfaCommon.displayDate(getActivity());

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_about));

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btnCheckUpdate: {

                try {
                    new ProgressDialogUtils(getActivity());

                    ProgressDialogUtils.showProgressDialog("Checking ...");

                    updateServiceUtils = new UpdateServiceUtils();

                    //updateServiceUtils.checkUpdate();

                    ProgressDialogUtils.closeProgressDialog();

                } catch (Exception ex) {

                    ProgressDialogUtils.closeProgressDialog();
                    DialogUtils.showAlertDialog(getActivity(), "Error while checking update");
                    //Logger.Log(AboutFragment.class.getName(), ex);
                    return;
                }

            }
            break;

            case R.id.txtHelpLine: {

                try {
                    // Create the intent.
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                    // Set the data for the intent as the phone number.
                    dialIntent.setData(Uri.parse("tel:" + "9676692934"));
                    // sending intent.
                    startActivity(dialIntent);
                } catch (Exception e) {
                    DialogUtils.showAlertDialog(getActivity(), "No Dialer is present in this Device");
                }

            }
            break;

            case R.id.txtYouTubeLink: {

                try {
                    // Starting YouTube Intent
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=VrrFNQ0QCp8")));

                } catch (Exception ex) {
                    DialogUtils.showAlertDialog(getActivity(), "Error while opening youtube");
                }

            }
            break;
        }
    }


}