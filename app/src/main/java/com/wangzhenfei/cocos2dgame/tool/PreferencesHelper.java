package com.wangzhenfei.cocos2dgame.tool;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zhenfei.wang on 2016/8/10.
 * sharePreferences 工具箱
 */
public class PreferencesHelper {
    // 用户配置
    public static final String TB_USER = "TB_USER";

    SharedPreferences mPreferences;
    SharedPreferences.Editor mEditor;
    Context context;
    public PreferencesHelper(Context c, String tbName) {
        context = c;
        mPreferences = context.getSharedPreferences(tbName, 0);
        mEditor = mPreferences.edit();
    }

    /**
     * 设置参数
     * @param key
     * @param value
     */
    public void setValue(String key, String value) {
        mEditor = mPreferences.edit();
        mEditor.putString(key, value);
        mEditor.commit();

    }

    /**
     * 获取参数
     * @param key
     * @return
     */
    public String getValue(String key) {
        return mPreferences.getString(key, "");
    }


    public void setIntValue(String key, int value){
        mEditor = mPreferences.edit();
        mEditor.putInt(key, value);
        mEditor.commit();
    }

    public int getIntValue(String key){
       return mPreferences.getInt(key, 0);
    }

    /**
     * 设置boolean值
     * @param key
     * @param value
     */
    public void setBooleanValue(String key, boolean value) {
        mEditor = mPreferences.edit();
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    /**
     * 获取boolean值
     * @param key
     * @return
     */
    public boolean getBooleanValue(String key) {
        return mPreferences.getBoolean(key, false);
    }

    /**
     * 带默认值的获取参数
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getValue(String key, String defaultValue) {
        if (!mPreferences.contains(key)) {
            return defaultValue;
        }
        return mPreferences.getString(key, defaultValue);
    }

    public void remove(String name) {
        mEditor.remove(name);

    }

    public void clearHelper() {
        mEditor = mPreferences.edit();
        mEditor.clear();
        mEditor.commit();
    }
}
