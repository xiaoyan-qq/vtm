package com.cateye.android.vtm;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.cateye.vtm.fragment.CatEyeMainFragment;
import com.jkb.fragment.rigger.annotation.Puppet;
import com.jkb.fragment.rigger.rigger.Rigger;

@Puppet(containerViewId = R.id.fragment_main_container)
public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //启动fragment，显示地图界面
        CatEyeMainFragment mainFragment=new CatEyeMainFragment();
        Rigger.getRigger(this).startFragment(mainFragment);
    }
}
