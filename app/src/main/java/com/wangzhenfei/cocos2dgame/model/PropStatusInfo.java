package com.wangzhenfei.cocos2dgame.model;

/**
 * Created by wangzhenfei on 2016/11/23.
 */
public class PropStatusInfo {
    private int propId;
    private boolean show;

    public PropStatusInfo(int propId, boolean show) {
        this.propId = propId;
        this.show = show;
    }

    public int getPropId() {
        return propId;
    }

    public void setPropId(int propId) {
        this.propId = propId;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }
}
