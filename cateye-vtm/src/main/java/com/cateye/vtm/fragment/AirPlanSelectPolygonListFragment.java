package com.cateye.vtm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cateye.android.entity.AirPlanDBEntity;
import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.R;
import com.cateye.vtm.fragment.base.BaseDrawFragment;
import com.cateye.vtm.fragment.base.BaseFragment;
import com.cateye.vtm.util.CatEyeMapManager;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.SnackBar;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.vondear.rxtool.RxRecyclerViewDividerTool;

import org.oscim.map.Map;
import org.xutils.DbManager;
import org.xutils.ex.DbException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoxiao on 2018/8/31.
 */

public class AirPlanSelectPolygonListFragment extends BaseDrawFragment {
    private Map mMap;
    private RefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private AirPlanPolygonAdapter adapter;
    private List<AirPlanDBEntity> listData;
    private DbManager dbManager;

    private final int PAGE_SIZE = 10;
    private int page = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mMap = CatEyeMapManager.getMapView().map();
        this.dbManager = ((MainActivity) getActivity()).getDbManager();
    }

    public static BaseFragment newInstance(Bundle bundle) {
        AirPlanSelectPolygonListFragment airPlanDrawFragment = new AirPlanSelectPolygonListFragment();
        airPlanDrawFragment.setArguments(bundle);
        return airPlanDrawFragment;
    }

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_air_plan_polygon_list;
    }

    @Override
    public void initView(View rootView) {
        refreshLayout = rootView.findViewById(R.id.refreshLayout);
        recyclerView = rootView.findViewById(R.id.rv_air_plan_polygon);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        listData = new ArrayList<>();
        adapter = new AirPlanPolygonAdapter(getActivity(), listData);
        recyclerView.setAdapter(adapter);
        //设置 Footer 为 球脉冲 样式
        refreshLayout.setRefreshFooter(new BallPulseFooter(getActivity()).setSpinnerStyle(SpinnerStyle.Scale));
        recyclerView.addItemDecoration(new RxRecyclerViewDividerTool(0, 0, 2, 2));
        //默认加载前20条数据
        try {
            List<AirPlanDBEntity> dbEntityList = dbManager.selector(AirPlanDBEntity.class).limit(PAGE_SIZE).offset(page * PAGE_SIZE).findAll();
            if (dbEntityList != null && !dbEntityList.isEmpty()) {
                listData.addAll(dbEntityList);
            } else {
                SnackBar.make(getActivity()).text("没有存储的polygon数据!").actionText("确定").actionClickListener(new SnackBar.OnActionClickListener() {
                    @Override
                    public void onActionClick(SnackBar sb, int actionId) {
                        sb.dismiss();
                    }
                }).duration(Toast.LENGTH_LONG);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        //上拉加载更多
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshLayout) {
                page++;
                try {
                    List<AirPlanDBEntity> dbEntityList = dbManager.selector(AirPlanDBEntity.class).limit(PAGE_SIZE).offset(page * PAGE_SIZE).findAll();
                    if (dbEntityList != null && !dbEntityList.isEmpty()) {
                        listData.addAll(dbEntityList);
                        adapter.notifyDataSetChanged();
                    } else {
                        SnackBar.make(getActivity()).text("没有更多的数据!").actionText("确定").actionClickListener(new SnackBar.OnActionClickListener() {
                            @Override
                            public void onActionClick(SnackBar sb, int actionId) {
                                sb.dismiss();
                            }
                        }).duration(Toast.LENGTH_LONG);
                        refreshLayout.setEnableLoadMore(false);//没有更多数据，设置不可再通过上拉加载数据
                        page--;
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }finally {
                    refreshLayout.finishLoadMore();
                }
            }
        });
    }


    private class AirPlanPolygonAdapter extends RecyclerView.Adapter<AirPlanPolygonAdapter.ViewHolder> {
        private List<AirPlanDBEntity> listData;
        private Context mContext;

        public AirPlanPolygonAdapter(Context mContext, List<AirPlanDBEntity> listData) {
            this.listData = listData;
            this.mContext = mContext;
        }


        @NonNull
        @Override
        public AirPlanPolygonAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.item_air_plan_polygon_dblist, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder(rootView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull AirPlanPolygonAdapter.ViewHolder viewHolder, int i) {
            viewHolder.chk_name.setText(listData.get(i).getName());
            viewHolder.tv_updateTime.setText(listData.get(i).getLastUpdate());
        }

        @Override
        public int getItemCount() {
            if (listData != null) {
                return listData.size();
            }
            return 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CheckBox chk_name;//polygon的名称
            public TextView tv_updateTime;//最后更新时间

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                chk_name = itemView.findViewById(R.id.chk_polygon_name);
                tv_updateTime = itemView.findViewById(R.id.tv_polygon_updatetime);
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
        ((MainActivity) getActivity()).hiddenSlidingLayout();//同时隐藏右侧面板
        return true;
    }
}
