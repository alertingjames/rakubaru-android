package com.app.rakubaru.models;

import java.util.ArrayList;

public class Route {
    int idx = 0;
    int member_id = 0;
    int assign_id = 0;
    String name = "";
    String description = "";
    String start_time = "";
    String end_time = "";
    long duration = 0;
    double speed = 0.0d;
    double distance = 0;
    String status = "";
    String area_name = "";
    String assign_title = "";
//    ArrayList<RPoint> rpoints = new ArrayList<>();
//    ArrayList<RPoint> pins = new ArrayList<>();

    public Route() {}

    public String getArea_name() {
        return area_name;
    }

    public void setArea_name(String area_name) {
        this.area_name = area_name;
    }

    public String getAssign_title() {
        return assign_title;
    }

    public void setAssign_title(String assign_title) {
        this.assign_title = assign_title;
    }

    public int getAssign_id() {
        return assign_id;
    }

    public void setAssign_id(int assign_id) {
        this.assign_id = assign_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getMember_id() {
        return member_id;
    }

    public void setMember_id(int member_id) {
        this.member_id = member_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

//    public ArrayList<RPoint> getRPoints() {
//        return rpoints;
//    }
//
//    public void setRPoints(ArrayList<RPoint> RPoints) {
//        this.rpoints.clear();
//        this.rpoints.addAll(RPoints);
//    }
//
//    public ArrayList<RPoint> getPins() {
//        return pins;
//    }
//
//    public void setPins(ArrayList<RPoint> pins) {
//        this.pins.clear();
//        this.pins.addAll(pins);
//    }
}




























