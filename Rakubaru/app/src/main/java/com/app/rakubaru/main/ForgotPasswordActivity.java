package com.app.rakubaru.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.rakubaru.R;
import com.app.rakubaru.base.BaseActivity;
import com.app.rakubaru.commons.ReqConst;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ForgotPasswordActivity extends BaseActivity {

    TextView submitBtn;
    EditText emailBox;
    AVLoadingIndicatorView progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        emailBox = (EditText)findViewById(R.id.emailBox);
        submitBtn = (TextView)findViewById(R.id.submitBtn);

        emailBox.setFocusable(true);
        emailBox.requestFocus();
//        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        emailBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0){
                    submitBtn.setAlpha(1.0f);
                }else{
                    submitBtn.setAlpha(0.3f);
                }
            }
        });

        setupUI(findViewById(R.id.activity), this);

    }

    public void back(View view){
        onBackPressed();
    }

    public void submit(View view){
        if(submitBtn.getAlpha() < 1.0f)return;
        if(!isValidEmail(emailBox.getText().toString())){
            emailBox.setError("Invalid email");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "forgotpassword")
                .addBodyParameter("email", emailBox.getText().toString().trim())
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if (result.equals("0")) {
                                showToast2(getString(R.string.we_sent_password_reset_link));
                                openMail();
                            } else if(result.equals("1")){
                                showToast(getString(R.string.you_no_registered));
                            } else {
                                showToast(getString(R.string.something_wrong));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("ERROR!!!", error.getErrorBody());
                        progressBar.setVisibility(View.GONE);
                        showToast(error.getErrorDetail());
                    }
                });

    }

    public void openMail(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
        startActivity(intent);
    }

}



















