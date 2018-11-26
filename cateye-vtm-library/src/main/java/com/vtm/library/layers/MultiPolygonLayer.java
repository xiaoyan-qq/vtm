package com.vtm.library.layers;

import org.oscim.core.GeoPoint;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.vector.geometries.PolygonDrawable;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;
import org.oscim.utils.geom.GeometryUtils;

import java.util.List;

/**
 * Created by xiaoxiao on 2018/3/26.
 */

public class MultiPolygonLayer extends PathLayer {
    private List<PolygonDrawable> polygonDrawableList;

    public MultiPolygonLayer(Map map, Style style) {
        super(map, style);
        mStyle = style;
    }

    public MultiPolygonLayer(Map map, int lineColor, float lineWidth) {
        this(map, Style.builder()
                .fixed(true)
                .strokeColor(lineColor)
                .strokeWidth(lineWidth)
                .build());
    }

    public MultiPolygonLayer(Map map, int lineColor) {
        this(map, lineColor, 2);
    }

    /**
     * 设置polygon的点位
     */
    public void setPolygonList(List<List<GeoPoint>> pointListList, boolean isClose) {
        if (pointListList == null || pointListList.isEmpty()) {
            return;
        }
        for (List<GeoPoint> pointList : pointListList) {
            if (pointList == null || pointList.size() < 3) {
                return;
            }
            if (isClose && pointList != null && pointList.get(0) != pointList.get(pointList.size() - 1)) {
                pointList.add(pointList.get(0));
            }
            synchronized (this) {
                PolygonDrawable polygonDrawable = new PolygonDrawable(pointList, mStyle);
                add(polygonDrawable);
                polygonDrawableList.add(polygonDrawable);
            }
            mWorker.submit(0);
        }
    }

    /**
     * 移除正在绘制的polygon的图形
     */
    public void removePolygonDrawable(int i) {
        if (polygonDrawableList != null && polygonDrawableList.size() > i) {
            remove(polygonDrawableList.get(i));
        }
    }

    public void removePolygonDrawable(List<GeoPoint> geoPointList){
        if (polygonDrawableList != null && !polygonDrawableList.isEmpty()) {
            GeometryUtils.
            for (PolygonDrawable polygonDrawable:polygonDrawableList){
                re
            }
        }
    }
}
