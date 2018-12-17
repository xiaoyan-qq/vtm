package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.canyinghao.candialog.CanDialog;
import com.canyinghao.candialog.CanDialogInterface;
import com.cateye.android.entity.AirPlanEntity;
import com.cateye.android.entity.AirPlanFeature;
import com.cateye.android.entity.AirPlanProperties;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.CatEyeMapManager;
import com.cateye.vtm.util.SystemConstant;
import com.github.lazylibrary.util.StringUtils;
import com.litesuits.common.assist.Check;
import com.litesuits.common.io.IOUtils;
import com.litesuits.common.io.StringCodingUtils;
import com.vondear.rxtool.RxTimeTool;
import com.vondear.rxtool.view.RxToast;
import com.vtm.library.layers.MultiPolygonLayer;
import com.vtm.library.tools.GeometryTools;
import com.vtm.library.tools.OverlayerManager;

import org.oscim.backend.canvas.Color;
import org.oscim.map.Map;
import org.xutils.DbManager;
import org.xutils.x;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                if (polygonOverlay == null) {
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
                if (polygonOverlay == null) {
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
        multiPolygonLayer = (MultiPolygonLayer) OverlayerManager.getInstance(mMap).getLayerByName(SystemConstant.AIR_PLAN_MULTI_POLYGON_DRAW);
        if (multiPolygonLayer == null) {
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

    public void completeDrawAirPlan() {
        if (img_airplan_draw != null) {
            img_airplan_draw.setSelected(false);
        }
        currentDrawState = DRAW_STATE.DRAW_NONE;
        if (polygonOverlay != null) {
            if (polygonOverlay.getPoints() == null || polygonOverlay.getPoints().isEmpty()) {
                RxToast.warning("没有绘制任何内容！");
            } else if (polygonOverlay.getPoints().size() >= 3) {
                //绘制结束，提示用户设置该polygon的参数，弹出对话框
                //弹出参数设置对话框
                final View airPlanRootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_air_plan_set_param, null);
                new CanDialog.Builder(getActivity()).setView(airPlanRootView).setNeutralButton("取消", true, null).setPositiveButton("确定", true, new CanDialogInterface.OnClickListener() {
                    @Override
                    public void onClick(CanDialog dialog, int checkItem, CharSequence text, boolean[] checkItems) {
                        //用户点击确定，首先检查用户输入的内容是否合规
                        BootstrapEditText edt_name = airPlanRootView.findViewById(R.id.edt_air_plan_name);//名称
                        BootstrapEditText edt_altitude = airPlanRootView.findViewById(R.id.edt_air_plan_altitude);//海拔
                        BootstrapEditText edt_seqnum = airPlanRootView.findViewById(R.id.edt_air_plan_seqnum);//顺序
                        BootstrapEditText edt_describe = airPlanRootView.findViewById(R.id.edt_air_plan_describe);//描述

                        String altitude = edt_altitude.getText().toString();
                        if (Check.isEmpty(altitude)) {
                            RxToast.info("海拔数据不能为空");
                            return;
                        }
                        String currentTime = RxTimeTool.getCurTimeString();

                        String name = edt_name.getText().toString();
                        if (Check.isEmpty(name)) {
                            name = currentTime;
                        }

                        //自动保存用户输入的参数数据到指定的文件夹中
                        AirPlanEntity airPlanEntity = new AirPlanEntity();
                        airPlanEntity.setName(name);
                        List<AirPlanFeature> airPlanFeatureList = new ArrayList<>();
                        airPlanEntity.setFeatures(airPlanFeatureList);
                        AirPlanFeature feature = new AirPlanFeature();
                        AirPlanProperties properties = new AirPlanProperties();
                        properties.setId(UUID.randomUUID());
                        properties.setName(name + "_" + i);
                        properties.setAltitude(Integer.parseInt(altitude));
                        properties.setDescriptor(edt_describe.getText().toString());
                        properties.setSeqnum(i + 1);
                        properties.setAlt_ai(0);
                        feature.setProperties(properties);
                        feature.setGeometry(GeometryTools.createPolygon(polygonOverlay.getPoints()));

                        airPlanFeatureList.add(feature);

                        DbManager db= x.getDb();
                        db.execNonQuery();
                        //保存数据到指定目录
                        File textFile = new File(SystemConstant.AIR_PLAN_PATH + File.separator + name + ".json");
                        if (!textFile.getParentFile().exists()) {
                            textFile.getParentFile().mkdirs();
                        }
                        try {

                            IOUtils.write(JSONObject.toJSONString(airPlanEntity), new FileOutputStream(textFile), "UTF-8");
                        } catch (Exception ee) {
                            return;
                        }
                    }
                }).show();

                //绘制结束，将绘制的数据添加到airplan的图层内
                multiPolygonLayer.addPolygonDrawable(polygonOverlay.getPoints());
            } else {
                RxToast.warning("绘制的点无法组成面！");
            }
        }
        //复制点位到展示图层，则清除绘制面的所有数据
        clearDrawLayers();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapEventsReceiver != null) {
            mMap.layers().remove(mapEventsReceiver);
        }
    }
}
