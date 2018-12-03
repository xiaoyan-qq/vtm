package com.cateye.android.entity;

/**
 * Created by xiaoxiao on 2018/12/3.
 */

public class AirPlanProperties {
    private int id;
    private String name;
    private int altitude;
    private int seqnum;
    private String descriptor;
    private int alt_ai;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public int getSeqnum() {
        return seqnum;
    }

    public void setSeqnum(int seqnum) {
        this.seqnum = seqnum;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }

    public int getAlt_ai() {
        return alt_ai;
    }

    public void setAlt_ai(int alt_ai) {
        this.alt_ai = alt_ai;
    }
}
