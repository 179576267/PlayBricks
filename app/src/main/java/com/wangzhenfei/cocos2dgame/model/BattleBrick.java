package com.wangzhenfei.cocos2dgame.model;

/**
 * Created by wangzhenfei on 2016/11/15.
 * 砖块碰撞的通知
 */
public class BattleBrick {
    private int id;
    private int type;

    public BattleBrick(int id, int type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
