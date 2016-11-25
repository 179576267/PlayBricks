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
public class UDPClientB {
	
	public static ExecutorService pool = Executors.newCachedThreadPool();
	public static boolean isNat = false;
	public static void main(String[] args) {
		startClientB();
	}

	public static void startClientB() {
		try {

			Log.i("UDPClientB", "1. 客户端B启动成功！");
			// 向server发起请求
			SocketAddress target = new InetSocketAddress("182.254.247.160", 7777);
			DatagramSocket client = new DatagramSocket();
			String message = "WanghShengYao I am ClientB, send test request!";
			byte[] sendbuf = message.getBytes();
			DatagramPacket pack = new DatagramPacket(sendbuf, sendbuf.length,
					target);
			Log.i("UDPClientB", "2. 发送数据 目标：" + pack.getAddress() + ":" + pack.getPort() + "内容：" + new String(pack.getData()));
			client.send(pack); 
			// 接收server的回复内容
			byte[] buf = new byte[1024];
			DatagramPacket recpack = new DatagramPacket(buf, buf.length);
			client.receive(recpack);
			Log.i("UDPClientB", "3. 接收server的回复内容：" + recpack.getAddress() + ":" + recpack.getPort() + "内容：" + new String(recpack.getData()));

			Log.i("UDPClientB", "4. 处理server回复的内容，然后向内容中的地址与端口发起请求（打洞）");
			String receiveMessage = new String(recpack.getData(), 0, recpack.getLength());
			String[] params = receiveMessage.split(",");
			String host = params[0].substring(5);
			String port = params[1].substring(5);
			sendMessage(host, port, client);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 向UPDClientA发起请求(在NAT上打孔)
	 */
	private static void sendMessage(String host, String port,
			DatagramSocket client) {
		try {
			Log.i("UDPClientB", "5. 向UPDClientA发起请求(在NAT上打孔):" + host + "  " + port);
			SocketAddress target = new InetSocketAddress(host, Integer.parseInt(port));
			while(!isNat){
				String message = "I'm B, to initiate A request, can test data to A!";
				byte[] sendbuf = message.getBytes();
				DatagramPacket pack = new DatagramPacket(sendbuf,sendbuf.length, target);
				client.send(pack);
				Log.i("UDPClientB", "6. 发送数据（向A发送）：" + pack.getAddress() + ":" + pack.getPort() + "内容：" + new String(pack.getData()));
				// 等待接收UDPClientA回复的内容
				receive(client);
				Thread.sleep(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**收到UDPClientA的回复内容，穿透已完成*/
	private static void receive(DatagramSocket client) {
		final DatagramSocket clientF = client;
		pool.execute(new Runnable(){
			@Override
			public void run() {
				try {

					for (;;) {
						// 将接收到的内容打印
						byte[] buf = new byte[1024];
						DatagramPacket recpack = new DatagramPacket(buf, buf.length);
						clientF.receive(recpack);
						System.out.println("recpack.getLength():" + recpack.getLength() +"---:---"+ isNat);
						if(recpack.getLength() >1){
							System.out.println("------------------------------");
							Log.i("UDPClientB", "7. 收到UDPClientA的回复内容，穿透已完成!");
							isNat = true;
						}
						String receiveMessage = new String(recpack.getData(), "UTF-8");
						Log.i("UDPClientB", "8. 接收A发来的信息：" + recpack.getAddress() + ":" + recpack.getPort() + "内容：" + new String(recpack.getData(), "UTF-8"));
						Log.i("UDPClientB", "8. 接收A发来的信息2：" + receiveMessage);
						
						// 记得重新收地址与端口，然后在以新地址发送内容到UPDClientA,就这样互发就可以了。
						Log.i("UDPClientB", "9. 记得重新收地址与端口，然后在以新地址发送内容到UPDClientA,就这样互发就可以了。");
						int port = recpack.getPort();
						InetAddress address = recpack.getAddress();
						
						String reportMessage = "我是B,向A发送数据，测试成功性！";
						// 发送消息
					/*	System.out.print("B:请输入发送内容： ");
						Reader in = new InputStreamReader(System.in);
						BufferedReader br = new BufferedReader(in);
						String reportMessage = br.readLine();
						System.out.println("B:输入内容为：" + reportMessage);*/
						sendMessage(reportMessage, port, address, clientF);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		
	}

	private static void sendMessage(String reportMessage, int port,
			InetAddress address, DatagramSocket client) {
		try {
			Log.i("UDPClientB", "10. 向A发送数据");
			byte[] sendBuf = reportMessage.getBytes("UTF-8");
			DatagramPacket sendPacket = new DatagramPacket(sendBuf,
					sendBuf.length, address, port);
			client.send(sendPacket);
			Log.i("UDPClientB", "11 . 向A发送信息：" + sendPacket.getAddress() + ":" + sendPacket.getPort() + "内容：" + new String(sendPacket.getData(), "gbk"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
