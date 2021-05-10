package com.app.rakubaru.main;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.app.rakubaru.R;
import com.app.rakubaru.base.BaseActivity;
import com.app.rakubaru.commons.Commons;
import com.iamhabib.easy_preference.EasyPreference;

public class SettingsActivity extends BaseActivity {

    Switch myLocationSwitchButton, mapViewSwitchButton, reportSwitchButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        myLocationSwitchButton = (Switch)findViewById(R.id.locationSetting);
        mapViewSwitchButton = (Switch) findViewById(R.id.mapviewSetting);
        reportSwitchButton = (Switch) findViewById(R.id.reportSetting);

        if(Commons.curMapTypeIndex == 2)mapViewSwitchButton.setChecked(true);
        else mapViewSwitchButton.setChecked(false);

        if(Commons.mapCameraMoveF)myLocationSwitchButton.setChecked(true);
        else myLocationSwitchButton.setChecked(false);

        if(Commons.autoReport)reportSwitchButton.setChecked(true);
        else reportSwitchButton.setChecked(false);


        myLocationSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Commons.mapCameraMoveF = true;
                    Commons.homeActivity.moveToMyLocation();
                }else {
                    Commons.mapCameraMoveF = false;
                }
            }
        });
        mapViewSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Commons.curMapTypeIndex = 2;
                }else {
                    Commons.curMapTypeIndex = 1;
                }
                Commons.googleMap.setMapType(HomeActivity.MAP_TYPES[Commons.curMapTypeIndex]);
            }
        });

        reportSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    Commons.autoReport = true;
                    EasyPreference.with(getApplicationContext()).addBoolean("autoReport", true).save();
                }else {
                    Commons.autoReport = false;
                    EasyPreference.with(getApplicationContext()).addBoolean("autoReport", false).save();
                }
            }
        });

    }

    public void back(View view){
        onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}























