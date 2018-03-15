package com.cateye.vtm.fragment;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.cateye.android.vtm.R;
import com.jkb.fragment.rigger.annotation.Puppet;

import org.oscim.android.MapView;
import org.oscim.android.filepicker.FilePicker;
import org.oscim.backend.CanvasAdapter;
import org.oscim.core.MapElement;
import org.oscim.core.MapPosition;
import org.oscim.core.Tile;
import org.oscim.layers.tile.MapTile;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.buildings.S3DBLayer;
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
import org.oscim.theme.ExternalRenderTheme;
import org.oscim.theme.ThemeUtils;
import org.oscim.theme.styles.AreaStyle;
import org.oscim.theme.styles.RenderStyle;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.tiling.source.mapfile.MapInfo;

/**
 * Created by zhangdezhi1702 on 2018/3/15.
 */
@Puppet
public class CatEyeMainFragment extends BaseFragment {
    private MapView mapView;//地图控件
    private Map mMap;
    private Button btn_select_local_map_file;//选择需要显示的本地map文件

    @Override
    public int getFragmentLayoutId() {
        return R.layout.fragment_main_cateye;
    }

    @Override
    public void initView(View rootView) {
        mapView = rootView.findViewById(R.id.mapView);
        mMap = mapView.map();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == SELECT_MAP_FILE) {
            if (resultCode != RESULT_OK || intent == null || intent.getStringExtra(FilePicker.SELECTED_FILE) == null) {
                finish();
                return;
            }

            mTileSource = new MapFileTileSource();
            //mTileSource.setPreferredLanguage("en");
            String file = intent.getStringExtra(FilePicker.SELECTED_FILE);
            if (mTileSource.setMapFile(file)) {

                mTileLayer = mMap.setBaseMap(mTileSource);
                loadTheme(null);

                if (mS3db)
                    mMap.layers().add(new S3DBLayer(mMap, mTileLayer));
                else
                    mMap.layers().add(new BuildingLayer(mMap, mTileLayer));
                mMap.layers().add(new LabelLayer(mMap, mTileLayer));

                mMapScaleBar = new DefaultMapScaleBar(mMap);
                mMapScaleBar.setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.BOTH);
                mMapScaleBar.setDistanceUnitAdapter(MetricUnitAdapter.INSTANCE);
                mMapScaleBar.setSecondaryDistanceUnitAdapter(ImperialUnitAdapter.INSTANCE);
                mMapScaleBar.setScaleBarPosition(MapScaleBar.ScaleBarPosition.BOTTOM_LEFT);

                MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(mMap, mMapScaleBar);
                BitmapRenderer renderer = mapScaleBarLayer.getRenderer();
                renderer.setPosition(GLViewport.Position.BOTTOM_LEFT);
                renderer.setOffset(5 * CanvasAdapter.getScale(), 0);
                mMap.layers().add(mapScaleBarLayer);

                MapInfo info = mTileSource.getMapInfo();
                MapPosition pos = new MapPosition();
                pos.setByBoundingBox(info.boundingBox, Tile.SIZE * 4, Tile.SIZE * 4);
                mMap.setMapPosition(pos);

                mPrefs.clear();
            }
        } else if (requestCode == SELECT_THEME_FILE) {
            if (resultCode != RESULT_OK || intent == null || intent.getStringExtra(FilePicker.SELECTED_FILE) == null) {
                return;
            }

            String file = intent.getStringExtra(FilePicker.SELECTED_FILE);
            ExternalRenderTheme externalRenderTheme = new ExternalRenderTheme(file);

            // Use tessellation with sea and land for Mapsforge themes
            if (ThemeUtils.isMapsforgeTheme(externalRenderTheme)) {
                mTileLayer.addHook(new VectorTileLayer.TileLoaderThemeHook() {
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
            mMap.setTheme(externalRenderTheme);
            mMenu.findItem(R.id.theme_external).setChecked(true);
        }
    }
}
