package com.mm.engine.framework.control.scene;

/**
 * Created by a on 2016/9/14.
 *
 * 场景是一个全缓存服务对象，
 * 客户端通过场景入口进行访问，然后通过SceneService进行分配
 * 同一个场景中的玩家需要都通过场景入口连接场景所在服务器
 *
 * 一下场合适合用场景：
 * 1、实时性要求较高的，如实时战斗
 * 2、多人实时互动，如聊天室
 *
 * 场景支持AOP，但不鼓励使用，尤其是要求效率较高的场合
 */
public abstract class Scene {

    protected int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public abstract void init();
    public abstract void destroy();

    public abstract Object handle(int opcode,Object data);
}
