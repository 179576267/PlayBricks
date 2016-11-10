package com.wangzhenfei.cocos2dgame.tool;

import android.graphics.Rect;

import org.cocos2d.nodes.CCSprite;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;

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

}
