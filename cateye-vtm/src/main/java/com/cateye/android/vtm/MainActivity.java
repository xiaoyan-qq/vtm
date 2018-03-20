package com.cateye.android.vtm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cateye.vtm.fragment.CatEyeMainFragment;
import com.jkb.fragment.rigger.annotation.Puppet;
import com.jkb.fragment.rigger.rigger.Rigger;

import org.oscim.android.filepicker.FilePicker;
import org.oscim.android.filepicker.FilterByFileExtension;
import org.oscim.android.filepicker.ValidMapFile;
import org.oscim.android.filepicker.ValidRenderTheme;

@Puppet(containerViewId = R.id.fragment_main_container,bondContainerView = true)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //启动fragment，显示地图界面
        Rigger.getRigger(this).startFragment(CatEyeMainFragment.newInstance(new Bundle()));
    }

    /**
     * 选择本地地图文件的文件选择过滤器
     * */
    public static class MapFilePicker extends FilePicker {
        public MapFilePicker() {
            setFileDisplayFilter(new FilterByFileExtension(".map"));
            setFileSelectFilter(new ValidMapFile());
        }
    }

    /**
     * 选择本地地图样式文件的文件选择过滤器
     * */
    public static class ThemeFilePicker extends FilePicker {
        public ThemeFilePicker() {
            setFileDisplayFilter(new FilterByFileExtension(".xml"));
            setFileSelectFilter(new ValidRenderTheme());
        }
    }
}
