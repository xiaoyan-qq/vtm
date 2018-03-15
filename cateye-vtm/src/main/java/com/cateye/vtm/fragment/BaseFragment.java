package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jkb.fragment.rigger.annotation.Puppet;

/**
 * Created by zhangdezhi1702 on 2018/3/15.
 */

@Puppet
public abstract class BaseFragment extends Fragment implements BaseFragmentInterface {
    protected View rootView;//当前fragment的根View

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(getFragmentLayoutId(), container);
        initView(rootView);
        return rootView;
    }
}
