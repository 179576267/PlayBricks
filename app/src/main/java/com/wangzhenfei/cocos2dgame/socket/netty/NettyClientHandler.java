package com.wangzhenfei.cocos2dgame.socket.netty;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.wangzhenfei.cocos2dgame.model.BattleBall;
import com.wangzhenfei.cocos2dgame.model.BattleBrick;
import com.wangzhenfei.cocos2dgame.model.BattleEndResponse;
import com.wangzhenfei.cocos2dgame.model.BattleInitInfo;
import com.wangzhenfei.cocos2dgame.model.BattleNotifyResponse;
import com.wangzhenfei.cocos2dgame.model.ControlBarInfo;
import com.wangzhenfei.cocos2dgame.model.GameResult;
import com.wangzhenfei.cocos2dgame.model.Location;
import com.wangzhenfei.cocos2dgame.model.PropStatusInfo;
import com.wangzhenfei.cocos2dgame.model.UserInfo;
import com.wangzhenfei.cocos2dgame.socket.MySocket;
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
    private  String TAG = getClass().getSimpleName();
    HandlerThread callHandlerThread = new HandlerThread("callHandlerThread");
    { callHandlerThread.start(); }
    protected Handler handler = new Handler(callHandlerThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            MySocket.getInstance().setUdpMessageToServet(UserInfo.info.getId());
            sendEmptyMessageDelayed(0, 100);
        }
    };

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.i(TAG,"channelActive");
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        ByteBuf buf = (ByteBuf) msg;
        String rev = Utils.getMessage(buf);
        Log.i(TAG,"返回信息:\n" + JsonUtils.format(rev));
        JSONObject json = new JSONObject(rev);
        int code = -1 ;
        String data = "";
        if(json.has("code")){
            code = json.getInt("code");
        }
        if(json.has("data")){
            data = json.getString("data");
        }
        switch (code){
            case RequestCode.LOGIN:
                UserInfo.info = JsonUtils.fromJSON(UserInfo.class, data);
                handler.sendEmptyMessage(0);
                break;
            case RequestCode.STOP:
                handler.removeMessages(0);
                callHandlerThread.quit();
                break;
            case RequestCode.BATTLE_START:
                BattleInitInfo info = JsonUtils.fromJSON(BattleInitInfo.class, data);
                EventBus.getDefault().postSticky(info);
                break;
            case RequestCode.BATTLE_DATA_BALL:
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
            case RequestCode.BATTLE_DATA_GET_PROP:
                PropStatusInfo statusInfo = JsonUtils.fromJSON(PropStatusInfo.class, data);
                EventBus.getDefault().post(statusInfo);
                break;
            case RequestCode.BATTLE_END:
                BattleEndResponse endResponse = JsonUtils.fromJSON(BattleEndResponse.class, data);
                GameResult result = new GameResult(endResponse.getWinId());
                EventBus.getDefault().post(result);
                break;
            default:
                Log.i(TAG,"未知消息号码:" + rev);
        }

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Log.i(TAG,cause.toString());
    }
}