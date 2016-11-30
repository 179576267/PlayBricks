package com.wangzhenfei.cocos2dgame.config;

/**
 * Created by wangzhenfei on 2016/11/10.
 */
public class SpriteConfig {
    // 玻璃块
    public final static int NORMAL_BRICK_SIZE = (int)(720 * 0.135);
    // 普通横杆
    public final static int CONTROL_TO_BRICK = (int)(1280 * 0.006);
    public final static int NORMAL_CONTROL_BAR_W = (int)(0.3 * 720);
    public final static int EXPEND_CONTROL_BAR_W = (int) (NORMAL_CONTROL_BAR_W * 1.5);
    public  static int CONTROL_BAR_W = NORMAL_CONTROL_BAR_W;
    public final static int NORMAL_CONTROL_BAR_H = (int)(0.037* 1280);

    public final static int TAG_MY_NORMAL_CONTROL_BAR = 9 * 1000;
    public final static int TAG_OFFSET_NORMAL_CONTROL_BAR = 10 * 1000;
    // 球
    public final static int BALL_SIZE = (int)(0.11 * 720);
    public final static int TAG_NORMAL_BALL = 10 * 10000;
    public final static int TAG_ADD_BALL1 = 11 * 10000;
    public final static int TAG_ADD_BALL2 = 12 * 10000;
    // 主堡垒
    public final static int NORMAL_HOME_BRICK_SIZE = NORMAL_BRICK_SIZE * 2;

    //道具
    public final static int PROP_SIZE = (int)(720 * 0.135);

    //全局的速度
    public  final static float v = 0.5f;//
}
