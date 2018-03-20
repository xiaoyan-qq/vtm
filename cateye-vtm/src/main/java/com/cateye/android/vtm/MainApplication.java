package com.cateye.android.vtm;

import android.app.Application;

import com.vondear.rxtools.RxTool;

/**
 * Created by xiaoxiao on 2018/3/21.
 */

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RxTool.init(this);
    }
}
