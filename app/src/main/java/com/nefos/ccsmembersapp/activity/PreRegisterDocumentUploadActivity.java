package com.nefos.ccsmembersapp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.nefos.ccsmembersapp.R;
import com.nefos.ccsmembersapp.Util.SharedPreference;
import com.nefos.ccsmembersapp.Util.Utils;
import com.nefos.ccsmembersapp.server.ApiClient;
import com.nefos.ccsmembersapp.server.ApiInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreRegisterDocumentUploadActivity extends BaseActivity {


    private Context mContext;
    Button btnPassportPic, btnYourSelfPic, btnSubmit;
    private LinearLayout parentView;
    private ApiClient apiClient;
    private ProgressDialog progressDialog;
    private ImageView ivPassport, ivYourself, ivBack;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS_PASSPORT = 111;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS_YOURSELF = 112;
    private static final int MAX_IMAGE_SIZE = 20000000; // 20 MB in bytes
    private String clubName, tempNumber, year, month, day, orderTime, orderDateDB, clubCode;
    private Uri imageUrlPassport, imageUrlSelfie;
    private Bitmap passportBitmap, selfieBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_register_document_upload);

        clubName = getIntent().getStringExtra("club_name");
        clubCode = getIntent().getStringExtra("club_code");
        orderTime = getIntent().getStringExtra("orderTime");
        orderDateDB = getIntent().getStringExtra("orderDateDB");
        tempNumber = getIntent().getStringExtra("tempNumber");
        year = getIntent().getStringExtra("year");
        month = getIntent().getStringExtra("month");
        day = getIntent().getStringExtra("day");
        mContext = this;
        MainActivity.count = 0;
        MainActivity.appBar.setVisibility(View.VISIBLE);
        initView();
    }

    private void initView() {
        MainActivity.toolbarTitle.setText(mContext.getResources().getString(R.string.pre_register));
        MainActivity.cartLayout.setVisibility(View.GONE);
        MainActivity.notification.setVisibility(View.GONE);
        MainActivity.notification_rl_main.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(mContext, R.style.ProgressDialogStyle);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        apiClient = new ApiClient();

        btnPassportPic = findViewById(R.id.btnPassportPic);
        btnYourSelfPic = findViewById(R.id.btnYourSelfPic);
        btnSubmit = findViewById(R.id.btnSubmit);
        ivPassport = findViewById(R.id.ivPassport);
        ivYourself = findViewById(R.id.ivYourself);
        ivBack = findViewById(R.id.ivBack);

        ivBack.setOnClickListener(v -> {
            onBackPressed();
        });

        parentView = findViewById(R.id.parentView);
        parentView.setOnClickListener(view1 -> Utils.closeKeyBoard(parentView, PreRegisterDocumentUploadActivity.this));

        btnPassportPic.setOnClickListener(v -> {
            int wExtStorePermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                if (wExtStorePermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded
                            .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded
                                .toArray(new String[0]),
                        REQUEST_ID_MULTIPLE_PERMISSIONS_PASSPORT);
            } else {
                chooseImagePassport(mContext);
            }
        });

        btnYourSelfPic.setOnClickListener(v -> {
            int wExtStorePermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                if (wExtStorePermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded
                            .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded
                                .toArray(new String[0]),
                        REQUEST_ID_MULTIPLE_PERMISSIONS_YOURSELF);
            } else {
                chooseImageYourSelf(mContext);
            }
        });

        btnSubmit.setOnClickListener(v -> {
            if(checkValidation()) {
                callDocumentApi();
            }
        });

    }

    public static boolean checkAndRequestPermissions(final Activity context, boolean isPassportImage) {
        int wExtStorePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int cameraPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (wExtStorePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded
                        .add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            if(isPassportImage) {
                ActivityCompat.requestPermissions(context, listPermissionsNeeded
                                .toArray(new String[0]),
                        REQUEST_ID_MULTIPLE_PERMISSIONS_PASSPORT);
            } else {
                ActivityCompat.requestPermissions(context, listPermissionsNeeded
                                .toArray(new String[0]),
                        REQUEST_ID_MULTIPLE_PERMISSIONS_YOURSELF);
            }
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS_PASSPORT:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(mContext,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    } else if (ContextCompat.checkSelfPermission(mContext,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {


                    } else {
                        chooseImagePassport(mContext);
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(mContext,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    } else {
                        chooseImagePassport(mContext);
                    }
                }
                break;
            case REQUEST_ID_MULTIPLE_PERMISSIONS_YOURSELF:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(mContext,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    } else if (ContextCompat.checkSelfPermission(mContext,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {


                    } else {
                        chooseImageYourSelf(mContext);
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(mContext,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                    } else {
                        chooseImageYourSelf(mContext);
                    }
                }
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
        return imageFile;
    }


    private Uri createImageFileUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".jpg";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(storageDir, imageFileName);
        return FileProvider.getUriForFile(PreRegisterDocumentUploadActivity.this, "com.nefos.ccsmembersapp.fileprovider", imageFile);
    }

    private void chooseImagePassport(Context context){
        final CharSequence[] optionsMenu = {getResources().getString(R.string.take_photo_),getResources().getString(R.string.choose_from_gallery),getResources().getString(R.string.exit)}; // create a menuOption Array
        // create a dialog for showing the optionsMenu
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // set the items in builder
        builder.setItems(optionsMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(optionsMenu[i].equals(getResources().getString(R.string.take_photo_))){
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File imageFile = null;
                        try {
                            imageFile = createImageFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (imageFile != null) {
                            imageUrlPassport = FileProvider.getUriForFile(PreRegisterDocumentUploadActivity.this,
                                    "com.nefos.ccsmembersapp.fileprovider", imageFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUrlPassport);
                            startActivityForResult(takePictureIntent, 0);
                        }
                    }
                }
                else if(optionsMenu[i].equals(getResources().getString(R.string.choose_from_gallery))){
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickPhoto.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {getResources().getString(R.string.image_jpeg),getResources().getString(R.string.image_png)});
                    startActivityForResult(pickPhoto , 1);
                }
                else if (optionsMenu[i].equals(getResources().getString(R.string.exit))) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private void chooseImageYourSelf(Context context){
        final CharSequence[] optionsMenu = {getResources().getString(R.string.take_photo_),getResources().getString(R.string.choose_from_gallery),getResources().getString(R.string.exit)}; // create a menuOption Array
        // create a dialog for showing the optionsMenu
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // set the items in builder
        builder.setItems(optionsMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(optionsMenu[i].equals(getResources().getString(R.string.take_photo_))){
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File imageFile = null;
                        try {
                            imageFile = createImageFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (imageFile != null) {
                            imageUrlSelfie = FileProvider.getUriForFile(PreRegisterDocumentUploadActivity.this,
                                    "com.nefos.ccsmembersapp.fileprovider", imageFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUrlSelfie);
                            startActivityForResult(takePictureIntent, 2);
                        }
                    }
                }
                else if(optionsMenu[i].equals(getResources().getString(R.string.choose_from_gallery))){
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickPhoto.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {getResources().getString(R.string.image_jpeg),getResources().getString(R.string.image_png)});
                    startActivityForResult(pickPhoto , 3);
                }
                else if (optionsMenu[i].equals(getResources().getString(R.string.exit))) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private long getImageSize(Uri imageUri) {
        Cursor cursor = mContext.getContentResolver().query(imageUri, null, null, null, null);
        cursor.moveToFirst();
        @SuppressLint("Range") long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
        cursor.close();
        return size;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage,
                getResources().getString(R.string.title), null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        String path = "";
        if (getContentResolver() != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }

    private Bitmap uriToBitmap(Uri uri) throws IOException {
        return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK) {
                        if (imageUrlPassport != null) {
                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUrlPassport);
                                long imageSize = getImageSize(imageUrlPassport);
                                if (imageSize <= MAX_IMAGE_SIZE) {
                                    if (bitmap != null) {
                                        passportBitmap = bitmap;
//                                        ivPassport.setImageBitmap(bitmap);
                                        Glide.with(this).load(bitmap).placeholder(R.drawable.ic_profile_grey).into(ivPassport);
                                    }
                                } else {
                                    Toast.makeText(PreRegisterDocumentUploadActivity.this, getResources().getString(R.string.please_select_an_image_less_than_20_MB_in_size_), Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        imageUrlPassport = selectedImage;
                        long imageSize = getImageSize(selectedImage);
                        if (imageSize <= MAX_IMAGE_SIZE) {
                            try {
                                passportBitmap = uriToBitmap(selectedImage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
//                            ivPassport.setImageBitmap(passportBitmap);
                            Glide.with(this).load(passportBitmap).placeholder(R.drawable.ic_profile_grey).into(ivPassport);
                        } else {
                            Toast.makeText(PreRegisterDocumentUploadActivity.this, getResources().getString(R.string.please_select_an_image_less_than_20_MB_in_size_), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case 2:
                    if (resultCode == RESULT_OK) {
                        if (imageUrlSelfie != null) {
                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUrlSelfie);
                                long imageSize = getImageSize(imageUrlSelfie);
                                if (imageSize <= MAX_IMAGE_SIZE) {
                                    if (bitmap != null) {
                                        selfieBitmap = bitmap;
//                                        ivYourself.setImageBitmap(bitmap);
                                        Glide.with(this).load(bitmap).placeholder(R.drawable.ic_profile_grey).into(ivYourself);
                                    }
                                } else {
                                    Toast.makeText(PreRegisterDocumentUploadActivity.this, getString(R.string.please_select_an_image_less_than_20_MB_in_size_), Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case 3:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        imageUrlSelfie = selectedImage;
                        long imageSize = getImageSize(selectedImage);
                        if (imageSize <= MAX_IMAGE_SIZE) {
                            try {
                                selfieBitmap = uriToBitmap(selectedImage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
//                            ivYourself.setImageBitmap(selfieBitmap);
                            Glide.with(this).load(selfieBitmap).placeholder(R.drawable.ic_profile_grey).into(ivYourself);
                        } else {
                            Toast.makeText(PreRegisterDocumentUploadActivity.this, getString(R.string.please_select_an_image_less_than_20_MB_in_size_), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
    }


    private String encodeImage(Bitmap bm)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();

        return "data:image/png;base64," + Base64.encodeToString(b, Base64.DEFAULT);
    }

    private void callDocumentApi() {
        btnSubmit.setEnabled(false);
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }

        String encodedImage;
        encodedImage = encodeImage(passportBitmap);

        apiClient = new ApiClient();
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.preRegisterPassportUpload(SharedPreference.getSelectedLanguage(PreRegisterDocumentUploadActivity.this),
                encodedImage, clubName, tempNumber);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());
                        int flag = jsonObject.getInt("flag");
                        if (flag == 1) {
                            callUploadSelfie();
                        } else {
                            btnSubmit.setEnabled(true);
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            String msg = jsonObject.getString("message");
                            toast(msg);
                        }
                    }
                } catch (JSONException e) {
                    btnSubmit.setEnabled(true);
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    e.printStackTrace();
                } catch (IOException e) {
                    btnSubmit.setEnabled(true);
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSubmit.setEnabled(true);
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void callUploadSelfie() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }

        String encodedImage;
        encodedImage = encodeImage(selfieBitmap);

        apiClient = new ApiClient();
        ApiInterface apiService = apiClient.getClient().create(ApiInterface.class);
        Call<ResponseBody> call = apiService.preRegisterSelfieUpload(SharedPreference.getSelectedLanguage(PreRegisterDocumentUploadActivity.this), encodedImage,
                clubName, tempNumber);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    JSONObject jsonObject;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());
                        int flag = jsonObject.getInt("flag");
                        if (flag == 1) {
                            btnSubmit.setEnabled(true);
                            Intent intent = new Intent(PreRegisterDocumentUploadActivity.this, PreRegisterPersonalDetailsActivity.class);
                            intent.putExtra("club_name", clubName);
                            intent.putExtra("club_code", clubCode);
                            intent.putExtra("orderTime", orderTime);
                            intent.putExtra("orderDateDB", orderDateDB);
                            intent.putExtra("tempNumber", tempNumber);
                            intent.putExtra("year", year);
                            intent.putExtra("month", month);
                            intent.putExtra("day", day);
                            startActivity(intent);
                        } else {
                            String msg = jsonObject.getString("message");
                            toast(msg);
                        }
                    }
                } catch (JSONException e) {
                    btnSubmit.setEnabled(true);
                    e.printStackTrace();
                } catch (IOException e) {
                    btnSubmit.setEnabled(true);
                    e.printStackTrace();
                }
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnSubmit.setEnabled(true);
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private boolean checkValidation() {
        if(imageUrlPassport == null || imageUrlPassport.getPath().isEmpty()) {
            toast(getResources().getString(R.string.please_upload_passport_image));
            return false;
        } else if(imageUrlSelfie == null || imageUrlSelfie.getPath().isEmpty()) {
            toast(getResources().getString(R.string.please_upload_profile_image));
            return false;
        }
        return true;
    }
}