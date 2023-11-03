package com.nefos.ccsmembersapp.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nefos.ccsmembersapp.BuildConfig;
import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.StringUtility;
import com.nefos.ccsmembersapp.Util.Utils;
import com.nefos.ccsmembersapp.databinding.ActivityLoginBinding;
import com.nefos.ccsmembersapp.server.ApiClient;
import com.nefos.ccsmembersapp.server.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginAcvitiy extends BaseActivity implements View.OnClickListener {
    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    public static boolean selectedTitle = false;
    final int REGISTER_CODE = 101;
    TextView textEnglish, textSpanish, tvRegister;
    ImageView ivBack;
    ActivityLoginBinding binding;
    Boolean rememberMe = false;
    private EditText edtEmail, edtPassword;
    private ApiClient apiClient;
    private ProgressDialog progressDialog;
    private RelativeLayout parentView;
    private RelativeLayout rlPassword, rlEmail;
    private Context mContext;
    private boolean isCallCheckVersion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        if(SharedPreference.getString(this, SharedPreference.IS_USER_FIRST_TIME).isEmpty()) {
            SharedPreference.setString(this, SharedPreference.IS_USER_FIRST_TIME, "1");
        } else {
            SharedPreference.setString(this, SharedPreference.IS_USER_FIRST_TIME, "2");
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

       /* if (Build.BRAND.equalsIgnoreCase("xiaomi") || Build.BRAND.equalsIgnoreCase("Redmi")) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            startActivity(intent);
        }*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (this.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            NotificationChannel channel = new NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            if(SharedPreference.getString(this, SharedPreference.IS_USER_FIRST_TIME).equals("1")) {
                // If not, show the system notification settings screen
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                if (!notificationManager.areNotificationsEnabled()) {
                    // If not, show the system notification settings screen
                    Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }


        initView();
        addListenersOnEditText();
        validateRememberMeData();
        checkIfLanguageChange();

        SharedPreference.clearClubNameCredentails(LoginAcvitiy.this);
        SharedPreference.clearUserData(LoginAcvitiy.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 22) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted, perform required code
                } else {
                    // not granted
                }
            }
        }
    }

    private void removeFcmTokenCall(String prevMemberId, String prevToken) {
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.removeFcmToke(SharedPreference.getSelectedLanguage(LoginAcvitiy.this),
                prevMemberId, prevToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());
                        int flag = jsonObject.getInt("flag");
                        getFCMToken();
                    }
                } catch (JSONException | IOException e) {
                    getFCMToken();
                    e.printStackTrace();
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

    void updateLanguage() {
        String selectedLanguage = SharedPreference.getSelectedLanguage(LoginAcvitiy.this);
        Utils.updateResources(LoginAcvitiy.this, selectedLanguage);
        if (selectedLanguage.equals("en")) {
            setLanguage(0);
        } else {
            setLanguage(1);
        }
        Locale locale = new Locale(selectedLanguage);
        Locale.setDefault(locale);
        Resources resources = LoginAcvitiy.this.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        //to update current view
        binding.textEnglish.setText(getString(R.string.english));
        binding.textSpanish.setText(getString(R.string.spanish));
        binding.textEmail.setText(getString(R.string.email));
        binding.edtEmail.setHint(getString(R.string.email_hint));
        binding.textPassword.setText(getString(R.string.password));
        binding.edtPassword.setHint(getString(R.string.password));
        binding.buttonLogin.setText(getString(R.string.login));
        binding.textRememberMe.setText(getString(R.string.remember_me));
        binding.tvForgotPassword.setText(getString(R.string.forgot_password));
        binding.textNotMemberYet.setText(getString(R.string.not_a_member_yet));
        binding.tvRegister.setText(getString(R.string.register_here));
    }

    private void validateRememberMeData() {
        rememberMe = SharedPreference.getBoolean(this, SharedPreference.PREF_KEY_REMEMBER_ME);
        String email = SharedPreference.getString(this, SharedPreference.PREF_KEY_EMAIL);
        String password = SharedPreference.getString(this, SharedPreference.PREF_KEY_PASSWORD);
        if (rememberMe) {
            binding.edtEmail.setText(email);
            binding.edtPassword.setText(password);
        }
        validateRememberMeView();
    }

    private void checkIfLanguageChange() {
        if (getIntent().hasExtra("email")) {
            binding.edtEmail.setText(getIntent().getStringExtra("email"));
        }
        if (getIntent().hasExtra("password")) {
            binding.edtPassword.setText(getIntent().getStringExtra("password"));
        }
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

                    rlEmail.setBackground(ContextCompat.getDrawable(LoginAcvitiy.this, R.drawable.bg_edt_error));
                } else {
                    rlEmail.setBackground(ContextCompat.getDrawable(LoginAcvitiy.this, R.drawable.bg_edt));
                }
                binding.tvErrorEmail.setText(value);
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
                if (!StringUtility.validateEditText(edtPassword)) {
                    value = getResources().getString(R.string.please_enter_password);

                    rlPassword.setBackground(ContextCompat.getDrawable(LoginAcvitiy.this, R.drawable.bg_edt_error));
                } else {
                    rlPassword.setBackground(ContextCompat.getDrawable(LoginAcvitiy.this, R.drawable.bg_edt));
                }
                binding.tvErrorPassword.setText(value);
            }
        });

    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SharedPreference.setFCMToken(LoginAcvitiy.this, task.getResult());
                loginCall(task.getResult());
            }
        });
    }

    private void initView() {
        apiClient = new ApiClient();
        textEnglish = findViewById(R.id.text_english);
        textSpanish = findViewById(R.id.text_spanish);

        rlEmail = findViewById(R.id.rlEmail);
        rlPassword = findViewById(R.id.rlPassword);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        tvRegister = findViewById(R.id.tvRegister);

        Button loginButton = findViewById(R.id.button_login);

        ivBack = findViewById(R.id.ivback);
        ivBack.setVisibility(View.GONE);

        loginButton.setOnClickListener(this);
        textEnglish.setOnClickListener(this);
        textSpanish.setOnClickListener(this);
        tvRegister.setOnClickListener(this);

        updateLanguage();

        ivBack.setOnClickListener(view -> onBackPressed());

        progressDialog = new ProgressDialog(LoginAcvitiy.this, R.style.ProgressDialogStyle);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        parentView = findViewById(R.id.parentView);
    }

    private void setLanguage(int position) {
        if (position == 0) {
            textEnglish.setBackground(ContextCompat.getDrawable(this, R.drawable.language_selected_bg_left));
            textSpanish.setBackground(ContextCompat.getDrawable(this, R.drawable.language_unselected_bg_right));
        } else {
            textSpanish.setBackground(ContextCompat.getDrawable(this, R.drawable.language_selected_bg_right));
            textEnglish.setBackground(ContextCompat.getDrawable(this, R.drawable.language_unselected_bg_left));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login:
                Utils.closeKeyBoard(parentView, this);
                if (Utils.isNetworkConnected(LoginAcvitiy.this)) {
                    if (validate()) {
                        if (progressDialog != null && !progressDialog.isShowing()) {
                            progressDialog.show();
                        }
                        String prevMemberId = SharedPreference.getMemberId(LoginAcvitiy.this);
                        String prevToken = SharedPreference.getFCMToken(LoginAcvitiy.this);
                        removeFcmTokenCall(prevMemberId, prevToken);
                    }
                } else {
                    toast(getResources().getString(R.string.no_internet));
                }
                break;
            case R.id.text_english:
                SharedPreference.setSelectedLanguage(LoginAcvitiy.this, textEnglish.getTag().toString());
                updateLanguage();
//                restartActivity();
                break;
            case R.id.text_spanish:
                SharedPreference.setSelectedLanguage(LoginAcvitiy.this, textSpanish.getTag().toString());
                updateLanguage();
//                restartActivity();
                break;
            case R.id.tvRegister:
                Utils.closeKeyBoard(parentView, this);
                startActivityForResult(new Intent(LoginAcvitiy.this, RegisterActivity.class), REGISTER_CODE);
                break;
            case R.id.rlRegisterSuccess:
                binding.rlRegisterSuccess.setVisibility(View.GONE);
                break;
            case R.id.llRemember:
                if (rememberMe) {
                    rememberMe = false;
                    SharedPreference.setBoolean(LoginAcvitiy.this, SharedPreference.PREF_KEY_REMEMBER_ME, false);
                } else {
                    rememberMe = true;
                    SharedPreference.setBoolean(LoginAcvitiy.this, SharedPreference.PREF_KEY_REMEMBER_ME, true);
                }
                validateRememberMeView();
                break;
            case R.id.tvForgotPassword:
                startActivity(new Intent(LoginAcvitiy.this, ForgotPasswordActivity.class));
                break;
            case R.id.parentView:
            case R.id.parentView2:
                Utils.closeKeyBoard(parentView, this);
                break;
        }
    }

    void validateRememberMeView() {
        if (rememberMe) {
            binding.ivRemember.setImageResource(R.drawable.ic_check_enable);
        } else {
            binding.ivRemember.setImageResource(R.drawable.ic_check_disable);
        }
    }

    private boolean validate() {
        boolean value = true;
        if (!StringUtility.validateEmail(edtEmail.getText().toString())) {
            rlEmail.setBackground(ContextCompat.getDrawable(LoginAcvitiy.this, R.drawable.bg_edt_error));
            binding.tvErrorEmail.setText(getResources().getString(R.string.enter_a_valid_email));
            value = false;
        } else {
            rlEmail.setBackground(ContextCompat.getDrawable(LoginAcvitiy.this, R.drawable.bg_edt));
            binding.tvErrorEmail.setText("");
        }
        if (!StringUtility.validateEditText(edtPassword)) {
            binding.tvErrorPassword.setText(getResources().getString(R.string.please_enter_password));
            rlPassword.setBackground(ContextCompat.getDrawable(LoginAcvitiy.this, R.drawable.bg_edt_error));
            value = false;
        } else {
            binding.tvErrorPassword.setText("");
            rlPassword.setBackground(ContextCompat.getDrawable(LoginAcvitiy.this, R.drawable.bg_edt));
        }
        if (value) {
            rlEmail.setBackground(ContextCompat.getDrawable(LoginAcvitiy.this, R.drawable.bg_edt));
            rlPassword.setBackground(ContextCompat.getDrawable(LoginAcvitiy.this, R.drawable.bg_edt));
        }

        return value;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isCallCheckVersion) {
            checkVersionApiCall();
        }
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
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());
                        int flag = jsonObject.getInt("flag");

                        if (flag == 1) {
                            boolean is_force_update = jsonObject.getBoolean("is_force_update");
                            boolean is_update_available = jsonObject.getBoolean("is_update_available");
                            String message = jsonObject.getString("message");
                            isCallCheckVersion = !is_force_update;
                            if (is_update_available) {
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getMyContext());
                                builder.setMessage(message);
                                builder.setCancelable(false);
                                builder.setPositiveButton(mContext.getResources().getString(R.string.update), new DialogInterface.OnClickListener() {
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
                                if (!is_force_update) {
                                    builder.setNegativeButton(mContext.getResources().getString(R.string.not_now), (dialog, which) -> dialog.dismiss());
                                }
                                builder.show();
                            }

                        } else {
                            String msg = jsonObject.getString("message");
                            toast(msg);
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

    private void loginCall(String token) {
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.login(edtEmail.getText().toString().toLowerCase(), edtPassword.getText().toString(), Utils.deviceId(LoginAcvitiy.this), "Android",
                token, Utils.getNetworkAddress(LoginAcvitiy.this), SharedPreference.getSelectedLanguage(this));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());

                        int flag = jsonObject.getInt("flag");
                        if (flag == 1) {

                            String member_id = jsonObject.getString("member_id");

                            SharedPreference.setMemberId(LoginAcvitiy.this, member_id);

                            JSONObject jsonObject1 = jsonObject.optJSONObject("data");

                            SharedPreference.setBoolean(LoginAcvitiy.this, SharedPreference.IS_LOGIN, true);

                            SharedPreference.setString(LoginAcvitiy.this, SharedPreference.CHAT_QR_CODE, jsonObject1.optString("qrcode"));

                            if (jsonObject1 != null) {
                                SharedPreference.setString(LoginAcvitiy.this, SharedPreference.PREF_KEY_USER_EMAIL, jsonObject1.optString("email"));
                                SharedPreference.setString(LoginAcvitiy.this, SharedPreference.PREF_KEY_USER_NAME, jsonObject1.optString("username"));
                                SharedPreference.setString(LoginAcvitiy.this, SharedPreference.PREF_KEY_USER_DATA_EMAIL, jsonObject1.optString("email"));
                            }
                            if (rememberMe) {
                                SharedPreference.setString(LoginAcvitiy.this, SharedPreference.PREF_KEY_EMAIL, binding.edtEmail.getText().toString());
                                SharedPreference.setString(LoginAcvitiy.this, SharedPreference.PREF_KEY_PASSWORD, binding.edtPassword.getText().toString());
                                SharedPreference.setBoolean(LoginAcvitiy.this, SharedPreference.PREF_KEY_REMEMBER_ME, true);

                                SharedPreference.setLoggedIn(LoginAcvitiy.this);
                            } else {
                                SharedPreference.setString(LoginAcvitiy.this, SharedPreference.PREF_KEY_EMAIL, "");
                                SharedPreference.setString(LoginAcvitiy.this, SharedPreference.PREF_KEY_PASSWORD, "");
                                SharedPreference.setBoolean(LoginAcvitiy.this, SharedPreference.PREF_KEY_REMEMBER_ME, false);
                            }
                            getGoogleAuthToken(member_id, jsonObject1.optString("email"), "LOGIN",
                                    token,
                                    jsonObject1.optString("username"));
                        } else {
                            String msg = jsonObject.getString("message");
                            toast(msg);
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

    public void getGoogleAuthToken(String member_id, String email, String status, String fcmToken, String username) {
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.getGoogleAuthToken();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());
                        int flag = jsonObject.getInt("flag");
                        if(flag == 1) {
                            String googleAuthKey = jsonObject.getString("message");
                            SharedPreference.setString(LoginAcvitiy.this, SharedPreference.GOOGLE_AUTH_TOKEN, googleAuthKey);
                            addDataInFirebase(member_id, email, status, fcmToken, username);
                        }
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
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

    private void addDataInFirebase(String member_id, String email, String status, String fcmToken, String username) {
        HashMap<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("name", username);
        userData.put("status", status);
        userData.put("token", fcmToken);
//        userData.put("isOnline", true);
        DatabaseReference userDetailsDatabaseReference;
        userDetailsDatabaseReference = FirebaseDatabase.getInstance().getReference(ApiClient.users);
        userDetailsDatabaseReference.child(member_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null) {
                    userDetailsDatabaseReference.child(member_id).setValue(userData);
                } else {
                    userDetailsDatabaseReference.child(member_id).updateChildren(userData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Intent intent;
        intent = new Intent(LoginAcvitiy.this, MainActivity.class);
        Intent i = getIntent();

        if (i != null) {
            if (i.hasExtra("From")) {
                intent.putExtra("From", "PushNoti");
                Log.e("From", "PushNoti");
            }
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REGISTER_CODE) {
                binding.rlRegisterSuccess.setVisibility(View.VISIBLE);
                binding.textMessage.setText(getResources().getString(R.string.registered_successfully_));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreference.setString(this, SharedPreference.IS_USER_FIRST_TIME, "2");
    }
}
