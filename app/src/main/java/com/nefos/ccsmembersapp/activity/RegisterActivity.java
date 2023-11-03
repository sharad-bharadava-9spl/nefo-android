package com.nefos.ccsmembersapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.messaging.FirebaseMessaging;
import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.StringUtility;
import com.nefos.ccsmembersapp.Util.Utils;
import com.nefos.ccsmembersapp.databinding.ActivityRegisterBinding;
import com.nefos.ccsmembersapp.server.ApiClient;
import com.nefos.ccsmembersapp.server.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    private EditText edtEmail, edtPassword, edtUsername, edtConfirmPassword;
    private ApiClient apiClient;
    private ProgressDialog progressDialog;
    TextView textEnglish, textSpanish, tvRegister;
    ImageView ivBack;
    private String pushToken = "";
    //int REGISTRATION_SUCCESS_CODE = 101;
    ActivityRegisterBinding binding;
    private RelativeLayout rlPassword, rlEmail, rlUserName, rlRepeatPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFCMToken();

        String selectedLanguage = SharedPreference.getSelectedLanguage(RegisterActivity.this);
        Utils.updateResources(RegisterActivity.this, selectedLanguage);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        pushToken = SharedPreference.getFCMToken(this);
        log("token: " + pushToken);
        try {
            if (pushToken.equals("")) {
                getFCMToken();
                pushToken = SharedPreference.getFCMToken(this);
                log("token: " + pushToken);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        initView();
        addListenersOnEditText();
    }

    private void addListenersOnEditText() {

        binding.edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = "";
                if (!StringUtility.validateEmail(edtEmail.getText().toString())) {
                    value = getResources().getString(R.string.enter_a_valid_email);

                    rlEmail.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt_error));
                } else {
                    rlEmail.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt));
                }
                binding.tvEmail.setText(value);
            }
        });

        binding.edtUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = "";
                if (!StringUtility.validateEditText(binding.edtUsername)) {
                    value = getResources().getString(R.string.please_enter_username);

                    rlUserName.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt_error));
                } else {
                    rlUserName.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt));
                }
                binding.tvUsername.setText(value);
            }
        });

        binding.edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = "";
                if (!StringUtility.validatePassword(edtPassword.getText().toString())) {
                    value = getResources().getString(R.string.password_validation);

                    rlPassword.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt_error));
                } else {
                    rlPassword.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt));
                }
                binding.tvPassword.setText(value);
            }
        });

        binding.edtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = "";
                if (!StringUtility.validateEditText(edtConfirmPassword)) {
                    value = getResources().getString(R.string.please_reenter_password);
                    rlRepeatPassword.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt_error));
                } else if (!edtPassword.getText().toString().equals(edtConfirmPassword.getText().toString())) {
                    value = getResources().getString(R.string.passwords_do_not_match);
                    rlRepeatPassword.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt_error));
                } else {
                    rlRepeatPassword.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt));
                }
                binding.tvRepeatPassword.setText(value);
            }
        });

    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SharedPreference.setFCMToken(RegisterActivity.this, task.getResult());
                pushToken = SharedPreference.getFCMToken(this);
                log("token: " + pushToken);
            }
        });
    }

    private void initView() {
        apiClient = new ApiClient();
        textEnglish = findViewById(R.id.text_english);
        textSpanish = findViewById(R.id.text_spanish);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        tvRegister = findViewById(R.id.tvRegister);
        edtUsername = findViewById(R.id.edtUsername);

        rlEmail = findViewById(R.id.rlEmail);
        rlUserName = findViewById(R.id.rlUserName);
        rlPassword = findViewById(R.id.rlPassword);
        rlRepeatPassword = findViewById(R.id.rlRepeatPassword);

        Button loginButton = findViewById(R.id.button_login);

        ivBack = findViewById(R.id.ivBack);

        loginButton.setOnClickListener(this);

        ivBack.setOnClickListener(view -> onBackPressed());

        progressDialog = new ProgressDialog(RegisterActivity.this, R.style.ProgressDialogStyle);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login:
                if (pushToken == null) {
                    getFCMToken();
                    log("token=" + pushToken);
                }
                if (Utils.isNetworkConnected(RegisterActivity.this)) {
                    if (validate()) {
                        if (progressDialog != null && !progressDialog.isShowing()) {
                            progressDialog.show();
                        }
                        memberRegisterCall();
                    }
                } else {
                    toast(getResources().getString(R.string.no_internet));

                }
                break;
            case R.id.text_english:
                SharedPreference.setSelectedLanguage(RegisterActivity.this, textEnglish.getTag().toString());
                restartActivity();
                break;
            case R.id.text_spanish:
                SharedPreference.setSelectedLanguage(RegisterActivity.this, textSpanish.getTag().toString());
                restartActivity();
                break;
            case R.id.parentView:
            case R.id.parentView2:
                Utils.closeKeyBoard(binding.parentView, this);
                break;
        }
    }

    private void restartActivity() {
        Intent intent = new Intent(RegisterActivity.this, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private boolean validate() {
        boolean value = true;

        if (!StringUtility.validateEmail(edtEmail.getText().toString())) {
            binding.tvEmail.setText(getResources().getString(R.string.enter_a_valid_email));
            rlEmail.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt_error));
            value = false;
        } else {
            binding.tvEmail.setText("");
            rlEmail.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt));
        }
        if (!StringUtility.validateEditText(edtUsername)) {
            binding.tvUsername.setText(getResources().getString(R.string.please_enter_username));
            rlUserName.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt_error));
            value = false;
        } else {
            binding.tvUsername.setText("");
            rlUserName.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt));
        }
        if (!StringUtility.validatePassword(edtPassword.getText().toString())) {
            binding.tvPassword.setText(getResources().getString(R.string.password_validation));
            rlPassword.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt_error));
            value = false;
        } else {
            binding.tvPassword.setText("");
            rlPassword.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt));
        }
        if (edtConfirmPassword.getText().toString().trim().isEmpty()) {
            binding.tvRepeatPassword.setText(getResources().getString(R.string.please_reenter_password));
            rlRepeatPassword.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt_error));
            value = false;
        } else {
            binding.tvRepeatPassword.setText("");
            rlRepeatPassword.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt));
        }
        if(!edtConfirmPassword.getText().toString().trim().isEmpty() && !edtPassword.getText().toString().isEmpty()) {
            if (!edtPassword.getText().toString().equals(edtConfirmPassword.getText().toString())) {
                rlRepeatPassword.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt_error));
                binding.tvRepeatPassword.setText(getResources().getString(R.string.passwords_do_not_match));
                value = false;
            } else {
                binding.tvRepeatPassword.setText("");
                rlRepeatPassword.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt));
            }
        }

        if (value) {
            rlEmail.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt));
            rlUserName.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt));
            rlPassword.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt));
            rlRepeatPassword.setBackground(ContextCompat.getDrawable(RegisterActivity.this,R.drawable.bg_edt));
        }

        return value;
    }

    private void memberRegisterCall() {
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.memberRegister(edtEmail.getText().toString().toLowerCase(), edtUsername.getText().toString(), edtPassword.getText().toString(), Utils.deviceId(RegisterActivity.this), "Android", pushToken, Utils.getNetworkAddress(RegisterActivity.this), SharedPreference.getSelectedLanguage(this));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());
                        if (jsonObject.has("flag")) {
                            int flag = jsonObject.getInt("flag");
                            String msg = jsonObject.getString("message");
                            if (flag == 1) {
                                setResult(RESULT_OK);
                                onBackPressed();
                            } else {
                                toast(msg);
                            }
                        }
                    }

                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

}
