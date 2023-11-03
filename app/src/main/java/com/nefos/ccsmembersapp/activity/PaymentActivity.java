package com.nefos.ccsmembersapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

//import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.common.api.Status;
//import com.google.android.gms.identity.intents.model.UserAddress;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.android.gms.wallet.AutoResolveHelper;
//import com.google.android.gms.wallet.CardInfo;
//import com.google.android.gms.wallet.CardRequirements;
//import com.google.android.gms.wallet.IsReadyToPayRequest;
//import com.google.android.gms.wallet.PaymentData;
//import com.google.android.gms.wallet.PaymentDataRequest;
//import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
//import com.google.android.gms.wallet.PaymentsClient;
//import com.google.android.gms.wallet.TransactionInfo;
//import com.google.android.gms.wallet.Wallet;
//import com.google.android.gms.wallet.WalletConstants;
import com.google.gson.Gson;
import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.Utils;
import com.nefos.ccsmembersapp.model.User;
import com.nefos.ccsmembersapp.server.ApiClient;
import com.nefos.ccsmembersapp.server.ApiInterface;
//import com.stripe.android.Stripe;
//import com.stripe.android.TokenCallback;
//import com.stripe.android.model.Card;
//import com.stripe.android.model.Token;
//import com.stripe.android.view.CardMultilineWidget;
//import com.stripe.exception.APIConnectionException;
//import com.stripe.exception.APIException;
//import com.stripe.exception.AuthenticationException;
//import com.stripe.exception.CardException;
//import com.stripe.exception.InvalidRequestException;
//import com.stripe.model.Charge;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends BaseActivity {

//    private final String TAG = PaymentActivity.class.getSimpleName();
//    private Context context;
//    private String dataTosend = "", comment = "";
//    private CardMultilineWidget cardInputWidget;
//    private JSONObject jsonObject;
//    private Button btnPayNow;
//    private TextView toolbarTitle;
//    private ApiClient apiClient;
//    private ProgressDialog progressDialog;
//    private ImageView imgDrawer;
//    private String dataToSend = "", userGrandTotal = "", creditAmount = "", amount = "";
//    private User userData;
//    private PaymentsClient paymentsClient;
//    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 53;
//    private View mPayWithGoogleButton;
//    private boolean isFromCart;
//    private ImageView imgUp, imgDown;
//    private CardView cardViewGooglePay;
//    private String strStripeKey = " ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_payment);
//        context = this;
//        isFromCart = getIntent().getBooleanExtra("isFromCart", false);
//        String user = SharedPreference.getUserID(PaymentActivity.this);
//        Gson gson = new Gson();
//        userData = gson.fromJson(user, User.class);
//        if (isFromCart) {
//            dataTosend = getIntent().getStringExtra("json");
//            userGrandTotal = getIntent().getStringExtra("userGrandTotal");
//            amount = userGrandTotal;
//            try {
//                jsonObject = new JSONObject(dataTosend);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        } else {
//            creditAmount = getIntent().getStringExtra("creditAmount");
//            comment = getIntent().getStringExtra("comment");
//            amount = creditAmount;
//        }
//        setToolbar();
//        setComponents();
//        registerListener();
//
//        paymentsClient = Wallet.getPaymentsClient(this,
//                new Wallet.WalletOptions.Builder()
//                        .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
//                        .build());
//        isReadyToPay();
//
//        getStripePaymentKey();

    }


//    private void setToolbar() {
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//       ImageView cart = toolbar.findViewById(R.id.image_cart);
//       cart.setVisibility(View.GONE);
//        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
//        imgDrawer = toolbar.findViewById(R.id.imgDrawer);
//        imgDrawer.setVisibility(View.GONE);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayShowTitleEnabled(false);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            toolbarTitle.setText(context.getResources().getString(R.string.payment));
//        }
//
//
//    }
//
//    private void setComponents() {
//        apiClient = new ApiClient();
//        progressDialog = new ProgressDialog(context, R.style.ProgressDialogStyle);
//        progressDialog.setMessage(getResources().getString(R.string.loading));
//        progressDialog.setCancelable(false);
//        progressDialog.setCanceledOnTouchOutside(false);
//        mPayWithGoogleButton = findViewById(R.id.btn_buy_pwg);
//        mPayWithGoogleButton.setEnabled(false);
//        imgUp = findViewById(R.id.imgUp);
//        imgDown = findViewById(R.id.imgDown);
//        btnPayNow = findViewById(R.id.btnPayNow);
//        cardViewGooglePay = findViewById(R.id.cardViewGooglePay);
//
//    }
//
//    private void registerListener() {
//        mPayWithGoogleButton.setOnClickListener(this);
//        btnPayNow.setOnClickListener(this);
//        imgUp.setOnClickListener(this);
//        imgDown.setOnClickListener(this);
//        cardViewGooglePay.setOnClickListener(this);
//
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.cardViewGooglePay:
//                PaymentDataRequest request = createPaymentDataRequest();
//                if (request != null) {
//                    AutoResolveHelper.resolveTask(
//                            paymentsClient.loadPaymentData(request),
//                            this,
//                            LOAD_PAYMENT_DATA_REQUEST_CODE);
//                }
//                break;
//            case R.id.imgUp:
//                if (imgUp.getVisibility() == View.VISIBLE) {
//                    imgUp.setVisibility(View.GONE);
//                    imgDown.setVisibility(View.VISIBLE);
//                    cardInputWidget.setVisibility(View.VISIBLE);
//                    btnPayNow.setVisibility(View.VISIBLE);
//
//                } else {
//                    imgUp.setVisibility(View.VISIBLE);
//                    imgDown.setVisibility(View.GONE);
//                    cardInputWidget.setVisibility(View.GONE);
//                    btnPayNow.setVisibility(View.GONE);
//
//                }
//                break;
//            case R.id.imgDown:
//                if (imgDown.getVisibility() == View.VISIBLE) {
//                    imgDown.setVisibility(View.GONE);
//                    imgUp.setVisibility(View.VISIBLE);
//                    cardInputWidget.setVisibility(View.GONE);
//                    btnPayNow.setVisibility(View.GONE);
//                } else {
//                    imgDown.setVisibility(View.VISIBLE);
//                    imgUp.setVisibility(View.GONE);
//                    cardInputWidget.setVisibility(View.VISIBLE);
//                    btnPayNow.setVisibility(View.VISIBLE);
//
//                }
//                break;
//            case R.id.btnPayNow:
//                Card card = cardInputWidget.getCard();
//
//                if (card == null) {
//                    toast("Invalid Card Data");
//                    return;
//                }
//                final Stripe stripe = new Stripe(context, strStripeKey);
//                progressDialog.show();
//                stripe.createToken(
//                        card,
//                        new TokenCallback() {
//                            public void onSuccess(@NonNull Token token) {
//                                log("token>>" + token.getId());
//                                if (isFromCart) {
//                                    try {
//                                        jsonObject.put("stripe_token", token.getId());
//                                        log("json>>" + jsonObject.toString());
//                                        dataToSend = jsonObject.toString();
//                                        //chargeClient(amount, "EUR", token);
//                                        placeOrder();
//
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//                                } else {
//                                    // chargeClient(amount, "EUR", token);
//                                    addDonation(token.getId());
//
//                                }
//
//                            }
//
//                            public void onError(@NonNull Exception error) {
//                                progressDialog.dismiss();
//                                toast(error.getMessage());
//                            }
//                        }
//                );
//                break;
//
//        }
//    }
//
//    public void chargeClient(String price, String currency, Token token) {
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
//                .permitAll().build();
//        StrictMode.setThreadPolicy(policy);
//
//        com.stripe.Stripe.apiKey = Utils.SECRET_KEY;
//
//        Map<String, Object> chargeParams = new HashMap<String, Object>();
//        chargeParams.put("amount", (int) Double.parseDouble(amount) * 100);
//        chargeParams.put("currency", currency);
//        chargeParams.put("card", token.getId());
//
//
//        try {
//            Charge charge = Charge.create(chargeParams, Utils.SECRET_KEY);
//
//            if (charge.getStatus().equals("succeeded")) {
//
//                String txnId = charge.getId();
//                log("Transaction Id:-" + txnId);
//
//                if (isFromCart) {
//                    try {
//                        jsonObject.put("transaction_id", txnId);
//                        log("json>>" + jsonObject.toString());
//                        dataToSend = jsonObject.toString();
//                        placeOrder();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    addDonation(txnId);
//                }
//
//                log("test "+ "Invoice: " + charge.toString());
//
//
//            } else {
//                progressDialog.dismiss();
//                log("test "+ "err: " + charge.getStatus());
//
//            }
//        } catch (AuthenticationException e) {
//            progressDialog.dismiss();
//
//            e.printStackTrace();
//            log("test "+ "err: " + e.getMessage());
//
//        } catch (InvalidRequestException e) {
//            progressDialog.dismiss();
//
//            e.printStackTrace();
//            log("test "+ "err: " + e.getMessage());
//
//        } catch (APIConnectionException e) {
//            progressDialog.dismiss();
//
//            e.printStackTrace();
//            log("test "+ "err: " + e.getMessage());
//
//        } catch (CardException e) {
//            progressDialog.dismiss();
//
//            e.printStackTrace();
//            log("test "+ "err: " + e.getMessage());
//
//        } catch (APIException e) {
//            progressDialog.dismiss();
//
//            e.printStackTrace();
//            log("test "+ "err: " + e.getMessage());
//
//        }
//
//
//    }
//
//    private void addDonation(String token) {
//
//        JSONObject jsonObject = new JSONObject();
//
//        try {
//            jsonObject.put("stripe_token", token);
//            jsonObject.put("donation_amount", amount);
//            jsonObject.put("comment", comment);
//            jsonObject.put("user_id", userData.getUserId());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
//        Call<ResponseBody> call = apiService.addDonation(SharedPreference.getSelectedLanguage(getApplicationContext()),
//                SharedPreference.getClubName(getApplicationContext()), jsonObject.toString(), Utils.getNetworkAddress(context));
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                try {
//                    progressDialog.dismiss();
//                    JSONObject jsonObj = new JSONObject(response.body().string());
//                    log("AddCredit>> "+ jsonObj.toString());
//                    String flag = jsonObj.getString("flag");
//                    String message = jsonObj.getString("message");
//
//                    if (flag.equals("1")) {
//                        toast(message);
//                        Intent i = new Intent(context, MainActivity.class);
//                        startActivity(i);
//                        finish();
//                    } else {
//                        toast(message);
//                        progressDialog.dismiss();
//
//                    }
//                } catch (JSONException e) {
//                    progressDialog.dismiss();
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    progressDialog.dismiss();
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                if (progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//            }
//        });
//    }
//
//    private void isReadyToPay() {
//        IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
//                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
//                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
//                .build();
//        paymentsClient.isReadyToPay(request).addOnCompleteListener(
//                new OnCompleteListener<Boolean>() {
//                    public void onComplete(Task<Boolean> task) {
//                        try {
//                            final boolean result =
//                                    task.getResult(ApiException.class);
//                            if (result == true) {
//                                mPayWithGoogleButton.setEnabled(true);
//
//                            } else {
//                                mPayWithGoogleButton.setEnabled(false);
//
//                            }
//                        } catch (ApiException exception) {
//                        }
//                    }
//                }
//        );
//    }
//
//    private PaymentMethodTokenizationParameters createTokenizationParameters() {
//        return PaymentMethodTokenizationParameters.newBuilder()
//                .setPaymentMethodTokenizationType(
//                        WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
//                .addParameter("gateway", "stripe")
//                .addParameter("stripe:publishableKey",
//                        strStripeKey)
//                .addParameter("stripe:version", "2018-11-08")
//                .build();
//    }
//
//    private PaymentDataRequest createPaymentDataRequest() {
//        return PaymentDataRequest.newBuilder()
//                .setTransactionInfo(
//                        TransactionInfo.newBuilder()
//                                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
//                                .setTotalPrice(amount)
//                                .setCurrencyCode("EUR")
//                                .build())
//                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
//                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
//                .setCardRequirements(
//                        CardRequirements.newBuilder()
//                                .addAllowedCardNetworks(Arrays.asList(
//                                        WalletConstants.CARD_NETWORK_AMEX,
//                                        WalletConstants.CARD_NETWORK_DISCOVER,
//                                        WalletConstants.CARD_NETWORK_VISA,
//                                        WalletConstants.CARD_NETWORK_MASTERCARD))
//                                .build())
//                .setPaymentMethodTokenizationParameters(createTokenizationParameters())
//                .build();
//    }
//
//    private void placeOrder() {
//        //  progressDialog.show();
//        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
//        log("placeOrder>>>"+"http://devsj72web.websiteserverhost.com/cannabisclub/api/orderPlace?language="+
//                SharedPreference.getSelectedLanguage(getApplicationContext())+"&club_name="+SharedPreference.getClubName(getApplicationContext())+"&order_request="+
//                dataToSend+"&macAddress="+Utils.getNetworkAddress(context) );
//        Call<ResponseBody> call = apiService.placeOrder(SharedPreference.getSelectedLanguage(getApplicationContext()),
//                SharedPreference.getClubName(getApplicationContext()), dataToSend, Utils.getNetworkAddress(context));
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                try {
//
//                    JSONObject jsonObj = new JSONObject(response.body().string());
//                    log("placeOrder>> "+ jsonObj.toString());
//                    String flag = jsonObj.getString("flag");
//                    String message = jsonObj.getString("message");
//
//                    if (flag.equals("1")) {
//                        toast(message);
//                        Intent i = new Intent(context, OrderSuccessActivity.class);
//                        i.putExtra("msg", message);
//                        startActivity(i);
//                        finish();
//                    } else {
//                        toast(message);
//                        progressDialog.dismiss();
//
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    progressDialog.dismiss();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    progressDialog.dismiss();
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                if (progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//            }
//        });
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                finish();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case LOAD_PAYMENT_DATA_REQUEST_CODE: {
//                switch (resultCode) {
//                    case RESULT_OK: {
//                        PaymentData paymentData = PaymentData.getFromIntent(data);
//                        CardInfo info = paymentData.getCardInfo();
//                        UserAddress address = paymentData.getShippingAddress();
//                        String rawToken = paymentData.getPaymentMethodToken()
//                                .getToken();
//
//                        // Now that you have a Stripe token object,
//                        // charge that by using the id
//                        Token stripeToken = Token.fromString(rawToken);
//                        if (stripeToken != null) {
//                            // This chargeToken function is a call to your own
//                            // server, which should then connect to Stripe's
//                            // API to finish the charge.
//                            log("token>>" + stripeToken.getId());
//
//                            if (isFromCart) {
//                                try {
//                                    jsonObject.put("stripe_token", stripeToken.getId());
//                                    log("json>>" + jsonObject.toString());
//                                    dataToSend = jsonObject.toString();
//                                    // chargeClient(amount, "EUR", stripeToken);
//                                    placeOrder();
//
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                // chargeClient(amount, "EUR", stripeToken);
//                                addDonation(stripeToken.getId());
//                            }
//
//                        }
//                        break;
//                    }
//                    case RESULT_CANCELED: {
//                        break;
//                    }
//                    case AutoResolveHelper.RESULT_ERROR: {
//                        Status status = AutoResolveHelper.getStatusFromIntent(data);
//                        // Log the status for debugging
//                        // Generally there is no need to show an error to
//                        // the user as the Google Payment API will do that
//                        break;
//                    }
//                    default: {
//                        // Do nothing.
//                    }
//                }
//                break; // Breaks the case LOAD_PAYMENT_DATA_REQUEST_CODE
//            }
//            // Handle any other startActivityForResult calls you may have made.
//            default: {
//                // Do nothing.
//            }
//        }
//    }
//
//    public void onBackPressed() {
//        super.onBackPressed();
//        finish();
//    }
//
//
//    private void getStripePaymentKey() {
//
//        String user = SharedPreference.getUserID(context);
//        Gson gson = new Gson();
//        //UserData userData = gson.fromJson(user, UserData.class);
//        User userData = gson.fromJson(user, User.class);
//        log("category>>" + "http://devsj72web.websiteserverhost.com/cannabisclub/api/getStripekey=" + SharedPreference.getSelectedLanguage(context) +
//                "&club_name=" + SharedPreference.getClubName(context) + "&user_id=" + userData.getUserId());
//        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
//        Call<ResponseBody> call = apiService.getStripePaymentKey(SharedPreference.getClubName(context), SharedPreference.getSelectedLanguage(this), Utils.getNetworkAddress(this));
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//
//                try {
//                    JSONObject jsonObject = new JSONObject(String.valueOf(response.body().string()));
//                    log("json "+ "object" + jsonObject);
//                    int flag = jsonObject.getInt("flag");
//                    if (flag == 1) {
//
//                        try {
//
//                            JSONObject data = jsonObject.getJSONObject("data");
//
//
//                            String strStripeEncodeKey = data.getString("stripe_key");
//
//                            byte[] dataDecode = Base64.decode(strStripeEncodeKey, Base64.DEFAULT);
//                            strStripeKey = new String(dataDecode, StandardCharsets.UTF_8);
//
//
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//
//                    } else {
//                        String msg = jsonObject.getString("message");
//                        //toast(msg);
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//
//            }
//        });
//    }
}