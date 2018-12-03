package com.cateye.android.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.vividsolutions.jts.geom.Geometry;
import com.vtm.library.tools.GeometryTools;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Created by xiaoxiao on 2018/12/3.
 */

public class AirPlanFeature implements Serializable {
    private String type="Feature";
    private AirPlanProperties properties;
    private String geometry;

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

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

}
