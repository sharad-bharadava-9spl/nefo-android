package com.nefos.ccsmembersapp.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SearchableSpinner;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.StringUtility;
import com.nefos.ccsmembersapp.Util.Utils;
import com.nefos.ccsmembersapp.adpater.ClubAdapter;
import com.nefos.ccsmembersapp.dialog.CustomAlertDialog;
import com.nefos.ccsmembersapp.interfaces.IDialog;
import com.nefos.ccsmembersapp.interfaces.OnClubClickListener;
import com.nefos.ccsmembersapp.model.ClubModel;
import com.nefos.ccsmembersapp.model.MyExtraData;
import com.nefos.ccsmembersapp.model.User;
import com.nefos.ccsmembersapp.server.ApiClient;
import com.nefos.ccsmembersapp.server.ApiInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddSelectClubActivity extends BaseActivity implements View.OnClickListener, OnClubClickListener, IDialog {
    private final String TAG = AddSelectClubActivity.class.getSimpleName();
    private String selectedLanguage = "";
    private ApiClient apiClient;
    private ProgressDialog progressDialog;
    TextView tvAddClub, tvNoClub, toolbarTitle;
    ImageView ivback;
    private String pushToken = "";
    ArrayList<ClubModel> spinnerValue;
    private SearchableSpinner spinnerClubName;
    //public static boolean selectedTitle = false;
    //private String selectedClubName = "";
    private String member_id = "";
    public static ClubModel selectedClubModel;
    Dialog dialog;
    private RelativeLayout rlClubDropDown;
    private boolean isLogin, fromPushNotification = false;
    RecyclerView recyclerView;
    ClubAdapter adapter;
    final int ADD_NEW_CLUB_CODE = 101;
    AppCompatTextView tvWelcome, tvToGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedClubModel = null;
        getFCMToken();
        selectedLanguage = SharedPreference.getSelectedLanguage(AddSelectClubActivity.this);
        Utils.updateResources(AddSelectClubActivity.this, selectedLanguage);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_add_select_club);
        initView();
        setRecyclerViewData();
        if (isLogin) {
            //onInfo("Show welcome message here");
            ivback.setVisibility(View.GONE);
        } else {
            ivback.setVisibility(View.VISIBLE);
        }
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SharedPreference.setFCMToken(AddSelectClubActivity.this, task.getResult());
                pushToken = SharedPreference.getFCMToken(this);
                log("token: " + pushToken);
            }
        });
    }

    private void initView() {
        if(getIntent().hasExtra("fromPushNotification")){
            fromPushNotification = getIntent().getBooleanExtra("fromPushNotification", false);
        }

        isLogin = getIntent().getBooleanExtra("isLogin", false);

        dialog = new Dialog(this);

        if (getIntent().hasExtra("member_id")) {
            member_id = getIntent().getStringExtra("member_id");
        }

        apiClient = new ApiClient();

        tvAddClub = findViewById(R.id.tvAddClub);
        tvNoClub = findViewById(R.id.tvNoClub);
        rlClubDropDown = findViewById(R.id.rlClubDropDown);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvToGetStarted = findViewById(R.id.tvToGetStarted);
        toolbarTitle = findViewById(R.id.toolbar_title);

        Button loginButton = findViewById(R.id.button_login);

        ivback = findViewById(R.id.ivback);

        loginButton.setOnClickListener(this);
        tvAddClub.setOnClickListener(this);

        ivback.setOnClickListener(view -> onBackPressed());

        progressDialog = new ProgressDialog(AddSelectClubActivity.this, R.style.ProgressDialogStyle);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        spinnerClubName = findViewById(R.id.spinner_clubname);
        recyclerView = findViewById(R.id.recyclerView);

        spinnerClubName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //selectedTitle = true;
                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                //selectedClubName = String.valueOf(parent.getItemAtPosition(position));
                //Log.w("selectedClub", "" + selectedClubName);
                selectedClubModel = ((ClubModel) parent.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        apiGetClubCall();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login:
                validateDataAndHitApi();
                break;
            case R.id.tvAddClub:
                showAddClubDialog();
                break;

            case R.id.llAddNewClub:
                Intent intent = new Intent(this, AddNewClubActivity.class);
                intent.putExtra("member_id", member_id);
                startActivityForResult(intent, ADD_NEW_CLUB_CODE);
                break;
        }
    }

    void validateDataAndHitApi() {
        if (Utils.isNetworkConnected(AddSelectClubActivity.this)) {
            if (validate()) {
                if (progressDialog != null && !progressDialog.isShowing()) {
                    progressDialog.show();
                }
                clubAccess();
            }
        } else {
            toast(getResources().getString(R.string.no_internet));
        }
    }

    private void setRecyclerViewData() {
        spinnerValue = new ArrayList<>();
        adapter = new ClubAdapter(this, spinnerValue, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void showAddClubDialog() {
        dialog.setContentView(R.layout.add_club_dialog);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView imgCancel = dialog.findViewById(R.id.imgCancel);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);
        EditText edtClub = dialog.findViewById(R.id.edtClub);
        imgCancel.setOnClickListener(v -> dialog.dismiss());
        btnSubmit.setOnClickListener(v -> {
            if (!StringUtility.validateEditText(edtClub)) {
                toast(getString(R.string.please_add_club_code));
                edtClub.setText("");
            } else {
                addClubCall(edtClub.getText().toString());
            }
        });

        dialog.show();
    }

    private boolean validate() {
        boolean value = true;

        if (selectedClubModel == null) {
            toast(getResources().getString(R.string.please_select_club_name));
            value = false;
        } else if (selectedClubModel != null && selectedClubModel.getClubApprove().equals("0")) {
            toast(getString(R.string.awaiting_club_approval));
            value = false;
        }

        return value;
    }

    /*private boolean isSelectClub() {
        return selectedTitle;
    }*/

    private void apiGetClubCall() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }

        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        log(String.valueOf(apiService));
        Call<ResponseBody> call = apiService.clubMemberList(member_id, Utils.getNetworkAddress(AddSelectClubActivity.this), SharedPreference.getSelectedLanguage(this));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response.body().string()));
                    int flag = jsonObject.getInt("flag");
                    if (flag == 1) {
                        JSONArray jsonArray = jsonObject.getJSONArray("data");

                        //spinnerValue = new ArrayList<>();
                        spinnerValue.clear();

                        String clubName = SharedPreference.getClubName(AddSelectClubActivity.this);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            ClubModel clubModel = new ClubModel();
                            clubModel.setClubName(jsonArray.getJSONObject(i).getString("club_name"));
                            clubModel.setClubApprove(jsonArray.getJSONObject(i).getString("club_approve"));
                            clubModel.setClubCode(jsonArray.getJSONObject(i).getString("club_code"));
                            clubModel.city = jsonArray.getJSONObject(i).getString("city");
                            clubModel.state = jsonArray.getJSONObject(i).getString("state");
                            clubModel.country = jsonArray.getJSONObject(i).getString("country");
                            clubModel.registeredSince = jsonArray.getJSONObject(i).getString("registeredSince");
                            clubModel.userGroup = jsonArray.getJSONObject(i).getString("usergroup");
                            clubModel.lat = jsonArray.getJSONObject(i).getDouble("lat");
                            clubModel.lng = jsonArray.getJSONObject(i).getDouble("lat");

                            spinnerValue.add(clubModel);
                            if (StringUtility.validateString(clubName)) {
                                if (clubName.equalsIgnoreCase(clubModel.getClubName())) {
                                    selectedClubModel = clubModel;
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }

                        if (spinnerValue.isEmpty()) {
                            tvWelcome.setVisibility(View.VISIBLE);
                            tvToGetStarted.setVisibility(View.VISIBLE);

                            toolbarTitle.setText(getString(R.string.add_your_first_club));
                        } else {
                            tvWelcome.setVisibility(View.GONE);
                            tvToGetStarted.setVisibility(View.GONE);

                            toolbarTitle.setText(getString(R.string.your_clubs));
                        }

                        rlClubDropDown.setVisibility(View.VISIBLE);
                        tvNoClub.setVisibility(View.GONE);

                        ArrayAdapter<ClubModel> spinnerArrayAdapter = new ArrayAdapter<>
                                (AddSelectClubActivity.this, android.R.layout.simple_spinner_item,
                                        spinnerValue); //selected item will look like a spinner set from XML
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                                .simple_spinner_dropdown_item);

                        // Applying the adapter to our spinner
                        spinnerClubName.setAdapter(spinnerArrayAdapter);
                        adapter.notifyDataSetChanged();
                    } else {
                        rlClubDropDown.setVisibility(View.GONE);
                        tvNoClub.setVisibility(View.VISIBLE);

                        spinnerValue.clear();
                        adapter.notifyDataSetChanged();
                        tvWelcome.setVisibility(View.VISIBLE);
                        tvToGetStarted.setVisibility(View.VISIBLE);

                        toolbarTitle.setText(getString(R.string.add_your_first_club));

                        String msg = jsonObject.getString("message");
                        //toast(msg);
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

    private void addClubCall(String clubCode) {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }

        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.addClubMember(member_id, clubCode, Utils.getNetworkAddress(AddSelectClubActivity.this), SharedPreference.getSelectedLanguage(this));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    int flag = jsonObject.getInt("flag");
                    if (flag == 1) {
                        String msg = jsonObject.getString("message");
                        toast(msg);
                        dialog.dismiss();
                        apiGetClubCall();
                    } else {
                        String msg = jsonObject.getString("message");
                        toast(msg);
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

    private void clubAccess() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }

        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.clubAccess(member_id, selectedClubModel.getClubName(), Utils.deviceId(AddSelectClubActivity.this), "Android", pushToken, Utils.getNetworkAddress(AddSelectClubActivity.this), SharedPreference.getClubName(this), getUserId(), SharedPreference.getSelectedLanguage(this));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    int flag = jsonObject.getInt("flag");
                    if (flag == 1) {
                        currentClub();
                        String cart_count = jsonObject.getString("cart_count");
                        JSONObject data = jsonObject.getJSONObject("data");
                        String user_id = data.getString("user_id");
                        String email = data.getString("email");
                        String memberno = data.getString("memberno");
                        String first_name = data.getString("first_name");
                        String userGroup = data.getString("userGroup");
                        String workStation = data.getString("workStation");
                        String domain = data.getString("domain");
                        String image = data.getString("image");
                        String user_type = data.getString("user_type");
                        String memberNickname = data.getString("member_nickname");

                        User user = new User();
                        user.setUserId(Integer.parseInt(user_id));
                        user.setEmail(email);
                        user.setMemberNo(memberno);
                        user.setName(first_name);
                        user.setUserGroup(userGroup);
                        user.setWorkStation(workStation);
                        user.setDomain(domain);
                        user.setImage(image);
                        user.setPushToken(pushToken);
                        user.setUserType(user_type);
                        user.setCartCount(cart_count);
                        user.setMemberNickname(memberNickname);

                        Gson gson = new Gson();

                        ArrayList<User> userList = SharedPreference.getUserListPref(AddSelectClubActivity.this, "userList");
                        boolean isLoggedIn = false;

                        if (isLogin) {

                            if (userList != null && !userList.isEmpty()) {

                                for (int i = 0; i < userList.size(); i++) {
                                    if (user.getUserId() == userList.get(i).getUserId()) {
                                        isLoggedIn = true;
                                        Collections.swap(userList, i, 0);
                                        SharedPreference.setUserListPref(AddSelectClubActivity.this, "userList", userList);
                                    }
                                }


                            }
                            if (!isLoggedIn) {
                                if (userList == null || userList.isEmpty()) {
                                    userList = new ArrayList<>();
                                }
                                userList.add(0, user);
                                SharedPreference.setUserListPref(AddSelectClubActivity.this, "userList", userList);

                            }


                            saveUserDetails(user, gson);
                        } else {
                            if (!userList.isEmpty()) {
                                for (int i = 0; i < userList.size(); i++) {
                                    if (user.getDomain().equals(userList.get(i).getDomain())) {
                                        isLoggedIn = true;
                                    }
                                }
                            }
                            if (isLoggedIn) {
                                //toast(getResources().getString(R.string.account_already_added));

                                if (userList != null && !userList.isEmpty()) {

                                    for (int i = 0; i < userList.size(); i++) {
                                        if (user.getUserId() == userList.get(i).getUserId()) {
                                            Collections.swap(userList, i, 0);
                                            SharedPreference.setUserListPref(AddSelectClubActivity.this, "userList", userList);
                                        }
                                    }
                                }

                                saveUserDetails(user, gson);

                            } else {
                                userList.add(0, user);
                                SharedPreference.setUserListPref(AddSelectClubActivity.this, "userList", userList);
                                SharedPreference.setClubName(AddSelectClubActivity.this, selectedClubModel.getClubName());
                                String userDataJson = gson.toJson(user);
                                SharedPreference.storeCredentials(AddSelectClubActivity.this, userDataJson);

                                if (selectedClubModel.lat == 0) {
                                    SharedPreference.setBoolean(AddSelectClubActivity.this, SharedPreference.HAS_MAP, false);
                                } else {
                                    SharedPreference.setBoolean(AddSelectClubActivity.this, SharedPreference.HAS_MAP, true);
                                }

                                Intent mainScreenIntent = new Intent(AddSelectClubActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainScreenIntent);
                                finish();
                            }
                        }

                    } else {
                        String msg = jsonObject.getString("message");
                        toast(msg);
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

    private Integer getUserId() {
        String user = SharedPreference.getUserID(getApplicationContext());
        Gson gson = new Gson();
        User userData = gson.fromJson(user, User.class);
        if (userData == null) {
            return null;
        } else {
            return userData.getUserId();
        }
    }

    private void saveUserDetails(User user, Gson gson) {
        String userDataJson = gson.toJson(user);
        SharedPreference.setClubName(AddSelectClubActivity.this, selectedClubModel.getClubName());
        SharedPreference.storeCredentials(AddSelectClubActivity.this, userDataJson);
        if (selectedClubModel.lat == 0) {
            SharedPreference.setBoolean(AddSelectClubActivity.this, SharedPreference.HAS_MAP, false);
        } else {
            SharedPreference.setBoolean(AddSelectClubActivity.this, SharedPreference.HAS_MAP, true);
        }
        goHome();
    }

    private void goHome() {
        Intent mainScreenIntent = new Intent(AddSelectClubActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if(fromPushNotification){
            mainScreenIntent.putExtra("go_to_notification", true);
        }
        startActivity(mainScreenIntent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }

    @Override
    public void onClubClick(int position, ClubModel object, int id) {
        if (id == R.id.ivDelete) {
            selectedClubModel = object;
            new CustomAlertDialog(String.format(getString(R.string.alert_delete_club), object.getClubName()), this, 1, true).show(getSupportFragmentManager(), "");
        } else if (id == R.id.parent) {
            if (object.getClubApprove().equals("0")) {
                toast(getString(R.string.awaiting_club_approval));
                return;
            }
            if (SharedPreference.getClubName(this).equalsIgnoreCase(object.getClubName())) {
                currentClub();
                goHome();
            } else {
                selectedClubModel = object;
                new CustomAlertDialog(String.format(getString(R.string.alert_select_active_club), object.getClubName()), this, 2, true).show(getSupportFragmentManager(), "");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ADD_NEW_CLUB_CODE:
                    apiGetClubCall();
                    break;
            }
        }
    }

    @Override
    public void onDialogClick(boolean isOk, int tag, MyExtraData extraData) {
        if (tag == 1) {
            deleteClubCall(selectedClubModel.getClubCode());
        } else if (tag == 2) {
            validateDataAndHitApi();
        }
    }

    @Override
    public void onDialogCancelClick(boolean isOk, int tag, MyExtraData extraData) {

    }

    private void deleteClubCall(String clubCode) {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }

        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.deleteClubMember(member_id, clubCode, Utils.getNetworkAddress(AddSelectClubActivity.this), SharedPreference.getSelectedLanguage(this));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    int flag = jsonObject.getInt("flag");
                    if (flag == 1) {
                        String msg = jsonObject.getString("message");
                        toast(msg);
                        dialog.dismiss();
                        apiGetClubCall();
                    } else {
                        String msg = jsonObject.getString("message");
                        toast(msg);
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

    private void currentClub() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }

        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.currentClub(member_id, Utils.getNetworkAddress(AddSelectClubActivity.this), SharedPreference.getSelectedLanguage(this));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                /*try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    int flag = jsonObject.getInt("flag");
                    if (flag == 1) {

                    }

                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }*/
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
