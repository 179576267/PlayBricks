package com.wangzhenfei.cocos2dgame.socket.netty;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.wangzhenfei.cocos2dgame.model.BattleBall;
import com.wangzhenfei.cocos2dgame.model.BattleBrick;
import com.wangzhenfei.cocos2dgame.model.BattleEndResponse;
import com.wangzhenfei.cocos2dgame.model.BattleInitInfo;
import com.wangzhenfei.cocos2dgame.model.ControlBarInfo;
import com.wangzhenfei.cocos2dgame.model.GameResult;
import com.wangzhenfei.cocos2dgame.model.PropStatusInfo;
import com.wangzhenfei.cocos2dgame.model.SaveUserInfo;
import com.wangzhenfei.cocos2dgame.model.UserInfo;
import com.wangzhenfei.cocos2dgame.socket.MsgData;
import com.wangzhenfei.cocos2dgame.socket.MySocket;
import com.wangzhenfei.cocos2dgame.config.RequestCode;
import com.wangzhenfei.cocos2dgame.tool.AppDeviceInfo;
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
    private  String TAG = getClass().getSimpleName();
    HandlerThread callHandlerThread = new HandlerThread("callHandlerThread");
    { callHandlerThread.start(); }
    protected Handler handler = new Handler(callHandlerThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            MySocket.getInstance().setUdpMessageToServer(SaveUserInfo.getInstance().getId());
            sendEmptyMessageDelayed(0, 100);
        }
    };

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.i(TAG,"channelActive");
        if(SaveUserInfo.getInstance().getId() != 0){
            MsgData<UserInfo> data = new MsgData<>();
            data.setCode(RequestCode.LOGIN);
            UserInfo userInfo = SaveUserInfo.getInstance().getUserInfo();
            userInfo.setIp(AppDeviceInfo.getIpAddress());
            data.setData(userInfo);
            MySocket.getInstance().setMessage(data);
        }
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        ByteBuf buf = (ByteBuf) msg;
        String rev = Utils.getMessage(buf);
        Log.i(TAG,"返回信息:\n" + JsonUtils.format(rev));
        JSONObject json = new JSONObject(rev);
        int code = -1 ;
        String data = "";
        String info = "";
        if(json.has("code")){
            code = json.getInt("code");
        }
        if(json.has("data")){
            data = json.getString("data");
        }
        if(json.has("info")){
            info = json.getString("info");
        }
        switch (code){
            case RequestCode.LOGIN:
                UserInfo userInfo = JsonUtils.fromJSON(UserInfo.class, data);
                SaveUserInfo.getInstance().setUserinfo(userInfo);
//                handler.sendEmptyMessage(0);
                EventBus.getDefault().post(userInfo);
                break;
            case RequestCode.STOP:
                handler.removeMessages(0);
                callHandlerThread.quit();
                break;
            case RequestCode.BATTLE_START:
                BattleInitInfo battleInitInfo = JsonUtils.fromJSON(BattleInitInfo.class, data);
                EventBus.getDefault().postSticky(battleInitInfo);
                break;
            case RequestCode.GET_UPLOAD_PATH:
                JSONObject jsonObject = new JSONObject(data);
                if(jsonObject.has("uploadPath")){
                    RequestCode.UP_LOAD_PATH = jsonObject.getString("uploadPath");
                }
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
            case RequestCode.FAILURE:
                MsgData msgData = new MsgData();
                msgData.setCode(RequestCode.FAILURE);
                msgData.setInfo(info);
                EventBus.getDefault().post(msgData);
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