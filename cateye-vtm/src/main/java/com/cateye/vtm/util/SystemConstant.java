package com.cateye.vtm.util;

public class SystemConstant {
    public static final int MSG_WHAT_DRAW_POINT_LINE_POLYGON_DESTROY = 0x1001;//绘制点线面
    public static final int MSG_WHAT_LOCATION_UPDATE = 0x1002;//位置更新
    public static final int MSG_WHAT_MAIN_AREA_HIDEN_VISIBLE = 0x1003;//显示隐藏主界面上的某些元素
    public static final int MSG_WHAT_DRAW_POINT_LINE_POLYGON_TAP = 0x1004;//用户在绘制界面点击
    public static final int MSG_WHAT_DRAW_RESULT = 0x1005;//用户在绘制界面点击

    public static final String BASE_URL = "http://39.107.104.63:8080";
    public static final String URL_MAP_SOURCE_NET = BASE_URL + "/meta/maps";
    public static final String URL_CONTOUR_CALCULATE = BASE_URL + "/dem/contour";

    public static final String DATA_CONTOUR_CHART = "DATA_CONTOUR_CHART";
    public static final String BUNDLE_AREA_HIDEN_STATE = "BUNDLE_AREA_HIDEN_STATE";//主界面上部分区域的显隐状态，隐藏或显示
    public static final String BUNDLE_BUTTON_AREA = "BUNDLE_BUTTON_AREA";//主界面上控制的显隐区域
    public static final long SCREEN_MOVE_BOUNDARY = 20;//判断点击屏幕时是否为移动事件的边界值

    public static final String DRAW_POINT_LIST = "DRAW_POINT_LIST";//绘制点线面后的点位集合
    public static final String DRAW_USAGE = "DRAW_USAGE";//绘制点线面的用处
    public static final int DRAW_CONTOUR_LINE = 1;//绘制等高线的线段
    public static final String LATITUDE = "LATITUDE";//latitude
    public static final String LONGITUDE = "LONGITUDE";//longitude
}
