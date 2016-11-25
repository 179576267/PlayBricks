package com.wangzhenfei.cocos2dgame.model;

/**
 * Created by wangzhenfei on 2016/11/25.
 */
public class RegisterInfo {
    private String name;
    private String avatar;
    private String ip;

    public RegisterInfo(String name, String avatar, String ip) {
        this.name = name;
        this.avatar = avatar;
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNickName() {
        return name;
    }

    public void setNickName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
