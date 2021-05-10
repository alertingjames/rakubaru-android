package com.app.rakubaru.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Area {
    int idx = 0;
    int memberId = 0;
    String areaName = "";
    String title = "";
    String distribution = "";
    long startTime = 0;
    long endTime = 0;
    int copies = 0;
    float unitPrice = 0;
    int allowance = 0;
    float amount = 0;
    float distance = 0;
    float myDistance = 0;
    String status = "";

    ArrayList<LatLng> coords = new ArrayList<>();
    ArrayList<Subloc> sublocs = new ArrayList<>();

    public Area(){}

    public ArrayList<LatLng> getCoords() {
        return coords;
    }

    public void setCoords(ArrayList<LatLng> coords) {
        this.coords = coords;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getAllowance() {
        return allowance;
    }

    public void setAllowance(int allowance) {
        this.allowance = allowance;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getMyDistance() {
        return myDistance;
    }

    public void setMyDistance(float myDistance) {
        this.myDistance = myDistance;
    }

    public ArrayList<Subloc> getSublocs() {
        return sublocs;
    }

    public void setSublocs(ArrayList<Subloc> sublocs) {
        this.sublocs = sublocs;
    }
}
