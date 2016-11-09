package com.wangzhenfei.cocos2dgame;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.opengl.CCGLSurfaceView;

public class MainActivity extends Activity {
    CCGLSurfaceView view = null;
    static {
        System.loadLibrary("gdx");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new CCGLSurfaceView(this);
          /*set it to be no title*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
       /*set it to be full screen*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(view);
        // 获得全局的 CCDirector 对象
        CCDirector director = CCDirector.sharedDirector();
        // 设置应用程序相关的属性
        //设置当前游戏程序中所使用的view对象
        director.attachInView(view);
        //设置是否显示fps值
        director.setDisplayFPS(true);
        // 设置帧率
        director.setAnimationInterval(1/ 30.0);
        director.setDeviceOrientation(CCDirector.kCCDeviceOrientationPortrait);
        director.setScreenSize(720,1280);

        // 生成一个游戏场景对象
        CCScene scene = CCScene.node();
        //生成布景层对象
        GameLayer gameLayer = new GameLayer(this);
        //将布景层对象增加到场景对象中
        scene.addChild(gameLayer);
        // 运行游戏场景
        director.runWithScene(scene);
    }
}
