package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.CatEyeMapManager;

/**
 * Created by xiaoxiao on 2018/8/31.
 */

public class AirPlanDrawFragment extends BaseDrawFragment {
    private ImageView img_airplan_draw/*绘制按钮*/, img_airplan_previous/*上一笔*/, img_airplan_clear/*清空*/;
    protected MapEventsReceiver mapEventsReceiver;//用户操作的回调

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        img_airplan_draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!view.isSelected()) {
                    //开始绘制
                    view.setSelected(true);
                    currentDrawState = DRAW_STATE.DRAW_POLYGON;
                } else {
                    //结束绘制
                    view.setSelected(false);
                    currentDrawState = DRAW_STATE.DRAW_NONE;
                    //绘制结束，将绘制的数据添加到airplan的图层内
                }
            }
        });

        //添加一个操作图层，监听用户在地图上的点击事件
        mapEventsReceiver = new MapEventsReceiver(CatEyeMapManager.getInstance(getActivity()).getCatEyeMap());
        CatEyeMapManager.getInstance(getActivity()).getCatEyeMap().layers().add(mapEventsReceiver, MainActivity.LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);
    }
}
