package com.cateye.vtm.fragment;

import android.os.Bundle;
import android.view.View;

import com.cateye.android.entity.ContourMPData;
import com.cateye.android.vtm.R;
import com.cateye.vtm.util.SystemConstant;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/5/23.
 */

public class ContourMPChartFragment extends BaseFragment {
    private LineChart contourChart;//显示等高线的图表控件
    private List<ContourMPData> mpChartDataList;//需要显示等高线的数据

    public static BaseFragment newInstance(Bundle bundle) {
        ContourMPChartFragment contourMPChartFragment = new ContourMPChartFragment();
        contourMPChartFragment.setArguments(bundle);
        return contourMPChartFragment;
    }

    @Override
    public void onNewBundle(Bundle args) {
        super.onNewBundle(args);
        if (args != null) {
            //获取当前的绘制状态
            mpChartDataList = (List<ContourMPData>) args.getSerializable(SystemConstant.DATA_CONTOUR_CHART);
        }
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_contour_mp_chart;
    }

    @Override
    public void initView(View rootView) {
        contourChart = rootView.findViewById(R.id.contour_chart);
        initChartData(mpChartDataList);
    }

    /**
     * @param :
     * @return :
     * @method : initChartData
     * @Author : xiaoxiao
     * @Describe : 初始化表格所需要的数据内容
     * @Date : 2018/5/23
     */
    private void initChartData(List<ContourMPData> contourMPDataList) {
        if (contourMPDataList != null && !contourMPDataList.isEmpty()) {
            List<Entry> entries = new ArrayList<Entry>();
            for (int i = 0; i < contourMPDataList.size(); i++) {
                entries.add(new Entry(i, contourMPDataList.get(i).getmHeight()));
            }
            LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
            dataSet.setColor(getResources().getColor(R.color.color_blue_alpha_400));
            dataSet.setValueTextColor(getResources().getColor(R.color.colorPrimaryLight_alpha)); // styling, ...

            LineData lineData = new LineData(dataSet);
            contourChart.setData(lineData);
            contourChart.invalidate(); // refresh
        }
    }
}
