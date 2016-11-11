package com.wangzhenfei.cocos2dgame.socket.netty;


import android.util.Log;

import com.wangzhenfei.cocos2dgame.model.BattleInitInfo;
import com.wangzhenfei.cocos2dgame.model.UserInfo;
import com.wangzhenfei.cocos2dgame.socket.CODE;
import com.wangzhenfei.cocos2dgame.socket.MsgData;
import com.wangzhenfei.cocos2dgame.tool.JsonUtils;
import com.wangzhenfei.cocos2dgame.tool.Utils;

import org.json.JSONObject;

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
            data = json.getJSONObject("data").toString();
        }
        switch (code){
            case CODE.LOGIN:
                UserInfo.info = JsonUtils.fromJSON(UserInfo.class, data);
                break;
            case CODE.BATTLE_START:
                BattleInitInfo info = JsonUtils.fromJSON(BattleInitInfo.class, data);
                EventBus.getDefault().postSticky(info);
                break;
            default:
                Log.i(TAG,"返回错误:" + rev);
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}