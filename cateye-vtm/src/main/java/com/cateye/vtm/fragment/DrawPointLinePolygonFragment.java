package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.util.CatEyeMapManager;
import com.jkb.fragment.rigger.annotation.Puppet;
import com.jkb.fragment.rigger.rigger.Rigger;
import com.vtm.library.layers.PolygonLayer;

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

import java.util.ArrayList;
import java.util.List;

import static org.oscim.android.canvas.AndroidGraphics.drawableToBitmap;

/**
 * Created by xiaoxiao on 2018/3/21.
 */
@Puppet
public class DrawPointLinePolygonFragment extends BaseFragment {
    private CheckBox chk_draw_point, chk_draw_line, chk_draw_polygon;
    private List<CheckBox> checkBoxes;
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
    public void initView(View rootView) {
        chk_draw_point = rootView.findViewById(R.id.chk_draw_point);
        chk_draw_line = rootView.findViewById(R.id.chk_draw_line);
        chk_draw_polygon = rootView.findViewById(R.id.chk_draw_polygon);
        checkBoxes = new ArrayList<>();
        checkBoxes.add(chk_draw_point);
        checkBoxes.add(chk_draw_line);
        checkBoxes.add(chk_draw_polygon);


        chk_draw_point.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setCheckBoxSingleChoice((CheckBox) buttonView);
                }
            }
        });
        chk_draw_line.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setCheckBoxSingleChoice((CheckBox) buttonView);
                }
            }
        });
        chk_draw_polygon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setCheckBoxSingleChoice((CheckBox) buttonView);
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
        if (checkBoxes != null && !checkBoxes.isEmpty()) {
            for (CheckBox chk : checkBoxes) {
                if (chk.isChecked()) {
                    if (chk == chk_draw_point) {
                        return DRAW_STATE.DRAW_POINT;
                    } else if (chk == chk_draw_line) {
                        return DRAW_STATE.DRAW_LINE;
                    } else if (chk == chk_draw_polygon) {
                        return DRAW_STATE.DRAW_POLYGON;
                    }
                }
            }
        }
        return DRAW_STATE.DRAW_NONE;
    }

    public enum DRAW_STATE {
        DRAW_NONE, DRAW_POINT, DRAW_LINE, DRAW_POLYGON
    }

    private void setCheckBoxSingleChoice(CheckBox currendChk) {
        //将其他两个控件的选中状态置为未选中
        if (checkBoxes != null && !checkBoxes.isEmpty()) {
            for (CheckBox chk : checkBoxes) {
                if (chk != currendChk) {
                    chk.setChecked(false);
                }
            }
        }
    }

    private class MapEventsReceiver extends Layer implements GestureListener {

        MapEventsReceiver(Map map) {
            super(map);
        }

        @Override
        public boolean onGesture(Gesture g, MotionEvent e) {
            if (g instanceof Gesture.Tap) {

            }
            if (g instanceof Gesture.Press) {
                GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
                DRAW_STATE currentState = getCurrentDrawState();
                if (currentState != DRAW_STATE.DRAW_NONE) {//如果当前是绘制模式，则自动添加marker
                    markerLayer.addItem(new MarkerItem("", "", p));
                    //如果当前是绘制线模式，则增加pathLayer
                    if (currentState == DRAW_STATE.DRAW_LINE) {
                        polylineOverlay.addPoint(p);
                        if (polylineOverlay.getPoints() != null && polylineOverlay.getPoints().size() > 1) {
                            polylineOverlay.setLineString(getPointArray(polylineOverlay.getPoints()));
                        }
                    }
                    if (currentState == DRAW_STATE.DRAW_POLYGON) {
                        polygonOverlay.addPoint(p);
                        if (polygonOverlay.getPoints() != null) {
                            if (polygonOverlay.getPoints().size() > 2) {
                                polygonOverlay.setPolygonString(polygonOverlay.getPoints(), true);
                            } else if (polygonOverlay.getPoints().size() == 2) {
                                polygonOverlay.setLineString(getPointArray(polygonOverlay.getPoints()));
                            }
                        }
                    }
                    markerLayer.map().updateMap(true);//重绘
                }
                Toast.makeText(getActivity(), "Map tap\n" + p, Toast.LENGTH_SHORT).show();
                return true;
            }
//            if (g instanceof Gesture.LongPress) {//长按
//            }
//            if (g instanceof Gesture.TripleTap) {//双击
//            }
            return false;
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

    /**
     * Author : xiaoxiao
     * Describe : 回退按钮拦截,目前是无效的
     * param :
     * return :
     * Date : 2018/3/23
     */
    public void onRiggerBackPressed() {
        Rigger.getRigger(this).hideFragment(this);
    }
}
