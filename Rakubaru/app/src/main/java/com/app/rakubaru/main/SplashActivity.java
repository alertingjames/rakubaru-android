package com.app.rakubaru.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;

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
import com.app.rakubaru.service.ForegroundServiceInitialize;
import com.app.rakubaru.utils.SharedPreferenceUtil;
import com.google.android.gms.maps.model.LatLng;
import com.iamhabib.easy_preference.EasyPreference;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends BaseActivity {

    AVLoadingIndicatorView progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

//        checkPermissions(LOC_PER);

//        EasyPreference.with(getApplicationContext(), "user_info").clearAll().save();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                checkPermissions0(LOC_PER);
                String email = EasyPreference.with(getApplicationContext(), "user_info").getString("email", "");
                String password = EasyPreference.with(getApplicationContext(), "user_info").getString("password", "");
                Log.d("EMAIL!!!", email);
                if(email.length() > 0 && password.length() > 0){
                    login(email, password);
                }else {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(0,0);
                }
            }
        }, 1500);

    }

    private void login(String email, String password){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "login")
                .addBodyParameter("email", email)
                .addBodyParameter("password", password)
                .addBodyParameter("device", "")
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
                                user.setIdx(object.getInt("id"));
                                user.setName(object.getString("name"));
                                user.setEmail(object.getString("email"));
                                user.setPassword(object.getString("password"));
                                user.setStatus(object.getString("status"));

                                Commons.thisUser = user;

                                EasyPreference.with(getApplicationContext(), "user_info")
                                        .addString("email", Commons.thisUser.getEmail())
                                        .addString("password", Commons.thisUser.getPassword())
                                        .save();

                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                                finish();

                            }else {
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("ERROR!!!", error.getErrorBody());
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });

    }

}












