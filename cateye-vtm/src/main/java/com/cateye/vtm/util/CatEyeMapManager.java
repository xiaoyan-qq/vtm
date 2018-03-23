package com.cateye.vtm.util;

import android.content.Context;

import org.oscim.android.MapView;
import org.oscim.map.Map;

/**
 * Created by xiaoxiao on 2018/3/22.
 * 注意：此方法为单例模式，如果需要调用，必须首先调用其init方法对其进行初始化操作
 */

public class CatEyeMapManager {
    private static CatEyeMapManager instance;

    private Context mContext;
    private static MapView mapView;
    private Map catEyeMap;

    public static CatEyeMapManager getInstance(Context mContext) {
        if (instance == null) {
            instance = new CatEyeMapManager(mContext);
        }
        return instance;
    }

    public void init(MapView mapView) {
        this.mapView = mapView;
        this.catEyeMap = mapView.map();
    }

    protected CatEyeMapManager(Context mContext) {
        this.mContext = mContext;
    }

    public static MapView getMapView() {
        return mapView;
    }

    public Map getCatEyeMap() {
        return catEyeMap;
    }
}
