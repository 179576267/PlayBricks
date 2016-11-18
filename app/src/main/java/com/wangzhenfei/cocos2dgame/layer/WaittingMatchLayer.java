package com.wangzhenfei.cocos2dgame.layer;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.wangzhenfei.cocos2dgame.model.BattleInitInfo;
import com.wangzhenfei.cocos2dgame.model.UserInfo;
import com.wangzhenfei.cocos2dgame.socket.RequestCode;
import com.wangzhenfei.cocos2dgame.socket.MsgData;
import com.wangzhenfei.cocos2dgame.socket.MySocket;
import com.wangzhenfei.cocos2dgame.tool.SpriteUtils;

import org.cocos2d.actions.UpdateCallback;
import org.cocos2d.actions.interval.CCJumpTo;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;

import de.greenrobot.event.EventBus;

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
        EventBus.getDefault().register(this);
        MsgData<String> msgData = new MsgData<String>();
        msgData.setCode(RequestCode.BATTLE_START);
        MySocket.getInstance().setMessage(msgData);
    }

    @Override
    public void goToNext() {
        super.goToNext();
        long id = Thread.currentThread().getId();
        CCScene scene = CCScene.node();
        BaseCCLayer layer = null;
        if(info.getInitiativeUser().getId() == UserInfo.info.getId()){ // 自己主动
            layer = new GameLayer(info);
        }else {
            layer = new GameProjectionLayer(info);
        }
        scene.addChild(layer);
        // Make the Scene active
        CCDirector.sharedDirector().replaceScene(scene);
        EventBus.getDefault().unregister(this);
    }

    BattleInitInfo info;
    public void onEvent(BattleInitInfo info) {
        long id = Thread.currentThread().getId();
        this.info = info;
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
    if(info != null){
        goToNext();
    }
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
            setNum();

            if(timeCount == 5){
                long id = Thread.currentThread().getId();
                CCScene scene = CCScene.node();
                scene.addChild(new GameLayer(null));
                // Make the Scene active
                CCDirector.sharedDirector().replaceScene(scene);
                EventBus.getDefault().unregister(this);
            }
            if(timeCount == 30){
                CCScene scene = CCScene.node();
                scene.addChild(new StartPageLayer());
                // Make the Scene active
                CCDirector.sharedDirector().runWithScene(scene);
                EventBus.getDefault().unregister(this);
            }
            timeCount++;
        }
    }

    private void setNum() {
        if(timeCount > 30) { // 返回
            goToNext();
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
