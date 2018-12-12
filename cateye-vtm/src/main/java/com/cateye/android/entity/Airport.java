package com.cateye.android.entity;

/**
 * Created by xiaoxiao on 2018/12/12.
 */

public class Airport {
    private String geoJson;//机场的位置
    private double altitude;//机场的高度

    public String getGeoJson() {
        return geoJson;
    }

    public void setGeoJson(String geoJson) {
        this.geoJson = geoJson;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
}
