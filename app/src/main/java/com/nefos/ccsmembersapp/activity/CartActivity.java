package com.nefos.ccsmembersapp.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.Utils;
import com.nefos.ccsmembersapp.adpater.CartAdapter;
import com.nefos.ccsmembersapp.interfaces.OnCartRemoveClick;
import com.nefos.ccsmembersapp.model.CartTotal;
import com.nefos.ccsmembersapp.model.User;
import com.nefos.ccsmembersapp.model.ViewCartData;
import com.nefos.ccsmembersapp.server.ApiClient;
import com.nefos.ccsmembersapp.server.ApiInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends BaseActivity implements OnCartRemoveClick {
    private TextView toolbarTitle;
    RecyclerView recyclerView;
    CartAdapter cartAdapter;
    ArrayList<ViewCartData> arrayListViewCartdata;
    private ApiClient apiClient;
    private ProgressDialog progressDialog;
    private String Pre_order="";

    TextView cartCounter, tvitemtotalcount, tvcreditamount, tvpayableamount, tvempty;
    private ImageView cart, cart_bg, notification, imgDrawer;
    private RelativeLayout cartCountLayout, cartLayout;
    public static RelativeLayout rl_order,notification_rl_main,notification_count_rl;
    public static TextView tvpayorder,notification_count;
    public static TextView tvAmount;
    private ImageView imgCancel,imgPlaceOrder,notification_bg;
    private Context mContext;
    private String dataToSend = "";
    JSONObject dataObj;
    User userData;
    public static CartActivity cartActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_cart);
        mContext = this;
        cartActivity = this;
        String user = SharedPreference.getUserID(getApplicationContext());
        Gson gson = new Gson();
        userData = gson.fromJson(user, User.class);
        setToolbar();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        imgDrawer = toolbar.findViewById(R.id.imgDrawer);
        imgDrawer.setVisibility(View.GONE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            setActionBarTitle(getResources().getString(R.string.cart));
        }
        cart = toolbar.findViewById(R.id.image_cart);
        notification = toolbar.findViewById(R.id.image_notification);
        cartCountLayout = toolbar.findViewById(R.id.cart_count_rl);
        cartLayout = toolbar.findViewById(R.id.cart_rl);
        cart_bg = toolbar.findViewById(R.id.cart_bg);
        cartCounter = toolbar.findViewById(R.id.cart_count);
        cart_bg.setBackground(drawCircle());
        cart.setVisibility(View.VISIBLE);
       // notification.setVisibility(View.VISIBLE);

        notification_bg = toolbar.findViewById(R.id.notification_bg);
        notification_count_rl = toolbar.findViewById(R.id.notification_count_rl);
        notification_count = toolbar.findViewById(R.id.notification_count);
        notification_rl_main = toolbar.findViewById(R.id.notification_rl_main);

       setNotificationcount(Utils.notificationcount);
        initview();
        imgPlaceOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Pre_order.equals("1"))
                {
                    if (!CartAdapter.cartTotal.userGrandTotal.equals("0.00")) {
                        Intent i = new Intent(mContext, PaymentActivity.class);
                        i.putExtra("json", getData());
                        i.putExtra("userGrandTotal", CartAdapter.cartTotal.userGrandTotal);
                        i.putExtra("isFromCart", true);
                        i.putExtra("Pre_order",Pre_order);
                        startActivity(i);
                    } else {
                        dataToSend = getData();
                        progressDialog.show();
                        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
                        Call<ResponseBody> call = apiService.placeOrder(SharedPreference.getSelectedLanguage(getApplicationContext()),
                                SharedPreference.getClubName(getApplicationContext()), dataToSend,Utils.getNetworkAddress(mContext));
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {

                                    JSONObject jsonObj = new JSONObject(response.body().string());
                                    log(jsonObj.toString());
                                    String flag = jsonObj.getString("flag");
                                    String message = jsonObj.getString("message");

                                    if (flag.equals("1")) {
                                        toast(message);
                                        Intent i = new Intent(mContext, OrderSuccessActivity.class);
                                        i.putExtra("msg", message);
                                        startActivity(i);
                                        finish();
                                    } else {
                                        toast(message);

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                    if (arrayListViewCartdata.isEmpty()) {
                                        recyclerView.setVisibility(View.GONE);
                                        tvempty.setVisibility(View.VISIBLE);
                                    }
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
                else
                {
                    toast(mContext.getResources().getString(R.string.permission_err_msg));

                }


            }
        });
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CartActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    private String getData() {

        dataObj = new JSONObject();
        try {
            dataObj.put("total_price", CartAdapter.cartTotal.totalPrice);
            dataObj.put("user_credit", CartAdapter.cartTotal.userCredit);
            dataObj.put("user_grand_total", CartAdapter.cartTotal.userGrandTotal);
            dataObj.put("user_discount", CartAdapter.cartTotal.userDiscount);
            dataObj.put("userupdatecredit", CartAdapter.cartTotal.userupdateCredit);
            dataObj.put("payment_mode", "credit");
            dataObj.put("stripe_token", "");
            dataObj.put("user_id", userData.getUserId() + "");

            JSONArray cartItemsArray = new JSONArray();
            JSONObject cartItemsObject;

            for (int i = 0; i < CartAdapter.arrayListViewCartdata.size(); i++) {
                cartItemsObject = new JSONObject();

                cartItemsObject.putOpt("product_id", CartAdapter.arrayListViewCartdata.get(i).product_id);
                cartItemsObject.putOpt("category_id", CartAdapter.arrayListViewCartdata.get(i).category_id);
                cartItemsObject.putOpt("category_name", CartAdapter.arrayListViewCartdata.get(i).category_name);
                cartItemsObject.putOpt("category_type", CartAdapter.arrayListViewCartdata.get(i).category_type);
                cartItemsObject.putOpt("product_name", CartAdapter.arrayListViewCartdata.get(i).product_name);
                cartItemsObject.putOpt("extra_price", CartAdapter.arrayListViewCartdata.get(i).extra_price);
                cartItemsObject.putOpt("extra_price_count", CartAdapter.arrayListViewCartdata.get(i).extra_price_count);
                cartItemsObject.putOpt("is_discount", CartAdapter.arrayListViewCartdata.get(i).isDiscount);
                cartItemsObject.putOpt("user_discount", CartAdapter.arrayListViewCartdata.get(i).userDiscount);
                cartItemsObject.putOpt("user_discount_price", CartAdapter.arrayListViewCartdata.get(i).userDiscountPrice);

                cartItemsArray.put(cartItemsObject);

            }

            dataObj.put("data", cartItemsArray);
            log(dataObj.toString());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dataObj.toString();
    }

    private void initview() {
        apiClient = new ApiClient();
        progressDialog = new ProgressDialog(CartActivity.this, R.style.ProgressDialogStyle);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        recyclerView = findViewById(R.id.recyclerView);
        tvitemtotalcount = findViewById(R.id.tvitemtotalcount);
        tvcreditamount = findViewById(R.id.tvcreditamount);
        tvpayableamount = findViewById(R.id.tvpayableamount);
        tvAmount = findViewById(R.id.tvAmount);
        tvpayorder = findViewById(R.id.tvpayorder);
        imgCancel = findViewById(R.id.imgCancel);
        imgPlaceOrder = findViewById(R.id.imgPlaceOrder);
        rl_order = findViewById(R.id.rl_order);
        tvempty = findViewById(R.id.tvempty);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CartActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        //recyclerView.setLayoutManager(new LinearLayoutManager(CartActivity.this));

        recyclerView.setNestedScrollingEnabled(false);
        arrayListViewCartdata = new ArrayList<>();
        if (Utils.isNetworkConnected(getApplicationContext())) {
            getViewCartData();
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.show();
            }
        } else {
            toast(getResources().getString(R.string.no_internet));

        }


    }

    private Drawable drawCircle() {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setCornerRadii(new float[]{0, 0, 0, 0, 0, 0, 0, 0});
        shape.setColor(Color.RED);
        shape.setStroke(2, Color.WHITE);
        return shape;
    }

    private void setActionBarTitle(String title) {
        toolbarTitle.setText(title);
    }

    private void getViewCartData() {

        log("ViewCart>> "+
                "http://devsj72web.websiteserverhost.com/cannabisclub/api/viewCart?language=" + SharedPreference.getSelectedLanguage(getApplicationContext())
                + "&club_name=" + SharedPreference.getClubName(getApplicationContext()) + "&user_id=" + userData.getUserId());
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.getviewcart(SharedPreference.getSelectedLanguage(getApplicationContext()),
                SharedPreference.getClubName(getApplicationContext()), userData.getUserId(),Utils.getNetworkAddress(mContext));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (arrayListViewCartdata == null) {
                        recyclerView.setVisibility(View.GONE);
                        tvempty.setVisibility(View.VISIBLE);
                    } else {
                        tvempty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    JSONObject jsonObject = new JSONObject(String.valueOf(response.body().string()));
                    String flag = jsonObject.getString("flag");
                    log("view cart"+ "== " + jsonObject);

                    if (flag.equals("1")) {

                        int cart_count = jsonObject.getInt("cart_count");
                        String total_price = jsonObject.getString("total_price");
                        Pre_order = jsonObject.getString("Pre_order");
                        String user_credit = jsonObject.getString("user_credit");
                        String user_grand_total = jsonObject.getString("user_grand_total");
                        String user_credit_detail = jsonObject.getString("user_credit_detail");
                        String usercredit_title = jsonObject.getString("usercredit_title");
                        String user_discount = jsonObject.getString("user_discount");
                        String userupdate_credit = jsonObject.getString("userupdate_credit");
                        String userupdatecredit = jsonObject.getString("userupdatecredit");
                        if (cart_count > 0) {
                            cartCountLayout.setVisibility(View.VISIBLE);
                            cartCounter.setText(cart_count+"");

                        } else {
                            cartCountLayout.setVisibility(View.GONE);

                        }
                        CartTotal cartTotal = new CartTotal(getApplicationContext());
                        cartTotal.cartCount = String.valueOf(cart_count);
                        cartTotal.totalPrice = total_price;
                        cartTotal.userCredit = user_credit;
                        cartTotal.userGrandTotal = user_grand_total;
                        cartTotal.user_credit_detail = user_credit_detail;
                        cartTotal.userDiscount = user_discount;
                        cartTotal.userUpdatedCredit = userupdate_credit;
                        cartTotal.userupdateCredit = userupdatecredit;
                        rl_order.setVisibility(View.VISIBLE);
                        Gson gson = new Gson();
                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            ViewCartData categoriesList = gson.fromJson(jsonArray.getString(i),
                                    ViewCartData.class);
                            arrayListViewCartdata.add(categoriesList);

                        }
                        if (arrayListViewCartdata != null) {
                            tvempty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                        } else {
                            recyclerView.setVisibility(View.GONE);
                            tvempty.setVisibility(View.VISIBLE);
                        }
                        /*if (arrayListViewCartdata.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            tvempty.setVisibility(View.VISIBLE);
                        } else {

                            tvempty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }*/
                        //cartAdapter.notifyDataSetChanged();
                        tvitemtotalcount.setText(String.valueOf(cart_count));

                        tvcreditamount.setText(user_credit.concat(" ").concat("€"));
                        tvpayableamount.setText(user_grand_total);
                       /* if (user_grand_total.contains("0 €")) {
                            tvpayorder.setText(getResources().getString(R.string.remaining_credit) + " " + user_grand_total);
                        } else {
                            tvpayorder.setText(getResources().getString(R.string.pay) + " " + user_grand_total);
                        }*/
                        tvpayorder.setText(usercredit_title);
                        if(usercredit_title.equals("Remaining Credit"))
                        {
                            tvAmount.setText(user_credit_detail);

                        }
                        else
                        {
                            tvAmount.setText(user_grand_total.concat(" €"));

                        }
                        cartAdapter = new CartAdapter(CartActivity.this, arrayListViewCartdata, CartActivity.this, cartTotal, CartActivity.this);
                        recyclerView.setAdapter(cartAdapter);

                        cartAdapter.notifyDataSetChanged();
                    } else {

                        if(Utils.cartCount>0)
                        {
                            cartCountLayout.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            cartCountLayout.setVisibility(View.GONE);

                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    if (arrayListViewCartdata.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        tvempty.setVisibility(View.VISIBLE);
                    }
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent(CartActivity.this, MainActivity.class);
                startActivity(i);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(CartActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onCartRemoveClick(final ViewCartData viewCartData, final RelativeLayout ll_remove, final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(mContext.getResources().getString(R.string.delete_product_dialog));
        builder.setPositiveButton(mContext.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog = new ProgressDialog(CartActivity.this, R.style.ProgressDialogStyle);
                progressDialog.setMessage(CartActivity.this.getResources().getString(R.string.loading));
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                if (progressDialog != null && !progressDialog.isShowing()) {
                    progressDialog.show();
                }
                getDeleteCart(viewCartData, ll_remove, position);
            }

        });
        builder.setNegativeButton(mContext.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();


    }

    private void getDeleteCart(ViewCartData viewCartData, RelativeLayout ll_remove, final int position) {

        String user = SharedPreference.getUserID(CartActivity.this);
        Gson gson = new Gson();
        User userData = gson.fromJson(user, User.class);
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.deleteCart(SharedPreference.getSelectedLanguage(CartActivity.this),
                SharedPreference.getClubName(CartActivity.this), userData.getUserId(), Integer.parseInt(viewCartData.product_id),
                Utils.deviceId(CartActivity.this), "Android",Utils.getNetworkAddress(mContext));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response.body().string()));
                    log("delete cart"+ "== " + jsonObject);
                    String flag = jsonObject.getString("flag");
                    String message = jsonObject.getString("message");

                    if (flag.equals("1")) {
                        progressDialog.dismiss();

                        int cart_count = jsonObject.getInt("cart_count");
                        String total_price = jsonObject.getString("total_price");
                        String user_credit = jsonObject.getString("user_credit");
                        String user_grand_total = jsonObject.getString("user_grand_total");
                        String user_credit_detail = jsonObject.getString("user_credit_detail");
                        String usercredit_title = jsonObject.getString("usercredit_title");

                        String user_discount = jsonObject.getString("user_discount");
                        String userupdate_credit = jsonObject.getString("userupdate_credit");
                        String userupdatecredit = jsonObject.getString("userupdatecredit");
                        Utils.cartCount=cart_count;
                        if (cart_count > 0) {
                            cartCountLayout.setVisibility(View.VISIBLE);
                            cartCounter.setText(cart_count+"");

                        } else {
                            cartCountLayout.setVisibility(View.GONE);

                        }
                        CartTotal cartTotal = new CartTotal(getApplicationContext());
                        cartTotal.cartCount = String.valueOf(cart_count);
                        cartTotal.totalPrice = total_price;
                        cartTotal.userCredit = user_credit;
                        cartTotal.userGrandTotal = user_grand_total;
                        cartTotal.user_credit_detail = user_credit_detail;
                        cartTotal.userDiscount = user_discount;
                        cartTotal.userUpdatedCredit = userupdate_credit;
                        cartTotal.userupdateCredit = userupdatecredit;
                        arrayListViewCartdata.remove(position);
                        if (arrayListViewCartdata.isEmpty()) {
                            rl_order.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.GONE);
                            tvempty.setVisibility(View.VISIBLE);
                        } else {
                            rl_order.setVisibility(View.VISIBLE);
                            tvempty.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                        tvitemtotalcount.setText(String.valueOf(cart_count));

                        tvcreditamount.setText(user_credit.concat(" ").concat("€"));
                        tvpayableamount.setText(user_grand_total);
                       /* if (user_grand_total.contains("0 €")) {
                            tvpayorder.setText(getResources().getString(R.string.remaining_credit) + " " + user_grand_total);
                        } else {
                            tvpayorder.setText(getResources().getString(R.string.pay) + " " + user_grand_total);
                        }*/
                        tvpayorder.setText(usercredit_title);
                        if(usercredit_title.equals("Remaining Credit"))
                        {
                            tvAmount.setText(user_credit_detail);

                        }
                        else
                        {
                            tvAmount.setText(user_grand_total.concat(getResources().getString(R.string.sign)));

                        }
                        cartAdapter = new CartAdapter(CartActivity.this, arrayListViewCartdata, CartActivity.this, cartTotal, CartActivity.this);
                        recyclerView.setAdapter(cartAdapter);

                        cartAdapter.notifyDataSetChanged();

                    } else {
                        toast(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
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

    public  void setNotificationcount(int count){

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



        notification_count.setText(""+count);
    }
}
