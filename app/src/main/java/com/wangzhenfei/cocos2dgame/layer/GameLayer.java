package com.wangzhenfei.cocos2dgame.layer;

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
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.wangzhenfei.cocos2dgame.SpriteConfig;
import com.wangzhenfei.cocos2dgame.tool.SpriteUtils;

import org.cocos2d.actions.UpdateCallback;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    // 特殊道具位置
    private List<Integer> integers = new ArrayList<Integer>();

    // 球的集合
    private List<Integer> balls = new ArrayList<Integer>();

    public GameLayer() {
        super();
        this.setIsTouchEnabled(true);
        integers.add(10);
        integers.add(12);
        integers.add(41);
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
//                    Log.i(TAG, spriteA.getTag() + "----"+ spriteB.getTag());
                    // 有球的碰撞
                    if(balls.contains(spriteA.getTag()) ^ balls.contains(spriteB.getTag())){
                        if(balls.contains(spriteA.getTag())){
                            dealContact(bodyB, spriteB.getTag());
                        }else {
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

    private void dealContact(Body body, int tag) {
        CCSprite sprite = (CCSprite) body.getUserData();
        if(tag < 1000 && tag > -1){ // 是砖块的碰撞
            if(integers.contains(tag - SpriteConfig.OFFSET_BRICK_AND_BG)){ // 击中道具
                CCSprite spProp = SpriteUtils.getSprite("marbles_prop_02.png",SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE,false, SpriteConfig.TAG_PROP_1);
                spProp.setPosition(sprite.getPosition());
                this.addChild(spProp);
//                spProp.runAction(CCMoveTo.action(3, CGPoint.ccp(sprite.getPosition().x, -100)));
            }
            removeChildByTag(tag,true);
            removeChildByTag(tag - SpriteConfig.OFFSET_BRICK_AND_BG, true);
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
        groundBox.set( bottomLeft, bottomRight );
        groundBody.createFixture(fixtureDef);

        // top
        groundBox.set( topLeft, topRight );
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
        for(int i = 0; i < 6; i++){
            kk: for(int j = 0; j < 3 ;j++){
                if((j == 0 && i == 2) ||
                        (j == 0 && i == 3) ||
                        (j == 1 && i == 2) ||
                        (j == 1 && i == 3)){
                    continue kk;
                }
                if((i+j) % 2 == 0){
                    brickBg = SpriteUtils.getSprite("marbles_gem_base_01.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, SpriteConfig.TAG_NROMAL_BRICK);
                }else {
                    brickBg = SpriteUtils.getSprite("marbles_gem_base_02.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, SpriteConfig.TAG_NROMAL_BRICK);
                }

                if((i==1 && j==0)  ||
                        (i == 1 && j == 2) ||
                        (i == 4 && j == 1) ){
                    brick = SpriteUtils.getSprite("marbles_gem_prop.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, SpriteConfig.TAG_NROMAL_BRICK);
                }else {
                    brick = SpriteUtils.getSprite("marbles_gem_yellow.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, SpriteConfig.TAG_NROMAL_BRICK);
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
        myControlBar = SpriteUtils.getSprite("marbles_baffle.png", SpriteConfig.NORMAL_CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H, false, SpriteConfig.TAG_OFFSET_NORMAL_CONTROL_BAR);
        CGPoint ccp = CGPoint.ccp(screenWith / 2, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10);
        myControlBar.setPosition(ccp);
        this.addChild(myControlBar);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(ccp.x / PTM_RATIO, ccp.y / PTM_RATIO);

        PolygonShape dynamicBox = new PolygonShape();
        dynamicBox.setAsBox(SpriteConfig.NORMAL_CONTROL_BAR_W / 2 / PTM_RATIO, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 / PTM_RATIO);

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
        dynamicBox.setRadius(SpriteConfig.BALL_SIZE/2/PTM_RATIO);//These are mid points for our 1m box
        Body body = bxWorld.createBody(bodyDef);
        body.setUserData(ball);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicBox;
        fixtureDef.density = 0f;
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 1.0f;
        body.createFixture(fixtureDef);

        Vector2 v = body.getLinearVelocity();
        if(tag == SpriteConfig.TAG_ADD_BALL1){
            v.x -= 10;
        }else {
            v.x += 10;
        }
        v.y += 30;
        body.setLinearVelocity(v);
    }


    private void addOffsetHome() {
        // 增加自己的砖块
        CCSprite brickBg;
        CCSprite brick;
        for(int i = 0; i < 6; i++){
           kk: for(int j = 0; j < 3 ;j++){
                if((j == 0 && i == 2) ||
                        (j == 0 && i == 3) ||
                        (j == 1 && i == 2) ||
                        (j == 1 && i == 3)){
                    continue kk;
                }
                if((i+j) % 2 == 0){
                    brickBg = SpriteUtils.getSprite("marbles_gem_base_01.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, SpriteConfig.OFFSET_BRICK_AND_BG + i * 10 + j);
                }else {
                    brickBg = SpriteUtils.getSprite("marbles_gem_base_02.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, SpriteConfig.OFFSET_BRICK_AND_BG + i * 10 + j);
                }

               if(integers.contains(i * 10 + j) ){
                   brick = SpriteUtils.getSprite("marbles_gem_prop.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, i * 10 + j);
                   brick.setRotation(180);
//                   brickBg.setTag(SpriteConfig.TAG_MY_SPECIAL_BRICK1);
               }else {
                    brick = SpriteUtils.getSprite("marbles_gem_green.png", SpriteConfig.NORMAL_BRICK_SIZE, SpriteConfig.NORMAL_BRICK_SIZE, false, i * 10 + j);
//                   brickBg.setTag(SpriteConfig.TAG_NROMAL_BRICK);
               }
               CGPoint point = CGPoint.ccp(screenWith / 2 - SpriteConfig.NORMAL_BRICK_SIZE * (3 - i) + SpriteConfig.NORMAL_BRICK_SIZE / 2,
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
        offsetControlBar = SpriteUtils.getSprite("marbles_baffle.png", SpriteConfig.NORMAL_CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H, false, SpriteConfig.TAG_OFFSET_NORMAL_CONTROL_BAR);
        CGPoint ccp = CGPoint.ccp(screenWith / 2,
                screenHeight - (SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10));
        offsetControlBar.setPosition(ccp);
        this.addChild(offsetControlBar);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(ccp.x / PTM_RATIO, ccp.y / PTM_RATIO);

        PolygonShape dynamicBox = new PolygonShape();
        dynamicBox.setAsBox(SpriteConfig.NORMAL_CONTROL_BAR_W / 2 / PTM_RATIO, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 / PTM_RATIO);

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
        
        getProp();

        rdelta = 0;

        // Iterate over the bodies in the physics world
        Iterator<Body> it = bxWorld.getBodies();
        while(it.hasNext()) {
            Body b = it.next();
            Object userData = b.getUserData();

            if (userData != null && userData instanceof CCSprite) {
                //Synchronize the Sprites position and rotation with the corresponding body
                final CCSprite sprite = (CCSprite)userData;
                final Vector2 pos = b.getPosition();
                if(balls.contains(sprite.getTag())){ // 球的运动
                    sprite.setPosition(pos.x * PTM_RATIO, pos.y * PTM_RATIO);
                    Vector2 linearVelocity = b.getLinearVelocity();
                    if(linearVelocity.y == 0){
                        linearVelocity.y = 10;
                    }
                    b.setLinearVelocity(linearVelocity);
//                    myControlBar.setPosition(pos.x * PTM_RATIO, pos.y * PTM_RATIO);
                }
            }else if(userData != null && userData instanceof GameLayer){ // 销毁
                bxWorld.destroyBody(b);
            }
        }
//        ContactListener
    }

    private void getProp() {
        CCSprite prop = (CCSprite) this.getChildByTag(SpriteConfig.TAG_PROP_1);
        if(prop != null){
            //判断是否接住
            if(SpriteUtils.isSpriteConfict(myControlBar, SpriteConfig.NORMAL_CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H,
                    prop, SpriteConfig.PROP_SIZE, SpriteConfig.PROP_SIZE)){
                Log.i(TAG, "接住");
                prop.removeSelf();
                addBalls(SpriteConfig.TAG_ADD_BALL1);
                addBalls(SpriteConfig.TAG_ADD_BALL2);
            }
            CGPoint point = prop.getPosition();
            point.y = point.y-10;
            prop.setPosition(point);
            Log.i(TAG, point.toString());
            if(point.y < -100){
                prop.removeSelf();
            }
        }
    }

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
            if(p2.x < SpriteConfig.NORMAL_CONTROL_BAR_W / 2){
                p2.x = SpriteConfig.NORMAL_CONTROL_BAR_W / 2;
            }else if(p2.x > screenWith - (SpriteConfig.NORMAL_CONTROL_BAR_W / 2)){
                p2.x = screenWith - (SpriteConfig.NORMAL_CONTROL_BAR_W / 2);
            }
            myControlBar.setPosition(CGPoint.ccp(p2.x, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10));
            Vector2 vector2 = new Vector2(p2.x  / PTM_RATIO, (SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10)/ PTM_RATIO);
            myControlBarBody.setTransform(vector2, 0);
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
            if(p2.x < SpriteConfig.NORMAL_CONTROL_BAR_W / 2){
                p2.x = SpriteConfig.NORMAL_CONTROL_BAR_W / 2;
            }else if(p2.x > screenWith - (SpriteConfig.NORMAL_CONTROL_BAR_W / 2)){
                p2.x = screenWith - (SpriteConfig.NORMAL_CONTROL_BAR_W / 2);
            }
            myControlBar.setPosition(CGPoint.ccp(p2.x, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10));
            Vector2 vector2 = new Vector2(p2.x  / PTM_RATIO, (SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10)/ PTM_RATIO);
            myControlBarBody.setTransform(vector2, 0);
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
