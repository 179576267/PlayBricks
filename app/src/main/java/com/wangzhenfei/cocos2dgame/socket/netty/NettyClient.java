package com.wangzhenfei.cocos2dgame.socket.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


/**
* Created by Administrator on 2016/11/9.
*/
public class NettyClient {

    /*
	 * 服务器端口号
	 */
    private int port;

    /*
     * 服务器IP
     */
    private String host;

    public NettyClient(int port, String host) throws InterruptedException {
        this.port = port;
        this.host = host;
        start();
    }

    private void start() throws InterruptedException {

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.group(eventLoopGroup);
            bootstrap.remoteAddress(host, port);
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
            ChannelFuture future = bootstrap.connect(host, port).sync();

            SocketChannel socketChannel;
            if (future.isSuccess()) {
                socketChannel = (SocketChannel) future.channel();
            }
            future.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }


}
