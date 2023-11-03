package com.nefos.ccsmembersapp.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.StringUtility;
import com.nefos.ccsmembersapp.Util.Utils;
import com.nefos.ccsmembersapp.databinding.ActivityForgotPasswordBinding;
import com.nefos.ccsmembersapp.server.ApiClient;
import com.nefos.ccsmembersapp.server.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends BaseActivity implements View.OnClickListener{

    ActivityForgotPasswordBinding binding;
    private ProgressDialog progressDialog;
    private RelativeLayout rlEmail;
    private EditText edtEmail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password);
        init();
        addListenersOnEditText();
    }

    void init()
    {
        rlEmail = findViewById(R.id.rlEmail);
        edtEmail = findViewById(R.id.edtEmail);
        progressDialog = new ProgressDialog(this, R.style.ProgressDialogStyle);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
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

                    rlEmail.setBackground(ContextCompat.getDrawable(ForgotPasswordActivity.this,R.drawable.bg_edt_error));
                } else {
                    rlEmail.setBackground(ContextCompat.getDrawable(ForgotPasswordActivity.this,R.drawable.bg_edt));
                }
                binding.tvErrorEmail.setText(value);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDone:
                String email = binding.edtEmail.getText().toString();
                if (StringUtility.validateEmail(email))
                {
                    rlEmail.setBackground(ContextCompat.getDrawable(ForgotPasswordActivity.this,R.drawable.bg_edt));
                    if (Utils.isNetworkConnected(this)) {
                            if (progressDialog != null && !progressDialog.isShowing()) {
                                progressDialog.show();
                            }
                            hitApi();
                    } else {
                        toast(getResources().getString(R.string.no_internet));
                    }
                }else
                {
                    binding.tvErrorEmail.setText(getResources().getString(R.string.enter_a_valid_email));
                    rlEmail.setBackground(ContextCompat.getDrawable(ForgotPasswordActivity.this,R.drawable.bg_edt_error));
                }
                break;

            case R.id.ivBack:
                onBackPressed();
                break;
            case R.id.parentView:
                Utils.closeKeyBoard(binding.parentView, this);
                break;
        }
    }

    private void hitApi() {
        ApiInterface apiService = new ApiClient().getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.resetMemberPassword(binding.edtEmail.getText().toString().toLowerCase(),
                Utils.deviceId(this), "Android", SharedPreference.getFCMToken(this),
                Utils.getNetworkAddress(this), SharedPreference.getSelectedLanguage(this));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());

                        int flag = jsonObject.getInt("flag");
                        String msg = jsonObject.getString("message");
                        if (flag == 1) {
                            onInfo(msg,true);
                        } else {
                            onInfo(msg,false);
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
