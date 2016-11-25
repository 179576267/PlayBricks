package com.wangzhenfei.cocos2dgame.model;

/**
 * Created by wangzhenfei on 2016/11/11.
 */
public class UserInfo {
    /**
     * id : 110
     * name : 我摸
     * avatar : /IMAGE/.jpg/2016/11/25/1480045581563_97.jpg
     * account : 1008
     * password : 123456
     * resourceShowPath : http://182.254.247.160:180/quyuedan/resource
     */

    private int id;
    private String name;
    private String avatar;
    private String account;
    private String password;
    private String resourceShowPath;
    private String ip;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getResourceShowPath() {
        return resourceShowPath;
    }

    public void setResourceShowPath(String resourceShowPath) {
        this.resourceShowPath = resourceShowPath;
    }
}
