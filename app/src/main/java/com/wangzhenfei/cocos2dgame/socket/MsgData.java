package com.wangzhenfei.cocos2dgame.socket;

import com.wangzhenfei.cocos2dgame.tool.JsonUtils;

/**
 * Created by bean on 2016/11/10.
 */
public class MsgData<T> implements Cloneable{

    private int code;

    private String info;

    private T data;

    public MsgData(){}


    public MsgData(int code){
        this.code = code;
    }

    public MsgData(int code, T data){
        this.code = code;
        this.data = data;
    }


    public MsgData clone(){
        try {
            return (MsgData) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData(Class cls){
        return JsonUtils.fromJSON(cls, data.toString());
    }

    public T getData(){
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }


    @Override
    public String toString() {
        return "MsgData{" +
                "code=" + code +
                ", info='" + info + '\'' +
                ", data=" + data +
                '}';
    }
}
