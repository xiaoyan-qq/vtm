package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.util.CatEyeMapManager;
import com.cateye.vtm.util.SystemConstant;
import com.vondear.rxtools.view.RxToast;
import com.vtm.library.layers.PolygonLayer;

import org.greenrobot.eventbus.EventBus;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
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
 * Created by xiaoxiao on 2018/3/21.
 */
//@Puppet
public class DrawPointLinePolygonFragment extends BaseFragment {
    private TextView tv_last, tv_clear, tv_finish;
    private DRAW_STATE currentDrawState = DRAW_STATE.DRAW_NONE;

    //overLayer图层
    private ItemizedLayer<MarkerItem> markerLayer;
    private PathLayer polylineOverlay;
    private PolygonLayer polygonOverlay;
    private MarkerSymbol pointMarker;
    private MapEventsReceiver mapEventsReceiver;

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_draw_point_line_polygon;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            //获取当前的绘制状态
            currentDrawState = (DRAW_STATE) savedInstanceState.getSerializable(DRAW_STATE.class.getSimpleName());
        }
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            currentDrawState = (DRAW_STATE) bundle.getSerializable(DRAW_STATE.class.getSimpleName());
        }
    }

    @Override
    public void onNewBundle(Bundle args) {
        super.onNewBundle(args);
        if (args != null) {
            //获取当前的绘制状态
            currentDrawState = (DRAW_STATE) args.getSerializable(DRAW_STATE.class.getSimpleName());
        }
    }

    @Override
    public void initView(View rootView) {
        tv_last = rootView.findViewById(R.id.tv_draw_last);
        tv_clear = rootView.findViewById(R.id.tv_draw_clear);
        tv_finish = rootView.findViewById(R.id.tv_draw_finish);

        if (currentDrawState == DRAW_STATE.DRAW_POINT) {
            tv_last.setVisibility(View.GONE);
            tv_clear.setVisibility(View.GONE);
        }

        tv_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDrawState != DRAW_STATE.DRAW_NONE) {
                    if (markerLayer.getItemList() == null || markerLayer.getItemList().isEmpty()) {
                        RxToast.info("没有需要清除的点!", Toast.LENGTH_SHORT);
                        return;
                    }
                    if (markerLayer.getItemList() != null && !markerLayer.getItemList().isEmpty()) {
                        markerLayer.removeItem(markerLayer.getItemList().size() - 1);
                        markerLayer.map().updateMap(true);//重绘
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

        tv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        tv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentDrawState==DRAW_STATE.DRAW_LINE&&polylineOverlay.getPoints().size()<2){
                    markerLayer.removeAllItems();
                    markerLayer.map().updateMap(true);
                    polylineOverlay.clearPath();
                    redrawPolyline(polylineOverlay);
                }
                if (currentDrawState==DRAW_STATE.DRAW_POLYGON&&polylineOverlay.getPoints().size()<3){
                    markerLayer.removeAllItems();
                    markerLayer.map().updateMap(true);
                    polygonOverlay.getPoints().clear();
                    redrawPolygon(polygonOverlay);
                }
            }
        });

        //添加一个操作图层，监听用户在地图上的点击事件
        mapEventsReceiver = new MapEventsReceiver(CatEyeMapManager.getInstance(getActivity()).getCatEyeMap());
        CatEyeMapManager.getInstance(getActivity()).getCatEyeMap().layers().add(mapEventsReceiver, MainActivity.LAYER_GROUP_ENUM.GROUP_OPERTOR.ordinal());
        //打开该fragment，则自动向地图中添加marker的overlay
        Bitmap bitmapPoi = drawableToBitmap(getResources().getDrawable(R.drawable.marker_poi));
        pointMarker = new MarkerSymbol(bitmapPoi, MarkerSymbol.HotspotPlace.CENTER);
        markerLayer = new ItemizedLayer<MarkerItem>(CatEyeMapManager.getInstance(getActivity()).getCatEyeMap(), pointMarker);
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

    }

    public static BaseFragment newInstance(Bundle bundle) {
        DrawPointLinePolygonFragment drawPointLinePolygonFragment = new DrawPointLinePolygonFragment();
        drawPointLinePolygonFragment.setArguments(bundle);
        return drawPointLinePolygonFragment;
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


    private class MapEventsReceiver extends Layer implements GestureListener {

        MapEventsReceiver(Map map) {
            super(map);
        }

        @Override
        public boolean onGesture(Gesture g, MotionEvent e) {
            if (g instanceof Gesture.Press) {
                GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
                DRAW_STATE currentState = getCurrentDrawState();
                if (currentState != DRAW_STATE.DRAW_NONE) {//如果当前是绘制模式，则自动添加marker
                    markerLayer.addItem(new MarkerItem("", "", p));
                    markerLayer.map().updateMap(true);//重绘
                    //如果当前是绘制线模式，则增加pathLayer
                    if (currentState == DRAW_STATE.DRAW_LINE) {
                        polylineOverlay.addPoint(p);
                        redrawPolyline(polylineOverlay);
                    }
                    if (currentState == DRAW_STATE.DRAW_POLYGON) {
                        polygonOverlay.addPoint(p);
                        redrawPolygon(polygonOverlay);
                    }
                }
                Toast.makeText(getActivity(), "Map tap\n" + p, Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        }
    }

    //根据polyline的点位自动绘制线
    private void redrawPolyline(PathLayer polylineOverlay) {
        if (polylineOverlay.getPoints() != null && polylineOverlay.getPoints().size() > 1) {
            polylineOverlay.setLineString(getPointArray(polylineOverlay.getPoints()));
        }else if (polylineOverlay.getPoints() != null && polylineOverlay.getPoints().size() == 1){
            GeoPoint firstPoint=polylineOverlay.getPoints().get(0);
            polylineOverlay.clearPath();
            polylineOverlay.addPoint(firstPoint);
        }else {
            polylineOverlay.clearPath();
        }
    }

    //根据polygon的点位自动绘制线或者面
    private void redrawPolygon(PolygonLayer polygonOverlay) {
        if (polygonOverlay.getPoints() != null) {
            if (polygonOverlay.getPoints().size() > 2) {
                polygonOverlay.setPolygonString(polygonOverlay.getPoints(), true);
            } else if (polygonOverlay.getPoints().size() == 2) {
                polygonOverlay.removeCurrentPolygonDrawable();
                polygonOverlay.setLineString(getPointArray(polygonOverlay.getPoints()));
            }else if (polygonOverlay.getPoints() != null && polygonOverlay.getPoints().size() == 1){
                polygonOverlay.removeCurrentPolylineDrawable();
                polygonOverlay.update();
            }else {
                polygonOverlay.clearPath();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //当前界面被返回时，自动移除所有的overlayer
        if (mapEventsReceiver != null) {
            CatEyeMapManager.getInstance(getActivity()).getCatEyeMap().layers().remove(mapEventsReceiver);
        }
    }

    private double[] getPointArray(List<GeoPoint> geoPointList) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Message msg = new Message();
        msg.what = SystemConstant.MSG_WHAT_DRAW_POINT_LINE_POLYGON_DESTROY;
        EventBus.getDefault().post(msg);
    }
}
