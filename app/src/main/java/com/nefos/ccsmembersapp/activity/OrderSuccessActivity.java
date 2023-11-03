package com.nefos.ccsmembersapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.Utils;

public class OrderSuccessActivity extends BaseActivity implements View.OnClickListener {

    private Context context;
    private TextView tvMsg;
    private TextView toolbarTitle,tvTitle;
    private ImageView imgDrawer;
    private ImageView imgSuccess,cart;
    private Button btnGoHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_order_success);
        context = this;
        setToolbar();
        setComponents();
        registerListener();
        Utils.cartCount=0;
    }


    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        tvTitle = findViewById(R.id.tvTitle);
        toolbarTitle.setGravity(Gravity.CENTER);
        imgDrawer = toolbar.findViewById(R.id.imgDrawer);
        cart = toolbar.findViewById(R.id.image_cart);
        cart.setVisibility(View.GONE);
        imgDrawer.setVisibility(View.GONE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            tvTitle.setText(context.getResources().getString(R.string.order_success));
        }


    }

    private void setComponents() {
        tvMsg = findViewById(R.id.tvMsg);
        imgSuccess = findViewById(R.id.imgSuccess);
        btnGoHome = findViewById(R.id.btnGoHome);
        tvMsg.setText(context.getResources().getString(R.string.order_success_msg));
       /* Glide.with(this)
                .load(R.drawable.tenor)
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                .into(new DrawableImageViewTarget(imgSuccess));*/

    }

    private void registerListener() {
        btnGoHome.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(context, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
    }
}
