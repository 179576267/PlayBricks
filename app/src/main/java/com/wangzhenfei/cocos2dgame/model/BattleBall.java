package com.wangzhenfei.cocos2dgame.model;


/**
 * Created by bean on 2016/11/14.
 */
public class BattleBall {

    private int id;

    private Location location;

    public BattleBall(int id, Location location) {
        this.id = id;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
