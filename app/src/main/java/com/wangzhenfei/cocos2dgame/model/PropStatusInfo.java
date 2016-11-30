package com.wangzhenfei.cocos2dgame.model;

/**
 * Created by wangzhenfei on 2016/11/23.
 */
public class PropStatusInfo {
    private int type;
    private boolean show;

    public PropStatusInfo(int propId, boolean show) {
        this.type = propId;
        this.show = show;
    }

    public int getPropId() {
        return type;
    }

    public void setPropId(int propId) {
        this.type = propId;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }
}
