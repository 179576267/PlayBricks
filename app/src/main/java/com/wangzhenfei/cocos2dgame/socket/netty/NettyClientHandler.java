package com.wangzhenfei.cocos2dgame.socket.netty;


import android.util.Log;

import com.wangzhenfei.cocos2dgame.model.BattleBall;
import com.wangzhenfei.cocos2dgame.model.BattleBrick;
import com.wangzhenfei.cocos2dgame.model.BattleInitInfo;
import com.wangzhenfei.cocos2dgame.model.BattleNotifyResponse;
import com.wangzhenfei.cocos2dgame.model.ControlBarInfo;
import com.wangzhenfei.cocos2dgame.model.Location;
import com.wangzhenfei.cocos2dgame.model.UserInfo;
import com.wangzhenfei.cocos2dgame.socket.RequestCode;
import com.wangzhenfei.cocos2dgame.tool.JsonUtils;
import com.wangzhenfei.cocos2dgame.tool.Utils;

import org.json.JSONObject;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
* Created by Administrator on 2016/11/9.
*/
public class NettyClientHandler extends ChannelHandlerAdapter {
    private  String TAG = "NettyClientHandler";

    private ByteBuf firstMessage;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

//        MsgData data = MsgUtil.createMsgData(MsgID.LOGIN);
//
//        LoginRequest login = new LoginRequest();
//        login.setAccount("hoodle2");
//        login.setPassword("123456");
//        data.setData(login);
//
//        ctx.writeAndFlush(Utils.getSendByteBuf(JSONObject.toJSONString(data)));
    }
    private int receiverTimes;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        ByteBuf buf = (ByteBuf) msg;
        String rev = Utils.getMessage(buf);
        Log.i(TAG,"返回信息:\n" + JsonUtils.format(rev));
        JSONObject json = new JSONObject(rev);
        int code = -1 ;
        String data = null;
        if(json.has("code")){
            code = json.getInt("code");
            data = json.getString("data");
        }
        switch (code){
            case RequestCode.LOGIN:
                UserInfo.info = JsonUtils.fromJSON(UserInfo.class, data);
                break;
            case RequestCode.BATTLE_START:
                BattleInitInfo info = JsonUtils.fromJSON(BattleInitInfo.class, data);
                EventBus.getDefault().postSticky(info);
                break;
            case RequestCode.BATTLE_DATA_BALL:
//                Log.i("cishutest","receiverTimes:" + (receiverTimes ++));
                BattleBall infos = JsonUtils.fromJSON(BattleBall.class, data);
                EventBus.getDefault().postSticky(infos);
                break;
            case RequestCode.BATTLE_DATA_STICK:
                ControlBarInfo barInfo = JsonUtils.fromJSON(ControlBarInfo.class, data);
                EventBus.getDefault().postSticky(barInfo);
                break;
            case RequestCode.BATTLE_DATA_BUMP:
                BattleBrick brick = JsonUtils.fromJSON(BattleBrick.class, data);
                EventBus.getDefault().postSticky(brick);
                break;
            default:
                Log.i(TAG,"返回错误:" + rev);
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Log.i(TAG, "返回错误:" + cause.toString());
    }
}