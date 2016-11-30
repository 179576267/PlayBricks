package com.wangzhenfei.cocos2dgame.model;

/**
 * Created by bean on 2016/11/24.
 */
public class UDPPlayerInfoResponse {

    private int id;

    private String ip;

    private int udpPort;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    @Override
    public String toString() {
        return "UDPPlayerInfoResponse{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", udpPort=" + udpPort +
                '}';
    }
}
