package com.wangzhenfei.cocos2dgame.socket.netty;

import android.util.Log;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * Created by Administrator on 2016/11/22.
 */
public class NettyUDPServer {

    private EventLoopGroup group;

    private Bootstrap bootstrap;

    public void start() throws Exception
    {
        group = new NioEventLoopGroup();

        bootstrap = new Bootstrap();

        try {
            bootstrap.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST,true)
                    .handler(new MessageUDPServerHandler());
            ChannelFuture future = bootstrap.bind(8888).sync();
            if (future.isSuccess()) {
                Log.i("TAG","CHENGG");
            }else {
                Log.i("TAG","CHENGG");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop()
    {
        if(group != null){
            group.shutdownGracefully();
        }
    }


    public static void main(String[] args) {
        try {
            new NettyUDPServer().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
