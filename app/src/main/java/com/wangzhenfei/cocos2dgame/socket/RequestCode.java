package com.wangzhenfei.cocos2dgame.socket;

/**
 * Created by wangzhenfei on 2016/11/11.
 * 请求参数
 */
public class RequestCode {
    public final static String IP = "192.168.2.121";
//    public final static String IP = "182.254.247.160";
//    public final static String IP = "114.95.157.185";
    public final static int PORT = 2817;
//    public final static int PORT = 9999;

    /**
     * 登录
     */
    public final static int LOGIN = 1001;

    /**
     * 登出
     */
    public final static int LOGOUT = 1002;

    /**
     * 开始对战
     */
    public final static int BATTLE_START = 2001;
    /**
     * 战斗结束
     */
    public final static int BATTLE_END = 2002;

    /**
     * 战斗数据(球的运动轨迹)
     */
    public final static int BATTLE_DATA_BALL = 3001;
    /**
     * 战斗数据(横条的运动轨迹)
     */
    public final static int BATTLE_DATA_STICK = 3002;
    /**
     * 战斗数据(碰撞)
     */
    public final static int BATTLE_DATA_BUMP = 3003;
    /**
     * 战斗数据(接到道具)
     */
    public final static int BATTLE_DATA_GET_PROP = 3004;
    /**
     * 战斗数据(道具时间到期)
     */
    public final static int BATTLE_DATA_PROP_END = 3005;

}
