package com.app.rakubaru.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.rakubaru.R;
import com.app.rakubaru.base.BaseActivity;
import com.app.rakubaru.commons.Commons;
import com.app.rakubaru.commons.ReqConst;
import com.app.rakubaru.models.User;
import com.iamhabib.easy_preference.EasyPreference;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class LoginActivity extends BaseActivity {

    TextView loginBtn;
    EditText emailBox, passwordBox;
    AVLoadingIndicatorView progressBar;
    LinearLayout alertDialogBox;
    FrameLayout background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        loginBtn = (TextView)findViewById(R.id.loginBtn);
        emailBox = (EditText)findViewById(R.id.emailBox);
        passwordBox = (EditText)findViewById(R.id.passwordBox);

        alertDialogBox = (LinearLayout)findViewById(R.id.alertDialog);
        background = (FrameLayout)findViewById(R.id.background);

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
                if(passwordBox.getText().length() > 0 && editable.length() > 0 && isValidEmail(editable)){
                    loginBtn.setAlpha(1.0f);
                }else{
                    loginBtn.setAlpha(0.3f);
                }
            }
        });

        passwordBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(isValidEmail(emailBox.getText().toString()) && emailBox.getText().length() > 0 && editable.length() > 0){
                    loginBtn.setAlpha(1.0f);
                }else{
                    loginBtn.setAlpha(0.3f);
                }
            }
        });

        setupUI(findViewById(R.id.activity), this);

        boolean hint_read = EasyPreference.with(getApplicationContext(), "action_info").getBoolean("hint_read", false);
        if(!hint_read){
            showAlertDialogForHint();
        }else {
            checkPermissions(LOC_PER);
            checkPermissions(CAM_PER);
            String myVersion = android.os.Build.VERSION.RELEASE;
            Log.i("OS version", myVersion);
            if(Integer.parseInt(myVersion.split("\\.")[0]) > 10){
                if(ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("バックグラウンドロケーション許可")
                            .setMessage("バックグラウンドで位置情報を取得するには、位置情報のアクセス許可を「常に許可」に設定します。\n" +
                                    "また、ルートをファイルとして安全に保存するには、保存権限を「許可する」に設定します。")
                            .setPositiveButton("許可する", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    openSettings();
                                }
                            })
                            .setNegativeButton("キャンセル", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        }

        EasyPreference.with(getApplicationContext(), "backup").clearAll().save();

    }

    public void back(View view){
        onBackPressed();
    }

    public void toForgotPassword(View view){
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    public void login(View view){
        if(loginBtn.getAlpha() < 1.0f)return;
        if(!isValidEmail(emailBox.getText().toString())){
            emailBox.setError(getString(R.string.invalid_email));
            return;
        }

        String deviceID = getDeviceId(LoginActivity.this);
        Log.d("DeviceID", deviceID);

        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "login")
                .addBodyParameter("email", emailBox.getText().toString().trim())
                .addBodyParameter("password", passwordBox.getText().toString().trim())
                .addBodyParameter("device", deviceID)
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
                            if(result.equals("0")){
                                JSONObject object = response.getJSONObject("data");
                                User user = new User();
                                user.setAdminID(Integer.parseInt(object.getString("admin_id")));
                                user.setIdx(object.getInt("id"));
                                user.setName(object.getString("name"));
                                user.setEmail(object.getString("email"));
                                user.setPhone_number(object.getString("phone_number"));
                                user.setPassword(object.getString("password"));
                                user.setPicture_url(object.getString("picture_url"));
                                user.setRegistered_time(object.getString("registered_time"));
                                user.setRole(object.getString("role"));
                                user.setStatus(object.getString("status"));

                                Commons.thisUser = user;

                                EasyPreference.with(getApplicationContext(), "user_info")
                                        .addString("email", Commons.thisUser.getEmail())
                                        .addString("password", Commons.thisUser.getPassword())
                                        .save();

                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                                finish();

                            }else if(result.equals("1")){
                                Commons.thisUser = null;
                                showToast(getString(R.string.you_no_registered));
                                logOut();
                            }else if(result.equals("2")){
                                Commons.thisUser = null;
                                showToast(getString(R.string.incorrect_password));
                            }else if(result.equals("3")){
                                Commons.thisUser = null;
                                showToast(getString(R.string.already_loggedin));
                            }else if(result.equals("100")){
                                Commons.thisUser = null;
                                showToast(getString(R.string.admin_payment_introuble));
                            }else {
                                Commons.thisUser = null;
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
                    }
                });
    }

    public void showAlertDialogForHint(){
        alertDialogBox.setVisibility(View.VISIBLE);
        background.setVisibility(View.VISIBLE);
    }

    public void OK(View view){
        EasyPreference.with(getApplicationContext(), "action_info").addBoolean("hint_read", true).save();
        checkPermissions(LOC_PER);
        checkPermissions(CAM_PER);
        String myVersion = android.os.Build.VERSION.RELEASE;
        Log.i("OS version", myVersion);
        if(Integer.parseInt(myVersion.split("\\.")[0]) > 10){
            if(ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("バックグラウンドロケーション許可")
                        .setMessage("バックグラウンドで位置情報を取得するには、位置情報のアクセス許可を「常に許可」に設定します。\n" +
                                "また、ルートをファイルとして安全に保存するには、保存権限を「許可する」に設定します。")
                        .setPositiveButton("許可する", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                openSettings();
                            }
                        })
                        .setNegativeButton("キャンセル", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
        alertDialogBox.setVisibility(View.GONE);
        background.setVisibility(View.GONE);
    }

    public void openHelp(View view) {
        Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
        startActivity(intent);
    }

}



























