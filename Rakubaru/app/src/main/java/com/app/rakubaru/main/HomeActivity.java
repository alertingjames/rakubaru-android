package com.app.rakubaru.main;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.app.rakubaru.BuildConfig;
import com.app.rakubaru.R;
import com.app.rakubaru.adapters.CustomInfoWindowAdapter;
import com.app.rakubaru.base.BaseActivity;
import com.app.rakubaru.classes.JsonFile;
import com.app.rakubaru.classes.MapWrapperLayout;
import com.app.rakubaru.classes.TestDirectionData;
import com.app.rakubaru.commons.Commons;
import com.app.rakubaru.commons.Constants;
import com.app.rakubaru.commons.ReqConst;
import com.app.rakubaru.models.Area;
import com.app.rakubaru.models.RPoint;
import com.app.rakubaru.models.Route;
import com.app.rakubaru.models.Subloc;
import com.app.rakubaru.service.ForegroundOnlyLocationService;
import com.app.rakubaru.service.ForegroundServiceInitialize;
import com.app.rakubaru.utils.SharedPreferenceUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.iamhabib.easy_preference.EasyPreference;
import com.skydoves.powermenu.MenuAnimation;
import com.skydoves.powermenu.OnMenuItemClickListener;
import com.skydoves.powermenu.PowerMenu;
import com.skydoves.powermenu.PowerMenuItem;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.PI;

public class HomeActivity extends BaseActivity implements OnMapReadyCallback, View.OnClickListener,  SharedPreferences.OnSharedPreferenceChangeListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public LatLng myLatLng = null;
    MapWrapperLayout mapWrapperLayout;
    String info = "", city = "", country = "", address = "";
    LocationManager locationManager;
    Marker myMarker = null;
    LatLng lastLatlng = null;

    Button saveBtn, recordBtn;
    LinearLayout panel;
    TextView durationBox, speedBox, distanceBox;
    PowerMenu powerMenu = null;
    AVLoadingIndicatorView progressBar;

    public static final int[] MAP_TYPES = {
            GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};

    public static long routeID = 0;
    public static long assignID = 0;
    public static boolean isLocationRecording = false;
    public static double totalDistance = 0.0d;
    public double speed = 0.0d;
    public double mSpeed = 0;
    public static long duration = 0;
    public static long startedTime = 0;
    public static long endedTime = 0;
    public static ArrayList<Polyline> polylines = new ArrayList<>();

    public static ArrayList<RPoint> traces0 = new ArrayList<>();
    public static ArrayList<RPoint> traces1 = new ArrayList<>();
    public static ArrayList<RPoint> traces = new ArrayList<>();
    public static boolean isDataLoading = false;

    SharedPreferences shref;
    SharedPreferences.Editor editor;

    ArrayList<File> gFiles = new ArrayList<>();

    OnMenuItemClickListener<PowerMenuItem> onMenuItemClickListener = new OnMenuItemClickListener<PowerMenuItem>() {
        @Override
        public void onItemClick(int position, PowerMenuItem item) {
            switch (position){
                case 0:
                    Intent intent = new Intent(getApplicationContext(), AreasActivity.class);
                    startActivity(intent);
                    break;
//                case 1:
//                    intent = new Intent(getApplicationContext(), SavedHistoryActivity.class);
//                    startActivity(intent);
//                    break;
                case 1:
                    intent = new Intent(getApplicationContext(), MyReportsActivity.class);
                    startActivity(intent);
                    break;
                case 2:
                    intent = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(intent);
                    break;
            }
            powerMenu.setSelectedPosition(position); // change selected item
            powerMenu.dismiss();
        }
    };

    ForegroundServiceInitialize initialize;
    ServiceConnection foregroundOnlyServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Commons.homeActivity = this;

        Commons.autoReport = EasyPreference.with(getApplicationContext()).getBoolean("autoReport", true);

        initialize = new ForegroundServiceInitialize();
        foregroundOnlyServiceConnection = initialize.getForegroundOnlyServiceConnection();

        initialize.initialize();
        initialize.sharedPreferences =
                getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        initialize.sharedPreferences.edit().apply();

        initialize.sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED)) {
                    updateButtonState(sharedPreferences.getBoolean(
                            SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
                    );
                }
            }
        });

        isLocationRecording = false;
        totalDistance = 0.0d;
        speed = 0.0d;
        duration = 0;
        startedTime = 0;
        endedTime = 0;

        clearPolylines();

        shref = getSharedPreferences("ROUTE", Context.MODE_PRIVATE);

        progressBar = (AVLoadingIndicatorView)findViewById(R.id.loading_bar);

        saveBtn = (Button)findViewById(R.id.saveBtn);
        recordBtn = (Button)findViewById(R.id.recordBtn);
        panel = (LinearLayout)findViewById(R.id.panel);
        durationBox = (TextView)findViewById(R.id.durationBox);
        speedBox = (TextView)findViewById(R.id.speedBox) ;
        distanceBox = (TextView)findViewById(R.id.distanceBox);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);
        mapFragment.getMapAsync(this);
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
        isLocationEnabled();

        isLocationRecording = EasyPreference.with(getApplicationContext(), "backup").getBoolean("isLocationRecording", false);
        if(isLocationRecording){
            restore();
        }else {
            EasyPreference.with(getApplicationContext(), "backup").clearAll().save();
//            clearSharedPreference(HomeActivity.this, "TRACE", "traces");
        }

        checkPermissions(CAM_PER);

    }

    private void restore(){
        routeID = EasyPreference.with(getApplicationContext(), "backup").getLong("route_id", 0);
        assignID = EasyPreference.with(getApplicationContext(), "backup").getLong("assign_id", 0);
        totalDistance = EasyPreference.with(getApplicationContext(), "backup").getFloat("totalDistance", 0);
        speed = EasyPreference.with(getApplicationContext(), "backup").getFloat("speed", 0);
        duration = EasyPreference.with(getApplicationContext(), "backup").getLong("duration", 0);
        startedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("startedTime", 0);
        endedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("endedTime", 0);

        float last_lat = EasyPreference.with(getApplicationContext(), "backup").getFloat("last_lat", 0);
        float last_lng = EasyPreference.with(getApplicationContext(), "backup").getFloat("last_lng", 0);
        lastLatlng = new LatLng((double) last_lat, (double) last_lng);

//        Objects.requireNonNull(initialize.getForegroundOnlyLocationService()).subscribeToLocationUpdates();
        clearPolylines();
        recordBtn.setBackgroundResource(R.drawable.red_round_fill);
        recordBtn.setTextColor(Color.YELLOW);
        recordBtn.setText(getString(R.string.end));
        panel.setVisibility(View.VISIBLE);

        distanceBox.setText(df.format(totalDistance) + "km");
        duration = new Date().getTime() - startedTime;
        durationBox.setText(getTimeStr(duration));
        speed = (double) totalDistance * 3600/((new Date().getTime() - startedTime > 1000? new Date().getTime() - startedTime : 1000)*0.001);
        speedBox.setText(df.format(speed) + "km/h");

//        traces0 = getSavedTraceFromPreference(HomeActivity.this, "TRACE", "traces");
        ArrayList<RPoint> rpoints = getLocationPoints(getString(R.string.location_points));
        if(rpoints == null) traces1 = rpoints;

        ((LinearLayout)findViewById(R.id.loadingView)).setVisibility(View.VISIBLE);
        ((View)findViewById(R.id.darkbg)).setVisibility(View.VISIBLE);

        getRoute(routeID);
    }

    private void showKeyboard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialize.onResume();
        if(isLocationRecording) {
            distanceBox.setText(df.format(totalDistance) + "km");
            duration = new Date().getTime() - startedTime;
            durationBox.setText(getTimeStr(duration));
            speed = (double) totalDistance * 3600/((new Date().getTime() - startedTime > 1000? new Date().getTime() - startedTime : 1000)*0.001);
            speedBox.setText(df.format(speed) + "km/h");
        }

        checkDevice();
    }



    public void reset(){
        startedTime = new Date().getTime();
        clearPolylines();
        totalDistance = 0;
        duration = 0;
        panel.setVisibility(View.INVISIBLE);
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 7001;

    private void checkForLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);

        } else {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(location == null) location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location == null) location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (location != null) {
                try {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                    refreshMyMarker(latLng);
                    Log.d("MyLoc!", String.valueOf(latLng));
                    initCamera(latLng);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void moveToMyLocation(View view){
        if(myLatLng != null) {
            initCamera(myLatLng);
        }
    }

    public void moveToMyLocation(){
        if(myLatLng != null) {
            initCamera(myLatLng);
        }
    }

    public void refreshMyMarker(LatLng latLng) {
        myLatLng = latLng;
        if(Commons.mapCameraMoveF){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom));
        }
        if(isLocationRecording) drawRoute(myLatLng, false);
    }

    public LatLng getCenterCoordinate(LatLng latLng) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(latLng);
        LatLngBounds bounds = builder.build();
        return bounds.getCenter();
    }

    Circle circle = null;

    private void drawCircle(LatLng latLng) {
        if(circle == null){
            try{
                LatLng loc = getCenterCoordinate(latLng);
                double radius = Constants.RADIUS;
                CircleOptions options = new CircleOptions();
                if(loc != null) {
                    options.center(loc);
                    //Radius in meters
                    options.radius(radius);
                    options.fillColor(getResources()
                            .getColor(R.color.circle_fill_color));
                    options.strokeColor(getResources()
                            .getColor(R.color.circle_stroke_color));
                    options.strokeWidth(2);
                    circle = mMap.addCircle(options);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }else circle.setCenter(latLng);
    }

    private void initCamera(LatLng location) {
        CameraPosition position = CameraPosition.builder()
                .target(location)
                .zoom(18f)
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(0)
                .build();

        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), null);
    }

    private void getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(getApplicationContext());

        try {
            addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            this.address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            String zip = addresses.get(0).getPostalCode();
            String url= addresses.get(0).getUrl();

            this.city = state; this.country = country;

        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
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
            new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this)
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
        for(Marker marker1:Commons.markers){
            if(marker1.getPosition().equals(marker.getPosition())){
                showKeyboard();
                showAlertDialogForPinCommentEdit(marker1, getString(R.string.edit_pin), marker1.getTitle(), HomeActivity.this);
                break;
            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        showKeyboard();
        showAlertDialogForPinCommentInput(latLng, getString(R.string.stick_pin), getString(R.string.add_comment_pin), HomeActivity.this);
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

        CustomInfoWindowAdapter adapter = new CustomInfoWindowAdapter(HomeActivity.this);
        mMap.setInfoWindowAdapter(adapter);

        checkForLocationPermission();
        getPins();

    }

    private void isLocationEnabled() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(HomeActivity.this);
            alertDialog.setTitle(getString(R.string.enable_location));
            alertDialog.setMessage(getString(R.string.please_enabled_in_settings_menu));
            alertDialog.setPositiveButton(getString(R.string.location_setting), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    dialog.cancel();
                }
            });
            alertDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialog.create();
            alert.show();
        } else {
            Log.d("Info+++", "Location enabled");
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            try {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                                refreshMyMarker(latLng);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        Log.d("INFO+++", "GPS Provider enabled");
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.d("INFO+++", "GPS Provider disabled");
                    }
                });
            }
            else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            try {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                                refreshMyMarker(latLng);
//                                initCamera(latLng);

                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        Log.d("INFO+++", "NETWORK Provider enabled");
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.d("INFO+++", "NETWORK Provider disabled");
                    }
                });
            }
            else if(locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)){
                Log.d("Info+++", "Passive Location Provider enabled");
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            try {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());           ///////////////////////////////////////////////////////////////////////////////////////////////
                                refreshMyMarker(latLng);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        Log.d("INFO+++", "PASSIVE Provider enabled");
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        Log.d("INFO+++", "PASSIVE Provider disabled");
                    }
                });
            }
        }
    }

    public void openSettings(View view){
        Commons.googleMap = mMap;
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    public void openReport(){
        Intent intent = new Intent(getApplicationContext(), MyReportsActivity.class);
        startActivity(intent);
    }

    public void openMenu(View v) {
        powerMenu = new PowerMenu.Builder(HomeActivity.this)
                .addItem(new PowerMenuItem(getString(R.string.distribution_area), R.drawable.ic_area, false))
//                .addItem(new PowerMenuItem(getString(R.string.saved_history), R.drawable.ic_route, false))
                .addItem(new PowerMenuItem(getString(R.string.my_reports), R.drawable.report, false))
                .addItem(new PowerMenuItem(getString(R.string.my_profile), R.drawable.profile, false))
                .setAnimation(MenuAnimation.SHOWUP_TOP_LEFT) // Animation start point (TOP | LEFT)
                .setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.text))
                .setSelectedTextColor(Color.BLACK)
                .setMenuColor(Color.WHITE)
                .setSelectedMenuColor(ContextCompat.getColor(getApplicationContext(), R.color.circle_stroke_color))
                .setSelectedEffect(true)
                .setSelectedEffect(false)
                .setMenuShadow(10f)
                .setMenuRadius(10f)
                .setWidth(1000)
                .setOnMenuItemClickListener(onMenuItemClickListener)
                .build();
        powerMenu.showAsDropDown(v);
    }

    public void toggleRecording(View view){

        Log.d("Start Button", "Clicked");

        if(myLatLng == null)return;
//        if(saveBtn.getVisibility() == View.VISIBLE)return;

        boolean enabled = initialize.sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false);

        if (enabled) {
            if(!isLocationRecording){
                initialize.sharedPreferences.edit().remove(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED).commit();
            }else{
                Objects.requireNonNull(initialize.getForegroundOnlyLocationService()).unsubscribeToLocationUpdates();
                recordBtn.setBackgroundResource(R.drawable.primarydark_round_fill);
                recordBtn.setTextColor(Color.WHITE);
                recordBtn.setText(getString(R.string.start));
                isLocationRecording = false;
                showAlertDialogForRouteSaveInput(HomeActivity.this);

                if(!traces1.isEmpty()){
                    try {
                        uploadRoute();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            // TODO: Step 1.0, Review Permissions: Checks and requests if needed.
            if (foregroundPermissionApproved()) {
                showAlertDialogForRouteName(HomeActivity.this);
            } else {
                requestForegroundPermissions();
            }
        }

        checkDevice();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateButtonState(
                initialize.sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
        );
        initialize.sharedPreferences.registerOnSharedPreferenceChangeListener(HomeActivity.this);
        Intent serviceIntent = new Intent(this, ForegroundOnlyLocationService.class);
        bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void updateButtonState(boolean trackingLocation) {
        if (trackingLocation) {

        } else {

        }
    }

    @Override
    protected void onPause() {
        initialize.onPause();
        super.onPause();
        checkDevice();
    }

    @Override
    protected void onStop() {
        if (initialize.getForegroundOnlyLocationServiceBound()) {
            unbindService(foregroundOnlyServiceConnection);
            initialize.setForegroundOnlyLocationServiceBound(false);
        }
        initialize.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

//        backup();

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if(isLocationRecording){
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startActivity(startMain);
        }else{
            super.onBackPressed();
        }
        checkDevice();
    }

    @Override
    protected void onDestroy() {
        if (initialize.getForegroundOnlyLocationServiceBound()) {
            unbindService(foregroundOnlyServiceConnection);
            initialize.setForegroundOnlyLocationServiceBound(false);
        }
        initialize.sharedPreferences.unregisterOnSharedPreferenceChangeListener(HomeActivity.this);

        backup();

        super.onDestroy();
    }

    private boolean foregroundPermissionApproved() {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        );
    }

    private static int REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34;

    private void requestForegroundPermissions() {
        if(foregroundPermissionApproved()){
            startedTime = new Date().getTime();
            clearPolylines();
            totalDistance = 0;
            duration = 0;
            recordBtn.setBackgroundResource(R.drawable.red_round_fill);
            recordBtn.setTextColor(Color.YELLOW);
            recordBtn.setText(getString(R.string.end));
//            saveBtn.setVisibility(View.INVISIBLE);
            panel.setVisibility(View.VISIBLE);
            isLocationRecording = true;
        }else{
            ActivityCompat.requestPermissions(
                    HomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Objects.requireNonNull(initialize.getForegroundOnlyLocationService()).subscribeToLocationUpdates();
                startedTime = new Date().getTime();
                clearPolylines();
                totalDistance = 0;
                duration = 0;
                recordBtn.setBackgroundResource(R.drawable.red_round_fill);
                recordBtn.setTextColor(Color.YELLOW);
                recordBtn.setText(getString(R.string.end));
//                saveBtn.setVisibility(View.INVISIBLE);
                panel.setVisibility(View.VISIBLE);
                isLocationRecording = true;
            } else {
                updateButtonState(false);
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

    public void saveLocation(View view){
//        showAlertDialogForRouteSaveInput(HomeActivity.this);
    }

    public void clearPolylines(){
        for(Polyline polyline:polylines){
            polyline.remove();
        }
        polylines.clear();
        traces.clear();
        traces0.clear();
    }

    public LatLng getMyLatLng(Location location){
        if(location != null){
            return new LatLng(location.getLatitude(), location.getLongitude());
        }
        return null;
    }

    private static double lastDistance = 0.0d;

    public void drawRoute(LatLng curLatLng, boolean isFirst){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long currentTime = new Date().getTime();
                if(traces.isEmpty()){
                    RPoint rpoint = new RPoint();
                    rpoint.setLat(lastLatlng.latitude);
                    rpoint.setLng(lastLatlng.longitude);
                    rpoint.setTime(String.valueOf(currentTime));
                    traces.add(rpoint);
                }
                ArrayList<LatLng> points = new ArrayList<LatLng>();
                PolylineOptions lineOptions = new PolylineOptions();

                RPoint lastPoint = traces.get(traces.size() - 1);

                boolean pulse = false;

                if(traces.size() >= 2) {
                    RPoint lastPoint0 = traces.get(traces.size() - 2);
                    double dist0 = getDistance(new LatLng(lastPoint0.getLat(), lastPoint0.getLng()), new LatLng(lastPoint.getLat(), lastPoint.getLng()));
                    double dist1 = getDistance(new LatLng(lastPoint.getLat(), lastPoint.getLng()), curLatLng);
                    double dist2 = getDistance(new LatLng(lastPoint0.getLat(), lastPoint0.getLng()), curLatLng);
                    if(dist2 < dist0 || dist2 < dist1){
                        traces.remove(lastPoint);
                        pulse = true;
                        polylines.get(polylines.size() - 1).remove();
                        polylines.remove(polylines.get(polylines.size() - 1));
                        totalDistance -= lastDistance;

                        points.add(new LatLng(lastPoint0.getLat(), lastPoint0.getLng()));
                        points.add(curLatLng);
                    }else {
                        points.add(new LatLng(lastPoint.getLat(), lastPoint.getLng()));
                        points.add(curLatLng);
                    }
                }else {
                    points.add(new LatLng(lastPoint.getLat(), lastPoint.getLng()));
                    points.add(curLatLng);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);

                lastDistance = getDistance(points.get(0), points.get(1)) * 1.60934;

                /////// Unit:  km/h
                mSpeed = (double) lastDistance * 3600
                        /((currentTime - Long.parseLong(lastPoint.getTime()) > 1000? currentTime - Long.parseLong(lastPoint.getTime()) : 1000) * 0.001);
                lineOptions.color(getColor(getColorFromSpeed(mSpeed)));
                Polyline polyline = mMap.addPolyline(lineOptions);
                polylines.add(polyline);

                totalDistance += lastDistance;  // Convert miles to killometers
                distanceBox.setText(df.format(totalDistance) + "km");
                duration = currentTime - startedTime;
                durationBox.setText(getTimeStr(duration));
                speedBox.setText(df.format(mSpeed) + "km/h");

                /////// Unit:  km/h
                speed = (double) totalDistance * 3600/((currentTime - startedTime > 1000? currentTime - startedTime : 1000) * 0.001);

                RPoint rpoint = new RPoint();
                rpoint.setLat(curLatLng.latitude);
                rpoint.setLng(curLatLng.longitude);
                rpoint.setTime(String.valueOf(currentTime));
                String colorStr = "#" + getResources().getString(getColorFromSpeed(mSpeed)).substring(3, 9);
                rpoint.setColor(colorStr);
                traces.add(rpoint);
//                if(!isNetworkConnected()){
//                    traces1.add(rpoint);
//                    Log.d("OFFLINE TRACE POINTS", String.valueOf(traces1.size()));
//                }

                traces1.add(rpoint);

                Log.d("Polyline color", colorStr);

                if(traces.size() == 2){
                    traces.get(0).setColor(rpoint.getColor());
                }

                endedTime = new Date().getTime();
                lastLatlng = curLatLng;

                backup();
                saveLocationPoints(traces1, getString(R.string.location_points));

                if(isNetworkConnected()) {
                    ArrayList<RPoint> rpoints = getLocationPoints(getString(R.string.location_points));
                    if(rpoints == null) traces1 = rpoints;
                    if(isFirst){
                        try {
                            uploadRoute();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        if(traces1.size() > 20) {
                            int cnt = EasyPreference.with(getApplicationContext()).getInt("network_delay", 0);
                            cnt++;
                            if(cnt > 5){
                                try {
                                    uploadRoute();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                traces1.add(rpoint);
                            }
                            EasyPreference.with(getApplicationContext()).addInt("network_delay", cnt).save();
                        }
                    }
                }

                Log.d("TRACE POINTS", String.valueOf(traces.size()));
            }
        });
    }

    private int getColorFromSpeed(double speed) {
        int color = R.color.speed_00_02;
        if(speed >= 0 && speed < 2) color = R.color.speed_00_02;
        else if(speed >= 2 && speed < 4) color = R.color.speed_02_04;
        else if(speed >= 4 && speed < 6) color = R.color.speed_04_06;
        else if(speed >= 6 && speed < 8) color = R.color.speed_06_08;
        else if(speed >= 8 && speed < 16) color = R.color.speed_08_16;
        else if(speed >= 16 && speed < 32) color = R.color.speed_16_32;
        else if(speed >= 32 && speed < 64) color = R.color.speed_32_64;
        else if(speed >= 64) color = R.color.speed_64_100;
        return color;
    }

    private String getTimeStr(long timeDiff){
        String timeStr = "";
        int seconds = (int) (timeDiff / 1000) % 60 ;
        int minutes = (int) ((timeDiff / (1000*60)) % 60);
        int hours   = (int) ((timeDiff / (1000*60*60)) % 24);
        timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return timeStr;
    }

    private void backup(){
        EasyPreference.with(getApplicationContext(), "backup").addFloat("totalDistance", (float) totalDistance).save();
        EasyPreference.with(getApplicationContext(), "backup").addFloat("speed", (float) speed).save();
        EasyPreference.with(getApplicationContext(), "backup").addLong("duration", duration).save();
        EasyPreference.with(getApplicationContext(), "backup").addLong("startedTime", startedTime).save();
        EasyPreference.with(getApplicationContext(), "backup").addLong("endedTime", endedTime).save();
        EasyPreference.with(getApplicationContext(), "backup").addBoolean("isLocationRecording", isLocationRecording).save();
        if(lastLatlng != null) {
            EasyPreference.with(getApplicationContext(), "backup").addFloat("last_lat", (float)lastLatlng.latitude).save();
            EasyPreference.with(getApplicationContext(), "backup").addFloat("last_lng", (float)lastLatlng.longitude).save();
        }else {
            EasyPreference.with(getApplicationContext(), "backup").addFloat("last_lat", (float)myLatLng.latitude).save();
            EasyPreference.with(getApplicationContext(), "backup").addFloat("last_lng", (float)myLatLng.longitude).save();
            lastLatlng = myLatLng;
        }
//        saveTraceToSharedPreference(HomeActivity.this, "TRACE", "traces", traces);
    }

    public void showAlertDialogForPinCommentInput(LatLng latLng, String title, String hint, Activity activity){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_alert_input, null);
        builder.setView(view);
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        TextView titleBox = (TextView)view.findViewById(R.id.title);
        titleBox.setText(title);
        EditText contentBox = (EditText) view.findViewById(R.id.content);
        contentBox.setHint(hint);
        contentBox.setFocusable(true);
        contentBox.requestFocus();
//        contentBox.setSelection(contentBox.getText().length());
        TextView okButton = (TextView)view.findViewById(R.id.btn_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(contentBox.getText().length() == 0){
                    return;
                }

                Long currentTimeStamp = new Date().getTime();

                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
                String currentTime = dateFormat.format(currentTimeStamp);

                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(contentBox.getText().toString())
                        .snippet(currentTime.replace("AM", "午前").replace("PM", "午後"))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.targetmarker)));
                marker.showInfoWindow();
                Commons.markers.add(marker);

                RPoint pin = new RPoint();
                pin.setLat(latLng.latitude);
                pin.setLng(latLng.longitude);
                pin.setComment(contentBox.getText().toString());
                pin.setTime(currentTime);

                AndroidNetworking.post(ReqConst.SERVER_URL + "savepin")
                        .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getIdx()))
                        .addBodyParameter("pin_id", "0")
                        .addBodyParameter("lat", String.valueOf(pin.getLat()))
                        .addBodyParameter("lng", String.valueOf(pin.getLng()))
                        .addBodyParameter("comment", pin.getComment())
                        .addBodyParameter("time", String.valueOf(currentTimeStamp))
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // do anything with response
                                Log.d("PinSaved!!!", response.toString());
                                try {
                                    String result = response.getString("result_code");
                                    if (result.equals("0")) {
                                        String pinId = response.getString("pin_id");
                                        pin.setIdx(Integer.parseInt(pinId));
                                        Commons.pins.add(pin);
                                        showToast2(getString(R.string.saved));
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

    public void showAlertDialogForRouteSaveInput(Activity activity){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_alert_route_input, null);
        builder.setView(view);
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        EditText nameBox = (EditText) view.findViewById(R.id.nameBox);
        nameBox.setVisibility(View.GONE);

        Long currentTimeStamp = new Date().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_hhmm");
        String currentTime = dateFormat.format(currentTimeStamp);
        nameBox.setText(Commons.thisUser.getName() + "_" + currentTime);

        EditText descriptionBox = (EditText) view.findViewById(R.id.descriptionBox);
        descriptionBox.setFocusable(true);
        descriptionBox.requestFocus();

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.auto_report);
        if(Commons.autoReport)checkBox.setChecked(true);
        checkBox.setVisibility(View.GONE);

        TextView okButton = (TextView)view.findViewById(R.id.btn_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(nameBox.getText().length() == 0){
//                    nameBox.setError(getString(R.string.enter_route_name));
//                    return;
//                }
//                nameBox.clearFocus();

                descriptionBox.clearFocus();

                try {
                    String name = nameBox.getText().toString();
                    String description = descriptionBox.getText().toString();
                    String status = "";
                    if(checkBox.isChecked()){
                        status = "report";
                    }
                    String colorStr = "#" + getResources().getString(getColorFromSpeed(mSpeed)).substring(3, 9);
                    uploadRealTimeRoute("", description, "report", 2, false);
                    dialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                try {
//                    shref = getSharedPreferences("ROUTE", Context.MODE_PRIVATE);
//
//                    Route route = new Route();
//                    route.setMember_id(Commons.thisUser.getIdx());
//                    route.setName(nameBox.getText().toString());
//                    route.setDescription(descriptionBox.getText().toString());
//                    route.setDuration(duration);
//                    route.setDistance(totalDistance);
//                    route.setSpeed(speed);
//                    route.setStart_time(String.valueOf(startedTime));
//                    route.setEnd_time(String.valueOf(endedTime));
//
//                    for(RPoint point:traces){
//                        point.setRoute_id(route.getIdx());
//                    }
//
//                    Log.d("TRACES COUNT!!!", String.valueOf(traces.size()));
//
//                    uploadRoute(route);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                dialog.dismiss();
            }
        });

        ImageView cancelButton = (ImageView)view.findViewById(R.id.btn_cancel);
//        cancelButton.setVisibility(View.GONE);
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
        int dialogWindowWidth = (int) (displayWidth * 0.9f);
        // Set alert dialog height equal to screen height 80%
        //    int dialogWindowHeight = (int) (displayHeight * 0.8f);

        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = dialogWindowWidth;
        //      layoutParams.height = dialogWindowHeight;

        // Apply the newly created layout parameters to the alert dialog window
        dialog.getWindow().setAttributes(layoutParams);
//        dialog.setCancelable(false);
    }

    public String createPointsJsonString()throws JSONException {
        String pointJsonStr = "";
        JSONObject jsonObj = null;
        JSONArray jsonArr = new JSONArray();
        if (traces1.size() > 0){
            int i = 0;
            for(RPoint point: traces1){
                jsonObj = new JSONObject();
                try {
                    jsonObj.put("id", String.valueOf(++i));
                    jsonObj.put("route_id", String.valueOf(routeID));
                    jsonObj.put("lat", String.valueOf(point.getLat()));
                    jsonObj.put("lng", String.valueOf(point.getLng()));
                    jsonObj.put("comment", point.getComment());
                    jsonObj.put("color", point.getColor());
                    jsonObj.put("time", String.valueOf(point.getTime()));
                    jsonObj.put("status", "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonArr.put(jsonObj);
            }
            JSONObject pointJsonObj = new JSONObject();
            pointJsonObj.put("points", jsonArr);
            pointJsonStr = pointJsonObj.toString();
            return pointJsonStr;
        }
        return pointJsonStr;
    }

    public void showAlertDialogForPinCommentEdit(Marker marker, String title, String comment, Activity activity){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_alert_input, null);
        builder.setView(view);
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        TextView titleBox = (TextView)view.findViewById(R.id.title);
        titleBox.setText(title);
        EditText contentBox = (EditText) view.findViewById(R.id.content);
        contentBox.setText(comment);
        contentBox.setFocusable(true);
        contentBox.requestFocus();
        contentBox.setSelection(contentBox.getText().length());
        TextView okButton = (TextView)view.findViewById(R.id.btn_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(contentBox.getText().length() == 0){
                    return;
                }

                Long currentTimeStamp = new Date().getTime();

                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
                String currentTime = dateFormat.format(currentTimeStamp);

                LatLng markerLatLng = marker.getPosition();
                marker.setTitle(contentBox.getText().toString());
                marker.setSnippet(currentTime.replace("AM", "午前").replace("PM", "午後"));
                marker.showInfoWindow();

                for(RPoint pin:Commons.pins){
                    if(new LatLng(pin.getLat(), pin.getLng()).equals(markerLatLng)){
                        pin.setComment(contentBox.getText().toString());
                        pin.setTime(currentTime);

                        AndroidNetworking.post(ReqConst.SERVER_URL + "savepin")
                                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getIdx()))
                                .addBodyParameter("pin_id", String.valueOf(pin.getIdx()))
                                .addBodyParameter("lat", String.valueOf(pin.getLat()))
                                .addBodyParameter("lng", String.valueOf(pin.getLng()))
                                .addBodyParameter("comment", pin.getComment())
                                .addBodyParameter("time", String.valueOf(currentTimeStamp))
                                .setPriority(Priority.HIGH)
                                .build()
                                .getAsJSONObject(new JSONObjectRequestListener() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // do anything with response
                                        Log.d("PinSaved!!!", response.toString());
                                        try {
                                            String result = response.getString("result_code");
                                            if (result.equals("0")) {
                                                showToast2(getString(R.string.saved));
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
                        break;
                    }
                }

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(),0);

                dialog.dismiss();
            }
        });

        TextView delButton = (TextView)view.findViewById(R.id.btn_del);
        delButton.setVisibility(View.VISIBLE);
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng markerLatLng = marker.getPosition();
                marker.remove();
                if(Commons.markers.contains(marker)) Commons.markers.remove(marker);
                for(RPoint pin:Commons.pins){
                    if(new LatLng(pin.getLat(), pin.getLng()).equals(markerLatLng)){
                        AndroidNetworking.post(ReqConst.SERVER_URL + "delpin")
                                .addBodyParameter("pin_id", String.valueOf(pin.getIdx()))
                                .setPriority(Priority.HIGH)
                                .build()
                                .getAsJSONObject(new JSONObjectRequestListener() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        // do anything with response
                                        Log.d("Pin deleted !!!", response.toString());
                                        try {
                                            String result = response.getString("result_code");
                                            if(result.equals("0")){
                                                Commons.pins.remove(pin);
                                                showToast2(getString(R.string.deleted));
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
                                    }
                                });
                        break;
                    }
                }

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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED)) {
            updateButtonState(sharedPreferences.getBoolean(
                    SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
            );
        }
    }

    public void clearPins(){
        for(Marker marker:Commons.markers){
            marker.remove();
        }
        Commons.markers.clear();
    }

    public void getPins(){
        AndroidNetworking.post(ReqConst.SERVER_URL + "getmypins")
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getIdx()))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("My pins !!!", response.toString());
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                Commons.pins.clear();
                                clearPins();
                                JSONArray dataArr = response.getJSONArray("pins");
                                for(int i=0; i<dataArr.length(); i++) {
                                    JSONObject object = (JSONObject) dataArr.get(i);
                                    RPoint pin = new RPoint();
                                    pin.setIdx(object.getInt("id"));
                                    pin.setLat(Double.parseDouble(object.getString("lat")));
                                    pin.setLng(Double.parseDouble(object.getString("lng")));
                                    pin.setComment(object.getString("comment"));
                                    pin.setTime(object.getString("time"));
                                    pin.setStatus(object.getString("status"));

                                    Commons.pins.add(pin);

                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(pin.getLat(), pin.getLng()))
                                            .title(pin.getComment())
                                            .snippet(pin.getTime().replace("AM", "午前").replace("PM", "午後"))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.targetmarker)));
                                    marker.showInfoWindow();
                                    Commons.markers.add(marker);
                                }
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
                    }
                });
    }

    Polygon areaPolygon;
    ArrayList<Polygon> subAreaPolygons = new ArrayList<>();

    public void drawArea(ArrayList<LatLng> coords){
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for(LatLng coord : coords) {
            builder.include(coord);
        }
        final LatLngBounds bounds = builder.build();
        try {
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        PolygonOptions poly = new PolygonOptions();
        poly.strokeWidth(5f)
                .strokeColor(Color.parseColor("#CCFF0000"))
                .fillColor(Color.parseColor("#1FFF0000"));
        for (LatLng latLng : coords) {
            poly.addAll(Collections.singleton(latLng));
        }
        areaPolygon = mMap.addPolygon(poly);
    }

    public void clearOldDrawing(){
        if(areaPolygon != null) areaPolygon.remove();
        for(Polygon polygon: subAreaPolygons){
            polygon.remove();
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
        subAreaPolygons = polygons;

        final LatLngBounds bounds = builder.build();
        try {
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goToLocation(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address != null) {
                try {
                    Address location = address.get(0);
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
                } catch (IndexOutOfBoundsException er) {
                    Log.d("Status", "Location isn't available");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String checkJson(String data) throws JSONException {
        String result = "jsonobj";
        Object json = new JSONTokener(data).nextValue();
        if (json instanceof JSONObject){
            return result;
        }else if (json instanceof JSONArray){
            return "jsonarray";
        }
        return result;
    }

    public void showAreaDetails(Area area) {
        ((LinearLayout)findViewById(R.id.areaDetailLayout)).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.areaNameBox)).setText(area.getAreaName());
        ((TextView)findViewById(R.id.titleBox)).setText(area.getTitle());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        String startedTime = dateFormat.format(new Date(area.getStartTime()));
        String endedTime = dateFormat.format(new Date(area.getEndTime()));
        ((TextView)findViewById(R.id.timeBox)).setText(startedTime + " ~ " + endedTime);
        ((TextView)findViewById(R.id.durBox)).setText(getDurationStr(area.getEndTime() - area.getStartTime()));
        ((TextView)findViewById(R.id.distBox)).setText("距離: " + df.format(area.getDistance()) + " km");
        ((TextView)findViewById(R.id.distributionBox)).setText("配布物: " + area.getDistribution());
        ((TextView)findViewById(R.id.priceBox)).setText("金額: " + BaseActivity.getFormattedStr((long) area.getAmount()) + " 円");
        ((TextView)findViewById(R.id.copiesBox)).setText("部数: " + BaseActivity.getFormattedStr((long) area.getCopies()));
    }

    private String getDurationStr(long timeDiff){
        int days  = (int) timeDiff / (1000*60*60*24);
        return String.valueOf(days) + " 日";
    }

    public void closeDetailFrame(View view) {
        if(isLocationRecording)return;
        ((LinearLayout)findViewById(R.id.areaDetailLayout)).setVisibility(View.GONE);
        if(areaPolygon != null) areaPolygon.remove();
        for(Polygon polygon: subAreaPolygons){
            polygon.remove();
        }
        EasyPreference.with(getApplicationContext(), "backup").addLong("assign_id", 0).save();
        Commons.area = null;
        assignID = 0;
    }

    ProgressDialog pdDialog;
    public void getRoute(long route_id){
        pdDialog = new ProgressDialog(HomeActivity.this);
        pdDialog.setTitle(getString(R.string.please_wait));
        pdDialog.setMessage(getString(R.string.processing_data));
        pdDialog.show();
        pdDialog.setCancelable(false);
        isDataLoading = true;
        AndroidNetworking.post(ReqConst.SERVER_URL + "routedetails")
                .addBodyParameter("route_id", String.valueOf(route_id))
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("RESPONSE!!!", response.toString());
                        pdDialog.dismiss();
                        isDataLoading = false;
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                traces0.clear();
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

                                    traces0.add(point);
                                }
                                new ShowPolyLine().execute();

                            }else {
                                Log.d("Result===>", "Error");
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
                        isDataLoading = false;
                    }
                });
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
                        for(RPoint point:traces0){
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
                                        polylines.add(lastPolyline);
                                        if(rPoints.size() == traces0.size() - ii){
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

    private void uploadRoute() throws JSONException {
        JsonFile jsonFile = new JsonFile();
        try {
            jsonFile.createFile();
            jsonFile.writeToFile(createPointsJsonString());
            Log.d("%%%%%%%%%%%%%%%%%%===>", jsonFile.readFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        routeID = EasyPreference.with(getApplicationContext(), "backup").getLong("route_id", 0);
        assignID = EasyPreference.with(getApplicationContext(), "backup").getLong("assign_id", 0);
        totalDistance = EasyPreference.with(getApplicationContext(), "backup").getFloat("totalDistance", 0);
        speed = EasyPreference.with(getApplicationContext(), "backup").getFloat("speed", 0);
        duration = EasyPreference.with(getApplicationContext(), "backup").getLong("duration", 0);
        startedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("startedTime", 0);
        endedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("endedTime", 0);

//        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER_URL + "updatereportdatainrealtime")
                .addMultipartFile("jsonfile", jsonFile.getFile())
                .addMultipartParameter("route_id", String.valueOf(routeID))
                .addMultipartParameter("assign_id", String.valueOf(assignID))
                .addMultipartParameter("member_id", String.valueOf(Commons.thisUser.getIdx()))
                .addMultipartParameter("name", "")
                .addMultipartParameter("description", "")
                .addMultipartParameter("start_time", String.valueOf(startedTime))
                .addMultipartParameter("end_time", String.valueOf(endedTime))
                .addMultipartParameter("duration", String.valueOf(duration))
                .addMultipartParameter("speed", String.valueOf(speed))
                .addMultipartParameter("distance", String.valueOf(totalDistance))
                .addMultipartParameter("pulse", "0")
                .addMultipartParameter("status", "report")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("XXXXXXXXXXX===>", response.toString());
//                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                traces1.clear();
                                clearLocationPoints(getString(R.string.location_points));
                                EasyPreference.with(getApplicationContext()).addInt("network_delay", 0).save();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    private void uploadRealTimeRoute(String name, String description, String status, int end, boolean pulse) throws JSONException {
        if(end == 0 || end == 2) progressBar.setVisibility(View.VISIBLE);
        routeID = EasyPreference.with(getApplicationContext(), "backup").getLong("route_id", 0);
        assignID = EasyPreference.with(getApplicationContext(), "backup").getLong("assign_id", 0);
        totalDistance = EasyPreference.with(getApplicationContext(), "backup").getFloat("totalDistance", 0);
        speed = EasyPreference.with(getApplicationContext(), "backup").getFloat("speed", 0);
        duration = EasyPreference.with(getApplicationContext(), "backup").getLong("duration", 0);
        startedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("startedTime", 0);
        endedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("endedTime", 0);
        AndroidNetworking.post(ReqConst.SERVER_URL + "startorendreporting")
                .addBodyParameter("route_id", String.valueOf(routeID))
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getIdx()))
                .addBodyParameter("assign_id", String.valueOf(assignID))
                .addBodyParameter("name", name)
                .addBodyParameter("description", description)
                .addBodyParameter("start_time", String.valueOf(startedTime))
                .addBodyParameter("end_time", String.valueOf(endedTime))
                .addBodyParameter("duration", String.valueOf(duration))
                .addBodyParameter("speed", String.valueOf(speed))
                .addBodyParameter("distance", String.valueOf(totalDistance))
                .addBodyParameter("pulse", pulse? "1":"0")
                .addBodyParameter("status", status)

//                .addBodyParameter("lat", String.valueOf(latLng.latitude))
//                .addBodyParameter("lng", String.valueOf(latLng.longitude))
//                .addBodyParameter("comment", comment)
//                .addBodyParameter("color", color)

                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("/////////////////===>", response.toString());
                        if(end == 0 || end == 2) progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                if(end == 0) {
                                    Objects.requireNonNull(initialize.getForegroundOnlyLocationService()).subscribeToLocationUpdates();
                                    clearPolylines();
                                    recordBtn.setBackgroundResource(R.drawable.red_round_fill);
                                    recordBtn.setTextColor(Color.YELLOW);
                                    recordBtn.setText(getString(R.string.end));
                                    panel.setVisibility(View.VISIBLE);
                                    isLocationRecording = true;

                                    distanceBox.setText("0km");
                                    durationBox.setText(getTimeStr(0));
                                    speedBox.setText(df.format(0) + "km/h");

                                    backup();

                                    routeID = Long.parseLong(response.getString("route_id"));
                                    EasyPreference.with(getApplicationContext(), "backup").addLong("route_id", routeID).save();

                                    drawRoute(myLatLng, true);

                                }else {
                                    routeID = Long.parseLong(response.getString("route_id"));
                                    EasyPreference.with(getApplicationContext(), "backup").addLong("route_id", routeID).save();
                                    if(end == 2) {
//                                        Objects.requireNonNull(initialize.getForegroundOnlyLocationService()).unsubscribeToLocationUpdates();
//                                        recordBtn.setBackgroundResource(R.drawable.primarydark_round_fill);
//                                        recordBtn.setTextColor(Color.WHITE);
//                                        recordBtn.setText(getString(R.string.start));
                                        endedTime = new Date().getTime();
//                                        isLocationRecording = false;
                                        if(status.length() > 0)
                                            showToast2(getString(R.string.successfully_sent));
                                        else
                                            showToast2(getString(R.string.saved));
                                        EasyPreference.with(getApplicationContext(), "backup").clearAll().save();

                                        openReport();
                                    }
                                }
                            }else {
                                if(end == 2) showToast(getString(R.string.something_wrong));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.d("ERROR!!!", error.getErrorDetail());
                        if(end == 2) {
                            progressBar.setVisibility(View.GONE);
                            showToast(getString(R.string.something_wrong));
                        }
                    }
                });

    }

    public void showAlertDialogForRouteName(Activity activity){
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
        LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.layout_alert_route_input, null);
        builder.setView(view);
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();

        TextView titleBox = (TextView) view.findViewById(R.id.title);
        titleBox.setText("ルートに名前を付けます。");

        EditText nameBox = (EditText) view.findViewById(R.id.nameBox);
        nameBox.setFocusable(true);
        nameBox.requestFocus();

        Long currentTimeStamp = new Date().getTime();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_hhmm");
        String currentTime = dateFormat.format(currentTimeStamp);
        nameBox.setText(Commons.thisUser.getName() + "_" + currentTime);

        EditText descriptionBox = (EditText) view.findViewById(R.id.descriptionBox);
        descriptionBox.setVisibility(View.GONE);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.auto_report);
        checkBox.setVisibility(View.GONE);
        if(Commons.autoReport)checkBox.setChecked(true);

        TextView okButton = (TextView)view.findViewById(R.id.btn_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(nameBox.getText().length() == 0){
                    nameBox.setError(getString(R.string.enter_route_name));
                    return;
                }

                nameBox.clearFocus();
                descriptionBox.clearFocus();

                startedTime = new Date().getTime();
                routeID = 0;
                totalDistance = 0;
                duration = 0;

                endedTime = new Date().getTime();
                lastLatlng = null;

                EasyPreference.with(getApplicationContext(), "backup").clearAll().save();
                EasyPreference.with(getApplicationContext(), "backup").addLong("assign_id", Commons.area != null ? Commons.area.getIdx() : 0).save();

                backup();

                try {
                    String name = nameBox.getText().toString();
                    String description = descriptionBox.getText().toString();
                    String status = "";
                    if(checkBox.isChecked()){
                        status = "report";
                    }

                    uploadRealTimeRoute(name, "", "report", 0, false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                try {
//                    shref = getSharedPreferences("ROUTE", Context.MODE_PRIVATE);
//
//                    Route route = new Route();
//                    route.setMember_id(Commons.thisUser.getIdx());
//                    route.setName(nameBox.getText().toString());
//                    route.setDescription(descriptionBox.getText().toString());
//                    route.setDuration(duration);
//                    route.setDistance(totalDistance);
//                    route.setSpeed(speed);
//                    route.setStart_time(String.valueOf(startedTime));
//                    route.setEnd_time(String.valueOf(endedTime));
//
//                    for(RPoint point:traces){
//                        point.setRoute_id(route.getIdx());
//                    }
//
//                    Log.d("TRACES COUNT!!!", String.valueOf(traces.size()));
//
//                    uploadRoute(route);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                dialog.dismiss();
            }
        });

        ImageView cancelButton = (ImageView)view.findViewById(R.id.btn_cancel);
//        cancelButton.setVisibility(View.GONE);
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
        int dialogWindowWidth = (int) (displayWidth * 0.9f);
        // Set alert dialog height equal to screen height 80%
        //    int dialogWindowHeight = (int) (displayHeight * 0.8f);

        // Set the width and height for the layout parameters
        // This will bet the width and height of alert dialog
        layoutParams.width = dialogWindowWidth;
        //      layoutParams.height = dialogWindowHeight;

        // Apply the newly created layout parameters to the alert dialog window
        dialog.getWindow().setAttributes(layoutParams);
//        dialog.setCancelable(false);
    }

}










































