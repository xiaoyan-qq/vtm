package com.cateye.vtm.fragment.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.util.CatEyeMapManager;
import com.vtm.library.layers.PolygonLayer;

import org.oscim.backend.canvas.Bitmap;
import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.vector.PathLayer;
import org.oscim.layers.vector.geometries.Style;
import org.oscim.map.Map;

import java.io.Serializable;
import java.util.List;

import static org.oscim.android.canvas.AndroidGraphics.drawableToBitmap;

/**
 * Created by xiaoxiao on 2018/5/24.
 * 地图绘制形状的父类，需要绘制点线面时，可继承该fragment
 */

public class BaseDrawFragment extends BaseFragment {

    protected DRAW_STATE currentDrawState = DRAW_STATE.DRAW_NONE;

    //overLayer图层
    protected ItemizedLayer<MarkerItem> markerLayer;
    protected PathLayer polylineOverlay;
    protected PolygonLayer polygonOverlay;
    protected MarkerSymbol defaultMarkerSymbol;

    @Override
    public int getFragmentLayoutId() {
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //打开该fragment，则自动向地图中添加marker的overlay
        Bitmap bitmapPoi = drawableToBitmap(getResources().getDrawable(R.drawable.marker_poi));
        defaultMarkerSymbol = new MarkerSymbol(bitmapPoi, MarkerSymbol.HotspotPlace.CENTER);
        markerLayer = new ItemizedLayer<MarkerItem>(CatEyeMapManager.getInstance(getActivity()).getCatEyeMap(), defaultMarkerSymbol);
        CatEyeMapManager.getInstance(getActivity()).getCatEyeMap().layers().add(markerLayer, MainActivity.LAYER_GROUP_ENUM.GROUP_BUILDING.ordinal());

        //自动添加pathLayer
        int c = Color.RED;
        Style lineStyle = Style.builder()
                .stippleColor(c)
                .stipple(24)
                .stippleWidth(1)
                .strokeWidth(2)
                .strokeColor(c)
                .fixed(true)
                .randomOffset(false)
                .build();
        polylineOverlay = new PathLayer(CatEyeMapManager.getInstance(getActivity()).getCatEyeMap(), lineStyle);
        CatEyeMapManager.getInstance(getActivity()).getCatEyeMap().layers().add(polylineOverlay, MainActivity.LAYER_GROUP_ENUM.GROUP_BUILDING.ordinal());

        Style polygonStyle = Style.builder()
                .stippleColor(c)
                .stipple(24)
                .stippleWidth(1)
                .strokeWidth(2)
                .strokeColor(c).fillColor(c).fillAlpha(0.5f)
                .fixed(true)
                .randomOffset(false)
                .build();
        polygonOverlay = new PolygonLayer(CatEyeMapManager.getInstance(getActivity()).getCatEyeMap(), polygonStyle);
        CatEyeMapManager.getInstance(getActivity()).getCatEyeMap().layers().add(polygonOverlay, MainActivity.LAYER_GROUP_ENUM.GROUP_BUILDING.ordinal());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void initView(View rootView) {
    }

    /**
     * Author : xiaoxiao
     * Describe : 获取当前的绘制状态
     * param :
     * return : 返回绘制状态的枚举类型，如果为NONE则当前没有进行绘制
     * Date : 2018/3/22
     */
    public DRAW_STATE getCurrentDrawState() {
        return currentDrawState;
    }

    public enum DRAW_STATE implements Serializable {
        DRAW_NONE("DRAW_NONE"), DRAW_POINT("DRAW_POINT"), DRAW_LINE("DRAW_LINE"), DRAW_POLYGON("DRAW_POLYGON");

        DRAW_STATE(String name) {
            this.name = name;
        }

        private String name;

        public String getName() {
            return name;
        }
    }

    //根据polyline的点位自动绘制线
    protected void redrawPolyline(PathLayer polylineOverlay) {
        if (polylineOverlay.getPoints() != null && polylineOverlay.getPoints().size() > 1) {
            polylineOverlay.setLineString(getPointArray(polylineOverlay.getPoints()));
        } else if (polylineOverlay.getPoints() != null && polylineOverlay.getPoints().size() == 1) {
            GeoPoint firstPoint = polylineOverlay.getPoints().get(0);
            polylineOverlay.clearPath();
            polylineOverlay.addPoint(firstPoint);
        } else {
            polylineOverlay.clearPath();
        }
        polylineOverlay.update();
    }

    //根据polygon的点位自动绘制线或者面
    protected void redrawPolygon(PolygonLayer polygonOverlay) {
        if (polygonOverlay.getPoints() != null) {
            if (polygonOverlay.getPoints().size() > 2) {
                polygonOverlay.setPolygonString(polygonOverlay.getPoints(), true);
            } else if (polygonOverlay.getPoints().size() == 2) {
                polygonOverlay.removeCurrentPolygonDrawable();
                polygonOverlay.setLineString(getPointArray(polygonOverlay.getPoints()));
            } else if (polygonOverlay.getPoints() != null && polygonOverlay.getPoints().size() == 1) {
                polygonOverlay.removeCurrentPolylineDrawable();
                polygonOverlay.update();
            } else {
                polygonOverlay.clearPath();
            }
            polygonOverlay.update();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    protected double[] getPointArray(List<GeoPoint> geoPointList) {
        if (geoPointList != null && !geoPointList.isEmpty()) {
            double[] lonLatArray = new double[geoPointList.size() * 2];
            for (int i = 0; i < geoPointList.size(); i++) {
                lonLatArray[i * 2] = geoPointList.get(i).getLongitude();
                lonLatArray[i * 2 + 1] = geoPointList.get(i).getLatitude();
            }
            return lonLatArray;
        }
        return null;
    }

    /**
     * @param :
     * @return :
     * @method : obtainNewMarker
     * @Author : xiaoxiao
     * @Describe : 生成一个新的marker
     * @Date : 2018/5/28
     */
    protected MarkerItem obtainMarker(Object uid, String title, String des, GeoPoint geoPoint) {
        MarkerItem markerItem = new MarkerItem(uid, title, des, geoPoint);
        if (defaultMarkerSymbol != null) {
            markerItem.setMarker(defaultMarkerSymbol);
        }
        return markerItem;
    }

    protected void setDefaultMarkerSymbol(MarkerSymbol defaultMarkerSymbol) {
        this.defaultMarkerSymbol = defaultMarkerSymbol;
    }

    /**
     * @param :
     * @return :
     * @method : clearMapOverlayer
     * @Author : xiaoxiao
     * @Describe : 清空绘制的点线面
     * @Date : 2018/5/24
     */
    protected void clearMapOverlayer() {
        Map map = null;
        if (markerLayer != null && markerLayer.getItemList() != null) {
            map = markerLayer.map();
            markerLayer.getItemList().clear();
            markerLayer.update();
            markerLayer.map().layers().remove(markerLayer);
        }
        if (polylineOverlay != null && polylineOverlay.getPoints() != null) {
            map = polylineOverlay.map();
            polylineOverlay.getPoints().clear();
            polylineOverlay.update();
            markerLayer.map().layers().remove(polylineOverlay);
        }
        if (polygonOverlay != null && polygonOverlay.getPoints() != null) {
            map = polygonOverlay.map();
            polygonOverlay.getPoints().clear();
            polygonOverlay.update();
            markerLayer.map().layers().remove(polygonOverlay);
        }
        if (map != null) {
            markerLayer.map().updateMap(true);
        }
    }
}
