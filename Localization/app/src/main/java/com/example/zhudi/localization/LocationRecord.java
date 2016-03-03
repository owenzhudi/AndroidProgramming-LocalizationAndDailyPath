package com.example.zhudi.localization;

import android.location.Location;

/**
 * Created by zhudi on 15/10/14.
 */
public class LocationRecord {
    private double latitude;
    private double longitude;
    private String updateTime;

    public LocationRecord(double latitude, double longitude, String time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.updateTime = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setUpdateTime(String time) {
        this.updateTime = time;
    }
}
