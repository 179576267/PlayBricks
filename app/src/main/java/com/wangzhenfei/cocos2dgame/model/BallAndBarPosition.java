package com.wangzhenfei.cocos2dgame.model;

import java.util.List;

/**
 * Created by wangzhenfei on 2016/11/25.
 */
public class BallAndBarPosition {
    private List<BattleBall> list;
    private float poleX;


    public List<BattleBall> getData() {
        return list;
    }

    public void setData(List<BattleBall> data) {
        this.list = data;
    }

    public float getPoleX() {
        return poleX;
    }

    public void setPoleX(float poleX) {
        this.poleX = poleX;
    }
}
