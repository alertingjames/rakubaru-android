package com.app.rakubaru.base;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.algorithmia.TypeToken;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.BuildConfig;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.rakubaru.R;
import com.app.rakubaru.commons.Commons;
import com.app.rakubaru.commons.ReqConst;
import com.app.rakubaru.models.RPoint;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.iamhabib.easy_preference.EasyPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class BaseActivity extends AppCompatActivity {

    public static DecimalFormat df = new DecimalFormat("0.00");

    @SuppressLint("InlinedApi")
    public static final String[] LOC_PER = {
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.FOREGROUND_SERVICE,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.READ_PHONE_STATE,
    };
    public static final String[] CAM_PER = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
//            android.Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        if(Commons.thisUser != null) {
            checkDevice();
        }
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void saveLocationPoints(ArrayList<RPoint> list, String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public ArrayList<RPoint> getLocationPoints(String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<RPoint>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void clearLocationPoints(String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, "");
        editor.apply();     // This line is IMPORTANT !!!
    }

    public static String getFormattedStr(long number){
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        return formatter.format(number);
    }

    public void showToast(String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = BaseActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.toast_view, null);
                TextView textView = (TextView) dialogView.findViewById(R.id.text);
                textView.setText(content);
                Toast toast = new Toast(BaseActivity.this);
                toast.setView(dialogView);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public void logOut() {
        showToast2(getString(R.string.logging_out));
        Commons.thisUser = null;
        exit();
    }

    private void exit() {
        EasyPreference.with(getApplicationContext(), "user_info").clearAll().save();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        } else{
            finish();
            System.exit( 0 );
        }
    }

    public void showToast2(String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LayoutInflater inflater = BaseActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.toast_view, null);
                TextView textView = (TextView) dialogView.findViewById(R.id.text);
                textView.setBackgroundResource(R.drawable.toast_green_background);
                textView.setText(content);
                Toast toast = new Toast(BaseActivity.this);
                toast.setView(dialogView);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static boolean isValidCellPhone(String number) {
        return android.util.Patterns.PHONE.matcher(number).matches();
    }

    public void setupUI(View view, Activity activity) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    try {
                        hideSoftKeyboard(activity);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView, activity);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void checkPermissions(String[] PERMISSIONS) {
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if (hasPermissions(this, PERMISSIONS)) {

        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 101);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void showAlertDialogForQuestion(String title, String content, Activity activity, Callable<Void> nofunc, Callable<Void> yesfunc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_alert_question, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        TextView titleBox = (TextView) view.findViewById(R.id.title);
        titleBox.setText(title);
        TextView contentBox = (TextView) view.findViewById(R.id.content);
        contentBox.setText(content);
        TextView noButton = (TextView) view.findViewById(R.id.no_button);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    nofunc.call();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        TextView yesButton = (TextView) view.findViewById(R.id.yes_button);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    yesfunc.call();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        ImageView cancelButton = (ImageView) view.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Get screen width and height in pixels
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // The absolute width of the available display size in pixels.
        int displayWidth = displayMetrics.widthPixels;
        // The absolute height of the available display size in pixels.
        int displayHeight = displayMetrics.heightPixels;

        // Initialize a new window manager layout parameters
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        // Copy the alert dialog window attributes to new layout parameter instance
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        // Set alert dialog width equal to screen width 80%
        int dialogWindowWidth = (int) (displayWidth * 0.8f);
        // Set alert dialog height equal to screen height 80%
        //    int dialogWindowHeight = (int) (displayHeight * 0.8f);

        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = dialogWindowWidth;
        //      layoutParams.height = dialogWindowHeight;

        // Apply the newly created layout parameters to the alert dialog window
        dialog.getWindow().setAttributes(layoutParams);
        dialog.setCancelable(false);
    }

    public void showAlertDialog(String title, String content, Activity activity, Callable<Void> func) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_alert_dialog, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        TextView titleBox = (TextView) view.findViewById(R.id.title);
        titleBox.setText(title);
        TextView contentBox = (TextView) view.findViewById(R.id.content);
        contentBox.setText(content);
        TextView okButton = (TextView) view.findViewById(R.id.btn_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    func.call();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        ImageView cancelButton = (ImageView) view.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Get screen width and height in pixels
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // The absolute width of the available display size in pixels.
        int displayWidth = displayMetrics.widthPixels;
        // The absolute height of the available display size in pixels.
        int displayHeight = displayMetrics.heightPixels;

        // Initialize a new window manager layout parameters
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        // Copy the alert dialog window attributes to new layout parameter instance
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        // Set alert dialog width equal to screen width 80%
        int dialogWindowWidth = (int) (displayWidth * 0.8f);
        // Set alert dialog height equal to screen height 80%
        //    int dialogWindowHeight = (int) (displayHeight * 0.8f);

        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = dialogWindowWidth;
        //      layoutParams.height = dialogWindowHeight;

        // Apply the newly created layout parameters to the alert dialog window
        dialog.getWindow().setAttributes(layoutParams);
        dialog.setCancelable(false);
    }


    public String calculateFileSize(String filepath) {
        //String filepathstr=filepath.toString();
        File file = new File(filepath);

        long fileSizeInBytes = file.length();
        float fileSizeInKB = fileSizeInBytes / 1024;
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        float fileSizeInMB = fileSizeInKB / 1024;

        String calString = Float.toString(fileSizeInMB);
        return calString;
    }


    public File cropResized(String imageFilePath, int width, int height) {
        Bitmap croppedBitmap = null;
        int x = 0, y = 0;
        if (width > height) {
            x = (width - height) / 2;
            width = height;
        } else if (width < height) {
            y = (height - width) / 2;
            height = width;
        }
        Rect rect = new Rect(x, y, x + width, y + height);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            BitmapRegionDecoder decoder = null;
            try {
                decoder = BitmapRegionDecoder.newInstance(imageFilePath, true);
                croppedBitmap = decoder.decodeRegion(rect, null);
                decoder.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Bitmap bitmapOriginal = BitmapFactory.decodeFile(imageFilePath, null);
            croppedBitmap = Bitmap.createBitmap(bitmapOriginal, rect.left, rect.top, rect.width(), rect.height());
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        assert croppedBitmap != null;
        croppedBitmap.compress(Bitmap.CompressFormat.PNG, 70, byteArrayOutputStream);

//        Log.d("Cropped dimensions!!!", String.valueOf(croppedBitmap.getWidth()) + " / " + String.valueOf(croppedBitmap.getHeight()));

        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures";

        File storageDir = new File(path);
        if (!storageDir.exists() && !storageDir.mkdir()) {
            return null;
        }

        FileOutputStream outStream = null;
        File file = new File(path,
                System.currentTimeMillis() + ".jpg");
        try {
            file.createNewFile();
            outStream = new FileOutputStream(file);
            outStream.write(byteArrayOutputStream.toByteArray());
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /////////////////

//        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
//        Log.e("Resized dimensions!!!", bitmap.getWidth() + "-" + bitmap.getHeight());
//        Log.e("Cropped file size!!!", calculateFileSize(file.getPath()));

        //////////////////////

        return file;
    }

    public androidx.appcompat.app.AlertDialog dialog = null;

    public void showAlertDialogForUploading(String content1, String content2, Activity activity) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_dialog_loading, null);
        builder.setView(view);
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        TextView contentBox1 = (TextView) view.findViewById(R.id.content1);
        contentBox1.setText(content1);
        TextView contentBox2 = (TextView) view.findViewById(R.id.content2);
        contentBox2.setText(content2);
        // Get screen width and height in pixels
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // The absolute width of the available display size in pixels.
        int displayWidth = displayMetrics.widthPixels;
        // The absolute height of the available display size in pixels.
        int displayHeight = displayMetrics.heightPixels;

        // Initialize a new window manager layout parameters
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        // Copy the alert dialog window attributes to new layout parameter instance
        layoutParams.copyFrom(dialog.getWindow().getAttributes());

        // Set alert dialog width equal to screen width 80%
        int dialogWindowWidth = (int) (displayWidth * 0.8f);
        // Set alert dialog height equal to screen height 80%
        //    int dialogWindowHeight = (int) (displayHeight * 0.8f);

        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = dialogWindowWidth;
        //      layoutParams.height = dialogWindowHeight;

        // Apply the newly created layout parameters to the alert dialog window
        dialog.getWindow().setAttributes(layoutParams);
        dialog.setCancelable(false);
    }

    public static String getDeviceId(Context context) {
        String deviceId;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } else {
            final TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephony.getDeviceId() != null) {
                deviceId = mTelephony.getDeviceId();
            } else {
                deviceId = Settings.Secure.getString(
                        context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
            }
        }
        return deviceId;
    }

    public double getDistance(LatLng latLng1, LatLng latLng2) {
        try{
            // lat1 and lng1 are the values of a previously stored location
            return distance(latLng1, latLng2);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return 0.0d;
    }

    /** calculates the distance between two locations in MILES */
    private double distance(LatLng latLng1, LatLng latLng2) {

        double lat1,lng1,lat2,lng2;
        lat1 = latLng1.latitude;
        lng1 = latLng1.longitude;
        lat2 = latLng2.latitude;
        lng2 = latLng2.longitude;

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist; // output distance, in MILES
    }

    public void checkDevice(){
        AndroidNetworking.post(ReqConst.SERVER_URL + "checkdevice")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getIdx()))
                .addBodyParameter("device", getDeviceId(BaseActivity.this))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("My Device Status!!!", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                String devStatus = response.getString("status");
                                if(devStatus.equals("out"))logOut();
                            }else if(result.equals("1")){
                                logOut();
                            }else if(result.equals("100")){
                                showToast(getString(R.string.admin_payment_introuble));
                                logOut();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                    }
                });
    }

    public static void clearSharedPreference(Context context, String preferenceFileName, String serializedObjectKey) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString(serializedObjectKey, "");
        sharedPreferencesEditor.apply();
    }


    public void saveTraceToSharedPreference(Context context, String preferenceFileName, String serializedObjectKey, ArrayList<RPoint> list){
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        final Gson gson = new Gson();
        String serializedObject = gson.toJson(list);
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject);
        sharedPreferencesEditor.apply();
    }

    public ArrayList<RPoint> getSavedTraceFromPreference(Context context, String preferenceFileName, String preferenceKey) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceFileName, 0);
        if (sharedPreferences.contains(preferenceKey)) {
            final Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<RPoint>>() {}.getType();
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), type);
        }
        return null;
    }

    public void openSettings(){
        String myVersion = android.os.Build.VERSION.RELEASE;
        Log.i("OS version", myVersion);
        if(Integer.parseInt(myVersion.split("\\.")[0]) > 10){
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(
                    "package",
                    BuildConfig.APPLICATION_ID,
                    null
            );
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }


}










































