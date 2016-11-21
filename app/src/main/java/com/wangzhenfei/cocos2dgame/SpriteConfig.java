package com.wangzhenfei.cocos2dgame;

/**
 * Created by wangzhenfei on 2016/11/10.
 */
public class SpriteConfig {
    public final static int TAG_DESTORY = -100;
    // 玻璃块
    public final static int OFFSET_BRICK_AND_BG = 100;
    public final static int NORMAL_BRICK_SIZE = 92;

    public final static int TAG_NROMAL_BRICK = 1 * 1000;
    public final static int TAG_MY_SPECIAL_BRICK1 = 2 * 1000;
    public final static int TAG_MY_SPECIAL_BRICK2 = 3 * 1000;
    public final static int TAG_MY_SPECIAL_BRICK3 = 4 * 1000;
    public final static int TAG_OFFSET_NROMAL_BRICK1 = 5 * 1000;
    public final static int TAG_OFFSET_SPECIAL_BRICK1 = 6 * 1000;
    public final static int TAG_OFFSET_SPECIAL_BRICK2 = 7 * 1000;
    public final static int TAG_OFFSET_SPECIAL_BRICK3 = 8 * 1000;

    // 普通横杆
    public final static int NORMAL_CONTROL_BAR_W = 213;
    public final static int EXPEND_CONTROL_BAR_W = (int) (213 * 1.5);
    public  static int CONTROL_BAR_W = NORMAL_CONTROL_BAR_W;
    public final static int NORMAL_CONTROL_BAR_H = 48;

    public final static int TAG_MY_NORMAL_CONTROL_BAR = 9 * 1000;
    public final static int TAG_OFFSET_NORMAL_CONTROL_BAR = 10 * 1000;
    // 球
    public final static int BALL_SIZE = 80;
    public final static int TAG_NORMAL_BALL = 11 * 1000;
    public final static int TAG_ADD_BALL1 = 17 * 1000;
    public final static int TAG_ADD_BALL2 = 18 * 1000;
    // 主堡垒
    public final static int NORMAL_HOME_BRICK_SIZE = 184;
    public final static int TAG_MY_NORMAL_HOME_BRICK = 12 * 1000;
    public final static int TAG_OFFSET_NORMAL_HOME_BRICK = 13 * 1000;

    //道具
    public final static int PROP_SIZE = 95;
    public final static int TAG_PROP_EXPEND_LARGE = 14 * 1000;
    public final static int TAG_PROP_MORE_BALL = 15 * 1000;
    public final static int TAG_PROP_ACCELERATE = 16 * 1000;

    //全局的速度
    public final static float v = 1f;//
}
