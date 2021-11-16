package com.app.rakubaru.commons;

import com.app.rakubaru.main.HomeActivity;
import com.app.rakubaru.models.Area;
import com.app.rakubaru.models.RPoint;
import com.app.rakubaru.models.Route;
import com.app.rakubaru.models.User;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class Commons {
    public static User thisUser = null;

    public static int curMapTypeIndex = 1;
    public static boolean mapCameraMoveF = false;
    public static GoogleMap googleMap = null;
    public static Marker marker = null;
    public static HomeActivity homeActivity = null;
    public static boolean autoReport = true;
    public static ArrayList<Route> routes = new ArrayList<>();
    public static Route route = null;

    public static ArrayList<RPoint> points = new ArrayList<>();
    public static ArrayList<RPoint> pins = new ArrayList<>();
    public static ArrayList<Marker> markers = new ArrayList<>();

    public static Area area = null;

    public static ArrayList<RPoint> traces = new ArrayList<>();
    public static ArrayList<RPoint> routeTraces111 = new ArrayList<>();
}
