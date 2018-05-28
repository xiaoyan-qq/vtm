package com.cateye.vtm.fragment.base;

import android.view.View;

/**
 * Created by zhangdezhi1702 on 2018/3/15.
 */

public interface BaseFragmentInterface {
    int getFragmentLayoutId();

    void initView(View rootView);

}
