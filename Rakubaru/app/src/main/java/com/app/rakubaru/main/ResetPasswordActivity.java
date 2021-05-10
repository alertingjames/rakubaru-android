package com.app.rakubaru.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

public class ResetPasswordActivity extends BaseActivity {

    EditText oldPasswordBox, newPasswordBox, confirmPasswordBox;
    TextView submitBtn;
    AVLoadingIndicatorView progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);
        oldPasswordBox = (EditText)findViewById(R.id.oldPasswordBox);
        newPasswordBox = (EditText)findViewById(R.id.newPasswordBox);
        confirmPasswordBox = (EditText)findViewById(R.id.confirmPasswordBox);

        oldPasswordBox.requestFocus();

        submitBtn = (TextView) findViewById(R.id.submitBtn);

        oldPasswordBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(newPasswordBox.getText().length() > 0 && editable.length() > 0 && confirmPasswordBox.getText().length() > 0){
                    if(editable.toString().equals(Commons.thisUser.getPassword())
                            && newPasswordBox.getText().toString().trim().equals(confirmPasswordBox.getText().toString().trim()))
                        submitBtn.setAlpha(1.0f);
                    else submitBtn.setAlpha(0.3f);
                }else{
                    submitBtn.setAlpha(0.3f);
                }
            }
        });

        newPasswordBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0 && oldPasswordBox.length() > 0 && confirmPasswordBox.getText().length() > 0){
                    if(oldPasswordBox.getText().toString().equals(Commons.thisUser.getPassword())
                            && editable.toString().trim().equals(confirmPasswordBox.getText().toString().trim()))
                        submitBtn.setAlpha(1.0f);
                    else submitBtn.setAlpha(0.3f);
                }else{
                    submitBtn.setAlpha(0.3f);
                }
            }
        });

        confirmPasswordBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(newPasswordBox.getText().length() > 0 && editable.length() > 0 && oldPasswordBox.getText().length() > 0){
                    if(oldPasswordBox.getText().toString().equals(Commons.thisUser.getPassword())
                            && newPasswordBox.getText().toString().trim().equals(editable.toString().trim())){
                        submitBtn.setAlpha(1.0f);
                    }
                    else submitBtn.setAlpha(0.3f);
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

    public void updatePassword(View view){
        if(submitBtn.getAlpha() < 1.0f)return;

        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "passwordupdate")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getIdx()))
                .addBodyParameter("password", newPasswordBox.getText().toString().trim())
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
                                user.setPassword(object.getString("password"));
                                user.setPhone_number(object.getString("phone_number"));
                                user.setPicture_url(object.getString("picture_url"));
                                user.setRegistered_time(object.getString("registered_time"));
                                user.setRole(object.getString("role"));
                                user.setStatus(object.getString("status"));

                                Commons.thisUser = user;

                                EasyPreference.with(getApplicationContext(), "user_info")
                                        .addString("email", Commons.thisUser.getEmail())
                                        .addString("password", Commons.thisUser.getPassword())
                                        .save();

                                showToast2(getString(R.string.successfully_updated));
                                finish();

                            }else if(result.equals("1")){
                                Commons.thisUser = null;
                                showToast(getString(R.string.you_no_registered));
                                logOut();
                            }else{
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

}


























