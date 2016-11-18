package com.wangzhenfei.cocos2dgame.layer;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.wangzhenfei.cocos2dgame.SpriteConfig;
import com.wangzhenfei.cocos2dgame.model.BattleBall;
import com.wangzhenfei.cocos2dgame.model.BattleBrick;
import com.wangzhenfei.cocos2dgame.model.BattleInitInfo;
import com.wangzhenfei.cocos2dgame.model.ControlBarInfo;
import com.wangzhenfei.cocos2dgame.model.E_GameType;
import com.wangzhenfei.cocos2dgame.model.Location;
import com.wangzhenfei.cocos2dgame.model.UserInfo;
import com.wangzhenfei.cocos2dgame.socket.RequestCode;
import com.wangzhenfei.cocos2dgame.socket.MsgData;
import com.wangzhenfei.cocos2dgame.socket.MySocket;
import com.wangzhenfei.cocos2dgame.tool.SpriteUtils;
import com.wangzhenfei.cocos2dgame.tool.Utils;

import org.cocos2d.actions.UpdateCallback;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by wangzhenfei on 2016/11/9.
 */
public class GameLayer extends BaseCCLayer{
    private  World bxWorld = null;
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

    private int acclerate = 0;
    private int ACCLERATR_RAT = 1300;
    private BattleInitInfo.InitiativeUserBean myBatter;
    private BattleInitInfo.InitiativeUserBean offsetBatter;
    private boolean start;
    private int sendTimes;
    HandlerThread callHandlerThread = new HandlerThread("callHandlerThread");
    { callHandlerThread.start(); }
    protected Handler handler = new Handler(callHandlerThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int forceX = 0;
            int forceY = 0;
            forceX = 500;
            forceY += 1500;
            ballBody.applyForceToCenter(forceX, forceY);
            start = true;
        }
    };

    private Body ballBody;
    public GameLayer() {
    }

    public GameLayer(BattleInitInfo info) {
        super();
        long id = Thread.currentThread().getId();
        if(info == null){
            info = Utils.readTestJson();
        }
//        if(UserInfo.info.getId() == info.getInitiativeUser().getId()){
            myBatter = info.getInitiativeUser();
            offsetBatter = info.getPassivityUser();
//        }else {
//            myBatter = info.getPassivityUser();
//            offsetBatter = info.getInitiativeUser();
//        }
        handler.sendEmptyMessageDelayed(0, 5000);
        EventBus.getDefault().register(this);
        this.setIsTouchEnabled(true);
        balls.add(SpriteConfig.TAG_ADD_BALL1);
        balls.add(SpriteConfig.TAG_ADD_BALL2);
        balls.add(SpriteConfig.TAG_NORMAL_BALL);

        Vector2 gravity = new Vector2(0f, 0f);
        bxWorld = new World(gravity, true);
        bxWorld.setContinuousPhysics(true);
        ContactListener listener = new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                CCSprite spriteA = null; //
                Body bodyA = contact.getFixtureA().getBody();
                if(bodyA != null && bodyA.getUserData() instanceof CCSprite){
                    spriteA = (CCSprite) bodyA.getUserData();
                }
                CCSprite spriteB = null; //
                Body bodyB = contact.getFixtureB().getBody();
                if(bodyB != null && bodyB.getUserData() instanceof CCSprite){
                    spriteB = (CCSprite) bodyB.getUserData();
                }
                if(bodyA != null && bodyB != null && spriteA != null && spriteB != null){ // 有效碰撞
                    Log.i(TAG, spriteA.getTag() + "----"+ spriteB.getTag());
                    // 有球的碰撞
                    if(balls.contains(spriteA.getTag()) ^ balls.contains(spriteB.getTag())){
                        if(balls.contains(spriteA.getTag())){
                            // 发送球的位置
                            sendBallLocation(bodyA);
                            dealContact(bodyB, spriteB.getTag());
                        }else {
                            sendBallLocation(bodyB);
                            dealContact(bodyA, spriteA.getTag());
                        }

                    }else {
                        Log.i(TAG, spriteA.getTag() + "----"+ spriteB.getTag());
                    }
                }

            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        };
        bxWorld.setContactListener(listener);
        addWall();
        addSprite();
    }

    /**
     * 发送球体的位置
     * @param body
     */
    private void sendBallLocation(Body body) {
        CCSprite sprite = (CCSprite) body.getUserData();
        Location location = new Location(sprite.getPosition().x / screenWith, sprite.getPosition().y / screenHeight, body.getLinearVelocity().x, body.getLinearVelocity().y);
        MsgData msgData = new MsgData();
        msgData.setCode(RequestCode.BATTLE_DATA_BALL);
        msgData.setData(new BattleBall(1, location));
        MySocket.getInstance().setMessage(msgData);
    }

    @Override
    public void goToNext() {
        EventBus.getDefault().unregister(this);
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
            Vector2 vector2 = new Vector2(position.x  / PTM_RATIO,
                    (screenHeight - (SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10))/ PTM_RATIO);
            offsetControlBarBody.setTransform(vector2, 0);
        }
    }
    private void dealContact(Body body, int tag) {
        CCSprite sprite = (CCSprite) body.getUserData();
        if(tag < 1000 && tag > -1){ // 是砖块的碰撞
            BattleInitInfo.InitiativeUserBean.BlockListBean brick = null;
            if(tag > 200){ // 他人的
                int index = tag % 200 - 1;
                CCSprite spProp;
                brick = offsetBatter.getBlockList().get(index);
                if(brick.getPropType() == E_GameType.ADD_SPEED.getCode()){
                    spProp = SpriteUtils.getSprite("marbles_prop_03.png",SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE,false, brick.getPropType());
                    spProp.setPosition(sprite.getPosition());
                    this.addChild(spProp);
                }else if(brick.getPropType() == E_GameType.ADD_WIDTH.getCode()){
                    spProp = SpriteUtils.getSprite("marbles_prop_01.png",SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE,false, brick.getPropType());
                    spProp.setPosition(sprite.getPosition());
                    this.addChild(spProp);
                }else if(brick.getPropType() == E_GameType.TRHEE_HOODLE.getCode()){
                    spProp = SpriteUtils.getSprite("marbles_prop_02.png",SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE,false, brick.getPropType());
                    spProp.setPosition(sprite.getPosition());
                    this.addChild(spProp);
                }
            }
            else if(tag > 100){ // 自己的
                int index = tag % 100 - 1;
                CCSprite spProp;
                brick = myBatter.getBlockList().get(index);
                if(brick.getPropType() == E_GameType.ADD_SPEED.getCode()){
//                    spProp = SpriteUtils.getSprite("marbles_prop_03.png",SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE,false, brick.getPropType());
//                    spProp.setPosition(sprite.getPosition());
//                    this.addChild(spProp);
                }else if(brick.getPropType() == E_GameType.ADD_WIDTH.getCode()){
//                    spProp = SpriteUtils.getSprite("marbles_prop_01.png",SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE,false, brick.getPropType());
//                    spProp.setPosition(sprite.getPosition());
//                    this.addChild(spProp);
                }else if(brick.getPropType() == E_GameType.TRHEE_HOODLE.getCode()){
//                    spProp = SpriteUtils.getSprite("marbles_prop_02.png",SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE,false, brick.getPropType());
//                    spProp.setPosition(sprite.getPosition());
//                    this.addChild(spProp);
                }
            }

            MsgData msgData = new MsgData();
            msgData.setCode(RequestCode.BATTLE_DATA_BUMP);
            msgData.setData(new BattleBrick(brick.getId(), brick.getType()));
            MySocket.getInstance().setMessage(msgData);

            removeChildByTag(tag,true);
            removeChildByTag(tag - 50, true);
            body.setUserData(new GameLayer());
        }
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

    private void addSprite() {
        addBg();
        addOffsetHome();
        addMyHome();
        addBalls(SpriteConfig.TAG_NORMAL_BALL);
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
        CCSprite spHome = SpriteUtils.getSprite("home.png", SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE, false, SpriteConfig.TAG_MY_NORMAL_HOME_BRICK);
        spHome.setPosition(CGPoint.ccp(screenWith / 2, SpriteConfig.NORMAL_HOME_BRICK_SIZE / 2));
        this.addChild(spHome);
        addToWorld(spHome, BodyDef.BodyType.StaticBody, SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE);


        //添加杆子
        myControlBar = SpriteUtils.getSprite("marbles_baffle.png", SpriteConfig.CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H, false, SpriteConfig.TAG_MY_NORMAL_CONTROL_BAR);
        CGPoint ccp = CGPoint.ccp(screenWith / 2, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10);
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
        fixtureDef.friction = 1f;
        fixtureDef.restitution = 1.0f;
        myControlBarBody.createFixture(fixtureDef);
    }


    private void addBalls(int tag) {
        //添加球
        CCSprite ball = SpriteUtils.getSprite("marbles_ball.png", SpriteConfig.BALL_SIZE, SpriteConfig.BALL_SIZE, false, tag);
        CGPoint ballPoint = CGPoint.ccp(screenWith / 2, SpriteConfig.BALL_SIZE / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + SpriteConfig.NORMAL_CONTROL_BAR_H);
        ball.setPosition(ballPoint);
        this.addChild(ball);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(ballPoint.x / PTM_RATIO, ballPoint.y / PTM_RATIO);
        CircleShape dynamicBox = new CircleShape();
        dynamicBox.setRadius(SpriteConfig.BALL_SIZE / 2 / PTM_RATIO);//These are mid points for our 1m box
        ballBody = bxWorld.createBody(bodyDef);
        ballBody.setUserData(ball);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicBox;
        fixtureDef.density = 0f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 1.0f;
        ballBody.createFixture(fixtureDef);

//        int forceX = 0;
//        int forceY = 0;
//        if(tag == SpriteConfig.TAG_ADD_BALL1){
//            forceX = -500;
//        }else {
//            forceX = 500;
//        }
//        forceY += 1500;
//        body.applyForceToCenter(forceX, forceY);
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
        CCSprite spHome = SpriteUtils.getSprite("app_logo.png", SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE, false, SpriteConfig.TAG_OFFSET_NORMAL_HOME_BRICK);
        spHome.setPosition(CGPoint.ccp(screenWith / 2, screenHeight - SpriteConfig.NORMAL_HOME_BRICK_SIZE / 2));
        this.addChild(spHome);
        addToWorld(spHome, BodyDef.BodyType.StaticBody, SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE);


        //添加杆子
        offsetControlBar = SpriteUtils.getSprite("marbles_baffle.png", SpriteConfig.CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H, false, SpriteConfig.TAG_OFFSET_NORMAL_CONTROL_BAR);
        CGPoint ccp = CGPoint.ccp(screenWith / 2,
                screenHeight - (SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10));
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
        fixtureDef.friction = 1f;
        fixtureDef.restitution = 1.0f;
        offsetControlBarBody.createFixture(fixtureDef);
    }

    private void addBg() {
        // 增加背景
        CCSprite bg = SpriteUtils.getSprite("marbles_background_02.png", screenWith, screenHeight, true, -1);
        this.addChild(bg);
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
    public synchronized void tick(float delta) {
        if ((rdelta += delta) < FPS) return;

        synchronized (bxWorld) {
            bxWorld.step(FPS, 8, 1);
        }
        catchProp();
        rdelta = 0;
        // Iterate over the bodies in the physics world
        Iterator<Body> it = bxWorld.getBodies();
        List<BattleBall> list = new ArrayList<BattleBall>();
        while(it.hasNext()) {
            Body b = it.next();
            Object userData = b.getUserData();

            if (userData != null && userData instanceof CCSprite) {
                //Synchronize the Sprites position and rotation with the corresponding body
                final CCSprite sprite = (CCSprite)userData;
                final Vector2 pos = b.getPosition();
                if(balls.contains(sprite.getTag()) && start){ // 球的运动
                    sprite.setPosition(pos.x * PTM_RATIO, pos.y * PTM_RATIO);
                    Vector2 linearVelocity = b.getLinearVelocity();

                    // 发送球的位置

                    if(linearVelocity.y == 0){
                        b.applyForceToCenter(0,20);
                    }
                    int forceX = 0;
                    int forceY = 0;
                    if(acclerate != 0){
                        if(acclerate > 0){ // 加速
                            if(linearVelocity.x > 0){
                                forceX = acclerate;
                            }else {
                                forceX = -acclerate;
                            }

                            if(linearVelocity.y > 0){
                                forceY = acclerate;
                            }else {
                                forceY = - acclerate;
                            }
                        }else { // 减速
                            if(linearVelocity.x > 0){
                                forceX = -acclerate;
                            }else {
                                forceX = acclerate;
                            }

                            if(linearVelocity.y > 0){
                                forceY = - acclerate;
                            }else {
                                forceY =  acclerate;
                            }
                        }
                        b.applyForceToCenter(forceX,forceY);
                    }
                }
            }else if(userData != null && userData instanceof GameLayer){ // 销毁
                bxWorld.destroyBody(b);
            }
        }
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
                addBalls(SpriteConfig.TAG_ADD_BALL1);
                addBalls(SpriteConfig.TAG_ADD_BALL2);
                schedule("moreBall", 1);
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
                acclerate = ACCLERATR_RAT;
                schedule("acclerate", 1);
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

    // ***************************道具持续的时间***************************************************
    private int expendLagerSecond = 0;
    public void expendLager(float dx){
        expendLagerSecond ++;
        if(expendLagerSecond > 10){ // 结束道具
            expendLagerSecond = 0;
            SpriteConfig.CONTROL_BAR_W = SpriteConfig.NORMAL_CONTROL_BAR_W;
            changeControlBarSize();
            unschedule("expendLager");
        }
    }

    private void changeControlBarSize() {
        myControlBar.setScaleX((SpriteConfig.CONTROL_BAR_W) / myControlBar.getContentSize().width);
        PolygonShape dynamicBox = new PolygonShape();
        dynamicBox.setAsBox(SpriteConfig.CONTROL_BAR_W / 2 / PTM_RATIO, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 / PTM_RATIO);
        Fixture fixture = myControlBarBody.getFixtureList().get(0);
        if(fixture != null){
            PolygonShape largeShape = (PolygonShape) fixture.getShape();
            largeShape.setAsBox((SpriteConfig.CONTROL_BAR_W) / 2 / PTM_RATIO, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 / PTM_RATIO);
        }
    }

    private int moreBallSecond = 0;
    public void moreBall(float dx){
        moreBallSecond ++;
        if(moreBallSecond > 10){ // 结束道具
            moreBallSecond = 0;
            unschedule("moreBall");
            CCSprite ball1 = (CCSprite) this.getChildByTag(SpriteConfig.TAG_ADD_BALL1);
            CCSprite ball2 = (CCSprite) this.getChildByTag(SpriteConfig.TAG_ADD_BALL2);
            if(ball1 != null && ball2 != null){
                Iterator<Body> it = bxWorld.getBodies();
                while(it.hasNext()) {
                    Body b = it.next();
                    Object userData = b.getUserData();
                    if (userData != null && userData instanceof CCSprite) {
                        CCSprite sprite = (CCSprite)userData;
                            if(sprite.getTag() == ball1.getTag()){
                                ball1.removeSelf();
                                b.setUserData(new GameLayer());
                            }else if(sprite.getTag() == ball2.getTag()){
                                ball2.removeSelf();
                                b.setUserData(new GameLayer());
                            }
                        }
                    }

            }

        }
    }

    private int acclerateSecond = 0;
    public void acclerate(float dx){
        acclerateSecond ++;
        if(acclerateSecond > 10){ // 结束道具
            acclerateSecond = 0;
            unschedule("acclerate");
            acclerate = - ACCLERATR_RAT;
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
            Vector2 vector2 = new Vector2(p2.x  / PTM_RATIO, (SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10)/ PTM_RATIO);
            myControlBarBody.setTransform(vector2, 0);

            MsgData msgData = new MsgData();
            msgData.setCode(RequestCode.BATTLE_DATA_STICK);
            msgData.setData(new ControlBarInfo(p2.x/screenWith));
            MySocket.getInstance().setMessage(msgData);
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
            Vector2 vector2 = new Vector2(p2.x  / PTM_RATIO, (SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10)/ PTM_RATIO);
            myControlBarBody.setTransform(vector2, 0);

            MsgData msgData = new MsgData();
            msgData.setCode(RequestCode.BATTLE_DATA_STICK);
            msgData.setData(new ControlBarInfo(p2.x/screenWith));
            MySocket.getInstance().setMessage(msgData);
        }
        return super.ccTouchesMoved(event);
    }

    /**
     * 触摸结束时
     */
    @Override
    public boolean ccTouchesEnded(MotionEvent event) {
//        myControlBar.runAction(CCHide.action());
        return super.ccTouchesEnded(event);
    }

    @Override
    public boolean ccTouchesCancelled(MotionEvent event) {
        return super.ccTouchesCancelled(event);
    }

    //***********************************触摸事件********************************************************
}
