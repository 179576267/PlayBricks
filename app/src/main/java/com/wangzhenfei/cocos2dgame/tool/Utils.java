package com.wangzhenfei.cocos2dgame.tool;

/**
 * Created by wangzhenfei on 2016/11/10.
 */
public class Utils {

    public static boolean isCollsionWithRect(float x1, float y1, float w1, float h1, float x2, float
            y2, float w2, float h2) {
        //当矩形1 位于矩形2 的右侧
        if (x1 > x2 + w2) {
            return false;
            //当矩形1 位于矩形2 的左侧
        } else if (x1 + w1 < x2) {
            return false;
            //当矩形1 位于矩形2 的下方
        } else if (y1 > y2 + h2) {
            return false;
            //当矩形1 位于矩形2 的上方
        } else if (y1 + h1 < y2) {
            return false;
        }
        return true;
    }
}
