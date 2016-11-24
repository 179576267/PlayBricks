package com.wangzhenfei.cocos2dgame;

import android.support.multidex.MultiDexApplication;

import com.tencent.bugly.crashreport.CrashReport;
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
        /* Bugly SDK初始化
        * 参数1：上下文对象
        * 参数2：APPID，平台注册时得到,注意替换成你的appId
        * 参数3：是否开启调试模式，调试模式下会输出'CrashReport'tag的日志
        */
        CrashReport.initCrashReport(this, "5553d23d31", true);
        MySocket.getInstance().initSocket();
    }

    public static GameApplication getAppInstance() {
        return application;
    }
}
