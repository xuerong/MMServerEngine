package com.live.battle;

import javafx.geometry.Pos;

/**
 * Created by a on 2016/10/13.
 * 对它数据的修改都放在本函数中，以便加锁
 */
public class BattleUser {
    public static int initBlood = 2000;
    public static int initAttackValue = 10;
    public static int initSpeed = 200;
    public static int initAttackDistance = 100;
    public static int initLostBloodValuePer = 10;
    public static int initSuckBlood = 20;

    private boolean isRobot;
    private String accountId;
    //
    private int x;
    private int y;
    private float direction;// 0-2π,复数为不动

    private long time; // 本次设置位置的时间，用于判断当前位置
    //
    private int blood;
    private int attackValue;
    private float speed; // s
    private int attackDistance;
    private int lostBloodValuePer; // 每次掉的血量
    private int suckBlood; // 每次攻击吸收的血量
    //


    public BattleUser(boolean isRobot,String accountId){
        this.isRobot = isRobot;
        this.accountId = accountId;
    }
    public void start(int x,int y,long time){
        this.x = x;
        this.y = y;
        this.time = time;
        this.direction = -1;
        this.blood = initBlood;
        this.attackValue = initAttackValue;
        this.speed = initSpeed;
        this.attackDistance = initAttackDistance;
        this.lostBloodValuePer = initLostBloodValuePer;
        this.suckBlood = initSuckBlood;
    }
    /**
     * 更新位置，并返回位置
     * @return
     */
    public PosInfo getPosInfo() {
        if(direction<0){
            PosInfo posInfo = new PosInfo();
            posInfo.setX(x);
            posInfo.setY(y);
            posInfo.setDirection(direction);
            return posInfo;
        }

        long curTime = System.currentTimeMillis();
        int dis = (int)((curTime-time)*speed/1000);
        if(dis>0){
            int _x=x+(int)(dis*Math.cos(direction));
            int _y=y+(int)(dis*Math.sin(direction));

            x=_x<0?0:(_x>BattleRoom.roomSizeWidth?BattleRoom.roomSizeWidth:_x);
            y=_y<0?0:(_y>BattleRoom.roomSizeHeight?BattleRoom.roomSizeHeight:_y);

            this.time = curTime;
        }
        PosInfo posInfo = new PosInfo();
        posInfo.setX(x);
        posInfo.setY(y);
        posInfo.setDirection(direction);
        return posInfo;
    }

    public PosInfo updateDirection(float direction) {
        PosInfo posInfo = getPosInfo();
        this.direction=direction;
        this.time = System.currentTimeMillis();
        posInfo.setDirection(direction);
        return posInfo;
    }

    public boolean isDie(){
        return blood<=0;
    }

    public int beAttack(int attackValue){
        int blood = this.blood-attackValue;
        this.blood = blood<0?0:blood;
        return this.blood;
    }

    public int lostBlood(){
        int blood = this.blood-lostBloodValuePer;
        this.blood = blood<0?0:blood;
        return this.blood;
    }

    public int suckBlood(){
        this.blood += suckBlood;
        return this.blood;
    }

    public String getAccountId() {
        return accountId;
    }

    public boolean isRobot() {
        return isRobot;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public int getBlood() {
        return blood;
    }

    public void setBlood(int blood) {
        this.blood = blood;
    }

    public int getAttackValue() {
        return attackValue;
    }

    public void setAttackValue(int attackValue) {
        this.attackValue = attackValue;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getAttackDistance() {
        return attackDistance;
    }

    public void setAttackDistance(int attackDistance) {
        this.attackDistance = attackDistance;
    }
}
