package com.wangzhenfei.cocos2dgame.layer;

import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import com.wangzhenfei.cocos2dgame.tool.SpriteUtils;

import org.cocos2d.actions.instant.CCHide;
import org.cocos2d.actions.instant.CCShow;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;

/**
 * Created by wangzhenfei on 2016/11/9.
 */
public class StartPageLayer extends BaseCCLayer{
    public StartPageLayer() {
        super();
        this.setIsTouchEnabled(true);
        addSprite();
    }

    @Override
    public void goToNext() {
        CCScene scene = CCScene.node();
        scene.addChild(new WaittingMatchLayer());
        // Make the Scene active
        CCDirector.sharedDirector().runWithScene(scene);
    }

    CCSprite startButtonUp;
    CCSprite startButtonDown;
    private void addSprite() {
        // 增加背景
        CCSprite bg = SpriteUtils.getSprite("marbles_background.png", screenWith, screenHeight,true, -1);
        this.addChild(bg);

        // 增加开始按钮
        startButtonUp = SpriteUtils.getSprite("marbles_button_start.png", 334, 334, false, -1);
        startButtonUp.setPosition(CGPoint.ccp(screenWith / 2, screenHeight / 2 + 130));
        this.addChild(startButtonUp);

        startButtonDown = SpriteUtils.getSprite("marbles_button_start_02.png", 334, 334, false, -1);
        startButtonDown.setPosition(CGPoint.ccp(screenWith / 2, screenHeight / 2 + 130));
        this.addChild(startButtonDown);
        startButtonDown.runAction(CCHide.action());



        //增加开始对战
        CCSprite startText = SpriteUtils.getSprite("marbles_text_start.png", screenWith, 364, false, -1);
        startText.setPosition(CGPoint.ccp(screenWith / 2 , screenHeight/ 2  - 170));
        this.addChild(startText);
    }

    @Override
    public boolean ccTouchesBegan(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        CGPoint p1 = CGPoint.ccp(x, y);
        // 将以左上角为原点的坐标转换为以左下角为原点的坐标
        CGPoint p2 = CCDirector.sharedDirector().convertToGL(p1);
        CGRect rect = SpriteUtils.getSpriteRect(startButtonUp, 334, 334);
        if(rect.contains(p2.x, p2.y)){
            startButtonUp.runAction(CCHide.action());
            startButtonDown.runAction(CCShow.action());
        }
        return super.ccTouchesBegan(event);
    }

    @Override
    public boolean ccTouchesEnded(MotionEvent event) {
        startButtonUp.runAction(CCShow.action());
        startButtonDown.runAction(CCHide.action());
       goToNext();
        return super.ccTouchesEnded(event);
    }
}
