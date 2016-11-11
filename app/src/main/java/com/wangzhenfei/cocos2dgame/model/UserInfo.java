package com.wangzhenfei.cocos2dgame.model;

/**
 * Created by wangzhenfei on 2016/11/11.
 */
public class UserInfo {
    public static UserInfo info = new UserInfo();

    /**
     * id : 1010
     * name : hoodle_1010
     * avatar : hoodle_1010
     */

    private int id;
    private String name;
    private String avatar;

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
}
