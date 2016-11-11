package com.wangzhenfei.cocos2dgame;

import android.support.multidex.MultiDexApplication;

import com.wangzhenfei.cocos2dgame.socket.MySocket;

/**
 * Author: zhenfei.wang
 * Date: 15-05-10
 * Time: 下午5:21
 * Description: 全局的application，在这里做一些全局框架性的东西 Version: 3.0
 */
public class GameApplication extends MultiDexApplication {
    private final String TAG = "DouQuApplication";
    private static GameApplication application;
    /**
     * 这个才是程序的入口，这个类在不必要的情况下不要做修改
     */
    @Override

    public void onCreate() {
        super.onCreate();
        application = this;
        MySocket.getInstance().initSocket();
    }

    public static GameApplication getAppInstance() {
        return application;
    }
}
