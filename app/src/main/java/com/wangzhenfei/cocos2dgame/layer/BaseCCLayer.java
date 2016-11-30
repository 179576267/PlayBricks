package com.wangzhenfei.cocos2dgame.layer;

import org.cocos2d.layers.CCLayer;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.types.CGSize;

/**
 * Created by wangzhenfei on 2016/11/9.
 */
public abstract class BaseCCLayer extends CCLayer{
    protected final String TAG = getClass().getSimpleName();
    // 像素与米比
    protected static final float PTM_RATIO = 32.0f;
    protected float screenWith;
    protected float screenHeight;
    protected BaseCCLayer(){
        CGSize size = CCDirector.sharedDirector().winSize();
        screenWith = size.getWidth();
        screenHeight = size.getHeight();
    }

    public void goToNext(){
        this.cleanup();
    }

}
