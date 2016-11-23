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
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.wangzhenfei.cocos2dgame.SpriteConfig;
import com.wangzhenfei.cocos2dgame.model.BattleBall;
import com.wangzhenfei.cocos2dgame.model.BattleBrick;
import com.wangzhenfei.cocos2dgame.model.BattleEndRequest;
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
import com.wangzhenfei.cocos2dgame.tool.Utils;

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
    private BattleInitInfo.InitiativeUserBean myBatter;
    private BattleInitInfo.InitiativeUserBean offsetBatter;
    private boolean start;
    CCLabel endLable;
    private CCSprite subNum;
    private CCSprite bgNum;
    private int timeCount = 5;
    private Body ballBody;
    public GameLayer() {
    }

    public GameLayer(BattleInitInfo info) {
        super();
        SpriteConfig.CONTROL_BAR_W = SpriteConfig.NORMAL_CONTROL_BAR_W;
        if(info == null){
            info = Utils.readTestJson();
        }
        if(UserInfo.info.getId() == info.getInitiativeUser().getId()){
            myBatter = info.getInitiativeUser();
            offsetBatter = info.getPassivityUser();
        }else {
            myBatter = info.getPassivityUser();
            offsetBatter = info.getInitiativeUser();
        }
        MySocket.getInstance().ip = offsetBatter.getIp();
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
            }

            @Override
            public void endContact(Contact contact) {
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
            public void preSolve(Contact contact, Manifold oldManifold) {
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
                        }else {
                            if(spriteA.getTag() == myControlBar.getTag()){
                                if(contact.getWorldManifold().getNormal().y < -0.5f){
                                        Log.i("Contact", "preSolve:myControlBar" + contact.getWorldManifold().getNormal().toString());
                                        contact.setEnabled(false);
                                }
                            }

                            if(spriteA.getTag() == offsetControlBar.getTag()){
                                 if(contact.getWorldManifold().getNormal().y > 0.5f){
                                    Log.i("Contact", "preSolve:offsetControlBar" + contact.getWorldManifold().getNormal().toString());
                                    contact.setEnabled(false);
                                }
                            }
                        }
                    }else {
                        Log.i(TAG, spriteA.getTag() + "----"+ spriteB.getTag());
                    }
                }
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        };
        bxWorld.setContactListener(listener);
        addWall();
        addSprite();
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
            Vector2 vector2 = new Vector2();
            vector2.x = 20;
            vector2.y = 20;
            ballBody.setLinearVelocity(vector2);
            start = true;
            unschedule("startMinus");
        }else {
            setNum();
        }
        timeCount --;
    }


    /**
     * 发送球体的位置
     */
    private void sendBallLocation() {
        CCSprite ball = null;
        Location location = null;
        List<BattleBall> list = new ArrayList<BattleBall>();
        for(int i=0; i < balls.size(); i++){
            ball = (CCSprite) getChildByTag(balls.get(i));
            if(ball != null){
                location = new Location(ball.getPosition().x / screenWith, ball.getPosition().y / screenHeight);
                list.add(new BattleBall(ball.getTag(), location));
            }
        }
        MsgData msgData = new MsgData();
        msgData.setCode(RequestCode.BATTLE_DATA_BALL);
        msgData.setData(list);
        MySocket.getInstance().setUdpMessage(msgData);
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

    /**
     * 道具状态的回调
     */
    private boolean addMoreBall;
    public void onEventBackgroundThread(PropStatusInfo info) {
        if(info.getPropId() == E_GameType.ADD_WIDTH.getCode()){
            if(info.isShow()){
                offsetControlBar.setScaleX((SpriteConfig.EXPEND_CONTROL_BAR_W) / myControlBar.getContentSize().width);
            }else {
                offsetControlBar.setScaleX((SpriteConfig.NORMAL_CONTROL_BAR_W) / myControlBar.getContentSize().width);
            }
        }else  if(info.getPropId() == E_GameType.TRHEE_HOODLE.getCode()){
            if(info.isShow()){
                addMoreBall = true;
            }else {
                deleteMoreBall();
            }
        } else if(info.getPropId() == E_GameType.ADD_SPEED.getCode()){
                acclerate = info.isShow() ? 1 : -1;
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
     * 删除多余的球
     */
    private void deleteMoreBall() {
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


    private void dealContact(Body body, int tag) {
        CCSprite sprite = (CCSprite) body.getUserData();
        if(tag == E_GameType.MY_MASTER.getCode()){// 我输了
            endLable = CCLabel.makeLabel("YOU LOSE",  CGSize.make(400, 400), CCLabel.TextAlignment.CENTER, "", 80);
            endLable.setPosition(CGPoint.ccp(screenWith / 2, screenHeight / 2));
            endLable.setColor(new ccColor3B(255, 255, 255));
            this.addChild(endLable);
            start = false;

            MsgData msgData = new MsgData();
            msgData.setCode(RequestCode.BATTLE_END);
            msgData.setData(new BattleEndRequest(offsetBatter.getId()));
            MySocket.getInstance().setMessage(msgData);
            return;
        }else if(tag == E_GameType.OPPOSITE_MASTER.getCode()){// 对方输了
            endLable = CCLabel.makeLabel("YOU WIN", CGSize.make(400, 400), CCLabel.TextAlignment.CENTER, "", 80);
            endLable.setPosition(CGPoint.ccp(screenWith / 2, screenHeight / 2));
            endLable.setColor(new ccColor3B(255, 255, 255));
            this.addChild(endLable);
            start = false;

            MsgData msgData = new MsgData();
            msgData.setCode(RequestCode.BATTLE_END);
            msgData.setData(new BattleEndRequest(myBatter.getId()));
            MySocket.getInstance().setMessage(msgData);
            return;
        }
        if(tag < 1000 && tag > 50){ // 是砖块的碰撞
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
        addBalls(SpriteConfig.TAG_NORMAL_BALL, true);
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
        myControlBar = SpriteUtils.getSprite("marbles_baffle.png", SpriteConfig.CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H, false, E_GameType.MY_CONTROL_BAR.getCode());
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
        fixtureDef.friction = 0f;
        fixtureDef.restitution = 1.0f;
        myControlBarBody.createFixture(fixtureDef);
    }


    private void addBalls(int tag, boolean fromMe) {
        //添加球
        CCSprite ball = SpriteUtils.getSprite("marbles_ball.png", SpriteConfig.BALL_SIZE, SpriteConfig.BALL_SIZE, false, tag);
        CGPoint ballPoint ;
        if(fromMe){
            ballPoint = CGPoint.ccp(screenWith / 2, SpriteConfig.BALL_SIZE / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + SpriteConfig.NORMAL_CONTROL_BAR_H);
        }else {
            ballPoint = CGPoint.ccp(screenWith / 2, screenHeight - (SpriteConfig.BALL_SIZE / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + SpriteConfig.NORMAL_CONTROL_BAR_H));
        }
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
        if(tag == SpriteConfig.TAG_NORMAL_BALL){
            this.ballBody = body;
        }

        if(tag != SpriteConfig.TAG_NORMAL_BALL){
            Vector2 vector2 = new Vector2();
            if(tag != SpriteConfig.TAG_ADD_BALL1){
                vector2.x = 20;
            }else {
                vector2.x = -20;
            }
            if(fromMe){
                vector2.y = 20;
            }else {
                vector2.y = -20;
            }
            body.setLinearVelocity(vector2);
        }
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
        offsetControlBar = SpriteUtils.getSprite("marbles_baffle.png", SpriteConfig.CONTROL_BAR_W, SpriteConfig.NORMAL_CONTROL_BAR_H, false, E_GameType.OPPOSITE_CONTROL_BAR.getCode());
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
        fixtureDef.friction = 0f;
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
        if(!start){
            return;
        }

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

        if ((rdelta += delta) < FPS) return;
        if(addMoreBall){ // 增加来自对面多球的请求
            addMoreBall = false;
            addBalls(SpriteConfig.TAG_ADD_BALL1, false);
            addBalls(SpriteConfig.TAG_ADD_BALL2, false);
        }
        // 发送球的位置
        sendBallLocation();
        synchronized (bxWorld) {
            bxWorld.step(FPS, 8, 1);
        }
        catchProp();
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
                if(balls.contains(sprite.getTag()) && start){ // 球的运动
                    Vector2 linearVelocity = b.getLinearVelocity();
                    sprite.setPosition(pos.x * PTM_RATIO, pos.y * PTM_RATIO);
                    if(Math.abs(linearVelocity.y) < 1){
                        if(sprite.getPosition().y > screenHeight / 2){
                            linearVelocity.y = -10;
                        }else {
                            linearVelocity.y = 10;
                        }

                    }
                    if(Math.abs(linearVelocity.x) < 1){
                        if(sprite.getPosition().x > screenWith / 2){
                            linearVelocity.x = -10;
                        }else {
                            linearVelocity.x = 10;
                        }
                    }
                    if(acclerate > 0){
                        linearVelocity.x *= 2  ;
                        linearVelocity.y *= 2;
                        acclerate = 0;
                    }else if(acclerate < 0){
                        linearVelocity.x /= 2 ;
                        linearVelocity.y /= 2 ;
                        acclerate = 0;
                    }
                    ballBody.setLinearVelocity(linearVelocity);
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
                addBalls(SpriteConfig.TAG_ADD_BALL1,true);
                addBalls(SpriteConfig.TAG_ADD_BALL2,true);
                schedule("moreBall", 1);
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
                acclerate = 1;
                schedule("acclerate", 1);
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
            deleteMoreBall();

        }
    }

    private int acclerateSecond = 0;
    public void acclerate(float dx){
        acclerateSecond ++;
        if(acclerateSecond > 10){ // 结束道具
            acclerateSecond = 0;
            unschedule("acclerate");
            acclerate = -1;
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
            myControlBar.setPosition(CGPoint.ccp(p2.x, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10));
            Vector2 vector2 = new Vector2(p2.x  / PTM_RATIO, (SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10)/ PTM_RATIO);
            myControlBarBody.setTransform(vector2, 0);

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
            myControlBar.setPosition(CGPoint.ccp(p2.x, SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10));
            Vector2 vector2 = new Vector2(p2.x  / PTM_RATIO, (SpriteConfig.NORMAL_CONTROL_BAR_H / 2 + SpriteConfig.NORMAL_BRICK_SIZE * 3 + 10)/ PTM_RATIO);
            myControlBarBody.setTransform(vector2, 0);

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
