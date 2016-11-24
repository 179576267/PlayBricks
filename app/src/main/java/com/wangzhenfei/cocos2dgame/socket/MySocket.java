package com.wangzhenfei.cocos2dgame.socket;

import android.util.Log;

import com.wangzhenfei.cocos2dgame.socket.netty.NettyClientHandler;
import com.wangzhenfei.cocos2dgame.socket.netty.NettyUDPServer;
import com.wangzhenfei.cocos2dgame.tool.ByteUtils;
import com.wangzhenfei.cocos2dgame.tool.JsonUtils;
import com.wangzhenfei.cocos2dgame.tool.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * Created by wangzhenfei on 2016/11/11.
 */
public class MySocket {
    private  final String TAG = getClass().getSimpleName();

    private static MySocket mInstance;
    public static String  ip = "";
    public static int port ;
    private SocketChannel socketChannel;
    private  DatagramSocket client;
    private MySocket(){
        try {
            client = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void initSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    new NettyUDPServer().start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
                    bootstrap.remoteAddress(RequestCode.IP, RequestCode.PORT);
                    bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel)
                                throws Exception {

                            ChannelPipeline pipe = socketChannel.pipeline();
                    pipe.addLast(new LengthFieldBasedFrameDecoder(1024 * 10,0,4,0,4));
                            pipe.addLast(new LengthFieldPrepender(4, false));
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
                    ChannelFuture future = bootstrap.connect(RequestCode.IP, RequestCode.PORT).sync();

                    if (future.isSuccess()) {
                        socketChannel = (SocketChannel) future.channel();
                    }
                    future.channel().closeFuture().sync();

                }catch (InterruptedException e){
                    Log.i(TAG,e.toString());
                } finally {
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
    public void setMessage(Object s) {
        Log.i(TAG, s.toString());
        if (socketChannel != null) {
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


    private int times;
    public void setUdpMessage(Object s){
        times ++;
        Log.i("MySocket", times + "");
        if(client == null){
            try {
                client = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        try {
            client.setSoTimeout(3000);
            InetAddress address = InetAddress.getByName(ip);
            String str = JsonUtils.toJson(s);
            byte[] data = str.getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, address,8888);
            client.send(sendPacket);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setUdpMessageToServet(int id){
        if(client == null){
            try {
                client = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        try {
            client.setSoTimeout(3000);
            InetAddress address = InetAddress.getByName(RequestCode.UDP_IP);
//            byte [] data = ByteUtils.intToBytes(id);
            byte[] data = (id + "").getBytes();
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, address,RequestCode.UDP_PORT);
            client.send(sendPacket);

        }  catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //*********************************api*************************************************
}
