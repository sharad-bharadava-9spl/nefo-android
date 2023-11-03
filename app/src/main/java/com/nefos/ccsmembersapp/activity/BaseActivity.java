package com.nefos.ccsmembersapp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.nefos.ccsmembersapp.BuildConfig;
import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.Utils;
import com.nefos.ccsmembersapp.interfaces.IDialog;
import com.nefos.ccsmembersapp.interfaces.IView;
import com.nefos.ccsmembersapp.model.MyExtraData;
import com.nefos.ccsmembersapp.server.ApiClient;
import com.nefos.ccsmembersapp.server.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class  BaseActivity extends AppCompatActivity implements IDialog, IView {

    LocationManager locationManager;
    static final int ASK_PERMISSION_TAG = 2002;
    static final int OPEN_SETTING_TAG = 2004;

    @RequiresApi(api = Build.VERSION_CODES.M)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app.
                    if (!checkGpsStatus()) {
                        alertAskUserToEnableLocation(OPEN_SETTING_TAG, getString(R.string.enable_gps));
                    }
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
                        alertAskUserToEnableLocation(ASK_PERMISSION_TAG, getString(R.string.ask_location_permission));
                    else
                        onInfo(getString(R.string.ask_location_permission));
                }
            });

    private void alertAskUserToEnableLocation(int tag, String message) {
        Utils.onInfoWithResult(message, this, tag,
                getString(R.string.ok), getString(R.string.cancel), new MyExtraData(), "", true, this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            if (!checkGpsStatus()) {
                alertAskUserToEnableLocation(OPEN_SETTING_TAG, getString(R.string.enable_gps));
            }
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // In an educational UI, explain to the user why your app requires this
            // permission for a specific feature to behave as expected. In this UI,
            // include a "cancel" or "no thanks" button that allows the user to
            // continue using your app without granting the permission.
            alertAskUserToEnableLocation(ASK_PERMISSION_TAG, getString(R.string.ask_location_permission));
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    public Boolean checkGpsStatus() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    /*This method is use to show log*/
    void log(String message) {
        Utils.log(this.getClass().getSimpleName(), message);
    }

    /*This method is use to show toast*/
    public void toast(String message) {
        Utils.toast(message, this);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onDialogClick(boolean isOk, int tag, MyExtraData extraData) {
        if (isOk) {
            switch (tag) {
                case ASK_PERMISSION_TAG:
                    requestPermissionLauncher.launch(
                            Manifest.permission.ACCESS_FINE_LOCATION);
                    break;
                case OPEN_SETTING_TAG:
                    openSettings();
                    break;
            }
        }
    }

    @Override
    public void onDialogCancelClick(boolean isOk, int tag, MyExtraData extraData) {

    }

    private void checkVersionApiCall() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
        ApiClient apiClient;
        apiClient = new ApiClient();
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.checkVersion(
                SharedPreference.getSelectedLanguage(this), "Android", BuildConfig.VERSION_NAME);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());
                        int flag = jsonObject.getInt("flag");

                        if (flag == 1) {
                            boolean is_force_update = jsonObject.getBoolean("is_force_update");
                            boolean is_update_available = jsonObject.getBoolean("is_update_available");
                            String message = jsonObject.getString("message");
                            if(is_force_update) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getMyContext());
                                builder.setMessage(message);
                                builder.setCancelable(false);
                                builder.setPositiveButton(getMyContext().getResources().getString(R.string.update), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final String appPackageName = getPackageName();
                                        try {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                        } catch (android.content.ActivityNotFoundException anfe) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                        }
                                    }
                                });
                                builder.show();
                            }

                        } else {
                            String msg = jsonObject.getString("message");
                            toast(msg);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {

                    e.printStackTrace();
                }
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void openSettings() {
        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }
    private ProgressDialog progressDialog;

    public void showLoading(){
        if (progressDialog != null) {
            progressDialog.dismiss();

            progressDialog = new ProgressDialog(this, R.style.ProgressDialogStyle);
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.isShowing();
        }


    }

    public void hideLoading(){
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Nullable
    @Override
    public Context getMyContext() {
        return this;
    }

    @Nullable
    @Override
    public Activity getMyActivity() {
        return this;
    }

    @Nullable
    @Override
    public Fragment getMyFragment() {
        return null;
    }

    /*To show simple dialog*/
    void onInfo(String message) {
        Utils.onInfo(message, this, false);
    }

    void onInfo(String message, Boolean shouldFinish) {
        Utils.onInfo(message, this, shouldFinish);
    }

    /*To show simple dialog*/
    void onInfo(String title, String message) {
        Utils.onInfo(title, message, this, false);
    }

    /*To show simple dialog*/
    void onInfo(String title, String message, Boolean shouldFinish) {
        Utils.onInfo(title, message, this, shouldFinish);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onResume", "onResume");
        if(SharedPreference.getBoolean(getMyContext(), SharedPreference.IS_LOGIN)) {
            checkVersionApiCall();
        }
        log("================================================================================== Activity Name " + this.getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("onPause", "onPause");
        log("================================================================================== Activity Name " + this.getClass().getSimpleName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("onStop", "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy", "onDestroy");
        log("================================================================================== Activity Name " + this.getClass().getSimpleName());
    }
}
