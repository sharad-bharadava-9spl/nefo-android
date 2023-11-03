package com.nefos.ccsmembersapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.StringUtility;
import com.nefos.ccsmembersapp.Util.Utils;
import com.nefos.ccsmembersapp.databinding.ActivityAddNewClubBinding;
import com.nefos.ccsmembersapp.server.ApiClient;
import com.nefos.ccsmembersapp.server.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddNewClubActivity extends BaseActivity implements View.OnClickListener {

    ActivityAddNewClubBinding binding;
    private ProgressDialog progressDialog;
    private ApiClient apiClient;
    private String member_id = "";

    private Boolean goBackOnTap = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_new_club);
        init();
        addListenersOnEditText();
        applyFilterOnEditText();

    }

    void init() {
        apiClient = new ApiClient();

        if (getIntent().hasExtra("member_id")) {
            member_id = getIntent().getStringExtra("member_id");
        }

        progressDialog = new ProgressDialog(this, R.style.ProgressDialogStyle);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void addListenersOnEditText() {
        binding.etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = "";
                if (!StringUtility.validateEditText(binding.etCode)) {
                    value = getString(R.string.please_add_club_code);
                    binding.rlCode.setBackground(ContextCompat.getDrawable(AddNewClubActivity.this,R.drawable.bg_edt_error));
                } else {
                    binding.rlCode.setBackground(ContextCompat.getDrawable(AddNewClubActivity.this,R.drawable.bg_edt));
                }
                binding.tvCode.setText(value);
            }
        });
    }

    void applyFilterOnEditText() {
        String alphaNumericRegX = getString(R.string.alpha_numeric_regx);
        InputFilter.AllCaps allCapsFilter = new InputFilter.AllCaps();
        binding.etCode.setFilters(new InputFilter[]{allCapsFilter, (source, start, end, dest, dStart, dEnd) -> {
            for (int i = 0; i < source.length(); i++) {
                if (source.equals("")) {
                    return source;
                }
                if (source.toString().matches(alphaNumericRegX)) {
                    return source;
                }
                return "";
            }
            return null;
        }});
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDone:
                if (!StringUtility.validateEditText(binding.etCode)) {
                    binding.rlCode.setBackground(ContextCompat.getDrawable(AddNewClubActivity.this,R.drawable.bg_edt_error));
                    binding.etCode.setText("");
                    binding.tvCode.setText(getString(R.string.please_add_club_code));
                } else {
                    binding.rlCode.setBackground(ContextCompat.getDrawable(AddNewClubActivity.this,R.drawable.bg_edt));
                    addClubCall(binding.etCode.getText().toString());
                }
                break;
            case R.id.ivback:
                onBackPressed();
                break;
            case R.id.parentView:
                Utils.closeKeyBoard(binding.parentView, this);
                break;
            case R.id.add_new_club_toast_layout:
                hideStickyToast();
                break;
        }
    }

    private void addClubCall(String clubCode) {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
            binding.btnDone.setEnabled(false);
        }
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.addClubMember(member_id, clubCode, Utils.getNetworkAddress(this), SharedPreference.getSelectedLanguage(this));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                binding.btnDone.setEnabled(true);
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    int flag = jsonObject.getInt("flag");
                    if (flag == 1) {
                        String msg = jsonObject.getString("message");
                        toast(msg);
//                        showStickyToast(msg);
                        goBackOnTap = true;
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String msg = jsonObject.getString("message");
                        toast(msg);
//                        showStickyToast(msg);
                    }

                } catch (JSONException | IOException e) {
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

    private void showStickyToast(String msg){
        binding.addNewClubToastLayout.setVisibility(View.VISIBLE);
        binding.addNewClubToast.setText(msg);
    }

    private void hideStickyToast(){
        binding.addNewClubToastLayout.setVisibility(View.GONE);
        binding.addNewClubToast.setText("");
        if(goBackOnTap){
            setResult(RESULT_OK);
            finish();
        }
    }

}
