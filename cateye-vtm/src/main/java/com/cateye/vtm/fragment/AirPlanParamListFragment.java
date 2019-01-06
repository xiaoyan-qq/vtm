package com.cateye.vtm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.cateye.android.entity.AirPlanDBEntity;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.CatEyeMapManager;
import com.cateye.vtm.util.LayerUtils;
import com.desmond.ripple.RippleCompat;
import com.vondear.rxtool.view.RxToast;
import com.vtm.library.layers.MultiPolygonLayer;
import com.yydcdut.sdlv.Menu;
import com.yydcdut.sdlv.SlideAndDragListView;

import org.oscim.map.Map;
import org.xutils.DbManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/8/31.
 * 设置航飞参数时，选择的polygon的列表
 */

public class AirPlanParamListFragment extends BaseDrawFragment {
    private Map mMap;
    private DbManager dbManager;

    private SlideAndDragListView slideAndDragListView;
    private AirPlanParamAdapter adapter;
    private List<AirPlanDBEntity> listData;
    private AirPlanDBEntity dragDBEntity;

    private ImageView img_back;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMap = CatEyeMapManager.getMapView().map();
        this.dbManager = ((MainActivity) getActivity()).getDbManager();
    }

    public static BaseFragment newInstance(Bundle bundle) {
        AirPlanParamListFragment airPlanDrawFragment = new AirPlanParamListFragment();
        airPlanDrawFragment.setArguments(bundle);
        return airPlanDrawFragment;
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_air_plan_param_list;
    }

    @Override
    public void initView(View rootView) {
        slideAndDragListView = rootView.findViewById(R.id.sadLv_paramPolygonList);
        listData = new ArrayList<>();
        adapter = new AirPlanParamAdapter(getActivity(), listData);
        Menu menu = new Menu(true, 0);//第1个参数表示滑动 item 是否能滑的过头，像弹簧那样( true 表示过头，就像 Gif 中显示的那样；false 表示不过头，就像 Android QQ 中的那样)
        slideAndDragListView.setMenu(menu);
        slideAndDragListView.setAdapter(adapter);
        slideAndDragListView.setOnDragDropListener(new SlideAndDragListView.OnDragDropListener() {//用户拖动listview的item上下滑动
            @Override
            public void onDragViewStart(int beginPosition) {
                dragDBEntity=listData.get(beginPosition);
            }

            @Override
            public void onDragDropViewMoved(int fromPosition, int toPosition) {
                AirPlanDBEntity removeEntity = listData.remove(fromPosition);
                listData.add(toPosition, removeEntity);
            }

            @Override
            public void onDragViewDown(int finalPosition) {
                listData.set(finalPosition, dragDBEntity);
            }
        });

        img_back = (ImageView) findViewById(R.id.tv_air_plan_list_back);
//        RippleCompat.apply(img_back, R.color.gray);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressedSupport();
            }
        });
    }


    private class AirPlanParamAdapter extends BaseAdapter {
        private Context mContext;
        private List<AirPlanDBEntity> listData;
        private MultiPolygonLayer airPlanParamLayer;

        public AirPlanParamAdapter(Context mContext, List<AirPlanDBEntity> listData) {
            this.mContext = mContext;
            this.listData = listData;
            this.airPlanParamLayer = LayerUtils.getAirPlanParamLayer(mMap);
        }

        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * addData 向当前listview中添加polygon，更新列表
         *
         * @param airPlanDBEntity
         */
        public void addData(AirPlanDBEntity airPlanDBEntity) {
            listData.add(airPlanDBEntity);
            notifyDataSetChanged();
        }

        /**
         * addAllData 批量向当前listview中添加polygon，更新列表
         *
         * @param airPlanDBEntityList
         */
        public void addAllData(List<AirPlanDBEntity> airPlanDBEntityList) {
            listData.addAll(airPlanDBEntityList);
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_air_plan_polygon_param, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final AirPlanDBEntity airPlanDBEntity = listData.get(position);
            if (airPlanDBEntity != null) {
                viewHolder.tv_polygonName.setText(airPlanDBEntity.getName());
                viewHolder.tv_updateTime.setText(airPlanDBEntity.getLastUpdate());
                viewHolder.btn_delete.setOnClickListener(new View.OnClickListener() {//移除指定的polygon
                    @Override
                    public void onClick(View v) {
                        airPlanParamLayer.removePolygonDrawable(airPlanDBEntity.getGeometry());
                        RxToast.info(mContext, "移除" + airPlanDBEntity.getName());
                        listData.remove(position);
                        notifyDataSetChanged();
                    }
                });
            }

            return convertView;
        }

        protected class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tv_updateTime;//最后更新时间
            public TextView tv_polygonName;//polygon名称
            public BootstrapButton btn_delete;//删除

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_updateTime = itemView.findViewById(R.id.tv_polygon_updatetime);
                tv_polygonName = itemView.findViewById(R.id.tv_polygon_name);
                btn_delete = itemView.findViewById(R.id.btn_polygon_delete);

                RippleCompat.apply(btn_delete, R.color.gray);
            }
        }
    }

    /**
     * addData 向当前listview中添加polygon，更新列表
     *
     * @param airPlanDBEntity
     */
    public void addData(AirPlanDBEntity airPlanDBEntity) {
        adapter.addData(airPlanDBEntity);
    }

    /**
     * addAllData 批量向当前listview中添加polygon，更新列表
     *
     * @param airPlanDBEntityList
     */
    public void addAllData(List<AirPlanDBEntity> airPlanDBEntityList) {
        adapter.addAllData(airPlanDBEntityList);
    }

    public void removeData(AirPlanDBEntity airPlanDBEntity) {
        if (listData != null && airPlanDBEntity != null) {
            Iterator iterator = listData.iterator();
            while (iterator.hasNext()) {
                AirPlanDBEntity entity = (AirPlanDBEntity) iterator.next();
                if (entity != null && airPlanDBEntity.getId()==entity.getId()) {
                    iterator.remove();
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public boolean onBackPressedSupport() {
        pop();
        ((MainActivity) getActivity()).hiddenSlidingLayout(true);//同时隐藏右侧面板
        return true;
    }
}
