package com.nefos.ccsmembersapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.nefos.ccsmembersapp.R;

import java.io.ByteArrayOutputStream;

public class SignatureActivity extends AppCompatActivity {

    private SignaturePad mSignaturePad;
    private Button mClearButton;
    private Button mSaveButton;
    private boolean isSignaturePad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        mSignaturePad = (SignaturePad) findViewById(R.id.signaturePad);
        mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {

            }

            @Override
            public void onSigned() {
                isSignaturePad = true;
            }

            @Override
            public void onClear() {
                isSignaturePad = false;
            }
        });

        mClearButton = (Button) findViewById(R.id.clearButton);
        mSaveButton = (Button) findViewById(R.id.saveButton);

        mClearButton.setOnClickListener(view -> mSignaturePad.clear());

        mSaveButton.setOnClickListener(view -> {
            if(isSignaturePad) {
                Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", BitMapToString(signatureBitmap));
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            } else {
                Toast.makeText(SignatureActivity.this, getResources().getString(R.string.please_add_signature), Toast.LENGTH_SHORT).show();
            }

        });

    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream bAos = new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, bAos);
        byte [] b = bAos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

}