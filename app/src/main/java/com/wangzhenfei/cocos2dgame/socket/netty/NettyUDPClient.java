package com.wangzhenfei.cocos2dgame.socket.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Administrator on 2016/11/22.
 */
public class NettyUDPClient {
    public void start(){
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(group).channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST,true)
                .handler(new NettyUDPHandler());
    }

    public void send(){
        try {
            DatagramSocket client = new DatagramSocket();
            client.setSoTimeout(3000);
            InetAddress address = InetAddress.getByName("192.168.2.169");

            String str = "测试";
            byte[] data = str.getBytes("UTF-8");
            System.out.println("发送:");
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, address,2818);
            client.send(sendPacket);

            byte[] buffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            client.receive(receivePacket);
            int len = receivePacket.getLength();
            byte[] receiveBuf = receivePacket.getData();
            System.out.println("收到:"+new String(receiveBuf,"UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        new NettyUDPClient().send();
    }
}
