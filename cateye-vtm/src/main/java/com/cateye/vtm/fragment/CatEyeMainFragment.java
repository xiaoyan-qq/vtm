package com.cateye.vtm.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.cateye.android.vtm.MainActivity;
import com.cateye.android.vtm.MainActivity.LAYER_GROUP_ENUM;
import com.cateye.android.vtm.R;
import com.cateye.vtm.util.CatEyeMapManager;
import com.jkb.fragment.rigger.annotation.Puppet;
import com.jkb.fragment.rigger.rigger.Rigger;
import com.ta.utdid2.android.utils.StringUtils;
import com.vondear.rxtools.RxLogTool;
import com.vondear.rxtools.view.RxToast;

import org.jeo.map.Style;
import org.jeo.vector.VectorDataset;
import org.oscim.android.MapPreferences;
import org.oscim.android.MapView;
import org.oscim.android.cache.TileCache;
import org.oscim.android.filepicker.FilePicker;
import org.oscim.android.theme.AssetsRenderTheme;
import org.oscim.backend.CanvasAdapter;
import org.oscim.backend.canvas.Color;
import org.oscim.core.MapElement;
import org.oscim.core.MapPosition;
import org.oscim.core.Tag;
import org.oscim.core.Tile;
import org.oscim.layers.Layer;
import org.oscim.layers.OSMIndoorLayer;
import org.oscim.layers.TileGridLayer;
import org.oscim.layers.tile.MapTile;
import org.oscim.layers.tile.bitmap.BitmapTileLayer;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.buildings.S3DBLayer;
import org.oscim.layers.tile.vector.OsmTileLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.renderer.BitmapRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.renderer.bucket.RenderBuckets;
import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.ImperialUnitAdapter;
import org.oscim.scalebar.MapScaleBar;
import org.oscim.scalebar.MapScaleBarLayer;
import org.oscim.scalebar.MetricUnitAdapter;
import org.oscim.test.JeoTest;
import org.oscim.theme.ExternalRenderTheme;
import org.oscim.theme.ThemeUtils;
import org.oscim.theme.VtmThemes;
import org.oscim.theme.XmlRenderThemeMenuCallback;
import org.oscim.theme.XmlRenderThemeStyleLayer;
import org.oscim.theme.XmlRenderThemeStyleMenu;
import org.oscim.theme.styles.AreaStyle;
import org.oscim.theme.styles.RenderStyle;
import org.oscim.theme.styles.TextStyle;
import org.oscim.tiling.TileSource;
import org.oscim.tiling.source.bitmap.BitmapTileSource;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.tiling.source.mapfile.MapInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by zhangdezhi1702 on 2018/3/15.
 */
@Puppet
public class CatEyeMainFragment extends BaseFragment {
    private MapView mapView;//地图控件
    private Map mMap;
    private MapPreferences mPrefs;

    static final int SELECT_MAP_FILE = 0;
    static final int SELECT_THEME_FILE = SELECT_MAP_FILE + 1;
    static final int SELECT_GEOJSON_FILE = SELECT_MAP_FILE + 2;

    private static final Tag ISSEA_TAG = new Tag("natural", "issea");
    private static final Tag NOSEA_TAG = new Tag("natural", "nosea");
    private static final Tag SEA_TAG = new Tag("natural", "sea");

    private TileGridLayer mGridLayer;
    private Menu mMenu;
    private boolean mS3db;
    private List<TileSource> mTileSourceList;//当前正在显示的tileSource的集合

    //控件
    private Button btn_select_local_map_file;//选择需要显示的本地map文件
    private Button btn_select_net_map_file;//选择需要显示的在线map文件
    private Button btn_select_geoJson_file;//选择需要显示的geoJson文件
    private Button btn_draw_plp;//绘制点线面

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_main_cateye;
    }

    @Override
    public void initView(View rootView) {
        mapView = rootView.findViewById(R.id.mapView);

        //初始化MapManager，方便全局使用map对象
        CatEyeMapManager.getInstance(getActivity()).init(mapView);

        mMap = mapView.map();
        mPrefs = new MapPreferences(this.getTag(), getActivity());
        mTileSourceList = new ArrayList<>();

        //选择底图map文件
        btn_select_local_map_file = rootView.findViewById(R.id.btn_select_local_map_file);
        btn_select_local_map_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), MainActivity.MapFilePicker.class),
                        SELECT_MAP_FILE);
            }
        });
        //向地图中添加地图图层分组
        for (LAYER_GROUP_ENUM group_enum : LAYER_GROUP_ENUM.values()) {
            mMap.layers().addGroup(group_enum.ordinal());
        }

        //开始绘制点线面
        btn_draw_plp = rootView.findViewById(R.id.btn_draw_plp);
        btn_draw_plp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //自动弹出绘制点线面的fragment
                Rigger.getRigger(CatEyeMainFragment.this).showFragment(DrawPointLinePolygonFragment.newInstance(new Bundle()), R.id.layer_main_cateye_bottom);
            }
        });

        //选择网络地图显示
        btn_select_net_map_file = rootView.findViewById(R.id.btn_select_net_map_file);
        btn_select_net_map_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BitmapTileSource mTileSource = BitmapTileSource.builder()
                        .url("http://39.107.104.63:8080/tms/1.0.0/world_satellite_raster@EPSG:900913@jpeg").tilePath("/{Z}/{X}/{Y}.png")
                        .zoomMax(18).build();
//                BitmapTileSource mTileSource= DefaultSources.OPENSTREETMAP.build();
                createTileLayer(getActivity(), mTileSource, true);
            }
        });

        //选择显示GeoJson文件
        btn_select_geoJson_file=rootView.findViewById(R.id.btn_select_geoJson);
        btn_select_geoJson_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), MainActivity.GeoJsonFilePicker.class),
                        SELECT_GEOJSON_FILE);
            }
        });

        //scale的图层到操作分组中
        DefaultMapScaleBar mMapScaleBar = new DefaultMapScaleBar(mMap);
        mMapScaleBar.setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.BOTH);
        mMapScaleBar.setDistanceUnitAdapter(MetricUnitAdapter.INSTANCE);
        mMapScaleBar.setSecondaryDistanceUnitAdapter(ImperialUnitAdapter.INSTANCE);
        mMapScaleBar.setScaleBarPosition(MapScaleBar.ScaleBarPosition.BOTTOM_LEFT);

        MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(mMap, mMapScaleBar);
        BitmapRenderer renderer = mapScaleBarLayer.getRenderer();
        renderer.setPosition(GLViewport.Position.BOTTOM_LEFT);
        renderer.setOffset(5 * CanvasAdapter.getScale(), 0);
        mMap.layers().add(mapScaleBarLayer, LAYER_GROUP_ENUM.GROUP_OPERTOR.ordinal());
    }

    public static BaseFragment newInstance(Bundle bundle) {
        CatEyeMainFragment catEyeMainFragment = new CatEyeMainFragment();
        catEyeMainFragment.setArguments(bundle);
        return catEyeMainFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == SELECT_MAP_FILE) {//选择本地地图文件显示
            if (resultCode != getActivity().RESULT_OK || intent == null || intent.getStringExtra(FilePicker.SELECTED_FILE) == null) {
//                finish();
                return;
            }

            MapFileTileSource mTileSource = new MapFileTileSource();
//            mTileSource.setPreferredLanguage("zh");
            String file = intent.getStringExtra(FilePicker.SELECTED_FILE);
            //过滤判断选中的文件是否已经在显示中了
            if (mTileSourceList != null && !mTileSourceList.isEmpty()) {
                for (TileSource tileSource : mTileSourceList) {
                    if (tileSource instanceof MapFileTileSource && ((MapFileTileSource) tileSource).getOption("file").equals(file)) {
                        RxToast.error(getActivity().getResources().getString(R.string.the_local_map_file_exists));
                        return;
                    }
                }
            }

            if (mTileSource.setMapFile(file)) {

                //设置当前的文件选择的layer为地图的基础图层(第一层)==此处去掉此设置
                //VectorTileLayer mTileLayer = mMap.setBaseMap(mTileSource);
                VectorTileLayer mTileLayer = new OsmTileLayer(mMap);
                mTileLayer.setTileSource(mTileSource);
                mMap.layers().add(mTileLayer, LAYER_GROUP_ENUM.GROUP_VECTOR.ordinal());

                if (mS3db)
                    mMap.layers().add(new S3DBLayer(mMap, mTileLayer), LAYER_GROUP_ENUM.GROUP_3D_OBJECTS.ordinal());
                else
                    mMap.layers().add(new BuildingLayer(mMap, mTileLayer), LAYER_GROUP_ENUM.GROUP_BUILDING.ordinal());
                mMap.layers().add(new LabelLayer(mMap, mTileLayer), LAYER_GROUP_ENUM.GROUP_LABELS.ordinal());

                MapInfo info = mTileSource.getMapInfo();
                MapPosition pos = new MapPosition();
                pos.setByBoundingBox(info.boundingBox, Tile.SIZE * 4, Tile.SIZE * 4);
                mMap.setMapPosition(pos);
                loadTheme(null, true);

                mPrefs.clear();
                mTileSourceList.add(mTileSource);
            }
        } else if (requestCode == SELECT_THEME_FILE) {//选择本地style文件显示
            if (resultCode != getActivity().RESULT_OK || intent == null || intent.getStringExtra(FilePicker.SELECTED_FILE) == null) {
                return;
            }

            String file = intent.getStringExtra(FilePicker.SELECTED_FILE);
            ExternalRenderTheme externalRenderTheme = new ExternalRenderTheme(file);

            // Use tessellation with sea and land for Mapsforge themes
            if (ThemeUtils.isMapsforgeTheme(externalRenderTheme)) {
                //遍历所有的地图图层，添加hook
                if (mMap.layers() != null && !mMap.layers().isEmpty()) {
                    for (Layer layer : mMap.layers()) {
                        if (layer.isEnabled() && layer instanceof OsmTileLayer)
                            ((OsmTileLayer) layer).addHook(new VectorTileLayer.TileLoaderThemeHook() {
                                @Override
                                public boolean process(MapTile tile, RenderBuckets buckets, MapElement element, RenderStyle style, int level) {
                                    if (element.tags.contains(ISSEA_TAG) || element.tags.contains(SEA_TAG) || element.tags.contains(NOSEA_TAG)) {
                                        if (style instanceof AreaStyle)
                                            ((AreaStyle) style).mesh = true;
                                    }
                                    return false;
                                }

                                @Override
                                public void complete(MapTile tile, boolean success) {
                                }
                            });
                    }
                }

            }
            mMap.setTheme(externalRenderTheme);
//            mMenu.findItem(R.id.theme_external).setChecked(true);
        }else if (requestCode==SELECT_GEOJSON_FILE){
            if (resultCode != getActivity().RESULT_OK || intent == null || intent.getStringExtra(FilePicker.SELECTED_FILE) == null) {
                return;
            }
            String filePath = intent.getStringExtra(FilePicker.SELECTED_FILE);
            File geoJsonFile=new File(filePath);
            if (geoJsonFile.exists()&&geoJsonFile.isFile()){
                FileInputStream geoInputStream=null;
                try {
                    geoInputStream=new FileInputStream(geoJsonFile);
                    loadJson(geoInputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 设置地图样式
     */
    protected void loadTheme(final String styleId, boolean isAllLayers) {
        if (!StringUtils.isEmpty(styleId)) {
            mMap.setTheme(new AssetsRenderTheme(getActivity().getAssets(), "", "vtm/stylemenu.xml", new XmlRenderThemeMenuCallback() {
                @Override
                public Set<String> getCategories(XmlRenderThemeStyleMenu renderThemeStyleMenu) {
                    // Use the selected style or the default
                    String style = styleId != null ? styleId : renderThemeStyleMenu.getDefaultValue();

                    // Retrieve the layer from the style id
                    XmlRenderThemeStyleLayer renderThemeStyleLayer = renderThemeStyleMenu.getLayer(style);
                    if (renderThemeStyleLayer == null) {
                        System.err.println("Invalid style " + style);
                        return null;
                    }

                    // First get the selected layer's categories that are enabled together
                    Set<String> categories = renderThemeStyleLayer.getCategories();

                    // Then add the selected layer's overlays that are enabled individually
                    // Here we use the style menu, but users can use their own preferences
                    for (XmlRenderThemeStyleLayer overlay : renderThemeStyleLayer.getOverlays()) {
                        if (overlay.isEnabled())
                            categories.addAll(overlay.getCategories());
                    }

                    // This is the whole categories set to be enabled
                    return categories;
                }
            }), isAllLayers);
        } else {
            mMap.setTheme(VtmThemes.DEFAULT, isAllLayers);
        }
    }

    private void createTileLayer(Context mContext, BitmapTileSource mTileSource, boolean USE_CACHE) {
        if (mTileSource == null)
            return;

        if (USE_CACHE) {
            String cacheFile = mTileSource.getUrl()
                    .toString()
                    .replaceFirst("https?://", "")
                    .replaceAll("/", "-");

            RxLogTool.i("use bitmap cache {}", cacheFile);
            TileCache mCache = new TileCache(mContext, null, cacheFile);
            mCache.setCacheSize(512 * (1 << 10));
            mTileSource.setCache(mCache);
        }

        BitmapTileLayer mBitmapLayer = new BitmapTileLayer(mMap, mTileSource);
        mMap.layers().add(mBitmapLayer, LAYER_GROUP_ENUM.GROUP_VECTOR.ordinal());
        mMap.updateMap(true);
    }

    /**
     * 加载指定的GeoJsonlayer
     * */
    private void loadJson(InputStream is) {
        VectorDataset data = JeoTest.readGeoJson(is);
        Style style = JeoTest.getStyle();
        TextStyle textStyle = TextStyle.builder()
                .isCaption(true)
                .fontSize(16 * CanvasAdapter.getScale()).color(Color.BLACK)
                .strokeWidth(2.2f * CanvasAdapter.getScale()).strokeColor(Color.WHITE)
                .build();
        OSMIndoorLayer mIndoorLayer = new OSMIndoorLayer(mMap, data, style, textStyle);
        mMap.layers().add(mIndoorLayer);

        mMap.updateMap(true);
    }
//    public void onRiggerBackPressed() {
//        RxLogTool.d("onRiggerBackPressed", "点击回退按钮");
//
//    }
}
