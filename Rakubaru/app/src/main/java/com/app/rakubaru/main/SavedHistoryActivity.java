package com.app.rakubaru.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.rakubaru.R;
import com.app.rakubaru.adapters.RouteListAdapter;
import com.app.rakubaru.base.BaseActivity;
import com.app.rakubaru.commons.Commons;
import com.app.rakubaru.commons.ReqConst;
import com.app.rakubaru.models.RPoint;
import com.app.rakubaru.models.Route;
import com.app.rakubaru.utils.SharedPreferenceUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class SavedHistoryActivity extends BaseActivity {

    public AVLoadingIndicatorView progressBar;
    ListView list;

    ImageView searchButton, cancelButton;
    public LinearLayout searchBar;
    EditText ui_edtsearch;
    TextView title;

    public ArrayList<Route> routes = new ArrayList<>();
    RouteListAdapter adapter = new RouteListAdapter(this);

    SharedPreferences shref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_history);

        shref = getSharedPreferences("ROUTE", Context.MODE_PRIVATE);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);
        list = (ListView) findViewById(R.id.list);

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

        getMySavedRoutes();

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

    public void showAlertDialogForReportButtons(Route route){
        AlertDialog.Builder builder = new AlertDialog.Builder(SavedHistoryActivity.this);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_alert_buttons, null);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();

        TextView reportButton = (TextView)view.findViewById(R.id.reportButton);
        TextView deleteButton = (TextView)view.findViewById(R.id.deleteButton);

        if(route.getStatus().length() > 0)reportButton.setVisibility(View.GONE);

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportRoute(route);

                dialog.dismiss();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialogForQuestion(getString(R.string.warning), getString(R.string.sure_delete_data), SavedHistoryActivity.this, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        return null;
                    }
                }, new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        deleteRoute(route);
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

    private void deleteRoute(Route route){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "delroute")
                .addBodyParameter("route_id", String.valueOf(route.getIdx()))
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
                                showToast2(getString(R.string.deleted));
                                routes.remove(route);
                                adapter.setDatas(routes);
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
                        Log.d("ERROR!!!", error.getErrorBody());
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void getMySavedRoutes(){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "getmyroutes")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getIdx()))
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
                                routes.clear();
                                JSONArray dataArr = response.getJSONArray("data");
                                for(int i=0; i<dataArr.length(); i++) {
                                    JSONObject object = (JSONObject) dataArr.get(i);
                                    Route route = new Route();
                                    route.setIdx(object.getInt("id"));
                                    route.setAssign_id(Integer.parseInt(object.getString("assign_id")));
                                    route.setMember_id(Integer.parseInt(object.getString("member_id")));
                                    route.setName(object.getString("name"));
                                    route.setDescription(object.getString("description"));
                                    route.setStart_time(object.getString("start_time"));
                                    route.setEnd_time(object.getString("end_time"));
                                    route.setDuration(Long.parseLong(object.getString("duration")));
                                    route.setSpeed(Double.parseDouble(object.getString("speed")));
                                    route.setDistance(Double.parseDouble(object.getString("distance")));
                                    route.setStatus(object.getString("status"));
                                    route.setArea_name(object.getString("area_name"));
                                    route.setAssign_title(object.getString("assign_title"));

                                    if(object.getString("status2").length() == 0)
                                        routes.add(route);
                                }

                                if(routes.isEmpty()){
                                    ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.VISIBLE);
                                }
                                else {
                                    ((FrameLayout)findViewById(R.id.no_result)).setVisibility(View.GONE);
                                }

                                adapter.setDatas(routes);
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
                        Log.d("ERROR!!!", error.getErrorBody());
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void reportRoute(Route route){
        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.post(ReqConst.SERVER_URL + "reportroute")
                .addBodyParameter("route_id", String.valueOf(route.getIdx()))
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
                                route.setStatus("reported");
                                showToast2(getString(R.string.successfully_sent));
                                adapter.setDatas(routes);
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
                        Log.d("ERROR!!!", error.getErrorBody());
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }


    ProgressDialog pdDialog;

    public void getRouteDetails(Route route){
        pdDialog = new ProgressDialog(SavedHistoryActivity.this);
        pdDialog.setTitle(getString(R.string.please_wait));
        pdDialog.setMessage(getString(R.string.processing_data));
        pdDialog.show();
        pdDialog.setCancelable(false);
        AndroidNetworking.post(ReqConst.SERVER_URL + "routedetails")
                .addBodyParameter("route_id", String.valueOf(route.getIdx()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        pdDialog.dismiss();
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                Commons.points.clear();
                                JSONArray dataArr = response.getJSONArray("points");
                                for(int i=0; i<dataArr.length(); i++) {
                                    JSONObject object = (JSONObject) dataArr.get(i);
                                    RPoint point = new RPoint();
                                    point.setIdx(object.getInt("id"));
                                    point.setLat(Double.parseDouble(object.getString("lat")));
                                    point.setLng(Double.parseDouble(object.getString("lng")));
                                    point.setComment(object.getString("comment"));
                                    point.setColor(object.getString("color"));
                                    point.setTime(object.getString("time"));
                                    point.setStatus(object.getString("status"));

                                    Commons.points.add(point);

                                }

                                Intent intent = new Intent(getApplicationContext(), RouteActivity.class);
                                startActivity(intent);

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
                        Log.d("ERROR!!!", error.getErrorBody());
                        pdDialog.dismiss();
                    }
                });
    }




}








































