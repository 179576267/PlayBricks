package com.wangzhenfei.cocos2dgame;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.wangzhenfei.cocos2dgame.layer.StartPageLayer;
import com.wangzhenfei.cocos2dgame.socket.MsgData;
import com.wangzhenfei.cocos2dgame.socket.MySocket;

import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.opengl.CCGLSurfaceView;

public class MainActivity extends Activity {
    CCGLSurfaceView mGLSurfaceView = null;
    static {
        System.loadLibrary("gdx");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mGLSurfaceView = new CCGLSurfaceView(this);
        CCDirector director = CCDirector.sharedDirector();
        director.attachInView(mGLSurfaceView);
        director.setDeviceOrientation(CCDirector.kCCDeviceOrientationPortrait);
        director.setScreenSize(720, 1280);
        setContentView(mGLSurfaceView);

        // show FPS
        CCDirector.sharedDirector().setDisplayFPS(true);

        // frames per second
        CCDirector.sharedDirector().setAnimationInterval(1.0f / 30.0f);

        CCScene scene = CCScene.node();
        scene.addChild(new StartPageLayer());

        // Make the Scene active
        CCDirector.sharedDirector().runWithScene(scene);
    }
}
