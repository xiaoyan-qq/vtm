package com.cateye.android.entity;

import com.alibaba.fastjson.JSON;


import java.io.Serializable;
import java.util.Map;

/**
 * Created by xiaoxiao on 2018/12/3.
 */

public class AirPlanFeature implements Serializable {
    private String type = "Feature";
    private AirPlanProperties properties;
    private Map<String, Object> geometry;

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

    public Map<String, Object> getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = null;
        if (geometry != null) {
            this.geometry = JSON.parseObject(geometry, Map.class);
        }
    }

}
