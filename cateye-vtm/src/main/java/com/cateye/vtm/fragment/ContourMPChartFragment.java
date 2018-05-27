package com.cateye.vtm.fragment;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.cateye.android.entity.ContourMPData;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.util.CatEyeMapManager;
import com.cateye.vtm.util.SystemConstant;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.vondear.rxtools.RxDeviceTool;

import org.oscim.android.canvas.AndroidBitmap;
import org.oscim.backend.canvas.Bitmap;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.layers.vector.geometries.Style;

import java.util.ArrayList;
import java.util.List;

import static org.oscim.android.canvas.AndroidGraphics.drawableToBitmap;

/**
 * Created by xiaoxiao on 2018/5/23.
 */

public class ContourMPChartFragment extends BaseDrawFragment {
    private LineChart contourChart;//显示等高线的图表控件
    private List<ContourMPData> mpChartDataList;//需要显示等高线的数据
    private ImageView img_close;
    private MapPosition currentMapPosition;

    private ItemizedLayer<MarkerItem> mLineClickMarkerLayer;//用来捕捉用户点击事件的marker的layer

    public static BaseFragment newInstance(Bundle bundle) {
        ContourMPChartFragment contourMPChartFragment = new ContourMPChartFragment();
        contourMPChartFragment.setArguments(bundle);
        return contourMPChartFragment;
    }

    @Override
    public void onNewBundle(Bundle args) {
        super.onNewBundle(args);
        if (args != null) {
            //获取等高线的数据
            mpChartDataList = (List<ContourMPData>) args.getSerializable(SystemConstant.DATA_CONTOUR_CHART);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            //获取等高线的数据
            mpChartDataList = (List<ContourMPData>) savedInstanceState.getSerializable(SystemConstant.DATA_CONTOUR_CHART);
        }
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            //获取等高线的数据
            mpChartDataList = (List<ContourMPData>) bundle.getSerializable(SystemConstant.DATA_CONTOUR_CHART);
        }
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_contour_mp_chart;
    }

    @Override
    public void initView(View rootView) {
        currentMapPosition = new MapPosition();
        contourChart = rootView.findViewById(R.id.contour_chart);
        img_close = rootView.findViewById(R.id.img_contour_chart_close);
        contourChart.setMinimumHeight(((int) (RxDeviceTool.getScreenHeight(getActivity()) * 0.3)));
        initChartData(mpChartDataList);

        //自动添加pathLayer,
        initPolylineOverlayer();
        //自动添加绘制marker的overlayer，当用户滑动折线图时，自动定位到折线图上对应的点位
        initMarkerOverlayer();

        if (mpChartDataList != null && polylineOverlay != null) {
            if (polylineOverlay.getPoints() == null) {
                polylineOverlay.setPoints(new ArrayList<GeoPoint>());
            }
            polylineOverlay.getPoints().clear();
            for (ContourMPData data : mpChartDataList) {
                GeoPoint geoPoint = new GeoPoint(data.getmLatitude(), data.getmLongitude());
                polylineOverlay.getPoints().add(geoPoint);
                markerLayer.addItem(new MarkerItem(geoPoint.getLatitude() + "/" + geoPoint.getLongitude(), "", geoPoint));
            }
            redrawPolyline(polylineOverlay);

            //自动将地图缩放级别提高，并将中心点位置定位到第一个点的位置
            polylineOverlay.map().getMapPosition(currentMapPosition);
            currentMapPosition.setPosition(polylineOverlay.getPoints().get(0).getLatitude(), polylineOverlay.getPoints().get(0).getLongitude());
            if (currentMapPosition.getZoomLevel() < 14) {
                currentMapPosition.setZoomLevel(14);
            }
            polylineOverlay.map().animator().animateTo(currentMapPosition);
        }

        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pop();
            }
        });
    }

    /**
     * @param :
     * @return :
     * @method : initPolylineOverlayer
     * @Author : xiaoxiao
     * @Describe : 初始化绘制线的图层
     * @Date : 2018/5/24
     */
    private void initPolylineOverlayer() {
        //自动添加pathLayer
        int c = getResources().getColor(R.color.violet);
        Style lineStyle = Style.builder()
                .stippleColor(c)
                .stipple(24)
                .stippleWidth(1)
                .strokeWidth(2)
                .strokeColor(c)
                .fixed(true)
                .randomOffset(false)
                .build();
        polylineOverlay.setStyle(lineStyle);
    }

    /**
     * @param :
     * @return :
     * @method : initMarkerOverlayer
     * @Author : xiaoxiao
     * @Describe : 初始化线的点击layer
     * @Date : 2018/5/24
     */
    private void initMarkerOverlayer() {
        //打开该fragment，则自动向地图中添加marker的overlay
        Bitmap bitmapPoi = drawableToBitmap(getResources().getDrawable(R.drawable.marker_poi));
        pointMarker = new MarkerSymbol(bitmapPoi, MarkerSymbol.HotspotPlace.CENTER);

//        MarkerSymbol unClickMarkerSymbol=new MarkerSymbol(new AndroidBitmap(null), MarkerSymbol.HotspotPlace.CENTER);

        markerLayer = new ItemizedLayer<MarkerItem>(CatEyeMapManager.getInstance(getActivity()).getCatEyeMap(), new ArrayList<MarkerItem>(), pointMarker/*默认不显示任何marker*/, new ItemizedLayer.OnItemGestureListener<MarkerItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, MarkerItem item) {
                markerLayer.removeAllItems();
                item.setMarker(pointMarker);
                markerLayer.update();
                markerLayer.map().updateMap(true);
                return true;
            }

            @Override
            public boolean onItemLongPress(int index, MarkerItem item) {
                return false;
            }
        });
        CatEyeMapManager.getInstance(getActivity()).getCatEyeMap().layers().add(markerLayer, MainActivity.LAYER_GROUP_ENUM.GROUP_BUILDING.ordinal());
    }


    /**
     * @param :
     * @return :
     * @method : initChartData
     * @Author : xiaoxiao
     * @Describe : 初始化表格所需要的数据内容
     * @Date : 2018/5/23
     */
    private void initChartData(List<ContourMPData> contourMPDataList) {
        if (contourMPDataList != null && !contourMPDataList.isEmpty()) {
            List<Entry> entries = new ArrayList<Entry>();
            for (int i = 0; i < contourMPDataList.size(); i++) {
                entries.add(new Entry(i, contourMPDataList.get(i).getmHeight()));
            }
            LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
            dataSet.setColor(getResources().getColor(R.color.color_blue_alpha_400));
            dataSet.setValueTextColor(getResources().getColor(R.color.primary_text)); // styling, ...
            dataSet.setFillColor(getResources().getColor(R.color.color_blue_alpha_200));

            LineData lineData = new LineData(dataSet);
            contourChart.setData(lineData);
            contourChart.animateXY(1000, 1500, Easing.EasingOption.Linear, Easing.EasingOption.Linear);
            contourChart.invalidate(); // refresh
            contourChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    int pointIndex = (int) e.getX();
                    if (markerLayer != null && mpChartDataList != null && mpChartDataList.size() > pointIndex) {
                        markerLayer.removeAllItems();
                        markerLayer.addItem(new MarkerItem("", "", new GeoPoint(mpChartDataList.get(pointIndex).getmLatitude(), mpChartDataList.get(pointIndex).getmLongitude())));
                        markerLayer.update();
                        markerLayer.map().updateMap(true);
                    }
                }

                @Override
                public void onNothingSelected() {
                    if (markerLayer != null) {
                        markerLayer.getItemList().clear();
                        markerLayer.update();
                        markerLayer.map().updateMap(true);
                    }
                }
            });
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //通知主界面隐藏部分重新显示
        setMainFragmentAreaVisible(CatEyeMainFragment.BUTTON_AREA.BOTTOM_RIGHT, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        clearMapOverlayer();
        //通知主界面隐藏部分重新显示
        setMainFragmentAreaVisible(CatEyeMainFragment.BUTTON_AREA.BOTTOM_RIGHT, true);
    }
}
