package com.app.rakubaru.main;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.FrameLayout;
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
import com.app.rakubaru.classes.JsonFile;
import com.app.rakubaru.classes.MapWrapperLayout;
import com.app.rakubaru.commons.Commons;
import com.app.rakubaru.commons.Constants;
import com.app.rakubaru.commons.ReqConst;
import com.app.rakubaru.models.Area;
import com.app.rakubaru.models.RPoint;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.PI;
import static java.lang.Math.min;

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
    FrameLayout progressBar;

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
    public static boolean isDataLoading = false;

    private RPoint curPoint;

    SharedPreferences shref;
    SharedPreferences.Editor editor;

    public static final int PERMISSIONS_REQUEST = 101;

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

    @RequiresApi(api = Build.VERSION_CODES.P)
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

        checkPermissions(LOC_PER);
        checkPermissions(CAM_PER);
        grantPermissions();

        String myVersion = android.os.Build.VERSION.RELEASE;
        Log.i("OS version", myVersion);
        if(Integer.parseInt(myVersion.split("\\.")[0]) > 10){
            if(ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this)
                        .setTitle("バックグラウンドロケーション許可")
                        .setMessage("バックグラウンドで位置情報を取得するには、位置情報のアクセス許可を「常に許可」に設定します。\n" +
                                "また、ルートをファイルとして安全に保存するには、保存権限を「許可する」に設定します。")
                        .setPositiveButton("許可する", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                openSettings();
                            }
                        })
                        .setNegativeButton("キャンセル", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }

        isLocationRecording = false;
        totalDistance = 0.0d;
        speed = 0.0d;
        duration = 0;
        startedTime = 0;
        endedTime = 0;

        clearPolylines();

        shref = getSharedPreferences("ROUTE", Context.MODE_PRIVATE);

        progressBar = (FrameLayout)findViewById(R.id.progress_overlay);

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
            Commons.traces.clear();
            Commons.routeTraces111.clear();
        }

    }

    public void grantPermissions() {
        ArrayList<String> arrPerm = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.FOREGROUND_SERVICE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.FOREGROUND_SERVICE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.FOREGROUND_SERVICE);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        if(!arrPerm.isEmpty()) {
            String[] permissions = new String[arrPerm.size()];
            permissions = arrPerm.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST);
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.P)
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

        new ShowPolyLine().execute();
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
        if(isLocationRecording) drawRoute(myLatLng);
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
                finalizeReport(false);
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

    boolean IS8HOURS = false;
    private void finalizeReport(boolean is8hours) {
        Objects.requireNonNull(initialize.getForegroundOnlyLocationService()).unsubscribeToLocationUpdates();
        recordBtn.setBackgroundResource(R.drawable.primarydark_round_fill);
        recordBtn.setTextColor(Color.WHITE);
        recordBtn.setText(getString(R.string.start));
        isLocationRecording = false;
        backup();

        IS8HOURS = is8hours;

        if(!is8hours) showAlertDialogForRouteSaveInput(HomeActivity.this);
        else initialize.notify8hoursExceed();

        if(curPoint != null){
            if(isNetworkConnected()) {
                if(Commons.routeTraces111.size() > 0) {
                    try {
                        uploadRoutePoints(true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    if(is8hours){
                        try {
                            uploadStartOrEndRoute("", "", 2);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
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
                    HomeActivity.this, LOC_PER,
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
                openSettings();
            }
        }else if(requestCode == PERMISSIONS_REQUEST){
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0) {
                for(int i = 0; i < grantResults.length; i++) {
                    String permission = permissions[i];
                    if(Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)) {
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // you now have permission
                        }else openSettings();
                    }
                    if(Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)) {
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // you now have permission
                        }else openSettings();
                    }
                    if(Manifest.permission.FOREGROUND_SERVICE.equals(permission)) {
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // you now have permission
                        }else openSettings();
                    }
                    if(Manifest.permission.ACCESS_BACKGROUND_LOCATION.equals(permission)) {
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // you now have permission
                        }else openSettings();
                    }
                }
            } else {
                openSettings();
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
        traces0.clear();
    }

    public LatLng getMyLatLng(Location location){
        if(location != null){
            return new LatLng(location.getLatitude(), location.getLongitude());
        }
        return null;
    }

    private static double lastDistance = 0.0d;
    long currentTime = new Date().getTime();

    boolean xxx = false;

    public void drawRoute(LatLng curLatLng){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentTime = new Date().getTime();
                if(Commons.traces.isEmpty()){
                    RPoint rpoint = new RPoint();
                    rpoint.setLat(lastLatlng.latitude);
                    rpoint.setLng(lastLatlng.longitude);
                    rpoint.setTime(String.valueOf(currentTime));
                    Commons.traces.add(rpoint);
                    curPoint = rpoint;
                }
                ArrayList<LatLng> points = new ArrayList<LatLng>();
                PolylineOptions lineOptions = new PolylineOptions();

                RPoint lastPoint = Commons.traces.get(Commons.traces.size() - 1);

                boolean pulse = false;

                if(Commons.traces.size() >= 2) {
                    RPoint lastPoint0 = Commons.traces.get(Commons.traces.size() - 2);
                    double dist0 = getDistance(new LatLng(lastPoint0.getLat(), lastPoint0.getLng()), new LatLng(lastPoint.getLat(), lastPoint.getLng()));
                    double dist1 = getDistance(new LatLng(lastPoint.getLat(), lastPoint.getLng()), curLatLng);
                    double dist2 = getDistance(new LatLng(lastPoint0.getLat(), lastPoint0.getLng()), curLatLng);
                    if(dist2 < dist0 || dist2 < dist1){
                        Commons.traces.remove(lastPoint);
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
                        /((currentTime - Long.parseLong(lastPoint.getTime()) > 100? currentTime - Long.parseLong(lastPoint.getTime()) : 100) * 0.001);

                int pColor = getColorFromSpeed(mSpeed);
                lineOptions.color(getColor(pColor));
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
                String colorStr = "#" + getResources().getString(pColor).substring(3, 9);
                rpoint.setColor(colorStr);

                curPoint = rpoint;

                Log.d("Polyline color", colorStr);

                if(Commons.traces.size() == 2){
                    Commons.traces.get(0).setColor(rpoint.getColor());
                }

                Commons.traces.add(rpoint);
                Commons.routeTraces111.add(rpoint);

                endedTime = new Date().getTime();
                lastLatlng = curLatLng;

                backup();

                if(getDur(duration).getHours() >= 12) {
                    finalizeReport(true);
                    return;
                }

                if(xxx) {
                    traces0.add(rpoint);
                    return;
                }

                long last_loaded = EasyPreference.with(getApplicationContext()).getLong("last_loaded", 0);
                long diff = currentTime - last_loaded;
                Log.d("DIFF ========== >", String.valueOf(diff));
                if(diff > 600000){
                    if(isNetworkConnected()){
                        try {
                            xxx = true;
                            uploadRoutePoints(false);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        xxx = false;
                        if(!traces0.isEmpty())traces0.clear();
                    }
                }
                Log.d("TRACE POINTS", String.valueOf(Commons.traces.size()));
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

    private DurTime getDur(long timeDiff){
        int seconds = (int) (timeDiff / 1000) % 60 ;
        int minutes = (int) ((timeDiff / (1000*60)) % 60);
        int hours   = (int) ((timeDiff / (1000*60*60)) % 24);
        return new DurTime(hours, minutes);
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

        TextView noteBox = (TextView) view.findViewById(R.id.note);
        noteBox.setVisibility(View.GONE);

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.auto_report);
        if(Commons.autoReport)checkBox.setChecked(true);
        checkBox.setVisibility(View.GONE);

        TextView okButton = (TextView)view.findViewById(R.id.btn_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                descriptionBox.clearFocus();

                try {
                    String name = nameBox.getText().toString();
                    String description = descriptionBox.getText().toString();
                    String status = "";
                    if(checkBox.isChecked()){
                        status = "report";
                    }
                    String colorStr = "#" + getResources().getString(getColorFromSpeed(mSpeed)).substring(3, 9);
                    uploadStartOrEndRoute("", description, 2);
                    dialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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

    public String createPointsJsonString(ArrayList<RPoint> rPoints)throws JSONException {
        String pointJsonStr = "";
        JSONObject jsonObj = null;
        JSONArray jsonArr = new JSONArray();
        ArrayList<RPoint> pointArrayList = new ArrayList<>(rPoints);
        if (pointArrayList.size() > 0){
            int i = 0;
            for(RPoint point: pointArrayList){
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
                        for(RPoint point:Commons.traces){
                            new Handler().postDelayed(new Runnable() {
                                @RequiresApi(api = Build.VERSION_CODES.P)
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
                                        if(rPoints.size() >= Commons.traces.size() - ii){
                                            ((LinearLayout)findViewById(R.id.loadingView)).setVisibility(View.GONE);
                                            ((View)findViewById(R.id.darkbg)).setVisibility(View.GONE);
                                        }
                                    }
                                    if(rPoints.size() >= Commons.traces.size()){
                                        ((LinearLayout)findViewById(R.id.loadingView)).setVisibility(View.GONE);
                                        ((View)findViewById(R.id.darkbg)).setVisibility(View.GONE);
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

    private void uploadRoutePoints(boolean end) throws JSONException {

        String jsonstr = createPointsJsonString(Commons.routeTraces111);
        WriteData(jsonstr);

        final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        final File file = new File(path, "traces1.txt");

        routeID = EasyPreference.with(getApplicationContext(), "backup").getLong("route_id", 0);
        assignID = EasyPreference.with(getApplicationContext(), "backup").getLong("assign_id", 0);
        totalDistance = EasyPreference.with(getApplicationContext(), "backup").getFloat("totalDistance", 0);
        speed = EasyPreference.with(getApplicationContext(), "backup").getFloat("speed", 0);
        duration = EasyPreference.with(getApplicationContext(), "backup").getLong("duration", 0);
        startedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("startedTime", 0);
        endedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("endedTime", 0);

//        progressBar.setVisibility(View.VISIBLE);
        AndroidNetworking.upload(ReqConst.SERVER_URL + "rakuETMupdate")
                .addMultipartFile("jsonfile", file)
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
                .addMultipartParameter("status", end ? "1" : "0")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @RequiresApi(api = Build.VERSION_CODES.P)
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("XXXXXXXXXX JSON ===>", response.toString());
                        xxx = false;
//                        progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                Commons.routeTraces111.clear();
                                EasyPreference.with(getApplicationContext()).addLong("last_loaded", currentTime).save();
                                if(!end) Commons.routeTraces111.addAll(traces0);
                                else if(IS8HOURS){
                                    EasyPreference.with(getApplicationContext(), "backup").clearAll().save();
                                    Commons.traces.clear();
                                    openReport();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        traces0.clear();
                    }

                    @Override
                    public void onError(ANError error) {
                        progressBar.setVisibility(View.GONE);
                        xxx = false;
                        traces0.clear();
                    }
                });

    }

//    private void uploadRoutePoints2(boolean end) throws JSONException {
//
//        routeID = EasyPreference.with(getApplicationContext(), "backup").getLong("route_id", 0);
//        assignID = EasyPreference.with(getApplicationContext(), "backup").getLong("assign_id", 0);
//        totalDistance = EasyPreference.with(getApplicationContext(), "backup").getFloat("totalDistance", 0);
//        speed = EasyPreference.with(getApplicationContext(), "backup").getFloat("speed", 0);
//        duration = EasyPreference.with(getApplicationContext(), "backup").getLong("duration", 0);
//        startedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("startedTime", 0);
//        endedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("endedTime", 0);
//
////        progressBar.setVisibility(View.VISIBLE);
//        AndroidNetworking.post(ReqConst.SERVER_URL + "ETMupdate")
//                .addBodyParameter("points_json_data", createPointsJsonString())
//                .addBodyParameter("route_id", String.valueOf(routeID))
//                .addBodyParameter("assign_id", String.valueOf(assignID))
//                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getIdx()))
//                .addBodyParameter("name", "")
//                .addBodyParameter("description", "")
//                .addBodyParameter("start_time", String.valueOf(startedTime))
//                .addBodyParameter("end_time", String.valueOf(endedTime))
//                .addBodyParameter("duration", String.valueOf(duration))
//                .addBodyParameter("speed", String.valueOf(speed))
//                .addBodyParameter("distance", String.valueOf(totalDistance))
//                .addBodyParameter("status", end ? "1" : "0")
//                .setPriority(Priority.HIGH)
//                .build()
//                .getAsJSONObject(new JSONObjectRequestListener() {
//                    @RequiresApi(api = Build.VERSION_CODES.P)
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        // do anything with response
//                        Log.d("XXXXXXXXXX JSON ===>", response.toString());
////                        progressBar.setVisibility(View.GONE);
//                        try {
//                            String result = response.getString("result_code");
//                            if(result.equals("0")){
//                                routeTraces111.clear();
//                                clearLocationPoints(getString(R.string.location_points));
//                                EasyPreference.with(getApplicationContext()).addLong("last_loaded", currentTime).save();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(ANError error) {
//                        progressBar.setVisibility(View.GONE);
//                    }
//                });
//
//    }

    private void uploadStartOrEndRoute(String name, String description, int status) throws JSONException {
        if(status == 0 || status == 2) progressBar.setVisibility(View.VISIBLE);
        routeID = EasyPreference.with(getApplicationContext(), "backup").getLong("route_id", 0);
        assignID = EasyPreference.with(getApplicationContext(), "backup").getLong("assign_id", 0);
        totalDistance = EasyPreference.with(getApplicationContext(), "backup").getFloat("totalDistance", 0);
        speed = EasyPreference.with(getApplicationContext(), "backup").getFloat("speed", 0);
        duration = EasyPreference.with(getApplicationContext(), "backup").getLong("duration", 0);
        startedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("startedTime", 0);
        endedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("endedTime", 0);

        AndroidNetworking.post(ReqConst.SERVER_URL + "rakuSOEreporting")
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
                .addBodyParameter("status", String.valueOf(status))

                .addBodyParameter("lat", String.valueOf(myLatLng.latitude))
                .addBodyParameter("lng", String.valueOf(myLatLng.longitude))
                .addBodyParameter("comment", "")
                .addBodyParameter("color", "#" + getResources().getString(getColorFromSpeed(0)).substring(3, 9))
                .addBodyParameter("tm", String.valueOf(new Date().getTime()))

                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @RequiresApi(api = Build.VERSION_CODES.P)
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("/////////////////===>", response.toString());
                        if(status == 0 || status == 2) progressBar.setVisibility(View.GONE);
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                if(status == 0) {
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

                                }else {
                                    routeID = Long.parseLong(response.getString("route_id"));
                                    EasyPreference.with(getApplicationContext(), "backup").addLong("route_id", routeID).save();
                                    if(status == 2) {
                                        endedTime = new Date().getTime();
                                        showToast2(getString(R.string.saved));
                                        EasyPreference.with(getApplicationContext(), "backup").clearAll().save();

                                        Commons.traces.clear();
                                        openReport();
                                    }
                                }
                            }else {
                                if(status == 2) showToast(getString(R.string.something_wrong));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.d("ERROR!!!", error.getErrorDetail());
                        if(status == 2) {
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

        TextView noteBox = (TextView) view.findViewById(R.id.note);
        noteBox.setVisibility(View.VISIBLE);

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

                Commons.traces.clear();
                Commons.routeTraces111.clear();
                traces0.clear();

                IS8HOURS = false;

                try {
                    String name = nameBox.getText().toString();
                    uploadStartOrEndRoute(name, "", 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
            }
        });

        ImageView cancelButton = (ImageView)view.findViewById(R.id.btn_cancel);
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

    ///////// Test ///////////////////////////////////////////////////////////////////////////////
    private void uploadRealTimeRoute(RPoint rPoint, boolean pulse) throws JSONException {

        routeID = EasyPreference.with(getApplicationContext(), "backup").getLong("route_id", 0);
        assignID = EasyPreference.with(getApplicationContext(), "backup").getLong("assign_id", 0);
        totalDistance = EasyPreference.with(getApplicationContext(), "backup").getFloat("totalDistance", 0);
        speed = EasyPreference.with(getApplicationContext(), "backup").getFloat("speed", 0);
        duration = EasyPreference.with(getApplicationContext(), "backup").getLong("duration", 0);
        startedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("startedTime", 0);
        endedTime = EasyPreference.with(getApplicationContext(), "backup").getLong("endedTime", 0);

        AndroidNetworking.post(ReqConst.SERVER_URL + "upRTRoute")
                .addBodyParameter("route_id", String.valueOf(routeID))
                .addBodyParameter("assign_id", String.valueOf(assignID))
                .addBodyParameter("member_id", String.valueOf(Commons.thisUser.getIdx()))
                .addBodyParameter("name", "")
                .addBodyParameter("description", "")
                .addBodyParameter("start_time", String.valueOf(startedTime))
                .addBodyParameter("end_time", String.valueOf(endedTime))
                .addBodyParameter("duration", String.valueOf(duration))
                .addBodyParameter("speed", String.valueOf(speed))
                .addBodyParameter("distance", String.valueOf(totalDistance))
                .addBodyParameter("pulse", pulse ? "1" : "0")
                .addBodyParameter("status", "report")

                .addBodyParameter("lat", String.valueOf(rPoint.getLat()))
                .addBodyParameter("lng", String.valueOf(rPoint.getLng()))
                .addBodyParameter("comment", rPoint.getComment())
                .addBodyParameter("color", rPoint.getColor())
                .addBodyParameter("tm", rPoint.getTime())

                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @RequiresApi(api = Build.VERSION_CODES.P)
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
                        Log.d("XXXXXXXXXXX RT ===>", response.toString());
                        Log.d("Data Usage===>", android.net.TrafficStats.getMobileRxBytes()+"Bytes");
                        try {
                            String result = response.getString("result_code");
                            if(result.equals("0")){
                                Commons.routeTraces111.clear();
                                clearLocationPoints(getString(R.string.location_points));
                                EasyPreference.with(getApplicationContext()).addInt("network_delay", 0).save();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        Log.d("ERROR!!!", error.getLocalizedMessage());
                    }
                });

    }

    public void WriteData(String jsonStr){
        final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        // Make sure the path directory exists.
        if (!path.exists()) {
            path.mkdirs();
        } // Make sure the path directory exists.
        final File file = new File(path, "traces1.txt");
        // Save your stream, don't forget to flush() it before closing it.
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(jsonStr);
            myOutWriter.close();
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", "File write failed: " + e.getMessage());
        }
    }


    class DurTime {
        int hours = 0;
        int minutes = 0;

        public DurTime(int hours, int minutes) {
            this.hours = hours;
            this.minutes = minutes;
        }

        public int getHours() {
            return hours;
        }

        public int getMinutes() {
            return minutes;
        }
    }


}










































