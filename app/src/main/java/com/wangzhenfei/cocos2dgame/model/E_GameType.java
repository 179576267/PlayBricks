package com.wangzhenfei.cocos2dgame.model;

/**
 * Created by bean on 2016/11/10.
 */
public enum E_GameType {

    NONE(0,"无"),
    ADD_SPEED(1,"加速"),
    ADD_WIDTH(2,"加宽"),
    TRHEE_HOODLE(3,"三颗弹珠"),
    MY_MASTER(4,"我的大本营"),
    MY_CONTROL_BAR(5,"我的控制杆"),
    OPPOSITE_MASTER(6,"对面的大本营"),
    OPPOSITE_CONTROL_BAR(7,"对面的控制杆"),
    ;

    int code;

    String msg;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    E_GameType(int code, String msg)
    {
        this.code = code;
        this.msg = msg;
    }
}
