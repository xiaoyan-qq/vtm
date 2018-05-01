package com.cateye.vtm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.yokeyword.fragmentation.SupportFragment;

/**
 * Created by zhangdezhi1702 on 2018/3/15.
 */

//@Puppet
public abstract class BaseFragment extends SupportFragment implements BaseFragmentInterface {
    protected View rootView;//当前fragment的根View
    protected Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(getFragmentLayoutId(), container, false);
        initView(rootView);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    protected View findViewById(@IdRes int id) {
        return rootView.findViewById(id);
    }

    protected String getFragmentTag(){
        return getTag();
    }

    @Override
    public boolean onBackPressedSupport() {
        pop();
        return true;
    }
}
