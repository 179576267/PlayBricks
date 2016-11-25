package com.wangzhenfei.cocos2dgame.tool;

import android.graphics.Bitmap;
import android.util.Log;

import com.wangzhenfei.cocos2dgame.config.SpriteConfig;

import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCTextureCache;
import org.cocos2d.opengl.CCTexture2D;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;

/**
 * Created by wangzhenfei on 2016/11/9.
 */
public class SpriteUtils {
    public static CCSprite getSprite(String name,float w, float h, boolean resetAnchor, int tag){
        CCSprite sprite = CCSprite.sprite(name);
        sprite.setTag(tag);
        if(resetAnchor){
            sprite.setAnchorPoint(CGPoint.getZero());
        }
        float srcW = sprite.getContentSize().getWidth();
        float srcH = sprite.getContentSize().getHeight();
//        sprite.setContentSize(CGSize.make(w, h));
        sprite.setScaleX(w / srcW);
        sprite.setScaleY(h / srcH);
        return sprite;
    }

    public static CCSprite getSprite(Bitmap bitmap,float w, float h, boolean resetAnchor, int tag){
        CCSprite sprite = CCSprite.sprite(bitmap, tag + "");
        sprite.setTag(tag);
        if(resetAnchor){
            sprite.setAnchorPoint(CGPoint.getZero());
        }
        float srcW = sprite.getContentSize().getWidth();
        float srcH = sprite.getContentSize().getHeight();
//        sprite.setContentSize(CGSize.make(w, h));
        sprite.setScaleX(w / srcW);
        sprite.setScaleY(h / srcH);
        return sprite;
    }

    public static CGRect getSpriteRect(CCSprite sprite, int w, int h){
        CGPoint point = sprite.getPosition();
        CGRect rect = CGRect.make(point.x - w / 2 ,point.y - h / 2,
                point.x +  w / 2 ,point.y + h / 2);
        return rect;
    }

    public static boolean isSpriteConfict(CCSprite sprite1, int w1, int h1,CCSprite sprite2, int w2, int h2){
        CGPoint point1 = sprite1.getPosition();
        CGPoint point2 = sprite2.getPosition();
        return Utils.isCollsionWithRect(point1.x - w1 / 2,point1.y - h1 / 2 ,w1, h1,
                point2.x - w2 / 2,point2.y - h2 / 2 ,w2, h2);
    }

    public static CGPoint getNewPoint(CGPoint point, float vX, float vY, float interval){
//        if(vX ==  0){
//            vX = 10;
//        }
//        if(vY == 0){
//            vY = 10;
//        }
        float x = (float)(vX * interval * SpriteConfig.v * 100 / Math.sqrt(vX * vX + vY * vY));
        float y = (float)(vY * interval * SpriteConfig.v * 100 / Math.sqrt(vX * vX + vY * vY));
//        if(x + point.x > 720 || y + point.y > 1280){
//            return point;
//        }
//
//        if(x + point.x < 0 || y + point.y < 0){
//            return point;
//        }

        Log.i("SpriteUtils", point.toString() + "--" + vX +"--" + vY +"--" + interval + "   x:" + x + "y:" + y);
        CGPoint newPoint = CGPoint.ccp(point.x + x, point.y + y);
        return  newPoint;
    }

}
