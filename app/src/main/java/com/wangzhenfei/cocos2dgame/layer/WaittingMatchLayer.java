package com.wangzhenfei.cocos2dgame.layer;

import com.wangzhenfei.cocos2dgame.tool.SpriteUtils;

import org.cocos2d.actions.UpdateCallback;
import org.cocos2d.actions.interval.CCJumpTo;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrame;
import org.cocos2d.nodes.CCSpriteFrameCache;
import org.cocos2d.opengl.CCTexture2D;
import org.cocos2d.types.CGPoint;

/**
 * Created by wangzhenfei on 2016/11/9.
 */
public class WaittingMatchLayer extends BaseCCLayer{
    CCSprite point1;
    CCSprite point2;
    CCSprite point3;
    CCSprite tenNum;
    CCSprite geNum;
    private int timeCount = 0;
    private int jumpCount = 0;

    public WaittingMatchLayer() {
        super();
        this.setIsTouchEnabled(true);
        addSprite();
    }

    private void addSprite() {
        // 增加背景
        CCSprite bg = SpriteUtils.getSprite("marbles_background.png", screenWith, screenHeight, true, -1);
        this.addChild(bg);

        //匹配对手中
        CCSprite waitText = SpriteUtils.getSprite("marbles_text_pipei.png", 387, 84,false, -1);
        waitText.setPosition(CGPoint.ccp(screenWith / 2, screenHeight - 200));
        this.addChild(waitText);

        //三个等待点
        point1 = SpriteUtils.getSprite("marbles_icon_point.png", 24, 22,false, -1);
        point1.setPosition(CGPoint.ccp(screenWith / 2 - 70, screenHeight - 290));
        this.addChild(point1);

        point2 = SpriteUtils.getSprite("marbles_icon_point.png", 24, 22,false, -1);
        point2.setPosition(CGPoint.ccp(screenWith /2 ,screenHeight - 290));
        this.addChild(point2);

        point3 = SpriteUtils.getSprite("marbles_icon_point.png", 24, 22,false, -1);
        point3.setPosition(CGPoint.ccp(screenWith /2 + 70,screenHeight - 290));
        this.addChild(point3);

        //添加计时器
        CCSprite bgCount = SpriteUtils.getSprite("marbles_clock_base.png", 366, 366,false, -1);
        bgCount.setPosition(CGPoint.ccp(screenWith /2 ,screenHeight/2));
        this.addChild(bgCount);

        CCSprite imClock = SpriteUtils.getSprite("marbles_icon_clock.png", 101, 125,false, -1);
        imClock.setPosition(CGPoint.ccp(screenWith /2 - 70 ,screenHeight/2));
        this.addChild(imClock);

        //添加数字
        tenNum = SpriteUtils.getSprite("0.png", 29, 70,false, -1);
        tenNum.setPosition(CGPoint.ccp(screenWith /2 + 30 ,screenHeight/2));
        this.addChild(tenNum);

        geNum = SpriteUtils.getSprite("0.png", 29, 70,false, -1);
        geNum.setPosition(CGPoint.ccp(screenWith /2 + 30 + 30 ,screenHeight/2));
        this.addChild(geNum);

        schedule(tickCallback, 0.2f);
    }

    private UpdateCallback tickCallback = new UpdateCallback() {

        @Override
        public void update(float d) {
            tick(d);
        }
    };

    private void tick(float d) {
        int r = jumpCount % 3;
        CCJumpTo jumpTo;
        switch (r){
            case 0:
                 jumpTo = CCJumpTo.action(0.3f, CGPoint.ccp(screenWith / 2 - 70, screenHeight - 290), 10, 1);
                point1.runAction(jumpTo);
                break;
            case 1:
                 jumpTo = CCJumpTo.action(0.3f, CGPoint.ccp(screenWith / 2 , screenHeight - 290), 10, 1);
                point2.runAction(jumpTo);
                break;
            case 2:
                jumpTo = CCJumpTo.action(0.3f, CGPoint.ccp(screenWith / 2  + 70, screenHeight - 290), 10, 1);
                point3.runAction(jumpTo);
                break;
        }
        jumpCount++;
        if(jumpCount % 5 == 0){ // 1s
            timeCount++;
            setNum();
            if(timeCount == 5){
                CCScene scene = CCScene.node();
                scene.addChild(new GameLayer());
                // Make the Scene active
                CCDirector.sharedDirector().runWithScene(scene);
            }
        }
    }

    private void setNum() {
        if(timeCount > 30) { // 返回
            CCScene scene = CCScene.node();
            scene.addChild(new StartPageLayer());
            // Make the Scene active
            CCDirector.sharedDirector().runWithScene(scene);
        }
        int ten = timeCount / 10;
        int ge = timeCount % 10;
        tenNum.removeSelf();
        geNum.removeSelf();

        //添加数字
        tenNum = SpriteUtils.getSprite(ten +".png", 29, 70,false, -1);
        tenNum.setPosition(CGPoint.ccp(screenWith /2 + 30 ,screenHeight/2));
        this.addChild(tenNum);

        geNum = SpriteUtils.getSprite(ge + ".png", 29, 70,false, -1);
        geNum.setPosition(CGPoint.ccp(screenWith / 2 + 30 + 30, screenHeight / 2));
        this.addChild(geNum);

    }
}
