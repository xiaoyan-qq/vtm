package com.cateye.vtm.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cateye.android.entity.MapSourceFromNet;
import com.cateye.android.vtm.R;

import java.util.Comparator;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/6/22.
 */

public class LayerManagerAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private List<MapSourceFromNet.DataBean> dataBeanList;
    private LayoutInflater inflater;

    public LayerManagerAdapter(Context mContext, List<MapSourceFromNet.DataBean> dataBeanList) {
        this.mContext = mContext;
        this.dataBeanList = dataBeanList;
        this.inflater = LayoutInflater.from(mContext);
        //对传递进来的数据按照图层分组排序
        sortListData();
    }

    @Override
    public int getGroupCount() {
        return dataBeanList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return dataBeanList.get(groupPosition).getMaps().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return dataBeanList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return dataBeanList.get(groupPosition).getMaps().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition * 1000 + childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.item_layer_manager_group, null);
        TextView tv_groupName = (TextView) convertView.findViewById(R.id.tv_group_name);
        tv_groupName.setText(dataBeanList.get(groupPosition).getGroup());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_layer_manager, null);
            holder = new ViewHolder();
            holder.img_icon = (ImageView) convertView.findViewById(R.id.img_item_layer_manager_icon);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_item_layer_manager_name);
            holder.chk_visibile = (CheckBox) convertView.findViewById(R.id.chk_item_layer_manager_visibile);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (dataBeanList != null && dataBeanList.size() > groupPosition && dataBeanList.get(groupPosition).getMaps().size() > childPosition) {
            final MapSourceFromNet.DataBean.MapsBean mapsBean = dataBeanList.get(groupPosition).getMaps().get(childPosition);
            holder.tv_name.setText(mapsBean.getAbstractX());
            holder.chk_visibile.setChecked(mapsBean.isShow());

            //用户点击勾选框，实时修改图层的显隐状态
            holder.chk_visibile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mapsBean.setShow(((CompoundButton) view).isChecked());
                }
            });
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
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
    public void sortListData() {
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
