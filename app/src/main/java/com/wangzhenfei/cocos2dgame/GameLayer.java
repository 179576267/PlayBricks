package com.wangzhenfei.cocos2dgame;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import org.cocos2d.actions.base.CCAction;
import org.cocos2d.actions.base.CCRepeatForever;
import org.cocos2d.actions.instant.CCFlipY;
import org.cocos2d.actions.instant.CCHide;
import org.cocos2d.actions.instant.CCShow;
import org.cocos2d.actions.interval.CCBlink;
import org.cocos2d.actions.interval.CCJumpTo;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCScaleBy;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.config.ccMacros;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCLabel;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;

import java.util.Iterator;

/**
 * Created by wangzhenfei on 2016/11/7.
 */
public class GameLayer extends CCLayer {
    private final String TAG = getClass().getSimpleName();
    // 像素与米比
    protected static final float PTM_RATIO = 32.0f;
    private Context mContext;
    private float screenWith;
    private float screenHeight;
    int brickNumber = 6;
    private CCSprite spControl;
    protected  World bxWorld;
    public GameLayer(Context context) {
        this.mContext = context;
        CGSize size = CCDirector.sharedDirector().winSize();
        screenWith = size.getWidth();
        screenHeight = size.getHeight();
        // 设置当前图层是否接受触摸事件 会执行回调
       this.setIsTouchEnabled(true);

        // Define the gravity vector.
        Vector2 gravity = new Vector2(9.8f, -9.8f);
        bxWorld = new World(gravity, true);
        bxWorld.setContinuousPhysics(true);
       init();
    }

    private void init() {
        setBg();
        setBricks();
        setBar();
        setBall();
        initControlBar();
        setFourWall();
    }

    /**
     * 创建四面墙
     */
    private void setFourWall() {

        float scaledWidth = screenWith /PTM_RATIO;
        float scaledHeight = screenHeight/PTM_RATIO;
        // 定义墙
        BodyDef bxGroundBodyDef = new BodyDef();
        bxGroundBodyDef.type = BodyDef.BodyType.StaticBody;
        bxGroundBodyDef.position.set(0.0f, 0.0f);

        // Call the body factory which allocates memory for the ground body
        // from a pool and creates the ground box shape (also from a pool).
        // The body is also added to the world.
        Body groundBody = bxWorld.createBody(bxGroundBodyDef);
        // 定义盒子的边缘.
        EdgeShape groundBox = new EdgeShape();

        Vector2 bottomLeft = new Vector2(0f,0f);
        Vector2 topLeft = new Vector2(0f,scaledHeight);
        Vector2 topRight = new Vector2(scaledWidth,scaledHeight);
        Vector2 bottomRight = new Vector2(scaledWidth,0f);

        // bottom
        groundBox.set( bottomLeft, bottomRight );
        groundBody.createFixture(groundBox, 0);

        // top
        groundBox.set( topLeft, topRight );
        groundBody.createFixture(groundBox, 0);

        // left
        groundBox.set( topLeft, bottomLeft );
        groundBody.createFixture(groundBox, 0);

        // right
        groundBox.set( topRight, bottomRight );
        groundBody.createFixture(groundBox, 0);
        this.schedule("update", 1);
    }

    public void update(float delta){
        bxWorld.step(delta, 8, 1);
        Iterator<Body> it = bxWorld.getBodies();
        while(it.hasNext()) {
            Body b = it.next();
            Object userData = b.getUserData();
            CCSprite  sp = (CCSprite)userData;
            Log.e("UPDATE", b.getPosition().toString());
            sp.setPosition(CGPoint.ccp(b.getPosition().x * PTM_RATIO,
                    b.getPosition().y * PTM_RATIO));
        }
    }

    private void initControlBar() {
        spControl = CCSprite.sprite("controlBar.png");
        spControl.setScaleX(300 / spControl.getContentSize().getWidth());
        spControl.setScaleY(50 / spControl.getContentSize().getHeight());
        this.addChild(spControl);
        spControl.runAction(CCHide.action());
    }

    /**
     * 设置球
     */
    private void setBall() {
        CCSprite sp = CCSprite.sprite("app_logo.png");
        sp.setPosition(screenWith / 2, bar.getPosition().y + 10 + sp.getContentSize().getHeight()/2);
        this.addChild(sp);

        BodyDef bxGroundBodyDef = new BodyDef();
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(screenWith / PTM_RATIO, screenHeight / PTM_RATIO);

        PolygonShape dynamicBox = new PolygonShape();
        dynamicBox.setAsBox(.5f, .5f);//These are mid points for our 1m box
        synchronized (bxWorld) {
            // Define the dynamic body fixture and set mass so it's dynamic.
            Body body = bxWorld.createBody(bxGroundBodyDef);
            body.setUserData(sp);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = dynamicBox;
            fixtureDef.density = 1.0f;
            fixtureDef.friction = 0.3f;
            body.createFixture(fixtureDef);
        }
    }

    /**
     * 创建移动条
     */
    CCSprite bar;
    private void setBar() {
        bar = CCSprite.sprite("brick.png");
        bar.setScaleX(240 / bar.getContentSize().getWidth());
        bar.setScaleY(20 / bar.getContentSize().getHeight());
        bar.setPosition(screenWith / 2, (screenWith / brickNumber) * 3 + 40);
        this.addChild(bar);
    }

    /**
     * 设置砖块
     */
    private void setBricks() {

        CCSprite sp = CCSprite.sprite("head_5.png");
        float spw = sp.getContentSize().getWidth();
        float screenW = screenWith / brickNumber; // 每行六个
        float rat = screenW / spw ;
        for(int i = 0 ; i < brickNumber; i++){ // 列
            for(int j = 0; j < 3; j++){ // 行
                sp = CCSprite.sprite("head_5.png");
//                sp.setPosition(i * screenW + spw /2 + (screenW - spw)/rat, j * screenW + spw /2 + (screenW - spw )/rat);
                sp.setPosition(i * screenW + spw / 2 * rat, j * screenW + spw /2 * rat);
                sp.setScaleX(rat);
                sp.setScaleY(rat);
                this.addChild(sp);
            }
        }
    }

    private void setBg() {
        // 创建大背景
        CCSprite sBg = CCSprite.sprite("bg.png");
        // CGPoint通常用于坐标表示，或者向量表示
        CGPoint point = CGPoint.getZero();
        // 设置精灵的位置
        sBg.setPosition(point);
        this.addChild(sBg);
        CCScaleBy scaleBy = CCScaleBy.action(0, 100);
        sBg.runAction(scaleBy);
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
        CGPoint p3 = CCDirector.sharedDirector().convertToUI(p2);
        Log.e(TAG, "p1.x:" + p1.x + "   p1.y:" + p1.y);
        Log.e(TAG, "p2.x:" + p2.x + "   p2.y:" + p2.y);
        Log.e(TAG, "p3.x:" + p3.x + "   p3.y:" + p3.y);

        if (p2.y < screenHeight / 2) { // 有效触发范围
//            spControl.runAction(CCMoveTo.action(0, CGPoint.ccp(p2.x, p2.y)));
            if(p2.x < 300 / 2){
                p2.x = 300 / 2;
            }else if(p2.x > screenWith - (300 / 2)){
                p2.x = screenWith - (300 / 2);
            }
            spControl.setPosition(CGPoint.ccp(p2.x, p2.y));
            spControl.runAction(CCShow.action());
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
//            spControl.setPosition(CGPoint.ccp(p2.x, p2.y));
            spControl.runAction(CCShow.action());
        }else {
            spControl.runAction(CCHide.action());
        }
        return super.ccTouchesMoved(event);
    }

    /**
     * 触摸结束时
     */
    @Override
    public boolean ccTouchesEnded(MotionEvent event) {
        spControl.runAction(CCHide.action());
        return super.ccTouchesEnded(event);
    }

    @Override
    public boolean ccTouchesCancelled(MotionEvent event) {
        return super.ccTouchesCancelled(event);
    }

    //***********************************触摸事件********************************************************
}
