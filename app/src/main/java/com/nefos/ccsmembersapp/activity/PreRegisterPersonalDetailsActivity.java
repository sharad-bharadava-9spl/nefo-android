package com.nefos.ccsmembersapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.StringUtility;
import com.nefos.ccsmembersapp.Util.Utils;
import com.nefos.ccsmembersapp.server.ApiClient;
import com.nefos.ccsmembersapp.server.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreRegisterPersonalDetailsActivity extends BaseActivity {


    private Context mContext;
    private ApiClient apiClient;
    private ProgressDialog progressDialog;
    private LinearLayout parentView, llDetails;
    private EditText etFirstName, etLastName, etNationality, etDNIOrPassport, etTelephone, etEmail, etStreetName, etPostalCode, etCity, etCountry;
    private Button btnConfirm;
    private RelativeLayout rlFirstName, rlLastName, rlTelephone, rlEmail, rlDNIPassport;
    private TextView tvFirstName, tvLastName, tvTelephone, tvEmail, tvDNIPassport;
    private RadioGroup rdoGenderGrp;
    private RadioGroup rdoUsagesGrp;
    private ImageView ivBack;
    private String clubName, orderTime , orderDateDB, tempNumber, year, month, day, clubCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_register_personal_details);

        clubName = getIntent().getStringExtra("club_name");
        clubCode = getIntent().getStringExtra("club_code");
        orderTime = getIntent().getStringExtra("orderTime");
        orderDateDB = getIntent().getStringExtra("orderDateDB");
        tempNumber = getIntent().getStringExtra("tempNumber");
        year = getIntent().getStringExtra("year");
        month = getIntent().getStringExtra("month");
        day = getIntent().getStringExtra("day");
        mContext = this;
        MainActivity.count = 0;
        MainActivity.appBar.setVisibility(View.VISIBLE);
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

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etNationality = findViewById(R.id.etNationality);
        etDNIOrPassport = findViewById(R.id.etDniOrOPassport);
        etTelephone = findViewById(R.id.etTelephone);
        etEmail = findViewById(R.id.etEmail);
        etEmail.setText(SharedPreference.getString(PreRegisterPersonalDetailsActivity.this, SharedPreference.PREF_KEY_USER_EMAIL));
        etStreetName = findViewById(R.id.etStreetName);
        etPostalCode = findViewById(R.id.etPostalCode);
        etCity = findViewById(R.id.etCity);
        etCountry = findViewById(R.id.etCountry);
        btnConfirm = findViewById(R.id.btnConfirm);
        rlFirstName = findViewById(R.id.rlFirstName);
        rlLastName = findViewById(R.id.rlLastName);
        rlTelephone = findViewById(R.id.rlTelephone);
        rlEmail = findViewById(R.id.rlEmail);
        rlDNIPassport = findViewById(R.id.rlDniOrOPassport);
        tvFirstName = findViewById(R.id.tvFirstName);
        tvLastName = findViewById(R.id.tvLastName);
        tvTelephone = findViewById(R.id.tvTelephone);
        tvEmail = findViewById(R.id.tvEmail);
        tvDNIPassport = findViewById(R.id.tvDNIPassport);
        rdoGenderGrp = findViewById(R.id.radioGrp);
        rdoUsagesGrp = findViewById(R.id.radioGrpUsage);
        ivBack = findViewById(R.id.ivBack);
        llDetails = findViewById(R.id.llDetails);

        ivBack.setOnClickListener(v -> {
            onBackPressed();
        });

        parentView = findViewById(R.id.parentView);
        parentView.setOnClickListener(v -> {
            Utils.closeKeyBoard(parentView, PreRegisterPersonalDetailsActivity.this);
        });
        llDetails.setOnClickListener(v -> {
            Utils.closeKeyBoard(parentView, PreRegisterPersonalDetailsActivity.this);
        });
        btnConfirm.setOnClickListener(v -> {
            if(checkValidation()) {
                callPersonalDetailsAPI();
            }
        });

    }

    private boolean checkValidation() {
        boolean value = true;
        if (etFirstName.getText().toString().isEmpty()) {
            tvFirstName.setText(getResources().getString(R.string.please_enter_first_name));
            rlFirstName.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this, R.drawable.bg_edt_error));
            value = false;
        } else {
            tvFirstName.setText("");
            rlFirstName.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this, R.drawable.bg_edt));
        }
        if (etLastName.getText().toString().isEmpty()) {
            tvLastName.setText(getResources().getString(R.string.please_enter_last_name));
            rlLastName.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this, R.drawable.bg_edt_error));
            value = false;
        } else {
            tvLastName.setText("");
            rlLastName.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this, R.drawable.bg_edt));
        }
        if (etDNIOrPassport.getText().toString().isEmpty()) {
            tvDNIPassport.setText(getResources().getString(R.string.please_enter_dni_and_passport));
            rlDNIPassport.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this, R.drawable.bg_edt_error));
            value = false;
        } else {
            tvDNIPassport.setText("");
            rlDNIPassport.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this, R.drawable.bg_edt));
        }
//        int selectedUsagesId = rdoUsagesGrp.getCheckedRadioButtonId();
//        if(selectedUsagesId == -1) {
//            value = false;
//            toast(getResources().getString(R.string.please_choose_usage));
//        }
        if (etTelephone.getText().toString().isEmpty()) {
            tvTelephone.setText(getResources().getString(R.string.please_enter_telephone));
            rlTelephone.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this, R.drawable.bg_edt_error));
            value = false;
        } else {
            tvTelephone.setText("");
            rlTelephone.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this, R.drawable.bg_edt));
        }
        if (etTelephone.getText().toString().length() < 8) {
            tvTelephone.setText(getResources().getString(R.string.please_enter_valid_telephone));
            rlTelephone.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this, R.drawable.bg_edt_error));
            value = false;
        } else {
            tvTelephone.setText("");
            rlTelephone.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this, R.drawable.bg_edt));
        }
        if (!StringUtility.validateEmail(etEmail.getText().toString())) {
            tvEmail.setText(getResources().getString(R.string.enter_a_valid_email));
            rlEmail.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this, R.drawable.bg_edt_error));
            value = false;
        } else {
            tvEmail.setText("");
            rlEmail.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this, R.drawable.bg_edt));
        }
        if (value) {
            rlFirstName.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this,R.drawable.bg_edt));
            rlLastName.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this,R.drawable.bg_edt));
            rlTelephone.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this,R.drawable.bg_edt));
            rlEmail.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this,R.drawable.bg_edt));
            rlDNIPassport.setBackground(ContextCompat.getDrawable(PreRegisterPersonalDetailsActivity.this,R.drawable.bg_edt));
        }
        return value;
    }

    private void callPersonalDetailsAPI() {

        String nationality;
        if(etNationality.getText().toString().trim().isEmpty()) {
            nationality = "";
        } else {
            nationality = etNationality.getText().toString().trim();
        }
        String streetName;
        if(etStreetName.getText().toString().trim().isEmpty()) {
            streetName = "";
        } else {
            streetName = etStreetName.getText().toString().trim();
        }
        String postalCode;
        if(etPostalCode.getText().toString().trim().isEmpty()) {
            postalCode = "";
        } else {
            postalCode = etPostalCode.getText().toString().trim();
        }
        String city;
        if(etCity.getText().toString().trim().isEmpty()) {
            city = "";
        } else {
            city = etCity.getText().toString().trim();
        }
        String country;
        if(etCountry.getText().toString().trim().isEmpty()) {
            country = "";
        } else {
            country = etCountry.getText().toString().trim();
        }

        String gender;
        int selectedGenderId = rdoGenderGrp.getCheckedRadioButtonId();
        RadioButton rdoGender;
        rdoGender = findViewById(selectedGenderId);
        if(selectedGenderId == -1) {
            gender = "";
        } else {
            gender = rdoGender.getText().toString();
        }

        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
        apiClient = new ApiClient();
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.preRegisterPersonalDetails(
                SharedPreference.getSelectedLanguage(PreRegisterPersonalDetailsActivity.this), etFirstName.getText().toString(),
                etLastName.getText().toString().trim(), etEmail.getText().toString().trim(),
                nationality, gender, streetName,
                "", "", postalCode,
                city, country,
                etTelephone.getText().toString().trim(), etDNIOrPassport.getText().toString().trim(), Utils.deviceId(PreRegisterPersonalDetailsActivity.this),  "Android",
                SharedPreference.getFCMToken(PreRegisterPersonalDetailsActivity.this), clubName,day, month, year, tempNumber, orderDateDB, orderTime,
                SharedPreference.getMemberId(PreRegisterPersonalDetailsActivity.this),
                clubCode
        );
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());
                        int flag = jsonObject.getInt("flag");
                        if (flag == 1) {
                            String msg = jsonObject.getString("message");
                            toast(msg);
                            Intent intent;
                            intent = new Intent(PreRegisterPersonalDetailsActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
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
}