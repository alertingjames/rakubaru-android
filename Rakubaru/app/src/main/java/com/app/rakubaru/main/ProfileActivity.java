package com.app.rakubaru.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.app.rakubaru.R;
import com.app.rakubaru.base.BaseActivity;
import com.app.rakubaru.commons.Commons;
import com.app.rakubaru.commons.ReqConst;
import com.app.rakubaru.models.User;
import com.bumptech.glide.Glide;
import com.iamhabib.easy_preference.EasyPreference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends BaseActivity {

    EditText nameBox, phoneBox;
    TextView emailBox, cumulative;
    FrameLayout pictureFrame;
    CircleImageView pictureBox;
    TextView saveButton;
    AVLoadingIndicatorView progressBar;
    File imageFile = null;
    ArrayList<File> files = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        checkPermissions(CAM_PER);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        pictureFrame = (FrameLayout)findViewById(R.id.pictureFrame);
        pictureBox = (CircleImageView)findViewById(R.id.pictureBox);

        nameBox = (EditText)findViewById(R.id.nameBox);
        emailBox = (TextView) findViewById(R.id.emailBox);
        phoneBox = (EditText)findViewById(R.id.phoneBox);

        cumulative = (TextView)findViewById(R.id.cumulative);

        saveButton = (TextView) findViewById(R.id.saveBtn);

        pictureFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(ProfileActivity.this);
            }
        });

        setupUI(findViewById(R.id.activity), this);

        if(Commons.thisUser != null){
            if(Commons.thisUser.getPicture_url().length() > 0){
                Glide.with(this).load(Commons.thisUser.getPicture_url()).into(pictureBox);
            }
            nameBox.setText(Commons.thisUser.getName());
            emailBox.setText(Commons.thisUser.getEmail());
            phoneBox.setText(Commons.thisUser.getPhone_number());
        }

        nameBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0){
                    saveButton.setAlpha(1.0f);
                }else{
                    saveButton.setAlpha(0.3f);
                }
            }
        });

        phoneBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0){
                    saveButton.setAlpha(1.0f);
                }else{
                    saveButton.setAlpha(0.3f);
                }
            }
        });

        getMyCumulativeDistance();

    }

    public void back(View view){
        onBackPressed();
    }

    public void toResetPassword(View view){
        Intent intent = new Intent(ProfileActivity.this, ResetPasswordActivity.class);
        startActivity(intent);
    }

    public void logout(View view){
        logOut();
    }

    public void saveProfile(View view){
        if(saveButton.getAlpha() < 1.0f)return;

        if(nameBox.getText().length() == 0){
            nameBox.setError(getString(R.string.enter_name));
            return;
        }

        if(phoneBox.getText().length() > 0 && !isValidCellPhone(phoneBox.getText().toString())){
            phoneBox.setError(getString(R.string.invalid_phone));
            return;
        }

        updateMember(nameBox.getText().toString().trim(), phoneBox.getText().toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                //From here you can load the image however you need to, I recommend using the Glide library
                imageFile = new File(resultUri.getPath());
                files.clear();
                files.add(imageFile);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    pictureBox.setImageBitmap(bitmap);
                    ((ImageView)findViewById(R.id.cameraButton)).setVisibility(View.GONE);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void updateMember(String name, String phone_number) {
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER_URL + "updatemember")
                .addMultipartFileList("files", files)
                .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.getIdx()))
                .addMultipartParameter("name", name)
                .addMultipartParameter("phone_number", phone_number)
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        // do anything with progress
                        Log.d("UPLOADED!!!", String.valueOf(bytesUploaded) + "/" + String.valueOf(totalBytes));
                    }
                })
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

                                showToast2(getString(R.string.profile_updated));

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
                        showToast(error.getErrorDetail());
                    }
                });

    }

    private void getMyCumulativeDistance() {
        AndroidNetworking.get(ReqConst.SERVER_URL + "getMyCumulativeDistance?member_id=" + String.valueOf(Commons.thisUser.getIdx()))
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("CUMULATIVE RESPONSE!!!", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                cumulative.setText(getString(R.string.cumulative_distance) + ": "
                                        + df.format(Double.parseDouble(response.getString("cumulative"))) + "km");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.d("ERROR===>", error.getErrorDetail());
                    }
                });
    }

}




































