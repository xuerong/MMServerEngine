package com.live.battle;

/**
 * Created by a on 2016/10/13.
 * 位置信息
 */
public class PosInfo {
    private int x;
    private int y;
    private float direction;// 0-2π,复数为不动

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }
}
