package com.inventrax.atc.appupdate;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;

public class UpdateApp extends AsyncTask<String, Integer, String> {

    private ProgressDialog mPDialog;
    private Context mContext;

    public void setContext(Activity context) {
        mContext = context;
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPDialog = new ProgressDialog(mContext);
                mPDialog.setMessage("Please wait...");
                mPDialog.setIndeterminate(true);
                mPDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mPDialog.setCancelable(false);
                mPDialog.show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected String doInBackground(String... arg0) {
        try {
            URL url = new URL(arg0[0]);
            URLConnection connection = url.openConnection();
            connection.connect();
            // Detect the file length
            int fileLength = connection.getContentLength();
            // Locate storage location
            String PATH = Objects.requireNonNull(mContext.getExternalFilesDir(null)).getAbsolutePath();
            File file = new File(PATH);
            boolean isCreate = file.mkdirs();
            File outputFile = new File(file, "appupdate.apk");
            if (outputFile.exists()) {
                boolean isDelete = outputFile.delete();
            }
            // Download the file
            InputStream input = new BufferedInputStream(url.openStream());
            // Save the downloaded file
            OutputStream output = new FileOutputStream(outputFile);
            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // Publish the progress
                publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();

            if (mPDialog != null)
                mPDialog.dismiss();
        } catch (MalformedURLException e) {
            Log.e("UpdateAPP1", "Update error 1 ! " + e.getStackTrace());
            return e.toString();
        }catch (IOException e) {
            Log.e("UpdateAPP2", "Update error 2 ! " + e.getStackTrace());
            return e.toString();
        }
/*        } catch (Exception e) {
            Log.e("UpdateAPP", "Update error! " + e.getStackTrace());
            return e.toString();
        }*/
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mPDialog != null)
            mPDialog.show();
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (mPDialog != null) {
            mPDialog.setIndeterminate(false);
            mPDialog.setMax(100);
            mPDialog.setProgress(values[0]);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onPostExecute(String result) {

        if (mPDialog != null)
            mPDialog.dismiss();

        if (result != null)
            Toast.makeText(mContext, "Download error: " + result, Toast.LENGTH_LONG).show();
        else{
            installApk();
            Toast.makeText(mContext, "File Downloaded", Toast.LENGTH_SHORT).show();
        }

/*        if(result == null && !result.isEmpty() ){
            installApk();
            Toast.makeText(mContext, "File Downloaded", Toast.LENGTH_SHORT).show();
        }*/

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void installApk() {
        try {
            String PATH = Objects.requireNonNull(mContext.getExternalFilesDir(null)).getAbsolutePath();
            File file = new File(PATH + "/appupdate.apk");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= 24) {
                Uri downloaded_apk = FileProvider.getUriForFile(mContext, mContext.getApplicationContext().getPackageName() + ".provider", file);
                intent.setDataAndType(downloaded_apk, "application/vnd.android.package-archive");
                List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    mContext.grantUriPermission(mContext.getApplicationContext().getPackageName() + ".provider", downloaded_apk, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mContext.startActivity(intent);
            } else {
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}