package com.wangzhenfei.cocos2dgame.model;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.wangzhenfei.cocos2dgame.GameApplication;
import com.wangzhenfei.cocos2dgame.tool.PreferencesHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Created by zhenfei.wang on 2016/8/10.
 * 用户信息 采用sharePreferences保存
 */
public class SaveUserInfo {
    private static SaveUserInfo userInfo;
    private String id = "id";
    private String name = "name";
    private String avatar = "avatar";
    private String account = "account";
    private String password = "password";
    private String resourceShowPath = "resourceShowPath";

    private PreferencesHelper mHelper;

    private SaveUserInfo() {
        mHelper = new PreferencesHelper(GameApplication.getAppInstance(), PreferencesHelper.TB_USER);
    }

    public static SaveUserInfo getInstance() {
        if (userInfo == null) {
            synchronized (SaveUserInfo.class) {
                if (userInfo == null) {
                    userInfo = new SaveUserInfo();
                }
            }
        }
        return userInfo;
    }

    public void setUserinfo(UserInfo info){
        if(info != null){
            setId(info.getId());
            setName(info.getName());
            setAvatar(info.getAvatar());
            setAccount(info.getAccount());
            setPassword(info.getPassword());
            setResourceShowPath(info.getResourceShowPath());
        }
    }

    public UserInfo getUserInfo(){
        UserInfo userInfo = new UserInfo();
        userInfo.setId(getId());
        userInfo.setResourceShowPath(getResourceShowPath());
        userInfo.setPassword(getPassword());
        userInfo.setAccount(getAccount());
        userInfo.setAvatar(getAvatar());
        userInfo.setName(getName());
        return userInfo;
    }


    public int getId() {
        return mHelper.getIntValue(id);
    }

    public void setId(int id) {
        mHelper.setIntValue(this.id, id);
    }

    public String getName() {
        return mHelper.getValue(name) == null ? "" : mHelper.getValue(name);
    }

    public void setName(String name) {
        mHelper.setValue(this.name, name);
    }

    public String getAvatar() {
        return mHelper.getValue(avatar) == null ? "" : mHelper.getValue(avatar);
    }

    public void setAvatar(String avatar) {
        mHelper.setValue(this.avatar, avatar);
    }

    public String getAccount() {
        return mHelper.getValue(account) == null ? "" : mHelper.getValue(account);
    }

    public void setAccount(String account) {
        mHelper.setValue(this.account, account);
    }

    public String getPassword() {
        return mHelper.getValue(password) == null ? "" : mHelper.getValue(password);
    }

    public void setPassword(String password) {
        mHelper.setValue(this.password, password);
    }

    public String getResourceShowPath() {
        return mHelper.getValue(resourceShowPath) == null ? "" : mHelper.getValue(resourceShowPath);
    }

    public void setResourceShowPath(String resourceShowPath) {
        mHelper.setValue(this.resourceShowPath, resourceShowPath);
    }

}
