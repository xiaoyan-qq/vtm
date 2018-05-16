package com.cateye.android.vtm;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import com.cateye.vtm.fragment.CatEyeMainFragment;
import com.cateye.vtm.util.SystemConstant;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.vondear.rxtools.view.dialog.RxDialogSure;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RequestExecutor;

import org.greenrobot.eventbus.EventBus;
import org.oscim.android.filepicker.FilePicker;
import org.oscim.android.filepicker.FilterByFileExtension;
import org.oscim.android.filepicker.ValidMapFile;
import org.oscim.android.filepicker.ValidRenderTheme;

import java.util.List;

import me.yokeyword.fragmentation.SupportActivity;

//@Puppet(containerViewId = R.id.fragment_main_container, bondContainerView = true)
public class MainActivity extends SupportActivity implements TencentLocationListener {
    private TencentLocation currentLocation;

    //地图layer的分组
    public enum LAYER_GROUP_ENUM {
        GROUP_BASE/*基础图层*/, GROUP_VECTOR/*矢量图层分组*/, GROUP_OTHER/*其他图层分组*/, GROUP_BUILDING/*建筑图层分组*/,
        GROUP_LABELS/*label图层分组*/, GROUP_3D_OBJECTS/*3D图层分组*/, GROUP_OPERTOR/*操作图层分组*/, GROUP_LOCATION/*当前位置分组*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //启动fragment，显示地图界面
//        Rigger.getRigger(this).startFragment(CatEyeMainFragment.newInstance(new Bundle()));
//        startFragment(CatEyeMainFragment.class);
        loadRootFragment(R.id.fragment_main_container, CatEyeMainFragment.newInstance(new Bundle()));
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

    @Override
    public void onStatusUpdate(String s, int i, String s1) {
        //定位设备状态信息更新
    }

    public TencentLocation getCurrentLocation() {
        return currentLocation;
    }
}
