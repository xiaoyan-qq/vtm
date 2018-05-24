package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cateye.android.vtm.R;
import com.cateye.vtm.util.SystemConstant;
import com.vondear.rxtools.view.RxToast;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by xiaoxiao on 2018/3/21.
 */
//@Puppet
public class DrawPointLinePolygonFragment extends BaseDrawFragment {
    private TextView tv_last, tv_clear, tv_finish;

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
                pop();//退出当前界面
            }
        });
    }

    public static BaseFragment newInstance(Bundle bundle) {
        DrawPointLinePolygonFragment drawPointLinePolygonFragment = new DrawPointLinePolygonFragment();
        drawPointLinePolygonFragment.setArguments(bundle);
        return drawPointLinePolygonFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        //通知主界面隐藏部分重新显示
        setMainFragmentAreaVisible(CatEyeMainFragment.BUTTON_AREA.BOTTOM_RIGHT, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //通知主界面绘制点线面结束
        Message msg = new Message();
        msg.what = SystemConstant.MSG_WHAT_DRAW_POINT_LINE_POLYGON_DESTROY;
        EventBus.getDefault().post(msg);

        //通知主界面隐藏部分重新显示
        setMainFragmentAreaVisible(CatEyeMainFragment.BUTTON_AREA.BOTTOM_RIGHT, true);
    }
}
