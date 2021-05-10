package com.app.rakubaru.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.rakubaru.R;
import com.app.rakubaru.adapters.AreaListAdapter;
import com.app.rakubaru.base.BaseActivity;
import com.app.rakubaru.commons.Commons;
import com.app.rakubaru.commons.ReqConst;
import com.app.rakubaru.models.Area;
import com.app.rakubaru.models.RPoint;
import com.app.rakubaru.models.Subloc;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;

public class AreasActivity extends BaseActivity {

    public AVLoadingIndicatorView progressBar;
    SwipeMenuListView list;

    ImageView searchButton, cancelButton;
    public LinearLayout searchBar;
    EditText ui_edtsearch;
    TextView title;

    public ArrayList<Area> areas = new ArrayList<>();
    AreaListAdapter adapter = new AreaListAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_areas);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);
        list = (SwipeMenuListView) findViewById(R.id.list);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_trash);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };
// set creator
        list.setMenuCreator(creator);

        list.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // open
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        list.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        setupUI(findViewById(R.id.activity), this);

        title = (TextView)findViewById(R.id.title);

        searchBar = (LinearLayout)findViewById(R.id.search_bar);
        searchButton = (ImageView)findViewById(R.id.searchButton);
        cancelButton = (ImageView)findViewById(R.id.cancelButton);

        ui_edtsearch = (EditText)findViewById(R.id.edt_search);
        ui_edtsearch.setFocusable(true);
        ui_edtsearch.requestFocus();

        ui_edtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = ui_edtsearch.getText().toString().trim().toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }
        });

        getMyAreas();

    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    public void back(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if(HomeActivity.isLocationRecording){
            finish();
        }else{
            Commons.homeActivity.reset();
            finish();
        }
    }

    public void search(View view){
        cancelButton.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.GONE);
        searchBar.setVisibility(View.VISIBLE);
        title.setVisibility(View.GONE);
    }

    public void cancelSearch(View view){
        cancelButton.setVisibility(View.GONE);
        searchButton.setVisibility(View.VISIBLE);
        searchBar.setVisibility(View.GONE);
        title.setVisibility(View.VISIBLE);
        ui_edtsearch.setText("");
    }

    private void getMyAreas(){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.get(ReqConst.SERVER_URL + "getAssignedAreas?member_id=" + String.valueOf(Commons.thisUser.getIdx()))
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("AREA RESPONSE!!!", response.toString());
                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                areas.clear();
                                JSONArray dataArr = response.getJSONArray("data");
                                for(int i=0; i<dataArr.length(); i++) {
                                    JSONObject object = (JSONObject) dataArr.get(i);
                                    JSONObject assignObj = object.getJSONObject("assign");
                                    JSONObject areaObj = object.getJSONObject("area");
                                    Area area = new Area();
                                    area.setIdx(assignObj.getInt("id"));
                                    area.setMemberId(Integer.parseInt(assignObj.getString("member_id")));
                                    area.setAreaName(areaObj.getString("area_name"));
                                    area.setTitle(assignObj.getString("title"));
                                    area.setDistribution(assignObj.getString("distribution"));
                                    area.setStartTime(Long.parseLong(assignObj.getString("start_time")));
                                    area.setEndTime(Long.parseLong(assignObj.getString("end_time")));
                                    area.setCopies(Integer.parseInt(assignObj.getString("copies")));
                                    area.setAmount(Float.parseFloat(assignObj.getString("amount")));
                                    area.setDistance(Float.parseFloat(assignObj.getString("distance")));
                                    area.setMyDistance(Float.parseFloat(areaObj.getString("client_dist")));
                                    area.setStatus(assignObj.getString("status"));

                                    String locarr = areaObj.getString("locarr");
                                    if(locarr.length() > 0) {
                                        JSONArray locJsonArr = new JSONArray(locarr);
                                        ArrayList<LatLng> coords = new ArrayList<>();
                                        for(int j=0; j<locJsonArr.length(); j++) {
                                            JSONObject locObj = locJsonArr.getJSONObject(j);
                                            LatLng latLng = new LatLng(Double.parseDouble(locObj.getString("lat")),
                                                    Double.parseDouble(locObj.getString("lng")));
                                            coords.add(latLng);
                                        }
                                        area.setCoords(coords);
                                    }

                                    areas.add(area);
                                }

                                if(areas.isEmpty()){
                                    ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                                }
                                else {
                                    ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.GONE);
                                }

                                adapter.setDatas(areas);
                                list.setAdapter(adapter);
                            }else {
                                showToast(getString(R.string.something_wrong));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("Loading Error===>", error.getErrorDetail());
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    ProgressDialog pdDialog;

    public void getAreaLocations(Area area){
        if(HomeActivity.isLocationRecording){
            finish();
            overridePendingTransition(0,0);
            return;
        }
        pdDialog = new ProgressDialog(AreasActivity.this);
        pdDialog.setTitle(getString(R.string.please_wait));
        pdDialog.setMessage(getString(R.string.processing_data));
        pdDialog.show();
        pdDialog.setCancelable(false);
        Log.d("Area ID===>", String.valueOf(area.getIdx()));
        AndroidNetworking.get(ReqConst.SERVER_URL + "getAreaSublocs?assign_id=" + String.valueOf(area.getIdx()))
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("SUBLOCS RESPONSE!!!", response.toString());
                        pdDialog.dismiss();
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                ArrayList<Subloc> sublocs = new ArrayList<>();
                                JSONArray dataArr = response.getJSONArray("data");
                                for(int i=0; i<dataArr.length(); i++) {
                                    JSONObject object = (JSONObject) dataArr.get(i);
                                    Subloc subloc = new Subloc();
                                    subloc.setIdx(object.getInt("id"));
                                    subloc.setLatLng(new LatLng(Double.parseDouble(object.getString("lat")), Double.parseDouble(object.getString("lng"))));
                                    subloc.setLocationName(object.getString("loc_name"));
                                    subloc.setColor(object.getString("color"));
                                    JSONArray coordArrayJson = new JSONArray(object.getString("locarr"));
                                    ArrayList<LatLng> coords = new ArrayList<>();
                                    for(int j=0; j<coordArrayJson.length(); j++){
                                        JSONObject object1 = (JSONObject) coordArrayJson.get(j);
                                        double lat = object1.getDouble("lat");
                                        double lng = object1.getDouble("lng");
                                        LatLng latLng = new LatLng(lat, lng);
                                        coords.add(latLng);
                                    }
                                    subloc.setCoordnates(coords);

                                    sublocs.add(subloc);

                                }

                                Commons.homeActivity.clearOldDrawing();

                                Log.d("Area coords === > ", String.valueOf(area.getCoords().size()));

                                if(area.getCoords().size() > 0) {
                                    Commons.homeActivity.drawArea(area.getCoords());
                                    if(sublocs.size() > 0) {
                                        Commons.homeActivity.drawSubareas(sublocs);
                                    }
                                }else {
                                    if(sublocs.size() > 0) {
                                        Commons.homeActivity.drawSubareas(sublocs);
                                    }
                                    Commons.homeActivity.goToLocation(area.getAreaName());
                                }
                                Commons.homeActivity.showAreaDetails(area);
                                finish();
                                overridePendingTransition(0,0);

                            }else {
                                showToast(getString(R.string.something_wrong));
                                getMyAreas();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        Log.d("ERROR!!!", error.getErrorBody());
                        pdDialog.dismiss();
                    }
                });
    }

    public void showAlertDialogForAreaButtons(Area area){
        AlertDialog.Builder builder = new AlertDialog.Builder(AreasActivity.this);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_alert_buttons, null);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();

        TextView reportButton = (TextView)view.findViewById(R.id.reportButton);
        TextView deleteButton = (TextView)view.findViewById(R.id.deleteButton);

        reportButton.setText("編集");
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlertDialogForDistanceEdit(AreasActivity.this, area);
                dialog.dismiss();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialogForQuestion(getString(R.string.warning), getString(R.string.sure_delete_data), AreasActivity.this, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        return null;
                    }
                }, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        deleteAssign(area);
                        return null;
                    }
                });
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
        int dialogWindowWidth = (int) (displayWidth * 0.4f);
        // Set alert dialog height equal to screen height 80%
        //    int dialogWindowHeight = (int) (displayHeight * 0.8f);

        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = dialogWindowWidth;
        //      layoutParams.height = dialogWindowHeight;

        // Apply the newly created layout parameters to the alert dialog window
        dialog.getWindow().setAttributes(layoutParams);
    }

    private void deleteAssign(Area area){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.get(ReqConst.SERVER_URL + "removeAssign?assign_id=" + String.valueOf(area.getIdx()))
                .setPriority(Priority.LOW)
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
                                showToast2(getString(R.string.deleted));
                                areas.remove(area);
                                adapter.setDatas(areas);
                                list.setAdapter(adapter);
                            }else {
                                showToast(getString(R.string.something_wrong));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    public void showAlertDialogForDistanceEdit(Activity activity, Area area){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_distance_edit, null);
        builder.setView(view);
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        EditText distanceBox = (EditText) view.findViewById(R.id.distanceBox);
        distanceBox.setFocusable(true);
        distanceBox.requestFocus();
//        contentBox.setSelection(contentBox.getText().length());
        TextView submitButton = (TextView)view.findViewById(R.id.submitBtn);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(distanceBox.getText().length() == 0 || Integer.parseInt(distanceBox.getText().toString().trim()) == 0){
                    return;
                }
                AndroidNetworking.post(ReqConst.SERVER_URL + "submitDistance")
                        .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getIdx()))
                        .addBodyParameter("assign_id", String.valueOf(area.getIdx()))
                        .addBodyParameter("distance", String.valueOf(distanceBox.getText().toString().trim()))
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // do anything with response
                                Log.d("Distance sent!!!", response.toString());
                                try {
                                    String result = response.getString("result_code");
                                    if (result.equals("0")) {
                                        showToast2(getString(R.string.successfully_sent));
                                    } else {
                                        showToast(getString(R.string.something_wrong));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(ANError error) {
                                Log.d("ERROR!!!", error.getErrorBody());
                            }
                        });

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(),0);

                dialog.dismiss();
            }
        });

        ImageView cancelButton = (ImageView)view.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(),0);
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

}






















