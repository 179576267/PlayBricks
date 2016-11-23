package com.wangzhenfei.cocos2dgame.layer;

import android.os.HandlerThread;
import android.view.MotionEvent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.wangzhenfei.cocos2dgame.SpriteConfig;
import com.wangzhenfei.cocos2dgame.model.BattleBall;
import com.wangzhenfei.cocos2dgame.model.BattleBrick;
import com.wangzhenfei.cocos2dgame.model.BattleInitInfo;
import com.wangzhenfei.cocos2dgame.model.ControlBarInfo;
import com.wangzhenfei.cocos2dgame.model.Location;
import com.wangzhenfei.cocos2dgame.model.UserInfo;
import com.wangzhenfei.cocos2dgame.socket.MsgData;
import com.wangzhenfei.cocos2dgame.socket.MySocket;
import com.wangzhenfei.cocos2dgame.socket.RequestCode;
import com.wangzhenfei.cocos2dgame.tool.SpriteUtils;

import org.cocos2d.actions.UpdateCallback;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import de.greenrobot.event.EventBus;

/**
 * Created by wangzhenfei on 2016/11/14.
 * 游戏镜像世界
 */
public class GameProjectionLayer extends BaseCCLayer{
    private World bxWorld = null;
    protected static final float FPS = (float)CCDirector.sharedDirector().getAnimationInterval();
    private static float rdelta = 0;
    private long time = 0;
    CCSprite ball;
    private Body ballBody;
    private BattleInitInfo.InitiativeUserBean myBatter;
    private BattleInitInfo.InitiativeUserBean offsetBatter;
    private Vector2 direction;

    // 自己的
    private CCSprite myControlBar;
    private Body myControlBarBody;
    // 别人的
    private CCSprite offsetControlBar;
    private Body offsetControlBarBody;
    LinkedBlockingQueue<Location> queue = new LinkedBlockingQueue<Location>();
    // 球的集合
    private List<Integer> balls = new ArrayList<Integer>();
    HandlerThread callHandlerThread = new HandlerThread("callHandlerThread");
    { callHandlerThread.start(); }
    private boolean start;
    private int timeCount;

    public GameProjectionLayer(BattleInitInfo info) {
        super();
        Vector2 gravity = new Vector2(0f, 0f);
        bxWorld = new World(gravity, true);
        bxWorld.setContinuousPhysics(true);
        EventBus.getDefault().register(this);
        this.setIsTouchEnabled(true);
        long id = Thread.currentThread().getId();
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
    }

    @Override
    public void goToNext() {
        EventBus.getDefault().unregister(this);
    }

    /**
     * 球的位置回调
     * @param infos
     */
    public void onEvent( BattleBall infos) {
        if(infos != null){
//            CGPoint point = CGPoint.ccp((1 - infos.getLocation().getX()) * screenWith,
//                    (1 - infos.getLocation().getY()) * screenHeight);
//            ball.setPosition(point);
//            Vector2 linearVelocity = new Vector2();
//            linearVelocity.x = - infos.getLocation().getVx();
//            linearVelocity.y = - infos.getLocation().getVy();
//            ballBody.setLinearVelocity(linearVelocity);
//
//            Vector2 vector2 = new Vector2(point.x  / PTM_RATIO, point.y / PTM_RATIO);
//            ballBody.setTransform(vector2, 0);
//            direction = new Vector2(-infos.getLocation().getVx(), -infos.getLocation().getVy());
//            CGPoint point = CGPoint.ccp((1 - infos.getLocation().getX()) * screenWith,
//                    (1 - infos.getLocation().getY()) * screenHeight);
//            ball.setPosition(point);
            try {
                queue.put(infos.getLocation());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

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
    public void onEventBackgroundThread( BattleBrick infos) {
        if(infos != null){
            removeChildByTag(infos.getId(),true);
            removeChildByTag(infos.getId() - 50, true);
        }
    }
    private void addSprite() {
        addBg();
        addBalls(SpriteConfig.TAG_NORMAL_BALL);
        addMyHome();
        addOffsetHome();
        addWall();
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


    private void addBalls(int tag) {
        //添加球
        ball = SpriteUtils.getSprite("marbles_ball.png", SpriteConfig.BALL_SIZE, SpriteConfig.BALL_SIZE, false, tag);
        CGPoint ballPoint = CGPoint.ccp(screenWith / 2, screenHeight - (SpriteConfig.BALL_SIZE / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + SpriteConfig.NORMAL_CONTROL_BAR_H));
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
        fixtureDef.restitution = 0f;
        ballBody.createFixture(fixtureDef);

//        Vector2 linearVelocity = new Vector2();
//        linearVelocity.x = 10;
//        linearVelocity.y = 30;
//        ballBody.setLinearVelocity(linearVelocity);

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
                addToWorld(brickBg, SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE);
            }
        }

        //添加我的主堡垒
        CCSprite spHome = SpriteUtils.getSprite("home.png", SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE, false, SpriteConfig.TAG_MY_NORMAL_HOME_BRICK);
        spHome.setPosition(CGPoint.ccp(screenWith / 2, SpriteConfig.NORMAL_HOME_BRICK_SIZE / 2));
        this.addChild(spHome);
        addToWorld(spHome,  SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE);


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
        fixtureDef.restitution = 0f;
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
                addToWorld(brickBg, SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE);
            }
        }

        //添加他人的主堡垒
        CCSprite spHome = SpriteUtils.getSprite("app_logo.png", SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE, false, SpriteConfig.TAG_OFFSET_NORMAL_HOME_BRICK);
        spHome.setPosition(CGPoint.ccp(screenWith / 2, screenHeight - SpriteConfig.NORMAL_HOME_BRICK_SIZE / 2));
        this.addChild(spHome);
        addToWorld(spHome, SpriteConfig.NORMAL_HOME_BRICK_SIZE, SpriteConfig.NORMAL_HOME_BRICK_SIZE);
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
        fixtureDef.restitution = 0f;
        offsetControlBarBody.createFixture(fixtureDef);
    }



    /**
     * 添加到世界中
     * @param ball
     */
    private Body addToWorld(CCSprite ball,int w , int h) {
        CGPoint ballPoint = ball.getPosition();
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(ballPoint.x / PTM_RATIO, ballPoint.y / PTM_RATIO);
        PolygonShape dynamicBox = new PolygonShape();
        dynamicBox.setAsBox(w / 2 / PTM_RATIO, h / 2 / PTM_RATIO);//These are mid points for our 1m box
        Body body = bxWorld.createBody(bodyDef);
        body.setUserData(ball);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicBox;
        fixtureDef.density = 1000.0f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 0f;
        body.createFixture(fixtureDef);
        return body;
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
        rdelta = 0;
        timeCount ++;
        // Iterate over the bodies in the physics world
        Iterator<Body> it = bxWorld.getBodies();
        while(it.hasNext()) {
            Body b = it.next();
            Object userData = b.getUserData();
            if (userData != null && userData instanceof CCSprite) {
                //Synchronize the Sprites position and rotation with the corresponding body
                final CCSprite sprite = (CCSprite)userData;
                final Vector2 pos = b.getPosition();
                if(sprite != null && balls.contains(sprite.getTag())){ // 球的运动
//                    long now = System.currentTimeMillis();
//                    if(time != 0 && direction != null){
//                        CGPoint point = SpriteUtils.getNewPoint(sprite.getPosition(), direction.x, direction.y, (now - time) * 1.0f / 1000);
//                        sprite.setPosition(point);
//                        Vector2 vector2 = new Vector2(point.x  / PTM_RATIO, point.y/ PTM_RATIO);
//                        b.setTransform(vector2,b.getAngle());
//                    }
//                    time = now;
                    Location location = queue.poll();
                    if(location != null){
                        CGPoint point = CGPoint.ccp((1 - location.getX()) * screenWith,
                                (1 - location.getY()) * screenHeight);
//                        ball.setPosition(point);
                        CCMoveTo moveTo = CCMoveTo.action(delta,point);
                        ball.runAction(moveTo);
                    }
                    if(timeCount > 60){
                        timeCount = 0;
                        queue.clear();
                    }
                }
            }
        }
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
