package com.wangzhenfei.cocos2dgame;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * 客户端A 
 * @author eswyao@126.com
 *
 */
public class UdpDemo {
    public static ExecutorService pool = Executors.newCachedThreadPool();
    public static void main(String[] args) {
        startClientA();

    }


    public static void startClientA() {
        try {

            // 向server发起请求
            SocketAddress target = new InetSocketAddress("182.254.247.160", 7777);
            DatagramSocket client = new DatagramSocket();
            String message = "I am ClientA 我是A";
            byte[] sendbuf = message.getBytes();
            DatagramPacket pack = new DatagramPacket(sendbuf, sendbuf.length,target);
            client.send(pack);
            receive(client);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 接收请求 （接收请求的回复,可能不是server回复的，有可能来自UPDClientB的请求内）
     * @param client
     */
    private static void receive(DatagramSocket client) {
        final DatagramSocket clientf = client;
        pool.execute(new Runnable(){
            @Override
            public void run() {
                try {
                    for (;;) {

                        byte[] buf = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        clientf.receive(packet);
                        String receiveMessage = new String(packet.getData(), 0, packet.getLength());
                        Log.i("UdpDemo", receiveMessage);
                        Log.i("UdpDemo","3. A接收数据1：" + packet.getAddress() + ":" + packet.getPort() + "内容：" + new String(packet.getData()));
                        Log.i("UdpDemo", "3. A接收数据2： " + receiveMessage);
						/*int port = packet.getPort();
						InetAddress address = packet.getAddress();*/
                        String reportMessage = "我是A，这是我发送的测试数据！";

                        if(receiveMessage.startsWith("<S>")){
                            String[] rmArr = receiveMessage.split(",");
                            int port = new Integer(rmArr[1].split(":")[1]).intValue();
                            String ip = rmArr[0].split(":")[1];
                            Log.i("UdpDemo", "==============3. A接收数据3：  " + ip + ":" + port);
                            sendMessaage(reportMessage, port, InetAddress.getByName(ip), clientf);
                        }
						/*System.out.print("A:请输入发送内容： ");
						Reader in = new InputStreamReader(System.in);
						BufferedReader br = new BufferedReader(in);
						String reportMessage = br.readLine();
						System.out.println("A:输入内容为：" + reportMessage);*/
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    /**
     * 回复内容  （获取接收到请问内容后并取到地址与端口,然后用获取到地址与端口回复内容）
     * @param reportMessage
     * @param port
     * @param address
     * @param client
     */
    private static void sendMessaage(String reportMessage, int port,
                                     InetAddress address, DatagramSocket client) {
        try {
            byte[] sendBuf = reportMessage.getBytes("UTF-8");
            DatagramPacket sendPacket = new DatagramPacket(sendBuf,
                    sendBuf.length, address, port);
            client.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
