package com.cateye.android.entity;

import org.oscim.core.GeoPoint;

import java.io.Serializable;

/**
 * Created by xiaoxiao on 2018/5/23.
 */

public class ContourMPData implements Serializable {
    private GeoPoint geoPoint;
    private float mHeight;

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public float getmHeight() {
        return mHeight;
    }

    public void setmHeight(float mHeight) {
        this.mHeight = mHeight;
    }
}
