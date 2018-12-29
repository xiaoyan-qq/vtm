package com.cateye.vtm.util;

import com.cateye.android.entity.AirPlanDBEntity;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vtm.library.layers.MultiPolygonLayer;
import com.vtm.library.tools.GeometryTools;

import org.oscim.layers.vector.geometries.PolygonDrawable;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/3/26.
 */

public class AirPlanMultiPolygonLayer extends MultiPolygonLayer {
    private List<PolygonDrawable> polygonDrawableList;
    private java.util.Map<String, AirPlanDBEntity> airPlanDBEntityMap;

    public AirPlanMultiPolygonLayer(Map map, Style style) {
        super(map, style);
        this.mStyle = style;
        this.polygonDrawableList = new ArrayList<>();
        this.airPlanDBEntityMap = new HashMap<>();
    }

    public AirPlanMultiPolygonLayer(Map map, Style style, String name) {
        this(map, style);
        this.mName = name;
    }

    public AirPlanMultiPolygonLayer(Map map, int lineColor, float lineWidth, int fillColor, float fillAlpha, String name) {
        this(map, Style.builder()
                .stippleColor(lineColor)
                .stipple(24)
                .stippleWidth(lineWidth)
                .strokeWidth(lineWidth)
                .strokeColor(lineColor).fillColor(fillColor).fillAlpha(fillAlpha)
                .fixed(true)
                .randomOffset(false)
                .build(), name);
    }

    public AirPlanMultiPolygonLayer(Map map, int lineColor, int fillColor, float fillAlpha, String name) {
        this(map, lineColor, 0.5f, fillColor, fillAlpha, name);
    }

    public boolean addPolygon(AirPlanDBEntity airPlanDBEntity) {
        if (airPlanDBEntityMap != null) {
            if (airPlanDBEntityMap.containsKey(airPlanDBEntity.getGeometry())) {
                return false;
            }
            if (airPlanDBEntity != null) {
                addPolygonDrawable((Polygon) GeometryTools.createGeometry(airPlanDBEntity.getGeometry()));
                airPlanDBEntityMap.put(airPlanDBEntity.getGeometry(), airPlanDBEntity);
                return true;
            }
        }
        return false;
    }

    @Override
    public void removePolygonDrawable(String polygonStr) {
        super.removePolygonDrawable(polygonStr);
        if (airPlanDBEntityMap != null && airPlanDBEntityMap.containsKey(polygonStr)) {
            airPlanDBEntityMap.remove(polygonStr);
        }
    }

    @Override
    public void removePolygonDrawable(Geometry polygon) {
        super.removePolygonDrawable(polygon);
        if (airPlanDBEntityMap != null && airPlanDBEntityMap.containsKey(polygon.toString())) {
            airPlanDBEntityMap.remove(polygon.toString());
        }
    }

    public java.util.Map<String, AirPlanDBEntity> getAirPlanDBEntityMap() {
        return airPlanDBEntityMap;
    }
}
