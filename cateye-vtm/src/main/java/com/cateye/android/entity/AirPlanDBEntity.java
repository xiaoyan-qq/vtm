package com.cateye.android.entity;

/**
 * Created by xiaoxiao on 2018/12/3.
 */

public class AirPlanDBEntity {
    private String id;//id
    private String name;//航区名称，方便用户再次查找
    private String lastUpdate;//最后一次更新时间
    private int altitude;//海拔
    private String descriptor;//描述
    private int alt_ai;//保留字段，智能分区高度

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
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
