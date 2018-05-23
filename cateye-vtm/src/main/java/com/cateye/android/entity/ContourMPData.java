package com.cateye.android.entity;

import java.io.Serializable;

/**
 * Created by xiaoxiao on 2018/5/23.
 */

public class ContourMPData implements Serializable{
    private double mLatitude;
    private double mLongitude;
    private float mHeight;

    public double getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public float getmHeight() {
        return mHeight;
    }

    public void setmHeight(float mHeight) {
        this.mHeight = mHeight;
    }
}
