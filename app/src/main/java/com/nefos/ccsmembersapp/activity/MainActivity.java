package com.nefos.ccsmembersapp.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.Utils;
import com.nefos.ccsmembersapp.adpater.UserAccountsAdapter;
import com.nefos.ccsmembersapp.databinding.ActivityMainBinding;
import com.nefos.ccsmembersapp.dialog.CustomAlertDialog;
import com.nefos.ccsmembersapp.fragment.AddSelectClubFragment;
import com.nefos.ccsmembersapp.fragment.CategoriesFragment;
import com.nefos.ccsmembersapp.fragment.ChangePasswordFragment;
import com.nefos.ccsmembersapp.fragment.Chat.Adapter.FriendsChatListModelData;
import com.nefos.ccsmembersapp.fragment.Chat.Adapter.model.ClubChatListModelData;
import com.nefos.ccsmembersapp.fragment.Chat.ChatFragment;
import com.nefos.ccsmembersapp.fragment.DashboardFragment;
import com.nefos.ccsmembersapp.fragment.DispenseHistoryFragment;
import com.nefos.ccsmembersapp.fragment.DonationFragment;
import com.nefos.ccsmembersapp.fragment.HomeFragment;
import com.nefos.ccsmembersapp.fragment.LanguageFragment;
import com.nefos.ccsmembersapp.fragment.MapFragment;
import com.nefos.ccsmembersapp.fragment.MyCartFragment;
import com.nefos.ccsmembersapp.fragment.MyProfileFragment;
import com.nefos.ccsmembersapp.fragment.NotificationFragment;
import com.nefos.ccsmembersapp.fragment.PreRegisterCodeFragment;
import com.nefos.ccsmembersapp.fragment.SettingsFragment;
import com.nefos.ccsmembersapp.fragment.ccsmembermap.CCSMemberFragment;
import com.nefos.ccsmembersapp.interfaces.IDialog;
import com.nefos.ccsmembersapp.model.MyExtraData;
import com.nefos.ccsmembersapp.model.User;
import com.nefos.ccsmembersapp.server.ApiClient;
import com.nefos.ccsmembersapp.server.ApiInterface;
import com.nefos.ccsmembersapp.service.KillService;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements View.OnClickListener, DashboardFragment.DashboardBackPressListener, IDialog {
    private final String TAG = MainActivity.class.getSimpleName();
    public static TextView tvSave, tvUserName, tvUserEmail, tvName, tvEmail;
    public static TextView cartCounter, toolbarTitle, notification_count;
    public static RelativeLayout cartCountLayout, cartLayout, notification_rl_main, notification_count_rl;
    public static ImageView cart, cart_bg, notification_bg, ivQrCode, ivChatToolbar;
    public static RelativeLayout rlChatCount, rlShowCount;
    private String selectedLanguage = "";

    public static ImageView notification, switchAccount;
    private boolean switchAccountOpenOrClose = false;
    private CircleImageView circularImageViewMain, circularImageViewAccFirst, circularImageViewAccSecond, ivProfile;
    private LinearLayout ll_switch_account;
    ArrayList<User> userList;
    Toolbar toolbar;
    private Context mContext;
    private DrawerLayout drawer;
    public static LinearLayout ll_drawer, llLanguages, llSettings, llPreRegister,
            llMap, llMenu, llMenuSide, llHome, llCategory, llDonation,
            llChat, llDispenseHistory,
            llMyProfile, llNotification, llLogout, llChangePassword , llCCSMember;
    private RelativeLayout rl_add_account;
    private ImageView imgDrawer;
    private RecyclerView recyclerViewAccountList;
    private UserAccountsAdapter userAccountsAdapter;
    private Fragment fragment;
    User userData;
    public static int count = 0;
    boolean exit = false;
    public static View appBar;
    private ApiClient apiClient;
    public static TextView tvChatCount;
    public static Context context;
    BroadcastReceiver updateUIReciver;
    ActivityMainBinding binding;
    DatabaseReference databaseReference;
    DatabaseReference clubDatabaseReference;
    public static int totalChatCount = 0;
    public static int totalClubChatCount = 0;
    private FragmentRefreshListener fragmentRefreshListener;

    ValueEventListener val = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            try {
                totalChatCount = 0;
                for (DataSnapshot dataSnapshotRead : dataSnapshot.getChildren()) {
                    FriendsChatListModelData friendsChatListModelData;
                    friendsChatListModelData = dataSnapshotRead.getValue(FriendsChatListModelData.class);
                    if(friendsChatListModelData != null && friendsChatListModelData.fromId != null) {
                        if(friendsChatListModelData.count > 0) {
                            totalChatCount = totalChatCount + 1;
                            if(totalChatCount + totalClubChatCount == 0) {
                                rlShowCount.setVisibility(View.GONE);
                            } else {
                                rlShowCount.setVisibility(View.VISIBLE);
                                tvChatCount.setText(String.valueOf(totalChatCount + totalClubChatCount));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Exception", e.toString());
                e.printStackTrace();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ValueEventListener valClub = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            try {
                totalClubChatCount = 0;
                for (DataSnapshot dataSnapshotRead : dataSnapshot.getChildren()) {
                    ClubChatListModelData clubChatListModelData;
                    clubChatListModelData = dataSnapshotRead.getValue(ClubChatListModelData.class);
                    if(clubChatListModelData != null && clubChatListModelData.fromId != null) {
                        if(clubChatListModelData.count > 0) {
                            totalClubChatCount = totalClubChatCount + 1;
                            if(totalChatCount + totalClubChatCount == 0) {
                                rlShowCount.setVisibility(View.GONE);
                            } else {
                                rlShowCount.setVisibility(View.VISIBLE);
                                tvChatCount.setText(String.valueOf(totalChatCount + totalClubChatCount));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("EXCEPTION", e.toString());
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mContext = this;
        context = this;

        Intent serviceIntent = new Intent(this, KillService.class);
        startService(serviceIntent);

        selectedLanguage = SharedPreference.getSelectedLanguage(mContext);

        databaseReference = FirebaseDatabase.getInstance().getReference(ApiClient.usersChat)
                .child(SharedPreference.getMemberId(MainActivity.this)).child("conversations");
        clubDatabaseReference = FirebaseDatabase.getInstance().getReference(ApiClient.clubsChat)
                .child(SharedPreference.getMemberId(MainActivity.this)).child("conversations");
        databaseReference.addValueEventListener(val);
        clubDatabaseReference.addValueEventListener(valClub);

        Utils.updateResources(mContext, selectedLanguage);
        setComponents();
        registerListener();
        notificationShowOrHide(true);
        scanQrCodeShowOrHide(false);
        if (!Utils.isNoPayment()) {
            cartShowOrHide(true);
        }


        if(getIntent() != null) {
            Intent intent = getIntent();
            boolean isFromNotification = intent.getBooleanExtra("isFromNotification", false);
            String type = intent.getStringExtra("type");
            String userId = intent.getStringExtra("userId");
            String clubName = intent.getStringExtra("clubName");
            String conversationId = intent.getStringExtra("conversationId");
            String receiverId = intent.getStringExtra("receiverId");
            if(isFromNotification) {
                if(type.equals("1") || type.equals("2") || type.equals("3") || type.equals("4")) {
                    init(false, "", "", type, userId, clubName, conversationId, true, receiverId);
                } else {
                    init(false, "", "", "", "", "", "",
                            false, "");
                }
            } else {
                init(false, "", "", "", "", "", "",
                        false, "");
            }
        } else {
            init(false, "", "", "", "", "", "",
                    false, "");
        }

        checkBannedclub(false);
    }

    public static void updateCount() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(ApiClient.usersChat)
                .child(SharedPreference.getMemberId(context)).child("conversations");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    totalChatCount = 0;
                    for (DataSnapshot dataSnapshotRead : snapshot.getChildren()) {
                        FriendsChatListModelData friendsChatListModelData;
                        friendsChatListModelData = dataSnapshotRead.getValue(FriendsChatListModelData.class);
                        if(friendsChatListModelData != null && friendsChatListModelData.fromId != null) {
                            if(friendsChatListModelData.count > 0) {
                                totalChatCount = totalChatCount + 1;
                                if(totalChatCount + totalClubChatCount == 0) {
                                    rlShowCount.setVisibility(View.GONE);
                                } else {
                                    rlShowCount.setVisibility(View.VISIBLE);
                                    tvChatCount.setText(String.valueOf(totalChatCount + totalClubChatCount));
                                }
                            } else {
                                if(totalChatCount + totalClubChatCount == 0) {
                                    rlShowCount.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference clubDatabaseReference = FirebaseDatabase.getInstance().getReference(ApiClient.clubsChat)
                .child(SharedPreference.getMemberId(context)).child("conversations");
        clubDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    totalClubChatCount = 0;
                    for (DataSnapshot dataSnapshotRead : snapshot.getChildren()) {
                        ClubChatListModelData clubChatListModelData;
                        clubChatListModelData = dataSnapshotRead.getValue(ClubChatListModelData.class);
                        if(clubChatListModelData != null && clubChatListModelData.fromId != null) {
                            if(clubChatListModelData.count > 0) {
                                totalClubChatCount = totalClubChatCount + 1;
                                if(totalChatCount + totalClubChatCount == 0) {
                                    rlShowCount.setVisibility(View.GONE);
                                } else {
                                    rlShowCount.setVisibility(View.VISIBLE);
                                    tvChatCount.setText(String.valueOf(totalChatCount + totalClubChatCount));
                                }
                            } else {
                                if(totalChatCount + totalClubChatCount == 0) {
                                    rlShowCount.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("EXCEPTION", e.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void init(boolean isFromNotification, String clubName, String clubUserId,
                     String notificationType, String notificationUserId, String notificationClubName, String notificationConversationId,
                     boolean isFromChatNotification, String receiverId) {
        if (SharedPreference.getClubName(this).isEmpty() && !isFromChatNotification) {
            tvName.setText(SharedPreference.getString(this, SharedPreference.PREF_KEY_USER_NAME));
            tvEmail.setText(SharedPreference.getString(this, SharedPreference.PREF_KEY_USER_EMAIL));
            fragment = new AddSelectClubFragment();
            openFragment();
        } else if(isFromChatNotification) {
            tvName.setText(SharedPreference.getString(this, SharedPreference.PREF_KEY_USER_NAME));
            tvEmail.setText(SharedPreference.getString(this, SharedPreference.PREF_KEY_USER_EMAIL));
            Bundle args = new Bundle();
            fragment = ChatFragment.newInstance();
            args.putString("type", notificationType);
            args.putString("userId", notificationUserId);
            args.putString("clubName", notificationClubName);
            args.putString("conversationId", notificationConversationId);
            args.putString("receiverId", receiverId);
            fragment.setArguments(args);
            notificationShowOrHide(!SharedPreference.getClubName(this).isEmpty());
            scanQrCodeShowOrHide(true);
            openFragment();
        }
        else {
            if (getIntent().hasExtra("go_to_notification")) {
                boolean goToNotification = getIntent().getBooleanExtra("go_to_notification", false);
                if (goToNotification) {
                    fragment = new NotificationFragment();
                    Bundle args = new Bundle();
                    notificationShowOrHide(false);
                    scanQrCodeShowOrHide(false);
                    args.putString("notificationData", "0");
                    fragment.setArguments(args);
                }
            } else {
                if(isFromNotification) {
                    SharedPreference.setString(this, SharedPreference.PREF_KEY_USER_ID, clubUserId);
                    Bundle args = new Bundle();
                    fragment = NotificationFragment.newInstance();
                    args.putString("notificationData", "1");
                    args.putString("clubName", clubName);
                    fragment.setArguments(args);
                    notificationShowOrHide(false);
                    scanQrCodeShowOrHide(false);
                } else {
                    fragment = DashboardFragment.newInstance();
                }
            }

            openFragment();

            setActionBarTitle(getResources().getString(R.string.home));
            userList = SharedPreference.getUserListPref(MainActivity.this, "userList");
            userAccountsAdapter = new UserAccountsAdapter(this, userList);
            recyclerViewAccountList.setAdapter(userAccountsAdapter);
            String user = SharedPreference.getUserID(MainActivity.this);
            Gson gson = new Gson();
            userData = gson.fromJson(user, User.class);
            tvUserName.setText(userData.getName() + " | " + getResources().getString(R.string.demo));
            tvUserEmail.setText(userData.getEmail());
            tvName.setText(userData.getMemberNickname());
            tvEmail.setText(userData.getEmail());

            if (!Utils.isNoPayment()) {
                Utils.cartCount = Integer.parseInt(userData.getCartCount());
            }

            Log.e("userId>>>", "" + userData.getImage());
            Picasso.get().load(userData.getImage()).placeholder(R.drawable.ic_profile_grey).into(circularImageViewMain);
            Glide.with(this).load(userData.getImage()).placeholder(R.drawable.ic_profile_grey).into(ivProfile);
            if (userList.size() > 1) {
                if (userList.size() >= 3) {

                    circularImageViewAccFirst.setVisibility(View.VISIBLE);
                    circularImageViewAccSecond.setVisibility(View.VISIBLE);
                    Picasso.get()
                            .load(userList.get(1).getImage())
                            .placeholder(R.drawable.ic_profile_grey)
                            .into(circularImageViewAccFirst);

                    Picasso.get()
                            .load(userList.get(2).getImage())
                            .placeholder(R.drawable.ic_profile_grey)
                            .into(circularImageViewAccSecond);

                } else {
                    circularImageViewAccFirst.setVisibility(View.VISIBLE);
                    circularImageViewAccSecond.setVisibility(View.INVISIBLE);
                    Picasso.get()
                            .load(userList.get(1).getImage())
                            .placeholder(R.drawable.ic_profile_grey)
                            .into(circularImageViewAccFirst);

                }
            }
            if (userData.getUserType().equals("Admin")) {
                llSettings.setVisibility(View.VISIBLE);
            } else {
                llSettings.setVisibility(View.GONE);
            }
            IntentFilter filter = new IntentFilter();

            filter.addAction("count");

            updateUIReciver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {

                }
            };
            registerReceiver(updateUIReciver, filter);
        }
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {

            }

            @Override
            public void onDrawerOpened(@NonNull View view) {

            }

            @Override
            public void onDrawerClosed(@NonNull View view) {

            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });
    }

    private void openFragment() {
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }

    public void setComponents() {
        apiClient = new ApiClient();
        toolbar = findViewById(R.id.toolbar);
        appBar = findViewById(R.id.appBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        cart = toolbar.findViewById(R.id.image_cart);
        cartCountLayout = toolbar.findViewById(R.id.cart_count_rl);
        cartLayout = toolbar.findViewById(R.id.cart_rl);
        cart_bg = toolbar.findViewById(R.id.cart_bg);
        cartCounter = toolbar.findViewById(R.id.cart_count);
        cart_bg.setBackground(drawCircle());

        notification_bg = toolbar.findViewById(R.id.notification_bg);
        notification_count_rl = toolbar.findViewById(R.id.notification_count_rl);
        notification_count = toolbar.findViewById(R.id.notification_count);
        notification_rl_main = toolbar.findViewById(R.id.notification_rl_main);
        ivQrCode = toolbar.findViewById(R.id.ivQrCode);
        ivChatToolbar = toolbar.findViewById(R.id.ivChatToolbar);
        rlChatCount = toolbar.findViewById(R.id.rlChatCount);
        tvChatCount = toolbar.findViewById(R.id.tvChatCount);
        rlShowCount = toolbar.findViewById(R.id.rlShowCount);

        ivQrCode.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DisplayQRCodeActivity.class);
            intent.putExtra("qrCode", SharedPreference.getString(MainActivity.this, SharedPreference.CHAT_QR_CODE));
            intent.putExtra("fromChat", true);
            startActivity(intent);
        });

        ivChatToolbar.setOnClickListener(v -> {
            if(getFragmentRefreshListener() != null){
                getFragmentRefreshListener().onRefresh();
            }
        });

        tvSave = toolbar.findViewById(R.id.tvSave);
        tvSave.setVisibility(View.GONE);
        imgDrawer = toolbar.findViewById(R.id.imgDrawer);
        notification = toolbar.findViewById(R.id.image_notification);
        switchAccountOpenOrClose = false;
        drawer = findViewById(R.id.drawer_layout);
        ll_drawer = findViewById(R.id.ll_drawer);
        //viewChat = findViewById(R.id.viewChat);
        switchAccount = findViewById(R.id.switch_account);
        ll_switch_account = findViewById(R.id.ll_switch_account);
        tvUserName = findViewById(R.id.user_name);
        tvUserEmail = findViewById(R.id.user_email);
        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvEmail);
        circularImageViewAccFirst = findViewById(R.id.account_first);
        circularImageViewMain = findViewById(R.id.main_account);
        ivProfile = findViewById(R.id.ivProfile);
        circularImageViewAccSecond = findViewById(R.id.account_second);
        circularImageViewAccFirst.setVisibility(View.GONE);
        circularImageViewAccSecond.setVisibility(View.GONE);
        llMenu = findViewById(R.id.llMenu);
        llMap = findViewById(R.id.llMap);
        rl_add_account = findViewById(R.id.rl_add_account);
        llHome = findViewById(R.id.llHome);
        llMenuSide = findViewById(R.id.llMenuSide);
        llCategory = findViewById(R.id.llCategory);
        llDonation = findViewById(R.id.llDonation);
        llChat = findViewById(R.id.llChat);
        llCCSMember = findViewById(R.id.llCCSMember);
        llDispenseHistory = findViewById(R.id.llDispenseHistory);
        llMyProfile = findViewById(R.id.llMyProfile);
        llNotification = findViewById(R.id.llNotification);
        llChangePassword = findViewById(R.id.llChangePassword);
        llSettings = findViewById(R.id.llSettings);
        llPreRegister = findViewById(R.id.llPreRegister);
        llLanguages = findViewById(R.id.llLanguages);
        llLogout = findViewById(R.id.llLogout);
        recyclerViewAccountList = findViewById(R.id.recyclerViewAccountList);
        recyclerViewAccountList.setLayoutManager(new LinearLayoutManager(this));

        manageSideMenuVisibility();
    }

    private void manageSideMenuVisibility() {
        int visibility = View.VISIBLE;

        if (SharedPreference.getClubName(this).isEmpty()) {
            visibility = View.GONE;

            llMap.setVisibility(visibility);
        } else {
            if (SharedPreference.getBoolean(this, SharedPreference.HAS_MAP)) {
                llMap.setVisibility(View.VISIBLE);
            } else {
                llMap.setVisibility(View.GONE);
            }
        }

        llHome.setVisibility(visibility);
        llMyProfile.setVisibility(visibility);
        llDispenseHistory.setVisibility(visibility);
        llSettings.setVisibility(visibility);
    }

    public static void manageSideMenuVisibilityFromDashBoard(String clubName, boolean hasMap, String showMenu, String showDispense) {
        int visibility = View.VISIBLE;

        if (clubName.isEmpty()) {
            visibility = View.GONE;

            llMap.setVisibility(visibility);
        } else {
            if (hasMap) {
                llMap.setVisibility(View.VISIBLE);
            } else {
                llMap.setVisibility(View.GONE);
            }
        }

        llHome.setVisibility(visibility);
        llMyProfile.setVisibility(visibility);
        llDispenseHistory.setVisibility(visibility);

        if (showMenu.equals("1")){
            llMenuSide.setVisibility(View.VISIBLE);
        } else {
            llMenuSide.setVisibility(View.GONE);
        }
        if(showDispense.equals("1")) {
            llDispenseHistory.setVisibility(View.VISIBLE);
        } else {
            llDispenseHistory.setVisibility(View.GONE);
        }
    }

    private void registerListener() {
        cartLayout.setOnClickListener(this);
        imgDrawer.setOnClickListener(this);
        notification.setOnClickListener(this);
        ll_switch_account.setOnClickListener(this);
        circularImageViewAccFirst.setOnClickListener(this);
        circularImageViewAccSecond.setOnClickListener(this);
        llHome.setOnClickListener(this);
        llMenuSide.setOnClickListener(this);
        llCategory.setOnClickListener(this);
        llDonation.setOnClickListener(this);
        llChat.setOnClickListener(this);
        llCCSMember.setOnClickListener(this);
        llDispenseHistory.setOnClickListener(this);
        llMyProfile.setOnClickListener(this);
        llNotification.setOnClickListener(this);
        llChangePassword.setOnClickListener(this);
        llSettings.setOnClickListener(this);
        llPreRegister.setOnClickListener(this);
        llLanguages.setOnClickListener(this);
        llLogout.setOnClickListener(this);
        llMap.setOnClickListener(this);
        rl_add_account.setOnClickListener(this);

    }

    private Drawable drawCircle() {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
        shape.setColor(Color.RED);
        shape.setStroke(2, Color.WHITE);
        return shape;
    }

    private void displaySelectedScreen(int itemId) {
        try {
            drawer.closeDrawer(ll_drawer);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        switch (itemId) {
            case R.id.cart_rl:
                fragment = MyCartFragment.newInstance();
                break;
            case R.id.llHome:

//                if (!(fragment instanceof DashboardFragment)) {
                    fragment = DashboardFragment.newInstance();
                    setActionBarTitle(getResources().getString(R.string.home));
                    cart.setVisibility(View.VISIBLE);
                    notificationShowOrHide(true);
                    scanQrCodeShowOrHide(false);
                    tvSave.setVisibility(View.GONE);
                    highlightSelectedView(itemId, false);
//                }
                break;
            case R.id.llMenuSide:

//                if (!(fragment instanceof HomeFragment)) {

                    fragment = HomeFragment.newInstance();
                    setActionBarTitle(getResources().getString(R.string.menu));

                    if (!Utils.isNoPayment()) {
                        if (Utils.cartCount > 0) {
                            cartCountLayout.setVisibility(View.VISIBLE);
                        } else {
                            cartCountLayout.setVisibility(View.GONE);

                        }
                        cartLayout.setVisibility(View.VISIBLE);
                        cart.setVisibility(View.VISIBLE);
                    }
                    notificationShowOrHide(true);
                    scanQrCodeShowOrHide(false);
                    tvSave.setVisibility(View.GONE);
                    highlightSelectedView(itemId, false);
//                }

                break;
            case R.id.llCategory:

//                if (!(fragment instanceof CategoriesFragment)) {
                    HomeFragment.categoryId = 0;
                    fragment = CategoriesFragment.newInstance();
                    setActionBarTitle(getResources().getString(R.string.categories));
                    if (!Utils.isNoPayment()) {
                        if (Utils.cartCount > 0) {
                            cartCountLayout.setVisibility(View.VISIBLE);
                        } else {
                            cartCountLayout.setVisibility(View.GONE);

                        }
                        cartLayout.setVisibility(View.VISIBLE);
                        cart.setVisibility(View.VISIBLE);
                    }
                    notificationShowOrHide(true);
                    scanQrCodeShowOrHide(false);
                    tvSave.setVisibility(View.GONE);
                    highlightSelectedView(itemId, false);
//                }

                break;
            case R.id.llDonation:
//                if (!(fragment instanceof DonationFragment)) {

                    fragment = DonationFragment.newInstance();
                    setActionBarTitle(getResources().getString(R.string.wallet));
                    if (Utils.cartCount > 0) {
                        cartCountLayout.setVisibility(View.VISIBLE);
                    } else {
                        cartCountLayout.setVisibility(View.GONE);

                    }
                    cartLayout.setVisibility(View.VISIBLE);

                    cart.setVisibility(View.VISIBLE);
                    notificationShowOrHide(true);
                    scanQrCodeShowOrHide(false);
                    tvSave.setVisibility(View.GONE);
                    highlightSelectedView(itemId, false);
//                }
                break;
            case R.id.llDispenseHistory:
//                if (!(fragment instanceof DispenseHistoryFragment)) {

                    fragment = DispenseHistoryFragment.newInstance();
                    setActionBarTitle(getResources().getString(R.string.dispense_history));
                    if (!Utils.isNoPayment()) {
                        if (Utils.cartCount > 0) {
                            cartCountLayout.setVisibility(View.VISIBLE);
                        } else {
                            cartCountLayout.setVisibility(View.GONE);

                        }
                        cartLayout.setVisibility(View.VISIBLE);
                        cart.setVisibility(View.VISIBLE);
                    }
                    notificationShowOrHide(true);
                    scanQrCodeShowOrHide(false);
                    tvSave.setVisibility(View.GONE);
                    highlightSelectedView(itemId, false);
//                }

                break;
            case R.id.llChangePassword:

//                if (!(fragment instanceof ChangePasswordFragment)) {

                    fragment = ChangePasswordFragment.newInstance();
                    setActionBarTitle(getResources().getString(R.string.change_password));
                    if (!Utils.isNoPayment()) {
                        if (Utils.cartCount > 0) {
                            cartCountLayout.setVisibility(View.VISIBLE);
                        } else {
                            cartCountLayout.setVisibility(View.GONE);

                        }
                        cartLayout.setVisibility(View.VISIBLE);
                        cart.setVisibility(View.VISIBLE);
                    }
                    notificationShowOrHide(!SharedPreference.getClubName(this).isEmpty());
                    scanQrCodeShowOrHide(false);
                    tvSave.setVisibility(View.GONE);
                    highlightSelectedView(itemId, false);
//                }
                break;
            case R.id.llMyProfile:
//                if (!(fragment instanceof MyProfileFragment)) {
                    fragment = MyProfileFragment.newInstance();
                    setActionBarTitle(getResources().getString(R.string.my_profile));
                    if (!Utils.isNoPayment()) {
                        if (Utils.cartCount > 0) {
                            cartCountLayout.setVisibility(View.VISIBLE);
                        } else {
                            cartCountLayout.setVisibility(View.GONE);

                        }
                        cartLayout.setVisibility(View.VISIBLE);
                        cart.setVisibility(View.VISIBLE);
                    }
                    notificationShowOrHide(true);
                    scanQrCodeShowOrHide(false);
                    tvSave.setVisibility(View.GONE);
                    highlightSelectedView(itemId, false);
//                }
                break;
            case R.id.llNotification:
            case R.id.menu_notification:
                fragment = NotificationFragment.newInstance();
                Bundle args = new Bundle();
                args.putString("notificationData", "0");
                fragment.setArguments(args);
                setActionBarTitle(getResources().getString(R.string.notification));
                if (!Utils.isNoPayment()) {
                    if (Utils.cartCount > 0) {
                        cartCountLayout.setVisibility(View.VISIBLE);
                    } else {
                        cartCountLayout.setVisibility(View.GONE);

                    }
                    cartLayout.setVisibility(View.VISIBLE);
                    cart.setVisibility(View.VISIBLE);
                }
                notificationShowOrHide(false);
                scanQrCodeShowOrHide(false);
                tvSave.setVisibility(View.GONE);
                highlightSelectedView(itemId, false);

                break;
            case R.id.llSettings:
//                if (!(fragment instanceof SettingsFragment)) {
                    fragment = SettingsFragment.newInstance();
                    setActionBarTitle(getResources().getString(R.string.settings));
                    tvSave.setVisibility(View.VISIBLE);
                    tvSave.setText(mContext.getResources().getString(R.string.save));
                    if (!Utils.isNoPayment()) {
                        cartLayout.setVisibility(View.GONE);
                        cartCountLayout.setVisibility(View.GONE);
                        cart.setVisibility(View.GONE);
                    }
                    notificationShowOrHide(true);
                    scanQrCodeShowOrHide(false);
                    tvSave.setVisibility(View.GONE);
                    highlightSelectedView(itemId, false);
//                }
                break;
            case R.id.llCCSMember:
                fragment = CCSMemberFragment.newInstance();
                setActionBarTitle(getResources().getString(R.string.ccs_member));
                if (!Utils.isNoPayment()) {
                    cartLayout.setVisibility(View.GONE);
                    cartCountLayout.setVisibility(View.GONE);
                    cart.setVisibility(View.GONE);
                }
                notificationShowOrHide(!SharedPreference.getClubName(this).isEmpty());
                scanQrCodeShowOrHide(false);
                tvSave.setVisibility(View.GONE);
                highlightSelectedView(itemId, false);
                break;

            case R.id.llPreRegister:
                fragment = PreRegisterCodeFragment.newInstance();
                setActionBarTitle(getResources().getString(R.string.pre_register));
                if (!Utils.isNoPayment()) {
                    cartLayout.setVisibility(View.GONE);
                    cartCountLayout.setVisibility(View.GONE);
                    cart.setVisibility(View.GONE);
                }
                notificationShowOrHide(!SharedPreference.getClubName(this).isEmpty());
                scanQrCodeShowOrHide(false);
                tvSave.setVisibility(View.GONE);
                highlightSelectedView(itemId, false);
                break;
            case R.id.llChat:
//                if (!(fragment instanceof ChatFragment)) {
                    fragment = ChatFragment.newInstance();
                    setActionBarTitle(getResources().getString(R.string.chat));
                    if (!Utils.isNoPayment()) {
                        cartLayout.setVisibility(View.GONE);
                        cartCountLayout.setVisibility(View.GONE);
                        cart.setVisibility(View.GONE);
                    }
                    notificationShowOrHide(!SharedPreference.getClubName(this).isEmpty());
                    scanQrCodeShowOrHide(true);
                    tvSave.setVisibility(View.GONE);
                    highlightSelectedView(itemId, false);
//                }
                break;
            case R.id.llLanguages:
//                if (!(fragment instanceof LanguageFragment)) {
                    fragment = LanguageFragment.newInstance();
                    setActionBarTitle(getResources().getString(R.string.language));
                    if (!Utils.isNoPayment()) {
                        if (Utils.cartCount > 0) {
                            cartCountLayout.setVisibility(View.VISIBLE);
                        } else {
                            cartCountLayout.setVisibility(View.GONE);

                        }
                        cartLayout.setVisibility(View.VISIBLE);
                        cart.setVisibility(View.VISIBLE);
                    }
                    notificationShowOrHide(!SharedPreference.getClubName(this).isEmpty());
                    scanQrCodeShowOrHide(false);
                    tvSave.setVisibility(View.GONE);
                    highlightSelectedView(itemId, false);
                    break;
            case R.id.llLogout:
                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(mContext.getResources().getString(R.string.logout_dialog));
                builder.setPositiveButton(mContext.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseDatabase.getInstance().getReference(ApiClient.users).child(SharedPreference.getMemberId(MainActivity.this)).child("isOnline").setValue(false);
                        if (userList != null && userList.size() == 1) {
                            SharedPreference.setUserListPref(MainActivity.this, "userList", new ArrayList<User>());
                        }
                        SharedPreference.clearCredentails(MainActivity.this);
                        SharedPreference.clearClubNameCredentails(MainActivity.this);
                        SharedPreference.setBoolean(MainActivity.this, SharedPreference.IS_LOGIN, false);
                        Intent loginScreenIntent = new Intent(MainActivity.this, LoginAcvitiy.class);
                        loginScreenIntent.putExtra("isLogin", true);
                        loginScreenIntent.putExtra("isFromLogin", false);
                        loginScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        loginScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        SharedPreferences senderIdPref = mContext.getSharedPreferences("senderId", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = senderIdPref.edit();
                        editor.putString("id", "");
                        editor.apply();
                        startActivity(loginScreenIntent);
                    }

                });
                builder.setNegativeButton(mContext.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();

                break;
            case R.id.rl_add_account:

//                if (!(fragment instanceof AddSelectClubFragment)) {
                    fragment = AddSelectClubFragment.newInstance();
                    setActionBarTitle(getResources().getString(R.string.add_your_first_club));
                    if (!Utils.isNoPayment()) {
                        if (Utils.cartCount > 0) {
                            cartCountLayout.setVisibility(View.VISIBLE);
                        } else {
                            cartCountLayout.setVisibility(View.GONE);

                        }
                        cartLayout.setVisibility(View.VISIBLE);
                        cart.setVisibility(View.VISIBLE);
                    }
                    notificationShowOrHide(false);
                    scanQrCodeShowOrHide(false);
                    tvSave.setVisibility(View.GONE);
                    highlightSelectedView(itemId, false);
//                }
                break;
            case R.id.llMap:
                checkLocation(itemId);
                break;
        }
        if ((fragment instanceof NotificationFragment)) {
            Utils.changeFragmentForNotification(this, fragment, null);
        } else {
            Utils.changeFragment(this, fragment, null);
        }
    }

    private void checkBannedclub(boolean showProgress) {
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(MainActivity.this, R.style.ProgressDialogStyle);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        if(showProgress) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.show();
            }
        }
        Log.e("PROGRESS", "CALL MAIN ACTIVITY");
        try {
            ArrayList<User> userList = SharedPreference.getUserListPref(this, "userList");
            String memberId = SharedPreference.getMemberId(this);
            String clubname = SharedPreference.getClubName(this);
            String  language = SharedPreference.getSelectedLanguage(this);

            if (memberId != null && !clubname.isEmpty()) {
                ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
                Call<ResponseBody> call = apiService.bannedClub(memberId, language,clubname);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());

                            int flag = jsonObject.getInt("flag");
                            String message = jsonObject.getString("message");

                            if (flag == 0) {
                                if (userList != null && userList.size() == 1) {
                                    SharedPreference.setUserListPref(MainActivity.this, "userList", new ArrayList<User>());
                                }
                                SharedPreference.clearCredentails(MainActivity.this);
                                SharedPreference.clearClubNameCredentails(MainActivity.this);
                                new CustomAlertDialog(String.format(message,
                                        ""), MainActivity.this, 2, false).show(getSupportFragmentManager(), "");

                            } else {
                                String msg = jsonObject.getString("message");
                                JSONObject data = jsonObject.getJSONObject("data");
                                String showMenu = data.getString("showmenu_option");
                                SharedPreference.setString(MainActivity.this, SharedPreference.SHOW_MENU, showMenu);
                                if (showMenu.equals("1")){
                                    llMenuSide.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    llMenuSide.setVisibility(View.GONE);
                                }
                                String showDispense = data.getString("dispensmenu_option");
                                if(showDispense.equals("1")) {
                                    llDispenseHistory.setVisibility(View.VISIBLE);
                                } else {
                                    llDispenseHistory.setVisibility(View.GONE);
                                }
                                Log.d(TAG, "onResponse: " + showMenu);
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
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        hideLoading();
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                });
            } else {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

        } catch (Exception e) {
            Log.d(TAG, "checkBannedclub: " + e);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }


    }

    @Override
    public void onDialogClick(boolean isOk, int tag, MyExtraData extraData) {
        Intent loginScreenIntent = new Intent(MainActivity.this, LoginAcvitiy.class);
        loginScreenIntent.putExtra("isLogin", true);
        loginScreenIntent.putExtra("isFromLogin", false);
        loginScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        loginScreenIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        SharedPreferences senderIdPref = mContext.getSharedPreferences("senderId", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = senderIdPref.edit();
        editor.putString("id", "");
        editor.apply();
        startActivity(loginScreenIntent);
    }

    void highlightSelectedView(int itemId, Boolean isClickOnDrwerIcon) {

        int delay = 0;
        if (!isClickOnDrwerIcon) {
            delay = 2000;
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                binding.tvHome.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvHome.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvMenuSide.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvMenuSide.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvMyProfile.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvMyProfile.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvCategory.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvCategory.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvDonation.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvDonation.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvDispenseHistory.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvDispenseHistory.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvMap.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvMap.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvLanguage.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvLanguage.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvChangePassword.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvChangePassword.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvNotification.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvNotification.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvChat.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvChat.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvCCSMember.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvCCSMember.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvPreRegister.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvPreRegister.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvSettings.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvSettings.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                binding.tvMyClubs.setTextColor(getResources().getColor(R.color._EBEDEF));
                binding.tvMyClubs.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_text_bg));

                Utils.tint(binding.ivHome, R.color._EBEDEF, MainActivity.this);
                binding.flHome.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_icon_bg));

                Utils.tint(binding.ivMenuSide, R.color._EBEDEF, MainActivity.this);
                binding.flMenu.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_icon_bg));

                Utils.tint(binding.ivMyProfile, R.color._EBEDEF, MainActivity.this);
                binding.flProfile.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_icon_bg));

                Utils.tint(binding.ivCategory, R.color._EBEDEF, MainActivity.this);
                binding.ivCategory.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_icon_bg));

                Utils.tint(binding.ivDonation, R.color._EBEDEF, MainActivity.this);

                Utils.tint(binding.ivDispenseHistory, R.color._EBEDEF, MainActivity.this);
                binding.flHistory.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_icon_bg));

                Utils.tint(binding.ivMap, R.color._EBEDEF, MainActivity.this);
                binding.flMap.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_icon_bg));

                Utils.tint(binding.ivLanguage, R.color._EBEDEF, MainActivity.this);
                binding.flLanguage.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_icon_bg));

                Utils.tint(binding.ivChangePassword, R.color._EBEDEF, MainActivity.this);
                binding.flChangePassword.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_icon_bg));

                Utils.tint(binding.ivNotification, R.color._EBEDEF, MainActivity.this);

                Utils.tint(binding.ivChat, R.color._EBEDEF, MainActivity.this);
                binding.flChat.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_icon_bg));

                Utils.tint(binding.ivCCSMember, R.color._EBEDEF, MainActivity.this);
                binding.flCCSMember.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_icon_bg));

                Utils.tint(binding.ivPreRegister, R.color._EBEDEF, MainActivity.this);
                binding.flPreRegister.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_icon_bg));

                Utils.tint(binding.ivSettings, R.color._EBEDEF, MainActivity.this);
                binding.flSetting.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_icon_bg));

                Utils.tint(binding.ivMyClubs, R.color._EBEDEF, MainActivity.this);
                binding.flMyClubs.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.nv_unselected_icon_bg));

                Utils.tint(binding.ivLogout, R.color._EBEDEF, MainActivity.this);

                Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (f instanceof DashboardFragment) {
                    log("R.id.llHome");
                    enableSelection(binding.tvHome, binding.ivHome, binding.llHome, binding.flHome);
                }
                if (f instanceof HomeFragment) {
                    log("R.id.llMenu");
                    enableSelection(binding.tvMenuSide, binding.ivMenuSide, binding.llMenuSide, binding.flMenu);
                }
                if (f instanceof MyProfileFragment) {
                    log("R.id.llMyProfile");
                    enableSelection(binding.tvMyProfile, binding.ivMyProfile, binding.llMyProfile, binding.flProfile);
                }
                if (f instanceof DispenseHistoryFragment) {
                    log("R.id.llDispenseHistory");
                    enableSelection(binding.tvDispenseHistory, binding.ivDispenseHistory, binding.llDispenseHistory, binding.flHistory);
                }
                if (f instanceof MapFragment) {
                    log("R.id.llMap");
                    enableSelection(binding.tvMap, binding.ivMap, binding.llMap, binding.flMap);
                }
                if (f instanceof LanguageFragment) {
                    log("R.id.llLanguages");
                    enableSelection(binding.tvLanguage, binding.ivLanguage, binding.llLanguages, binding.flLanguage);
                }
                if (f instanceof ChatFragment) {
                    log("R.id.ChatFragment");
                    enableSelection(binding.tvChat, binding.ivChat, binding.llChat, binding.flChat);
                }
                if (f instanceof CCSMemberFragment) {
                    log("R.id.CCSMemberFragment");
                    enableSelection(binding.tvCCSMember, binding.ivCCSMember, binding.llCCSMember, binding.flCCSMember);
                }
                if (f instanceof PreRegisterCodeFragment) {
                    log("R.id.PreRegisterCodeFragment");
                    enableSelection(binding.tvPreRegister, binding.ivPreRegister, binding.llPreRegister, binding.flPreRegister);
                }
                if (f instanceof ChangePasswordFragment) {
                    log("R.id.llChangePassword");
                    enableSelection(binding.tvChangePassword, binding.ivChangePassword, binding.llChangePassword, binding.flChangePassword);
                }
                if (f instanceof AddSelectClubFragment) {
                    log("R.id.llChangePassword");
                    enableSelection(binding.tvMyClubs, binding.ivMyClubs, binding.llMyClubs, binding.flMyClubs);
                }
                if (f instanceof SettingsFragment) {
                    log("R.id.llSetting");
                    enableSelection(binding.tvSettings, binding.ivSettings, binding.llSettings, binding.flSetting);
                }
            }
        }, delay);
    }

    void enableSelection(TextView textView, ImageView imageView, LinearLayout linearLayout, FrameLayout frameLayout) {
        textView.setTextColor(getResources().getColor(R.color._161D26));
        textView.setBackground(ContextCompat.getDrawable(this, R.drawable.nv_selected_text_bg));
        frameLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.nv_selected_icon_bg));
        Utils.tint(imageView, R.color._33FF99, this);
    }


    boolean defaultFragmentLoaded = false;

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            if (fragment instanceof AddSelectClubFragment) {
                super.onBackPressed();
            } else {
                drawer.closeDrawer(ll_drawer);
                fragment = new AddSelectClubFragment();
                Utils.changeFragment(this, new AddSelectClubFragment(), null);
                defaultFragmentLoaded = true;
            }
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    private void cartShowOrHide(boolean show) {
        if (show) {
            cartLayout.setVisibility(View.VISIBLE);
        } else {
            cartLayout.setVisibility(View.GONE);
        }
    }

    public static void notificationShowOrHide(boolean show) {
        if (show) {
            notification.setVisibility(View.VISIBLE);
            notification_rl_main.setVisibility(View.VISIBLE);
        } else {
            notification.setVisibility(View.GONE);
            notification_rl_main.setVisibility(View.GONE);
            ivQrCode.setVisibility(View.GONE);
            ivChatToolbar.setVisibility(View.GONE);
            rlChatCount.setVisibility(View.GONE);
            tvChatCount.setVisibility(View.GONE);
        }
    }

    public static void scanQrCodeShowOrHide(boolean show) {
        if (show) {
            ivQrCode.setVisibility(View.VISIBLE);
            ivChatToolbar.setVisibility(View.VISIBLE);
            rlChatCount.setVisibility(View.VISIBLE);
            tvChatCount.setVisibility(View.VISIBLE);
            if((totalClubChatCount + totalChatCount) == 0) {
                rlShowCount.setVisibility(View.GONE);
            } else {
                rlShowCount.setVisibility(View.VISIBLE);
            }
        } else {
            ivQrCode.setVisibility(View.GONE);
            ivChatToolbar.setVisibility(View.GONE);
            rlChatCount.setVisibility(View.GONE);
            tvChatCount.setVisibility(View.GONE);
        }
    }

    public static void setNotificationcount(int count) {

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
        shape.setColor(Color.RED);
        shape.setStroke(2, Color.WHITE);

        notification_bg.setBackground(shape);

        if (count > 0) {
            MainActivity.notification_count_rl.setVisibility(View.VISIBLE);
        } else {
            MainActivity.notification_count_rl.setVisibility(View.GONE);

        }


        notification_count.setText("" + count);
    }


    private void setActionBarTitle(String title) {
        toolbarTitle.setText(title);
    }

    public void setToolbarText(String title) {
        toolbarTitle.setText(title);
    }


    private void checkLocation(int itemId) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {

            if (!(fragment instanceof MapFragment)) {
                fragment = MapFragment.newInstance();
                setActionBarTitle(getResources().getString(R.string.map));
                if (!Utils.isNoPayment()) {
                    if (Utils.cartCount > 0) {
                        cartCountLayout.setVisibility(View.VISIBLE);
                    } else {
                        cartCountLayout.setVisibility(View.GONE);

                    }
                    cartLayout.setVisibility(View.VISIBLE);
                    cart.setVisibility(View.VISIBLE);
                }
                notificationShowOrHide(true);
                scanQrCodeShowOrHide(false);
                tvSave.setVisibility(View.GONE);
                highlightSelectedView(itemId, false);
            }


        }
    }


    private void checkSecondLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {
            displaySelectedScreen(R.id.llMap);


        }
    }

    public static void hideKeyboard(View view,Context context) {
        InputMethodManager in = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        User user;
        Gson gson;
        String json;
        switch (v.getId()) {
            case R.id.imgDrawer:
                drawer.openDrawer(ll_drawer);
                highlightSelectedView(0, true);
                hideKeyboard(v,getApplication());
                break;
            case R.id.cart_rl:
                displaySelectedScreen(R.id.cart_rl);
                break;
            case R.id.image_notification:
                displaySelectedScreen(R.id.menu_notification);
                break;
            case R.id.ll_switch_account:
                if (switchAccountOpenOrClose) {
                    switchAccount.setRotation(90);
                    switchAccountOpenOrClose = false;
                    llMenu.setVisibility(View.VISIBLE);
                    rl_add_account.setVisibility(View.GONE);
                    recyclerViewAccountList.setVisibility(View.GONE);
                } else {
                    llMenu.setVisibility(View.GONE);
                    rl_add_account.setVisibility(View.VISIBLE);
                    if (userList.size() > 1) {
                        recyclerViewAccountList.setVisibility(View.VISIBLE);

                    } else {
                        recyclerViewAccountList.setVisibility(View.GONE);

                    }
                    switchAccount.setRotation(270);
                    switchAccountOpenOrClose = true;
                }
                break;
            case R.id.account_first:
                SharedPreference.clearCredentails(MainActivity.this);
                user = new User();
                user.setUserId(userList.get(1).getUserId());
                user.setEmail(userList.get(1).getEmail());
                user.setName(userList.get(1).getName());
                user.setImage(userList.get(1).getImage());
                user.setPushToken(userList.get(1).getPushToken());
                user.setMemberNo(userList.get(1).getMemberNo());
                user.setUserGroup(userList.get(1).getUserGroup());
                user.setWorkStation(userList.get(1).getWorkStation());
                user.setDomain(userList.get(1).getDomain());
                user.setUserType(userList.get(1).getUserType());
                user.setCartCount(userList.get(1).getCartCount());
                SharedPreference.setClubName(MainActivity.this, userList.get(1).getDomain());

                if (!Utils.isNoPayment()) {
                    Utils.cartCount = Integer.parseInt(userData.getCartCount());
                }

                gson = new Gson();
                json = gson.toJson(user);
                SharedPreference.storeCredentials(MainActivity.this, json);
                Collections.swap(userList, 1, 0);
                SharedPreference.setUserListPref(MainActivity.this, "userList", userList);

                finish();
                startActivity(getIntent());
                break;
            case R.id.account_second:
                SharedPreference.clearCredentails(MainActivity.this);
                user = new User();
                user.setUserId(userList.get(2).getUserId());
                user.setEmail(userList.get(2).getEmail());
                user.setName(userList.get(2).getName());
                user.setImage(userList.get(2).getImage());
                user.setPushToken(userList.get(2).getPushToken());
                user.setMemberNo(userList.get(2).getMemberNo());
                user.setUserGroup(userList.get(2).getUserGroup());
                user.setWorkStation(userList.get(2).getWorkStation());
                user.setDomain(userList.get(2).getDomain());
                user.setUserType(userList.get(2).getUserType());
                user.setCartCount(userList.get(2).getCartCount());
                SharedPreference.setClubName(MainActivity.this, userList.get(2).getDomain());

                if (!Utils.isNoPayment()) {
                    Utils.cartCount = Integer.parseInt(userData.getCartCount());
                }

                gson = new Gson();
                json = gson.toJson(user);
                SharedPreference.storeCredentials(MainActivity.this, json);
                Collections.swap(userList, 2, 0);
                SharedPreference.setUserListPref(MainActivity.this, "userList", userList);

                finish();
                startActivity(getIntent());
                break;
            case R.id.llHome:
                displaySelectedScreen(R.id.llHome);
                break;
            case R.id.llMenuSide:
                displaySelectedScreen(R.id.llMenuSide);
                break;
            case R.id.llCategory:
                displaySelectedScreen(R.id.llCategory);
                break;
            case R.id.llDonation:
                displaySelectedScreen(R.id.llDonation);
                break;
            case R.id.llChat:
                displaySelectedScreen(R.id.llChat);
                break;
            case R.id.llCCSMember:
                displaySelectedScreen(R.id.llCCSMember);
                break;
            case R.id.llDispenseHistory:
                displaySelectedScreen(R.id.llDispenseHistory);
                break;
            case R.id.llMyProfile:
                displaySelectedScreen(R.id.llMyProfile);
                break;
            case R.id.llNotification:
                displaySelectedScreen(R.id.llNotification);
                break;
            case R.id.llSettings:
                displaySelectedScreen(R.id.llSettings);
                break;
            case R.id.llPreRegister:
                displaySelectedScreen(R.id.llPreRegister);
                break;
            case R.id.llLanguages:
                displaySelectedScreen(R.id.llLanguages);
                break;
            case R.id.llLogout:
                displaySelectedScreen(R.id.llLogout);
                break;
            case R.id.llMap:
                checkSecondLocation();
                break;
            case R.id.llChangePassword:
                displaySelectedScreen(R.id.llChangePassword);
                break;
            case R.id.llMyClubs:
                displaySelectedScreen(R.id.rl_add_account);
                break;
            case R.id.llTopProfile:
                if (!SharedPreference.getClubName(this).isEmpty()) {
                    enableSelection(binding.tvMyProfile, binding.ivMyProfile, binding.llMyProfile, binding.flProfile);

                    displaySelectedScreen(R.id.llMyProfile);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    displaySelectedScreen(R.id.llMap);
                }
            } else {
                Toast.makeText(this,getResources().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
    }

    @Override
    public void dashboardBackPressed() {

        if (count == 1) {
            if (exit) {
                ActivityCompat.finishAffinity(MainActivity.this);

            } else {
                toast(this.getString(R.string.press_text));
                exit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 1000);

            }
        } else {
            count = 1;

        }
    }

    private void getCartCount() {
        String user = SharedPreference.getUserID(mContext);
        Gson gson = new Gson();
        //UserData userData = gson.fromJson(user, UserData.class);
        User userData = gson.fromJson(user, User.class);
        log("category>>" + "http://devsj72web.websiteserverhost.com/cannabisclub/api/userLoginSwitch?language=" + SharedPreference.getSelectedLanguage(mContext) +
                "&club_name=" + SharedPreference.getClubName(mContext) + "&user_id=" + userData.getUserId());
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.getCartCount(SharedPreference.getClubName(mContext), userData.getUserId(), SharedPreference.getSelectedLanguage(this), Utils.getNetworkAddress(this));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    log("json " + "object" + jsonObject);
                    int flag = jsonObject.getInt("flag");
                    if (flag == 1) {


                        try {

                            int count = jsonObject.getInt("cart_count");

                            Utils.cartCount = count;
                            if (!Utils.isNoPayment()) {
                                if (Utils.cartCount > 0) {
                                    MainActivity.cartCountLayout.setVisibility(View.VISIBLE);
                                } else {
                                    MainActivity.cartCountLayout.setVisibility(View.GONE);

                                }
                            }
                            /*int notificationCount = jsonObject.getInt("notification_count");
                            MainActivity.setNotificationcount(notificationCount);
                            Utils.notificationcount = notificationCount;*/

                            cartCounter.setText("" + Utils.cartCount);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        String msg = jsonObject.getString("message");
                        toast(msg);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        SharedPreference.setBoolean(this, SharedPreference.IS_LOGIN, true);
        if (!SharedPreference.getClubName(this).isEmpty()) {
            getCartCount();
        }
    }

    @Override
    protected void onStop() {
        try {
            if (updateUIReciver != null) {
                unregisterReceiver(updateUIReciver);
            }
        } catch (Exception e) {
            // already unregistered
        }
        super.onStop();
//        SharedPreference.setBoolean(this, SharedPreference.IS_LOGIN, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        SharedPreference.setBoolean(this, SharedPreference.IS_LOGIN, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("IBRAHIM MAIN", "onDestroy");
//        databaseReference.removeEventListener(val);
//        clubDatabaseReference.removeEventListener(valClub);
        SharedPreference.setBoolean(this, SharedPreference.IS_LOGIN, false);
        Log.e("IBRAHIM MAIN", String.valueOf(SharedPreference.getBoolean(this, SharedPreference.IS_LOGIN)));
    }

    public FragmentRefreshListener getFragmentRefreshListener() {
        return fragmentRefreshListener;
    }

    public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.fragmentRefreshListener = fragmentRefreshListener;
    }

    public interface FragmentRefreshListener{
        void onRefresh();
    }

}
//irenavion@gmail.com
//4ppS7uf!
//
//        app@cannabisclub.systems
//4ppS7uf!
//
//        irena.trdin@gmail.com
//4ppS7uf!
