package com.nefos.ccsmembersapp.activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.Utils;
import com.nefos.ccsmembersapp.fragment.PreRegisterDocumentUploadFragment;
import com.nefos.ccsmembersapp.server.ApiClient;
import com.nefos.ccsmembersapp.server.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreRegisterChooseDateAndTimeActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private Context mContext;
    private ApiClient apiClient;
    private ProgressDialog progressDialog;
    Button btnDate, btnConfirm, btnTime;
    private LinearLayout parentView;
    private String selectedDate;
    private ImageView ivBack;
    private String clubName, tempNumber, year, month, day, clubCode;
    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
    private int calYear = calendar.get(Calendar.YEAR);
    private int calMonth = calendar.get(Calendar.MONTH);
    private int calDay =  calendar.get(Calendar.DAY_OF_MONTH);
    private int calHours =  calendar.get(Calendar.HOUR_OF_DAY);
    private int calMinute =  calendar.get(Calendar.MINUTE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_register_choose_date_and_time);

        mContext = this;
        MainActivity.count = 0;
        MainActivity.appBar.setVisibility(View.VISIBLE);
        clubName = getIntent().getStringExtra("club_name");
        clubCode = getIntent().getStringExtra("club_code");
        tempNumber = getIntent().getStringExtra("tempNumber");
        year = getIntent().getStringExtra("year");
        month = getIntent().getStringExtra("month");
        day = getIntent().getStringExtra("day");
        initView();
    }

    private void initView() {
        MainActivity.toolbarTitle.setText(mContext.getResources().getString(R.string.pre_register));
        MainActivity.cartLayout.setVisibility(View.GONE);
        MainActivity.notification.setVisibility(View.GONE);
        MainActivity.notification_rl_main.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(mContext, R.style.ProgressDialogStyle);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        apiClient = new ApiClient();


        btnConfirm = findViewById(R.id.btnConfirm);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        ivBack = findViewById(R.id.ivBack);

        parentView = findViewById(R.id.parentView);
        parentView.setOnClickListener(view1 -> Utils.closeKeyBoard(parentView, PreRegisterChooseDateAndTimeActivity.this));

        ivBack.setOnClickListener(v -> {
            onBackPressed();
        });

        btnDate.setOnClickListener(v -> {
            Locale myLocale = new Locale(SharedPreference.getSelectedLanguage(PreRegisterChooseDateAndTimeActivity.this));
            Locale.setDefault(myLocale);
            DatePickerDialog dialog = new DatePickerDialog(PreRegisterChooseDateAndTimeActivity.this,  R.style.CustomDatePickerDialogTheme, this,
                    calYear, calMonth,
                    calDay);
            dialog.getDatePicker().setMinDate(new Date().getTime());
            dialog.setCancelable(false);
            dialog.show();
        });

        btnTime.setOnClickListener(v -> {
            calHours = calendar.get(Calendar.HOUR_OF_DAY);
            calMinute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(PreRegisterChooseDateAndTimeActivity.this, this,
                    calHours,
                    calMinute,
                    true
            );
            timePickerDialog.show();
        });

        btnConfirm.setOnClickListener(v -> {
            if(checkValidation()) {
                callVisitDateApi();
            }
        });

    }

    private void callVisitDateApi() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
        ApiClient apiClient;
        apiClient = new ApiClient();
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.preRegisterVisitDate(
                SharedPreference.getSelectedLanguage(PreRegisterChooseDateAndTimeActivity.this), selectedDate);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());
                        int flag = jsonObject.getInt("flag");
                        if (flag == 1) {
                            String orderDateDB = jsonObject.getString("order-dateDB");
                            callVisitTimeApi(orderDateDB);
                        } else {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            String msg = jsonObject.getString("message");
                            toast(msg);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void callVisitTimeApi(String orderDateDB) {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
        ApiClient apiClient;
        apiClient = new ApiClient();
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.preRegisterVisitTime(
                SharedPreference.getSelectedLanguage(PreRegisterChooseDateAndTimeActivity.this), btnTime.getText().toString(), clubName);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());
                        int flag = jsonObject.getInt("flag");
                        if (flag == 1) {
                            String orderTime = jsonObject.getString("order-time");
                            Intent  intent = new Intent(PreRegisterChooseDateAndTimeActivity.this, PreRegisterDocumentUploadActivity.class);
                            intent.putExtra("club_name", clubName);
                            intent.putExtra("club_code", clubCode);
                            intent.putExtra("orderTime", orderTime);
                            intent.putExtra("orderDateDB", orderDateDB);
                            intent.putExtra("tempNumber", tempNumber);
                            intent.putExtra("year", year);
                            intent.putExtra("month", month);
                            intent.putExtra("day", day);
                            startActivity(intent);
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
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private boolean checkValidation() {
        if (btnDate.getText().toString().equals(getResources().getString(R.string.please_choose_a_date_for_your_visit))) {
            toast(getResources().getString(R.string.please_select_visit_date));
            return false;
        } else if(btnTime.getText().toString().equals(getResources().getString(R.string.please_choose_a_time_for_your_visit))) {
            toast(getResources().getString(R.string.please_select_visit_time));
            return false;
        }
        return true;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        String month;
        String day;
        calYear = year;
        calMonth = monthOfYear;
        calDay = dayOfMonth;
        if((monthOfYear + 1) < 10) {
            month = "0" + (monthOfYear + 1);
        } else{
            month = String.valueOf((monthOfYear + 1));
        }
        if(dayOfMonth < 10) {
            day = "0" + dayOfMonth;
        } else {
            day = String.valueOf(dayOfMonth);
        }
        selectedDate = day + "-" + month + "-" + year;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date myDate = null;
        try {
            myDate = dateFormat.parse(selectedDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        String formatDate = formatter.format(myDate);
        btnDate.setText(formatDate);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        calHours = hourOfDay;
        calMinute = minute;
        String hours;
        String min;
        if(hourOfDay >= 12 && hourOfDay <= 20) {
            if(hourOfDay < 10) {
                hours = "0" + hourOfDay;
            } else {
                hours = String.valueOf(hourOfDay);
            }
            if(minute < 10) {
                min = "0" + minute;
            } else {
                min = String.valueOf(minute);
            }
            btnTime.setText(hours + ":" + min);
        } else {
            Toast.makeText(PreRegisterChooseDateAndTimeActivity.this, getResources().getString(R.string.please_select_time_between_12_to_20), Toast.LENGTH_SHORT).show();
        }
    }
}