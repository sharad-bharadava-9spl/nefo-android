package com.nefos.ccsmembersapp.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.Utils;
import com.nefos.ccsmembersapp.server.ApiClient;
import com.nefos.ccsmembersapp.server.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class PreRegisterSignAndAcceptActivity extends BaseActivity implements DatePickerDialog.OnDateSetListener  {


    private Context mContext;
    private ApiClient apiClient;
    private ProgressDialog progressDialog;
    private LinearLayout parentView;
    private Button btnSubmit, btnDateOfBirth, btnSignature;
    private CheckBox chkTermThree;
    private ImageView ivSign, ivBack;
    private String imageUrl;
    private String selectedDate;
    private String clubName, clubCode;
    private TextView tvTermAndCondition;
    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
    private int year = calendar.get(Calendar.YEAR);
    private int month = calendar.get(Calendar.MONTH);
    private int day =  calendar.get(Calendar.DAY_OF_MONTH);
    public static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_register_sign_and_accept);

        mContext = this;
        MainActivity.count = 0;
        MainActivity.appBar.setVisibility(View.VISIBLE);
        clubName = getIntent().getStringExtra("club_name");
        clubCode = getIntent().getStringExtra("club_code");
        initView();
    }

    private String readTxt() {
        InputStream inputStream;
        if(SharedPreference.getSelectedLanguage(PreRegisterSignAndAcceptActivity.this).equals(getResources().getString(R.string.en))) {
            inputStream = getResources().openRawResource(R.raw.pre_reg_t_a_c_en);
        } else {
            inputStream = getResources().openRawResource(R.raw.pre_reg_t_a_c_es);
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString();
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

//        chkTermOne = findViewById(R.id.chkTermOne);
        tvTermAndCondition = findViewById(R.id.tvTermAndCondition);
//        chkTermTwo = findViewById(R.id.chkTermTwo);
        chkTermThree = findViewById(R.id.chkTermThree);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnDateOfBirth = findViewById(R.id.btnDateOfBirth);
        btnSignature = findViewById(R.id.btnAddSignature);
        ivSign = findViewById(R.id.ivSign);
        ivBack = findViewById(R.id.ivBack);

        tvTermAndCondition.setText(Html.fromHtml(readTxt()));

        ivBack.setOnClickListener(v -> {
            onBackPressed();
        });

        btnSignature.setOnClickListener(v -> {
            Intent intent = new Intent(PreRegisterSignAndAcceptActivity.this, SignatureActivity.class);
            startActivityForResult(intent, 1001);
        });

        btnDateOfBirth.setOnClickListener(v -> {
            Locale myLocale = new Locale(SharedPreference.getSelectedLanguage(PreRegisterSignAndAcceptActivity.this));
            Locale.setDefault(myLocale);
            DatePickerDialog dialog = new DatePickerDialog(PreRegisterSignAndAcceptActivity.this,
                    R.style.CustomDatePickerDialogTheme, this,
                    year, month, day);
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());

            dialog.setCancelable(false);
            dialog.show();
        });

        parentView = findViewById(R.id.parentView);
        parentView.setOnClickListener(view1 -> Utils.closeKeyBoard(parentView, PreRegisterSignAndAcceptActivity.this));
        btnSubmit.setOnClickListener(v -> {
            if(checkValidation()) {
                callDobSignApi();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            if(resultCode == Activity.RESULT_OK){
                String result = data.getStringExtra("result");
                ivSign.setVisibility(View.VISIBLE);
                imageUrl = result;
                ivSign.setImageBitmap(stringToBitMap(result));
            }
            if (resultCode == Activity.RESULT_CANCELED) {
//                imageUrl = null;
            }
        }
    } //onActivityResult

    public Bitmap stringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }


    private void callDobSignApi() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
        Log.e("clubName", clubName);


        Bitmap bm = stringToBitMap(imageUrl);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); // bm is the bitmap object
        byte[] byteArrayImage = baos.toByteArray();
        String encodedImage = "data:image/png;base64," + Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

        apiClient = new ApiClient();
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.preRegisterDobSign(SharedPreference.getSelectedLanguage(PreRegisterSignAndAcceptActivity.this),
                selectedDate, clubName, encodedImage);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());
                        int flag = jsonObject.getInt("flag");
                        if (flag == 1) {
                            String tempNumber = jsonObject.getString("tempnumber");
                            String year = jsonObject.getString("year");
                            String month = jsonObject.getString("month");
                            String day = jsonObject.getString("day");
                            Intent intent = new Intent(PreRegisterSignAndAcceptActivity.this, PreRegisterChooseDateAndTimeActivity.class);
                            intent.putExtra("club_name", clubName);
                            intent.putExtra("club_code", clubCode);
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
        if (btnDateOfBirth.getText().toString().equals(getResources().getString(R.string.date_of_birth))) {
            toast(getResources().getString(R.string.please_select_date_of_birth));
            return false;
        } else if(imageUrl == null || imageUrl.isEmpty()) {
            toast(getResources().getString(R.string.please_add_your_signature_first));
            return false;
        } else if(!chkTermThree.isChecked()) {
            toast(getResources().getString(R.string.you_have_to_accept));
            return false;
        }
        return true;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        month = monthOfYear;
        day = dayOfMonth;
        String month;
        String day;
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
        selectedDate = year + "-" + month + "-" + day;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date myDate = null;
        try {
            myDate = dateFormat.parse(selectedDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        String formatDate = formatter.format(myDate);
        btnDateOfBirth.setText(formatDate);
    }

}