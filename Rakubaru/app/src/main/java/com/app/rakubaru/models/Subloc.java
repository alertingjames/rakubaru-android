package com.app.rakubaru.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Subloc {
    int idx = 0;
    int areaId = 0;
    String locationName = "";
    String color = "";
    LatLng latLng = null;
    ArrayList<LatLng> coordnates = new ArrayList<>();

    public ArrayList<LatLng> getCoordnates() {
        return coordnates;
    }

    public void setCoordnates(ArrayList<LatLng> coordnates) {
        this.coordnates = coordnates;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
