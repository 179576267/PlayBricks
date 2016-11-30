package com.wangzhenfei.cocos2dgame.layer;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.wangzhenfei.cocos2dgame.config.RequestCode;
import com.wangzhenfei.cocos2dgame.config.SpriteConfig;
import com.wangzhenfei.cocos2dgame.model.ActivityLifeCycle;
import com.wangzhenfei.cocos2dgame.model.BallAndBarPosition;
import com.wangzhenfei.cocos2dgame.model.BattleBall;
import com.wangzhenfei.cocos2dgame.model.BattleBrick;
import com.wangzhenfei.cocos2dgame.model.BattleEndRequest;
import com.wangzhenfei.cocos2dgame.model.BattleInitInfo;
import com.wangzhenfei.cocos2dgame.model.E_GameType;
import com.wangzhenfei.cocos2dgame.model.GameResult;
import com.wangzhenfei.cocos2dgame.model.Location;
import com.wangzhenfei.cocos2dgame.model.PropStatusInfo;
import com.wangzhenfei.cocos2dgame.model.SaveUserInfo;
import com.wangzhenfei.cocos2dgame.socket.MsgData;
import com.wangzhenfei.cocos2dgame.socket.MySocket;
import com.wangzhenfei.cocos2dgame.tool.AsyTaskForLoadNetPicture;
import com.wangzhenfei.cocos2dgame.tool.SpriteUtils;

import org.cocos2d.actions.UpdateCallback;
import org.cocos2d.layers.CCScene;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
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
    private World bxWorld = null;
    protected static final float FPS = (float)CCDirector.sharedDirector().getAnimationInterval();
    private static float rdelta = 0;

    // 自己的
    private CCSprite myControlBar;
    private Body myControlBarBody;
    // 别人的
    private CCSprite offsetControlBar;
    private Body offsetControlBarBody;
    // 球的集合
    private List<Integer> balls = new ArrayList<Integer>();
    private Body ballBody;
    private boolean start;
    CCSprite spEnd;

    private CCSprite subNum;
    private CCSprite bgNum;
    private int timeCount = 5;
    private Bitmap mBitmap;
    private Bitmap oBitmap;

    private long time;
    private float v = SpriteConfig.v;;

    public GameProjectionLayer(BattleInitInfo info) {
        super();
        EventBus.getDefault().register(this);

        Vector2 gravity = new Vector2(0f, 0f);
        bxWorld = new World(gravity, true);
        bxWorld.setContinuousPhysics(true);

        SpriteConfig.CONTROL_BAR_W = SpriteConfig.NORMAL_CONTROL_BAR_W;
        this.setIsTouchEnabled(true);
        if(SaveUserInfo.getInstance().getId() == info.getInitiativePlayer().getId()){
            myBatter = info.getInitiativePlayer();
            offsetBatter = info.getPassivityPlayer();
        }else {
            myBatter = info.getPassivityPlayer();
            offsetBatter = info.getInitiativePlayer();
        }

        addSprite();
        balls.add(SpriteConfig.TAG_ADD_BALL1);
        balls.add(SpriteConfig.TAG_ADD_BALL2);
        balls.add(SpriteConfig.TAG_NORMAL_BALL);
        schedule("startMinus", 1);
        uploadAvatar();
    }

    private void uploadAvatar() {
        new AsyTaskForLoadNetPicture() {
            @Override
            public void onResult(Bitmap bitmap) {
                oBitmap = bitmap;

            }
        }.execute(SaveUserInfo.getInstance().getResourceShowPath() + offsetBatter.getAvatar());

        new AsyTaskForLoadNetPicture() {
            @Override
            public void onResult(Bitmap bitmap) {
                    mBitmap = bitmap;
            }
        }.execute(SaveUserInfo.getInstance().getResourceShowPath() + myBatter.getAvatar());
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
            Vector2 vector2 = new Vector2();
            vector2.x = -10;
            vector2.y = -10;
            ballBody.setLinearVelocity(vector2);
            start = true;
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
                Location location = infos.get(i).getLocation();
                CCSprite ball = (CCSprite) getChildByTag(infos.get(i).getId());
                if(ball != null){
                    CGPoint point = CGPoint.ccp((1 - location.getX()) * screenWith,
                            (1 - location.getY()) * screenHeight);
                    ball.setPosition(point);
                    Vector2 vector2 = new Vector2();
                    vector2.set(-location.getVx(),-location.getVy());
                    ballBody.setLinearVelocity(vector2);
                    ballBody.setTransform(new Vector2(point.x / PTM_RATIO, point.y / PTM_RATIO), location.getAngle() + 180);
                    v = location.getV();
                }
            }
        }

    }

    /**
     * 杆子的位置回调
     * @param infos
     */
    public void onEvent( BallAndBarPosition infos) {
        if(infos != null){
            CGPoint position = offsetControlBar.getPosition();
            position.x = (1 - infos.getPoleX()) * screenWith;
            offsetControlBar.setPosition(position);
            Vector2 vector2 = new Vector2(position.x  / PTM_RATIO,
                    (screenHeight - (SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 2 * SpriteConfig.CONTROL_TO_BRICK))/ PTM_RATIO);
            offsetControlBarBody.setTransform(vector2, 0);
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
     * 碰撞的回调
     * @param infos
     */
    BattleBrick battleBrick;
    public void onEventBackgroundThread( BattleBrick infos) {
        battleBrick = infos;
    }

    public void onEvent(ActivityLifeCycle cycle){
        if(!cycle.isShow()){ // 应用退到后台
            spEnd = SpriteUtils.getSprite("marbles_text_failure.png",screenWith, 300, false, -1);
            spEnd.setPosition(CGPoint.ccp(screenWith / 2, screenHeight / 2));
            spEnd.setColor(new ccColor3B(255, 255, 255));
            this.addChild(spEnd);

            MsgData msgData = new MsgData();
            msgData.setCode(RequestCode.BATTLE_END);
            msgData.setData(new BattleEndRequest(offsetBatter.getId()));
            MySocket.getInstance().setMessage(msgData);
        }
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
        addWall();
        addBalls(SpriteConfig.TAG_NORMAL_BALL, false);
//        addBalls(SpriteConfig.TAG_ADD_BALL1);
//        addBalls(SpriteConfig.TAG_ADD_BALL2);
        addMyHome();
        addOffsetHome();
    }

    /**
     * 增加墙
     */
    private void addWall() {

        float scaledWidth = screenWith/PTM_RATIO;
        float scaledHeight = screenHeight/PTM_RATIO;
        // Define the ground body.
        BodyDef bxGroundBodyDef = new BodyDef();
        bxGroundBodyDef.position.set(0.0f, 0.0f);

        // Call the body factory which allocates memory for the ground body
        // from a pool and creates the ground box shape (also from a pool).
        // The body is also added to the world.
        Body groundBody = bxWorld.createBody(bxGroundBodyDef);

        // Define the ground box shape.
        EdgeShape groundBox = new EdgeShape();

        Vector2 bottomLeft = new Vector2(0f,0f);
        Vector2 topLeft = new Vector2(0f,scaledHeight);
        Vector2 topRight = new Vector2(scaledWidth,scaledHeight);
        Vector2 bottomRight = new Vector2(scaledWidth,0f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundBox;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 1.0f;

        // bottom
        groundBox.set(bottomLeft, bottomRight);
        groundBody.createFixture(fixtureDef);

        // top
        groundBox.set(topLeft, topRight);
        groundBody.createFixture(fixtureDef);

        // left
        groundBox.set( topLeft, bottomLeft );
        groundBody.createFixture(fixtureDef);

        // right
        groundBox.set( topRight, bottomRight );
        groundBody.createFixture(fixtureDef);
    }

    private void addBalls(int tag, boolean b) {
        //添加球
        CCSprite ball = SpriteUtils.getSprite("marbles_ball.png", SpriteConfig.BALL_SIZE, SpriteConfig.BALL_SIZE, false, tag);
        CGPoint ballPoint = CGPoint.ccp(screenWith / 2, screenHeight - (SpriteConfig.BALL_SIZE / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + SpriteConfig.NORMAL_CONTROL_BAR_H));
        ball.setPosition(ballPoint);
        this.addChild(ball);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(ballPoint.x / PTM_RATIO, ballPoint.y / PTM_RATIO);
        CircleShape dynamicBox = new CircleShape();
        dynamicBox.setRadius(SpriteConfig.BALL_SIZE / 2 / PTM_RATIO);//These are mid points for our 1m box
        Body body = bxWorld.createBody(bodyDef);
        body.setUserData(ball);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicBox;
        fixtureDef.density = 0f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 1.0f;
        body.createFixture(fixtureDef);
        this.ballBody = body;
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
                addToWorld(brickBg, BodyDef.BodyType.StaticBody, SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE);
            }
        }

        //添加我的主堡垒
         CCSprite spHome = SpriteUtils.getSprite("app_logo.png", SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE, false, E_GameType.MY_MASTER.getCode());
        spHome.setPosition(CGPoint.ccp(screenWith / 2, SpriteConfig.NORMAL_HOME_BRICK_SIZE / 2));
        this.addChild(spHome);
        addToWorld(spHome, BodyDef.BodyType.StaticBody, SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE);



        //添加杆子
        myControlBar = SpriteUtils.getSprite("marbles_baffle.png", SpriteConfig.CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H, false, SpriteConfig.TAG_MY_NORMAL_CONTROL_BAR);
        CGPoint ccp = CGPoint.ccp(screenWith / 2, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + SpriteConfig.CONTROL_TO_BRICK);
        myControlBar.setPosition(ccp);
        this.addChild(myControlBar);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(ccp.x / PTM_RATIO, ccp.y / PTM_RATIO);

        PolygonShape dynamicBox = new PolygonShape();
        dynamicBox.setAsBox(SpriteConfig.CONTROL_BAR_W / 2 / PTM_RATIO, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 / PTM_RATIO);
        myControlBarBody = bxWorld.createBody(bodyDef);
        myControlBarBody.setUserData(myControlBar);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicBox;
        fixtureDef.density = 1000.0f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 1.0f;
        myControlBarBody.createFixture(fixtureDef);
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
                addToWorld(brickBg, BodyDef.BodyType.StaticBody, SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE);
            }
        }

        //添加他人的主堡垒
        CCSprite spHome = SpriteUtils.getSprite("app_logo.png", SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE, false, E_GameType.OPPOSITE_MASTER.getCode());
        spHome.setPosition(CGPoint.ccp(screenWith / 2, screenHeight - SpriteConfig.NORMAL_HOME_BRICK_SIZE / 2));
        this.addChild(spHome);
        addToWorld(spHome, BodyDef.BodyType.StaticBody, SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE);

        //添加杆子
        offsetControlBar = SpriteUtils.getSprite("marbles_baffle.png", SpriteConfig.CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H, false, SpriteConfig.TAG_OFFSET_NORMAL_CONTROL_BAR);
        CGPoint ccp = CGPoint.ccp(screenWith / 2,
                screenHeight - (SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + SpriteConfig.CONTROL_TO_BRICK));
        offsetControlBar.setPosition(ccp);
        this.addChild(offsetControlBar);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(ccp.x / PTM_RATIO, ccp.y / PTM_RATIO);

        PolygonShape dynamicBox = new PolygonShape();
        dynamicBox.setAsBox(SpriteConfig.CONTROL_BAR_W / 2 / PTM_RATIO, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 / PTM_RATIO);

        offsetControlBarBody = bxWorld.createBody(bodyDef);
        offsetControlBarBody.setUserData(offsetControlBar);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicBox;
        fixtureDef.density = 1000.0f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 1.0f;
        offsetControlBarBody.createFixture(fixtureDef);
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

    private int second = 0;
    private void tick(float d) {
        second ++;
        if(second == 60){
            second  = 0;
        }
        if(oBitmap != null){
            CCSprite spHome = SpriteUtils.getSprite(oBitmap, SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE, false, E_GameType.OPPOSITE_MASTER.getCode());
            spHome.setPosition(CGPoint.ccp(screenWith / 2, screenHeight - SpriteConfig.NORMAL_HOME_BRICK_SIZE / 2));
            GameProjectionLayer.this.addChild(spHome);
            oBitmap = null;
        }

        if(mBitmap != null){
            CCSprite spHome = SpriteUtils.getSprite(mBitmap, SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE, false, E_GameType.MY_MASTER.getCode());
            spHome.setPosition(CGPoint.ccp(screenWith / 2, SpriteConfig.NORMAL_HOME_BRICK_SIZE / 2));
            GameProjectionLayer.this.addChild(spHome);
            mBitmap = null;
        }

        if(result != null){
            start = false;
            if(result.isWin()){
                spEnd = SpriteUtils.getSprite("marbles_text_victory.png", screenWith, 300, false, -1);
                spEnd.setPosition(CGPoint.ccp(screenWith / 2, screenHeight / 2));
                spEnd.setColor(new ccColor3B(255, 255, 255));
                this.addChild(spEnd);
            }else {
                spEnd = SpriteUtils.getSprite("marbles_text_failure.png", screenWith, 300, false, -1);
                spEnd.setPosition(CGPoint.ccp(screenWith / 2, screenHeight / 2));
                spEnd.setColor(new ccColor3B(255, 255, 255));
                this.addChild(spEnd);
            }
        }

        if(battleBrick != null){
            CGPoint point  = CGPoint.getZero();
            CCSprite sprite = (CCSprite) getChildByTag(battleBrick.getId());
            if(sprite != null){
                point = sprite.getPosition();
            }
            sprite.setTag(999);
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
        if(!start){
            return;
        }
        if ((rdelta += d) < FPS) return;
        rdelta = 0;
        synchronized (bxWorld) {
            bxWorld.step(FPS, 8, 1);
        }
        // Iterate over the bodies in the physics world
        Iterator<Body> it = bxWorld.getBodies();
        while(it.hasNext()) {
            Body b = it.next();
            Object userData = b.getUserData();
            final CCSprite sprite = (CCSprite)userData;
            final Vector2 pos = b.getPosition();
            if (userData != null && userData instanceof CCSprite) {
                if(sprite.getTag() == 999){
                    removeChildByTag(999, true);
                    bxWorld.destroyBody(b);
                }
                //Synchronize the Sprites position and rotation with the corresponding body
                if(balls.contains(sprite.getTag()) && start) { // 球的运动
                    Vector2 linearVelocity = b.getLinearVelocity();
//                    sprite.setPosition(pos.x * PTM_RATIO, pos.y * PTM_RATIO);
                    if(time != 0){
                        long now = System.currentTimeMillis();
                        CGPoint point = SpriteUtils.getNewPoint(sprite.getPosition(), linearVelocity.x, linearVelocity.y, (float) 1.0 * (now - time) / 1000,v);
                        sprite.setPosition(point);
                        b.setTransform(new Vector2(point.x / PTM_RATIO, point.y / PTM_RATIO), b.getAngle());
                        time = now;
                    }
                }

//                sprite.setPosition(pos.x * PTM_RATIO, pos.y * PTM_RATIO);
//                if(Math.abs(linearVelocity.y) < 1){
//                    if(sprite.getPosition().y > screenHeight / 2){
//                        linearVelocity.y = -10;
//                    }else {
//                        linearVelocity.y = 10;
//                    }
//
//                }
//                if(Math.abs(linearVelocity.x) < 1){
//                    if(sprite.getPosition().x > screenWith / 2){
//                        linearVelocity.x = -10;
//                    }else {
//                        linearVelocity.x = 10;
//                    }
//                }
//                sprite.setPosition(pos.x * PTM_RATIO, pos.y * PTM_RATIO);
            }else if(userData != null && userData instanceof String){ // 销毁
                bxWorld.destroyBody(b);
            }
        }
        time = System.currentTimeMillis();
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
            point.y = point.y-5;
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
            point.y = point.y-5;
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
            point.y = point.y-5;
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
            msgData.setCode(RequestCode.BATTLE_DATA_PROP_END);
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
            msgData.setCode(RequestCode.BATTLE_DATA_PROP_END);
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
            msgData.setCode(RequestCode.BATTLE_DATA_PROP_END);
            msgData.setData(new PropStatusInfo(E_GameType.ADD_SPEED.getCode(), false));
            MySocket.getInstance().setMessage(msgData);
        }
    }
    // ***************************道具持续的时间***************************************************

    /**
     * 添加到世界中
     * @param ball
     * @param type
     */
    private Body addToWorld(CCSprite ball,BodyDef.BodyType type,int w , int h) {
        CGPoint ballPoint = ball.getPosition();
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = type;
        bodyDef.position.set(ballPoint.x / PTM_RATIO, ballPoint.y / PTM_RATIO);
        PolygonShape dynamicBox = new PolygonShape();
        dynamicBox.setAsBox(w / 2 / PTM_RATIO, h / 2 / PTM_RATIO);//These are mid points for our 1m box
        Body body = bxWorld.createBody(bodyDef);
        body.setUserData(ball);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicBox;
        fixtureDef.density = 1000.0f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 1.0f;
        body.createFixture(fixtureDef);
        return body;
    }
    //***********************************触摸事件********************************************************

    /**
     * 触摸屏幕时
     */
    @Override
    public boolean ccTouchesBegan(MotionEvent event) {
        if(!start){
            return super.ccTouchesBegan(event);
        }
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
            myControlBar.setPosition(CGPoint.ccp(p2.x, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + SpriteConfig.CONTROL_TO_BRICK));
//            MsgData msgData = new MsgData();
//            msgData.setCode(RequestCode.BATTLE_DATA_STICK);
//            msgData.setData(new ControlBarInfo(p2.x/screenWith));
//            MySocket.getInstance().setUdpMessageToClient(msgData);
            sendControlBarLocation();
        }
        return super.ccTouchesBegan(event);
    }

    /**
     * 移动时
     */
    @Override
    public boolean ccTouchesMoved(MotionEvent event) {
        if(!start){
            return super.ccTouchesMoved(event);
        }
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
            myControlBar.setPosition(CGPoint.ccp(p2.x, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + SpriteConfig.CONTROL_TO_BRICK));
//            MsgData msgData = new MsgData();
//            msgData.setCode(RequestCode.BATTLE_DATA_STICK);
//            msgData.setData(new ControlBarInfo(p2.x/screenWith));
//            MySocket.getInstance().setUdpMessageToClient(msgData);
            sendControlBarLocation();
        }
        return super.ccTouchesMoved(event);
    }

    /**
     * 触摸结束时
     */
    @Override
    public boolean ccTouchesEnded(MotionEvent event) {
        if(spEnd != null){
            float x = event.getX();
            float y = event.getY();
            CGPoint p1 = CGPoint.ccp(x, y);
            // 将以左上角为原点的坐标转换为以左下角为原点的坐标
            CGPoint p2 = CCDirector.sharedDirector().convertToGL(p1);
            Rect rect = SpriteUtils.getSpriteRect(spEnd, 400, 400);
            if(rect.contains((int)p2.x, (int)p2.y)){
                goToNext();
            }
        }
        return super.ccTouchesEnded(event);
    }

    /**
     * 发送杆子的位置
     */
    private void sendControlBarLocation() {
        MsgData msgData = new MsgData();
        msgData.setCode(RequestCode.BATTLE_DATA_STICK);
        BallAndBarPosition position = new BallAndBarPosition();
        position.setPoleX(myControlBar.getPosition().x / screenWith);
        msgData.setData(position);
        MySocket.getInstance().setUdpMessageToClient(msgData);
    }
    //***********************************触摸事件********************************************************
}
