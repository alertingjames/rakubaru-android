package com.app.rakubaru.main;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import com.app.rakubaru.adapters.ReportListAdapter;
import com.app.rakubaru.base.BaseActivity;
import com.app.rakubaru.commons.Commons;
import com.app.rakubaru.commons.ReqConst;
import com.app.rakubaru.models.RPoint;
import com.app.rakubaru.models.Route;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

public class MyReportsActivity extends BaseActivity {

    public AVLoadingIndicatorView progressBar;
    ListView list;

    ImageView searchButton, cancelButton;
    public LinearLayout searchBar;
    EditText ui_edtsearch;
    TextView title;

    public ArrayList<Route> routes = new ArrayList<>();
    ReportListAdapter adapter = new ReportListAdapter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);

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

        getMyReports();

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

    private void getMyReports(){
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

                                    if(route.getStatus().equals("reported"))
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

    ProgressDialog pdDialog;

    public void getReportDetails(Route route){
        pdDialog = new ProgressDialog(MyReportsActivity.this);
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

























