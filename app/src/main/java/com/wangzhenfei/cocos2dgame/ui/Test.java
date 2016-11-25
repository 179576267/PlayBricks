package com.wangzhenfei.cocos2dgame.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wangzhenfei.cocos2dgame.R;
import com.wangzhenfei.cocos2dgame.UDPClientB;
import com.wangzhenfei.cocos2dgame.UdpDemo;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wangzhenfei on 2016/11/25.
 */
public class Test extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        ButterKnife.bind(this);
    }
    @OnClick({R.id.btn_A, R.id.btn_B})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_A:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UdpDemo.startClientA();
                    }
                }).start();
                break;
            case R.id.btn_B:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        UDPClientB.startClientB();
                    }
                }).start();
                break;
        }
    }
}
