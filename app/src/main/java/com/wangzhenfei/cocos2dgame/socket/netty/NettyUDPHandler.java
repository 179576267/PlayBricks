package com.wangzhenfei.cocos2dgame.socket.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * Created by Administrator on 2016/11/22.
 */
public class NettyUDPHandler extends SimpleChannelInboundHandler<DatagramPacket> {


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

    public void channelInactive(ChannelHandlerContext ctx){
    }

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {

    }
}
