package com.mm.engine.framework.control.scene;

/**
 * Created by a on 2016/9/14.
 */
public class SceneNetData {
    private int sceneId;
    private int opcode;
    private Object data;

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public int getOpcode() {
        return opcode;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
