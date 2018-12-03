package com.cateye.android.entity;

import com.vividsolutions.jts.geom.Geometry;

import java.io.Serializable;

/**
 * Created by xiaoxiao on 2018/12/3.
 */

public class AirPlanFeature implements Serializable {
    private String type="Feature";
    private AirPlanProperties properties;
    private Geometry geometry;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AirPlanProperties getProperties() {
        return properties;
    }

    public void setProperties(AirPlanProperties properties) {
        this.properties = properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
}
