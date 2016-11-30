package com.wangzhenfei.cocos2dgame.model;


/**
 * Created by bean on 2016/11/14.
 */
public class BattleBall {

    private int ballId;

    private Location position;

    public BattleBall(int id, Location location) {
        this.ballId = id;
        this.position = location;
    }

    public int getId() {
        return ballId;
    }

    public void setId(int id) {
        this.ballId = id;
    }

    public Location getLocation() {
        return position;
    }

    public void setLocation(Location location) {
        this.position = location;
    }

    @Override
    public String toString() {
        return "BattleBall{" +
                "id=" + ballId +
                ", location=" + position +
                '}';
    }
}
