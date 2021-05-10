package com.app.rakubaru.main;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.rakubaru.R;
import com.app.rakubaru.adapters.CustomInfoWindowAdapter;
import com.app.rakubaru.base.BaseActivity;
import com.app.rakubaru.classes.MapWrapperLayout;
import com.app.rakubaru.commons.Commons;
import com.app.rakubaru.commons.ReqConst;
import com.app.rakubaru.models.Area;
import com.app.rakubaru.models.RPoint;
import com.app.rakubaru.models.Subloc;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.PI;

public class RouteActivity extends BaseActivity implements OnMapReadyCallback, View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLng myLatLng = null;
    MapWrapperLayout mapWrapperLayout;
    LocationManager locationManager;
    Marker myMarker = null;
    TextView durationBox, speedBox, distanceBox;

    public static final int[] MAP_TYPES = {
            GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};

    SharedPreferences shref;
    SharedPreferences.Editor editor;

    AVLoadingIndicatorView progressBar;

    ArrayList<RPoint> points = new ArrayList<>(), pinPoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        checkPermissions(LOC_PER);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        durationBox = (TextView)findViewById(R.id.durationBox);
        speedBox = (TextView)findViewById(R.id.speedBox) ;
        distanceBox = (TextView)findViewById(R.id.distanceBox);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);
        mapFragment.getMapAsync(this);
        mapFragment.setRetainInstance(true);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, 39 + 20));

        mapFragment.setHasOptionsMenu(true);

        View zoomControls = mapFragment.getView().findViewById(0x1);
        if (zoomControls != null && zoomControls.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
            // ZoomControl is inside of RelativeLayout
            RelativeLayout.LayoutParams params_zoom = (RelativeLayout.LayoutParams) zoomControls.getLayoutParams();

            // Align it to - parent top|left
            params_zoom.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params_zoom.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            // Update margins, set to 10dp
            final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics());
            params_zoom.setMargins(margin, margin * 20, margin, margin);
        }

        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 200, 30, 30);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
//        isLocationEnabled();

        if(Commons.route.getAssign_id() > 0) {
            Log.d("Route Assign ID===>", String.valueOf(Commons.route.getAssign_id()));
            getRouteArea();
        }

    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void back(View view){
        onBackPressed();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void getPointInfo(Marker marker) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(getApplicationContext());

        try {
            addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            String zip = addresses.get(0).getPostalCode();
            String url= addresses.get(0).getUrl();

            String info = marker.getTitle() + "\n"
                    + "Address: " + address + "\n"
                    + "City: " + city + "\n"
                    + "State: " + state + "\n"
                    + "Postal code: " + postalCode + "\n"
                    + "Latitude: " + String.valueOf(marker.getPosition().latitude) + "\n"
                    + "Longitude: " + String.valueOf(marker.getPosition().longitude);
            new androidx.appcompat.app.AlertDialog.Builder(RouteActivity.this)
                    .setTitle("Location Info")
                    .setMessage(info)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).show();

        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        getPointInfo(marker);
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initListeners();
//        googleMap.setPadding(0, 0, 0, 500);
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    private void initListeners() {

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled( true );
        mMap.setBuildingsEnabled(true);
        mMap.setMapType(MAP_TYPES[Commons.curMapTypeIndex]);

        CameraPosition.builder()
                .target(new LatLng(Commons.points.get(0).getLat(),Commons.points.get(0).getLng()))
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(0)
                .build();
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(Commons.points.get(0).getLat(), Commons.points.get(0).getLng()), 16);
        mMap.animateCamera(update);

        LatLngBounds.Builder builder = LatLngBounds.builder();
        for(RPoint point:Commons.points){
            builder.include(new LatLng(point.getLat(), point.getLng()));
        }
        final LatLngBounds bounds = builder.build();
        // Animate camera to the bounds
        try {
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        new ShowPolyLine().execute();

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(Commons.points.get(0).getLat(), Commons.points.get(0).getLng()))
                .title(getString(R.string.start))
                .snippet("")
        ).showInfoWindow();

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(Commons.points.get(Commons.points.size() - 1).getLat(), Commons.points.get(Commons.points.size() - 1).getLng()))
                .title(getString(R.string.end)).snippet("")).showInfoWindow();

        CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(RouteActivity.this);
        mMap.setInfoWindowAdapter(adapter);

        for(RPoint pin : Commons.pins){
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(pin.getLat(), pin.getLng()))
                    .title(pin.getComment())
                    .snippet(pin.getTime().replace("AM", "午前").replace("PM", "午後"))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.targetmarker)));
            marker.showInfoWindow();
        }

        distanceBox.setText(df.format(Commons.route.getDistance()) + "km");
        durationBox.setText(getTimeStr(Commons.route.getDuration()));
        speedBox.setText(df.format(Commons.route.getSpeed()) + "km/h");

    }

    private String getTimeStr(long timeDiff){
        String timeStr = "";
        int seconds = (int) (timeDiff / 1000) % 60 ;
        int minutes = (int) ((timeDiff / (1000*60)) % 60);
        int hours   = (int) ((timeDiff / (1000*60*60)) % 24);
        timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return timeStr;
    }

    public void toggleMapType(View view){
        if(mMap != null){
            if(mMap.getMapType() == 1) {
                mMap.setMapType(MAP_TYPES[0]);
                ((ImageView)findViewById(R.id.mapTypeBtn)).setImageResource(R.drawable.map);
            }
            else {
                mMap.setMapType(MAP_TYPES[1]);
                ((ImageView)findViewById(R.id.mapTypeBtn)).setImageResource(R.drawable.satellite);
            }
        }
    }

    Polyline lastPolyline = null;
    long ii = 0;

    private class ShowPolyLine extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // show pollyline code
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<RPoint> rPoints = new ArrayList<>();
                        for(RPoint point:Commons.points){
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    rPoints.add(point);
                                    // Adding all the points in the route to LineOptions
                                    PolylineOptions lineOptions = new PolylineOptions();
                                    if(rPoints.size() > 1){
                                        LatLng lastLoc1 = new LatLng(rPoints.get(rPoints.size() - 2).getLat(), rPoints.get(rPoints.size() - 2).getLng());
                                        LatLng lastLoc2 = new LatLng(point.getLat(), point.getLng());
                                        if(rPoints.size() > 2){
                                            LatLng lastLoc0 = new LatLng(rPoints.get(rPoints.size() - 3).getLat(), rPoints.get(rPoints.size() - 3).getLng());

                                            double dist0 = getDistance(lastLoc0, lastLoc1);
                                            double dist1 = getDistance(lastLoc1, lastLoc2);
                                            double dist2 = getDistance(lastLoc0, lastLoc2);

                                            double angle = Math.acos((Math.pow(dist0, 2)  + Math.pow(dist1, 2) - Math.pow(dist2, 2)) / (2 * dist0 * dist1)) * 180 / PI;

                                            if(dist2 < dist0 || dist2 < dist1){
                                                if(lastPolyline != null) lastPolyline.remove();
                                                rPoints.remove(rPoints.get(rPoints.size() - 2));
                                                ii++;
                                                lineOptions.add(lastLoc0);
                                                lineOptions.add(lastLoc2);
                                            }else {
                                                lineOptions.add(lastLoc1);
                                                lineOptions.add(lastLoc2);
                                            }
                                        }else {
                                            lineOptions.add(lastLoc1);
                                            lineOptions.add(lastLoc2);
                                        }
                                        lineOptions.width(10);
                                        lineOptions.color(rPoints.get(rPoints.size() - 1).getColor().length() > 0 ? Color.parseColor(rPoints.get(rPoints.size() - 1).getColor()) : Color.RED);
                                        lastPolyline = mMap.addPolyline(lineOptions);
                                        if(rPoints.size() == Commons.points.size() - ii){
                                            ((LinearLayout)findViewById(R.id.loadingView)).setVisibility(View.GONE);
                                            ((View)findViewById(R.id.darkbg)).setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }, 1);
                        }
                    }
                });
            } catch (Exception e) {
                Log.d("exception", ".........................." + e);
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }

    }

    Timer timer = null;
    private int i = 0;
    ArrayList<RPoint> pnts = new ArrayList<>();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //TODO Update UI
            i++;
            if(i <= 3000){
                pnts.add(Commons.points.get(i - 1));
                // Adding all the points in the route to LineOptions
                PolylineOptions lineOptions = new PolylineOptions();
                if(pnts.size() > 1){
                    lineOptions.add(new LatLng(pnts.get(pnts.size() - 2).getLat(), pnts.get(pnts.size() - 2).getLng()));
                    lineOptions.add(new LatLng(pnts.get(pnts.size() - 1).getLat(), pnts.get(pnts.size() - 1).getLng()));
                    lineOptions.width(10);
                    lineOptions.color(Color.RED);
                    mMap.addPolyline(lineOptions);
                }
            }else {
                stopTimer();
            }
        }
    };

    Handler handler = null;
//
    public void stopTimer() {
        if (timer != null) {
            handler.removeCallbacks(runnable);
            handler = null;
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    public void startTimer() {
        if (timer != null) timer.cancel();
        timer = new Timer();
        handler = new Handler();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        }, 0, 1);
    }

    private void getRouteArea(){
        AndroidNetworking.post(ReqConst.SERVER_URL + "routearea")
                .addBodyParameter("route_id", String.valueOf(Commons.route.getIdx()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("AREA RESPONSE!!!", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                JSONObject dataObj = response.getJSONObject("data");
                                JSONObject areaObj = dataObj.getJSONObject("area");

                                Area area = new Area();
                                area.setAreaName(areaObj.getString("area_name"));

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

                                drawArea(area.getCoords());

                                JSONArray sublocArr = dataObj.getJSONArray("sublocs");
                                ArrayList<Subloc> sublocs = new ArrayList<>();
                                for(int i=0; i<sublocArr.length(); i++) {
                                    JSONObject object = (JSONObject) sublocArr.get(i);
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

                                drawSubareas(sublocs);

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

    public void drawArea(ArrayList<LatLng> coords){
//        LatLngBounds.Builder builder = LatLngBounds.builder();
//        for(LatLng coord : coords) {
//            builder.include(coord);
//        }
//        final LatLngBounds bounds = builder.build();
//        try {
//            int width = getResources().getDisplayMetrics().widthPixels;
//            int height = getResources().getDisplayMetrics().heightPixels;
//            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 100));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        if(!coords.isEmpty()){
            PolygonOptions poly = new PolygonOptions();
            poly.strokeWidth(5f)
                    .strokeColor(Color.parseColor("#CCFF0000"))
                    .fillColor(Color.parseColor("#1FFF0000"));
            for (LatLng latLng : coords) {
                poly.addAll(Collections.singleton(latLng));
            }
            mMap.addPolygon(poly);
        }
    }

    public void drawSubareas(ArrayList<Subloc> sublocs) {
        LatLngBounds.Builder builder = LatLngBounds.builder();
        ArrayList<Polygon> polygons = new ArrayList<>();
        for(Subloc subloc: sublocs) {
            PolygonOptions poly = new PolygonOptions();
            poly.strokeWidth(5f)
                    .strokeColor(Color.parseColor("#CC" + subloc.getColor().replace("#","")))
                    .fillColor(Color.parseColor("#1F" + subloc.getColor().replace("#","")));
            for (LatLng latLng : subloc.getCoordnates()) {
                poly.addAll(Collections.singleton(latLng));
                builder.include(latLng);
            }
            Polygon polygon = mMap.addPolygon(poly);
            polygons.add(polygon);
        }

//        final LatLngBounds bounds = builder.build();
//        try {
//            int width = getResources().getDisplayMetrics().widthPixels;
//            int height = getResources().getDisplayMetrics().heightPixels;
//            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 100));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

}










































