package com.cateye.android.vtm;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.canyinghao.candialog.CanDialog;
import com.canyinghao.candialog.CanDialogInterface;
import com.cateye.android.entity.DataFromNet;
import com.cateye.android.entity.Project;
import com.cateye.vtm.fragment.CatEyeMainFragment;
import com.cateye.vtm.fragment.MultiTimeLayerSelectFragment;
import com.cateye.vtm.util.SystemConstant;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.Response;
import com.lzy.okrx2.adapter.ObservableResponse;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.umeng.analytics.MobclickAgent;
import com.vondear.rxtool.RxBarTool;
import com.vondear.rxtool.RxLogTool;
import com.vondear.rxtool.view.RxToast;
import com.vondear.rxui.view.dialog.RxDialogLoading;
import com.vondear.rxui.view.dialog.RxDialogSure;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import org.greenrobot.eventbus.EventBus;
import org.oscim.android.MapView;
import org.oscim.android.filepicker.FilePicker;
import org.oscim.android.filepicker.FilterByFileExtension;
import org.oscim.android.filepicker.ValidMapFile;
import org.oscim.android.filepicker.ValidRenderTheme;
import org.oscim.backend.CanvasAdapter;
import org.oscim.map.Map;
import org.oscim.renderer.BitmapRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.scalebar.CatEyeMapScaleBar;
import org.oscim.scalebar.ImperialUnitAdapter;
import org.oscim.scalebar.MapScaleBar;
import org.oscim.scalebar.MapScaleBarLayer;
import org.oscim.scalebar.MetricUnitAdapter;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.yokeyword.fragmentation.SupportActivity;

//@Puppet(containerViewId = R.id.fragment_main_container, bondContainerView = true)
public class MainActivity extends SupportActivity implements TencentLocationListener {
    private TencentLocation currentLocation;
    private CatEyeMainFragment mainFragment;

    public MapView mapView;//地图控件
    public Map mMap;//地图

    //地图layer的分组
    public enum LAYER_GROUP_ENUM {
        BASE_GRID_GROUP(0, "L0", "基础栅格图层L0", false), PROJ_GRID_GROUP(1, "L1", "项目栅格图层分组L1", false), BASE_VECTOR_GROUP(2, "L2", "基础矢量图层L2", true), PROJ_VECTOR_GROUP(3, "L3", "项目矢量图层分组L3", true), OTHER_GROUP(4, "L4", "其他图层分组", true), OBJECTS_3D_GROUP(5, "L5", "3D图层分组", true), OPERTOR_GROUP(6, "L6", "操作图层分组L4", true), LOCATION_GROUP(7, "L7", "当前位置分组", false);
        public int orderIndex;//图层分组的顺序
        public String name;//图层分组名称
        public String desc;//图层描述
        public boolean isMulti;//图层分组是否支持多个图层

        LAYER_GROUP_ENUM(int orderIndex, String name, String desc, boolean isMulti) {
            this.orderIndex = orderIndex;
            this.name = name;
            this.desc = desc;
            this.isMulti = isMulti;
        }

        public static LAYER_GROUP_ENUM getGroupByName(String groupName) {
            for (LAYER_GROUP_ENUM groupEnum : LAYER_GROUP_ENUM.values()) {
                if (groupEnum.name.contains(groupName)) {
                    return groupEnum;
                }
            }
            return OTHER_GROUP;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.mapView);

        mMap = mapView.map();
        initScaleBar();


        //启动fragment，显示地图界面
//        Rigger.getRigger(this).startFragment(CatEyeMainFragment.newInstance(new Bundle()));
//        startFragment(CatEyeMainFragment.class);
        mainFragment = CatEyeMainFragment.newInstance(new Bundle());
        loadRootFragment(R.id.fragment_main_container, mainFragment);
        //申请所需要的权限
        AndPermission.with(this).permission(Permission.Group.LOCATION/*定位权限*/, Permission.Group.STORAGE/*存储权限*/ /*, Permission.Group.PHONE*//*电话相关权限*//*, Permission.Group.MICROPHONE*//*录音权限*/)
                .onGranted(new Action() {//用户允许
                    @Override
                    public void onAction(List<String> permissions) {

                    }
                })
                .onDenied(new Action() {//用户拒绝
                    @Override
                    public void onAction(List<String> permissions) {
                        if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, permissions)) {
                            StringBuilder permissionSB = new StringBuilder();
                            if (permissions != null && !permissions.isEmpty()) {
                                for (String p : permissions) {
                                    permissionSB.append(Permission.transformText(MainActivity.this, p));
                                    permissionSB.append(",");
                                }
                            }
                            if (permissionSB.toString().endsWith(",")) {
                                permissionSB.delete(permissionSB.length() - 1, permissionSB.length());
                            }
                            // 这些权限被用户总是拒绝。
                            RxDialogSure sureDialog = new RxDialogSure(MainActivity.this);
                            sureDialog.setContent("您拒绝了" + permissionSB + "权限，可能会导致某些功能无法正常使用!");
                            sureDialog.setTitle("提示");
                            sureDialog.getSureView().setEnabled(true);
                            sureDialog.show();
                        }
                    }
                }).rationale(new Rationale() {
            @Override
            public void showRationale(Context context, List<String> permissions, RequestExecutor executor) {

            }
        }).start();

        TencentLocationRequest request = TencentLocationRequest.create();
        TencentLocationManager locationManager = TencentLocationManager.getInstance(this);
        locationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_WGS84);//使用wgs84坐标系
        int error = locationManager.requestLocationUpdates(request, this);

        //处理ActionBar
        RxBarTool.transparencyBar(this);

        setCurrentProject();//设置当前正在作业的项目
    }

    /**
     * 选择本地地图文件的文件选择过滤器
     */
    public static class MapFilePicker extends FilePicker {
        public MapFilePicker() {
            setFileDisplayFilter(new FilterByFileExtension(".map"));
            setFileSelectFilter(new ValidMapFile());
        }
    }

    /**
     * 选择本地等高线文件的文件选择过滤器
     */
    public static class ContourFilePicker extends FilePicker {
        public ContourFilePicker() {
            setFileDisplayFilter(new FilterByFileExtension(".json"));
        }
    }

    /**
     * 选择本地地图样式文件的文件选择过滤器
     */
    public static class ThemeFilePicker extends FilePicker {
        public ThemeFilePicker() {
            setFileDisplayFilter(new FilterByFileExtension(".xml"));
            setFileSelectFilter(new ValidRenderTheme());
        }
    }

    /**
     * 选择本地地图文件的文件选择过滤器
     */
    public static class GeoJsonFilePicker extends FilePicker {
        public GeoJsonFilePicker() {
            setFileDisplayFilter(new FilterByFileExtension(".json"));
            setFileSelectFilter(null);
        }
    }

    @Override
    public void onBackPressedSupport() {
        super.onBackPressedSupport();
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int error, String reason) {
        //通过腾讯定位获取到位置信息
        if (TencentLocation.ERROR_OK == error) {
            // 定位成功,更新当前的位置信息，如果是第一次定位，则自动将屏幕中心位置设置为当前位置
            currentLocation = tencentLocation;
            Message msg = new Message();
            msg.obj = tencentLocation;
            msg.what = SystemConstant.MSG_WHAT_LOCATION_UPDATE;
            EventBus.getDefault().post(msg);
        }
    }

    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - firstTime > 2000) {
                RxToast.info("再按一次退出程序");
                firstTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {
        //定位设备状态信息更新
    }

    public TencentLocation getCurrentLocation() {
        return currentLocation;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    /**
     * 设置当前正在作业的项目
     */
    public void setCurrentProject() {
        //主界面加载后，首先获取当前用户的项目列表，需要用户从中选择要操作的项目
        final RxDialogLoading rxDialogLoading = new RxDialogLoading(this);
        OkGo.<String>get(SystemConstant.URL_PROJECTS_LIST).tag(this).converter(new StringConvert()).adapt(new ObservableResponse<String>()).subscribeOn(Schedulers.io()).doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {
                rxDialogLoading.show();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Response<String>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Response<String> stringResponse) {
                String resultStr = stringResponse.body();
                DataFromNet<List<Project>> projectFromNet = JSON.parseObject(resultStr, new TypeReference<DataFromNet<List<Project>>>() {
                }.getType());
                if (projectFromNet != null) {
                    final List<Project> projectList = projectFromNet.getData();
                    if (projectList != null && !projectList.isEmpty()) {
                        String[] projectNames = new String[projectList.size()];
                        int checkedItem = 0;
                        for (int i = 0; i < projectList.size(); i++) {
                            projectNames[i] = projectList.get(i).getName();
                            if (projectList.get(i).getId() == SystemConstant.CURRENT_PROJECTS_ID) {
                                checkedItem = i;
                            }
                        }

                        new CanDialog.Builder(MainActivity.this).setTitle("请选择要作业的项目").setSingleChoiceItems(projectNames, checkedItem, null).setPositiveButton("确定", true, new CanDialogInterface.OnClickListener() {
                            @Override
                            public void onClick(CanDialog dialog, int checkItem, CharSequence text, boolean[] checkItems) {
                                if (SystemConstant.CURRENT_PROJECTS_ID != projectList.get(checkItem).getId()) {
                                    //清空map中的layer
                                    if (mainFragment != null) {
                                        mainFragment.clearAllMapLayers();
                                        //清空当前已选中的图层
                                        if (mainFragment.getLayerDataBeanList() != null) {
                                            mainFragment.getLayerDataBeanList().clear();
                                        }

                                        //隐藏顶部时间序列选择控件
                                        if (mainFragment.getMultiTimeLayerList() != null) {
                                            mainFragment.getMultiTimeLayerList().clear();
                                        }
                                        if (mainFragment.findChildFragment(MultiTimeLayerSelectFragment.class) != null) {
                                            mainFragment.popToChild(MultiTimeLayerSelectFragment.class, true);
                                        }

                                    }

                                    if (SystemConstant.CURRENT_PROJECTS_ID != -1) {
                                        RxToast.info("切换项目，自动清除当前地图所有图层");
                                    }
                                    SystemConstant.CURRENT_PROJECTS_ID = projectList.get(checkItem).getId();
                                } else {
                                    RxToast.info("未切换项目，仍保持当前项目继续作业");
                                }
                            }
                        }).setCancelable(true).show();
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                RxToast.info("请求失败，请检查网络!", Toast.LENGTH_SHORT);
                RxLogTool.saveLogFile(e.toString());
                if (rxDialogLoading != null && rxDialogLoading.isShowing()) {
                    rxDialogLoading.dismiss();
                }
            }

            @Override
            public void onComplete() {
                if (rxDialogLoading != null && rxDialogLoading.isShowing()) {
                    rxDialogLoading.dismiss();
                }
            }
        });
    }

    private void initScaleBar() {
        //scale的图层到操作分组中
        CatEyeMapScaleBar mMapScaleBar = new CatEyeMapScaleBar(mMap);
        mMapScaleBar.setScaleBarMode(CatEyeMapScaleBar.ScaleBarMode.BOTH);
        mMapScaleBar.setDistanceUnitAdapter(MetricUnitAdapter.INSTANCE);
        mMapScaleBar.setSecondaryDistanceUnitAdapter(ImperialUnitAdapter.INSTANCE);
        mMapScaleBar.setScaleBarPosition(MapScaleBar.ScaleBarPosition.BOTTOM_LEFT);

        MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(mMap, mMapScaleBar);
        BitmapRenderer renderer = mapScaleBarLayer.getRenderer();
        renderer.setPosition(GLViewport.Position.BOTTOM_LEFT);
        renderer.setOffset(5 * CanvasAdapter.getScale(), 0);
        mMap.layers().add(mapScaleBarLayer, LAYER_GROUP_ENUM.OPERTOR_GROUP.orderIndex);
    }
}
