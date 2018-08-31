package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.cateye.android.entity.MapSourceFromNet;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.SystemConstant;
import com.xw.repo.BubbleSeekBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/8/31.
 */

public class MultiTimeLayerSelectFragment extends BaseFragment {
    private Spinner btn_layer_select;
    private View layer_seekbar;
    List<MapSourceFromNet.DataBean> dataBeanList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (bundle != null) {
            dataBeanList = (ArrayList<MapSourceFromNet.DataBean>) bundle.getSerializable(SystemConstant.BUNDLE_MULTI_TIME_SELECTOR_DATA);
        }
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_multi_time_select;
    }

    @Override
    public void initView(View rootView) {
        btn_layer_select = rootView.findViewById(R.id.tv_multi_time_layer_name);
        layer_seekbar = rootView.findViewById(R.id.layer_multi_time_seekbar);

        if (dataBeanList != null && !dataBeanList.isEmpty()) {
            List<String> multiLayerNameList = new ArrayList<>();
            for (MapSourceFromNet.DataBean dataBean : dataBeanList) {
                multiLayerNameList.add(dataBean.getMemo());
            }

            if (!multiLayerNameList.isEmpty()){
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_dropdown_item,android.R.id.text1,multiLayerNameList);
                btn_layer_select.setAdapter(adapter);
                btn_layer_select.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        btn_layer_select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                //用户选中某个选项，自动绘制右侧的滑动控件
                                BubbleSeekBar seekBar=new BubbleSeekBar(getContext());
                                seekBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                seekBar.getConfigBuilder()
                                        .min(0.0f)
                                        .max(100)
                                        .progress(0)
                                        .sectionCount(dataBeanList.get(i).getMaps().size())
                                        .trackColor(ContextCompat.getColor(getContext(), R.color.))
                                        .secondTrackColor(ContextCompat.getColor(getContext(), R.color.color_blue))
                                        .thumbColor(ContextCompat.getColor(getContext(), R.color.color_blue))
                                        .showSectionText()
                                        .sectionTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                                        .sectionTextSize(18)
                                        .showThumbText()
                                        .thumbTextColor(ContextCompat.getColor(getContext(), R.color.color_red))
                                        .thumbTextSize(18)
                                        .bubbleColor(ContextCompat.getColor(getContext(), R.color.color_green))
                                        .bubbleTextSize(18)
                                        .showSectionMark()
                                        .seekBySection()
                                        .autoAdjustSectionMark()
                                        .sectionTextPosition(BubbleSeekBar.TextPosition.BELOW_SECTION_MARK)
                                        .build();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
                    }
                });
                btn_layer_select.setSelection(0);
            }
        }
    }
}
