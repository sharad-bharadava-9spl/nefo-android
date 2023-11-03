package com.nefos.ccsmembersapp.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import com.google.firebase.messaging.FirebaseMessaging;
import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.Utils;

public class SplashActivity extends BaseActivity {

    boolean isFromNotification = false;
    String type = "";
    String userId = "";
    String clubName = "";
    String conversationId = "";
    String receiverId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        int DELAY_TIME = 500;
        setContentView(R.layout.activity_splash);
        log("SDK Int - "+Build.VERSION_CODES.S);

        if(getIntent() != null) {
            isFromNotification = getIntent().getBooleanExtra("isFromNotification", false);
            type = getIntent().getStringExtra("type");
            userId = getIntent().getStringExtra("userId");
            clubName = getIntent().getStringExtra("clubName");
            conversationId = getIntent().getStringExtra("conversationId");
            receiverId = getIntent().getStringExtra("receiverId");
            Log.e("isFromNotification", String.valueOf(isFromNotification));
            Log.e("type", String.valueOf(type));
            Log.e("userId", String.valueOf(userId));
            Log.e("clubName", String.valueOf(clubName));
            Log.e("conversationId", String.valueOf(conversationId));
            Log.e("receiverId", String.valueOf(receiverId));
            Log.e("IBRAHIM PREF", String.valueOf(SharedPreference.getBoolean(SplashActivity.this, SharedPreference.IS_LOGIN)));
        } else {
            Log.e("isFromNotification", "NO DATA FOUND");
        }


        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        String selectedLanguage = SharedPreference.getSelectedLanguage(SplashActivity.this);
        Utils.updateResources(SplashActivity.this, selectedLanguage);
        Handler handler = new Handler();

        handler.postDelayed(() -> {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    SharedPreference.setFCMToken(SplashActivity.this, task.getResult());
                }
            });

            if(isFromNotification) {
                if(SharedPreference.getBoolean(SplashActivity.this, SharedPreference.IS_LOGIN)) {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.putExtra("isFromNotification", isFromNotification);
                    intent.putExtra("type", type);
                    intent.putExtra("userId", userId);
                    intent.putExtra("clubName", clubName);
                    intent.putExtra("conversationId", conversationId);
                    intent.putExtra("receiverId", receiverId);
                    startActivity(intent);
                    finish();
                } else {
                    SharedPreference.clearUserData(SplashActivity.this);
                    Intent intent = new Intent(SplashActivity.this, LoginAcvitiy.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                if (SharedPreference.getBoolean(SplashActivity.this, SharedPreference.IS_LOGIN)) {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    SharedPreference.clearUserData(SplashActivity.this);
                    Intent intent = new Intent(SplashActivity.this, LoginAcvitiy.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, DELAY_TIME);
    }
}
