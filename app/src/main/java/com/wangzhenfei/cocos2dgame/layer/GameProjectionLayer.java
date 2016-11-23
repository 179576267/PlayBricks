package com.wangzhenfei.cocos2dgame.layer;

import android.util.Log;
import android.view.MotionEvent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.wangzhenfei.cocos2dgame.SpriteConfig;
import com.wangzhenfei.cocos2dgame.model.BattleBall;
import com.wangzhenfei.cocos2dgame.model.BattleBrick;
import com.wangzhenfei.cocos2dgame.model.BattleInitInfo;
import com.wangzhenfei.cocos2dgame.model.ControlBarInfo;
import com.wangzhenfei.cocos2dgame.model.E_GameType;
import com.wangzhenfei.cocos2dgame.model.GameResult;
import com.wangzhenfei.cocos2dgame.model.Location;
import com.wangzhenfei.cocos2dgame.model.PropStatusInfo;
import com.wangzhenfei.cocos2dgame.model.UserInfo;
import com.wangzhenfei.cocos2dgame.socket.MsgData;
import com.wangzhenfei.cocos2dgame.socket.MySocket;
import com.wangzhenfei.cocos2dgame.socket.RequestCode;
import com.wangzhenfei.cocos2dgame.tool.SpriteUtils;

import org.cocos2d.actions.UpdateCallback;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCLabel;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by wangzhenfei on 2016/11/14.
 * 游戏镜像世界
 */
public class GameProjectionLayer extends BaseCCLayer{
    private BattleInitInfo.InitiativeUserBean myBatter;
    private BattleInitInfo.InitiativeUserBean offsetBatter;
    // 自己的
    private CCSprite myControlBar;
    // 别人的
    private CCSprite offsetControlBar;
    // 球的集合
    private List<Integer> balls = new ArrayList<Integer>();

    CCLabel endLable;

    private CCSprite subNum;
    private CCSprite bgNum;
    private int timeCount = 5;

    public GameProjectionLayer(BattleInitInfo info) {
        super();
        EventBus.getDefault().register(this);
        this.setIsTouchEnabled(true);
        if(UserInfo.info.getId() == info.getInitiativeUser().getId()){
            myBatter = info.getInitiativeUser();
            offsetBatter = info.getPassivityUser();
        }else {
            myBatter = info.getPassivityUser();
            offsetBatter = info.getInitiativeUser();
        }
        MySocket.getInstance().ip = offsetBatter.getIp();

        addSprite();
        balls.add(SpriteConfig.TAG_ADD_BALL1);
        balls.add(SpriteConfig.TAG_ADD_BALL2);
        balls.add(SpriteConfig.TAG_NORMAL_BALL);
        schedule("startMinus", 1);
    }

    public void startMinus(float rdelta){
        if(timeCount == 0){
            if(subNum != null){
                subNum.removeSelf();
            }
            if(bgNum != null){
                bgNum.removeSelf();
            }
            unschedule("startMinus");
        }else {
            setNum();
        }
        timeCount  --;
    }

    @Override
    public void goToNext() {
        CCScene scene = CCScene.node();
        scene.addChild(new StartPageLayer());
        // Make the Scene active
        CCDirector.sharedDirector().runWithScene(scene);
        EventBus.getDefault().unregister(this);
    }

    /**
     * 球的位置回调
     * @param infos
     */
    public void onEvent( List<BattleBall> infos) {
        if(infos != null){
           if(infos.size() == 1){
               hideMoreBall();
           }
            for(int i=0; i < Math.min(infos.size(), balls.size()); i++){
                CCSprite ball = (CCSprite) getChildByTag(infos.get(i).getId());
                if(ball != null){
                    CGPoint point = CGPoint.ccp((1 - infos.get(i).getLocation().getX()) * screenWith,
                            (1 - infos.get(i).getLocation().getY()) * screenHeight);
                    ball.setPosition(point);
                }
            }
        }

    }

    /**
     * 游戏结果回调
     */
    GameResult result;
    public void onEvent(GameResult result) {
        this.result = result;
    }


    /**
     * 对方横杆位置回调
     * @param infos
     */
    public void onEvent( ControlBarInfo infos) {
        if(infos != null){
            CGPoint position = offsetControlBar.getPosition();
            position.x = (1 - infos.getDx()) * screenWith;
            offsetControlBar.setPosition(position);
        }
    }

    /**
     * 碰撞的回调
     * @param infos
     */
    BattleBrick battleBrick;
    public void onEventBackgroundThread( BattleBrick infos) {
        battleBrick = infos;
    }


    /**
     * 道具状态的回调
     */
    public void onEventBackgroundThread(PropStatusInfo info) {
       if(info.getPropId() == E_GameType.ADD_WIDTH.getCode()){
            if(info.isShow()){
                offsetControlBar.setScaleX((SpriteConfig.EXPEND_CONTROL_BAR_W) / myControlBar.getContentSize().width);
            }else {
                offsetControlBar.setScaleX((SpriteConfig.NORMAL_CONTROL_BAR_W) / myControlBar.getContentSize().width);
            }
       }else  if(info.getPropId() == E_GameType.TRHEE_HOODLE.getCode()){

       } else if(info.getPropId() == E_GameType.ADD_SPEED.getCode()){

       }
    }


    private void addSprite() {
        addBg();
        addBalls(SpriteConfig.TAG_NORMAL_BALL);
        addBalls(SpriteConfig.TAG_ADD_BALL1);
        addBalls(SpriteConfig.TAG_ADD_BALL2);
        addMyHome();
        addOffsetHome();
    }
    private void addBalls(int tag) {
        //添加球
        CCSprite ball = SpriteUtils.getSprite("marbles_ball.png", SpriteConfig.BALL_SIZE, SpriteConfig.BALL_SIZE, false, tag);
        if(tag == SpriteConfig.TAG_NORMAL_BALL){
            CGPoint ballPoint = CGPoint.ccp(screenWith / 2, screenHeight - (SpriteConfig.BALL_SIZE / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + SpriteConfig.NORMAL_CONTROL_BAR_H));
            ball.setPosition(ballPoint);
        }else {
            ball.setPosition(CGPoint.ccp(-200, -200));
        }
        this.addChild(ball);
    }

    /**
     * 隐藏多余的球
     */
    private void hideMoreBall(){
        CCSprite ball1 = (CCSprite) getChildByTag(SpriteConfig.TAG_ADD_BALL1);
        if(ball1 != null){
            ball1.setPosition(CGPoint.ccp(-200, -200));
        }

        CCSprite ball2 = (CCSprite) getChildByTag(SpriteConfig.TAG_ADD_BALL2);
        if(ball2 != null){
            ball2.setPosition(CGPoint.ccp(-200, -200));
        }
    }

    private void addBg() {
        // 增加背景
        CCSprite bg = SpriteUtils.getSprite("marbles_background_02.png", screenWith, screenHeight, true, -1);
        this.addChild(bg);
    }

    private void addMyHome() {
        // 增加对手的砖块
        CCSprite brickBg;
        CCSprite brick;
        int brickPoint = 0;
        for( int j = 2; j > -1  ;j--){
            kk: for(int i = 0; i < 6; i++){
                if((j == 0 && i == 2) ||
                        (j == 0 && i == 3) ||
                        (j == 1 && i == 2) ||
                        (j == 1 && i == 3)){
                    continue kk;
                }
                BattleInitInfo.InitiativeUserBean.BlockListBean blockListBean = myBatter.getBlockList().get(brickPoint++);
                if((i+j) % 2 == 0){
                    brickBg = SpriteUtils.getSprite("marbles_gem_base_01.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, blockListBean.getId());
                }else {
                    brickBg = SpriteUtils.getSprite("marbles_gem_base_02.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, blockListBean.getId());
                }
                if(blockListBean.getPropType() == 0){
                    brick = SpriteUtils.getSprite("marbles_gem_yellow.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, blockListBean.getId() - 50);
                }else {
                    brick = SpriteUtils.getSprite("marbles_gem_prop.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, blockListBean.getId() - 50);
                }
                CGPoint point = CGPoint.ccp(screenWith / 2 - SpriteConfig.NORMAL_BRICK_SIZE * (3 - i) + SpriteConfig.NORMAL_BRICK_SIZE / 2, SpriteConfig.NORMAL_BRICK_SIZE / 2 + j * SpriteConfig.NORMAL_BRICK_SIZE);
                brick.setPosition(point);
                brickBg.setPosition(point);
                this.addChild(brickBg);
                this.addChild(brick);
            }
        }

        //添加我的主堡垒
        CCSprite spHome = SpriteUtils.getSprite("app_logo.png", SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE, false, SpriteConfig.TAG_MY_NORMAL_HOME_BRICK);
        spHome.setPosition(CGPoint.ccp(screenWith / 2, SpriteConfig.NORMAL_HOME_BRICK_SIZE / 2));
        this.addChild(spHome);


        //添加杆子
        myControlBar = SpriteUtils.getSprite("marbles_baffle.png", SpriteConfig.CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H, false, SpriteConfig.TAG_MY_NORMAL_CONTROL_BAR);
        CGPoint ccp = CGPoint.ccp(screenWith / 2, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10);
        myControlBar.setPosition(ccp);
        this.addChild(myControlBar);
    }
    private void addOffsetHome() {
        // 增加自己的砖块
        CCSprite brickBg;
        CCSprite brick;
        int brickPoint = 0;
        for(int j = 2; j > -1 ;j--){
            kk: for(int i = 0; i < 6; i++){
                if((j == 0 && i == 2) ||
                        (j == 0 && i == 3) ||
                        (j == 1 && i == 2) ||
                        (j == 1 && i == 3)){
                    continue kk;
                }
                BattleInitInfo.InitiativeUserBean.BlockListBean blockListBean = offsetBatter.getBlockList().get(brickPoint++);
                if((i+j) % 2 == 0){
                    brickBg = SpriteUtils.getSprite("marbles_gem_base_01.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, blockListBean.getId());
                }else {
                    brickBg = SpriteUtils.getSprite("marbles_gem_base_02.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, blockListBean.getId());
                }

                if(blockListBean.getPropType() == 0){
                    brick = SpriteUtils.getSprite("marbles_gem_green.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, blockListBean.getId() - 50);
                    brick.setRotation(180);
                }else {
                    brick = SpriteUtils.getSprite("marbles_gem_prop.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, blockListBean.getId() - 50);
                }
                CGPoint point = CGPoint.ccp(screenWith / 2 - SpriteConfig.NORMAL_BRICK_SIZE * (i - 3) - SpriteConfig.NORMAL_BRICK_SIZE / 2,
                        screenHeight - (SpriteConfig.NORMAL_BRICK_SIZE / 2 + j * SpriteConfig.NORMAL_BRICK_SIZE));
                brick.setPosition(point);
                brickBg.setPosition(point);
                this.addChild(brickBg);
                this.addChild(brick);
            }
        }

        //添加他人的主堡垒
        CCSprite spHome = SpriteUtils.getSprite("app_logo.png", SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE, false, SpriteConfig.TAG_OFFSET_NORMAL_HOME_BRICK);
        spHome.setPosition(CGPoint.ccp(screenWith / 2, screenHeight - SpriteConfig.NORMAL_HOME_BRICK_SIZE / 2));
        this.addChild(spHome);
        //添加杆子
        offsetControlBar = SpriteUtils.getSprite("marbles_baffle.png", SpriteConfig.CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H, false, SpriteConfig.TAG_OFFSET_NORMAL_CONTROL_BAR);
        CGPoint ccp = CGPoint.ccp(screenWith / 2,
                screenHeight - (SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10));
        offsetControlBar.setPosition(ccp);
        this.addChild(offsetControlBar);
    }




    @Override
    public void onEnter() {
        super.onEnter();
        schedule(tickCallback);
    }



    @Override
    public void onExit() {
        super.onExit();
        unschedule(tickCallback);
    }
    private UpdateCallback tickCallback = new UpdateCallback() {

        @Override
        public void update(float d) {
            tick(d);
        }
    };

    private void tick(float d) {
        if(result != null){
            if(result.isWin()){
                endLable = CCLabel.makeLabel("YOU WIN", CGSize.make(400, 400), CCLabel.TextAlignment.CENTER, "", 80);
                endLable.setPosition(CGPoint.ccp(screenWith / 2, screenHeight / 2));
                endLable.setColor(new ccColor3B(255, 255, 255));
                this.addChild(endLable);
            }else {
                endLable = CCLabel.makeLabel("YOU LOSE",  CGSize.make(400, 400), CCLabel.TextAlignment.CENTER, "", 80);
                endLable.setPosition(CGPoint.ccp(screenWith / 2, screenHeight / 2));
                endLable.setColor(new ccColor3B(255, 255, 255));
                this.addChild(endLable);
            }
        }

        if(battleBrick != null){
            CGPoint point  = CGPoint.getZero();
            CCSprite sprite = (CCSprite) getChildByTag(battleBrick.getId());
            if(sprite != null){
                point = sprite.getPosition();
            }
            removeChildByTag(battleBrick.getId(),true);
            removeChildByTag(battleBrick.getId() - 50, true);
            // 查看是否接到道具
            if(battleBrick.getId() < 200 && battleBrick.getId() > 100){ // 他人的
                BattleInitInfo.InitiativeUserBean.BlockListBean brick = null;
                int index = battleBrick.getId() % 100 - 1;
                CCSprite spProp = null;
                brick = offsetBatter.getBlockList().get(index);
                if(brick.getPropType() == E_GameType.ADD_SPEED.getCode()){
                    spProp = SpriteUtils.getSprite("marbles_prop_03.png",SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE,false, brick.getPropType());
                    spProp.setPosition(point);
                    this.addChild(spProp);
                }else if(brick.getPropType() == E_GameType.ADD_WIDTH.getCode()){
                    spProp = SpriteUtils.getSprite("marbles_prop_01.png",SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE,false, brick.getPropType());
                    spProp.setPosition(point);
                    this.addChild(spProp);
                }else if(brick.getPropType() == E_GameType.TRHEE_HOODLE.getCode()){
                    spProp = SpriteUtils.getSprite("marbles_prop_02.png",SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE,false, brick.getPropType());
                    spProp.setPosition(point);
                    this.addChild(spProp);
                }
            }
            battleBrick = null;
        }
        catchProp();
    }

    /**
     * 接住道具
     */
    private void catchProp() {
        CCSprite spAddWidth = (CCSprite) this.getChildByTag(E_GameType.ADD_WIDTH.getCode());
        CCSprite spMoreBall = (CCSprite) this.getChildByTag(E_GameType.TRHEE_HOODLE.getCode());
        CCSprite spAddSpeed = (CCSprite) this.getChildByTag(E_GameType.ADD_SPEED.getCode());
        if(spAddWidth != null){
            //判断是否接住
            if(SpriteUtils.isSpriteConfict(myControlBar, SpriteConfig.CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H,
                    spAddWidth, SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE)){
                Log.i(TAG, "接住");
                spAddWidth.removeSelf();
                // 横杆变大
                SpriteConfig.CONTROL_BAR_W = SpriteConfig.EXPEND_CONTROL_BAR_W;
                changeControlBarSize();
                schedule("expendLager", 1);

                MsgData msgData = new MsgData();
                msgData.setCode(RequestCode.BATTLE_DATA_GET_PROP);
                msgData.setData(new PropStatusInfo(E_GameType.ADD_WIDTH.getCode(), true));
                MySocket.getInstance().setMessage(msgData);
            }
            CGPoint point = spAddWidth.getPosition();
            point.y = point.y-10;
            spAddWidth.setPosition(point);
            Log.i(TAG, point.toString());
            if(point.y < -100){
                spAddWidth.removeSelf();
            }
        }

        if(spMoreBall != null){
            //判断是否接住
            if(SpriteUtils.isSpriteConfict(myControlBar, SpriteConfig.CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H,
                    spMoreBall, SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE)){
                Log.i(TAG, "接住");
                spMoreBall.removeSelf();
                // 多球
                schedule("moreBall", 1);

                MsgData msgData = new MsgData();
                msgData.setCode(RequestCode.BATTLE_DATA_GET_PROP);
                msgData.setData(new PropStatusInfo(E_GameType.TRHEE_HOODLE.getCode(), true));
                MySocket.getInstance().setMessage(msgData);
            }
            CGPoint point = spMoreBall.getPosition();
            point.y = point.y-10;
            spMoreBall.setPosition(point);
            Log.i(TAG, point.toString());
            if(point.y < -100){
                spMoreBall.removeSelf();
            }
        }

        if(spAddSpeed != null){
            //判断是否接住
            if(SpriteUtils.isSpriteConfict(myControlBar, SpriteConfig.CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H,
                    spAddSpeed, SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE)){
                Log.i(TAG, "接住");
                spAddSpeed.removeSelf();
                // 加速
                schedule("acclerate", 1);

                MsgData msgData = new MsgData();
                msgData.setCode(RequestCode.BATTLE_DATA_GET_PROP);
                msgData.setData(new PropStatusInfo(E_GameType.ADD_SPEED.getCode(), true));
                MySocket.getInstance().setMessage(msgData);
            }
            CGPoint point = spAddSpeed.getPosition();
            point.y = point.y-10;
            spAddSpeed.setPosition(point);
            Log.i(TAG, point.toString());
            if(point.y < -100){
                spAddSpeed.removeSelf();
            }
        }
    }


    private void setNum() {
        if(subNum != null){
            subNum.removeSelf();
        }
        if(bgNum == null){
            bgNum = SpriteUtils.getSprite("marbles_base_number.png",screenWith,128,false,-1);
            bgNum.setPosition(CGPoint.ccp(screenWith /2, screenHeight / 2));
            this.addChild(bgNum);
        }

        //添加数字
        subNum = SpriteUtils.getSprite(timeCount +".png", 29, 70,false, -1);
        subNum.setPosition(CGPoint.ccp(screenWith /2 + 30 ,screenHeight/2));
        this.addChild(subNum);
    }



    // ***************************道具持续的时间***************************************************
    private int expendLagerSecond = 0;
    public void expendLager(float dx){
        expendLagerSecond ++;
        if(expendLagerSecond > 10){ // 结束道具
            expendLagerSecond = 0;
            SpriteConfig.CONTROL_BAR_W = SpriteConfig.NORMAL_CONTROL_BAR_W;
            changeControlBarSize();
            unschedule("expendLager");

            MsgData msgData = new MsgData();
            msgData.setCode(RequestCode.BATTLE_DATA_GET_PROP);
            msgData.setData(new PropStatusInfo(E_GameType.ADD_WIDTH.getCode(), false));
            MySocket.getInstance().setMessage(msgData);
        }
    }

    private void changeControlBarSize() {
        myControlBar.setScaleX((SpriteConfig.CONTROL_BAR_W) / myControlBar.getContentSize().width);
    }

    private int moreBallSecond = 0;
    public void moreBall(float dx){
        moreBallSecond ++;
        if(moreBallSecond > 10){ // 结束道具
            moreBallSecond = 0;
            unschedule("moreBall");
            hideMoreBall();
            MsgData msgData = new MsgData();
            msgData.setCode(RequestCode.BATTLE_DATA_GET_PROP);
            msgData.setData(new PropStatusInfo(E_GameType.TRHEE_HOODLE.getCode(), false));
            MySocket.getInstance().setMessage(msgData);
        }
    }

    private int acclerateSecond = 0;
    public void acclerate(float dx){
        acclerateSecond ++;
        if(acclerateSecond > 10){ // 结束道具
            acclerateSecond = 0;
            unschedule("acclerate");

            MsgData msgData = new MsgData();
            msgData.setCode(RequestCode.BATTLE_DATA_GET_PROP);
            msgData.setData(new PropStatusInfo(E_GameType.ADD_SPEED.getCode(), false));
            MySocket.getInstance().setMessage(msgData);
        }
    }
    // ***************************道具持续的时间***************************************************


    //***********************************触摸事件********************************************************

    /**
     * 触摸屏幕时
     */
    @Override
    public boolean ccTouchesBegan(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        CGPoint p1 = CGPoint.ccp(x, y);
        // 将以左上角为原点的坐标转换为以左下角为原点的坐标
        CGPoint p2 = CCDirector.sharedDirector().convertToGL(p1);

        if (p2.y < screenHeight / 2) { // 有效触发范围
//            spControl.runAction(CCMoveTo.action(0, CGPoint.ccp(p2.x, p2.y)));
            if(p2.x < SpriteConfig.CONTROL_BAR_W / 2){
                p2.x = SpriteConfig.CONTROL_BAR_W / 2;
            }else if(p2.x > screenWith - (SpriteConfig.CONTROL_BAR_W / 2)){
                p2.x = screenWith - (SpriteConfig.CONTROL_BAR_W / 2);
            }
            myControlBar.setPosition(CGPoint.ccp(p2.x, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10));
            MsgData msgData = new MsgData();
            msgData.setCode(RequestCode.BATTLE_DATA_STICK);
            msgData.setData(new ControlBarInfo(p2.x/screenWith));
            MySocket.getInstance().setUdpMessage(msgData);
        }
        return super.ccTouchesBegan(event);
    }

    /**
     * 移动时
     */
    @Override
    public boolean ccTouchesMoved(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        CGPoint p1 = CGPoint.ccp(x, y);
        // 将以左上角为原点的坐标转换为以左下角为原点的坐标
        CGPoint p2 = CCDirector.sharedDirector().convertToGL(p1);
        if (p2.y < screenHeight / 2) { // 有效触发范围
//            spControl.runAction(CCMoveTo.action(0, CGPoint.ccp(p2.x, p2.y)));
            if(p2.x < SpriteConfig.CONTROL_BAR_W / 2){
                p2.x = SpriteConfig.CONTROL_BAR_W / 2;
            }else if(p2.x > screenWith - (SpriteConfig.CONTROL_BAR_W / 2)){
                p2.x = screenWith - (SpriteConfig.CONTROL_BAR_W / 2);
            }
            myControlBar.setPosition(CGPoint.ccp(p2.x, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10));
            MsgData msgData = new MsgData();
            msgData.setCode(RequestCode.BATTLE_DATA_STICK);
            msgData.setData(new ControlBarInfo(p2.x/screenWith));
            MySocket.getInstance().setUdpMessage(msgData);
        }
        return super.ccTouchesMoved(event);
    }

    /**
     * 触摸结束时
     */
    @Override
    public boolean ccTouchesEnded(MotionEvent event) {
        if(endLable != null){
            float x = event.getX();
            float y = event.getY();
            CGPoint p1 = CGPoint.ccp(x, y);
            // 将以左上角为原点的坐标转换为以左下角为原点的坐标
            CGPoint p2 = CCDirector.sharedDirector().convertToGL(p1);
            CGRect rect = SpriteUtils.getSpriteRect(endLable, 400, 400);
            if(rect.contains(p2.x, p2.y)){
                goToNext();
            }
        }
        return super.ccTouchesEnded(event);
    }

    @Override
    public boolean ccTouchesCancelled(MotionEvent event) {
        return super.ccTouchesCancelled(event);
    }

    //***********************************触摸事件********************************************************
}
