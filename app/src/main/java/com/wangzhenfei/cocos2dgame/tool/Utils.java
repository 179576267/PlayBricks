package com.wangzhenfei.cocos2dgame.tool;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Created by wangzhenfei on 2016/11/10.
 */
public class Utils {

    public static boolean isCollsionWithRect(float x1, float y1, float w1, float h1, float x2, float
            y2, float w2, float h2) {
        //当矩形1 位于矩形2 的右侧
        if (x1 > x2 + w2) {
            return false;
            //当矩形1 位于矩形2 的左侧
        } else if (x1 + w1 < x2) {
            return false;
            //当矩形1 位于矩形2 的下方
        } else if (y1 > y2 + h2) {
            return false;
            //当矩形1 位于矩形2 的上方
        } else if (y1 + h1 < y2) {
            return false;
        }
        return true;
    }

    /**
     * 将字节数组转换为String
     *
     * @param b
     *            byte[]
     * @return String
     */
    public static String bytesToString(byte[] b) {
        StringBuffer result = new StringBuffer("");
        int length = b.length;
        for (int i = 0; i < length; i++) {
            result.append((char) (b[i] & 0xff));
        }
        return result.toString();
    }

    /*
	 * 从ByteBuf中获取信息 使用UTF-8编码返回
	 */
    public static String getMessage(ByteBuf buf) {
//        int len = buf.readInt();
//        System.out.println("读取数据长度:"+len);
        byte[] con = new byte[buf.readableBytes()];
        buf.readBytes(con);
        try {
            return new String(con, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ByteBuf getSendByteBuf(String message) throws UnsupportedEncodingException {

        byte[] req = message.getBytes("UTF-8");
        ByteBuf pingMessage = Unpooled.buffer();
        pingMessage.writeBytes(req);

        return pingMessage;
    }
}
