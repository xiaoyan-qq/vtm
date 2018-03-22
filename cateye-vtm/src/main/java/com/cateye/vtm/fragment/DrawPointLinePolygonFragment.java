package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.cateye.android.vtm.R;
import com.jkb.fragment.rigger.annotation.Puppet;
import com.jkb.fragment.rigger.rigger.Rigger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/3/21.
 */
@Puppet(bondContainerView = false)
public class DrawPointLinePolygonFragment extends BaseFragment {
    private CheckBox chk_draw_point, chk_draw_line, chk_draw_polygon;
    private List<CheckBox> checkBoxes;
    private DRAW_STATE currentDrawState = DRAW_STATE.DRAW_NONE;

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_draw_point_line_polygon;
    }

    @Override
    public void initView(View rootView) {
        chk_draw_point = rootView.findViewById(R.id.chk_draw_point);
        chk_draw_line = rootView.findViewById(R.id.chk_draw_line);
        chk_draw_polygon = rootView.findViewById(R.id.chk_draw_polygon);
        checkBoxes = new ArrayList<>();
        checkBoxes.add(chk_draw_point);
        checkBoxes.add(chk_draw_line);
        checkBoxes.add(chk_draw_polygon);


        chk_draw_point.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setCheckBoxSingleChoice((CheckBox) buttonView);
                }
            }
        });
        chk_draw_line.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setCheckBoxSingleChoice((CheckBox) buttonView);
                }
            }
        });
        chk_draw_polygon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    setCheckBoxSingleChoice((CheckBox) buttonView);
                }
            }
        });
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

    /**
     * Author : xiaoxiao
     * Describe : 获取当前的绘制状态
     * param :
     * return : 返回绘制状态的枚举类型，如果为NONE则当前没有进行绘制
     * Date : 2018/3/22
     */
    public DRAW_STATE getCurrentDrawState() {
        if (checkBoxes != null && !checkBoxes.isEmpty()) {
            for (CheckBox chk : checkBoxes) {
                if (chk == chk_draw_point) {
                    return DRAW_STATE.DRAW_POINT;
                } else if (chk == chk_draw_line) {
                    return DRAW_STATE.DRAW_LINE;
                } else if (chk == chk_draw_polygon) {
                    return DRAW_STATE.DRAW_POLYGON;
                }
            }
        }
        return DRAW_STATE.DRAW_NONE;
    }

    public enum DRAW_STATE {
        DRAW_NONE, DRAW_POINT, DRAW_LINE, DRAW_POLYGON
    }

    private void setCheckBoxSingleChoice(CheckBox currendChk) {
        //将其他两个控件的选中状态置为未选中
        if (checkBoxes != null && !checkBoxes.isEmpty()) {
            for (CheckBox chk : checkBoxes) {
                if (chk != currendChk) {
                    chk.setChecked(false);
                }
            }
        }
    }
}
