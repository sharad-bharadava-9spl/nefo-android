package com.nefos.ccsmembersapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.StringUtility;
import com.squareup.picasso.Picasso;

public class DisplayQRCodeActivity extends AppCompatActivity {

    ImageView ivQR;
    private ImageView ivBack;
    String qrCode;
    TextView tvIns, tvUserName, toolbar_title;
    ImageView ivBackArrow;
    TextView tvGoBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_qrcode);

        qrCode = getIntent().getStringExtra("qrCode");
        boolean isFromChat = getIntent().getBooleanExtra("fromChat", false);

        ivQR = findViewById(R.id.ivQR);
        ivBack = findViewById(R.id.ivBack);
        tvUserName = findViewById(R.id.tvUserName);
        tvIns = findViewById(R.id.tvIns);
        toolbar_title = findViewById(R.id.toolbar_title);
        ivBackArrow = findViewById(R.id.ivBackArrow);
        tvGoBack = findViewById(R.id.tvGoBack);

        if(isFromChat) {
            tvUserName.setVisibility(View.VISIBLE);
            tvIns.setVisibility(View.VISIBLE);
            ivBackArrow.setVisibility(View.VISIBLE);
            tvGoBack.setVisibility(View.VISIBLE);
            ivBack.setVisibility(View.GONE);
            toolbar_title.setVisibility(View.GONE);
            tvUserName.setText(SharedPreference.getString(DisplayQRCodeActivity.this, SharedPreference.PREF_KEY_USER_NAME));
        } else {
            ivBack.setVisibility(View.VISIBLE);
            toolbar_title.setVisibility(View.VISIBLE);
            tvUserName.setVisibility(View.GONE);
            tvIns.setVisibility(View.GONE);
            ivBackArrow.setVisibility(View.GONE);
            tvGoBack.setVisibility(View.GONE);
        }

        ivBack.setOnClickListener(v -> {
            onBackPressed();
        });

        ivBackArrow.setOnClickListener(v -> {
            onBackPressed();
        });

        tvGoBack.setOnClickListener(v -> {
            onBackPressed();
        });

        if(StringUtility.validateString(qrCode)) {
            Picasso.get()
                    .load(qrCode)
                    .placeholder(R.drawable.no_image_default)
                    .error(R.drawable.no_image_default)
                    .into(ivQR);
        }
    }
}