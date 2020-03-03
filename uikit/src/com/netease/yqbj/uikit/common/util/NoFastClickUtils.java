package com.netease.yqbj.uikit.common.util;

/**
 * 作者：guoyzh
 * 时间：2018年6月14日
 * 功能：判断是否是进行了快速点击的操作
 */

public class NoFastClickUtils {
    private static long lastClickTime = 0;//上次点击的时间
    private static int spaceTime = 1000;//时间间隔

    public static boolean isFastClick() {

        long currentTime = System.currentTimeMillis();//当前系统时间
        boolean isAllowClick;//是否允许点击

        if (currentTime - lastClickTime > spaceTime) {

            isAllowClick = false;
            lastClickTime = currentTime;

        } else {
            isAllowClick = true;

        }


        return isAllowClick;
    }

}
