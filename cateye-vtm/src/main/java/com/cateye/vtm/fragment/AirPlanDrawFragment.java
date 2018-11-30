package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.CatEyeMapManager;
import com.cateye.vtm.util.SystemConstant;
import com.vondear.rxtool.view.RxToast;
import com.vtm.library.layers.MultiPolygonLayer;
import com.vtm.library.tools.OverlayerManager;

import org.oscim.backend.canvas.Color;
import org.oscim.map.Map;

/**
 * Created by xiaoxiao on 2018/8/31.
 */

public class AirPlanDrawFragment extends BaseDrawFragment {
    private Map mMap;

    private ImageView img_airplan_draw/*绘制按钮*/, img_airplan_previous/*上一笔*/, img_airplan_clear/*清空*/;
    protected MapEventsReceiver mapEventsReceiver;//用户操作的回调
    private MultiPolygonLayer multiPolygonLayer;//用于展示用户绘制的polygon

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMap = CatEyeMapManager.getMapView().map();
    }

    public static BaseFragment newInstance(Bundle bundle) {
        AirPlanDrawFragment airPlanDrawFragment = new AirPlanDrawFragment();
        airPlanDrawFragment.setArguments(bundle);
        return airPlanDrawFragment;
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_layer_airplan_draw;
    }

    @Override
    public void initView(View rootView) {
        img_airplan_draw = rootView.findViewById(R.id.img_air_plan_draw);
        img_airplan_previous = rootView.findViewById(R.id.img_air_plan_previous);
        img_airplan_clear = rootView.findViewById(R.id.img_air_plan_clear);

        clearDrawLayers();
        img_airplan_clear.setEnabled(true);
        img_airplan_previous.setEnabled(true);

        img_airplan_draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!view.isSelected()) {
                    //开始绘制
                    view.setSelected(true);
                    currentDrawState = DRAW_STATE.DRAW_POLYGON;
                    //初始化绘制图层
                    initDrawLayers();

                } else {
                    completeDrawAirPlan();
                }
            }
        });


        img_airplan_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (polygonOverlay==null){
                    RxToast.warning("绘制状态下才可以操作上一笔");
                }
                if (currentDrawState != DRAW_STATE.DRAW_NONE) {
                    if (markerLayer.getItemList() == null || markerLayer.getItemList().isEmpty()) {
                        RxToast.info("没有需要清除的点!", Toast.LENGTH_SHORT);
                        return;
                    }
                    if (markerLayer.getItemList() != null && !markerLayer.getItemList().isEmpty()) {
                        markerLayer.removeItem(markerLayer.getItemList().size() - 1);
//                        markerLayer.map().updateMap(true);//重绘
                        markerLayer.update();
                    }
                    if (currentDrawState == DRAW_STATE.DRAW_LINE) {//绘制线
                        if (polylineOverlay.getPoints() != null && !polylineOverlay.getPoints().isEmpty()) {
                            polylineOverlay.getPoints().remove(polylineOverlay.getPoints().size() - 1);
                            redrawPolyline(polylineOverlay);
                        }
                    } else if (currentDrawState == DRAW_STATE.DRAW_POLYGON) {//绘制面
                        if (polygonOverlay.getPoints() != null && !polygonOverlay.getPoints().isEmpty()) {
                            polygonOverlay.getPoints().remove(polygonOverlay.getPoints().size() - 1);
                            redrawPolygon(polygonOverlay);
                        }
                    }
                }
            }
        });

        img_airplan_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (polygonOverlay==null){
                    RxToast.warning("绘制状态下才可以操作清空");
                }
                if (markerLayer.getItemList() == null || markerLayer.getItemList().isEmpty()) {
                    RxToast.info("没有需要清除的点!", Toast.LENGTH_SHORT);
                    return;
                }
                if (markerLayer.getItemList() != null && !markerLayer.getItemList().isEmpty()) {
                    markerLayer.removeAllItems();
                    markerLayer.map().updateMap(true);//重绘
                }
                if (currentDrawState == DRAW_STATE.DRAW_LINE) {
                    polylineOverlay.getPoints().clear();
                    redrawPolyline(polylineOverlay);
                } else if (currentDrawState == DRAW_STATE.DRAW_POLYGON) {
                    polygonOverlay.getPoints().clear();
                    redrawPolygon(polygonOverlay);
                }
            }
        });

        //添加一个操作图层，监听用户在地图上的点击事件
        mapEventsReceiver = new MapEventsReceiver(CatEyeMapManager.getInstance(getActivity()).getCatEyeMap());
        CatEyeMapManager.getInstance(getActivity()).getCatEyeMap().layers().add(mapEventsReceiver, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);

        //如果当前地图不存在multiPolygon的图层，则自动生成添加到地图上
        if (OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.AIR_PLAN_MULTI_POLYGON_DRAW) == null) {
            //向主界面添加polygon显示的overlayer
            int c = Color.GREEN;
            org.oscim.layers.vector.geometries.Style polygonStyle = org.oscim.layers.vector.geometries.Style.builder()
                    .stippleColor(c)
                    .stipple(24)
                    .stippleWidth(1)
                    .strokeWidth(1)
                    .strokeColor(c).fillColor(c).fillAlpha(0.35f)
                    .fixed(true)
                    .randomOffset(false)
                    .build();
            multiPolygonLayer = new MultiPolygonLayer(mMap, polygonStyle, SystemConstant.AIR_PLAN_MULTI_POLYGON_DRAW);
            mMap.layers().add(multiPolygonLayer, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);
        }
    }

    public void completeDrawAirPlan(){
        if (img_airplan_draw!=null){
            img_airplan_draw.setSelected(false);
        }
        currentDrawState = DRAW_STATE.DRAW_NONE;
        if (polygonOverlay!=null){
            if (polygonOverlay.getPoints()==null||polygonOverlay.getPoints().isEmpty()){
                RxToast.warning("没有绘制任何内容！");
            }else if (polygonOverlay.getPoints().size()>=3){
                //绘制结束，将绘制的数据添加到airplan的图层内
                multiPolygonLayer.addPolygonDrawable(polygonOverlay.getPoints());
            }else {
                RxToast.warning("绘制的点无法组成面！");
            }
        }
        //复制点位到展示图层，则清除绘制面的所有数据
        clearDrawLayers();
    }
}
