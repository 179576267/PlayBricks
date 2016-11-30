package com.wangzhenfei.cocos2dgame.socket.netty;

import android.util.Log;

import com.wangzhenfei.cocos2dgame.model.BallAndBarPosition;
import com.wangzhenfei.cocos2dgame.model.BattleBall;
import com.wangzhenfei.cocos2dgame.model.BattleBrick;
import com.wangzhenfei.cocos2dgame.model.BattleInitInfo;
import com.wangzhenfei.cocos2dgame.model.ControlBarInfo;
import com.wangzhenfei.cocos2dgame.model.UserInfo;
import com.wangzhenfei.cocos2dgame.config.RequestCode;
import com.wangzhenfei.cocos2dgame.tool.JsonUtils;

import org.json.JSONObject;

import java.util.List;

import de.greenrobot.event.EventBus;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * Created by Administrator on 2016/11/22.
 */
public class MessageUDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private  String TAG = "MessageUDPServerHandler";
    /*
         * channelAction
         *
         * channel 通道
         * action  活跃的
         *
         * 当客户端主动链接服务端的链接后，这个通道就是活跃的了。也就是客户端与服务端建立了通信通道并且可以传输数据
         *
         */
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.i(TAG, "channelActive");
    }

    /*
     * channelInactive
     *
     * channel 	通道
     * Inactive 不活跃的
     *
     * 当客户端主动断开服务端的链接后，这个通道就是不活跃的。也就是说客户端与服务端的关闭了通信通道并且不可以传输数据
     *
     */
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.i(TAG, "channelInactive");

    }




    /*
     * channelReadComplete
     *
     * channel  通道
     * Read     读取
     * Complete 完成
     *
     * 在通道读取完成后会在这个方法里通知，对应可以做刷新操作
     * ctx.flush()
     *
     */
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /*
     * exceptionCaught
     *
     * exception	异常
     * Caught		抓住
     *
     * 抓住异常，当发生异常的时候，可以做一些相应的处理，比如打印日志、关闭链接
     *
     */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    private int times;
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        times ++;
        Log.i("MessageUDPServerHandler", times + "");
        String rec = datagramPacket.content().toString(CharsetUtil.UTF_8);
        Log.i(TAG, "返回信息:\n" + JsonUtils.format(rec));
        JSONObject json = new JSONObject(rec);
        int code = -1 ;
        String data = null;
        if(json.has("code")){
            code = Integer.valueOf(json.getString("code"));
            data = json.getString("data");
        }
        switch (code){
            case RequestCode.BATTLE_START:
                BattleInitInfo info = JsonUtils.fromJSON(BattleInitInfo.class, data);
                EventBus.getDefault().postSticky(info);
                break;
            case RequestCode.BATTLE_DATA_BALL:
                BallAndBarPosition infos = JsonUtils.fromJSON(BallAndBarPosition.class, data);
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
                Log.i(TAG,"返回错误:" + rec);
        }
    }

}
