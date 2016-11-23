package com.wangzhenfei.cocos2dgame.model;

/**
 * Created by wangzhenfei on 2016/11/23.
 */
public class GameResult {
    private int winnerId;
    private boolean win;


    public GameResult(int winnerId) {
        this.winnerId = winnerId;
        if(UserInfo.info != null){
            if(UserInfo.info.getId() == winnerId){
                win = true;
            }else {
                win = false;
            }
        }
    }

    public int getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(int winnerId) {
        this.winnerId = winnerId;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }
}
