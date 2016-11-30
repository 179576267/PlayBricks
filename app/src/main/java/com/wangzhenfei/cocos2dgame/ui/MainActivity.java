package com.wangzhenfei.cocos2dgame.ui;

import android.app.Activity;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.wangzhenfei.cocos2dgame.config.RequestCode;
import com.wangzhenfei.cocos2dgame.layer.StartPageLayer;
import com.wangzhenfei.cocos2dgame.model.ActivityLifeCycle;
import com.wangzhenfei.cocos2dgame.model.SaveUserInfo;
import com.wangzhenfei.cocos2dgame.model.UserInfo;
import com.wangzhenfei.cocos2dgame.socket.MsgData;
import com.wangzhenfei.cocos2dgame.socket.MySocket;

import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.opengl.CCGLSurfaceView;
import org.cocos2d.types.CGSize;

import de.greenrobot.event.EventBus;

public class MainActivity extends Activity {
    CCGLSurfaceView mGLSurfaceView = null;
    public static Activity activity;
    static {
        System.loadLibrary("gdx");
    }
    private RegisterDialog registerDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
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
        CCDirector.sharedDirector().setAnimationInterval(1.0f / 60.0f);

        CCScene scene = CCScene.node();
        scene.addChild(new StartPageLayer());

        // Make the Scene active
        CCDirector.sharedDirector().runWithScene(scene);
        EventBus.getDefault().register(this);

        if(SaveUserInfo.getInstance().getId() == 0){
            checkAcount();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(final MsgData data){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(data.getCode() == RequestCode.FAILURE){
                    String errorString = ErrorCode.getErrorString(data.getInfo());
                    if(!TextUtils.isEmpty(errorString)){
                        Toast.makeText(MainActivity.this, ErrorCode.getErrorString(data.getInfo()), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }
    public void onEvent(UserInfo info ){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(registerDialog != null && registerDialog.isShowing()){
                    registerDialog.dismiss();
                }
            }
        });
    }


    private void checkAcount() {
        registerDialog = new RegisterDialog(this);
        registerDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().post(new ActivityLifeCycle(false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().post(new ActivityLifeCycle(true));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(registerDialog != null){
            registerDialog.onActivityResult(requestCode, resultCode, data);
        }
    }
}
