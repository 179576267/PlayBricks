package com.wangzhenfei.cocos2dgame.model;

/**
 * Created by bean on 2016/11/10.
 */
public class BattleEndRequest {

    private int winId;

    public BattleEndRequest(int winId) {
        this.winId = winId;
    }

    public int getWinId() {
        return winId;
    }

    public void setWinId(int winId) {
        this.winId = winId;
    }
}
