package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.view.View;

import com.cateye.android.vtm.R;
import com.jkb.fragment.rigger.annotation.Puppet;
import com.jkb.fragment.rigger.rigger.Rigger;

/**
 * Created by xiaoxiao on 2018/3/21.
 */
@Puppet(bondContainerView = false)
public class DrawPointLinePolygonFragment extends BaseFragment {
    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_draw_point_line_polygon;
    }

    @Override
    public void initView(View rootView) {

    }

    public static BaseFragment newInstance(Bundle bundle) {
        DrawPointLinePolygonFragment drawPointLinePolygonFragment = new DrawPointLinePolygonFragment();
        drawPointLinePolygonFragment.setArguments(bundle);
        return drawPointLinePolygonFragment;
    }

    @Override
    public void onRiggerBackPressed() {
        Rigger.getRigger(this).close();
    }
}
