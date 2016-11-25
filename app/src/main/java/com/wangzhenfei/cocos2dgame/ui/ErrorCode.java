package com.wangzhenfei.cocos2dgame.ui;

/**
 * Created by wangzhenfei on 2016/11/25.
 */
public class ErrorCode {
    public static String getErrorString(String info){
        String result = "";
        switch (info){
            case "101":
                result = "服务器错误";
                break;
            case "102":
                result = "未授权";
                break;
            case "103":
                result = "账户不存在或密码错误";
                break;
            case "104":
                result = "重复登录";
                break;
            case "105":
                result = "注册失败";
                break;
            case "201":
                result = "你已经在战斗中";
                break;
            case "106":
                result = "昵称已存在";
                break;
            case "202":
//                result = "匹配失败";
                break;
        }
        return  result;
    }

}
