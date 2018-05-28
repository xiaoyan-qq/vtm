package com.cateye.vtm.fragment.base;

import org.oscim.core.GeoPoint;

/**
 * Created by xiaoxiao on 2018/5/28.
 */

public interface DrawPointLinePolygonInterface {
    public void onTapMapDrawPointLinePolygon(GeoPoint point, BaseDrawFragment.DRAW_STATE draw_state);
}
