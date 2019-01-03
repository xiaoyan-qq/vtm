package com.cateye.android.entity;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by xiaoxiao on 2018/12/3.
 */

@Table(name = "AirPlanDBEntity")
public class AirPlanDBEntity {
    @Column(name = "_id", isId = true, autoGen = true)
    private int id;//id
    @Column(name = "name")
    private String name;//航区名称，方便用户再次查找
    @Column(name = "lastUpdate")
    private String lastUpdate;//最后一次更新时间
    @Column(name = "altitude")
    private int altitude;//海拔
    @Column(name = "descriptor")
    private String descriptor;//描述
    @Column(name = "alt_ai")
    private int alt_ai;//保留字段，智能分区高度
    @Column(name = "geometry")
    private String geometry;//geometry

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

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }
}
