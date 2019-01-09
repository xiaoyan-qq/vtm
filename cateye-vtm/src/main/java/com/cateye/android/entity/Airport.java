package com.cateye.android.entity;


import com.alibaba.fastjson.JSONObject;

/**
 * Created by xiaoxiao on 2018/12/12.
 */

public class Airport {
    private JSONObject geoJson;//机场的位置
    private double altitude;//机场的高度

    public JSONObject getGeoJson() {
        return geoJson;
    }

    public void setGeoJson(JSONObject geoJson) {
        this.geoJson = geoJson;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
}
