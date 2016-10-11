package com.mm.engine.framework.security.exception;

/**
 * Created by a on 2016/8/24.
 * TODO 添加一个属性，用于判断是否关闭User连接
 */
public class MMException extends RuntimeException {
    private static final long serialVersionUID = 5908169566019016047L;
    private String errMsg = null;

    public MMException(){
        super();
    }
    public MMException(String msg) {
        super(msg);
        this.errMsg = msg;
        printStackTrace();
    }

    public MMException(Throwable cause) {
        super(null, cause);
        cause.printStackTrace();
    }

    public void setMessage(String message){
        this.errMsg = message;
    }

    public String getMessage(){
        String tmp = this.errMsg;
        if (tmp==null && this.getCause()!=null){
            tmp = this.getCause().getMessage();
        }
        return tmp;
    }
}
