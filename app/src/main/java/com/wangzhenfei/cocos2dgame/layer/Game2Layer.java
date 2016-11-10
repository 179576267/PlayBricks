package com.wangzhenfei.cocos2dgame.layer;

import android.content.Context;
import android.graphics.BitmapFactory;
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

import org.cocos2d.actions.UpdateCallback;
import org.cocos2d.actions.instant.CCHide;
import org.cocos2d.actions.instant.CCShow;
import org.cocos2d.actions.interval.CCScaleBy;
import org.cocos2d.config.ccMacros;
import org.cocos2d.events.CCTouchDispatcher;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCLabel;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteSheet;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;

import java.util.Iterator;

/**
 * Created by wangzhenfei on 2016/11/7.
 */
public class Game2Layer extends CCLayer {
    public static final int kTagSpriteManager = 1;

    // Pixel to meters ratio. Box2D uses meters as the unit for measurement.
    // This ratio defines how many pixels correspond to 1 Box2D "meter"
    // Box2D is optimized for objects of 1x1 meter therefore it makes sense
    // to define the ratio so that your most common object type is 1x1 meter.
    protected static final float PTM_RATIO = 32.0f;

    //FPS for the PhysicsWorld to sync to
    protected static final float FPS = (float)CCDirector.sharedDirector().getAnimationInterval();
    private static float rdelta = 0;

    protected final World bxWorld;

    public Game2Layer() {
        super();

        this.setIsTouchEnabled(true);
//        this.setIsAccelerometerEnabled(true);

        CGSize s = CCDirector.sharedDirector().winSize();

        // Define the gravity vector.
        Vector2 gravity = new Vector2(0f, 0f);

        float scaledWidth = s.width/PTM_RATIO;
        float scaledHeight = s.height/PTM_RATIO;

//        	Vector2 lower = new Vector2(-BUFFER, -BUFFER);
//        	Vector2 upper = new Vector2(scaledWidth+BUFFER, scaledHeight+BUFFER);

        bxWorld = new World(gravity, true);
        bxWorld.setContinuousPhysics(true);

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

        // bottom
        groundBox.set( bottomLeft, bottomRight );
        groundBody.createFixture(groundBox,0);

        // top
        groundBox.set( topLeft, topRight );
        groundBody.createFixture(groundBox,0);

        // left
        groundBox.set( topLeft, bottomLeft );
        groundBody.createFixture(groundBox,0);

        // right
        groundBox.set( topRight, bottomRight );
        groundBody.createFixture(groundBox,0);

        //Set up sprite
//        CCSpriteSheet mgr = CCSpriteSheet.spriteSheet("blocks.png", 150);
//        addChild(mgr, 0, kTagSpriteManager);


        addNewSpriteWithCoords(CGPoint.ccp(s.width / 2.0f, s.height / 2.0f));
        addNewSpriteWithCoords(CGPoint.ccp(100, 100));
        addNewSpriteWithCoords(CGPoint.ccp(200, 100));
        addNewSpriteWithCoords(CGPoint.ccp(300, 100));
        addNewSpriteWithCoords(CGPoint.ccp(400, 100));
        addNewSpriteWithCoords(CGPoint.ccp(500, 100));

        CCLabel label = CCLabel.makeLabel("好玩的游戏", "DroidSans", 32);
        label.setPosition(CGPoint.make(s.width / 2f, s.height - 50f));
        label.setColor(new ccColor3B(255, 255, 255));
        addChild(label);
    }

    private UpdateCallback tickCallback = new UpdateCallback() {

        @Override
        public void update(float d) {
            tick(d);
        }
    };

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
    private void addNewSpriteWithCoords(CGPoint pos) {
        CCSprite sprite = CCSprite.sprite("app_logo.png");
        addChild(sprite);
        sprite.setPosition(pos);

        // Define the dynamic body.
        //Set up a 1m squared box in the physics world
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(pos.x/PTM_RATIO, pos.y/PTM_RATIO);

        // Define another box shape for our dynamic body.
        PolygonShape dynamicBox = new PolygonShape();
        dynamicBox.setAsBox(.5f, .5f);//These are mid points for our 1m box
//    		dynamicBox.density = 1.0f;
//            dynamicBox.friction = 0.3f;

        synchronized (bxWorld) {
            // Define the dynamic body fixture and set mass so it's dynamic.
            Body body = bxWorld.createBody(bodyDef);
            body.setUserData(sprite);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = dynamicBox;
            fixtureDef.density = 1.0f;
            fixtureDef.friction = 0f;
            fixtureDef.restitution = 1.0f;
            body.createFixture(fixtureDef);
        }
    }


    public synchronized void tick(float delta) {
        if ((rdelta += delta) < FPS) return;

        // It is recommended that a fixed time step is used with Box2D for stability
        // of the simulation, however, we are using a variable time step here.
        // You need to make an informed choice, the following URL is useful
        // http://gafferongames.com/game-physics/fix-your-timestep/

        // Instruct the world to perform a simulation step. It is
        // generally best to keep the time step and iterations fixed.
        synchronized (bxWorld) {
            bxWorld.step(FPS, 8, 1);
        }

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
                sprite.setPosition(pos.x * PTM_RATIO, pos.y * PTM_RATIO);
                sprite.setRotation(-1.0f * ccMacros.CC_RADIANS_TO_DEGREES(b.getAngle()));
            }
        }
    }

    CGPoint pointBegin;
    @Override
    public boolean ccTouchesBegan(MotionEvent event) {
        pointBegin = CCDirector.sharedDirector()
                .convertToGL(CGPoint.make(event.getX(), event.getY()));
        return CCTouchDispatcher.kEventHandled;
    }

    @Override
    public boolean ccTouchesEnded(MotionEvent event) {
        CGPoint position = CCDirector.sharedDirector()
                .convertToGL(CGPoint.make(event.getX(), event.getY()));
        float dx = (position.x - pointBegin.x)/ PTM_RATIO;
        float dy = (position.y - pointBegin.y)/ PTM_RATIO;

        // Iterate over the bodies in the physics world
        Iterator<Body> it = bxWorld.getBodies();
        while(it.hasNext()) {
            Body body = it.next();
            Vector2 v = body.getLinearVelocity();
            v.x += dx;
            v.y += dy;
            body.setLinearVelocity(v);
        }
        return super.ccTouchesEnded(event);
    }
}
