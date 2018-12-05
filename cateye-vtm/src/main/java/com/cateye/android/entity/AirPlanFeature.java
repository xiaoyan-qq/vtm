package com.cateye.android.entity;

import com.cocoahero.android.geojson.GeoJSONObject;

import java.io.Serializable;

/**
 * Created by xiaoxiao on 2018/12/3.
 */

public class AirPlanFeature implements Serializable {
    private String type="Feature";
    private AirPlanProperties properties;
    private GeoJSONObject geometry;

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

    public GeoJSONObject getGeometry() {
        return geometry;
    }

    public void setGeometry(GeoJSONObject geometry) {
        this.geometry = geometry;
    }
}
