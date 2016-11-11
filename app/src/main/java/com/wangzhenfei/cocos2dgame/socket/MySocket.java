package com.wangzhenfei.cocos2dgame.socket;

import android.util.Log;

import com.wangzhenfei.cocos2dgame.socket.netty.NettyClientHandler;
import com.wangzhenfei.cocos2dgame.tool.JsonUtils;
import com.wangzhenfei.cocos2dgame.tool.Utils;

import java.io.UnsupportedEncodingException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by wangzhenfei on 2016/11/11.
 */
public class MySocket {
    private  final String TAG = getClass().getSimpleName();

    private static MySocket mInstance;
    private SocketChannel socketChannel;
    private MySocket(){
    }

    public void initSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 初始化客户端链接
                EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

                try {

                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.channel(NioSocketChannel.class);
                    bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                    bootstrap.group(eventLoopGroup);
                    bootstrap.remoteAddress(CODE.IP, CODE.PORT);
                    bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel)
                                throws Exception {

                            ChannelPipeline pipe = socketChannel.pipeline();
//                    pipe.addLast(new LengthFieldBasedFrameDecoder(1024,0,4,0,4));
//                    pipe.addLast(new StringDecoder());

                            // 基于指定字符串【换行符，这样功能等同于LineBasedFrameDecoder】
//                    pipe.addLast(new DelimiterBasedFrameDecoder(1024, false, Delimiters.lineDelimiter()));
                            // 基于最大长度
//                    pipe.addLast(new FixedLengthFrameDecoder(4));
////                    // 编码器 String
//                    pipe.addLast(new StringEncoder());
//                    // 解码转String
//                    pipe.addLast(new StringDecoder());

                            pipe.addLast(new NettyClientHandler());
                        }
                    });
                    ChannelFuture future = bootstrap.connect(CODE.IP, CODE.PORT).sync();

                    if (future.isSuccess()) {
                        socketChannel = (SocketChannel) future.channel();
                    }
                    future.channel().closeFuture().sync();
                }catch (InterruptedException e){
                    Log.i(TAG,e.toString());
                }finally {
                    eventLoopGroup.shutdownGracefully();
                }
            }
        }).start();
    }

    public static MySocket getInstance() {
        if (mInstance == null) {
            synchronized (MySocket.class) {
                if (mInstance == null) {
                    mInstance = new MySocket();
                }
            }
        }
        return mInstance;
    }

    //*********************************api*************************************************
    public void setMessage(Object s){
        if(socketChannel != null){
            ChannelFuture future = null;
            try {
                future = socketChannel.writeAndFlush(Utils.getSendByteBuf(JsonUtils.toJson(s)));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            /** test begin **/
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future)
                        throws Exception {
                    if (future.isSuccess()) {
                        Log.i(TAG, "写入消息成功");
                    } else {
                        Log.i(TAG, "写入消息失败");
                    }
                }
            });
        }
    }

    //*********************************api*************************************************
}
