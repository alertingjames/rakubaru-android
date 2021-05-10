package com.app.rakubaru.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;

import com.app.rakubaru.BuildConfig;
import com.app.rakubaru.R;
import com.app.rakubaru.base.BaseActivity;
import com.app.rakubaru.commons.Commons;
import com.app.rakubaru.service.ForegroundOnlyLocationService;
import com.app.rakubaru.service.ForegroundServiceInitialize;
import com.app.rakubaru.utils.SharedPreferenceUtil;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static int i = 0;
    Button foregroundOnlyLocationButton;
    public TextView outTextView;

    ForegroundServiceInitialize initialize;
    ServiceConnection foregroundOnlyServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Commons.mainActivity = this;
//
//        initialize = new ForegroundServiceInitialize();
//        foregroundOnlyServiceConnection = initialize.getForegroundOnlyServiceConnection();
//
//        checkPermissions(LOC_PER);
//
//        initialize.initialize();
//
//        initialize.sharedPreferences =
//                getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
//        foregroundOnlyLocationButton  =(Button)findViewById(R.id.foreground_only_location_button) ;
//        outTextView = (TextView)findViewById(R.id.output_text_view);
//        foregroundOnlyLocationButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                boolean enabled = initialize.sharedPreferences.getBoolean(
//                        SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false);
//
//                if (enabled) {
//                    Objects.requireNonNull(initialize.getForegroundOnlyLocationService()).unsubscribeToLocationUpdates();
//                } else {
//                    // TODO: Step 1.0, Review Permissions: Checks and requests if needed.
//                    if (foregroundPermissionApproved()) {
//                        Objects.requireNonNull(initialize.getForegroundOnlyLocationService()).subscribeToLocationUpdates();
//                    } else {
//                        requestForegroundPermissions();
//                    }
//                }
//            }
//        });
//
//        initialize.sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
//            @Override
//            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//                if (key.equals(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED)) {
//                    updateButtonState(sharedPreferences.getBoolean(
//                            SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
//                    );
//                }
//            }
//        });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        updateButtonState(
//                initialize.sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
//        );
//        initialize.sharedPreferences.registerOnSharedPreferenceChangeListener(MainActivity.this);
//        Intent serviceIntent = new Intent(this, ForegroundOnlyLocationService.class);
//        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    private void updateButtonState(boolean trackingLocation) {
//        if (trackingLocation) {
//            foregroundOnlyLocationButton.setText(getString(R.string.stop_location_updates_button_text));
//        } else {
//            foregroundOnlyLocationButton.setText(getString(R.string.start_location_updates_button_text));
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        initialize.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        initialize.onPause();
//        super.onPause();
//    }
//
//    @Override
//    protected void onStop() {
//        if (initialize.getForegroundOnlyLocationServiceBound()) {
//            unbindService(foregroundOnlyServiceConnection);
//            initialize.setForegroundOnlyLocationServiceBound(false);
//        }
//        initialize.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
//        super.onStop();
//    }
//
//    private boolean foregroundPermissionApproved() {
//        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//        );
//    }
//
//    private static int REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34;
//
//    private void requestForegroundPermissions() {
//        if(foregroundPermissionApproved()){
//
//        }else{
//            ActivityCompat.requestPermissions(
//                    MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
//            );
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Objects.requireNonNull(initialize.getForegroundOnlyLocationService()).subscribeToLocationUpdates();
//            } else {
//                updateButtonState(false);
//                Intent intent = new Intent();
//                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                Uri uri = Uri.fromParts(
//                        "package",
//                        BuildConfig.APPLICATION_ID,
//                        null
//                );
//                intent.setData(uri);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            }
//        }
//    }
//
//    private void logResultsToScreen(String output) {
//
//    }
//
//
//    Timer timer = null;
//    Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            //TODO Update UI
//            i++;
//            Log.d("i+++", String.valueOf(i));
//        }
//    };
//
//    Handler handler = null;
//
//    public void stopTimer() {
//        if (timer != null) {
//            handler.removeCallbacks(runnable);
//            handler = null;
//            timer.cancel();
//            timer.purge();
//            timer = null;
//        }
//    }
//
//    public void startTimer() {
//        timer = new Timer();
//        handler = new Handler();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                handler.post(runnable);
//            }
//        }, 0, 500);
//    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        if (key.equals(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED)) {
//            updateButtonState(sharedPreferences.getBoolean(
//                    SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
//            );
//        }
    }
}


























