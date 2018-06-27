package com.cateye.vtm.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.cateye.android.entity.MapSourceFromNet;
import com.cateye.android.vtm.R;

import java.util.Comparator;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/6/22.
 */

public class LayerManagerAdapter extends BaseAdapter {
    private Context mContext;
    private List<MapSourceFromNet.DataBean> dataBeanList;
    private LayoutInflater inflater;

    public LayerManagerAdapter(Context mContext, List<MapSourceFromNet.DataBean> dataBeanList) {
        this.mContext = mContext;
        this.dataBeanList = dataBeanList;
        this.inflater = LayoutInflater.from(mContext);
        //对传递进来的数据按照图层分组排序
        sortListData(dataBeanList);
    }

    @Override
    public int getCount() {
        if (dataBeanList != null) {
            return dataBeanList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        if (dataBeanList != null && dataBeanList.size() > i) {
            return dataBeanList.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.item_layer_manager, null);
            holder = new ViewHolder();
            holder.img_icon = (ImageView) view.findViewById(R.id.img_item_layer_manager_icon);
            holder.tv_name = (TextView) view.findViewById(R.id.tv_item_layer_manager_name);
            holder.chk_visibile = (CheckBox) view.findViewById(R.id.chk_item_layer_manager_visibile);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (dataBeanList != null && dataBeanList.size() > i) {
            MapSourceFromNet.DataBean dataBean = dataBeanList.get(i);
            holder.tv_name.setText(dataBean.getAbstractX() + "(" + dataBean.getGroup() + ")");
            holder.chk_visibile.setChecked(dataBean.isShow());
        }
        return view;
    }

    protected class ViewHolder {
        public ImageView img_icon;
        public TextView tv_name;
        public CheckBox chk_visibile;
    }

    /**
     * 对传递进来的数据做排序
     */
    @SuppressLint("NewApi")
    protected void sortListData(List<MapSourceFromNet.DataBean> dataBeanList) {
        if (dataBeanList != null && !dataBeanList.isEmpty()) {
            dataBeanList.sort(new Comparator<MapSourceFromNet.DataBean>() {
                @Override
                public int compare(MapSourceFromNet.DataBean dataBean, MapSourceFromNet.DataBean t1) {
                    return dataBean.getGroup().compareTo(t1.getGroup());
                }
            });
        }
    }
}
