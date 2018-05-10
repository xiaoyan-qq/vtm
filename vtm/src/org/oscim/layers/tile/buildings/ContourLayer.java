/*
 * Copyright 2013 Hannes Janetzek
 * Copyright 2016-2018 devemux86
 * Copyright 2016 Robin Boldt
 * Copyright 2017 Gustl22
 *
 * This file is part of the OpenScienceMap project (http://www.opensciencemap.org).
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.oscim.layers.tile.buildings;

import org.oscim.core.MapElement;
import org.oscim.core.Tag;
import org.oscim.layers.Layer;
import org.oscim.layers.tile.MapTile;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.VectorTileLayer.TileLoaderThemeHook;
import org.oscim.map.Map;
import org.oscim.renderer.OffscreenRenderer;
import org.oscim.renderer.OffscreenRenderer.Mode;
import org.oscim.renderer.bucket.ExtrusionBuckets;
import org.oscim.renderer.bucket.RenderBuckets;
import org.oscim.theme.styles.ExtrusionStyle;
import org.oscim.theme.styles.RenderStyle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * 显示等高线的图层
 * */
public class ContourLayer extends Layer implements TileLoaderThemeHook {

    protected final static int BUILDING_LEVEL_HEIGHT = 280; // cm

    protected final static int MIN_ZOOM = 17;
    protected final static int MAX_ZOOM = 17;

    public static boolean POST_AA = false;
    public static boolean TRANSLUCENT = true;

    private static final Object CONTOUR_DATA = ContourLayer.class.getName();

    // Can be replaced with Multimap in Java 8
    protected java.util.Map<Integer, List<ContourElement>> mContours = new HashMap<>();

    class ContourElement {
        MapElement element;
        ExtrusionStyle style;

        ContourElement(MapElement element, ExtrusionStyle style) {
            this.element = element;
            this.style = style;
        }
    }

    public ContourLayer(Map map, VectorTileLayer tileLayer) {
        this(map, tileLayer, 8, 20, false);
    }

    public ContourLayer(Map map, VectorTileLayer tileLayer, boolean mesh) {
        this(map, tileLayer, 8, 20, mesh);
    }

    public ContourLayer(Map map, VectorTileLayer tileLayer, int zoomMin, int zoomMax, boolean mesh) {

        super(map);

        tileLayer.addHook(this);

        mRenderer = new ContourRenderer(tileLayer.tileRenderer(),
                zoomMin, zoomMax,
                mesh, !mesh && TRANSLUCENT); // alpha must be disabled for mesh renderer
        if (POST_AA)
            mRenderer = new OffscreenRenderer(Mode.SSAO_FXAA, mRenderer);
    }

    /**
     * TileLoaderThemeHook
     */
    @Override
    public boolean process(MapTile tile, RenderBuckets buckets, MapElement element,
                           RenderStyle style, int level) {
        // FIXME check why some buildings are processed up to 4 times (should avoid overhead)
        // FIXME fix artifacts at tile borders

        if (!(style instanceof ExtrusionStyle))
            return false;

        ExtrusionStyle extrusion = (ExtrusionStyle) style.current();

        // Filter all building elements
        // TODO #TagFromTheme: load from theme or decode tags to generalize mapsforge tags
        if (element.isContour() || element.isContourPart()) {
            List<ContourElement> contourElements = mContours.get(tile.hashCode());
            if (contourElements == null) {
                contourElements = new ArrayList<>();
                mContours.put(tile.hashCode(), contourElements);
            }
            element = new MapElement(element); // Deep copy, because element will be cleared
            contourElements.add(new ContourElement(element, extrusion));
            return true;
        }

        // Process other elements immediately
        processElement(element, extrusion, tile);

        return true;
    }

    /**
     * Process map element.
     *
     * @param element   the map element
     * @param extrusion the style of map element
     * @param tile      the tile which contains map element
     */
    protected void processElement(MapElement element, ExtrusionStyle extrusion, MapTile tile) {
        int height = 0; // cm
        int minHeight = 0; // cm

        String v = element.tags.getValue(Tag.KEY_HEIGHT);
        if (v != null)
            height = (int) (Float.parseFloat(v) * 100);
        else {
            // #TagFromTheme: generalize level/height tags
            if ((v = element.tags.getValue(Tag.KEY_BUILDING_LEVELS)) != null)
                height = (int) (Float.parseFloat(v) * BUILDING_LEVEL_HEIGHT);
        }

        v = element.tags.getValue(Tag.KEY_MIN_HEIGHT);
        if (v != null)
            minHeight = (int) (Float.parseFloat(v) * 100);
        else {
            // #TagFromTheme: level/height tags
            if ((v = element.tags.getValue(Tag.KEY_BUILDING_MIN_LEVEL)) != null)
                minHeight = (int) (Float.parseFloat(v) * BUILDING_LEVEL_HEIGHT);
        }

        if (height == 0)
            height = extrusion.defaultHeight * 100;

        ExtrusionBuckets ebs = get(tile);
        ebs.addPolyElement(element, tile.getGroundScale(), extrusion.colors, height, minHeight);
    }

    /**
     * Process all stored map elements (here only buildings).
     *
     * @param tile the tile which contains stored map elements
     */
    protected void processElements(MapTile tile) {
        if (!mContours.containsKey(tile.hashCode()))
            return;

        List<ContourElement> tileContours = mContours.get(tile.hashCode());
        Set<ContourElement> rootContours = new HashSet<>();
        for (ContourElement partContour : tileContours) {
            if (!partContour.element.isContourPart())
                continue;

            String refId = partContour.element.tags.getValue(Tag.KEY_REF); // #TagFromTheme
            refId = refId == null ? partContour.element.tags.getValue("root_id") : refId; // Mapzen
            if (refId == null)
                continue;

            // Search buildings which inherit parts
            for (ContourElement rootContour : tileContours) {
                if (rootContour.element.isContourPart()
                        || !(refId.equals(rootContour.element.tags.getValue(Tag.KEY_ID))))
                    continue;

                rootContours.add(rootContour);
                break;
            }
        }

        tileContours.removeAll(rootContours); // root buildings aren't rendered

        for (ContourElement contourElement : tileContours) {
            processElement(contourElement.element, contourElement.style, tile);
        }
        mContours.remove(tile.hashCode());
    }

    /**
     * @param tile the MapTile
     * @return ExtrusionBuckets of the tile
     */
    public static ExtrusionBuckets get(MapTile tile) {
        ExtrusionBuckets ebs = (ExtrusionBuckets) tile.getData(CONTOUR_DATA);
        if (ebs == null) {
            ebs = new ExtrusionBuckets(tile);
            tile.addData(CONTOUR_DATA, ebs);
        }
        return ebs;
    }

    @Override
    public void complete(MapTile tile, boolean success) {
        if (success) {
            processElements(tile);
            get(tile).prepare();
        } else
            get(tile).resetBuckets(null);
    }

    //    private int multi;
    //    @Override
    //    public void onInputEvent(Event event, MotionEvent e) {
    //        int action = e.getAction() & MotionEvent.ACTION_MASK;
    //        if (action == MotionEvent.ACTION_POINTER_DOWN) {
    //            multi++;
    //        } else if (action == MotionEvent.ACTION_POINTER_UP) {
    //            multi--;
    //            if (!mActive && mAlpha > 0) {
    //                // finish hiding
    //                //log.debug("add multi hide timer " + mAlpha);
    //                addShowTimer(mFadeTime * mAlpha, false);
    //            }
    //        } else if (action == MotionEvent.ACTION_CANCEL) {
    //            multi = 0;
    //            log.debug("cancel " + multi);
    //            if (mTimer != null) {
    //                mTimer.cancel();
    //                mTimer = null;
    //            }
    //        }
    //    }

}
