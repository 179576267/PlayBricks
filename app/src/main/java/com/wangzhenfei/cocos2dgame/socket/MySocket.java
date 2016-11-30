package com.wangzhenfei.cocos2dgame.socket;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.wangzhenfei.cocos2dgame.config.RequestCode;
import com.wangzhenfei.cocos2dgame.model.BallAndBarPosition;
import com.wangzhenfei.cocos2dgame.model.BattleBall;
import com.wangzhenfei.cocos2dgame.model.BattleBrick;
import com.wangzhenfei.cocos2dgame.model.BattleInitInfo;
import com.wangzhenfei.cocos2dgame.model.ControlBarInfo;
import com.wangzhenfei.cocos2dgame.model.SaveUserInfo;
import com.wangzhenfei.cocos2dgame.model.UDPPlayerInfoResponse;
import com.wangzhenfei.cocos2dgame.socket.netty.NettyClientHandler;
import com.wangzhenfei.cocos2dgame.socket.netty.NettyUDPServer;
import com.wangzhenfei.cocos2dgame.tool.JsonUtils;
import com.wangzhenfei.cocos2dgame.tool.Utils;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;
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
import io.netty.util.CharsetUtil;

/**
 * Created by wangzhenfei on 2016/11/11.
 */
public class MySocket {
    private  final String TAG = getClass().getSimpleName();
    public static ExecutorService pool = Executors.newCachedThreadPool();
    private static MySocket mInstance;
    public static String  ip = RequestCode.UDP_IP;
    public static int port = RequestCode.UDP_PORT;
    private SocketChannel socketChannel;
    private  DatagramSocket client;
    static {
        ip = RequestCode.UDP_IP;
        port = RequestCode.UDP_PORT;
    }
    private String oldip = "";
    HandlerThread callHandlerThread = new HandlerThread("callHandlerThread");
    { callHandlerThread.start(); }
    protected Handler handler = new Handler(callHandlerThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if(!oldip.equals(ip)){ // ip切换需要重新创建reciver
                hasReciver = false;
                oldip = ip;
            }
                SocketAddress target = new InetSocketAddress(ip, port);
                if(client == null){
                    try {
                        client = new DatagramSocket();
//                        receive();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    MsgData msgData = (MsgData) msg.obj;
                    msgData.setInfo(SaveUserInfo.getInstance().getId() + "");
                    String str = JsonUtils.toJson(msgData);
                    byte[] data = str.getBytes("UTF-8");
                    DatagramPacket pack = new DatagramPacket(data, data.length,target);
                    client.send(pack);
                    receive();
//                    client.send(new DatagramPacket(data, data.length,new InetSocketAddress("192.168.2.121", 28181)));
                    Log.i(TAG, "udp发送成功：" + "ip:" + ip + "port:" + port + str);
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    };


    private MySocket(){
    }

    public void initSocket() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    new NettyUDPServer().start();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
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


    public void setUdpMessageToClient(Object s){
        setUdpMessageToServer(s);
    }


    public void setUdpMessageToServer(Object o){
        Message message = new Message();
        message.what = 0;
        message.obj = o;
        handler.sendMessage(message);
    }



    /**
     * 接收请求
     */
    private boolean hasReciver;
    public  void receive() {
        if(hasReciver){
            return;
        }
        hasReciver = true;
        pool.execute(new Runnable(){
            @Override
            public void run() {
                try {
                    for (;;) {
                        byte[] buf = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        client.receive(packet);
                        String rec = new String(packet.getData(), 0, packet.getLength()).trim();
                        Log.i("MySocket", "返回信息:\n" + JsonUtils.format(rec));
                        JSONObject json = new JSONObject(rec);
                        int code = -1 ;
                        String data = null;
                        if(json.has("code")){
                            code = json.getInt("code");
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
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    //*********************************api*************************************************
}
