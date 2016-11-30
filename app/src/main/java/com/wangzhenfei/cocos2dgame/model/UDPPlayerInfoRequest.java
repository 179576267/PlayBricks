package com.wangzhenfei.cocos2dgame.model;

/**
 * Created by bean on 2016/11/28.
 */
public class UDPPlayerInfoRequest {

    private int id;

    /**
     * wifi内网ip
     */
    private String inNetIp;

    public UDPPlayerInfoRequest(int id, String inNetIp) {
        this.id = id;
        this.inNetIp = inNetIp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInNetIp() {
        return inNetIp;
    }

    public void setInNetIp(String inNetIp) {
        this.inNetIp = inNetIp;
    }
}
