package com.cateye.android.entity;


import com.alibaba.fastjson.JSONObject;

import java.util.Vector;

/**
 * Created by xiaoxiao on 2018/12/12.
 */

public class FlightParameter {
    private DigitalCameraInfo CameraInfo;
    private double AverageElevation;  //摄区地面平均高程,m,in WGS84
    private Vector<Double> FightHeight_Vec; //摄区航高, m,in WGS84

    private double GuidanceEntrancePointsDistance;  // 引导点,进入点距离,m
    private double overlap;                         // (0,1) 航向重叠率
    private double overlap_crossStrip;              // 旁向重叠度

    private int RedudantBaselines;         // 冗余基线

    private Vector<JSONObject> FightRegion; // 摄区, 面状或者线状

    private Airport airport;// 机场中心,in WGS84

    public DigitalCameraInfo getCameraInfo() {
        return CameraInfo;
    }

    public void setCameraInfo(DigitalCameraInfo cameraInfo) {
        CameraInfo = cameraInfo;
    }

    public double getAverageElevation() {
        return AverageElevation;
    }

    public void setAverageElevation(double averageElevation) {
        AverageElevation = averageElevation;
    }


    public Vector<Double> getFightHeight_Vec() {
        return FightHeight_Vec;
    }

    public void setFightHeight_Vec(Vector<Double> fightHeight_Vec) {
        FightHeight_Vec = fightHeight_Vec;
    }

    public double getGuidanceEntrancePointsDistance() {
        return GuidanceEntrancePointsDistance;
    }

    public void setGuidanceEntrancePointsDistance(double guidanceEntrancePointsDistance) {
        GuidanceEntrancePointsDistance = guidanceEntrancePointsDistance;
    }

    public double getOverlap() {
        return overlap;
    }

    public void setOverlap(double overlap) {
        this.overlap = overlap;
    }

    public double getOverlap_crossStrip() {
        return overlap_crossStrip;
    }

    public void setOverlap_crossStrip(double overlap_crossStrip) {
        this.overlap_crossStrip = overlap_crossStrip;
    }

    public int getRedudantBaselines() {
        return RedudantBaselines;
    }

    public void setRedudantBaselines(int redudantBaselines) {
        RedudantBaselines = redudantBaselines;
    }

    public Vector<JSONObject> getFightRegion() {
        return FightRegion;
    }

    public void setFightRegion(Vector<JSONObject> fightRegion) {
        FightRegion = fightRegion;
    }

    public Airport getAirport() {
        return airport;
    }

    public void setAirport(Airport airport) {
        this.airport = airport;
    }
}
